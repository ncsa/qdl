package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

public class WrongValueException extends QDLExceptionWithTrace{
    public WrongValueException(Statement statement) {
        super(statement);
    }

    public WrongValueException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public WrongValueException(String message, Statement statement) {
        super(message, statement);
    }

    public WrongValueException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }

}
