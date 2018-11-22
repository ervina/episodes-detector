package cc.episodeMining.mubench.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

public class EventGeneratorTest {

	private String typeName = "type";
	private String methodName = "method";
	private List<String> paramTypes;
	private String returnType;

	private Event event;

	@Before
	public void setup() {
		paramTypes = Lists.newLinkedList();
		returnType = "";

		event = new Event();
	}

	@Test
	public void absolutePath() {
		String file_path = "file.java";

		event.setKind(EventKind.ABSOLUTE_PATH);
		event.setMethod(Names.newMethod("[?] [file]." + file_path));

		Event actuals = EventGenerator.absolutePath(file_path);

		assertEquals(event, actuals);
	}

	@Test
	public void relativePath() {
		String file_path = "file.java";

		event.setKind(EventKind.RELATIVE_PATH);
		event.setMethod(Names.newMethod("[?] [file]." + file_path));

		Event actuals = EventGenerator.relativePath(file_path);

		assertEquals(event, actuals);
	}

	@Test
	public void firstCtx() {
		returnType = "type";
		String method = "[type] [?] [" + typeName + "]." + methodName + "()";

		event.setKind(EventKind.FIRST_DECLARATION);
		event.setMethod(Names.newMethod(method));

		Event actuals = EventGenerator.firstContext(typeName, methodName,
				paramTypes, returnType);

		assertEquals(event, actuals);
	}

	@Test
	public void superCtx() {
		paramTypes.add("String");
		returnType = "void";
		String method = "[void] [?] [" + typeName + "]." + methodName
				+ "([String])";

		event.setKind(EventKind.SUPER_DECLARATION);
		event.setMethod(Names.newMethod(method));

		Event actuals = EventGenerator.superContext(typeName, methodName,
				paramTypes, returnType);

		assertEquals(event, actuals);
	}

	@Test
	public void elementCtx() {
		paramTypes.add("String");
		paramTypes.add("int");

		returnType = "String";

		String method = "[String] [?] [" + typeName + "]." + methodName
				+ "([String], [int])";

		event.setKind(EventKind.METHOD_DECLARATION);
		event.setMethod(Names.newMethod(method));

		Event actuals = EventGenerator.elementContext(typeName, methodName,
				paramTypes, returnType);

		assertEquals(event, actuals);
	}

	@Test
	public void invocation() {
		paramTypes.add("Char");
		returnType = "String";
		String method = "[String] [?] [" + typeName + "]." + methodName
				+ "([Char])";

		event.setKind(EventKind.INVOCATION);
		event.setMethod(Names.newMethod(method));

		Event actuals = EventGenerator.invocation(typeName, methodName,
				paramTypes, returnType);

		assertEquals(event, actuals);
	}

	@Test
	public void constructor() {
		returnType = "type1";

		event.setKind(EventKind.CONSTRUCTOR);
		event.setMethod(Names.newMethod("[type1] [?] [" + typeName
				+ "]..ctor()"));

		Event actuals = EventGenerator.constructor(typeName, paramTypes,
				returnType);

		assertEquals(event, actuals);
	}

	@Test
	public void initializer() {
		event.setKind(EventKind.INITIALIZER);
		event.setMethod(Names.newMethod("[?] [" + typeName + "]..cctor()"));

		Event actuals = EventGenerator.initializer(typeName);

		assertEquals(event, actuals);
	}
}
