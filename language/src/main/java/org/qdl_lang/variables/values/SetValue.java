package org.qdl_lang.variables.values;

import org.qdl_lang.variables.QDLSet;

public class SetValue extends QDLValue {
    public SetValue(QDLSet value) {
        super(value);
        type = SET_TYPE;
    }

    /**
     * Create this as the empty set.
     */
    public SetValue() {
        this(new QDLSet());
    }
}
