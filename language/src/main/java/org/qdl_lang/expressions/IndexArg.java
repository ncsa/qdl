package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.ListEvaluator;
import org.qdl_lang.functions.FunctionReferenceNodeInterface;
import org.qdl_lang.functions.LambdaDefinitionNode;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/1/22 at  4:51 PM
 */
public class IndexArg implements Serializable {
    public boolean interpretListArg = false;

    public IndexArg() {
    }

    Boolean function = null;
    Boolean functionDefinition = null;

    protected boolean isFunctionDefinition() {
        if (functionDefinition == null) {
            isFunction(); // computes if it is a definition too
        }
        return functionDefinition;
    }

    protected boolean isFunction() {
        ExpressionInterface exi = swri;
        if (function == null) {
            while (exi instanceof ParenthesizedExpression) {
                exi = ((ParenthesizedExpression) exi).getExpression();
            }
            function = exi instanceof FunctionReferenceNodeInterface;
            functionDefinition = exi instanceof LambdaDefinitionNode;
        }
        return function;
    }

    protected FunctionReferenceNodeInterface getFunction() {
        ExpressionInterface exi = swri;
        while (exi instanceof ParenthesizedExpression) {
            exi = ((ParenthesizedExpression) exi).getExpression();
        }
        return (FunctionReferenceNodeInterface) exi;
    }
    protected LambdaDefinitionNode getFunctionDefinition() {
        ExpressionInterface exi = swri;
        while (exi instanceof ParenthesizedExpression) {
            exi = ((ParenthesizedExpression) exi).getExpression();
        }
        return (LambdaDefinitionNode) exi;
    }

    /*
    a.≔n(2,3,4,[1;1+2*3*4]);
    k(v)→v<2;
    a\*\1@k\[1,3]

    a.≔n(2,3,4,[;1+2*3*4]);
k(v)→v<2;
f(k,v)→k+v<5;
a\*\(2@f)\[1,3]
     */
    public Collection createKeySet(QDLStem in, State state) {
        if ((in != null) && isWildcard()) {
            return in.keySet();
        }
        List<QDLValue> stemKeys = new ArrayList<>();
        if (isFunction() || isFunctionDefinition()) {
            Polyad pick = new Polyad(ListEvaluator.PICK);
            if(isFunctionDefinition()) {
                pick.addArgument(getFunctionDefinition()); // Send along lambda
            }else{
                pick.addArgument((ExpressionImpl) getFunction()); // trick. This is either a dyadic FR or a FR. Both extend this
            }

            pick.addArgument(new ConstantNode(asQDLValue(in)));
            pick.evaluate(state);
            QDLValue keys = pick.getResult();
            swri.setResult(keys);
            swri.setEvaluated(true);
            if (keys.isStem()) {
                for (Object key : keys.asStem().keySet()) {
                    stemKeys.add(asQDLValue(key));
                }
            }
            return stemKeys;
        }
        QDLValue obj = swri.getResult();

        if (obj.isScalar()) {
            stemKeys.add(obj);
        }
        if (obj.isStem()) {
            // NOTE that the stem is contractually a list of indices. Take the values
            QDLStem stem = obj.asStem();
            for (Object key : stem.keySet()) {
                stemKeys.add(stem.get(key));
            }
            return stemKeys;
        }

        if (obj.isSet()) {
            stemKeys.addAll(obj.asSet());
        }

        return stemKeys;

    }


    public IndexArg(ExpressionInterface swri, boolean strictOrder) {
        this.swri = swri;
        this.strictOrder = strictOrder;
    }

    public boolean isWildcard() {
        return swri instanceof AllIndices;
    }

    public ExpressionInterface swri;
    public boolean strictOrder = false;

    @Override
    public String toString() {
        return "IndexArg{" +
                "swri='" + swri + "(evaluated? " + swri.isEvaluated() + ")?" +
                "', strictOrder=" + strictOrder +
                ", isList=" + interpretListArg +
                '}';
    }
}
