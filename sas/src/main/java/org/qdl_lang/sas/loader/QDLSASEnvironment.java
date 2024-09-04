package org.qdl_lang.sas.loader;

import edu.uiuc.ncsa.sas.SASEnvironment;
import edu.uiuc.ncsa.sas.client.SASClient;
import edu.uiuc.ncsa.sas.thing.action.ActionDeserializer;
import edu.uiuc.ncsa.sas.thing.response.ResponseSerializer;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:14 AM
 */
public class QDLSASEnvironment extends SASEnvironment {
    public QDLSASEnvironment(MyLoggingFacade myLogger,
                             Store<? extends SASClient> clientStore,
                             ActionDeserializer actionDeserializer,
                             ResponseSerializer responseSerializer,
                             List<String> accessList) {
        super(myLogger, clientStore, actionDeserializer, responseSerializer,accessList);
    }
}
