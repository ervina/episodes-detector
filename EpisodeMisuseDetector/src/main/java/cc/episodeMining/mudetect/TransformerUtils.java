package cc.episodeMining.mudetect;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

class TransformerUtils {
    public static MethodCallNode createCallNode(IMethodName method) {
        // TODO consider parameter names
        return new MethodCallNode(method.getDeclaringType().getFullName(), method.getName() + "()");
    }
}
