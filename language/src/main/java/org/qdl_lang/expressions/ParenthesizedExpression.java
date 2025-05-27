package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/13/21 at  3:44 PM
 */
public class ParenthesizedExpression implements ExpressionNode {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
    @Override
    public boolean hasAlias() {
        return alias != null;
    }

    String alias = null;

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ExpressionInterface getExpression() {
        if (getArgCount() == 0) {
            return null;
        }
        return getArguments().get(0);
    }

    @Override
    public ExpressionInterface getArgAt(int index) {
        if ((index < 0) || (getArgCount() <= index)) {
            return null;
        }
        return getArguments().get(index);
    }

    public void setExpression(ExpressionInterface expression) {
        if (getArgCount() == 0) {
            getArguments().add(expression);
        } else {
            getArguments().set(0, expression);
        }
    }


    @Override
    public QDLValue getResult() {
        return getExpression().getResult();
    }

    @Override
    public void setResult(QDLValue object) {
        getExpression().setResult(object);
    }

    @Override
    public void setResult(Object result) {
           setResult(QDLValue.asQDLValue( result));
    }

    @Override
    public int getResultType() {
        return getExpression().getResultType();
    }


    @Override
    public boolean isEvaluated() {
        return getExpression().isEvaluated();
    }

    @Override
    public void setEvaluated(boolean evaluated) {
        getExpression().setEvaluated(evaluated);
    }

    @Override
    public QDLValue evaluate(State state) {
        return getExpression().evaluate(state);
    }

    List<String> doxx;

    @Override
    public List<String> getSourceCode() {
        if (doxx == null) {
            doxx = new ArrayList<>();
        }
        return doxx;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        doxx = sourceCode;
    }

    @Override
    public ExpressionInterface makeCopy() {
        ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression();
        parenthesizedExpression.setExpression(getExpression().makeCopy());
        return parenthesizedExpression;
    }

    ArrayList<ExpressionInterface> args = new ArrayList<>();

    @Override
    public ArrayList<ExpressionInterface> getArguments() {
        return args;
    }

    @Override
    public void setArguments(ArrayList<ExpressionInterface> arguments) {
        args = arguments;
    }

    @Override
    public int getArgCount() {
        return args.size();
    }

    @Override
    public int getOperatorType() {
        return OpEvaluator.UNKNOWN_VALUE;
    }

    @Override
    public void setOperatorType(int operatorType) {
    }
    @Override
        public int getNodeType() {
            return PARENTHESIZED_NODE;
        }
}
