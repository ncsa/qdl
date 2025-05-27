package org.qdl_lang.variables.values;

import org.qdl_lang.expressions.AxisExpression;
import org.qdl_lang.statements.ConditionalStatement;

public class AxisRestrictionValue extends QDLValue {
    public AxisRestrictionValue(AxisExpression value) {
        super(value);
        type = AXIS_RESTRICTION_TYPE;
    }
}
