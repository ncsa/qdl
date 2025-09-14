package org.qdl_lang.sas.loader;

import edu.uiuc.ncsa.sas.client.SASClient;
import edu.uiuc.ncsa.sas.loader.SASCFConfigurationLoader;
import edu.uiuc.ncsa.sas.loader.SASConfigurationLoader;
import edu.uiuc.ncsa.sas.thing.response.ResponseSerializer;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.cf.CFNode;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.qdl_lang.sas.action.QDLSASActionDeserializer;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:14 AM
 */
public class QDLSASCFConfigurationLoader<T extends QDLSASEnvironment> extends SASCFConfigurationLoader<T> {
    public QDLSASCFConfigurationLoader(CFNode node, MyLoggingFacade logger) {
        super(node, logger);
    }

    public QDLSASCFConfigurationLoader(CFNode node) {
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
