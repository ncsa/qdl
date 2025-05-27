package org.qdl_lang.variables;

public interface Constants {
    int AXIS_RESTRICTION_TYPE = 12;
    int BOOLEAN_TYPE = 1;
    int DECIMAL_TYPE = 5;
    int DYADIC_FUNCTION_TYPE = 11;
    int FUNCTION_TYPE = 6;
    int LIST_TYPE = 14;
    int LONG_TYPE = 2;
    int MODULE_TYPE = 7;
    int NULL_TYPE = 0;
    int SET_TYPE = 10;
    int STEM_TYPE = 4; // these are mixed type
    int STRING_TYPE = 3;
    int UNKNOWN_TYPE = -1;
    /**
     * Every system function can be interrogated for its arg count. Since we are using QDLValues,
     * we have a special internal type for it. This is only used by the system in a very specific context.
     */
    int ARG_COUNT_TYPE = -100;


    String UNKNOWN_NAME = "unknown";
    String NULL_NAME = "null";
    String BOOLEAN_NAME = "boolean";
    String LIST_NAME = "list";
    String LONG_NAME = "integer";
    String STRING_NAME = "string";
    String STEM_NAME = "stem"; // these are mixed type
    String DECIMAL_NAME = "decimal";
    String FUNCTION_NAME = "function";
    String DYADIC_FUNCTION_NAME = "dyadic function";
    String AXIS_RESTRICTION_NAME = "axis restriction";
    String SET_NAME = "set";
    String MODULE_NAME = "module";

}
