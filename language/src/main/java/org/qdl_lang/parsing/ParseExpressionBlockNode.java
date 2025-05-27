package org.qdl_lang.parsing;

import org.qdl_lang.expressions.ExpressionNode;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/1/21 at  6:43 AM
 */
public class ParseExpressionBlockNode implements ExpressionInterface {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
    public List<ExpressionNode> getExpressionNodes() {
        return expressionNodes;
    }

    public void setExpressionNodes(List<ExpressionNode> expressionNodes) {
        this.expressionNodes = expressionNodes;
    }

    @Override
    public boolean hasAlias() {
        return alias != null;
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

    List<ExpressionNode> expressionNodes = new ArrayList<>();

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


    @Override
    public boolean isEvaluated() {
        return false;
    }

    @Override
    public void setEvaluated(boolean evaluated) {

    }

    @Override
    public Object evaluate(State state) {
        return null;
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
            return PARSE_EXPRESSION_BLOCK_NODE;
        }
}
