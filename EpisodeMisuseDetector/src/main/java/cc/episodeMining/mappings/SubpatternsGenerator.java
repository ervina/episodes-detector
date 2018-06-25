package cc.episodeMining.mappings;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class SubpatternsGenerator {

	public Set<APIUsagePattern> generate(APIUsagePattern pattern) {
		Set<APIUsagePattern> subpatterns = Sets.newLinkedHashSet();

		if (!isDisconnected(pattern)) {
			subpatterns.add(pattern);
			return subpatterns;
		}
		if (isSetNodes(pattern)) {
			subpatterns.addAll(generateNodesSet(pattern));
			return subpatterns;
		}
		subpatterns.addAll(generateConnectedPatterns(pattern));

		return subpatterns;
	}

	private boolean isDisconnected(APIUsagePattern pattern) {
		Set<Node> nodes = pattern.vertexSet();
		int numNodes = nodes.size();

		for (Node n : nodes) {
			int nodeDegree = pattern.edgesOf(n).size();
			if (nodeDegree < (numNodes - 1)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSetNodes(APIUsagePattern pattern) {

		if (pattern.getEdgeSize() == 0) {
			return true;
		}
		return false;
	}

	private Set<APIUsagePattern> generateNodesSet(APIUsagePattern pattern) {
		Set<APIUsagePattern> results = Sets.newLinkedHashSet();

		Set<Node> nodes = pattern.vertexSet();
		for (Node n : nodes) {
			APIUsagePattern subpattern = createOneNodePattern(n, pattern);
			results.add(subpattern);
		}
		return results;
	}

	private Set<APIUsagePattern> generateConnectedPatterns(
			APIUsagePattern pattern) {
		Set<APIUsagePattern> results = Sets.newLinkedHashSet();
		Set<Node> nodes = pattern.vertexSet();
		Set<Node> processedNodes = Sets.newLinkedHashSet();

		for (Node n : nodes) {
			if (processedNodes.contains(n)) {
				continue;
			}
			processedNodes.add(n);
			Set<Edge> edges = pattern.edgesOf(n);

			if (edges.size() == 0) {
				APIUsagePattern subpattern = createOneNodePattern(n, pattern);
				results.add(subpattern);
			}
			Set<Node> subpatternNodes = Sets.newLinkedHashSet();
			subpatternNodes.add(n);
			Set<Edge> subppaternEdge = Sets.newLinkedHashSet();

			for (Edge e : edges) {
				Node source = e.getSource();
				Node target = e.getTarget();

				subpatternNodes.add(source);
				subpatternNodes.add(target);

				processedNodes.add(target);
				processedNodes.add(source);
			}
			for (Node sn : subpatternNodes) {
				subppaternEdge.addAll(pattern.edgesOf(sn));
			}
			APIUsagePattern subpattern = new APIUsagePattern(
					pattern.getSupport(), new HashSet<>());
			for (Node node : subpatternNodes) {
				subpattern.addVertex(node);
			}
			for (Edge edge : subppaternEdge) {
				Node source = edge.getSource();
				Node target = edge.getTarget();
				subpattern.addEdge(source, target,
						new OrderEdge(source, target));
			}
			results.add(subpattern);
		}
		return results;
	}

	private APIUsagePattern createOneNodePattern(Node node,
			APIUsagePattern pattern) {
		APIUsagePattern subpattern = new APIUsagePattern(pattern.getSupport(),
				new HashSet<>());
		subpattern.addVertex(node);
		return subpattern;
	}
}
