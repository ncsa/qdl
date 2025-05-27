package org.qdl_lang.expressions;

import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  4:08 PM
 */
public class ConstantNode extends ExpressionImpl {
    @Override
    public Object evaluate(State state) {
        setEvaluated(true);
        return result;
    }

/*    public ConstantNode(String result) {
        this(new QDLValue(result));
    }

    public ConstantNode(Long result) {
        this(new QDLValue(result));
    }

    public ConstantNode(BigDecimal result) {
        this(result, Constant.DECIMAL_TYPE);
    }

    public ConstantNode(Boolean result) {
        this(result, Constant.BOOLEAN_TYPE);
    }

    public ConstantNode(QDLNull result) {
        this(result, Constant.NULL_TYPE);
    }

    public ConstantNode(Object result) {
        this(result, Constant.getType(result));
    }*/

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
