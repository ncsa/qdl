package org.qdl_lang.statements;

import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLValue;

import java.io.Serializable;
import java.util.List;

/**
 * Top-level interface for all statements and expressions. Note that in QDL a statement has
 * no result and an expression does. However, due to historical reasons, everything in the
 * implementation is a "statement". So in reality, statements (such as if[]then[])
 * implement this but expressions implement {@link ExpressionInterface}. This mostly
 * has parser-related generic stuff in it. It, like the source code, token position in parsing etc.
 * <p>Created by Jeff Gaynor<br>
 * on 1/14/20 at  9:11 AM
 */
public interface Statement extends Serializable {
    QDLValue evaluate(State state);

    List<String> getSourceCode();

    void setSourceCode(List<String> sourceCode);

    /**
     * Set the location of this token from the parser. This is used
     * for error notifications later.
     * @param tokenPosition
     */
    void setTokenPosition(TokenPosition tokenPosition);
    TokenPosition getTokenPosition();
    boolean hasTokenPosition();
}
