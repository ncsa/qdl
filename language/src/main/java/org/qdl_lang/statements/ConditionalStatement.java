package org.qdl_lang.statements;

import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.expressions.ExpressionNode;
import org.qdl_lang.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/17/20 at  3:34 PM
 */
public class ConditionalStatement implements Statement {
    public ConditionalStatement(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
    ExpressionNode conditional;

    public ExpressionNode getConditional() {
        return conditional;
    }

    public void setConditional(ExpressionNode conditional) {
        this.conditional = conditional;
    }

    public List<Statement> getIfArguments() {
        return ifArguments;
    }

    public void setIfArguments(List<Statement> ifArguments) {
        this.ifArguments = ifArguments;
    }

    public List<Statement> getElseArguments() {
        return elseArguments;
    }

    public void setElseArguments(List<Statement> elseArguments) {
        this.elseArguments = elseArguments;
    }

    List<Statement> ifArguments = new ArrayList<>();
    List<Statement> elseArguments = new ArrayList<>();

    @Override
    public Object evaluate(State state) {
        State newState = state.newLocalState();

        Boolean result = false;
        getConditional().evaluate(newState);
        if (!(getConditional().getResult() instanceof Boolean)) {
            throw new QDLExceptionWithTrace("Error: You must have a boolean value for your conditional not \"" +getConditional().getResult() + "\".", conditional.getArgAt(0) );
        }
        if ((Boolean) getConditional().getResult()) {
            result = true;
            for (Statement arg : ifArguments) {
                arg.evaluate(newState);
            }
        } else {
            if (elseArguments != null && !elseArguments.isEmpty()) {
                for (Statement arg : elseArguments) {
                    arg.evaluate(newState);
                }
            }

        }
        return result;
    }

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    List<String> sourceCode;
}
