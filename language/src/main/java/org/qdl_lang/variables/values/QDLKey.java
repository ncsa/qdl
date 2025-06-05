package org.qdl_lang.variables.values;

import org.qdl_lang.exceptions.WrongValueException;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
        if (value instanceof QDLValue) {
            QDLValue key = (QDLValue) value;
            switch (key.getType()) {
                case LONG_TYPE:
                    if (key instanceof LongValue) {
                        return (LongValue) key;
                    }
                    return new LongValue(key.asLong());
                case STRING_TYPE:
                    if (QDLStem.isLongIndex(key.asString())) {
                        return new LongValue(Long.parseLong(key.asString()));
                    }
                    if (key instanceof StringValue) {
                        return (StringValue) key;
                    }
                    return new StringValue(key.asString());

                case STEM_TYPE:
                    if (key instanceof StemValue) {
                        return (StemValue) key;
                    }
                    return new StemValue(key.asStem());
                case DECIMAL_TYPE:
                    return decimalToLong(key.asDecimal());
                default:
                    throw new WrongValueException(value + " is not a valid key type", null);
            }
        }
        // So just a POJO. Figure it out.
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
                throw new WrongValueException(value + " is not a valid key type (" + value.getClass().getSimpleName() + ")", null);
        }
    }

    /**
     * Decimals <i>may</i> be used as keys if they have a trivial fractional part.
     * Since an expression like
     * <pre>
     *     a := 6.0/3.0;
     * </pre>
     * would reliably yield the value 2.0, This should be usable as a stem index as the integer 2.
     *
     * @param decimal
     * @return
     */
    static protected LongValue decimalToLong(BigDecimal decimal) {
        try {
            BigInteger bi = decimal.toBigIntegerExact();
            return new LongValue(bi.longValueExact());
        } catch (ArithmeticException e) {
            // Too big to be an index. Maybe it actually is an integer anyway
        }
        throw new WrongValueException(decimal + " is not a valid key type", null);
    }

    /**
     * Converts a collection -- a list or set -- of objects to keys. Note that
     * non-key values will cause an error.
     *
     * @param values
     * @return
     */
    public static Collection<QDLKey> from(Collection<Object> values) {
        Collection<QDLKey> out;
        if (values instanceof List) {
            out = new ArrayList<>(values.size());
        } else {
            out = new HashSet<>(values.size());
        }
        for (Object v : values) {
            out.add(from(v));
        }
        return out;
    }
}
