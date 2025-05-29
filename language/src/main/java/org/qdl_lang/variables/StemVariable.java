package org.qdl_lang.variables;

import org.qdl_lang.variables.values.QDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  12:09 PM
 * @deprecated use {@link QDLStem}
 */
public class StemVariable extends QDLStem {

    public StemVariable() {
        super();
    }

    public StemVariable(Long count, QDLValue[] fillList) {
        super(count, fillList);
    }

    @Override
    public QDLStem newInstance() {
        return new StemVariable();
    }

    @Override
    public QDLStem newInstance(Long count, QDLValue[] fillList) {
        return new StemVariable(count, fillList);
    }

}
