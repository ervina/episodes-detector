package cc.episodeMining.mubench.model;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

public class EventGenerator {

	public static Event absolutePath(String source) {

		int idxStart = source.lastIndexOf("/");
		int idxEnd = source.lastIndexOf(".");
		String type = source.substring(idxStart + 1, idxEnd);

		Event event = new Event();
		event.setKind(EventKind.ABSOLUTE_PATH);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + source + "()"));

		return event;
	}

	public static Event relativePath(String source) {

		int idxStart = source.lastIndexOf("/");
		int idxEnd = source.lastIndexOf(".");
		String type = source.substring(idxStart + 1, idxEnd);

		Event event = new Event();
		event.setKind(EventKind.RELATIVE_PATH);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + source + "()"));

		return event;
	}

	public static Event elementContext(String typeName, String methodName) {

		Event event = new Event();
		event.setKind(EventKind.METHOD_DECLARATION);
		event.setMethod(getMethodName(typeName, methodName));

		return event;
	}

	public static Event superContext(String type, String method) {

		Event event = new Event();
		event.setKind(EventKind.SUPER_DECLARATION);
		event.setMethod(getMethodName(type, method));

		return event;
	}

	public static Event firstContext(String type, String method) {

		Event event = new Event();
		event.setKind(EventKind.FIRST_DECLARATION);
		event.setMethod(getMethodName(type, method));

		return event;
	}

	public static Event invocation(String type, String method) {

		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setMethod(getMethodName(type, method));

		return event;
	}

	public static Event initializer(String type) {
		String method = ".cctor";

		Event event = new Event();
		event.setKind(EventKind.INITIALIZER);
		event.setMethod(getMethodName(type, method));

		return event;
	}

	public static Event constructor(String type) {
		String method = ".ctor";

		Event event = new Event();
		event.setKind(EventKind.CONSTRUCTOR);
		event.setMethod(getMethodName(type, method));

		return event;
	}

	private static IMethodName getMethodName(String type, String method) {
		IMethodName methodName = Names.newMethod("[?] [" + type + "]." + method
				+ "()");
		return methodName;
	}
}
