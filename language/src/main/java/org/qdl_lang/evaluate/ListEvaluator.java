package org.qdl_lang.evaluate;

import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.ExpressionImpl;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FunctionRecordInterface;
import org.qdl_lang.functions.FunctionReferenceNodeInterface;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.qdl_lang.state.NamespaceAwareState.NS_DELIMITER;
import static org.qdl_lang.variables.StemUtility.axisWalker;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 9/30/21 at  5:07 PM
 */
public class ListEvaluator extends AbstractEvaluator {
    public static final String LIST_NAMESPACE = "list";
    public static final String LIST_FQ = LIST_NAMESPACE + NS_DELIMITER;
    public static final int LIST_BASE_VALUE = 10000;
    public static final String LIST_INSERT_AT = "insert_at";
    public static final String LIST_INSERT_AT2 = "list_insert_at";
    public static final int LIST_INSERT_AT_TYPE = 1 + LIST_BASE_VALUE;

    public static final String LIST_SUBSET = "sublist";
    public static final int LIST_SUBSET_TYPE = 2 + LIST_BASE_VALUE;

    public static final String LIST_COPY = "list_copy";
    public static final String LIST_COPY2 = "copy";
    public static final int LIST_COPY_TYPE = 3 + LIST_BASE_VALUE;

    public static final String LIST_STARTS_WITH = "starts_with";
    public static final String LIST_STARTS_WITH2 = "list_starts_with";
    public static final int LIST_STARTS_WITH_TYPE = 6 + LIST_BASE_VALUE;

    public static final String LIST_REVERSE = "reverse";
    public static final String LIST_REVERSE2 = "list_reverse";
    public static final int LIST_REVERSE_TYPE = 7 + LIST_BASE_VALUE;

    public static final String LIST_SORT = "sort";
    public static final int LIST_SORT_TYPE = 8 + LIST_BASE_VALUE;

    public static final String PICK = "pick";
    public static final int PICK_TYPE = 9 + LIST_BASE_VALUE;


    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{
                    LIST_SORT,
                    LIST_INSERT_AT,
                    LIST_SUBSET,
                    LIST_COPY,
                    LIST_REVERSE,
                    LIST_STARTS_WITH,
                    PICK
            };
        }
        return fNames;
    }

    @Override
    public boolean dispatch(Polyad polyad, State state) {
        switch (polyad.getName()) {
            case LIST_SORT:
                doListSort(polyad, state);
                return true;
            case LIST_COPY:
            case LIST_COPY2:
                doListCopyOrInsert(polyad, state, false);
                return true;
            case LIST_INSERT_AT:
            case LIST_INSERT_AT2:
                doListCopyOrInsert(polyad, state, true);
                return true;
            case PICK:
                return doPickSubset(polyad, state);
            case LIST_SUBSET:
                return doListSubset(polyad, state);
            case LIST_REVERSE:
            case LIST_REVERSE2:
                doListReverse(polyad, state);
                return true;
            case LIST_STARTS_WITH:
            case LIST_STARTS_WITH2:
                doListStartsWith(polyad, state);
                return true;
        }
        return false;
    }


    @Override
    public int getType(String name) {
        switch (name) {
            case LIST_SORT:
                return LIST_SORT_TYPE;
            case LIST_COPY:
            case LIST_COPY2:
                return LIST_COPY_TYPE;
            case LIST_REVERSE:
            case LIST_REVERSE2:
                return LIST_REVERSE_TYPE;
            case LIST_STARTS_WITH:
            case LIST_STARTS_WITH2:
                return LIST_STARTS_WITH_TYPE;
            case LIST_INSERT_AT:
            case LIST_INSERT_AT2:
                return LIST_INSERT_AT_TYPE;
            case LIST_SUBSET:
                return LIST_SUBSET_TYPE;
            case PICK:
                return PICK_TYPE;
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }


    @Override
    public String getNamespace() {
        return LIST_NAMESPACE;
    }

    /**
     * Always returns a sorted list.
     *
     * @param polyad
     * @param state
     */
    protected void doListSort(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(LIST_SORT + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(LIST_SORT + " requires at most 2 arguments", polyad.getArgAt(2));
        }
        // Contract is to take everything in a list sort it and return it.
        Object arg0 = polyad.evalArg(0, state);
        ArrayList<QDLValue> list = null;
        switch (Constant.getType(arg0)) {
            case Constant.SET_TYPE:
                list = new ArrayList();
                list.addAll((QDLSet) arg0);
                break;
            case Constant.STEM_TYPE:
                QDLStem inStem = (QDLStem) arg0;
                if (inStem.isList()) {
                    list = inStem.getQDLList().values(); // fast
                } else {
                    list = new ArrayList();
                    list.addAll(inStem.values());
                }
                break;
            default:
                QDLStem stemVariable = new QDLStem();
                stemVariable.put(0, asQDLValue(arg0));
                polyad.setEvaluated(true);
                polyad.setResult(stemVariable);
                return;
        }
        boolean sortUp = true;
        if (polyad.getArgCount() == 2) {
            Object arg1 = polyad.evalArg(1, state);
            if (arg1 instanceof Boolean) {
                sortUp = (Boolean) arg1;
            } else {
                throw new BadArgException(LIST_SORT + " requires a boolean as it second argument if present", polyad.getArgAt(1));
            }
        }
        try {
            doSorting(list, sortUp);
            QDLStem output = new QDLStem((long) list.size(), list.toArray(new QDLValue[list.size()]));
            polyad.setEvaluated(true);
            polyad.setResult(output);
            return;
        } catch (ClassCastException classCastException) {

        }
        // So there is mixed information. we'll have to do this the hard way....
        ArrayList numbers = new ArrayList();
        ArrayList strings = new ArrayList();
        ArrayList others = new ArrayList();
        ArrayList booleans = new ArrayList();
        ArrayList nulls = new ArrayList();
        for (Object element : list) {
            switch (Constant.getType(element)) {
                case Constant.STRING_TYPE:
                    strings.add(element);
                    break;
                case Constant.DECIMAL_TYPE:
                    numbers.add(element);
                    break;
                case Constant.LONG_TYPE:
                    // BigDecimals and longs are not comparable. Have to fudge it
                    numbers.add(BigDecimal.valueOf((long) element));
                    break;
                case Constant.BOOLEAN_TYPE:
                    booleans.add(element);
                    break;
                case Constant.NULL_TYPE:
                    nulls.add(element);
                    break;
                default:
                    others.add(element);
                    break;
            }
        }
        doSorting(nulls, sortUp);
        doSorting(numbers, sortUp);
        doSorting(strings, sortUp);
        doSorting(booleans, sortUp);
        list.clear();
        if (sortUp) {
            list.addAll(nulls);
            list.addAll(booleans);
            list.addAll(strings);
            list.addAll(numbers);
            list.addAll(others);
        } else {
            list.addAll(others);
            list.addAll(numbers);
            list.addAll(strings);
            list.addAll(booleans);
            list.addAll(nulls);

        }
        QDLStem output = new QDLStem((long) list.size(), list.toArray(new QDLValue[list.size()]));
        polyad.setEvaluated(true);
        polyad.setResult(output);
    }

    private void doSorting(ArrayList list, boolean sortUp) {
        if (sortUp) {
            Collections.sort(list);
        } else {
            Collections.sort(list, Collections.reverseOrder());
        }
    }

    /*
        copy(source., startIndex, length, target., targetIndex)
                                           ^ targetArgIndex
     */
    protected void doListCopyOrInsert(Polyad polyad, State state, boolean doInsert) {
        // Fixes https://github.com/ncsa/qdl/issues/31
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3, 45});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires at least 2 arguments", polyad);
        }

        if (5 < polyad.getArgCount()) {
            throw new ExtraArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires at most 5 arguments", polyad.getArgAt(5));
        }
        Object arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0));

        if (!isStem(arg1)) {
            throw new BadArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires a stem as its first argument", polyad.getArgAt(0));
        }
        QDLStem sourceStem = (QDLStem) arg1;
        QDLStem targetStem = null;
        Long targetIndex = 0L;
        int targetArgIndex = 5;
        // There are a couple of cases for the targetStem. It can be just an expression to evaluate like [;5] or
        // it may be a variable. If the variable does not exist, then create it (that's the trick). So we have
        // to check if the argument is a variable node and take action accordingly.
        Object ooo = checkCopyNode(polyad.getLastArg(), state, doInsert);
        if(ooo instanceof QDLStem){
            targetStem = (QDLStem) ooo;
            targetIndex = 0L;
            targetArgIndex = polyad.getArgCount() - 1;
        }else{
            if(!(ooo instanceof Long)){
                throw new BadArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires an integer as its last argument", polyad.getArgAt(4));
            }
            targetIndex = (Long)ooo;
            ooo = checkCopyNode(polyad.getArgAt(polyad.getArgCount()-2), state, doInsert);
            if(!(ooo instanceof QDLStem)){
                throw new BadArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires a stem as its next to lat argument", polyad.getArgAt(4));
            }
            targetStem = (QDLStem)ooo;
            if(targetIndex < 0L){
                targetIndex = targetIndex + targetStem.size();
            }
            targetArgIndex = polyad.getArgCount() - 2;
        }
        // Now for the other arguments, if any.
        Long startIndex = 0L;
        long length = sourceStem.size();
        if (1 < targetArgIndex) {
            Object arg2 = polyad.evalArg(1, state);
            checkNull(arg2, polyad.getArgAt(1));
            if (!isLong(arg2)) {
                throw new BadArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires an integer as its second argument", polyad.getArgAt(1));
            }
            startIndex = (Long) arg2;
            length = length - startIndex; // just take the rest of the stem.
        }
        if (2 < targetArgIndex) {
            Object arg3 = polyad.evalArg(2, state);
            checkNull(arg3, polyad.getArgAt(2));

            if (!isLong(arg3)) {
                throw new BadArgException((doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires an integer as its third argument", polyad.getArgAt(2));
            }
            length = (Long) arg3;
            if(length <0){
                length = length + sourceStem.size();
            }
        }

        if (doInsert) {
            sourceStem.listInsertAt(startIndex, length, targetStem, targetIndex);
        } else {
            sourceStem.listCopy(startIndex, length, targetStem, targetIndex);
        }
        polyad.setResult(targetStem);
        polyad.setEvaluated(true);
    }

    protected Object checkCopyNode(ExpressionInterface expr, State state, boolean doInsert) {
        if (expr instanceof VariableNode) {
            // If the last argument is a stem, then supply default targetIndex
            return getOrCreateStem(expr,
                    state, (doInsert ? LIST_INSERT_AT : LIST_COPY2) + " requires a stem as its target argument"
            );
        }
            return expr.evaluate(state); // May be a stem or Long
    }

    /*
          subset(3*[;15], {'foo':3,'bar':5,'baz':7})
          subset(3*[;15], 2*[;5]+1)
          a. := n(3,4,n(12))
          remap(a., [[0,1],[1,1],[2,3]])
       [1,5,11]
          remap(a., {'foo':[0,1],'bar':[1,1], 'baz':[2,3]})
      {bar:5, foo:1, baz:11}
       */
    protected boolean doListSubset(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2, 3});
            polyad.setEvaluated(true);
            return true;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(LIST_SUBSET + " requires at least 2 arguments", polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(LIST_SUBSET + " requires at most 3 arguments", polyad.getArgAt(3));
        }


        Object arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0));
        if (isScalar(arg1)) {
            QDLList qdlList = new QDLList();
            qdlList.add(asQDLValue(arg1));
            QDLStem stemVariable = new QDLStem();
            stemVariable.setQDLList(qdlList);
            polyad.setResult(stemVariable);
            polyad.setEvaluated(true);
            return true;
        }
        Object arg2 = polyad.evalArg(1, state);
        checkNull(arg2, polyad.getArgAt(1));
        if (!isLong(arg2)) {
            throw new BadArgException(LIST_SUBSET + " requires an integer as its second argument", polyad.getArgAt(1));
        }
        Object arg3 = null;
        if (polyad.getArgCount() == 3) {
            arg3 = polyad.evalArg(2, state);
            checkNull(arg3, polyad.getArgAt(2));

            if (!isLong(arg3)) {
                throw new BadArgException(LIST_SUBSET + " requires an integer as its third argument", polyad.getArgAt(2));
            }
        }
        QDLStem stem = null;
        QDLSet set = null;

        long startIndex = 0L;
        Long count = -1L; // return rest of list

        switch (Constant.getType(arg1)) {
            case Constant.STEM_TYPE:
                stem = (QDLStem) arg1;
                if (!stem.isList()) {
                    throw new BadArgException(LIST_SUBSET + " requires a list", polyad.getArgAt(0));
                }
                startIndex = (Long) arg2;


              /*  if (polyad.getArgCount() == 2) {
                    count = (long) stem.size() - startIndex;
                } else {*/
                if (polyad.getArgCount() == 3) {
                    // must be 3
                    count = (Long) arg3;
                    if (count < 0) {
                        throw new BadArgException(LIST_SUBSET + " requires that the number of elements be positive", polyad.getArgAt(2));
                    }
/*
                    if(stem.size() < startIndex + count){
                        count = stem.size() - startIndex; // run to end of list
                    }
*/
                }

                break;
            case Constant.SET_TYPE:
                if (polyad.getArgCount() == 3) {
                    throw new ExtraArgException(LIST_SUBSET + " takes a single argument for a set", polyad.getArgAt(1));
                }
                set = (QDLSet) arg1;
                count = (Long) arg2;
                if (count < 0) {
                    count = -count; // no wrap around possible for sets
                }
                if (set.size() < count) {
                    count = (long) set.size();
                }
                break;
            default:
                QDLList qdlList = new QDLList();
                qdlList.add(asQDLValue(arg1));
                QDLStem stemVariable = new QDLStem();
                stemVariable.setQDLList(qdlList);
                polyad.setResult(stemVariable);
                polyad.setEvaluated(true);
                return true;
        }


        if (set != null) {
            QDLSet outSet = new QDLSet();
            Iterator<QDLValue> iterator = set.iterator();

            for (long i = 0; i < count; i++) {
                outSet.add(iterator.next());
            }

            polyad.setResult(outSet);
            polyad.setEvaluated(true);
            return true;
        }

        QDLStem outStem;
        if (count == 0) {
            outStem = new QDLStem();
        } else {
            outStem = stem.listSubset(startIndex, count);
        }
        polyad.setResult(outStem);
        polyad.setEvaluated(true);
        return true;

    }

    /*
      subset((x,y)->2<x, [;10])
{3:3, 4:4, 5:5, 6:6, 7:7, 8:8, 9:9}
  subset((x)->x<0, [-2;3])
[-2,-1]
    subset((x)->x<0, [-2;3])
[-2,-1]
  my_f(x,y)->2<x
  subset(@my_f, [;10])
{3:3, 4:4, 5:5, 6:6, 7:7, 8:8, 9:9}
  my_f(x)->x<0
  subset(@my_f, [-2;5])
    subset((x,y)->mod(x,2)==0, [-4;5])
{0:-4, 2:-2, 4:0, 6:2, 8:4}
pick((v)-> 7<v<20,[|pi(); pi(3) ; 10|])
     */

    /**
     * Pick elements based on a function that is supplied.
     *
     * @param polyad
     * @param state
     * @return
     */
    protected boolean doPickSubset(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return true;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(PICK + " requires 2 arguments", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(PICK + " requires 2 arguments", polyad.getArgAt(3));
        }

        // addresses https://github.com/ncsa/qdl/issues/110

        State localState = state.newLocalState();
        FunctionReferenceNodeInterface frn = getFunctionReferenceNode(localState, polyad.getArgAt(0), true);
        //FunctionReferenceNodeInterface frn = getFunctionReferenceNode(state, polyad.getArgAt(0), true);
        QDLValue arg1 = polyad.evalArg(1, state);
        if(arg1 == null){
            throw new MissingArgException(PICK + " second argument not found ", polyad.getArgAt(1));
        }
        ExpressionImpl f = null;
        int argCount = 1; // default
        try {
            // addresses https://github.com/ncsa/qdl/issues/107
            if(frn instanceof DyadicFunctionReferenceNode){
                ((DyadicFunctionReferenceNode) frn).evaluate(localState);
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
                List<FunctionRecordInterface> functionRecordList = localState.getFTStack().getByAllName(frn.getFunctionName());
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

            f = getOperator(localState, frn, argCount); // single argument
        } catch (UndefinedFunctionException ufx) {
            ufx.setStatement(polyad.getArgAt(0));
            throw ufx;
        }
        // 3 cases
        if (arg1.isSet()) {
            if (argCount != 1) {
                throw new BadArgException(PICK + " pick function for sets can only have a single argument", polyad.getArgAt(0));
            }
            QDLSet result = new QDLSet();
            QDLSet argSet = arg1.asSet();
            ArrayList<QDLValue> rawArgs = new ArrayList<>();
            for (QDLValue element : argSet) {
                rawArgs.clear();
                rawArgs.add(element);
                f.setArguments(toConstants(rawArgs));
                //Object test = f.evaluate(state);
                QDLValue test = f.evaluate(localState);
                if (test.isBoolean()) {
                    if (test.asBoolean()) {
                        result.add(element);
                    }
                }
            }

            polyad.setResult(result);
            polyad.setEvaluated(true);
            return true;
        }
        // For stems it's quite similar, but not enough to have a single piece of code.
        if (arg1.isStem()) {
            QDLStem outStem = new QDLStem();
            QDLStem stemArg = arg1.asStem();
            ArrayList<QDLValue> rawArgs = new ArrayList<>();
            for (Object key : stemArg.keySet()) {
                rawArgs.clear();
                QDLValue value = stemArg.get(key);
                // Contract for pick!
                // monad = value only
                // dyad (key, value)
                if (argCount == 2) {
                    rawArgs.add(asQDLValue(key));
                }
                rawArgs.add(stemArg.get(key));
                f.setArguments(toConstants(rawArgs));
                //Object test = f.evaluate(state);
                QDLValue test = f.evaluate(localState);
                if (test.isBoolean()) {
                    if (test.asBoolean()) {
                        outStem.putLongOrString(key, value);
                    }
                }
            }

            polyad.setResult(outStem);
            polyad.setEvaluated(true);
            return true;
        }
        // final case is that it is a scalar.
        ArrayList<QDLValue> rawArgs = new ArrayList<>();
        rawArgs.add(arg1);
        f.setArguments(toConstants(rawArgs));
        //Object test = f.evaluate(state);
        Object test = f.evaluate(localState);
        Object result = null;
        if (isBoolean(test)) {
            if ((Boolean) test) {
                result = arg1;
            } else {
                result = QDLNull.getInstance();
            }
        }
        polyad.setResult(result);
        polyad.setEvaluated(true);
        return true;
    }

    /**
     * Returns a list of indices. The results is conformable to the left argument and the values in it
     * are the indices of the right argument.
     *
     * @param polyad
     * @param state
     */
    // starts_with(['a','qrs','pqr'],['a','p','s','t'])
    protected void doListStartsWith(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException(LIST_STARTS_WITH + " requires 2 arguments", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(LIST_STARTS_WITH + " requires 2 arguments", polyad.getArgAt(2));
        }

        Object leftArg = polyad.evalArg(0, state);
        checkNull(leftArg, polyad.getArgAt(0));

        QDLStem leftStem = null;
        if (isString(leftArg)) {
            leftStem = new QDLStem();
            leftStem.put(0L, asQDLValue(leftArg));
        }
        if (leftStem == null) {
            if (isStem(leftArg)) {
                leftStem = (QDLStem) leftArg;
            } else {
                throw new BadArgException(LIST_STARTS_WITH + " requires a stem for the left argument.", polyad.getArgAt(0));
            }
        }

        Object rightArg = polyad.evalArg(1, state);
        checkNull(rightArg, polyad.getArgAt(1));

        QDLStem rightStem = null;
        if (isString(rightArg)) {
            rightStem = new QDLStem();
            rightStem.put(0L, asQDLValue(rightArg));
        }
        if (rightStem == null) {
            if (isStem(rightArg)) {
                rightStem = (QDLStem) rightArg;

            } else {
                throw new BadArgException(LIST_STARTS_WITH + " requires a stem for the right argument.", polyad.getArgAt(1));
            }
        }
        QDLStem output = new QDLStem();

        // Fix https://github.com/ncsa/qdl/issues/1, with regression test StringFunctionTests#testIndexOfGaps
        for (Object leftKey : leftStem.keySet()) {
            boolean gotOne = false;
            if (!(leftKey instanceof Long)) {
                throw new IndexError(leftKey + " is not an integer index", polyad.getArgAt(0));
            }
            Long leftIndex = (Long) leftKey;
            for (Object rightKey : rightStem.keySet()) {
                if (!(rightKey instanceof Long)) {
                    throw new IndexError(rightKey + " is not an integer index", polyad.getArgAt(1));
                }
                Long rightIndex = (Long) rightKey;
                if (leftStem.getString(leftIndex).startsWith(rightStem.getString(rightIndex))) {
                    output.put(leftIndex, asQDLValue(rightIndex));
                    gotOne = true;
                    break;
                }
            }
            if (!gotOne) {
                output.put(leftIndex, asQDLValue(-1L));
            }

        }
        polyad.setResult(output);
        polyad.setEvaluated(true);
    }

    protected void doListReverse(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(LIST_REVERSE + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(LIST_REVERSE + " requires at most 2 arguments", polyad.getArgAt(2));
        }

        Object arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0));

        if (!isStem(arg1)) {
            throw new BadArgException(LIST_REVERSE + " requires a stem as its argument.", polyad.getArgAt(0));
        }
        int axis = 0;
        if (polyad.getArgCount() == 2) {
            Object arg2 = polyad.evalArg(1, state);
            checkNull(arg2, polyad.getArgAt(1));

            if (!isLong(arg2)) {
                throw new BadArgException(LIST_REVERSE + " an integer as its axis.", polyad.getArgAt(1));
            }
            axis = ((Long) arg2).intValue();
        }

        QDLStem input = (QDLStem) arg1;

        DoReverse reverse = this.new DoReverse();

        Object result = axisWalker(input, axis, reverse);
        polyad.setResult(result);
        polyad.setEvaluated(true);

    }

    protected class DoReverse implements StemUtility.StemAxisWalkerAction1 {
        @Override
        public Object action(QDLStem inStem) {
            QDLStem output = new QDLStem();
            Iterator<QDLValue> iterator = inStem.getQDLList().descendingIterator(true);
            while (iterator.hasNext()) {
                output.listAdd(iterator.next());
            }
            return output;
        }
    }
}
