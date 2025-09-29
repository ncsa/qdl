package org.qdl_lang.variables.values;

import org.qdl_lang.exceptions.WrongValueException;
import org.qdl_lang.expressions.AxisExpression;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.variables.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
 * The top-level wrapper class for every value QDL knows about.
 */
public class QDLValue implements Constants, Serializable, Comparable<QDLValue> {
    protected Long longValue = null;
    protected Boolean booleanValue = null;
    protected BigDecimal decimalValue = null;
    protected String stringValue = null;
    protected Module moduleValue = null;
    protected FunctionReferenceNode functionValue = null;
    protected DyadicFunctionReferenceNode dyadicFunctionValue = null;
    protected AxisExpression axisValue = null;
    protected QDLSet setValue = null;
    protected QDLStem stemValue = null;
    protected QDLNull nullValue = null;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        type = Constant.getType(value);
        switch (type) {
            case AXIS_RESTRICTION_TYPE:
                axisValue = (AxisExpression) value;
                break;
            case BOOLEAN_TYPE:
                booleanValue = (Boolean) value;
                break;
            case DECIMAL_TYPE:
                decimalValue = (BigDecimal) value;
                break;
            case STRING_TYPE:
                stringValue = (String) value;
                break;
            case LONG_TYPE:
                longValue = (Long) value;
                break;
            case MODULE_TYPE:
                moduleValue = (Module) value;
                break;
            case FUNCTION_TYPE:
                functionValue = (FunctionReferenceNode) value;
                break;
            case DYADIC_FUNCTION_TYPE:
                dyadicFunctionValue = (DyadicFunctionReferenceNode) value;
                break;
            case SET_TYPE:
                setValue = (QDLSet) value;
                break;
            case STEM_TYPE:
                stemValue = (QDLStem) value;
                break;
            default:
        //        this.value = value;
        }
        this.value = value;

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
        if(value instanceof QDLValue) {
            // just in case someone tries to nest these, unpack it for them
            this.value = ((QDLValue) value).getValue();
        }else {
            this.value = value;
        }
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
    public boolean isAllIndices() {
        return getType() == ALL_INDICES_TYPE;
    }

    /*
        Casts the value to the given type.
     */
    public Boolean asBoolean() {
        if (getType() != BOOLEAN_TYPE) {
            throw new WrongValueException("expected a boolean, but got a " + Constant.getName(getType()), null);
        }
        if (booleanValue == null) {
            booleanValue = (Boolean) getValue();
        }
        return booleanValue;
    }

    public BigDecimal asDecimal() {
        if (getType() != DECIMAL_TYPE) {
            throw new WrongValueException("expected a decimal, but got a " + Constant.getName(getType()), null);
        }
        if (decimalValue == null) {

            decimalValue = (BigDecimal) getValue();
        }
        return decimalValue;
    }

    public QDLStem asStem() {
        if (getType() != STEM_TYPE) {
            throw new WrongValueException("expected a stem, but got a " + Constant.getName(getType()), null);
        }
        if (stemValue == null) {
            stemValue = (QDLStem) getValue();
        }
        return stemValue;
    }

    public String asString() {
        if (getType() != STRING_TYPE) {
            throw new WrongValueException("expected a string, but got a " + Constant.getName(getType()), null);
        }
        if (stringValue == null) {
            stringValue = (String) getValue();
        }
        return stringValue;
    }

    public QDLNull asNull() {
        if (getType() != NULL_TYPE) {
            throw new WrongValueException("expected a null, but got a " + Constant.getName(getType()), null);
        }
        if (nullValue == null) {
            nullValue = (QDLNull) getValue();
        }
        return nullValue;
    }

    public QDLSet asSet() {
        if (getType() != SET_TYPE) {
            throw new WrongValueException("expected a set, but got a " + Constant.getName(getType()), null);
        }
        if (setValue == null) {
            setValue = (QDLSet) getValue();
        }
        return setValue;
    }

    public Module asModule() {
        if (getType() != MODULE_TYPE) {
            throw new WrongValueException("expected a module, but got a " + Constant.getName(getType()), null);
        }
        if (moduleValue == null) {
            moduleValue = (Module) getValue();
        }
        return moduleValue;
    }

    public Long asLong() {
        if (getType() != LONG_TYPE) {
            throw new WrongValueException("expected an integer, but got a " + Constant.getName(getType()), null);
        }
        if (longValue == null) {
            longValue = (Long) getValue();
        }
        return longValue;
    }

    public FunctionReferenceNode asFunction() {
        if (getType() != FUNCTION_TYPE) {
            throw new WrongValueException("expected an function reference, but got a " + Constant.getName(getType()), null);
        }
        if (functionValue == null) {
            functionValue = (FunctionReferenceNode) getValue();
        }
        return functionValue;
    }

    public DyadicFunctionReferenceNode asDyadicFunction() {
        if (getType() != DYADIC_FUNCTION_TYPE) {
            throw new WrongValueException("expected a dyadic function, but got a " + Constant.getName(getType()), null);
        }
        if (dyadicFunctionValue == null) {
            dyadicFunctionValue = (DyadicFunctionReferenceNode) getValue();
        }
        return dyadicFunctionValue;
    }

    public AxisExpression asAxisExpression() {
        if (getType() != AXIS_RESTRICTION_TYPE) {
            throw new WrongValueException("expected an axis restriction, but got a " + Constant.getName(getType()), null);
        }
        if (axisValue == null) {
            axisValue = (AxisExpression) getValue();
        }
        return axisValue;
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
        Object targetValue = object;
        if(object instanceof QDLValue) {
            QDLValue other = (QDLValue) object;
            if(getType() != other.getType()) {
                return false;
            }
            targetValue = other.getValue();
        }
      //  if (object == null || getClass() != object.getClass()) return false;
        return  Objects.equals(value, targetValue);
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
            case INTEGER_TYPE:
                return new LongValue((Integer)value);
            case MODULE_TYPE:
                return new ModuleValue((Module) value);
            case ALL_INDICES_TYPE:
               return AllIndicesValue.getAllIndicesValue();
            case Constants.UNKNOWN_TYPE:
                throw new IllegalArgumentException(" unknown object type for '" + value + "' " + value.getClass().getName());
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

    /**
     * Utility to convert a collection of {@link QDLValue}s to their Java objects.
     *
     * @param values
     * @return
     */
    public static List<Object> castToJavaValues(List<QDLValue> values) {
        ArrayList<Object> list = new ArrayList<>(values.size());
        for (QDLValue value : values) {
            list.add(asJavaValue(value));
        }
        return list;
    }
    public static Object[] castToJavaValues(Object[] values) {
        Object[] array = new Object[values.length];
        int i = 0;
        for (Object value : values) {
            array[i++] = (asJavaValue(value));
        }
        return array;
    }

    /**
     * Inverse cast of {@link #castToJavaValues(List)}. This takes a collection of java values
     * and turns it into a list of {@link QDLValue}s
     *
     * @param values
     * @return
     */
    public static List<QDLValue> castToQDLValues(List<Object> values) {
        ArrayList<QDLValue> list = new ArrayList<>(values.size());
        for (Object value : values) {
            list.add(asQDLValue(value));
        }
        return list;
    }

    public static Collection<QDLValue> castToQDLValues(Collection<Object> values) {
        if(values instanceof Set) return castToQDLValues((Set) values);
        if(values instanceof List) return castToQDLValues((List) values);
        throw new WrongValueException("expected a set or list, but got a " + values.getClass().getSimpleName(), null);
    }
    public static Set<QDLValue> castToQDLValues(Set<Object> values) {
        HashSet<QDLValue> set = new HashSet<>(values.size());
        for (Object value : values) {
            set.add(asQDLValue(value));
        }
        return set;
    }

    public static List<QDLValue> castToQDLValueList(Object[] values) {
        QDLValue[] array = castToQDLValues(values);
        return Arrays.asList(array);
    }
    public static QDLValue[] castToQDLValues(Object[] values) {
        QDLValue list[] = new QDLValue[values.length];
        int i = 0;
        for (Object value : values) {
            list[i++] = (asQDLValue(value));
        }
        return list;
    }

    @Override
    public int compareTo(QDLValue qdlValue) {
        Comparable A = null, B = null;
        if(getValue() instanceof Comparable) {
            A = (Comparable) getValue();
        }else{
            throw new ClassCastException("value is not comparable");
        }
        if(qdlValue.getValue() instanceof Comparable) {
            B = (Comparable) qdlValue.getValue();
        }else{
            throw new ClassCastException("value is not comparable");

        }
        return A.compareTo(B);
    }

    public boolean isScalar(){
        return !(isStem() || isSet() || isModule() || isFunction() || isDyadicFunction());
    }
}
