package org.qdl_lang.util.aggregate;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.variables.Constants;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for processors that traverse stems. Each method gets the current key. Note that there
 * are not methods for sets or stems here since these are handled differently at lower levels, i.e.
 * you must recurse through them. Effectively, this processes scalars only. {@link ProcessScalar}
 * processes each element in a nested stem or set.  {@link ProcessStemAxisRestriction} treats the stems or
 * sets along an axis as aggregates. This handles Java objects, not {@link QDLValue}s. To return a QDL
 * value, wrap it using {@link QDLValue#asQDLValue(Object)}.
 * <h2>Usage</h2>
 * <p>Each call has an index that is the stem path to the coordinates. So if you access A.b.c.d, then
 * the index is [b,c] and the key is d.</p>
 * <p><b>Note</b> that the default value here refers to the default for this processor, i.e., what
 * to return if there is no other logic. It ranges from just handing it back for the identity processor
 * to throwing an exception in almost every case for the no-op processor.</p>
 */
public interface ProcessStemValues extends Constants {
    Object process(List<Object> index, Object key, QDLValue qdlValue);
    Object process(List<Object> index, Object key, Boolean booleanValue);
    Object process(List<Object> index, Object key, String stringValue);
    Object process(List<Object> index, Object key, Long longValue);
    Object process(List<Object> index, Object key, BigDecimal decimalValue);
    Object process(List<Object> index, Object key, QDLNull nullValue);
    Object process(List<Object> index, Object key, Module moduleValue);
    Object process(List<Object> index, Object key, FunctionReferenceNode frValue);
    Object process(List<Object> index, Object key, DyadicFunctionReferenceNode dyadicFunctionReferenceNode);
}
