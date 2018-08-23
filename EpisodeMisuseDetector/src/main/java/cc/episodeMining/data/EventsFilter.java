package cc.episodeMining.data;

import java.util.List;
import java.util.Map;

import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventsFilter {

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
}
