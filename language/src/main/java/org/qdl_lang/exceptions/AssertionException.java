package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;
import org.qdl_lang.variables.QDLStem;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  12:57 PM
 */
public class AssertionException extends QDLExceptionWithTrace{
    public AssertionException(Statement statement) {
        super(statement);
    }

    public AssertionException(Throwable cause, Statement statement) {
        super(cause, statement);
    }

    public AssertionException(String message, Statement statement) {
        super(message, statement);
    }

    public AssertionException(String message, Throwable cause, Statement statement) {
        super(message, cause, statement);
    }

    public AssertionException(String message, QDLStem stateStem, Statement statement) {
        super(message, statement);
        setAssertionState(stateStem);
    }

    public Object getAssertionState() {
        return assertionState;
    }

    public void setAssertionState(QDLStem assertionState) {
        this.assertionState = assertionState;
    }

    QDLStem assertionState;

    public boolean hasPayload(){
        return assertionState != null;
    }

}
