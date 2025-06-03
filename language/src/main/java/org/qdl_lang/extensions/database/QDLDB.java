package org.qdl_lang.extensions.database;

import com.google.protobuf.NullValue;
import org.qdl_lang.evaluate.SystemEvaluator;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.state.State;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.variables.*;
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
import org.qdl_lang.variables.values.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider.*;
import static edu.uiuc.ncsa.security.storage.sql.SQLDatabase.rsToMap;
import static java.sql.Types.*;
import static org.qdl_lang.variables.values.BooleanValue.*;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/5/22 at  7:11 AM
 */
/*
   A test file is in ~/dev/csd/config/test/db-test.qdl that has connection information and
   samples for using this class.
 */
public class QDLDB implements QDLMetaModule {
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its argument", 0);
            }
            QDLStem stemVariable = qdlValues[0].asStem();
            if (!stemVariable.containsKey(TYPE_ARG)) {
                throw new BadArgException("missing " + TYPE_ARG + " argument", 0);
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
                    throw new BadArgException("unknown database type", 0);
            }
            isConnected = true;
            return True;
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


    public static final String BATCH_QUERY_COMMAND = "batch_read";

    public class BatchRead implements QDLFunction {
        @Override
        public String getName() {
            return BATCH_QUERY_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!(qdlValues[0].isString())) {
                throw new BadArgException(getName() + " requires a string as its first argument", 0);
            }
            if (!(qdlValues[1].isStem())) {
                throw new BadArgException(getName() + " requires a stem as its second argument", 1);
            }
            QDLStem inStem = qdlValues[1].asStem();
            QDLStem outStem = new QDLStem();
            boolean flattenList = false;
            if (qdlValues.length == 3) {
                if (qdlValues[2].isBoolean()) {
                    flattenList = qdlValues[2].asBoolean();
                } else {
                    throw new BadArgException(getName() + " requires a boolean as its third argument", 2);
                }
            }
            Read read = new Read();
            for (QDLKey key : inStem.keySet()) {
                try {
                    QDLValue rawArg = inStem.get(key);
                    QDLStem stemArg;
                    if (rawArg.isStem()) {
                        stemArg = rawArg.asStem();
                    } else {
                        // its a scalar, so convert to a list
                        stemArg = new QDLStem();
                        stemArg.put(0, asQDLValue(rawArg));
                    }
                    QDLValue[] oArgs = new QDLValue[]{qdlValues[0], asQDLValue(stemArg)};
                    QDLValue output = read.evaluate(oArgs, state);
                    if (flattenList && (output.isStem())) {
                        QDLStem flattenStem = output.asStem();
                        if (flattenStem.size() == 1) {
                            outStem.put(key, flattenStem.get(LongValue.Zero));
                        } else {
                            outStem.put(key, output);
                        }

                    } else {

                        outStem.put(key, output);
                    }
                } catch (Throwable t) {
                    if (state.isDebugOn()) {
                        t.printStackTrace();
                    }
                    outStem.put(key, QDLNullValue.getNullValue());
                }
            }
            return asQDLValue(outStem);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            switch (argCount) {
                case 2:
                    docs.add(getName() + "(prepared_statement, batch_values.) - do multiple reads as a batch");
                    break;
                case 3:
                    docs.add(getName() + "(prepared_statement, batch_values., flatten) - do multiple reads as a batch. If there is a single result, do not wrap in a list");
                    docs.add("Since any read returns a list of results, setting flatten to true will take ");
                    docs.add("a list that has a single element and use that. This permits you to have simpler output.");
                    break;
            }
            docs.add("This is like " + QUERY_COMMAND + ", except that the batch_values. will be used");
            docs.add("to construct multiple queries and execute them.");
            docs.add("The result is a conformable stem to batch_values, with the key holding");
            docs.add("the result (which is in general a list of results!) or a NULL if the operation failed.");
            docs.add("\n");
            docs.add("If the batch_values. are scalars, the ");
            docs.add("/nE.g. Passing in a generic stem\n");
            docs.add("   stmt := 'select client_id, creation_ts from oauth2.clients where client_id=?'");
            docs.add("   args.:={'zero':['oa4mp:/client/234234'], 'one':['oa4mp:/client_id/5667']}");
            docs.add("   db#batch_read(stmt, args.);\n");
            docs.add("{\n" +
                    " zero : [{creation_ts:2023-05-19T05:00:00.000Z,client_id:oa4mp:/client/234234}],\n" +
                    "  one : [{creation_ts:2024-03-21T05:00:00.000Z,client_id:oa4mp:/client_id/5667}]\n" +
                    "}");
            docs.add("This shows that there was one result for each query.");
            docs.add("\nE.g. Using a list of scalars\n");
            docs.add("   args1.:=['oa4mp:/client/234234','oa4mp:/client_id/5667']");
            docs.add("   db#batch_read(stmt, args1.)");
            docs.add("\nSame as the above results, but as a list since args1 is a list.");
            docs.add("\nE.g.with flatten\n");
            docs.add("   db#batch_read(stmt, args1., true)");
            docs.add("[\n" +
                    "{creation_ts:2023-05-19T05:00:00.000Z,client_id:oa4mp:/client/234234},\n" +
                    "{creation_ts:2024-03-21T05:00:00.000Z,client_id:oa4mp:/client_id/5667}\n" +
                    "]");
            return docs;
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            // This provides
            // #1 a statement. If prepared, then
            // #1 List of values
            String rawStatement = qdlValues[0].asString();
            List<QDLValue> args = null;
            if (qdlValues.length == 2) {
                if (qdlValues[1].isStem()) {
                    QDLStem stemVariable = qdlValues[1].asStem();
                    if (stemVariable.isList()) {
                        //args = stemVariable.getQDLList().toJSON();
                        args = stemVariable.getQDLList();
                    } else {
                        throw new BadArgException(QUERY_COMMAND + " requires its second argument, if present to be a list", 1);
                    }
                } else { // if a scalar, float it to a list.
                    args = new ArrayList();
                    args.add(qdlValues[1]);
                }
            }
            /*
      db#read('select @ from oauth2.clients where ?<create_ts AND ?<last_accessed', [10000,10000])

             */
            // Args are list of form
            /*
                [a0,a1,...]
                where a's are either simple values - long, big decimal, string, boolean or null
                or are an explicit record
                [value, type]
                In which case the type will be asserted (and the value may be changed too).
                E.g.
                ['foo',[12223,DATE],['3dgb3ty24fgf',BINARY]]
                would assert the first is a string, convert the second into a date and the 3rd is assumed
                to be base 64 encoded and would be decoded and asserted as a byte[]
             */

            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            QDLStem outStem;
            try {
                PreparedStatement stmt = c.prepareStatement(rawStatement);
                if (args != null) {
                    int i = 1;
                    for (QDLValue entry : args) {
                        setParam(stmt, i++, entry.getValue());
                    }
                }
                stmt.executeQuery();
                outStem = processResultSet(stmt);
                stmt.close();
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw e;
            }
            return asQDLValue(outStem);
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
            doc.add("where type is one of the values in the variable " + TYPE_VAR_NAME);
            doc.add("E.g.");
            doc.add("db#query('select * from my_table where user_id=?',['2355',values.SMALLINT]);");
            doc.add("Note that the values are specific to the table structure of the database! ");
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

    /**
     * <b>IF</b> there is a result set, then invoke this. It will convert the entire result set
     * to a QDL stem. Only invoke if you know there is a result set!
     * <br/><br/>
     * <b>Note</b> this closes the result set.
     *
     * @param stmt
     * @return
     * @throws SQLException
     */
    private QDLStem processResultSet(PreparedStatement stmt) throws SQLException {
        QDLStem outStem = new QDLStem();
        ResultSet rs = stmt.getResultSet();
        // Now we have to pull in all the values.
        while (rs.next()) {
            ColumnMap map = rsToMap(rs);
            QDLStem currentEntry = new QDLStem();
            for (String key : map.keySet()) {
                if (map.get(key) != null) {
                    currentEntry.put(key, asQDLValue(sqlConvert(map.get(key))));
                }
            }
            outStem.getQDLList().add(asQDLValue(currentEntry));
        }
        rs.close();

        return outStem;
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
            return Long.valueOf((Integer) obj);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            String rawStatement = qdlValues[0].asString();
            List<QDLValue> args = null;
            if (qdlValues.length == 2) {
                if (qdlValues[1].isStem()) {
                    QDLStem stemVariable = qdlValues[1].asStem();
                    if (stemVariable.isList()) {
                        args = stemVariable.getQDLList().toJSON();
                    } else {
                        throw new BadArgException(QUERY_COMMAND + " requires its second argument, if present to be a list", 1);
                    }
                } else {
                    args = new ArrayList();
                    args.add(qdlValues[1]);
                }
            }

            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            Long updateCount = 0L;
            try {
                PreparedStatement stmt = c.prepareStatement(rawStatement);
                if (args != null) {
                    int i = 1;
                    for (QDLValue entry : args) {
                        setParam(stmt, i++, entry.getValue());
                    }
                }
                updateCount = (long) stmt.executeUpdate();
                stmt.close();
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
            return asQDLValue(updateCount);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            return doSQLExecute(qdlValues, getName());
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doc = new ArrayList<>();
            switch (argCount) {
                case 1:
                    doc.add(getName() + "(statement) - executes a statement with or without no return value");
                    break;
                case 2:
                    doc.add(getName() + "(statement,arg_list) - executes a prepared statement with or without returned values.");
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
                HashMap<String, Object> map = new HashMap<>();
                map.put("VARCHAR", (long) VARCHAR);
                map.put("CHAR", (long) CHAR);
                map.put("LONGVARCHAR", (long) LONGVARCHAR);
                map.put("BIT", (long) BIT);
                map.put("NUMERIC", (long) NUMERIC);
                map.put("TINYINT", (long) TINYINT);
                map.put("SMALLINT", (long) SMALLINT);
                map.put("INTEGER", (long) INTEGER);
                map.put("BIGINT", (long) BIGINT);
                map.put("REAL", (long) REAL);
                map.put("FLOAT", (long) FLOAT);
                map.put("DOUBLE", (long) DOUBLE);
                map.put("VARBINARY", (long) VARBINARY);
                map.put("BINARY", (long) BINARY);
                map.put("DATE", (long) DATE);
                map.put("TIME", (long) TIME);
                map.put("TIMESTAMP", (long) TIMESTAMP);
                map.put("CLOB", (long) CLOB);
                map.put("BLOB", (long) BLOB);
                map.put("ARRAY", (long) ARRAY);
                map.put("REF", (long) REF);
                map.put("STRUCT", (long) STRUCT);
                map.put("SQLXML", (long) SQLXML);
                types = new QDLStem();
                StemUtility.setStemValue(types, map);
            }
            return types;
        }


    }

    /**
     * Used for both create and delete.
     *
     * @param qdlValues
     * @param name
     * @return
     */
    public QDLValue doSQLExecute(QDLValue[] qdlValues, String name) {
        if (!isConnected) {
            throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
        }
        if(!qdlValues[0].isString()){
            throw new IllegalArgumentException("First argument must be a string.");
        }
        String rawStatement = qdlValues[0].asString();
        List<QDLValue> args = null;
        if (qdlValues.length == 2) {
            if (qdlValues[1].isStem()) {
                QDLStem stemVariable = qdlValues[1].asStem();
                if (stemVariable.isList()) {
                    args = stemVariable.getQDLList().toJSON(); // converts to a list of more or less standard Java values.
                } else {
                    throw new BadArgException(name + " requires its second argument, if present to be a list", 1);
                }
            } else {
                args = new ArrayList();
                args.add(qdlValues[1]);
            }
        }

        ConnectionRecord connectionRecord = connectionPool.pop();
        Connection c = connectionRecord.connection;
        boolean gotResult = false;
        QDLStem outStem = null;
        Long updateCount = 0L;
        try {
            PreparedStatement stmt = c.prepareStatement(rawStatement);
            if (args != null) {
                int i = 1;
                for (QDLValue entry : args) {
                    setParam(stmt, i++, entry.getValue());
                }
            }
            gotResult = stmt.execute();
            if (gotResult) {
                // Fix https://github.com/ncsa/qdl/issues/80
                outStem = processResultSet(stmt);
            } else {
                updateCount = stmt.getLargeUpdateCount();
            }
            stmt.close();
            releaseConnection(connectionRecord);
        } catch (SQLException e) {
            destroyConnection(connectionRecord);
            throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
        }
        if (gotResult) {
            return asQDLValue(outStem);
        }
        return asQDLValue(updateCount);
    }

    List<String> argStatement = new ArrayList<>();

    protected List<String> getArgStatement() {
        if (argStatement.isEmpty()) {
            argStatement.add("The argument list is used for prepared statements and is of the form");
            argStatement.add(" [a0,a1,...]\n" +
                    "where a's are either simple values - long, big decimal, string, boolean or null\n" +
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

    public static String BATCH = "batch_execute";

    public class BatchExecute implements QDLFunction {
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!(qdlValues[0].isString())) {
                throw new BadArgException("the first argument to " + getName() + " must be a string", 0);
            }
            if (!qdlValues[1].isStem()) {
                throw new BadArgException("the second argument to " + getName() + " must be a stem", 1);
            }
            QDLStem stemVariable = qdlValues[1].asStem();
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            HashMap returnCodes = new HashMap();
            int counter = 0;
            try {
                PreparedStatement stmt = c.prepareStatement(qdlValues[0].asString());
                for (QDLKey key : stemVariable.keySet()) {
                    QDLValue value = stemVariable.get(key);
                    QDLList list;
                    if (value.isStem()) {
                        QDLStem arg = value.asStem();
                        if (!arg.isList()) {
                            throw new IllegalArgumentException("the element with index '" + key + " must be a list");
                        }
                        list = arg.getQDLList();
                    } else {
                        list = new QDLList();
                        list.add(asQDLValue(value));
                    }

                    int i = 1;
                    for (QDLValue entry : list) {
                        setParam(stmt, i++, entry);
                    }
                    stmt.addBatch();
                    returnCodes.put(counter++, key.getValue());
                }
                long[] rcs = stmt.executeLargeBatch();
                QDLStem outStem = new QDLStem();
                for (int i = 0; i < rcs.length; i++) {
                    outStem.put(QDLKey.from(returnCodes.get(i)), rcs[i]);
                }
                stmt.close();
                releaseConnection(connectionRecord);
                return asQDLValue(outStem);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> documentation = new ArrayList<>();
            documentation.add(getName() + "(statement, value_list.) - execute a statement with multiple values. ");
            documentation.add("This will take a prepared statement and a stem of values, each of whose");
            documentation.add("entries is a list or scalar (converted to a list with one element)");
            documentation.add(" for the prepared statement. It may be used for INSERT, DELETE or UPDATE.");
            documentation.add("See " + BATCH_QUERY_COMMAND + " for batch reads");
            documentation.add("It is logically equivalent to loop through statements and execute them,");
            documentation.add("but most SQL databases optimize such a batch execution and for very large");
            documentation.add("datasets, the performance difference can be quite dramatic.");
            documentation.add("Moreover, drivers that talk to databases need make a single call with this");
            documentation.add("method, which saves resources and is much more efficient.");
            documentation.add("\nThis returns a conformable stem each of whose values is a non-zero integer");
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
            documentation.add("\n(just 2 for this basic, but it could be thousands). You issue");
            documentation.add("\nrc. := " + getName() + "(stmt, v.)");
            documentation.add("\nE.g. mass delete");
            documentation.add("This will do a mass delete by a unique id. It uses the fact that");
            documentation.add("the function accepts a list of scalars if there is a single parameter.");
            documentation.add("   stmt = 'DELETE from my_table WHERE id = ?");
            documentation.add("   ids.:=['ADC745B','B6434F','C984E875',...];");
            documentation.add("   " + getName() + "(stmt, ids.);");
            documentation.add("[1," + Statement.SUCCESS_NO_INFO + ",1,...]");

            return documentation;
        }
    }

    /* Handy dandy table of SQL values and calls.
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
