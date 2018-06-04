package cc.episodeMining.data;

import java.util.List;
import java.util.Map;

import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventsFilter {

	public List<Event> frequent(List<Event> sequence, int frequency) {
		List<Event> output = Lists.newLinkedList();

		Map<Event, Integer> eventsCounter = counter(sequence);

		for (Event event : sequence) {
			if ((event.getKind() != EventKind.SOURCE_FILE_PATH)
					&& (event.getKind() != EventKind.METHOD_DECLARATION)) {
				if (eventsCounter.get(event) >= frequency) {
					output.add(event);
				}
			} else {
				output.add(event);
			}
		}
		return output;
	}

	private Map<Event, Integer> counter(List<Event> events) {
		Map<Event, Integer> results = Maps.newLinkedHashMap();

		for (Event e : events) {
			if (e.getKind() != EventKind.METHOD_DECLARATION) {
				if (results.containsKey(e)) {
					int freq = results.get(e);
					results.put(e, freq + 1);
				} else {
					results.put(e, 1);
				}
			}
		}
		return results;
	}
}
