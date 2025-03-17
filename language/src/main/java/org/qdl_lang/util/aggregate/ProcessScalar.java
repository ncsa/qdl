package org.qdl_lang.util.aggregate;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

/**
 * Processing interface for stems and sets. This handles the individual set values.
 */
public interface ProcessScalar extends ProcessStemValues{
    // The following are for sets
    Object process(Boolean booleanValue);
    Object process(String stringValue);
    Object process(Long longValue);
    Object process(BigDecimal decimalValue);
    Object process(QDLNull nullValue);
    Object process(Module moduleValue);
    Object process(FunctionReferenceNode frValue);
    Object process(DyadicFunctionReferenceNode dyadicFunctionReferenceNode);

}
