package cc.episodeMining.IO;

import java.util.Map;
import java.util.Set;

import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.mining.graphs.EpisodeToGraphConverter;
import cc.kave.episodes.mining.patterns.PatternFilter;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.EpisodeType;
import cc.kave.episodes.postprocessor.TransClosedEpisodes;

import com.google.inject.Inject;

public class Patterns {

	private EventStreamIo streamIo;

	private EpisodeParser parser;
	private PatternFilter filter;
	private TransClosedEpisodes transClosure;
	private EpisodeToGraphConverter episodeGraphConverter;

	@Inject
	public Patterns(EventStreamIo streamIo, EpisodeParser epParser,
			PatternFilter pFilter, TransClosedEpisodes transClosure,
			EpisodeToGraphConverter episodeToGraph) {
		this.streamIo = streamIo;
		this.parser = epParser;
		this.filter = pFilter;
		this.transClosure = transClosure;
		this.episodeGraphConverter = episodeToGraph;
	}

	public void output() throws Exception {
		Map<Integer, Set<Episode>> episodes = parser.parser(3);
		Map<Integer, Set<Episode>> patterns = filter.filter(
				EpisodeType.GENERAL, episodes, 3, 0.2);

		for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
			if (entry.getKey() == 1) {
				continue;
			}
			for (Episode ep : entry.getValue()) {
				Episode p = transClosure.remTransClosure(ep);
				episodeGraphConverter.
			}
		}

	}
}
