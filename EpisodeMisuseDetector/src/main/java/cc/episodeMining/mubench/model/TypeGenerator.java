package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;

import cc.kave.commons.model.naming.impl.v0.types.TypeName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.naming.types.organization.IAssemblyName;
import cc.kave.commons.model.naming.types.organization.INamespaceName;

public class TypeGenerator {

	public ITypeName generate(IMethodBinding bindings,
			NamespaceGenerator namespace, AssemblyGenerator assembly) {

		INamespaceName namespacePart = namespace.generate(bindings);
		String type = getType(bindings);
		IAssemblyName assemblyPart = assembly.generate(bindings);

		String identifier = namespacePart.getIdentifier() + "." + type + ", "
				+ assemblyPart.getIdentifier();
		
		ITypeName tyoeName = new TypeName(identifier);

		return tyoeName;
	}

	private String getType(IMethodBinding bindings) {
		// TODO Auto-generated method stub
		return null;
	}
}
