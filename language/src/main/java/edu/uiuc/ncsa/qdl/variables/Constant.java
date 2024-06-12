package edu.uiuc.ncsa.qdl.variables;

import edu.uiuc.ncsa.qdl.functions.DyadicFunctionReferenceNode;
import edu.uiuc.ncsa.qdl.functions.FunctionReferenceNode;
import edu.uiuc.ncsa.qdl.module.Module;

import java.math.BigDecimal;

/**
 * Utility class to manage constants for the system. This is the value recorded at parse time
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:07 PM
 */
public class Constant {
    public static int getType(Object object){

        if(object instanceof QDLNull) return NULL_TYPE;
        if(object instanceof String) return STRING_TYPE;
        if(object instanceof Long) return LONG_TYPE;
        if(object instanceof Boolean) return BOOLEAN_TYPE;
        if(object instanceof QDLStem) return STEM_TYPE;
        if(object instanceof BigDecimal) return DECIMAL_TYPE;
        if(object instanceof QDLSet) return SET_TYPE;
        if(object instanceof Module) return MODULE_TYPE;
        if(object instanceof FunctionReferenceNode) return FUNCTION_TYPE;
        if(object instanceof DyadicFunctionReferenceNode) return DYADIC_FUNCTION_TYPE;
        return UNKNOWN_TYPE;
    }
    public static final int UNKNOWN_TYPE = -1;
    public static final int NULL_TYPE = 0;
    public static final int BOOLEAN_TYPE = 1;
    public static final int LONG_TYPE = 2;
    public static final int STRING_TYPE = 3;
    public static final int STEM_TYPE = 4; // these are mixed type
    public static final int DECIMAL_TYPE = 5;
    public static final int FUNCTION_TYPE = 6;
    public static final int DYADIC_FUNCTION_TYPE = 11;
    public static final int SET_TYPE = 10;
    public static final int MODULE_TYPE = 7;
    Object value;
    int type = UNKNOWN_TYPE;

    public static boolean isString(Object key) {
        return key instanceof String;
    }

    public static boolean isModule(Object key){return key instanceof Module;}

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
    public Long getLong(){
        if(value instanceof Long) return (Long)value;
        return 0L;
    }
    public static boolean isNull(Object obj){
        return obj instanceof QDLNull;
    }
    public static boolean isScalar(Object obj){
         int type = getType(obj);
         return type != STEM_TYPE && type != SET_TYPE && type != FUNCTION_TYPE && type != UNKNOWN_TYPE;
    }
    public static boolean isStem(Object obj){
        return getType(obj) == STEM_TYPE ;
    }
    public static boolean isSet(Object obj){
        return getType(obj) == SET_TYPE;
    }
}
