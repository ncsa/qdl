package org.qdl_lang.variables.values;

import org.qdl_lang.variables.Constants;
import org.qdl_lang.variables.QDLNull;

/**
 * Since there is a single instance of the {@link QDLNull}, there needs to be a dedicated class
 * for it that sidesteps some bootstrapping issues.
 */
public class QDLNullValue extends QDLValue{
    public QDLNullValue() {
        super(QDLNull.getInstance());
        type = NULL_TYPE;
    }

}
