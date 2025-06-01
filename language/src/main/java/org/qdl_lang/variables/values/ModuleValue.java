package org.qdl_lang.variables.values;

import org.qdl_lang.expressions.module.Module;

public class ModuleValue extends QDLValue {
    public ModuleValue(Module value) {
        super(value);
        type = MODULE_TYPE;
    }
}
