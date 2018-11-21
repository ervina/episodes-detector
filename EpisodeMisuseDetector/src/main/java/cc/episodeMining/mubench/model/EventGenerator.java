package cc.episodeMining.mubench.model;

import java.util.List;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

import com.google.common.collect.Lists;

public class EventGenerator {

	public static Event absolutePath(String source) {

		int idxStart = source.lastIndexOf("/");
		int idxEnd = source.lastIndexOf(".");
		String type = source.substring(idxStart + 1, idxEnd);

		Event event = new Event();
		event.setKind(EventKind.ABSOLUTE_PATH);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + source));

		return event;
	}

	public static Event relativePath(String source) {

		int idxStart = source.lastIndexOf("/");
		int idxEnd = source.lastIndexOf(".");
		String type = source.substring(idxStart + 1, idxEnd);

		Event event = new Event();
		event.setKind(EventKind.RELATIVE_PATH);
		event.setMethod(Names.newMethod("[?] [" + type + "]." + source));

		return event;
	}

	public static Event elementContext(String typeName, String methodName,
			List<String> paramTypes) {

		Event event = new Event();
		event.setKind(EventKind.METHOD_DECLARATION);
		event.setMethod(getMethodName(typeName, methodName, paramTypes));

		return event;
	}

	public static Event superContext(String type, String method,
			List<String> paramTypes) {

		Event event = new Event();
		event.setKind(EventKind.SUPER_DECLARATION);
		event.setMethod(getMethodName(type, method, paramTypes));

		return event;
	}

	public static Event firstContext(String type, String method,
			List<String> paramTypes) {

		Event event = new Event();
		event.setKind(EventKind.FIRST_DECLARATION);
		event.setMethod(getMethodName(type, method, paramTypes));

		return event;
	}

	public static Event invocation(String type, String method,
			List<String> paramTypes) {

		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setMethod(getMethodName(type, method, paramTypes));

		return event;
	}

	public static Event initializer(String type) {
		String method = ".cctor";

		Event event = new Event();
		event.setKind(EventKind.INITIALIZER);
		event.setMethod(getMethodName(type, method, Lists.newArrayList()));

		return event;
	}

	public static Event constructor(String type, List<String> paramTypes) {
		String method = ".ctor";

		Event event = new Event();
		event.setKind(EventKind.CONSTRUCTOR);
		event.setMethod(getMethodName(type, method, paramTypes));

		return event;
	}

	private static IMethodName getMethodName(String type, String method,
			List<String> types) {
		StringBuilder sb = new StringBuilder();
		sb.append("[?] [" + type + "].");
		sb.append(method + "(");
		boolean isFirst = true;

		if (types.size() != 0) {
			for (String t : types) {
				if (!isFirst) {
					sb.append(", ");

				}
				isFirst = false;
				sb.append(t);
			}
		}
		sb.append(")");
		IMethodName methodName = Names.newMethod(sb.toString());
		return methodName;
	}
}
