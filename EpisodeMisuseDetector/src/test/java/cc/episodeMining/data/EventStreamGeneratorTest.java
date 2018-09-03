package cc.episodeMining.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.utils.json.JsonUtils;
import cc.kave.episodes.model.EventStream;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;
import cc.kave.episodes.model.events.Events;
import cc.recommenders.datastructures.Tuple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

public class EventStreamGeneratorTest {

	@Rule
	public TemporaryFolder rootFolder = new TemporaryFolder();

	List<Event> stream;

	private EventStreamGenerator sut;

	@Before
	public void setup() {
		stream = Lists.newLinkedList();

		sut = new EventStreamGenerator();
	}

	@Test
	public void emptyStream() {
		Map<String, List<Tuple<Event, List<Event>>>> actuals = sut
				.fileMethodStructure(stream);

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
		Map<String, List<Tuple<Event, List<Event>>>> expected = Maps
				.newLinkedHashMap();
		expected.put(srcPath, Lists.newArrayList(
				Tuple.newTuple(md, Lists.newArrayList(event2, event3)),
				Tuple.newTuple(event1, Lists.newArrayList(event3, event2))));

		Map<String, List<Tuple<Event, List<Event>>>> actuals = sut
				.fileMethodStructure(stream);

		assertEquals(expected, actuals);
	}

	@Test
	public void eventstream() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> eventStream = Maps.newLinkedHashMap();
		eventStream.put(srcPath, Lists.newLinkedList());
		eventStream.get(srcPath).add(Tuple.newTuple(md, Lists.newArrayList(event2, event3)));
		eventStream.get(srcPath).add(Tuple.newTuple(event1, Lists.newArrayList(event3, event2)));

		EventStream expected = new EventStream();
		expected.addEvent(event2);
		expected.addEvent(event3);
		expected.increaseTimeout();
		expected.addEvent(event3);
		expected.addEvent(event2);
		expected.increaseTimeout();

		EventStream actuals = sut
				.generateFiles(rootFolder.getRoot(), eventStream);

		assertEquals(expected.getMapping(), actuals.getMapping());
		assertEquals(expected.getStreamText(), actuals.getStreamText());
	}

	@Test
	public void filesCreated() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.SOURCE_FILE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> eventStream = Maps.newLinkedHashMap();
		eventStream.put(srcPath, Lists.newLinkedList());
		eventStream.get(srcPath).add(Tuple.newTuple(md, Lists.newArrayList(event2, event3)));
		eventStream.get(srcPath).add(Tuple.newTuple(event1, Lists.newArrayList(event3, event2)));

		sut.generateFiles(rootFolder.getRoot(), eventStream);

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
		Map<String, List<Tuple<Event, List<Event>>>> eventStream = Maps.newLinkedHashMap();
		eventStream.put(srcPath, Lists.newLinkedList());
		eventStream.get(srcPath).add(Tuple.newTuple(md, Lists.newArrayList(event2, event3)));
		eventStream.get(srcPath).add(Tuple.newTuple(event1, Lists.newArrayList(event3, event2)));

		sut.generateFiles(rootFolder.getRoot(), eventStream);

		@SuppressWarnings("serial")
		Type type1 = new TypeToken<Map<String, List<Tuple<Event, List<Event>>>>>() {
		}.getType();
		Map<String, List<Tuple<Event, List<Event>>>> actualObject = JsonUtils
				.fromJson(getStreamObjectPath(), type1);

		String expectedStream = "1,0.000\n2,0.001\n2,5.002\n1,5.003\n";
		String actualStream = FileUtils.readFileToString(getStreamPath());

		List<Event> expectedMap = Lists.newLinkedList();
		expectedMap.add(Events.newDummyEvent());
		expectedMap.add(event2);
		expectedMap.add(event3);

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
