package edu.uiuc.ncsa.qdl;

import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.parsing.QDLParserDriver;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.variables.VTable;
import edu.uiuc.ncsa.qdl.variables.VThing;

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
     * test that the basic value for basic types get stored right.
     *
     * @throws Exception
     */
     
    public void testVariables() throws Exception {

        VTable vTable = new VTable();

        vTable.put(new VThing(new XKey("a"), 12345L));
        assert ((VThing)vTable.get(new XKey("a"))).getValue().equals(12345L);
        vTable.put(new VThing(new XKey("b"), Boolean.TRUE));
        assert ((VThing)vTable.get(new XKey("b"))).getValue() == Boolean.TRUE;
        vTable.put(new VThing(new XKey("c"), Boolean.FALSE));
        assert ((VThing)vTable.get(new XKey("c"))).getValue() == Boolean.FALSE;
        String value = "mairzy((%^998e98nfg98u";
        vTable.put(new VThing(new XKey("e"),  value ));
        assert ((VThing)vTable.get(new XKey("e"))).getValue().equals(value);
    }

    /**
     * Test that stem variables get resolved right, so foo.i resolves to foo.0 if i:=0.
     *
     * @throws Exception
     */
     
    public void testStemVariables() throws Exception {
        State state = testUtils.getNewState();
        state.setValue("i", 0L);
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "foo.i";
        String value = "abc";
        state.setValue(stem, value);
        assert state.isDefined("foo.i");
        assert state.isDefined("foo.0");
        assert !state.isDefined("foo.1"); // just in case
        assert state.getValue("foo.0").equals(value);
        assert state.getValue("foo.i").equals(value);
    }

    /**
     * This tests that remove via the state object works.
     * @throws Exception
     */
     
    public void testRemove() throws Exception {
        State state = testUtils.getNewState();

        state.setValue("i", 0L);
        state.setValue("j", 1L);
        state.setValue("k", -1L); // relative index
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "foo.i";
        String value = "abc";
        state.setValue("foo.i", value);
        state.setValue("foo.j", value);


        assert state.isDefined(stem);
        state.remove(stem);
        assert !state.isDefined(stem);
        state.setValue("foo.i", value);
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
        state.setValue("z",   1L);
        state.setValue("y.1", 2L);
        state.setValue("x.2", 3L);
        state.setValue("w.3", 4L);
        state.setValue("A.4", 5L);
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "A.w.x.y.z";
        Object output = state.getValue(stem);
        // FYI Every integer in QDL is a long!!! so testing against an int fails.
        assert output.equals(5L) : "expected 5 and got " + state.getValue(stem);
        assert state.isDefined(stem);
    }

     
    public void testDeepResolutionSet() throws Exception {
        State state = testUtils.getNewState();
        state.setValue("z", "1");
        state.setValue("y.1", "2");
        state.setValue("x.2", "3");
        state.setValue("w.3", "4");
        state.setValue("A.4", "5");
        // first test, i = 0, so foo.i should resolve to foo.0
        String stem = "A.w.x.y.z";
        // so now we set the value.
        state.setValue(stem, 6L);
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
     * the variable). New {@link edu.uiuc.ncsa.qdl.expressions.ANode2} class did not quite resolve the
     * case of an {@link edu.uiuc.ncsa.qdl.expressions.ESN2}.
     * @throws Throwable
     */
    public void testIsDefined() throws Throwable{
        testIsDefined(false);
        testIsDefined(true);
    }
    public void testIsDefined(boolean testXML) throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.epe. := {'a':'b','b':'c'};");
        addLine(script, "b := 'foo';");
        if(testXML){
            state= roundTripXMLSerialization(state,script);
            script = new StringBuffer();
        }
        addLine(script, "ok0 := is_defined(a.epe) && is_defined(a.epe.);"); // should handle both cases of trailing . or not
        addLine(script, "ok1 := is_defined(b);"); // most basic test
        addLine(script, "ok2 := !is_defined(a.ZZZ);"); // check that missing elements in stems
        addLine(script, "ok3 := !is_defined(aaa.ZZZ);"); // check that no stem is caught right
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "is_defined failed for a stem";
        assert getBooleanValue("ok1", state) : "is_defined failed for a scalar variable";
        assert getBooleanValue("ok2", state) : "is_defined failed for missing element in a stem.";
        assert getBooleanValue("ok3", state) : "is_defined failed for non-existent stem.";
    }
    
    public void testRemoveVariable() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := 1;");
        addLine(script, "remove(a);");
        addLine(script, "ok := !is_defined(a);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a scalar variable";
    }
    public void testRemoveStem() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := [;5];");
        addLine(script, "remove(a.);");
        addLine(script, "ok := !is_defined(a.);"); // should handle both cases of trailing . or not
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a stem variable";
    }

    public void testRemoveStemEntry() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "α. := [;5];");
        addLine(script, "remove(α.2);");
        addLine(script, "ok := !is_defined(α.2);"); // should handle both cases of trailing . or not
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a stem entry";
    }

    public void testRemoveStemEntry2() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ψ. := n(4,5);");
        addLine(script, "remove(ψ.2.3);");
        addLine(script, "ok := !is_defined(ψ.2.3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not remove a stem entry";
    }
    public void testRemoveVariables() throws Throwable{
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

}
