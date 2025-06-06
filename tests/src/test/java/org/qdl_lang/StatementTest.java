package org.qdl_lang;

import org.qdl_lang.exceptions.AssertionException;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.exceptions.ReturnException;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.QDLConstants;
import org.qdl_lang.state.State;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/14/20 at  10:57 AM
 */
public class StatementTest extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();


    /**
     * Test that a malformed loop (conditional statement is not a conditional) fails reasonably.
     *
     * @throws Throwable
     */

    public void testBadLoop() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "i:=0;");
        addLine(script, "while[");
        addLine(script, "   i + 2"); // Not a conditional, so the system should throw it out.
        addLine(script, "]do[");
        addLine(script, "  say(i);");
        addLine(script, "]; // end while");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (QDLExceptionWithTrace isx) {
            bad = false;
        }
        if (bad) {
            assert false : "Was able to execute a loop without a valid conditional";
        }

    }

    /**
     * Test a loop that has a basic conditional statement (with something that needs evaluated)
     *
     * @throws Throwable
     */

    public void testBasicLoop() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "i:=0;");
        addLine(script, "j:=1;");
        addLine(script, "while[");
        addLine(script, "   i++ < 5");
        addLine(script, "]do[");
        addLine(script, "  j *= j+i;");
        addLine(script, "]; // end while");
        // Note the conditional for the loop has i = 0,1,2,3,4
        // but inside the loop it has been incremented and is 1,2,3,4,5
        // With the multiplication assignment to j, this results in fast exponential growth.
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("j", state).equals(65585696L) : "Loop did not execute properly.";
    }
    /*
        while[x∈[;5]][try[break();]catch[];]
        i := 0;
        while[
          x∈[;5]
        ][
         try[
           if[i++ == 3][break();];
         ]catch[
         ];
        ];
     */

    public void testLoopWithTryAndBreak() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "        i := 0;\n" +
                "        while[\n" +
                "          x∈[;5]\n" +
                "        ][\n" +
                "         try[\n" +
                "           if[i == 3][break();]else[i++;];\n" +
                "         ]catch[\n" +
                "         ];\n" +
                "        ];");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("i", state).equals(3L) : "break() inside try..catch inside loop did not execute properly.";
    }

    public void testLoopWithTryAndContinue() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script,
                "        i := 0;\n" +
                        "        while[\n" +
                        "          x∈[;5]\n" +
                        "        ][\n" +
                        "         try[\n" +
                        "           if[i == 3][continue();]else[i++;];\n" +
                        "         ]catch[\n" +
                        "         ];\n" +
                        "        ];");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("i", state).equals(3L) : "continue() inside try..catch inside loop did not execute properly.";
    }

    // for the next test. This is the correct result, used to verify in the test.
    String contTest = "[{0:'0_0', 1:'0_1', 2:'0_2', 4:'0_4', 5:'0_5'},{0:'1_0', 1:'1_1', 2:'1_2', 4:'1_4', 5:'1_5'},{0:'2_0', 1:'2_1', 2:'2_2', 4:'2_4', 5:'2_5'},{0:'3_0', 1:'3_1', 2:'3_2', 4:'3_4', 5:'3_5'},{0:'4_0', 1:'4_1', 2:'4_2', 4:'4_4', 5:'4_5'}];";

    /**
     * Tests continue(boolean). This does so in a nested loop, so it also tests that
     * the scope of the function is correct. It applies to the inner loop only.
     *
     * @throws Throwable
     */
    public void testMondicContinue() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script,
                "b. ≔ null;\n" +
                        "while[j∈[;5]]\n" +
                        " do[\n" +
                        "    while[k∈[;6]]\n" +
                        "      do[\n" +
                        "          continue(k==3);\n" +
                        "          b.j.k := to_string(j) + '_' + to_string(k);\n" +
                        "        ];\n" +
                        "  ];\n" +
                        "test. := " + contTest + "\n" +
                        "ok := (false ∉ b. == test.) && (size(test.) == size(b.));"
        );
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "continue(arg) did not execute properly.";
    }

    String breakTest = "[['0_0','0_1','0_2'],['1_0','1_1','1_2'],['2_0','2_1','2_2'],['3_0','3_1','3_2'],['4_0','4_1','4_2']];\n";

    /**
     * test monadic break. This is inside a nested loop to show scope works right too.
     *
     * @throws Throwable
     */
    public void testMondicBreak() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script,
                     "b. ≔ null;\n" +
                        "while[j∈[;5]]\n" +
                        " do[\n" +
                        "    while[k∈[;6]]\n" +
                        "      do[\n" +
                        "          break(k==3);\n" +
                        "          b.j.k := to_string(j) + '_' + to_string(k);\n" +
                        "        ];\n" +
                        "  ];\n" +
                        "test. := " + breakTest +
                        "ok := (false ∉ b. == test.) && (size(test.) == size(b.));"
        );
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "break(arg) did not execute properly.";
    }

    /*
    v. := [1;7]
    i :=1 ;
    while[x∈v.]
       do[
          if[0==mod(x,2)][continue();];
          i:=i*x;
          ];
    ok := i == 15;

     */
    public void testLoopWithTryAndContinue2a() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script,
                "i := 1;                  "
                        + "v. := [;7];                      "
                        + "while[x∈v.]             "
                        + "   do[                          "
                        + "      if[0==mod(x,2)][continue();];  "
                        + "       i:=i*x;                "
                        + "    ];          "
                        + " ok := i == 15;               "
        );
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "continue() inside loop inside conditional did not execute properly.";
    }

    public void testLoopWithTryAndContinue2() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script,
                "ok := true;                  "
                        + "i := 0;                      "
                        + "key_set := {'a','b','c','d'};"      // The test is that the continue statement
                        + "while[k∈key_set]             "      // skips the rest of the loop.
                        + "do[                          "      //  If it does not, then ok is reset
                        + "   if[k=='b'][continue();];  "      //  to false and the test fails
                        + "   if[k=='b']                "      // The i variable is just to do something.
                        + "   then[ok:=false;]          "
                        + "   else[i++;];               "
                        + "];                           "
        );
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "continue() inside conditional did not execute properly.";
    }

     /*
      ok := true;
      i := 0;
      key_set := {'a','b','c','d'};
      while[k∈key_set]
      do[
         if[k=='b'][continue();];
         if[k=='b']
         then[ok:=false;]
         else[i++;];
      ];
      */
    /*
    msg := 'fail';
    try[
     ⊨ 2 == 3 : 'ok';
    ]catch[
       msg:=error_message;
    ];
    ok := msg=='ok';
     */

    /**
     * Regression test that error messages from assert statements get propagated correctly.
     *
     * @throws Throwable
     */
    public void testCatchAssertion() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "    msg := 'fail';\n" +
                "    try[\n" +
                "     ⊨ 2 == 3 : 'ok';\n" +
                "    ]catch[\n" +
                "       msg:=error_message;\n" +
                "    ];\n" +
                "    ok := msg=='ok';");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to propagate assertion message.";
    }

    /**
     * Test that assigning a value to a keyword, e.g. false := true fails.
     *
     * @throws Throwable
     */

    public void testReservedWordAssignments() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, QDLConstants.RESERVED_TRUE + " := 2;");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (IllegalStateException | IllegalArgumentException iax) {
            bad = false;
        }
        if (bad) {
            assert false : "Error; Was able to assign " + QDLConstants.RESERVED_TRUE + " a value";
        }
        bad = true;

        script = new StringBuffer();
        addLine(script, QDLConstants.RESERVED_FALSE + " := 1;");
        try {
            interpreter.execute(script.toString());
        } catch (IllegalStateException | IllegalArgumentException iax) {
            bad = false;
        }
        if (bad) {
            assert false : "Error; Was able to assign " + QDLConstants.RESERVED_FALSE + " a value";
        }
        bad = true;

        script = new StringBuffer();
        addLine(script, QDLConstants.RESERVED_NULL + " := 'foo';");
        try {
            interpreter.execute(script.toString());
        } catch (IllegalArgumentException iax) {
            bad = false;
        }
        if (bad) {
            assert false : "Error; Was able to assign " + QDLConstants.RESERVED_NULL + " a value";
        }
    }

    public void testAssert() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "assert[2<3]['test 1'];");
        addLine(script, "⊨ 2<3 : 'test 1';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        // should work without incident since both assert statements pass
    }

    public void testBadAssert1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "assert[3<2]['test 1'];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException assertionException) {
            bad = false;
            assert assertionException.getMessage().equals("test 1");
        }
        if (bad) {
            assert false : "failed assertion not asserted";
        }

    }

    public void testBadAssert2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "⊨ 3<2 : 'test 1';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (AssertionException assertionException) {
            bad = false;
            assert assertionException.getMessage().equals("test 1");
        }
        if (bad) {
            assert false : "failed assertion not asserted";
        }

    }

    /**
     * Simple block test. This resets a global variable, ok, and sets a local
     * variable, a.
     *
     * @throws Throwable
     */
    public void testVariableBlock() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := false;");
        addLine(script, "block[a:=2;ok:=a==2;];");// set local variable, a, reset ok
        addLine(script, "oka := ∄a;"); // check that a does not exist outside of block
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("oka", state);
    }

    /**
     * tests that function defined in a block are local to the block.
     *
     * @throws Throwable
     */
    public void testFunctionBlock() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := false;");
        addLine(script, "block[f(x)->!x; ok :=f(false);];");
        addLine(script, "okf := !is_function(f,1);"); // check that a does not exist outside of block
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("okf", state);
    }

    /**
     * Basic try ... catch block. This has spaces to test new parser upgrades in 1.4.
     *
     * @throws Throwable
     */
    public void testTryCatch() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := -1;");
        addLine(script, "try   [to_number('foo');a:=1;] catch  [a:=2;];");// set local variable, a,
        addLine(script, "ok := a == 2;"); // check that a does not exist outside of block
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testSwitch0() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := -1;b:=2;");
        addLine(script, "switch   [");// set local variable, a,
        addLine(script, "   if[a+b < 0][a:=-2;];");
        addLine(script, "   if[a-b > 0][a:=-3;];");
        addLine(script, "   if[a*b < 0][a:=0;];");
        addLine(script, "];");
        addLine(script, "ok := a == 0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Same as {@link #testSwitch0()}, just change the order of the conditionals
     *
     * @throws Throwable
     */
    public void testSwitch1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := -1;b:=2;");
        addLine(script, "switch   [");// set local variable, a,
        addLine(script, "   if[a+b < 0][a:=-2;];");
        addLine(script, "   if[a*b < 0][a:=0;];");
        addLine(script, "   if[a-b > 0][a:=-3;];");
        addLine(script, "];");
        addLine(script, "ok := a == 0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Same as {@link #testSwitch0()}, just change the order of the conditionals
     *
     * @throws Throwable
     */
    public void testSwitch2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := -1;b:=2;");
        addLine(script, "switch   [");// set local variable, a,
        addLine(script, "   if[a*b < 0][a:=0;];");
        addLine(script, "   if[a-b > 0][a:=-3;];");
        addLine(script, "   if[a+b < 0][a:=-2;];");
        addLine(script, "];");
        addLine(script, "ok := a == 0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * how to do a default case for a switch in QDL: Since the statements are evaluated in order, just
     * have the last one as if[true]. This test shows that if an earlier one works, then that is chosen.
     *
     * @throws Throwable
     */
    public void testSwitchDefault() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := -1;b:=2;");
        addLine(script, "switch   [");// set local variable, a,
        addLine(script, "   if[a*b < 0][a:=-4;];");
        addLine(script, "   if[a-b > 0][a:=-3;];");
        addLine(script, "   if[a+b == 0][a:=-2;];");
        addLine(script, "   if[true][a:=0;];");  // default case, not used
        addLine(script, "];");
        addLine(script, "ok := a == -4;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * How to do a default case for a switch in QDL part 2:  This test shows that
     * the default is use if all the others fail.
     *
     * @throws Throwable
     */
    public void testSwitchDefault1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := -1;b:=2;");
        addLine(script, "switch   [");// set local variable, a,
        addLine(script, "   if[a*b > 0][a:=-4;];");
        addLine(script, "   if[a-b > 0][a:=-3;];");
        addLine(script, "   if[a+b == 0][a:=-2;];");
        addLine(script, "   if[true][a:=0;];"); // default case used
        addLine(script, "];");
        addLine(script, "ok := a == 0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Tests that a ternary expression using a stem of booleans returns a conformable argument.
     *
     * @throws Throwable
     */
    public void testStemTernary() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "b. :=[true,false,true,true];");
        addLine(script, "x. := b.?-1:1;");
        addLine(script, "ok := reduce(@&&, x. == [-1,1,-1,-1]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Basic test of the ternary operator.
     *
     * @throws Throwable
     */
    public void testTernary() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 1 ==  (2<1?-1:1);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testTernaryArrow() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 1 ==  (2<1⇒-1:1);");// use the arrow, not the ?
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * in the case that the ternary arrow is missing the else clause, make sure a null is returned.
     * @throws Throwable
     */
    public void testTernaryArrowDefault() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := null ==  (3<2⇒-1);");// use the arrow, not the ?
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    // Fix for https://github.com/ncsa/qdl/issues/86 are next two tests
    /*
       Test that a non-boolean left argument fails in a timely fashion
       during parsing with the right exception
     */
    public void testTernaryArrowFail1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 1 ==  ([;2]⇒-1:1);");// use the arrow, not the ?
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "was able to set stem instead of boolean in if expression";
        } catch (QDLExceptionWithTrace iax) {
            // ok
        }
    }

    /**
     * Shows that if the expression seems syntactically valid, but does not evaluate to
     * a boolean, it fails with the right error.
     *
     * @throws Throwable
     */
    public void testTernaryArrowFail2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "claims.'foo' := 'bar';"); // set value
        addLine(script, "'a' ∉ ['post_token', 'post_refresh', 'post_user_info'] ⇒ return();");// use the arrow, not the ?
        //addLine(script, "!is_defined(claims.'isMemberOf') ⇒ return();");// use the arrow, not the ?
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        //
        try {
            interpreter.execute(script.toString());
            assert false : "was able to set stem instead of boolean in if expression";
        } catch (ReturnException iax) {
            // ok
        }
    }

    /**
     * Regression test to ensure that using the define statement keeps its state
     * separate
     *
     * @throws Throwable
     */
    public void testFunctionDefinitionVariableVisibility() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Evaluate the function. The test is that at. is not altered in the ambient state
        addLine(script, "tokens.:={'foo':{'bar':'baz'},'fnord':'woof'};\n" +
                "at.:=tokens.;\n" +
                "define[f(tt.)][at.:=tt.'foo';];    \n" +  // sets a. to the substem.
                "f(tokens.); \n");
        addLine(script, "ok := at.'fnord'=='woof';"); // fingers and toes check, but it works.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "define statement variable visibility contract violated.";
    }

    /**
     * Test for https://github.com/ncsa/qdl/issues/103. Invoking return() inside the argument to the
     * interpret function should actually just return the value. This is done inside a conditional to
     * test that scope is immaterial too.
     * @throws Throwable
     */
    public void testInterpretReturn() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 11 ≡ interpret('if[2^2<3^3][return(11);];');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "define statement variable visibility contract violated.";
    }
/*
tokens.:={'foo':{'bar':'baz'},'fnord':'woof'};
at.:=tokens.;
define[f(tt.)][at.:=tt.'foo';];
f(tokens.)
 */
}
