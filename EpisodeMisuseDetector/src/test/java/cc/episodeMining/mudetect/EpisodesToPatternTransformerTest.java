package cc.episodeMining.mudetect;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.io.FileReader;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class EpisodesToPatternTransformerTest {

	@Test
	public void transformsOneEventEpisode() {
		Set<Episode> episodes = new HashSet<>(
				Collections.singletonList(createEpisode(42, new Fact(0))));
		List<Event> mapping = Collections
				.singletonList(createMethodCallEvent(Names
						.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer()
				.transform(episodes, mapping);

		assertThat(patterns, hasSize(1));
		assertThat(patterns.iterator().next(),
				hasNodes(methodCall("Namespace.DeclaringType", "M()")));
	}

	@Test
	public void transformsMultiEventEpisode() {
		Fact fact0 = new Fact(0);
		Fact fact1 = new Fact(1);
		Set<Episode> episodes = new HashSet<>(
				Collections.singletonList(createEpisode(1337, fact0, fact1,
						new Fact(fact0, fact1))));
		List<Event> mapping = Arrays
				.asList(createMethodCallEvent(Names
						.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")),
						createMethodCallEvent(Names
								.newMethod("0M:[p:void] [i:Other.Type, a, 1].N()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer()
				.transform(episodes, mapping);

		assertThat(patterns, hasSize(1));
		assertThat(
				patterns.iterator().next(),
				hasOrderEdge(methodCall("Namespace.DeclaringType", "M()"),
						methodCall("Other.Type", "N()")));
	}

	@Test
	public void transformsTwoDiscEventEpisode() {
		Fact fact0 = new Fact(0);
		Fact fact1 = new Fact(1);
		Set<Episode> episodes = new HashSet<>(
				Collections.singletonList(createEpisode(1337, fact0, fact1)));
		List<Event> mapping = Arrays
				.asList(createMethodCallEvent(Names
						.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")),
						createMethodCallEvent(Names
								.newMethod("0M:[p:void] [i:Other.Type, a, 1].N()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer()
				.transform(episodes, mapping);

		Episode episode1 = createEpisode(1337, fact0, fact1, new Fact(fact0,
				fact1));
		Episode episode2 = createEpisode(1337, fact0, fact1, new Fact(fact1,
				fact0));
		APIUsagePattern pattern1 = new EpisodesToPatternTransformer()
				.transform(episode1, mapping);
		APIUsagePattern pattern2 = new EpisodesToPatternTransformer()
				.transform(episode2, mapping);

		assertThat(patterns, hasSize(2));
		assertTrue(patterns.contains(pattern1));
		assertTrue(patterns.contains(pattern2));
	}

	@Test
	public void transformsThreeDiscEventEpisode() {
		Fact fact0 = new Fact(0);
		Fact fact1 = new Fact(1);
		Fact fact2 = new Fact(1);
		Set<Episode> episodes = new HashSet<>(
				Collections.singletonList(createEpisode(1337, fact0, fact1,
						fact2)));
		List<Event> mapping = Arrays
				.asList(createMethodCallEvent(Names
						.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")),
						createMethodCallEvent(Names
								.newMethod("0M:[p:void] [i:Other.Type, a, 1].N()")),
						createMethodCallEvent(Names
								.newMethod("0M:[p:void] [i:Third.Type, a, 1].O()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer()
				.transform(episodes, mapping);

		Episode episode1 = createEpisode(1337, fact0, fact1, fact2, new Fact(
				fact0, fact1), new Fact(fact0, fact2));
		Episode episode2 = createEpisode(1337, fact0, fact1, fact2, new Fact(
				fact1, fact0), new Fact(fact2, fact0));
		Episode episode3 = createEpisode(1337, fact0, fact1, fact2, new Fact(
				fact1, fact0), new Fact(fact1, fact2));
		Episode episode4 = createEpisode(1337, fact0, fact1, fact2, new Fact(
				fact0, fact1), new Fact(fact2, fact1));
		Episode episode5 = createEpisode(1337, fact0, fact1, fact2, new Fact(
				fact2, fact0), new Fact(fact2, fact1));
		Episode episode6 = createEpisode(1337, fact0, fact1, fact2, new Fact(
				fact0, fact2), new Fact(fact1, fact2));

		APIUsagePattern pattern1 = new EpisodesToPatternTransformer()
				.transform(episode1, mapping);
		APIUsagePattern pattern2 = new EpisodesToPatternTransformer()
				.transform(episode2, mapping);
		APIUsagePattern pattern3 = new EpisodesToPatternTransformer()
				.transform(episode3, mapping);
		APIUsagePattern pattern4 = new EpisodesToPatternTransformer()
				.transform(episode4, mapping);
		APIUsagePattern pattern5 = new EpisodesToPatternTransformer()
				.transform(episode5, mapping);
		APIUsagePattern pattern6 = new EpisodesToPatternTransformer()
				.transform(episode6, mapping);

		assertThat(patterns, hasSize(6));
		assertTrue(patterns.contains(pattern1));
		assertTrue(patterns.contains(pattern2));
		assertTrue(patterns.contains(pattern3));
		assertTrue(patterns.contains(pattern4));
		assertTrue(patterns.contains(pattern5));
		assertTrue(patterns.contains(pattern6));
	}

	@Test
	public void keepsEpisodeFrequency() {
		Set<Episode> episodes = new HashSet<>(
				Collections.singletonList(createEpisode(2345890)));
		List<Event> mapping = Collections.emptyList();

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer()
				.transform(episodes, mapping);

		assertThat(patterns, hasSize(1));
		assertThat(patterns.iterator().next().getSupport(), is(2345890));
	}

	@Test
	public void smokeTest() {
		File data = new File(getClass().getResource("/episodes").getFile()
				.replaceAll("%20", " "));

		Map<Integer, Set<Episode>> episodesByNumNodes = new EpisodeParser(data,
				new FileReader()).parser(1);
		EventStreamIo streamIo = new EventStreamIo(data);
		List<Event> mapping = streamIo.readMapping(1);
		EpisodesToPatternTransformer transformer = new EpisodesToPatternTransformer();

		Set<APIUsagePattern> patterns = new HashSet<>();
		for (Set<Episode> episodes : episodesByNumNodes.values()) {
			patterns.addAll(transformer.transform(episodes, mapping));
		}

		System.out.println("Converted " + patterns.size()
				+ " episodes to patterns.");
	}

	private Episode createEpisode(int frequency, Fact... facts) {
		Episode episode = new Episode();
		episode.setFrequency(frequency);
		episode.addListOfFacts(Arrays.asList(facts));
		return episode;
	}

	private Event createMethodCallEvent(IMethodName methodName) {
		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setMethod(methodName);
		return event;
	}

}
