package org.qdl_lang.sas.loader;

import edu.uiuc.ncsa.sas.loader.SASBootstrapper;
import edu.uiuc.ncsa.security.servlet.Initialization;


/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:13 AM
 */
public class QDLSASBootstrapper extends SASBootstrapper {
    private static final String QDL_SAS_CONFIG_FILE_KEY = "sas:qdl.config.file";
    private static final String QDL_SAS_CONFIG_NAME_KEY = "sas:qdl.config.name";


    @Override
    public Initialization getInitialization() {
        return super.getInitialization();
    }
}
