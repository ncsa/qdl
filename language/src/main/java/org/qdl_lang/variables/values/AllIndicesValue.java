package org.qdl_lang.variables.values;

import org.qdl_lang.expressions.AllIndices;
import org.qdl_lang.variables.Constants;

/**
 * Since this needs to be passed around, this is essentially a marker class. Test the
 * type with {@link #getType()}
 */
public class AllIndicesValue extends QDLValue {
    public AllIndicesValue() {
        super(new AllIndices());
        type = Constants.ALL_INDICES_TYPE;
    }

    public static AllIndicesValue getAllIndicesValue() {
        if(allIndicesValue == null) {
            allIndicesValue = new AllIndicesValue();
        }
        return allIndicesValue;
    }

    static AllIndicesValue allIndicesValue = null;
}
