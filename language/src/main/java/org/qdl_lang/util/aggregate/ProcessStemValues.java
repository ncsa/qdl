package org.qdl_lang.util.aggregate;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * interface for processors that traverse stems. Each method gets the current key. Note that there
 * are not methods for sets or stems here since these are handled differently at lower levels. {@link ProcessScalar}
 * processes each element in a nested stem or set.  {@link ProcessStemAxisRestriction} treats the stems or
 * sets along an axis as aggregates.
 */
public interface ProcessStemValues {
    Object process(List<Object> index, Object key, Boolean booleanValue);
    Object process(List<Object> index, Object key, String stringValue);
    Object process(List<Object> index, Object key, Long longValue);
    Object process(List<Object> index, Object key, BigDecimal decimalValue);
    Object process(List<Object> index, Object key, QDLNull nullValue);
    Object process(List<Object> index, Object key, Module moduleValue);
    Object process(List<Object> index, Object key, FunctionReferenceNode frValue);
    Object process(List<Object> index, Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode);
}
