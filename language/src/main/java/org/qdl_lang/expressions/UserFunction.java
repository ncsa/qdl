package org.qdl_lang.expressions;

import org.qdl_lang.functions.FR_WithState;
import org.qdl_lang.functions.FunctionRecordInterface;
import org.qdl_lang.statements.TokenPosition;

/**
 * MOdels a user-defined function in QDL. This is a specialized type of {@link Polyad}.
 */
public class UserFunction extends Polyad {
    public UserFunction() {
    }

    public UserFunction(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    public UserFunction(String name) {
        super(name);
    }

    public UserFunction(int operatorType) {
        super(operatorType);
    }

    public FunctionRecordInterface getFunctionRecord() {
        return functionRecord;
    }

    public void setFunctionRecord(FunctionRecordInterface functionRecord) {
        this.functionRecord = functionRecord;
    }

    FunctionRecordInterface functionRecord;
    Boolean hasFRWithState = null;

    /**
     * Check that it has a function record before calling this.
     * @return
     */
    public boolean hasFR_WithState() {
        if (hasFRWithState == null) {
            hasFRWithState = functionRecord instanceof FR_WithState;
        }
        return hasFRWithState;
    }

    public boolean hasFunctionRecord() {
        return functionRecord != null;
    }

    public int arity() {
        if (hasFunctionRecord()) {
            return getFunctionRecord().getArgCount();
        }
        return -1;
    }
}
