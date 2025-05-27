package org.qdl_lang.variables.values;

public class StringValue extends QDLValue{
    public StringValue(String value) {
        super(value);
        type = STRING_TYPE;
    }

    /**
     * Convenience constructor for empty string.
     */
    public StringValue() {
        this("");
    }

}
