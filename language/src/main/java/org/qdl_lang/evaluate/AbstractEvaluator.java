package org.qdl_lang.evaluate;

import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.*;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.values.SetValue;
import org.qdl_lang.vfs.VFSEntry;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import org.apache.commons.codec.binary.Base32;
import org.qdl_lang.functions.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

import static org.qdl_lang.variables.Constant.UNKNOWN_TYPE;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * Top level. All evaluators should extend this.
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  10:59 AM
 */
public abstract class AbstractEvaluator implements EvaluatorInterface {

    protected String[] fNames = null;

    public abstract String[] getFunctionNames();

    String[] fqNames = null;

    public String[] getFQFunctionNames() {
        if (fqNames == null) {
            fqNames = new String[getFunctionNames().length];
            for (int i = 0; i < fqNames.length; i++) {
                fqNames[i] = getNamespace() + State.NS_DELIMITER + getFunctionNames()[i];
            }
        }

        return fqNames;
    }

    @Override
    public TreeSet<String> listFunctions(boolean listFQ) {
        TreeSet<String> names = new TreeSet<>();
        String[] fnames = listFQ ? getFQFunctionNames() : getFunctionNames();

        for (String key : fnames) {
            try {
                int[] argCount = MetaEvaluator.getInstance().getArgCount(key);
                if (AbstractEvaluator.MAX_ARG_COUNT <= argCount.length) {
                    names.add(key + "([" + argCount[0] + "," + argCount[1] + "," + argCount[2] + ",...])");
                } else {
                    String aaa = Arrays.toString(argCount);
                    aaa = aaa.replace(" ", ""); // remove blanks
                    names.add(key + "(" + aaa + ")");
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return names;
    }

    public boolean isBuiltInFunction(String name) {
        for (String x : getFunctionNames()) {
            if (x.equals(name)) return true;
        }
        return false;
    }


    /**
     * Decides if a {@link Polyad} is evaluated by this evaluator and if not, returns false,
     * if so, it evaluates it and returns true. This function actually just dispatches it
     * to {@link #dispatch(Polyad, State)} where the work is done and manages putting better
     * trace information in if there is a failure.
     *
     * @param polyad
     * @param state
     * @return
     */
    public boolean evaluate(Polyad polyad, State state) {
        try {
            return dispatch(polyad, state);
        } catch (QDLException q) {
            throw q;
        } catch (Throwable t) {
            QDLExceptionWithTrace qq = new QDLExceptionWithTrace(t, polyad);
            throw qq;
        }
    }

    /**
     * Does the actual evaluation of the {@link Polyad}.
     *
     * @param polyad
     * @param state
     * @return
     */
    public abstract boolean dispatch(Polyad polyad, State state);

    public boolean evaluate(String alias, Polyad polyad, State state) {
        if (alias.equals(getNamespace())) {
            return evaluate(polyad, state);
        }
        return false;
    }

    public static boolean isStem(Object obj) {
        return StemUtility.isStem(obj);
    }

    public static boolean isSet(Object obj) {
        if(obj instanceof QDLValue) {
            return ((QDLValue)obj).isSet();
        }
        return obj instanceof QDLSet;
    }

    public static boolean isStemList(Object obj) {
        if(obj instanceof QDLValue) {
            obj = ((QDLValue)obj).getValue();
        }
        return isStem(obj) && ((QDLStem) obj).containsKey("0");
    }

    public static boolean isLong(Object obj) {
        if(obj instanceof QDLValue) {
            return ((QDLValue)obj).isLong();
        }
        return obj instanceof Long;
    }

    public static boolean isBoolean(Object obj) {
        if(obj instanceof QDLValue) {
            return ((QDLValue)obj).isBoolean();
        }
        return obj instanceof Boolean;
    }

    public static boolean areAllBoolean(Object... objects) {
        for (Object arg : objects) {
            if (!isBoolean(arg)) return false;
        }
        return true;
    }

    public static boolean areAllSets(Object... objects) {
        for (Object obj : objects) {
            if (!isSet(obj)) {
                return false;
            }
        }
        return true;
    }

    public static boolean areAllStems(Object... objects) {
        return StemUtility.areAllStems(objects);
    }

    public static boolean areNoneStems(Object... objects) {
        return StemUtility.areNoneStems(objects);
    }

    public static boolean isString(Object obj)
    {
        if(obj instanceof QDLValue) {
            return ((QDLValue)obj).isString();
        }
        return obj instanceof String;
    }

    public static boolean areAllStrings(Object... objects) {
        for (Object arg : objects) {
            if (!isString(arg)) return false;
        }
        return true;
    }

    public static boolean areAllLongs(Object... objects) {
        for (Object arg : objects) {
            if (!(arg instanceof Long)) return false;
        }
        return true;
    }

    public static boolean isNumber(Object arg) {
        if(arg instanceof QDLValue) {
            QDLValue val = (QDLValue)arg;
            return val.isLong() || val.isDecimal();
        }
        return (arg instanceof Long) || (arg instanceof BigDecimal);
    }

    public static boolean isBigDecimal(Object obj) {
        if(obj instanceof QDLValue) {
            return ((QDLValue)obj).isDecimal();
        }
        return obj instanceof BigDecimal;
    }

    public static boolean areAnyBigDecimals(Object... objects) {
        for (Object arg : objects) {
            if (isBigDecimal(arg)) return true;
        }
        return false;
    }

    public static boolean areAllBigDecimals(Object... objects) {
        for (Object arg : objects) {
            if (!isBigDecimal(arg)) return false;
        }
        return true;
    }

    /**
     * How to compare two big decimals requires some work.
     *
     * @param a
     * @param b
     * @return
     */
    protected boolean bdEquals(BigDecimal a, BigDecimal b) {
        BigDecimal r = a.subtract(b);
        return r.compareTo(BigDecimal.ZERO) == 0;
    }

    protected boolean areAllNumbers(Object... objects) {
        for (Object arg : objects) {
            if (!isNumber(arg)) return false;
        }
        return true;
    }

    public static BigDecimal toBD(Object obj) {
        if (!isNumber(obj)) throw new IllegalArgumentException("'" + obj + "' is not a number");
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Long) return new BigDecimal((Long) obj, OpEvaluator.getMathContext());
        if (obj instanceof Integer) return new BigDecimal((Integer) obj, OpEvaluator.getMathContext());
        throw new IllegalArgumentException("'" + obj + "' is not a number");
    }

    public static QDLStem toStem(Object object) {
        if(object instanceof QDLValue && ((QDLValue)object).isStem()) {
            return ((QDLValue)object).asStem();
        }
        if (isStem(object)) return (QDLStem) object;
        QDLStem out = new QDLStem();
        out.setDefaultValue(asQDLValue(object));
        return out;
    }


    /**
     * Function pointer class since this ishow youdo that in Java.
     */
    public static abstract class fPointer {
        public abstract fpResult process(Object... objects);

        public boolean isFirstArgumentMonadicMinus = false;
    }

    public static class fpResult {
        public QDLValue result;
    }

    public static void finishExpr(ExpressionImpl node, fpResult r) {
        node.setResult(r.result);
        node.setEvaluated(true);
    }

    /**
     * Main workhorse for monadic system functions. See the note in {@link #process2(ExpressionImpl, fPointer, String, State, boolean)}!
     *
     * @param polyad
     * @param pointer
     * @param name
     * @param state
     */
    public static void process1(ExpressionImpl polyad,
                                fPointer pointer,
                                String name,
                                State state) {
        if (polyad.getArgCount() == 0) {
            throw new MissingArgException(name + " requires at least 1 argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(name + " requires at most 1 argument", polyad.getArgAt(0));
        }
        QDLValue arg1 = polyad.evalArg(0, state);

        checkNull(arg1.getValue(), polyad.getArgAt(0), state);

        if (arg1.isSet()) {
            QDLSet argSet = arg1.asSet();
            QDLSet outSet = new QDLSet();
            processSet1(outSet, argSet, pointer);
            polyad.setResult(asQDLValue(outSet));
            polyad.setEvaluated(true);
            return;
        }
        if (!arg1.isStem()) {
            fpResult r = pointer.process(arg1.getValue(), state);
            finishExpr(polyad, r);
            return;
        }
        QDLStem stemVariable = arg1.asStem();
        QDLStem outStem = new QDLStem();
        processStem1(outStem, stemVariable, pointer);
        polyad.setResult(asQDLValue(outStem));
        polyad.setEvaluated(true);
    }

    /**
     * Processing stems for monadic functions
     *
     * @param outStem
     * @param stemVariable
     * @param pointer
     */
    public static void processStem1(QDLStem outStem, QDLStem stemVariable, fPointer pointer) {
        for (Object key : stemVariable.keySet()) {
            QDLValue qdlValue = stemVariable.get(key);
            boolean isLongKey = key instanceof Long;
            if (qdlValue.isStem()) {
                QDLStem newOut = new QDLStem();
                processStem1(newOut, qdlValue.asStem(), pointer);
                if (!newOut.isEmpty()) {
                    if (isLongKey) {
                        outStem.put((Long) key, asQDLValue(newOut));
                    } else {
                        outStem.put((String) key, asQDLValue(newOut));
                    }
                }
            } else {
                if (isLongKey) {
                    outStem.put((Long) key, pointer.process(stemVariable.get(key)).result);
                } else {
                    outStem.put((String) key, pointer.process(stemVariable.get(key)).result);
                }
            }
        }
    }


    public static void processSet1(QDLSet outSet, QDLSet arg, fPointer pointer) {
        for (QDLValue element : arg) {
            if (element.isStem()) {
                // Do something here???
            } else {
                outSet.add(pointer.process(element).result);
            }
        }
    }

    /**
     * Main workhorse method of evaluating a QDL dyadic system function. See note for
     * {@link #process2(ExpressionImpl, fPointer, String, State, boolean)} )}
     *
     * @param polyad
     * @param pointer
     * @param name
     * @param state
     */
    public static void process2(ExpressionImpl polyad,
                                fPointer pointer,
                                String name,
                                State state
    ) {
        process2(polyad, pointer, name, state, false);
    }

    /**
     * <h2>Note</h2>
     * This (and the other processN functions)
     * do all the magic of figuring out sets, stems, subsetting etc. You simply write a
     * function that implements {@link fPointer} that operates on a single pair of numbers.
     * <br/><br/>
     * <b>Tip</b>: You should check for arguments values in the fPointer, not before. Argument checks before
     * invoking this are often a lot more work to unpack. Just let the method do the work. Besides, you can throw
     * {@link BadArgException}s which are extremely exact at the point of failure.
     * <br/><br/>
     * <B>Tip</B>: OptionalArguments means that the {@link fPointer} an take more than 2 arguments.
     * So the basic functionality requires 2 args and there may be more.
     * <h3>Caveat</h3>
     * <p>This applies subsetting! So any functions that require something more exotic
     * cannot use this.</p>
     *
     * @param polyad
     * @param pointer
     * @param name
     * @param state
     * @param optionalArgs
     */
    public static void process2(ExpressionImpl polyad,
                                fPointer pointer,
                                String name,
                                State state,
                                boolean optionalArgs) {
        if (!optionalArgs && polyad.getArgCount() != 2) {
            throw new IllegalArgumentException(name + " requires 2 arguments");
        }
        QDLValue arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0), state);
        QDLValue arg2 = null;
        QDLException usx = null;
        try {
            if (!name.equals(OpEvaluator.IS_A)) { // special case is_a operator
                arg2 = polyad.evalArg(1, state);
                checkNull(arg2, polyad.getArgAt(1), state);
            }
            // CIL-1498 -- catch a generic exception here rather than unknown symbol or some such
            // since exceptions are now all wrapped. Basically only QDLException gets thrown any more.
        } catch (QDLException unknownSymbolException) {
            usx = unknownSymbolException;
        }
        // Short circuit dyadic logical && ||
        // We do allow for short circuiting if the second argument does not exist.
        // This is a common construct e.g. if[is_defined(x.) && size(x.) != 0]then[...
        if (arg1.isBoolean()) {
            if (polyad.getOperatorType() == OpEvaluator.OR_VALUE) {
                if (arg1.asBoolean()) { // if arg1 true then...
                    if ((usx != null) || !(isSet(arg2) || isStem(arg2))) {
                        polyad.setResult(new BooleanValue(Boolean.TRUE));
                        polyad.setEvaluated(true);
                        return;
                    }
                }
            }
            if (polyad.getOperatorType() == OpEvaluator.AND_VALUE) {
                if (!arg1.isBoolean()) { // if arg1 false then...
                    if ((usx != null) || !(isSet(arg2) || isStem(arg2))) {
                        polyad.setResult(new BooleanValue(Boolean.FALSE));
                        polyad.setEvaluated(true);
                        return;
                    }

                }
            }
        }
        if (usx != null) {
            throw usx;
        }
        Object[] argList = new Object[polyad.getArgCount()];
        argList[0] = arg1.getValue();
        argList[1] = arg2.getValue();
        if (optionalArgs) {
            for (int i = 2; i < polyad.getArgCount(); i++) {
                argList[i] = polyad.getArguments().get(i).evaluate(state).getValue();
            }
        }

        boolean isOneSet = false;
        boolean isTwoSets = false;

        boolean scalarRHS = false;
        QDLSet leftSet = null;
        QDLSet rightSet = null;
        if ((arg1.isSet()) && (arg2.isStem())) {
            if (arg2.asStem().isEmpty()) {
                arg2 = new SetValue();// make the empty set
            }
        }

        if ((arg2.isSet()) && (arg1.isStem())) {
            if (arg1.asStem().isEmpty()) {
                arg1 = new SetValue();// make the empty set
            }
        }

        if (arg1.isSet()) {
            if ((arg2.isSet())) {
                leftSet = arg1.asSet();
                rightSet = arg2.asSet();
                isTwoSets = true;
            } else {
                if (arg2.isStem()) {
                    throw new IllegalArgumentException("can only apply scalar operations on sets.");
                }
                isOneSet = true;
                scalarRHS = true;
            }
        }
        if ((leftSet == null) && arg2.isSet()) {
            if (arg1.isStem()) {
                throw new IllegalArgumentException("can only apply scalar operations on sets.");
            }
            isOneSet = true;
            scalarRHS = false;
        }
        if (isOneSet) {
            QDLSet outSet = new QDLSet();
            QDLSet inSet = (scalarRHS ? arg1 : arg2).asSet();
            Object scalar = scalarRHS ? arg2 : arg1;
            processSet2(outSet, inSet, scalar, scalarRHS, pointer, polyad, optionalArgs);
            polyad.setEvaluated(true);
            polyad.setResult(asQDLValue(outSet));
            return;
        }
        if (isTwoSets) {
            QDLSet outSet = new QDLSet();
            QDLValue result = processSet2(leftSet, rightSet, pointer, polyad, optionalArgs);
            polyad.setEvaluated(true);
            polyad.setResult(result);
            return;
        }

        if (areNoneStems(argList)) {
            fpResult result = pointer.process(argList);
            finishExpr(polyad, result);
            return;
        }
        QDLStem stem1 = toStem(arg1);
        QDLStem stem2 = toStem(arg2);
        QDLStem outStem = new QDLStem();
        processStem2(outStem, stem1, stem2, pointer, polyad, optionalArgs);
        polyad.setResult(asQDLValue(outStem));
        polyad.setEvaluated(true);
    }

    /**
     * Operations on two sets can return either a set (e.g. intersection) or a scalar (e.g. subset of)
     *
     * @param leftSet
     * @param rightSet
     * @param pointer
     * @param polyad
     * @param optionalArgs
     * @return
     */
    public static QDLValue processSet2(QDLSet leftSet, QDLSet rightSet, fPointer pointer, ExpressionImpl polyad, boolean optionalArgs) {
        fpResult r = null;
        Object[] objects;
        if (optionalArgs) {
            objects = new Object[polyad.getArgCount()];

        } else {
            objects = new Object[2];
        }
        objects[0] = leftSet;
        objects[1] = rightSet;
        if (optionalArgs) {
            for (int i = 2; i < objects.length; i++) {
                objects[i] = polyad.getArguments().get(i).getResult();
            }
        }
        if (isStem(objects[0]) || isStem(objects[1])) {
            throw new NotImplementedException("stems as elements of sets not implemented ");
        } else {
            r = pointer.process(objects);
            return r.result;
        }
    }

    /**
     * Apply a scalar to every element in a set.
     *
     * @param outSet
     * @param inSet
     * @param scalar
     * @param pointer
     * @param polyad
     * @param optionalArgs
     */
    public static void processSet2(QDLSet outSet, QDLSet inSet, Object scalar, boolean scalarRHS, fPointer pointer, ExpressionImpl polyad, boolean optionalArgs) {
        for (QDLValue element : inSet) {
            fpResult r = null;
            Object[] objects;
            if (optionalArgs) {
                objects = new Object[polyad.getArgCount()];

            } else {
                objects = new Object[2];
            }
            if (scalarRHS) {
                objects[0] = element.getValue();
                objects[1] = scalar;
            } else {
                objects[0] = scalar;
                objects[1] = element.getValue();

            }
            if (optionalArgs) {
                for (int i = 2; i < objects.length; i++) {
                    objects[i] = polyad.getArguments().get(i).getResult().getValue();
                }
            }
            if (isStem(objects[0]) || isStem(objects[1])) {
                throw new NotImplementedException("stems as elements of sets not implemented ");
            } else {
                r = pointer.process(objects);
                outSet.add(r.result);
            }
        }
    }

    /*
    For debugging
      d.0.0 := 5; d.0.1 := 4; d.1.0:=  3; d.1.1 := -2;
      c.0.0 := 2; c.0.1 := 1; c.1.0:= -7;  c.1.1 := 5;
      c. + d.
     */
    public static void processStem2(QDLStem outStem,
                                    QDLStem stem1,
                                    QDLStem stem2,
                                    fPointer pointer,
                                    ExpressionImpl polyad, boolean optionalArgs) {
        CommonKeyIterator iterator = getCommonKeys(stem1, stem2);
        // now we loop -- note that we must still preserve which is the first and second argument
        // so all this is basically to figure out how to loop over what.
        while (iterator.hasNext()) {
            Object key = iterator.next();
            fpResult r = null;
            Object[] objects;
            if (optionalArgs) {
                objects = new Object[polyad.getArgCount()];

            } else {
                objects = new Object[2];
            }
            objects[0] = stem1.get(key);
            objects[1] = stem2.get(key);
            if (optionalArgs) {
                for (int i = 2; i < objects.length; i++) {
                    objects[i] = polyad.getArguments().get(i).getResult();
                }
            }
            if (isStem(objects[0]) || isStem(objects[1])) {
                QDLStem newOut = new QDLStem();
                processStem2(newOut, toStem(objects[0]), toStem(objects[1]), pointer, polyad, optionalArgs);
                if (!newOut.isEmpty()) {
                    outStem.putLongOrString(key, asQDLValue(newOut));
                }
            } else {
                r = pointer.process(objects);
                outStem.putLongOrString(key, r.result);
            }
        }

    }

    /**
     * Main workhorse for evaluating QDL system valence 3 functions. See the note at
     * {@link #process2(ExpressionImpl, fPointer, String, State, boolean)}!
     *
     * @param polyad
     * @param pointer
     * @param name
     * @param state
     * @param optionalArguments
     */
    public static void process3(ExpressionImpl polyad,
                                fPointer pointer,
                                String name,
                                State state,
                                boolean optionalArguments) {
        if (!optionalArguments && polyad.getArgCount() != 3) {
            throw new IllegalArgumentException(name + " requires at least 3  arguments");
        }
        QDLValue arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0), state);
        QDLValue arg2 = polyad.evalArg(1, state);
        checkNull(arg2, polyad.getArgAt(1), state);

        QDLValue arg3 = polyad.evalArg(2, state);
        checkNull(arg3, polyad.getArgAt(3), state);
        Object[] argList = new Object[polyad.getArgCount()];
        argList[0] = arg1.getValue();
        argList[1] = arg2.getValue();
        argList[2] = arg3.getValue();
        if (optionalArguments) {
            for (int i = 2; i < polyad.getArgCount(); i++) {
                argList[i] = polyad.getArguments().get(i).evaluate(state).getValue();
            }
        }
        if (areNoneStems(argList)) {
            fpResult result = pointer.process(argList);
            finishExpr(polyad, result);
            return;
        }
        // Convert to stems as needed
        QDLStem stem1 = toStem(arg1);
        QDLStem stem2 = toStem(arg2);
        QDLStem stem3 = toStem(arg3);
        QDLStem outStem = new QDLStem();
        processStem3(outStem, stem1, stem2, stem3, pointer, polyad, true);
        polyad.setResult(asQDLValue(outStem));
        polyad.setEvaluated(true);
    }


    public static void processStem3(QDLStem outStem,
                                    QDLStem stem1,
                                    QDLStem stem2,
                                    QDLStem stem3,
                                    fPointer pointer,
                                    ExpressionImpl polyad, boolean optionalArgs) {
        CommonKeyIterator iterator = getCommonKeys(stem1, stem2, stem3);
        // now we loop -- note that we must still preserve which is the first and second argument
        // so all this is basically to figure out how to loop over what.
        while (iterator.hasNext()) {
            Object key = iterator.next();
            fpResult r = null;
            Object[] objects;
            if (optionalArgs) {
                objects = new Object[polyad.getArgCount()];
            } else {
                objects = new Object[3];
            }
            objects[0] = stem1.get(key).getValue();
            objects[1] = stem2.get(key).getValue();
            objects[2] = stem3.get(key).getValue();
            if (optionalArgs) {
                for (int i = 3; i < objects.length; i++) {
                    objects[i] = polyad.getArguments().get(i).getResult().getValue();
                }
            }

            if (objects[0] instanceof QDLStem) {
                QDLStem newOut = new QDLStem();
                processStem3(newOut,
                        toStem(objects[0]),
                        toStem(objects[1]),
                        toStem(objects[2]),
                        pointer, polyad, optionalArgs);
                if (!newOut.isEmpty()) {
                    outStem.putLongOrString(key, asQDLValue(newOut));
                }
            } else {
                r = pointer.process(objects);
                outStem.putLongOrString(key, r.result);
            }
        }

    }

    /**
     * An iterator for the keys that are common between stems. These are the keys of the
     * final result of operations on stems. Contract is that this has a list of keys
     * and {@link #add(StemKeys)} will add the set to the list. When this iterates, it
     * iterates over this list and looks for a key.
     */

    public static class CommonKeyIterator implements Iterator {
        ArrayList<StemKeys> stemKeys = new ArrayList<>();

        public void add(StemKeys keys) {
            if (smallestKeys == null) {
                smallestKeys = keys;
            } else {
                //keys.removeAll(smallestKeys); // Don't add redundant keys since that just adds un-needed looping
                smallestKeys = keys.size() < smallestKeys.size() ? keys : smallestKeys;
                stemKeys.add(keys);
            }
        }

        StemKeys smallestKeys = null;
        Iterator iterator = null;

        Object nextValue;

        @Override
        public boolean hasNext() {
            if (iterator == null) {
                if (smallestKeys == null) {
                    return false;
                }
                iterator = smallestKeys.iterator();
            }

            while (iterator.hasNext()) {
                Object v = iterator.next();
                if (allHaveValue(v)) {
                    nextValue = v;
                    return true;
                }
            }
            return false;


        }

        /**
         * Since {@link #next()} has to peek inside of the iterator to tell if the
         * next key is really valid, {@link #hasNext()} consumes the iterator. This resets the iterator
         * so {@link #hasNext()} works from the start.
         */
        protected void reset() {

        }

        protected boolean allHaveValue(Object value) {
            for (StemKeys keys : stemKeys) {
                if (!keys.contains(value)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object next() {
            return nextValue;
        }
    }

    public static CommonKeyIterator getCommonKeys(QDLStem... stems) {
        CommonKeyIterator iterator = new CommonKeyIterator();
        boolean gotKeys = false;
        StemKeys stemKey = new StemKeys();
        if (stems[0].hasDefaultValue()) {
            stemKey.addAll(stems[1].keySet());
            gotKeys = true;
        }
        if (stems[1].hasDefaultValue()) {
            stemKey.addAll(stems[0].keySet());
            gotKeys = true;
        }
        if (gotKeys) {
            iterator.add(stemKey);
            return iterator;
        }
        for (QDLStem stem : stems) {
            if (stem.hasDefaultValue()) {
                continue;
            }
            if (!stem.isEmpty()) {
                iterator.add(stem.keySet());
            }
        }
        // edge case. E.g. all the stems are empty
        if (iterator.smallestKeys == null) {
            iterator.smallestKeys = new StemKeys();
        }
        return iterator;
    }

    /**
     * This will take an {@link ExpressionImpl} that should contain a stem, check the reference and it the stem
     * does not exist, create and put it in the symbol table. If the stem exists, it just returns it.
     * This lets you do things like issue:
     * <pre>
     *     foo. := null
     *     if[
     *        some_condition
     *     ]then[
     *        list_append(foo., 4);
     *     // ... other stuff
     *     ];
     * </pre>
     * and not get a null pointer exception. This is needed especially if a the command is issued in a different scope,
     * e.g. in a conditional block to assign the value.<br/><br/>
     * This <b><i>WILL</i></b> throw an exception if the argument is not a stem!! So this is invoked where
     * there is a required stem that is missing and should be there.
     *
     * @param node
     * @param state
     * @param informativeMessage
     * @return
     */
    protected QDLStem getOrCreateStem(ExpressionInterface node, State state, String informativeMessage) {
        QDLStem stem1 = null;
        if (node instanceof VariableNode) {
            VariableNode vn = (VariableNode) node;
            String varName = vn.getVariableReference();
            if (!state.isDefined(varName)) {
                if (!varName.endsWith(QDLStem.STEM_INDEX_MARKER)) {
                    throw new IllegalArgumentException(informativeMessage);
                }
                stem1 = new QDLStem();
                state.setValue(varName, stem1);
            } else {
                Object arg1 = node.evaluate(state);
                if (!isStem(arg1)) {
                    throw new IllegalArgumentException(informativeMessage);
                }
                stem1 = (QDLStem) arg1;
            }
        }
        if (stem1 == null) {
            throw new MissingArgumentException("the first argument is not a variable in this workspace.");
        }
        return stem1;
    }

    /**
     * This will look at the resource name and decide if it is in a VFS and resolve it against that.
     * If not, it will try to resolve it as a file name against the file system if this
     * is not in server mode. This merely returns a null if there is no such resource. It will
     * throw an exception if the resource refers to a virtual file and there are no providers
     * for that namespace.
     *
     * @param resourceName
     * @param state
     * @return
     */
    public VFSEntry resolveResourceToFile(String resourceName, int type, State state) {
        if (state.isVFSFile(resourceName)) {
            if (!state.hasVFSProviders()) {
                throw new QDLException("unkonwn virtual file system for resource '" + resourceName + "'");
            }
            try {
                return state.getFileFromVFS(resourceName, type);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new QDLException("could not file from VFS:" + t.getMessage(), t);
            }
        }
        return null;
    }

    /**
     * get a polyad or dyad (for the operator) from the {@link FunctionReferenceNode}.
     * You must still set any arguments, but the type and name should be correctly set.
     *
     * @param state
     * @param frNode
     * @return
     */
    public static ExpressionImpl getOperator(State state, FunctionReferenceNodeInterface frNode, int nAry) {
        return OLDgetOperator(state, frNode, nAry);
    }

    protected static ExpressionImpl NEWgetOperator(State state, FunctionReferenceNodeInterface frNode, int nAry) {
        UserFunction operator = null;
        String operatorName = frNode.getFunctionName();
        if (state.getOpEvaluator().isMathOperator(operatorName)) {
            if (nAry == 1) {
                return new Monad(state.getOperatorType(operatorName), false); // only post fix allowed for monads here
            } else {
                return new Dyad(state.getOperatorType(operatorName));
            }
        }
        if (state.getMetaEvaluator().isBuiltInFunction(operatorName)) {
            return new Polyad(operatorName);
        }
        return operator;
    }

    protected static ExpressionImpl OLDgetOperator(State state, FunctionReferenceNodeInterface frNode, int nAry) {
        ExpressionImpl operator;
        String operatorName = frNode.getFunctionName();
        if (state.getOpEvaluator().isMathOperator(operatorName)) {
            if (nAry == 1) {
                operator = new Monad(state.getOperatorType(operatorName), false); // only post fix allowed for monads here
            } else {
                operator = new Dyad(state.getOperatorType(operatorName));
            }
        } else {
            if (state.getMetaEvaluator().isBuiltInFunction(operatorName)) {
                operator = new Polyad(operatorName);
            } else {
                FunctionRecordInterface fri;
                if (frNode instanceof DyadicFunctionReferenceNode) {
                    DyadicFunctionReferenceNode dfr = (DyadicFunctionReferenceNode) frNode;
                    dfr.evaluate(state);
                    fri = dfr.getFunctionRecord();
                } else {
                    FR_WithState fr_withState = state.resolveFunction(operatorName, nAry, true); // It's a dyad!
                    if (fr_withState == null || fr_withState.functionRecord == null) {
                        if (frNode.isAnonymous()) {
                            List<FunctionRecordInterface> anons = state.getFTStack().getByAllName(frNode.getFunctionName());
                            for (FunctionRecordInterface anon : anons) {
                                state.getFTStack().remove(anon.getKey());
                            }
                            throw new UndefinedFunctionException("no anonymous lambda function is defined with " + nAry + " arguments", null);
                        }
                        throw new UndefinedFunctionException("'" + operatorName + "' is not defined with " + nAry + " arguments", null);
                    }
                    fri = fr_withState;
                }
// ((x)→x^2)∀[;5]
                UserFunction userFunction = new UserFunction(operatorName);
                userFunction.setBuiltIn(false); // or it will not execute!
                userFunction.setFunctionRecord(fri);
                operator = userFunction;
            }
        }
        return operator;
    }

    /*
     if(frn instanceof DyadicFunctionReferenceNode){
                    ((DyadicFunctionReferenceNode) frn).evaluate(state);
                    //((DyadicFunctionReferenceNode) frn).evalArg(0,state);
                    Object ooo = ((DyadicFunctionReferenceNode) frn).getArgAt(0).getResult();
                    if(ooo instanceof Long){
                        argCount = ((Long) ooo).intValue();
                        if(argCount !=1 && argCount !=2){
                            throw new ExtraArgException(PICK + " function reference has illegal valence, must be 1 or 2.", polyad.getArgAt(0));
                        }
                    }else{
                        throw new ExtraArgException(PICK + " function reference has non-integer valence", polyad.getArgAt(0));
                    }
                }else{
                    // not qualified. Try and find the right one
                    List<FunctionRecordInterface> functionRecordList = state.getFTStack().getByAllName(frn.getFunctionName());
                    if (functionRecordList.isEmpty()) {
                        throw new UndefinedFunctionException("no functions found for pick function at all.", polyad.getArgAt(0));
                    }
                    int totalCount = 0;
                    for (FunctionRecordInterface fr : functionRecordList) {

                        if (2 == fr.getArgCount()) {
                            totalCount = totalCount +2;
                        }
                        if (1 == fr.getArgCount()) {
                            totalCount = totalCount +1;
                        }

                        argCount = Math.max(argCount, fr.getArgCount());
                    }
                    if(totalCount == 0 || totalCount == 3){
                        // then there are multiple functions with the name and different valences. Don't
                        // try to choose one, just throw an exception.
                        throw new BadArgException(PICK + " unqualified function reference, both monad and dyad found. Specify which to use.", polyad.getArgAt(0));
                    }
                }

     */
    public static final int FILE_OP_AUTO = -100; // Let the system determine it.
    public static final int FILE_OP_BINARY = 0; // file is treated as b64 string
    public static final int FILE_OP_TEXT_STEM = 1; //File is treated as a stem of lines
    public static final int FILE_OP_TEXT_INI = 2; //File is treated as an initialization file
    public static final int FILE_OP_TEXT_WITHOUT_LIST_INI = 3; //File is treated as an initialization file, no lists allowed
    public static final int FILE_OP_TEXT_STRING = -1; // File is treated as one long string
    public static final int FILE_OP_INPUT_STREAM = -2; // File is returned as a Reader. This is for internal use only!

    /**
     * Create an unused name for a function. Note that this <i>cannot</i>
     * produce a legal function name since the base 32 encoding slaps on trailing
     * "=". This assures there will never be a collision with the ambient
     * state.
     *
     * @param state
     * @return
     */
    public static String tempFname(State state) {
        Base32 base32 = new Base32((byte) '=');
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        String tempName = base32.encodeToString(bytes);
        for (int i = 0; i < 10; i++) {
            if (!state.getFTStack().containsKey(new FKey(tempName, -1))) {
                return tempName;
            }
            tempName = base32.encodeToString(bytes);

        }
        throw new IllegalStateException("cannot create anonymous function");

    }
    //  pick((k)->3<k<10, |>mod(random(20),11))

    /**
     * Checks if the argument is some form of a function reference. This lets you test for
     * overloading before invoking one of {@link #getFunctionReferenceNode(State, ExpressionInterface)}
     *
     * @param arg0
     * @return
     */
    public boolean isFunctionRef(ExpressionInterface arg0) {
        return (arg0 instanceof LambdaDefinitionNode) || (arg0 instanceof FunctionDefinitionStatement) || (arg0 instanceof FunctionReferenceNode);
    }

    /**
     * <p></p>
     * This will take a node that is either a parenthesized expression, a {@link FunctionNodeInterface} such as
     * {@link FunctionReferenceNode}, {@link DyadicFunctionReferenceNode} or {@link LambdaDefinitionNode}
     * or is {@link ModuleExpression}
     * and determine the right {@link FunctionReferenceNode},
     * updating the state (including adding local state as needed for the duration of the evaluation).
     * It will also throw an exception if the argument is not of the right type.<p/><p/>
     * Any place you want to use a function as an argument, pass it to this and let
     * it do the work.
     * </p>
     * <p>
     * This also does some housekeeping, like assigning random names locally to anonymous lambda
     * functions. Be <i>sure</i> to remove them from any state table if you need to when done.
     * If they are in local state only (usual case), then no need to.
     * </p>
     *
     * @param state        - The current state
     * @param arg0         - the function definition, etc.
     * @param pushNewState - create a new {@link FTable} and add this function. If this is being called as part of e.g. evaluating
     *                     a function, it should probably set true
     * @return
     */
    public FunctionReferenceNodeInterface getFunctionReferenceNode(State state, ExpressionInterface arg0, boolean pushNewState) {
        FunctionReferenceNodeInterface frn = null;
        while (arg0.getNodeType() == ExpressionInterface.PARENTHESIZED_NODE) {
            arg0 = ((ParenthesizedExpression) arg0).getExpression();
        }
        //   g(x,y,n)->x^n+y^n ;((@g))∀[4,[;5],1]

        switch (arg0.getNodeType()) {
            case ExpressionInterface.LAMBDA_DEFINITION_NODE:
                LambdaDefinitionNode lds = (LambdaDefinitionNode) arg0;
                if (!lds.hasName()) {
                    lds.getFunctionRecord().setName(tempFname(state));
                    lds.getFunctionRecord().setAnonymous(true);
                }
                if (pushNewState) {
                    FTable ft = new FTable();
                    ft.put(lds.getFunctionRecord());
                    state.getFTStack().push(ft);
                } else {
                    lds.evaluate(state);
                }
                FunctionReferenceNode functionReferenceNode = new FunctionReferenceNode();
                functionReferenceNode.setFunctionName(lds.getFunctionRecord().getName());
                functionReferenceNode.setAnonymous(lds.getFunctionRecord().isAnonymous());
                functionReferenceNode.getFunctionRecords().add(lds.getFunctionRecord());
                frn = functionReferenceNode;
                break;
            case ExpressionInterface.FUNCTION_REFERENCE_NODE:
            case ExpressionInterface.DYADIC_FUNCTION_REFERENCE_NODE:
                frn = (FunctionReferenceNodeInterface) arg0;
                break;
            case ExpressionInterface.MODULE_NODE:
                ModuleExpression moduleExpression = (ModuleExpression) arg0;
                Object r = moduleExpression.evaluate(state);
                while (!(r instanceof FunctionReferenceNode)) {
                    if (r instanceof ModuleExpression) {
                        r = ((ModuleExpression) r).getExpression();
                    }
                }
                if (r instanceof FunctionReferenceNodeInterface) {
                    frn = (FunctionReferenceNodeInterface) r;
                }
                break;
        }

/*   ????? Don't think this case can happen from the now current inheritence structure. Leave
           for now in case it comes back to haunt us.
        if ((arg0 instanceof FunctionDefinitionStatement)) {
            LambdaDefinitionNode lds = new LambdaDefinitionNode(((FunctionDefinitionStatement) arg0));
            if (!lds.hasName()) {
                lds.getFunctionRecord().setName(tempFname(state));
                lds.getFunctionRecord().setAnonymous(true);
            }
            if (pushNewState) {
                //FunctionTableImpl ft = new FunctionTableImpl();
                FTable ft = new FTable();
                ft.put(lds.getFunctionRecord());
                state.getFTStack().push(ft);
            } else {
                lds.evaluate(state);
            }
            frn = new FunctionReferenceNode();
            frn.setFunctionName(lds.getFunctionRecord().name);
            frn.setAnonymous(lds.getFunctionRecord().isAnonymous());

        }*/
        if (frn == null) {
            throw new IllegalArgumentException("the argument is not a function reference or lambda");
        }
        return frn;
    }

    /*
     f(x)->x^2
g(@z, y)->y*z(y)
g(1@f, 2)
     */
    public boolean isScalar(Object arg) {
        return !isStem(arg) && !isSet(arg);
    }

    /**
     * Takes a list of Java objects and converts them to QDL constants to be used as
     * arguments to functions. Checks also that there are no illegal values first.
     *
     * @param objects
     * @return
     */
    public static ArrayList<ExpressionInterface> toConstants(ArrayList<Object> objects) {
        ArrayList<ExpressionInterface> args = new ArrayList<>();
        for (Object obj : objects) {
            int type = Constant.getType(obj);
            if (type == UNKNOWN_TYPE) {
                // Future proofing in case something changes in the future internally
                throw new IllegalArgumentException(" unknown object type");
            }
            args.add(new ConstantNode(QDLValue.asQDLValue(obj)));
        }
        return args;
    }

    protected FunctionReferenceNodeInterface getFunctionReferenceNode(State state, ExpressionInterface arg0) {
        return getFunctionReferenceNode(state, arg0, false);
    }

    /**
     * If a function gets an argument which should not be a Java null, then this will
     * try to track down the variable reference.
     *
     * @param arg
     * @param swri
     */
    public static void checkNull(Object arg, ExpressionInterface swri) {
        if (arg != null) {
            return;
        }
        if (swri instanceof VariableNode) {
            VariableNode vNode = (VariableNode) swri;
            throw new UnknownSymbolException("unknown symbol '" + vNode.getVariableReference() + "'", vNode);
        }
        throw new UnknownSymbolException("unknown symbol", swri);
    }

    /**
     * Check for Java nulls and logs any errors
     *
     * @param arg1
     * @param swri
     * @param state
     */
    public static void checkNull(Object arg1, ExpressionInterface swri, State state) {
        if (arg1 == null) {
            UnknownSymbolException unknownSymbolException;
            String message = "unknown symbol";
            if (swri instanceof VariableNode) {
                message = message + " '" + ((VariableNode) swri).getVariableReference() + "'";
                unknownSymbolException = new UnknownSymbolException(message, swri);
            } else {
                unknownSymbolException = new UnknownSymbolException(message, swri);
            }
            if (state.getLogger() != null) {
                // Check they have logging in the first place before writing to it.
                state.getLogger().error(message);
            }
            throw unknownSymbolException;
        }
    }

    static protected int[] bigArgList = null;
    static protected int[] bigArgList0 = null;

    public static int MAX_ARG_COUNT = 10;

    /**
     * Used in arg count queries. Returns [1,2,... {@link #MAX_ARG_COUNT}].
     * Note that this does not limit argument lists, but it used in dereferencing
     * function references. See {@link FunctionEvaluator#resolveArguments(FunctionRecordInterface, Polyad, State, State)}.
     *
     * @return
     */
    protected static int[] getBigArgList() {
        if (bigArgList == null) {
            bigArgList = new int[MAX_ARG_COUNT];
            for (int i = 1; i < MAX_ARG_COUNT + 1; i++) {
                bigArgList[i - 1] = i;
            }
        }
        return bigArgList;
    }

    /**
     * returns integers [0,1,..., {@link #MAX_ARG_COUNT}
     *
     * @return
     */
    protected static int[] getBigArgList0() {
        if (bigArgList0 == null) {
            bigArgList0 = new int[MAX_ARG_COUNT + 1];
            for (int i = 0; i < MAX_ARG_COUNT + 1; i++) {
                bigArgList0[i] = i;
            }
        }
        return bigArgList0;
    }

    /**
     * Converts a couple of different arguments to the form
     * [[a0{,b0}],[a1{,b1}],...,[an{,bn}] or (if a single argument that is
     * a stem) can pass back:
     * <p>
     * {key0:[[a0{,b0}], key1:[a1{,b1}],...}
     * <p>
     * where the bk are optional. All ak, bk are strings.
     * a,b -> [[a,b]] (pair of arguments, function is dyadic
     * [a,b] ->[[a,b]] (simple list, convert to nested list
     * [a0,a1,...] -> [[a0],[a1],...] allow for scalars
     * Use in both module import and load for consistent arguments
     *
     * @param polyad
     * @param state
     * @param component
     * @return
     */
    protected QDLStem convertArgsToStem(Polyad polyad, Object arg, State state, String component) {
        QDLStem argStem = null;

        boolean gotOne = false;

        switch (polyad.getArgCount()) {
            case 0:
                throw new MissingArgException(component + " requires an argument", polyad);
            case 1:
                // single string arguments
                if (isString(arg)) {
                    argStem = new QDLStem();
                    argStem.listAdd(asQDLValue(arg));
                    gotOne = true;
                }
                if (isStem(arg)) {
                    argStem = (QDLStem) arg;
                    gotOne = true;
                }
                break;
            case 2:
                if (!isString(arg)) {
                    throw new BadArgException("Dyadic " + component + " requires string arguments only", polyad.getArgAt(0));
                }
                Object arg2 = polyad.evalArg(1, state);
                checkNull(arg2, polyad.getArgAt(1), state);
                if (!isString(arg2)) {
                    throw new BadArgException("Dyadic " + component + " requires string arguments only", polyad.getArgAt(1));
                }

                argStem = new QDLStem();
                QDLStem innerStem = new QDLStem();
                innerStem.listAdd(asQDLValue(arg));
                innerStem.listAdd(asQDLValue(arg2));
                argStem.put(0L, asQDLValue(innerStem));
                gotOne = true;
                break;
            default:
                throw new ExtraArgException(component + ": too many arguments", polyad.getArgAt(2));
        }
        if (!gotOne) {
            throw new BadArgException(component + ": unknown argument type", polyad);
        }
        return argStem;
    }

}
