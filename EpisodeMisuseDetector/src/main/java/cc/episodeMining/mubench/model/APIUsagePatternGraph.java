package cc.episodeMining.mubench.model;

import java.util.Set;

import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class APIUsagePatternGraph extends APIUsagePattern{

	private static final long serialVersionUID = 3552596016817244377L;

	public APIUsagePatternGraph(int support, Set<Location> exampleLocations) {
		super(support, exampleLocations);
	}

	public boolean isDisconnectedGraph(APIUsagePattern pattern) {
		// to be implemented
		return false;
	}
	
	public boolean isSetNodes(APIUsagePattern pattern) {
		// to be implemented
		return false;
	}
	
	public boolean isMixedGraph(APIUsagePattern pattern) {
		// to be implemented
		return false;
	}
}
