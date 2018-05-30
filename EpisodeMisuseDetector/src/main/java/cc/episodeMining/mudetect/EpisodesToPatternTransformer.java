package cc.episodeMining.mudetect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class EpisodesToPatternTransformer {
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
                MethodCallNode node = TransformerUtils.createCallNode(method);
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
