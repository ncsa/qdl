package edu.uiuc.ncsa.qdl.evaluate;

import edu.uiuc.ncsa.qdl.exceptions.*;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.Statement;
import edu.uiuc.ncsa.qdl.variables.Constant;
import edu.uiuc.ncsa.qdl.variables.QDLCodec;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import edu.uiuc.ncsa.security.core.util.TokenUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Locale;

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

/*
    public static final String ENCODE_B16 = "encode_b16";
    public static final int ENCODE_B16_TYPE = 5 + MATH_FUNCTION_BASE_VALUE;

    public static final String DECODE_B16 = "decode_b16";
    public static final int DECODE_B16_TYPE = 6 + MATH_FUNCTION_BASE_VALUE;

    public static final String ENCODE_B32 = "encode_b32";
    public static final int ENCODE_B32_TYPE = 17 + MATH_FUNCTION_BASE_VALUE;

    public static final String DECODE_B32 = "decode_b32";
    public static final int DECODE_B32_TYPE = 18 + MATH_FUNCTION_BASE_VALUE;
*/

    public static final String ENCODE = "encode";
    public static final int ENCODE_TYPE = 19 + MATH_FUNCTION_BASE_VALUE;

    public static final String DECODE = "decode";
    public static final int DECODE_TYPE = 20 + MATH_FUNCTION_BASE_VALUE;


    public static final String DATE_MS = "date_ms";
    public static final int DATE_MS_TYPE = 7 + MATH_FUNCTION_BASE_VALUE;

/*
    public static final String DECODE_B64 = "decode_b64";
    public static final int DECODE_B64_TYPE = 8 + MATH_FUNCTION_BASE_VALUE;

    public static final String ENCODE_B64 = "encode_b64";
    public static final int ENCODE_B64_TYPE = 9 + MATH_FUNCTION_BASE_VALUE;
*/

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
/*
                    ENCODE_B16,
                    DECODE_B16,
*/
                    DATE_MS,
                    DATE_ISO,
                    NUMERIC_DIGITS,
/*
                    DECODE_B64,
                    ENCODE_B64,
                    DECODE_B32,
                    ENCODE_B32,
*/
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
/*
            case ENCODE_B16:
                return ENCODE_B16_TYPE;
            case DECODE_B16:
                return DECODE_B16_TYPE;
            case ENCODE_B64:
                return ENCODE_B64_TYPE;
            case DECODE_B64:
                return DECODE_B64_TYPE;
            case ENCODE_B32:
                return ENCODE_B32_TYPE;
            case DECODE_B32:
                return DECODE_B32_TYPE;
*/
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
    public boolean evaluate(Polyad polyad, State state) {
        try {
            return evaluate2(polyad, state);
        } catch (QDLException q) {
            throw q;
        } catch (Throwable t) {
            QDLExceptionWithTrace qq = new QDLExceptionWithTrace(t, polyad);
            throw qq;
        }
    }

    public boolean evaluate2(Polyad polyad, State state) {

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

/*
            case ENCODE_B16:
                toFromhex(polyad, state, true);
                return true;
            case DECODE_B16:
                toFromhex(polyad, state, false);
                return true;
*/
            case NUMERIC_DIGITS:
                doNumericDigits(polyad, state);
                return true;
/*
            case ENCODE_B64:
                doB64(polyad, state, true);
                return true;
            case DECODE_B64:
                doB64(polyad, state, false);
                return true;
            case ENCODE_B32:
                doB32(polyad, state, true);
                return true;
            case DECODE_B32:
                doB32(polyad, state, false);
                return true;
*/
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
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException((isEncode ? ENCODE : DECODE) + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException((isEncode ? ENCODE : DECODE) + " requires at most 2 arguments", polyad.getArgAt(2));
        }
        final int type; // default for base 64.
        if (polyad.getArgCount() == 2) {
            Object obj = polyad.evalArg(1, state);
            if (!(obj instanceof Long)) {
                throw new BadArgException("the second argument must be an integer", polyad.getArgAt(1));
            }
            long lll = (Long)obj;
            type = (int)lll;
        } else {
            type = 64;
        }
        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                if (!(objects[0] instanceof String)) {
                    r.result = objects[0];
                    r.resultType = polyad.getArguments().get(0).getResultType();
                    return r;
                }
                String arg = objects[0].toString();
                switch (type) {
                    case 0:// vencode
                        QDLCodec codec = new QDLCodec();
                        if (isEncode) {
                            r.result = codec.encode(arg);
                        } else {
                            r.result = codec.decode(arg);
                        }
                        break;
                    case 1:// url encode
                        try {
                            if (isEncode) {
                                r.result = URLEncoder.encode(arg, "UTF-8");
                            } else {
                                r.result = URLDecoder.decode(arg, "UTF-8");
                            }
                        }catch(UnsupportedEncodingException unsupportedEncodingException){
                            throw new QDLException("internal error doing URL code:" + unsupportedEncodingException.getMessage());
                        }
                        break;

                    case 16:
                        if (isEncode) {
                            r.result = Hex.encodeHexString(arg.getBytes());
                        } else {
                            try {
                                byte[] decoded = Hex.decodeHex(arg.toCharArray());
                                r.result = new String(decoded);
                            } catch (Throwable t) {
                                r.result = "(error)";
                            }
                        }
                        break;
                    case 32:
                        if (isEncode) {
                            r.result = TokenUtil.b32EncodeToken(arg);
                        } else {
                            r.result = TokenUtil.b32DecodeToken(arg.toUpperCase(Locale.ROOT));
                        }
                        break;
                    case 64:
                        if (isEncode) {
                            r.result = Base64.encodeBase64URLSafeString(arg.getBytes());
                        } else {
                            r.result = new String(Base64.decodeBase64(arg));
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("unknown conversion type " + type);
                }
                r.resultType = Constant.getType(r.result);
                return r;
            }
        };
        if(polyad.getArgCount() == 1){
            process1(polyad, pointer, isEncode ? ENCODE : DECODE, state);
        }else{
            process2(polyad, pointer, isEncode ? ENCODE : DECODE, state,true);
        }


    }

/*
    protected void doB32(Polyad polyad, State state, boolean isEncode) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException((isEncode ? ENCODE_B64 : DECODE_B64) + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException((isEncode ? ENCODE_B64 : DECODE_B64) + " requires at most 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                if (objects[0] instanceof String) {
                    if (isEncode) {
                        r.result = TokenUtil.b32EncodeToken(objects[0].toString());
                    } else {
                        r.result = TokenUtil.b32DecodeToken(objects[0].toString().toUpperCase(Locale.ROOT));
                    }
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArguments().get(0).getResultType();
                }
                return r;
            }
        };
        process1(polyad, pointer, isEncode ? ENCODE_B64 : DECODE_B64, state);

    }
*/


    /**
     * The identity function returns its argument. Simple as that.
     *
     * @param polyad
     * @param state
     */
    protected void doIdentityFunction(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
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
        polyad.setResultType(polyad.getArgAt(0).getResultType());
        polyad.setEvaluated(true);

    }

    protected void doAbs(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
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
                switch (Constant.getType(objects[0])) {
                    case Constant.LONG_TYPE:
                        r.result = Math.abs((Long) objects[0]);
                        r.resultType = Constant.LONG_TYPE;
                        break;
                    case Constant.DECIMAL_TYPE:
                        BigDecimal bd = (BigDecimal) objects[0];
                        r.result = bd.abs();
                        r.resultType = Constant.DECIMAL_TYPE;
                        break;
                    default:
                        r.result = objects[0];
                        r.resultType = polyad.getArguments().get(0).getResultType();
                }

                return r;
            }
        };
        process1(polyad, pointer, ABS_VALUE, state);
    }

    SecureRandom secureRandom = new SecureRandom();

    protected void doRandom(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(RANDOM + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            polyad.setResult(secureRandom.nextLong());
            polyad.setResultType(Constant.LONG_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        Object result;
        int resultType = 0;

        // if the argument is a number return that many random numbers in a stem variable.
        Object arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0));
        if (arg instanceof Long) {
            int size = ((Long) arg).intValue();
            QDLStem stemVariable = new QDLStem();
            for (int i = 0; i < size; i++) {
                stemVariable.put(Integer.toString(i), secureRandom.nextLong());
            }
            result = stemVariable;
            resultType = Constant.STEM_TYPE;
        } else {
            // unknown type is ignored.
            throw new BadArgException(RANDOM + " requires a integer as its argument if present", polyad.getArgAt(0));
        }
        polyad.setResultType(resultType);
        polyad.setResult(result);
        polyad.setEvaluated(true);

    }

    protected void doNumericDigits(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(NUMERIC_DIGITS + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Long oldND = new Long((long) state.getOpEvaluator().getNumericDigits());
        polyad.setResult(oldND);
        polyad.setResultType(Constant.LONG_TYPE);

        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
        } else {
            Object arg1 = polyad.evalArg(0, state);
            checkNull(arg1, polyad.getArgAt(0));

            if (!isLong(arg1)) {
                throw new BadArgException(NUMERIC_DIGITS + " requires an integer argument", polyad.getArgAt(0));
            }
            Long newND = (Long) arg1;
            state.getOpEvaluator().setNumericDigits(newND.intValue());
            polyad.setEvaluated(true);
        }
        return;
    }

    protected void doRandomString(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(RANDOM_STRING + " requires at most 2 arguments", polyad.getArgAt(2));
        }

        int length = 16;

        if (0 < polyad.getArgCount()) {
            polyad.evalArg(0, state);
            Object obj = polyad.getArguments().get(0).getResult();
            checkNull(obj, polyad.getArgAt(0));

            if (obj instanceof Long) {
                length = ((Long) obj).intValue();
            } else {
                throw new BadArgException(RANDOM_STRING + " takes an integer as its first argument", polyad.getArgAt(0));

            }
        }
        // Second optional argument is number of strings.
        int returnCount = 1;
        if (polyad.getArgCount() == 2) {
            polyad.evalArg(1, state);
            Object obj = polyad.getArguments().get(1).getResult();
            checkNull(obj, polyad.getArgAt(1));// re-used varaible obj for arg #1

            if (!isLong(obj)) {
                throw new BadArgException(RANDOM_STRING + " takes an integer as its second argument.", polyad.getArgAt(1));
            }

            if (obj instanceof Long) {
                returnCount = ((Long) obj).intValue();
                if (returnCount <= 0) {
                    returnCount = 1;
                }
            }
        }


        byte[] ba = new byte[length];
        if (returnCount == 1) {
            secureRandom.nextBytes(ba);
            String rc = Base64.encodeBase64URLSafeString(ba);
            polyad.setResult(rc);
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        // so more than one string needs to be returned.

        QDLStem stem = new QDLStem();
        for (int i = 0; i < returnCount; i++) {
            secureRandom.nextBytes(ba);
            String rc = Base64.encodeBase64URLSafeString(ba);
            stem.put((long) i, rc);
        }
        polyad.setResult(stem);
        polyad.setResultType(Constant.STEM_TYPE);
        polyad.setEvaluated(true);
        return;
    }

    protected void doHash(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(HASH + " requires 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(HASH + " requires 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                if (objects[0] instanceof String) {
                    r.result = DigestUtils.sha1Hex(objects[0].toString());
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArguments().get(0).getResultType();
                }
                return r;
            }
        };
        process1(polyad, pointer, HASH, state);
    }

/*
    protected void doB64(Polyad polyad, State state, boolean isEncode) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException((isEncode ? ENCODE_B64 : DECODE_B64) + " requires 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException((isEncode ? ENCODE_B64 : DECODE_B64) + " requires at most 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                if (objects[0] instanceof String) {
                    if (isEncode) {
                        r.result = Base64.encodeBase64URLSafeString(objects[0].toString().getBytes());
                    } else {
                        r.result = new String(Base64.decodeBase64(objects[0].toString()));
                    }
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArguments().get(0).getResultType();
                }
                return r;
            }
        };
        process1(polyad, pointer, isEncode ? ENCODE_B64 : DECODE_B64, state);
    }

    protected void toFromhex(Polyad polyad, State state, boolean toHex) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException((toHex ? ENCODE_B16 : DECODE_B16) + " requires 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException((toHex ? ENCODE_B16 : DECODE_B16) + " requires at most 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                Long ll = null;

                if (objects[0] instanceof String) {
                    if (toHex) {
                        r.result = Hex.encodeHexString(objects[0].toString().getBytes());
                    } else {
                        try {
                            byte[] decoded = Hex.decodeHex(objects[0].toString().toCharArray());
                            r.result = new String(decoded);
                        } catch (Throwable t) {
                            r.result = "(error)";
                        }
                    }
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArguments().get(0).getResultType();
                }
                return r;
            }
        };
        process1(polyad, pointer, toHex ? ENCODE_B16 : DECODE_B16, state);
    }
*/

    /**
     * Compute the modulus of two numbers, i.e. the remainder after division
     *
     * @param polyad
     */
    protected void doModulus(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2});
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
                if (!areAllNumbers(objects)) {
                    // Contract is that if there are not numbers, just return the
                    // first argument unaltered.
                    r.result = objects[0];
                    r.resultType = polyad.getArguments().get(0).getResultType();
                    return r;
                }
                if (areAllLongs(objects)) {
                    long second = (Long) objects[1];
                    if (second == 0L) {
                        throw new BadArgException(MOD + " requires non-zero second argument", polyad.getArgAt(1));

                    }
                    r.result = ((Long) objects[0]) % second;
                    r.resultType = Constant.LONG_TYPE;
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
                    r.result = bdr.longValueExact();
                    r.resultType = Constant.LONG_TYPE;
                    return r;
                } catch (ArithmeticException ax) {
                }

                r.result = bdr; // won't fit in a long, so this is really a BigInteger, which we don't support directly.
                r.resultType = Constant.DECIMAL_TYPE;
                return r;
            }
        };
        process2(polyad, pointer, MOD, state);
    }

    protected void doDates(Polyad polyad, State state, boolean isInMillis) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
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
                polyad.setResultType(Constant.LONG_TYPE);
                polyad.setEvaluated(true);
            } else {
                Date d = new Date();
                String now = Iso8601.date2String(d);
                polyad.setResult(now);
                polyad.setResultType(Constant.STRING_TYPE);
                polyad.setEvaluated(true);

            }
            return;
        }
        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                if (isInMillis) {
                    if (isLong(objects[0])) {
                        // do nothing. hand it back.
                        r.resultType = Constant.LONG_TYPE;
                        r.result = objects[0];
                    } else {
                        // assume its and ISO 8601 date and should lbe converted to millis
                        try {
                            Long ts = Iso8601.string2Date(objects[0].toString()).getTimeInMillis();
                            r.resultType = Constant.LONG_TYPE;
                            r.result = ts;
                        } catch (Throwable t) {
                            r.result = objects[0];
                            r.resultType = polyad.getArguments().get(0).getResultType();
                        }
                    }
                } else {
                    // work with ISO 8601 dates
                    if (isLong(objects[0])) {
                        String now = Iso8601.date2String((Long) objects[0]);
                        r.result = now;
                        r.resultType = Constant.STRING_TYPE;
                        return r;
                    } else {
                        r.result = objects[0];
                        r.resultType = polyad.getArguments().get(0).getResultType();
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
            polyad.setResult(new int[]{2});
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
                    r.result = isMax ? Math.max(first, second) : Math.min(first, second);
                    r.resultType = Constant.LONG_TYPE;
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
                        r.result = bdr.longValueExact();
                        r.resultType = Constant.LONG_TYPE;
                        return r;
                    }
                    if (longValues == 2 && bdr.equals(bd1)) {
                        r.result = bdr.longValueExact();
                        r.resultType = Constant.LONG_TYPE;
                        return r;
                    }
                    r.result = bdr;
                    r.resultType = Constant.DECIMAL_TYPE;
                    return r;
                } catch (ArithmeticException ax) {
                }

                r.result = bdr; // won't fit in a long, so this is really a BigInteger, which we don't support directly.
                r.resultType = Constant.DECIMAL_TYPE;
                return r;
            }
        };
        process2(polyad, pointer, isMax ? MAX : MIN, state);
    }

    public static boolean isIntegerValue(BigDecimal bd) {
        return bd.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
    }
}
