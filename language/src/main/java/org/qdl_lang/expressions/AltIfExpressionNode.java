package org.qdl_lang.expressions;

import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/27/21 at  5:13 PM
 */
public class AltIfExpressionNode extends ExpressionImpl {
    public ExpressionInterface getIF() {
        return getArguments().get(0);
    }

    public void setIF(ExpressionInterface x) {
        getArguments().set(0, x);
    }

    public ExpressionInterface getTHEN() {
        return getArguments().get(1);
    }

    public void setTHEN(ExpressionInterface x) {
        getArguments().set(1, x);
    }

    public ExpressionInterface getELSE() {
        return getArguments().get(2);
    }

    public void setELSE(ExpressionInterface x) {
        getArguments().set(2, x);
    }

    @Override
    public ArrayList<ExpressionInterface> getArguments() {
        if (arguments == null || arguments.size()!= 3) {
            arguments = new ArrayList<>(3);
            arguments.add(null);
            arguments.add(null);
            arguments.add(null);
        }
        return arguments;
    }

    @Override
    public QDLValue evaluate(State state) {
        QDLValue arg0 = getIF().evaluate(state);
        if(arg0.isStem()){
            QDLStem out = new QDLStem();
            QDLStem inStem = arg0.asStem();
            for(Object key : inStem.keySet()){
                   Object obj = inStem.get(key);
                   if(!(obj instanceof Boolean)){
                       throw new QDLExceptionWithTrace("expression requires a boolean at index '" + key + "', got '" + obj + "'", getIF());
                   }
                Boolean flag = (Boolean) obj;
                Object arg1;
                if (flag) {
                    arg1 = getTHEN().evaluate(state);
                } else {
                    arg1 = getELSE().evaluate(state);
                }
                out.putLongOrString(key, asQDLValue(arg1));
            }
            setResult(new QDLValue(out));
            setEvaluated(true);
            return getResult();

        }
        if (!(arg0.isBoolean())) {
            throw new QDLExceptionWithTrace("error: expression requires a boolean as its first argument, got '" + arg0.getValue() + "'", getIF());
        }
        Boolean flag = arg0.asBoolean();
        Object arg1;
        if (flag) {
            arg1 = getTHEN().evaluate(state);
        } else {
            arg1 = getELSE().evaluate(state);
        }
        setResult(new QDLValue(arg1));
        setEvaluated(true);
        return getResult();
    }

    @Override
    public ExpressionInterface makeCopy() {
        return null;
    }
    @Override
        public int getNodeType() {
            return ALT_IF_NODE;
        }
}
