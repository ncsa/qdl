package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 9/26/21 at  6:54 AM
 */
public class IntrinsicViolation extends QDLExceptionWithTrace{
    public IntrinsicViolation(Statement statement) {
        super(statement);
    }

    public IntrinsicViolation(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public IntrinsicViolation(String message, Statement statement) {
        super(message, statement);
    }

    public IntrinsicViolation(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }
}
