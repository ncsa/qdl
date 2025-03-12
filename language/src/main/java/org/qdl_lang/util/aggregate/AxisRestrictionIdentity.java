package org.qdl_lang.util.aggregate;

import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

public class AxisRestrictionIdentity extends AbstractIdentityStemProcess implements ProcessStemAxisRestriction {
    @Override
    public Object process(Object key, QDLSet set) {
        return getDefaultValue(set);
    }

    @Override
    public Object process(Object key, QDLStem value) {
        return getDefaultValue(value);
    }
    protected int axis = ALL_AXES;
    @Override
    public int getAxis() {
        return axis;
    }
}
