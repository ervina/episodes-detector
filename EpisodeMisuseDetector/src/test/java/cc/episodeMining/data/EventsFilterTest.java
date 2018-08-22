package cc.episodeMining.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EventsFilterTest {

	private List<Event> stream;

	private Map<IMethodName, List<Event>> expected;
	
	private EventsFilter sut;

	@Before
	public void setup() {
		stream = Lists.newLinkedList();
		
		expected = Maps.newLinkedHashMap();

		sut = new EventsFilter();
	}

	@Test
	public void emptyStream() {
		Map<IMethodName, List<Event>> actuals = sut.duplicates(stream);
		
		assertTrue(actuals.isEmpty());
	}
	
	@Test
	public void noDuplicates() {
		stream.add(createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		List<Event> events = Lists.newLinkedList();
		events.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		events.add(createEvent("type2", "m2", EventKind.INVOCATION));
		expected.put(Names.newMethod("[?] [type1].link1()"), events);
		
		Map<IMethodName, List<Event>> actuals = sut.duplicates(stream);
		
		assertEquals(expected, actuals);
	}

	private Event createEvent(String type, String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + method + "()"));
		return event;
	}
}
