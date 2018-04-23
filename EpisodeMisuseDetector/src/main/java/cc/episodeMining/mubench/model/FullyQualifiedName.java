package cc.episodeMining.mubench.model;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import cc.kave.commons.utils.StringUtils;
import cc.recommenders.exceptions.ValidationException;

public class FullyQualifiedName {
	
	private static final String UnknownMethodIdentifier = "[?] [?].???()";
	private static final String UNKNOWN_NAME_IDENTIFIER = "???";
	
	private String identifier;
	
	public FullyQualifiedName(String id) {
		validate(id != null, "identifier must not be null");
		this.identifier = id;
	}
	
	protected static void validate(boolean condition, String msg) {
		if (!condition) {
			throw new ValidationException(msg);
		}
	}
	
	public boolean isUnknown() {
		return UnknownMethodIdentifier.equals(identifier);
	}
	
	private String _name;
	
	public String getName() {
		if (_name == null) {
			if (isUnknown()) {
				_name = UNKNOWN_NAME_IDENTIFIER;
			} else {
				int openR = identifier.indexOf('[');
				int closeR = StringUtils.FindCorrespondingCloseBracket(identifier, openR);
				int openD = StringUtils.FindNext(identifier, closeR, '[');
				int closeD = StringUtils.FindCorrespondingCloseBracket(identifier, openD);
				int startName = StringUtils.FindNext(identifier, closeD, '.') + 1;
				int endName = StringUtils.FindNext(identifier, startName, '`', '(');
				_name = identifier.substring(startName, endName);
			}
		}
		return _name;
	}

	public String declarations(MethodDeclaration decl) {
		
		IMethodBinding bindings = decl.resolveBinding();
		
		String name = "";
		
		ITypeBinding declaringClass = bindings.getDeclaringClass();
		String methodName = bindings.getName();
		ITypeBinding[] parameterTypes = bindings.getParameterTypes();
		ITypeBinding returnType = bindings.getReturnType();
		
		// generic method
		boolean genericMethod = bindings.isGenericMethod();
		boolean parameterizedMethod = bindings.isParameterizedMethod();
		ITypeBinding[] typeArguments = bindings.getTypeArguments();
		ITypeBinding[] typeParameters = bindings.getTypeParameters();
		
		return name;
	}
	
	public String invocations(MethodInvocation inv) {
		String name = "";
		
		IMethodBinding bindings = inv.resolveMethodBinding();
		
		if (bindings.isConstructor()) {
			
		}
		
		return name;
	}
}
