package cc.episodeMining.mudetect;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.io.EpisodeParser;
import cc.kave.episodes.io.EventStreamIo;
import cc.kave.episodes.io.FileReader;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EpisodesToPatternTransformerTest {

	@Test
	public void transformsOneEventEpisode() {
		Set<Episode> episodes = set(createEpisode(42, new Fact(0)));
		List<Event> mapping = list(
				createMethodCallEvent(Names.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer().transform(episodes, mapping);

		assertThat(patterns, hasSize(1));
		assertThat(patterns.iterator().next(),
				hasNodes(methodCall("Namespace.DeclaringType", "M()")));
	}

	@Test
	public void transformsMultiEventEpisode() {
		Fact fact0 = new Fact(0);
		Fact fact1 = new Fact(1);
		Set<Episode> episodes = set(createEpisode(1337, fact0, fact1, new Fact(fact0, fact1)));
		List<Event> mapping = list(
				createMethodCallEvent(Names.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")),
				createMethodCallEvent(Names.newMethod("0M:[p:void] [i:Other.Type, a, 1].N()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer().transform(episodes, mapping);

		assertThat(patterns, hasSize(1));
		assertThat(patterns.iterator().next(),
				hasOrderEdge(methodCall("Namespace.DeclaringType", "M()"), methodCall("Other.Type", "N()")));
	}

	@Test
	public void transformsTwoDiscEventEpisode() {
		Fact fact0 = new Fact(0);
		Fact fact1 = new Fact(1);
		Set<Episode> episodes = set(createEpisode(1337, fact0, fact1));
		List<Event> mapping = list(
				createMethodCallEvent(Names.newMethod("0M:[p:void] [i:A, a, 1].A()")),
				createMethodCallEvent(Names.newMethod("0M:[p:void] [i:B, a, 1].B()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer().transform(episodes, mapping);

		assertThat(patterns, containsInAnyOrder(
				hasOrderEdge(methodCall("A", "A()"), methodCall("B", "B()")),
				hasOrderEdge(methodCall("B", "B()"), methodCall("A", "A()"))
		));
	}

	@Test
	public void transformsThreeDiscEventEpisode() {
		Fact fact0 = new Fact(0);
		Fact fact1 = new Fact(1);
		Fact fact2 = new Fact(2);
		Set<Episode> episodes = set(createEpisode(1337, fact0, fact1, fact2));
		List<Event> mapping = list(
		        createMethodCallEvent(Names.newMethod("0M:[p:void] [i:A, a, 1].A()")),
                createMethodCallEvent(Names.newMethod("0M:[p:void] [i:B, a, 1].B()")),
                createMethodCallEvent(Names.newMethod("0M:[p:void] [i:C, a, 1].C()")));

		Set<APIUsagePattern> patterns = new EpisodesToPatternTransformer().transform(episodes, mapping);

		assertThat(patterns, containsInAnyOrder(
		        both(hasOrderEdge(methodCall("A", "A()"), methodCall("B", "B()")))
                        .and(hasOrderEdge(methodCall("A", "A()"), methodCall("C", "C()")))
                        .and(hasOrderEdge(methodCall("B", "B()"), methodCall("C", "C()"))),

                both(hasOrderEdge(methodCall("A", "A()"), methodCall("B", "B()")))
                        .and(hasOrderEdge(methodCall("A", "A()"), methodCall("C", "C()")))
                        .and(hasOrderEdge(methodCall("C", "C()"), methodCall("B", "B()"))),

                both(hasOrderEdge(methodCall("B", "B()"), methodCall("A", "A()")))
                        .and(hasOrderEdge(methodCall("B", "B()"), methodCall("C", "C()")))
                        .and(hasOrderEdge(methodCall("A", "A()"), methodCall("C", "C()"))),

                both(hasOrderEdge(methodCall("B", "B()"), methodCall("A", "A()")))
                        .and(hasOrderEdge(methodCall("B", "B()"), methodCall("C", "C()")))
                        .and(hasOrderEdge(methodCall("C", "C()"), methodCall("A", "A()"))),

                both(hasOrderEdge(methodCall("C", "C()"), methodCall("A", "A()")))
                        .and(hasOrderEdge(methodCall("C", "C()"), methodCall("B", "B()")))
                        .and(hasOrderEdge(methodCall("A", "A()"), methodCall("B", "B()"))),

                both(hasOrderEdge(methodCall("C", "C()"), methodCall("A", "A()")))
                        .and(hasOrderEdge(methodCall("C", "C()"), methodCall("B", "B()")))
                        .and(hasOrderEdge(methodCall("B", "B()"), methodCall("A", "A()")))
        ));
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

	@SafeVarargs
	private final <A> Set<A> set(A... elements) {
		return new HashSet<>(list(elements));
	}

	@SafeVarargs
	private final <A> List<A> list(A... elements) {
		return Arrays.asList(elements);
	}

	private Episode createEpisode(int frequency, Fact... facts) {
		Episode episode = new Episode();
		episode.setFrequency(frequency);
		episode.addListOfFacts(list(facts));
		return episode;
	}

	private Event createMethodCallEvent(IMethodName methodName) {
		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setMethod(methodName);
		return event;
	}

}
