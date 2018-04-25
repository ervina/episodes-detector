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
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import cc.kave.episodes.model.events.Event;
import cc.kave.episodes.model.events.Events;

import com.google.common.collect.Lists;

public class StreamGenerator {

	public List<Event> generateMethodTraces(File sourcePath,
			String[] classpaths) {
		ArrayList<File> files = getPaths(sourcePath);
		String[] paths = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			paths[i] = files.get(i).getAbsolutePath();
		}
		final List<Event> methodCallSequences = Lists.newLinkedList();
		FileASTRequestor r = new FileASTRequestor() {

			private SimpleName firstCtx;
			private SimpleName superCtx;
			private SimpleName elementCtx;

			private ArrayList<Event> tempStream = new ArrayList<Event>();

			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {

				for (int i = 0; i < cu.types().size(); i++) {
					buildHierarchy((AbstractTypeDeclaration) cu.types()
							.get(i), cu.getPackage() == null ? "" : cu
							.getPackage().getName().getFullyQualifiedName()
							+ ".");
				}

				cu.accept(new ASTVisitor() {
					@Override
					public boolean visit(MethodDeclaration node) {
						
						ASTNode parent = node.getParent();
						IMethodBinding binding = node.resolveBinding();
						
						
						
						

						firstCtx = null;
						superCtx = null;
						SimpleName name = node.getName();
						elementCtx = name;

						return super.visit(node);
					}

					@Override
					public boolean visit(MethodInvocation node) {
						return super.visit(node);
					}
					
					@Override
					public boolean visit(ConstructorInvocation node) {
						// TODO Auto-generated method stub
						return super.visit(node);
					}
					
					@Override
					public void endVisit(MethodInvocation node) {
						
						IMethodBinding resolveMethodBinding = node.resolveMethodBinding();
						
						//invocations
//						binding.getDeclaredReceiverType();
						
						String name = node.getName().getIdentifier();

						if (elementCtx != null) {
							methodCallSequences.add(Events
									.newElementContext(elementCtx
											.getFullyQualifiedName()));
							elementCtx = null;
						}
						methodCallSequences.add(Events.newInvocation(name));

						super.endVisit(node);
					}

					@Override
					public void endVisit(MethodDeclaration node) {
						super.endVisit(node);
					}
				});
			}

			private void buildHierarchy(AbstractTypeDeclaration type,
					String prefix) {
				if (type instanceof TypeDeclaration) {
					
					String typeName = prefix + type.getName().getIdentifier();
					
					if (((TypeDeclaration) type).getSuperclassType() != null) {
					}
				}
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
		parser.setEnvironment(classpaths == null ? new String[0]
				: classpaths, new String[] {}, new String[] {}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.createASTs(paths, null, new String[0], r, null);
		return methodCallSequences;
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
