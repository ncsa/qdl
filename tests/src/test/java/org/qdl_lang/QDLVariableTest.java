package org.qdl_lang;

import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.parsing.QDLParserDriver;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.QDLVariable;
import org.qdl_lang.variables.VTable;
import org.qdl_lang.variables.VThing;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.variables.values.BooleanValue;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * Test that directly test the functioning of variables and state. These typically create and manipulate stacks
 * then check values.
 * <p>Created by Jeff Gaynor<br>
 * on 1/10/20 at  2:43 PM
 */
public class QDLVariableTest extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();
    QDLParserDriver runner;

    /**
     * test that the basic value for basic values get stored right.
     *
     * @throws Exception
     */

    public void testVariables() throws Exception {

        VTable vTable = new VTable();

        vTable.put(new VThing(new XKey("a"), new QDLVariable(12345L)));
        assert ((VThing) vTable.get(new XKey("a"))).getVariable().getQDLValue().asLong().equals(12345L);
        vTable.put(new VThing(new XKey("b"), new QDLVariable(BooleanValue.True)));
        assert ((VThing) vTable.get(new XKey("b"))).getVariable().getQDLValue().asBoolean();
        vTable.put(new VThing(new XKey("c"), new QDLVariable(Boolean.FALSE)));
        assert !((VThing) vTable.get(new XKey("c"))).getVariable().getQDLValue().asBoolean();
        String value = "mairzy((%^998e98nfg98u";
        vTable.put(new VThing(new XKey("e"), new QDLVariable(value)));
        assert ((VThing) vTable.get(new XKey("e"))).getVariable().getQDLValue().equals(value);
    }

    /**
     * Test that stem variables get resolved right, so foo.i resolves to foo.0 if i:=0.
     *
     * @throws Exception
     */

    public void testStemVariables() throws Exception {
        State state = testUtils.getNewState();
        state.setValue("i", asQDLValue(0L));
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "foo.i";
        String value = "abc";
        state.setValue(stem, asQDLValue(value));
        assert state.isDefined("foo.i");
        assert state.isDefined("foo.0");
        assert !state.isDefined("foo.1"); // just in case
        assert state.getValue("foo.0").equals(value);
        assert state.getValue("foo.i").equals(value);
    }

    /**
     * This tests that remove via the state object works.
     *
     * @throws Exception
     */

    public void testRemove() throws Exception {
        State state = testUtils.getNewState();

        state.setValue("i", asQDLValue(0L));
        state.setValue("j", asQDLValue(1L));
        state.setValue("k", asQDLValue(-1L)); // relative index
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "foo.i";
        String value = "abc";
        state.setValue("foo.i", asQDLValue(value));
        state.setValue("foo.j", asQDLValue(value));


        assert state.isDefined(stem);
        state.remove(stem);
        assert !state.isDefined(stem);
        state.setValue("foo.i", asQDLValue(value));
        state.remove("foo.k"); // removes last one
        assert !state.isDefined("foo.j");

        state.remove("i");
        assert !state.isDefined("i");

    }

    /**
     * We allow for multiple stems like a.b.c.d if all the tails resolve. In point of fact
     * we only allow for single stems, so a. exists, but a.b. is disallowed (b. can be a stem, but not an index)
     * This test
     * defines a sequence of stem variables and then tests that they resolve.
     *
     * @throws Exception
     */

    public void testDeepResolution() throws Exception {
        State state = testUtils.getNewState();
        state.setValue("z",   asQDLValue(1L));
        state.setValue("y.1", asQDLValue(2L));
        state.setValue("x.2", asQDLValue(3L));
        state.setValue("w.3", asQDLValue(4L));
        state.setValue("A.4", asQDLValue(5L));
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "A.w.x.y.z";
        Object output = state.getValue(stem);
        // FYI Every integer in QDL is a long!!! so testing against an int fails.
        assert output.equals(5L) : "expected 5 and got " + state.getValue(stem);
        assert state.isDefined(stem);
    }


    public void testDeepResolutionSet() throws Exception {
        State state = testUtils.getNewState();
        state.setValue("z",   asQDLValue("1"));
        state.setValue("y.1", asQDLValue("2"));
        state.setValue("x.2", asQDLValue("3"));
        state.setValue("w.3", asQDLValue("4"));
        state.setValue("A.4", asQDLValue("5"));
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "A.w.x.y.z";
        // so now we set the value.
        state.setValue(stem, asQDLValue(6L));
        Object output = state.getValue(stem);
        // FYI Every integer in QDL is a long!!! so testing against an int fails.
        assert output.equals(6L) : "expected 6 and got " + state.getValue(stem);
        assert state.isDefined(stem);
    }

    /**
     * Acid test for the stack. In this case there is a long stem and each of the variables is
     * placed in a different symbol table, maximizing searching needed.
     *
     * @throws Exception
     */


    /**
     * Regression test after parser updated to use . as bona fide operator (rather than having it folded into
     * the variable). New {@link org.qdl_lang.expressions.ANode2} class did not quite resolve the
     * case of an {@link org.qdl_lang.expressions.ESN2}.
     *
     * @throws Throwable
     */
    public void testIsDefined() throws Throwable {
        isDefinedTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        isDefinedTest(ROUNDTRIP_XML);
        isDefinedTest(ROUNDTRIP_QDL);
        isDefinedTest(ROUNDTRIP_JAVA);
        isDefinedTest(ROUNDTRIP_JSON);
    }

    public void isDefinedTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.epe. := {'a':'b','b':'c'};");
        addLine(script, "b := 'foo';");
        state = rountripState(state, script, testCase);
        addLine(script, "ok0 := ∃a.epe ∧ ∃a.epe.;"); // should handle both cases of trailing . or not
        addLine(script, "ok1 := is_defined(b);"); // most basic test
        addLine(script, "ok2 := !is_defined(a.ZZZ);"); // check that missing elements in stems
        addLine(script, "ok4 := reduce(@&&, [false,false,true]==∃[p,q,b]);"); // check that stems are checked
        addLine(script, "zzz. := ∃{'a':p,'b':q};");
        addLine(script, "ok5:= (!zzz.'a')&&(!zzz.'b');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "is_defined failed for a stem";
        assert getBooleanValue("ok1", state) : "is_defined failed for a scalar variable";
        assert getBooleanValue("ok2", state) : "is_defined failed for missing element in a stem.";
        assert getBooleanValue("ok4", state) : "is_defined failed to check list elements.";
        assert getBooleanValue("ok5", state) : "is_defined failed to check general stem elements.";
    }

    /**
     * If is_defined is called on a non-existent stem variable, an exception should be raised.
     * This allows users to see if they have a scope issue with the variable, rather than
     * a false positive that the variable exists, but something is undefined (especially with stems!).
     * @throws Throwable
     */
    public void isDefinedFailedTest() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "∃a.0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        }catch(UnknownSymbolException unknownSymbolException){
            bad = false;
        }
        assert !bad : "Failed to throw undefined symbol exception for undefined stem symbol";
    }

    /**
     * Case is that there is a stem of stem values. Check the elements of the stem
     * for existence.
     * @throws Throwable
     */
    public void isDefinedOnStemList() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.3 := 0;");
        addLine(script, "ok := ⊗∧⊙(∃[a.0,a.3,a.21]) == [false, true, false];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
            interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "is_defined failed for a stem of stem values";

    }
    /*
    a.3 ≔ 0;
∃Q;
∃[a.0,a.3,a.21]
     */

    public void testIsUndefinedMissingStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok3 := ∄aaa.ZZZ;"); // check that no stem is caught right
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean good = false;
        try {
            interpreter.execute(script.toString());
        }catch(UnknownSymbolException unknownSymbolException){
            good = true;
        }
        assert good : "unknown symbol exception for missing stem in is_defined not thrown.";
    }

    /**
     * Case is that the stem is a bit more complex.
     * @throws Throwable
     */
    public void testIsDefinedTestOnNestedStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.≔[;5];");
        addLine(script, "ok := ⊗∧⊙((∃{'a':{'p':a.0, 'q':a.10}}) == {'a':{'p':true, 'q':false}})`*;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "is_defined failed for a nested stem";
    }

    public void testIsDefinedTestOnNestedList() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.≔[;5];");
        addLine(script, "ok := ⊗∧⊙((∃[[a.0,a.1],a.10]) == [[true,true],false])`*;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "is_defined failed for a nested list";
    }

    /**
     * the contract for sets is that they are treated like funky scalars, to wit, the
     * test returns a scalar boolean if everything is defined/
     * @throws Throwable
     */
    public void testIsDefinedTestOnSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.≔[;5];");
        addLine(script, "ok := ∃{a.0,a.2,{a.0,a.1}};");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "is_defined failed for a set";
    }

    public void testIsDefinedTestOnNestedSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.≔[;5];");
        addLine(script, "ok := ∄{a.0,a.2,{a.0,a.10}};"); // element in subset fails.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "is_defined failed for a nested set";
    }
    public void testIsDefinedTestOnConstants() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer(); //
        addLine(script, "ok := ⊗∧⊙((∃[2,π()/3,[;5]]) ≡ [true,true,true]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "is_defined failed for a list of constants";
    }


    public void testIsFunction() throws Throwable {
        isFunctionTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        isFunctionTest(ROUNDTRIP_XML);
        isFunctionTest(ROUNDTRIP_QDL);
        isFunctionTest(ROUNDTRIP_JAVA);
        isFunctionTest(ROUNDTRIP_JSON);
    }
    public void isFunctionTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x)->x^2;");
        addLine(script, "f(x,y,z)->x+y+z;");
        addLine(script, "g(x,y)->x*y;");
        state = rountripState(state, script, testCase);
        addLine(script, "ok0 := f∃null;");
        addLine(script, "ok1 := f∃1;");
        addLine(script, "ok2 := !(f∃2);");
        addLine(script, "ok3 := f∄2;");
        addLine(script, "ok4 := reduce(@&&, f∃[1,3]);");
        addLine(script, "ok5 := reduce(@&&, [f,g]∃[1,2]);");
        addLine(script, "ok6 := 2 == mask([;5], g∃[;5]).2;"); //
        addLine(script, "ok7 := reduce(@&&, g∄[0,1,4,5]);"); //
        addLine(script, "ok8 := reduce(@&&, {'a':f,'b':g}∃{'a':1,'b':2});"); //
        addLine(script, "ok9 := reduce(@&&, {'a':f,'b':g, 'q':h}∃{'a':1,'b':2,'c':4,'d':7});"); // make sure it does subsetting

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "∃ failed for testing any function existence";
        assert getBooleanValue("ok1", state) : "∃ failed for f with one argument";
        assert getBooleanValue("ok2", state) : "!∃ test failed";
        assert getBooleanValue("ok3", state) : "∄ test failed";
        assert getBooleanValue("ok4", state) : "∃ failed for list of functions and scalar cound.";
        assert getBooleanValue("ok5", state) : "∃ failed for mixed list of functions and arg counts";
        assert getBooleanValue("ok6", state) : "∃ failed for single function and list of arg counts";
        assert getBooleanValue("ok7", state) : "∄ failed for single function and list of arguments";
        assert getBooleanValue("ok8", state) : "∃ failed for stem of functions and arg counts";
        assert getBooleanValue("ok9", state) : "∃ failed subsetting for stem of functions and arg counts";
    }

    /*
          ∃{'a':p,'b':q}
{
 a:false,
 b:false
}
     */
    public void testRemoveVariable() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := 1;");
        addLine(script, "remove(a);");
        addLine(script, "ok := !is_defined(a);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a scalar variable";
    }

    public void testRemoveStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := [;5];");
        addLine(script, "remove(a.);");
        addLine(script, "ok := !is_defined(a.);"); // should handle both cases of trailing . or not
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a stem variable";
    }

    public void testRemoveStemEntry() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "α. := [;5];");
        addLine(script, "remove(α.2);");
        addLine(script, "ok := !is_defined(α.2);"); // should handle both cases of trailing . or not
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a stem entry";
    }

    public void testRemoveStemEntry2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ψ. := n(4,5);");
        addLine(script, "remove(ψ.2.3);");
        addLine(script, "ok := !is_defined(ψ.2.3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a stem entry";
    }

    public void testRemoveVariables() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := 1;");
        addLine(script, "a. := [;6];");
        addLine(script, "remove(a);");
        addLine(script, "Ω0 := !is_defined(a);");
        addLine(script, "Ω1 := is_defined(a.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("Ω0", state) : "Did not remove a scalar variable";
        assert getBooleanValue("Ω1", state) : "removed stem by accident";
    }

    // Next tests are for https://github.com/ncsa/qdl/issues/20
    public void testLocalVariableAssignInFunctions() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "define[h(x)][return(a*x^2);];");
        addLine(script, "ok := 432==h(2*(y:=(a:=3))*2);");
        addLine(script, "ok3 := 3*18^2 == h(3*(2=:y)*(a:=3));");
        addLine(script, "ok1 := !is_defined(a);");
        addLine(script, "ok2 := !is_defined(y);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not correctly resolve assignments in variable list";
        assert getBooleanValue("ok1", state) : "Did not preserve state in assignment";
        assert getBooleanValue("ok2", state) : "Did not preserve state";
        assert getBooleanValue("ok3", state) : "Did not resolve assignment in function arguments";
    }

    /*
    /home/ncsa/dev/ncsa-git/qdl/tests/src/test/resources/arg_visibility_test.qdl
     */
    public void testLocalVariableAssignInScriptRun() throws Throwable {
        State state = testUtils.getNewState();
        String test_script = DebugUtil.getDevPath()+"/qdl/tests/src/test/resources/arg_visibility_test.qdl";
        StringBuffer script = new StringBuffer();
        addLine(script, "ok:= 2 == script_run('" + test_script + "', 3*(2=:a));");
        addLine(script, "ok1 := !is_defined(a);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not correctly resolve assignments in script_load";
        assert getBooleanValue("ok1", state) : "Did not preserve state in assignment";
    }

    /**
     * Visibility test for setting values in the argument list..
     * NOTE that script_load runs the script in exactly the current environment,
     * it does not create its own sub-environment (like a function would).
     * This allows for scripts to inject variables and functions into the caller's
     * state. The block in the script happens AFTER the arguments are evaluated, hence
     * hs no effect on the visibility of the arguments
     *
     * @throws Throwable
     */
    public void testLocalVariableAssignInScriptLoad1() throws Throwable {
        State state = testUtils.getNewState();
        String test_script = DebugUtil.getDevPath()+"/qdl/tests/src/test/resources/arg_visibility_test2.qdl";
        StringBuffer script = new StringBuffer();
        addLine(script, "ok:= 2 == script_load('" + test_script + "', 3*((x:=2)=:a));");
        addLine(script, "ok1 := is_defined(a) ;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not correctly resolve assignments in script_load";
        assert getBooleanValue("ok1", state) : "Did not preserve state in assignment";
    }

    public void testLocalVariableAssignInScriptLoad2() throws Throwable {
        State state = testUtils.getNewState();
        String test_script = DebugUtil.getDevPath()+"/qdl/tests/src/test/resources/arg_visibility_test.qdl";
        StringBuffer script = new StringBuffer();
        addLine(script, "a:=3;");
        addLine(script, "ok:= 2 == script_load('" + test_script + "', 3*((x:=2)=:a));");
        addLine(script, "ok1 := is_defined(a) && a==2;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not correctly resolve assignments in script_load";
        assert getBooleanValue("ok1", state) : "Did not preserve state in assignment";
    }

     /*
       t:='/home/ncsa/dev/ncsa-git/qdl/tests/src/test/resources/arg_visibility_test.qdl'
          script_load(t,3*((x:=2)=:a))
       t2:='/home/ncsa/dev/ncsa-git/qdl/tests/src/test/resources/arg_visibility_test2.qdl'
          script_load(t2,3*((x:=2)=:a))

      */
}
