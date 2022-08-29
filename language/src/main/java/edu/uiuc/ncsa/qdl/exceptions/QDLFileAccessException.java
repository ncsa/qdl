package edu.uiuc.ncsa.qdl.exceptions;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/29/22 at  2:41 PM
 */
public class QDLFileAccessException extends QDLException{
    public QDLFileAccessException() {
    }

    public QDLFileAccessException(Throwable cause) {
        super(cause);
    }

    public QDLFileAccessException(String message) {
        super(message);
    }

    public QDLFileAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
