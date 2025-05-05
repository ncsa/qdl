package org.qdl_lang.statements;

import org.qdl_lang.exceptions.AssertionException;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;

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
    public Object evaluate(State state) {
        if (!state.isAssertionsOn()) {
            return null;
        }

        Object obj = getConditional().evaluate(state);
        if (obj instanceof Boolean) {
            Boolean b = (Boolean) obj;
            if (!b) {
                AssertionException assertionException = null;
                if (getMesssge() == null) {
                    assertionException = new AssertionException("", getConditional()); // no message implies empty message
                } else {
                    Object m = getMesssge().evaluate(state);
                    if (m instanceof String) {
                        assertionException = new AssertionException((String) m, getConditional());
                    } else {
                        if(m instanceof QDLStem) {
                            assertionException = new AssertionException("assertion failed", getConditional());
                            assertionException.setAssertionState((QDLStem) m);
                        }else{
                            assertionException = new AssertionException(m.toString(), getConditional());
                        }
                    }
                }
                throw assertionException;
            } else {
                return Boolean.TRUE;
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
