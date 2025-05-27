package org.qdl_lang.util.aggregate;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.List;

public class AbstractIdentityStemProcess implements ProcessStemValues{
    @Override
    public Object process(List<Object> index, Object key, Boolean booleanValue) {
        return getDefaultValue(index, key,booleanValue);
    }

    @Override
    public Object process(List<Object> index,Object key, String stringValue) {
        return getDefaultValue(index, key,stringValue);
    }

    @Override
    public Object process(List<Object> index,Object key, Long longValue) {
        return getDefaultValue(index, key,longValue);
    }

    @Override
    public Object process(List<Object> index,Object key, BigDecimal decimalValue) {
        return getDefaultValue(index, key,decimalValue);
    }

    @Override
    public Object process(List<Object> index,Object key, QDLNull nullValue) {
        return getDefaultValue(index, key,nullValue);
    }

    @Override
    public Object process(List<Object> index,Object key, Module moduleValue) {
        return getDefaultValue(index, key,moduleValue);
    }

    @Override
    public Object process(List<Object> index,Object key, FunctionReferenceNode frValue) {
        return getDefaultValue(index, key,frValue);
    }

    @Override
    public Object process(List<Object> index,Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode) {
        return getDefaultValue(index, key, dyadicFunctionReferenceNode);
    }

    /**
     * This is what makes this the identity function. If you want/need a different default
     * value for each call, override this.
     * @param value
     * @return
     */
    public QDLValue getDefaultValue(List<Object> index, Object key, Object value) {
        if(value instanceof QDLValue) {return (QDLValue) value;}
        return new QDLValue(value);
    }

}
