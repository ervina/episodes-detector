package cc.episodeMining.mappings;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;
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
	private Matcher<? super Node> mc5 = methodCall("Namespace.Type", "a()");
	private Matcher<? super Node> mc6 = methodCall("Namespace.Type", "b()");

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
		target.addVertex(node5);
		target.addVertex(node6);
		target.addVertex(node4);

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
						.and(new VeryUnspecificReceiverTypePredicate().negate());
				nodeMatcher = new EquallyLabelledNodeMatcher(labelProvider);
				edgeMatcher = new EquallyLabelledEdgeMatcher(labelProvider);
				edgeOrder = new DataEdgeTypePriorityOrder();
				extensionEdgeTypes = new HashSet<>(
						Arrays.asList(OrderEdge.class));
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
		Overlap overlap2 = actuals.get(1);
		
		assertPattern(overlap1);
		assertTarget(overlap1);
		
		assertPattern(overlap2);
		assertTarget(overlap2);

		if (overlap1.getNodeSize() == 3) {
			assertThat(overlap1.getNodeSize(), is(3));
			assertThat(overlap2.getNodeSize(), is(2));
			
			assertThat(overlap1.getEdgeSize(), is(2));
			assertThat(overlap2.getEdgeSize(), is(1));
			
			assertTrue(overlap1.getMappedTargetNodes().contains(node1));
			assertTrue(overlap1.getMappedTargetNodes().contains(node2));
			assertTrue(overlap1.getMappedTargetNodes().contains(node3));
			
			assertTrue(overlap2.getMappedTargetNodes().contains(node5));
			assertTrue(overlap2.getMappedTargetNodes().contains(node6));
			
		} else if (overlap1.getNodeSize() == 2) {
			assertThat(overlap1.getNodeSize(), is(2));
			assertThat(overlap2.getNodeSize(), is(3));
			
			assertThat(overlap1.getEdgeSize(), is(1));
			assertThat(overlap2.getEdgeSize(), is(2));
			
			assertTrue(overlap1.getMappedTargetNodes().contains(node5));
			assertTrue(overlap1.getMappedTargetNodes().contains(node6));
			
			assertTrue(overlap2.getMappedTargetNodes().contains(node1));
			assertTrue(overlap2.getMappedTargetNodes().contains(node2));
			assertTrue(overlap2.getMappedTargetNodes().contains(node3));
		} else {
			assertFalse(true);
		}
	}
	
	@Test
	public void twoDisconnectedPattern() {
		pattern.addVertex(node1);
		pattern.addVertex(node2);
		pattern.addVertex(node3);
		pattern.addVertex(node4);

		pattern.addEdge(node1, node2, new OrderEdge(node1, node2));
		pattern.addEdge(node1, node3, new OrderEdge(node1, node3));

		List<Overlap> actuals = sut.findOverlaps(target, pattern);
		
		assertThat(actuals, hasSize(2));
		
		Overlap overlap1 = actuals.get(0);
		Overlap overlap2 = actuals.get(1);
		
		assertTarget(overlap1);
		assertTarget(overlap2);
		
		if (overlap1.getNodeSize() == 4) {
			assertThat(overlap1.getPattern(), hasNodes(mc1, mc2, mc3, mc4));
			assertThat(overlap1.getPattern(), hasOrderEdge(mc1, mc2));
			assertThat(overlap1.getPattern(), hasOrderEdge(mc1, mc3));
			
			assertThat(overlap2.getPattern(), hasNodes(mc1, mc2, mc4));
			assertThat(overlap2.getPattern(), hasOrderEdge(mc1, mc2));
			
			assertThat(overlap1.getNodeSize(), is(4));
			assertThat(overlap2.getNodeSize(), is(3));
			
			assertThat(overlap1.getEdgeSize(), is(2));
			assertThat(overlap2.getEdgeSize(), is(1));
			
			assertTrue(overlap1.getMappedTargetNodes().contains(node1));
			assertTrue(overlap1.getMappedTargetNodes().contains(node2));
			assertTrue(overlap1.getMappedTargetNodes().contains(node3));
			assertTrue(overlap1.getMappedTargetNodes().contains(node4));
			
			assertTrue(overlap2.getMappedTargetNodes().contains(node1));
			assertTrue(overlap2.getMappedTargetNodes().contains(node2));
			assertTrue(overlap2.getMappedTargetNodes().contains(node4));
		} else if (overlap1.getNodeSize() == 3) {
			assertThat(overlap1.getPattern(), hasNodes(mc1, mc2, mc4));
			assertThat(overlap1.getPattern(), hasOrderEdge(mc1, mc2));
			
			assertThat(overlap2.getPattern(), hasNodes(mc1, mc2, mc3, mc4));
			assertThat(overlap2.getPattern(), hasOrderEdge(mc1, mc2));
			assertThat(overlap2.getPattern(), hasOrderEdge(mc1, mc3));
			
			assertThat(overlap1.getNodeSize(), is(3));
			assertThat(overlap2.getNodeSize(), is(4));
			
			assertThat(overlap1.getEdgeSize(), is(1));
			assertThat(overlap2.getEdgeSize(), is(2));
			
			assertTrue(overlap1.getMappedTargetNodes().contains(node1));
			assertTrue(overlap1.getMappedTargetNodes().contains(node2));
			assertTrue(overlap1.getMappedTargetNodes().contains(node4));
			
			assertTrue(overlap2.getMappedTargetNodes().contains(node1));
			assertTrue(overlap2.getMappedTargetNodes().contains(node2));
			assertTrue(overlap2.getMappedTargetNodes().contains(node3));
			assertTrue(overlap2.getMappedTargetNodes().contains(node4));
		} else {
			assertFalse(true);
		}
	}

	private void assertTarget(Overlap overlap) {
		assertThat(overlap.getTarget(), hasNodes(mc1, mc2, mc3, mc4, mc5, mc6));
		assertThat(overlap.getTarget(), hasOrderEdge(mc1, mc2));
		assertThat(overlap.getTarget(), hasOrderEdge(mc1, mc3));
		assertThat(overlap.getTarget(), hasOrderEdge(mc1, mc5));
		assertThat(overlap.getTarget(), hasOrderEdge(mc1, mc6));
		assertThat(overlap.getTarget(), hasOrderEdge(mc1, mc4));

		assertThat(overlap.getTarget(), hasOrderEdge(mc2, mc3));
		assertThat(overlap.getTarget(), hasOrderEdge(mc2, mc5));
		assertThat(overlap.getTarget(), hasOrderEdge(mc2, mc6));
		assertThat(overlap.getTarget(), hasOrderEdge(mc2, mc4));

		assertThat(overlap.getTarget(), hasOrderEdge(mc3, mc5));
		assertThat(overlap.getTarget(), hasOrderEdge(mc3, mc6));
		assertThat(overlap.getTarget(), hasOrderEdge(mc3, mc4));

		assertThat(overlap.getTarget(), hasOrderEdge(mc5, mc6));
		assertThat(overlap.getTarget(), hasOrderEdge(mc5, mc4));

		assertThat(overlap.getTarget(), hasOrderEdge(mc6, mc4));
	}

	private void assertPattern(Overlap overlap) {
		assertThat(overlap.getPattern(), hasNodes(mc1, mc2, mc3));
		assertThat(overlap.getPattern(), hasOrderEdge(mc1, mc2));
		assertThat(overlap.getPattern(), hasOrderEdge(mc1, mc3));
	}
}
