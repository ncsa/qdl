package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.exceptions.ReturnException;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.StemListNode;
import org.qdl_lang.module.Module;

/**
 * Very much improved way to handle assignments. Use this
 * <p>Created by Jeff Gaynor<br>
 * on 6/3/21 at  5:10 AM
 */
public class ANode2 extends ExpressionImpl {
    TokenPosition tokenPosition = null;

    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    @Override
    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    @Override
    public boolean hasTokenPosition() {
        return tokenPosition != null;
    }

    public ANode2(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    String op;

    public ExpressionInterface getLeftArg() {
        if (!hasLeftArg()) {
            return null;
        }
        return getArguments().get(0);
    }

    public void setLeftArg(ExpressionInterface leftArg) {
        if (getArguments().size() == 0) {
            getArguments().add(leftArg);
        } else {
            getArguments().set(0, leftArg);
        }
    }

    public ExpressionInterface getRightArg() {
        if (!hasRightArg()) {
            return null;
        }
        return getArguments().get(1);
    }

    public void setRightArg(ExpressionInterface rightArg) {
        if (getArguments().size() == 1) {

            getArguments().add(rightArg);
        } else {
            getArguments().set(1, rightArg);
        }
    }

    public boolean hasLeftArg() {
        return getArguments() != null && (!getArguments().isEmpty());
    }

    public boolean hasRightArg() {
        return getArguments() != null && (2 <= getArguments().size());
    }

    @Override
    public Object evaluate(State state) {
        //     return oldAssign(state);
        return newAssign(state);
    }

    public Object newAssign(State state) {
        Dyad d = null;

        if (getAssignmentType() != leftAssignmentType && getAssignmentType() != rightAssignmentType) {
            // Do other assignments like +=
            d = new Dyad(getAssignmentType());
            if (getLeftArg().getNodeType() == ExpressionInterface.ASSIGNMENT_NODE) {
                d.setLeftArgument(((ANode2) getLeftArg()).getRightArg());
            } else {
                d.setLeftArgument(getLeftArg());
            }

            d.setRightArgument(getRightArg());
            d.evaluate(state);
            setResult(d.getResult());
            setResultType(d.getResultType());
        } else {
            // regular assignment, evaluate RHS. That is result.
            try {
                getRightArg().evaluate(state);
            }catch(ReturnException returnException){
                if(!returnException.hasResult()){
                    // edge case where someone is trying to assign a value from a return();
                    throw new QDLExceptionWithTrace("no value", getRightArg() );
                }
            }
            setResult(getRightArg().getResult());
            setResultType(getRightArg().getResultType());
        }
        setEvaluated(true);
        ANode2 lastAnode = this;
        ExpressionInterface realLeftArg = getLeftArg();
        boolean chained = false;
        while (realLeftArg.getNodeType()==ExpressionInterface.ASSIGNMENT_NODE) {
            ANode2 rla = (ANode2) realLeftArg;
            ANode2 xNode = new ANode2(rla.getTokenPosition());
            xNode.setLeftArg(rla.getRightArg());
            xNode.setRightArg(lastAnode.getRightArg());
            xNode.setOp(lastAnode.getOp());
            xNode.evaluate(state);
            lastAnode = rla;
            realLeftArg = rla.getLeftArg();
            chained = true;
        }
        if (chained) {
            // Since this was chained, there is every reason to suspect that the
            // value from earlier has been altered. Update it.
            lastAnode.evaluate(state);
            setResult(lastAnode.getResult());
            setResultType(lastAnode.getResultType());
            setEvaluated(true);
        }
        while (realLeftArg instanceof ParenthesizedExpression) {
            realLeftArg = ((ParenthesizedExpression) realLeftArg).getExpression();
        }
        if (realLeftArg instanceof StemListNode) {
            StemListNode stemListNode = (StemListNode) realLeftArg;
            QDLStem rStem;
            if (getResult() instanceof QDLStem) {
                rStem = (QDLStem) getResult();
                if (!rStem.isList()) {
                    throw new QDLExceptionWithTrace("unknown rh node type", getRightArg());
                }
            } else {
                rStem = new QDLStem();
                rStem.setDefaultValue(getResult());
            }

            for (int i = 0; i < stemListNode.getStatements().size(); i++) {
                ExpressionInterface swri = stemListNode.getStatements().get(i);
                if (!(swri instanceof VariableNode)) {
                    throw new QDLExceptionWithTrace("unknown left assignment statement ", getLeftArg());
                }
                Object value = rStem.get(i);
                // https://github.com/ncsa/qdl/issues/20
                state.getTargetState().setValue(((VariableNode) swri).getVariableReference(), value);
            }
            return getResult();
        }
        if (realLeftArg instanceof VariableNode) {
            // https://github.com/ncsa/qdl/issues/20
            state.getTargetState().setValue(((VariableNode) realLeftArg).getVariableReference(), getResult());
            // last detail that cannot be done until this point is to set the alias if it is a module assigned to the variable.
            if(getResult() instanceof Module){
                ((Module)getResult()).setAlias(((VariableNode) realLeftArg).getVariableReference());
            }
            return getResult();
        }
        if (realLeftArg instanceof ConstantNode) {
            throw new IllegalArgumentException("error: cannot assign value to constant \"" + getLeftArg().getResult() + "\"");
        }
        // So all we have in the LHS is an ESN2
        if (realLeftArg instanceof ESN2) {
            ((ESN2) realLeftArg).set(state, getResult());
            return getResult();
        }
        if (realLeftArg instanceof ModuleExpression) {
            ((ModuleExpression) realLeftArg).set(state, getResult());
            return getResult();
        }
        throw new QDLExceptionWithTrace("unknown type for left hand side", getLeftArg());
    }


    @Override
    public ExpressionInterface makeCopy() {
        ANode2 aNode2 = new ANode2(getTokenPosition());
        aNode2.setOp(getOp());
        aNode2.setLeftArg(getLeftArg().makeCopy());
        aNode2.setRightArg(getRightArg().makeCopy());
        return aNode2;
    }

    public static final int leftAssignmentType = -100;
    public static final int rightAssignmentType = -200;

    public int getAssignmentType() {
        switch (op) {
            case "+=":
                return OpEvaluator.PLUS_VALUE;
            case "-=":
                return OpEvaluator.MINUS_VALUE;
            case "×=":
            case "*=":
                return OpEvaluator.TIMES_VALUE;
            case "÷=":
            case "/=":
                return OpEvaluator.DIVIDE_VALUE;
            case "%=":
                return OpEvaluator.INTEGER_DIVIDE_VALUE;
            case "^=":
                return OpEvaluator.POWER_VALUE;
            case ":=":
            case "≔":
                return leftAssignmentType;
            case "=:":
            case "≕":
                return rightAssignmentType;
        }
        return OpEvaluator.UNKNOWN_VALUE;
    }

  /*  protected Object setExpValue(State state, ExpressionStemNode esn, int resultType, Object result) {
        return esn.setValue(state, result);
    }*/

  /*  protected Object setVariableValue(State state, String variableReference, int resultType, Object result) {
        // Now the real work -- set the value of the variable in the symbol table.
        // Mostly this just throws an exception if some how we get an unknown type, but this is the
        // right place to do it, before it gets in to the symbol table.

        switch (resultType) {

            case STEM_TYPE:
                if (!variableReference.endsWith(STEM_INDEX_MARKER)) {
                    throw new IllegalArgumentException("Error: Cannot set the stem \"" + variableReference + "\" to a non-stem variable");
                }

                state.setValue(variableReference, result);
                break;
            case NULL_TYPE:
                // Can set any variable to null
                state.setValue(variableReference, QDLNull.getInstance());
                break;

            case STRING_TYPE:
            case BOOLEAN_TYPE:
            case LONG_TYPE:
            case DECIMAL_TYPE:
                if (variableReference.endsWith(STEM_INDEX_MARKER)) {
                    throw new IllegalArgumentException("Error: Cannot set the scalar value \"" + result + "\"to a stem variable \"" + variableReference + "\"");
                }

                state.setValue(variableReference, result);
                break;
            default:
                throw new IllegalArgumentException("error, the type of the value \"" + result + "\" is unknown");

        }
        return result;
    }*/

    @Override
    public String toString() {
        return "ANode2{" +
                "op='" + op + '\'' +
                ", left=" + getLeftArg() +
                ", right=" + getRightArg() +
                '}';
    }

    @Override
    public int getNodeType() {
        return ASSIGNMENT_NODE;
    }
}
