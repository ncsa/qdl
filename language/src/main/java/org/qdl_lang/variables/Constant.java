package org.qdl_lang.variables;

import org.qdl_lang.expressions.AxisExpression;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;

/**
 * Utility class to manage constants for the system. This is the value recorded at parse time
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:07 PM
 */
public class Constant implements Constants {
    public static int getType(Object object) {
        if(object instanceof QDLValue) {return ((QDLValue)object).getType();}
        if (object instanceof QDLNull) return NULL_TYPE;
        if (object instanceof String) return STRING_TYPE;
        if (object instanceof Long) return LONG_TYPE;
        if (object instanceof Integer) return INTEGER_TYPE; // used only to help create QDLValues
        if (object instanceof Boolean) return BOOLEAN_TYPE;
        if (object instanceof QDLStem) {
            // Next line, while right, breaks stuff (every place there is
            // a check if the type is stem, it fails), so eventually use it
            //if(((QDLStem)object).isList()) return LIST_TYPE;
            return STEM_TYPE;
        };
        if (object instanceof BigDecimal) return DECIMAL_TYPE;
        if (object instanceof QDLSet) return SET_TYPE;
        if (object instanceof Module) return MODULE_TYPE;
        if (object instanceof FunctionReferenceNode) return FUNCTION_TYPE;
        if (object instanceof DyadicFunctionReferenceNode) return DYADIC_FUNCTION_TYPE;
        if (object instanceof AxisExpression) return AXIS_RESTRICTION_TYPE;
        return UNKNOWN_TYPE;
    }

    /**
     * Given the (integer) type, return the string name. This is used mostly for error or
     * diagnostic messsages.
     *
     * @param type
     * @return
     */
    public static String getName(int type) {
        switch (type) {
            case STRING_TYPE:
                return STRING_NAME;
            case LONG_TYPE:
                return LONG_NAME;
            case BOOLEAN_TYPE:
                return BOOLEAN_NAME;
            case DECIMAL_TYPE:
                return DECIMAL_NAME;
            case SET_TYPE:
                return SET_NAME;
            case MODULE_TYPE:
                return MODULE_NAME;
            case FUNCTION_TYPE:
                return FUNCTION_NAME;
            case DYADIC_FUNCTION_TYPE:
                return DYADIC_FUNCTION_NAME;
            case AXIS_RESTRICTION_TYPE:
                return AXIS_RESTRICTION_NAME;
            case NULL_TYPE:
                return NULL_NAME;
            default:
                return UNKNOWN_NAME;
        }
    }



    Object value;
    int type = UNKNOWN_TYPE;

    public static boolean isString(Object key) {
        return key instanceof String;
    }

    public static boolean isModule(Object key) {
        return key instanceof Module;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getString() {
        if (value == null) return null;
        return value.toString();
    }

    public Long getLong() {
        if (value instanceof Long) return (Long) value;
        return 0L;
    }

    public static boolean isNull(Object obj) {
        return obj instanceof QDLNull;
    }

    public static boolean isScalar(Object obj) {
        int type = getType(obj);
        return type != STEM_TYPE && type != SET_TYPE && type != FUNCTION_TYPE && type != UNKNOWN_TYPE;
    }

    public static boolean isStem(Object obj) {
        return getType(obj) == STEM_TYPE;
    }

    public static boolean isSet(Object obj) {
        return getType(obj) == SET_TYPE;
    }
}
