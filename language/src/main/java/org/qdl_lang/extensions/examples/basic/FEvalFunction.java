package org.qdl_lang.extensions.examples.basic;

import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.ExpressionImpl;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

import static org.qdl_lang.evaluate.AbstractEvaluator.getOperator;

/** Example of a QDL function for a module that accepts a function reference.
 * This takes two arguments
 * <pre>
 *     f_eval(@f, x)
 * </pre>
 * and returns
 * <pre>
 *     f(x)
 * </pre>
 * <p>Created by Jeff Gaynor<br>
 * on 10/7/21 at  4:40 PM
 */
public class FEvalFunction implements QDLFunction {
    public static String F_NAME = "f_eval";
    @Override
    public String getName() {
        return F_NAME;
    }

    @Override
    public int[] getArgCount() {
        return new int[]{2};
    }

    @Override
    public QDLValue evaluate(QDLValue[] qdlValues, State state) {
        // no real argument checking done since this is sample code.
        ExpressionImpl expression = getOperator(state, qdlValues[0].asFunction(), 1);
        expression.getArguments().add( new ConstantNode(qdlValues[1]));
        return expression.evaluate(state);
    }

    @Override
    public List<String> getDocumentation(int argCount) {
        List<String> doxx = new ArrayList<>();

        doxx.add(getName() + "(@f, x) - simple basic that evaluates f at x using a function reference.");
        doxx.add("E.g.");
        doxx.add("    eg#" + getName() + "(@cos, pi()/7)");
        doxx.add("0.900968867902419");
        doxx.add("\nThis is the same as issuing cos(pi()/7)");
        return doxx;
    }
}
