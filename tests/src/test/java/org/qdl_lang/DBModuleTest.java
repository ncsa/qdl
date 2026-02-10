package org.qdl_lang;

import org.qdl_lang.exceptions.AssertionException;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;

public class DBModuleTest extends AbstractQDLTester {

    /**
     * Path in the distribution to the DB module test scripts
     */
    protected String SCRIPT_PATH = "/qdl/tests/src/test/resources/db-module/";

    /**
     * Path to the connection ini file. Since this has passwords etc. it cannot be in the
     * distribution lest it end up in GitHub/
     */
    protected String MYSQL_CONNECTION_INI = "/home/ncsa/dev/csd/config/mysql-connector.qdl";
    protected String DERBY_CONNECTION_INI = "/home/ncsa/dev/csd/config/derby-connector.qdl";
    protected String POSTGRES_CONNECTION_INI = "/home/ncsa/dev/csd/config/pg-connector.qdl";

    /**
     * Creates the QDL script_run command from the name of the script to run.
     *
     * @param qdlScript
     * @return
     */
    protected String createScriptRun(String qdlScript) {
        return "script_load(os_env('NCSA_DEV_INPUT')+'" + SCRIPT_PATH + qdlScript + "','" + MYSQL_CONNECTION_INI + "');";
    }

    protected String createScriptRun(String qdlScript, String connectionIni) {
        return "script_load(os_env('NCSA_DEV_INPUT')+'" + SCRIPT_PATH + qdlScript + "','" + connectionIni + "');";
    }

    protected String createScriptRun(String qdlScript, String connectionIni, boolean forceCaseOnColumns) {
        return "script_load(os_env('NCSA_DEV_INPUT')+'" + SCRIPT_PATH + qdlScript + "','" + connectionIni + "',"+ forceCaseOnColumns + ");";
    }

    protected String createUtilLoad(String utils) {
        return "interpret(file_read(os_env('NCSA_DEV_INPUT')+'" + SCRIPT_PATH + utils + "'));";
    }

    public void testReadWithQDLTypes() throws Throwable {
        testReadWithQDLTypes(MYSQL_CONNECTION_INI);
        testReadWithQDLTypes(DERBY_CONNECTION_INI);
        testReadWithQDLTypes(POSTGRES_CONNECTION_INI);
    }
    protected void testReadWithQDLTypes(String connectionIni) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-read-qtypes.qdl", connectionIni));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println("Update failed. Columns that were not round-tripped:");
            System.out.println(ae.getAssertionState().toString(1));
            assert false : "Round-tripping failed.";
        } catch (Throwable t) {
            throw t;
        }
    }

    public void testBasicRead() throws Throwable {
        testBasicRead(MYSQL_CONNECTION_INI, false);
        testBasicRead(DERBY_CONNECTION_INI, true);
        testBasicRead(POSTGRES_CONNECTION_INI, false);
    }
    protected void testBasicRead(String connectionIni, boolean columnCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-basic-read.qdl", connectionIni, columnCase));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println("Update failed for connector=" + connectionIni + ". Columns that were not round-tripped:");
            System.out.println(ae.getAssertionState().toString(1));
            assert false : "Round-tripping failed.";
        } catch (Throwable t) {
            throw t;
        }
    }

    /**
     * Creates a database entry from a stem, no QDL conversions. Just checks that
     * the default column mappings are actually honored.
     *
     * @throws Throwable
     */
    public void testBasicCreate() throws Throwable {
        testBasicCreate(MYSQL_CONNECTION_INI, false);
        testBasicCreate(DERBY_CONNECTION_INI, true);
        testBasicCreate(POSTGRES_CONNECTION_INI, false);
    }
    protected void testBasicCreate(String connectionIni, boolean forceCaseOnColumns) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-basic-create.qdl",connectionIni, forceCaseOnColumns));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println("Update failed. Columns that were not round-tripped:");
            System.out.println(ae.getAssertionState().toString(1));
            assert false : "Round-tripping failed.";
        } catch (Throwable t) {
            throw t;
        }
    }

    public void testBatchExecute() throws Throwable {
        testBatchExecute(MYSQL_CONNECTION_INI);
        testBatchExecute(POSTGRES_CONNECTION_INI);
        testBatchExecute(DERBY_CONNECTION_INI);
    }
    protected void testBatchExecute(String connectionIni) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-batch-create.qdl", connectionIni));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println("Update failed. Columns that were not round-tripped:");
            System.out.println(ae.getAssertionState().toString(1));
            assert false : "Round-tripping failed.";
        } catch (Throwable t) {
            throw t;
        }
    }
    public void testCreateWithQDLTypes() throws Throwable {
        testCreateWithQDLTypes(MYSQL_CONNECTION_INI, false);
        testCreateWithQDLTypes(DERBY_CONNECTION_INI, true);
        testCreateWithQDLTypes(POSTGRES_CONNECTION_INI, false);
    }
    protected void testCreateWithQDLTypes(String connectionIni, boolean force_column_case) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-create-qtypes.qdl", connectionIni, force_column_case));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println("Update failed. Columns that were not round-tripped:");
            System.out.println(ae.getAssertionState().toString(1));
            assert false : "Round-tripping failed.";
        } catch (Throwable t) {
            throw t;
        }
    }

    public void testQDLTOSQL() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createScriptRun("test-qdl_to_sql.qdl"));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println(ae.getMessage());
            assert false : "Conversion failed";
        } catch (Throwable t) {
            throw t;
        }
    }

    public void testSQLTOQDL() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createScriptRun("test-sql_to_qdl.qdl"));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println(ae.getMessage());
            assert false : "Conversion failed";
        } catch (Throwable t) {
            throw t;
        }
    }
    public void testTableMetadata() throws Throwable {
        testTableMetadata(MYSQL_CONNECTION_INI);
        testTableMetadata(DERBY_CONNECTION_INI);
        testTableMetadata(POSTGRES_CONNECTION_INI);
    }
    protected void testTableMetadata(String connectionIni) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createScriptRun("test-table-metadata.qdl", connectionIni));
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException ae) {
            System.out.println(ae.getMessage());
            assert false : "Conversion failed";
        } catch (Throwable t) {
            throw t;
        }
    }

}
