package org.qdl_lang.exceptions;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/29/22 at  2:39 PM
 */
public class QDLFileNotFoundException extends QDLException{
    public QDLFileNotFoundException() {
    }

    public QDLFileNotFoundException(Throwable cause) {
        super(cause);
    }

    public QDLFileNotFoundException(String message) {
        super(message);
    }

    public QDLFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
