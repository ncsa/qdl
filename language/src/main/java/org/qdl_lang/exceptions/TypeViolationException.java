package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

public class TypeViolationException extends QDLExceptionWithTrace{

    public TypeViolationException(Statement statement) {
        super(statement);
    }

    public TypeViolationException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public TypeViolationException(String message, Statement statement) {
        super(message, statement);
    }

    public TypeViolationException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }
}
