package cc.episodeMining.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class EventsFilterTest {
	
	private static final int FREQUENCY = 2;

	private List<Event> stream;

	private List<Event> expected;
	
	private EventsFilter sut;

	@Before
	public void setup() {
		stream = Lists.newLinkedList();
		
		expected = Lists.newLinkedList();

		sut = new EventsFilter();
	}

	@Test
	public void emptyStream() {
		List<Event> duplicates = sut.duplicates(stream);
		List<Event> frequent = sut.frequent(stream, FREQUENCY);
		
		assertTrue(duplicates.isEmpty());
		assertTrue(frequent.isEmpty());
	}
	
	@Test
	public void localAPIs() {
		stream.add(createEvent("type1", "checkouts/prj1/link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		stream.add(createEvent("type1", "checkouts/prj2/link2/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type2", "..ctor", EventKind.INITIALIZER));
		stream.add(createEvent("type1", "m1", EventKind.CONSTRUCTOR));
		
		expected.add(createEvent("type1", "checkouts/prj1/link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		List<Event> actuals = sut.locals(stream);
		
		assertEquals(expected, actuals);
	}
	
	@Test
	public void noDuplicates() {
		stream.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		stream.add(createEvent("type1", "link2/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type2", "..ctor", EventKind.INITIALIZER));
		stream.add(createEvent("type1", "m1", EventKind.CONSTRUCTOR));
		
		expected.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		expected.add(createEvent("type1", "link2/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("type2", "..ctor", EventKind.INITIALIZER));
		expected.add(createEvent("type1", "m1", EventKind.CONSTRUCTOR));
		
		List<Event> actuals = sut.duplicates(stream);
		
		assertEquals(expected, actuals);
	}
	
	@Test
	public void duplicates() {
		stream.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		stream.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		stream.add(createEvent("type2", "link1/type2.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type2", ".type2.java", EventKind.RELATIVE_PATH));
		stream.add(createEvent("type2", "..ctor", EventKind.INITIALIZER));
		stream.add(createEvent("type1", "m1", EventKind.CONSTRUCTOR));
		
		expected.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type1", ".type1.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("type2", "m2", EventKind.INVOCATION));
		
		expected.add(createEvent("type2", "link1/type2.java", EventKind.ABSOLUTE_PATH));
		stream.add(createEvent("type2", ".type2.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("type2", "..ctor", EventKind.INITIALIZER));
		expected.add(createEvent("type1", "m1", EventKind.CONSTRUCTOR));
		
		List<Event> actuals = sut.duplicates(stream);
		
		assertEquals(expected, actuals);
	}
	
	@Test
	public void frequent() {
			stream.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
			
			stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
			stream.add(createEvent("type2", ".ctor", EventKind.CONSTRUCTOR));
			
			stream.add(createEvent("type1", "link2/type1.java", EventKind.ABSOLUTE_PATH));
			stream.add(createEvent("type2", "m2", EventKind.METHOD_DECLARATION));
			stream.add(createEvent("type2", ".ctor", EventKind.CONSTRUCTOR));
			
			expected.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
			expected.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
			expected.add(createEvent("type2", ".ctor", EventKind.CONSTRUCTOR));
			
			expected.add(createEvent("type1", "link2/type1.java", EventKind.ABSOLUTE_PATH));
			expected.add(createEvent("type2", "m2", EventKind.METHOD_DECLARATION));
			expected.add(createEvent("type2", ".ctor", EventKind.CONSTRUCTOR));
			
			List<Event> actuals = sut.frequent(stream, FREQUENCY);
			
			assertEquals(expected, actuals);
		}
	
	@Test
	public void initializer() {
			stream.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
			stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
			stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
			
			stream.add(createEvent("type1", "link2/type1.java", EventKind.ABSOLUTE_PATH));
			stream.add(createEvent("type2", "m2", EventKind.INITIALIZER));
			stream.add(createEvent("type2", "m2", EventKind.INVOCATION));
			
			expected.add(createEvent("type1", "link1/type1.java", EventKind.ABSOLUTE_PATH));
			expected.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
			expected.add(createEvent("type2", "m2", EventKind.INVOCATION));
			
			expected.add(createEvent("type1", "link2/type1.java", EventKind.ABSOLUTE_PATH));
			expected.add(createEvent("type2", "m2", EventKind.INITIALIZER));
			expected.add(createEvent("type2", "m2", EventKind.INVOCATION));
			
			List<Event> actuals = sut.frequent(stream, FREQUENCY);
			
			assertEquals(expected, actuals);
		}

	private Event createEvent(String type, String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + method + "()"));
		return event;
	}
}
