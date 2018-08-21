package cc.episodeMining.data;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class EventsFilterTest {

	private List<Event> stream;

	private EventsFilter sut;

	@Before
	public void setup() {
		stream = Lists.newLinkedList();

		sut = new EventsFilter();
	}

	@Test
	public void noDuplicates() {
		stream.add(createEvent("type1", "method1", EventKind.SOURCE_FILE_PATH));
	}

	private Event createEvent(String type, String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + method + "()"));
		return event;
	}
}
