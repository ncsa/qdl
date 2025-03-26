package org.qdl_lang.expressions;

import org.qdl_lang.functions.FunctionRecordInterface;

/**
 * MOdels a user-defined function in QDL. This is a specialized type of {@link Polyad}.
 */
public class UserFunction extends Polyad{
    public FunctionRecordInterface getFunctionRecord() {
        return functionRecord;
    }

    public void setFunctionRecord(FunctionRecordInterface functionRecord) {
        this.functionRecord = functionRecord;
    }

    FunctionRecordInterface functionRecord;

    public boolean hasFunctionRecord() {
        return functionRecord != null;
    }
    public int arity() {
        if(hasFunctionRecord()) {
            return getFunctionRecord().getArgCount();
        }
        return -1;
    }
}
