package org.qdl_lang.variables;

import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import net.sf.json.JSONObject;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 9/28/20 at  1:28 PM
 */
public class StemListNode implements ExpressionInterface {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
    @Override
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

    QDLValue result;

    @Override
    public QDLValue getResult() {
        return result;
    }

    @Override
    public void setResult(QDLValue qdlValue) {
        if (!(qdlValue.isStem())) {
            throw new IllegalStateException("error: cannot set a " + getClass().getSimpleName() + " to type " + qdlValue.getClass().getSimpleName());
        }
    }
    @Override
    public void setResult(Object result) {
        setResult(QDLValue.asQDLValue( result));
    }
    @Override
    public int getResultType() {
        return getResult().getType();
    }

/*
    @Override
    public void setResultType(int type) {
        // No op, actually, since this only returns a single type of object.
        if (type != Constants.STEM_TYPE && type != Constants.LIST_TYPE) {
            throw new NFWException("Internal error: Attempt to set stem to type = " + type);
        }
    }
*/

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
        QDLStem stemOut = new QDLStem();
        long i = 0;
        for (ExpressionInterface stmt : statements) {
            stmt.evaluate(state);
            if(stmt.getResult() == null && stmt instanceof VariableNode){
                throw new UnknownSymbolException("\'" + ((VariableNode)stmt).getVariableReference() + "' not found", stmt);
            }
            stmt.setEvaluated(true);
            //stmt.setResultType(Constant.getType(stmt.getResult()));
            stemOut.put(i++, stmt.getResult());
        }
        setEvaluated(true);
        return new QDLValue(stemOut);
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
    public ExpressionInterface makeCopy() {
        StemListNode newSLN = new StemListNode();
        for (ExpressionInterface s : statements) {
            newSLN.getStatements().add(s.makeCopy());
        }
        QDLStem newStem = new QDLStem();

        // Kludge, but it works.
        newStem.fromJSON((JSONObject) getResult().asStem().toJSON());
        newSLN.setResult(newStem);
        newSLN.setSourceCode(getSourceCode());
        newSLN.setEvaluated(isEvaluated());
        return newSLN;
    }

    @Override
    public String toString() {
        return "StemListNode{" +
                "sourceCode=" + sourceCode +
                '}';
    }
    @Override
        public int getNodeType() {
            return LIST_NODE;
        }
}
