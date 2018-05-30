package cc.episodeMining.mudetect;

import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.codeelements.IParameterName;
import cc.kave.commons.model.naming.types.ITypeName;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.Iterator;

class TransformerUtils {
    public static MethodCallNode createCallNode(IMethodName method) {
        return new MethodCallNode(getTypeName(method.getDeclaringType()), getMethodSignature(method));
    }

    private static String getTypeName(ITypeName typeName) {
        return typeName.getFullName();
    }

    private static String getMethodSignature(IMethodName method) {
        StringBuilder methodSignature = new StringBuilder(method.getName()).append("(");
        for (Iterator<IParameterName> params = method.getParameters().iterator(); params.hasNext(); ) {
            IParameterName param = params.next();
            methodSignature.append(getTypeName(param.getValueType()));
            if (params.hasNext()) {
                methodSignature.append(", ");
            }
        }
        return methodSignature.append(")").toString();
    }
}
