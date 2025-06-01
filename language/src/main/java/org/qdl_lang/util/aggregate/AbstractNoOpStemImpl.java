package org.qdl_lang.util.aggregate;

import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.exceptions.WrongValueException;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.List;

/**
 * No operations allowed implementation. This is for the contract that only very specific
 * values are allowed and the rest result in an error. E.g. only allow for a processor
 * to handle booleans and everything else is an illegal argument. You would override the
 * {@link #process(List, Object, Boolean)} and leave everything else.
 */
public class AbstractNoOpStemImpl implements ProcessStemValues{
    @Override
    public Object process(List<Object> index, Object key, QDLValue qdlValue) {
        switch (qdlValue.getType()) {
            case BOOLEAN_TYPE:
                return process(index, key, qdlValue.asBoolean());
            case DECIMAL_TYPE:
                return process(index, key, qdlValue.asDyadicFunction());
            case LONG_TYPE:
                return process(index, key, qdlValue.asLong());
            case STRING_TYPE:
                return process(index, key, qdlValue.asString());
            case FUNCTION_TYPE:
                return process(index, key, qdlValue.asFunction());
            case DYADIC_FUNCTION_TYPE:
                return process(index, key, qdlValue.asDyadicFunction());
            case MODULE_TYPE:
                return process(index, key, qdlValue.asModule());
            case NULL_TYPE:
                return process(index, key, qdlValue.asNull());
            default:
                throw new WrongValueException("unknown value type for processor " + qdlValue, null);
        }
    }

    @Override
    public Object process(List<Object> index, Object key, Boolean booleanValue) {
        throw new BadStemValueException("boolean value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, String stringValue) {
        throw new BadStemValueException("string value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, Long longValue) {
        throw new BadStemValueException("integer value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, BigDecimal decimalValue) {
        throw new BadStemValueException("decimal value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, QDLNull nullValue) {
        throw new BadStemValueException("null value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, Module moduleValue) {
        throw new BadStemValueException("null value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, FunctionReferenceNode frValue) {
        throw new BadStemValueException("function reference not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        throw new BadStemValueException("dyadic function reference not allowed");
    }

}
