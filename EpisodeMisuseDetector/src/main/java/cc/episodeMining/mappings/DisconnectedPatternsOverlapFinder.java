package cc.episodeMining.mappings;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import de.tu_darmstadt.stg.mudetect.OverlapsFinder;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.model.Overlap;

public class DisconnectedPatternsOverlapFinder implements OverlapsFinder {

	private OverlapsFinder of;

	@Override
	public List<Overlap> findOverlaps(APIUsageExample target,
			APIUsagePattern pattern) {
		Set<APIUsagePattern> subPatterns = getConnectedSubpatterns(pattern);
		for (APIUsagePattern aup : subPatterns) {
			List<Overlap> overlaps = of.findOverlaps(target, aup);
		}
		// Overlap overlap = new Overlap(pattern, target,
		// targetNodeByPatternNode,
		// targetEdgeByPatternEdge);
		return null;
	}

	private Set<APIUsagePattern> getConnectedSubpatterns(APIUsagePattern pattern) {

		Set<APIUsagePattern> results = Sets.newLinkedHashSet();


		return results;
	}
}
