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

import static edu.uiuc.ncsa.security.core.util.StringUtils.justify;
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
            QDLStem args = qdlValues[1].asStem();
            QDLStem outStem = new QDLStem();
            boolean flattenList = false;
            if (qdlValues.length == 3) {
                if (qdlValues[2].isBoolean()) {
                    flattenList = qdlValues[2].asBoolean();
                } else {
                    throw new BadArgException(getName() + " requires a boolean as its third argument", 2);
                }
            }
            /*
            Nota Bene: JDBC does not support batch more for select statements and will throw
                       a BatchUpdateException. It is intended to insert, update and delete statements.
                       Therefore, we deledate multiple selects to the module function.
             */
            PreparedRead preparedRead = new PreparedRead();
            for (QDLKey key : args.keySet()) {
                try {
                    QDLValue rawArg = args.get(key);
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

    public static final String QUERY_COMMAND = "read";

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

            boolean hasQDLTypes = qdlValues.length > 2;
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
                    if (hasQDLTypes) {
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
                    Map<Long, int[]> qdlTypeMap = creatQDLTypeMap(qdlTypes);
                    int i = 1;
                    for (QDLValue entry : args) {
                        if (hasQDLTypes && qdlTypeMap.containsKey((long) i - 1)) {
                            int[] q = qdlTypeMap.get((long) i - 1);
                            setParam(stmt, i, q[0], q[1], entry);
                        } else {
                            setParam(stmt, i, entry.getValue());
                        }
                        i++;
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
            doc.add("db#" + getName() + "(select * from my_table where user_id=?',['user_2355']);");
            doc.add("Note that the values are specific to the table structure of the database! ");
            doc.add("If you do not supply them then the default will be ");
            doc.add("if you specify the qdl_type for a column, then the returned SQL value will");
            doc.add("converted to that type. This lets you, e.g., read a JSON object that is stored");
            doc.add("as a string and turn it into a stem. Updating the object should include this, so the ");
            doc.add("conversion back to the correct SQL type is done.");
            return doc;
        }
    }

    public static final String QDL_TO_SQL = "qdl_to_sql";

    public class QDLToSQL implements QDLFunction {
        @Override
        public String getName() {
            return QDL_TO_SQL;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            boolean isArgScalar = true;
            boolean isSQLTypeScalar = true;
            boolean isQDLTypeScalar = true;
            QDLStem value;    // arg 0
            QDLStem qdlTypes; // arg 1
            QDLStem sqlTypes; // arg 2
            switch (qdlValues[0].getType()) {
                case QDLValue.STEM_TYPE:
                    value = qdlValues[0].asStem();
                    break;
                default:
                    value = new QDLStem();
                    value.put(QDLKey.from(0L), qdlValues[0]);
                    isArgScalar = true;
            }
            if (qdlValues[1].isStem()) {
                qdlTypes = qdlValues[1].asStem();
                isQDLTypeScalar = false;
            } else {
                if (!qdlValues[1].isLong()) {
                    throw new BadArgException("qdl_type must be a long", 1);
                }
                qdlTypes = new QDLStem();
                qdlTypes.setDefaultValue(qdlValues[1]);
                //qdlTypes.put(QDLKey.from(0L), qdlValues[1]);
            }
            if (qdlValues[2].isStem()) {
                sqlTypes = qdlValues[2].asStem();
                isSQLTypeScalar = false;
            } else {
                if (!qdlValues[2].isLong()) {
                    throw new BadArgException("sql_type must be a long", 1);
                }
                sqlTypes = new QDLStem();
                sqlTypes.setDefaultValue(qdlValues[2]);
            }
            // special case
            if (isArgScalar && isQDLTypeScalar && isSQLTypeScalar) {
                Object out = qdlToSQL(qdlTypes.getLong(0L).intValue(),
                        sqlTypes.getLong(0L).intValue(),
                        qdlValues[0]);
                return QDLValue.asQDLValue(out);
            }
            QDLStem outStem = new QDLStem();
            for (QDLKey key : value.keySet()) {
                Long sql = sqlTypes.getLong(key);
                Long qdl = qdlTypes.getLong(key);
                outStem.put(key, qdlToSQL(qdl == null ? QDL_TYPE_DEFAULT : qdl.intValue(),
                        sql == null ? -15 : sql.intValue(),
                        value.get(key)));
            }
            if (isArgScalar) {
                return outStem.getQDLList().get(0L);
            }
            return QDLValue.asQDLValue(outStem);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            docs.add(getName() + "(value{.},  qdl_type{.}, sql_type{.}) - convert a QDL value to a SQL value");
            docs.add("   value - a QDL value to convert");
            docs.add("qdl_type - the QDL type to convert from");
            docs.add("sql_type - the SQL type to convert to");
            docs.add("Returns the (scalar) SQL value.");
            docs.add("Note that if value is a stem, then arbitrary keys are allowed but either sql and qdl type are scalaras, or the same keys must");
            docs.add("be used.");
            docs.add("This is used for prepared statements when you want to do the conversion directly,");
            docs.add("then pass it in. Complex SQL statements are allowed, so if it is quite complex (such as an updated with an embedded");
            docs.add("join), then it is better to do the conversion yourself and just pass it in. ");
            docs.add("Or, if the entries to your stem are very complex and require manual handling, this is a useful tool.");
            docs.add("Since this module supports arbitrar SQL statements, it may be in fact impossible to ");
            docs.add("interpret it without having an industrial strength SQL engine which would be very complex to ");
            docs.add("create and maintain, mostly negating the utility of this module.");
            return docs;
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
    private QDLStem processResultSet(PreparedStatement stmt, QDLStem qdlTypes, State state) throws Throwable {
        QDLStem outStem = new QDLStem();
        ResultSet rs = stmt.getResultSet();
        // Now we have to pull in all the values.
        while (rs.next()) {
            ColumnMap map = rsToMap(rs);
            QDLStem currentEntry = new QDLStem();
            for (String key : map.keySet()) {
                int qdlType = qdlTypes.containsKey(key) ? qdlTypes.getLong(key).intValue() : QDL_TYPE_DEFAULT;
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

MySQL
db := j_load('db');
        cfg. := interpret(file_read('/home/ncsa/dev/csd/config/mysql-connector.qdl'));
        db#connect(cfg.);

Derby
db := j_load('db');
        cfg. := interpret(file_read('/home/ncsa/dev/csd/config/derby-connector.qdl'));
        db#connect(cfg.);



c. ≔ db#p_read('select * from qdl_test.db_test where varchar_128=?',['id123'],{'blob_0':4, 'timestamp_long':3}).0;
c.'varchar_128' ≔ 'id-' + random_string(4);
c.'timestamp_long'≔ date_iso();
db#create('oauth2.db_test',c.,{'blob_0':4, 'timestamp_long':3});

     */
    private void setParam(PreparedStatement stmt,
                          int i,
                          int sqlType,
                          int qdlType,
                          QDLValue entry) throws SQLException, ParseException {
        Object obj = qdlToSQL(qdlType, sqlType, entry);
        if (sqlType == BLOB) {
            stmt.setBinaryStream(i, (ByteArrayInputStream) obj);
        } else {
            if (obj instanceof BigDecimal) {
                stmt.setBigDecimal(i, (BigDecimal) obj);
            } else {
                stmt.setObject(i, qdlToSQL(qdlType, sqlType, entry), sqlType);
            }
        }
    }

    protected Object qdlToSQL(int qdlType,
                              int sqlType,
                              QDLValue entry) throws SQLException, ParseException {
        Object value = entry.getValue();
        switch (sqlType) {
            case Types.BIT:
            case Types.BOOLEAN:
                checkArg(entry.isBoolean(), "incompatible value -- must be a boolean");
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
                checkArg(entry.isLong(), "incompatible value -- must be an integer, got a " + entry.getValue().getClass().getSimpleName());
                break;
            case Types.INTEGER:
                checkArg(entry.isLong(), "incompatible value -- must be an integer");
                break;
            case BIGINT:
                if (qdlType == QDL_TYPE_DATE) {
                    if (entry.getType() == Constant.STRING_TYPE) {
                        // it's an ISO 8601 string, but the SQL column type is BigInt
                        Calendar calendar = Iso8601.string2Date(entry.asString());
                        return calendar.getTimeInMillis();
                    }
                }
                checkArg(entry.isLong(), "incompatible value -- must be an integer");
                return entry.asLong();
            case Types.NUMERIC:
            case Types.DECIMAL:
                checkArg(entry.isDecimal(), "incompatible value -- must be a decimal");
                break;
            case Types.VARCHAR:
            case LONGVARCHAR:
            case Types.CHAR:
            case Types.CLOB:
            case Types.NCHAR:
            case NVARCHAR:
            case LONGNVARCHAR:
            case Types.NCLOB:
                switch (qdlType) {
                    case QDL_TYPE_DEFAULT:
                        break;
                    case QDL_TYPE_JSON:
                        if (entry.isStem()) {
                            value = entry.asStem().toJSON().toString();
                        }
                        break;
                    case QDL_TYPE_JSON_SET:
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
                break;
            case FLOAT:
            case REAL: // Same as Float
                checkArg(entry.isDecimal() || entry.isLong(), "incompatible value -- must be a decimal");
                value = Float.valueOf(value.toString()); // safest way
                break;
            case Types.DOUBLE:
                checkArg(entry.isDecimal() || entry.isLong(), "incompatible value -- must be a decimal");
                value = Double.valueOf(value.toString()); // safest wayu
                break;
            case Types.DATE:
                if (entry.isString()) {
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    value = new java.sql.Date(calendar.getTimeInMillis());
                } else {
                    if (entry.isLong()) {
                        value = new java.sql.Date(entry.asLong());
                    } else {
                        throw new IllegalArgumentException("incompatible value -- date must be an ISO string or integer, not a " + value.getClass().getSimpleName());
                    }
                }
                break;
            case Types.TIME:
            case TIME_WITH_TIMEZONE:
                // An SQL time is of the form hh:mm:ss and does not correspond to anything easily.
                // The contract with JDBC is to have java.sql.Time thinly wrap a Date object and
                // set year, month and day to 0, then ignore them. So it is up to the programmer
                // to manage that if they have to. Messy but that's SQL...
                if (entry.isString()) {
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    value = new java.sql.Time(calendar.getTimeInMillis());
                    break;
                } else {
                    if (entry.isLong()) {
                        value = new java.sql.Time(entry.asLong());
                    } else {
                        throw new IllegalArgumentException("incompatible value -- date must be an ISO string or integer, not a " + value.getClass().getSimpleName());
                    }
                }
                break;
            case Types.TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                if (entry.isString()) {
                    Calendar calendar = Iso8601.string2Date(entry.asString());
                    value = new java.sql.Timestamp(calendar.getTimeInMillis());
                    break;
                } else {
                    if (entry.isLong()) {
                        value = new java.sql.Timestamp(entry.asLong());
                    } else {
                        throw new IllegalArgumentException("incompatible value -- date must be an ISO string or integer, not a " + value.getClass().getSimpleName());
                    }
                }
                break;
            case BLOB:
            case Types.BINARY:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
                //    System.out.println("setParam:" + new String(Base64.decodeBase64(entry.asString())));
                ByteArrayInputStream bais;
                switch (qdlType) {
                    case QDL_TYPE_DEFAULT:
                        bais = new ByteArrayInputStream(Base64.decodeBase64(entry.asString()));
                        break;
                    default:
                    case QDL_TYPE_JSON_SET:
                        checkArg(entry.isSet(), "incompatible value -- must be a set");
                        bais = new ByteArrayInputStream(entry.asSet().toJSON().toString().getBytes(StandardCharsets.UTF_8));
                        break;

                    case QDL_TYPE_JSON:
                        checkArg(entry.isStem(), "incompatible value -- must be a stem");
                        switch (entry.getType()) {
                            case QDLValue.STEM_TYPE:
                                bais = new ByteArrayInputStream(entry.asStem().toJSON().toString().getBytes(StandardCharsets.UTF_8));
                                break;
                            case QDLValue.LIST_TYPE:
                                bais = new ByteArrayInputStream(entry.asStem().getQDLList().toJSON().toString().getBytes(StandardCharsets.UTF_8));
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
                value = bais;
                break;
            default:
                throw new IllegalArgumentException("incompatible value -- unrecognized SQL type  " + sqlType + " for QDL value of type " + value.getClass().getSimpleName());
        }
        return value;
    }
/*
db := j_load('db');
        cfg. := interpret(file_read('/home/ncsa/dev/csd/config/qdl-connector.qdl'));
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

    public static final String SQL_TO_QDL = "sql_to_qdl";

    public class SQLToQDL implements QDLFunction {
        @Override
        public String getName() {
            return SQL_TO_QDL;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            QDLValue v = sqlToQDL(qdlValues[0].getValue(), qdlValues[1].asLong().intValue(), state);
            return v;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            docs.add(getName() + "(sql_value, qdl_type) - convert a SQL value to a QDL value");
            docs.add("   sql_value - an SQL value to convert");
            docs.add("    qdl_type - the QDL type to convert to");
            docs.add("Returns the (scalar) QDL value. Note this is mostly informational so you can");
            docs.add("inspect the value and see what it is.");
            return docs;
        }
    }

    /**
     * Converts a generic object to a QDL object. If it is a known supported type, that is just used otherwise
     * only basic conversions are done.
     *
     * @param obj
     * @param qdlType The QDL type of the object to convert to, for in the qdl_types. stem.
     * @return
     */
    protected QDLValue sqlToQDL(Object obj, int qdlType, State state) throws Throwable {
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
            if (qdlType != QDL_TYPE_DEFAULT) {
                return sqlStringToQDLValue(new String((byte[]) obj), qdlType, state);
            }
            // in this case, we are getting back an array of bytes, not a decoded string.
            return asQDLValue(Base64.encodeBase64URLSafeString((byte[]) obj));
        }
        if (obj instanceof Blob) {
            Blob blob = (Blob) obj;
            byte[] b = blob.getBytes(1L, (int) blob.length());
            if (qdlType != QDL_TYPE_DEFAULT) {
                return sqlStringToQDLValue(new String(b), qdlType, state);
            }
            return asQDLValue(Base64.encodeBase64URLSafeString(b));
        }
        // finally! Since this is overlaoded for things like dates, can't check until everything else
        // has been checked.
        if (varType == Constant.STRING_TYPE) {
            return sqlStringToQDLValue(obj.toString(), qdlType, state);
        }
        throw new IllegalArgumentException("unknown SQL type for " + obj.getClass().getCanonicalName());
    }

    String dummyVar = "ξΞξΞξΞξ";
    String dummyStem = "ξΞξΞξΞξ.";

    protected QDLValue sqlStringToQDLValue(String string, int qdlType, State state) throws ParseException {

        switch (qdlType) {
            case QDL_TYPE_DEFAULT:
                return asQDLValue(string);
            case QDL_TYPE_DATE:
                Calendar c = Iso8601.string2Date(string);
                return QDLValue.asQDLValue(c.getTimeInMillis());
            case QDL_TYPE_STRING:
                return asQDLValue(string);
            case QDL_TYPE_JSON_SET:
                JSONArray jsonArray = JSONArray.fromObject(string);
                QDLSet set = new QDLSet();
                set.fromJSON(jsonArray);
                return asQDLValue(set);
            case QDL_TYPE_JSON:
                QDLStem stem = new QDLStem();
                try {
                    JSONObject jsonObject = JSONObject.fromObject(string);
                    stem.fromJSON(jsonObject);
                    return asQDLValue(stem);
                } catch (Throwable throwable) {

                    jsonArray = JSONArray.fromObject(string);
                    stem.fromJSON(jsonArray);
                    return asQDLValue(stem);
                }
            case QDL_TYPE_INPUT_FORM:
                //String x = dummyVar + ":=" + string;
                /*
                  Trickery! We need a handle to get this value back, hence we assign it to a variable.
                  Since we don't know if the value is a stem or scalar and QDL blows up if we get it wrong,
                  create a list with a single element and return that, then we just get the 0th element of that list.
                  Also, part of the contract is that this has been pickled to input_form by the system alread
                  so it is just the expression for a value, and not completely arbitrary QDL!
                 */
                String x = dummyStem + ":=" + "[" + string + "]";
                Polyad polyad = new Polyad(SystemEvaluator.INTERPRET);
                polyad.addArgument(new ConstantNode(asQDLValue(x)));
                state.getMetaEvaluator().evaluate(polyad, state);
                QDLValue v = asQDLValue(state.getValue(dummyStem));
                state.remove(dummyStem); // don't litter the symbol table!
                return v.asStem().get(0L);
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


    public static String CREATE_COMMAND = "qdl_create";

    public class QDLCreate implements QDLFunction {
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
            boolean forceCaseInsensitiveColumns = false;
            String tablename = "";
            QDLStem metadata = null;
            boolean hasMetadata = false;
            if (qdlValues[0].isString()) {
                tablename = qdlValues[0].asString();
            } else {
                if (!qdlValues[0].isStem()) {
                    throw new IllegalArgumentException("First argument must be a string or stem");
                }
                metadata = qdlValues[0].asStem();
                // Worst case is the FQ name is catalog.schema.tablename
                if (metadata.containsKey(MD_CATALOG)) {
                    tablename = tablename + metadata.get(MD_CATALOG).asString() + ".";
                }
                if (metadata.containsKey(MD_SCHEMA)) {
                    tablename = tablename + metadata.get(MD_SCHEMA).asString() + ".";
                }

                tablename = tablename + metadata.get(MD_TABLENAME).asString();
                hasMetadata = true;
            }
            QDLStem arg = qdlValues[1].asStem();
            boolean doBatch = arg.isList();
            QDLStem map = null;
            if (qdlValues.length > 2) {
                if(qdlValues[2].isStem()) {
                    map = qdlValues[2].asStem();
                    if(map.containsKey(QDL_TYPE_NAME_FORCE_COLUMN_CASE)){
                        forceCaseInsensitiveColumns = map.get(QDL_TYPE_NAME_FORCE_COLUMN_CASE).asBoolean();
                    }
                }else{
                    if(qdlValues[2].isBoolean()) {
                        forceCaseInsensitiveColumns = qdlValues[2].asBoolean();
                    }else{
                        throw new IllegalArgumentException("Third argument must be a boolean or stem");
                    }
                }
            } else {
                map = new QDLStem();
            }
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            long rowUpdated = 0;
            long[] rowsUpdated = null;
            try {
                TreeMap<String, Integer> columnNames = hasMetadata ? getColumns(metadata) : getSQLMetadata(c, tablename);
                ;
                PreparedStatement pstmt = createInsertStatement(connectionRecord.connection, tablename,
                        columnNames, arg, map, forceCaseInsensitiveColumns);
                if (doBatch) {
                    rowsUpdated = pstmt.executeLargeBatch();
                } else {
                    rowUpdated = pstmt.executeUpdate();
                }
                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
            if (doBatch) {
                QDLStem out = new QDLStem();
                for (long row : rowsUpdated) {
                    out.getQDLList().add(QDLValue.asQDLValue(row));
                }
                return QDLValue.asQDLValue(out);
            }
            return QDLValue.asQDLValue(rowUpdated);
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
                    docs.add(getName() + "(table, arg., conversions. | "+QDL_TYPE_NAME_FORCE_COLUMN_CASE + ") - insert the stem into the database as a new row.");
                    docs.add("table = the (fully-qualified) table name to construct the insert statement.");
                    docs.add("arg. = the col-value pairs to use for the update.");
                    docs.add(QDL_TYPE_NAME_FORCE_COLUMN_CASE + " = Some databases allow for case sensitive column names. This flag is");
                    docs.add("                  for the situation where the database allows for case sensitive columns, but you are not using them.");
                    docs.add("                  Typically this means the database reports the column names in all upper or lower case, which might not");
                    docs.add("                  be how you have them in your stem. So, if true, it will force the all column names");
                    docs.add("                  to be processed case insensitive. The default is false, meaning either (a) no matter how you use them,");
                    docs.add("                  the database itself will ignore the case, or (b) you use the exact case at all times.");
                    addQDLTypesBlurb(docs);
                    docs.add("Note that if you may also supply the " +QDL_TYPE_NAME_FORCE_COLUMN_CASE + " flag in the conversion stem.");
                    break;
            }
            return docs;
        }
    }

    public static String GET_TABLE_METADATA = "get_table_metadata";
    public static String MD_TABLENAME = "table_name";
    public static String MD_CATALOG = "catalog";
    public static String MD_SCHEMA = "schema";
    public static String MD_PRIMARY_KEY = "primary_key";
    public static String MD_AUTO_COMMIT = "auto_commit";
    public static String MD_COLUMNS = "columns";
    public static String MD_DB_VENDOR = "vendor";
    public static String MD_DB_VERSION = "version";
    public static String MD_DB_MAJOR_RELEASE = "major_release";
    public static String MD_DB_MINOR_RELEASE = "minor_release";

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
            // It is possible that the tablename is fully qualified with the schema, e.g.
            // oauth2.clients. To get primary keys we need exactly the unqualified table name
            String givenSchema = tablename.substring(0, tablename.lastIndexOf('.'));
            String unqTableName = tablename.substring(tablename.lastIndexOf('.') + 1);
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            QDLStem outStem = new QDLStem();
            boolean isDerby = connectionPool.getConnectionParameters() instanceof DerbyConnectionParameters;

            try {
                QDLStem db = new QDLStem();


                db.put(MD_DB_VENDOR, asQDLValue(c.getMetaData().getDatabaseProductName()));
                db.put(MD_DB_VERSION, asQDLValue(c.getMetaData().getDatabaseProductVersion()));
                db.put(MD_DB_MAJOR_RELEASE, asQDLValue(c.getMetaData().getDatabaseMajorVersion()));
                db.put(MD_DB_MINOR_RELEASE, asQDLValue(c.getMetaData().getDatabaseMinorVersion()));
                outStem.put("db", QDLValue.asQDLValue(db));
                Statement stmt = c.createStatement();
                ResultSet res = stmt.executeQuery(dudQuery);
                ResultSetMetaData rsmd = res.getMetaData();
                String tttt = rsmd.getTableName(1);
                nonTrivialPut(outStem, MD_TABLENAME, rsmd.getTableName(1));
                nonTrivialPut(outStem, MD_CATALOG, c.getCatalog());
                if (isDerby) {
                    // Derby has the annoying bug that the default schema is the username. So explicitly
                    // getting the metadata for a table will return the wrong schema.
                    nonTrivialPut(outStem, MD_SCHEMA, givenSchema);
                } else {
                    nonTrivialPut(outStem, MD_SCHEMA, c.getSchema());
                }
                nonTrivialPut(outStem, MD_AUTO_COMMIT, c.getAutoCommit());
                ResultSet pkRS = c.getMetaData().getPrimaryKeys(c.getCatalog(), c.getSchema(), tttt);
                QDLStem primaryKeyList = new QDLStem();
                while (pkRS.next()) {
                    primaryKeyList.getQDLList().add(QDLValue.asQDLValue(pkRS.getString("COLUMN_NAME")));
                    System.out.println("column name : " + pkRS.getString("COLUMN_NAME"));
                    System.out.println("    pk name : " + pkRS.getString("PK_NAME"));
                    System.out.println("    key seq : " + pkRS.getString("KEY_SEQ"));
                }
                pkRS.close();
                nonTrivialPut(outStem, MD_PRIMARY_KEY, primaryKeyList);
                QDLStem columns = new QDLStem();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    int type = rsmd.getColumnType(i);
                    String columnName = rsmd.getColumnName(i);
                    columns.put(QDLKey.from(columnName), QDLValue.asQDLValue(type));
                }

                stmt.close();
                outStem.put(QDLKey.from(MD_COLUMNS), QDLValue.asQDLValue(columns));
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw e;
            }

            return QDLValue.asQDLValue(outStem);
        }

        protected void nonTrivialPut(QDLStem stem, Object key, Object value) {
            if (value == null) {
                return;
            }
            if (value instanceof String) {
                if (StringUtils.isTrivial((String) value)) {
                    return;
                }
            }
            stem.put(QDLKey.from(key), QDLValue.asQDLValue(value));

        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            docs.add(getName() + "(table) - get metadata about the given table. Returns a stem with the following keys:");
            docs.add(justify(MD_AUTO_COMMIT, 15, true) + " = true if auto-commit is enabled.");
            docs.add(justify(MD_CATALOG, 15, true) + " = the catalog name.");
            docs.add(justify(MD_COLUMNS, 15, true) + " = a stem with the column names as keys and the column SQL types as values.");
            docs.add(justify(MD_PRIMARY_KEY, 15, true) + " = a list with the primary key column names.");
            docs.add(justify(MD_SCHEMA, 15, true) + " = the schema name.");
            return docs;
        }
    }

    /**
     * Given tablename get just the columns names and their types.
     *
     * @param c
     * @param tablename
     * @return
     * @throws SQLException
     */
    protected TreeMap<String, Integer> getSQLMetadata(Connection c, String tablename) throws SQLException {
        String dudQuery = "select * from " + tablename + " where 1 < 0";
        // It is possible that the tablename is fully qualified with the schema, e.g.
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
     * Utility to convert the columns entry in a metadata stem to usable (column, int) pairs
     * for SQL.
     *
     * @param metadata
     * @return
     * @throws SQLException
     */
    protected TreeMap<String, Integer> getColumns(QDLStem metadata) throws SQLException {
        TreeMap<String, Integer> columnNames = new TreeMap<>();
        QDLStem columns = metadata.getStem("columns");
        for (QDLKey k : columns.keySet()) {
            columnNames.put(k.toString(), columns.get(k).asLong().intValue());
        }
        return columnNames;
    }

    /**
     * Create the prepared statement. This requires creating the SQL statement and managing
     * all the column names and types.
     *
     * @param conn
     * @param tablename
     * @param columnNames From the database, key = col name, value = SQL type.
     * @param arg         = stem of col-value pairs to insert. May be partial
     * @param map         = stem of QDL types keyed by column name.
     * @return
     * @throws SQLException
     */
    PreparedStatement createInsertStatement(Connection conn,
                                            String tablename,
                                            TreeMap<String, Integer> columnNames,
                                            QDLStem arg,
                                            QDLStem map,
                                            boolean forceCaseInsensitive) throws SQLException, ParseException {
        String insertStatement = "insert into " + tablename + " (";
        String values = " values (";
        boolean isFirst = true;
        List<String> actualColumns = new ArrayList<>();
        //Now just run through the column names and see what was sent,
        if (!forceCaseInsensitive) {
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
        } else {
            TreeMap<String, Integer> columnNames2 = new TreeMap<>();

            // E.g. for Derby, the column names are probably upper case, so we need to match them exactly.
            for (String column : columnNames.keySet()) {
                TreeMap<String, Integer> finalColumnNames = columnNames;
                arg.keySet().stream().filter(k -> k.toString().equalsIgnoreCase(column)).forEach(k -> {
                    actualColumns.add(k.toString());
                    columnNames2.put(k.toString(), finalColumnNames.get(column));
                });
            }
            for (String column : actualColumns) {
                if (arg.containsKey(column.toLowerCase())) {
                    //  actualColumns.add(column);
                    insertStatement = insertStatement + (isFirst ? "" : ",") + column;
                    values = values + (isFirst ? "" : ",") + "?";
                    if (isFirst) {
                        isFirst = false;
                    }
                }
            }
            columnNames = columnNames2;
        }
        insertStatement = insertStatement + ")" + values + ")";
        return doPreparedStatement(conn, columnNames, arg, map, insertStatement, actualColumns);
    }

    /**
     * Used by {@link #createInsertStatement(Connection, String, TreeMap, QDLStem, QDLStem)} and
     * {@link #createUpdateStatement} (Connection, String, TreeMap, QDLStem, QDLStem)} to actually
     * populate the prepared statement created by this system. The point is the query is generated
     * in this module and managed directly, rather than being sent by the user and this has the logic
     * for that.
     *
     * @param conn
     * @param columnNames
     * @param arg
     * @param map
     * @param rawPStatement
     * @param actualColumns
     * @return
     * @throws SQLException
     * @throws ParseException
     */
    private PreparedStatement doPreparedStatement(Connection conn,
                                                  TreeMap<String, Integer> columnNames,
                                                  QDLStem arg,
                                                  QDLStem map,
                                                  String rawPStatement,
                                                  List<String> actualColumns) throws SQLException, ParseException {
        PreparedStatement pstmt = conn.prepareStatement(rawPStatement);
        int counter = 1;
        boolean doBatch = false;
        if (arg.isList()) {
            doBatch = true;
            counter = arg.getQDLList().size();
        }
        for (int j = 0; j < counter; j++) {
            int i = 1; // SQL columns start at 1
            for (String columnName : actualColumns) {
                int qdlType = QDL_TYPE_DEFAULT;
                if (map.hasDefaultValue() || map.containsKey(columnName)) {
                    qdlType = map.getLong(columnName).intValue();
                }
                setParam(pstmt, i++, columnNames.get(columnName), qdlType, arg.get(columnName));
            }
            if (doBatch) {
                pstmt.addBatch();
            }
        }
        return pstmt;
    }

    PreparedStatement createUpdateStatement(Connection conn,
                                            QDLStem metadata,
                                            QDLStem arg,
                                            QDLStem map) throws SQLException, ParseException {
        String updateStatement = "update " + metadata.getString(MD_TABLENAME) + " set ";
        boolean isFirst = true;
        List<String> actualColumns = new ArrayList<>();
        QDLStem mdColumns = metadata.getStem(MD_COLUMNS);
        QDLList mdPrimaryKeys = metadata.getStem(MD_PRIMARY_KEY).getQDLList();
        TreeMap<String, Integer> columnNames = new TreeMap<>();
        for (QDLKey key : mdColumns.keySet()) {
            columnNames.put(key.asString(), mdColumns.getLong(key).intValue());
        }
        TreeSet<String> primarykeyColumns = new TreeSet<>();
        for (Object value : mdPrimaryKeys) {
            primarykeyColumns.add(((QDLValue) value).asString());
        }

        //Now just run through the column names and see what was sent,
        for (String column : columnNames.keySet()) {
            if (arg.containsKey(column) && !primarykeyColumns.contains(column)) {
                actualColumns.add(column);
                updateStatement = updateStatement + (isFirst ? "" : ", ") + column + "=?";
                if (isFirst) {
                    isFirst = false;
                }
            }
        }
        // now create the where clause, or EVERY element in the table is updated!
        updateStatement = updateStatement + " where ";
        isFirst = true;
        for (String pk : primarykeyColumns) {
            actualColumns.add(pk);
            updateStatement = updateStatement + (isFirst ? "" : " and ") + pk + "=?";
            if (isFirst) {
                isFirst = false;
            }
        }

        return doPreparedStatement(conn, columnNames, arg, map, updateStatement, actualColumns);

    }

    protected void addQDLTypesBlurb(List<String> docs) {
        docs.add("qdl_types. = a stem of code to use for conversions from values in the stem to SQL.");
        docs.add("This will use the data type as found in " + QDL_TYPES_VAR_NAME + " to convert between");
        docs.add("QDL and SQL.");
        docs.add("Example.");
        docs.add("If an SQL store has a string representation of a JSON object, then on read");
        docs.add("the string will be converted to a stem., On update or insert, an embedded stem");
        docs.add("will be converted to its JSON representation then stored as a string.");
    }

/*    public static String DATA_TYPES_STEM_NAME = "$$DATA_TYPE.";

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
    }*/

    /*
    We use these in case statements and longs as cae values are not supporte
    in Java. So have the fiddle with them here.
     */
    public static final int QDL_TYPE_DEFAULT = 0; //default
    public static final int QDL_TYPE_JSON = 1;
    public static final int QDL_TYPE_INPUT_FORM = 2;
    public static final int QDL_TYPE_DATE = 3;
    public static final int QDL_TYPE_STRING = 4;
    public static final int QDL_TYPE_JSON_SET = 5;

    public static final String QDL_TYPE_NAME_DEFAULT = "default"; //default
    public static final String QDL_TYPE_NAME_JSON = "json";
    public static final String QDL_TYPE_NAME_INPUT_FORM = "input_form";
    public static final String QDL_TYPE_NAME_DATE = "date";
    public static final String QDL_TYPE_NAME_STRING = "string";
    public static final String QDL_TYPE_NAME_JSON_SET = "json_set";
    public static final String QDL_TYPE_NAME_FORCE_COLUMN_CASE = "force_column_case";


    public QDLStem getDataTypes() {
        if (types == null) {
            types = new QDLStem();
            put(types, QDL_TYPE_NAME_JSON, (long) QDL_TYPE_JSON);
            put(types, QDL_TYPE_NAME_JSON_SET, (long) QDL_TYPE_JSON_SET);
            put(types, QDL_TYPE_NAME_INPUT_FORM, (long) QDL_TYPE_INPUT_FORM);
            put(types, QDL_TYPE_NAME_DEFAULT, (long) QDL_TYPE_DEFAULT);
            put(types, QDL_TYPE_NAME_DATE, (long) QDL_TYPE_DATE);
            put(types, QDL_TYPE_NAME_STRING, (long) QDL_TYPE_STRING);
        }
        return types;
    }

    QDLStem types = null;

    public static String UPDATE_COMMAND = "qdl_update";

    public class QDLUpdate implements QDLFunction {
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
            long[] rowsUpdated = null;
            boolean doBatch = arg.isList();
            try {
                GetTableMetadata getTableMetadata = new GetTableMetadata();
                QDLStem md = getTableMetadata.evaluate(new QDLValue[]{qdlValues[0]}, state).asStem();
                PreparedStatement pstmt = createUpdateStatement(connectionRecord.connection, md, arg, qdlTypes);
                if (doBatch) {
                    rowsUpdated = pstmt.executeLargeBatch();
                } else {
                    rowsUpdates = pstmt.executeUpdate();
                }

                releaseConnection(connectionRecord);
            } catch (SQLException e) {
                destroyConnection(connectionRecord);
                throw new GeneralException("Error executing SQL: " + e.getMessage(), e);
            }
            if (doBatch) {
                QDLStem out = new QDLStem();
                for (long row : rowsUpdated) {
                    out.getQDLList().add(QDLValue.asQDLValue(row));
                }
                return QDLValue.asQDLValue(out);
            }
            return QDLValue.asQDLValue(rowsUpdates);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            if (argCount == 2) {
                docs.add(getName() + "(table_metadate. | tablename, arg.)");
            }
            if (argCount == 3) {
                docs.add(getName() + "( table_metadate. | tablename, arg., qdl_types.)");
            }
            docs.add("table_metadata. = the metadata for the table, typically from " + GET_TABLE_METADATA + "()");
            docs.add("                   Some vendors (Derby is notorious) have very poor support for metadata queries.");
            docs.add("                   In particular, they might not return primary keys. In that case for constructed");
            docs.add("                   insert statements, should be specified in the metadata stem. Best we can do.");
            docs.add("tablename = name of the table to update. The metadata will be obtained from " + GET_TABLE_METADATA + "().");
            docs.add("     arg. = the QDL stem that will be used tp update");
            if (argCount == 3) {
                addQDLTypesBlurb(docs);
            }
            docs.add("Returns the number of rows updated.");
            docs.add("This function will create an update statement based on the arg., which is a QDL stem.");
            docs.add("It will get the SQL types for the table and do conversions based on the qdl_types.");
            docs.add("It will also update based on the primary key, so that must be part of the arg., ");
            docs.add("otherwise the update will fail. Compound keys are allowed.");
            return docs;
        }
    }

    public static String PREPARED_UPDATE_COMMAND = "update";

    public class PreparedUpdate implements QDLFunction {
        @Override
        public String getName() {
            return PREPARED_UPDATE_COMMAND;
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
            String rawStatement = qdlValues[0].asString();
            List<QDLValue> args = null;
            boolean hasQDLTypes = qdlValues.length > 2;
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
                    if (hasQDLTypes) {
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

            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            Long updateCount = 0L;
            /*

             */
            try {
                PreparedStatement stmt = c.prepareStatement(rawStatement);
                if (args != null) {

                    Map<Long, int[]> qdlTypeMap = creatQDLTypeMap(qdlTypes);
                    int i = 1;
                    for (QDLValue entry : args) {
                        if (qdlTypeMap.containsKey((long) i - 1)) {
                            int[] q = qdlTypeMap.get((long) i - 1);
                            setParam(stmt, i, q[0], q[1], entry);
                        } else {
                            setParam(stmt, i, entry.getValue());
                        }
                        i++;
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
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
                map.put("none", (long) QDL_TYPE_DEFAULT);
                map.put("json", (long) QDL_TYPE_JSON);
                map.put("json_set", (long) QDL_TYPE_JSON_SET);
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
                map.put("DECIMAL", (long) DECIMAL);
                map.put("INTEGER", (long) INTEGER);
                map.put("BIGINT", (long) BIGINT);
                map.put("REAL", (long) REAL);
                map.put("FLOAT", (long) FLOAT);
                map.put("NULL", (long) NULL_TYPE);
                map.put("DOUBLE", (long) DOUBLE);
                map.put("VARBINARY", (long) VARBINARY);
                map.put("LONGVARBINARY", (long) LONGVARBINARY);
                map.put("BINARY", (long) BINARY);
                map.put("DATE", (long) DATE);
                map.put("TIME", (long) TIME);
                map.put("TIMESTAMP", (long) TIMESTAMP);
                map.put("CLOB", (long) CLOB);
                map.put("NCLOB", (long) NCLOB);
                map.put("NCHAR", (long) NCHAR);
                map.put("NVARCHAR", (long) NVARCHAR);
                map.put("BLOB", (long) BLOB);
                map.put("ARRAY", (long) ARRAY);
                map.put("REF", (long) REF);
                map.put("STRUCT", (long) STRUCT);
                map.put("SQLXML", (long) SQLXML);
                map.put("TIME_WITH_TIMEZONE", (long) TIME_WITH_TIMEZONE);
                map.put("TIMESTAMP_WITH_TIMEZONE", (long) TIMESTAMP_WITH_TIMEZONE);
                sqlTypes = new QDLStem();
                StemUtility.setStemValue(sqlTypes, map);
            }
            return sqlTypes;
        }


    }

    public static final String SHUTDOWN = "close";

    public class Shutdown implements QDLFunction {
        @Override
        public String getName() {
            return SHUTDOWN;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            BooleanValue b = True;
            if ((connectionPool.getConnectionParameters() instanceof DerbyConnectionParameters)) {
                DerbyConnectionParameters params = (DerbyConnectionParameters) connectionPool.getConnectionParameters();
                try {
                    DriverManager.getConnection(params.getJdbcUrl() + ";shutdown=true");
                } catch (SQLException e) {
                    if (e.getSQLState().equals("08006")) {
                        b = True;
                    } else {
                        if (state.isDebugOn()) {
                            e.printStackTrace();
                        }
                        b =  False;
                    }
                }
            }
            connectionPool = null; // contract is no more connections allowed after this called.
            return b;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> docs = new ArrayList<>();
            docs.add(getName() + "() - closes the database connection pool. Further connection are not possible.");
            docs.add("This returns true if the shutdown was successful, false otherwise.");
            docs.add("Mostly this is only required for Derby, as it requires special handling.");
            return docs;
        }
    }

    /* *************** End of classes. Below is all utilities ************ */

    /**
     * Used for both create and delete.
     *
     * @param qdlValues
     * @param name
     * @return
     */
    public QDLValue doSQLExecute(QDLValue[] qdlValues, String name, State state) throws Throwable {
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
        boolean hasQDlTypes = qdlValues.length == 3;
        if (hasQDlTypes) {
            if (!qdlValues[2].isStem()) {
                throw new BadArgException("The third argument must be a stem", 2);
            }
            if (!qdlValues[2].isList()) {
                throw new BadArgException("The last argument if present must be a list of entries [sql_type, qdl_type]", 2);
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
            if (args != null && !args.isEmpty()) {
                int i = 1;
                Map<Long, int[]> qdlTypeMap = creatQDLTypeMap(qdlTypes);
                for (QDLValue entry : args) {
                    if (qdlTypeMap.containsKey((long) i - 1)) {
                        int[] q = qdlTypeMap.get((long) i - 1);
                        setParam(stmt, i, q[0], q[1], entry);
                    } else {
                        setParam(stmt, i, entry.getValue());
                    }
                    i++;
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

    protected Map<Long, int[]> creatQDLTypeMap(QDLStem qdlTypes) {

        Map<Long, int[]> q = new HashMap<>();
        if (qdlTypes == null) {
            return q;
        }
        for (QDLKey key : qdlTypes.getQDLList().orderedKeys()) {
            if (key.isLong()) {
                QDLValue ee = qdlTypes.getQDLList().get(key.asLong());
                int[] qq = new int[2];
                qq[0] = ee.asStem().getLong(0L).intValue();
                qq[1] = ee.asStem().getLong(1L).intValue();
                q.put(key.asLong(), qq);
            } else {
                throw new BadArgException("The last argument if present must be a list of entries [sql_type, qdl_type]", 2);
            }
        }
        return q;
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
            return new int[]{2, 3};
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
            boolean hasQDLTypes = qdlValues.length == 3;
            if (hasQDLTypes) {
                if (!qdlValues[2].isStem()) {
                    throw new BadArgException("the second argument to " + getName() + " must be a stem", 2);

                }
                qdlTypes = qdlValues[2].asStem();
                if (!qdlTypes.isList()) {
                    throw new BadArgException("the second argument to " + getName() + " must be a list", 2);
                }
            } else {
                qdlTypes = new QDLStem();
                qdlTypes.setDefaultValue(QDL_TYPE_DEFAULT);
            }
            QDLStem args = qdlValues[1].asStem();
            ConnectionRecord connectionRecord = connectionPool.pop();
            Connection c = connectionRecord.connection;
            HashMap returnCodes = new HashMap();
            int counter = 0;
            try {
                PreparedStatement stmt = c.prepareStatement(qdlValues[0].asString());

                Map<Long, int[]> qdlTypeMap = creatQDLTypeMap(qdlTypes);

                for (QDLKey key : args.keySet()) {
                    QDLValue value = args.get(key);
                    QDLList<? extends QDLValue> list;
                    if (value.isStem()) {
                        QDLStem arg = value.asStem();
                        if (!arg.isList()) {
                            throw new IllegalArgumentException("the element with index '" + key + " must be a list or scalar");
                        }
                        list = arg.getQDLList();
                    } else {
                        list = new QDLList();
                        list.add(asQDLValue(value));
                    }

                    int i = 1;
                    for (QDLValue entry : list) {
                        //setParam(stmt, i, qdlTypes.get(i - 1).asLong().intValue(), entry);
                        if (hasQDLTypes && qdlTypeMap.containsKey((long) i - 1)) {
                            int[] q = qdlTypeMap.get((long) i - 1);
                            setParam(stmt, i, q[0], q[1], entry);
                        } else {
                            setParam(stmt, i, entry.getValue());
                        }
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
            if (argCount == 2) {
                documentation.add(getName() + "(statement, values.) - execute a statement with multiple values. ");
            }
            if (argCount == 3) {
                documentation.add(getName() + "(statement, values., conversions.) - execute a statement with multiple values. ");
            }
            documentation.add("  statement - the (prepared) statement to execute. There is no restriction on the complexity.");
            documentation.add("              However, the same statement applies to all values.");
            documentation.add("    values. - a stem of scalars or lists, with the elements for the prepared statement");
            documentation.add("              If the elements are scalars, then this is assumed to be a single value. If lists,");
            documentation.add("              then the values are applied to each element in the list.");
            if (argCount == 3) {
                documentation.add("conversions - a stem of [sql_type, qdl_type] pairs, one for each value in values.");
                documentation.add("              sql_type -  one of the constants in the sql_types. variable");
                documentation.add("              qdl_type - one of the constants in the qdl_types. variable");
                documentation.add("              Note that this may be sparse, so {4:[-1,4]} is valid.");
                documentation.add("              If this absent, then the default is to assume no conversion.");
            }
            documentation.add("It may be used for INSERT, DELETE or UPDATE.");
            documentation.add("It returns either:");
            documentation.add("  A stem of return codes indexed by keys (if no result), or ");
            documentation.add("  a conformable stem each of whose values is a non-zero integer");
            documentation.add("    indicating the number of records in the database changed by this statment, or");
            documentation.add("     a negative integer where");
            documentation.add(Statement.SUCCESS_NO_INFO + " = the operation worked, but no other information is available");
            documentation.add(Statement.EXECUTE_FAILED + " = the statement failed, but processing continued.");
            documentation.add("See help for " + BATCH_QUERY_COMMAND + " for batch reads");
            documentation.add("This call is logically equivalent to loop through statements and execute them,");
            documentation.add("but most SQL databases optimize such a batch execution and for very large");
            documentation.add("datasets, the performance difference can be quite dramatic. ");
            documentation.add("Moreover, drivers that talk to databases need make a single call with this");
            documentation.add("method, which saves resources and is much more efficient. Indeed, a large loop may");
            documentation.add("exhaust connection resources and cause a failure. Batches are optimized to always work.");
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
