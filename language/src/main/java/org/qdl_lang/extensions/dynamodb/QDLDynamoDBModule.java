package org.qdl_lang.extensions.dynamodb;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/5/22 at  12:02 PM
 */
public class QDLDynamoDBModule extends JavaModule {
    public QDLDynamoDBModule() {
    }

    public QDLDynamoDBModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        QDLDynamoDBModule module = new QDLDynamoDBModule(URI.create("qdl:/tools/db"), "db");
        DynamoDB dynamoDB = new DynamoDB();
        module.setMetaClass(dynamoDB);
        funcs.add(dynamoDB.new Get());
        funcs.add(dynamoDB.new Open());
        funcs.add(dynamoDB.new Close());
        funcs.add(dynamoDB.new Regions());
        funcs.add(dynamoDB.new RegionFunction());
        funcs.add(dynamoDB.new TableNameFunction());
        funcs.add(dynamoDB.new PartitionKeyFunction());
        module.addFunctions(funcs);
        if (state != null) {
            module.init(state);
        }
        setupModule(module);
        return module;
    }
    @Override
    public List<String> getDescription() {
        if(doxx == null){
            doxx = new ArrayList<>();
            doxx.add("Module for AWS (Amazon) Dynamo DB database operations in QDL.");
            doxx.add("There are several basic functions available");
            doxx.add("At this time, we only support reading from a Dynamo DB");
            doxx.add("The basic usage is to call " + DynamoDB.OPEN +
                    " a connection, then " + DynamoDB.GET_ITEM +  "  then when done,");
            doxx.add(DynamoDB.CLOSE + " to close the connection");
            doxx.add("Other functions are mutators to set connection information and ");
            doxx.add("the " + DynamoDB.REGIONS + " function to query supported regions.");
            doxx.add("See the detailed help for each function for more");
            doxx.add("You do need to set the table name, and partition key before getting values.");
       }
        return doxx;
    }
    List<String> doxx = null;
}
