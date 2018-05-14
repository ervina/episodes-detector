import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import cc.episodeMining.data.StreamGenerator;
import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.mining.graphs.EpisodeToGraphConverter;
import cc.kave.episodes.mining.patterns.PatternFilter;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.postprocessor.TransClosedEpisodes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.mxgraph.util.svg.Parser;

import de.tu_darmstadt.stg.mubench.cli.DetectionStrategy;
import de.tu_darmstadt.stg.mubench.cli.DetectorArgs;
import de.tu_darmstadt.stg.mubench.cli.DetectorFinding;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput;
import de.tu_darmstadt.stg.mubench.cli.DetectorOutput.Builder;
import de.tu_darmstadt.stg.mubench.cli.MuBenchRunner;

public class runner {

	public static void main(String[] args) throws Exception {
		new MuBenchRunner().withMineAndDetectStrategy(new Strategy()).run(args);

		
		
		System.out.println("done");
	}

	static class Strategy implements DetectionStrategy {

		public DetectorOutput detectViolations(DetectorArgs args, Builder output)
				throws Exception {
			parser(args.getTargetSrcPaths(), args.getDependencyClassPath());
			return output.withFindings(new ArrayList<DetectorFinding>());
		}

		public void parser(String[] srcPaths, String[] classpaths)
				throws IOException {
			List<Event> sequences = Lists.newLinkedList();
			for (String srcPath : srcPaths) {
				StreamGenerator generator = new StreamGenerator();
				sequences.addAll(generator.generateMethodTraces(new File(
						srcPath), classpaths));
			}

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
