package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;

import cc.kave.commons.model.naming.impl.v0.types.organization.NamespaceName;
import cc.kave.commons.model.naming.types.organization.INamespaceName;

public class NamespaceGenerator {

	public INamespaceName generate(IMethodBinding bindings) {
		
		String namespace = getNamespace(bindings);
		
		if (namespace.equalsIgnoreCase("???")) {
			return new NamespaceName();
		}
		return new NamespaceName(namespace);
	}

	private String getNamespace(IMethodBinding bindings) {
		// TODO Auto-generated method stub
		return null;
	}
}
