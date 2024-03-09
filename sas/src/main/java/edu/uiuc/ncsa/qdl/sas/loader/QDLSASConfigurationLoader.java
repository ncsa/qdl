package edu.uiuc.ncsa.qdl.sas.loader;

import edu.uiuc.ncsa.qdl.sas.action.QDLSASActionDeserializer;
import edu.uiuc.ncsa.sas.loader.SASConfigurationLoader;
import edu.uiuc.ncsa.sas.client.SASClient;
import edu.uiuc.ncsa.sas.thing.response.ResponseSerializer;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:14 AM
 */
public class QDLSASConfigurationLoader<T extends QDLSASEnvironment> extends SASConfigurationLoader<T> {
    public QDLSASConfigurationLoader(ConfigurationNode node, MyLoggingFacade logger) {
        super(node, logger);
    }

    public QDLSASConfigurationLoader(ConfigurationNode node) {
        super(node);
    }

    @Override
    public T createInstance() {
        return (T) new QDLSASEnvironment(loggerProvider.get(),
                (Store<? extends SASClient>) getCSP().get(),
                new QDLSASActionDeserializer(),
                new ResponseSerializer(),
                getAccessList());
    }
}
