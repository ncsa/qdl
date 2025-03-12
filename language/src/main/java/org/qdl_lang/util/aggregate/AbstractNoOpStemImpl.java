package org.qdl_lang.util.aggregate;

import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

/**
 * No operations allowed implementation. This is for the contract that only very specific
 * values are allowed and the rest result in an error. E.g. only allow for a processor
 * to handle booleans and everything else is an illegal argument. You would override the
 * {@link #process(Object, Boolean)} and leave everything else.
 */
public class AbstractNoOpStemImpl implements ProcessStemValues{
    @Override
    public Object process(Object key, Boolean booleanValue) {
        throw new BadStemValueException("boolean value not allowed");
    }

    @Override
    public Object process(Object key, String stringValue) {
        throw new BadStemValueException("string value not allowed");
    }

    @Override
    public Object process(Object key, Long longValue) {
        throw new BadStemValueException("integer value not allowed");
    }

    @Override
    public Object process(Object key, BigDecimal decimalValue) {
        throw new BadStemValueException("decimal value not allowed");
    }

    @Override
    public Object process(Object key, QDLNull nullValue) {
        throw new BadStemValueException("null value not allowed");
    }

    @Override
    public Object process(Object key, Module moduleValue) {
        throw new BadStemValueException("null value not allowed");
    }

    @Override
    public Object process(Object key, FunctionReferenceNode frValue) {
        throw new BadStemValueException("function reference not allowed");
    }

    @Override
    public Object process(Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        throw new BadStemValueException("dyadic function reference not allowed");
    }

}
