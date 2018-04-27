package cc.episodeMining.mudetect;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import java.util.*;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class EpisodesToPatternTransformerTest {

    @Test
    public void transformsOneEventEpisode() {
        Set<Episode> episodes = new HashSet<>(Collections.singletonList(
                createEpisode(42, new Fact(0))
        ));
        List<Event> mapping = Collections.singletonList(
                createMethodCallEvent(Names.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()"))
        );

        Set<APIUsagePattern> patterns = transform(episodes, mapping);

        assertThat(patterns, hasSize(1));
        assertThat(patterns.iterator().next(), hasNodes(methodCall("Namespace.DeclaringType", "M()")));
    }

    @Test
    public void transformsMultiEventEpisode() {
        Fact fact0 = new Fact(0);
        Fact fact1 = new Fact(1);
        Set<Episode> episodes = new HashSet<>(Collections.singletonList(
                createEpisode(1337, fact0, fact1, new Fact(fact0, fact1))
        ));
        List<Event> mapping = Arrays.asList(
                createMethodCallEvent(Names.newMethod("0M:[p:void] [i:Namespace.DeclaringType, a, 1].M()")),
                createMethodCallEvent(Names.newMethod("0M:[p:void] [i:Other.Type, a, 1].N()"))
        );

        Set<APIUsagePattern> patterns = transform(episodes, mapping);

        assertThat(patterns, hasSize(1));
        assertThat(patterns.iterator().next(),
                hasOrderEdge(methodCall("Namespace.DeclaringType", "M()"), methodCall("Other.Type", "N()")));
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

    public Set<APIUsagePattern> transform(Set<Episode> episodes, List<Event> mapping) {
        Set<APIUsagePattern> patterns = new HashSet<>();
        for (Episode episode : episodes) {
            patterns.add(transform(episode, mapping));
        }
        return patterns;
    }

    private APIUsagePattern transform(Episode episode, List<Event> mapping) {
        APIUsagePattern pattern = new APIUsagePattern(episode.getFrequency(), new HashSet<>());
        Map<Integer, Node> factIdToNodeMap = new HashMap<>();
        for (Fact fact : episode.getFacts()) {
            if (!fact.isRelation()) {
                Event event = mapping.get(fact.getFactID());
                IMethodName method = event.getMethod();
                MethodCallNode node = new MethodCallNode(method.getDeclaringType().getFullName(), method.getName() + "()");
                pattern.addVertex(node);
                factIdToNodeMap.put(fact.getFactID(), node);
            } else {
                Tuple<Fact, Fact> relation = fact.getRelationFacts();
                Node sourceNode = factIdToNodeMap.get(relation.getFirst().getFactID());
                Node targetNode = factIdToNodeMap.get(relation.getSecond().getFactID());
                pattern.addEdge(sourceNode, targetNode, new OrderEdge(sourceNode, targetNode));
            }
        }
        return pattern;
    }

}
