package org.qdl_lang.extensions.dynamodb;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLList;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.RegionMetadata;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (dynamoDbClient != null) {
                throw new IllegalStateException("A connection is already open");
            }
            String accessKeyId;
            String secretAccessKey;
            if (objects[0] instanceof String) {
                accessKeyId = (String) objects[0];
                if (!(objects[1] instanceof String)) {
                    throw new BadArgException("The arguments to " + getName() + " must be a stem or pair of strings.",1);
                }
                secretAccessKey = (String) objects[1];
            } else {
                if (!(objects[0] instanceof QDLStem)) {
                    throw new BadArgException("argument to " + getName() + " must be a stem", 0);
                }
                QDLStem cfg = (QDLStem) objects[0];
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

                accessKeyId = cfg.getString(ACCESS_KEY_ID);
                secretAccessKey = cfg.getString(SECRET_ACCESS_KEY);
                if (cfg.containsKey(REGION_KEY)) {
                    region = Region.of(cfg.getString(REGION_KEY));
                }

            }
            AwsCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            // Instantiate a DynamoDB client for the region that uses the credentials.
            dynamoDbClient = DynamoDbClient.builder()
                    .region(region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .httpClientBuilder(ApacheHttpClient.builder())
                    .build();
            return Boolean.TRUE;
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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            QDLStem out;
            String keyVal;
            checkInit();
            if (!(objects[0] instanceof String)) {
                throw new BadArgException("first argument for " + getName() + " must be a string, and is the key value", 0);
            }
            if (tableName == null) {
                throw new IllegalStateException("You must set the " + TABLE_NAME_KEY + " before calling " + getName());
            }
            if (partitionKey == null) {
                throw new IllegalStateException("You must set the " + PARTITION_KEY + " before calling " + getName());
            }

            keyVal = (String) objects[0];
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
            out = mapToStem(item);

            return out;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            d.add(getName() + "(key) - get the results for the given key");
            d.add("Note that these will be converted to native QDL types, so numbers");
            d.add("are indeed numbers, binary values are b64 encoded strings and such.");
            d.add("Maps and lists are faithfully converted as well.");
            return d;
        }


    }
    /*
      Below starts the outline for writing values. There are some technical issues involved
      since roundtripping values would seem impossible. The fly in the ointment is binary values
      which end up as b64 encoded strings. Unless there is some way to track these by type,
      then they are identical to just messy strings. This implies that, perhaps, types should be
      added to them, so you get {'value':v, 'type':'b64'} or some such. Don't want to add types for
      all values since that would be redundant and make stems hugely messy.
     */
    protected Map<String, AttributeValue> stemToMap(QDLStem stem) {
        Map<String, AttributeValue> outMap = new HashMap<>();

        for (Object key : stem.keySet()) {

        }
        return outMap;

    }

    protected AttributeValue convertToAV(Long lValue) {
        return AttributeValue.builder().n(lValue.toString()).build();
    }

    protected AttributeValue convertToAV(String sValue) {
        return AttributeValue.builder().s(sValue).build();
    }

    protected AttributeValue convertToAV(BigDecimal bigDecimal) {
        return AttributeValue.builder().n(bigDecimal.toString()).build();
    }

    protected AttributeValue convertToAV(Boolean bValue) {
        return AttributeValue.builder().bool(bValue).build();
    }
    protected AttributeValue convertToAV(QDLNull bValue) {
    return AttributeValue.builder().nul(Boolean.TRUE).build();
}
    protected AttributeValue convertToAV(QDLList qdlList) {
        List list = qdlList.values();
        int type = -1;
        boolean isMixedType = false;
        boolean isFirstPass = true;
        for (Object obj : list) {
            if (isFirstPass) {
                isFirstPass = false;
                type = Constant.getType(obj);
            } else {
                if (type != Constant.getType(obj)) {
                    isMixedType = true;
                    break;
                }
            }
        }
        // So now we can decide how to create this.
        if(isMixedType){

            //return AttributeValue.builder().l(sArray).build();

        }
        String[] sArray;
        switch (type){
            case Constant.STRING_TYPE:
                 sArray = new String[list.size()];
                for(int i = 0; i < list.size(); i++){
                     sArray[i] = (String)list.get(i);
                }
                return AttributeValue.builder().ss(sArray).build();
            case Constant.LONG_TYPE:
            case Constant.DECIMAL_TYPE:
                sArray = new String[list.size()];
                for(int i = 0; i < list.size(); i++){
                     sArray[i] = (String)list.get(i);
                }
                return AttributeValue.builder().ns(sArray).build();
            case Constant.BOOLEAN_TYPE:
                // Actually Dynamo DB only supports single boolean, so a list/set of them
                // is impossible.
           //     return AttributeValue.builder().bs(sArray).build();

        }

        return null;
    }


    protected QDLStem mapToStem(Map<String, AttributeValue> items) {

        QDLStem out = new QDLStem();
        for (String key : items.keySet()) {
            AttributeValue attributeValue = items.get(key);
            out.put(key, convertToQDL(attributeValue));
        }
        return out;
    }

    protected Object convertToQDL(AttributeValue attributeValue) {
        QDLList list;
        QDLStem stem;
        switch (attributeValue.type()) {
            case S:
                return attributeValue.s();
            case BOOL:
                return attributeValue.bool();
            case B:
                return convertToB64(attributeValue);
            case NUL:
                return QDLNull.getInstance();
            case N:
                return convertToNumber(attributeValue.n());
            case BS: // list of bytes
                List<SdkBytes> bytesList = attributeValue.bs();
                list = new QDLList();
                for (SdkBytes bbb : bytesList) {
                    list.add(convertToB64(bbb));
                }
                stem = new QDLStem();
                stem.setQDLList(list);
                return stem;
            case SS: // list fo strings
                list = new QDLList();
                list.addAll(attributeValue.ss());
                stem = new QDLStem();
                stem.setQDLList(list);
                return stem;
            case NS: // list of numbers
                List<String> numbers = attributeValue.ns();
                list = new QDLList();
                for (String n : numbers) {
                    list.add(convertToNumber(n));
                }
                stem = new QDLStem();
                stem.setQDLList(list);
                return stem;
            case L: // mixed list type
                List<AttributeValue> listValues = attributeValue.l();
                list = new QDLList();
                for (AttributeValue n : listValues) {
                    list.add(convertToQDL(n));
                }
                stem = new QDLStem();
                stem.setQDLList(list);
                return stem;
            case M: // mixed map
                stem = new QDLStem();
                Map<String, AttributeValue> m = attributeValue.m();
                for (String key : m.keySet()) {
                    stem.put(key, convertToQDL(m.get(key)));
                }
                return stem;
            default:
                throw new IllegalArgumentException("unknown attribute type '" + attributeValue.type() + "'");
        }
    }

    Object convertToNumber(String v) {
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException nfx) {

        }
        return new BigDecimal(v);
    }

    String convertToB64(SdkBytes sdkBytes) {
        return Base64.encodeBase64URLSafeString(sdkBytes.asByteArray());

    }

    String convertToB64(AttributeValue attributeValue) {
        return convertToB64(attributeValue.b());
    }

    private DynamoDbClient dynamoDbClient;
    public static final String ACCESS_KEY_ID = "access_key_id";
    public static final String SECRET_ACCESS_KEY = "secret_access_key";
    public static final String REGION_KEY = "region";
    public static final Region DEFAULT_REGION = Region.US_EAST_2;
    public static final String CLOSE = "close";
    public static final String TABLE_NAME_KEY = "table_name";
    public static final String PARTITION_KEY = "partition_key";
    public static Region region = DEFAULT_REGION;
    public static String tableName = null;
    public static String partitionKey = null;


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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (dynamoDbClient != null) {
                dynamoDbClient.close();
                dynamoDbClient = null;
            }
            return Boolean.TRUE;
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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            List<Region> regions = Region.regions();
            QDLStem stem = new QDLStem();
            for (Region region : regions) {
                QDLStem r = new QDLStem();
                r.put("id", region.id());
                r.put("is_global", region.isGlobalRegion());
                RegionMetadata regionMetadata = region.metadata();
                if (regionMetadata != null) {
                    r.put("description", regionMetadata.description());
                }
                stem.getQDLList().add(r);
            }
            return stem;
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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (objects.length == 0) {
                if (region == null) return QDLNull.getInstance();
                return region.id();
            }
            if (!(objects[0] instanceof String)) {
                throw new BadArgException("The argument to " + getName() + " must be a string", 0);
            }
            String name = (String) objects[0];
            Region oldRegion = region;
            region = Region.of(name);
            return oldRegion.id();
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
            d.add("A default value of '" + DEFAULT_REGION + "' is set initially" );
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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (objects.length == 0) {
                if (tableName == null) return QDLNull.getInstance();
                return tableName;
            }
            if (!(objects[0] instanceof String)) {
                throw new BadArgException("The argument to " + getName() + " must be a string", 0);
            }
            String name = (String) objects[0];
            String oldName = tableName;
            tableName = name;
            if (oldName == null) {
                return QDLNull.getInstance();
            }
            return oldName;
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
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (objects.length == 0) {
                if (partitionKey == null) return QDLNull.getInstance();
                return partitionKey;
            }
            if (!(objects[0] instanceof String)) {
                throw new IllegalArgumentException("The argument to " + getName() + " must be a string");
            }
            String name = (String) objects[0];
            String oldName = partitionKey;
            partitionKey = name;
            if (oldName == null) {
                return QDLNull.getInstance();
            }
            return oldName;
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

}
