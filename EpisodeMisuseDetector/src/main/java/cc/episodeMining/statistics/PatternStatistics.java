package cc.episodeMining.statistics;

import java.util.Map;
import java.util.Set;

import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Sets;

public class PatternStatistics {

	public void compute(Map<Integer, Set<Episode>> patterns) {
		System.out.println("NumNodes\t<1DiscNode\tNoOrder\tTotal");
		for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
			int noOrder = 0;
			int disconnected = 0;

			for (Episode p : entry.getValue()) {
				if (p.getRelations().size() == 0) {
					noOrder++;
				} else if (numDiscNodes(p) > 0) {
					disconnected++;
				}
			}
			System.out.println(entry.getKey() + "\t" + disconnected + "\t"
					+ noOrder + "\t" + entry.getValue().size());
		}
	}

	public void DiscNodes(Map<Integer, Set<Episode>> patterns) {
		System.out.println("Number of disconnected nodes:"); 
		for (Map.Entry<Integer, Set<Episode>> entry : patterns.entrySet()) {
			System.out.println(entry.getKey() + "-node patterns");
			for (Episode p : entry.getValue()) {
				System.out.println(numDiscNodes(p));
			}
			System.out.println();
		}
	}
	
	private int numDiscNodes(Episode p) {
		Set<Fact> relFacts = Sets.newLinkedHashSet();
		for (Fact relation : p.getRelations()) {
			Tuple<Fact, Fact> tuple = relation.getRelationFacts();
			relFacts.add(tuple.getFirst());
			relFacts.add(tuple.getSecond());
		}
		return (p.getNumEvents() - relFacts.size());
	}
}
