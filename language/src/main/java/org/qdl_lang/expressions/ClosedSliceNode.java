package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  9:25 AM
 */
public class ClosedSliceNode extends ExpressionImpl {
    public ClosedSliceNode() {
    }

    public ClosedSliceNode(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    protected BigDecimal argToDB(QDLValue arg) {
        if (arg.isDecimal()) {
            return arg.asDecimal();

        } else {
            if (arg.isLong()) {
                return new BigDecimal(arg.toString());
            }
        }

        throw new IllegalArgumentException("error: \"" + arg + "\"  is not a number");
    }

    @Override
    public QDLValue evaluate(State state) {
        BigDecimal bd0 = argToDB(evalArg(0, state));
        BigDecimal bd1 = argToDB(evalArg(1, state));
        Long arg2 = null;
        if (getArgCount() == 2) {
            arg2 = 2L; // default
        } else {
            QDLValue obj2 = evalArg(2, state);
            if (!obj2.isLong()) {
                throw new IllegalArgumentException("error: the last argument must be an integer");
            }
            arg2 = obj2.asLong();
        }
        if (arg2 < 2) {
            throw new IllegalArgumentException("error: the last argument must be greater than 1");
        }
        int count = arg2.intValue();

        BigDecimal bd2 = new BigDecimal(arg2.toString());
        // speed optimization. Fill up array list with preset number of elements allocated.
        // Makes a fair difference in large slices.
        ArrayList<QDLValue> args = new ArrayList<>(count); // limited to in value

        // Yuck. This is why nobody likes BigDecimals. This is (arg1 - arg0)/(arg2 - 1)
        BigDecimal fudgeFactor = bd1.subtract(bd0, OpEvaluator.getMathContext()).divide(bd2.subtract(BigDecimal.ONE, OpEvaluator.getMathContext()), OpEvaluator.getMathContext());
        args.add( getArgAt(0).getResult());
        for (int i = 1; i < count - 1; i++) {
            args.add(asQDLValue(bd0.add(fudgeFactor.multiply(new BigDecimal(Long.toString(i), OpEvaluator.getMathContext())))));
        }
        args.add(getArgAt(1).getResult());
        QDLStem out = new QDLStem();
        out.getQDLList().setArrayList(args);
        setResult(new QDLValue(out));
        setEvaluated(true);
        return getResult();
    }

    @Override
    public ExpressionInterface makeCopy() {
        ClosedSliceNode r = new ClosedSliceNode(getTokenPosition());
        r.setArguments(getArguments());
        return r;
    }

    @Override
    public int getNodeType() {
        return CLOSED_SLICE_NODE;
    }
}
