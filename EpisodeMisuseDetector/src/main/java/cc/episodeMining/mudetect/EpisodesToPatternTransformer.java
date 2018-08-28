package cc.episodeMining.mudetect;

import java.util.*;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;
import com.google.common.collect.Collections2;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class EpisodesToPatternTransformer {
	public Set<APIUsagePattern> transform(Map<Integer, Set<Episode>> episodes,
			List<Event> mapping) {
		Set<APIUsagePattern> patterns = new HashSet<>();
		for (Map.Entry<Integer, Set<Episode>> entry : episodes.entrySet()) {
			for (Episode episode : entry.getValue()) {
				APIUsagePattern pattern = transformStrict(episode, mapping);
				patterns.addAll(makeArbitraryOrderExplicit(pattern));
			}
		}
		return patterns;
	}

	private APIUsagePattern transformStrict(Episode episode, List<Event> mapping) {
		APIUsagePattern pattern = new APIUsagePattern(episode.getFrequency(),
				new HashSet<>());
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
				Node sourceNode = factIdToNodeMap.get(relation.getFirst()
						.getFactID());
				Node targetNode = factIdToNodeMap.get(relation.getSecond()
						.getFactID());
				pattern.addEdge(sourceNode, targetNode, new OrderEdge(
						sourceNode, targetNode));
			}
		}
		return pattern;
	}

	private Set<APIUsagePattern> makeArbitraryOrderExplicit(
			APIUsagePattern pattern) {
		Set<Set<Node>> components = getConnectedComponents(pattern);
		Set<APIUsagePattern> patterns = new HashSet<>();

		for (List<Set<Node>> permutation : Collections2.orderedPermutations(
				components, Comparator.comparingInt(Set::hashCode))) {
			APIUsagePattern patternPermutation = (APIUsagePattern) pattern
					.clone();
			for (int i = 0; i < permutation.size() - 1; i++) {
				for (Node source : permutation.get(i)) {
					for (int j = i + 1; j < permutation.size(); j++) {
						for (Node target : permutation.get(j)) {
							patternPermutation.addEdge(source, target,
									new OrderEdge(source, target));
						}
					}
				}
			}
			patterns.add(patternPermutation);
		}

		return patterns;
	}

	private Set<Set<Node>> getConnectedComponents(APIUsagePattern pattern) {
		Set<Set<Node>> components = new HashSet<>();
		for (Node node : pattern.vertexSet()) {
			if (!isInComponent(node, components)) {
				Set<Node> component = new HashSet<>();
				createComponent(node, pattern, component);
				components.add(component);
			}
		}
		return components;
	}

	private boolean isInComponent(Node node, Set<Set<Node>> components) {
		for (Set<Node> component : components) {
			if (component.contains(node)) {
				return true;
			}
		}
		return false;
	}

	private void createComponent(Node node, APIUsagePattern pattern,
			Set<Node> component) {
		component.add(node);
		for (Node source : pattern.incomingNodesOf(node)) {
			if (!component.contains(source)) {
				createComponent(source, pattern, component);
			}
		}
		for (Node target : pattern.outgoingNodesOf(node)) {
			if (!component.contains(target)) {
				createComponent(target, pattern, component);
			}
		}
	}
}
