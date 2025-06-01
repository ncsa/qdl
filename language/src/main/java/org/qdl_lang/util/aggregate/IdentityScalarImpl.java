package org.qdl_lang.util.aggregate;

import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.variables.QDLNull;

import java.math.BigDecimal;

/**
 * Basic (identity) implementation of {@link ProcessScalar}, simply returns each argument unchanged.
 * The default is that the dyadic calls (for stems) just call the monadic ones, so you
 * can usually just get away with implementing one method for any type. If you want
 * some specific error message, you have enough information for that too.
 *
 * This is the most permissive type of processing.
 * This class is designed to do 95% of the work for handling aggregates and is really
 * redundant and boring. Again, you should only need to override those things that change.
 */
public class IdentityScalarImpl extends AbstractIdentityStemProcess implements ProcessScalar {
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

    /**
     * Since the assumption is that this is being called resolving nested sets (which are unordered
     * hence unindexed) there is no index or key really possible.
     * @param value
     * @return
     */
    public Object getDefaultValue(Object value) {
        return getDefaultValue(null, null, value);
    }
}
