package org.qdl_lang.exceptions;

import org.qdl_lang.statements.Statement;
import org.qdl_lang.variables.QDLStem;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/25/20 at  6:47 AM
 */
public class UnknownSymbolException extends QDLExceptionWithTrace {
    public UnknownSymbolException(Statement statement, String unknownSymbol ) {
        super(statement);
        this.unknownSymbol = unknownSymbol;
    }

    public UnknownSymbolException(Throwable cause, Statement statement, String unknownSymbol) {
        super(cause, statement);
        this.unknownSymbol = unknownSymbol;
    }

    public UnknownSymbolException(String message, Statement statement, String unknownSymbol) {
        super(message, statement);
        this.unknownSymbol = unknownSymbol;
    }

    public UnknownSymbolException(String message, Throwable cause, Statement statement, String unknownSymbol) {
        super(message, cause, statement);
        this.unknownSymbol = unknownSymbol;
    }

    public boolean hasUnknownSymbol() {
        return unknownSymbol != null;
    }
    public String getUnknownSymbol() {
        return unknownSymbol;
    }

    public void setUnknownSymbol(String unknownSymbol) {
        this.unknownSymbol = unknownSymbol;
    }

    String unknownSymbol;

    public boolean isUnknownSymbolAStem(){
        return getUnknownSymbol().endsWith(QDLStem.STEM_INDEX_MARKER);
    }
}
