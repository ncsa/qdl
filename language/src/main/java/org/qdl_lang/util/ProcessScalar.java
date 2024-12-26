package org.qdl_lang.util;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

public interface ProcessScalar {
    // The following are for sets
    Object process(Boolean booleanValue);
    Object process(String stringValue);
    Object process(Long longValue);
    Object process(BigDecimal decimalValue);
    Object process(QDLNull nullValue);
    Object process(Module moduleValue);
    Object process(FunctionReferenceNode frValue);
    Object process(DyadicFunctionReferenceNode dyadicFunctionReferenceNode);

   // The following are for Stems. Passing in the key allows for better error messages.
    Object process(Object key, Boolean booleanValue);
    Object process(Object key, String stringValue);
    Object process(Object key, Long longValue);
    Object process(Object key, BigDecimal decimalValue);
    Object process(Object key, QDLNull nullValue);
    Object process(Object key, Module moduleValue);
    Object process(Object key, FunctionReferenceNode frValue);
    Object process(Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode);

}
