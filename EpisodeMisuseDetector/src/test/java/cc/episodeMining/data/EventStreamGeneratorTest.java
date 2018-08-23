package cc.episodeMining.data;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

import static org.junit.Assert.assertTrue;

public class EventStreamGeneratorTest {

	@Rule
	public TemporaryFolder rootFolder = new TemporaryFolder();

	List<Event> stream;

	private EventStreamGenerator sut;

	@Before
	public void setup() {
		stream = Lists.newLinkedList();

		sut = new EventStreamGenerator(rootFolder.getRoot());
	}

	@Test
	public void emptyStream() {
		List<Triplet<String, Event, List<Event>>> actuals = sut
				.createSrcMapper(stream);

		assertTrue(actuals.isEmpty());
	}

	@Test
	public void duplMD() {
		stream.add(createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type2", "m2", EventKind.INVOCATION));

		stream.add(createEvent("type1", "link2", EventKind.SOURCE_FILE_PATH));
		stream.add(createEvent("type1", "m1", EventKind.METHOD_DECLARATION));
		stream.add(createEvent("type1", "m1", EventKind.CONSTRUCTOR));

		List<Triplet<String, Event, List<Event>>> expected = Lists
				.newLinkedList();
		expected.add(new Triplet<String, Event, List<Event>>(Names.newMethod(
				"[?] [type1].link1()").getFullName(), createEvent("type1",
				"m1", EventKind.METHOD_DECLARATION), Lists
				.newArrayList(createEvent("type2", "m2", EventKind.INVOCATION))));
	}

	private Event createEvent(String type, String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + method + "()"));
		return event;
	}
}
