package org.qdl_lang.variables;

import org.qdl_lang.state.XKey;
import org.qdl_lang.state.XThing;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/20/22 at  6:07 AM
 */
public class VThing implements XThing {
    public VThing(XKey key, QDLVariable variable) {
        this.key = key;
        this.variable = variable;
    }

    @Override
    public String getName() {
        return key.getKey();
    }

    XKey key;

    @Override
    public XKey getKey() {
        return key;
    }

    public QDLVariable getVariable() {
        return variable;
    }

    public void setVariable(QDLVariable variable) {
        this.variable = variable;
    }

    QDLVariable variable;

    public int getType() {
    return getVariable().getVariableType();
    }
     public boolean isStem(){
        if(variable == null) return false;
        return variable.getValue().isStem();
     }

     public QDLStem getStemValue(){
        return variable.getValue().asStem();
     }
     public Long getLongValue(){
        return variable.getValue().asLong();
     }
     public boolean isNull(){
        if(variable == null) return false;
        return variable.getValue().isNull();
     }
}
