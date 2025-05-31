package org.qdl_lang.expressions;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.QDLValue;

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
    public QDLValue evaluate(State state) {

        QDLValue obj = getSWITCH().evaluate(state);
        QDLStem stem = null;

        if ((obj.isStem())) {
            stem = obj.asStem();
        } else {
            if (obj.isBoolean()) {
                Object result;
                if (obj.asBoolean()  ) {
                    result = getCASE().evaluate(state);
                } else {
                    result = getDEFAULT().evaluate(state);
                }
                setResult(result);
                setEvaluated(true);
                return getResult();
            }
            throw new BadArgException("left hand argument must be a boolean if its a scalar", getSWITCH());
        }

        Object foundIndex = null;
        for (Object k : stem.keySet()) {
            QDLValue value = stem.get(k);
            if (!(value.isBoolean())) {
                throw new BadArgException("left hand argument at index '" + k + "' is not a boolean", getSWITCH());
            }
            Boolean b = value.asBoolean();
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
            setEvaluated(true);
            return getResult();
        }
        ExpressionInterface caseObj = getCASE();
        QDLValue result;
        // The next cases are to try and pick apart switch statements so we don't run the
        // risk of evaluating things that are undefined. Fallthrough case is we just
        // can't figure it out, so try it directly.
         switch (caseObj.getNodeType()){
             case ExpressionInterface.VARIABLE_NODE:
                 result = caseObj.evaluate(state);
                 if(result.isStem()){
                       QDLStem qdlStem = result.asStem();
                       QDLValue rr = qdlStem.get(foundIndex);
                       if(rr == null){
                           throw new BadArgException("no such index " + foundIndex + " exists in this list.", getCASE());
                       }
                       setResult(rr);;
                       setEvaluated(true);
                       return getResult();
                 }else{
                     throw new BadArgException("scalar value for case not supported", getCASE());
                 }

             case ExpressionInterface.LIST_NODE:
                 StemListNode stemListNode = (StemListNode) caseObj;
                 if (!(foundIndex instanceof Long)) {
                     throw new BadArgException("no such index " + foundIndex + " exists in this list.", getCASE());
                 }
                 QDLValue r = stemListNode.getStatements().get(((Long) foundIndex).intValue()).evaluate(state);
                 setResult(r);
                 setEvaluated(true);
                 return getResult();

             case ExpressionInterface.STEM_NODE:
                 StemVariableNode stemVariableNode = (StemVariableNode) caseObj;
                 for (StemEntryNode stemEntryNode : stemVariableNode.getStatements()) {
                     QDLValue key = stemEntryNode.getKey().evaluate(state);
                     if (key.equals(foundIndex)) {
                          result = stemEntryNode.getValue().evaluate(state);
                         setResult(result);
                         setEvaluated(true);
                         return getResult();
                     }
                 }

             case ExpressionInterface.OPEN_SLICE_NODE:
                 OpenSliceNode openSliceNode = (OpenSliceNode) caseObj;
                 if (!(foundIndex instanceof Long)) {
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                 // Arguments of the slice are the parameters for it, not elements!
                 QDLStem qq = openSliceNode.evaluate(state).asStem();
                 if(!qq.containsKey(foundIndex)){
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                  result = qq.get(foundIndex);
                 setResult(result);
                 setEvaluated(true);
                 return getResult();

             case ExpressionInterface.CLOSED_SLICE_NODE:
                 ClosedSliceNode closedSliceNode = (ClosedSliceNode) caseObj;
                 if (!(foundIndex instanceof Long)) {
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                 // Arguments of the slice are the parameters for it, not elements!
                 QDLStem qqq = closedSliceNode.evaluate(state).asStem();
                 if(!qqq.containsKey(foundIndex)){
                     throw new BadArgException("index " + foundIndex + " not found", getCASE());
                 }
                  result = qqq.get(foundIndex);
                 setResult(result);
                 setEvaluated(true);
                 return getResult();

             default:
                 // Might be the case they sent along a polyad whose result is a stem.
                 // try that
                 QDLValue ooo = getCASE().evaluate(state);
                 if(!(ooo.isStem())) {
                     throw new BadArgException("scalars are not  supported as case values", getCASE());
                 }
                 QDLStem qdlStem = ooo.asStem();
                 result = qdlStem.get(foundIndex);
                 if(result == null){
                     throw new BadArgException("index '" + foundIndex + "' not found", getCASE());
                 }
                 setResult(result);
                 setEvaluated(true);
                 return getResult();
         }
    }
    @Override
        public int getNodeType() {
            return SELECT_NODE;
        }
}
