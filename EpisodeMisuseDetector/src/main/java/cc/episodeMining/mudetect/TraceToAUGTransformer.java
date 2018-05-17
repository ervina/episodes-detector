package cc.episodeMining.mudetect;

import cc.kave.episodes.model.events.Event;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraceToAUGTransformer {
    public static APIUsageExample transform(List<Event> trace) {
        String file = "get the trace's origin file";
        String methodSignature = "get the trace's origin method";

        Set<Node> predecessors = new HashSet<>();
        APIUsageExample aug = new APIUsageExample(new Location("project", file, methodSignature));
        for (Event event : trace) {
            MethodCallNode node = TransformerUtils.createCallNode(event.getMethod());
            aug.addVertex(node);

            for (Node predecessor : predecessors) {
                aug.addEdge(predecessor, node, new OrderEdge(predecessor, node));
            }

            predecessors.add(node);
        }
        return aug;
    }
}
