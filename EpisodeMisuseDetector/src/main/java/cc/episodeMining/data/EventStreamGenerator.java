package cc.episodeMining.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class EventStreamGenerator {

	public List<Triplet<String, Event, List<Event>>> generateStructure(
			List<Event> stream) {
		List<Triplet<String, Event, List<Event>>> srcMapper = Lists
				.newLinkedList();

		String source = "";
		Event element = null;
		List<Event> method = Lists.newLinkedList();

		for (Event event : stream) {
			EventKind kind = event.getKind();
			if ((kind == EventKind.SOURCE_FILE_PATH)
					|| (kind == EventKind.METHOD_DECLARATION)
					|| (kind == EventKind.INITIALIZER)) {
				if (!source.isEmpty() && (element != null) && !method.isEmpty()) {
					srcMapper.add(new Triplet<String, Event, List<Event>>(
							source, element, method));
				}
				if (kind == EventKind.SOURCE_FILE_PATH) {
					source = event.getMethod().getFullName();
					element = null;
				}
				if ((kind == EventKind.METHOD_DECLARATION)
						|| (kind == EventKind.INITIALIZER)) {
					element = event;
				}
				method = Lists.newLinkedList();
				if (kind == EventKind.INITIALIZER) {
					method.add(event);
				}
			} else {
				method.add(event);
			}
		}
		if (!source.isEmpty() && (element != null) && !method.isEmpty()) {
			srcMapper.add(new Triplet<String, Event, List<Event>>(source,
					element, method));
		}
		return srcMapper;
	}

	public EventStream generateFiles(File path,
			List<Triplet<String, Event, List<Event>>> stream)
			throws IOException {
		EventStream es = new EventStream();

		for (Triplet<String, Event, List<Event>> triplet : stream) {
			for (Event event : triplet.getThird()) {
				es.addEvent(event);
			}
			es.increaseTimeout();
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
