package edu.uiuc.ncsa.qdl.config;

import edu.uiuc.ncsa.qdl.xml.SerializationConstants;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/27/20 at  1:09 PM
 */
public abstract class ModuleConfigImpl implements ModuleConfig {

    boolean importOnStart = false;

    @Override
    public boolean isImportOnStart() {
        return importOnStart;
    }

    @Override
    public void setImportOnStart(boolean importOnStart) {
        this.importOnStart = importOnStart;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    boolean use = false;

    @Override
    public boolean isUse() {
        return use;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    String varName;

    @Override
    public String getVarName() {
        return varName;
    }

    String version = SerializationConstants.VERSION_2_0_TAG;

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    boolean failOnError = false;

    @Override
    public boolean isFailOnError() {
        return failOnError;
    }

    @Override
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }
}
