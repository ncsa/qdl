package org.qdl_lang.util.aggregate;

import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

import java.util.List;

public class AxisRestrictionNoOp extends AbstractNoOpStemImpl implements ProcessStemAxisRestriction{
    @Override
    public Object process(List<Object> index, Object key, QDLSet sset) {
        throw new BadStemValueException("set value not allowed");
    }

    @Override
    public Object process(List<Object> index,Object key, QDLStem value) {
        throw new BadStemValueException("stem value not allowed");
    }

    int axis = ALL_AXES;
    @Override
    public int getAxis() {
        return axis;
    }
}
