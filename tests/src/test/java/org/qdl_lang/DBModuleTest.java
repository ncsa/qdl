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
    protected String CONNECTION_INI = "/home/ncsa/dev/csd/config/db-connector.qdl";

    /**
     * Creates the QDL script_run command from the name of the script to run.
     *
     * @param qdlScript
     * @return
     */
    protected String createScriptRun(String qdlScript) {
        return "script_load(os_env('NCSA_DEV_INPUT')+'" + SCRIPT_PATH + qdlScript + "','" + CONNECTION_INI + "');";
    }

    protected String createUtilLoad(String utils) {
        return "interpret(file_read(os_env('NCSA_DEV_INPUT')+'" + SCRIPT_PATH + utils + "'));";
    }

    public void testRead() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-read.qdl"));
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

    /**
     * Creates a database entry from a stem, no QDL conversions. Just checks that
     * the default column mappings are actually honored.
     *
     * @throws Throwable
     */
    public void testBasicCreate() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-create2.qdl"));
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
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, createUtilLoad("util.qdl"));
        addLine(script, createScriptRun("test-create-qtypes.qdl"));
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

}
