package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;

import cc.kave.commons.model.naming.impl.v0.types.organization.AssemblyName;
import cc.kave.commons.model.naming.types.organization.IAssemblyName;

public class AssemblyGenerator {

	public IAssemblyName generate(IMethodBinding bindings) {
		
		String assemblyName = generateName(bindings);
		String assemblyVersion = generateVersion(bindings);
		
		String fullName = assemblyName + ", " + assemblyVersion;
		
		IAssemblyName assembly = new AssemblyName(fullName);

		return assembly;
	}
	
	public boolean isLocal(IMethodBinding binding) {
//		if (!isUnknown() && !identifier.contains(",")) {
//			isLocalProject = true;
//		}
		return false;
	}

	private String generateName(IMethodBinding bindings) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String generateVersion(IMethodBinding bindings) {
		// TODO Auto-generated method stub
		return null;
	}
}
