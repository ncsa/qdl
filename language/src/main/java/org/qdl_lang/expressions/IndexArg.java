package org.qdl_lang.expressions;

import org.checkerframework.checker.units.qual.C;
import org.qdl_lang.evaluate.AbstractEvaluator;
import org.qdl_lang.evaluate.ListEvaluator;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNodeInterface;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;
import software.amazon.awssdk.services.medialive.model.EpochLockingSettings;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.qdl_lang.evaluate.AbstractEvaluator.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/1/22 at  4:51 PM
 */
public class IndexArg implements Serializable {
    public boolean interpretListArg = false;

    public IndexArg() {
    }

    protected boolean isFunction() {
        return swri instanceof FunctionReferenceNodeInterface;
    }

    protected FunctionReferenceNodeInterface getFunction() {
        return (FunctionReferenceNodeInterface) swri;
    }

    /*
    a.≔n(2,3,4,[1;1+2*3*4]);
    k(v)→v<2;
    a\*\1@k\[1,3]
     */
    public Collection createKeySet(QDLStem in, State state) {
        if ((in != null) && isWildcard()) {
            return in.keySet();
        }
        List stemKeys = new ArrayList();
        if (isFunction()) {
            Polyad pick = new Polyad(ListEvaluator.PICK);
            pick.addArgument((ExpressionImpl)getFunction()); // trick. This is either a dyadic FR or a FR. Both extend this
            QDLStem ndx = new QDLStem();
            ndx.getQDLList().appendAll(Arrays.asList(in.keySet().toArray()));
            pick.addArgument(new ConstantNode(ndx));
            pick.evaluate(state);
            Object keys = pick.getResult();
            swri.setResult(keys);
            if(keys instanceof QDLStem) {
                for(Object key : ((QDLStem)keys).keySet()) {
                    stemKeys.add(key);
                }
            }

/*
            FunctionReferenceNodeInterface frn = getFunction();
            ExpressionImpl f = getOperator(state, frn, 1);
            ArrayList<Object> rawArgs = new ArrayList<>();

            for (Object key : in.keySet()) {
                rawArgs.clear();
                rawArgs.add(key);
                f.setArguments(toConstants(rawArgs));
                Object test = f.evaluate(state);
                if (isBoolean(test)) {
                    if ((Boolean) test) {
                        stemKeys.add(key);
                    }
                }
            }
*/
            return stemKeys;
        }
        Object obj = swri.getResult();

        if (Constant.isScalar(obj)) {
            stemKeys.add(obj);
        }
        if (Constant.isStem(obj)) {
            // NOTE that the stem is contractually a list of indices. Take the values
            QDLStem stem = (QDLStem) obj;
            for (Object key : stem.keySet()) {
                stemKeys.add(stem.get(key));
            }
            return stemKeys;
        }

        if (Constant.isSet(obj)) {
            stemKeys.addAll(((QDLSet) obj));
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
