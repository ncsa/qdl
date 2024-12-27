package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

/**
 * Thrown when the argument to a  function is not an accepted type.
 * <p>Created by Jeff Gaynor<br>
 * on 4/17/22 at  6:07 AM
 */
public class BadArgException extends FunctionArgException{
    public BadArgException(Statement statement) {
        super(statement);
    }

    public BadArgException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public BadArgException(String message, Statement statement) {
        super(message, statement);
    }

    /**
     * Pass in the index of the bad argument (in a {@link org.qdl_lang.extensions.QDLFunction} implementation
     * to get filled in by the error handler.
     * @param message
     * @param argIndex
     */
    public BadArgException(String message, int argIndex) {
        super(message, null);
        this.argIndex = argIndex;
    }
    public BadArgException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }

    int argIndex = -1;

    public int getArgIndex() {
        return argIndex;
    }

    public void setArgIndex(int argIndex) {
        this.argIndex = argIndex;
    }
}
