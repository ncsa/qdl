package org.qdl_lang.evaluate;


import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.*;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionRecordInterface;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.types.Types;
import org.qdl_lang.variables.*;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.qdl_lang.variables.values.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static org.qdl_lang.types.Types.NULL;
import static org.qdl_lang.variables.Constant.*;
import static org.qdl_lang.variables.values.QDLValue.*;

/**
 * Class charged with evaluating algebraic expressions.
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:20 PM
 */
public class OpEvaluator extends AbstractEvaluator {
    // reference for unicode and other characters: https://en.wikipedia.org/wiki/Mathematical_operators_and_symbols_in_Unicode
    public static final String AND = "&&";
    public static final String AND2 = "∧"; // unicode 2227
    public static final String ASSIGNMENT = ":=";
    public static final String CEILING = "⌈";  // unicode 2308
    public static final String DIVIDE = "/";
    public static final String DIVIDE2 = "÷"; // unicode f7
    public static final String EQUALS = "==";
    public static final String EQUALS2 = "≡";  // unicode 2261
    public static final String EXCISE = "!~";
    public static final String FLOOR = "⌊";  // unicode 230a
    public static final String INTEGER_DIVIDE = "%";
    public static final String SYMMETRIC_DIFFERENCE = "∆"; // unicode 2206
    public static final String LESS_THAN = "<";
    public static final String LESS_THAN_EQUAL = "<=";
    public static final String LESS_THAN_EQUAL2 = "=<";
    public static final String LESS_THAN_EQUAL3 = "≤"; // unicode 2264
    public static final String MINUS = "-";
    public static final String MINUS2 = "¯";  // unicode 00af
    public static final String MINUS_MINUS = "--";
    public static final String MORE_THAN = ">";
    public static final String MORE_THAN_EQUAL = ">=";
    public static final String MORE_THAN_EQUAL2 = "=>";
    public static final String MORE_THAN_EQUAL3 = "≥"; // unicode 2265
    public static final String NOT = "!";
    public static final String NOT2 = "¬"; // unicode ac
    public static final String NOT_EQUAL = "!=";
    public static final String NOT_EQUAL2 = "≠"; // unicode 2260
    public static final String OR = "||";
    public static final String OR2 = "∨"; // unicode 2228
    public static final String PLUS = "+";
    public static final String PLUS2 = "⁺"; // unciode 207a unary plus
    public static final String PLUS_PLUS = "++";
    public static final String POWER = "^";
    public static final String NROOT = "√";
    public static final String UNION = "∪"; //unicode 2229
    public static final String UNION_2 = "\\/";
    public static final String INTERSECTION = "∩"; //unicode 222a
    public static final String INTERSECTION_2 = "/\\";
    public static final String TILDE = "~";
    public static final String TILDE_STILE = "~|";
    public static final String TILDE_STILE2 = "≁"; // unicode 2241
    public static final String TIMES = "*";
    public static final String TIMES2 = "×"; // unicode d7
    public static final String DOT = "."; // for stems
    public static final String REGEX_MATCH = "=~";
    public static final String REGEX_MATCH2 = "≈";
    public static final String TO_SET = "⊢";
    public static final String TO_SET2 = "|^";
    public static final String EPSILON = "∈";
    public static final String EPSILON_NOT = "∉";
    public static final String IS_A = "<<";
    public static final String IS_DEFINED = "∃";
    public static final String IS_NOT_DEFINED = "∄";
    public static final String CONTAINS_KEY = "∋"; //2203
    public static final String NOT_CONTAINS_KEY = "∌"; //220c
    public static final String FOR_ALL_KEY = "∀"; //2200
    public static final String MASK_OP_KEY = "⌆"; // 2306
    public static final String TRANSPOSE_OP_KEY = "µ"; //00B5
    public static final String REDUCE_OP_KEY = "⊙"; //2299
    public static final String EXPAND_OP_KEY = "⊕"; //2295
    public static final String APPLY_OP_KEY = "∂"; // 	U+2202


    public static final int ASSIGNMENT_VALUE = 10;
    public static final int PLUS_VALUE = 100;
    public static final int MINUS_VALUE = 101;
    public static final int PLUS_PLUS_VALUE = 102;
    public static final int MINUS_MINUS_VALUE = 103;
    public static final int AND_VALUE = 200;
    public static final int OR_VALUE = 201;
    public static final int EQUALS_VALUE = 202;
    public static final int NOT_EQUAL_VALUE = 203;
    public static final int LESS_THAN_VALUE = 204;
    public static final int LESS_THAN_EQUAL_VALUE = 205;
    public static final int MORE_THAN_VALUE = 206;
    public static final int MORE_THAN_EQUAL_VALUE = 207;
    public static final int NOT_VALUE = 208;
    public static final int TIMES_VALUE = 209;
    public static final int DIVIDE_VALUE = 210;
    public static final int POWER_VALUE = 211;
    public static final int INTEGER_DIVIDE_VALUE = 212;
    public static final int TILDE_VALUE = 213;
    public static final int DOT_VALUE = 214;
    public static final int TILDE_STILE_VALUE = 215;
    public static final int REGEX_MATCH_VALUE = 216;
    public static final int UNION_VALUE = 217;
    public static final int INTERSECTION_VALUE = 218;
    public static final int FLOOR_VALUE = 219;
    public static final int CEILING_VALUE = 220;
    public static final int TO_SET_VALUE = 221;
    public static final int EPSILON_VALUE = 222;
    public static final int EPSILON_NOT_VALUE = 223;
    public static final int IS_A_VALUE = 224;
    public static final int IS_DEFINED_VALUE = 225;
    public static final int IS_NOT_DEFINED_VALUE = 226;
    public static final int CONTAINS_KEY_VALUE = 227;
    public static final int NOT_CONTAINS_KEY_VALUE = 228;
    public static final int FOR_ALL_KEY_VALUE = 229;

    public static final int MASK_OP_VALUE = 230; // 2306
    public static final int TRANSPOSE_OP_VALUE = 231; //00b5

    public static final int REDUCE_OP_VALUE = 232; //2a00
    public static final int EXPAND_OP_VALUE = 233; //2a01
    public static final int APPLY_OP_VALUE = 234; // 	U+2202
    public static final int NROOT_VALUE = 235; // 	U+221A
    public static final int EXCISE_VALUE = 236; // 	U+221A

    /**
     * This is for operators that are in the parser and never are created directly, e.g.
     * → or #. "->"
     */
    public static String[] OTHER_MATH_OPS = new String[]{
            "->", "→", State.NS_DELIMITER,
            "⊨",
            "|^", "⊢", "\\", QDLStem.STEM_INDEX_MARKER,
            "¿", "?",
            "≔", ":=", "≕", "=:", "+=", "-=", "^=", "*=", "~=", "×=", "÷=", "%=", "!~"
    };

    /**
     * All Math operators. These are used in function references.
     */
    public static String[] ALL_MATH_OPS = new String[]{
            CONTAINS_KEY, NOT_CONTAINS_KEY, FOR_ALL_KEY, MASK_OP_KEY, TRANSPOSE_OP_KEY,
            REDUCE_OP_KEY, EXPAND_OP_KEY,
            IS_DEFINED, IS_NOT_DEFINED,
            EPSILON, EPSILON_NOT, IS_A,
            TO_SET, TO_SET2,
            FLOOR, CEILING,
            UNION, UNION_2, INTERSECTION, INTERSECTION_2,
            POWER, NROOT,
            TILDE, TILDE_STILE, TILDE_STILE2,
            TIMES,
            TIMES2,
            DIVIDE,
            DIVIDE2,
            INTEGER_DIVIDE, SYMMETRIC_DIFFERENCE,
            PLUS, PLUS2,
            MINUS, MINUS2,
            AND, AND2,
            OR,
            OR2,
            EQUALS,
            EQUALS2,
            EXCISE,
            NOT_EQUAL,
            NOT_EQUAL2,
            LESS_THAN,
            LESS_THAN_EQUAL,
            LESS_THAN_EQUAL2,
            LESS_THAN_EQUAL3,
            MORE_THAN,
            MORE_THAN_EQUAL,
            MORE_THAN_EQUAL2,
            MORE_THAN_EQUAL3,
            NOT, NOT2,
            REGEX_MATCH, REGEX_MATCH2,
            APPLY_OP_KEY};

    public static ArrayList<String> ALL_MONADS = new ArrayList<>(Arrays.asList(new String[]{
            NOT, NOT2, MINUS, MINUS2, PLUS, PLUS2, TILDE, PLUS_PLUS, MINUS_MINUS, IS_DEFINED, IS_NOT_DEFINED, TRANSPOSE_OP_KEY, APPLY_OP_KEY
    }));
    public static ArrayList<String> ONLY_MONADS = new ArrayList<>(Arrays.asList(new String[]{
            NOT, NOT2, PLUS_PLUS, MINUS_MINUS, FLOOR, CEILING, TO_SET, TO_SET2, IS_DEFINED, IS_NOT_DEFINED
    }));

    int[] monadOnlyArg = new int[]{1};
    int[] dyadOnlyArg = new int[]{2};
    int[] monadAndDyadArg = new int[]{1, 2};

    /**
     * Return the arg counts for an operator.
     *
     * @param name
     * @return
     */
    public int[] getArgCount(String name) {
        // special case floor and ceiling since the parser intercepts them and replaces them with
        // their function.
        if (ONLY_MONADS.contains(name)) {
            return monadOnlyArg;
        }
        if (!ALL_MONADS.contains(name)) {
            return dyadOnlyArg;
        }
        return monadAndDyadArg;
    }


    @Override
    public String getNamespace() {
        throw new NotImplementedException("namespaces for operators not supported");
    }

    public boolean isMathOperator(String x) {
        return getType(x) != UNKNOWN_TYPE; // switch is faster than rumaging around
    }

    @Override
    public String[] getFunctionNames() {
        return new String[0];
    }

    static MathContext mathContext;

    public static MathContext getMathContext() {
        if (mathContext == null) {
            mathContext = new MathContext(getNumericDigits(), RoundingMode.HALF_EVEN);
        }
        return mathContext;
    }

    public static int getNumericDigits() {
        return numericDigits;
    }

    public static void setNumericDigits(int newNumericDigits) {
        numericDigits = newNumericDigits;
        mathContext = new MathContext(numericDigits);
        TMathEvaluator.setPi(null); // zero it out, force recompute at new precision
        TMathEvaluator.setNaturalLogBase(null);
    }

    public static int numericDigits = 15; // default precision for decimals.

    /**
     * No listing for these yet since they are not the standard func() pattern, e.g. a &lt; b.
     *
     * @return
     */
    public TreeSet<String> listFunctions(boolean listFQ) {
        TreeSet<String> names = new TreeSet<>();
        return names;
    }

    /**
     * Given an operator, this will return the integer value associated with it for lookups later.
     * {@link ExpressionNode}s store the value, not the operator.
     *
     * @param oo
     * @return
     */
    public int getType(String oo) {
        switch (oo) {
            case EXCISE:
                return EXCISE_VALUE;
            case EXPAND_OP_KEY:
                return EXPAND_OP_VALUE;
            case REDUCE_OP_KEY:
                return REDUCE_OP_VALUE;
            case MASK_OP_KEY:
                return MASK_OP_VALUE;
            case TRANSPOSE_OP_KEY:
                return TRANSPOSE_OP_VALUE;
            case FOR_ALL_KEY:
                return FOR_ALL_KEY_VALUE;
            case CONTAINS_KEY:
                return CONTAINS_KEY_VALUE;
            case NOT_CONTAINS_KEY:
                return NOT_CONTAINS_KEY_VALUE;
            case IS_DEFINED:
                return IS_NOT_DEFINED_VALUE;
            case IS_NOT_DEFINED:
                return IS_NOT_DEFINED_VALUE;
            case IS_A:
                return IS_A_VALUE;
            case EPSILON:
                return EPSILON_VALUE;
            case EPSILON_NOT:
                return EPSILON_NOT_VALUE;
            case TO_SET:
            case TO_SET2:
                return TO_SET_VALUE;
            case FLOOR:
                return FLOOR_VALUE;
            case CEILING:
                return CEILING_VALUE;
            case UNION:
            case UNION_2:
                return UNION_VALUE;
            case INTERSECTION:
            case INTERSECTION_2:
                return INTERSECTION_VALUE;
            case ASSIGNMENT:
                return ASSIGNMENT_VALUE;
            case AND:
            case AND2:
                return AND_VALUE;
            case EQUALS:
            case EQUALS2:
                return EQUALS_VALUE;
            case LESS_THAN:
                return LESS_THAN_VALUE;
            case LESS_THAN_EQUAL:
            case LESS_THAN_EQUAL2:
            case LESS_THAN_EQUAL3:
                return LESS_THAN_EQUAL_VALUE;
            case MINUS:
            case MINUS2:
                return MINUS_VALUE;
            case MORE_THAN:
                return MORE_THAN_VALUE;
            case MORE_THAN_EQUAL:
            case MORE_THAN_EQUAL2:
            case MORE_THAN_EQUAL3:
                return MORE_THAN_EQUAL_VALUE;
            case NOT_EQUAL:
            case NOT_EQUAL2:
                return NOT_EQUAL_VALUE;
            case OR:
            case OR2:
                return OR_VALUE;
            case PLUS:
            case PLUS2:
                return PLUS_VALUE;
            case POWER:
                return POWER_VALUE;
            case NROOT:
                return NROOT_VALUE;
            case TIMES:
            case TIMES2:
                return TIMES_VALUE;
            case DIVIDE:
            case DIVIDE2:
                return DIVIDE_VALUE;
            case INTEGER_DIVIDE:
            case SYMMETRIC_DIFFERENCE:
                return INTEGER_DIVIDE_VALUE;
            case MINUS_MINUS:
                return MINUS_MINUS_VALUE;
            case NOT:
            case NOT2:
                return NOT_VALUE;
            case PLUS_PLUS:
                return PLUS_PLUS_VALUE;
            case TILDE_STILE:
                return TILDE_STILE_VALUE;
            case TILDE:
                return TILDE_VALUE;
            case DOT:
                return DOT_VALUE;
            case REGEX_MATCH:
            case REGEX_MATCH2:
                return REGEX_MATCH_VALUE;
            case APPLY_OP_KEY:
                return APPLY_OP_VALUE;
        }
        return UNKNOWN_VALUE;
    }

    public void evaluate(Dyad dyad, State state) {
        try {
            evaluate2(dyad, state);
        } catch (QDLException q) {
            throw q;
        } catch (Throwable t) {
            QDLExceptionWithTrace qq = new QDLExceptionWithTrace(t, dyad);
            throw qq;
        }
    }

    public void evaluate2(Dyad dyad, State state) {
        switch (dyad.getOperatorType()) {
            case EXCISE_VALUE:
                doExcise(dyad, state);
                return;
            case REDUCE_OP_VALUE:
                doFRefDyadicOperator(dyad, SystemEvaluator.REDUCE, state);
                return;
            case EXPAND_OP_VALUE:
                doFRefDyadicOperator(dyad, SystemEvaluator.EXPAND, state);
                return;
            case MASK_OP_VALUE:
                doExpressionDyadOperator(dyad, StemEvaluator.MASK, state, true);
                return;
            case TRANSPOSE_OP_VALUE:
                doExpressionDyadOperator(dyad, StemEvaluator.TRANSPOSE, state, true);
                return;
            case FOR_ALL_KEY_VALUE:
                doForAll(dyad, state);
                return;
            case CONTAINS_KEY_VALUE:
                doContainsKey(dyad, state, true);
                return;
            case NOT_CONTAINS_KEY_VALUE:
                doContainsKey(dyad, state, false);
                return;
            case IS_DEFINED_VALUE:
                doIsDefinedDyad(dyad, state, true);
                return;
            case IS_NOT_DEFINED_VALUE:
                doIsDefinedDyad(dyad, state, false);
                return;
            case IS_A_VALUE:
                doIsA(dyad, state);
                return;
            case EPSILON_VALUE:
                doMembership(dyad, state, true);
                return;
            case EPSILON_NOT_VALUE:
                doMembership(dyad, state, false);
                return;
            case UNION_VALUE:
            case INTERSECTION_VALUE:
                doSetUnionOrInteresection(dyad, state);
                return;
            case POWER_VALUE:
                doPower(dyad, state);
                return;
            case NROOT_VALUE:
                doNroot(dyad, state);
                return;
            case TIMES_VALUE:
                doDyadTimesOrDivide(dyad, state, true);
                return;
            case INTEGER_DIVIDE_VALUE:
                doDyadIntegerDivide(dyad, state);
                return;
            case DIVIDE_VALUE:
                doDyadTimesOrDivide(dyad, state, false);
                return;
            case PLUS_VALUE:
                doDyadPlus(dyad, state);
                return;
            case TILDE_STILE_VALUE:
                doJoin(dyad, state);
                return;
            case TILDE_VALUE:
                doTilde(dyad, state);
                return;
            case MINUS_VALUE:
                doDyadMinus(dyad, state);
                return;
            case AND_VALUE:
            case OR_VALUE:
                doDyadLogicalOperator(dyad, state);
                return;
            case EQUALS_VALUE:
            case NOT_EQUAL_VALUE:
                doDyadEqualsOperator(dyad, state);
                return;
            case LESS_THAN_VALUE:
            case MORE_THAN_VALUE:
            case LESS_THAN_EQUAL_VALUE:
            case MORE_THAN_EQUAL_VALUE:
                doDyadComparisonOperator(dyad, state);
                return;
            case REGEX_MATCH_VALUE:
                doRegexMatch(dyad, state);
                return;
            case APPLY_OP_VALUE:
                doDyadicApply(dyad, state);
                return;
            default:
                throw new NotImplementedException("Unknown dyadic operator " + dyad.getOperatorType());
        }
    }

    // Fix https://github.com/ncsa/qdl/issues/114
    private void doExcise(Dyad dyad, State state) {
        Polyad polyad = new Polyad(StemEvaluator.EXCISE);
        polyad.addArgument(dyad.getArgAt(0));
        polyad.addArgument(dyad.getArgAt(1));
        polyad.setSourceCode(dyad.getSourceCode());
        polyad.setTokenPosition(dyad.getTokenPosition());
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }

    private void doNroot(Dyad dyad, State state) {
        Polyad polyad = new Polyad(TMathEvaluator.N_ROOT);
        polyad.addArgument(dyad.getArgAt(1));
        polyad.addArgument(dyad.getArgAt(0));
        polyad.setSourceCode(dyad.getSourceCode());
        polyad.setTokenPosition(dyad.getTokenPosition());
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }


    private void doDyadicApply2(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (objects[1] instanceof FunctionReferenceNode) {
                    FunctionReferenceNode fNode = (FunctionReferenceNode) objects[1];
                    State actualState = fNode.hasModuleState() ? fNode.getModuleState() : state; // determined per fNode

                    r.result = doSingleApply(asQDLValue(objects[0]), (FunctionReferenceNode) objects[1], actualState);
                    return r;
                }
                if (areAllSets(objects)) {
                    throw new QDLExceptionWithTrace(REGEX_MATCH + " not defined on sets.", dyad.getLeftArgument());
                }

                if (!isString(objects[0])) {
                    throw new QDLExceptionWithTrace(REGEX_MATCH + " requires a regular expression as its left argument", dyad.getLeftArgument());
                }
                String regex = objects[0].toString();
                String ob = objects[1].toString();
                r.result = new BooleanValue(ob.matches(regex));
                return r;
            }
        };
        process2(dyad, pointer, REGEX_MATCH, state);
    }

    /*
        f(x)->x^2
  f(x,y)->x*y
  g(x)->x^2
  ∂[@f,@g]
  {'a':[1,2],'b':1}∂{'a':@f, 'b':@g}
     */
    private void doDyadicApply(Dyad dyad, State state) {
        QDLValue lArg = dyad.getLeftArgument().evaluate(state); // evaluated in ambient state
        QDLValue rArg = dyad.getRightArgument().evaluate(state); // evaluated in ambient state
        QDLStem wrapper;
        if (lArg.isStem()) {
            wrapper = lArg.asStem();
        } else {
            // arguments are wrapper, so if they sent a scalar or set,
            QDLStem s = new QDLStem();
            s.getQDLList().add(asQDLValue(lArg));
            wrapper = new QDLStem();
            wrapper.setDefaultValue(s);
        }
        Object result;
        result = evaluateNextArgForApplies(asQDLValue(wrapper), rArg.getValue(), wrapper.getDefaultValue(), state, dyad);
        dyad.setEvaluated(true);
        dyad.setResult(result);
    }

    protected QDLSet applyToSet(QDLStem lArg, QDLSet rArg, QDLValue defaultValue, State state, Dyad dyad) {
        QDLSet output = new QDLSet();
        Iterator rIterator = rArg.iterator();
        while (rIterator.hasNext()) {
            Object nextRArg = rIterator.next();
            Object result = evaluateNextArgForApplies(asQDLValue(lArg), nextRArg, defaultValue, state, dyad);
            output.add(asQDLValue(result));
        }
        return output;
    }

    protected Object evaluateNextArgForApplies(QDLValue lArg,
                                               Object rArg,
                                               QDLValue defaultValue,
                                               State state,
                                               Dyad dyad) {
        Object result;
        rArg = asJavaValue(rArg);
        if (rArg instanceof ExpressionInterface) {
            switch (((ExpressionInterface) rArg).getNodeType()) {
                case ExpressionInterface.FUNCTION_REFERENCE_NODE:
                    result = doSingleApply(lArg, (FunctionReferenceNode) rArg, defaultValue, state, dyad);
                    break;
                case ExpressionInterface.DYADIC_FUNCTION_REFERENCE_NODE:
                    result = doSingleApply(lArg, (DyadicFunctionReferenceNode) rArg, defaultValue, state, dyad);
                    break;
                default:
                    result = rArg;
                    break;
            }
        } else {
            // stems and sets are not nodes. The are created from nodes which have been consumed by this point.
            if (rArg instanceof QDLStem) {
                if (!(lArg.isStem()) && defaultValue == null) {
                    throw new BadArgException("non-conformable left argument (should be a stem)", dyad.getLeftArgument());
                }
                result = applyToStem(lArg.asStem(), (QDLStem) rArg, defaultValue, state, dyad);
            } else {
                if (rArg instanceof FunctionReferenceNode) {
                    result = doSingleApply(lArg, (FunctionReferenceNode) rArg, defaultValue, state, dyad);
                } else {
                    result = rArg;
                }
            }
        }

        return result;
    }

    protected QDLStem applyToStem(QDLStem lArg,
                                  QDLStem rArg,
                                  QDLValue defaultValue,
                                  State state,
                                  Dyad dyad) {
        QDLStem output = new QDLStem();
        for (QDLKey key : rArg.keySet()) {
            QDLValue nextLArg = null;
            if (lArg == null) {
                if (defaultValue == null) {
                    throw new MissingArgException("missing argument for index " + key.getValue(), dyad.getLeftArgument());
                }
            } else {
                if (lArg.containsKey(key)) {
                    nextLArg = lArg.get(key);
                }
                if (lArg.hasDefaultValue()) {
                    defaultValue = lArg.getDefaultValue();
                }
            }

            QDLValue nextRArg = rArg.get(key);
            Object result = evaluateNextArgForApplies(nextLArg==null?null:nextLArg,
                    nextRArg==null?null: nextRArg.getValue(), defaultValue, state, dyad);
            output.put(key, result);
        }
        return output;
    }

    /*
      f(x)->x^2
g(x)->x^3
apply([@f,@g],[2])
[2]∂[@f,@g]

     [3]∂@f
      <==> f(3.4)
     -----
     Complex basic: This sets up a matrix fo function refs with a default value an specific values.
    zz.0.1 := zz.0.3 := zz.1.1:= zz.1.3:=[2,3];
    zz.2.1:=zz.2.3 := [-1,1];
    zz.:=zz.~{*:[4,1,2]};
    zz.0.0 := [-11]; // one outlier for monadic f
    f(x)->x;
    g(p,q)->(p^2+q^2)^0.5;
    f(x,y,z)->x+y+z;
    ff.:=n(3,4,[@f,@g]);
    apply(ff., zz.);
[[-11,3.60555127546399,7,3.60555127546399],[7,3.60555127546399,7,3.60555127546399],[7,1.4142135623731,7,1.4142135623731]]

    g(p,q)->p*q;
    (zz.)∂ff.
[[-11,6,7,6],[7,6,7,6],[7,-1,7,-1]]

     */

    /*
       f(x)->x^2
      f(x,y)->x*y
      [3,4]∂2@f
      [3,4]∂@f;
      [3,4]∂1@f;
        3∂1@f

      [2,3]∂2@*; // built in

     */
    protected Object doSingleApply(QDLValue lArg, DyadicFunctionReferenceNode fNode, QDLValue defaultValue, State state, Dyad dyad) {

        State actualState = fNode.hasModuleState() ? fNode.getModuleState() : state; // determined per fNode
        FunctionRecordInterface fRecord = fNode.getFunctionRecord();
/*        if(fRecord == null) {
            fRecord = state.resolveFunction(polyad, checkForDuplicates); // Do the heavy work of getting it
        }*/
        boolean isBuiltin = fRecord == null;
        if (lArg == null) {
            lArg = defaultValue;
        }
        if (lArg.isStem()) {
            QDLStem lStem = lArg.asStem();
            if (lStem.isEmpty()) {
                if (lStem.hasDefaultValue()) {
                    if (!lStem.getDefaultValue().isStem()) {
                        // So they used a scalar as the default value. Assume they mean it.
                        Polyad polyad = new Polyad(fRecord.getName());
                        polyad.setBuiltIn(false);
                        polyad.addArgument(new ConstantNode(lStem.getDefaultValue()));
                        return polyad.evaluate(actualState);
                    }
                    lStem = lStem.getDefaultValue().asStem();
                }
            }
            ExpressionImpl expression = null;
            boolean isOperator = false;
            if (isBuiltin) {
                Polyad polyad = new Polyad(fRecord.getName());
                polyad.setBuiltIn(true);
                if (actualState.getMetaEvaluator().isBuiltInFunction(fRecord.getName())) {
                    expression = polyad;
                } else {
                    if (!actualState.getOpEvaluator().isMathOperator(fRecord.getName())) {
                        throw new UnknownSymbolException("unknown function '" + fRecord.getName() + "'", dyad.getLeftArgument(), fRecord.getName());
                    }
                    // so it's an operator.
                    if (lStem.size() == 1) {
                        Monad monad = new Monad(actualState.getOperatorType(fRecord.getName()), null); // arg set later
                        expression = monad;
                    }
                    if (lStem.size() == 2) {
                        Dyad dyad1 = new Dyad(actualState.getOperatorType(fRecord.getName()), null, null);
                        expression = dyad1;
                    }
                    expression.setArguments(new ArrayList<>()); // zero this out since we are adding argument later and just set them to null
                    isOperator = true;
                    isBuiltin = true;
                }
            } else {
                // user-defined case
                // May need to pass in argCount = -1 for general case?
                if (lStem.size() != fNode.getFunctionArgCount()) {
                    throw new BadArgException("incompatible argument list length, function requires " + fNode.getFunctionArgCount(), dyad.getArgAt(0));
                }
                Polyad polyad = new Polyad(fRecord.getName());

                polyad.setBuiltIn(false);
                expression = polyad;
            }
            if (lStem.isList()) {
                for (QDLKey key : lStem.keySet()) {
                    expression.getArguments().add(new ConstantNode(lStem.get(key)));
                }

            } else {
                if (isBuiltin) {
                    for (int i = 0; i < lStem.size(); i++) {
                        Object obj = lStem.get(FunctionEvaluator.DUMMY_BUILT_IN_FUNCTION_NAME_CAPUT + i);
                        if (obj == null) {
                            throw new BadArgException("missing argument for '" + FunctionEvaluator.DUMMY_BUILT_IN_FUNCTION_NAME_CAPUT + i + "'", dyad.getLastArg());
                        }
                        expression.getArguments().add(new ConstantNode(asQDLValue(obj)));
                    }
                } else {
                    //FunctionRecordInterface fRec = fRecord.getByArgCount(lStem.size());
                    for (String name : fRecord.getArgNames()) {
                        Object object = lStem.get(name);
                        if (object == null) {
                            throw new BadArgException(APPLY_OP_KEY + " '" + fRecord.getName() + "' missing value for " + name, dyad.getLeftArgument());
                        }
                        expression.getArguments().add(new ConstantNode(asQDLValue(object)));
                    }

                }
            }

            if (isOperator) {
                if (expression instanceof Monad) {
                    actualState.getOpEvaluator().evaluate((Monad) expression, actualState);
                }
                if (expression instanceof Dyad) {
                    actualState.getOpEvaluator().evaluate((Dyad) expression, actualState);
                }
                return expression.getResult();
            }
            return expression.evaluate(actualState);
        }
        // so this is a scalar
        Polyad polyad = new Polyad(fRecord.getName());
        polyad.setBuiltIn(false);
        polyad.addArgument(new ConstantNode(lArg));
        return polyad.evaluate(actualState);

    }

    /**
     * apply the argument to a single function. Note that the dyad here is passed along to make
     * error messages only.
     *
     * @param fNode
     * @param dyad
     * @return
     */
    protected Object doSingleApply(QDLValue lArg, FunctionReferenceNode fNode, QDLValue defaultValue, State state, Dyad dyad) {
        State actualState = fNode.hasModuleState() ? fNode.getModuleState() : state; // determined per fNode
        if (lArg == null) {
            lArg = defaultValue;
        }
        if (lArg.isStem()) {
            QDLStem lStem = lArg.asStem();
            if (lStem.isEmpty()) {
                if (lStem.hasDefaultValue()) {
                    if (!lStem.getDefaultValue().isStem()) {
                        // So they used a scalar as the default value. Assume they mean it.
                        Polyad polyad = new Polyad(fNode.getFunctionName());
                        polyad.setBuiltIn(false);
                        polyad.addArgument(new ConstantNode(lStem.getDefaultValue()));
                        return polyad.evaluate(actualState);
                    }
                    lStem = lStem.getDefaultValue().asStem();
                }
            }
            ExpressionImpl expression = null;
            boolean isOperator = false;
            boolean isBuiltin = false;
            if (fNode.getFunctionRecords().isEmpty()) {
                Polyad polyad = new Polyad(fNode.getFunctionName());
                isBuiltin = true; // need flag for later since expression is general
                polyad.setBuiltIn(true);
                if (state.getMetaEvaluator().isBuiltInFunction(fNode.getFunctionName())) {
                    expression = polyad;
                } else {
                    if (!state.getOpEvaluator().isMathOperator(fNode.getFunctionName())) {
                        throw new UnknownSymbolException("unknown function '" + fNode.getFunctionName() + "'", dyad.getLeftArgument(), fNode.getFunctionName());
                    }
                    // so it's an operator.
                    if (lStem.size() == 1) {
                        Monad monad = new Monad(actualState.getOperatorType(fNode.getFunctionName()), null); // arg set later
                        expression = monad;
                    }
                    if (lStem.size() == 2) {
                        Dyad dyad1 = new Dyad(actualState.getOperatorType(fNode.getFunctionName()), null, null);
                        expression = dyad1;
                    }
                    expression.setArguments(new ArrayList<>()); // zero this out since we are adding argument later and just set them to null
                    isOperator = true;
                    isBuiltin = true;
                }
            } else {
                // user-defined case
                Polyad polyad = new Polyad(fNode.getFunctionName());
                polyad.setBuiltIn(false);
                expression = polyad;
            }
            if (lStem.isList()) {
                for (QDLKey key : lStem.keySet()) {
                    expression.getArguments().add(new ConstantNode(lStem.get(key)));
                }

            } else {
                if (isBuiltin) {
                    for (int i = 0; i < lStem.size(); i++) {
                        Object obj = lStem.get(FunctionEvaluator.DUMMY_BUILT_IN_FUNCTION_NAME_CAPUT + i);
                        if (obj == null) {
                            throw new BadArgException("missing argument for '" + FunctionEvaluator.DUMMY_BUILT_IN_FUNCTION_NAME_CAPUT + i + "'", dyad.getLastArg());
                        }
                        expression.getArguments().add(new ConstantNode(asQDLValue(obj)));
                    }
                } else {
                    FunctionRecordInterface fRec = fNode.getByArgCount(lStem.size());
                    for (String name : fRec.getArgNames()) {
                        Object object = lStem.get(name);
                        if (object == null) {
                            throw new BadArgException(APPLY_OP_KEY + " '" + fNode.getFunctionName() + "' missing value for " + name, dyad.getLeftArgument());
                        }
                        expression.getArguments().add(new ConstantNode(asQDLValue(object)));
                    }

                }
            }

            if (isOperator) {
                if (expression instanceof Monad) {
                    actualState.getOpEvaluator().evaluate((Monad) expression, actualState);
                }
                if (expression instanceof Dyad) {
                    actualState.getOpEvaluator().evaluate((Dyad) expression, actualState);
                }
                return expression.getResult();
            }
            return expression.evaluate(actualState);
        }
        // so this is a scalar
        Polyad polyad = new Polyad(fNode.getFunctionName());
        polyad.setBuiltIn(false);
        polyad.addArgument(new ConstantNode(lArg));
        return polyad.evaluate(actualState);
    }

    protected QDLValue doSingleApply(QDLValue lArg, FunctionReferenceNode fNode, State actualState) {

        if (lArg.isStem()) {
            QDLStem lStem = lArg.asStem();
            Polyad polyad = new Polyad(fNode.getFunctionName());
            polyad.setBuiltIn(false);
            if (lStem.isList()) {
                for (QDLKey key : lStem.keySet()) {
                    polyad.addArgument(new ConstantNode(lStem.get(key)));
                }

            } else {
                FunctionRecordInterface fRec = fNode.getByArgCount(lStem.size());
                if (fRec == null) {
                    throw new BadArgException(APPLY_OP_KEY + " cannot resolve the function '" + fNode.getFunctionName() + "' with the argument count of " + lStem.size(), null);
                }

                for (String name : fRec.getArgNames()) {
                    Object object = lStem.get(name);
                    if (object == null) {
                        throw new BadArgException(APPLY_OP_KEY + " '" + fNode.getFunctionName() + "' missing value for " + name, null);
                    }
                    polyad.addArgument(new ConstantNode(asQDLValue(object)));
                }
            }
            return polyad.evaluate(actualState);
        }
        throw new BadArgException("unknown argument type", null);
    }

    private void doFRefDyadicOperator(Dyad dyad, String expand, State state) {
        Polyad polyad = new Polyad(expand);
        polyad.setTokenPosition(dyad.getTokenPosition());
        polyad.setSourceCode(dyad.getSourceCode());
        polyad.addArgument(dyad.getLeftArgument()); // should be a function reference
        Object obj = dyad.getRightArgument().evaluate(state);
        polyad.addArgument(new ConstantNode(asQDLValue(obj)));
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }

    private void doExpressionDyadOperator(Dyad dyad, String operatorKey, State state, boolean swapArgs) {
        Polyad polyad = new Polyad(operatorKey);
        polyad.setTokenPosition(dyad.getTokenPosition());
        polyad.setSourceCode(dyad.getSourceCode());
        if (swapArgs) { // a op B --> op(B,A)
            polyad.addArgument(dyad.getRightArgument());
            polyad.addArgument(dyad.getLeftArgument());
        } else { // a op B --> op(A,B)
            polyad.addArgument(dyad.getLeftArgument());
            polyad.addArgument(dyad.getRightArgument());
        }
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }

    /*
      a. := [true, false, false, true]
b. := [;4]
a.⌆b.
     */
    private void doForAll(Dyad dyad, State state) {
        Polyad polyad = new Polyad(StemEvaluator.FOR_EACH);
        polyad.setTokenPosition(dyad.getTokenPosition());
        polyad.setSourceCode(dyad.getSourceCode());
        ExpressionInterface ei = dyad.getLeftArgument();
        while ((ei instanceof ParenthesizedExpression)) {
            ei = ((ParenthesizedExpression) ei).getArgAt(0);
        }
        polyad.addArgument(ei); // should be a function reference
        QDLValue obj = dyad.getRightArgument().evaluate(state);
        if (!(obj.isStem())) {
            throw new QDLExceptionWithTrace("right argument of " + FOR_ALL_KEY + " must be a list", dyad.getRightArgument());
        }
        QDLStem stem =  obj.asStem();
        if (!stem.isList()) {
            throw new QDLExceptionWithTrace("right argument of " + FOR_ALL_KEY + " must be a list", dyad.getRightArgument());
        }
        for (QDLKey key : stem.getQDLList().orderedKeys()) {
            // key is a long, convert to int
            polyad.addArgument(new ConstantNode(stem.getQDLList().get(key.asLong())));
        }
        //polyad.addArgument(dyad.getRightArgument());
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }

    protected void doContainsKey(Dyad dyad, State state, boolean containsKey) {
        Polyad polyad = new Polyad(StemEvaluator.HAS_KEY);
        polyad.setTokenPosition(dyad.getTokenPosition());
        polyad.setSourceCode(dyad.getSourceCode());
        polyad.addArgument(dyad.getLeftArgument());
        polyad.addArgument(dyad.getRightArgument());
        if (!containsKey) {
            Monad monad = new Monad(OpEvaluator.NOT_VALUE, false);
            monad.setArgument(polyad);
            monad.setTokenPosition(polyad.getTokenPosition());
            monad.setSourceCode(polyad.getSourceCode());
            state.getOpEvaluator().evaluate(monad, state);
            dyad.setResult(monad.getResult());
            dyad.setEvaluated(monad.isEvaluated());
            return;
        }
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());

    }

    protected void doIsDefinedDyad(Dyad dyad, State state, boolean isDefined) {
        Polyad polyad = new Polyad(FunctionEvaluator.IS_FUNCTION);
        polyad.setTokenPosition(dyad.getTokenPosition());
        polyad.setSourceCode(dyad.getSourceCode());
        polyad.addArgument(dyad.getLeftArgument());
        polyad.addArgument(dyad.getRightArgument());
        if (!isDefined) {
            // The request is for !whatever, so wrap the whole thing in a negation.
            Monad monad = new Monad(OpEvaluator.NOT_VALUE, false);
            monad.setArgument(polyad);
            monad.setTokenPosition(polyad.getTokenPosition());
            monad.setSourceCode(polyad.getSourceCode());
            state.getOpEvaluator().evaluate(monad, state);
            dyad.setResult(monad.getResult());
            dyad.setEvaluated(monad.isEvaluated());
            return;
        }
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }

    private void doIsA(Dyad dyad, State state) {
        doIsA1(dyad, state);
    }

    private void doIsA1(Dyad dyad, State state) {
        QDLValue lhQDLValue = dyad.evalArg(0, state);
        Object lhs;
        if (lhQDLValue.isAxisRestriction()) {
            // If it's a stem with an axis, it is still a stem, get that
            // and let the machinery do its work here.
            lhs = lhQDLValue.asAxisExpression().getStem();
        }else{
            lhs = lhQDLValue.getValue();
        }
        if (!(dyad.getRightArgument() instanceof VariableNode)) {
            List<String> source = dyad.getRightArgument().getSourceCode();
            String text;
            if (source.size() == 1) {
                text = source.get(0);
            } else {
                text = source.toString();
            }
            throw new BadArgException("unknown type '" + text + "'", dyad.getRightArgument());
        }
        QDLValue obj2 = dyad.evalArg(1, state); // If the variable resolves to something, blow up
        if (obj2 != null) {
            throw new BadArgException("you must supply a type, not a value ", dyad.getRightArgument());
        }
        String typeName = ((VariableNode) dyad.getRightArgument()).getVariableReference();
        boolean x;
        switch (typeName) {
            case NULL:
                x = lhQDLValue.isNull();
                break;
            case Types.BOOLEAN:
                x = lhQDLValue.isBoolean();
                break;
            case Types.STRING:
                x = lhQDLValue .isString();
                break;
            case Types.NUMBER:
                x = (lhQDLValue.isLong()) || (lhQDLValue.isDecimal());
                break;
            case Types.INTEGER:
                x = lhQDLValue.isLong();
                break;
            case Types.DECIMAL:
                x = lhQDLValue.isDecimal();
                break;
            case Types.STEM:
                x = lhQDLValue.isStem();
                break;
            case Types.LIST:
                x = (lhQDLValue.isStem()) && lhQDLValue.asStem().isList();
                break;
            case Types.SET:
                x = lhQDLValue.isSet();
                break;
            default:
                throw new BadArgException("unkown type", dyad.getRightArgument());
        }
        dyad.setResult(x ? BooleanValue.True : BooleanValue.False);
        dyad.setEvaluated(true);

    }

    private void doMembership(Dyad dyad, State state, boolean isMember) {
        Polyad polyad;
        polyad = new Polyad(StemEvaluator.HAS_VALUE);
        polyad.setTokenPosition(dyad.getTokenPosition());
        polyad.setSourceCode(dyad.getSourceCode());
        polyad.addArgument(dyad.getLeftArgument());
        polyad.addArgument(dyad.getRightArgument());
        if (!isMember) {
            Monad monad = new Monad(OpEvaluator.NOT_VALUE, false);
            monad.setArgument(polyad);
            monad.setTokenPosition(polyad.getTokenPosition());
            monad.setSourceCode(polyad.getSourceCode());
            state.getOpEvaluator().evaluate(monad, state);
            dyad.setResult(monad.getResult());
            dyad.setEvaluated(monad.isEvaluated());
            return;
        }
        state.getMetaEvaluator().evaluate(polyad, state);
        dyad.setResult(polyad.getResult());
        dyad.setEvaluated(polyad.isEvaluated());
    }

    // '[a-zA-Z]{3}' =~ 'aBc'

    /**
     * Contract is expression regex ≈ expression returns true if it matches expression as a string.
     *
     * @param dyad
     * @param state
     */
    protected void doRegexMatch(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (areAllSets(objects)) {
                    throw new QDLExceptionWithTrace(REGEX_MATCH + " not defined on sets.", dyad.getLeftArgument());
                }

                if (!isString(objects[0])) {
                    throw new QDLExceptionWithTrace(REGEX_MATCH + " requires a regular expression as its left argument", dyad.getLeftArgument());
                }
                String regex = objects[0].toString();
                String ob = objects[1].toString();
                r.result = new BooleanValue(ob.matches(regex));
                return r;
            }
        };
        process2(dyad, pointer, REGEX_MATCH, state);
    }

    private void doJoin(Dyad dyad, State state) {
        Polyad joinPolyad = new Polyad(StemEvaluator.JOIN);
        joinPolyad.getArguments().add(dyad.getLeftArgument());
        joinPolyad.getArguments().add(dyad.getRightArgument());
        joinPolyad.getArguments().add(new ConstantNode(LongValue.MinusOne));
        state.getMetaEvaluator().evaluate(joinPolyad, state);
        dyad.setResult(joinPolyad.getResult());
        dyad.setEvaluated(true);
    }

    protected void doTilde(Dyad dyad, State state) {
        QDLValue obj1 = dyad.evalArg(1, state);
        if (obj1.isSet()) {
            QDLSet set = obj1.asSet();
            QDLStem outStem = new QDLStem();
            // special case. If this is a unary ~, then the first argument is
            // ignored.
            if (dyad.isUnary()) {
                dyad.setResult(set.toStem());
                dyad.setEvaluated(true);
                return;

            }
            // so they are trying to append the list to an existing stem.
            // In that case it becomes another entry
            QDLValue obj0 = dyad.evalArg(0, state);
            if ((obj0.isNull())) {
                throw new QDLExceptionWithTrace("cannot do a union a null", dyad.getLeftArgument());
            }
            if ((obj1.isNull())) {
                throw new QDLExceptionWithTrace("cannot do a union a null", dyad.getRightArgument());
            }
            QDLStem stem0;
            if (obj0.isStem()) {
                stem0 = obj0.asStem();
            } else {
                stem0 = new QDLStem();
                stem0.put(LongValue.Zero, asQDLValue(obj0));
            }
            outStem = outStem.union(stem0); // copy over elements
            long index = -1L;
            SparseEntry sparseEntry;
            if (!outStem.getQDLList().isEmpty()) {
                sparseEntry = outStem.getQDLList().last();
                index = sparseEntry.index;
            }
            SparseEntry newEntry = new SparseEntry(index + 1, new SetValue(set));

            outStem.getQDLList().add(newEntry);
            dyad.setResult(outStem);
            dyad.setEvaluated(true);
            return;
        }
        QDLValue obj0 = dyad.evalArg(0, state);

        QDLStem stem0 = null;
        QDLStem stem1 = null;
        if ((obj0.isNull())) {
            stem0 = new QDLStem();
            stem0.put(LongValue.Zero, QDLValue.getNullValue());
        } else {
            if (obj0.isStem()) {
                stem0 = obj0.asStem();
            } else {
                stem0 = new QDLStem();
                stem0.put(LongValue.Zero, asQDLValue(obj0));
            }
        }


        if (obj1.isStem()) {
            stem1 = obj1.asStem();
        } else {
            stem1 = new QDLStem();
            stem1.put(LongValue.Zero, asQDLValue(obj1));
        }
        // NOTE this is done so we don't end up shlepping around references to things and modifying them
        // without warning.
        //       stem0 = (StemVariable) stem0.clone();
        //       stem1 = (StemVariable)stem1.clone();
        QDLStem newStem = stem0.union(stem1);
        dyad.setResult(newStem);
        dyad.setEvaluated(true);
    }

    protected void doDyadIntegerDivide(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                objects = castToJavaValues(objects);
                fpResult r = new fpResult();
                if (areAllSets(objects)) {
                    QDLSet leftSet = (QDLSet) objects[0];
                    QDLSet rightSet = (QDLSet) objects[1];
                    r.result = asQDLValue(leftSet.symmetricDifference(rightSet));
                    return r;
                }
                if (!areAllNumbers(objects)) {
                    throw new QDLExceptionWithTrace("division is not defined for  non-numeric values", dyad.getLeftArgument());
                }
                if (areAllLongs(objects)) {
                    r.result = asQDLValue((Long) objects[0] / (Long) objects[1]);
                } else {
                    BigDecimal left = toBD(objects[0]);
                    BigDecimal right = toBD(objects[1]);
                    BigDecimal rr = null;
                    try {
                        rr = left.divideToIntegralValue(right, OpEvaluator.getMathContext());
                    } catch (ArithmeticException ax0) {
                        throw new QDLExceptionWithTrace("Insufficient precision to divide. Please increase " + MathEvaluator.NUMERIC_DIGITS, dyad.getRightArgument());
                    }
                    try {
                        r.result = asQDLValue(rr.longValueExact());
                        return r;
                    } catch (ArithmeticException ax) {

                    }
                    r.result = asQDLValue(rr);
                }
                return r;
            }
        };
        process2(dyad, pointer, INTEGER_DIVIDE, state);
    }

    private void doPower(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                objects = castToJavaValues(objects);
                fpResult r = new fpResult();
                if (areAllSets(objects)) {
                    throw new QDLExceptionWithTrace(POWER + " not defined for sets.", dyad);
                }
                if (areAllNumbers(objects)) {
                    boolean doBigD = isBigDecimal(objects[0]) || isBigDecimal(objects[1]);
                    BigDecimal left = toBD(objects[0]);
                    BigDecimal result;
                    if (isLong(objects[1])) {
                        result = ch.obermuhlner.math.big.BigDecimalMath.pow(left, (Long) objects[1], getMathContext());
                    } else {
                        result = ch.obermuhlner.math.big.BigDecimalMath.pow(left, (BigDecimal) objects[1], getMathContext());
                    }
                    if (!doBigD) {
                        try {
                            r.result = asQDLValue(result.longValueExact());
                            return r;
                        } catch (ArithmeticException ax) {
                        }
                    }
                    r.result = asQDLValue(result);
                } else {
                    throw new QDLExceptionWithTrace("Exponentiation requires a int or decimal be raised to an int power", dyad.getLeftArgument());
                }
                return r;
            }
        };
        process2(dyad, pointer, POWER, state);

    }


    protected void doDyadComparisonOperator(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (areAllSets(objects)) {
                    QDLSet leftSet = (QDLSet) objects[0];
                    QDLSet rightSet = (QDLSet) objects[1];
                    Object tempValue = null;
                    switch (dyad.getOperatorType()) {
                        case LESS_THAN_VALUE:
                            tempValue = leftSet.isSubsetOf(rightSet) && (leftSet.size() != rightSet.size());
                            break;
                        case LESS_THAN_EQUAL_VALUE:
                            tempValue = leftSet.isSubsetOf(rightSet);
                            break;
                        case MORE_THAN_VALUE:
                            tempValue = rightSet.isSubsetOf(leftSet) && (leftSet.size() != rightSet.size());
                            break;
                        case MORE_THAN_EQUAL_VALUE:
                            tempValue = rightSet.isSubsetOf(leftSet);
                            break;
                    }
                    r.result = asQDLValue(tempValue);
                    return r;
                }
                if (areAllStrings(objects)) {
                    String left = (String) objects[0];
                    String right = (String) objects[1];
                    Object tempValue = null;
                    switch (dyad.getOperatorType()) {
                        case LESS_THAN_VALUE:
                            tempValue = -1 < right.indexOf(left) && left.length() < right.length();
                            break;
                        case LESS_THAN_EQUAL_VALUE:
                            tempValue = -1 < right.indexOf(left);
                            break;
                        case MORE_THAN_VALUE:
                            tempValue = -1 < left.indexOf(right) && right.length() < left.length();
                            break;
                        case MORE_THAN_EQUAL_VALUE:
                            tempValue = -1 < left.indexOf(right);
                            break;
                    }
                    r.result = asQDLValue(tempValue);
                    return r;
                }
                if (!areAllNumbers(objects)) {
                    throw new QDLExceptionWithTrace("only numbers may be compared", dyad.getLeftArgument());
                }
                BigDecimal left = toBD(objects[0]);
                BigDecimal right = toBD(objects[1]);
                int leftToRight = left.compareTo(right);
                Boolean result = false;
                switch (dyad.getOperatorType()) {
                    case LESS_THAN_VALUE:
                        result = (leftToRight < 0);
                        break;
                    case LESS_THAN_EQUAL_VALUE:
                        result = (leftToRight < 0) || (leftToRight == 0);
                        break;
                    case MORE_THAN_VALUE:
                        result = (0 < leftToRight);
                        break;
                    case MORE_THAN_EQUAL_VALUE:
                        result = (0 == leftToRight) || (0 < leftToRight);
                        break;
                }
                r.result = new BooleanValue(result);
                return r;
            }
        };
        String op = "";
        switch (dyad.getOperatorType()) {
            case LESS_THAN_VALUE:
                op = LESS_THAN;
                break;
            case LESS_THAN_EQUAL_VALUE:
                op = LESS_THAN_EQUAL;
                break;
            case MORE_THAN_VALUE:
                op = MORE_THAN;
                break;
            case MORE_THAN_EQUAL_VALUE:
                op = MORE_THAN_EQUAL;
                break;
        }
        process2(dyad, pointer, op, state);

    }

    protected void doDyadEqualsOperator(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);

                if (areAllSets(objects)) {
                    QDLSet leftSet = (QDLSet) objects[0];
                    QDLSet rightSet = (QDLSet) objects[1];
                    Boolean tempValue = null;
                    switch (dyad.getOperatorType()) {
                        case EQUALS_VALUE:
                            tempValue = leftSet.isEqualTo(rightSet);
                            break;
                        case NOT_EQUAL_VALUE:
                            tempValue = !leftSet.isEqualTo(rightSet);
                            break;
                    }
                    r.result = asQDLValue(tempValue);
                    return r;
                }
                if (isSet(objects[0])) {
                    QDLSet leftSet = (QDLSet) objects[0];
                    r.result = asQDLValue(leftSet.contains(objects[1]));
                    return r;
                }
                if (isSet(objects[1])) {
                    QDLSet set = (QDLSet) objects[1];
                    r.result = asQDLValue(set.contains(objects[0]));
                    return r;
                }

                if (areAnyBigDecimals(objects)) {
                    BigDecimal left;
                    BigDecimal right;
                    try {
                        left = toBD(objects[0]);
                        right = toBD(objects[1]);
                    } catch (IllegalArgumentException iax) {
                        // means that something cannot be converted to a big decimal
                        Boolean v = null;
                        switch (dyad.getOperatorType()) {
                            case EQUALS_VALUE:
                                v = Boolean.FALSE;
                                break;
                            case NOT_EQUAL_VALUE:
                                v = Boolean.TRUE;
                                break;
                        }
                        r.result = asQDLValue(v);
                        return r;
                    }
                    Boolean v = null;
                    switch (dyad.getOperatorType()) {
                        case EQUALS_VALUE:
                            v = bdEquals(left, right);
                            break;
                        case NOT_EQUAL_VALUE:
                            v = !bdEquals(left, right);
                            break;
                    } // end switch
                    r.result = asQDLValue(v);
                } else {
                    Boolean temp = null;
                    // just do object comparison
                    switch (dyad.getOperatorType()) {
                        case EQUALS_VALUE:
                            if (objects[0] == null) {
                                temp = objects[1] == objects[0];
                            } else {
                                // edge case == null
                            //    if (objects[1] instanceof QDLNull) {
                              //      temp = objects[0] instanceof QDLNull;
                               // } else {
                                    temp = objects[0].equals(objects[1]);
                               // }
                            }
                            break;
                        case NOT_EQUAL_VALUE:
                            if (objects[0] == null) {
                                temp = objects[1] != objects[0];
                            } else {
                                temp = !objects[0].equals(objects[1]);
                            }
                            break;
                    }//end switch
                    r.result = asQDLValue(temp);
                }
                return r;
            }
        };
        String op = (dyad.getOperatorType() == EQUALS_VALUE ? EQUALS : NOT_EQUAL);
        process2(dyad, pointer, op, state);

    }

    protected void doSetUnionOrInteresection(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (!areAllSets(objects)) {
                    throw new QDLExceptionWithTrace("Set operations require only sets", dyad.getLeftArgument());
                }
                QDLSet leftSet = (QDLSet) objects[0];
                QDLSet rightSet = (QDLSet) objects[1];
                QDLSet outSet = null;
                switch (dyad.getOperatorType()) {
                    case INTERSECTION_VALUE:
                        outSet = leftSet.intersection(rightSet);
                        break;
                    case UNION_VALUE:
                        outSet = leftSet.union(rightSet);
                        break;
                }
                r.result = asQDLValue(outSet);
                return r;


            }
        };
        // Figure out the operator from the type to pass along
        String op = "";
        switch (dyad.getOperatorType()) {
            case UNION_VALUE:
                op = UNION;
                break;
            case INTERSECTION_VALUE:
                op = INTERSECTION;
                break;
        }
        process2(dyad, pointer, op, state);

    }

    protected void doDyadLogicalOperator(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (areAllSets(objects)) {
                    QDLSet leftSet = (QDLSet) objects[0];
                    QDLSet rightSet = (QDLSet) objects[1];
                    Boolean tempValue = null;
                    switch (dyad.getOperatorType()) {
                        case EQUALS_VALUE:
                            tempValue = leftSet.isEqualTo(rightSet);
                            break;
                        case NOT_EQUAL_VALUE:
                            tempValue = !leftSet.isEqualTo(rightSet);
                            break;
                    }
                    r.result = asQDLValue(tempValue);
                    return r;
                }
                if (!areAllBoolean(objects)) {
                    throw new QDLExceptionWithTrace("arguments must be boolean for logical operations", dyad.getLeftArgument());
                }
                Boolean left = (Boolean) objects[0];
                Boolean right = (Boolean) objects[1];
                Boolean result = null;
                switch (dyad.getOperatorType()) {
                    case AND_VALUE:
                        result = left && right;
                        break;
                    case OR_VALUE:
                        result = left || right;
                        break;
                    case EQUALS_VALUE:
                        result = left == right;
                        break;
                    case NOT_EQUAL_VALUE:
                        result = left != right;
                        break;
                }
                r.result = asQDLValue(result);
                return r;
            }
        };
        String op = "";
        switch (dyad.getOperatorType()) {
            case AND_VALUE:
                op = AND;
                break;
            case OR_VALUE:
                op = OR;
                break;
            case EQUALS_VALUE:
                op = EQUALS;
                break;
            case NOT_EQUAL_VALUE:
                op = NOT_EQUAL;
                break;
        }
        process2(dyad, pointer, op, state);
    }

    protected void doDyadMinus(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (areAllSets(objects)) {
                    throw new QDLExceptionWithTrace(MINUS + " not defined on sets. Did you mean difference (" + DIVIDE + ")?", dyad);
                }
                if (areAllNumbers(objects)) {
                    if (areAllLongs(objects)) {
                        try {
                            r.result = asQDLValue(Math.subtractExact((Long) objects[0], (Long) objects[1]));
                            return r;
                        } catch (ArithmeticException arithmeticException) {
                            // fall through to big decimal case
                        }
                    }

                    BigDecimal left = toBD(objects[0]);
                    BigDecimal right = toBD(objects[1]);
                    r.result = new DecimalValue(left.subtract(right));
                } else {
                    if (!areAllStrings(objects)) {
                        throw new QDLExceptionWithTrace("cannot perform " + MINUS + " on mixed argument values.", dyad.getLeftArgument());
                    }
                    String lString = objects[0].toString();
                    String rString = objects[1].toString();
                    int ndx = lString.indexOf(rString);
                    while (0 <= ndx) {
                        lString = lString.substring(0, ndx) + lString.substring(ndx + rString.length());
                        ndx = lString.indexOf(rString);
                    }
                    r.result = new StringValue(lString);
                }
                return r;
            }
        };
        process2(dyad, pointer, MINUS, state);
    }

    protected void doDyadTimesOrDivide(Dyad dyad, State state, boolean doTimes) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (areAllSets(objects)) {
                    if (doTimes) {
                        throw new QDLExceptionWithTrace(TIMES + " not defined on sets.", dyad.getLeftArgument());
                    }
                    QDLSet leftSet = (QDLSet) objects[0];
                    QDLSet rightSet = (QDLSet) objects[1];
                    r.result = new SetValue(leftSet.difference(rightSet));
                    return r;
                }
                if (areAllNumbers(objects)) {
                    if (doTimes) {
                        if (areAllLongs(objects)) {
                            try {
                                r.result = new LongValue(Math.multiplyExact((Long) objects[0], (Long) objects[1]));
                                return r;
                            } catch (ArithmeticException arithmeticException) {
                                // fall through to BD case
                            }
                        }
                        BigDecimal left = toBD(objects[0]);
                        BigDecimal right = toBD(objects[1]);
                        BigDecimal rr = left.multiply(right);
                        try {
                            r.result = new LongValue(rr.longValueExact());
                        } catch (ArithmeticException arithmeticException) {
                            r.result = asQDLValue(rr);
                        }
                        return r;
                    } else {
                        BigDecimal left = toBD(objects[0]);
                        BigDecimal right = toBD(objects[1]);
                        BigDecimal res = left.divide(right, getNumericDigits(), BigDecimal.ROUND_DOWN);
                        if (MathEvaluator.isIntegerValue(res)) {
                            // try to turn it into an integer
                            try {
                                r.result = new LongValue(res.longValueExact());
                                return r;
                            } catch (ArithmeticException arithmeticException) {
                                // so it cannot eb turned into a long value for whatever reason
                            }
                        }
                        r.result = asQDLValue(res);
                    }
                    return r;
                } else {
                    long count = 0;
                    String arg = "";
                    String tempOutput = "";
                    boolean gotOne = false;
                    if (!doTimes && isString(objects[0]) && isString(objects[1])) {
                        String left = (String) objects[0];
                        String right = (String) objects[1];
                        r.result = new LongValue(StringUtils.countMatches(left, right));
                        return r;
                    }
                    if (doTimes && isLong(objects[0]) && isString(objects[1])) {
                        count = (Long) objects[0];
                        arg = (String) objects[1];
                        if (count < 0) {
                            throw new QDLExceptionWithTrace("multiplication is undefined for strings and  negative integers", dyad.getLeftArgument());
                        }
                        gotOne = 0 <= count;
                    }
                    if (doTimes && isLong(objects[1]) && isString(objects[0])) {
                        arg = (String) objects[0];
                        count = (Long) objects[1];
                        if (count < 0) {
                            throw new QDLExceptionWithTrace("multiplication is undefined for strings and  negative integers", dyad.getRightArgument());
                        }
                        gotOne = 0 <= count;
                    }

                    if (gotOne) {
                        if (count == 0) {
                            r.result = new StringValue();
                            return r;
                        }
                        for (long i = 0; i < count; i++) {
                            tempOutput = tempOutput + arg;
                        }
                        r.result = new StringValue(tempOutput);
                        return r;
                    }
                    throw new QDLExceptionWithTrace((doTimes ? "multiplication" : "division") + " is undefined for  non-numeric values", dyad);
                }
            }
        };
        try {
            process2(dyad, pointer, doTimes ? TIMES : DIVIDE, state);
        } catch (ArithmeticException ax) {
            if (ax.getMessage().equals("/ by zero")) {
                ax = new ArithmeticException("divide by zero");
            }
            throw ax;
        }
    }


    // maximum long value is 9223372036854775807
    // almost max is 9223372036854775806

    /**
     * For dyadic plus.
     *
     * @param dyad
     * @param state
     */
    protected void doDyadPlus(Dyad dyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                // At this point, only scalars should ever get passed here as arguments.
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (areAllSets(objects)) {
                    throw new QDLExceptionWithTrace(PLUS + " not defined on sets. Did you mean union (" + UNION + ")?", dyad);
                }
                if (areAllNumbers(objects)) {
                    if (areAllLongs(objects)) {
                        try {
                            r.result = new LongValue(Math.addExact((Long) objects[0], (Long) objects[1]));
                            return r;
                        } catch (ArithmeticException arithmeticException) {
                            // fall through
                        }
                    }

                    BigDecimal left = toBD(objects[0]);
                    BigDecimal right = toBD(objects[1]);
                    r.result = new DecimalValue(left.add(right));
                    return r;
                }

                if (!isStem(objects[1])) {
                    r.result = new StringValue(objects[0].toString() + objects[1].toString());
                    return r;
                }
                // This is a stem
                throw new QDLExceptionWithTrace("stem encountered in scalar operation", dyad);
            }
        };
        process2(dyad, pointer, PLUS, state);
    }


    public void evaluate(Monad monad, State state) {
        try {
            evaluate2(monad, state);
        } catch (QDLException q) {
            throw q;
        } catch (Throwable t) {
            QDLExceptionWithTrace qq = new QDLExceptionWithTrace(t, monad);
            throw qq;
        }
    }

    public void evaluate2(Monad monad, State state) {

        switch (monad.getOperatorType()) {
            case TRANSPOSE_OP_VALUE:
                doMonadicTranspose(monad, state);
                return;
            case IS_DEFINED_VALUE:
                doIsDefined(monad, state, true);
                return;
            case IS_NOT_DEFINED_VALUE:
                doIsDefined(monad, state, false);
                return;

            case TO_SET_VALUE:
                doToSet(monad, state);
                return;

            case CEILING_VALUE:
                doFloorOrCeiling(monad, state, false);
                return;
            case FLOOR_VALUE:
                doFloorOrCeiling(monad, state, true);
                return;

            case NOT_VALUE:
                doMonadNot(monad, state);
                return;
            case PLUS_PLUS_VALUE:
                doMonadIncOrDec(monad, state, true);
                return;
            case MINUS_MINUS_VALUE:
                doMonadIncOrDec(monad, state, false);
                return;
            case MINUS_VALUE:
                doMonadMinus(monad, state);
                return;
            case PLUS_VALUE:
                doMonadPlus(monad, state);
                return;
            case TILDE_VALUE:
                doMonadicTilde(monad, state, false);
                return;
            case TILDE_STILE_VALUE:
                doMonadicTilde(monad, state, true);
                return;
            case APPLY_OP_VALUE:
                doMonadicApply(monad, state);
                return;
            default:
                throw new NotImplementedException("Unknown monadic operator");
        }

    }

    private void doMonadicApply(Monad monad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);
                if (objects[0] instanceof FunctionReferenceNode) {
                    FunctionReferenceNode fNode = (FunctionReferenceNode) objects[0];

                    TreeSet<Long> counts = new TreeSet<>(); // keep it all sorted
                    if (fNode.getFunctionRecords().isEmpty()) {
                        if (state.getMetaEvaluator().isBuiltInFunction(fNode.getFunctionName())) {
                            Polyad polyad = new Polyad(fNode.getFunctionName());
                            polyad.setSizeQuery(true);
                            state.getMetaEvaluator().evaluate(polyad, state);
                            for (int argCount : polyad.getAllowedArgCounts()) {
                                counts.add((long) argCount);
                            }
                        } else {
                            // final case, this might be just an operator. Operators like +
                            // are processed in the OpEvaluator, so check there.
                            if (!isMathOperator(fNode.getFunctionName())) {
                                throw new UnknownSymbolException("no such function named '" + fNode.getFunctionName() + "'", monad.getArgument(), fNode.getFunctionName());
                            }
                            int[] argCounts = getArgCount(fNode.getFunctionName());
                            for (int argCount : argCounts) {
                                counts.add((long) argCount);
                            }
                        }
                    } else {
                        for (FunctionRecordInterface functionRecordInterface : fNode.getFunctionRecords()) {
                            counts.add((long) functionRecordInterface.getArgCount());
                        }
                    }
                    QDLStem stem = new QDLStem();
                    for (Long x : counts) {
                        stem.getQDLList().add(asQDLValue(x));
                    }
                    r.result = new StemValue(stem);
                }
                return r;
            }
        };
        process1(monad, pointer, NOT, state);
    }


    private void doMonadicTranspose(Monad monad, State state) {
        Polyad polyad = new Polyad(StemEvaluator.TRANSPOSE);
        polyad.setTokenPosition(monad.getTokenPosition());
        polyad.setSourceCode(monad.getSourceCode());
        polyad.addArgument(monad.getArgument());
        polyad.evaluate(state);
        monad.setResult(polyad.getResult());
        monad.setEvaluated(true);
    }

    protected void doMonadicTilde(Monad monad, State state, boolean isStile) {
        Dyad dyad = new Dyad(isStile ? TILDE_STILE_VALUE : TILDE_VALUE);
        dyad.setUnary(true);
        dyad.setTokenPosition(monad.getTokenPosition());
        dyad.setSourceCode(monad.getSourceCode());
        dyad.setLeftArgument(new ConstantNode(new StemValue()));
        dyad.setRightArgument(monad.getArgument());
        dyad.evaluate(state);
        monad.setResult(dyad.getResult());
        monad.setEvaluated(true);

    }

    private void doIsDefined(Monad monad, State state, boolean isDefined) {
        Polyad polyad;
        polyad = new Polyad(SystemEvaluator.IS_DEFINED);
        polyad.setTokenPosition(monad.getTokenPosition());
        polyad.setSourceCode(monad.getSourceCode());
        polyad.addArgument(monad.getArgument());
        if (!isDefined) {
            Monad notMonad = new Monad(OpEvaluator.NOT_VALUE, false);
            notMonad.setArgument(polyad);
            notMonad.setTokenPosition(polyad.getTokenPosition());
            notMonad.setSourceCode(polyad.getSourceCode());
            state.getOpEvaluator().evaluate(notMonad, state);
            monad.setResult(notMonad.getResult());
            monad.setEvaluated(notMonad.isEvaluated());
            return;
        }
        state.getMetaEvaluator().evaluate(polyad, state);
        monad.setResult(polyad.getResult());
        monad.setEvaluated(polyad.isEvaluated());
    }

    private void doToSet(Monad monad, State state) {
        Object r = monad.getArgument().evaluate(state);
        switch (Constant.getType(r)) {
            case BOOLEAN_TYPE:
            case STRING_TYPE:
            case DECIMAL_TYPE:
            case LONG_TYPE:
            case NULL_TYPE:
                QDLSet set = new QDLSet();
                set.add(asQDLValue(r));
                monad.setEvaluated(true);
                monad.setResult(set);
                return;
            case SET_TYPE:
                monad.setEvaluated(true);
                monad.setResult(asQDLValue(r));
                return;
            case LIST_TYPE:
            case STEM_TYPE:
                Polyad p = new Polyad(StemEvaluator.UNIQUE_VALUES);
                p.setArguments(monad.getArguments());
                p.evaluate(state);
                QDLStem stemVariable = p.getResult().asStem(); // as per contract
                set = new QDLSet();
                set.addAll(stemVariable.getQDLList().values());

                monad.setResult(set);
                monad.setEvaluated(true);
                return;
        }
        throw new QDLExceptionWithTrace("unknown type", monad.getArgument());
    }

    private void doFloorOrCeiling(Monad monad, State state, boolean isFloor) {
        Polyad polyad;
        if (isFloor) {
            polyad = new Polyad(TMathEvaluator.FLOOR);
        } else {
            polyad = new Polyad(TMathEvaluator.CEILING);
        }
        polyad.addArgument(monad.getArgument());
        polyad.setSourceCode(monad.getSourceCode());
        polyad.setTokenPosition(monad.getTokenPosition());
        state.getMetaEvaluator().evaluate(polyad, state);
        monad.setResult(polyad.getResult());
        monad.setEvaluated(polyad.isEvaluated());
    }

    /**
     * NOTE that at this point this only works for single variables -- you can't apply
     * this to a stem.
     *
     * @param monad
     * @param state
     * @param isPlusPlus
     */
    protected void doMonadIncOrDec(Monad monad, State state, boolean isPlusPlus) {
        if (!(monad.getArgument() instanceof VariableNode)) {
            throw new QDLExceptionWithTrace("You can only " + (isPlusPlus ? "increment" : "decrement") + " a variable.", monad.getArgument());
        }
        VariableNode var = (VariableNode) monad.getArgument();
        QDLValue qdlValue = var.evaluate(state);
        boolean gotOne = false;
        Object resultValue = null;
        if (qdlValue.isLong()) {
            gotOne = true;
            Long x = qdlValue.asLong();
            if (isPlusPlus) {
                resultValue = x + 1L;
            } else {
                resultValue = x - 1L;
            }
        }
        if (qdlValue.isDecimal()) {
            gotOne = true;
            BigDecimal bd = qdlValue.asDecimal();
            BigDecimal one = new BigDecimal("1.0");
            if (isPlusPlus) {
                resultValue = bd.add(one);
            } else {
                resultValue = bd.subtract(one);
            }
        }
        if (!gotOne) {
            throw new QDLExceptionWithTrace("" + (isPlusPlus ? PLUS_PLUS : MINUS_MINUS) + " requires a number value", monad.getArgument());
        }
        if (monad.isPostFix()) {
            monad.setResult(qdlValue); // so the returned result is NOT incremented for postfixes.
        } else {
            monad.setResult(resultValue); // so the returned result is the increment for prefixes
        }
        monad.setEvaluated(true);
        state.setValue(var.getVariableReference(), asQDLValue(resultValue));
    }


    protected void doMonadNot(Monad monad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                objects = castToJavaValues(objects);
                fpResult r = new fpResult();
                if (!isBoolean(objects[0])) {
                    throw new QDLExceptionWithTrace("negation requires a strictly boolean argument not '" + objects[0] + "'", monad.getArgument());
                }
                r.result = new BooleanValue(!(Boolean) objects[0]);
                return r;
            }
        };
        process1(monad, pointer, NOT, state);
    }

    /**
     * This will evaluate the expression and take its opposite. This is because the parser does not differentiate
     * between -3^4 and (-3)^4, turing the first of each into a single monad. It works right if there
     * are parentheses, but this is possible a thronier issue to fix than we want now. Best to
     * ket it do this since algebraic operations work as expected.
     *
     * @param monad
     * @param state
     * @param sign
     */
    protected void doUnaryPlusMinus(Monad monad, State state, Long sign) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                objects = castToJavaValues(objects);

                switch (Constant.getType(objects[0])) {
                    case LONG_TYPE:
                        r.result = asQDLValue(sign * (Long) objects[0]);
                        break;
                    case DECIMAL_TYPE:
                        BigDecimal x = toBD(objects[0]);
                        r.result = asQDLValue(sign < 0 ? x.negate() : x);
                        break;
                    case STRING_TYPE:
                        if (sign > 0) {
                            r.result = asQDLValue(objects[0]);
                        } else {
                            r.result = new StringValue();
                        }
                        break;
                    default:
                        throw new QDLExceptionWithTrace("You can only take the negative of a number or string", monad.getArgument());
                }
                return r;
            }
        };
        process1(monad, pointer, sign == 1 ? PLUS : MINUS, state);
    }

    protected void doMonadPlus(Monad monad, State state) {
        doUnaryPlusMinus(monad, state, 1L);
    }

    protected void doMonadMinus(Monad monad, State state) {
        doUnaryPlusMinus(monad, state, -1L);
    }

    public void evaluate(Nilad nilad, State state) {
        switch (nilad.getOperatorType()) {
            default:
                throw new NotImplementedException("Unknown niladic operator");
        }
    }


    @Override
    public boolean evaluate(Polyad polyad, State state) {
        return false;
    }

    @Override
    public boolean dispatch(Polyad polyad, State state) {
        return false;
    }



}
