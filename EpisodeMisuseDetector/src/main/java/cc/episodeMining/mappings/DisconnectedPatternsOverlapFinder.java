package cc.episodeMining.mappings;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Edge;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder.Config;

public class DisconnectedPatternsOverlapFinder implements OverlapsFinder {
	
	private Config config;
	
	private OverlapsFinder of;

	public DisconnectedPatternsOverlapFinder(Config config) {
		this.config = config;
		
		of = new AlternativeMappingsOverlapsFinder(config);
	}

	

	@Override
	public List<Overlap> findOverlaps(APIUsageExample target,
			APIUsagePattern pattern) {
		Set<APIUsagePattern> subPatterns = new SubpatternsGenerator()
				.generate(pattern);
		if (subPatterns.size() == 1) {
			return of.findOverlaps(target, subPatterns.iterator().next());
		}

		List<List<Overlap>> spOverlaps = Lists.newLinkedList();
		for (APIUsagePattern aup : subPatterns) {
			List<Overlap> overlaps = of.findOverlaps(target, aup);
			spOverlaps.add(overlaps);
		}
		List<Overlap> results = combineOverlaps(target, spOverlaps.get(0),
				spOverlaps.subList(1, spOverlaps.size() - 1));
		return results;
	}

	private List<Overlap> combineOverlaps(APIUsageExample target,
			List<Overlap> list, List<List<Overlap>> subList) {
		if (subList.size() == 1) {
			return combinePairs(target, list, subList.get(0));
		}
		return combineOverlaps(target, subList.get(0),
				subList.subList(1, subList.size() - 1));
	}

	private List<Overlap> combinePairs(APIUsageExample target,
			List<Overlap> ovs1, List<Overlap> ovs2) {
		List<Overlap> combinedOverlaps = Lists.newLinkedList();

		for (Overlap o1 : ovs1) {
			for (Overlap o2 : ovs2) {
				APIUsagePattern p1 = o1.getPattern();
				APIUsagePattern p2 = o2.getPattern();

				APIUsagePattern pattern = generatePattern(p1, p2);
				Map<Node, Node> targetNodeByPatternNode = Maps
						.newLinkedHashMap();

				for (Node patternNode : p1.vertexSet()) {
					Node targetNode = o1.getMappedTargetNode(patternNode);
					targetNodeByPatternNode.put(targetNode, patternNode);
				}
				for (Node patternNode : p2.vertexSet()) {
					Node targetNode = o2.getMappedTargetNode(patternNode);
					targetNodeByPatternNode.put(targetNode, patternNode);
				}

				Map<Edge, Edge> targetEdgeByPatternEdge = Maps
						.newLinkedHashMap();

				for (Edge targetEdge : target.edgeSet()) {
					Node tSource = targetEdge.getSource();
					Node tTarget = targetEdge.getTarget();

					Node pSource = targetNodeByPatternNode.get(tSource);
					Node pTarget = targetNodeByPatternNode.get(tTarget);
					Edge patternEdge = new OrderEdge(pSource, pTarget);

					targetEdgeByPatternEdge.put(targetEdge, patternEdge);
				}
				Overlap overlap = new Overlap(pattern, target,
						targetNodeByPatternNode, targetEdgeByPatternEdge);
				combinedOverlaps.add(overlap);
			}
		}

		return combinedOverlaps;
	}

	private APIUsagePattern generatePattern(APIUsagePattern p1,
			APIUsagePattern p2) {
		APIUsagePattern pattern = new APIUsagePattern(p1.getSupport(),
				new HashSet<>());
		for (Node node : p1.vertexSet()) {
			pattern.addVertex(node);
		}
		for (Node node : p2.vertexSet()) {
			pattern.addVertex(node);
		}
		for (Edge edge : p1.edgeSet()) {
			pattern.addEdge(edge.getSource(), edge.getTarget(), edge);
		}
		for (Edge edge : p2.edgeSet()) {
			pattern.addEdge(edge.getSource(), edge.getTarget(), edge);
		}
		return pattern;
	}
}
