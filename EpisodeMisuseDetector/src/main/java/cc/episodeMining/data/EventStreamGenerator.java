package cc.episodeMining.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import cc.episodeMining.mubench.model.EventGenerator;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class EventStreamGenerator {

	private File folder;

	public EventStreamGenerator(File dir) {
		this.folder = dir;
	}

	public List<Triplet<String, Event, List<Event>>> createSrcMapper(
			List<Event> events, int frequency) {
		List<Triplet<String, Event, List<Event>>> srcMapper = Lists
				.newLinkedList();

		String source = null;
		Event md = null;
		List<Event> method = Lists.newLinkedList();

		for (Event e : events) {
			if (e.getKind() == EventKind.SOURCE_FILE_PATH) {
				source = e.getMethod().getFullName();
			} else if (e.getKind() == EventKind.METHOD_DECLARATION) {
				if (!method.isEmpty()) {
					if (md == null) {
						md = EventGenerator.sourcePath(source);
					}
					srcMapper.add(new Triplet<String, Event, List<Event>>(source,
							md, method));
				}
				md = e;
				method = Lists.newLinkedList();
			} else {
				method.add(e);
			}
		}
		if (!method.isEmpty()) {
			srcMapper.add(new Triplet<String, Event, List<Event>>(source, md, method));
		}
		JsonUtils.toJson(srcMapper, getStreamObjectPath(frequency));
		return srcMapper;
	}

	public void eventStream(List<Triplet<String, Event, List<Event>>> stream,
			int frequency) throws IOException {
		EventStream es = new EventStream();

		for (Triplet<String, Event, List<Event>> triplet : stream) {
			for (Event event : triplet.getThird()) {
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
