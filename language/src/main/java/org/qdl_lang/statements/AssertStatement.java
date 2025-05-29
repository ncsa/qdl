package org.qdl_lang.statements;

import org.qdl_lang.exceptions.AssertionException;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.QDLValue;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  12:53 PM
 */
public class AssertStatement implements Statement {
    TokenPosition tokenPosition = null;

    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    @Override
    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    @Override
    public boolean hasTokenPosition() {
        return tokenPosition != null;
    }

    public AssertStatement(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    ExpressionInterface conditional;

    public ExpressionInterface getConditional() {
        return conditional;
    }

    public void setConditional(ExpressionInterface conditional) {
        this.conditional = conditional;
    }

    public ExpressionInterface getMesssge() {
        return messsge;
    }

    public void setMesssge(ExpressionInterface messsge) {
        this.messsge = messsge;
    }

    // Fix https://github.com/ncsa/qdl/issues/39 change this from ExpressionNode to ExpressionInterface.
    ExpressionInterface messsge;

    @Override
    public QDLValue evaluate(State state) {
        if (!state.isAssertionsOn()) {
            return null;
        }

        QDLValue obj = getConditional().evaluate(state);
        if (obj.isBoolean()) {
            Boolean b = obj.asBoolean();
            if (!b) {
                AssertionException assertionException = null;
                if (getMesssge() == null) {
                    assertionException = new AssertionException("", getConditional()); // no message implies empty message
                } else {
                    QDLValue m = getMesssge().evaluate(state);
                    if (m.isString()) {
                        assertionException = new AssertionException(m.asString(), getConditional());
                    } else {
                        if(m.isStem()) {
                            assertionException = new AssertionException("assertion failed", getConditional());
                            assertionException.setAssertionState(m.asStem());
                        }else{
                            assertionException = new AssertionException(m.toString(), getConditional());
                        }
                    }
                }
                throw assertionException;
            } else {
                return BooleanValue.True;
            }
        }
        throw new IllegalArgumentException("error: the conditional must be boolean valued, got '" + obj + "'");
    }

    @Override
    public List<String> getSourceCode() {
        return null;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {

    }
}
