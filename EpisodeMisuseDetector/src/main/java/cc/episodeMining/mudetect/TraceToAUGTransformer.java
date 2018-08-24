package cc.episodeMining.mudetect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;

public class TraceToAUGTransformer {
    public static APIUsageExample transform(Triplet<String, Event, List<Event>> data) {
        return transform(data.getFirst(), data.getSecond(), data.getThird());
    }

    public static APIUsageExample transform(String fileName, Event declaration, List<Event> trace) {
        return transform(createLocation(fileName, declaration), trace);
    }

    public static Location createLocation(String fileName, Event declaration) {
        String methodSignature = TransformerUtils.getMethodSignature(declaration.getMethod());
        return new Location(":SomeProject:", fileName, methodSignature);
    }

    public static APIUsageExample transform(Location location, List<Event> trace) {

        Set<Node> predecessors = new HashSet<>();
        APIUsageExample aug = new APIUsageExample(location);
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
