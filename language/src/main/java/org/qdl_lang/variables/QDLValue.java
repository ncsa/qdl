package org.qdl_lang.variables;

public class QDLValue {
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        type = Constant.getType(value);
    }

    private Object value;

    public int getType() {
        return type;
    }

    int type;

    public boolean isAxisRestriction() {return getType() == Constant.AXIS_RESTRICTION_TYPE;}
    public boolean isBoolean() {return getType() == Constant.BOOLEAN_TYPE;}
    public boolean isDecimal() {return getType() == Constant.DECIMAL_TYPE;}
    public boolean isDyadicFunction() {return getType() == Constant.DYADIC_FUNCTION_TYPE;}
    public boolean isFunction() {return getType() == Constant.FUNCTION_TYPE;}
    public boolean isList() {return getType() == Constant.LIST_TYPE;}
    public boolean isListOrStem() {return getType() == Constant.STEM_TYPE || getType() == Constant.LIST_TYPE;}
    public boolean isLong() {return getType() == Constant.LONG_TYPE;}
    public boolean isModule() {return getType() == Constant.MODULE_TYPE;}
    public boolean isNull() {return getType() == Constant.NULL_TYPE;}
    public boolean isSet() {return getType() == Constant.SET_TYPE;}
    public boolean isStem() {return getType() == Constant.STEM_TYPE;}
    public boolean isString() {return getType() == Constant.STRING_TYPE;}
}
