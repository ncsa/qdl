package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.QDLVariable;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  8:38 AM
 */
public class OpenSliceNode extends ExpressionImpl {
    public OpenSliceNode(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    @Override
    public QDLValue evaluate(State state) {
        QDLValue arg0 = evalArg(0, state);
        if (!longOrBD(arg0)) {
            throw new IllegalArgumentException("error: slice requires a number for the first argument ");
        }
        QDLValue arg1 = evalArg(1, state);
        if (!longOrBD(arg1)) {
            throw new IllegalArgumentException("error: slice requires a number for the second argument ");
        }
        QDLValue arg2;
        if (getArgCount() == 3) {
            arg2 = evalArg(2, state);
        } else {
            arg2 = asQDLValue(1L);
        }
        QDLStem out;
        if (areAnyBD(arg0, arg1, arg2)) {
            out = doDecimalCase(arg0, arg1, arg2);
        } else {
            out = doLongCase(arg0, arg1, arg2);
        }

        setResult(out);
        setEvaluated(true);
        return getResult();
    }

    protected boolean areAnyBD(QDLValue... args) {
        boolean areAnyDB = false;
        for (int i = 0; i < args.length; i++) {
            areAnyDB = areAnyDB || (args[i].isDecimal());
        }
        return areAnyDB;
    }

    protected boolean longOrBD(QDLValue args) {
        return args.isDecimal() || args.isLong();
    }

    public QDLStem doDecimalCase(QDLValue... args) {
        BigDecimal start = null;
        if (args[0].isDecimal()) {
            start = args[0].asDecimal();
        } else {
            start = new BigDecimal(args[0].toString());
        }
        BigDecimal stop = null;
        if (args[1].isDecimal()) {
            stop = args[1].asDecimal();
        } else {
            stop = new BigDecimal(args[1].toString());
        }
        BigDecimal step = null;
        if (args[2].isDecimal()) {
            step = args[2].asDecimal();
        } else {
            step = new BigDecimal(args[2].toString());
        }
        // Now check that this isn't goofy.
        // step < 0 and start < stop means an infinite loop would happen

        if ((step.compareTo(BigDecimal.ZERO) == 0) || ((step.compareTo(BigDecimal.ZERO) < 0) && (start.compareTo(stop) < 0)) ||
                ((0 < step.compareTo(BigDecimal.ZERO)) && (stop.compareTo(start) < 0))) {
            throw new IllegalArgumentException("cannot do slice from " + start + " to " + stop + " by increment of " + step);
        }
        BigDecimal x = stop.subtract(start);
        x = x.divide(step,0, RoundingMode.CEILING);
        int count = x.intValueExact();
        ArrayList<QDLValue> aList = new ArrayList<>(count);
        aList.add( asQDLValue(start));
        BigDecimal result = start.add(step, OpEvaluator.getMathContext());
        long i = 1L;
        while (result.compareTo(stop) < 0) {
            aList.add(asQDLValue(result));
            result = result.add(step, OpEvaluator.getMathContext());
        }
        if (stop.compareTo(start) < 0) {
            // decrement case
            while (result.compareTo(stop) > 0) {
                aList.add(asQDLValue(result));
                result = result.add(step, OpEvaluator.getMathContext());
            }
        } else {
            //increment case
            while (result.compareTo(stop) < 0) {
                aList.add(asQDLValue(result));
                result = result.add(step, OpEvaluator.getMathContext());
            }
        }

        QDLStem out = new QDLStem();
        out.getQDLList().setArrayList(aList);
        return out;
    }

    protected QDLStem doLongCase(QDLValue... args) {
        long start = args[0].asLong();
        long stop =  args[1].asLong();
        long step =  args[2].asLong();
        // step == 0 ∨ (step < 0 ∧ start < stop) ∨ (0 < step ∧ stop < start)
        // means an infinite loop would happen
        if ((step == 0) || ((step < 0) && (start < stop)) || ((0 < step) && (stop < start))) {
            throw new IllegalArgumentException("cannot do slice from " + start + " to " + stop + " by increment of " + step);
        }

        Long result = start;
        Long i = 0L;
        Double dd = Math.ceil((stop - stop) / (step * 1.00D));
        int lCount = dd.intValue();

        ArrayList<QDLValue> aList = new ArrayList<>(lCount);
        QDLStem out = new QDLStem();
        if (stop < start) {
            // decrement case, so step <0
            while (result > stop) {
                //out.put(i++, result);
                aList.add(asQDLValue(result));
                result = result + step;
            }
        } else {
            //increment case
            while (result < stop) {
                //out.put(i++, result);
                aList.add(asQDLValue(result));
                result = result + step;
            }
        }
        out.getQDLList().setArrayList(aList);
        return out;
    }

    @Override
    public ExpressionInterface makeCopy() {
        OpenSliceNode sliceNode = new OpenSliceNode(getTokenPosition());
        sliceNode.setArguments(getArguments());
        return sliceNode;
    }

    @Override
    public int getNodeType() {
        return OPEN_SLICE_NODE;
    }
}
