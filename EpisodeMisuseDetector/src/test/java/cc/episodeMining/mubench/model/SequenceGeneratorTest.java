package cc.episodeMining.mubench.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cc.episodeMining.data.SequenceGenerator;
import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class SequenceGeneratorTest {

	@Rule
	public TemporaryFolder rootFolder = new TemporaryFolder();
	
	private SequenceGenerator sut;
	
	@Before
	public void setup() {
		sut = new SequenceGenerator();
	}
	
//	@Test
//	public void streamGeneration() {
//		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
//		
//		assertEquals(expStream(), actuals);
//	}
	
	@Test
	public void oneMethod() throws IOException {
		String method = "void m(Object o) {\n o.hashCode();\n }";
		String code = createJavaClass(method);
		
		FileUtils.writeStringToFile(getFileName("test.java"), code);
		
		List<Event> expected = Lists.newLinkedList();
		expected.add(createEvent("[?] [?]." + rootFolder.getRoot() + "/test.java", EventKind.SOURCE_FILE_PATH));
		expected.add(createEvent("[?] [C].m", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		
		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
		
		assertTrue(expected.size() == actuals.size());
		assertEquals(expected, actuals);
	}
	
//	@Test
//	public void twoMethods() throws IOException {
//		String method1 = "void m1(Object o) {\n o.hashCode();\n }";
//		String method2 = "class NullTextNull extends StrBuilder {\n" + 
//				"  String pattern(Object obj) {\n" + 
//				"    String str = (obj == null ? this.getNullText() : obj.toString());\n" + 
//				"    if (str == null) {\n" + 
//				"      str = \"\";\n" + 
//				"    }\n" + 
//				"    return str;\n" + 
//				"  }\n" + 
//				"}";
//		String code = createJavaClass(method1);
//		
//		FileUtils.writeStringToFile(getFileName("test1.java"), code);
//		FileUtils.writeStringToFile(getFileName("test2.java"), method2);
//		
//		List<Event> expected = Lists.newLinkedList();
//		expected.add(createEvent("[?] [?]." + rootFolder.getRoot() + "/test1.java", EventKind.SOURCE_FILE_PATH));
//		expected.add(createEvent("[?] [C].m", EventKind.METHOD_DECLARATION));
//		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
//		
//		expected.add(createEvent("[?] [?]." + rootFolder.getRoot() + "/test2.java", EventKind.SOURCE_FILE_PATH));
//		expected.add(createEvent("StrBuilder.pattern", EventKind.SUPER_DECLARATION));
//		expected.add(createEvent("NullTextNull.pattern", EventKind.METHOD_DECLARATION));
//		expected.add(createEvent("StrBuilder.getNullText", EventKind.INVOCATION));
//		expected.add(createEvent("Object.toString", EventKind.INVOCATION));
//		
//		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
//		
//		assertEquals(expected, actuals);
//	}
	
	private String createJavaClass(String code) {
		String result = "class C {\n" + code + "\n}";
		return result;
	}

	private File getFileName(String fileName) {
		return new File(rootFolder.getRoot().getAbsolutePath() + "/" + fileName);
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
		event.setMethod(Names.newMethod(method + "()"));
		
		return event;
	}
}
