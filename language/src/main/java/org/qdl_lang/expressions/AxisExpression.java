package org.qdl_lang.expressions;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLValue;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

public class AxisExpression extends ExpressionImpl {
    public AxisExpression() {
    }

    public AxisExpression(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    public AxisExpression(int operatorType, TokenPosition tokenPosition) {
        super(operatorType, tokenPosition);
    }

    public AxisExpression(int operatorType) {
        super(operatorType);
    }

    @Override
    public ExpressionInterface makeCopy() {
        return null;
    }

    @Override
    public int getNodeType() {
        return AXIS_RESTRICTION_NODE;
    }

    @Override
    public QDLValue evaluate(State state) {
        QDLValue arg0 = null;
        if(getArgAt(0) instanceof VariableNode) {
            VariableNode vNode = (VariableNode)getArgAt(0);
            String varName = vNode.getVariableReference();
            if (vNode.getVariableReference().endsWith(QDLStem.STEM_INDEX_MARKER)) {
                arg0 = state.getValue(varName);
            }else{
                arg0 = state.getValue(varName + QDLStem.STEM_INDEX_MARKER);
            }
        }else {
             arg0 = evalArg(0, state);
        }
        if (arg0.isStem()) {
            setStem( arg0.asStem() );
        } else {
            if (arg0.isAxisRestriction()) {
                setAxisExpression(arg0.asAxisExpression());
            } else {
                throw new BadArgException("only stems have axes", getArgAt(0));
            }
        }
        Object arg1;
        if (getArguments().size() > 1) {
            arg1 = evalArg(1, state);
            if (arg1 instanceof AllIndices) {
                setStar(true);
            } else {
                if (arg1 instanceof Long) {
                    setAxis((Long) arg1);
                } else {
                    throw new BadArgException("axis must be an integer", getArgAt(1));
                }
            }
        }
        setEvaluated(true);
        setResult(asQDLValue(this));
        return getResult();
    }

    public boolean hasAxis() {
        return axis != null;
    }

    public Long getAxis() {
        return axis;
    }

    public void setAxis(Long axis) {
        this.axis = axis;
    }

    Long axis = null;

    public QDLStem getStem() {
        return stem;
    }

    public void setStem(QDLStem stem) {
        this.stem = stem;
    }

    QDLStem stem = null;

    public boolean hasStem() {
        return stem != null;
    }

    public AxisExpression getAxisExpression() {
        return axisExpression;
    }

    public void setAxisExpression(AxisExpression axisExpression) {
        this.axisExpression = axisExpression;
    }

    AxisExpression axisExpression = null;

    public boolean hasAxisExpression() {
        return axisExpression != null;
    }

    @Override
    public String toString() {
        return "AxisExpression{" +
                "axis=" + axis +
                ", stem=" + stem +
                ", axisExpression=" + axisExpression +
                ", star=" + star +
                '}';
    }

    public boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    boolean star = false;
}
