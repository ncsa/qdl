package org.qdl_lang.variables.values;

import org.qdl_lang.functions.FunctionReferenceNode;

public class FunctionReferenceValue extends QDLValue {
    public FunctionReferenceValue(FunctionReferenceNode value) {
        super(value);
        type = FUNCTION_TYPE;
    }
}
