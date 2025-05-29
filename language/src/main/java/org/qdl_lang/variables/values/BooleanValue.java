package org.qdl_lang.variables.values;

public class BooleanValue extends QDLValue {
    public BooleanValue(Boolean value) {
        super(value);
        type = BOOLEAN_TYPE;
    }
    public static final BooleanValue True = new BooleanValue(true);
    public static final BooleanValue False = new BooleanValue(false);
}
