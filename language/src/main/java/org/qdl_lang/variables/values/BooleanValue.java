package org.qdl_lang.variables.values;

public class BooleanValue extends QDLValue {
    public BooleanValue(Boolean value) {
        super(value);
        type = BOOLEAN_TYPE;
    }
}
