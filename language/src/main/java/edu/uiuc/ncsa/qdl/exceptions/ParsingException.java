package edu.uiuc.ncsa.qdl.exceptions;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/29/20 at  3:00 PM
 */
public class ParsingException extends QDLException {
    public static final String SYNTAX_TYPE= "syntax";
    public static final String AMBIGUOUS_TYPE= "ambiguous";
    public static final String MISMATCH_TYPE= "mismatch";

    public ParsingException(String message, int lineNumber, int characterPosition,  String type) {
        super(message);
        this.characterPosition = characterPosition;
        this.lineNumber = lineNumber;
        this.type = type;
    }
    public ParsingException(String message, int lineNumber, int characterPosition,  int endCharacterPosition, String type) {
        super(message);
        this.characterPosition = characterPosition;
        this.endCharacterPosition = endCharacterPosition;
        this.lineNumber = lineNumber;
        this.type = type;
    }

    public ParsingException() {
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getCharacterPosition() {
        return characterPosition;
    }

    public void setCharacterPosition(int characterPosition) {
        this.characterPosition = characterPosition;
    }

    int characterPosition = -1;


    int lineNumber = -1;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type;

    public int getEndCharacterPosition() {
        return endCharacterPosition;
    }

    public void setEndCharacterPosition(int endCharacterPosition) {
        this.endCharacterPosition = endCharacterPosition;
    }

    int endCharacterPosition = -1;

    public boolean hasScriptName(){
        return scriptName!=null;
    }
    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    String scriptName = null;
}
