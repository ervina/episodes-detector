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
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import cc.episodeMining.mubench.model.EventGenerator;
import cc.kave.episodes.model.events.Event;

import com.google.common.collect.Lists;

import edu.iastate.cs.egroum.utils.JavaASTUtil;

public class SequenceGenerator {

	private List<Event> stream;

	public SequenceGenerator() {
		this.stream = Lists.newLinkedList();
	}

	public List<Event> generateMethodTraces(File sourcePath, String[] classpaths) {
		ArrayList<File> files = getPaths(sourcePath);
		String[] paths = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			paths[i] = files.get(i).getAbsolutePath();
		}
		FileASTRequestor r = new FileASTRequestor() {

			private String type;

			private Event firstCtx;
			private Event superCtx;
			private Event elementCtx;

			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				
				String relativePath = "." + sourceFilePath.substring(sourcePath.toString().length());
				stream.add(EventGenerator.absolutePath(sourceFilePath));
				stream.add(EventGenerator.relativePath(relativePath));
				
				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(MethodDeclaration node) {

						ASTNode parent = node.getParent();

						IMethodBinding binding = node.resolveBinding();
//						adding the parameter types
//						binding.getParameterTypes()

						firstCtx = null;
						superCtx = null;

						String typeName = binding.getDeclaringClass().getName();
						String methodName = binding.getName();

						elementCtx = EventGenerator.elementContext(typeName,
								methodName);

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
					public boolean visit(ClassInstanceCreation node) {
						return super.visit(node);
					}

					@Override
					public boolean visit(TypeDeclaration node) {

						type = null;

						ITypeBinding binding = node.resolveBinding();
						ITypeBinding tb = binding.getTypeDeclaration();
						type = tb.getName();

						return super.visit(node);
					}

					@Override
					public boolean visit(Initializer node) {
						if (type != null) {
							stream.add(EventGenerator.initializer(type));
							type = null;
						}
						return super.visit(node);
					}

					@Override
					public void endVisit(Initializer node) {
						super.endVisit(node);
					}

					@Override
					public void endVisit(ClassInstanceCreation node) {

						IMethodBinding mb = node.resolveConstructorBinding();
						if (mb != null) {
							IMethodBinding md = mb.getMethodDeclaration();
							String sig = JavaASTUtil.buildSignature(md);
							ITypeBinding tb = getBase(md.getDeclaringClass()
									.getTypeDeclaration(), md, sig);
							String type = tb.getName();

							addEnclosingContextIfAvailable();
							stream.add(EventGenerator.constructor(type));
						} else {
							try {
								throw new Exception("Unresolved type");
							} catch (Exception e) {
								System.out.println(node.toString());
								e.printStackTrace();
							}
						}
						super.endVisit(node);
					}

					@Override
					public void endVisit(MethodInvocation node) {

						ASTNode parent = node.getParent();

						IMethodBinding mb = node.resolveMethodBinding();

						if (mb != null) {
							IMethodBinding md = mb.getMethodDeclaration();
							String sig = JavaASTUtil.buildSignature(md);
							ITypeBinding tb = getBase(md.getDeclaringClass()
									.getTypeDeclaration(), md, sig);
							String type = tb.getName();

							addEnclosingContextIfAvailable();
							stream.add(EventGenerator.invocation(type,
									md.getName()));
						} else {
							try {
								throw new Exception("Unresolved type");
							} catch (Exception e) {
								System.out.println(node.toString());
								e.printStackTrace();
							}
						}
						super.endVisit(node);
					}

					@Override
					public void endVisit(TypeDeclaration node) {
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

			private ITypeBinding getSuper(ITypeBinding tb, IMethodBinding mb,
					String sig) {

				if (tb.getSuperclass() != null) {
					ITypeBinding stb = getSuper(tb.getSuperclass()
							.getTypeDeclaration(), mb, sig);
					if (stb != null) {
						superCtx = EventGenerator.superContext(stb.getName(),
								mb.getName());
						return stb;
					}
				}
				if (mb.getDeclaringClass().getTypeDeclaration() == tb)
					return tb;
				for (IMethodBinding smb : tb.getDeclaredMethods()) {
					if (JavaASTUtil.buildSignature(smb).equals(sig))
						return tb;
				}
				return null;
			}

			private ITypeBinding getFirst(ITypeBinding tb, IMethodBinding mb,
					String sig) {

				for (ITypeBinding itb : tb.getInterfaces()) {
					ITypeBinding stb = getFirst(itb.getTypeDeclaration(), mb,
							sig);
					if (stb != null) {
						firstCtx = EventGenerator.firstContext(stb.getName(),
								mb.getName());
						return stb;
					}
				}
				if (mb.getDeclaringClass().getTypeDeclaration() == tb)
					return tb;
				for (IMethodBinding smb : tb.getDeclaredMethods()) {
					if (JavaASTUtil.buildSignature(smb).equals(sig))
						return tb;
				}
				return null;
			}

			private ITypeBinding getBase(ITypeBinding tb, IMethodBinding mb,
					String sig) {
				if (tb.getSuperclass() != null) {
					ITypeBinding stb = getBase(tb.getSuperclass()
							.getTypeDeclaration(), mb, sig);
					if (stb != null)
						return stb;
				}
				for (ITypeBinding itb : tb.getInterfaces()) {
					ITypeBinding stb = getBase(itb.getTypeDeclaration(), mb,
							sig);
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
