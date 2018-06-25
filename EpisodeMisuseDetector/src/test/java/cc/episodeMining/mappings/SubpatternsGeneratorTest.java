package cc.episodeMining.mappings;

import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasNodes;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.AUGMatchers.hasOrderEdge;
import static de.tu_darmstadt.stg.mudetect.aug.matchers.NodeMatchers.methodCall;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import cc.episodeMining.mudetect.TransformerUtils;
import cc.kave.commons.model.naming.Names;
import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import de.tu_darmstadt.stg.mudetect.aug.model.controlflow.OrderEdge;
import de.tu_darmstadt.stg.mudetect.aug.model.patterns.APIUsagePattern;

public class SubpatternsGeneratorTest {

	private static final int SUPPORT = 10;

	private MethodCallNode node1 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, A, 1].a()"));
	private MethodCallNode node2 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, B, 1].b()"));
	private MethodCallNode node3 = TransformerUtils.createCallNode(Names
			.newMethod("0M:[p:void] [i:Namespace.Type, C, 1].c()"));

	private Matcher<? super Node> mc1 = methodCall("Namespace.Type", "a()");
	private Matcher<? super Node> mc2 = methodCall("Namespace.Type", "b()");
	private Matcher<? super Node> mc3 = methodCall("Namespace.Type", "c()");

	private APIUsagePattern pattern;

	private SubpatternsGenerator sut;

	@Before
	public void setup() {
		pattern = new APIUsagePattern(SUPPORT, new HashSet<>());
		pattern.addVertex(node1);
		pattern.addVertex(node2);
		pattern.addVertex(node3);

		sut = new SubpatternsGenerator();
	}

	@Test
	public void connectedPattern() {
		pattern.addEdge(node1, node2, new OrderEdge(node1, node2));
		pattern.addEdge(node1, node3, new OrderEdge(node1, node3));

		Set<APIUsagePattern> actuals = sut.generate(pattern);

		assertThat(actuals, hasSize(1));
		assertThat(actuals.iterator().next().getSupport(), is(SUPPORT));
		assertThat(actuals.iterator().next(), hasNodes(mc1, mc2, mc3));
		assertThat(actuals.iterator().next(), hasOrderEdge(mc1, mc2));
		assertThat(actuals.iterator().next(), hasOrderEdge(mc1, mc3));
	}

	@Test
	public void setOfNodes() {
		Set<APIUsagePattern> actuals = sut.generate(pattern);

		assertThat(actuals, hasSize(3));

		for (APIUsagePattern sp : actuals) {
			assertThat(sp.getSupport(), is(SUPPORT));
		}
		int i = 0;
		for (APIUsagePattern sp : actuals) {
			i++;
			if (i == 1) {
				assertThat(sp, hasNodes(mc1));
			}
			if (i == 2) {
				assertThat(sp, hasNodes(mc2));
			}
			if (i == 3) {
				assertThat(sp, hasNodes(mc3));
			}
		}
	}

	@Test
	public void mixPatterns() {
		pattern.addEdge(node1, node2, new OrderEdge(node1, node2));
		
		Set<APIUsagePattern> actuals = sut.generate(pattern);
		
		assertThat(actuals, hasSize(2));
		
		for (APIUsagePattern sp : actuals) {
			assertThat(sp.getSupport(), is(SUPPORT));
		}
		int i = 0;
		for (APIUsagePattern sp : actuals) {
			i++;
			if (i == 1) {
				assertThat(sp, hasNodes(mc1, mc2));
				assertThat(sp, hasOrderEdge(mc1, mc2));
			}
			if (i == 2) {
				assertThat(sp, hasNodes(mc3));
			}
		}
	}
}
