import cc.episodeMining.data.StreamGenerator;
import cc.episodeMining.mudetect.EpisodesToPatternTransformer;
import cc.episodeMining.mudetect.TraceToAUGTransformer;
import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.tu_darmstadt.stg.mubench.DefaultFilterAndRankingStrategy;
import de.tu_darmstadt.stg.mubench.ViolationUtils;
import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput.Builder;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;
import de.tu_darmstadt.stg.mudetect.MissingElementViolationPredicate;
import de.tu_darmstadt.stg.mudetect.MuDetect;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Violation;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.ranking.*;
import edu.iastate.cs.mudetect.mining.MinPatternActionsModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class runner {

	public static void main(String[] args) throws Exception {
		new MuBenchRunner().withMineAndDetectStrategy(new Strategy()).run(args);

		
		
		System.out.println("done");
	}

	static class Strategy implements DetectionStrategy {

		public DetectorOutput detectViolations(DetectorArgs args, Builder output)
				throws Exception {
			parser(args.getTargetSrcPaths(), args.getDependencyClassPath());

			List<Event> mapping = loadMapping(getMapPath());
			Set<Episode> episodes = mineEpisodes(getStreamPath());
            Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer().transform(episodes, mapping);

            Collection<APIUsageExample> targets = loadTargetAUGs(args.getTargetSrcPaths(), args.getDependencyClassPath());
            MuDetect detection = new MuDetect(
                    new MinPatternActionsModel(() -> patterns, 2),
                    new AlternativeMappingsOverlapsFinder(
                            new AlternativeMappingsOverlapsFinder.Config()
                    ),
                    new MissingElementViolationPredicate(),
                    new DefaultFilterAndRankingStrategy(new WeightRankingStrategy(
                            new ProductWeightFunction(
                                    new OverlapWithoutEdgesToMissingNodesWeightFunction(new ConstantNodeWeightFunction()),
                                    new PatternSupportWeightFunction(),
                                    new ViolationSupportWeightFunction()
                            ))));
            List<Violation> violations = detection.findViolations(targets);

            return output.withFindings(violations, ViolationUtils::toFinding);
		}

        private Collection<APIUsageExample> loadTargetAUGs(String[] srcPaths, String[] classpath) {
		    // TODO it is weird that building traces returns a List<Event>, isn't a List<Event> _one_ trace and the type
            // should be Collection<List<Events>>? I'm assuming this for the subsequent implementation. -Sven
            Collection<List<Event>> traces = Arrays.asList(buildMethodTraces(srcPaths, classpath));

            Collection<APIUsageExample> targets = new ArrayList<>();
            for (List<Event> trace : traces) {
                targets.add(TraceToAUGTransformer.transform(trace));
            }

            return targets;
        }

        private Set<Episode> mineEpisodes(File streamPath) {
			throw new NotImplementedException();
		}

		private List<Event> loadMapping(File mapPath) {
			throw new NotImplementedException();
		}

		public void parser(String[] srcPaths, String[] classpaths)
				throws IOException {
            List<Event> sequences = buildMethodTraces(srcPaths, classpaths);

			Map<Event, Integer> freqEvents = frequentEvents(sequences);
			EventStream es = new EventStream();
			for (Event event : sequences) {
				if (event.getKind() != EventKind.METHOD_DECLARATION) {
					if (freqEvents.get(event) > 2) {
						es.addEvent(event);
					}
				} else {
					es.increaseTimeout();
				}
			}
			FileUtils.writeStringToFile(getStreamPath(), es.getStreamText());
			JsonUtils.toJson(es.getMapping(), getMapPath());
		}

        private List<Event> buildMethodTraces(String[] srcPaths, String[] classpaths) {
            List<Event> sequences = Lists.newLinkedList();
            for (String srcPath : srcPaths) {
                StreamGenerator generator = new StreamGenerator();
                sequences.addAll(generator.generateMethodTraces(new File(
                        srcPath), classpaths));
            }
            return sequences;
        }

        private Map<Event, Integer> frequentEvents(List<Event> events) {
			Map<Event, Integer> results = Maps.newLinkedHashMap();

			for (Event e : events) {
				if (e.getKind() != EventKind.METHOD_DECLARATION) {
					if (results.containsKey(e)) {
						int freq = results.get(e);
						results.put(e, freq + 1);
					} else {
						results.put(e, 1);
					}
				}
			}
			return results;
		}

		private String getPath() {
			String pathName = "/Users/ervinacergani/Documents/MisuseDetector/events/";
			return pathName;
		}

		private File getStreamPath() {
			String streamName = getPath() + "eventStream.txt";
			return new File(streamName);
		}

		private File getMapPath() {
			String mapName = getPath() + "mapping.txt";
			return new File(mapName);
		}

		private File getEpisodesPath() {
			String mapName = getPath() + "episodes.txt";
			return new File(mapName);
		}
	}
}
