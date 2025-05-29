package org.qdl_lang.statements;

import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLNullValue;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/22/21 at  6:34 AM
 */
public class BlockStatement implements Statement{
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    List<Statement> statements = new ArrayList<>();


    @Override
    public QDLValue evaluate(State state) {
        State state1 = state.newLocalState();
        for(Statement statement : statements){
            statement.evaluate(state1);
        }
        return QDLNullValue.getNullValue();
    }

    @Override
    public List<String> getSourceCode() {
        return null;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {

    }
}
