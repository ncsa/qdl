package org.qdl_lang.variables.values;

import org.qdl_lang.variables.QDLList;
import org.qdl_lang.variables.QDLStem;

public class StemValue extends QDLValue{
    public StemValue(QDLStem value) {
        super(value);
        type = STEM_TYPE;
    }
    public StemValue(QDLList value) {
        this(new QDLStem(value));
    }

    /**
     * Create this as the empty stem
     */
    public StemValue() {
        this(new QDLStem());
    }
}
