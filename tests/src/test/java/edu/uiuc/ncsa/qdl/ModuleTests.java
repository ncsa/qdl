package edu.uiuc.ncsa.qdl;

import edu.uiuc.ncsa.qdl.exceptions.IntrinsicViolation;
import edu.uiuc.ncsa.qdl.exceptions.QDLException;
import edu.uiuc.ncsa.qdl.exceptions.UndefinedFunctionException;
import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.state.State;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/22/23 at  9:08 AM
 */

/*
   ╔══════════════════════════════════════════════════════════════════════════════╗
   ║ Tests for the new module system. These test that the state and other         ║
   ║ machinery works reliably. These are in addition to the old style             ║
   ║ module tests, which *must* work for backwards compatibility.                 ║
   ║                                                                              ║
   ║                                                                              ║
   ║                                                                              ║
   ║                                                                              ║
   ╚══════════════════════════════════════════════════════════════════════════════╝

 */

public class ModuleTests extends AbstractQDLTester {
    /*
          module['a:/c','c'][
         n(x)->1;
         module['a:/d','d'][n(x)->2;];
         module_import('a:/d','d');
         f(x)->n(3)+d#n(5);
         nn(x)->#n(x)+n(x);
         ];
   module_import('a:/c');
   c#n(4); //1
   c#d#n(4); //2
   c#f(4);//1+2 == 3
       */
       /*
      module['a:/t','a']body[define[f(x)]body[return(x+1);];];
      module['q:/z','w']body[module_import('a:/t');define[g(x)]body[return(a#f(x)+3);];];
      module_import('q:/z');
      is_function(w#a#f, 1)
        */
    public void testPassingJavaArguments() throws Throwable {
        testPassingJavaArguments(ROUNDTRIP_NONE);
        testPassingJavaArguments(ROUNDTRIP_JSON);
        //      testPassingJavaArguments(ROUNDTRIP_QDL);
    }

    protected void testPassingJavaArguments(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z:='https://foo.bar.com';");
        addLine(script,
                "module['A:X'][http_client := jload('http');];X:=import('A:X');");
        addLine(script, "X#http_client#host(z);"); // fails since the function references the default NS.
        state = rountripState(state, script, testCase); // tests that state in a module is handled on serialization.

        addLine(script, "ok := z == X#http_client#host();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";
    }

    public void testSerializingJavaArguments() throws Throwable {
        testSerializingJavaArguments(ROUNDTRIP_NONE);
        testSerializingJavaArguments(ROUNDTRIP_JSON);

    }
    protected void testSerializingJavaArguments(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z:='https://foo.bar.com';");
        addLine(script,"X:=jload('http');");
        addLine(script, "X#host(z);"); // fails since the function references the default NS.
        state = rountripState(state, script, testCase); // tests that state in a module is handled on serialization.

        addLine(script, "ok := z == X#host();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";
    }

    public void testBasicLoad() throws Throwable {
        testBasicLoad(ROUNDTRIP_NONE);
        testBasicLoad(ROUNDTRIP_JSON);
        testBasicLoad(ROUNDTRIP_QDL);
        // testBasicLoad(ROUNDTRIP_JAVA);
        // testBasicLoad(ROUNDTRIP_XML);
    }

    public void testBasicLoad(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,
                "module['A:X'][f(x)->x;foo:='bar';];" +
                        "X:=import('A:X');");
        state = rountripState(state, script, testCase); // tests that symbol table handles modules on serialization
        addLine(script, "ok0 :=  X#f(2) == 2;");
        addLine(script, "ok1 :=  X#foo == 'bar';");
        addLine(script, "ok2 := 'A:X' ∈ loaded();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "function call to module failed";
        assert getBooleanValue("ok1", state) : "variable access to module failed";
        assert getBooleanValue("ok2", state) : "loaded() does not return the loaded module";
    }

    /**
     * In this test, a module is loaded and the ambient state has other named objects.
     * the test is that these are kept straight
     *
     * @throws Throwable
     */
    public void testBasicScope() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:X'][f(x)->x;foo:='bar';];");
        addLine(script, "X:=import('A:X');");
        addLine(script, "f(x)->x^2;");
        addLine(script, "foo:='woof';");
        addLine(script, "ok0 :=  X#f(2) == 2;");
        addLine(script, "ok1 :=  X#foo == 'bar';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "function call to module failed";
        assert getBooleanValue("ok1", state) : "variable access to module failed";
    }

    /**
     * this test passes a module in as a variable and executes a method.
     *
     * @throws Throwable
     */
    public void testPassingModule() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:X'][f(x)->x;x:='bar';];");
        addLine(script, "x:=import('A:X');");
        addLine(script, "z(x,foo)->x#f(foo);");
        addLine(script, "ok := 3 == z(x,3);");
        addLine(script, "ok1 := 'bar' == x#x;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "function call passing module as a variable failed";
        assert getBooleanValue("ok1", state) : "function call accessing module variable failed";
    }

    /**
     * Shows that accessing a module inside a module works. Note that by default everything is
     * visible unless hidden as intrinsic.
     *
     * @throws Throwable
     */
    public void testBasicNesting() throws Throwable {
        testBasicNesting(ROUNDTRIP_NONE);
        testBasicNesting(ROUNDTRIP_JSON);
        testBasicNesting(ROUNDTRIP_QDL);
    }
    public void testBasicNesting(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:Y'][module['A:X'][y(y)->y;y:='foo';];z:=import('A:X');];");
        addLine(script, "y:=import('A:Y');");
        state = rountripState(state, script, testCase); // tests that symbol table handles modules on serialization

        addLine(script, "ok := 3 == y#z#y(3);");
        addLine(script, "ok1 := y#z#y == 'foo';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "call accessing nested module function failed";
        assert getBooleanValue("ok1", state) : "call accessing nesting module variable failed";
    }

    /**
     * When modules are loaded there is NO access to the ambient space. They are completely
     * blank slates.
     *
     * @throws Throwable
     */
    public void testBasicVisibility() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "w(z)->z^2;");
        addLine(script, "module['A:Y'][f(x)->w(2*x);];");
        addLine(script, "y:=import('A:Y');");
        addLine(script, "y#f(3);"); // should fail since w is not visibile.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "executing a function out of scope succeeded when it should fail.";
        } catch (QDLException qdlException) {
            assert qdlException.getCause() instanceof UndefinedFunctionException : " visibility test failed for unknown reasons:" + qdlException.getMessage();
        }
    }

    public void testBasicVisibility2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:X'][a:=3;f(x)->a*x;];");
        addLine(script, "z:=import('A:X');");
        addLine(script, "a:=5;");
        addLine(script, "ok := 9 == z#f(3);"); // should fail since w is not visibile.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getBooleanValue("ok", state) : "function call with internal module state failed";
    }

    /**
     * Creating a module with an intrinsic variable should not allow access to the variable.
     *
     * @throws Throwable
     */
    public void testIntrinsic1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:T'][__x:=null;name()->__x;name(y)->block[old:=__x;__x:=y;return(old);];];");
        addLine(script, "t := import('A:T');");
        addLine(script, "t#__x;");
        boolean testOK = true;
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            testOK = false;
        } catch (IntrinsicViolation intrinsicViolation) {
        }
        assert testOK : "accessing an intrinsic variable in a module should fail.";

    }

    /**
     * Basic test of a module with a hidden state variable and mutators. The contract is that
     * name() returns the name, name(new_name) sets the name, returns the old name.
     *
     * @throws Throwable
     */
    public void testIntrinsic2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:T'][__x:=null;name()->__x;name(y)->block[old:=__x;__x:=y;return(old);];];");
        addLine(script, "t := import('A:T');");
        addLine(script, "ok0 := t#name() == null;"); // initial value
        addLine(script, "ok1 := t#name('bob') == null;"); // old value
        addLine(script, "ok2 := t#name('dick') == 'bob';"); // previous value
        addLine(script, "ok3 := t#name() == 'dick';"); // current value
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "call to mutator failed, wrong initial value";
        assert getBooleanValue("ok1", state) : "call to mutator failed, reset return wrong previous value";
        assert getBooleanValue("ok2", state) : "call to mutator failed, reset returned wrong previous value";
        assert getBooleanValue("ok3", state) : "call to mutator failed, query returned wrong current value";
    }

    public void testIntrinsicFunction1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:T'][__f(x)->x^2;];");
        addLine(script, "t := import('A:T');");
        addLine(script, "t#__f(3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean testOK = true;
        try {
            interpreter.execute(script.toString());
            testOK = false;
        } catch (IntrinsicViolation intrinsicViolation) {
        }
        assert testOK : "accessing an intrinsic variable in a module should fail.";
    }

    public void testBasicMonadicApplyForModule() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:y'][f(x)->x^2;];y:=import('a:y');");
        addLine(script, "out.:= ⍺y#@f;");
        addLine(script, "ok := (1 ∈ out.) && (size(out.)==1);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicDyadicApplyForModule1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:y'][f(x)->x^2;];y:=import('a:y');");
        addLine(script, "f(x)->x^3; x:=10;"); // make sure that module state is used right
        addLine(script, "ok := 4 == [2]⍺y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicDyadicApplyForModule2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:y'][f(x)->x^2;];y:=import('a:y');");
        addLine(script, "f(x)->x^3; x:=10;"); // make sure that module state is used right
        addLine(script, "ok := 4 == {'x':2}⍺y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicMonadicApplyForNestedModule() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:x'][module['a:y'][f(x)->x^2+1;f(x,y)->x*y;];y:=import('a:y');];");
        addLine(script, "x:=import('a:x');");
        addLine(script, "out.:= ⍺x#y#@f;");
        addLine(script, "ok := (⊗∧⊙[1,2] ∈ out.) && (size(out.)==2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicDyadicApplyForNestedModule1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:x'][module['a:y'][f(x)->x^2+1;f(x,y)->x*y;];y:=import('a:y');];");
        addLine(script, "x:=import('a:x');");
        addLine(script, "f(x)->x^3;"); // make sure that module state is used right
        addLine(script, "f(x,y)->x/y; y:=11;");
        addLine(script, "okf := 5 == [2]⍺x#y#@f;");
        addLine(script, "okg := 6 == [2,3]⍺x#y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okf", state) : "calling applies to module function failed.";
        assert getBooleanValue("okg", state) : "calling applies to module function failed.";
    }

    public void testBasicDyadicApplyForNestedModule2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:x'][module['a:y'][f(x)->x^2+1;f(x,y)->x*y;];y:=import('a:y');];");
        addLine(script, "x:=import('a:x');");
        addLine(script, "f(x)->x^3;"); // make sure that module state is used right
        addLine(script, "f(x,y)->x/y; y:=11;");
        addLine(script, "okf := 5 == {'x':2}⍺x#y#@f;");
        addLine(script, "okg := 6 == {'y':3,'x':2}⍺x#y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okf", state) : "calling applies to module function failed.";
        assert getBooleanValue("okg", state) : "calling applies to module function failed.";
    }
       /*
          module['a:x'][module['a:y'][f(x)->x;];y:=import('a:y');]
       x:=import('a:x');
       x#y#f(3)
       ⍺x#y#@f
  [3]⍺x#y#@f
        */
}
