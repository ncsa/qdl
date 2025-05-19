package org.qdl_lang.exceptions;

import org.qdl_lang.state.SIEntry;
import org.qdl_lang.statements.Statement;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/25/20 at  2:35 PM
 */
public class InterruptException extends QDLExceptionWithTrace
{
    public InterruptException(Statement statement, SIEntry siEntry) {
        super(statement);
        this.siEntry = siEntry;
    }

    public InterruptException(Throwable cause, Statement statement, SIEntry siEntry) {
        super(cause, statement);
        this.siEntry = siEntry;
    }

    public InterruptException(String message, Statement statement, SIEntry siEntry) {
        super(message, statement);
        this.siEntry = siEntry;
    }

    public InterruptException(String message, Throwable cause, Statement statement, SIEntry siEntry) {
        super(message, cause, statement);
        this.siEntry = siEntry;
    }

    public SIEntry getSiEntry() {
        return siEntry;
    }

    public void setSiEntry(SIEntry siEntry) {
        this.siEntry = siEntry;
    }

    SIEntry siEntry;


}
