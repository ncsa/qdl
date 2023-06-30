package edu.uiuc.ncsa.qdl.expressions;

import edu.uiuc.ncsa.qdl.exceptions.BadArgException;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;
import edu.uiuc.ncsa.qdl.variables.*;

import java.util.ArrayList;

/**
 * An expression for switches. The contract is this:
 * <pre>
 *     switch.¿case.:default
 * </pre>
 * Where
 * <ul>
 *     <li>switch. - a boolean list with at most one true element</li>
 *     <li>case. - a list of expressions. Only the true element of switch. is evaluated</li>
 *     <li>default - value to return if all of the switch. elements are false</li>
 * </ul>
 * Since this is an expression is returns a value -- always.
 * <p>Created by Jeff Gaynor<br>
 * on 6/30/23 at  2:13 PM
 */
// Fixes https://github.com/ncsa/oa4mp/issues/114
public class SwitchExpressionNode extends ExpressionImpl {
    public ExpressionInterface getSWITCH() {
        return getArguments().get(0);
    }

    public void setSWITCH(ExpressionInterface x) {
        getArguments().set(0, x);
    }

    public ExpressionInterface getCASE() {
        return getArguments().get(1);
    }

    public void setCASE(ExpressionInterface x) {
        getArguments().set(1, x);
    }

    public ExpressionInterface getDEFAULT() {
        return getArguments().get(2);
    }

    public void setDEFAULT(ExpressionInterface x) {
        getArguments().set(2, x);
    }

    @Override
    public ExpressionInterface makeCopy() {
        return null;
    }

    @Override
    public ArrayList<ExpressionInterface> getArguments() {
        if (arguments == null || arguments.size() != 3) {
            arguments = new ArrayList<>(3);
            arguments.add(null);
            arguments.add(null);
            arguments.add(null);
        }
        return arguments;
    }

    @Override
    public Object evaluate(State state) {
        Object obj = getSWITCH().evaluate(state);
        QDLStem stem = null;

        if ((obj instanceof QDLStem)) {
            stem = (QDLStem) obj;
        } else {
            if (obj instanceof Boolean) {
                Object result;
                if ((Boolean) obj) {
                    result = getCASE().evaluate(state);
                } else {
                    result = getDEFAULT().evaluate(state);
                }
                setResult(result);
                setResultType(Constant.getType(result));
                setEvaluated(true);
                return result;
            }
            throw new BadArgException("left hand argument must be a boolean if its a scalar", getSWITCH());
        }

        Object foundIndex = null;
        for (Object k : stem.keySet()) {
            Object value = stem.get(k);
            if (!(value instanceof Boolean)) {
                throw new BadArgException("left hand argument at index '" + k + "' is not a boolean", getSWITCH());
            }
            Boolean b = (Boolean) value;
            if (b) {
                if (foundIndex != null) {
                    throw new BadArgException("already found true value at index " + foundIndex + " A second true value has been found at index " + k, getSWITCH());
                }
                foundIndex = k;
            }
        }
        if (foundIndex == null) {
            // use the default
            setResult(getDEFAULT().evaluate(state));
            setResultType(getDEFAULT().getResultType());
            setEvaluated(true);
            return getResult();
        }
        // otherwise, find foundIndex and evaluate that.
        Object caseObj = getCASE();
        if (caseObj instanceof StemListNode) {
            StemListNode stemListNode = (StemListNode) caseObj;
            if (!(foundIndex instanceof Long)) {
                throw new BadArgException("no such index " + foundIndex + " exists in this list.", getCASE());
            }
            Object r = stemListNode.getStatements().get(((Long) foundIndex).intValue()).evaluate(state);
            setResult(r);
            setResultType(Constant.getType(r));
            setEvaluated(true);
            return r;
        }
        if (caseObj instanceof StemVariableNode) {
            StemVariableNode stemVariableNode = (StemVariableNode) caseObj;
            for (StemEntryNode stemEntryNode : stemVariableNode.getStatements()) {
                Object key = stemEntryNode.getKey().evaluate(state);
                if (key.equals(foundIndex)) {
                    Object result = stemEntryNode.getValue().evaluate(state);
                    setResult(result);
                    setResultType(Constant.getType(result));
                    setEvaluated(true);
                    return result;
                }
            }
            // We have to cruise through the
        }
        throw new BadArgException("Unknown case type " + getCASE().getClass().getName(), getCASE());
    }
}
