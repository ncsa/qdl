package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;

public class NoDefaultValue extends QDLExceptionWithTrace{
    public NoDefaultValue(Statement statement) {
        super(statement);
    }

    public NoDefaultValue(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public NoDefaultValue(String message, Statement statement) {
        super(message, statement);
    }

    public NoDefaultValue(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }
}
