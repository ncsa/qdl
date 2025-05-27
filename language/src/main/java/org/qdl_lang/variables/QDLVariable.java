package org.qdl_lang.variables;

import org.qdl_lang.exceptions.TypeViolationException;
import org.qdl_lang.variables.values.QDLValue;

public class QDLVariable {
    public QDLValue getValue() {
        return value;
    }

    public void setValue(QDLValue value) {
        if(hasValue()) {
            if(getVariableType() != TYPE_DYNAMIC){
                if(getValue().getType() != value.getType()){
                    throw new TypeViolationException("attempt to set a " + Constant.getType(getValue().getType()) + " to a " + Constant.getType(value.getType()), null);
                }
            }
        }
        this.value = value;
    }

    QDLValue value = null;

    public boolean hasValue(){
        return value != null;
    }
    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    int scope = SCOPE_DEFAULT;

    public int getVariableType() {
        return variableType;
    }

    public void setVariableType(int variableType) {
        this.variableType = variableType;
    }

    int variableType = TYPE_DYNAMIC;

    public static int TYPE_DYNAMIC = 0;
    public static int TYPE_MANIFEST = 1;
    public static int SCOPE_DEFAULT = 0;
    public static int SCOPE_INTRINSIC = 1;
    public static int SCOPE_EXTRINSIC = 2;
}
