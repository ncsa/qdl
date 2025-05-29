package org.qdl_lang;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Dyad;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.*;

import java.math.BigDecimal;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  4:07 PM
 */
public class TestDyadicOperations extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();


    public void testDyadicBDPlus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(new BigDecimal("123.456")));
        ConstantNode right = new ConstantNode(asQDLValue(new BigDecimal("-123.4560000")));
        Dyad dyad = new Dyad(OpEvaluator.PLUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResultType() == Constant.DECIMAL_TYPE;
        assert dyad.getResult().asDecimal().compareTo(BigDecimal.ZERO) == 0;
    }


    public void testDyadicBDMinus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(new BigDecimal("123.456")));
        ConstantNode right = new ConstantNode(asQDLValue(new BigDecimal("123.056")));
        BigDecimal expectedResult = new BigDecimal((".4"));
        Dyad dyad = new Dyad(OpEvaluator.MINUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResultType() == Constant.DECIMAL_TYPE;
        BigDecimal returnedResult = dyad.getResult().asDecimal();
        assert (returnedResult.subtract(expectedResult)).compareTo(BigDecimal.ZERO) == 0;
    }


    public void testDyadicMixedMinus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(new BigDecimal("123.456")));
        ConstantNode right = new ConstantNode(asQDLValue(23L));
        BigDecimal expectedResult = new BigDecimal(("100.456"));
        Dyad dyad = new Dyad(OpEvaluator.MINUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResultType() == Constant.DECIMAL_TYPE;
        BigDecimal returnedResult = dyad.getResult().asDecimal();
        assert (returnedResult.subtract(expectedResult)).compareTo(BigDecimal.ZERO) == 0;
    }


    public void testDyadicMixedPlus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(new BigDecimal("123.456")));
        ConstantNode right = new ConstantNode(asQDLValue(23L));
        BigDecimal expectedResult = new BigDecimal(("146.456"));
        Dyad dyad = new Dyad(OpEvaluator.PLUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResultType() == Constant.DECIMAL_TYPE;
        BigDecimal returnedResult = dyad.getResult().asDecimal();
        assert (returnedResult.subtract(expectedResult)).compareTo(BigDecimal.ZERO) == 0;
    }



    public void testDyadicLongPlus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(1L));
        ConstantNode right = new ConstantNode(asQDLValue(2L));
        Dyad dyad = new Dyad(OpEvaluator.PLUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResult().asLong().equals(3L);
    }


    public void testDyadicLongMinus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(1L));
        ConstantNode right = new ConstantNode(asQDLValue(2L));
        Dyad dyad = new Dyad(OpEvaluator.MINUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResult().asLong().equals(-1L);
    }


    public void testDyadicStringPlus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue("abc"));
        ConstantNode right = new ConstantNode(asQDLValue("def"));
        Dyad dyad = new Dyad(OpEvaluator.PLUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResult().asString().equals("abcdef");
    }


    public void testDyadicStringMinus() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue("abcdef"));
        ConstantNode right = new ConstantNode(asQDLValue("def"));
        Dyad dyad = new Dyad(OpEvaluator.MINUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResult().asString().equals("abc");
    }


    public void testDyadicStringMinus2() throws Exception {
        // A - B for strings. removes *every* occurrence of B found in A
        /// here abcabdeabf - ab = cdef
        ConstantNode left = new ConstantNode(asQDLValue("abcabdeabf"));
        ConstantNode right = new ConstantNode(asQDLValue("ab"));
        Dyad dyad = new Dyad(OpEvaluator.MINUS_VALUE, left, right);
        dyad.evaluate(testUtils.getNewState());
        assert dyad.getResult().asString().equals("cdef");
    }

    /**
     * tests that we can create variable nodes, evaluate them and get back the expected results.
     *
     * @throws Exception
     */

    public void testVariableExpression() throws Exception {
        State state = testUtils.getTestState();
        VStack vStack = state.getVStack();
        // String test

        VThing testValue = (VThing) vStack.get(new XKey("string"));
        VariableNode variableNode = new VariableNode("string");
        variableNode.evaluate(state);
        assert variableNode.getResult().equals(testValue.getVariable());
        assert variableNode.getResultType() == Constant.STRING_TYPE;
        // random string test
        variableNode = new VariableNode("random.0");
        testValue = (VThing) vStack.get(new XKey("random."));
        variableNode.evaluate(state);
        assert variableNode.getResult().equals(testValue.getStemValue().get(0L));
        assert variableNode.getResultType() == Constant.STRING_TYPE;
        // Long-valued test
        variableNode = new VariableNode("long");
        testValue = (VThing) vStack.get(new XKey("long"));
        variableNode.evaluate(state);
        assert variableNode.getResult().asLong().equals(testValue.getLongValue());
        assert variableNode.getResultType() == Constant.LONG_TYPE;
    }


    public void testLongEquality() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(4));
        ConstantNode right = new ConstantNode(asQDLValue(5));
        State state = testUtils.getNewState();
        Dyad dyad = new Dyad(OpEvaluator.EQUALS_VALUE, left, right);
        dyad.evaluate(state);
        assert !dyad.getResult().asBoolean();
        dyad = new Dyad(OpEvaluator.NOT_EQUAL_VALUE, left, right);
        dyad.evaluate(state);
        assert  dyad.getResult().asBoolean();
    }

    public void testLongEquality2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := 4 == 5;");
        addLine(script, "ok1 := 4 != 5;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert !getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
    }

    public void testBDEquality() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(new BigDecimal("4.43000000")));
        ConstantNode right = new ConstantNode(asQDLValue(new BigDecimal("4.43")));
        State state = testUtils.getNewState();
        Dyad dyad = new Dyad(OpEvaluator.EQUALS_VALUE, left, right);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
        dyad = new Dyad(OpEvaluator.NOT_EQUAL_VALUE, left, right);
        dyad.evaluate(state);
        assert !dyad.getResult().asBoolean();
    }

    public void testBDEquality2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := 4.430000 == 4.43;");
        addLine(script, "ok1 := 4.430000 != 4.43;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert !getBooleanValue("ok1", state);
    }

    public void testMixedEquality() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(new BigDecimal("4.000000")));
        ConstantNode right = new ConstantNode(asQDLValue(4L));
        State state = testUtils.getNewState();
        Dyad dyad = new Dyad(OpEvaluator.EQUALS_VALUE, left, right);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
        dyad = new Dyad(OpEvaluator.NOT_EQUAL_VALUE, left, right);
        dyad.evaluate(state);
        assert !dyad.getResult().asBoolean();
    }

    public void testMixedEquality2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := 4.000 == 4;");
        addLine(script, "ok1 := 4.000 != 4;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert !getBooleanValue("ok1", state);
    }

    public void testStringEquality() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue("little bunny foo foo"));
        ConstantNode right = new ConstantNode(asQDLValue("It was a dark and stormy night"));
        State state = testUtils.getNewState();
        Dyad dyad = new Dyad(OpEvaluator.EQUALS_VALUE, left, right);
        dyad.evaluate(state);
        assert !dyad.getResult().asBoolean();
        dyad = new Dyad(OpEvaluator.NOT_EQUAL_VALUE, left, right);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
        // And check that a string is equal to itself. 
        dyad = new Dyad(OpEvaluator.EQUALS_VALUE, left, left);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
    }


    public void testLongComparison() throws Exception {
        ConstantNode left = new ConstantNode(asQDLValue(4));
        ConstantNode right = new ConstantNode(asQDLValue(5));
        State state = testUtils.getNewState();
        // test 4 < 5
        Dyad dyad = new Dyad(OpEvaluator.LESS_THAN_VALUE, left, right);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
        // test 4 > 5
        dyad = new Dyad(OpEvaluator.MORE_THAN_VALUE, left, right);
        dyad.evaluate(state);
        assert ! dyad.getResult().asBoolean();
        // test 4 <= 4
        dyad = new Dyad(OpEvaluator.LESS_THAN_EQUAL_VALUE, left, left);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
        // test 4 >= 4
        dyad = new Dyad(OpEvaluator.MORE_THAN_EQUAL_VALUE, left, left);
        dyad.evaluate(state);
        assert dyad.getResult().asBoolean();
    }

    public void testLongComparison2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := 4 < 5;");
        addLine(script, "ok1 := 4 > 5;");
        addLine(script, "ok2 := 4 <= 4;");
        addLine(script, "ok3 := 4 >= 4;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert !getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
    }

    /**
     * Checks that different values return false from equality. Found a bug where things like
     * <pre>
     *     3.21 == 'a'
     *     false == 0
     * </pre>
     * would throw an illegal argument exceptions rather than return false
     *
     * @throws Throwable
     */
    public void testMixedUnequals() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. := ['a','b','c',  'd','e',   1, 5,3,   4,  5,   true,false,false,true,true, null,null,null, null,'null',0.2,0.2,0.2,  0.2,0.2  ];");
        addLine(script, "y. := ['q', 2,  true,3.4, null,'a',2,true,3.4,null,'a', 2,    true, 3.4,  null,'a',  2,  true, 3.4,null,  'a', 2,   true,3.4,null];");
        addLine(script, "ok := !reduce(@∨, y.==x.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Similar to the previous one, this is needed to check that the contract for not equals
     * was not broken either at some point.
     *
     * @throws Throwable
     */
    public void testMixedUnequals2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. := ['a','b','c',  'd','e',   1, 5,3,   4,  5,   true,false,false,true,true, null,null,null, null,'null',0.2,0.2,0.2,  0.2,0.2  ];");
        addLine(script, "y. := ['q', 2,  true,3.4, null,'a',2,true,3.4,null,'a', 2,    true, 3.4,  null,'a',  2,  true, 3.4,null,  'a', 2,   true,3.4,null];");
        addLine(script, "ok := reduce(@∧, y.!=x.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * As of QDL 1.4.4 allow for chaining inequalities, like a &lt; b &lt; c &lt;==&gt; (a&lt;b) ∧ (b&lt;c)
     * Also allows for (in)equality too, so a &lt; b != c &lt; d &lt;==&gt; (a&lt;b)&&(b!=c)&&(c&lt;d)
     * <br/>
     *     Note that in the code, single cases like a &gt; b are handled independently of
     *     chained cases, so chaining should be tested separately.
     * @throws Throwable
     */
    public void testChainedConstantInequalties() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 2<=3 < 4 <=5;"); //true
        addLine(script, "ok0 := 2<=3 < 5 <=4;"); //false
        addLine(script, "x. := 2<[;5]<5;"); // mixed
        addLine(script, "ok1 := reduce(@&&, x. == [false,false,false,true,true]);");
        addLine(script, "y. := 2<[;5]<2;"); // all false
        addLine(script, "ok2 := reduce(@&&, y. == [false,false,false,false,false]);");
        // Now check for cases of embedded ==
        addLine(script, "ok3 := 2 < 3 == 3 <=4;"); //true
        addLine(script, "ok4 := 2 < 3 == 4 <=4;"); //false
        addLine(script, "z. := [;10]/5<tan(1)<tan(1.1);"); // mixed
        addLine(script, "ok5 := reduce(@&&, z. == [true,true,true,true,true,true,true,true,false,false]);");
        addLine(script, "w. := 1<[;10]/6<2;"); // mixed
        addLine(script, "ok6 := reduce(@&&, w. == [false,false,false,false,false,false,false,true,true,true]);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert !getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert !getBooleanValue("ok4", state);
        assert getBooleanValue("ok5", state);
    }

    public void testChainedVariableInequalties() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a:=2;b:=3;c:=4;d:=5;s.:=[;6];");
        addLine(script, "ok0 := a < b < c;"); //true
        addLine(script, "ok1 := b>a<c;"); //true
        addLine(script, "ok2 := a<b!=c<d;"); //true
        addLine(script, "ok3 := a<b==c<d;"); //false
        addLine(script, "ok4 := b>a>c;"); //false
        addLine(script, "x. := a<s.<c;"); // mixed
        addLine(script, "ok5 := reduce(@&&, x. == [false,false,false,true,false,false]);");
        addLine(script, "y. := b>s.<c;"); // mixed
        addLine(script, "ok6 := reduce(@&&, y. == [true,true,true,false,false,false]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert !getBooleanValue("ok3", state);
        assert !getBooleanValue("ok4", state);
        assert getBooleanValue("ok5", state);
        assert getBooleanValue("ok6", state);

    }

    /**
     * Very simple test that chained expressions are used correctly in a loop.
     * @throws Throwable
     */
    public void testChainedWhile() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "k := 0;");
        addLine(script, "while[0<=k<=3][k++;];");
        addLine(script, "ok := k==4;"); // has to exceed 3 to stop loop
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Very simple test that regexs can be chained. Part of the problem is coming up with a test that isn't
     * just daft/
     * @throws Throwable
     */
    public void testChainedRegex() throws Throwable {
         State state = testUtils.getNewState();
         StringBuffer script = new StringBuffer();
         addLine(script, "ok := 'foo'!= '[a-zA-Z]{3}' =~ 'aBc' == 'aBc';");//silly but true
         addLine(script, "ok1:='[a-zA-Z]{3}' =~ 'aBc' =='p';"); // true for regex, false for ==
         addLine(script, "ok2:='[a-zA-Z]{3}' =~ 'aBcq' =='aBcq';"); // false for regex, true for ==
         QDLInterpreter interpreter = new QDLInterpreter(null, state);
         interpreter.execute(script.toString());
         assert getBooleanValue("ok", state);
         assert !getBooleanValue("ok1", state);
         assert !getBooleanValue("ok2", state);
     }
}
