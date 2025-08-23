package org.qdl_lang;

import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;

/**
 * Tests for the system evaluator and its functionality.
 */
public class SystemTest extends AbstractQDLTester{
    /**
     * Basic script test. The script a.qdl calls several more scripts in turn,
     * so there is a chain of these. They in turn do the assertions. If the test works
     * it just runs, otherwise there is an assertion error.
     * @throws Throwable
     */
    public void testScripts() throws Throwable{
        String path = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/lib-test/";
        StringBuffer script = new StringBuffer();
        addLine(script, "lib_support(true);");
        addLine(script, "lib_support(['" +path + "']);");
        addLine(script, "⊨a('foo')≡'afoo';"); // this calls several scripts in turn with assertions
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert true;
    }

    /**
     * Checks that the defaults on startup are correct.
     * @throws Throwable
     */
    public void testLibSupport() throws Throwable{
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := lib_support();");
        addLine(script, "ok0 := a.'enabled' == false;");
        addLine(script, "ok1 := '" + System.getProperty("user.dir") + "'∈a.'path';");
        addLine(script, "ok2 := a.'mode' == 'load';");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "lib_support enabled flag incorect";
        assert getBooleanValue("ok1", state) : "lib_support default path is not the invocation directory";
        assert getBooleanValue("ok2", state) : "lib_support mode incorect";

    }

}
