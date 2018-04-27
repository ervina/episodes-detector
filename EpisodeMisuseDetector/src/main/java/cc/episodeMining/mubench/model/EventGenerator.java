package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.EventKind;

public class EventGenerator {

	public static Event elementContext(IMethodBinding binding) {

		// ITypeBinding classDeclaration = binding.getDeclaringClass();
		// ITypeBinding typeDeclaration = classDeclaration.getTypeDeclaration();
		// String namespace = typeDeclaration.getKey();
		// String signature = binding.getKey();

		ITypeName typeName = getType(binding);
		IMethodName methodName = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.METHOD_DECLARATION);
		event.setType(typeName);
		event.setMethod(methodName);

		return event;
	}

	public static Event superContext(ITypeBinding type, IMethodBinding binding) {

		IMethodName methodName = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.SUPER_DECLARATION);
		event.setType(Names.newType(type.getName()));
		event.setMethod(methodName);

		return event;
	}
	
	public static Event firstContext(ITypeBinding type, IMethodBinding binding) {

		IMethodName methodName = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.FIRST_DECLARATION);
		event.setType(Names.newType(type.getName()));
		event.setMethod(methodName);

		return event;
	}
	
	public static Event invocation(IMethodBinding binding) {

		ITypeName typeName = getType(binding);
		IMethodName methodName = getMethod(binding);

		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setType(typeName);
		event.setMethod(methodName);

		return event;
	}
	
	public static Event constructor(IMethodBinding binding) {

		ITypeName typeName = getType(binding);
		IMethodName methodName = Names.newMethod(".ctor()");

		Event event = new Event();
		event.setKind(EventKind.INVOCATION);
		event.setType(typeName);
		event.setMethod(methodName);

		return event;
	}

	private static IMethodName getMethod(IMethodBinding binding) {
		String methodName = binding.getName();
		return Names.newMethod(methodName);
	}

	private static ITypeName getType(IMethodBinding binding) {
		String typeName = binding.getDeclaringClass().getName();
		return Names.newType(typeName);
	}
}
