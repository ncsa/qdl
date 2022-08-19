package edu.uiuc.ncsa.qdl.parsing;

import edu.uiuc.ncsa.qdl.ini_generated.iniLexer;
import edu.uiuc.ncsa.qdl.ini_generated.iniParser;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;

import java.io.FileReader;
import java.io.Reader;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/11/21 at  8:24 AM
 */
public class IniParserDriver {
    protected QDLStem output = null;
    protected iniParser getParser(Reader reader) throws Throwable {
        if(parser == null) {
            lexer = new iniLexer(CharStreams.fromReader(reader));

            parser = new iniParser(new CommonTokenStream(lexer));
            output = new QDLStem();
            IniListenerImpl iniListener = new IniListenerImpl(output);
            lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
            parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
            /*
             * To get an exception at parsing rather than having everything get piped to System.err
             * we have to implement a listener that throws an exception
             */
            parser.addErrorListener(ParserExceptionListener.INSTANCE);

            parser.addParseListener(iniListener);
        }
        return parser;
    }
    iniLexer lexer;
     iniParser parser;
     /*
        QDLParserLexer lexer;
    QDLParserParser parser;
      */

    public QDLStem parse(Reader reader) throws Throwable {
        iniParser parser = getParser(reader);
        parser.ini();
        return output;
    }
    public static void main(String[] args) throws Throwable{
        IniParserDriver iniParserDriver = new IniParserDriver();
        FileReader fileReader = new FileReader("/home/ncsa/dev/ncsa-git/qdl/language/src/main/antlr4/iniFile/test.ini");
        QDLStem out = iniParserDriver.parse(fileReader);
        System.out.println(out);

    }
}
