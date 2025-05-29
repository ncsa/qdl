package org.qdl_lang.util.aggregate;

import org.qdl_lang.exceptions.WrongValueException;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.Constants;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.List;

public class AbstractIdentityStemProcess implements ProcessStemValues {
    @Override
    public Object process(List<Object> index, Object key, Boolean booleanValue) {
        return getDefaultValue(index, key, booleanValue);
    }

    @Override
    public Object process(List<Object> index, Object key, String stringValue) {
        return getDefaultValue(index, key, stringValue);
    }

    @Override
    public Object process(List<Object> index, Object key, Long longValue) {
        return getDefaultValue(index, key, longValue);
    }

    @Override
    public Object process(List<Object> index, Object key, BigDecimal decimalValue) {
        return getDefaultValue(index, key, decimalValue);
    }

    @Override
    public Object process(List<Object> index, Object key, QDLNull nullValue) {
        return getDefaultValue(index, key, nullValue);
    }

    @Override
    public Object process(List<Object> index, Object key, Module moduleValue) {
        return getDefaultValue(index, key, moduleValue);
    }

    @Override
    public Object process(List<Object> index, Object key, FunctionReferenceNode frValue) {
        return getDefaultValue(index, key, frValue);
    }

    @Override
    public Object process(List<Object> index, Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        return getDefaultValue(index, key, dyadicFunctionReferenceNode);
    }

    /**
     * This is what makes this the identity function. If you want/need a different default
     * value for each call, override this.
     *
     * @param value
     * @return
     */
    public Object getDefaultValue(List<Object> index, Object key, Object value) {
        if (value instanceof QDLValue) {
            return ((QDLValue) value).getValue();
        }
        return value;
    }

    @Override
    public Object process(List<Object> index, Object key, QDLValue qdlValue) {
        switch (qdlValue.getType()) {
            case BOOLEAN_TYPE:
                return process(index,key,qdlValue.asBoolean());
            case DECIMAL_TYPE:
                return process(index,key,qdlValue.asDyadicFunction());
            case LONG_TYPE:
                return process(index,key,qdlValue.asLong());
            case STRING_TYPE:
                return process(index,key,qdlValue.asString());
            case STEM_TYPE:
            case SET_TYPE:
            case FUNCTION_TYPE:
                return process(index,key,qdlValue.asFunction());
            case DYADIC_FUNCTION_TYPE:
                return process(index,key,qdlValue.asDyadicFunction());
            case MODULE_TYPE:
                return process(index,key,qdlValue.asModule());
            case NULL_TYPE:
                return process(index,key,qdlValue.asNull());
            default:
                throw new WrongValueException("unknown value type for processor " + qdlValue, null);
        }
    }
}
