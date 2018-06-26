package cc.episodeMining.mappings;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;

import java.nio.file.attribute.AttributeView;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import cc.episodeMining.mudetect.TransformerUtils;
import cc.kave.commons.model.naming.Names;
import de.tu_darmstadt.stg.mubench.DataEdgeTypePriorityOrder;
import de.tu_darmstadt.stg.mudetect.VeryUnspecificReceiverTypePredicate;
import de.tu_darmstadt.stg.mudetect.aug.model.APIUsageExample;
import de.tu_darmstadt.stg.mudetect.aug.model.Location;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;
import de.tu_darmstadt.stg.mudetect.aug.visitors.AUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.aug.visitors.BaseAUGLabelProvider;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledEdgeMatcher;
import de.tu_darmstadt.stg.mudetect.matcher.EquallyLabelledNodeMatcher;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder;
import de.tu_darmstadt.stg.mudetect.overlapsfinder.AlternativeMappingsOverlapsFinder.Config;

public class DisconnectedPatternsOverlapFinderTest {

	private static final int SUPPORT = 10;
	
	private Config config;

	private MethodCallNode node1 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, A, 1].a()"));
	private MethodCallNode node2 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, B, 2].b()"));
	private MethodCallNode node3 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, C, 3].c()"));
	private MethodCallNode node4 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, D, 4].d()"));
	private MethodCallNode node5 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, A, 1].a()"));
	private MethodCallNode node6 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, B, 2].b()"));
	
	private Matcher<? super Node> mc1 = methodCall("Namespace.Type", "a()");
	private Matcher<? super Node> mc2 = methodCall("Namespace.Type", "b()");
	private Matcher<? super Node> mc3 = methodCall("Namespace.Type", "c()");
	private Matcher<? super Node> mc4 = methodCall("Namespace.Type", "d()");

	private static final Location SOME_LOCATION = new Location(":project:",
			":file:", ":method():");

	APIUsagePattern pattern;
	APIUsageExample target;

	private DisconnectedPatternsOverlapFinder sut;

	@Before
	public void setup() {
		pattern = new APIUsagePattern(SUPPORT, new HashSet<>());
		target = new APIUsageExample(SOME_LOCATION);
		
		target.addVertex(node1);
		target.addVertex(node2);
		target.addVertex(node3);
		target.addVertex(node4);
		target.addVertex(node5);
		target.addVertex(node6);
		
		target.addEdge(node1, node2, new OrderEdge(node1, node2));
		target.addEdge(node1, node3, new OrderEdge(node1, node3));
		target.addEdge(node1, node5, new OrderEdge(node1, node5));
		target.addEdge(node1, node6, new OrderEdge(node1, node6));
		target.addEdge(node1, node4, new OrderEdge(node1, node4));
		
		target.addEdge(node2, node3, new OrderEdge(node2, node3));
		target.addEdge(node2, node5, new OrderEdge(node2, node5));
		target.addEdge(node2, node6, new OrderEdge(node2, node6));
		target.addEdge(node2, node4, new OrderEdge(node2, node4));
		
		target.addEdge(node3, node5, new OrderEdge(node3, node5));
		target.addEdge(node3, node6, new OrderEdge(node3, node6));
		target.addEdge(node3, node4, new OrderEdge(node3, node4));
		
		target.addEdge(node5, node6, new OrderEdge(node5, node6));
		target.addEdge(node5, node4, new OrderEdge(node5, node4));
		
		target.addEdge(node6, node4, new OrderEdge(node6, node4));
		
		AUGLabelProvider labelProvider = new BaseAUGLabelProvider();
		
		config = new AlternativeMappingsOverlapsFinder.Config() {
			{
				isStartNode = super.isStartNode
						.and(new VeryUnspecificReceiverTypePredicate()
								.negate());
				nodeMatcher = new EquallyLabelledNodeMatcher(
						labelProvider);
				edgeMatcher = new EquallyLabelledEdgeMatcher(
						labelProvider);
				edgeOrder = new DataEdgeTypePriorityOrder();
				extensionEdgeTypes = new HashSet<>(Arrays
						.asList(OrderEdge.class));
			}
		};
		
		sut = new DisconnectedPatternsOverlapFinder(config);
	}

	@Test
	public void connectedPattern() {
		pattern.addVertex(node1);
		pattern.addVertex(node2);
		pattern.addVertex(node3);

		pattern.addEdge(node1, node2, new OrderEdge(node1, node2));
		pattern.addEdge(node1, node3, new OrderEdge(node1, node3));

		List<Overlap> actuals = sut.findOverlaps(target, pattern);
		
		assertThat(actuals, hasSize(2));
		
		Overlap overlap1 = actuals.get(0);
//		assertThat(overlap1.getNodeSize(), is(3));
		assertThat(overlap1.getPattern(), hasNodes(mc1, mc2, mc3));
	}
}
