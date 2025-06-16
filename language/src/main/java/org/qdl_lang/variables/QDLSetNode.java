package org.qdl_lang.variables;

import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/6/22 at  4:06 PM
 */
public class QDLSetNode implements ExpressionInterface {
    QDLValue result;

    @Override
    public QDLValue getResult() {
        return result;
    }

    @Override
    public void setResult(QDLValue qdlValue) {
        if (!(qdlValue.isSet())) {
            throw new IllegalStateException("error: cannot set a " + getClass().getSimpleName() + " to type " + qdlValue.getClass().getSimpleName());
        }
        this.result = qdlValue;
    }
    @Override
    public void setResult(Object result) {
        setResult(QDLValue.asQDLValue( result));
    }
    @Override
    public int getResultType() {
        return getResult().getType();
    }



    boolean evaluated = false;

    @Override
    public boolean isEvaluated() {
        return evaluated;
    }

    @Override
    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public ArrayList<ExpressionInterface> getStatements() {
        return statements;
    }

    public void setStatements(ArrayList<ExpressionInterface> statements) {
        this.statements = statements;
    }

    ArrayList<ExpressionInterface> statements = new ArrayList<>();

    @Override
    public QDLValue evaluate(State state) {
        QDLSet setOut = new QDLSet();
        long i = 0;
        for (ExpressionInterface stmt : statements) {
            stmt.evaluate(state);
            if(stmt.getResult() == null && stmt instanceof VariableNode){
                     throw new UnknownSymbolException("\'" + ((VariableNode)stmt).getVariableReference() + "' not found for set value", stmt, ((VariableNode)stmt).getVariableReference());
                 }
            stmt.setEvaluated(true);
            setOut.add(stmt.getResult());
        }
        setResult(setOut);
        return getResult();
    }

    List<String> sourceCode = new ArrayList<>();

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    TokenPosition tokenPosition = null;

    @Override
    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    @Override
    public boolean hasTokenPosition() {
        return tokenPosition != null;
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


    @Override
    public ExpressionInterface makeCopy() {
        QDLSetNode setNode = new QDLSetNode();
        for (ExpressionInterface s : statements) {
            setNode.getStatements().add(s.makeCopy());
        }
        QDLSet qdlSet = new QDLSet();

        // Kludge, but it works.
        qdlSet.fromJSON(getResult().asSet().toJSON());
        setNode.setResult(qdlSet);
        setNode.setSourceCode(getSourceCode());
        setNode.setEvaluated(isEvaluated());

        return setNode;
    }
    @Override
        public int getNodeType() {
            return SET_NODE;
        }
}
