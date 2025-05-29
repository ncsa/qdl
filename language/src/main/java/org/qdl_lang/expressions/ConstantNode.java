package org.qdl_lang.expressions;

import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  4:08 PM
 */
public class ConstantNode extends ExpressionImpl {
    @Override
    public QDLValue evaluate(State state) {
        setEvaluated(true);
        return result;
    }

    public ConstantNode(QDLValue result) {
        valence = 0;
        this.result = result;
        evaluated = true; //trivally
        getSourceCode().add(result == null ? "null" : result.toString());
    }

    @Override
    public ExpressionNode makeCopy() {
        ConstantNode constantNode = new ConstantNode(new QDLValue(result));
        return constantNode;
    }
    @Override
        public int getNodeType() {
            return CONSTANT_NODE;
        }
}
