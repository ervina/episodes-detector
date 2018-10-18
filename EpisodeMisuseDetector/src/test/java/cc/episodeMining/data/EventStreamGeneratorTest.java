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

	public static final int FREQUENCY = 10;

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
				.absoluteFileMethodStructure(stream);

		assertTrue(actuals.isEmpty());
	}

	@Test
	public void structure() {
		Event source = createEvent("type1", "link1", EventKind.ABSOLUTE_PATH);
		Event relPath = createEvent("type1", "link1.java",
				EventKind.RELATIVE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		stream.add(source);
		stream.add(relPath);
		stream.add(md);
		stream.add(event2);
		stream.add(event3);

		stream.add(event1);
		stream.add(event3);
		stream.add(event2);

		String srcPath = source.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> expected = Maps
				.newLinkedHashMap();
		expected.put(srcPath, Lists.newArrayList(
				Tuple.newTuple(null, Lists.newArrayList(relPath)),
				Tuple.newTuple(md, Lists.newArrayList(event2, event3)),
				Tuple.newTuple(event1, Lists.newArrayList(event3, event2))));

		Map<String, List<Tuple<Event, List<Event>>>> actuals = sut
				.absoluteFileMethodStructure(stream);

		assertEquals(expected, actuals);
	}

	@Test
	public void threeMethods() {
		Event src = createEvent("type1", "link1", EventKind.ABSOLUTE_PATH);
		Event relPath = createEvent("type1", "link1.java",
				EventKind.RELATIVE_PATH);
		Event init = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event md1 = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event md2 = createEvent("type2", "m2", EventKind.METHOD_DECLARATION);
		Event inv = createEvent("type2", "m2", EventKind.INVOCATION);
		Event constr = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		stream.add(src);
		stream.add(relPath);
		stream.add(init);
		stream.add(constr);
		stream.add(inv);

		stream.add(md1);
		stream.add(inv);

		stream.add(md2);
		stream.add(constr);

		String srcPath = src.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> expected = Maps
				.newLinkedHashMap();
		Tuple<Event, List<Event>> tuple0 = Tuple.newTuple(null,
				Lists.newArrayList(relPath));
		Tuple<Event, List<Event>> tuple1 = Tuple.newTuple(init,
				Lists.newArrayList(constr, inv));
		Tuple<Event, List<Event>> tuple2 = Tuple.newTuple(md1,
				Lists.newArrayList(inv));
		Tuple<Event, List<Event>> tuple3 = Tuple.newTuple(md2,
				Lists.newArrayList(constr));
		expected.put(srcPath,
				Lists.newArrayList(tuple0, tuple1, tuple2, tuple3));

		Map<String, List<Tuple<Event, List<Event>>>> actuals = sut
				.absoluteFileMethodStructure(stream);

		assertEquals(expected, actuals);
	}

	@Test
	public void threeClasses() {
		Event src1 = createEvent("type1", "link1", EventKind.ABSOLUTE_PATH);
		Event src2 = createEvent("type2", "link2", EventKind.ABSOLUTE_PATH);
		Event src3 = createEvent("type3", "link3", EventKind.ABSOLUTE_PATH);

		Event rp1 = createEvent("type1", "link1.java", EventKind.RELATIVE_PATH);
		Event rp2 = createEvent("type2", "link2.java", EventKind.RELATIVE_PATH);
		Event rp3 = createEvent("type3", "link3.java", EventKind.RELATIVE_PATH);

		Event md1 = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event md2 = createEvent("type2", "m2", EventKind.METHOD_DECLARATION);
		Event init = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event1 = createEvent("type1", "m1", EventKind.INVOCATION);
		Event event2 = createEvent("type2", "ctor", EventKind.CONSTRUCTOR);
		Event event3 = createEvent("type3", "m3", EventKind.INVOCATION);

		stream.add(src1);
		stream.add(rp1);
		stream.add(md1);
		stream.add(event2);
		stream.add(event1);

		stream.add(src2);
		stream.add(rp2);
		stream.add(init);
		stream.add(event2);
		stream.add(event3);

		stream.add(src3);
		stream.add(rp3);
		stream.add(md2);
		stream.add(event2);
		stream.add(event1);
		stream.add(event3);

		String srcPath1 = src1.getMethod().getFullName();
		String srcPath2 = src2.getMethod().getFullName();
		String srcPath3 = src3.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> expected = Maps
				.newLinkedHashMap();
		Tuple<Event, List<Event>> tuple0 = Tuple.newTuple(null,
				Lists.newArrayList(rp1));
		Tuple<Event, List<Event>> tuple1 = Tuple.newTuple(md1,
				Lists.newArrayList(event2, event1));
		expected.put(srcPath1, Lists.newArrayList(tuple0, tuple1));

		tuple0 = Tuple.newTuple(null, Lists.newArrayList(rp2));
		tuple1 = Tuple.newTuple(init, Lists.newArrayList(event2, event3));
		expected.put(srcPath2, Lists.newArrayList(tuple0, tuple1));

		tuple0 = Tuple.newTuple(null, Lists.newArrayList(rp3));
		tuple1 = Tuple
				.newTuple(md2, Lists.newArrayList(event2, event1, event3));
		expected.put(srcPath3, Lists.newArrayList(tuple0, tuple1));

		Map<String, List<Tuple<Event, List<Event>>>> actuals = sut
				.absoluteFileMethodStructure(stream);

		assertEquals(expected, actuals);
	}

	@Test
	public void eventstream() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.ABSOLUTE_PATH);
		Event relPath = createEvent("type1", "link1.java",
				EventKind.RELATIVE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> eventStream = Maps
				.newLinkedHashMap();
		eventStream.put(srcPath, Lists.newLinkedList());
		eventStream.get(srcPath).add(
				Tuple.newTuple(null, Lists.newArrayList(relPath)));
		eventStream.get(srcPath).add(
				Tuple.newTuple(md, Lists.newArrayList(event2, event3)));
		eventStream.get(srcPath).add(
				Tuple.newTuple(event1, Lists.newArrayList(event3, event2)));

		EventStream expected = new EventStream();
		expected.addEvent(event2);
		expected.addEvent(event3);
		expected.increaseTimeout();
		expected.addEvent(event3);
		expected.addEvent(event2);
		expected.increaseTimeout();

		Map<String, List<Tuple<Event, List<Event>>>> rps = sut
				.relativeFileMethodStructure(eventStream);
		EventStream actuals = sut.generateFiles(rootFolder.getRoot(),
				FREQUENCY, rps);

		assertEquals(expected.getMapping(), actuals.getMapping());
		assertEquals(expected.getStreamText(), actuals.getStreamText());
	}

	@Test
	public void filesCreated() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.ABSOLUTE_PATH);
		Event relPath = createEvent("type1", "link1.java",
				EventKind.RELATIVE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> eventStream = Maps
				.newLinkedHashMap();
		eventStream.put(srcPath, Lists.newLinkedList());
		eventStream.get(srcPath).add(
				Tuple.newTuple(null, Lists.newArrayList(relPath)));
		eventStream.get(srcPath).add(
				Tuple.newTuple(md, Lists.newArrayList(event2, event3)));
		eventStream.get(srcPath).add(
				Tuple.newTuple(event1, Lists.newArrayList(event3, event2)));

		Map<String, List<Tuple<Event, List<Event>>>> rs = sut
				.relativeFileMethodStructure(eventStream);
		sut.generateFiles(rootFolder.getRoot(), FREQUENCY, rs);

		assertTrue(getStreamObjectPath().exists());
		assertTrue(getStreamPath().exists());
		assertTrue(getMapPath().exists());
	}

	@Test
	public void filesContent() throws IOException {
		Event source = createEvent("type1", "link1", EventKind.ABSOLUTE_PATH);
		Event relPath = createEvent("type1", "link1.java",
				EventKind.RELATIVE_PATH);
		Event md = createEvent("type1", "m1", EventKind.METHOD_DECLARATION);
		Event event1 = createEvent("type2", "cctor", EventKind.INITIALIZER);
		Event event2 = createEvent("type2", "m2", EventKind.INVOCATION);
		Event event3 = createEvent("type1", "ctor", EventKind.CONSTRUCTOR);

		String srcPath = source.getMethod().getFullName();
		Map<String, List<Tuple<Event, List<Event>>>> eventStream = Maps
				.newLinkedHashMap();
		eventStream.put(srcPath, Lists.newLinkedList());
		eventStream.get(srcPath).add(
				Tuple.newTuple(null, Lists.newArrayList(relPath)));
		eventStream.get(srcPath).add(
				Tuple.newTuple(md, Lists.newArrayList(event2, event3)));
		eventStream.get(srcPath).add(
				Tuple.newTuple(event1, Lists.newArrayList(event3, event2)));

		Map<String, List<Tuple<Event, List<Event>>>> rs = sut
				.relativeFileMethodStructure(eventStream);
		sut.generateFiles(rootFolder.getRoot(), FREQUENCY, rs);

		Map<String, List<Tuple<Event, List<Event>>>> actualObject = sut
				.readStreamObject(rootFolder.getRoot(), FREQUENCY);

		String expectedStream = "1,0.000\n2,0.001\n2,5.002\n1,5.003\n";
		String actualStream = FileUtils.readFileToString(getStreamPath());

		List<Event> expectedMap = Lists.newLinkedList();
		expectedMap.add(Events.newDummyEvent());
		expectedMap.add(event2);
		expectedMap.add(event3);

		Type type2 = new TypeToken<List<Event>>() {
		}.getType();
		List<Event> actualMap = JsonUtils.fromJson(getMapPath(), type2);

		assertEquals(rs, actualObject);
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
		String streamName = rootFolder.getRoot().getAbsolutePath() + "/freq"
				+ FREQUENCY + "/stream.txt";
		return new File(streamName);
	}

	private File getMapPath() {
		String mapName = rootFolder.getRoot().getAbsolutePath() + "/freq"
				+ FREQUENCY + "/mapping.txt";
		return new File(mapName);
	}

	private File getStreamObjectPath() {
		String mapName = rootFolder.getRoot().getAbsolutePath() + "/freq"
				+ FREQUENCY + "/object.json";
		return new File(mapName);
	}
}
