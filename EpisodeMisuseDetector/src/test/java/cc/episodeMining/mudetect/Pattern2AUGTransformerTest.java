package cc.episodeMining.mudetect;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Fact;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class Pattern2AUGTransformerTest {

    @Test
    public void transformsEpisode() {
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

    private Episode createEpisode(int frequency, Fact fact) {
        Episode episode = new Episode();
        episode.setFrequency(frequency);
        episode.addFact(fact);
        return episode;
    }

    private Event createMethodCallEvent(IMethodName methodName) {
        Event event = new Event();
        event.setKind(EventKind.INVOCATION);
        event.setMethod(methodName);
        return event;
    }

    private Set<APIUsagePattern> transform(Set<Episode> episodes, List<Event> mapping) {
        Set<APIUsagePattern> patterns = new HashSet<>();
        for (Episode episode : episodes) {
            patterns.add(transform(episode, mapping));
        }
        return patterns;
    }

    private APIUsagePattern transform(Episode episode, List<Event> mapping) {
        APIUsagePattern pattern = new APIUsagePattern(episode.getFrequency(), new HashSet<>());
        for (Fact fact : episode.getFacts()) {
            if (!fact.isRelation()) {
                Event event = mapping.get(fact.getFactID());
                IMethodName method = event.getMethod();
                pattern.addVertex(new MethodCallNode(method.getDeclaringType().getFullName(), method.getName() + "()"));
            }
        }
        return pattern;
    }

}
