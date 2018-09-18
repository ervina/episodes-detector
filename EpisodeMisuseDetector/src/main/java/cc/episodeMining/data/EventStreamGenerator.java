package cc.episodeMining.data;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventStreamGenerator {

	public Map<String, List<Tuple<Event, List<Event>>>> absoluteFileMethodStructure(
			List<Event> stream) {
		Map<String, List<Tuple<Event, List<Event>>>> results = Maps
				.newLinkedHashMap();

		String absPath = "";
		Event element = null;
		List<Event> method = Lists.newLinkedList();

		for (Event event : stream) {
			EventKind kind = event.getKind();
			if (kind == EventKind.ABSOLUTE_PATH) {
				if (!absPath.isEmpty() && (element != null)
						&& !method.isEmpty()) {
					results.get(absPath).add(Tuple.newTuple(element, method));
				}
				absPath = event.getMethod().getFullName();
				results.put(absPath, Lists.newLinkedList());
				element = null;
				method = Lists.newLinkedList();
			} else if (kind == EventKind.RELATIVE_PATH) {
				results.get(absPath).add(
						Tuple.newTuple(null, Lists.newArrayList(event)));
			} else if ((kind == EventKind.METHOD_DECLARATION)
					|| (kind == EventKind.INITIALIZER)) {
				if ((element != null) && !method.isEmpty()) {
					results.get(absPath).add(Tuple.newTuple(element, method));
				}
				element = event;
				method = Lists.newLinkedList();
			} else {
				method.add(event);
			}
		}
		if (!absPath.isEmpty() && (element != null) && !method.isEmpty()) {
			results.get(absPath).add(Tuple.newTuple(element, method));
		}
		return results;
	}

	public Map<String, List<Tuple<Event, List<Event>>>> relativeFileMethodStructure(
			Map<String, List<Tuple<Event, List<Event>>>> stream) {
		Map<String, List<Tuple<Event, List<Event>>>> results = Maps
				.newLinkedHashMap();
		for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : stream
				.entrySet()) {
			Event relativePath = getRelativePath(entry.getValue());
			if (relativePath == null) {
				System.err.println("Can't find relative path");
			}
			List<Tuple<Event, List<Event>>> classEvents = Lists.newLinkedList();
			for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
				if (tuple.getFirst() != null) {
					classEvents.add(tuple);
				}
			}
			results.put(relativePath.getMethod().getFullName(),
					classEvents);
		}
		return results;
	}

	public EventStream generateFiles(File path, int frequency,
			Map<String, List<Tuple<Event, List<Event>>>> stream)
			throws IOException {
		EventStream es = new EventStream();

		for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : stream
				.entrySet()) {
			for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
				for (Event event : tuple.getSecond()) {
					if (event.getKind() != EventKind.RELATIVE_PATH) {
						es.addEvent(event);
					}
				}
				es.increaseTimeout();
			}
		}
		JsonUtils.toJson(stream, getStreamObjectPath(path, frequency));
		FileUtils.writeStringToFile(getEventStreamPath(path, frequency),
				es.getStreamText());
		JsonUtils.toJson(es.getMapping(), getMapPath(path, frequency));

		return es;
	}

	private Event getRelativePath(List<Tuple<Event, List<Event>>> events) {
		for (Tuple<Event, List<Event>> tuple : events) {
			for (Event e : tuple.getSecond()) {
				if (e.getKind() == EventKind.RELATIVE_PATH) {
					return e;
				}
			}
		}
		return null;
	}

	private String getPath(File folder, int frequency) {
		String path = folder.getAbsolutePath() + "/freq" + frequency + "/";
		return path;
	}

	private File getEventStreamPath(File folder, int frequency) {
		String streamName = getPath(folder, frequency) + "stream.txt";
		return new File(streamName);
	}

	private File getMapPath(File folder, int frequency) {
		String mapName = getPath(folder, frequency) + "mapping.txt";
		return new File(mapName);
	}

	private File getStreamObjectPath(File folder, int frequency) {
		String mapName = getPath(folder, frequency) + "object.json";
		return new File(mapName);
	}
}
