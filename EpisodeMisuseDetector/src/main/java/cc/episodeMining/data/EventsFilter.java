package cc.episodeMining.data;

import java.util.List;
import java.util.Map;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventsFilter {

	public Map<IMethodName, List<Event>> duplicates(List<Event> stream) {
		Map<IMethodName, List<Event>> results = Maps.newLinkedHashMap();
		IMethodName methodName = null;
		List<Event> code = Lists.newLinkedList();

		for (Event event : stream) {
			if (event.getKind() == EventKind.SOURCE_FILE_PATH) {
				IMethodName method = event.getMethod();
				if ((methodName != null) && !code.isEmpty()) {
					results.put(methodName, code);
					methodName = null;
					code = Lists.newLinkedList();
				}
				if (results.containsKey(method)) {
					continue;
				}
				methodName = method;
			}
			code.add(event);
		}
		if ((methodName != null) && !code.isEmpty()) {
			results.put(methodName, code);
		}
		return results;
	}

	public Map<IMethodName, List<Event>> frequent(
			Map<IMethodName, List<Event>> stream, int frequency) {
		Map<IMethodName, List<Event>> output = Maps.newLinkedHashMap();

		Map<Event, Integer> eventsCounter = counter(stream);

		for (Map.Entry<IMethodName, List<Event>> entry : stream.entrySet()) {
			List<Event> events = Lists.newLinkedList();
			for (Event event : entry.getValue()) {
				if (event.getKind() != EventKind.METHOD_DECLARATION) {
					if (eventsCounter.get(event) >= frequency) {
						events.add(event);
					}
				} else {
					events.add(event);
				}
			}
			if (!events.isEmpty()) {
				output.put(entry.getKey(), events);
			}
		}
		return output;
	}

	private Map<Event, Integer> counter(Map<IMethodName, List<Event>> stream) {
		Map<Event, Integer> results = Maps.newLinkedHashMap();

		for (Map.Entry<IMethodName, List<Event>> entry : stream.entrySet()) {
			for (Event e : entry.getValue()) {
				if (e.getKind() != EventKind.METHOD_DECLARATION) {
					if (results.containsKey(e)) {
						int freq = results.get(e);
						results.put(e, freq + 1);
					} else {
						results.put(e, 1);
					}
				}
			}
		}
		return results;
	}
}
