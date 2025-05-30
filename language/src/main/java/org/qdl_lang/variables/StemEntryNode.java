package org.qdl_lang.variables;

import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.HasResultInterface;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Models an entry for a stem variable (that is not a list element). This is needed because these can be
 * expressions for the key and value which can only be determined at runtime, not earlier, hence everything has to be evaluated.
 * Note that these come from the parser directly and are really only used by {@link StemVariableNode} to track its
 * entries.
 * <p>Created by Jeff Gaynor<br>
 * on 9/28/20 at  1:47 PM
 */
public class StemEntryNode implements ExpressionInterface {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
    ExpressionInterface key;
    ExpressionInterface value;

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

    public boolean isDefaultValue() {
        return isDefaultValue;
    }

    public void setDefaultValue(boolean defaultValue) {
        isDefaultValue = defaultValue;
    }

    boolean isDefaultValue = false;
    public ExpressionInterface getKey() {
        return key;
    }

    public void setKey(ExpressionInterface key) {
        this.key = key;
    }

    public Statement getValue() {
        return value;
    }

    public void setValue(ExpressionInterface value) {
        this.value = value;
    }

    boolean evaluated = false;

    public boolean isEvaluated() {
        return evaluated;
    }

    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    @Override
    public QDLValue evaluate(State state) {
        if(!isDefaultValue) {
            getKey().evaluate(state);
            if(getKey() instanceof VariableNode){
                if(((VariableNode)getKey()).getResult() == null){
                    throw new UnknownSymbolException("\'" + ((VariableNode)getKey()).getVariableReference() + "' not found for stem key", getKey());
                }
            }

        }
        getValue().evaluate(state);
        if(getValue() instanceof VariableNode){
            if(((VariableNode)getValue()).getResult() == null){
                throw new UnknownSymbolException("\'" + ((VariableNode)getValue()).getVariableReference() + "' not found for stem value", getValue());
            }
        }
        setEvaluated(true);
        if(getValue() instanceof HasResultInterface){
            setResult(((HasResultInterface)getValue()).getResult());
            return getResult();
        }
        return null;
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
        return null;
    }

    // None of these should **ever** be called, but the interface requires them.
    @Override
    public QDLValue getResult() {
        return null;
    }
QDLValue result;
    @Override
    public void setResult(QDLValue object) {
this.result = object;
    }

    @Override
    public void setResult(Object result) {
        this.result = QDLValue.asQDLValue( result);
    }

    @Override
    public int getResultType() {
        return getResult().getType();
    }

    @Override
        public int getNodeType() {
            return STEM_ENTRY_NODE;
        }
}
