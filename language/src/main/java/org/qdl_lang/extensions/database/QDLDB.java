package org.qdl_lang.extensions.database;

import edu.uiuc.ncsa.security.core.util.StringUtils;
import org.qdl_lang.evaluate.SystemEvaluator;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;

import static edu.uiuc.ncsa.security.storage.sql.ConnectionPoolProvider.*;
import static edu.uiuc.ncsa.security.storage.sql.SQLDatabase.rsToMap;
import static java.sql.Types.*;
import static org.qdl_lang.variables.StemUtility.put;
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
            PreparedRead preparedRead = new PreparedRead();
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
                    QDLValue output = preparedRead.evaluate(oArgs, state);
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

    public static final String QUERY_COMMAND = "query";

    public class PreparedRead implements QDLFunction {
        @Override
        public String getName() {
            return QUERY_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            // This provides
            // #1 a statement. If prepared, then
            // #2 List of values for prepared statement (may be empty)
            // #3 (optional) qdl type map to turn, e.g.longs into dates or blobs into strings.
            String rawStatement = qdlValues[0].asString();
            List<QDLValue> args = null;
            QDLStem qdlTypes = null;
            switch (qdlValues.length) {
                case 1:
                    qdlTypes = new QDLStem();
                    break;
                case 2:
                case 3:
                    if (qdlValues[1].isStem()) {
                        QDLStem stemVariable = qdlValues[1].asStem();
                        if (stemVariable.isList()) {
                            //args = stemVariable.getQDLList().toJSON();
                            args = new ArrayList<>(stemVariable.getQDLList().size());
                            args.addAll(stemVariable.getQDLList());
                        } else {
                            throw new BadArgException(QUERY_COMMAND + " requires its second argument, if present to be a list", 1);
                        }
                    }
                    if (qdlValues.length == 3) {
                        if (qdlValues[2].isStem()) {
                            qdlTypes = qdlValues[2].asStem();
                        } else {
                            throw new BadArgException(QUERY_COMMAND + " requires its last argument, if present to be a stem", 2);
                        }
                    } else {
                        qdlTypes = new QDLStem();
                    }
                    break;
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
                outStem = processResultSet(stmt, qdlTypes, state);
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
                    doc.add(getName() + "(statement) - execute a query. The statement must be executable as is.");
                    doc.add("statement - an SQL statement that returns a result set.");
                    break;
                case 2:
                    doc.add(getName() + "(statement,arg_list.) - execute a prepared query");
                    doc.add("statement - an SQL statement that returns a result set.");
                    doc.add("arg_list. = a list of parameters that will be used in order to prepare the statement.");
                            doc.add("It may be empty");
                    break;
                case 3:
                    doc.add(getName() + "(statement,arg_list., qdl_types.) - execute a prepared query with qdl data types specified.");
                    doc.add(getName() + "(statement,arg_list.) - execute a prepared query");
                    doc.add("statement - an SQL statement that returns a result set.");
                    doc.add("arg_list. = a list of parameters that will be used in order to prepare the statement.");
                    doc.add("It may be empty");
                    doc.add("qdl_types. - a stem keyed by column name that has the type in QDL to convert the SQL entry to.");

                    break;
            }
            doc.add("A query is  a select, query, count or anything else that");
            doc.add("has  a result. The statement may be simply a statement or it may be a prepared statement.");
            doc.add("If it is prepared, then arg_list is a list of either scalars or pairs of the form [value, type]");
            doc.add("where type is one of the values in the variable " + SQL_TYPES_VAR_NAME);
            doc.add("E.g.");
            doc.add("db#"+getName() +"(select * from my_table where user_id=?',['user_2355']);");
            doc.add("Note that the values are specific to the table structure of the database! ");
            doc.add("If you do not supply them then the default will be ");
            doc.add("if you specify the qdl_type for a column, then the returned SQL value will");
            doc.add("converted to that type. This lets you, e.g., read a JSON object that is stored");
            doc.add("as a string and turn it into a stem. Updating the object should include this, so the ");
            doc.add("conversion back to the correct SQL type is done.");
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
    private QDLStem processResultSet(PreparedStatement stmt, QDLStem qdlTypes, State state) throws SQLException {
        QDLStem outStem = new QDLStem();
        ResultSet rs = stmt.getResultSet();
        // Now we have to pull in all the values.
        while (rs.next()) {
            ColumnMap map = rsToMap(rs);
            QDLStem currentEntry = new QDLStem();
            for (String key : map.keySet()) {
                int qdlType = qdlTypes.containsKey(key) ? qdlTypes.getLong(key).intValue() : QDL_TYPE_NONE;
                if (map.get(key) != null) {
                    currentEntry.put(QDLKey.from(key),
                            asQDLValue(sqlToQDL(map.get(key), qdlType, state)));
                }
            }
            outStem.getQDLList().add(asQDLValue(currentEntry));
        }
        rs.close();

        return outStem;
    }

    /**
     * Sets a value in a QDL stem to the correct SQL type based on the QDL type.
     * Very limited.
     *
     * @param stmt
     * @param i
     * @param entry
     * @throws SQLException
     */
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
     * For the case that the SQL type of the column is known.
     *
     * @param stmt
     * @param i
     * @param sqlType
     * @param entry
     * @throws SQLException
     */
    /* Conversion chart
    SQL                         Java
    ---------------------------+----------------------
    BIT(1), BOOLEAN             java.lang.Boolean
    CHAR, VARCHAR, TEXT, JSON	java.lang.String
    TINYINT, SMALLINT	        java.lang.Short
    MEDIUMINT, INTEGER, INT	    java.lang.Integer
    BIGINT	                    java.lang.Long
    FLOAT	                    java.lang.Float
    REAL, DOUBLE	            java.lang.Double
    NUMERIC, DECIMAL	        java.math.BigDecimal
    DATE	                    java.sql.Date (or java.time.LocalDate)
    TIME	                    java.sql.Time (or java.time.Duration)
    DATETIME, TIMESTAMP	        java.sql.Timestamp (or java.time.LocalDateTime)
    BLOB	                    java.sql.Blob or byte[]

db := j_load('db');
        cfg. := interpret(file_read('/home/ncsa/dev/csd/config/db-connector.qdl'));
        db#connect(cfg.);
c. ≔ db#p_read('select * from oauth2.db_test',[],{'blob_0':4, 'timestamp_long':3}).0;
c.'varchar_128' ≔ 'id-' + random_string(4);
c.'timestamp_long'≔ date_iso();
db#create('oauth2.db_test',c.,{'blob_0':4, 'timestamp_long':3});

     */
    private void setParam(PreparedStatement stmt,
                          int i,
                          int sqlType,
                          int qdlType,
                          QDLValue entry) throws SQLException, ParseException {
        Object value;
        switch (entry.getType()) {
            case QDLValue.STEM_TYPE:
                checkArg(entry.isStem(), "incompatible value -- must be a stem");
                value = entry.asStem().toJSON().toString();
                break;
            case QDLValue.SET_TYPE:
                checkArg(entry.isSet(), "incompatible value -- must be a set");
                value = entry.asSet().inputForm();
                break;
            case QDLValue.LIST_TYPE:
                checkArg(entry.isList(), "incompatible value -- must be a list");
                value = entry.asStem().toJSON().toString();
                break;
            case QDLValue.NULL_TYPE:
                stmt.setNull(i, sqlType); // any may be null/
                return;
            default:
                value = entry.getValue();
        }
        switch (sqlType) {
            case Types.BIT:
            case Types.BOOLEAN:
                checkArg(entry.isBoolean(), "incompatible value -- must be a boolean");
                stmt.setBoolean(i, (Boolean) value);
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
                checkArg(entry.isLong(), "incompatible value -- must be an integer, got a " + entry.getValue().getClass().getSimpleName());
                stmt.setShort(i, ((Long) value).shortValue());
                break;
            case Types.INTEGER:
                checkArg(entry.isLong(), "incompatible value -- must be an integer");
                stmt.setInt(i, ((Long) value).intValue());
                break;
            case BIGINT:
                if (entry.getType() == Constant.STRING_TYPE) {
                    // it's an ISO 8601 string, but the SQL column type is BigInt
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    stmt.setLong(i, calendar.getTimeInMillis());
                    return;
                }
                checkArg(entry.isLong(), "incompatible value -- must be an integer");
                stmt.setLong(i, entry.asLong());
                break;
            case Types.NUMERIC:
            case Types.DECIMAL:
                checkArg(entry.isDecimal(), "incompatible value -- must be a decimal");
                stmt.setBigDecimal(i, (BigDecimal) value);
                break;
            case Types.VARCHAR:
            case LONGVARCHAR:
            case Types.CHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case Types.NCLOB:
                switch (qdlType) {
                    case QDL_TYPE_NONE:
                        break;
                    case QDL_TYPE_JSON:
                        if (entry.isStem()) {
                            value = entry.asStem().toJSON().toString();
                        }
                        if (entry.isSet()) {
                            value = entry.asSet().toJSON().toString();
                        }
                        break;
                    default:
                    case QDL_TYPE_INPUT_FORM:
                        value = entry.getInputForm();
                        break;
                    case QDL_TYPE_DATE:
                        // Assumption is that the user wants an ISO 8601 date as a
                        // string in the database, and the stem value is a long.
                        if (entry.isLong()) {
                            value = Iso8601.date2String(entry.asLong());
                        }
                        break;
                    case QDL_TYPE_STRING:
                        value = entry.asString();
                        break;
                }
                checkArg(value instanceof String, "incompatible value -- must be a string");
                stmt.setString(i, (String) value);
                break;
            case FLOAT:
            case REAL: // Same as Float
                checkArg(entry.isDecimal() || entry.isLong(), "incompatible value -- must be a decimal");
                Float f = Float.valueOf(value.toString()); // safest way
                stmt.setFloat(i, f);
                break;
            case Types.DOUBLE:
                checkArg(entry.isDecimal() || entry.isLong(), "incompatible value -- must be a decimal");
                Double d = Double.valueOf(value.toString()); // safest wayu
                stmt.setDouble(i, d);
                break;
            case Types.DATE:
                if (entry.isString()) {
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    java.sql.Date sqlDate = new java.sql.Date(calendar.getTimeInMillis());
                    stmt.setDate(i, sqlDate);
                } else {
                    if (entry.isLong()) {
                        java.sql.Date sqlDate = new java.sql.Date(entry.asLong());
                        stmt.setDate(i, sqlDate);
                    } else {
                        throw new IllegalArgumentException("incompatible value -- date must be an ISO string or integer, not a " + value.getClass().getSimpleName());
                    }
                }
                break;
            case Types.TIME:
                // An SQL time is of the form hh:mm:ss and does not correspond to anything easily.
                // The contract with JDBC is to have java.sql.Time thinly wrap a Date object and
                // set year, month and day to 0, then ignore them. So it is up to the programmer
                // to manage that if they have to. Messy but that's SQL...
                if (entry.isString()) {
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    java.sql.Time sqlTime = new java.sql.Time(calendar.getTimeInMillis());
                    stmt.setTime(i, sqlTime);
                    break;
                } else {
                    if (entry.isLong()) {
                        stmt.setTime(i, new java.sql.Time(entry.asLong()));
                    } else {
                        throw new IllegalArgumentException("incompatible value -- date must be an ISO string or integer, not a " + value.getClass().getSimpleName());
                    }
                }
                break;
            case Types.TIMESTAMP:
                if (entry.isString()) {
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    java.sql.Timestamp sqlTime = new java.sql.Timestamp(calendar.getTimeInMillis());
                    stmt.setTimestamp(i, sqlTime);
                    break;
                } else {
                    if (entry.isLong()) {
                        stmt.setTimestamp(i, new java.sql.Timestamp(entry.asLong()));
                    } else {
                        throw new IllegalArgumentException("incompatible value -- date must be an ISO string or integer, not a " + value.getClass().getSimpleName());
                    }
                }
                break;
            case BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
                //    System.out.println("setParam:" + new String(Base64.decodeBase64(entry.asString())));
                ByteArrayInputStream bais;
                switch (qdlType) {
                    case QDL_TYPE_NONE:
                        bais = new ByteArrayInputStream(Base64.decodeBase64(entry.asString()));
                        break;
                    default:

                    case QDL_TYPE_JSON:
                        checkArg(entry.isStem() || entry.isSet(), "incompatible value -- must be a stem or set");
                        switch (entry.getType()) {
                            case QDLValue.STEM_TYPE:
                                bais = new ByteArrayInputStream(entry.asStem().toJSON().toString().getBytes(StandardCharsets.UTF_8));
                                break;
                            case QDLValue.SET_TYPE:
                                bais = new ByteArrayInputStream(entry.asSet().toJSON().toString().getBytes(StandardCharsets.UTF_8));
                                break;
                            default:
                                throw new IllegalArgumentException("incompatible value -- input form must be a stem or set");
                        }
                        break;
                    case QDL_TYPE_INPUT_FORM:
                        checkArg(entry.isStem() || entry.isSet(), "incompatible value -- must be a stem or set");
                        switch (entry.getType()) {
                            case QDLValue.STEM_TYPE:
                                bais = new ByteArrayInputStream(entry.asStem().inputForm().getBytes(StandardCharsets.UTF_8));
                                break;
                            case QDLValue.SET_TYPE:
                                bais = new ByteArrayInputStream(entry.asSet().inputForm().getBytes(StandardCharsets.UTF_8));
                                break;
                            default:
                                throw new IllegalArgumentException("incompatible value -- input form must be a stem or set");
                        }
                        break;
                    case QDL_TYPE_DATE:
                        throw new IllegalArgumentException("incompatible value -- BLOB must be a " + value.getClass().getSimpleName());
                    case QDL_TYPE_STRING: // store as a string
                        checkArg(entry.isString(), "incompatible value -- BLOB must be a string");
                        bais = new ByteArrayInputStream(entry.asString().getBytes(StandardCharsets.UTF_8));
                        break;

                }
                stmt.setBlob(i, bais);
                break;
            default:
                throw new IllegalArgumentException("incompatible value -- unrecognized SQL type  " + sqlType + " for QDL value of type " + value.getClass().getSimpleName());
        }
    }
/*
db := j_load('db');
        cfg. := interpret(file_read('/home/ncsa/dev/csd/config/db-connector.qdl'));
        db#connect(cfg.);
c. ≔ db#p_read('select * from oauth2.db_test',[],{'blob_0':4, 'timestamp_long':3}).0;
c.'varchar_128' ≔ 'id-333';
a.'varchar_128'≔'id_'+random_string(4);
b. ≔ db#p_read('select * from oauth2.db_test where varchar_128=\'id_AOGa\'', [],{'blob_0':4});
 db#insert('oauth2.db_test',c.,{'blob_0':4, 'timestamp_long':3});
 */

    /**
     * If the condition is true, do nothing, if false, throw and exception
     * with the given message
     *
     * @param condition
     * @param message
     */
    protected void checkArg(boolean condition, String message) {
        if (condition) return;
        throw new IllegalArgumentException(message);
    }

    /**
     * Converts a generic object to a QDL object. If it is a known supported type, that is just used otherwise
     * only basic conversions are done.
     *
     * @param obj
     * @param qdlType The QDL type of the object to convert to, for in the qdl_types. stem.
     * @return
     */
    protected QDLValue sqlToQDL(Object obj, int qdlType, State state) throws SQLException {
        int varType = Constant.getType(obj); // The QDL var_type
        switch (varType) {
            case Constant.UNKNOWN_TYPE:
                break;
            case Constant.BOOLEAN_TYPE:
            case Constant.DECIMAL_TYPE:
                return QDLValue.asQDLValue(obj);
            case Constant.LONG_TYPE:
                if (qdlType == QDL_TYPE_DATE) { // explicit request to change long to a date
                    return QDLValue.asQDLValue(Iso8601.date2String((long) obj));
                }
                return QDLValue.asQDLValue(obj);

        }
        if (obj == null) {
            return QDLValue.getNullValue();
        }
        if (obj instanceof Integer ||
                obj instanceof Short || obj instanceof Byte) {
            return asQDLValue(obj);
        }

        if ((obj instanceof Date)) {
            Date date = (Date) obj;
            return asQDLValue(Iso8601.date2String(date.getTime()));
        }
        if (obj instanceof Timestamp) {
            Timestamp timestamp = (Timestamp) obj;
            return asQDLValue(Iso8601.date2String(timestamp.getTime()));
        }
        if (obj instanceof Time) {
            Time time = (Time) obj;
            return asQDLValue(Iso8601.date2String(time.getTime()));
        }
        if (obj instanceof java.sql.Date) {
            java.sql.Date date = (java.sql.Date) obj;
            return asQDLValue(Iso8601.date2String(date.getTime()));
        }

        if ((obj instanceof Double) || (obj instanceof Float)) {
            return asQDLValue(new BigDecimal(obj.toString()));
        }

        if (obj instanceof byte[]) {
            //System.out.println("x:" + new String(Base64.decodeBase64((byte[]) obj)));
            //System.out.println("y:" + new String((byte[]) obj));
             if(qdlType != QDL_TYPE_NONE) {
                 return sqlStringToQDLValue(new String((byte[])obj),qdlType,state);
             }
            // in this case, we are getting back an array of bytes, not a decoded string.
            return asQDLValue(Base64.encodeBase64URLSafeString((byte[]) obj));
        }
        if (obj instanceof Blob) {
            Blob blob = (Blob) obj;
            byte[] b = blob.getBytes(1L, (int) blob.length());
                if(qdlType != QDL_TYPE_NONE) {
                return sqlStringToQDLValue(new String(b),qdlType,state);
            }
/*
            if (qdlType == QDL_TYPE_STRING) {
                return asQDLValue(new String(Base64.decodeBase64(b)));
            }
*/
            return asQDLValue(Base64.encodeBase64URLSafeString(b));
        }
        // finally! Since this is overlaoded for things like dates, can't check until everything else
        // has been checked.
        if (varType == Constant.STRING_TYPE) {
            if(qdlType != QDL_TYPE_NONE) {
                return sqlStringToQDLValue((String)obj,qdlType,state);
            }
            return QDLValue.asQDLValue(obj.toString());
        }
        throw new IllegalArgumentException("unknown SQLtype for " + obj.getClass().getCanonicalName());
    }

    String dummyVar = "ξΞξΞξΞξ";
    protected QDLValue sqlStringToQDLValue(String string, int qdlType, State state){

        switch (qdlType) {
            case QDL_TYPE_STRING:
                return asQDLValue(string);
            case QDL_TYPE_JSON:
                QDLStem stem = new QDLStem();
                try {
                    JSONObject jsonObject = JSONObject.fromObject(string);
                    stem.fromJSON(jsonObject);
                    return asQDLValue(stem);
                } catch (Throwable throwable) {

                    JSONArray jsonArray = JSONArray.fromObject(string);
                    stem.fromJSON(jsonArray);
                    return asQDLValue(stem);
                }
            case QDL_TYPE_INPUT_FORM:
                String x = dummyVar + ":=" + string;
                Polyad polyad = new Polyad(SystemEvaluator.INTERPRET);
                polyad.addArgument(new ConstantNode(asQDLValue(x)));
                state.getMetaEvaluator().evaluate(polyad, state);
                QDLValue v = asQDLValue(state.getValue(dummyVar));
                state.remove(dummyVar);
                return v;
        }
        throw new IllegalArgumentException("unknown SQLtype for QDLtype=" + qdlType);
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


    public static String CREATE_COMMAND = "create";

    public class Create implements QDLFunction {
        @Override
        public String getName() {
            return CREATE_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            String tablename = qdlValues[0].asString();
            QDLStem arg = qdlValues[1].asStem();
            QDLStem map = null;
            if (qdlValues.length > 2) {
                map = qdlValues[2].asStem();
            } else {
                map = new QDLStem();
            }
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            long rowsUpdates = 0;
            try {
                TreeMap<String, Integer> columnNames = getSQLMetadata(c, tablename);
                PreparedStatement pstmt = createInsertStatement(connectionRecord.connection, tablename,
                        columnNames, arg, map);
                rowsUpdates = pstmt.executeUpdate();
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
            return QDLValue.asQDLValue(rowsUpdates);
        }

        /*


        db := j_load('db');
        cfg. := interpret(file_read('/home/ncsa/dev/csd/config/db-connector.qdl'));
        db#connect(cfg.);
        db#insert('oauth2.clients', [;5])
         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            switch (argCount) {
                case 2:
                    docs.add(getName() + "(table, arg.) - insert the stem into the database as a new row.");
                    docs.add("table = the (fully-qualified) table name to construct the insert statement.");

                    docs.add("arg. = the col-value pairs to use for the update.");
                    docs.add(" This uses all standard implicit conversions.");
                    break;
                case 3:
                    docs.add(getName() + "(table, arg., conversions.) - insert the stem into the database as a new row.");
                    docs.add("table = the (fully-qualified) table name to construct the insert statement.");
                    ;
                    docs.add("arg. = the col-value pairs to use for the update.");
                    addConversionMapBlurb(docs);
                    break;
            }
            return docs;
        }
    }

    public static String GET_TABLE_METADATA = "get_table_metadata";
    public static String MD_TABLENAME="table_name";
    public static String MD_CATALOG="catalog";
    public static String MD_SCHEMA="schema";
    public static String MD_PRIMARY_KEY="primary_key";
    public static String MD_AUTO_COMMIT="auto_commit";
    public static String MD_COLUMNS="columns";
    public class GetTableMetadata implements QDLFunction {
        @Override
        public String getName() {
            return GET_TABLE_METADATA;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override

        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }

            String tablename = qdlValues[0].asString();
            String dudQuery = "select * from " + tablename + " where 1 < 0";
            // It is possible that the tablename is ffully qualified with the schema, e.g.
            // oauth2.clients. To get primary keys we need exactly the unqualified table name
            String unqTableName = tablename.substring(tablename.lastIndexOf('.') + 1);
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            QDLStem outStem = new QDLStem();

            try{
            Statement stmt = c.createStatement();
            ResultSet res = stmt.executeQuery(dudQuery);
            ResultSetMetaData rsmd = res.getMetaData();
            nonTrivialPut(outStem, MD_CATALOG,c.getCatalog());
            nonTrivialPut(outStem, MD_SCHEMA,c.getSchema());
            nonTrivialPut(outStem, MD_AUTO_COMMIT,c.getAutoCommit());
            ResultSet pkRS = c.getMetaData().getPrimaryKeys(c.getCatalog(),c.getSchema(),unqTableName);
            QDLStem primaryKeyList = new QDLStem();
            while (pkRS.next()) {
                primaryKeyList.getQDLList().add(QDLValue.asQDLValue( pkRS.getString("COLUMN_NAME")));
            }
            pkRS.close();
            nonTrivialPut(outStem, MD_PRIMARY_KEY,primaryKeyList);
            QDLStem columns = new QDLStem();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                int type = rsmd.getColumnType(i);
                columns.put(QDLKey.from(rsmd.getColumnName(i)), QDLValue.asQDLValue(type));
            }

            stmt.close();
            outStem.put(QDLKey.from(MD_COLUMNS),QDLValue.asQDLValue(columns));
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw e;
            }

            return QDLValue.asQDLValue(outStem);
        }

        protected void nonTrivialPut(QDLStem stem, Object  key, Object value){
            if(value == null){return;}
            if(value instanceof String){
                if(StringUtils.isTrivial((String) value)){
                    return;
                }
            }
            stem.put(QDLKey.from(key), QDLValue.asQDLValue(value));

        }
        @Override
        public List<String> getDocumentation(int argCount) {
            return List.of();
        }
    }

    /**
     * Given tablename get just the columns names and their types.
     * @param c
     * @param tablename
     * @return
     * @throws SQLException
     */
    protected TreeMap<String, Integer> getSQLMetadata(Connection c, String tablename) throws SQLException {
        String dudQuery = "select * from " + tablename + " where 1 < 0";
        // It is possible that the tablename is ffully qualified with the schema, e.g.
        // oauth2.clients. To get primary keys we need exactly the unqualified table name
        Statement stmt = c.createStatement();
        ResultSet res = stmt.executeQuery(dudQuery);
        ResultSetMetaData rsmd = res.getMetaData();
        TreeMap<String, Integer> columnNames = new TreeMap<>();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            int type = rsmd.getColumnType(i);
            columnNames.put(rsmd.getColumnName(i), type);
        }

        stmt.close();
        return columnNames;
    }

    /**
     * Create the prepared statement. This requires creating the SQL statement and managing
     * all the column names and types.
     *
     * @param conn
     * @param tablename
     * @param columnNames
     * @param arg
     * @param map
     * @return
     * @throws SQLException
     */
    PreparedStatement createInsertStatement(Connection conn,
                                            String tablename,
                                            TreeMap<String, Integer> columnNames,
                                            QDLStem arg,
                                            QDLStem map) throws SQLException, ParseException {
        String insertStatement = "insert into " + tablename + " (";
        String values = " values (";
        boolean isFirst = true;
        List<String> actualColumns = new ArrayList<>();
        //Now just run through the column names and see what was sent,
        for (String column : columnNames.keySet()) {
            if (arg.containsKey(column)) {
                actualColumns.add(column);
                insertStatement = insertStatement + (isFirst ? "" : ",") + column;
                values = values + (isFirst ? "" : ",") + "?";
                if (isFirst) {
                    isFirst = false;
                }
            }
        }
        insertStatement = insertStatement + ")" + values + ")";
        //  System.out.println("DBModule createInsertStatement:" + insertStatement);
        PreparedStatement pstmt = conn.prepareStatement(insertStatement);
        int i = 1; // SQL columns start at 1
        for (String columnName : actualColumns) {
            int qdlType = map.containsKey(columnName) ? map.getLong(columnName).intValue() : QDL_TYPE_NONE;
            setParam(pstmt, i++, columnNames.get(columnName), qdlType, arg.get(columnName));
        }
        return pstmt;
    }

    PreparedStatement createUpdateStatement(Connection conn,
                                            String tablename,
                                            TreeMap<String, Integer> columnNames,
                                            QDLStem arg,
                                            QDLStem map) throws SQLException, ParseException {
        String updateStatement = "update " + tablename + " set ";
        boolean isFirst = true;
        List<String> actualColumns = new ArrayList<>();
        //Now just run through the column names and see what was sent,
        for (String column : columnNames.keySet()) {
            if (arg.containsKey(column)) {
                actualColumns.add(column);
                updateStatement = updateStatement + (isFirst ? "" : ", ") + column + "=?";
                if (isFirst) {
                    isFirst = false;
                }
            }
        }
        //  System.out.println("DBModule createUpdateStatement:" + updateStatement);
        PreparedStatement pstmt = conn.prepareStatement(updateStatement);
        int i = 1; // SQL columns start at 1
        for (String columnName : actualColumns) {
            int qdlType = map.containsKey(columnName) ? map.getLong(columnName).intValue() : QDL_TYPE_NONE;
            setParam(pstmt, i++, columnNames.get(columnName), qdlType, arg.get(columnName));
        }
        return pstmt;

    }

    protected void addConversionMapBlurb(List<String> docs) {
        docs.add("conversions. = a stem of code to use for conversions from values in the stem to SQL.");
        docs.add("This will use the data type as found in " + DATA_TYPES_STEM_NAME + " to convert between");
        docs.add("QDL and SQL.");
        docs.add("Example.");
        docs.add("If an SQL store has a string representation of a JSON object, then on read");
        docs.add("the string will be converted to a stem., On update or insert, an embdded stem");
        docs.add("will be converted to its JSON representation then stored as a string.");
    }

    public static String DATA_TYPES_STEM_NAME = "$$DATA_TYPE.";

    public class StoreType implements QDLVariable {
        QDLStem storeTypes = null;

        @Override
        public String getName() {
            return DATA_TYPES_STEM_NAME;
        }

        @Override
        public Object getValue() {
            return getDataTypes();
        }
    }

    /*
    We use these in case statements and longs as cae values are not supporte
    in Java. So have the fiddle with them here.
     */
    public static final int QDL_TYPE_NONE = -1; //default
    public static final int QDL_TYPE_JSON = 1;
    public static final int QDL_TYPE_INPUT_FORM = 2;
    public static final int QDL_TYPE_DATE = 3;
    public static final int QDL_TYPE_STRING = 4;

    public QDLStem getDataTypes() {
        if (types == null) {
            types = new QDLStem();
            put(types, "json", (long) QDL_TYPE_JSON);
            put(types, "input_form", (long) QDL_TYPE_INPUT_FORM);
            put(types, "date", (long) QDL_TYPE_DATE);
            put(types, "string", (long) QDL_TYPE_STRING);
        }
        return types;
    }

    QDLStem types = null;

    public static String UPDATE_COMMAND = "update";

    public class Update implements QDLFunction {
        @Override
        public String getName() {
            return UPDATE_COMMAND;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!isConnected) {
                throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
            }
            String tablename = qdlValues[0].asString();
            QDLStem arg = qdlValues[1].asStem();
            QDLStem qdlTypes = null;
            if (qdlValues.length > 2) {
                qdlTypes = qdlValues[2].asStem();
            } else {
                qdlTypes = new QDLStem();
            }
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            long rowsUpdates = 0;
            try {
                TreeMap<String, Integer> columnNames = getSQLMetadata(c, tablename);
                PreparedStatement pstmt = createUpdateStatement(connectionRecord.connection, tablename,
                        columnNames, arg, qdlTypes);
                rowsUpdates = pstmt.executeUpdate();
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
            return QDLValue.asQDLValue(rowsUpdates);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            switch (argCount) {
                case 2:
                    docs.add(getName() + "(tablename, arg.)");
                    docs.add("tablename = name of the table to update.");
                    docs.add("     arg. = the QDL stem that will be used tp update");
                    break;
                case 3:
                    docs.add(getName() + "(tablename, arg., qdl_types.)");
                    docs.add(" tablename = name of the table to update.");
                    docs.add("      arg. = the QDL stem that will be used tp update");
                    docs.add("qdl_types. = a stem of qdl types keyed by column name that governs how QDL values are mapped");
                    docs.add("             to SQL values. ");
                    break;
            }
            return docs;
        }
    }

    public static String PREPARED_UPDATE_COMMAND = "p_update";

    public class PreparedUpdate implements QDLFunction {
        @Override
        public String getName() {
            return PREPARED_UPDATE_COMMAND;
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
                    doc.add(getName() + "(statement) - update an existing row or table in an SQL database,");
                    doc.add("without preparation. The statement must be wholly executable as is.");
                    break;
                case 2:
                    doc.add(getName() + "(statement, args.) - update an existing row or table in an SQL database using a prepared statement");
                    break;
            }
            if (argCount == 2) {
                doc.addAll(getPreparedArgStatement());
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
            return doSQLExecute(qdlValues, getName(), state);
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
            doc.add("The most general possible command. This will run/execute any SQL statement");
            doc.add("This is useful for inserts and deletes in particular.");
            if (argCount == 2) {
                doc.addAll(getPreparedArgStatement());
            }
            return doc;
        }
    }


    public static String QDL_TYPES_VAR_NAME = "qdl_types.";

    public class QDLTypes implements QDLVariable {
        @Override
        public String getName() {
            return QDL_TYPES_VAR_NAME;
        }

        QDLStem qdlTypes = null;

        @Override
        public Object getValue() {
            if (qdlTypes == null) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("json", (long) QDL_TYPE_JSON);
                map.put("input_form", (long) QDL_TYPE_INPUT_FORM);
                map.put("date", (long) QDL_TYPE_DATE);
                map.put("string", (long) QDL_TYPE_STRING);
                qdlTypes = new QDLStem();
                StemUtility.setStemValue(qdlTypes, map);
            }
            return qdlTypes;
        }
    }

    public static String SQL_TYPES_VAR_NAME = "sql_types.";

    public class SQLTypes implements QDLVariable {
        @Override
        public String getName() {
            return SQL_TYPES_VAR_NAME;
        }

        QDLStem sqlTypes = null;

        @Override
        public Object getValue() {
            if (sqlTypes == null) {
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
                sqlTypes = new QDLStem();
                StemUtility.setStemValue(sqlTypes, map);
            }
            return sqlTypes;
        }


    }

    /**
     * Used for both create and delete.
     *
     * @param qdlValues
     * @param name
     * @return
     */
    public QDLValue doSQLExecute(QDLValue[] qdlValues, String name, State state) {
        if (!isConnected) {
            throw new IllegalStateException("No database connection. Please run " + CONNECT_COMMAND + " first.");
        }
        if (!qdlValues[0].isString()) {
            throw new IllegalArgumentException("First argument must be a string.");
        }
        String rawStatement = qdlValues[0].asString();
        List<QDLValue> args = null;
        if (qdlValues.length == 2) {
            if (qdlValues[1].isStem()) {
                QDLStem stemVariable = qdlValues[1].asStem();
                if (stemVariable.isList()) {
                    args = stemVariable.getQDLList().getArrayList(); // converts to a list of more or less standard Java values.
                } else {
                    throw new BadArgException(name + " requires its second argument, if present to be a list", 1);
                }
            } else {
                args = new ArrayList();
                args.add(qdlValues[1]);
            }
        }
        QDLStem qdlTypes = null;
        if (qdlValues.length == 3) {
            if (!qdlValues[2].isStem()) {
                throw new BadArgException("The third argument must be a stem", 2);
            }
            qdlTypes = qdlValues[2].asStem();
        } else {
            qdlTypes = new QDLStem();
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
                outStem = processResultSet(stmt, new QDLStem(), state); // map not allowed in this call.
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

    protected List<String> getPreparedArgStatement() {
        if (argStatement.isEmpty()) {
            argStatement.add("The argument list is used for prepared statements and is of the form");
            argStatement.add(" [a0,a1,...]\n" +
                    "where a's are either simple values - long, big decimal, string, boolean or null\n" +
                    "or are an explicit record\n" +
                    "   [value, sql_type]\n" +
                    "In which case the type will be asserted (and the value may be changed too).\n" +
                    "E.g.\n" +
                    "   ['foo',[12223," + SQL_TYPES_VAR_NAME + "DATE],['3dgb3ty24fgf'," + SQL_TYPES_VAR_NAME + "BINARY]]\n" +
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
            QDLStem qdlTypes = null;
            if (qdlValues.length == 3) {
                if (!qdlValues[2].isStem()) {
                    throw new BadArgException("the second argument to " + getName() + " must be a stem", 2);

                }
                qdlTypes = qdlValues[2].asStem();
                if (!qdlTypes.isList()) {
                    throw new BadArgException("the second argument to " + getName() + " must be a list", 2);
                }
            } else {
                qdlTypes = new QDLStem();
                qdlTypes.setDefaultValue(QDL_TYPE_NONE);
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
                    QDLList<? extends QDLValue> list;
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
                        //setParam(stmt, i, qdlTypes.get(i - 1).asLong().intValue(), entry);
                        setParam(stmt, i, entry);
                        i++;
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
            documentation.add(getName() + "(statement, values.) - execute a statement with multiple values. ");
            documentation.add("statement - the (prepared) statement to execute");
            documentation.add("values. - a stem or list of lists wit the elements for the prepared statement");
            documentation.add("This will take a prepared statement and a stem of values, each of whose");
            documentation.add("entries is a list or scalar (converted to a list with one element)");
            documentation.add(" for the prepared statement. It may be used for INSERT, DELETE or UPDATE.");
            documentation.add("See " + BATCH_QUERY_COMMAND + " for batch reads");
            documentation.add("This is logically equivalent to loop through statements and execute them,");
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
