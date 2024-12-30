package org.qdl_lang.util;

import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

/**
 * Basic implementation of {@link ProcessScalar} that simply throws an exception for each argument.
 * This is extended in the case that unknown values cause an exception, hence only explicitly
 * allowed types are permitted. This is the most restrictive type of processing, cf. {@link ProcessScalarImpl}
 * which is the least restrictive.
 */
public class NoOpScalarImpl implements ProcessScalar {
    @Override
    public Object process(String stringValue) {
        throw new BadStemValueException("string value not allowed");
    }

    @Override
    public Object process(Long longValue) {
        throw new BadStemValueException("integer value not allowed");
    }

    @Override
    public Object process(BigDecimal decimalValue) {
        throw new BadStemValueException("decimal value not allowed");
    }

    @Override
    public Object process(QDLNull nullValue) {
        throw new BadStemValueException("null value not allowed");
    }

    @Override
    public Object process(Boolean booleanValue) {
        throw new BadStemValueException("boolean value not allowed");
    }

    @Override
    public Object process(Module moduleValue) {
        throw new BadStemValueException("module value not allowed");
    }

    @Override
    public Object process(FunctionReferenceNode frValue) {
        throw new BadStemValueException("function reference not allowed");
    }

    @Override
    public Object process(DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        throw new BadStemValueException("dyadic function reference not allowed");
    }

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
