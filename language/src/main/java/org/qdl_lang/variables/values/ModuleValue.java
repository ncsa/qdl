package org.qdl_lang.variables.values;

public class ModuleValue extends QDLValue {
    public ModuleValue(org.qdl_lang.module.Module value) {
        super(value);
        type = MODULE_TYPE;
    }
}
