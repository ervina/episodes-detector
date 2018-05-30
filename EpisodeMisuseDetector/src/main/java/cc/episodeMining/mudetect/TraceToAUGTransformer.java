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

import static cc.kave.episodes.model.events.EventKind.INVOCATION;
import static cc.kave.episodes.model.events.EventKind.METHOD_DECLARATION;

public class TraceToAUGTransformer {
    public static APIUsageExample transform(List<Event> trace) {

        Set<Node> predecessors = new HashSet<>();
        APIUsageExample aug = null;
        for (Event event : trace) {
            if (event.getKind() == METHOD_DECLARATION) {
                // TODO get the actual file location
                String file = "TODO: get the trace's origin file";
                String methodSignature = TransformerUtils.getMethodSignature(event.getMethod());
                aug = new APIUsageExample(new Location(":SomeProject:", file, methodSignature));
            } else if (event.getKind() == INVOCATION) {
                MethodCallNode node = TransformerUtils.createCallNode(event.getMethod());
                aug.addVertex(node);

                for (Node predecessor : predecessors) {
                    aug.addEdge(predecessor, node, new OrderEdge(predecessor, node));
                }

                predecessors.add(node);
            }
        }
        return aug;
    }
}
