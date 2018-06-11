package cc.episodeMining.mudetect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.Episode;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class EpisodesToPatternTransformer {
	public Set<APIUsagePattern> transform(Set<Episode> episodes,
			List<Event> mapping) {
		Set<APIUsagePattern> patterns = new HashSet<>();
		for (Episode episode : episodes) {
			if (containsDisconnectedNodes(episode)) {
				Set<Episode> genEpisode = generateAllPossible(episode);
				for (Episode ep : genEpisode) {
					patterns.add(transform(ep, mapping));
				}
			} else {
				patterns.add(transform(episode, mapping));
			}
		}
		return patterns;
	}

	private Set<Episode> generateAllPossible(Episode episode) {
		List<Set<Fact>> connectedFacts = getConnectedNodes(episode);
		Set<Fact> disconnectedFacts = getDisconnectedNodes(episode,
				connectedFacts);
		List<Fact> discFacts = convertToList(disconnectedFacts);
		Set<Episode> generatedEpisodes = Sets.newLinkedHashSet();

		if (connectedFacts.isEmpty()) {
			generatedEpisodes.addAll(generateEpisodes(episode, discFacts));
		} else {
			generatedEpisodes.addAll(generate(episode, connectedFacts,
					discFacts));
		}
		return generatedEpisodes;
	}

	private Set<Episode> generate(Episode episode, List<Set<Fact>> connFacts,
			List<Fact> discFacts) {
		Set<Episode> allEpisodes = Sets.newLinkedHashSet();
		int maxIdx = connFacts.size() - 1;

		for (int idx = 0; idx < connFacts.size(); idx++) {
			Set<Fact> relations = Sets.newLinkedHashSet();
			for (Fact fact : discFacts) {
				if (idx == 0) {
					for (Fact exist : connFacts.get(idx + 1)) {
						relations.add(new Fact(fact, exist));
					}
				}
				if ((idx > 0) && (idx < maxIdx)) {
					for (Fact exist : connFacts.get(idx - 1)) {
						relations.add(new Fact(exist, fact));
					}
					for (Fact exist : connFacts.get(idx + 1)) {
						relations.add(new Fact(fact, exist));
					}
				}
				if (idx == maxIdx) {
					for (Fact exist : connFacts.get(idx - 1)) {
						relations.add(new Fact(exist, fact));
					}
				}
			}
			allEpisodes.add(createEpisode(episode, relations));
		}
		return allEpisodes;
	}

	private List<Fact> convertToList(Set<Fact> set) {
		List<Fact> list = Lists.newLinkedList();
		for (Fact fact : set) {
			list.add(fact);
		}
		return list;
	}

	private Set<Episode> generateEpisodes(Episode episode,
			List<Fact> disconnected) {
		Set<Episode> episodes = Sets.newLinkedHashSet();

		for (int idx1 = 0; idx1 < disconnected.size(); idx1++) {
			Set<Fact> relations1 = Sets.newLinkedHashSet();
			Set<Fact> relations2 = Sets.newLinkedHashSet();

			for (int idx2 = 0; idx2 < disconnected.size(); idx2++) {
				if (idx1 != idx2) {
					relations1.add(new Fact(new Fact(idx1), new Fact(idx2)));
					relations2.add(new Fact(new Fact(idx2), new Fact(idx1)));
				}
			}
			episodes.add(createEpisode(episode, relations1));
			episodes.add(createEpisode(episode, relations2));
		}
		return episodes;
	}

	private Episode createEpisode(Episode episode, Set<Fact> relations) {
		Episode newEpisode = new Episode();
		newEpisode.setFrequency(episode.getFrequency());
		newEpisode.setEntropy(episode.getEntropy());

		for (Fact fact : episode.getFacts()) {
			newEpisode.addFact(fact);
		}
		for (Fact relation : relations) {
			newEpisode.addFact(relation);
		}
		return newEpisode;
	}

	private Set<Fact> getDisconnectedNodes(Episode episode,
			List<Set<Fact>> connectedFacts) {
		Set<Fact> disconnected = Sets.newLinkedHashSet();
		Set<Fact> connected = convertToSet(connectedFacts);

		for (Fact fact : episode.getEvents()) {
			if (!connected.contains(fact)) {
				disconnected.add(fact);
			}
		}
		return disconnected;
	}

	private Set<Fact> convertToSet(List<Set<Fact>> connectedFacts) {
		Set<Fact> facts = Sets.newLinkedHashSet();
		for (Set<Fact> set : connectedFacts) {
			facts.addAll(set);
		}
		return facts;
	}

	private List<Set<Fact>> getConnectedNodes(Episode episode) {
		List<Set<Fact>> connectedNodes = Lists.newLinkedList();
		Map<Fact, Integer> firstFacts = getFactsAsFirstNodeInRelation(episode);
		TreeSet<Integer> sortedFirsts = sortFirstFacts(firstFacts);

		for (Integer value : sortedFirsts.descendingSet()) {
			Set<Fact> facts = Sets.newLinkedHashSet();

			for (Map.Entry<Fact, Integer> entry : firstFacts.entrySet()) {
				if (entry.getValue() == value) {
					facts.add(entry.getKey());
				}
			}
			connectedNodes.add(facts);
		}
		return connectedNodes;
	}

	private TreeSet<Integer> sortFirstFacts(Map<Fact, Integer> factCounter) {
		TreeSet<Integer> sort = Sets.newTreeSet();
		for (Map.Entry<Fact, Integer> entry : factCounter.entrySet()) {
			sort.add(entry.getValue());
		}
		return sort;
	}

	private Map<Fact, Integer> getFactsAsFirstNodeInRelation(Episode episode) {
		Map<Fact, Integer> firstFacts = Maps.newLinkedHashMap();

		for (Fact relation : episode.getRelations()) {
			Tuple<Fact, Fact> tuple = relation.getRelationFacts();
			Fact first = tuple.getFirst();
			Fact second = tuple.getSecond();
			if (firstFacts.containsKey(first)) {
				int counter = firstFacts.get(first);
				firstFacts.put(first, counter + 1);
			} else {
				firstFacts.put(first, 1);
			}
			firstFacts.put(second, 0);
		}
		return firstFacts;
	}

	private boolean containsDisconnectedNodes(Episode episode) {
		Set<Fact> relFacts = Sets.newHashSet();

		for (Fact fact : episode.getRelations()) {
			Tuple<Fact, Fact> relation = fact.getRelationFacts();
			relFacts.add(relation.getFirst());
			relFacts.add(relation.getSecond());
		}
		if (relFacts.size() < episode.getNumEvents()) {
			return true;
		}
		return false;
	}

	public APIUsagePattern transform(Episode episode, List<Event> mapping) {
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
}
