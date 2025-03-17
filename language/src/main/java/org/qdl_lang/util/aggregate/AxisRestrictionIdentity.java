package org.qdl_lang.util.aggregate;

import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

import java.util.List;

public class AxisRestrictionIdentity extends AbstractIdentityStemProcess implements ProcessStemAxisRestriction {
    @Override
    public Object process(List<Object> index, Object key, QDLSet set) {
        return getDefaultValue(index, key, set);
    }

    @Override
    public Object process(List<Object> index,Object key, QDLStem value) {
        return getDefaultValue(index, key, value);
    }
    protected int axis = ALL_AXES;
    @Override
    public int getAxis() {
        return axis;
    }
}
