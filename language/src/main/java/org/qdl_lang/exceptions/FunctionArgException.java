package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/17/22 at  6:05 AM
 */
public class FunctionArgException extends QDLExceptionWithTrace{

    public FunctionArgException(Statement statement) {
        super(statement);
    }

    public FunctionArgException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public FunctionArgException(String message, Statement statement) {
        super(message, statement);
    }

    public FunctionArgException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }

}
