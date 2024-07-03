package edu.uiuc.ncsa.qdl.extensions.dynamodb;

import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;

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
            doxx.add(DynamoDB.OPEN + " open a connection to a dynamo DB server");
            doxx.add(DynamoDB.CLOSE + " close an existing connection.");
            doxx.add(DynamoDB.GET_ITEM + " get Dynamo DB objects. The result is a stem of objects");
            doxx.add(DynamoDB.REGIONS + " List regions available. Default is " + DynamoDB.DEFAULT_REGION);
            doxx.add(DynamoDB.REGION_FUNCTION + " Set the current region. " + DynamoDB.DEFAULT_REGION);
            doxx.add(DynamoDB.TABLE_NAME_FUNCTION + " Set or get the current table name.");
            doxx.add(DynamoDB.PARTITION_KEY_FUNCTION + " Set or get the current partition key.");
            doxx.add("You do need to set the table name, and partition key before getting values.");
       }
        return doxx;
    }
    List<String> doxx = null;
}
