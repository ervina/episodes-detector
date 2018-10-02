package cc.episodeMining.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
	
	@Test
	public void streamGeneration() {
		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
		
		assertTrue(actuals.isEmpty());
	}
	
	@Test
	public void oneMethod() throws IOException {
		String method = "void m(Object o) {\n o.hashCode();\n }";
		String code = createJavaClass(method);
		
		FileUtils.writeStringToFile(getFileName("test.java"), code);
		
		List<Event> expected = Lists.newLinkedList();
		expected.add(createEvent("[?] [test]." + rootFolder.getRoot() + "/test.java", EventKind.ABSOLUTE_PATH));
		expected.add(createEvent("[?] [test]../test.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("[?] [C].m", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		
		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
		
		assertTrue(expected.size() == actuals.size());
		assertEquals(expected, actuals);
	}
	
	@Test
	public void twoMethod() throws IOException {
		String method = "void m(Object o) {\n o.hashCode();\n }\n"
							+ "	void n(Object o) {\n o.hashCode();\n }";
		String code = createJavaClass(method);
		
		FileUtils.writeStringToFile(getFileName("test.java"), code);
		
		List<Event> expected = Lists.newLinkedList();
		expected.add(createEvent("[?] [test]." + rootFolder.getRoot() + "/test.java", EventKind.ABSOLUTE_PATH));
		expected.add(createEvent("[?] [test]../test.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("[?] [C].m", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		expected.add(createEvent("[?] [C].n", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		
		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
		
		assertTrue(expected.size() == actuals.size());
		assertEquals(expected, actuals);
	}
	
	@Test
	public void staticInitializer() throws IOException {
		String method = "static {\n " +
						"	Object o = new Object();\n" +
						"	o.hashCode();\n }\n" +
						"void n(Object o) {\n o.hashCode();\n }";
		String code = createJavaClass(method);
		
		FileUtils.writeStringToFile(getFileName("test.java"), code);
		
		List<Event> expected = Lists.newLinkedList();
		expected.add(createEvent("[?] [test]." + rootFolder.getRoot() + "/test.java", EventKind.ABSOLUTE_PATH));
		expected.add(createEvent("[?] [test]../test.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("[?] [C]..cctor", EventKind.INITIALIZER));
		expected.add(createEvent("[?] [Object]..ctor", EventKind.CONSTRUCTOR));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		expected.add(createEvent("[?] [C].n", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		
		List<Event> actuals = sut.generateMethodTraces(rootFolder.getRoot(), new String[] {});
		
		assertTrue(expected.size() == actuals.size());
		assertEquals(expected, actuals);
	}
	
	@Test
	public void superCtx() throws IOException {
		String method1 = "void m(Object o) {\n o.hashCode();\n }";
		String method2 = "class NullTextNull extends StrBuilder {\n" +
						"	String pattern(Object obj) {\n" +
						"		String str = (obj == null ? this.getNullText() : obj.toString());\n" +
						"		if (str == null) {\n" +
						"			str = \"\";\n" +
						"		}\n" +
						"		return str;\n" +
						"	}\n" +
						"}\n\n" +
						"abstract class StrBuilder {\n" +
						"	String pattern(Object obj) {\n" +
						"		return null;\n" +
						"	}\n" +
						"	String getNullText() {\n" +
						"		return \"\";\n" +
						"	}\n" +
						"}\n";
		
		String code = createJavaClass(method1);
		
		FileUtils.writeStringToFile(getFileName("test.java"), code);
		FileUtils.writeStringToFile(getFileName("test1.java"), method2);
		
		List<Event> expected = Lists.newLinkedList();
		expected.add(createEvent("[?] [test]." + rootFolder.getRoot().getAbsolutePath() + "/test.java", EventKind.ABSOLUTE_PATH));
		expected.add(createEvent("[?] [test]../test.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("[?] [C].m", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [Object].hashCode", EventKind.INVOCATION));
		
		expected.add(createEvent("[?] [test1]." + rootFolder.getRoot().getAbsolutePath() + "/test1.java", EventKind.ABSOLUTE_PATH));
		expected.add(createEvent("[?] [test1]../test1.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("[?] [NullTextNull].pattern", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [StrBuilder].pattern", EventKind.SUPER_DECLARATION));
		expected.add(createEvent("[?] [StrBuilder].getNullText", EventKind.INVOCATION));
		expected.add(createEvent("[?] [Object].toString", EventKind.INVOCATION));
		
		File path = new File(rootFolder.getRoot().getAbsolutePath());
		List<Event> actuals = sut.generateMethodTraces(path, new String[] {});
		
		assertEquals(expected, actuals);
	}
	
	@Test
	public void firstCtx() throws IOException {
		String method = "import javax.swing.JPanel;\n" +
						"class NullTextNull implements StrBuilder {\n" +
						"	public String pattern(Object obj) {\n" +
						"		String str = (obj == null ? this.getNullText() : obj.toString());\n" +
						"		if (str == null) {\n" +
						"			JPanel controlPanel = new JPanel();\n" +
						"			str = \"\";\n" +
						"			 controlPanel.add(str);\n" +
						"		}\n" +
						"		return str;\n" +
						"	}\n" +
						"	public String getNullText() {\n" +
						"		return \"\";\n" +
						"	}\n" +
						"}\n\n" +
						"interface StrBuilder {\n" +
						"	String pattern(Object obj);\n" +
						"	String getNullText();\n" +
						"}\n";
		
		FileUtils.writeStringToFile(getFileName("test.java"), method);
		
		List<Event> expected = Lists.newLinkedList();
		expected.add(createEvent("[?] [test]." + rootFolder.getRoot().getAbsolutePath() + "/test.java", EventKind.ABSOLUTE_PATH));
		expected.add(createEvent("[?] [test]../test.java", EventKind.RELATIVE_PATH));
		expected.add(createEvent("[?] [NullTextNull].pattern", EventKind.METHOD_DECLARATION));
		expected.add(createEvent("[?] [StrBuilder].pattern", EventKind.FIRST_DECLARATION));
		expected.add(createEvent("[?] [StrBuilder].getNullText", EventKind.INVOCATION));
		expected.add(createEvent("[?] [Object].toString", EventKind.INVOCATION));
		expected.add(createEvent("[?] [JPanel]..ctor", EventKind.CONSTRUCTOR));
		expected.add(createEvent("[?] [Container].add", EventKind.INVOCATION));
		
		File path = new File(rootFolder.getRoot().getAbsolutePath());
		List<Event> actuals = sut.generateMethodTraces(path, new String[] {});
		
		assertEquals(expected, actuals);
	}
	
	private String createJavaClass(String code) {
		String result = "class C {\n" + code + "\n}";
		return result;
	}

	private File getFileName(String fileName) {
		return new File(rootFolder.getRoot().getAbsolutePath() + "/" + fileName);
	}
	
	private Event createEvent(String method, EventKind kind) {
		Event event = new Event();
		event.setKind(kind);
		event.setMethod(Names.newMethod(method + "()"));
		
		return event;
	}
}
