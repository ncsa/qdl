package edu.uiuc.ncsa.qdl.extensions.database;

import edu.uiuc.ncsa.qdl.evaluate.SystemEvaluator;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.extensions.QDLModuleMetaClass;
import edu.uiuc.ncsa.qdl.extensions.QDLVariable;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.util.InputFormUtil;
import edu.uiuc.ncsa.qdl.variables.*;
import edu.uiuc.ncsa.security.core.configuration.StorageConfigurationTags;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import edu.uiuc.ncsa.security.core.util.PoolException;
import edu.uiuc.ncsa.security.storage.sql.ConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.ConnectionRecord;
import edu.uiuc.ncsa.security.storage.sql.SQLConnectionImpl;
import edu.uiuc.ncsa.security.storage.sql.derby.DerbyConnectionParameters;
import edu.uiuc.ncsa.security.storage.sql.internals.ColumnMap;
import edu.uiuc.ncsa.security.storage.sql.mariadb.MariaDBConnectionParameters;
import edu.uiuc.ncsa.security.storage.sql.mysql.MySQLConnectionParameters;
import edu.uiuc.ncsa.security.storage.sql.mysql.MySQLConnectionPool;
import edu.uiuc.ncsa.security.storage.sql.postgres.PostgresConnectionParameters;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider.*;
import static edu.uiuc.ncsa.security.storage.sql.SQLDatabase.rsToMap;
import static java.sql.Types.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/5/22 at  7:11 AM
 */
/*
   A test file is in ~/dev/csd/config/test/db-test.qdl that has connection information and
   samples for using this class.
 */
public class QDLDB implements QDLModuleMetaClass {
    public static String CONNECT_COMMAND = "connect";
    public static final String MYSQL_TYPE = "mysql";
    public static final String MARIADB_TYPE = "mariadb";
    public static final String POSTGRES_TYPE = "postgres";
    public static final String DYNAMODB_TYPE = "dynamodb";
    public static final String DERBY_TYPE = "derby";
    public static final String TYPE_ARG = "type";
    ConnectionPool connectionPool;
    boolean isConnected = false;

    public class Connect implements QDLFunction {

        @Override
        public String getName() {
            return CONNECT_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            if (objects.length != 1) {
                throw new IllegalArgumentException("missing argument.");
            }
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires a stem as its argument");
            }
            QDLStem stemVariable = (QDLStem) objects[0];
            if (!stemVariable.containsKey(TYPE_ARG)) {
                throw new IllegalArgumentException("missing " + TYPE_ARG + " argument");
            }
            String type = stemVariable.getString(TYPE_ARG);
            JSONObject json = (JSONObject) stemVariable.toJSON();
            json.remove(TYPE_ARG); // don't confuse connector
            SQLConnectionImpl connection;
            switch (type) {
                case MARIADB_TYPE:
                    connection = new MariaDBConnectionParameters(json);
                    connectionPool = new ConnectionPool(connection, ConnectionPool.CONNECTION_TYPE_MARIADB);
                    break;
                case MYSQL_TYPE:
                    connection = new MySQLConnectionParameters(json);
                    connectionPool = new MySQLConnectionPool(connection);
                    break;
                case DERBY_TYPE:
                    connection = new DerbyConnectionParameters(json);
                    connectionPool = new ConnectionPool(connection, ConnectionPool.CONNECTION_TYPE_DEBRY);
                    break;
                case POSTGRES_TYPE:
                    connection = new PostgresConnectionParameters(json);
                    connectionPool = new ConnectionPool(connection, ConnectionPool.CONNECTION_TYPE_POSTGRES);
                    break;
                case DYNAMODB_TYPE:
                    break;
                default:
                    throw new IllegalArgumentException("unknown database type");
            }
            isConnected = true;
            return Boolean.TRUE;
        }

        List<String> doc = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (doc.isEmpty()) {
                doc.add(getName() + "(arg.) - creates a connection to the given database with the given connection information.");
                doc.add("This is");
                doc.add(USERNAME + " = the username");
                doc.add(PASSWORD + " = the password");
                doc.add(SCHEMA + " = the database schema");
                doc.add(DATABASE + " = the database name");
                doc.add(HOST + " = the host where this lives");
                doc.add(PORT + " = the port");
                doc.add(PARAMETERS + " = (optional) extra connection parameters");
                doc.add(USE_SSL + " = use ssl. Make sure your database is properly configured for SSL first.");
                doc.add(BOOT_PASSWORD + " = the boot password (Derby only)");
                doc.add(StorageConfigurationTags.DERBY_STORE_TYPE_MEMORY + " = in memory only (Derby only)");
                doc.add(StorageConfigurationTags.DERBY_STORE_TYPE_FILE + " = file only (Derby only)");
                doc.add(StorageConfigurationTags.DERBY_STORE_TYPE_SERVER + " = server only (Derby only)");
                doc.add(TYPE_ARG + " = the type. One of " + MYSQL_TYPE + ", " + MARIADB_TYPE + ", " + POSTGRES_TYPE + " or " + DERBY_TYPE);

            }
            return doc;
        }
    }


    public static final String QUERY_COMMAND = "read";

    public class Read implements QDLFunction {
        @Override
        public String getName() {
            return QUERY_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            // This provides
            // #1 a statement. If prepared, then
            // #1 List of values
            String rawStatement = (String) objects[0];
            List args = null;
            if (objects.length == 2) {
                if (objects[1] instanceof QDLStem) {
                    QDLStem stemVariable = (QDLStem) objects[1];
                    if (stemVariable.isList()) {
                        //args = stemVariable.getQDLList().toJSON();
                        args = stemVariable.getQDLList();
                    } else {
                        throw new IllegalArgumentException(QUERY_COMMAND + " requires its second argument, if present to be a list");
                    }
                } else {
                    throw new IllegalArgumentException(QUERY_COMMAND + " requires its second argument, if present to be a list");
                }
            }
            /*
      db#read('select @ from oauth2.clients where ?<create_ts AND ?<last_accessed', [10000,10000])

             */
            // Args are list of form
            /*
                [a0,a1,...]
                where a's are either simple types - long, big decimal, string, boolean or null
                or are an explicit record
                [value, type]
                In which case the type will be asserted (and the value may be changed too).
                E.g.
                ['foo',[12223,DATE],['3dgb3ty24fgf',BINARY]]
                would assert the first is a string, convert the second into a date and the 3rd is assumed
                to be base 64 encoded and would be decoded and asserted as a byte[]
             */
            QDLStem outStem = new QDLStem();

            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            try {
                PreparedStatement stmt = c.prepareStatement(rawStatement);
                if (args != null) {
                    int i = 1;
                    for (Object entry : args) {
                        setParam(stmt, i++, entry);
                    }
                }
                stmt.executeQuery();
                ResultSet rs = stmt.getResultSet();
                // Now we have to pull in all the values.
                while (rs.next()) {
                    ColumnMap map = rsToMap(rs);
                    QDLStem currentEntry = new QDLStem();
                    for (String key : map.keySet()) {
                        if (map.get(key) != null) {
                            currentEntry.put(key, sqlConvert(map.get(key)));
                        }
                    }
                    outStem.getQDLList().add(currentEntry);
                }
                // Have to convert this map since it is col name + Java object.
                rs.close();
                stmt.close();
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw e;
            }
            return outStem;
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList<>();
            switch (argCount) {
                case 1:
                    doc.add(getName() + "(statement) - execute a query ");
                    break;
                case 2:
                    doc.add(getName() + "(statement,arg_list) - execute a prepared query");
                    break;
            }
            doc.add("A query is  a select, query, count or anything else that");
            doc.add("has  a result. The statement may be simply a statement or it may be a prepared statement.");
            doc.add("If it is prepared, then arg_list is a list of either scalars or pairs of the form [value, type]");
            doc.add("where type is one of the types in the variable " + TYPE_VAR_NAME);
            doc.add("E.g.");
            doc.add("db#query('select * from my_table where user_id=?',['2355',types.SMALLINT]);");
            doc.add("Note that the types are specific to the table structure of the database! ");
            doc.add("If you do not supply them then the default will be ");
            doc.add("int -> BIGINT");
            doc.add("decimal -> NUMERIC");
            doc.add("string -> STRING");
            doc.add("boolean -> BOOLEAN");
            doc.add("stem, set -> STRING (as input form)");
            doc.add("null -> LONGVARCHAR as a  NULL");
            return doc;
        }
    }

    private void setParam(PreparedStatement stmt, int i, Object entry) throws SQLException {
        Object value;
        int type = Integer.MIN_VALUE;
        if (entry instanceof JSONArray) {
            JSONArray array = (JSONArray) entry;
            value = array.get(0);
            type = array.getInt(1);
        } else {
            value = entry;
        }
        if (type == Integer.MIN_VALUE) {
            if (value instanceof String) {
                stmt.setString(i, (String) value);
                return;
            }
            if (value instanceof Long) {
                stmt.setObject(i, value, BIGINT);
                return;
            }
            // Since the entry comes from a trip through JSON
            if (value instanceof Integer) {
                stmt.setObject(i, value, BIGINT);
                return;
            }
            if (value instanceof BigDecimal) {
                stmt.setObject(i, value, NUMERIC);
                return;
            }
            if (value instanceof Boolean) {
                stmt.setBoolean(i, (Boolean) value);
                return;
            }
            if ((value instanceof QDLStem) || (value instanceof QDLSet)) {
                stmt.setString(i, InputFormUtil.inputForm(value));
                return;
            }
            if (value instanceof QDLNull) {
                stmt.setNull(NULL, LONGVARCHAR);
                return;
            }

            throw new IllegalArgumentException("unknown argument type for " + value + " of type  " + value.getClass().getCanonicalName());
        }
        stmt.setObject(i, value, type);
    }

    /**
     * Converts a generic object to a QDL object.
     *
     * @param obj
     * @return
     */
    protected Object sqlConvert(Object obj) {
        if (Constant.getType(obj) != Constant.UNKNOWN_TYPE) {
            return obj; // nixx to do
        }
        if (obj instanceof Integer) {
            return new Long((Integer) obj);
        }
        if (obj instanceof byte[]) {
            return Base64.encodeBase64URLSafeString((byte[]) obj);
        }
        if ((obj instanceof Date)) {
            Date date = (Date) obj;
            return Iso8601.date2String(date.getTime());
        }
        if (obj instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) obj;
            return Iso8601.date2String(timestamp.getTime());
        }

        if ((obj instanceof Double) || (obj instanceof Float)) {
            return new BigDecimal(obj.toString());
        }
        throw new IllegalArgumentException("unknown SQLtype for " + obj.getClass().getCanonicalName());
    }

    public void releaseConnection(ConnectionRecord c) {
        c.setLastAccessed(System.currentTimeMillis());
        connectionPool.push(c);
    }


    protected void destroyConnection(ConnectionRecord c) {
        try {
            connectionPool.doDestroy(c);
            DebugUtil.trace(this, "after destroyConnection for " + c + ", " + connectionPool);
        } catch (PoolException x) {
            throw new PoolException("pool failed to destroy connection", x);
        }
    }

    public static String UPDATE_COMMAND = "update";

    public class Update implements QDLFunction {
        @Override
        public String getName() {
            return UPDATE_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            String rawStatement = (String) objects[0];
            List args = null;
            if (objects.length == 2) {
                if (objects[1] instanceof QDLStem) {
                    QDLStem stemVariable = (QDLStem) objects[1];
                    if (stemVariable.isList()) {
                        args = stemVariable.getQDLList().toJSON();
                    } else {
                        throw new IllegalArgumentException(QUERY_COMMAND + " requires its second argument, if present to be a list");
                    }
                } else {
                    throw new IllegalArgumentException(QUERY_COMMAND + " requires its second argument, if present to be a list");
                }
            }

            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            try {
                PreparedStatement stmt = c.prepareStatement(rawStatement);
                if (args != null) {
                    int i = 1;
                    for (Object entry : args) {
                        setParam(stmt, i++, entry);
                    }
                }
                stmt.executeUpdate();
                // Nothing is ever returned from an update
                stmt.close();
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
            return Boolean.TRUE;
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList();
            switch (argCount) {
                case 1:
                    doc.add(getName() + "(statement) - update an existing row or table in an SQL database");
                    break;
                case 2:
                    doc.add(getName() + "(statement,args) - update an existing row or table in an SQL database using a prepared statement");
                    break;
            }
            if (argCount == 2) {
                doc.addAll(getArgStatement());
            }
            return doc;
        }
    }

    public static String EXECUTE_COMMAND = "execute";

    public class Execute implements QDLFunction {
        @Override
        public String getName() {
            return EXECUTE_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            return doSQLExecute(objects, getName());
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList<>();
            switch (argCount) {
                case 1:
                    doc.add(getName() + "(statement) - executes a statement with no return value");
                    break;
                case 2:
                    doc.add(getName() + "(statement,arg_list) - executes a prepared statement that has no return values.");
                    break;
            }
            doc.add("This is used for inserts and deletes in particular.");
            if (argCount == 2) {
                doc.addAll(getArgStatement());
            }
            return doc;
        }
    }


    public static String TYPE_VAR_NAME = "sql_types.";

    public class SQLTypes implements QDLVariable {
        @Override
        public String getName() {
            return TYPE_VAR_NAME;
        }

        QDLStem types = null;

        @Override
        public Object getValue() {
            if (types == null) {
                types = new QDLStem();
                types.put("VARCHAR", (long) VARCHAR);
                types.put("CHAR", (long) CHAR);
                types.put("LONGVARCHAR", (long) LONGVARCHAR);
                types.put("BIT", (long) BIT);
                types.put("NUMERIC", (long) NUMERIC);
                types.put("TINYINT", (long) TINYINT);
                types.put("SMALLINT", (long) SMALLINT);
                types.put("INTEGER", (long) INTEGER);
                types.put("BIGINT", (long) BIGINT);
                types.put("REAL", (long) REAL);
                types.put("FLOAT", (long) FLOAT);
                types.put("DOUBLE", (long) DOUBLE);
                types.put("VARBINARY", (long) VARBINARY);
                types.put("BINARY", (long) BINARY);
                types.put("DATE", (long) DATE);
                types.put("TIME", (long) TIME);
                types.put("TIMESTAMP", (long) TIMESTAMP);
                types.put("CLOB", (long) CLOB);
                types.put("BLOB", (long) BLOB);
                types.put("ARRAY", (long) ARRAY);
                types.put("REF", (long) REF);
                types.put("STRUCT", (long) STRUCT);
                types.put("SQLXML", (long) SQLXML);
            }
            return types;
        }


    }

    /**
     * Used for both create and delete.
     *
     * @param objects
     * @param name
     * @return
     */
    public Object doSQLExecute(Object[] objects, String name) {
        if (!isConnected) {
            throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
        }
        String rawStatement = (String) objects[0];
        List args = null;
        if (objects.length == 2) {
            if (objects[1] instanceof QDLStem) {
                QDLStem stemVariable = (QDLStem) objects[1];
                if (stemVariable.isList()) {
                    args = stemVariable.getQDLList().toJSON(); // converts to a list of more or less standard Java values.
                } else {
                    throw new IllegalArgumentException(name + " requires its second argument, if present to be a list");
                }
            } else {
                throw new IllegalArgumentException(name + " requires its second argument, if present to be a list");
            }
        }

        ConnectionRecord connectionRecord = connectionPool.pop();
        Connection c = connectionRecord.connection;
        try {
            PreparedStatement stmt = c.prepareStatement(rawStatement);
            if (args != null) {
                int i = 1;
                for (Object entry : args) {
                    setParam(stmt, i++, entry);
                }
            }
            stmt.execute();
            stmt.close();
            releaseConnection(connectionRecord);
        } catch (SQLException e) {
            destroyConnection(connectionRecord);
            throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
        }
        return Boolean.TRUE;
    }

    List<String> argStatement = new ArrayList<>();

    protected List<String> getArgStatement() {
        if (argStatement.isEmpty()) {
            argStatement.add("The argument list is used for prepared statements and is of the form");
            argStatement.add(" [a0,a1,...]\n" +
                    "where a's are either simple types - long, big decimal, string, boolean or null\n" +
                    "or are an explicit record\n" +
                    "   [value, sql_type]\n" +
                    "In which case the type will be asserted (and the value may be changed too).\n" +
                    "E.g.\n" +
                    "   ['foo',[12223," + TYPE_VAR_NAME + "DATE],['3dgb3ty24fgf'," + TYPE_VAR_NAME + "BINARY]]\n" +
                    "would assert the first is a string, convert the second into a date and the 3rd is assumed\n" +
                    "to be base 64 encoded and would be decoded and asserted as a byte array\n");
            argStatement.add("If QDL is given a complex type (stem, set) it will,");
            argStatement.add("convert it to " + SystemEvaluator.INPUT_FORM + " and pass along the value as a string.");
        }
        return argStatement;
    }

    @Override
    public JSONObject serializeToJSON() {
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {

    }
    public static String BATCH = "batch";
    public class BatchExecute implements QDLFunction{
        @Override
        public String getName() {
            return BATCH;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }
        // Fixes https://github.com/ncsa/qdl/issues/69
        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if(!(objects[0] instanceof String)){
                throw new IllegalArgumentException("the first argument to " + getName() + " must be a string");
            }
            if(!Constant.isStem(objects[1])){
                throw new IllegalArgumentException("the second argument to " + getName() + " must be a stem");
            }
            QDLStem stemVariable = (QDLStem) objects[1];
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            HashMap returnCodes = new HashMap();
            int counter = 0;
            try {
                PreparedStatement stmt = c.prepareStatement((String)objects[0]);
                for(Object key : stemVariable.keySet()){
                    Object value = stemVariable.get(key);
                    if(!(value instanceof QDLStem)){
                        throw new IllegalArgumentException("the element with index '" + key + " must be a list");
                    }
                    QDLStem arg = (QDLStem) value;
                    if(!arg.isList()){
                        throw new IllegalArgumentException("the element with index '" + key + " must be a list");
                    }
                    QDLList list = arg.getQDLList();
                    int i = 1;
                    for (Object entry : list) {
                        setParam(stmt, i++, entry);
                    }
                    stmt.addBatch();
                    returnCodes.put(counter++, key);

                }
                long[] rcs = stmt.executeLargeBatch();
                QDLStem outStem = new QDLStem();
                for(int i = 0; i < rcs.length; i++){
                   outStem.putLongOrString(returnCodes.get(i), rcs[i]);
                }
                stmt.close();
                releaseConnection(connectionRecord);
                return outStem;
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
        }
/*
    ConnectionRecord cr = getConnection();
        Connection c = cr.connection;
        try {
            PreparedStatement pStmt = c.prepareStatement(sql);
            for (Identifier id : idMap.keySet()) {
                pStmt.setLong(1, idMap.get(id));
                pStmt.setString(2, id.toString());
                pStmt.setLong(3, idMap.get(id));
                pStmt.addBatch();
                if (DEEP_DEBUG) {
                    System.out.println("MonitoredSQLStore: updating id=" + id + ", access time=" + idMap.get(id));
                }
            }
            int[] affectedRecords = pStmt.executeBatch();
 */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> documentation = new ArrayList<>();
documentation.add(getName() + "(statement, value_list.) - execute a statement with multiple values. ");
documentation.add("This will take a prepared statement and a stem of values, each of whose");
documentation.add("entries is a list for the prepared statement." );
documentation.add("it may be used for INSERT, DELETE or UPDATE.");
documentation.add("It is logically equivalent to loop through statements and execute them,");
documentation.add("but most SQL databases optimize such a batch execution and for very large");
documentation.add("datasets, the performance difference can be quite dramatic.");
documentation.add("\nThis returns a conformable stem each opf whose values is a non-zero integer");
documentation.add("indicating the number of records in the database changed by this statment, or");
documentation.add("a negative integer where");
documentation.add(Statement.SUCCESS_NO_INFO + " = the operation worked, but no other information is available");
documentation.add(Statement.EXECUTE_FAILED + " = the statement failed, but processing continued.");
documentation.add("\n");
documentation.add("E.g.");
documentation.add("Let us use the prepared statement");
documentation.add("stmt := UPDATE my_table set accessed=? where id=? AND (access IS NULL or create_ts<?)");
documentation.add("\nand we have a large list of values. Each element of the list is a list");
documentation.add("whose values are used in the prepared statement");
documentation.add("v.:=[[date_ms(), '7D5EF', date_ms()-2419200000], [date_ms(),'C46AB',date_ms()-2419200000]]");
documentation.add("\n(just 2 for this example, but it could be thousands). You issue");
documentation.add("\nrc. := "+getName() + "(stmt, v.)");
            return documentation;
        }
    }

    /* Handy dandy table of SQL types and calls.
    SQL 	        JDBC/Java 	            setXXX 	        updateXXX
    VARCHAR 	    java.lang.String 	    setString 	    updateString
    CHAR 	        java.lang.String 	    setString 	    updateString
    LONGVARCHAR 	java.lang.String 	    setString 	    updateString
    BIT 	        boolean 	            setBoolean 	    updateBoolean
    NUMERIC 	    java.math.BigDecimal 	setBigDecimal 	updateBigDecimal
    TINYINT 	    byte 	                setByte         updateByte
    SMALLINT 	    short 	                setShort        updateShort
    INTEGER 	    int 	                setInt 	        updateInt
    BIGINT 	        long 	                setLong         updateLong
    REAL 	        float 	                setFloat        updateFloat
    FLOAT 	        float 	                setFloat 	    updateFloat
    DOUBLE 	        double 	                setDouble 	    updateDouble
    VARBINARY    	byte[ ] 	            setBytes 	    updateBytes
    BINARY 	        byte[ ] 	            setBytes 	    updateBytes
    DATE  	        java.sql.Date 	        setDate 	    updateDate
    TIME 	        java.sql.Time 	        setTime 	    updateTime
    TIMESTAMP    	java.sql.Timestamp 	    setTimestamp 	updateTimestamp
    CLOB 	        java.sql.Clob 	        setClob 	    updateClob
    BLOB 	        java.sql.Blob 	        setBlob 	    updateBlob
    ARRAY 	        java.sql.Array 	        setARRAY 	    updateARRAY
    REF 	        java.sql.Ref 	        SetRef 	        updateRef
    STRUCT 	        java.sql.Struct     	SetStruc
     */
}
