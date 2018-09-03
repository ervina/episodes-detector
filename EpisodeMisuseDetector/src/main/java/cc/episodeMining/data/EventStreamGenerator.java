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

	public Map<String, List<Tuple<Event, List<Event>>>> fileMethodStructure(
			List<Event> stream) {
		Map<String, List<Tuple<Event, List<Event>>>> results = Maps
				.newLinkedHashMap();

		String source = "";
		Event element = null;
		List<Event> method = Lists.newLinkedList();

		for (Event event : stream) {
			EventKind kind = event.getKind();
			if (kind == EventKind.SOURCE_FILE_PATH) {
				if (!source.isEmpty() && (element != null) && !method.isEmpty()) {
					if (results.containsKey(source)) {
						results.get(source)
								.add(Tuple.newTuple(element, method));
					} else {
						results.put(source, Lists.newArrayList(Tuple.newTuple(
								element, method)));
					}
				}
				source = event.getMethod().getFullName();
				element = null;
				method = Lists.newLinkedList();
			} else if ((kind == EventKind.METHOD_DECLARATION)
					|| (kind == EventKind.INITIALIZER)) {
				if (!source.isEmpty() && (element != null) && !method.isEmpty()) {
					if (results.containsKey(source)) {
						results.get(source)
								.add(Tuple.newTuple(element, method));
					} else {
						results.put(source, Lists.newArrayList(Tuple.newTuple(
								element, method)));
					}
				}
				element = event;
				method = Lists.newLinkedList();
			} else {
				method.add(event);
			}
		}
		if (!source.isEmpty() && (element != null) && !method.isEmpty()) {
			if (results.containsKey(source)) {
				results.get(source).add(Tuple.newTuple(element, method));
			} else {
				results.put(source,
						Lists.newArrayList(Tuple.newTuple(element, method)));
			}
		}
		return results;
	}

	public EventStream generateFiles(File path,
			Map<String, List<Tuple<Event, List<Event>>>> stream)
			throws IOException {
		EventStream es = new EventStream();

		for (Map.Entry<String, List<Tuple<Event, List<Event>>>> entry : stream
				.entrySet()) {
			for (Tuple<Event, List<Event>> tuple : entry.getValue()) {
				for (Event event : tuple.getSecond()) {
					es.addEvent(event);
				}
				es.increaseTimeout();
			}
		}
		JsonUtils.toJson(stream, getStreamObjectPath(path));
		FileUtils.writeStringToFile(getEventStreamPath(path),
				es.getStreamText());
		JsonUtils.toJson(es.getMapping(), getMapPath(path));

		return es;
	}

	private File getEventStreamPath(File folder) {
		String streamName = folder.getAbsolutePath() + "/stream.txt";
		return new File(streamName);
	}

	private File getMapPath(File folder) {
		String mapName = folder.getAbsolutePath() + "/mapping.txt";
		return new File(mapName);
	}

	private File getStreamObjectPath(File folder) {
		String mapName = folder.getAbsolutePath() + "/object.json";
		return new File(mapName);
	}
}
