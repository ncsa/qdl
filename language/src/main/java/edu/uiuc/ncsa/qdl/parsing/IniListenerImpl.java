package edu.uiuc.ncsa.qdl.parsing;

import edu.uiuc.ncsa.qdl.evaluate.IOEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.ParsingException;
import edu.uiuc.ncsa.qdl.ini_generated.iniListener;
import edu.uiuc.ncsa.qdl.ini_generated.iniParser;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.StringEscapeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static edu.uiuc.ncsa.qdl.exceptions.ParsingException.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/11/21 at  8:15 AM
 */
public class IniListenerImpl implements iniListener {
    public QDLStem getOutput() {
        return output;
    }

    public void setOutput(QDLStem output) {
        this.output = output;
    }

    QDLStem output = null;

    public IniListenerImpl(QDLStem output) {
        this.output = output;
    }

    QDLStem currentStem;

    @Override
    public void enterIni(iniParser.IniContext ctx) {

    }

    @Override
    public void exitIni(iniParser.IniContext ctx) {
        checkLexer(ctx);

    }

    @Override
    public void enterSection(iniParser.SectionContext ctx) {
        currentStem = new QDLStem();
    }

    @Override
    public void exitSection(iniParser.SectionContext ctx) {
        checkLexer(ctx);

        currentStem = null; // so we clean up
    }

    String currentSectionHeader;

    @Override
    public void enterSectionheader(iniParser.SectionheaderContext ctx) {
        previousSectionHeader = currentSectionHeader;
        currentSectionHeader = null; // Don't just leave this from the last one.
        currentLineID = null;
        currentLineValue = null;
    }

    String previousSectionHeader = null;
    QDLStem stemPointer;

    @Override
    public void exitSectionheader(iniParser.SectionheaderContext ctx) {
        checkLexer(ctx);
        List<String> identifiers = new ArrayList<>();
        for (TerminalNode id : ctx.Identifier()) {
            identifiers.add(id.getText());
        }

        if (1 < identifiers.size()) {
            //StringTokenizer tokenizer = new StringTokenizer(currentSectionHeader, STEM_INDEX_MARKER);
            QDLStem currentStem1 = output;
            boolean hasStemPath = false;
            for (String nextToken : identifiers) {
                //String nextToken = tokenizer.nextToken();
                if (!currentStem1.containsKey(nextToken)) {
                    QDLStem nextStem = new QDLStem();
                    if (isAllowListEntries()) {
                        if (nextToken.startsWith(IOEvaluator.INI_LIST_ENTRY_START)) {
                            Long index = Long.parseLong(nextToken.substring(1));
                            currentStem1.put(index, nextStem);
                        } else {
                            currentStem1.put(nextToken, nextStem);
                        }
                    } else {
                        currentStem1.put(nextToken, nextStem);
                    }
                    currentStem1 = nextStem;
                } else {
                    currentStem1 = currentStem1.getStem(nextToken);
                }
            }
            //output = currentStem1;
            stemPointer = currentStem1;

            currentStem = currentStem1;
        } else {
            currentSectionHeader = identifiers.get(identifiers.size() - 1);
            if (currentSectionHeader.startsWith(IOEvaluator.INI_LIST_ENTRY_START)) {
                if (isAllowListEntries()) {
                    try {
                        Long index = Long.parseLong(currentSectionHeader.substring(1));
                        output.put(index, currentStem);
                    } catch (NumberFormatException nfx) {
                        output.put(currentSectionHeader, currentStem);
                    }
                } else {
                    output.put(currentSectionHeader, currentStem);
                }
            } else {
                output.put(currentSectionHeader, currentStem);
            }
        }

    }

    String currentLineID = null;

    @Override
    public void enterLine(iniParser.LineContext ctx) {
    }

    public boolean isAllowListEntries() {
        return allowListEntries;
    }

    public void setAllowListEntries(boolean allowListEntries) {
        this.allowListEntries = allowListEntries;
    }

    boolean allowListEntries = true;

    @Override
    public void exitLine(iniParser.LineContext ctx) {
        checkLexer(ctx);
        if (ctx.Identifier() == null) {
            if (ctx.Url() == null) {
                return; // means there was a blank line
            }
            currentLineID = ctx.Url().getText();
        } else {
            currentLineID = ctx.Identifier().getText();
        }
        if (isAllowListEntries()) {
            // Then any identifier of the form _number is treated as an
            // index and put into the list
            if (currentLineID.startsWith("_")) {
                try {
                    if (currentLineID.length() - 1 < 19) {
                        Long index = Long.parseLong(currentLineID.substring(1));
                        currentStem.put(index, currentLineValue);
                    } else {
                        BigInteger index = new BigInteger(currentLineID.substring(1));
                        currentStem.put(index.toString(), currentLineValue); // can't handle anything but longs as list indices
                    }

                } catch (NumberFormatException nfx) {
                    currentStem.put(currentLineID, currentLineValue);
                }
            } else {
                currentStem.put(currentLineID, currentLineValue);
            }
        } else {
            currentStem.put(currentLineID, currentLineValue);
        }
    }

    @Override
    public void enterEntries(iniParser.EntriesContext ctx) {
    }

    Object currentLineValue;

    @Override
    public void exitEntries(iniParser.EntriesContext ctx) {
        checkLexer(ctx);
        int entryCount = ctx.children.size();
        if (entryCount == 1) {
            currentLineValue = convertEntryToValue(ctx.entry(0));
            return;
        }
        QDLStem stemList = new QDLStem();
        for (int i = 0; i < entryCount; i++) {
            if (ctx.entry(i) == null) {
                continue;
            }
            Object obj = convertEntryToValue(ctx.entry(i));
            stemList.put(i, obj);
        }
        currentLineValue = stemList;

    }

    protected Object convertEntryToValue(iniParser.EntryContext entryContext) {
        if (entryContext.String() != null) {
            String outString = entryContext.String().getText().trim();
            // returned text will have the '' included, so string them off
            if (outString.startsWith("'") && outString.endsWith("'")) {
                outString = outString.substring(1, outString.length() - 1);
            }
            // Fix https://github.com/ncsa/OA4MP/issues/88
            outString = StringEscapeUtils.unescapeJava(outString);
            return outString;
        }
        if (entryContext.ConstantKeywords() != null) {
            //  System.out.println(entryContext.ConstantKeywords().getClass());
            if (entryContext.ConstantKeywords().getText().equals("true")) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        if (entryContext.Number() != null) {
            // the parser only recognizes numbers, so this *should* never fail to parse.
            BigDecimal bd;
            bd = new BigDecimal(entryContext.Number().getText());
            if(bd.scale()<=0){
                try {
                    return bd.longValueExact();
                }catch(ArithmeticException arithmeticException){
                    // In this case it does not fit into a long though it is a (big) integer
                    // return the big decimal since we use  big decimals
                }
            }
            return bd;
        }
        throw new IllegalArgumentException("unknown value type");
    }

    @Override
    public void enterEntry(iniParser.EntryContext ctx) {

    }

    @Override
    public void exitEntry(iniParser.EntryContext ctx) {
        checkLexer(ctx);

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }

    /**
     * First cut of catching lexer exceptions and handling them
     *
     * @param pre
     */
    protected void checkLexer(ParserRuleContext pre) {
        if (pre.exception == null) {
            return;
        }
        RecognitionException re = pre.exception;
        String type = SYNTAX_TYPE;
        if (re instanceof InputMismatchException) {
            // Any type of mismatch, when the current token does not match the expected token
            type = MISMATCH_TYPE;
        }
        if (re instanceof LexerNoViableAltException) {
            // Lexer cannot figure out which of two or more possible alternatives to resolve a token there are
            type = AMBIGUOUS_TYPE;
        }
        if (re instanceof NoViableAltException) {
            // Parser cannot figure out which of two or more possible alternatives to resolve a token there are
            type = SYNTAX_TYPE;
        }

        if (re instanceof FailedPredicateException) {
            // A token was found but it could not be validated as teh correct one to use.
            type = AMBIGUOUS_TYPE;
        }
        throw new ParsingException("parsing error, got " +
                re.getOffendingToken().getText(),
                re.getOffendingToken().getLine(),
                re.getOffendingToken().getCharPositionInLine(),
                type
        );
    }
}
