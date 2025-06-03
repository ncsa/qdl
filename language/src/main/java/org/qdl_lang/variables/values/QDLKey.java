package org.qdl_lang.variables.values;

import org.qdl_lang.exceptions.WrongValueException;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A marker class for those {@link QDLValue}s that may also function as keys to stems and lists.
 */
public class QDLKey extends QDLValue {
    public QDLKey(Object value) {
        super(value);
    }

    /**
     * This will convert the POJO to a key. It does try to return the correct actual
     * type so feeding in a string like "2" will result in a {@link LongValue} of 2.
     * By the same token, decimal values are checked to see if they are actually integers
     *
     * @param value
     * @return
     */
    public static QDLKey from(Object value) {
        if (value instanceof QDLKey) {
            QDLKey key = (QDLKey) value;
            if (key.isString()) {
                if (QDLStem.isLongIndex(key.asString())) {
                    return new LongValue(Long.parseLong(key.asString()));
                }
            }
            if (key.isDecimal()) {
                return decimalToLong(key.asDecimal());
            }
            return key;
        }
        switch (Constant.getType(value)) {
            case INTEGER_TYPE:
                return new LongValue((Integer) value);
            case LONG_TYPE:
                return new LongValue((Long) value);
            case STRING_TYPE:
                if (QDLStem.isLongIndex((String) value)) {
                    return new LongValue(Long.parseLong((String) value));
                }
                return new StringValue((String) value);
            case STEM_TYPE:
                return new StemValue((QDLStem) value);
            case DECIMAL_TYPE:
                return decimalToLong((BigDecimal) value);
            default:
                throw new WrongValueException(value + " is not a valid key type", null);
        }
    }

    static protected LongValue decimalToLong(BigDecimal decimal) {
        try {
            BigInteger bi = decimal.toBigIntegerExact();
            return new LongValue(bi.longValueExact());
        } catch (ArithmeticException e) {
            // Too big to be an index. Maybe it actually is an integer anyway
        }
        throw new WrongValueException(decimal + " is not a valid key type", null);
    }
}
