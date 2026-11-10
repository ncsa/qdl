package org.qdl_lang.extensions.dynamodb;

import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;
import org.kordamp.json.JSONObject;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLNullValue;
import org.qdl_lang.variables.values.QDLValue;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/20/24 at  9:54 AM
 */
public class DynamoDB implements QDLMetaModule {
    @Override
    public JSONObject serializeToJSON() {
        // no state to serialize.
        return new JSONObject();
    }

    DynamoCodec codec = new DynamoCodec();
    @Override
    public void deserializeFromJSON(JSONObject json) {
        // no state to reconstruct
    }

    public static final String OPEN = "open";

    public class Open implements QDLFunction {
        @Override
        public String getName() {
            return OPEN;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (dynamoDbClient != null) {
                throw new IllegalStateException("A connection is already open");
            }
            String accessKeyId;
            String secretAccessKey;
            String address = null;
            if (qdlValues[0].isString()) {
                accessKeyId = qdlValues[0].asString();
                if (!(qdlValues[1].isString())) {
                    throw new BadArgException("The arguments to " + getName() + " must be a stem or pair of strings.", 1);
                }
                secretAccessKey = qdlValues[1].asString();
            } else {
                if (!(qdlValues[0].isStem())) {
                    throw new BadArgException("argument to " + getName() + " must be a stem", 0);
                }
                QDLStem cfg = qdlValues[0].asStem();
                if (!cfg.containsKey(ACCESS_KEY_ID)) {
                    throw new BadArgException("missing " + ACCESS_KEY_ID, 0);
                }
                if (!cfg.containsKey(SECRET_ACCESS_KEY)) {
                    throw new BadArgException("missing " + SECRET_ACCESS_KEY, 0);
                }
                if (cfg.containsKey(TABLE_NAME_KEY)) {
                    tableName = cfg.getString(TABLE_NAME_KEY);
                }
                if (cfg.containsKey(PARTITION_KEY)) {
                    partitionKey = cfg.getString(PARTITION_KEY);
                }

                if (cfg.containsKey(ADDRESS_KEY)) {

                    address = cfg.getString(ADDRESS_KEY);
                }
                accessKeyId = cfg.getString(ACCESS_KEY_ID);
                secretAccessKey = cfg.getString(SECRET_ACCESS_KEY);
                if (cfg.containsKey(REGION_KEY)) {
                    region = Region.of(cfg.getString(REGION_KEY));
                }

            }
            AwsCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            // Instantiate a DynamoDB client for the region that uses the credentials.
            if (address != null) {
                dynamoDbClient = DynamoDbClient.builder()
                        .region(region)
                        .endpointOverride(URI.create(address))
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .httpClientBuilder(ApacheHttpClient.builder())
                        .build();
            } else {
                dynamoDbClient = DynamoDbClient.builder()
                        .region(region)
                        .credentialsProvider(StaticCredentialsProvider.create(credentials))
                        .httpClientBuilder(ApacheHttpClient.builder())
                        .build();
            }

            return BooleanValue.True;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            switch (argCount) {
                case 1:
                    d.add(getName() + "(cfg.) - open a connection to a dynamo database. It needs the following");
                    d.add("\n" +
                            "+-------------------+-------+--------------------------------------------------------+\n" +
                            "|       Name        |  Req? |                      Description                       |\n" +
                            "+-------------------+-------+--------------------------------------------------------+\n" +
                            "| access_key_id     |   Y   | The access key. Part of your AWS credentials           |\n" +
                            "| secret_access_key |   Y   | The secret access key. Part of your AWS credentials    |\n" +
                            "| region            |   N   | The AWS region                                         |\n" +
                            "| table_name        |   N   | The table name for all queries. Can be set             |\n" +
                            "| partition_key     |   N   | The partition key for all queries. Can be set          |\n" +
                            "+-------------------+-------+--------------------------------------------------------+");
                    break;
                case 2:
                    d.add(getName() + "(access_key_id,secret_access_key) = open the connection using the default region");
                    d.add("and supplied credentials. Note that you still must supply both the " + TABLE_NAME_KEY + " and the");
                    d.add(PARTITION_KEY + " before invoking " + GET_ITEM);
                    break;
            }
            return d;
        }
    }


    public static final String GET_ITEM = "get";

    public class Get implements QDLFunction {
        @Override
        public String getName() {
            return GET_ITEM;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] objects, State state) throws Throwable {
            QDLStem out;
            String keyVal;
            checkInit();
            if (!(objects[0].isString())) {
                throw new BadArgException("first argument for " + getName() + " must be a string, and is the key value", 0);
            }
            if (tableName == null) {
                throw new IllegalStateException("You must set the " + TABLE_NAME_KEY + " before calling " + getName());
            }
            if (partitionKey == null) {
                throw new IllegalStateException("You must set the " + PARTITION_KEY + " before calling " + getName());
            }

            keyVal = objects[0].asString();
            Map<String, AttributeValue> item = null;

            // Use the key and key value to build a request for the item
            // with that key value.
            HashMap<String, AttributeValue> keyToGet = new HashMap<>();
            keyToGet.put(partitionKey, AttributeValue.builder().s(keyVal).build());

            GetItemRequest request = GetItemRequest.builder()
                    .key(keyToGet)
                    .tableName(tableName)
                    .build();

            item = dynamoDbClient.getItem(request).item();
            out = codec.mapToStem(item);

            return asQDLValue(out);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(getName() + "(key) - get the results for the given key");
            d.add("Note that these will be converted to native QDL values, so numbers");
            d.add("are indeed numbers, binary values are b64 encoded strings and such.");
            d.add("Maps and lists are faithfully converted as well.");
            return d;
        }


    }


    private DynamoDbClient dynamoDbClient;
    public static final String ACCESS_KEY_ID = "access_key_id";
    public static final String SECRET_ACCESS_KEY = "secret_access_key";
    public static final String REGION_KEY = "region";
    public static final Region DEFAULT_REGION = Region.US_EAST_2;
    public static final String CLOSE = "close";
    public static final String TABLE_NAME_KEY = "table_name";
    public static final String PARTITION_KEY = "partition_key";
    public static final String ADDRESS_KEY = "address_key";
    public   Region region = DEFAULT_REGION;
    // Fix for https://jira.ncsa.illinois.edu/browse/CIL-2478, https://github.com/ncsa/qdl/issues/157
    public  String tableName = null;
    public  String partitionKey = null;


    public class Close implements QDLFunction {
        @Override
        public String getName() {
            return CLOSE;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (dynamoDbClient != null) {
                dynamoDbClient.close();
                dynamoDbClient = null;
            }
            return BooleanValue.True;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(getName() + "() - close the connection. Any attempt to get data after this will fail.");
            return d;

        }
    }

    protected void checkInit() {
        if (dynamoDbClient == null) {
            throw new IllegalStateException("You must call " + OPEN + " before using this module");
        }
    }

    public static String REGIONS = "regions";

    public class Regions implements QDLFunction {
        @Override
        public String getName() {
            return REGIONS;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            List<Region> regions = Region.regions();
            QDLStem stem = new QDLStem();
            for (Region region : regions) {
                QDLStem r = new QDLStem();
                r.put(from("id"), region.id());
                r.put(from("is_global"), region.isGlobalRegion());
                RegionMetadata regionMetadata = region.metadata();
                if (regionMetadata != null) {
                    r.put(from("description"), regionMetadata.description());
                }
                stem.getQDLList().add(asQDLValue(r));
            }
            return asQDLValue(stem);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(getName() + "() - list regions, along with some information.");
            d.add("You can get the region using the \"" + REGION_FUNCTION + "\" function and the id");
            return d;
        }
    }

    public static final String REGION_FUNCTION = "region";

    public class RegionFunction implements QDLFunction {
        @Override
        public String getName() {
            return REGION_FUNCTION;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 0) {
                if (region == null) return QDLNullValue.getNullValue();
                return asQDLValue(region.id());
            }
            if (!(qdlValues[0].isString())) {
                throw new BadArgException("The argument to " + getName() + " must be a string", 0);
            }
            String name = qdlValues[0].asString();
            Region oldRegion = region;
            region = Region.of(name);
            return asQDLValue(oldRegion.id());
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            switch (argCount) {
                case 0:
                    d.add(getName() + "() - query the current value;");
                    break;
                case 1:
                    d.add(getName() + "(new_value) - set to the new value, returning the old.");
            }
            d.add("A default value of '" + DEFAULT_REGION + "' is set initially");
            return d;
        }
    }

    public class TableNameFunction implements QDLFunction {
        @Override
        public String getName() {
            return TABLE_NAME_FUNCTION;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 0) {
                if (tableName == null) return QDLNullValue.getNullValue();
                return asQDLValue(tableName);
            }
            if (!(qdlValues[0].isString())) {
                throw new BadArgException("The argument to " + getName() + " must be a string", 0);
            }
            String name = qdlValues[0].asString();
            String oldName = tableName;
            tableName = name;
            if (oldName == null) {
                return QDLNullValue.getNullValue();
            }
            return asQDLValue(oldName);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            switch (argCount) {
                case 0:
                    d.add(getName() + "() - query the current value;");
                    break;
                case 1:
                    d.add(getName() + "(new_value) - set to the new value, returning the old.");

            }
            return d;
        }
    }

    public static final String PARTITION_KEY_FUNCTION = "partition_key";

    public class PartitionKeyFunction implements QDLFunction {
        @Override
        public String getName() {
            return PARTITION_KEY_FUNCTION;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 0) {
                if (partitionKey == null) return QDLNullValue.getNullValue();
                return asQDLValue(partitionKey);
            }
            if (!(qdlValues[0].isString())) {
                throw new IllegalArgumentException("The argument to " + getName() + " must be a string");
            }
            String name = qdlValues[0].asString();
            String oldName = partitionKey;
            partitionKey = name;
            if (oldName == null) {
                return QDLNullValue.getNullValue();
            }
            return asQDLValue(oldName);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            switch (argCount) {
                case 0:
                    d.add(getName() + "() - query the current value;");
                    break;
                case 1:
                    d.add(getName() + "(new_value) - set to the new value, returning the old.");

            }
            return d;
        }
    }

    public static final String TABLE_NAME_FUNCTION = "table_name";
    public static final String CREATE_TABLE_FUNCTION = "create_table";

    public class CreateTableFunction implements QDLFunction {
        @Override
        public String getName() {
            return CREATE_TABLE_FUNCTION;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            DynamoDbWaiter dbWaiter = dynamoDbClient.waiter();
            String key = null;
            CreateTableRequest request = CreateTableRequest.builder()
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(key)
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .keySchema(KeySchemaElement.builder()
                            .attributeName(key)
                            .keyType(KeyType.HASH)
                            .build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(new Long(10))
                            .writeCapacityUnits(new Long(10))
                            .build())
                    .tableName(tableName)
                    .build();
            String newTable = "";
            try {
                CreateTableResponse response = dynamoDbClient.createTable(request);
                DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build();

                // Wait until the Amazon DynamoDB table is created
                WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
                waiterResponse.matched().response().ifPresent(System.out::println);

                newTable = response.tableDescription().tableName();
                return QDLValue.asQDLValue(newTable);

            } catch (DynamoDbException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }

            return null;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(getName() + "(stem.) - Create a new table with the given types.");
            d.add("See the manual for details.");
            d.add("Example.");
            d.add("   t. := {\n" +
                    "         'table_name': 't42',\n" +
                    "         'billing_mode' : 'PAY_PER_REQUEST',\n" +
                    "         'attributes':[{'attribute_type':'integer','name':'date'}].\n" +
                    "         'key':[{'name':'user_uid','key_type':'hash','attribute_type':'string'}]\n" +
                    "       };\n");
            d.add("   db#" + getName() + "(t.);");
                d.add("true");
            return d;
        }
        /* table create stem. Edit as necessary.
        t. := {
        'table_name': 't42',
        'billing_mode' : 'PAY_PER_REQUEST',
        'attributes':[{'attribute_type':'integer','name':'date'}].
        'key':[{'name':'user_uid','key_type':'hash','attribute_type':'string'}]
   };*/
    }

    public static final String DELETE_TABLE_FUNCTION = "delete_table";

    public class DeleteTableFunction implements QDLFunction {
        @Override
        public String getName() {
            return DELETE_TABLE_FUNCTION;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            try {

                // 2. Create the delete request
                DeleteTableRequest request = DeleteTableRequest.builder()
                        .tableName(tableName)
                        .build();

                // 3. Execute the drop operation
                System.out.println("Deleting table: " + tableName + "...");
                dynamoDbClient.deleteTable(request);
                System.out.println("Table deletion request submitted successfully.");

            } catch (DynamoDbException e) {
                if(DebugUtil.isEnabled()){
                    e.printStackTrace();
                }
                //System.err.println("Error deleting table: " + e.getMessage());
                return BooleanValue.False;
            }
            return BooleanValue.True;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(DELETE_TABLE_FUNCTION + " table_name - delete a table");
            d.add("Note that there is no easy recovery from this.");
            d.add("returns true if successful, false otherwise.");
            return d;
        }
    }
    public static final String DYNAMO_SCALAR_TYPE_STRING = "S";
    public static final String DYNAMO_SCALAR_TYPE_NUMERIC = "N";
    public static final String DYNAMO_SCALAR_TYPE_BINARY = "B";
    public static final String DYNAMO_SCALAR_TYPE_BOOLEAN = "BOOL";
    public static final String DYNAMO_SCALAR_TYPE_NULL = "NULL";
    public static final String DYNAMO_TYPE_LIST = "L";
    public static final String DYNAMO_TYPE_MAP = "M";
    public static final String DYNAMO_SET_TYPE_STRING = "SS";
    public static final String DYNAMO_SET_TYPE_NUMERIC = "NS";
    public static final String DYNAMO_SET_TYPE_BINARY = "BS";

    /**
     * Given a QDL type, what is corresponding Dynamo type?
     */
    public class DynamoTypes implements QDLVariable {
        @Override
        public String getName() {
            return "DYNAMO_TYPES.";
        }

        @Override
        public QDLStem getValue() {
            QDLStem stem = new QDLStem();
            stem.put(QDLKey.from(Constant.BOOLEAN_NAME), asQDLValue(DYNAMO_SCALAR_TYPE_BOOLEAN));
            stem.put(QDLKey.from(Constant.STRING_NAME), asQDLValue(DYNAMO_SCALAR_TYPE_STRING));
            stem.put(QDLKey.from(Constant.DECIMAL_NAME), asQDLValue(DYNAMO_SCALAR_TYPE_NUMERIC));
            stem.put(QDLKey.from(Constant.NULL_NAME), asQDLValue(DYNAMO_SCALAR_TYPE_NULL));
            stem.put(QDLKey.from(Constant.LIST_NAME), asQDLValue(DYNAMO_TYPE_LIST));
            stem.put(QDLKey.from(Constant.STEM_NAME), asQDLValue(DYNAMO_TYPE_MAP));
            stem.put(QDLKey.from(Constant.SET_NAME), asQDLValue(DYNAMO_TYPE_MAP));
            return stem;
        }
    }
    public class QDLTypes implements QDLVariable {
        @Override
        public String getName() {
            return "QDL_TYPES.";
        }

        @Override
        public QDLStem  getValue() {
            QDLStem stem = new QDLStem();
            stem.put(QDLKey.from(DYNAMO_SCALAR_TYPE_BOOLEAN), asQDLValue(Constant.BOOLEAN_NAME));
            stem.put(QDLKey.from(DYNAMO_SCALAR_TYPE_NULL), asQDLValue(Constant.NULL_NAME));
            stem.put(QDLKey.from(DYNAMO_SCALAR_TYPE_NUMERIC), asQDLValue(Constant.DECIMAL_NAME));
            stem.put(QDLKey.from(DYNAMO_SCALAR_TYPE_STRING), asQDLValue(Constant.STRING_NAME));
            stem.put(QDLKey.from(DYNAMO_SET_TYPE_BINARY), asQDLValue(Constant.SET_NAME));
            stem.put(QDLKey.from(DYNAMO_SET_TYPE_STRING), asQDLValue(Constant.SET_NAME));
            stem.put(QDLKey.from(DYNAMO_SET_TYPE_NUMERIC), asQDLValue(Constant.SET_NAME));
            stem.put(QDLKey.from(DYNAMO_TYPE_LIST), asQDLValue(Constant.LIST_NAME));
            stem.put(QDLKey.from(DYNAMO_TYPE_MAP), asQDLValue(Constant.STEM_NAME));
            return stem;
        }
    }

    public static final String VERSION_NAME = "version";
    public static final String VERSION = "1.1";
    public class Version implements QDLFunction{
        @Override
        public String getName() {
            return VERSION_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            return QDLValue.asQDLValue(VERSION);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(getName() + "() - the current version of the DynamoDB module.");
            d.add("This module is at version " + VERSION);
            return d;
        }
    }

}
