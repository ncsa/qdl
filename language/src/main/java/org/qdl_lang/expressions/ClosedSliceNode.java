package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;

import java.math.BigDecimal;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  9:25 AM
 */
public class ClosedSliceNode extends ExpressionImpl{
    public ClosedSliceNode() {
    }

    public ClosedSliceNode(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    protected BigDecimal argToDB(Object arg){
        if(arg instanceof BigDecimal){
            return (BigDecimal)arg;

        }else{
            if(arg instanceof Long){
                  return new BigDecimal(arg.toString());
            }
        }

        throw new IllegalArgumentException("error: \"" + arg + "\"  is not a number" );
    }
    @Override
    public Object evaluate(State state) {
        BigDecimal bd0 = argToDB(evalArg(0, state));
        BigDecimal bd1 = argToDB(evalArg(1, state));
        Long arg2 = null;
        if(getArgCount() ==2){
                    arg2 = 2L; // default
        }else {
            Object obj2 = evalArg(2, state);
            if (!(obj2 instanceof Long)) {
                throw new IllegalArgumentException("error: the last argument must be an integer");
            }
            arg2 = (Long) obj2;
        }
        if(arg2 < 2){
            throw new IllegalArgumentException("error: the last argument must be greater than 1" );
        }

        BigDecimal bd2 = new BigDecimal(arg2.toString());

        QDLStem out = new QDLStem();
        // Yuck. This is why nobody likes BigDecimals. This is (arg1 - arg0)/(arg2 - 1)
        BigDecimal fudgeFactor = bd1.subtract(bd0, OpEvaluator.getMathContext()).divide(bd2.subtract(BigDecimal.ONE, OpEvaluator.getMathContext()), OpEvaluator.getMathContext());
        for(long i = 0L; i < arg2; i++){
            out.put(i, bd0.add(fudgeFactor.multiply(new BigDecimal(Long.toString(i), OpEvaluator.getMathContext()))));
        }
        setResult(out);
        setResultType(Constant.STEM_TYPE);
        setEvaluated(true);
        return out;
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
