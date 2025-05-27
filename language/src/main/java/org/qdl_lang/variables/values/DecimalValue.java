package org.qdl_lang.variables.values;

import java.math.BigDecimal;

public class DecimalValue extends QDLValue{
    public DecimalValue(BigDecimal value) {
        super(value);
        type = DECIMAL_TYPE;
    }
}
