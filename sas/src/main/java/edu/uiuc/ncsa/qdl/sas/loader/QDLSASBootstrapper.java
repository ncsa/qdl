package edu.uiuc.ncsa.qdl.sas.loader;

import edu.uiuc.ncsa.sas.loader.SASBootstrapper;
import edu.uiuc.ncsa.sas.loader.SASConfigurationLoader;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import edu.uiuc.ncsa.security.servlet.Initialization;
import edu.uiuc.ncsa.security.servlet.ServletConfigUtil;
import org.apache.commons.configuration.tree.ConfigurationNode;

import javax.servlet.ServletContext;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:13 AM
 */
public class QDLSASBootstrapper extends SASBootstrapper {
    private static final String QDL_SAS_CONFIG_FILE_KEY = "sas:qdl.config.file";
    private static final String QDL_SAS_CONFIG_NAME_KEY = "sas:qdl.config.name";
    @Override
    public SASConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        return new QDLSASConfigurationLoader(node);
    }

    @Override
    protected ConfigurationNode getNode(ServletContext servletContext) throws Exception {
        return ServletConfigUtil.findConfigurationNode(servletContext, QDL_SAS_CONFIG_FILE_KEY, QDL_SAS_CONFIG_NAME_KEY, SAS_CONFIG_TAG);
    }

    @Override
    public Initialization getInitialization() {
        return super.getInitialization();
    }
}
