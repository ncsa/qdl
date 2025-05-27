package org.qdl_lang.variables.values;

public class LongValue extends QDLValue {
    public LongValue(Long value) {
        super(value);
        type = LONG_TYPE;
    }

    /**
     * Convenience constructor to float the int to a long
     * @param value
     */
    public LongValue(Integer value) {
        this(value.longValue());
    }

}
