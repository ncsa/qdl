package org.qdl_lang.statements;

import org.qdl_lang.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/17/20 at  3:42 PM
 */
public class SwitchStatement implements Statement {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}

    public List<ConditionalStatement> getArguments() {
        return arguments;
    }

    public void setArguments(List<ConditionalStatement> arguments) {
        this.arguments = arguments;
    }

    List<ConditionalStatement> arguments = new ArrayList<>();
    @Override
    public Object evaluate(State state) {

        for(ConditionalStatement c : getArguments()){
            if((Boolean)c.conditional.evaluate(state)){
                c.evaluate(state);
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    List<String> sourceCode;

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }
}
