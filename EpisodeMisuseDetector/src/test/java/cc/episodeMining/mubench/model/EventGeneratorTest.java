package cc.episodeMining.mubench.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.model.naming.Names;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

public class EventGeneratorTest {
	
	private String typeName = "type";
	private String methodName = "method";
	
	private String method;
	
	private Event event;
	
	@Before 
	public void setup() {
		method = "[?] [" + typeName + "]." + methodName + "()";
		
		event = new Event();
	}

	@Test
	public void filePath() {
		String file_path = "file.java";

		event.setKind(EventKind.SOURCE_FILE_PATH);
		event.setMethod(Names.newMethod("[?] [?]." + file_path + "()"));

		Event actuals = EventGenerator.sourcePath(file_path);

		assertEquals(event, actuals);
	}
	
	@Test
	public void firstCtx() {
		event.setKind(EventKind.FIRST_DECLARATION);
		event.setMethod(Names.newMethod(method));
		
		Event actuals = EventGenerator.firstContext(typeName, methodName);
		
		assertEquals(event, actuals);
	}
	
	@Test
	public void superCtx() {
		event.setKind(EventKind.SUPER_DECLARATION);
		event.setMethod(Names.newMethod(method));
		
		Event actuals = EventGenerator.superContext(typeName, methodName);
		
		assertEquals(event, actuals);
	}

	@Test
	public void elementCtx() {

		event.setKind(EventKind.METHOD_DECLARATION);
		event.setMethod(Names.newMethod(method));
		
		Event actuals = EventGenerator.elementContext(typeName, methodName);
		
		assertEquals(event, actuals);
	}
	
	@Test
	public void invocation() {
		event.setKind(EventKind.INVOCATION);
		event.setMethod(Names.newMethod(method));
		
		Event actuals = EventGenerator.invocation(typeName, methodName);
		
		assertEquals(event, actuals);
	}
	
	@Test
	public void constructor() {
		event.setKind(EventKind.CONSTRUCTOR);
		event.setMethod(Names.newMethod("[?] [" + typeName + "]..ctor()"));
		
		Event actuals = EventGenerator.constructor(typeName);
		
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
