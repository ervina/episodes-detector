import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.episodeMining.algorithm.ShellCommand;
import cc.episodeMining.data.EventStreamGenerator;
import cc.episodeMining.data.EventsFilter;
import cc.episodeMining.data.SequenceGenerator;
import cc.episodeMining.mudetect.EpisodesToPatternTransformer;
import cc.episodeMining.mudetect.TraceToAUGTransformer;
import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.io.FileReader;
import cc.kave.episodes.mining.patterns.ParallelPatterns;
import cc.kave.episodes.mining.patterns.PartialPatterns;
import cc.kave.episodes.mining.patterns.PatternFilter;
import cc.kave.episodes.mining.patterns.SequentialPatterns;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.EpisodeType;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.recommenders.io.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.tu_darmstadt.stg.mubench.DefaultFilterAndRankingStrategy;
import de.tu_darmstadt.stg.mubench.ViolationUtils;
import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.MissingElementViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.ranking.ConstantNodeWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.OverlapWithoutEdgesToMissingNodesWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.PatternSupportWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.ProductWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.ViolationSupportWeightFunction;
import de.tu_darmstadt.stg.mudetect.ranking.WeightRankingStrategy;
import edu.iastate.cs.mudetect.mining.MinPatternActionsModel;

public class runner {

	private static FileReader reader = new FileReader();

	private static final int FREQUENCY = 2;
	private static final double ENTROPY = 0.001;
	private static final int BREAKER = 5000;

	private static final int THRESHFREQ = 20;
	private static final double THRESHENT = 0.5;

	public static void main(String[] args) throws Exception {
		new MuBenchRunner().withMineAndDetectStrategy(new Strategy()).run(args);

		Logger.log("done");
	}

	static class Strategy implements DetectionStrategy {

		public DetectorOutput detectViolations(DetectorArgs args,
				DetectorOutput.Builder output) throws Exception {
			List<Triplet<String, Event, List<Event>>> srcMapper = parser(
					args.getTargetSrcPaths(), args.getDependencyClassPath());

			ShellCommand command = new ShellCommand(new File(getEventsPath()),
					new File(getAlgorithmPath()));
			command.execute(FREQUENCY, ENTROPY, BREAKER);

			EpisodeParser episodeParser = new EpisodeParser(new File(
					getEventsPath()), reader);
			Map<Integer, Set<Episode>> episodes = episodeParser
					.parser(FREQUENCY);
			System.out.println("Maximal episode size " + episodes.size());
			System.out.println("Number of episodes: "
					+ getSetPatterns(episodes).size());

			PatternFilter patternFilter = new PatternFilter(
					new PartialPatterns(), new SequentialPatterns(),
					new ParallelPatterns());
			Map<Integer, Set<Episode>> patterns = patternFilter.filter(
					EpisodeType.GENERAL, episodes, THRESHFREQ, THRESHENT);
			Set<Episode> setOfPatterns = getSetPatterns(patterns);
			System.out.println("\nMaximal pattern size "
					+ (patterns.size() + 1));
			System.out.println("Number of patterns: " + setOfPatterns.size());

			EventStreamIo esio = new EventStreamIo(new File(getEventsPath()));
			List<Event> mapping = esio.readMapping(FREQUENCY);
			Set<APIUsagePattern> augPatterns = new EpisodesToPatternTransformer()
					.transform(setOfPatterns, mapping);

			// Collection<APIUsageExample> targets = loadTargetAUGs(
			// args.getTargetSrcPaths(), args.getDependencyClassPath());
			List<Triplet<String, Event, APIUsageExample>> targets = loadTargetAUGs(
					args.getTargetSrcPaths(), args.getDependencyClassPath());
			MuDetect detection = new MuDetect(
					new MinPatternActionsModel(() -> augPatterns, 2),
					new AlternativeMappingsOverlapsFinder(
							new AlternativeMappingsOverlapsFinder.Config()),
					new MissingElementViolationPredicate(),
					new DefaultFilterAndRankingStrategy(
							new WeightRankingStrategy(
									new ProductWeightFunction(
											new OverlapWithoutEdgesToMissingNodesWeightFunction(
													new ConstantNodeWeightFunction()),
											new PatternSupportWeightFunction(),
											new ViolationSupportWeightFunction()))));
			List<Violation> violations = detection.findViolations(targets);

			return output.withFindings(violations, ViolationUtils::toFinding);
		}

		private Set<Episode> getSetPatterns(Map<Integer, Set<Episode>> patterns) {
			Set<Episode> output = Sets.newLinkedHashSet();

			for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
				output.addAll(entry.getValue());
			}
			return output;
		}

		private List<Triplet<String, Event, APIUsageExample>> loadTargetAUGs(
				String[] srcPaths, String[] classpath) throws IOException {
			// TODO it is weird that building traces returns a List<Event>,
			// isn't a List<Event> _one_ trace and the type
			// should be Collection<List<Events>>? I'm assuming this for the
			// subsequent implementation. -Sven
			List<Triplet<String, Event, List<Event>>> traces = parser(srcPaths,
					classpath);

			// Collection<APIUsageExample> targets = new ArrayList<>();
			List<Triplet<String, Event, APIUsageExample>> targets = new ArrayList<>();
			for (Triplet<String, Event, List<Event>> trace : traces) {
				// targets.add(TraceToAUGTransformer.transform(trace));
				targets.add(new Triplet<String, Event, APIUsageExample>(trace
						.getFirst(), trace.getSecond(), TraceToAUGTransformer
						.transform(trace.getThird())));
			}
			return targets;
		}

		private List<Triplet<String, Event, List<Event>>> parser(
				String[] srcPaths, String[] classpaths) throws IOException {
			List<Event> sequences = buildMethodTraces(srcPaths, classpaths);
			System.out.println("Number of all events: " + sequences.size());

			EventsFilter ef = new EventsFilter();
			List<Event> frequentEvents = ef.frequent(sequences, FREQUENCY);
			System.out.println("Number of frequent events: "
					+ frequentEvents.size());

			EventStreamGenerator esg = new EventStreamGenerator(new File(
					getEventsPath()));
			List<Triplet<String, Event, List<Event>>> srcMapper = esg
					.createSrcMapper(frequentEvents, FREQUENCY);
			getNoFiles(srcMapper);
			esg.eventStream(srcMapper, FREQUENCY);

			return srcMapper;
		}

		private void getNoFiles(
				List<Triplet<String, Event, List<Event>>> srcMapper) {
			Set<String> noSrc = Sets.newLinkedHashSet();
			List<Event> md = Lists.newLinkedList();

			for (Triplet<String, Event, List<Event>> triplet : srcMapper) {
				noSrc.add(triplet.getFirst());
				md.add(triplet.getSecond());
			}
			System.out.println("Number of classes: " + noSrc.size());
			System.out.println("Number of methods: " + md.size());
		}

		private List<Event> buildMethodTraces(String[] srcPaths,
				String[] classpaths) {
			List<Event> sequences = Lists.newLinkedList();
			for (String srcPath : srcPaths) {
				SequenceGenerator generator = new SequenceGenerator();
				sequences.addAll(generator.generateMethodTraces(new File(
						srcPath), classpaths));
			}
			return sequences;
		}

		private String getEventsPath() {
			String pathName = "/Users/ervinacergani/Documents/MisuseDetector/events/";
			return pathName;
		}

		private String getAlgorithmPath() {
			String path = "/Users/ervinacergani/Documents/EpisodeMining/n-graph-miner/";
			return path;
		}
	}
}
