package org.qdl_lang.parsing;

import org.qdl_lang.exceptions.ParsingException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/9/20 at  9:33 PM
 */
public class ParserExceptionListener extends BaseErrorListener {
    public static final ParserExceptionListener INSTANCE = new ParserExceptionListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e)
            throws ParsingException {
        throw new ParsingException(msg, line, charPositionInLine, ParsingException.SYNTAX_TYPE);
    }

}


