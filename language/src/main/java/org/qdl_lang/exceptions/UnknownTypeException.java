package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

public class UnknownTypeException extends QDLExceptionWithTrace{
    public UnknownTypeException(Statement statement) {
        super(statement);
    }

    public UnknownTypeException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public UnknownTypeException(String message, Statement statement) {
        super(message, statement);
    }

    public UnknownTypeException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }
}
