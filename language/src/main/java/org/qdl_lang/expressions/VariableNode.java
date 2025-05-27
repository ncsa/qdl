package org.qdl_lang.expressions;

import org.qdl_lang.state.State;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  4:28 PM
 */
public
class VariableNode extends ExpressionImpl {
    public VariableNode() {
           super();
    }

    public VariableNode(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    public String getVariableReference() {
        return variableReference;
    }

    public void setVariableReference(String variableReference) {
        this.variableReference = variableReference;
    }

    String variableReference = null;

    public VariableNode(String variableReference) {
        this.variableReference = variableReference;
    }
     public boolean isStem(){
        if(variableReference == null){
            return false;
        }
        return variableReference.endsWith(QDLStem.STEM_INDEX_MARKER);
     }

    @Override
    public QDLValue evaluate(State state) {
        // The contract is that variables resolve to their values when asked and are not mutable.
        // Might change that..
        result = state.getValue(variableReference);
        evaluated = true;
        return result;
    }

    @Override
    public ExpressionNode makeCopy() {
        VariableNode variableNode = new VariableNode(variableReference);
        return variableNode;
    }

    @Override
    public String toString() {
        return "VariableNode{" +
                "variableReference='" + variableReference + '\'' +
                '}';
    }
    @Override
        public int getNodeType() {
            return VARIABLE_NODE;
        }
}
