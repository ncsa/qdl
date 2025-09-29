package org.qdl_lang;

import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;

/**
 * Set of tests for the supplied QDL modules in the standard distribution.
 */
public class QDLModuleTest extends AbstractQDLTester {
    String EXT_MODULE = DebugUtil.getDevPath() + "/qdl/language/src/main/resources/modules/ext.mdl";
    String MATHX_MODULE = DebugUtil.getDevPath() + "/qdl/language/src/main/resources/modules/math-x.mdl";

    public void testClone() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('"+EXT_MODULE + "'));");
        addLine(script, "a. ≔ {'a':random(5),'b':random(4)};");
        addLine(script, "b. := ext#clone(a.);");
        addLine(script, "a.'a'.0 ≔ 2;");
        addLine(script, "⊨b.'a'.0 ≠ 2;");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        QDLStem stem = getStemValue("a.", state);
    }
}
