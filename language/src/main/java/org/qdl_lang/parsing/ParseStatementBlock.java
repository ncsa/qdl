package org.qdl_lang.parsing;

import org.qdl_lang.state.State;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Top level for various block statements in the parser. These exist only to leverage
 * all the machinery for dealing with statements (and are blocks of statements).
 * <h2>Normal usage</h2>
 * In the course of parsing, these get made and are mined for their statements. They are
 * (at this point) not passed along or evaluated.
 * This allows you to collect things in syntactic blocks then restructure them,
 * e.g. in a conditional or a loop.
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  6:37 AM
 */
public class ParseStatementBlock implements ExpressionInterface {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}    @Override
    public boolean hasAlias() {
        return alias!=null;
    }
    String alias = null;

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
         this.alias = alias;
    }

    @Override
    public QDLValue getResult() {
        return null;
    }

    @Override
    public void setResult(QDLValue object) {

    }

    @Override
    public void setResult(Object result) {

    }

    @Override
    public int getResultType() {
        return 0;
    }


    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    List<Statement> statements = new ArrayList<>();

    @Override
    public boolean isEvaluated() {
        return false;
    }

    @Override
    public void setEvaluated(boolean evaluated) {

    }

    @Override
    public QDLValue evaluate(State state) {
     throw new NotImplementedException("parse statement blocks do not execute.");
    }

    List<String> src = new ArrayList<>();
    @Override
    public List<String> getSourceCode() {
        return src;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
                  this.src = sourceCode;
    }

    @Override
    public ExpressionInterface makeCopy() {
        return null;
    }
    @Override
        public int getNodeType() {
            return PARSE_STATEMENT_BLOCK_NODE;
        }
}
