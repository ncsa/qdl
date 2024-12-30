package org.qdl_lang.util;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

/**
 * Basic (identity) implementation of {@link ProcessScalar}, simply returns each argument unchanged.
 * The default is that the dyadic calls (for stems) just call the monadic ones, so you
 * can usually just get away with implementing one method for any type. If you want
 * some specific error message, you have enough information for that too.
 *
 * This is the most permissive type of processig.
 * This class is designed to do 95% of the work for handling aggregates and is really
 * redundant and boring.
 */
public class ProcessScalarImpl implements ProcessScalar {
    @Override
    public Object process(String stringValue) {
        return getDefaultValue(stringValue);
    }

    @Override
    public Object process(Long longValue) {
        return getDefaultValue(longValue);
    }

    @Override
    public Object process(BigDecimal decimalValue) {
        return getDefaultValue(decimalValue);
    }

    @Override
    public Object process(QDLNull nullValue) {
        return getDefaultValue(nullValue);
    }

    @Override
    public Object process(Boolean booleanValue) {
        return getDefaultValue(booleanValue);
    }

    @Override
    public Object process(Module moduleValue) {
        return getDefaultValue(moduleValue);
    }

    @Override
    public Object process(FunctionReferenceNode frValue) {
        return getDefaultValue(frValue);
    }

    @Override
    public Object process(DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        return getDefaultValue(dyadicFunctionReferenceNode);
    }

    @Override
    public Object process(Object key, Boolean booleanValue) {
        return process(booleanValue);
    }

    @Override
    public Object process(Object key, String stringValue) {
        return process(stringValue);
    }

    @Override
    public Object process(Object key, Long longValue) {
        return process(longValue);
    }

    @Override
    public Object process(Object key, BigDecimal decimalValue) {
        return process(decimalValue);
    }

    @Override
    public Object process(Object key, QDLNull nullValue) {
        return process(nullValue);
    }

    @Override
    public Object process(Object key, Module moduleValue) {
        return process(moduleValue);
    }

    @Override
    public Object process(Object key, FunctionReferenceNode frValue) {
        return process(frValue);
    }

    @Override
    public Object process(Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        return process(dyadicFunctionReferenceNode);
    }

    /**
     * This is what makes this the identity function. If you want/need a different default
     * value for each call, override this.
     * @param value
     * @return
     */
    public Object getDefaultValue(Object value) {
        return value;
    }
}
