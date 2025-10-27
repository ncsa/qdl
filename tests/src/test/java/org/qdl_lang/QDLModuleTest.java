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

    public void testEXTClone() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "a. ≔ {'a':random(5),'b':random(4)};");
        addLine(script, "b. := ext#clone(a.);");
        addLine(script, "a.'a'.0 ≔ 2;");
        addLine(script, "⊨b.'a'.0 ≠ 2;");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testEXTNCopy() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "a. ≔ ext#n_copy('x', 10);");
        addLine(script, "⊨size(a.) == 10;"); // There are 10
        addLine(script, "⊨⊗∧⊙'x'≡a.;"); // They are all named 'x'
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testEXTJFork() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "sum(x.)->reduce(@+, x.);");
        addLine(script, "⊨10 ≡ ext#jfork(@sum, @/, @size, 1+2*n(10));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testEXTMSetScalar() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "a.:=n(5,5,n(25));");
        addLine(script, "ext#m_set(a., [[0,0],[1,1],[2,2],[3,3],[4,4]], -1);"); // set diagonal to -1
        addLine(script, "⊨a.0.0 == -1;");
        addLine(script, "⊨a.1.0 != -1;");
        addLine(script, "⊨a.1.1 == -1;");
        addLine(script, "⊨a.2.2 == -1;");
        addLine(script, "⊨a.3.3 == -1;");
        addLine(script, "⊨a.4.4 == -1;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testEXTMSetList() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "a.:=n(5,5,n(25));");
        addLine(script, "ext#m_set(a., [[0,0],[1,1],[2,2],[3,3],[4,4]], ['a','b','c','d','e']);"); // set diagonal to strings
        addLine(script, "⊨a.0.0 == 'a';");
        addLine(script, "⊨a.1.0 != -1;");
        addLine(script, "⊨a.1.1 == 'b';");
        addLine(script, "⊨a.2.2 == 'c';");
        addLine(script, "⊨a.3.3 == 'd';");
        addLine(script, "⊨a.4.4 == 'e';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testEXTMToUUID() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "⊨'8fdb6080-1e9d-39a5-286a-a01dd1f4f4f3'≡ext#to_uuid('woof') : 'failed to create UUID';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testEXTMIindices() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ext := import(load('" + EXT_MODULE + "'));");
        addLine(script, "x. ≔ ext#m_indices([2;5],'a');");
        addLine(script, "⊨⊗∧⊙[[2,'a'],[3,'a'],[4,'a']] ≡ x.`*;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
    }

    public void testConvertYAML_IN() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        String y2 = "'--- # Optional: Start of a YAML document\\n'+\n" +
                "'# This is a comment in YAML\\n'+\n" +
                "'# Key-value pairs:\\n'+\n" +
                "'name: John Doe\\n'+\n" +
                "'age: 30\\n'+\n" +
                "'isStudent: false\\n'+\n" +
                "'\\n'+\n" +
                "'# Lists/Arrays:\\n'+\n" +
                "'skills:\\n'+\n" +
                "'  - Python\\n'+\n" +
                "'  - JavaScript\\n'+\n" +
                "'  - YAML\\n'+\n" +
                "'\\n'+\n" +
                "'# Nested Dictionaries/Objects:\\n'+\n" +
                "'address:\\n'+\n" +
                "'  street: 123 Main St\\n'+\n" +
                "'  city: Anytown\\n'+\n" +
                "'  zip: \"12345\" # Quoted to ensure it\\'s treated as a string\\n'+\n" +
                "'\\n'+\n" +
                "'# List of Dictionaries:\\n'+\n" +
                "'projects:\\n'+\n" +
                "'  - title: Project Alpha\\n'+\n" +
                "'    status: In Progress\\n'+\n" +
                "'    team:\\n'+\n" +
                "'      - Alice\\n'+\n" +
                "'      - Bob\\n'+\n" +
                "'  - title: Project Beta\\n'+\n" +
                "'    status: Completed\\n'+\n" +
                "'    team:\\n'+\n" +
                "'      - Charlie\\n'+\n" +
                "'\\n'+\n" +
                "'# Multi-line string (folded style):\\n'+\n" +
                "'description: >\\n'+\n" +
                "'  This is a multi-line string.\\n'+\n" +
                "'  Newlines are converted to spaces unless\\n'+\n" +
                "'  explicitly preserved.\\n'+\n" +
                "'\\n'+\n" +
                "'# Multi-line string (literal style):\\n'+\n" +
                "'notes: |\\n'+\n" +
                "'  - Item 1\\n'+\n" +
                "'  - Item 2\\n'+\n" +
                "'  - Item 3'";
        addLine(script, "convert ≔ j_load('convert');");
        addLine(script, "y := " + y2 + ";");
        addLine(script, "x. := convert#yaml_in(y);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        QDLStem stem = getStemValue("x.", state);
        assert stem.get("age").equals(30L) : "age is not 30";
    }

    public void testConvertHOCON_IN() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        String h = "'# This is a comment\\n'+\n" +
                "'application {\\n'+\n" +
                "'  name = \"MyAwesomeApp\"\\n'+\n" +
                "'  version = \"1.0.0\"\\n'+\n" +
                "'  settings {\\n'+\n" +
                "'    debug-mode = true\\n'+\n" +
                "'    log-level = \"INFO\"\\n'+\n" +
                "'  }\\n'+\n" +
                "'}'";
        addLine(script, "convert ≔ j_load('convert');");
        addLine(script, "y := " + h + ";");
        addLine(script, "x. := convert#hocon_in(y);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        QDLStem stem = getStemValue("x.", state);
        assert stem.getStem("application").get("version").equals("1.0.0") : "HOCON did not import correctly";
        assert stem.getStem("application").get("name").equals("MyAwesomeApp") : "HOCON did not import correctly";

    }
}
