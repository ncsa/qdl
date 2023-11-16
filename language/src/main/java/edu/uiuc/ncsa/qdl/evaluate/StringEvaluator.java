package edu.uiuc.ncsa.qdl.evaluate;

import edu.uiuc.ncsa.qdl.exceptions.*;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.Statement;
import edu.uiuc.ncsa.qdl.variables.Constant;
import edu.uiuc.ncsa.qdl.variables.QDLSet;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.security.core.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import static edu.uiuc.ncsa.qdl.state.QDLConstants.*;

/**
 * This evaluates all string functions.
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  9:17 AM
 */

/*
Note that there is an inheritance hierarchy here with String being a super class of Math, etc.
This is driven by Java because it is better to have small classes that specialize in, say, String
functions rather than a massive single class. So the inheritence is just encapsulating the logic of this.
 */
public class StringEvaluator extends AbstractEvaluator {
    public static final String STRING_NAMESPACE = "string";

    @Override
    public String getNamespace() {
        return STRING_NAMESPACE;
    }

    public static final String STRING_FQ = STRING_NAMESPACE + State.NS_DELIMITER;
    public static final int STRING_FUNCTION_BASE_VALUE = 3000;

    public static final String CONTAINS = "contains";
    public static final int CONTAINS_TYPE = 1 + STRING_FUNCTION_BASE_VALUE;

    public static final String TO_LOWER = "to_lower";
    public static final int TO_LOWER_TYPE = 2 + STRING_FUNCTION_BASE_VALUE;

    public static final String TO_UPPER = "to_upper";
    public static final int TO_UPPER_TYPE = 3 + STRING_FUNCTION_BASE_VALUE;

    public static final String TRIM = "trim";
    public static final int TRIM_TYPE = 4 + STRING_FUNCTION_BASE_VALUE;

    public static final String INSERT = "insert";
    public static final int INSERT_TYPE = 5 + STRING_FUNCTION_BASE_VALUE;

    public static final String SUBSTRING = "substring";
    public static final int SUBSTRING_TYPE = 6 + STRING_FUNCTION_BASE_VALUE;

    public static final String REPLACE = "replace";
    public static final int REPLACE_TYPE = 7 + STRING_FUNCTION_BASE_VALUE;

    public static final String INDEX_OF = "index_of";
    public static final int INDEX_OF_TYPE = 8 + STRING_FUNCTION_BASE_VALUE;

    public static final String TOKENIZE = "tokenize";
    public static final int TOKENIZE_TYPE = 9 + STRING_FUNCTION_BASE_VALUE;


    public static final String DETOKENIZE = "detokenize";
    public static final int DETOKENIZE_TYPE = 12 + STRING_FUNCTION_BASE_VALUE;

    public static final String TO_URI = "to_uri";
    public static final int TO_URI_TYPE = 13 + STRING_FUNCTION_BASE_VALUE;

    public static final String FROM_URI = "from_uri";
    public static final int FROM_URI_TYPE = 14 + STRING_FUNCTION_BASE_VALUE;

    public static final String CAPUT = "head";
    public static final int CAPUT_TYPE = 15 + STRING_FUNCTION_BASE_VALUE;

    public static final String DIFF = "differ_at";
    public static final int DIFF_TYPE = 16 + STRING_FUNCTION_BASE_VALUE;

    public static final String TAIL = "tail";
    public static final int TAIL_TYPE = 17 + STRING_FUNCTION_BASE_VALUE;


    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{
                    CONTAINS,
                    TAIL,
                    DIFF,
                    TO_LOWER,
                    TO_UPPER,
                    TRIM,
                    INSERT,
                    SUBSTRING,
                    REPLACE,
                    INDEX_OF,
                    CAPUT,
                    TOKENIZE,
                    DETOKENIZE,
                    TO_URI,
                    FROM_URI};
        }
        return fNames;
    }

    @Override
    public int getType(String name) {
        switch (name) {
            case CONTAINS:
                return CONTAINS_TYPE;
            case TO_LOWER:
                return TO_LOWER_TYPE;
            case TO_UPPER:
                return TO_UPPER_TYPE;
            case SUBSTRING:
                return SUBSTRING_TYPE;
            case REPLACE:
                return REPLACE_TYPE;
            case TRIM:
                return TRIM_TYPE;
            case INSERT:
                return INSERT_TYPE;
            case INDEX_OF:
                return INDEX_OF_TYPE;
            case CAPUT:
                return CAPUT_TYPE;
            case TAIL:
                return TAIL_TYPE;
            case TOKENIZE:
                return TOKENIZE_TYPE;
            case DETOKENIZE:
                return DETOKENIZE_TYPE;
            case TO_URI:
                return TO_URI_TYPE;
            case FROM_URI:
                return FROM_URI_TYPE;
            case DIFF:
                return DIFF_TYPE;
        }
        return UNKNOWN_VALUE;
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
            case CONTAINS:
                doContains(polyad, state);
                return true;
            case TRIM:
                doTrim(polyad, state);
                return true;
            case CAPUT:
                doCaput(polyad, state);
                return true;
            case TAIL:
                doTail(polyad, state);
                return true;
            case INDEX_OF:
                doIndexOf(polyad, state);
                return true;
            case TO_LOWER:
                doSwapCase(polyad, state, true);
                return true;
            case TO_UPPER:
                doSwapCase(polyad, state, false);
                return true;
            case REPLACE:
                doReplace(polyad, state);
                return true;
            case INSERT:
                doInsert(polyad, state);
                return true;
            case TOKENIZE:
                doTokenize(polyad, state);
                return true;
            case DETOKENIZE:
                doDetokeninze(polyad, state);
                return true;
            case SUBSTRING:
                doSubstring(polyad, state);
                return true;
            case TO_URI:
                doToURI(polyad, state);
                return true;
            case FROM_URI:
                doFromURI(polyad, state);
                return true;
            case DIFF:
                doDiff(polyad, state);
                return true;
        }
        return false;
    }

    //       tail('a d, m,\ti.n','\\s+|,|\\s*', true);
    //       tail('a d, m,\ti.n','\\s+', true);
    //       head('a\t\t d, \tm,\ti.n','\\s+', true);
    //   tail('qweaAzxc', '[aA]*,', true)
    protected void doTail(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(TAIL + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(TAIL + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                int pos = -1;
                boolean isRegEx = false;
                if (objects.length == 3) {
                    if (!(objects[2] instanceof Boolean)) {
                        throw new BadArgException("if the 3rd argument is given, it must be a boolean.", polyad.getArgAt(2));
                    }
                    isRegEx = (Boolean) objects[2];
                }

                if (areAllStrings(objects[0], objects[1])) {
                    String s0 = (String) objects[0];
                    String s1 = (String) objects[1];
                    if (isRegEx) {
                        String[] x = s0.split(s1);
                        if (x == null || x.length == 0) {
                            r.result = "";
                        } else {
                            r.result = x[x.length - 1];
                        }
                    } else {
                        pos = s0.lastIndexOf(s1);
                        if (pos < 0) {
                            // not found
                            r.result = "";
                        } else {
                            r.result = s0.substring(pos + s1.length());
                        }
                    }
                }
                r.resultType = Constant.STRING_TYPE;
                return r;
            }
        };
        process2(polyad, pointer, TAIL, state, true);

    }

    protected void doDiff(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(DIFF + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(DIFF + " requires 2 arguments", polyad.getArgAt(2));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                int pos = -1;
                boolean caseSensitive = true;

                if (areAllStrings(objects[0], objects[1])) {
                    String s0 = (String) objects[0];
                    String s1 = (String) objects[1];
                    if (StringUtils.isTrivial(s0) || StringUtils.isTrivial(s1)) {
                        r.result = 0L;
                        r.resultType = Constant.LONG_TYPE;
                        return r;
                    }
                    char[] b0 = s0.toCharArray();
                    char[] b1 = s1.toCharArray();
                    int stop = Math.min(b0.length, b1.length);
                    for (int i = 0; i < stop; i++) {
                        if (b0[i] != b1[i]) {
                            r.result = (long) i;
                            r.resultType = Constant.LONG_TYPE;
                            return r;
                        }
                    }
                    if (b0.length == stop && b1.length == stop) {
                        r.result = -1L;
                        r.resultType = Constant.LONG_TYPE;
                        return r;
                    }
                    r.result = (long) stop;
                    r.resultType = Constant.LONG_TYPE;
                    return r;
                }
                Statement s = isString(objects[0]) ? polyad.getArgAt(0) : polyad.getArgAt(1);
                throw new BadArgException(DIFF + " requires both argument be strings.", s);
            }
        };
        process2(polyad, pointer, DIFF, state, false);
    }

    protected void doCaput(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(CAPUT + " requires 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(CAPUT + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                int pos = -1;
                boolean isRegEx = false;
                if (objects.length == 3) {
                    if (!(objects[2] instanceof Boolean)) {
                        throw new BadArgException("if the 3rd argument is given, it must be a boolean.", polyad.getArgAt(2));
                    }
                    isRegEx = (Boolean) objects[2];
                }

                if (areAllStrings(objects[0], objects[1])) {
                    String s0 = (String) objects[0];
                    String s1 = (String) objects[1];
                    if (isRegEx) {
                        String[] x = s0.split(s1);
                        if (x.length == 1) {
                            // no match
                            r.result = "";
                        } else {
                            r.result = x[0];
                        }
                    } else {
                        pos = s0.indexOf(s1);
                        if (pos < 0) {
                            // not found
                            r.result = "";
                        } else {
                            r.result = s0.substring(0, pos);
                        }
                    }

                }
                r.resultType = Constant.STRING_TYPE;
                return r;
            }
        };
        process2(polyad, pointer, CAPUT, state, true);

    }

    private void doFromURI(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(FROM_URI + " requires  1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(FROM_URI + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object object = polyad.evalArg(0, state);
        if (!isStem(object)) {
            throw new BadArgException(FROM_URI + " requires a stem as its argument", polyad.getArgAt(0));
        }

        QDLStem s = (QDLStem) object;
        try {
            Long port = s.getLong("port");

            URI uri = new URI(s.getString(URI_SCHEME),
                    s.getString(URI_USER_INFO),
                    s.getString(URI_HOST),
                    port.intValue(),
                    s.getString(URI_PATH),
                    s.getString(URI_QUERY),
                    s.getString(URI_FRAGMENT));
            polyad.setResult(uri.toString());
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setEvaluated(Boolean.TRUE);
        } catch (URISyntaxException usx) {
            throw new BadArgException("not a valid uri", polyad.getArgAt(0));
        }
    }

    /**
     * Turn a string into a parsed uri.
     *
     * @param polyad
     * @param state
     */
    private void doToURI(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(TO_URI + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(TO_URI + " requires at most 1 argument", polyad.getArgAt(1));
        }
        Object object = polyad.evalArg(0, state);
        if (!isString(object)) {
            throw new BadArgException(TO_URI + " requires a string as its argument", polyad.getArgAt(0));
        }
        try {
            URI uri = URI.create(object.toString());
            QDLStem output = new QDLStem();
            putURIAttrib(output, URI_AUTHORITY, uri.getAuthority());
            putURIAttrib(output, URI_FRAGMENT, uri.getFragment());
            putURIAttrib(output, URI_HOST, uri.getHost());
            putURIAttrib(output, URI_PATH, uri.getPath());
            putURIAttrib(output, URI_QUERY, uri.getQuery());
            putURIAttrib(output, URI_SCHEME_SPECIFIC_PART, uri.getSchemeSpecificPart());
            putURIAttrib(output, URI_SCHEME, uri.getScheme());
            putURIAttrib(output, URI_USER_INFO, uri.getUserInfo());
            output.put(URI_PORT, new Long(uri.getPort()));
            polyad.setResult(output);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setEvaluated(Boolean.TRUE);
            return;
        } catch (Throwable t) {
            throw new BadArgException("'" + object + "' is not a valid uri: " + t.getMessage(), polyad.getArgAt(0));
        }

    }


    void putURIAttrib(QDLStem s, String key, String value) {
        if (StringUtils.isTrivial(value)) {
            return;
        }
        s.put(key, value);
    }

    public static final Long DETOKENIZE_PREPEND_VALUE = 1L;
    public static final Long DETOKENIZE_OMIT_DANGLING_DELIMITER_VALUE = 2L;

    /*
    Change a stem into a string with each value separated by a delimiter. Note that
    in lists, the order is preserved but in general stems there is no canonical order.
     */
    protected void doDetokeninze(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(DETOKENIZE + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(DETOKENIZE + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        Object leftArg = polyad.evalArg(0, state);
        Object rightArg = polyad.evalArg(1, state);
        boolean isPrepend = false;
        boolean omitDanglingDelimiter = true;
        if (polyad.getArgCount() == 3) {
            Object prepend = polyad.evalArg(2, state);
            if (!isLong(prepend)) {
                throw new BadArgException("the third argument for " + DETOKENIZE + " must be a n integer. You supplied '" + prepend + "'", polyad.getArgAt(2));
            }
            int options = ((Long) prepend).intValue();
            switch (options) {
                case 0:
                    isPrepend = false;
                    omitDanglingDelimiter = false;
                    break;
                case 1:  //DETOKENIZE_PREPEND_VALUE
                    isPrepend = true;
                    omitDanglingDelimiter = false;
                    break;

                case 2:
                    isPrepend = false;
                    omitDanglingDelimiter = true;
                    break;

                case 3: // DETOKENIZE_PREPEND_VALUE + DETOKENIZE_OMIT_DANGLING_DELIMITER_VALUE
                    isPrepend = true;
                    omitDanglingDelimiter = true;
                    break;


            }
        }
        String result = "";

        if (isSet(leftArg)) {
            leftArg = ((QDLSet) leftArg).toStem();
        }
        if (isStem(leftArg)) {
            QDLStem leftStem = (QDLStem) leftArg;
            int lsize = leftStem.size();
            int currentCount = 0;

            if (isStem(rightArg)) {
                QDLStem rightStem = (QDLStem) rightArg;
                for (Object key : leftStem.keySet()) {
                    if (rightStem.containsKey(key)) {
                        String delim = "";

                        if (isPrepend) {
                            if (omitDanglingDelimiter && currentCount == 0) {
                                result = String.valueOf(leftStem.get(key));
                            } else {
                                result = result + rightStem.get(key) + leftStem.get(key);
                            }
                        } else {
                            if (omitDanglingDelimiter && currentCount == lsize - 1) {

                                result = result + leftStem.get(key);
                            } else {

                                result = result + leftStem.get(key) + rightStem.get(key);
                            }

                        }
                    }
                    currentCount++;
                }
            } else {
                // propagate the right arg as delimiter everywhere.

                for (Object key : leftStem.keySet()) {
                    if (isPrepend) {
                        if (omitDanglingDelimiter && currentCount == 0) {

                            result = String.valueOf(leftStem.get(key));
                        } else {
                            result = result + rightArg + leftStem.get(key);

                        }

                    } else {
                        if (omitDanglingDelimiter && currentCount == lsize - 1) {
                            result = result + leftStem.get(key);
                        } else {
                            result = result + leftStem.get(key) + rightArg;
                        }
                    }
                    currentCount++;
                }
            }

        } else {
            if (isStem(rightArg)) {
                throw new BadArgException("a stem of delimiters cannot be applied to a scalar.", polyad.getArgAt(1));
            }
            if (omitDanglingDelimiter) {
                result = leftArg.toString();
            } else {
                if (isPrepend) {
                    result = rightArg.toString() + leftArg.toString();
                } else {
                    result = leftArg.toString() + rightArg.toString();
                }
            }

        }
        polyad.setResult(result);
        polyad.setResultType(Constant.STRING_TYPE);
        polyad.setEvaluated(true);

    }


    protected void doSubstring(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 2, 3, 4});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(SUBSTRING + " requires at least 1 argument", polyad);
        }

        if (4 < polyad.getArgCount()) {
            throw new ExtraArgException(SUBSTRING + " requires at most 4 arguments", polyad.getArgAt(4));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult result = new fpResult();
                if (!isString(objects[0])) {
                    throw new BadArgException("The first argument to " + SUBSTRING + " must be a string.", polyad.getArgAt(0));
                }
                String arg = objects[0].toString();
                if (!isLong(objects[1])) {
                    throw new BadArgException("The second argument to " + SUBSTRING + " must be an integer.", polyad.getArgAt(1));
                }
                int n = ((Long) objects[1]).intValue();
                int length = arg.length(); // default
                String padding = null;
                if (2 < objects.length) {
                    if (!isLong(objects[2])) {
                        throw new BadArgException("The third argument to " + SUBSTRING + " must be an integer.", polyad.getArgAt(2));
                    }
                    length = ((Long) objects[2]).intValue();
                }
                if (3 < objects.length) {
                    if (!isString(objects[3])) {
                        throw new BadArgException("The fourth argument to " + SUBSTRING + " must be a string.", polyad.getArgAt(3));
                    }
                    padding = objects[3].toString();
                }
                String r;
                if (padding == null) {
                    r = arg.substring(n, Math.min(n + length, arg.length())); // the Java way looks for end index, not length
                } else {
                    r = arg.substring(n, Math.min(length, arg.length())); // the Java way
                    if (r.length() < length) {
                        StringBuffer stringBuffer = new StringBuffer();
                        for (int i = 0; i < 1 + (length - r.length()) / padding.length(); i++) {
                            stringBuffer.append(padding);// This appends the padding so later we can extend it cyclically
                        }
                        // If the padding is long, then there will be too much, only take what the user requests
                        r = r + stringBuffer.toString().substring(0, length - r.length());
                    }
                }

                result.result = r;
                result.resultType = Constant.STRING_TYPE;

                return result;
            }
        };
        process2(polyad, pointer, SUBSTRING, state, true);
    }

    protected void doTrim(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(TRIM + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(TRIM + " requires at most 1 argument", polyad.getArgAt(1));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (objects[0] instanceof String) {
                    r.result = objects[0].toString().trim();
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArgAt(0).getResultType();
                }
                return r;
            }
        };
        process1(polyad, pointer, TRIM, state);
    }

    protected void doIndexOf(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(INDEX_OF + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(INDEX_OF + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();

                boolean caseSensitive = true;
                if (objects.length == 3) {
                    if (!(objects[2] instanceof Boolean)) {
                        throw new BadArgException("if the 3rd argument is given, it must be a boolean.", polyad.getArgAt(2));
                    }
                    caseSensitive = (Boolean) objects[2];
                }
                QDLStem outStem = new QDLStem();
                if (areAllStrings(objects[0], objects[1])) {
                    String haystack = (String) objects[0];
                    String needle = (String) objects[1];
                    if (!caseSensitive) {
                        needle = needle.toLowerCase();
                        haystack = haystack.toLowerCase();
                    }
                    int indexOf = haystack.indexOf(needle);
                    int index = 0; // index in resulting list
                    outStem.put(index++, new Long(indexOf));
                    indexOf = indexOf + needle.length();
                    while (-1 < indexOf) {
                        indexOf = haystack.indexOf(needle, indexOf);
                        if (-1 < indexOf) {
                            outStem.put(index++, new Long(indexOf));
                            indexOf = indexOf + needle.length();
                        }
                    }
                    //pos = new Long(objects[0].toString().indexOf(objects[1].toString()));
                } else {
                    outStem.put(0, -1L); // non-strings are never found. Default is always -1.
                }
                r.result = outStem;
                r.resultType = Constant.STEM_TYPE;

                return r;
            }
        };
        process2(polyad, pointer, INDEX_OF, state, true);
    }

    /*
      subset((x)->index_of(x, 'x_').0==0, z.)
 index_of(z., 'x_')
     */
    protected void doContains(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(CONTAINS + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(CONTAINS + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (areAllStrings(objects[0], objects[1])) {
                    boolean caseSensitive = true;
                    if (objects.length == 3) {
                        if (!(objects[2] instanceof Boolean)) {
                            throw new BadArgException("if the 3rd argument is given, it must be a boolean.", polyad.getArgAt(2));
                        }
                        caseSensitive = (Boolean) objects[2];
                    }
                    if (caseSensitive) {
                        r.result = objects[0].toString().contains(objects[1].toString());
                    } else {
                        r.result = objects[0].toString().toLowerCase().contains(objects[1].toString().toLowerCase());
                    }
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = Boolean.FALSE;
                    r.resultType = Constant.BOOLEAN_TYPE;
                }

                return r;
            }
        };
        process2(polyad, pointer, CONTAINS, state, true);

    }


    protected void doSwapCase(Polyad polyad, State state, boolean isLower) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException((isLower ? TO_LOWER : TO_UPPER) + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException((isLower ? TO_LOWER : TO_UPPER) + " requires at most 1 argument", polyad.getArgAt(1));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (objects[0] instanceof String) {
                    if (isLower) {
                        r.result = objects[0].toString().toLowerCase();
                    } else {
                        r.result = objects[0].toString().toUpperCase();
                    }
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArgAt(0).getResultType();
                }
                return r;
            }
        };
        process1(polyad, pointer, isLower ? TO_LOWER : TO_UPPER, state);
    }

    /**
     * Do replace where there is an arbitrary stem and a stem of replacements and regexes
     * The keys for the replacements and regexes correspond, and the replacements happen
     * to <i><b>every</b></i> elements if inStem, the target of the replacement.
     *
     * @param polyad
     * @param inStem
     * @param replacements
     * @param regexStem
     * @param state
     * @return
     */
    protected QDLStem doReplace(Polyad polyad, QDLStem inStem, QDLStem replacements, QDLStem regexStem, State state) {
        QDLStem outStem = new QDLStem();
        for (Object key : inStem.keySet()) {
            Object o = inStem.get(key);
            if (o instanceof String) {
                outStem.putLongOrString(key, doStringReplace((String) o, replacements, regexStem));
            } else {
                if (o instanceof QDLStem) {
                    outStem.putLongOrString(key, doReplace(polyad, (QDLStem) o, replacements, regexStem, state));
                } else {
                    if(o instanceof QDLSet){
                        outStem.putLongOrString(key, doReplace(polyad, (QDLSet) o, replacements, regexStem, state));
                    } else {
                        outStem.putLongOrString(key, o);// pass it back unchanged
                    }
                }
            }
        }
        return outStem;
    }
    protected QDLSet doReplace(Polyad polyad, QDLSet inSet, QDLStem replacements, QDLStem regexStem, State state) {
        QDLSet outSet = new QDLSet();
        for (Object o : inSet) {
            if (o instanceof String) {
                outSet.add(doStringReplace((String) o, replacements, regexStem));
            } else {
                if (o instanceof QDLStem) {
                    outSet.add(doReplace(polyad, (QDLStem) o, replacements, regexStem, state));
                } else {
                    if(o instanceof QDLSet){
                        outSet.add(doReplace(polyad, (QDLSet) o, replacements, regexStem, state));
                    }else {
                        outSet.add(o);// pass it back unchanged
                    }
                }
            }
        }
        return outSet;
    }

    protected String doStringReplace(String s, QDLStem replacements, QDLStem regexStem) {
        for (Object key : replacements.keySet()) {
            if (key instanceof Long) {
                continue;
            }
            boolean isRegex = false; // default
            if (regexStem.containsKey(key)) {
                Object r = regexStem.get(key);
                if (r instanceof Boolean) {
                    isRegex = (Boolean) r;
                }
            }
            if (isRegex) {
                s = s.replaceAll((String) key, replacements.getString((String) key));
            } else {
                s = s.replace((String) key, replacements.getString((String) key));
            }
        }
        return s;
    }

    protected void doOldReplace(Polyad polyad, State state) {
        // Keep since this acts like every other QDL function
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                boolean doregex = false;
                if (objects.length == 4) {
                    if (!isBoolean(objects[3])) {
                        throw new BadArgException("replace requires a boolean as its 4th argument", polyad.getArgAt(3));
                    }
                    doregex = (Boolean) objects[3];
                }
                if (areAllStrings(objects[0], objects[1], objects[2])) {
                    if (doregex) {
                        r.result = objects[0].toString().replaceAll(objects[1].toString(), objects[2].toString());
                    } else {
                        r.result = objects[0].toString().replace(objects[1].toString(), objects[2].toString());
                    }
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArgAt(0).getResultType();
                }
                return r;
            }
        };
        process3(polyad, pointer, REPLACE, state, true);

    }

    protected void doReplace(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3, 4});
            polyad.setEvaluated(true);
            return;
        }
        Object r1 = polyad.evalArg(1, state);
        // Old version, replace(a., b., c.) had b. and c. as lists.
        // new version allows for b. to be a general stem. If c. is present, then
        // it is assumed to be a stem of booleans with true meaning to do regexes
        if (r1 instanceof String || polyad.getArgCount() == 4) {
            doOldReplace(polyad, state);
            return;
        }

        QDLStem arg1 = getReplaceArg1(polyad, state, r1);


        // So we are now in the new case that the second argument should be a stem with
        // keys that are old values.

        // at most 3 arguments
        QDLStem regexStem;
        if (polyad.getArgCount() == 2) {
            regexStem = new QDLStem();
            regexStem.setDefaultValue(Boolean.FALSE);
        } else {
            // arg count can only be 3 if not 2
            Object r2 = polyad.evalArg(2, state);
            if (r2 instanceof Boolean) {
                regexStem = new QDLStem();
                regexStem.setDefaultValue(r2);
            } else {
                if (r2 instanceof QDLStem) {
                    regexStem = (QDLStem) r2;
                } else {
                    throw new BadArgException(REPLACE + " requires a boolean or stem of them as the final argument", polyad.getArgAt(2));
                }
            }
        }
        QDLStem inStem;
        boolean isScalar = false;
        Object r = polyad.evalArg(0, state);
        if (r instanceof String) {
            inStem = new QDLStem();
            inStem.put(0L, r);
            isScalar = true;
        } else {
            if (r instanceof QDLStem) {
                isScalar = false;
                inStem = (QDLStem) r;
            } else {
                if(r instanceof QDLSet){
                    polyad.setResult(doReplace(polyad, (QDLSet) r, arg1, regexStem, state));
                    polyad.setResultType(Constant.SET_TYPE);
                    polyad.setEvaluated(true);
                    return;
                }
                // Not a string or a stem, so just return it.
                polyad.setEvaluated(true);
                polyad.setResult(r);
                polyad.setResultType(Constant.getType(r));
                return;
            }
        }

        QDLStem outStem = doReplace(polyad, inStem, arg1, regexStem, state);
        polyad.setEvaluated(true);
        if (isScalar) {
            polyad.setResult(outStem.get(0L));
            polyad.setResultType(Constant.getType(polyad.getResult()));
        } else {
            polyad.setResult(outStem);
            polyad.setResultType(Constant.STEM_TYPE);
        }
        return;
    }

    /**
     * The logic of the replace calls for possibly lists or a stem and it all collides with arg 1.
     * This keeps all the logic for this separate.
     * @param polyad
     * @param state
     * @param r1
     * @return
     */
    private  QDLStem getReplaceArg1(Polyad polyad, State state, Object r1) {
        QDLStem arg1 = null;
        if (r1 instanceof QDLStem) {
            if (((QDLStem) r1).isList()) {
                QDLStem a = (QDLStem) r1;
                if (polyad.getArgCount() == 2) {
                    throw new BadArgException("Missing argument. If you supply a list of replacements, you need a list of new values", polyad.getArgAt(1));
                }
                // case is that r1 is a list.
                Object r2 = polyad.evalArg(2, state);
                QDLStem b;
                if (r2 instanceof String) {
                    b = new QDLStem();
                    b.setDefaultValue(r2);
                } else {
                    if (r2 instanceof QDLStem) {
                        b = (QDLStem) r2;
                        if (!b.isList()) {
                            throw new BadArgException("The third argument must be a list", polyad.getArgAt(2));
                        }
                    } else {
                        throw new BadArgException("The third argument must be a list", polyad.getArgAt(2));
                    }
                    arg1 = new QDLStem();
                    for (Object key : a.keySet()) {
                        Object newKey = a.get(key);
                        if (newKey instanceof String) {
                            if (b.hasDefaultValue() || b.containsKey(key)) {
                                Object newValue = b.get(key);
                                arg1.put((String) newKey, newValue);
                            }
                        }
                    }
                }


            } else {
                arg1 = (QDLStem) r1;
            }
        }
        return arg1;
    }


    protected void doInsert(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 3) {
            Statement s = polyad;
            switch (polyad.getArgCount()) {
                case 1:
                    s = polyad.getArgAt(0);
                    break;
                case 2:
                    s = polyad.getArgAt(1);
                    break;
            }
            throw new MissingArgException(INSERT + " requires at least 3 arguments", s);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(INSERT + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (areAllStrings(objects[0], objects[1]) && areAllLongs(objects[2])) {
                    int index = ((Long) objects[2]).intValue();
                    String src = (String) objects[0];
                    String snippet = (String) objects[1];
                    r.result = src.substring(0, index) + snippet + src.substring(index);
                    r.resultType = Constant.STRING_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArgAt(0).getResultType();
                }
                return r;
            }
        };
        process3(polyad, pointer, INSERT, state, false);
    }

    protected void doTokenize(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(TOKENIZE + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(TOKENIZE + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        // contract is tokenize(string, delimiter) returns stem of tokens
        // tokenize(stem, delimiter) returns a list whose elements are stems of tokens.
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (areAllStrings(objects[0], objects[1])) {
                    boolean doRegex = false;
                    if (objects.length == 3) {
                        if (isBoolean(objects[2])) {
                            doRegex = (Boolean) objects[2];
                        }
                    }
                    QDLStem outStem = new QDLStem();
                    if (doRegex) {
                        String[] tokens = objects[0].toString().split(objects[1].toString());
                        for (int i = 0; i < tokens.length; i++) {
                            outStem.put(Integer.toString(i), tokens[i]);
                        }
                    } else {
                        StringTokenizer st = new StringTokenizer(objects[0].toString(), objects[1].toString());
                        int i = 0;
                        while (st.hasMoreTokens()) {
                            outStem.put(Integer.toString(i++), st.nextToken());
                        }

                    }
                    r.result = outStem;
                    r.resultType = Constant.STEM_TYPE;
                } else {
                    r.result = objects[0];
                    r.resultType = polyad.getArgAt(0).getResultType();
                }
                return r;
            }
        };
        process2(polyad, pointer, TOKENIZE, state, true);
    }
}
/*
a := 'a d, m, i.n'
r := '\\s+|,\\s*|\\.\\s*'
tokenize(a,r,true)
*/
