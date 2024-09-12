package org.qdl_lang.extensions.examples.basic;

import org.qdl_lang.extensions.QDLVariable;

/**
 * This shows that you can set individual values with a stem. The stem,
 * however, is in the module. If you import this module to the variable
 * <code>eg</code>, then it creates the value <code>eg#a.</code>
 */
public class StemEntryVar implements QDLVariable {
    public static String STEM_NAME = "a.5";
    @Override
    public String getName() {
        return STEM_NAME    ;
    }

    @Override
    public Object getValue() {
        return "test value";
    }
}
