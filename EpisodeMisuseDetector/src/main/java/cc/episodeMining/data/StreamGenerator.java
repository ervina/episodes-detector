package cc.episodeMining.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import cc.episodeMining.mubench.model.EventGenerator;
import cc.kave.episodes.model.events.Event;

import com.google.common.collect.Lists;

import de.tu_darmstadt.stg.mudetect.utils.JavaASTUtil;

public class StreamGenerator {

	private List<Event> stream;

	public StreamGenerator() {
		this.stream = Lists.newLinkedList();
	}

	public List<Event> generateMethodTraces(File sourcePath, String[] classpaths) {
		ArrayList<File> files = getPaths(sourcePath);
		String[] paths = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			paths[i] = files.get(i).getAbsolutePath();
		}
		FileASTRequestor r = new FileASTRequestor() {

			private Event firstCtx;
			private Event superCtx;
			private Event elementCtx;

			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {

//				for (int i = 0; i < cu.types().size(); i++) {
//					buildHierarchy((AbstractTypeDeclaration) cu.types().get(i),
//							cu.getPackage() == null ? "" : cu.getPackage()
//									.getName().getFullyQualifiedName()
//									+ ".");
//				}

				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(MethodDeclaration node) {

						ASTNode parent = node.getParent();
						IMethodBinding binding = node.resolveBinding();

						firstCtx = null;
						superCtx = null;
						elementCtx = EventGenerator.elementContext(binding);

						ITypeBinding typeBinding = binding.getDeclaringClass()
								.getTypeDeclaration();
						getSuper(typeBinding, binding.getMethodDeclaration(),
								JavaASTUtil.buildSignature(binding));
						getFirst(typeBinding, binding.getMethodDeclaration(),
								JavaASTUtil.buildSignature(binding));

						return super.visit(node);
					}

					@Override
					public boolean visit(MethodInvocation node) {
						return super.visit(node);
					}

					@Override
					public boolean visit(ConstructorInvocation node) {
						return super.visit(node);
					}

					@Override
					public void endVisit(ConstructorInvocation node) {

						IMethodBinding bindings = node
								.resolveConstructorBinding()
								.getMethodDeclaration();
						addEnclosingContextIfAvailable();
						stream.add(EventGenerator.constructor(bindings));

						super.endVisit(node);
					}

					@Override
					public void endVisit(MethodInvocation node) {

						ASTNode parent = node.getParent();
						
						if (node.resolveMethodBinding() != null) {
							IMethodBinding mb = node.resolveMethodBinding().getMethodDeclaration();
							String sig = JavaASTUtil.buildSignature(mb);
							ITypeBinding tb = getBase(mb.getDeclaringClass().getTypeDeclaration(), mb, sig);
							String type = tb.getName();
							
							IMethodBinding binding = node.resolveMethodBinding();
							IMethodBinding methodBinding = binding.getMethodDeclaration();
							addEnclosingContextIfAvailable();
							stream.add(EventGenerator.invocation(methodBinding));
						} else {
							String methodName = node.getName().getIdentifier();
							Expression expression = node.getExpression();
							String typeName = expression.toString();
							addEnclosingContextIfAvailable();
							stream.add(EventGenerator.invocation(typeName, methodName));
						} 
						super.endVisit(node);
					}

					private void addEnclosingContextIfAvailable() {
						if (elementCtx != null) {
							stream.add(elementCtx);
							elementCtx = null;
						}
						if (firstCtx != null) {
							stream.add(firstCtx);
							firstCtx = null;
						}
						if (superCtx != null) {
							stream.add(superCtx);
							superCtx = null;
						}

					}

					@Override
					public void endVisit(MethodDeclaration node) {
						super.endVisit(node);
					}
				});
			}

			private ITypeBinding getSuper(ITypeBinding type,
					IMethodBinding method, String sig) {

				if (type.getSuperclass() != null) {
					ITypeBinding stb = getSuper(type.getSuperclass()
							.getTypeDeclaration(), method, sig);
					if (stb != null) {
						superCtx = EventGenerator.superContext(stb, method);
						return stb;
					}
				}
				if (method.getDeclaringClass().getTypeDeclaration() == type)
					return type;
				for (IMethodBinding smb : type.getDeclaredMethods()) {
					if (JavaASTUtil.buildSignature(smb).equals(sig))
						return type;
				}
				return null;
			}

			private ITypeBinding getFirst(ITypeBinding type,
					IMethodBinding method, String sig) {

				for (ITypeBinding itb : type.getInterfaces()) {
					ITypeBinding stb = getFirst(itb.getTypeDeclaration(),
							method, sig);
					if (stb != null) {
						firstCtx = EventGenerator.firstContext(stb, method);
						return stb;
					}
				}
				if (method.getDeclaringClass().getTypeDeclaration() == type)
					return type;
				for (IMethodBinding smb : type.getDeclaredMethods()) {
					if (JavaASTUtil.buildSignature(smb).equals(sig))
						return type;
				}
				return null;
			}

			private ITypeBinding getBase(ITypeBinding tb, IMethodBinding mb, String sig) {
				if (tb.getSuperclass() != null) {
					ITypeBinding stb = getBase(tb.getSuperclass().getTypeDeclaration(), mb, sig);
					if (stb != null)
						return stb;
				}
				for (ITypeBinding itb : tb.getInterfaces()) {
					ITypeBinding stb = getBase(itb.getTypeDeclaration(), mb, sig);
					if (stb != null)
						return stb;
				}
				if (mb.getDeclaringClass().getTypeDeclaration() == tb)
					return tb;
				for (IMethodBinding smb : tb.getDeclaredMethods()) {
					if (JavaASTUtil.buildSignature(smb).equals(sig))
						return tb;
				}
				return null;
			}
		};
		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(classpaths == null ? new String[0] : classpaths,
				new String[] {}, new String[] {}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.createASTs(paths, null, new String[0], r, null);
		System.out.println(stream.size());
		return stream;
	}

	public static ArrayList<File> getPaths(File dir) {
		ArrayList<File> files = new ArrayList<>();
		if (dir.isDirectory())
			for (File sub : dir.listFiles())
				files.addAll(getPaths(sub));
		else if (dir.getName().endsWith(".java"))
			files.add(dir);
		return files;
	}
}
