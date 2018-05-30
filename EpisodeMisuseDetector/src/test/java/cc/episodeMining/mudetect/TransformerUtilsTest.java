package cc.episodeMining.mudetect;

import cc.kave.commons.model.naming.impl.v0.codeelements.MethodName;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TransformerUtilsTest {
    @Test
    public void createCallNodeFromMethod() {
        MethodCallNode callNode = createCallNodeFromMethodIdentifier("[R] [C].m()");

        assertThat(callNode.getDeclaringTypeName(), is("C"));
        assertThat(callNode.getMethodSignature(), is("m()"));
    }

    @Test
    public void createCallNodeFromMethodWithParameter() {
        MethodCallNode callNode = createCallNodeFromMethodIdentifier("[R] [C].m([P1])");

        assertThat(callNode.getMethodSignature(), is("m(P1)"));
    }

    @Test
    public void createCallNodeFromMethodWithParameters() {
        MethodCallNode callNode = createCallNodeFromMethodIdentifier("[R] [C].m([P1], [P2])");

        assertThat(callNode.getMethodSignature(), is("m(P1, P2)"));
    }

    private MethodCallNode createCallNodeFromMethodIdentifier(String identifier) {
        MethodName methodName = new MethodName(identifier);
        return TransformerUtils.createCallNode(methodName);
    }
}
