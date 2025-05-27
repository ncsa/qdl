package org.qdl_lang.variables.values;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;

public class DyadicFunctionReferenceValue extends QDLValue{
    public DyadicFunctionReferenceValue(DyadicFunctionReferenceNode value) {
        super(value);
        type = DYADIC_FUNCTION_TYPE;
    }
}
