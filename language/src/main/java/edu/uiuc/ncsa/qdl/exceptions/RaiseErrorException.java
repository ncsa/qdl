package edu.uiuc.ncsa.qdl.exceptions;

import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.variables.QDLStem;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/24/20 at  3:48 PM
 */
public class RaiseErrorException extends QDLException {
    public Polyad getPolyad() {
        return polyad;
    }

    public void setPolyad(Polyad polyad) {
        this.polyad = polyad;
    }

    Polyad polyad;

    public RaiseErrorException(Polyad polyad) {
        this.polyad = polyad;
    }
    public RaiseErrorException(Polyad polyad, String message) {
        super(message);
        this.polyad = polyad;
    }

    public RaiseErrorException(Polyad polyad, String message, Long errorCode, QDLStem state) {
        super(message);
        this.polyad = polyad;
        this.errorCode = errorCode;
        this.state = state;
    }


    public Long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Long errorCode) {
        this.errorCode = errorCode;
    }

    Long errorCode = -1L;

    public QDLStem getState() {
        return state;
    }

    public void setState(QDLStem state) {
        this.state = state;
    }

    QDLStem  state = new QDLStem();
}
