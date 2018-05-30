package cc.episodeMining.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;

public class EventStreamGenerator {

	private File folder;

	public EventStreamGenerator(File dir) {
		this.folder = dir;
	}

	public List<Tuple<Event, List<Event>>> mdEventsMapper(List<Event> events,
			int frequency) {
		List<Tuple<Event, List<Event>>> mapper = Lists.newLinkedList();

		Event md = null;
		List<Event> method = Lists.newLinkedList();

		for (Event e : events) {
			if (e.getKind() == EventKind.METHOD_DECLARATION) {
				if ((md != null) && (method.size() > 1)) {
					mapper.add(Tuple.newTuple(md, method));
				}
				md = e;
				method = Lists.newLinkedList();
			} else {
				method.add(e);
			}
		}
		JsonUtils.toJson(mapper, getStreamObjectPath(frequency));
		return mapper;
	}

	public void eventStream(List<Tuple<Event, List<Event>>> stream,
			int frequency) throws IOException {
		EventStream es = new EventStream();

		for (Tuple<Event, List<Event>> tuple : stream) {
			for (Event event : tuple.getSecond()) {
				es.addEvent(event);
			}
			es.increaseTimeout();
		}
		FileUtils.writeStringToFile(getEventStreamPath(frequency),
				es.getStreamText());
		JsonUtils.toJson(es.getMapping(), getMapPath(frequency));
	}

	private String getPath(int frequency) {
		String pathName = folder.getAbsolutePath() + "/freq" + frequency + "/";
		return pathName;
	}

	private File getEventStreamPath(int frequency) {
		String streamName = getPath(frequency) + "stream.txt";
		return new File(streamName);
	}

	private File getMapPath(int frequency) {
		String mapName = getPath(frequency) + "mapping.txt";
		return new File(mapName);
	}

	private File getStreamObjectPath(int frequency) {
		String mapName = getPath(frequency) + "object.json";
		return new File(mapName);
	}
}
