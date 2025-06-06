package org.qdl_lang.evaluate;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.exceptions.ExtraArgException;
import org.qdl_lang.exceptions.MissingArgException;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.codecs.AbstractCodec;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.Date;

import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.variables.values.QDLValue.castToJavaValues;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  9:18 AM
 */
public class MathEvaluator extends AbstractEvaluator {
    @Override
    public String getNamespace() {
        return MATH_NAMESPACE;
    }

    public static final String MATH_NAMESPACE = "math";
    public static final int MATH_FUNCTION_BASE_VALUE = 1000;

    public static final String ABS_VALUE = "abs";
    public static final int ABS_VALUE_TYPE = 1 + MATH_FUNCTION_BASE_VALUE;

    public static final String RANDOM = "random";
    public static final int RANDOM_TYPE = 2 + MATH_FUNCTION_BASE_VALUE;

    public static final String RANDOM_STRING = "random_string";
    public static final int RANDOM_STRING_TYPE = 3 + MATH_FUNCTION_BASE_VALUE;

    public static final String HASH = "hash";
    public static final int HASH_TYPE = 4 + MATH_FUNCTION_BASE_VALUE;

    public static final String ENCODE = "encode";
    public static final int ENCODE_TYPE = 19 + MATH_FUNCTION_BASE_VALUE;

    public static final String DECODE = "decode";
    public static final int DECODE_TYPE = 20 + MATH_FUNCTION_BASE_VALUE;


    public static final String DATE_MS = "date_ms";
    public static final int DATE_MS_TYPE = 7 + MATH_FUNCTION_BASE_VALUE;


    public static final String MOD = "mod";
    public static final int MOD_TYPE = 10 + MATH_FUNCTION_BASE_VALUE;

    public static final String DATE_ISO = "date_iso";
    public static final int DATE_ISO_TYPE = 11 + MATH_FUNCTION_BASE_VALUE;

    public static final String NUMERIC_DIGITS = "numeric_digits";
    public static final int NUMERIC_DIGITS_TYPE = 12 + MATH_FUNCTION_BASE_VALUE;

    public static final String IDENTITY_FUNCTION = "i";
    public static final String LONG_IDENTITY_FUNCTION = "identity";
    public static final int IDENTITY_FUNCTION_TYPE = 14 + MATH_FUNCTION_BASE_VALUE;

    public static final String MIN = "min";
    public static final int MIND_TYPE = 15 + MATH_FUNCTION_BASE_VALUE;

    public static final String MAX = "max";
    public static final int MAX_TYPE = 16 + MATH_FUNCTION_BASE_VALUE;


    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{
                    IDENTITY_FUNCTION,
                    LONG_IDENTITY_FUNCTION,
                    ABS_VALUE,
                    RANDOM,
                    RANDOM_STRING,
                    HASH,
                    ENCODE,
                    DECODE,
                    DATE_MS,
                    DATE_ISO,
                    NUMERIC_DIGITS,
                    MOD,
                    MAX, MIN,
                    DATE_ISO};
        }
        return fNames;
    }

    @Override
    public int getType(String name) {
        switch (name) {
            case IDENTITY_FUNCTION:
            case LONG_IDENTITY_FUNCTION:
                return IDENTITY_FUNCTION_TYPE;
            case ABS_VALUE:
                return ABS_VALUE_TYPE;
            case RANDOM:
                return RANDOM_TYPE;
            case RANDOM_STRING:
                return RANDOM_STRING_TYPE;
            case HASH:
                return HASH_TYPE;
            case NUMERIC_DIGITS:
                return NUMERIC_DIGITS_TYPE;
            case ENCODE:
                return ENCODE_TYPE;
            case DECODE:
                return DECODE_TYPE;
            case DATE_MS:
                return DATE_MS_TYPE;
            case DATE_ISO:
                return DATE_ISO_TYPE;
            case MOD:
                return MOD_TYPE;
            case MAX:
                return MAX_TYPE;
            case MIN:
                return MIND_TYPE;
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }


    @Override
    public boolean dispatch(Polyad polyad, State state) {
        switch (polyad.getName()) {
            case IDENTITY_FUNCTION:
            case LONG_IDENTITY_FUNCTION:
                doIdentityFunction(polyad, state);
                return true;
            case ABS_VALUE:
                doAbs(polyad, state);
                return true;
            case RANDOM:
                doRandom(polyad, state);
                return true;
            case RANDOM_STRING:
                doRandomString(polyad, state);
                return true;
            case HASH:
                doHash(polyad, state);
                return true;
            case ENCODE:
                doCodec(polyad, state, true);
                return true;
            case DECODE:
                doCodec(polyad, state, false);
                return true;
            case NUMERIC_DIGITS:
                doNumericDigits(polyad, state);
                return true;
            case DATE_MS:
                doDates(polyad, state, true);
                return true;
            case DATE_ISO:
                doDates(polyad, state, false);
                return true;
            case MOD:
                doModulus(polyad, state);
                return true;
            case MAX:
                doMax(polyad, state);
                return true;
            case MIN:
                doMin(polyad, state);
                return true;
        }
        return false;
    }


    private void doCodec(Polyad polyad, State state, final boolean isEncode) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException((isEncode ? ENCODE : DECODE) + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException((isEncode ? ENCODE : DECODE) + " requires at most 2 arguments", polyad.getArgAt(2));
        }
        final int defaultAlgorithm = 64; // default for base 64.

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                objects = castToJavaValues(objects);
                AbstractCodec codec;
                if (objects.length == 2) {
                    try {
                        codec = MetaCodec.createCodec(objects[1]);
                    } catch (IllegalArgumentException iax) {
                        throw new BadArgException(iax.getMessage(), polyad.getArgAt(1));
                    }
                } else {
                    codec = MetaCodec.createCodec(defaultAlgorithm);
                }
                if (objects[0] instanceof String) {
                    String arg = objects[0].toString();
                    r.result = asQDLValue(applyCodec(codec, arg, isEncode));
                }else{
                    if(objects[0] instanceof QDLSet){
                       r.result = asQDLValue(doCodec(polyad, (QDLSet) objects[0], codec, isEncode));
                    }else{
                        if(objects[0] instanceof QDLStem){
                            r.result = asQDLValue(doCodec(polyad, (QDLStem) objects[0], codec, isEncode));
                        }else{
                            r.result = asQDLValue(objects[0]);
                        }
                    }
                }
                return r;
            }
        };
        if (polyad.getArgCount() == 1) {
            process1(polyad, pointer, isEncode ? ENCODE : DECODE, state);
        } else {
            process2(polyad, pointer, isEncode ? ENCODE : DECODE, state, true);
        }
    }

    /**
     * Apply code to the string.
     *
     * @param codec
     * @param arg
     * @param isEncode
     * @return
     */
    private String applyCodec(AbstractCodec codec, String arg, boolean isEncode) {
        if (isEncode) {
            return codec.encode(arg);
        } else {
            return codec.decode(arg);
        }
    }

    protected QDLSet doCodec(Polyad polyad, QDLSet<QDLValue> inSet, AbstractCodec codec, boolean isEncode) {
        QDLSet<QDLValue> outSet = new QDLSet();
        for (QDLValue object : inSet) {
            if (object.isString()) {
                outSet.add(asQDLValue(applyCodec(codec, object.asString(), isEncode)));
            } else {
                if (object.isSet()) {
                    outSet.add(asQDLValue(doCodec(polyad, object.asSet(), codec, isEncode)));
                } else {
                        outSet.add(object); // no change
                }
            }
        }
        return outSet;
    }

    protected QDLStem doCodec(Polyad polyad, QDLStem inStem, AbstractCodec codec, boolean isEncode) {
        QDLStem outStem = new QDLStem();
        for (QDLKey key : inStem.keySet()) {
            QDLValue value = inStem.get(key);
            if (value.isString()) {
                outStem.put(key, asQDLValue(applyCodec(codec, value.asString(), isEncode)));
            } else {
                if (value.isSet()) {
                    outStem.put(key, asQDLValue(doCodec(polyad, value.asSet(), codec, isEncode)));
                } else {
                    if (value.isStem()) {
                        outStem.put(key, asQDLValue(doCodec(polyad, value.asStem(), codec, isEncode)));
                    } else {
                        outStem.put(key, value); // no change
                    }
                }
            }
        }
        return outStem;
    }

    /**
     * The identity function returns its argument. Simple as that.
     *
     * @param polyad
     * @param state
     */
    protected void doIdentityFunction(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(IDENTITY_FUNCTION + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(IDENTITY_FUNCTION + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));

        polyad.setResult(arg);
        polyad.setEvaluated(true);
    }

    protected void doAbs(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(ABS_VALUE + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(ABS_VALUE + " requires at most 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                // If a long or decimal, take the absolute value. If anything else (e.g. a string) return argument.
                objects = castToJavaValues(objects);
                switch (Constant.getType(objects[0])) {
                    case Constant.LONG_TYPE:
                        r.result = asQDLValue(Math.abs((Long) objects[0]));
                        break;
                    case Constant.DECIMAL_TYPE:
                        BigDecimal bd = (BigDecimal) objects[0];
                        r.result = asQDLValue(bd.abs());
                        break;
                    default:
                        r.result = asQDLValue(objects[0]);
                }

                return r;
            }
        };
        process1(polyad, pointer, ABS_VALUE, state);
    }

    SecureRandom secureRandom = new SecureRandom();

    protected void doRandom(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(RANDOM + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            polyad.setResult(secureRandom.nextLong());
            polyad.setEvaluated(true);
            return;
        }
        Object result;
        int resultType = 0;

        // if the argument is a number return that many random numbers in a stem variable.
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));
        if (arg.isLong()) {
            int size = arg.asLong().intValue();
            QDLStem stemVariable = new QDLStem();
            for (int i = 0; i < size; i++) {
                stemVariable.put(Integer.toString(i), asQDLValue(secureRandom.nextLong()));
            }
            result = stemVariable;
            resultType = Constant.STEM_TYPE;
        } else {
            // unknown type is ignored.
            throw new BadArgException(RANDOM + " requires a integer as its argument if present", polyad.getArgAt(0));
        }
        polyad.setResult(result);
        polyad.setEvaluated(true);

    }

    protected void doNumericDigits(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(NUMERIC_DIGITS + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Long oldND = (long) state.getOpEvaluator().getNumericDigits();
        polyad.setResult(new QDLValue(oldND));

        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
        } else {
            QDLValue arg1 = polyad.evalArg(0, state);
            checkNull(arg1, polyad.getArgAt(0));

            if (!arg1.isLong()) {
                throw new BadArgException(NUMERIC_DIGITS + " requires an integer argument", polyad.getArgAt(0));
            }
            Long newND = arg1.asLong();
            state.getOpEvaluator().setNumericDigits(newND.intValue());
            polyad.setEvaluated(true);
        }
        return;
    }

    protected void doRandomString(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(RANDOM_STRING + " requires at most 2 arguments", polyad.getArgAt(2));
        }

        int length = 16;

        if (0 < polyad.getArgCount()) {
            polyad.evalArg(0, state);
            QDLValue obj = polyad.getArguments().get(0).getResult();
            checkNull(obj, polyad.getArgAt(0));

            if (obj.isLong()) {
                length = obj.asLong().intValue();
            } else {
                throw new BadArgException(RANDOM_STRING + " takes an integer as its first argument", polyad.getArgAt(0));

            }
        }
        // Second optional argument is number of strings.
        int returnCount = 1;
        if (polyad.getArgCount() == 2) {
            polyad.evalArg(1, state);
            QDLValue obj = polyad.getArguments().get(1).getResult();
            checkNull(obj, polyad.getArgAt(1));// re-used varaible obj for arg #1

            if (!obj.isLong()) {
                throw new BadArgException(RANDOM_STRING + " takes an integer as its second argument.", polyad.getArgAt(1));
            }

                returnCount = obj.asLong().intValue();
                if (returnCount <= 0) {
                    returnCount = 1;
                }
        }


        byte[] ba = new byte[length];
        if (returnCount == 1) {
            secureRandom.nextBytes(ba);
            String rc = Base64.encodeBase64URLSafeString(ba);
            polyad.setResult(rc);
            polyad.setEvaluated(true);
            return;
        }
        // so more than one string needs to be returned.

        QDLStem stem = new QDLStem();
        for (int i = 0; i < returnCount; i++) {
            secureRandom.nextBytes(ba);
            String rc = Base64.encodeBase64URLSafeString(ba);
            stem.put(from(i), asQDLValue(rc));
        }
        polyad.setResult(stem);
        polyad.setEvaluated(true);
        return;
    }


    protected void doHash(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(HASH + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(HASH + " takes at most 2 arguments", polyad.getArgAt(2));
        }


        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                objects = castToJavaValues(objects);
                if (objects[0] instanceof String) {
                    String algorithm = "sha-1";
                    if (objects.length == 2) {
                        if (objects[1] instanceof String) {
                            algorithm = (String) objects[1];
                        } else {
                            throw new BadArgException("hash algorithm name '" + objects[1] + "' must be a string", polyad.getArgAt(1));
                        }
                    }
                    String token = (String) objects[0];
                    String tempOut;
                    switch (algorithm) {
                        case HASH_ALGORITHM_MD2:
                             tempOut = DigestUtils.md2Hex(token);
                            break;
                        case HASH_ALGORITHM_MD5:
                             tempOut = DigestUtils.md5Hex(token);
                            break;
                        case HASH_ALGORITHM_SHA1:
                             tempOut = DigestUtils.sha1Hex(token);
                            break;
                        case HASH_ALGORITHM_SHA2:
                        case HASH_ALGORITHM_SHA_256:
                             tempOut = DigestUtils.sha256Hex(token);
                            break;
                        case HASH_ALGORITHM_SHA_384:
                             tempOut = DigestUtils.sha384Hex(token);
                            break;
                        case HASH_ALGORITHM_SHA_512:
                             tempOut = DigestUtils.sha512Hex(token);
                            break;
                        default:
                            throw new BadArgException("unknown hash algorithm'" + algorithm + "'", polyad.getArgAt(1));

                    }
                    r.result = asQDLValue(tempOut);
                } else {
                    r.result = asQDLValue(objects[0]);
                }
                return r;
            }
        };
        if (polyad.getArgCount() == 1) {
            process1(polyad, pointer, HASH, state);
        } else {
            process2(polyad, pointer, HASH, state);
        }
    }

    protected String hashIt(String token, String algorithm) {
        switch (algorithm) {
            case HASH_ALGORITHM_MD2:
                return DigestUtils.md2Hex(token);
            case HASH_ALGORITHM_MD5:
                return DigestUtils.md5Hex(token);
            case HASH_ALGORITHM_SHA1:
                return DigestUtils.sha1Hex(token);
            case HASH_ALGORITHM_SHA2:
            case HASH_ALGORITHM_SHA_256:
                return DigestUtils.sha256Hex(token);
            case HASH_ALGORITHM_SHA_384:
                return DigestUtils.sha384Hex(token);
            case HASH_ALGORITHM_SHA_512:
                return DigestUtils.sha512Hex(token);
            default:
                return null;

        }
    }

    public static final String HASH_ALGORITHM_MD2 = "md2";
    public static final String HASH_ALGORITHM_MD5 = "md5";
    public static final String HASH_ALGORITHM_SHA1 = "sha-1";
    public static final String HASH_ALGORITHM_SHA2 = "sha-2";
    public static final String HASH_ALGORITHM_SHA_256 = "sha-256";
    public static final String HASH_ALGORITHM_SHA_384 = "sha-384";
    public static final String HASH_ALGORITHM_SHA_512 = "sha-512";

    /**
     * Compute the modulus of two numbers, i.e. the remainder after division
     *
     * @param polyad
     */
    protected void doModulus(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(MOD + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(MOD + " requires 2 arguments", polyad.getArgAt(2));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                objects = castToJavaValues(objects);
                if (!areAllNumbers(objects)) {
                    // Contract is that if there are not numbers, just return the
                    // first argument unaltered.
                    r.result = asQDLValue(objects[0]);
                    return r;
                }
                if (areAllLongs(objects)) {
                    long second = (Long) objects[1];
                    if (second == 0L) {
                        throw new BadArgException(MOD + " requires non-zero second argument", polyad.getArgAt(1));

                    }
                    r.result = asQDLValue(((Long) objects[0]) % second);
                    return r;
                }
                // so one of these is a big decimal at least
                boolean b0 = isBigDecimal(objects[0]);
                boolean b1 = isBigDecimal(objects[1]);
                BigDecimal bd0 = b0 ? (BigDecimal) objects[0] : new BigDecimal((Long) objects[0]);
                BigDecimal bd1 = b1 ? (BigDecimal) objects[1] : new BigDecimal((Long) objects[1]);
                if (bd1.equals(BigDecimal.ZERO)) {
                    throw new BadArgException(MOD + " requires non-zero second argument", polyad.getArgAt(1));
                }
                BigDecimal bdr = null;
                try {
                    bdr = bd0.remainder(bd1, OpEvaluator.getMathContext());
                } catch (ArithmeticException ax1) {
                    // if the remainder is less than the rounding error, then this is thrown. Report it.
                    throw new IllegalStateException("There is insufficient numeric precision to compute this. Please adjust " + MathEvaluator.NUMERIC_DIGITS);
                }
                try {
                    r.result = asQDLValue(bdr.longValueExact());
                    return r;
                } catch (ArithmeticException ax) {
                }

                r.result = asQDLValue(bdr); // won't fit in a long, so this is really a BigInteger, which we don't support directly.
                return r;
            }
        };
        process2(polyad, pointer, MOD, state);
    }

    protected void doDates(Polyad polyad, State state, boolean isInMillis) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException((isInMillis ? DATE_MS : DATE_ISO) + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            // A niladic case. return the right date type.
            if (isInMillis) {
                Long now = new Date().getTime();
                polyad.setResult(now);
                polyad.setEvaluated(true);
            } else {
                Date d = new Date();
                String now = Iso8601.date2String(d);
                polyad.setResult(now);
                polyad.setEvaluated(true);
            }
            return;
        }
        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                objects = castToJavaValues(objects);
                if (isInMillis) {
                    if (isLong(objects[0])) {
                        // do nothing. hand it back.
                        r.result = asQDLValue(objects[0]);
                    } else {
                        // assume it's and ISO 8601 date and should le converted to millis
                        try {
                            String x = objects[0].toString().trim();
                            if(!x.endsWith("Z")){
                                // try it as a local time
                                LocalTime localTime =LocalTime.parse(x);
                            }
                            Long ts = Iso8601.string2Date(objects[0].toString()).getTimeInMillis();
                            r.result = asQDLValue(ts);
                        } catch (Throwable t) {

                            r.result = asQDLValue(objects[0]);
                        }
                    }
                } else {
                    // work with ISO 8601 dates
                    if (isLong(objects[0])) {
                        String now = Iso8601.date2String((Long) objects[0]);
                        r.result = asQDLValue(now);
                        return r;
                    } else {
                        r.result = asQDLValue(objects[0]);
                    }
                }
                return r;

            }
        };
        process1(polyad, pointer, isInMillis ? DATE_MS : DATE_ISO, state);

    }

    protected void doMin(Polyad polyad, State state) {
        doMinOrMax(polyad, state, false);
    }

    protected void doMax(Polyad polyad, State state) {
        doMinOrMax(polyad, state, true);
    }

    protected void doMinOrMax(Polyad polyad, State state, boolean isMax) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException((isMax ? MAX : MIN) + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException((isMax ? MAX : MIN) + " requires 2 arguments", polyad.getArgAt(2));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                objects = castToJavaValues(objects);
                if (!areAllNumbers(objects)) {
                    Statement s = null;
                    if (!isNumber(objects[0])) {
                        s = polyad.getArgAt(0);
                    } else {
                        s = polyad.getArgAt(1);
                    }
                    // Contract is that if there are not numbers, just return the
                    // first argument unaltered.
                    throw new BadArgException((isMax ? MAX : MIN) + " requires numeric arguments", s);
                }
                if (areAllLongs(objects)) {
                    long first = (Long) objects[0];
                    long second = (Long) objects[1];
                    r.result = asQDLValue(isMax ? Math.max(first, second) : Math.min(first, second));
                    return r;
                }
                // so one of these is a big decimal at least
                int longValues = ((objects[0] instanceof Long) ? 1 : 0) + ((objects[1] instanceof Long) ? 2 : 0);
                // 0 = no longs, 1 = left long, 2 = right long 3 = both long

                boolean b0 = isBigDecimal(objects[0]);
                boolean b1 = isBigDecimal(objects[1]);
                BigDecimal bd0 = b0 ? (BigDecimal) objects[0] : new BigDecimal((Long) objects[0]);
                BigDecimal bd1 = b1 ? (BigDecimal) objects[1] : new BigDecimal((Long) objects[1]);

                BigDecimal bdr = null;
                try {
                    bdr = isMax ? bd0.max(bd1) : bd0.min(bd1);
                } catch (ArithmeticException ax1) {
                    throw new IllegalStateException("Could not compute " + (isMax ? MAX : MIN));
                }
                try {
                    if (longValues == 1 && bdr.equals(bd0)) {
                        r.result = asQDLValue(bdr.longValueExact());
                        return r;
                    }
                    if (longValues == 2 && bdr.equals(bd1)) {
                        r.result = asQDLValue(bdr.longValueExact());
                        return r;
                    }
                    r.result = asQDLValue(bdr);
                    return r;
                } catch (ArithmeticException ax) {
                }

                r.result = asQDLValue(bdr); // won't fit in a long, so this is really a BigInteger, which we don't support directly.
                return r;
            }
        };
        process2(polyad, pointer, isMax ? MAX : MIN, state);
    }

    public static boolean isIntegerValue(BigDecimal bd) {
        return bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }
}
