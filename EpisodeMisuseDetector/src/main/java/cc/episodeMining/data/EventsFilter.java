package cc.episodeMining.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventsFilter {

	public List<Event> locals(List<Event> stream) {
		List<Event> results = Lists.newLinkedList();

		Map<String, Tuple<Set<String>, List<Event>>> pte = projectTypeEvents(stream);

		return results;
	}

	public List<Event> duplicates(List<Event> stream) {
		List<Event> results = Lists.newLinkedList();
		Event source = null;
		List<Event> code = Lists.newLinkedList();

		for (Event event : stream) {
			if (event.getKind() == EventKind.SOURCE_FILE_PATH) {
				if ((source != null) && !code.isEmpty()) {
					results.add(source);
					results.addAll(code);
					source = null;
					code = Lists.newLinkedList();
				}
				if (results.contains(event)) {
					continue;
				}
				source = event;
			} else {
				code.add(event);
			}
		}
		if ((source != null) && !code.isEmpty()) {
			results.add(source);
			results.addAll(code);
		}
		return results;
	}

	public List<Event> frequent(List<Event> stream, int frequency) {
		List<Event> output = Lists.newLinkedList();

		Map<Event, Integer> eventsCounter = counter(stream);

		for (Event event : stream) {
			EventKind kind = event.getKind();
			if ((kind != EventKind.SOURCE_FILE_PATH)
					&& (kind != EventKind.METHOD_DECLARATION)
					&& (kind != EventKind.INITIALIZER)) {
				if (eventsCounter.get(event) >= frequency) {
					output.add(event);
				}
			} else {
				output.add(event);
			}
		}
		return output;
	}

	private Map<Event, Integer> counter(List<Event> stream) {
		Map<Event, Integer> results = Maps.newLinkedHashMap();

		for (Event e : stream) {
			if (results.containsKey(e)) {
				int freq = results.get(e);
				results.put(e, freq + 1);
			} else {
				results.put(e, 1);
			}
		}
		return results;
	}

	private Map<String, Tuple<Set<String>, List<Event>>> projectTypeEvents(
			List<Event> stream) {
		Map<String, Tuple<Set<String>, List<Event>>> results = Maps
				.newLinkedHashMap();

		EventStreamGenerator esg = new EventStreamGenerator();
		List<Triplet<String, Event, List<Event>>> structure = esg
				.generateStructure(stream);

		for (Triplet<String, Event, List<Event>> triplet : structure) {
			String source = triplet.getFirst();
			int start = source.indexOf("checkouts/");
			String substring = source.substring(start + 1);
			int end = substring.indexOf("/");
			String project = substring.substring(0, end);

			start = source.lastIndexOf("/");
			end = source.lastIndexOf(".java");
			String type = source.substring(start + 1, end);
			
			List<Event> method = Lists.newLinkedList();

			for (Event event : triplet.getThird()) {
				String eventType = event.getMethod().getDeclaringType()
						.getFullName();
				if (eventType.equals(type)) {
					method = Lists.newLinkedList();
					break;
				} 
				method.add(event);
			}
			if (!method.isEmpty()) {
				results.get(project).getFirst().add(type);
				results.get(project).getSecond().addAll(method);
			}
			method = Lists.newLinkedList();
		}
		return results;
	}
}
