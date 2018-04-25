package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;

import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Events;

public class EventGenerator {

	public Event generate(IMethodBinding binding) {
		
//		ITypeBinding classDeclaration = binding.getDeclaringClass();
//		ITypeBinding typeDeclaration = classDeclaration.getTypeDeclaration();
//		String namespace = typeDeclaration.getKey();
//		String signature = binding.getKey();
		
		String typeName = binding.getDeclaringClass().getName();
		String methodName = binding.getName();
		
		String fullName = typeName + "." + methodName;
		
		IMethodName method = Names.newMethod(fullName);
		
		Event event = Events.newElementContext(method);
		
		return event;
	}
}
