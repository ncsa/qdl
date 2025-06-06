package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * A post or prefix operator, such a logical not or ++. The default is that this is postfix.
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:43 PM
 */
public class Monad extends ExpressionImpl{

    public Monad(boolean postFix) {
        this.postFix = postFix;
    }

    public Monad(boolean postFix, TokenPosition tokenPosition) {
        super(tokenPosition);
           this.postFix = postFix;
       }
    public Monad( int operatorType, boolean isPostFix) {
        this(isPostFix);
        this.operatorType = operatorType;
    }
    public Monad( int operatorType, boolean isPostFix, TokenPosition tokenPosition) {
         this(isPostFix, tokenPosition);
         this.operatorType = operatorType;
     }

    /**
     * Constructor for making a post fix monad.
     *
     * @param operatorType
     * @param argument
     */
    public Monad(int operatorType, ExpressionNode argument) {
        this(operatorType, true);
        getArguments().add(0, argument);
    }

    /**
     * Constructor for specifying the type of operator.
     *
     * @param operatorType
     * @param argument
     * @param isPostFix
     */
    public Monad(int operatorType, ExpressionNode argument, boolean isPostFix) {
        this(operatorType, argument);
        this.postFix = isPostFix;
    }

    public boolean isPostFix() {
        return postFix;
    }

    public void setPostFix(boolean postFix) {
        this.postFix = postFix;
    }

    boolean postFix = true; //default

    @Override
    public QDLValue evaluate(State state) {
        state.getOpEvaluator().evaluate(this, state);
        return getResult();
    }

    public ExpressionInterface getArgument() {
        return getArguments().get(0);
    }

    public void setArgument(ExpressionInterface node) {
        if (getArgCount() == 0) {
            getArguments().add(node);
            return;
        }
        arguments = new ArrayList<>();
        arguments.add(node);
    }

    @Override
    public ExpressionNode makeCopy() {
        Monad monad = new Monad(operatorType,postFix);
        monad.setArgument(getArgument().makeCopy());
        return monad;
    }
    public boolean isSigned(){
        return operatorType == OpEvaluator.MINUS_VALUE;
    }
    public Long getSignedLongValue(){
        if(!evaluated){
            throw new UnevaluatedExpressionException("Error: unevaulated expression");
        }
        if(resultType != Constant.LONG_TYPE){
            throw new IllegalArgumentException("error: not a long");
        }
        return (isSigned()?-1L:1L)* getResult().asLong();
    }
    public BigDecimal getSignedDecimalValue(){
        if(!evaluated){
            throw new UnevaluatedExpressionException("Error: unevaulated expression");
        }
        if(resultType != Constant.DECIMAL_TYPE){
            throw new IllegalArgumentException("error: not a long");
        }
        return isSigned()?getResult().asDecimal().negate():getResult().asDecimal();
    }
    @Override
        public int getNodeType() {
            return MONAD_NODE;
        }
}
