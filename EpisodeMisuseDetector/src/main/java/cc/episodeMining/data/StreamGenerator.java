package cc.episodeMining.data;

import java.io.File;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import cc.kave.commons.model.ssts.impl.visitor.AbstractTraversingNodeVisitor;
import cc.kave.commons.model.typeshapes.ITypeShape;
import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Events;

import com.google.common.collect.Lists;

public class StreamGenerator {

	private List<Event> eventStream = Lists.newLinkedList();

	public void addNow(File sourcePath, String[] classpaths) {
		List<File> files = getPaths(sourcePath);

		for (File file : files) {
			String path = file.getAbsolutePath();
			new StreamGenerationVisitor();
		}
	}

	public List<Event> getEventStream() {
		return eventStream;
	}

	public class StreamGenerationVisitor extends
			AbstractTraversingNodeVisitor<ITypeShape, Void> {

		private SimpleName firstCtx;
		private SimpleName superCtx;
		private SimpleName elementCtx;

		FileASTRequestor r = new FileASTRequestor() {

			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				cu.accept(new ASTVisitor() {

					// public Void visit(IMethodDeclaration decl, ITypeShape
					// context) {
					//
					// firstCtx = null;
					// superCtx = null;
					// IMethodName name = decl.getName();
					// elementCtx = name;
					// for (IMethodHierarchy h : context.getMethodHierarchies())
					// {
					// if (h.getElement().equals(name)) {
					// firstCtx = h.getFirst();
					// superCtx = h.getSuper();
					// }
					// }
					// return super.visit(decl, context);
					// }

					@Override
					public boolean visit(MethodDeclaration node) {

						firstCtx = null;
						superCtx = null;
						SimpleName name = node.getName();
						elementCtx = name;

						// node.getBody();
						// TODO Auto-generated method stub
						return super.visit(node);
					}

					// @Override
					// public Void visit(IInvocationExpression inv, ITypeShape
					// context) {
					// IMethodName erased = erase(inv.getMethodName());
					// addEnclosingMethodIfAvailable();
					// events.add(Events.newInvocation(erased));
					// return null;
					// }

					@Override
					public boolean visit(MethodInvocation node) {
						SimpleName name = node.getName();
						addEnclosingMethodIfAvailable();
						eventStream.add(Events.newInvocation(name.getFullyQualifiedName()));
						// TODO Auto-generated method stub
						return super.visit(node);
					}

					@Override
					public void endVisit(MethodDeclaration node) {
						eventStream.add(null);
						super.endVisit(node);
					}
				});
			}
		};

		private void addEnclosingMethodIfAvailable() {
			if (elementCtx != null) {
				eventStream.add(Events.newElementContext(elementCtx
						.getFullyQualifiedName()));
				elementCtx = null;
			}
			if (firstCtx != null) {
				eventStream.add(Events.newFirstContext(firstCtx
						.getFullyQualifiedName()));
				firstCtx = null;
			}
			if (superCtx != null) {
				eventStream.add(Events.newSuperContext(superCtx
						.getFullyQualifiedName()));
				superCtx = null;
			}
		}
	}
//
//	public void add(File sourcePath, String[] classpaths) {
//		List<File> files = getPaths(sourcePath);
//		String[] paths = new String[files.size()];
//		for (int i = 0; i < files.size(); i++) {
//			paths[i] = files.get(i).getAbsolutePath();
//		}
//		FileASTRequestor r = new FileASTRequestor() {
//			@Override
//			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
//				cu.accept(new ASTVisitor() {
//					@Override
//					public boolean visit(MethodDeclaration node) {
//						node.getBody();
//						// TODO Auto-generated method stub
//						return super.visit(node);
//					}
//
//					@Override
//					public boolean visit(MethodInvocation node) {
//						// TODO Auto-generated method stub
//						return super.visit(node);
//					}
//
//					@Override
//					public void endVisit(MethodDeclaration node) {
//						methodCallSequences.add(null);
//						super.endVisit(node);
//					}
//				});
//			}
//		};
//		@SuppressWarnings("rawtypes")
//		Map options = JavaCore.getOptions();
//		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
//		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
//				JavaCore.VERSION_1_8);
//		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
//		ASTParser parser = ASTParser.newParser(AST.JLS8);
//		parser.setCompilerOptions(options);
//		parser.setEnvironment(classpaths == null ? new String[0] : classpaths,
//				new String[] {}, new String[] {}, true);
//		parser.setResolveBindings(true);
//		parser.setBindingsRecovery(true);
//		parser.createASTs(paths, null, new String[0], r, null);
//	}

	public static List<File> getPaths(File dir) {
		List<File> files = Lists.newLinkedList();
		if (dir.isDirectory())
			for (File sub : dir.listFiles())
				files.addAll(getPaths(sub));
		else if (dir.getName().endsWith(".java"))
			files.add(dir);
		return files;
	}
}
