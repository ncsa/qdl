package org.qdl_lang.expressions;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.*;

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
public class SelectExpressionNode extends ExpressionImpl {
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
        return OLDevaluate(state);
    }
    protected Object NEWevaluate(State state) {
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
                    throw new BadArgException("redundant value at index " + k + " (" + foundIndex + " already found) ", getSWITCH());
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
        ExpressionInterface caseObj = getCASE();
        Object result = getCASE().evaluate(state);
        if(!(result instanceof QDLStem)) {
            throw new BadArgException("argument must be a stem", getCASE());
        }
        Object rr = ((QDLStem) result).get(foundIndex);
        if(rr == null){
            throw new BadArgException("no such index " + foundIndex + " exists in this list.", getCASE());
        }
        setResult(rr);;
        setResultType(Constant.getType(rr));
        setEvaluated(true);
        return rr;

    }

    protected Object OLDevaluate(State state) {
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
                    throw new BadArgException("redundant value at index " + k + " (" + foundIndex + " already found) ", getSWITCH());
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
        ExpressionInterface caseObj = getCASE();
        Object result;
        // The next cases are to try and pick apart switch statements so we don't run the
        // risk of evaluating things that are undefined. Fallthrough case is we just
        // can't figure it out, so try it directly.
         switch (caseObj.getNodeType()){
             case ExpressionInterface.VARIABLE_NODE:
                 result = caseObj.evaluate(state);
                 if(result instanceof QDLStem){
                       QDLStem qdlStem = (QDLStem) result;
                       Object rr = ((QDLStem) result).get(foundIndex);
                       if(rr == null){
                           throw new BadArgException("no such index " + foundIndex + " exists in this list.", getCASE());
                       }
                       setResult(rr);;
                       setResultType(Constant.getType(rr));
                       setEvaluated(true);
                       return rr;
                 }else{
                     throw new BadArgException("scalar value for case not supported", getCASE());
                 }

             case ExpressionInterface.LIST_NODE:
                 StemListNode stemListNode = (StemListNode) caseObj;
                 if (!(foundIndex instanceof Long)) {
                     throw new BadArgException("no such index " + foundIndex + " exists in this list.", getCASE());
                 }
                 Object r = stemListNode.getStatements().get(((Long) foundIndex).intValue()).evaluate(state);
                 setResult(r);
                 setResultType(Constant.getType(r));
                 setEvaluated(true);
                 return r;

             case ExpressionInterface.STEM_NODE:
                 StemVariableNode stemVariableNode = (StemVariableNode) caseObj;
                 for (StemEntryNode stemEntryNode : stemVariableNode.getStatements()) {
                     Object key = stemEntryNode.getKey().evaluate(state);
                     if (key.equals(foundIndex)) {
                          result = stemEntryNode.getValue().evaluate(state);
                         setResult(result);
                         setResultType(Constant.getType(result));
                         setEvaluated(true);
                         return result;
                     }
                 }

             case ExpressionInterface.OPEN_SLICE_NODE:
                 OpenSliceNode openSliceNode = (OpenSliceNode) caseObj;
                 if (!(foundIndex instanceof Long)) {
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                 // Arguments of the slice are the parameters for it, not elements!
                 QDLStem qq = (QDLStem) openSliceNode.evaluate(state);
                 if(!qq.containsKey(foundIndex)){
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                  result = qq.get(foundIndex);
                 setResult(result);
                 setResultType(Constant.getType(result));
                 setEvaluated(true);
                 return result;

             case ExpressionInterface.CLOSED_SLICE_NODE:
                 ClosedSliceNode closedSliceNode = (ClosedSliceNode) caseObj;
                 if (!(foundIndex instanceof Long)) {
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                 // Arguments of the slice are the parameters for it, not elements!
                 QDLStem qqq = (QDLStem) closedSliceNode.evaluate(state);
                 if(!qqq.containsKey(foundIndex)){
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                  result = qqq.get(foundIndex);
                 setResult(result);
                 setResultType(Constant.getType(result));
                 setEvaluated(true);
                 return result;

             default:
                 // Might be the case they sent along a polyad whose result is a stem.
                 // try that
                 Object ooo = getCASE().evaluate(state);
                 if(!(ooo instanceof QDLStem)) {
                     throw new BadArgException("scalars are not  supported as case types", getCASE());
                 }
                 QDLStem qdlStem = (QDLStem) ooo;
                 result = qdlStem.get(foundIndex);
                 if(result == null){
                     throw new BadArgException("index '" + foundIndex + "' not found", getCASE());
                 }
                 setResult(result);
                 setResultType(Constant.getType(result));
                 setEvaluated(true);
                 return result;


         }
    }
    @Override
        public int getNodeType() {
            return SELECT_NODE;
        }
}
