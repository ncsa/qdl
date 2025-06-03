package org.qdl_lang.variables.values;

import org.qdl_lang.variables.Constant;

public class StringValue extends QDLKey {
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

    boolean isParsed = false;

    Integer parsedLongType = null;

    @Override
    public boolean isLong() {
        /*
        If this has been parsed, then return if this resulted in a long value
         */
        if (isParsed) {
            return parsedLongType == Constant.LONG_TYPE;
        }
        return super.isLong();
    }

    /**
     * For use as a {@link QDLKey}, it is possible that this is really the string representation
     * of a long index.
     *
     * @return
     */
    @Override
    public Long asLong() {
        if (!isParsed && longValue == null) {
            isParsed = true;
            try {
                longValue = Long.parseLong(asString());
                parsedLongType = Constant.LONG_TYPE;
            } catch (NumberFormatException e) {

            }
        }
        return longValue;
    }
}
