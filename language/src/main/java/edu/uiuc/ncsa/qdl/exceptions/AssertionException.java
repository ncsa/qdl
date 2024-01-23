package edu.uiuc.ncsa.qdl.exceptions;

import edu.uiuc.ncsa.qdl.variables.QDLStem;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  12:57 PM
 */
public class AssertionException extends QDLException{
    public AssertionException() {
    }

    public AssertionException(Throwable cause) {
        super(cause);
    }

    public AssertionException(String message) {
        super(message);
    }


    public AssertionException(String message, QDLStem stateStem) {
        super(message);
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
    public AssertionException(String message, Throwable cause) {
        super(message, cause);
    }
}
