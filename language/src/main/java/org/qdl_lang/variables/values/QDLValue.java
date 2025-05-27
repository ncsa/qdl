package org.qdl_lang.variables.values;

import org.qdl_lang.expressions.AxisExpression;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.variables.*;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The top-level wrapper class for every value QDL knows about.
 */
public class QDLValue implements Constants {
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        type = Constant.getType(value);
    }

    private Object value;

    public boolean hasValue() {
        return value != null;
    }

    public int getType() {
        if (type == null) {
            type = Constant.getType(getValue());
        }
        return type;
    }

    Integer type = null;

    public QDLValue(Object value) {
        this.value = value;
    }

    /*
      Type checking
     */
    public boolean isAxisRestriction() {
        return getType() == AXIS_RESTRICTION_TYPE;
    }

    public boolean isBoolean() {
        return getType() == BOOLEAN_TYPE;
    }

    public boolean isDecimal() {
        return getType() == DECIMAL_TYPE;
    }

    public boolean isDyadicFunction() {
        return getType() == DYADIC_FUNCTION_TYPE;
    }

    public boolean isFunction() {
        return getType() == FUNCTION_TYPE;
    }

    public boolean isList() {
        return getType() == LIST_TYPE;
    }

    public boolean isListOrStem() {
        return getType() == STEM_TYPE || getType() == LIST_TYPE;
    }

    public boolean isLong() {
        return getType() == LONG_TYPE;
    }

    public boolean isModule() {
        return getType() == MODULE_TYPE;
    }

    public boolean isNull() {
        return getType() == NULL_TYPE;
    }

    public boolean isSet() {
        return getType() == SET_TYPE;
    }

    public boolean isStem() {
        return getType() == STEM_TYPE;
    }

    public boolean isString() {
        return getType() == STRING_TYPE;
    }

    /*
        Casts the value to the given type.
     */
    public Boolean asBoolean() {
        return (Boolean) getValue();
    }

    public BigDecimal asDecimal() {
        return (BigDecimal) getValue();
    }

    public QDLStem asStem() {
        return (QDLStem) getValue();
    }

    public String asString() {
        return (String) getValue();
    }

    public QDLNull asNull() {
        return (QDLNull) getValue();
    }

    public QDLSet asSet() {
        return (QDLSet) getValue();
    }

    public org.qdl_lang.module.Module asModule() {
        return (org.qdl_lang.module.Module) getValue();
    }

    public Long asLong() {
        return (Long) getValue();
    }

    public FunctionReferenceNode asFunction() {
        return (FunctionReferenceNode) getValue();
    }

    public DyadicFunctionReferenceNode asDyadicFunction() {
        return (DyadicFunctionReferenceNode) getValue();
    }
    public AxisExpression asAxisExpression() {
        return (AxisExpression) getValue();
    }

    @Override
    public String toString() {
        if (!hasValue()) {
            return "(?)";
        }
        return getValue().toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        QDLValue qdlValue = (QDLValue) object;
        return type == qdlValue.type && Objects.equals(value, qdlValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    public String getInputForm() {
        return InputFormUtil.inputForm(getValue());
    }

    static QDLNullValue qdlNullValue = new QDLNullValue();

    public static QDLNullValue getNullValue() {
        return qdlNullValue;
    }

    /**
     * Factory method to convert an object to a {@link QDLValue} if it is not one.
     *
     * @param value
     * @return
     */
    public static QDLValue asQDLValue(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("null value encountered");
        }
        if (value instanceof QDLValue) {
            return (QDLValue) value;
        }
        switch (Constant.getType(value)) {
            case AXIS_RESTRICTION_TYPE:
                return new AxisRestrictionValue((AxisExpression) value);
            case BOOLEAN_TYPE:
                return new BooleanValue((Boolean) value);
            case DECIMAL_TYPE:
                return new DecimalValue((BigDecimal) value);
            case DYADIC_FUNCTION_TYPE:
                return new DyadicFunctionReferenceValue((DyadicFunctionReferenceNode) value);
            case FUNCTION_TYPE:
                return new FunctionReferenceValue((FunctionReferenceNode) value);
            case LIST_TYPE:
                return new StemValue((QDLList) value);
            case STEM_TYPE:
                return new StemValue((QDLStem) value);
            case STRING_TYPE:
                return new StringValue((String) value);
            case NULL_TYPE:
                return getNullValue();
            case SET_TYPE:
                return new SetValue((QDLSet) value);
            case LONG_TYPE:
                return new LongValue((Long) value);
            case MODULE_TYPE:
                return new ModuleValue((Module) value);
            case Constants.UNKNOWN_TYPE:
                throw new IllegalArgumentException(" unknown object type for '" + value + "'");
            default:
                return new QDLValue(value);
        }
    }

    /**
     * effectively the inverse of {@link #asQDLValue(Object)}. If this is {@link QDLValue} it will
     * unwrap the value, otherwise it just returns the argument.
     *
     * @param value
     * @return
     */
    public static Object asJavaValue(Object value) {
        if (value instanceof QDLValue) {
            return ((QDLValue) value).getValue();
        }
        return value;
    }
}
