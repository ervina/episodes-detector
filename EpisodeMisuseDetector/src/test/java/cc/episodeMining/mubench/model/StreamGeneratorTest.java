package cc.episodeMining.mubench.model;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cc.episodeMining.data.StreamGenerator;
import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class StreamGeneratorTest {

	private StreamGenerator sut;
	
	@Before
	public void setup() {
		sut = new StreamGenerator();
	}
	
	@Test
	public void streamGeneration() {
		List<Event> actuals = sut.generateMethodTraces(getPaths(), new String[] {});
		
		assertEquals(expStream(), actuals);
	}
	
	private File getPaths() {
		String pathName = "/Users/ervinacergani/Documents/MisuseDetector/tests";
		return new File(pathName);
	}
	
	private List<Event> expStream() {
		List<Event> eventStream = Lists.newLinkedList();
		
		eventStream.add(createEvent("HashMap..ctor()", EventKind.CONSTRUCTOR));
		eventStream.add(createEvent("Collections.synchronizedMap()", EventKind.INVOCATION));
		eventStream.add(createEvent("HashMap..ctor()", EventKind.CONSTRUCTOR));
		eventStream.add(createEvent("Collections.synchronizedMap()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale.getAvailableLocales()", EventKind.INVOCATION));
		eventStream.add(createEvent("Arrays.asList()", EventKind.INVOCATION));
		eventStream.add(createEvent("Collections.unmodifiableList()", EventKind.INVOCATION));
		
		eventStream.add(createEvent("LocaleUtils.localeLookupList()", EventKind.METHOD_DECLARATION));
		eventStream.add(createEvent("LocaleUtils.localeLookupList()", EventKind.INVOCATION));
		
		eventStream.add(createEvent("LocaleUtils.localeLookupList()", EventKind.METHOD_DECLARATION));
		eventStream.add(createEvent("ArrayList..ctor()", EventKind.CONSTRUCTOR));
		eventStream.add(createEvent("ArrayList.add()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale.getVariant()", EventKind.INVOCATION));
		eventStream.add(createEvent("String.length()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale.getLanguage()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale.getCountry()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale..ctor()", EventKind.CONSTRUCTOR));
		eventStream.add(createEvent("ArrayList.add()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale.getCountry()", EventKind.INVOCATION));
		eventStream.add(createEvent("String.length()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale.getLanguage()", EventKind.INVOCATION));
		eventStream.add(createEvent("Locale..ctor()", EventKind.CONSTRUCTOR));
		eventStream.add(createEvent("ArrayList.add()", EventKind.INVOCATION));
		eventStream.add(createEvent("ArrayList.contains()", EventKind.INVOCATION));
		eventStream.add(createEvent("ArrayList.add()", EventKind.INVOCATION));
		eventStream.add(createEvent("Collections.unmodifiableList()", EventKind.INVOCATION));
		
		return eventStream;
	}
	
	private Event createEvent(String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod(method));
		
		return event;
	}
}