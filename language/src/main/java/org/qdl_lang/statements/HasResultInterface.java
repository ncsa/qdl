package org.qdl_lang.statements;

import org.qdl_lang.variables.values.QDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/11/20 at  6:33 AM
 */
public interface HasResultInterface {
    QDLValue getResult();

    void setResult(QDLValue result);

    /**
     * Convenience method.This should turn the object into a proper {@link QDLValue)} as needed.
     * @param result
     */
    void setResult(Object result);

    int getResultType();

    //void setResultType(int type);

    boolean isEvaluated();

    void setEvaluated(boolean evaluated);
}
