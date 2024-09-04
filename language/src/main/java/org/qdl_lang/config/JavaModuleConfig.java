package org.qdl_lang.config;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/27/20 at  1:07 PM
 */
public class JavaModuleConfig extends ModuleConfigImpl {
    @Override
    public String getType() {
        return QDLConfigurationConstants.MODULE_TYPE_JAVA;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    String className;
}
