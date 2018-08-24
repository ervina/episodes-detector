package cc.episodeMining.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.Triplet;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Events;
import cc.kave.episodes.model.events.Fact;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

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
				.generateStructure(stream);

		assertTrue(actuals.isEmpty());
	}

	@Test
	public void structure() {
		Event source = createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		stream.add(source);
		stream.add(md);
		stream.add(event2);
		stream.add(event3);

		stream.add(source);
		stream.add(event1);
		stream.add(event3);
		stream.add(event2);

		String srcPath = source.getMethod().getFullName();
		List<Triplet<String, Event, List<Event>>> expected = Lists
				.newLinkedList();
		expected.add(new Triplet<String, Event, List<Event>>(srcPath, md, Lists
				.newArrayList(event2, event3)));
		expected.add(new Triplet<String, Event, List<Event>>(srcPath, event1,
				Lists.newArrayList(event1, event3, event2)));

		List<Triplet<String, Event, List<Event>>> actuals = sut
				.generateStructure(stream);

		assertEquals(expected, actuals);
	}

	@Test
	public void filesCreated() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		List<Triplet<String, Event, List<Event>>> eventStream = Lists
				.newLinkedList();
		eventStream.add(new Triplet<String, Event, List<Event>>(srcPath, md,
				Lists.newArrayList(event2, event3)));
		eventStream.add(new Triplet<String, Event, List<Event>>(srcPath,
				event1, Lists.newArrayList(event3, event2)));

		sut.generateFiles(eventStream);

		assertTrue(getStreamObjectPath().exists());
		assertTrue(getStreamPath().exists());
		assertTrue(getMapPath().exists());
	}

	@Test
	public void filesContent() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		List<Triplet<String, Event, List<Event>>> eventStream = Lists
				.newLinkedList();
		eventStream.add(new Triplet<String, Event, List<Event>>(srcPath, md,
				Lists.newArrayList(event2, event3)));
		eventStream.add(new Triplet<String, Event, List<Event>>(srcPath,
				event1, Lists.newArrayList(event1, event3, event2)));

		sut.generateFiles(eventStream);

		@SuppressWarnings("serial")
		Type type1 = new TypeToken<List<Triplet<String, Event, List<Event>>>>() {
		}.getType();
		List<Triplet<String, Event, List<Event>>> actualObject = JsonUtils
				.fromJson(getStreamObjectPath(), type1);

		String expectedStream = "1,0.000\n2,0.001\n3,5.002\n2,5.003\n1,5.004\n";
		String actualStream = FileUtils.readFileToString(getStreamPath());

		List<Event> expectedMap = Lists.newLinkedList();
		expectedMap.add(Events.newDummyEvent());
		expectedMap.add(event2);
		expectedMap.add(event3);
		expectedMap.add(event1);

		Type type2 = new TypeToken<List<Event>>() {
		}.getType();
		List<Event> actualMap = JsonUtils.fromJson(getMapPath(), type2);

		assertEquals(eventStream, actualObject);
		assertEquals(expectedStream, actualStream);
		assertEquals(expectedMap, actualMap);
	}

	private Event createEvent(String type, String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + method + "()"));
		return event;
	}

	private File getStreamPath() {
		String streamName = rootFolder.getRoot().getAbsolutePath()
				+ "/stream.txt";
		return new File(streamName);
	}

	private File getMapPath() {
		String mapName = rootFolder.getRoot().getAbsolutePath()
				+ "/mapping.txt";
		return new File(mapName);
	}

	private File getStreamObjectPath() {
		String mapName = rootFolder.getRoot().getAbsolutePath()
				+ "/object.json";
		return new File(mapName);
	}
}
