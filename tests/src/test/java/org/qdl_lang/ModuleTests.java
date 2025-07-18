package org.qdl_lang;

import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.exceptions.IntrinsicViolation;
import org.qdl_lang.exceptions.NamespaceException;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.exceptions.UndefinedFunctionException;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import edu.uiuc.ncsa.security.core.util.DebugUtil;

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
        if(isSerializationTestsOff()) return;
        testPassingJavaArguments(ROUNDTRIP_JSON);
    }

    protected void testPassingJavaArguments(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z:='https://foo.bar.com';");
        addLine(script,
                "module['A:X'][http_client := j_load('http');];X:=import('A:X');");
        addLine(script, "X#http_client#host(z);"); // fails since the function references the default NS.
        state = rountripState(state, script, testCase); // tests that state in a module is handled on serialization.

/*        addLine(script, "say('---------------');");
        addLine(script, "say( X#http_client#host());");
        addLine(script, "say('z=' + z);");*/
        addLine(script, "ok := z == X#http_client#host();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";
    }

    public void testSerializingJavaArguments() throws Throwable {
        testSerializingJavaArguments(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testSerializingJavaArguments(ROUNDTRIP_JSON);

    }

    /**
     * Make sure machinery for j_load works using simple paths. Regression test mostly.
     *
     * @throws Throwable
     */
    public void testJLoadForSystemTools() throws Throwable {
        State state = testUtils.getNewState();
        state.createSystemInfo(null);    // make sure it is populated first!
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := null != j_load('cli');");
        addLine(script, "ok1 := null != j_load('tools.cli');");
        addLine(script, "ok2 := null != j_load(['cli']);");
        addLine(script, "ok3 := null != j_load(['tools','cli']);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed to load standard module using j_load";
        assert getBooleanValue("ok1", state) : "failed to load standard module using j_load";
        assert getBooleanValue("ok2", state) : "failed to load standard module using j_load";
        assert getBooleanValue("ok3", state) : "failed to load standard module using j_load";
    }

    protected void testSerializingJavaArguments(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z:='https://foo.bar.com';");
        addLine(script, "X:=j_load('http');");
        addLine(script, "X#host(z);"); // fails since the function references the default NS.
        state = rountripState(state, script, testCase); // tests that state in a module is handled on serialization.

        addLine(script, "ok := z == X#host();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to Java sub-module, (X#http_client#host() failed.)";
    }

    public void testBasicLoad() throws Throwable {
        BasicLoadTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;

        BasicLoadTest(ROUNDTRIP_JSON);
        BasicLoadTest(ROUNDTRIP_QDL);
        // testBasicLoad(ROUNDTRIP_JAVA);
        // testBasicLoad(ROUNDTRIP_XML);
    }

    public void BasicLoadTest(int testCase) throws Throwable {
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

    /*
          module['a:b'][size(x)->stem#size(x)+1;];
          b≔import('a:b');
          b#size([;5])
     */

    /**
     * In this test, a module creates a function name size which overrides the
     * like-named system function. This is a regression test that such
     * overrides work.
     *
     * @throws Throwable
     */
    public void testOverloadOfSystemFunction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:b'][size(x)->stem#size(x)+1;];");
        addLine(script, "b≔import('a:b');");
        addLine(script, "ok := 6 == b#size([;5]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "override of system function inside module failed";
    }

    /**
     * Attempts to use a non-existent function for a given system namespace should
     * be caught with a NamespaceException
     *
     * @throws Throwable
     */
    public void testBadSystemNamespace() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:b'][size(x)->function#size(x)+1;];"); // function as a NS is reserved, no such size()
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "Using a non-existent function in a system namespace should fail";
        } catch (NamespaceException namespaceException) {
            assert true;
        }

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
        basicNestingTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        basicNestingTest(ROUNDTRIP_JSON);
        basicNestingTest(ROUNDTRIP_QDL);
    }

    public void basicNestingTest(int testCase) throws Throwable {
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
     * Tests that using a java module, setting state, the serializing it preserves that stored state.
     *
     * @throws Throwable
     */
    public void testUseSerialization() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, ModuleEvaluator.JAVA_MODULE_USE + "('http');");
        addLine(script, "host('https://foo.com');");
        state = rountripState(state, script, ROUNDTRIP_JSON); // tests that symbol table handles modules on serialization

        addLine(script, "ok := 'https://foo.com' == host();");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "roundtripping state of a used java module failed";
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
        addLine(script, "y#f(3);"); // should fail since w is not visible.
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
     * Regression test for https://github.com/ncsa/qdl/issues/42
     *
     * @throws Throwable
     */
    public void testIntrinsicStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:T'][__x.:=null;getX()->block[if[__x.==null][__x.:=[;1];];return(__x.);];];");
        addLine(script, "t := import('A:T');");
        addLine(script, "ok:=size(t#getX())==1;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "intrinsic stem not set right";

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
        addLine(script, "out.:= ∂y#@f;");
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
        addLine(script, "ok := 4 == [2]∂y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicDyadicApplyForModule2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:y'][f(x)->x^2;];y:=import('a:y');");
        addLine(script, "f(x)->x^3; x:=10;"); // make sure that module state is used right
        addLine(script, "ok := 4 == {'x':2}∂y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicMonadicApplyForNestedModule() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:x'][module['a:y'][f(x)->x^2+1;f(x,y)->x*y;];y:=import('a:y');];");
        addLine(script, "x:=import('a:x');");
        addLine(script, "out.:= ∂x#y#@f;");
        addLine(script, "ok := (⊗∧⊙[1,2] ∈ out.) && (size(out.)==2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "calling applies to module function failed.";
    }

    public void testBasicDyadicApplyForNestedModule1() throws Throwable {
        basicDyadicApplyForNestedModule1Test(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        basicDyadicApplyForNestedModule1Test(ROUNDTRIP_JSON);
    }

    public void basicDyadicApplyForNestedModule1Test(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:x'][module['a:y'][f(x)->x^2+1;f(x,y)->x*y;];y:=import('a:y');];");
        addLine(script, "x:=import('a:x');");
        addLine(script, "f(x)->x^3;"); // make sure that module state is used right
        addLine(script, "f(x,y)->x/y; y:=11;");
        state = rountripState(state, script, testCase);
        addLine(script, "okf := 5 == [2]∂x#y#@f;");
        addLine(script, "okg := 6 == [2,3]∂x#y#@f;");
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
        addLine(script, "okf := 5 == {'x':2}∂x#y#@f;");
        addLine(script, "okg := 6 == {'y':3,'x':2}∂x#y#@f;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okf", state) : "calling applies to module function failed.";
        assert getBooleanValue("okg", state) : "calling applies to module function failed.";
    }

    public void testDeepNesting() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z:=import(load('" + getSourcePath("qdl/language/src/main/resources/modules/nested.mdl") + "'));");
        state = rountripState(state, script, ROUNDTRIP_JSON);
        // tests that various assignments and calls at various levels in the modules just work.
        addLine(script, "ok0 :=            x#qx==7;");
        addLine(script, "ok1 :=          x#y#qy==11;");
        addLine(script, "ok2 :=        x#y#z#qz==15;");
        addLine(script, "ok3 :=        x#y#f(3)==9;");
        addLine(script, "ok4 :=     x#y#f(x#qx)==49;");
        addLine(script, "ok5 :=   x#y#f(x#y#qy)==121;");
        addLine(script, "ok6 := x#y#f(x#y#z#qz)==225;");
        addLine(script, "ok7 :=   x#y#z#f(x#qx)==343;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed to assign module variable";
        assert getBooleanValue("ok1", state) : "failed to assign sub-module variable";
        assert getBooleanValue("ok2", state) : "failed to assign sub-sub-module variable";
        assert getBooleanValue("ok3", state) : "failed to evaluate module function";
        assert getBooleanValue("ok4", state) : "failed to evaluate module function";
        assert getBooleanValue("ok5", state) : "failed to evaluate module function";
        assert getBooleanValue("ok6", state) : "failed to evaluate module function";
        assert getBooleanValue("ok7", state) : "failed to evaluate module function";

    }

    public void testSharedModuleState() throws Throwable {
        sharedModuleStateTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        sharedModuleStateTest(ROUNDTRIP_JSON);
    }

    public void sharedModuleStateTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x)->x^2;");
        addLine(script, "s:=5;");
        addLine(script, "module['p:q'][g(x)->3+f(x);q:=3*s;];");
        addLine(script, "z:=import('p:q','share');"); // import shared mode.
        addLine(script, "ok0 := z#g(5) == 28;");
        addLine(script, "ok1:= z#q == 15;");
        state = rountripState(state, script, testCase);
        addLine(script, "f(x)->x^3;"); // test is to change the function which is then updated
        addLine(script, "ok2 := z#g(4) == 67;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed to share function definition in module";
        assert getBooleanValue("ok1", state) : "failed to share variable in module";
        assert getBooleanValue("ok2", state) : "failed to share updated function in module with shared state";
    }

    public void testSnapshotModuleState() throws Throwable {
        testSnapshotModuleState(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testSnapshotModuleState(ROUNDTRIP_JSON);
    }

    /**
     * In this mode, the current ambient state is cloned then copied. Changes to it should not be reflected
     *
     * @param testCase
     * @throws Throwable
     */
    public void testSnapshotModuleState(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x)->x^2;");
        addLine(script, "s:=5;");
        addLine(script, "module['p:q'][g(x)->3+f(x);q:=3*s;];");
        addLine(script, "z:=import('p:q','inherit');"); // import inherit mode.
        addLine(script, "ok0 := z#g(5) == 28;");
        addLine(script, "ok1:= z#q == 15;");
        state = rountripState(state, script, testCase);
        addLine(script, "f(x)->x^3;"); // test is to change the function which is NOT updated
        addLine(script, "ok2 := z#g(5) == 28;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed to share function definition in module";
        assert getBooleanValue("ok1", state) : "failed to share variable in module";
        assert getBooleanValue("ok2", state) : "inherit mode for module failed ";
    }

    /**
     * Identical to the like-named old module test, just using import instead of module_import.
     *
     * @throws Throwable
     */
    public void testPassingFunctionArgument() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module[ 'A:Y', 'Y']\n" +
                "  body[\n" +
                "      module['A:Z','Z'][f(x,y)->x+y;];\n" +
                "       Z:=import('A:Z');\n" +
                "    ]; //end module\n" +
                "   Y:=import('A:Y');");
        addLine(script, "z:='foo';"); // make this variable is not used, so no false positives
        addLine(script, "ok := 'foobar' == Y#Z#f(z,'bar');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to pass along function argument to QDL sub-module";
    }

    public void testLocalUseInModule() throws Throwable {
        testLocalUseInModule(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testLocalUseInModule(ROUNDTRIP_JSON);
    }

    /**
     * Tests that a module with an embedded use() keeps the state straight and loads. This also
     * is a test for the load call.
     *
     * @param testCase
     * @throws Throwable
     */
    public void testLocalUseInModule(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "  module['my:/ext/math']\n" +
                "         [load('" + getSourcePath("qdl/language/src/main/resources/modules/math-x.mdl") + "');\n" +
                "          use('qdl:/ext/math');\n" +
                "          versinh(x)→ 2*sinh(x/2)^2; // hyperbolic versine\n" +
                "          haversinh(x)→ versinh(x)/2; // hyperbolic haversine\n" +
                "         ];");
        addLine(script, "h := import('my:/ext/math');"); // make this variable is not used, so no false positives
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 0<h#versinh(1)-0.54308063;"); // trick. Mostly if this runs at all we are ok.
        addLine(script, "ok1 := 0<h#haversinh(1)-0.271;"); // trick. Mostly if this runs at all we are ok.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to use() a loaded QDL module in another module";
        assert getBooleanValue("ok1", state) : "failed to use() a loaded QDL module in another module";

    }

    public void testFunctionReferenceResolution() throws Throwable {
        testFunctionReferenceResolution(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testFunctionReferenceResolution(ROUNDTRIP_JSON);
    }

    public void testFunctionReferenceResolution(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][f(x)->x^2;];");
        addLine(script, "z:=import('a:a');");
        addLine(script, "h(@g, x)->g(x);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 16 == h(z#@f, 4);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to resolve module function reference as function argument";
    }

    /**
     * Similar to testFunctionReferenceResolution, but the module inculdes some state that is
     * used by the function. This verifies that the state is handled correctly. The
     * previous test assued that the function was handled correctly.
     *
     * @throws Throwable
     */
    public void testFunctionReferenceResolution1() throws Throwable {
        testFunctionReferenceResolution1(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testFunctionReferenceResolution1(ROUNDTRIP_JSON);
    }

    public void testFunctionReferenceResolution1(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:a','A'][a:=4;f(x)->a*x^2;];");
        addLine(script, "z:=import('a:a');");
        addLine(script, "h(@g, x)->g(x);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 36 == h(z#@f, 3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to resolve module function reference as function argument";
    }

    /*
    module['a:a','A'][f(x)->x^2;];
      z:=import('a:a')
      h(@g, x)->g(x)
      h(z#@f, 4)
     */
    public void testFunctionReferenceResolution2() throws Throwable {
        testFunctionReferenceResolution2(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testFunctionReferenceResolution2(ROUNDTRIP_JSON);
    }

    /**
     * Descend to an embedded module and get the correct function reference
     *
     * @param testCase
     * @throws Throwable
     */
    public void testFunctionReferenceResolution2(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:b'][module['a:a'][f(x)->x^2;];w:=import('a:a');];");
        addLine(script, "z:=import('a:b');");
        addLine(script, "h(@g, x)->g(x);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 25 == h(z#w#@f, 5);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to resolve module function reference as function argument";
    }

    /**
     * Test where embedded module has state as does ambient emvironment. Module state
     * has to win out.
     *
     * @throws Throwable
     */
    public void testFunctionReferenceResolution3() throws Throwable {
        testFunctionReferenceResolution3(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testFunctionReferenceResolution3(ROUNDTRIP_JSON);
    }

    public void testFunctionReferenceResolution3(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:b'][module['a:a'][a:=4;f(x)->a*x^2;];w:=import('a:a');];");
        addLine(script, "a:=11;");
        addLine(script, "z:=import('a:b');");
        addLine(script, "h(@g, x)->g(x);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 36 == h(z#w#@f, 3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to resolve module function reference as function argument";
    }

    /**
     * Test of case where the embedded module shares the state of the enclosing module.
     *
     * @throws Throwable
     */
    public void testFunctionReferenceResolution4() throws Throwable {
        testFunctionReferenceResolution4(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testFunctionReferenceResolution4(ROUNDTRIP_JSON);
    }

    public void testFunctionReferenceResolution4(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['a:b'][p:=4;module['a:a'][a:=4;f(x)->(p+a)*x^2;];w:=import('a:a','share');];");
        addLine(script, "a:=11;p:=-3;");
        addLine(script, "z:=import('a:b');");
        addLine(script, "h(@g, x)->g(x);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := 72 == h(z#w#@f, 3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to resolve module function reference as function argument";
    }

    public void testGithub45() throws Throwable {
        testGithub45(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testGithub45(ROUNDTRIP_JSON);
        testGithub45(ROUNDTRIP_QDL);
    }

    public void testGithub45λ() throws Throwable {
        testGithub45λ(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testGithub45λ(ROUNDTRIP_JSON);
        testGithub45λ(ROUNDTRIP_QDL);
    }

    /**
     * <h3>Test for https://github.com/ncsa/qdl/issues/45, λ functions</h3>
     * λ functions should have visibility down the hierarchy.  If not, then certain
     * standard patterns, such as creating setters and getters and referencing them inside
     * functions -- necessary for controlling module state -- fail.
     *
     * @param testCase
     * @throws Throwable
     */
    public void testGithub45λ(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,
                "g(x)→1/x; // should be ignored\n" +
                        "    module['a:a'][\n" +
                        "      g(x)→x^2;\n" +
                        "      f0(x)→\n" +
                        "       block[\n" +
                        "           f1(x)→\n" +
                        "            block[\n" +
                        "             f2(x)→\n" +
                        "              block[\n" +
                        "                f3(x)→\n" +
                        "                 block[\n" +
                        "                  return(g(x+1));\n" +
                        "                 ];\n" +
                        "                return(f3(x+1));\n" +
                        "              ]; //end f2\n" +
                        "             return(f2(x+2));\n" +
                        "            ]; //end f1\n" +
                        "           return(f1(x));\n" +
                        "        ];//end f0\n" +
                        "    ];\n" +
                        "    a ≔ import('a:a');"
        );
        state = rountripState(state, script, testCase);
        addLine(script, "ok ≔ 36≡a#f0(2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "regression for GitLab issue 45, module state bug accessing module λ functions";
    }
    /*
    g(x)->1/x; // should be ignored
    module['a:a'][
      g(x)→x^2;
      f0(x)→
      block[
           f1(x)→
           block[
             f2(x)→
             block[
                f3(x)→
                block[
                return(g(x+1));
                ];
                return(f3(x+1));
             ]; //end f2
             return(f2(x+2));
           ]; //end f1
           return(f1(x));
       ];//end f0
    ];
    a:=import('a:a');
    a#f0(2); // returns a#g(6);
     */


    /**
     * <h3>Test for https://github.com/ncsa/qdl/issues/45 variables</h3>
     * Passing along variables defined inside the module to other functions
     * is critical to being able to use them. In this case, nested modules
     * define functions and variables and a top-level call references them.
     * This emulates a common construct of having several modules inside another
     * and managing the state.
     *
     * @param testCase
     * @throws Throwable
     */
    public void testGithub45(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // triply nested module. The key here is that at each level it picks up local
        // state both  in the module and passed in. This has a very complex VStack at
        // evaluation time.
        addLine(script,
                "module['a:c'][\n" +
                        "   module['a:b'][\n" +
                        "      module['a:a'][\n" +
                        "        t(u,v,w,x,y,z)->u+v+w+x+y+z;\n" +
                        "      ]; // end a:a\n" +
                        "     A:= import('a:a');\n" +
                        "     a:='a';\n" +
                        "     b:='b';\n" +
                        "     s(u,v,w,x)->A#t(u,v,w,x,a,b);\n" +
                        "   ]; // end a:b\n" +
                        "  B:=import('a:b');\n" +
                        "  c:='c';\n" +
                        "  r(u,v,w)->B#s(u,v,w,c);\n" +
                        "]; // end a:c\n" +
                        " // pqrst\n" +
                        "C:=import('a:c');\n" +
                        "d:='d';\n" +
                        "e:='e';\n" +
                        "q(u)->C#r(u,d,e);\n" +
                        "zz:='zz';");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := q(zz)=='zzdecab';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "regression for GitLab issue 45, module state bug accessing module variables and functions";
    }

    /*
    module['a:c'][
       module['a:b'][
          module['a:a'][
            t(u,v,w,x,y,z)->u+v+w+x+y+z;
          ]; // end a:a
         A:= import('a:a');
         a:='a';
         b:='b';
         s(u,v,w,x)->A#t(u,v,w,x,a,b);
       ]; // end a:b
      B:=import('a:b');
      c:='c';
      r(u,v,w)->B#s(u,v,w,c);
    ]; // end a:c
     // pqrst
    C:=import('a:c');
    d:='d';
    e:='e';
    q(u)->C#r(u,d,e);
    zz:='zz';
    q(zz);
     */

    /*
    module['a:b'][p:=4;module['a:a'][a:=4;f(x)->(p+a)*x^2;];w:=import('a:a','share');];

      module['a:b'][module['a:a'][f(x)->x^2;];w:=import('a:a');];
      z:=import('a:b');
      h(@g, x)->g(x);
      h(z#w#@f, 5);

    f(x)->x^2
    s:=5;
     module['p:q'][g(x)->3+f(x);q:=3*s;]
  z:=import('p:q','share')
  z:=import('p:q','inherit')
  z#g(5)
  z#q;
     */
       /*
          module['a:x'][module['a:y'][f(x)->x;];y:=import('a:y');]
       x:=import('a:x');
       x#y#f(3)
       ∂x#y#@f
  [3]∂x#y#@f


       module['p:q'][f(x)->x^2;q:=3;]
       z:= import('p:q')
    )save -json -show

    )load -json -compress off /tmp/ws.json


p:='/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/modules/math-x.mdl';
   module['my:/ext/math']
         [load(p);
          use('qdl:/ext/math');
          versinh(x)→ 2*sinh(x/2)^2; // hyperbolic versine
         ];
    h := import('my:/ext/math');
    h#versinh(1/2)

   String devRoot = System.getenv("NCSA_DEV_INPUT");
        if (devRoot == null) {
            throw new IllegalStateException("NCSA_DEV_INPUT variable not set, cannot run test");
        }
        String file = devRoot + "/qdl/language/src/main/resources/modules/nested.mdl";

          module['a:a','A'][a:=4;f(x)->a*x^2;];
  z:=import('a:a')
  h(@g, x)->g(x);
  h(z#@f, 3)
        */
    public static String intrinsicModuleTest = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/modules/intrinsic.mdl";

    public void testIntrinsicModule() throws Throwable {
        testIntrinsicModule(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testIntrinsicModule(ROUNDTRIP_JSON);
        testIntrinsicModule(ROUNDTRIP_JAVA);
       // testExtrinsicModule(ROUNDTRIP_XML);
        // QDL dump does not capture complex module state.
    }

    public void testIntrinsicModule(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "A:=import(load('" + intrinsicModuleTest + "'));");
        addLine(script, "A#setX(11);");
        //   QDLInterpreter interpreter = new QDLInterpreter(null, state);
        //    interpreter.execute(script.toString());

        state = rountripState(state, script, testCase);

        addLine(script, "ok:= 11 == A#getX();");
        addLine(script, "ok1 := 42==$$MY_GLOBAL;");
        addLine(script, "ok2 := 33 == A#f(3);");
        //     interpreter = new QDLInterpreter(null, state);
        //     interpreter.execute(script.toString());

        state = rountripState(state, script, testCase);
        addLine(script, "ok3:=A#q(5)==1;");
        addLine(script, "ok4 := 495 == A#g(3,5);");
        //     interpreter = new QDLInterpreter(null, state);
        //     interpreter.execute(script.toString());
        state = rountripState(state, script, testCase);
        addLine(script, "ok5 := 36 == A#gg(5);"); // test an intrinsic function
        addLine(script, "ok6 := 144 == A#hh(5);"); // test an intrinsic function in an intrinsic function
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Test case(" + testCase + "): mutated value lost in serialization";
        assert getBooleanValue("ok1", state) : "Test case(" + testCase + "): global variable loaded from module lost in serialization";
        assert getBooleanValue("ok2", state) : "Test case(" + testCase + "): internal module state not preserved  in serialization";
        assert getBooleanValue("ok3", state) : "Test case(" + testCase + "): defined function  not preserved  in serialization";
        assert getBooleanValue("ok4", state) : "Test case(" + testCase + "): nested function state for g not preserved  in serialization";
        assert getBooleanValue("ok5", state) : "Test case(" + testCase + "): nested function state for gg not preserved  in serialization";
        assert getBooleanValue("ok6", state) : "Test case(" + testCase + "): nested function state for hh not preserved  in serialization";

    }

    public static String extrinsicModuleTest = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/modules/extrinsic.mdl";

    public void testExtrinsicModule() throws Throwable {
        testExtrinsicModule(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testExtrinsicModule(ROUNDTRIP_JSON);
        testExtrinsicModule(ROUNDTRIP_JAVA);
        //testExtrinsicModule(ROUNDTRIP_XML);
    }

    public void testExtrinsicModule(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        state.getExtrinsicFuncs().clear();
        state.getExtrinsicVars().clear();
        StringBuffer script = new StringBuffer();
        addLine(script, "A:=load('" + extrinsicModuleTest + "');");
        addLine(script, "ok1 := $$NEWTON∃3;");
        QDLInterpreter interpreter;
        if (testCase == ROUNDTRIP_NONE) {
            interpreter = new QDLInterpreter(null, state);
            interpreter.execute(script.toString());
            script = new StringBuffer();
        }
        state = rountripState(state, script, testCase);
        addLine(script, "ok3 := $$E∃1;");
        addLine(script, "ok4 := $$service_locator∄1;"); // checks that module defined extrinsic has not been evaluated somehow.
        if (testCase == ROUNDTRIP_NONE) {
            // none means that an interpreter is not called, and script is not
            // // cleared, hence partial results cannot be tracked. Since part of
            // the test is watching what is in the state after load then import,
            // we have to do it manually.
            interpreter = new QDLInterpreter(null, state);
            interpreter.execute(script.toString());
            script = new StringBuffer();
        }

        state = rountripState(state, script, testCase);
        addLine(script, "X:=import('test:extrinsic');");
        addLine(script, "ok5 := ∃$$E;");
        addLine(script, "ok6 := 8 == $$service_locator(2);");
        if (testCase == ROUNDTRIP_NONE) {
            interpreter = new QDLInterpreter(null, state);
            interpreter.execute(script.toString());
            script = new StringBuffer();
        }

        state = rountripState(state, script, testCase);
        addLine(script, "ok0 := ∃$$G;");
        addLine(script, "ok2 := ∃$$C;");
        addLine(script, "ok7 := 8 == $$service_locator(2);");
        addLine(script, "ok8 := 2 == ⌊100*$$connection_pool($$C, $$G);"); // cheap trick
        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "test case(" + testCase + "): $$G not found";
        assert getBooleanValue("ok1", state) : "test case(" + testCase + "): $$NEWTON([1]) not found";
        assert getBooleanValue("ok2", state) : "test case(" + testCase + "): $$C not found";
        assert getBooleanValue("ok3", state) : "test case(" + testCase + "): $$E([1]) not found";
        assert getBooleanValue("ok4", state) : "test case(" + testCase + "): $$service_locator([1]) (in module) was created on load. Should not have been.";
        assert getBooleanValue("ok5", state) : "test case(" + testCase + "): $$E (in module) failed on import. Should have been created.";
        assert getBooleanValue("ok6", state) : "test case(" + testCase + "): $$service_locator([1]) (in module) failed on import. Should have been created.";
        assert getBooleanValue("ok7", state) : "test case(" + testCase + "): $$service_locator([1]) failed to evaluate correctly.";
        assert getBooleanValue("ok8", state) : "test case(" + testCase + "): $$connection_pool([2]) failed to evaluate correctly.";

    }

    /**
     * In this test, a script that has functions, loops and conditionals invokes a module.
     * This is a regression test for the state. It does fetch some pages from CILogon, so
     * if that goes away, then this will have to be rewritten
     * @throws Throwable
     */
    public void testGitHub89() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := script_run('" + DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/github89_test.qdl');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "github issue 89 test script failed";
    }

    // Fix https://github.com/ncsa/qdl/issues/132
    /* This requires the module be used and some state checked.
module['A:A'][
 define[f(x.)]
 [
   if[∃x.'a'][return(1);]else[return(0);];
 ];
];

z ≔ import('A:A');
q.'b':=3;
z#f(q.); // returns 0

q.'a':=2;
say(z#f(q.)); // returns 1;

remove(q.'a');
say(z#f(q.)); // should return 0
     */
    public void testIsDefinedInModuleFunction_Github132() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "module['A:A'][define[f(x.)][if[∃x.'a'][return(1);]else[return(0);];];];");
        addLine(script, "z ≔ import('A:A');q.'b':=3;");
        addLine(script, "ok0 ≔ 0≡ z#f(q.);");
        addLine(script, "q.'a':=2;\n");
        addLine(script, "ok1 ≔ 1 ≡ z#f(q.);");
        addLine(script, "remove(q.'a');");
        addLine(script, "ok2 ≔ 0≡ z#f(q.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "testing nested is_defined in a module failed";
        assert getBooleanValue("ok1", state) : "testing nested is_defined in a module failed";
        assert getBooleanValue("ok2", state) : "testing nested is_defined in a module failed";
    }
}
