package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

public class EventGenerator {

	public static Event sourcePath(String source) {
		Event event = new Event();
		event.setKind(EventKind.SOURCE_FILE_PATH);
		event.setMethod(Names.newMethod("[?] [?]." + source + "()"));
		
		return event;
	}
	
	public static Event elementContext(IMethodBinding binding) {

		// ITypeBinding classDeclaration = binding.getDeclaringClass();
		// ITypeBinding typeDeclaration = classDeclaration.getTypeDeclaration();
		// String namespace = typeDeclaration.getKey();
		// String signature = binding.getKey();

		String type = getType(binding);
		String method = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.METHOD_DECLARATION);
		event.setMethod(getMethodName(type, method));

		return event;
	}

	public static Event superContext(ITypeBinding type, IMethodBinding binding) {

		String method = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.SUPER_DECLARATION);
		event.setMethod(getMethodName(type.getName(), method));

		return event;
	}

	public static Event firstContext(ITypeBinding type, IMethodBinding binding) {

		String method = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.FIRST_DECLARATION);
		event.setMethod(getMethodName(type.getName(), method));

		return event;
	}

	public static Event invocation(ITypeBinding type, IMethodBinding binding) {
		String method = getMethod(binding);
		
		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setMethod(getMethodName(type.getName(), method));
		
		return event;
	}
	
	public static Event constructor(ITypeBinding type) {
		String method = ".ctor";
		
		Event event = new Event();
		event.setKind(EventKind.CONSTRUCTOR);
		event.setMethod(getMethodName(type.getName(), method));
		
		return event;
	}

	private static String getMethod(IMethodBinding binding) {
		String methodName = binding.getName();
		return methodName;
	}

	private static String getType(IMethodBinding binding) {
		String typeName = binding.getDeclaringClass().getName();
		return typeName;
	}
	
	private static IMethodName getMethodName(String type, String method) {
		IMethodName methodName = Names.newMethod("[?] " + "[" + type + "]" + "." + method + "()");
		return methodName;
	}
}
