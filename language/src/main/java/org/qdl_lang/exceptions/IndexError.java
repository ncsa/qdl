package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/2/20 at  9:36 AM
 */
public class IndexError extends QDLExceptionWithTrace {
    public IndexError(Statement statement) {
        super(statement);
    }

    public IndexError(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public IndexError(String message, Statement statement) {
        super(message, statement);
    }

    public IndexError(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }
}
