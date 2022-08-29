package edu.uiuc.ncsa.qdl.sas.loader;

import edu.uiuc.ncsa.sas.loader.SASBootstrapper;
import edu.uiuc.ncsa.sas.loader.SASConfigurationLoader;
import edu.uiuc.ncsa.security.core.exceptions.MyConfigurationException;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:13 AM
 */
public class QDLSASBootstrapper extends SASBootstrapper {
    @Override
    public SASConfigurationLoader getConfigurationLoader(ConfigurationNode node) throws MyConfigurationException {
        return new QDLSASConfigurationLoader(node);
    }
}
