package org.qdl_lang.util.aggregate;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

public class AbstractIdentityStemProcess implements ProcessStemValues{
    @Override
    public Object process(Object key, Boolean booleanValue) {
        return getDefaultValue(booleanValue);
    }

    @Override
    public Object process(Object key, String stringValue) {
        return getDefaultValue(stringValue);
    }

    @Override
    public Object process(Object key, Long longValue) {
        return getDefaultValue(longValue);
    }

    @Override
    public Object process(Object key, BigDecimal decimalValue) {
        return getDefaultValue(decimalValue);
    }

    @Override
    public Object process(Object key, QDLNull nullValue) {
        return getDefaultValue(nullValue);
    }

    @Override
    public Object process(Object key, Module moduleValue) {
        return getDefaultValue(moduleValue);
    }

    @Override
    public Object process(Object key, FunctionReferenceNode frValue) {
        return getDefaultValue(frValue);
    }

    @Override
    public Object process(Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        return getDefaultValue(dyadicFunctionReferenceNode);
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
