package org.qdl_lang;

import org.qdl_lang.exceptions.*;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.VariableState;
import edu.uiuc.ncsa.security.core.util.DebugUtil;

import java.math.BigDecimal;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 9/23/21 at  2:20 PM
 */
public class OldModuleTests extends AbstractQDLTester {
    /**
     * Shows that importing module functions g and h outside a function body but referencing them
     * inside it will work: This means that, global functions can be used.
     *
     * @throws Throwable
     */

    public void testFunctionAndModules2() throws Throwable {
        String f_body = "(192*g(x)^3 + 192*g(x)^5 + 68*g(x)*h(y) + 216*g(x)^2*h(y) + \n" +
                "     68*g(x)^3*h(y) + 45*h(y)^2 + 192*g(x)^3*h(y)^2 + 68*g(x)*h(y)^3)/\n" +
                "   (384*g(x)^4 + 384*g(x)^6 + 280*g(x)^2*h(y) + 432*g(x)^3*h(y) + \n" +
                "     280*g(x)^4*h(y) + 21*h(y)^2 + 252*g(x)*h(y)^2 + 21*g(x)^2*h(y)^2 + \n" +
                "     384*g(x)^4*h(y)^2 + 280*g(x)^2*h(y)^3 + 21*h(y)^4)";

        String g_x = "define[g(x)]body[return((x^2-1)/(x^4+1));];";
        String g_module = "module['a:a','a']body[" + g_x + "];";
        String h_y = "define[h(y)]body[return((3*y^3-2)/(4*y^2+y+1));];";
        String h_module = "module['b:b','b']body[" + h_y + "];";
        // import the modules outside of f and try to use them.
        String import_g = "module_import('a:a');";
        String import_h = "module_import('b:b');";
        String f_xy = "define[f(x,y)]body[return(" + f_body + ");];";
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, g_x);
        addLine(script, g_module);
        addLine(script, h_module);
        addLine(script, import_g);
        addLine(script, import_h);
        addLine(script, f_xy);
        // It will ingest the function fine. It is attempting to use it later that will cause the error
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        // all that is set up. Now put in some values and try to evaluate it
        script = new StringBuffer();
        addLine(script, "x :=-3;");
        addLine(script, "y := 5;");
        addLine(script, "z := f(x,y);");
        //  try {
        interpreter.execute(script.toString());

        BigDecimal[] results = {
                new BigDecimal("0.224684095740375"),
                new BigDecimal("0.542433484968442"),
                new BigDecimal("1.22827852483025"),
                new BigDecimal("-0.454986621086766"),
                new BigDecimal("-0.749612627182112")
        };

        for (int i = 1; i < 1 + results.length; i++) {
            script = new StringBuffer();
            addLine(script, "x :=-3/" + i + ";");
            addLine(script, "y := 5/" + i + ";");
            addLine(script, "z := f(x,y);");
            interpreter.execute(script.toString());
            BigDecimal bd = results[i - 1];
            BigDecimal d = getBDValue("z", state);
            assert areEqual(d, bd);
        }
    }
        /*
            module['b:b','b'][h(x)->(3*x^3-2)/(4*x^2+x+1);];
            module['a:a','a'][g(x)->(x^2-1)/(x^4+1);];
            define[f(x,y)][module_import('a:a'); module_import('b:b')
         */

    /**
     * Shows importing modules only into the body of a function works and test that that is their visibility.
     *
     * @throws Throwable
     */
    public void testFunctionAndModules_Good() throws Throwable {
        testFunctionAndModules_Good(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testFunctionAndModules_Good(ROUNDTRIP_XML);
        testFunctionAndModules_Good(ROUNDTRIP_QDL);
        testFunctionAndModules_Good(ROUNDTRIP_JAVA);
        testFunctionAndModules_Good(ROUNDTRIP_JSON);
    }

    protected void testFunctionAndModules_Good(int testCase) throws Throwable {
        BigDecimal[] results = {
                new BigDecimal("0.224684095740375"),
                new BigDecimal("0.542433484968442"),
                new BigDecimal("1.22827852483025"),
                new BigDecimal("-0.454986621086766"),
                new BigDecimal("-0.749612627182112")
        };
        String f_body = "(192*g(x)^3 + 192*g(x)^5 + 68*g(x)*h(y) + 216*g(x)^2*h(y) + \n" +
                "     68*g(x)^3*h(y) + 45*h(y)^2 + 192*g(x)^3*h(y)^2 + 68*g(x)*h(y)^3)/\n" +
                "   (384*g(x)^4 + 384*g(x)^6 + 280*g(x)^2*h(y) + 432*g(x)^3*h(y) + \n" +
                "     280*g(x)^4*h(y) + 21*h(y)^2 + 252*g(x)*h(y)^2 + 21*g(x)^2*h(y)^2 + \n" +
                "     384*g(x)^4*h(y)^2 + 280*g(x)^2*h(y)^3 + 21*h(y)^4)";

        String g_x = "define[g(x)]body[return((x^2-1)/(x^4+1));];";
        String g_module = "module['a:a','a']body[" + g_x + "];";
        String h_y = "define[h(y)]body[return((3*y^3-2)/(4*y^2+y+1));];";
        String h_module = "module['b:b','b']body[" + h_y + "];";
        String import_g = "module_import('a:a');";
        String import_h = "module_import('b:b');";
        String f_xy = "define[f(x,y)]body[" +
                import_g + "\n" +
                import_h + "\n" +
                "return(" + f_body + ");];";
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, g_module);
        addLine(script, h_module);
        addLine(script, f_xy);
        QDLInterpreter interpreter;
        state = rountripState(state, script, testCase);

        addLine(script, "ok := g∄1;");// imported into f means not in session
        //addLine(script, "ok2 := !is_function(h,1);");// imported into f means not in session
        addLine(script, "ok2 := h∄1;");// imported into f means not in session
        addLine(script, "ok3 := reduce(@&&, [g,h]∄1);");// check is defined for modules
        // It will ingest the function fine. It is attempting to use it later that will cause the error
        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state) : "module check for is_function with two arguments failed";

        for (int i = 1; i < 1 + results.length; i++) {
            script = new StringBuffer();
            addLine(script, "x :=-3/" + i + ";");
            addLine(script, "y := 5/" + i + ";");
            addLine(script, "z := f(x,y);");
            interpreter.execute(script.toString());
            BigDecimal bd = results[i - 1];
            BigDecimal d = getBDValue("z", state);
            assert areEqual(d, bd);
        }
    }

    /**
     * Import the same module several times and show that the state of each
     * is kept separate.
     *
     * @throws Throwable
     */

    public void testMultipleModuleImport() throws Throwable {
        testMultipleModuleImport(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testMultipleModuleImport(ROUNDTRIP_XML);
        testMultipleModuleImport(ROUNDTRIP_QDL);
        testMultipleModuleImport(ROUNDTRIP_JAVA);
        testMultipleModuleImport(ROUNDTRIP_JSON);
    }

    protected void testMultipleModuleImport(int testCase) throws Throwable {
        String g_x = "define[g(x)]body[return(x+1);];";
        String h_y = "define[h(y)]body[return(y-1);];";
        String g_module = "module['a:a','a']body[q:=2;w:=3;" + g_x + h_y + "];";
        String import_g = "module_import('a:a');";
        String import_g1 = "module_import('a:a', 'b');";
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, g_module);
        addLine(script, import_g);
        addLine(script, import_g1);
        state = rountripState(state, script, testCase);

        addLine(script, "a#q:=10;");
        addLine(script, "b#q:=11;");
        addLine(script, "oka := a#q==10;");
        // have to stash the checks someplace other than the state object, since these are now
        // squirreled away in a module entry's state.
        addLine(script, "okb := b#q==11;");
        // It will ingest the function fine. It is attempting to use it later that will cause the error
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("oka", state);
        assert getBooleanValue("okb", state);
    }

    /**
     * Make modules with the same variables, import then use NS qualification on the stem and its
     * indices to access them.
     *
     * @throws Throwable
     */
    public void testNSAndStem() throws Throwable {
        testNSAndStem(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testNSAndStem(ROUNDTRIP_XML);
        testNSAndStem(ROUNDTRIP_QDL);
        testNSAndStem(ROUNDTRIP_JAVA);
        testNSAndStem(ROUNDTRIP_JSON);
    }

    protected void testNSAndStem(int testCase) throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','a']body[i:=2;j:=3;list. := -10 + n(5);];");
        addLine(script, "module['a:b','b']body[i:=1;j:=4;list. := -20 + n(5);];");
        addLine(script, "i:=0;");
        addLine(script, "j:=5;");
        addLine(script, "list. := n(10);");
        state = rountripState(state, script, testCase);

        addLine(script, "module_import('a:a');");
        addLine(script, "module_import('a:b');");
        addLine(script, "d := (a#list.).(b#i);");// Should resolve (a#list).(b#i) so index of b#i to 1.
        addLine(script, "e := (b#list.).i;");// should resolve i to 0 since it is not in the module.
        addLine(script, "okd := d == -9;");
        addLine(script, "oke := e == -20;");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okd", state) : "expected d = -9, got " + getLongValue("d", state);
        assert getBooleanValue("oke", state) : "expected e = -20, got " + getLongValue("e", state);
    }

    /**
     * If a variable is in a module and that module is imported, you should be able
     * to access the variable without a namespace if it has been imported and there
     * are no clashes
     *
     * @throws Throwable
     */
    public void testNSAndVariableResolution() throws Throwable {
        NSAndVariableResolutionTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        NSAndVariableResolutionTest(ROUNDTRIP_XML);
        NSAndVariableResolutionTest(ROUNDTRIP_QDL);
        NSAndVariableResolutionTest(ROUNDTRIP_JAVA);
        NSAndVariableResolutionTest(ROUNDTRIP_JSON);
    }

    protected void NSAndVariableResolutionTest(int testCase) throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','a']body[i:=2;list. := -10 + n(5);];");
        addLine(script, "module['a:b','b']body[j:=4;list2. := -20 + n(5);];");
        addLine(script, "module_import('a:a');");
        addLine(script, "module_import('a:b');");
        state = rountripState(state, script, testCase);

        addLine(script, "p := i;");
        addLine(script, "q := list.0;");
        addLine(script, "r := j;");
        addLine(script, "s := list2.0;");
        // Note that if we want to change the value, we need to qualify it still, however.
        // Since an unqualified name gets created in the local state, not the module.
        // May want to revisit how this is done in the design though....
        addLine(script, "a#i := 5;");


        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getLongValue("p", state).equals(2L);
        assert getLongValue("q", state).equals(-10L);
        assert getLongValue("r", state).equals(4L);
        assert getLongValue("s", state).equals(-20l);
        assert getLongValue("i", state).equals(5L);
    }

    /**
     * In this case, modules have, of course, unique namespaces, but the aliases conflict so that is
     * changed in import.
     *
     * @throws Throwable
     */

    public void testImportAndAlias() throws Throwable {
        importAndAliasTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        importAndAliasTest(ROUNDTRIP_XML);
        importAndAliasTest(ROUNDTRIP_QDL);
        importAndAliasTest(ROUNDTRIP_JAVA);
        importAndAliasTest(ROUNDTRIP_JSON);
    }

    public void importAndAliasTest(int testCase) throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','a']body[i:=2;j:=3;list. := -10 + n(5);f(x)->x^2;];");
        addLine(script, "module['b:b','b']body[i:=1;j:=4;list. := -20 + n(5);];");
        addLine(script, "module['a:b','b']body[i:=1;j:=4;list. := n(5);];");
        addLine(script, "i:=0;");
        addLine(script, "j:=5;");
        addLine(script, "list. := n(10);");
        state = rountripState(state, script, testCase);

        addLine(script, "module_import('a:a');");
        addLine(script, "module_import('b:b');");
        addLine(script, "module_import('a:b', 'd');");
        addLine(script, "d := d#list.b#i;");
   //     addLine(script, "e := b#list.d#i;");
        addLine(script, "q := b#list.a#f(1);");


        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getLongValue("d", state).equals(1L) : "value found d== " + getLongValue("d", state) + ", expected 1";
  //      assert getLongValue("e", state).equals(-19L) : "value found e== " + getLongValue("e", state) + ", expected -19";
        assert getLongValue("q", state).equals(-19L) : "value found q== " + getLongValue("q", state) + ", expected -19";
    }

    /**
     * Create a module, then import it to another module. The variables should be resolvable transitively
     * and the states should all be separate.
     *
     * @throws Throwable
     */


  /*
     module['a:/a','a']body[q:=1;];module_import('a:/a');module_import('a:/a','b')
     module['q:/q','w']body[module_import('a:/a');zz:=a#q+2;];
     a#q:=10;b#q:=11
     module_import('q:/q')
     w#a#q := -2
     w#zz
   */

    /*
    Define a function then define variants in modules.  Values are checked to track whether the state
    gets corrupted. Nesting module_import does not work right: -- this fails

        define[f(x)]body[return(x+100);];
        module['a:/t','a']body[define[f(x)]body[return(x+1);];];
        module['q:/z','w']body[module_import('a:/t');define[g(x)]body[return(a#f(x)+3);];];
        module_import('q:/z');
        w#a#f(3)
        w#g(2)
     */


    /**
     * Case of a simple module. The point is that a function f, is defined in the module and that
     * another module function g calls it. What should happen (assuming no other definitions of these
     * in the state, which would throw a namespace exception) is that g uses f and that is
     * that. If the module functions are not being added only to the module state, then there would be errors.
     * Critical simple use case.
     * <pre>
     *     module['a:a','a'][f(x)->x^2;g(x)->f(x+1);];
     *     module_import('a:a');
     *     g(1);
     *  4
     * </pre>
     *
     * @throws Throwable
     */
    public void testModuleFunctionVisibility() throws Throwable {
        moduleFunctionVisibilityTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        moduleFunctionVisibilityTest(ROUNDTRIP_XML);
        moduleFunctionVisibilityTest(ROUNDTRIP_QDL);
        moduleFunctionVisibilityTest(ROUNDTRIP_JAVA);
        moduleFunctionVisibilityTest(ROUNDTRIP_JSON);
    }

    public void moduleFunctionVisibilityTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " module['a:a','a'][f(x)->x^2;g(x)->f(x+1);];");
        addLine(script, "module_import('a:a');");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := is_function(f,1);"); // f didn't end up outside the module
        addLine(script, "ok1 := 4 == g(1);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        // returns true if any elements are true
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok", state);
    }

    /**
     * Same as above, with spaces.
     *
     * @throws Throwable
     */
    public void testModuleFunctionVisibilitySpaces() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " module   \n[\n'a:a',\n'a'\n]   \n\n[\n  f(x)->x^2;g(x)->f(x+1);  \n]   \n\n;");
        addLine(script, "module_import('a:a');");
        addLine(script, "y := g(1);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        // returns true if any elements are true
        assert getLongValue("y", state).equals(4L);
    }

    /**
     * Tests that a function inside a module defined in terms of another inner function is resolved
     * right.
     *
     * @throws Throwable
     */

    public void testFunctionVisibility() throws Throwable {
        functionVisibilityTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        functionVisibilityTest(ROUNDTRIP_XML);
        functionVisibilityTest(ROUNDTRIP_QDL);
        functionVisibilityTest(ROUNDTRIP_JAVA);
        functionVisibilityTest(ROUNDTRIP_JSON);
    }

    public void functionVisibilityTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x)->x;");
        addLine(script, " module['a:a','a'][f(x)->x^2;g(x)->f(x+1);];");
        state = rountripState(state, script, testCase);
        addLine(script, "module_import('a:a');");
        addLine(script, "ok := 16 == g(3);"); // uses f inside the module
        addLine(script, "g3 := g(3);"); // uses f inside the module
        addLine(script, "okf := 2 == f(2);"); // does not effect f outside
        addLine(script, "f2 :=  f(2);"); // does not effect f outside
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "expected g(3)=16, got " + getLongValue("g3", state);
        assert getBooleanValue("okf", state) : "expected f(2)=2, got " + getLongValue("f2", state);
    }
        /*
        module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];
         */

    /**
     * Test that importing two modules keeps the namespaces straight with their state.
     *
     * @throws Throwable
     */
    public void testMultipleModules() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "X#u := 5;X#v := 7;");
        addLine(script, "okxf := 31 == X#f(2,3);"); // uses f inside the module
        addLine(script, "okxg := 12 ==  X#g();"); // uses f inside the module
        addLine(script, "module_import('a:/b','Y');");
        addLine(script, "Y#u := -2; Y#v := -3;");
        addLine(script, "okyg := Y#g() == -5;");
        addLine(script, "okyf := Y#f(3,4) == -18;");
        addLine(script, "okxg2 := X#g() == 12;");
        addLine(script, "okxf2 := X#f(3,4) == 43;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okxf", state);
        assert getBooleanValue("okxg", state);
        assert getBooleanValue("okyf", state);
        assert getBooleanValue("okyg", state);
        assert getBooleanValue("okxf2", state);
        assert getBooleanValue("okxg2", state);
    }

    /**
     * Makes sure that importing different module versions are kept straight for input form,
     * so if <code>X</code> and <code>Y</code> are two versions of the same module. Setting state and
     * querying functions works
     *
     * <pre>
     *
     * </pre>
     *
     * @throws Throwable
     */
    public void testModuleInputForm() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "X#u:=42;");
        addLine(script, "xf0 :=input_form(X#f,2);");
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)/times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','Y');");
        addLine(script, "Y#u :=-7;");
        // show that getting input form of things inside modules works
        addLine(script, "yf :=input_form(Y#f,2);");
        addLine(script, "yu :=input_form(Y#u);");
        addLine(script, "xf :=input_form(X#f,2);");
        addLine(script, "xu :=input_form(X#u);");
        // shows getting input form of entire module works
        addLine(script, "all :=input_form(X);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStringValue("yf", state).contains("times(x,u)/times(y,v)");
        assert getStringValue("xf", state).contains("times(x,u)+times(y,v)");
        assert getStringValue("xu", state).contains("42");
        assert getStringValue("yu", state).contains("-7");
        assert getStringValue("all", state).contains("module['a:/b','X']");

    }

    /**
     * Test that creating a module inside another module works completely locally.
     */
    /*
        module['a:a','A'][module['b:b','B'][u:=2;f(x)->x+1;];module_import('b:b');];
        module_import('a:a');
        -11 =:  A#B#u;
     */
    public void testNestedModule() throws Throwable {
        nestedModuleTEst(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        nestedModuleTEst(ROUNDTRIP_XML);
        nestedModuleTEst(ROUNDTRIP_QDL);
        nestedModuleTEst(ROUNDTRIP_JAVA);
        nestedModuleTEst(ROUNDTRIP_JSON);
    }

    public void nestedModuleTEst(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][module['b:b','B'][u:=2;f(x)->x+1;];module_import('b:b');];");
        state = rountripState(state, script, testCase);
        addLine(script, "module_import('a:a');");
        addLine(script, "-11 =:  A#B#u;");
        addLine(script, "ok := -11 == A#B#u;"); // pull it out of the local state so we can test the value easily.

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "was not able to nest a module definition in another module.";

    }

    boolean testImports = true;

    /*
  module['a:/a','a']body[q:=1;];
  module_import('a:/a');
  module_import('a:/a','b');
  module['q:/q','w']body[module_import('a:/a');zz:=a#q+2;]
  module_import('q:/q');
  a#q:=10;b#q:=11;
  w#a#q
  a#q
  b#q
     */
    public void testNestedVariableImport() throws Throwable {
        nestedVariableImportTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        nestedVariableImportTest(ROUNDTRIP_XML);
        /*
        No QDL test. The issue is that the test checks that importing a module then
        changing a value internal to it is preserved. This implies order (load module
        then alter things) and a dump only spits out the final state of the workspace,
        so such a pattern cannot be preserved..
         */
//        testNestedVariableImport(ROUNDTRIP_QDL);
        nestedVariableImportTest(ROUNDTRIP_JAVA);
        nestedVariableImportTest(ROUNDTRIP_JSON);
    }

    public void nestedVariableImportTest(int testCase) throws Throwable {
        if (!testImports) {
            return;
        }
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/a','a']body[q:=1;];");
        addLine(script, "module_import('a:/a');");
        addLine(script, "module_import('a:/a','b');");
        addLine(script, "module['q:/q','w']body[module_import('a:/a');zz:=a#q+2;];");
        addLine(script, "a#q:=10;");
        addLine(script, "b#q:=11;");
        // Make sure that some of the state has changed to detect state management issues.
        state = rountripState(state, script, testCase);
        addLine(script, "module_import('q:/q');");
        addLine(script, "w#a#q:=3;");
        addLine(script, "waq := w#a#q;");
        addLine(script, "okw := w#a#q==3;");
        addLine(script, "okaq := a#q==10;");
        addLine(script, "aq := a#q;");
        addLine(script, "okbq := b#q==11;");
        addLine(script, "bq := b#q;");


        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okw", state) : "expected w#a#q=3, got " + getLongValue("waq", state);
        assert getBooleanValue("okaq", state) : "expected a#q=10, got " + getLongValue("aq", state);
        assert getBooleanValue("okbq", state) : "expected b#q=11, got " + getLongValue("bq", state);
    }

    /*
      define[f(x)]body[return(x+100);];
  module['a:/t','a']body[define[f(x)]body[return(x+1);];];
  module['q:/z','w']body[module_import('a:/t');define[g(x)]body[return(a#f(x)+3);];];
    module_import('a:/t');
         module_import('q:/z');
       w#g(2)

     */
    public void testNestedFunctionImport() throws Throwable {
        if (!testImports) {
            return;
        }
        StringBuffer script = new StringBuffer();
        addLine(script, "define[f(x)]body[return(x+100);];");
        addLine(script, "module['a:/t','a']body[define[f(x)]body[return(x+1);];];");
        addLine(script, "module['q:/z','w']body[module_import('a:/t');define[g(x)]body[return(a#f(x)+3);];];");
        addLine(script, "test_f:=f(1);");
        addLine(script, "module_import('a:/t');");
        addLine(script, "test_a:=a#f(1);");
        // Make sure that some of the state has changed to detect state management issues.
        addLine(script, "module_import('q:/z');");
        addLine(script, "test_waf := w#a#f(2);");
        addLine(script, "test_wg := w#g(2);");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("test_f", state) == 101L;
        assert getLongValue("test_a", state) == 2L;
        assert getLongValue("test_waf", state) == 3L;
        assert getLongValue("test_wg", state) == 6L;
    }
    /*
       module['a:a','a'][module['b:b','b'][f(x)->x;];module_import('b:b');g(x)->b#f(x^2);];
   module_import('a:a')
a
   a#g(2)
     */

    public void testNestedModule2() throws Throwable {
        if (!testImports) {
            return;
        }
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','a'][module['b:b','b'][f(x)->x;];module_import('b:b');g(x)->b#f(x^2);];");
        addLine(script, "module_import('a:a');");
        addLine(script, "ag2 := a#g(2);");
        addLine(script, "ok := 4 == a#g(2);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "expected a#g(2)=4, got " + getLongValue("ag2", state);
    }


    public void testUnqualifiedVariable() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "ok := u == 2;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "was not able to get unqualified variable name";
    }

    /**
     * Import 2 modules, repeat test. Should get a namespace exception.
     *
     * @throws Throwable
     */
    public void testUnqualifiedVariable2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "module_import('a:/b','Y');");
        addLine(script, "ok := u == 2;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "multiple namespaces with same variable failed unqualified test.";
        } catch (NamespaceException | UnknownSymbolException nsx) {
            assert true;
        }
    }

    public void testUnqualifiedFunction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "ok := f(2,2) == 10;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "was not able to get unqualified function name.";
    }

    public void testUnqualifiedFunction2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "module_import('a:/b','Y');");
        addLine(script, "ok := f(2,2) == 10;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "multiple namespaces with same funtions failed unqualified test.";
        } catch (NamespaceException nsx) {
            assert true;
        }
    }

    /*
      Intrinsic variable tests
     */
    public void testIntrinsicFunction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__f(x,y)->x*y;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "__f(2,2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (QDLExceptionWithTrace iv) {
            bad = !(iv.getCause() instanceof UndefinedFunctionException);
        }
        if (bad) {
            assert false : "was able to access an intrinsic function.";
        }
    }

    public void testIntrinsicFunction2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__f(x,y)->x*y;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "X#__f(2,2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic function.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testIntrinsicFunctionDefine() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__f(x,y)->x*y;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "X#__f(x,y)->x*y;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic function.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testIntrinsicVariable() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__a:=1;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "say(X#__a);"); // FQ
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic variable.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testIntrinsicVariable1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__a:=1;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "say(__a);"); // unqualified
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic variable.";
        } catch (UnknownSymbolException iv) {
            assert true;
        }
    }

    public void testIntrinsicVariableSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__a:=1;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "X#__a := 42;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic variable.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testIntrinsicGetter() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/b','X'][__a:=1;get_a()->__a;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "ok := 1 == X#get_a();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not access getter for private variable.";
    }


    /**
     * Tests that setting a variable on module import from the ambient state works.
     * Old modules inherit state automatically.
     *
     * @throws Throwable
     */
    public void testSetVariableFromGlobal() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "zzz := -2;");
        addLine(script, "module['a:/b','X'][__a:=zzz;get_a()->__a;];");
        addLine(script, "module_import('a:/b','X');");
        addLine(script, "ok := zzz == X#get_a();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not set variable from global state on module import";
    }


    /*
            zzz := -1;
             module['a:/b','X'][__a:=zzz;get_a()->__a;]
             module['a:/b','X'][__a:=1;get_a()->__a;]
  module_import('a:/b','X')
    X#__a  X

cannot access '__a'
         X#get_a()
     */
    /*
    The next set of tests is pretty much the same as the previous set, except that it does done on a QDL module
    loaded from disk. This is because the structure of the State is a little different when loading a module
    (vs. running a module) and is special cased in the VariableState/FunctionState objects. Mostly this is to
    guard against a change that breaks this. Simple, basic and essentail regression checks.
     */
    protected static String testModulePath = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/modules/test.mdl";

    // ML = module_load
    public void testMLIntrinsicFunction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "__f(2,2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (QDLExceptionWithTrace iv) {
            bad = !(iv.getCause() instanceof UndefinedFunctionException);
        }
        if (bad) {
            assert false : "was able to access an intrinsic function.";
        }
    }

    public void testMLIntrinsicFunction2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "X#__f(2,2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic function.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }


    /**
     * Similar to {@link #testSetVariableFromGlobal()} but loading the module from a file.
     *
     * @throws Throwable
     */
    public void testMLSetVariableFromGlobal() throws Throwable {
        testMLSetVariableFromGlobal(ROUNDTRIP_NONE);
        testMLSetVariableFromGlobal(ROUNDTRIP_JSON);
      //  testMLSetVariableFromGlobal(ROUNDTRIP_XML);
        testMLSetVariableFromGlobal(ROUNDTRIP_QDL);
        testMLSetVariableFromGlobal(ROUNDTRIP_JAVA);
    }

    protected void testMLSetVariableFromGlobal(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "zz := -1;");
        addLine(script, "module_import(q,'X');");
        QDLInterpreter interpreter;
        state = rountripState(state, script, testCase);
        addLine(script, "ok := zz == X#get_private();");
        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not set variable from global state on module import";
    }

    public void testMLRemove() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "zz := -1;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "ok := zz == X#get_private();");
        addLine(script, "module_remove('X');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not set variable from global state on module import";
    }

    public void testMLIntrinsicFunctionDefine() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "X#__f(x,y)->x*y;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic function.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testMLIntrinsicVariable() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "say(X#__a);"); // FQ
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic variable.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testMLIntrinsicVariable1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "say(__a);"); // unqualified
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic variable.";
        } catch (UnknownSymbolException iv) {
            assert true;
        }
    }

    public void testMLIntrinsicVariableSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "X#__a := 42;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to access an intrinsic variable.";
        } catch (IntrinsicViolation iv) {
            assert true;
        }
    }

    public void testMLIntrinsicGetter() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "ok := 4 == X#get_private();");
        addLine(script, "X#set_private(-10);");
        addLine(script, "ok1 := -10 == X#get_private();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not access getter for private variable.";
        assert getBooleanValue("ok1", state) : "Could not access getter for private variable.";
    }

    /**
     * Test that defining a function with the same name as a built-in function
     * (here size()) works at the module level.
     *
     * @throws Throwable
     */
    public void testMLBuiltinOverride() throws Throwable {
        if (skipBuiltinOverride) return;

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + testModulePath + "') =: q;");
        addLine(script, "module_import(q,'X');");
        addLine(script, "ok := 42 == X#size();"); // FQ size is a constant
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not override built in funciton locally.";
    }

    public void testMLOverrideBuiltin() throws Throwable {
        if (skipBuiltinOverride) return;

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][size()->42;];");
        addLine(script, "module_import('a:a');");
        addLine(script, "ok := 42 == A#size();");
        addLine(script, "ok1 := 3 == size([;3]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not locally override built in function";
        assert getBooleanValue("ok1", state) : "Could not disambiguate override built in function";
    }

    /**
     * In this test, a like-named module function overrides and uses the system function.
     *
     * @throws Throwable
     */
    public void testMLOverrideBuiltin2() throws Throwable {
        if (skipBuiltinOverride) return;

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // same signature as system function
        addLine(script, "module['a:a','A'][size(x)->stem#size(x)+1;];");
        addLine(script, "module_import('a:a');");
        addLine(script, "ok := 6 == A#size([;5]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not locally override built-in 'size' function";
    }

    boolean skipBuiltinOverride = true;

    String javaTestModule = "org.qdl_lang.extensions.examples.basic.EGModule";
    // Java module tests

    /**
     * Test that in the supplied sample class a fully qualified call to the method and
     * variable works.
     *
     * @throws Throwable
     */
    public void testJavaFQAccessTest() throws Throwable {
        JavaFQAccessTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        JavaFQAccessTest(ROUNDTRIP_JSON);
        JavaFQAccessTest(ROUNDTRIP_XML);
        JavaFQAccessTest(ROUNDTRIP_QDL);
        JavaFQAccessTest(ROUNDTRIP_JAVA);
    }

    protected void JavaFQAccessTest(int testCase) throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module_load('" + javaTestModule + "', 'java') =: q;");
        addLine(script, "module_import(q,'X');");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 'ab' == X#concat('a','b');");
        addLine(script, "ok1 := var_type(X#eg.)==constants().var_type.stem;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not access FQ java module function.";
        assert getBooleanValue("ok1", state) : "Could not access FQ Java moduel variable.";
    }

    /*
      module['a:a','A'][module['b:b','B'][a:=1;f(x)->a*x^2;];module_import('b:b','X');module_import('b:b','Y');X#a:=2;Y#a:=5;g(x)->X#f(x)*Y#f(x);];
      module_import('a:a')
    A
      g(3)
    810
      g(1)
     */

    /**
     * This defines a module and imports two instances of it,then defines a function in terms of these.
     * It is therefore self-contained completely in the outer module.
     *
     * @throws Throwable
     */
    public void testMultipleNestedModules() throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][module['b:b','B'][a:=1;f(x)->a*x^2;];module_import('b:b','X');module_import('b:b','Y');X#a:=2;Y#a:=5;g(x)->X#f(x)*Y#f(x);];");
        addLine(script, "module_import('a:a');");
        addLine(script, "g3 := g(3);"); // g(x) ends up being 10*x^4
        addLine(script, "g1 := g(1);");
        addLine(script, "ok := 810 ==g3;");
        addLine(script, "ok1 := 10 ==g1;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Expected g(3)==810, but got " + getLongValue("g3", state);
        assert getBooleanValue("ok1", state) : "Expected g(1)==10, but got " + getLongValue("g1", state);
    }
/*
   module['a:a','A'][__a := 2*b; f(x)->x*__a;];
 */

    /**
     * Tests that creating a module before a referenced global variable (b) works on import.
     *
     * @throws Throwable
     */
    public void testModuleInitialization() throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][__a := 2*b; f(x)->x*__a;];");
        addLine(script, "b := 5;");
        addLine(script, "module_import('a:a');");
        addLine(script, "f2 := f(2);");
        addLine(script, "ok := 20 == f2;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Expected f(2) == 20, but got " + getLongValue("f2", state);
    }

    public void testModuleInitializationFailure() throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][__a := 2*b; f(x)->x*__a;];");
        addLine(script, "module_import('a:a');");
        addLine(script, "f2 := f(2);");
        addLine(script, "ok := 20 == f2;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "Was able to import a module when there was a missing global variable referenced";
        } catch (UnknownSymbolException unknownSymbolException) {
            assert true;
        }
    }

    /**
     * Tests that passing a fully qualified function name as a reference is resolved right
     *
     * @throws Throwable
     */
    public void testModuleFunctionReference() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][f(x)->x^2;];");
        addLine(script, "module_import('a:a');");
        addLine(script, "h(@g, x)->g(x);");
        addLine(script, "ok := 16 == h(A#@f, 4);");// this actually took a small parser rewrite to fix.
        addLine(script, "ok1 := 16 == h(@f, 4);"); // old modules system allows for unqualified access
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }

    public void testNestedModuleFunctionReference() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "define[f(x)]body[return(x+100);];");
        addLine(script, "module['a:/t','a']body[define[f(x)]body[return(x+1);];];");
        addLine(script, "module['q:/z','w']body[zz:=17;module_import('a:/t');g(x)->a#f(x)+zz;];");
        addLine(script, "module_import('q:/z');");
        addLine(script, "h(@g, x)->g(x);");
        addLine(script, "ok := w#a#f(3) == h(w#a#@f, 3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /*
  define[f(x)]body[return(x+100);];
  module['a:/t','a']body[define[f(x)]body[return(x+1);];];
  module['q:/z','w']body[zz:=17;module_import('a:/t');g(x)->a#f(x)+zz;];
  module_import('q:/z');
  w#a#f(3); // returns 4
  w#g(2); // returns 20
  h(@g, x)->g(x)

  h(@w#g, 2); // == w#g(2)
  h(@w#a#f, 3); // == w#a#f(3)
     */
    public void testNestedModuleFunctionReference2() throws Throwable {
        testNestedModuleFunctionReference2(ROUNDTRIP_NONE);
        testNestedModuleFunctionReference2(ROUNDTRIP_JSON);
        testNestedModuleFunctionReference2(ROUNDTRIP_QDL);
        testNestedModuleFunctionReference2(ROUNDTRIP_JAVA);
        // Nested modules do not work quite right in XML serialization.
 //       testNestedModuleFunctionReference2(ROUNDTRIP_XML);
    }

    /**
     * tests that function references from two different modules with different states are resolved correctly.
     *
     * @throws Throwable
     */
    protected void testNestedModuleFunctionReference2(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "define[f(x)]body[return(x+100);];"); // This does not get evaluated. If it does, there is a state error.
        addLine(script, "module['a:/t','a']body[define[f(x)]body[return(x+1);];];");
        addLine(script, "module['q:/z','w']body[zz:=17;module_import('a:/t');g(x)->a#f(x)+zz;];");
        addLine(script, "module_import('q:/z');");
        QDLInterpreter interpreter;
        state = rountripState(state, script, testCase);
        addLine(script, "h(@g, @f, x)->g(x)*f(x);"); // reuse names in different order to test if they are kept straight
        addLine(script, "ok := w#a#f(3) * w#g(3) == h(w#a#@f, w#@g,3);");
        //addLine(script, "say( w#a#f(3) * w#g(3));say(h(w#a#@f, w#@g,3));say('ok='+ok);");
        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Since modules assume the state of their calling environment, it was possible to have functions
     * change the ambient state without warning. This has been fixed and this is the regression test
     * for it. A variable j is defined, a module is imported with a function that has the variable j in it.
     * The function is run. No change to j should happen.
     * <p>
     * This was accomplished by making a split between the state of lambdas (inherited) and
     * defined functions where the state is precisely what is passed in.
     *
     * @throws Throwable
     */
    public void testModuleVisibility() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "j := 5;");
        addLine(script, "module['a:a','A'][define[n_copy(s, n)][ out. := null;while[for_next(j, [;n])][out.j := s;];return(out.);];];");
        addLine(script, "module_import('a:a');");
        addLine(script, " n_copy(1,10);");
        addLine(script, "ok := 5 == j;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testExtrinsic() throws Throwable {
        extrinsicTest(ROUNDTRIP_NONE); // no serialization
        if(isSerializationTestsOff()) return;
        extrinsicTest(ROUNDTRIP_XML); // make sure serialization of extrinsics is done in XML
        extrinsicTest(ROUNDTRIP_QDL); // make sure serialization of extrinsics is done in QDL
        extrinsicTest(ROUNDTRIP_JAVA); // make sure serialization of extrinsics is done in Java
        extrinsicTest(ROUNDTRIP_JSON); // make sure serialization of extrinsics is done in Java
    }

    protected void extrinsicTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, VariableState.EXTRINSIC_MARKER + "j := 5;");
        addLine(script, "module['a:a','A'][define[f(s)][return(s*" + VariableState.EXTRINSIC_MARKER + "j);];];");
        QDLInterpreter interpreter;
        state = rountripState(state, script, testCase);
        addLine(script, "module_import('a:a');");
        addLine(script, "ok := 50 == A#f(10);");
        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }


    public void testIsFunction() throws Throwable {
        isFunctionTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        isFunctionTest(ROUNDTRIP_XML);
        isFunctionTest(ROUNDTRIP_QDL);
        isFunctionTest(ROUNDTRIP_JAVA);
        isFunctionTest(ROUNDTRIP_JSON);
    }

    protected void isFunctionTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/t','a']body[define[f(x)]body[return(x+1);];];");
        addLine(script, "module['q:/z','w']body[module_import('a:/t');define[g(x)]body[return(a#f(x)+3);];];");
        addLine(script, "module_import('q:/z');");
        QDLInterpreter interpreter;
        state = rountripState(state, script, testCase);
        addLine(script, "ok  := is_function(w#a#f, 1);"); // specific f in module with 1 arg
        addLine(script, "ok0  := is_function(w#a#f);"); // any f a function in the module?
        addLine(script, "ok1  := !is_function(f, 1);"); // specific f in module with 1 arg
        addLine(script, "ok2 := !is_function(w#a#f, 3);"); // wrong arg count
        addLine(script, "ok3:= is_function(w#g, 1);"); // exists
        addLine(script, "ok4:= !is_function(w#g, 2);"); // wrong arg count
        addLine(script, "ok5:= !is_function(foo, 4);"); // does not exist
        addLine(script, "ok6:= !is_function(w#a#foo, 1);"); // does not exist

        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert getBooleanValue("ok4", state);
        assert getBooleanValue("ok5", state);
        assert getBooleanValue("ok6", state);
    }
    /*
       module['a:/a','a'][n(x)->1;];
    module['a:/b','b'][n(x)->2;];

module_import('a:/a');
module_import('a:/b');
a#n(0)
     */

    /**
     * Tests that different modules keep their visibility, so here we import modules with
     * names a and and a like-named function f.
     *
     * @throws Throwable
     */
    public void testModuleVisibility2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,
                "f(x)->3;\n" +
                        "module['a:/c','c'][\n" +
                        "   f(x)->5; \n" +
                        "   module['a:/d','d'][f(x)->7;]; \n" +
                        "   module_import('a:/d','d');\n" +
                        "   ff(x)->f(x)+d#f(x);\n" +
                        " ];\n" +
                        "module_import('a:/c');");
        addLine(script, "ok0 := c#f(100)==5;");
        addLine(script, "ok1 := c#d#f(100)==7;");
        addLine(script, "ok2 := c#ff(100)==12;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
    }

    /**
     * Users should have the ability to create like-named functions for the standard QDL
     * functions in their own modules. So if (as here) n(x) is function, then c#n(x) can
     * be defined and is not confused with the built in. This shows that namespace qualification
     * works as it should and is a critical test.
     *
     * @throws Throwable
     */
    public void testModuleVisibilityBuiltInFunctions() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,
                "module['a:/c','c'][\n" +
                        "   n(x)->5; \n" +
                        "   module['a:/d','d'][n(x)->7;]; \n" +
                        "   module_import('a:/d','d');\n" +
                        "   f(x)->stem#n(x)+d#n(x);\n" + // can't access d#n() until it is imported
                        "   g(x)->stem#n(x)+n(x);\n" + // resolves n to the one in this module
                        "   ];\n" +
                        "module_import('a:/c');");
        addLine(script, "ok0 := reduce(@&&,c#f(3)==[7,8,9]);");
        addLine(script, "ok1 := c#n(1)==5;");
        addLine(script, "ok2 := c#d#n(1)==7;");
        addLine(script, "ok3 := reduce(@&&,c#g(3)==[5,6,7]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
    }

    /**
     * The default namespace should be refenceable. This test checks that indeed the system knows
     * when a system function is over-ridden and flags an unqualified use.
     *
     * @throws Throwable
     */
    public void testDefaultNamespaceFail() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "n(x)->5;");
        addLine(script, "n(5);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "Was able to create a like-named function n for the system and it was not flagged as ambiguous";
        } catch (QDLExceptionWithTrace x) {
            assert x.getMessage().contains("ambiguous");
        }
    }

    /**
     * Basic use case. Create a new function, here like-named with system function n, and reference it
     * explicitly. Simple, but a critical regression test for namespaces.
     *
     * @throws Throwable
     */
    public void testDefaultNamespaceOK() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "n(x)->5;");
        addLine(script, "ok := 5==#n(0);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Show that a like-named function can be referenced unambiguously
     *
     * @throws Throwable
     */
    public void testDefaultNamespaceOK2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "n(x)->5;");
        addLine(script, "ok := reduce(@&&, [5,6,7,8,9]==stem#n(5) + #n(5));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Test is that a module tries to access a function in the default namespace that has not been defined
     * when there is a like-named one in the module. The system should tell the user that the function is
     * not defined.
     *
     * @throws Throwable
     */
    public void testDefaultNamespaceOK3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:/c']\n" +
                "  body[\n" +
                "       n(x)->5; \n" +
                "       module['a:/d'][n(x)->7;]; \n" +
                "       module_import('a:/d','y');\n" +
                "       nn(x)->stem#n(x)+#n(x); // Note this has a problem. We'd like n(x) to refer to c#n(x) but it refers to builtin\n" +
                "      ];\n" +
                "module_import('a:/c', 'x');");
        addLine(script, "x#nn(5);"); // fails since the function references the default NS.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "Undefined function in default namespace should cause error when referencing it";
        } catch (QDLExceptionWithTrace q) {
            assert q.getMessage().contains("no such function");
        }
    }

    public void testDefaultNamespaceOK4() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "n(x)->99;"); // Add a function to the default namespace
        addLine(script, "module['a:/c']\n" +
                "  body[\n" +
                "       n(x)->5; \n" +
                "       module['a:/d'][n(x)->7;]; \n" +
                "       module_import('a:/d','y');\n" +
                "       nn(x)->stem#n(x)+#n(x); // Note this has a problem. We'd like n(x) to refer to c#n(x) but it refers to builtin\n" +
                "      ];\n" +
                "module_import('a:/c', 'x');");
        addLine(script, "x#nn(5);"); // fails since the function references the default NS.
        addLine(script, "ok := reduce(@&&,x#nn(5)==[99,100,101,102,103]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);

    }

    public void testPassingJavaFunctionArgument() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z:='https://foo.bar.com';");
        addLine(script,
                "module[ 'A:X'][http_client := j_load('http');];" +
                        "X:=import('A:X');");
        addLine(script, "X#http_client#host(z);"); // fails since the function references the default NS.
        addLine(script, "ok := z == X#http_client#host();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";

    }
    public void testPassingJavaFunctionArgumentStem() throws Throwable {
          State state = testUtils.getNewState();
          StringBuffer script = new StringBuffer();
          addLine(script, "ini.:={'address':'https://localhost:9443/oauth2/oidc-cm', 'caput':'cm-local:client_id', 'id':'admin:cm-test'};");
          addLine(script,
                  "module[ 'A:X'][http_client := j_load('http');];" +
                          "X:=import('A:X');");
          addLine(script, "X#http_client#host(ini.'address');"); // fails since the function references the default NS.
          addLine(script, "ok := ini.'address' == X#http_client#host();");
          QDLInterpreter interpreter = new QDLInterpreter(null, state);
          interpreter.execute(script.toString());
          assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";

      }
    public void testPassingJavaFunctionArgumentStem2() throws Throwable {
          State state = testUtils.getNewState();
          StringBuffer script = new StringBuffer();
          addLine(script, "ini.:={'address':'https://localhost:9443/oauth2/oidc-cm', 'caput':'cm-local:client_id', 'id':'admin:cm-test'};");
          addLine(script,"q := module_load('org.qdl_lang.extensions.http.QDLHTTPLoader','java');");
          addLine(script, "q := module_import(q);");
          addLine(script, "http#host(ini.'address');"); // fails since the function references the default NS.
          addLine(script, "ok :=  ini.'address' == http#host();");
          QDLInterpreter interpreter = new QDLInterpreter(null, state);
          interpreter.execute(script.toString());
          assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";

      }

    /**
     * Checks if passing along a variable argument to a QDL module function works.
     *
     * @throws Throwable
     */
    public void testPassingFunctionArgument() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module[ 'A:Y', 'Y']\n" +
                "  body[\n" +
                "      module['A:Z','Z'][f(x,y)->x+y;];\n" +
                "       module_import('A:Z');\n" +
                "    ]; //end module\n" +
                "   module_import('A:Y');");
        addLine(script, "z:='foo';"); // make this variable is not used, so no false positives
        addLine(script, "ok := 'foobar' == Y#Z#f(z,'bar');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to QDL sub-module";

    }

}
