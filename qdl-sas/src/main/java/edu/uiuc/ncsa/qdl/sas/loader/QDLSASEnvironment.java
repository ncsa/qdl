package edu.uiuc.ncsa.qdl.sas.loader;

import edu.uiuc.ncsa.sas.SASEnvironment;
import edu.uiuc.ncsa.sas.satclient.SATClient;
import edu.uiuc.ncsa.sas.thing.action.ActionDeserializer;
import edu.uiuc.ncsa.sas.thing.response.ResponseSerializer;
import edu.uiuc.ncsa.security.core.Store;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/28/22 at  8:14 AM
 */
public class QDLSASEnvironment extends SASEnvironment {
    public QDLSASEnvironment(MyLoggingFacade myLogger, Store<? extends SATClient> clientStore, ActionDeserializer actionDeserializer, ResponseSerializer responseSerializer) {
        super(myLogger, clientStore, actionDeserializer, responseSerializer);
    }
}
