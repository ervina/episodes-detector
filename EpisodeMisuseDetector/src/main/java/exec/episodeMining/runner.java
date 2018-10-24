package exec.episodeMining;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.episodeMining.data.EventStreamGenerator;
import cc.episodeMining.data.EventsFilter;
import cc.episodeMining.data.SequenceGenerator;
import cc.episodeMining.mubench.model.EventGenerator;
import cc.episodeMining.mudetect.EpisodesToPatternTransformer;
import cc.episodeMining.mudetect.TraceToAUGTransformer;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.io.FileReader;
import cc.kave.episodes.mining.patterns.ParallelPatterns;
import cc.kave.episodes.mining.patterns.PartialPatterns;
import cc.kave.episodes.mining.patterns.PatternFilter;
import cc.kave.episodes.mining.patterns.SequentialPatterns;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.EpisodeType;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;
import cc.recommenders.io.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_darmstadt.stg.mubench.DataEdgeTypePriorityOrder;
import de.tu_darmstadt.stg.mubench.DefaultFilterAndRankingStrategy;
import de.tu_darmstadt.stg.mubench.ViolationUtils;
import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.MissingElementViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
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

	private static final int FREQUENCY = 10;
	private static final double ENTROPY = 0.6;
	private static final int BREAKER = 5000;

	private static final int THRESHFREQ = 10;
	private static final double THRESHENT = 0.9;
	private static final double THRESHSUBP = 1.0;

	public static void main(String[] args) throws Exception {
		new MuBenchRunner().withMineAndDetectStrategy(new Strategy()).run(args);

		Logger.log("done");
	}

	static class Strategy implements DetectionStrategy {

		public DetectorOutput detectViolations(DetectorArgs args,
				DetectorOutput.Builder output) throws Exception {
			// Map<String, List<Tuple<Event, List<Event>>>> stream = parser(
			// args.getTargetSrcPaths(), args.getDependencyClassPath());
			// specific per project:
			// args.getAdditionalOutputPath()

			// ShellCommand command = new ShellCommand(new
			// File(getEventsPath()),
			// new File(getAlgorithmPath()));
			// command.execute(FREQUENCY, ENTROPY, BREAKER);

			EpisodeParser episodeParser = new EpisodeParser(new File(
					getEventsPath()), reader);
			Map<Integer, Set<Episode>> episodes = episodeParser
					.parser(FREQUENCY);
			System.out.println("Maximal episode size " + episodes.size());
			System.out.println("Number of episodes: " + counter(episodes));

			// episodes.remove(7);
			episodes.remove(6);
			// episodes.remove(5);

			PatternFilter patternFilter = new PatternFilter(
					new PartialPatterns(), new SequentialPatterns(),
					new ParallelPatterns());
			Map<Integer, Set<Episode>> superepisodes = patternFilter
					.subEpisodes(episodes, THRESHSUBP);
			System.out
					.println("Number of episodes after filtering subepisodes: "
							+ counter(superepisodes));
			Map<Integer, Set<Episode>> patterns = patternFilter.filter(
					EpisodeType.GENERAL, superepisodes, THRESHFREQ, THRESHENT);
			System.out.println("Number of patterns: " + counter(patterns));

			// PatternStatistics statistics = new PatternStatistics();
			// statistics.compute(patterns);
			// statistics.DiscNodes(patterns);

			EventStreamIo esio = new EventStreamIo(new File(getEventsPath()));
			List<Event> mapping = esio.readMapping(FREQUENCY);

			EventStreamGenerator esg = new EventStreamGenerator();
			Map<String, List<Tuple<Event, List<Event>>>> stream = esg
					.readStreamObject(new File(getEventsPath()), FREQUENCY);

			// Map<Integer, Set<Episode>> patternFound =
			// containsSubpattern(repr,
			// mapping);
			// debugStream();

			Set<APIUsagePattern> augPatterns = new EpisodesToPatternTransformer()
					.transform(patterns, mapping);

			// checkPatterns(episodes, mapping);

			System.out.println("Number of patterns of APIUsage transformer: "
					+ augPatterns.size());

			Collection<APIUsageExample> targets = loadTargetAUGs(stream);
			AUGLabelProvider labelProvider = new BaseAUGLabelProvider();
			MuDetect detection = new MuDetect(
					new MinPatternActionsModel(() -> augPatterns, 2),
					new AlternativeMappingsOverlapsFinder(
							new AlternativeMappingsOverlapsFinder.Config() {
								{
									isStartNode = super.isStartNode
											.and(new VeryUnspecificReceiverTypePredicate()
													.negate());
									nodeMatcher = new EquallyLabelledNodeMatcher(
											labelProvider);
									edgeMatcher = new EquallyLabelledEdgeMatcher(
											labelProvider);
									edgeOrder = new DataEdgeTypePriorityOrder();
									extensionEdgeTypes = new HashSet<>(Arrays
											.asList(OrderEdge.class));
								}
							}),
					new MissingElementViolationPredicate(),
					// new AlternativeRankingAndFilterStrategy()
					new DefaultFilterAndRankingStrategy(
							new WeightRankingStrategy(
									new ProductWeightFunction(
											new OverlapWithoutEdgesToMissingNodesWeightFunction(
													new ConstantNodeWeightFunction()),
											new PatternSupportWeightFunction(),
											new ViolationSupportWeightFunction()))));
			List<Violation> violations = detection.findViolations(targets);
			// List<Violation> violations = Lists.newLinkedList();

			// output.withRunInfo(key, value)
			return output.withFindings(violations, ViolationUtils::toFinding);
		}

		private void debugStream() {
			FileReader reader = new FileReader();
			List<String> stream = reader.readFile(new File(getEventsPath()
					+ "/freq" + FREQUENCY + "/stream.txt"));
			for (String line : stream) {
				String[] idx = line.split(",");
				if (idx[0].equals("15") || idx[0].equals("21")) {
					System.out.println(line);
				}
			}
		}

		private void checkPatterns(Map<Integer, Set<Episode>> patterns,
				List<Event> mapping) {
			System.out.println("Method: "
					+ mapping.get(2).getMethod().getIdentifier());
		}

		private int counter(Map<Integer, Set<Episode>> patterns) {
			int counter = 0;
			for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
				counter += entry.getValue().size();
			}
			return counter;
		}

		private Collection<APIUsageExample> loadTargetAUGs(
				Map<String, List<Tuple<Event, List<Event>>>> traces)
				throws IOException {

			Collection<APIUsageExample> targets = new ArrayList<>();
			for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : traces
					.entrySet()) {
				for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
					targets.add(TraceToAUGTransformer.transform(entry.getKey(),
							tuple.getFirst(), tuple.getSecond()));
				}
			}
			return targets;
		}

		private Map<String, List<Tuple<Event, List<Event>>>> parser(
				String[] srcPaths, String[] classpaths) throws IOException {
			System.out
					.println("Converting from source code to event stream ...");
			List<Event> eventStream = buildMethodTraces(srcPaths, classpaths);
			System.out
					.println("Number of classes: " + numbClasses(eventStream));
			System.out
					.println("Number of methods: " + numbMethods(eventStream));
			System.out.println("Number of events: " + numbEvents(eventStream));
			System.out.println();

			EventsFilter ef = new EventsFilter();
			// List<Event> localFilter = ef.locals(sequences);
			System.out.println("Filtering duplicate code ...");
			List<Event> noDuplicates = ef.duplicates(eventStream);
			System.out.println("Number of classes: "
					+ numbClasses(noDuplicates));
			System.out.println("Number of methods: "
					+ numbMethods(noDuplicates));
			System.out.println("Number of events: " + numbEvents(noDuplicates));
			System.out.println();

			System.out.println("Filtering infrequent events ...");
			List<Event> frequentEvents = ef.frequent(noDuplicates, FREQUENCY);
			System.out.println("Number of classes: "
					+ numbClasses(frequentEvents));
			System.out.println("Number of methods: "
					+ numbMethods(frequentEvents));
			System.out.println("Number of events: "
					+ numbEvents(frequentEvents));
			System.out.println();

			EventStreamGenerator esg = new EventStreamGenerator();
			Map<String, List<Tuple<Event, List<Event>>>> absPath = esg
					.absoluteFileMethodStructure(frequentEvents);
			// checkEventExist(sequences);
			// getMethodOccs(absPath);
			// containsPattern(absPath);
			System.out.println("After all filters, number of classes "
					+ absPath.size());
			Map<String, List<Tuple<Event, List<Event>>>> relPath = esg
					.relativeFileMethodStructure(absPath);
			// containsPattern(relPath);
			getNoFiles(relPath);
			esg.generateFiles(new File(getEventsPath()), FREQUENCY, relPath);

			return relPath;
		}

		private int numbEvents(List<Event> stream) {
			int counter = 0;
			for (Event event : stream) {
				if ((event.getKind() == EventKind.FIRST_DECLARATION)
						|| (event.getKind() == EventKind.SUPER_DECLARATION)
						|| (event.getKind() == EventKind.CONSTRUCTOR)
						|| (event.getKind() == EventKind.INVOCATION)) {
					counter++;
				}
			}
			return counter;
		}

		private int numbMethods(List<Event> stream) {
			int counter = 0;
			for (Event event : stream) {
				if ((event.getKind() == EventKind.METHOD_DECLARATION)
						|| (event.getKind() == EventKind.INITIALIZER)) {
					counter++;
				}
			}
			return counter;
		}

		private void containsPattern(
				Map<String, List<Tuple<Event, List<Event>>>> stream) {
			String tn1 = "PreparedStatement";
			String tn2 = "DBManager";

			String mn1 = "executeQuery";
			String mn2 = "closePreparedStatement";

			Event event1 = EventGenerator.invocation(tn1, mn1);
			Event event2 = EventGenerator.invocation(tn2, mn2);

			int counter = 0;
			int counter1 = 0;
			int counter2 = 0;

			int maxLength = 0;

			for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : stream
					.entrySet()) {
				for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
					if (tuple.getSecond().size() > maxLength) {
						maxLength = tuple.getSecond().size();
					}
					for (Event event : tuple.getSecond()) {
						if (event.equals(event1)) {
							counter1++;
						}
						if (event.equals(event2)) {
							counter2++;
						}
					}
					counter += Math.min(counter1, counter2);
					counter1 = 0;
					counter2 = 0;
				}
			}
			System.out.println("Pattern support = " + counter);
			System.out.println("Max method size = " + maxLength);
		}

		private Map<Integer, Set<Episode>> containsSubpattern(
				Map<Integer, Set<Episode>> episodes, List<Event> mapping) {
			String tn1 = "Connection";
			String tn2 = "DBManager";

			Map<Integer, Set<Episode>> p = Maps.newLinkedHashMap();

			String mn1 = "prepareStatement";
			String mn2 = "closePreparedStatement";

			Event event1 = EventGenerator.invocation(tn1, mn1);
			Event event2 = EventGenerator.invocation(tn2, mn2);

			int idx1 = mapping.indexOf(event1);
			int idx2 = mapping.indexOf(event2);

			boolean found = false;

			for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
				for (Episode episode : entry.getValue()) {
					if (episode.getEvents().contains(new Fact(idx1))
							&& (episode.getEvents().contains(new Fact(idx2)))) {
						System.out.println("Pattern: " + episode.toString());
						found = true;
						p.put(entry.getKey(), Sets.newHashSet(episode));
					}
				}
			}
			if (!found) {
				System.out.println("Pattern is not contained");
			}
			return p;
		}

		private void getMethodOccs(
				Map<String, List<Tuple<Event, List<Event>>>> stream) {
			for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : stream
					.entrySet()) {
				for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
					for (Event event : tuple.getSecond()) {
						IMethodName method = event.getMethod();
						String typeName = method.getDeclaringType()
								.getFullName();
						String methodName = method.getFullName();
						if (typeName.equalsIgnoreCase("AutoCloseable")
								&& methodName.equalsIgnoreCase("close")) {
							System.out.println();
							System.out.println("Class: " + entry.getKey());
							System.out.println("Method: "
									+ tuple.getFirst().getMethod()
											.getIdentifier());
							System.out.println("Events: " + tuple.getSecond());
							continue;
						}
					}
				}
			}

		}

		private void checkEventExist(List<Event> sequences) {
			for (Event event : sequences) {
				IMethodName method = event.getMethod();
				String typeName = method.getDeclaringType().getFullName();
				String methodName = method.getFullName();
				if (typeName.equalsIgnoreCase("DBManager")
						&& (methodName
								.equalsIgnoreCase("closePreparedStatement"))) {
					System.out.println("Found" + event);
				}
			}

		}

		private int numbClasses(List<Event> stream) {
			int counter = 0;
			for (Event event : stream) {
				if (event.getKind() == EventKind.ABSOLUTE_PATH) {
					counter++;
				}
			}
			return counter;
		}

		private int numRelClasses(List<Event> stream) {
			int counter = 0;
			for (Event event : stream) {
				if (event.getKind() == EventKind.RELATIVE_PATH) {
					counter++;
				}
			}
			return counter;
		}

		private void getNoFiles(
				Map<String, List<Tuple<Event, List<Event>>>> stream) {
			int methodCounter = 0;

			for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : stream
					.entrySet()) {
				methodCounter += entry.getValue().size();
			}
			System.out.println("Number of classes: " + stream.size());
			System.out.println("Number of methods: " + methodCounter);
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
			// String pathName =
			// "/Users/ervinacergani/Documents/projects/miner-detector/streamData/";
			String pathName = "/home/ervina/eventsData/test/";
			return pathName;
		}

		private String getAlgorithmPath() {
			// String path =
			// "/Users/ervinacergani/Documens/projects/n-graph-miner/";
			String path = "/home/ervina/n-graph-miner/";
			return path;
		}
	}
}
