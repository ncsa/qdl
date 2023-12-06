package edu.uiuc.ncsa.qdl.config;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/27/20 at  1:06 PM
 */
public interface ModuleConfig {
    String getType();
    boolean isImportOnStart();
    void setImportOnStart(boolean importOnStart);
    boolean isUse();
    String getVarName();
    void setUse(boolean useModule);
    void setVarName(String varName);
    String getVersion();
    void setVersion(String version);
    boolean isFailOnError();
    void setFailOnError(boolean failOnError);
}
