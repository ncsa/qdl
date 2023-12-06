package edu.uiuc.ncsa.qdl.config;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/27/20 at  1:09 PM
 */
public class QDLModuleConfig extends ModuleConfigImpl{
    @Override
    public String getType() {
        return QDLConfigurationConstants.MODULE_TYPE_QDL;
    }
    /**
   * The path to the module.
   * @return
   */
  public String getPath() {
      return path;
  }

    public void setPath(String path) {
        this.path = path;
    }

    String path;
}
