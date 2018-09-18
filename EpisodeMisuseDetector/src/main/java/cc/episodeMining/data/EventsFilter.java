package cc.episodeMining.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.episodeMining.mubench.model.EventGenerator;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EventsFilter {

	public List<Event> locals(List<Event> stream) {
		List<Event> noLocals = Lists.newLinkedList();

		Map<String, Tuple<Set<String>, List<Event>>> pte = projectTypeEvents(stream);
		System.out
				.println("Number of absolute classes in project-type-events: "
						+ numAbsClasses(pte));
		System.out
				.println("Number of relative classes in project-type-events: "
						+ numRelClasses(pte));

		for (Map.Entry<String, Tuple<Set<String>, List<Event>>> entry : pte
				.entrySet()) {
			Tuple<Set<String>, List<Event>> tuple = entry.getValue();
			for (Event event : tuple.getSecond()) {
				EventKind kind = event.getKind();
				if ((kind == EventKind.ABSOLUTE_PATH)
						|| (kind == EventKind.RELATIVE_PATH)
						|| (kind == EventKind.METHOD_DECLARATION)
						|| (kind == EventKind.INITIALIZER)) {
					noLocals.add(event);
				} else {
					String type = event.getMethod().getDeclaringType()
							.getFullName();
					if (!tuple.getFirst().contains(type)) {
						noLocals.add(event);
					}
				}
			}
		}
		return removeEmptyMethods(noLocals);
	}

	private int numAbsClasses(Map<String, Tuple<Set<String>, List<Event>>> pte) {
		int counter = 0;
		for (Map.Entry<String, Tuple<Set<String>, List<Event>>> entry : pte
				.entrySet()) {
			Tuple<Set<String>, List<Event>> tuple = entry.getValue();
			for (Event event : tuple.getSecond()) {
				if (event.getKind() == EventKind.ABSOLUTE_PATH) {
					counter++;
				}
			}
		}
		return counter;
	}

	private int numRelClasses(Map<String, Tuple<Set<String>, List<Event>>> pte) {
		int counter = 0;
		for (Map.Entry<String, Tuple<Set<String>, List<Event>>> entry : pte
				.entrySet()) {
			Tuple<Set<String>, List<Event>> tuple = entry.getValue();
			for (Event event : tuple.getSecond()) {
				if (event.getKind() == EventKind.RELATIVE_PATH) {
					counter++;
				}
			}
		}
		return counter;
	}

	public List<Event> duplicates(List<Event> stream) {
		List<Event> results = Lists.newLinkedList();
		Event source = null;
		List<Event> code = Lists.newLinkedList();

		for (Event event : stream) {
			if (event.getKind() == EventKind.ABSOLUTE_PATH) {
				if ((source != null) && !code.isEmpty()) {
					results.add(source);
					results.addAll(code);
				}
				source = null;
				code = Lists.newLinkedList();
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
		return removeEmptyMethods(results);
	}

	public List<Event> frequent(List<Event> stream, int frequency) {
		List<Event> results = Lists.newLinkedList();

		Map<Event, Integer> eventsCounter = counter(stream);

		for (Event event : stream) {
			EventKind kind = event.getKind();
			if ((kind == EventKind.ABSOLUTE_PATH)
					|| (kind == EventKind.RELATIVE_PATH)
					|| (kind == EventKind.METHOD_DECLARATION)
					|| (kind == EventKind.INITIALIZER)) {
				results.add(event);
			} else if (eventsCounter.get(event) >= frequency) {
				results.add(event);
			}
		}
		return removeEmptyMethods(results);
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
		Map<String, List<Tuple<Event, List<Event>>>> structure = esg
				.absoluteFileMethodStructure(stream);
		System.out.println("Absolute classes in file-method: "
				+ structure.size());
		System.out.println("Relative classes in file-method "
				+ getNumRelClasses(structure));

		for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : structure
				.entrySet()) {
			String source = entry.getKey();
			int start = source.indexOf("checkouts/");
			String substring = source.substring(start + 1);
			int end = substring.indexOf("/");
			String project = substring.substring(0, end);

			start = source.lastIndexOf("/");
			end = source.lastIndexOf(".java");
			String type = source.substring(start + 1, end);

			if (results.containsKey(project)) {
				results.get(project).getFirst().add(type);
			} else {
				Set<String> types = Sets.newLinkedHashSet();
				types.add(type);
				results.put(project,
						Tuple.newTuple(types, Lists.newLinkedList()));
			}
			results.get(project).getSecond()
					.add(EventGenerator.absolutePath(entry.getKey()));
			for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
				if (tuple.getFirst() != null) {
					results.get(project).getSecond().add(tuple.getFirst());
				}
				results.get(project).getSecond().addAll(tuple.getSecond());
			}
		}
		return results;
	}

	private int getNumRelClasses(
			Map<String, List<Tuple<Event, List<Event>>>> structure) {
		int counter = 0;
		for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : structure
				.entrySet()) {
			for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
				for (Event event : tuple.getSecond()) {
					if (event.getKind() == EventKind.RELATIVE_PATH) {
						counter++;
					}
				}
			}
		}
		return counter;
	}

	private List<Event> removeEmptyMethods(List<Event> stream) {
		List<Event> results = Lists.newLinkedList();

		EventStreamGenerator esg = new EventStreamGenerator();
		Map<String, List<Tuple<Event, List<Event>>>> structure = esg
				.absoluteFileMethodStructure(stream);
		for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : structure
				.entrySet()) {
			List<Event> classEvents = Lists.newLinkedList();
			for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
				if (validMethod(tuple.getSecond())) {
					classEvents.add(tuple.getFirst());
					classEvents.addAll(tuple.getSecond());
				} else {
					Event relativePath = checkRelativePath(tuple.getSecond());
					if (relativePath != null) {
						classEvents.add(relativePath);
					}
				}
			}
			if (classEvents.size() > 1) {
				results.add(EventGenerator.absolutePath(entry.getKey()));
				results.addAll(classEvents);
			}
		}
		return results;
	}

	private Event checkRelativePath(List<Event> method) {
		for (Event event : method) {
			if (event.getKind() == EventKind.RELATIVE_PATH) {
				return event;
			}
		}
		return null;
	}

	private boolean validMethod(List<Event> method) {
		for (Event event : method) {
			EventKind kind = event.getKind();
			if ((kind == EventKind.CONSTRUCTOR)
					|| (kind == EventKind.INVOCATION)) {
				return true;
			}
		}
		return false;
	}
}
