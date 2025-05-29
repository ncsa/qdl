package org.qdl_lang.variables;

import org.qdl_lang.exceptions.TypeViolationException;
import org.qdl_lang.variables.values.QDLValue;

public class QDLVariable {
    public QDLVariable(QDLValue value) {
        this.value = value;
    }

    public QDLVariable(Object value) {
        this(value instanceof QDLValue ? (QDLValue) value : QDLValue.asQDLValue(value));
    }
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

    /**
     * Dynamic typing is the default for QDL. It means that any variable can be re-assigned
     * to any value at any time
     */
    public static final int TYPE_DYNAMIC = 0;
    /**
     * Manifest typing means that when a variable is first defined, it must have that
     * type from that point forward
     */
    public static final int TYPE_MANIFEST = 1;
    /**
     * Strict typing means that ever variable must be explicitly typed either at or before
     * it is defined. Attempting to define a variable before it is given a type will raise
     * an error.
     */
    public static final int TYPE_STRICT = 2;
    public static final int SCOPE_DEFAULT = 0;
    public static final int SCOPE_INTRINSIC = 1;
    public static final int SCOPE_EXTRINSIC = 2;
}
