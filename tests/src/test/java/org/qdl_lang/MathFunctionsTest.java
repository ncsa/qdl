package org.qdl_lang;

import org.qdl_lang.evaluate.MathEvaluator;
import org.qdl_lang.exceptions.ParsingException;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.*;

import java.math.BigDecimal;

import static org.qdl_lang.variables.StemUtility.put;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/17/20 at  10:51 AM
 */
public class MathFunctionsTest extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();


    public void testAbsoluteValue() throws Exception {
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(MathEvaluator.ABS_VALUE);
        ConstantNode left = new ConstantNode(asQDLValue( -5L));
        polyad.addArgument(left);
        polyad.evaluate(state);
        assert polyad.getResult().asLong() == 5L;
    }

    /**
     * Manually insert a stem into the symbol table and test that it can indeed be fully recovered.
     *
     * @throws Exception
     */
    public void testAbsoluteValueStem() throws Exception {

        State state = testUtils.getNewState();

        VStack symbolTable = state.getVStack();
        QDLStem arg = new QDLStem();
        put(arg, "0", -12345L);
        put(arg, "1", 2468L);
        put(arg, "2", -1000000L);
        put(arg, "3", 987654321L);

        symbolTable.put(new VThing(new XKey("arg."), new QDLVariable(arg)));
        VariableNode argNode = new VariableNode("arg.");
        Polyad polyad = new Polyad(MathEvaluator.ABS_VALUE);
        polyad.addArgument(argNode);
        polyad.evaluate(state);
        QDLStem r = polyad.getResult().asStem();
        assert r.size() == 4;
        assert r.getLong(0L).equals(12345L);
        assert r.getLong(1L).equals(2468L);
        assert r.getLong("2").equals(1000000L);
        assert r.getLong("3").equals(987654321L);
    }


    public void testRandomValue() throws Exception {
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(MathEvaluator.RANDOM);
        polyad.evaluate(state);
        assert polyad.getResult().isLong();
    }


    public void testRandomValueWithArg() throws Exception {
        long count = 5L;
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(MathEvaluator.RANDOM);
        ConstantNode left = new ConstantNode(asQDLValue(count));
        polyad.addArgument(left);
        polyad.evaluate(state);
        QDLStem r = polyad.getResult().asStem();
        assert r.size() == count;
    }


    public void testRandomString() throws Exception {
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(MathEvaluator.RANDOM_STRING);
        polyad.evaluate(state);
        assert polyad.getResult().isString();
    }


    public void testRandomStringWithArg() throws Exception {
        State state = testUtils.getNewState();

        Long size = 32L;
        Polyad polyad = new Polyad(MathEvaluator.RANDOM_STRING);
        ConstantNode arg = new ConstantNode(asQDLValue(size));
        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().isString();
        assert polyad.getResult().toString().length() == 43;
    }


    public void testHash() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := hash(3*'woof')=='f6bd2bb62f3f93165d8fc903a286f2eb21267b87';");
        addLine(script, "ok1 := hash('The quick brown fox jumps over the lazy dog','sha-1')=='2fd4e1c67a2d28fced849ee1bb76e7391b93eb12';");
        addLine(script, "ok2 := hash(3*'woof','md2')=='e2a822e69c905a067d3351635927ba29';");
        addLine(script, "ok3 := hash(3*'woof','md5')=='0417584e92d7419ada979f982149507f';");
        addLine(script, "ok4 := hash(3*'woof','sha-1')=='f6bd2bb62f3f93165d8fc903a286f2eb21267b87';");
        addLine(script, "ok5 := hash(3*'woof','sha-2')=='ac1303206238621acf00a17e363a48d7ae3b30deb2d639520c9ae5bd38747a6c';");
        addLine(script, "ok6 := hash(3*'woof','sha-256')=='ac1303206238621acf00a17e363a48d7ae3b30deb2d639520c9ae5bd38747a6c';");
        addLine(script, "ok7 := hash(3*'woof','sha-384')=='86c77cb23bd700db8e98427e36253bc9c7d1f2be8446beb257ad549f15fe935dc776de421f34e80b675e5c51cb605b45';");
        addLine(script, "ok8 := hash(3*'woof','sha-512')=='0612369c638261e0576b625290a6d8bfb53672b978f0a3920291767212364c0a10cd3cb1ec6c711ca35833b24a66ac11450966070769fe10e65964dffe912210';");
        addLine(script, "ok9 := ⊗∧⊙['19b58543c85b97c5498edfd89c11c3aa8cb5fe51','3da541559918a808c2402bba5012f6c60b27661c']∈hash({'qwert','asdf'});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed hash for default";
        assert getBooleanValue("ok1", state) : "failed hash for sha-1";
        assert getBooleanValue("ok2", state) : "failed hash for md2";
        assert getBooleanValue("ok3", state) : "failed hash for md5";
        assert getBooleanValue("ok4", state) : "failed hash for sha-1, test #2";
        assert getBooleanValue("ok5", state) : "failed hash for sha-2";
        assert getBooleanValue("ok6", state) : "failed hash for sha-256";
        assert getBooleanValue("ok7", state) : "failed hash for sha-384";
        assert getBooleanValue("ok8", state) : "failed hash for sha-512";
        assert getBooleanValue("ok9", state) : "failed to hash sets";
    }


    public void testHashStem() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"0", "One Ring to rule them all");
        put(sourceStem,"1", "One Ring to find them");
        put(sourceStem,"2", "One Ring to bring them all");
        put(sourceStem,"3", "and in the darkness bind them");

        QDLStem expected = new QDLStem();
        put(expected,"0", "40d006b6b2e8bea7bf3e9aad679e13b5246f87a2");
        put(expected,"1", "15ac553ccb88f34f3c2a4f9ac0460d0fde29c8a8");
        put(expected,"2", "5fd8b1f8f66de0848eca4dfc468fc15e147e4670");
        put(expected,"3", "830b0c398047d0d3ac4834508eb1bb87ea7f9ba9");
        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        VariableNode arg = new VariableNode("sourceStem.");
        Polyad polyad = new Polyad(MathEvaluator.HASH);
        polyad.addArgument(arg);
        polyad.evaluate(state);
        QDLStem result = polyad.getResult().asStem();
        assert result.size() == 4;
        for (Object key : result.keySet()) {
            assert result.get(key).equals(expected.get(key));
        }
    }
         /*
         '<woof"ወንጌል  \n6%"$' == decode(encode('<woof"ወንጌል  \n6%"$',constants().codecs.string.), constants().codecs.int.)

          */

    /**
     * Check that the full set of encode and decode works. Note that we have to special case XSI
     * since that does not preserve linefeeds, all the others actuall allow for round-tripping.
     *
     * @throws Exception
     */
    public void testCodecStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. := encode('<woof\"ወንጌል  \\n6%\"$',constants().codecs.string.);");
        addLine(script, "check_xsi := x.'xsi'== '\\\\<woof\\\\\"ወንጌል\\\\ \\\\ 6\\\\%\\\\\"\\\\$';"); // lotsa slashes...
        addLine(script, "remove(x.'xsi');");
        // check that decoding with the integer codes works, so that the keys have not gotten out of whack.
        addLine(script,"decoded. := decode(x., constants().codecs.int) == '<woof\"ወንጌል  \\n6%\"$';");
        addLine(script,"check_size := size(decoded.)+1 == size(constants().codecs.int);"); // checks that no subsetting happened and we lost some
        addLine(script, "ok := ⊗∧⊙decoded.;"); // actual check.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "roundtrip with encode/decode did not work";
        assert getBooleanValue("check_size", state) : "roundtrip with encode/decode missing some entries. Check that constants().codecs are in syncs";
        assert getBooleanValue("check_xsi", state) : "round trip with XSI failed";
    }


    public void testCodecSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'PHdvb2YiIOGLiOGKleGMjOGIjSAKNiUk'∈encode({'<woof\" ወንጌል \\n6%$'});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "encode on sets fails.";
    }
    public void testB64Encode() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZw'==encode('The quick brown fox jumps over the lazy dog');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "base 64 encode fails.";
    }


    public void testB64Decode() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'The quick brown fox jumps over the lazy dog' ==decode('VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZw');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "base 64 decode fails.";
    }


    public void testToHex() throws Exception {
        State state = testUtils.getNewState();

        String original = "The quick brown fox jumps over the lazy dog";
        String expectedResult = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67";
        Polyad polyad = new Polyad(MathEvaluator.ENCODE);
        ConstantNode arg = new ConstantNode(asQDLValue(original));
        ConstantNode arg1 = new ConstantNode(asQDLValue(16));
        polyad.addArgument(arg);
        polyad.addArgument(arg1);
        polyad.evaluate(state);
        assert polyad.getResult().getValue().equals(expectedResult);
    }


    public void testFromHex() throws Exception {
        State state = testUtils.getNewState();

        String expectedResult = "The quick brown fox jumps over the lazy dog";
        String original = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67";
        Polyad polyad = new Polyad(MathEvaluator.DECODE);
        ConstantNode arg = new ConstantNode(asQDLValue(original));
        ConstantNode arg1 = new ConstantNode(asQDLValue(16));
        polyad.addArgument(arg);
        polyad.addArgument(arg1);
        polyad.evaluate(state);
        assert polyad.getResult().asString().equals(expectedResult);
    }

    /**
     * There was a bug in the parser that leading minus signs were not handled correctly.
     * This is because tutorials on writing parsers gave some bad advice (having unary minus
     * outrank other operators rather than being in their normal place in the hierarchy). This test
     * shows that is fixed. Note that decimals are tested here too in that 0.3 (leading zero mandatory)
     * and such are used.
     *
     * @throws Exception
     */

    public void testSignedNumbers() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.0 := -1;");
        addLine(script, "a.1 := -2^2;");
        addLine(script, "a.2 := -2^3;");
        addLine(script, "a.30 := -0.1;");
        addLine(script, "a.3 := -0.1^2;");
        addLine(script, "a.4 := -0.1^3;");
        addLine(script, "a.5 := -3*4;");
        addLine(script, "a.6 := -3*(-4);");
        addLine(script, "a.7 := 3*(-4);");
        addLine(script, "a.8 := (3)*(4);");
        addLine(script, "a.9 := -0.3*(-4);");
        addLine(script, "a.10 := 0-0.3*(-4);");
        addLine(script, "a.11 := 0.3*(4);");
        addLine(script, "a.12 := -3*(-0.4);");
        addLine(script, "a.13 := 0-3*(-0.4);");
        addLine(script, "a.14 := 3*(-0.4);");
        addLine(script, "a.15 := -0.3*(-0.4);");
        addLine(script, "a.16 := 0.3*(-0.4);");
        addLine(script, "a.17 := 0-0.3*(-0.4);");
        addLine(script, "b. := -3 + n(6);"); // {-3, -2, -1, 0, 1, 2}
        addLine(script, "c. := -b.;"); // {3, 2, 1, 0, -1, -2}
        addLine(script, "d. := -b.^2;"); // {-9, -4, -1, 0, -1, -4}
        addLine(script, "e. := -b.^3;"); // {27, 8, 1, 0, -1, -8}
        addLine(script, "f. := -3*b.;"); // {9, 6, 3, 0, -3, -6}
        addLine(script, "g. := -0.3*b.;"); // {.9, .6, .3, 0.0, -.3, -.6}
        addLine(script, "h. := -0.3 + b.;"); // {-3.3, -2.3, -1.3, -0.3, .7, 1.7}
        addLine(script, "i. := -0.3 - b.;"); // {2.7, 1.7, .7, -.3, -1.3, -2.3}


        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("a.0", state) == -1L;
        assert getLongValue("a.1", state) == -4L;
        assert getLongValue("a.2", state) == -8L;

        assert areEqual(getBDValue("a.30", state), new BigDecimal("-.1"));
        assert areEqual(getBDValue("a.3", state), new BigDecimal("-.01"));
        assert areEqual(getBDValue("a.4", state), new BigDecimal("-.001"));

        assert getLongValue("a.5", state) == -12L;
        assert getLongValue("a.6", state) == 12L;
        assert getLongValue("a.7", state) == -12L;
        assert getLongValue("a.8", state) == 12L;

        assert areEqual(getBDValue("a.9", state), new BigDecimal("1.2"));
        assert areEqual(getBDValue("a.10", state), new BigDecimal("1.2"));
        assert areEqual(getBDValue("a.11", state), new BigDecimal("1.2"));
        assert areEqual(getBDValue("a.12", state), new BigDecimal("1.2"));

        assert areEqual(getBDValue("a.13", state), new BigDecimal("1.2"));
        assert areEqual(getBDValue("a.14", state), new BigDecimal("-1.2"));
        assert areEqual(getBDValue("a.15", state), new BigDecimal(".12"));
        assert areEqual(getBDValue("a.16", state), new BigDecimal("-.12"));
        assert areEqual(getBDValue("a.17", state), new BigDecimal(".12"));

        assert areEqual(getStemValue("b.", state), arrayToStem(new int[]{-3, -2, -1, 0, 1, 2}));
        assert areEqual(getStemValue("c.", state), arrayToStem(new int[]{3, 2, 1, 0, -1, -2}));
        assert areEqual(getStemValue("d.", state), arrayToStem(new int[]{-9, -4, -1, 0, -1, -4}));
        assert areEqual(getStemValue("e.", state), arrayToStem(new int[]{27, 8, 1, 0, -1, -8}));
        assert areEqual(getStemValue("f.", state), arrayToStem(new int[]{9, 6, 3, 0, -3, -6}));
        QDLStem gStem = new QDLStem();
        // Caveat: Bigdecimal from double always induces rounding, so use string constructor for exact tests.
        gStem.listAdd(asQDLValue(new BigDecimal(".9")));
        gStem.listAdd(asQDLValue(new BigDecimal(".6")));
        gStem.listAdd(asQDLValue(new BigDecimal(".3")));
        gStem.listAdd(asQDLValue(0L));
        gStem.listAdd(asQDLValue(new BigDecimal("-0.3")));
        gStem.listAdd(asQDLValue(new BigDecimal("-0.6")));
        assert areEqual(getStemValue("g.", state), gStem);
        //assert areEqual(getStemValue("g.",state), arrayToStem(new double[] {.9, .6, .3, 0, -.3, -.6}));
        assert areEqual(getStemValue("h.", state), arrayToStem(new double[]{-3.3, -2.3, -1.3, -0.3, .7, 1.7}));
        assert areEqual(getStemValue("i.", state), arrayToStem(new double[]{2.7, 1.7, .7, -.3, -1.3, -2.3}));

    }

    public void testRaisedSignedNumbers() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.0 := ¯1;");
        addLine(script, "a.1 := ¯2^2;");
        addLine(script, "a.2 := ¯2^3;");
        addLine(script, "a.3 := ¯0.1^2;");
        addLine(script, "a.4 := ¯0.1^3;");
        addLine(script, "a.5 := ¯3*⁺4;");
        addLine(script, "a.6 := ¯3*¯4;");
        addLine(script, "a.7 := 3*¯4;");
        addLine(script, "a.8 := (3)*(4);");
        addLine(script, "a.9 := ¯0.1;");
        addLine(script, "a.10 := 0.1^¯3;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("a.0", state) == -1L;
        assert getLongValue("a.1", state) == -4L;
        assert getLongValue("a.2", state) == -8L;

        assert areEqual(getBDValue("a.3", state), new BigDecimal("-.01"));
        assert areEqual(getBDValue("a.4", state), new BigDecimal("-.001"));

        assert getLongValue("a.5", state) == -12L;
        assert getLongValue("a.6", state) == 12L;
        assert getLongValue("a.7", state) == -12L;
        assert getLongValue("a.8", state) == 12L;
        assert areEqual(getBDValue("a.9", state), new BigDecimal("-.1"));
        assert areEqual(getBDValue("a.10", state), new BigDecimal("1000"));

    }

    /**
     * Define a recursive function and invoke it. This will compute the Fibonacci numbers
     * 1,1,2,3,5,8,13,21,34,55,... It tests a single value.<br/><br/>
     * Mostly to do recursive functions like this requires a great deal of state management internally
     * so that the state of the various calls do not run together. This test will fail if somehow the
     * state management for functions changes, hence it is a required regression test.
     * Also, this test gets the 20th Fibonacci number which will take a bit. Again this is seeing
     * that the state gets managed for a stack of 20 calls then unravelled correctly. Since each call
     * calls up two more instances of this function, there is exponential growth in the number of calls
     * pending as it runs. Java itself falls over for a native version at about n = 50.
     * Yes this could be optimized not to do that (the math extensions module has an implementation of
     * Binet's formula), and QDL could easily have
     * a few simple expression to compute the numbers efficiently, but that is not the point of this test.
     * The point of the test is to have thousands of pending calls, each with their own state, that the system
     * has to keep straight or the result is wrong. Therefore, this is a version of Knuth's
     * "man or boy" test.
     * <p>
     * If you really want to compute the nth Fibonacci number fast, use Binet's formula:
     * <pre>
     *      γ := (1+sqrt(5))/2
     *     γ1 := (1-sqrt(5))/2
     *      ρ := (γ^n -γ1^n)/sqrt(5)
     *     floor(ρ+0.1); // compensate for rounding errors
     *  </pre>
     * This would calculate the 1000th (n = 1000) Fibonacci number almost instantly
     * as <br/><br/>
     * 4.346655768694153E208
     * </p>
     * <p>Here is a simple lambda for this. It shows fib(20) takes 1564 ms.</p>
     * <pre>
     *     fib(n)->n<=2?1:fib(n-1)+fib(n-2)
     *       date_ms();fib(20);date_ms()
     * 1627617295780
     * 6765
     * 1627617297334
     *   97344-95780
     * 1564
     * </pre>
     *
     * @throws Throwable
     */

    public void testRecursion() throws Throwable {
        recursionTest(false);
        if(isSerializationTestsOff()) return;
        recursionTest(true);
    }

    public void recursionTest(boolean testXML) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "define[");
        addLine(script, "    fib(n)");
        addLine(script, " ]body[");
        addLine(script, "    if[ n <= 2 ]then[ return(1);];");
        addLine(script, "    return( fib(n - 1) + fib(n - 2));");
        addLine(script, "];");
        if (testXML) {
            state = rountripState(state, script, testXML?ROUNDTRIP_XML:ROUNDTRIP_NONE);
        }
        addLine(script, "x := fib(20);"); // should return 6765
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 6765L;
    }

    public void testSignedIndex() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "y := n(10).(-1);"); // should return last one, 9
        addLine(script, "z := n(9).(-4);"); // should return 5
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("y", state) == 9L;
        assert getLongValue("z", state) == 5L;
    }

    /*
         This tests that arguments to functions are executed  in the ambient scope.
     */
  /*  public void testFunctionArgumentScope() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x)->a*x^2;"); // define a function that does not have a set.
        addLine(script, "ok := f(a:=3) == 27;"); // should return 27
        addLine(script, "ok2 := !is_defined(a);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok2", state);
    }*/

    /**
     * Test nroot and sqrt. this computes the closed form solution of
     * <pre>
     *     r^3 = r + 1
     * </pre>
     * using Cardano's formula. The test is to compute it:
     * <pre>
     *      r := (nroot((27+3*sqrt(69))/2,3) + nroot((27-3*sqrt(69))/2,3))/3
     * </pre>
     * and verify that indeed it works up to comparison tolerances.
     *
     * @throws Throwable
     */
    public void testRoots() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "sqrt(x)->nroot(x,2);");
        addLine(script, "r := (nroot((27+3*sqrt(69))/2,3) + nroot((27-3*sqrt(69))/2,3))/3;"); //
        addLine(script, "s := r^3 - r - 1;"); // should return zero
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert areEqual(getBDValue("s", state), BigDecimal.ZERO);
    }

    /**
     * Repeat previous test with √ operator.
     * @throws Throwable
     */

    public void testNRoot() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "r≔(3√((27+3*√69)/2) +  3√((27-3*√69)/2))/3;"); //
        addLine(script, "s := r^3 - r - 1;"); // should return zero
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert areEqual(getBDValue("s", state), BigDecimal.ZERO);
    }

    /**
     * If the modulus operation would get lost in rounding, then {@link BigDecimal} throws
     * and {@link ArithmeticException}. This test sets the digits high enough and computes
     * it. This tests one with two huge numbers (easy case) then sets the precision high enough.
     *
     * @throws Throwable
     */
    public void testBigMod() throws Throwable {
        BigModTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        BigModTest(ROUNDTRIP_XML);
        BigModTest(ROUNDTRIP_JAVA);
        BigModTest(ROUNDTRIP_QDL);
        BigModTest(ROUNDTRIP_JSON);
    }

    public void BigModTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := mod(494590348974597684,394874589745) == 53454241559;");
        addLine(script, "numeric_digits(100);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok1 := mod(498723987945689378498579456, 1009) == 556;");
        addLine(script, "ok2 := numeric_digits()==100;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Could not find modulus of two huge integers.";
        assert getBooleanValue("ok1", state) : "Could not find modulus of one huge, one small integer.";
        assert getBooleanValue("ok2", state) : "numeric_digits not saved correctly.";
    }

    public void testMinMax() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x := min(2,3.4) ;");
        addLine(script, "okx := var_type(x)==" + Constant.LONG_TYPE + ";"); // must be an integer
        addLine(script, "okx0 := x==2;");
        addLine(script, "y := max(2,3.4) ;");
        addLine(script, "oky := var_type(y)==" + Constant.DECIMAL_TYPE + ";"); // must be a decimal
        addLine(script, "oky0 := y==3.4;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okx", state) : MathEvaluator.MIN + " returns wrong numeric type, should be a long";
        assert getBooleanValue("okx0", state) : MathEvaluator.MIN + " returns wrong value.";
        assert getBooleanValue("oky", state) : MathEvaluator.MAX + " returns wrong numeric type, should be a decimal.";
        assert getBooleanValue("oky0", state) : MathEvaluator.MAX + " returns wrong value";
    }

    /**
     * Test multiple args. Github issue https://github.com/ncsa/qdl/issues/99 found that the parser allowed for
     * bogus multiple arguments like f(3 4 5 6). It would not throw an error. Should be intercepted by the parser.
     * @throws Throwable
     */
    public void testBadArgList() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(3 4);"); // does not matter if f exists, parser should catch it
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        }catch(ParsingException e) {
            return; // alles ok
        }
        assert false : "was able to pass multiple arguments like \"3 4 5\" to a function.";
    }

    /**
     * As of version 1.6.1 can drop lead 0 in decimals. This tests that.
     * @throws Throwable
     */
    public void testDecimals() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 0.3 == .3;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state):"Did not process decimal without leading 0 correctly";
    }

    /**
     * Bad case where a wrong expression like .a3 is entered. Parser should blow up with the correct
     * paring errors.
     * @throws Throwable
     */
    public void testBadDecimals() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, ".a3;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean isGood = false;
        try {
            interpreter.execute(script.toString());
        }catch(ParsingException t){
            isGood = true;
        }
        assert isGood : "failed to parse bad decimal without leading 0 correctly";
    }
}

