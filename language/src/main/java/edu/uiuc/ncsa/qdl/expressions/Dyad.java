package edu.uiuc.ncsa.qdl.expressions;

import edu.uiuc.ncsa.qdl.exceptions.BadArgException;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;
import edu.uiuc.ncsa.qdl.statements.TokenPosition;

import java.util.ArrayList;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:47 PM
 */
public class Dyad extends ExpressionImpl{
    public Dyad(int operatorType, TokenPosition tokenPosition) {
        super(operatorType, tokenPosition);
    }

    public Dyad(int operatorType) {
        super(operatorType);
        valence = 2;
        arguments = new ArrayList<>(valence);
    }


    public Dyad(int operatorType,
                ExpressionNode leftNode,
                ExpressionNode rightNode) {
        this(operatorType);
        getArguments().add(leftNode);
        getArguments().add(rightNode);
    }

    /**
     * Demotes that this is actually a unary operator that is converted to dyadic by the interpreter.
     * @return
     */
    public boolean isUnary() {
        return unary;
    }

    public void setUnary(boolean unary) {
        this.unary = unary;
    }

    boolean unary = false;

    @Override
    public Object evaluate(State state) {
        if(2<getArguments().size()){
            // This happened once when QDl was creating a dyad, so do check again
            throw new BadArgException("internal error, dyad type " + getOperatorType() + " has too many arguments",getArguments().get(2));
        }
        state.getOpEvaluator().evaluate(this, state);
        return getResult();
    }

    public ExpressionInterface getLeftArgument() {
        return getArguments().get(0);
    }

    public ExpressionInterface getRightArgument() {
        return getArguments().get(1);
    }

    public void setLeftArgument(ExpressionInterface node) {
        if(getArguments().size()==0){
            getArguments().add(node);
        }else {
            getArguments().set(0, node);
        }
    }

    public void setRightArgument(ExpressionInterface node) {
        switch(getArguments().size()){
            case 0:
                getArguments().add(null);
            case 1:
                getArguments().add(node);
                break;
            case 2:
                getArguments().set(1, node);
        }

    }

    @Override
    public ExpressionNode makeCopy() {
        Dyad dyad = new Dyad( operatorType);
        dyad.setLeftArgument(getLeftArgument().makeCopy());
        dyad.setRightArgument(getRightArgument().makeCopy());
        return dyad;
    }
    @Override
        public int getNodeType() {
            return DYAD_NODE;
        }
}
