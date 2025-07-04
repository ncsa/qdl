package org.qdl_lang;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/7/22 at  1:51 PM
 */
public class SetTest extends AbstractQDLTester {
    public void testSetCreate() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := {0,3,1,2} == " +OpEvaluator.TO_SET2 + "[;4];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }


    public void testOperator() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := {3} == ⊢3;");
        addLine(script, "ok1 := {false} == ⊢false;");
        addLine(script, "ok2 := {2.3} == ⊢2.3;");
        addLine(script, "ok3 := {null} == ⊢null;");
        addLine(script, "ok4 := {0,1,2,3} == " + OpEvaluator.TO_SET2 + "[;4];"); // use ASCII digraph
        addLine(script, "ok5 := {'b','e'} == " + OpEvaluator.TO_SET2 + "{'a':'b','d':'e'};"); // use ASCII digraph
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert getBooleanValue("ok4", state);
        assert getBooleanValue("ok5", state);
    }

    public void testSetToList() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := ~{0,1,2,3}∪{0,1,2,3};"); // convert to a list, verify that another operation (union) does not interfere.
        addLine(script, "ok := reduce(@&&, [0,1,2,3] ∈ a.);"); // note that order is not preserved, so test membership
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testSetInclusion() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3};");
        addLine(script, "ok0 := {1,2}<a;");
        addLine(script, "ok1 := a>{1,2};");
        addLine(script, "ok2 := a<={1,2};");
        addLine(script, "ok3 := a=={1,2};");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert !getBooleanValue("ok2", state);
        assert !getBooleanValue("ok3", state);
    }

    public void testIntersection() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "b := {2,4,6};");
        addLine(script, "ok := {2,4}==a∩b;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testASCIIIntersection() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "b := {2,4,6};");
        addLine(script, "ok := {2,4}==a/\\b;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testUnion() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "b := {2,4,6};");
        addLine(script, "ok := {0,1,2,3,4,6}==a∪b;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testASCIIUnion() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "b := {2,4,6};");
        addLine(script, "ok := {0,1,2,3,4,6}==(a\\/b);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Gratutious serialization test.
     *
     * @throws Throwable
     */
    public void testDifference() throws Throwable {
        testDifference(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testDifference(ROUNDTRIP_XML);
        testDifference(ROUNDTRIP_QDL);
        testDifference(ROUNDTRIP_JAVA);
        testDifference(ROUNDTRIP_JSON);
    }

    protected void testDifference(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "b := {2,4,6};");
        state = rountripState(state, script, testCase);
        addLine(script, "ok := {0,1,3}==(a/b);");
        addLine(script, "ok1 := {6}==(b/a);");

        QDLInterpreter interpreter;
        interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }

    public void testSymmetricDifference() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "b := {2,4,6};");
        addLine(script, "ok := {0,1,3,6}==(a%b);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testMemberOf() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "ok := 2 ∈ a;");
        addLine(script, "ok1 := reduce(@∧, [0,1]∈a);"); // list can be used
        addLine(script, "ok2 := 7 ∉ a;");
        addLine(script, "ok3 := reduce(@∧, [8,6]∉a);"); // list can be used
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
    }

    public void testSize() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {0,1,2,3,4};");
        addLine(script, "ok := size(a) == 5;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testSubset() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := {0,-2} == pick((x)->x<3, {-2,0,4,5});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testSubsetContract() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := sublist({1,2,3},7) == {1,2,3};");
        addLine(script, "s := sublist({1,2,3,4},3);");
        addLine(script, "ok1 := s < {1,2,3,4} && size(s)==3;");
        addLine(script, "r := sublist({1,2,3,4},-3);");
        addLine(script, "ok2 := r < {1,2,3,4} && size(r)==3;");
        addLine(script, "ok3 := sublist({1,2,3,4},0) == {};");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "requesting too many elements should return whole set";
        assert getBooleanValue("ok1", state) : "Should return 3 element subset";
        assert getBooleanValue("ok2", state) : "Should return 3 element subset for negative count";
        assert getBooleanValue("ok3", state) : "requesting zero elements should return empty set.";
    }

    public void testReduce() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 120 == reduce(@*, {1,2,3,4,5});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testLoop() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := 6;");
        addLine(script, "while[j∈{1,2,3,4,5}][a:=a*j;];");
        addLine(script, "ok := a== 720;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Checking if big decimals are involved takes a specific test (since they are
     * compared with both value and scale). This checks that that works
     *
     * @throws Throwable
     */
    public void testBDMembership() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := reduce(@&&, [0.25, 1.25, 2] ∈ ⊢ [1;10]/4);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * tests that membership can be used in a reference.
     *
     * @throws Throwable
     */
    public void testForEachMembership() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := for_each(@∈, ['a'], ['a', 'b']);"); // return [[true,false]]
        addLine(script, "ok := a.0.0;");
        addLine(script, "ok1 := !a.0.1;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }


    public void testNested() throws Throwable {
        testNested(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        testNested(ROUNDTRIP_XML);
        testNested(ROUNDTRIP_QDL);
        testNested(ROUNDTRIP_JAVA);
        testNested(ROUNDTRIP_JSON);
    }

    /**
     * Sets do work when nested but only with scalar entries, since comparing
     * two stems, e.g., is very complex. Don't forget that asking something like
     * [1,2] == [1,2] return [true, true] since operations component wise.
     *
     * @throws Throwable
     */
    protected void testNested(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := {{1,2},{3,4}};");
        addLine(script, "b := {{1,3},{2,4},{3,4}};");
        addLine(script, "ok := {{3,4}} == (a /\\ b);");
        addLine(script, "ok0 := a == a;");
        addLine(script, "ok1 := a==b;"); //false
        addLine(script, "ok2 := a != b;"); //true
        addLine(script, "ok3 := {{1,2}} == (a/b);");
        state = rountripState(state, script, testCase);
        addLine(script, "ok4 := {{1,3},{2,4}} == (b/a);");
        addLine(script, "ok5 := {{1,2},{1,3},{2,4}} == a%b;");//true
        addLine(script, "ok5a := {{1,2},{1,3},{2,4}} < a%b;");//false -- inclusion is strict
        addLine(script, "ok5b := {{1,2},{1,3},{2,4}} <= a%b;");//true
        addLine(script, "ok6 := {1,2} ∈ a;");
        addLine(script, "ok7 := {1,3} ∈ a;");//false
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok0", state);
        assert !getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert getBooleanValue("ok4", state);
        assert getBooleanValue("ok5", state);
        assert !getBooleanValue("ok5a", state);
        assert getBooleanValue("ok5b", state);
        assert getBooleanValue("ok6", state);
        assert !getBooleanValue("ok7", state);
    }

    /**
     * Regression test for https://github.com/ncsa/qdl/issues/130
     * @throws Throwable
     */
    public void testSetFromSparseList() throws Throwable {
        setFromSparseListTest(ROUNDTRIP_NONE);
        if(isSerializationTestsOff()) return;
        setFromSparseListTest(ROUNDTRIP_XML);
        setFromSparseListTest(ROUNDTRIP_QDL);
        setFromSparseListTest(ROUNDTRIP_JAVA);
        setFromSparseListTest(ROUNDTRIP_JSON);
    }

    public void setFromSparseListTest(int testCase) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.4:=4;a.5:=5;a.11:=92;a.93:=92;"); // internally no entries in arraylist
        addLine(script, "b.0:=4;b.1:=5;b.11:=92;b.93:=92;"); // internally, arraylist has some entries
        addLine(script, "ok := {4,5,92} == " +OpEvaluator.TO_SET2 + "a.;");
        addLine(script, "ok2 := {4,5,92} == " +OpEvaluator.TO_SET2 + "b.;");
        state = rountripState(state, script, testCase);

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to turn only sprase list into a set";
        assert getBooleanValue("ok2", state) : "failed to turn mixed list into a set";
    }
    /**
     * Killer test of various famous set identities and edge cases with null sets.
     * Also tests serialization of the sets and state.
     *
     * @throws Throwable
     */
    public void testIdentities() throws Throwable {
        // The test sets are in groups of 3 and have non-trivial intersections so the tests have
        // something to compare against.
        // Simple sets
        String A = makeTestSet(12);
        String B = makeSuperSet(A);
        String C = makeSuperSet(A);

        testIdentities(ROUNDTRIP_NONE, A, B, C);
        if(!isSerializationTestsOff()) {

            testIdentities(ROUNDTRIP_XML, A, B, C);
            testIdentities(ROUNDTRIP_QDL, A, B, C);
            testIdentities(ROUNDTRIP_JAVA, A, B, C);
        }
        //nested sets
        A = makeNestedTestSet(12, 3);
        B = makeSuperSet(A);
        C = makeSuperSet(A);
        testIdentities(ROUNDTRIP_NONE, A, B, C);
        if(!isSerializationTestsOff()) {
        testIdentities(ROUNDTRIP_XML, A, B, C);
        testIdentities(ROUNDTRIP_QDL, A, B, C);
        testIdentities(ROUNDTRIP_JAVA, A, B, C);
        }
    }

    protected void testIdentities(int testCase,
                                  String A, String B, String C) throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "A :=" + A + ";"); // more or less total size
        addLine(script, "B :=" + B + ";"); // more or less total size
        addLine(script, "C :=" + C + ";"); // more or less total size


        addLine(script, "A1 := sublist(A, 7);");
        addLine(script, "A0 := sublist(A1, 3);");

        //boatload of standard set identities.
        state = rountripState(state, script, testCase);
        addLine(script, "ok0 := A0 < A1 < A;");
        addLine(script, "ok1 := (A/B)/(B/C) == A/B;");
        addLine(script, "ok2 := A∆B == B∆A;");
        addLine(script, "ok3 := A < A∩B∪(A/B) ;"); // fails
        addLine(script, "ok4 := A > A∩B && A != A∪B ;"); // works
        addLine(script, "ok4a := A > A∩B != A;"); // works
        addLine(script, "ok5 := A <= A∩B∪(A/B) ;"); // works
        addLine(script, "ok6 := A == A∩B∪(A/B) ;"); // works
        addLine(script, "ok7 := A∩(B∆C) == A∩B ∆ A∩C;"); // right distributive
        addLine(script, "ok8 := (B%C)∩A == A∩B % A∩C;"); // left "
        addLine(script, "ok9 := A∩B == A/(A/B);");
        addLine(script, "oka := A/B==A/(A∩B);");
        addLine(script, "okb := A/B == A%(A∩B);");
        addLine(script, "okc := A∪B == A∪(B/A);");
        addLine(script, "okd := {} == A∩(B/A);");
        addLine(script, "oke := A∩{} == ∅;");
        addLine(script, "okf := A∪∅ == A;");
        addLine(script, "ok10 := {} == ∅;");
        addLine(script, "ok11 := {} < ∅;"); // fails
        addLine(script, "ok12 := ∅ < A;");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getBooleanValue("ok0", state) : " failure for A0 < A1 < A;";
        assert getBooleanValue("ok1", state) : " failure for (A/B)/(B/C) == A/B";
        assert getBooleanValue("ok2", state) : " failure for A∆B == B∆A";
        assert !getBooleanValue("ok3", state) : "failure for A < A∩B∪(A/B)";
        assert getBooleanValue("ok4", state) : " failure for A > A∩B && A != A∪B";
        assert getBooleanValue("ok4a", state) : " failure for A > A∩B != A -- chaining failed";
        assert getBooleanValue("ok5", state) : " failure for A <= A∩B∪(A/B)";
        assert getBooleanValue("ok6", state) : " failure for A == A∩B∪(A/B)";
        assert getBooleanValue("ok7", state) : " failure for A∩(B∆C) == A∩B∆A∩C";
        assert getBooleanValue("ok8", state) : " failure for (B%C)∩A == A∩B % A∩C";
        assert getBooleanValue("ok9", state) : " failure for A∩B == A/(A/B);";
        assert getBooleanValue("oka", state) : " failure for A/B==A/(A∩B);";
        assert getBooleanValue("okb", state) : " failure for A/B == A%(A∩B);";
        assert getBooleanValue("okc", state) : " failure for A∪B == A∪(B/A);";
        assert getBooleanValue("okd", state) : " failure for {} == A∩(B/A);";
        assert getBooleanValue("oke", state) : " failure for A∩{} == ∅";
        assert getBooleanValue("okf", state) : " failure for A∪∅ == A";
        assert getBooleanValue("ok10", state) : "failure for  {} == ∅";
        assert !getBooleanValue("ok11", state) : "failure for  {} < ∅ -- null set cannot be a proper subset of itself.";
        assert getBooleanValue("ok12", state) : "failure for  ∅ < A";

    }

    /**
     * Test order of operations on symmetric difference and intersection.
     * Runs with redundant parentheses and without and checkes results are same.
     * Then runs with difference parentheses to see if somehow that no longer works.
     *
     * @throws Throwable
     */
    public void testOOO() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "A:={6,7,9,12,13};");
        addLine(script, "B:={1,3,5,12,14};");
        addLine(script, "C:={16,17,3,5,7,10,12,13,15};");
        addLine(script, "D:={1,2,5,6,8,11,12,13};");
        addLine(script, "ok := A∩B∆C∩D == (A∩B)∆(C∩D);");
        addLine(script, "ok1 := A∩(B∆C)∩D == {13};");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }


    /**
     * Test that for_each applies to the ⊢ (to_set) operator. This turns each element of a matrix
     * into a set.
     *
     * @throws Throwable
     */
    public void testToSetForEach() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok ≔ {{0},{1},{2},{3},{4},{5},{6},{7},{8},{9},{10},{11}} ≡  ⊢for_each(⊗⊢, n(3,4,n(12)));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Checks that the to set operator sits at the top of the order of operations chart.
     * If this changes, a lot of stuff breaks.
     *
     * @throws Throwable
     */
    public void testToSetOOO() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok ≔ ⊢[;5] ≡  " +OpEvaluator.TO_SET2 + "[;5];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * One of the simplest ways to makes sets in QDL is to make them
     * in input form.
     *
     * @param n
     * @return
     * @throws Throwable
     */
    protected String makeTestSet(int n) throws Throwable {
        int prime = 17; // used to generate decimals, ets
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "n :=" + n + ";"); // more or less total size
        addLine(script, "p := " + prime + ";");
        addLine(script, "count := n%3;"); // number per group
        addLine(script, "A. := random_string(6, count)~[0==date_ms()%2] ~(mod(random(count), p)+p);");
        addLine(script, "A. := A. ~ (mod(random(n-size(A.)), p)+p)/(3+p%4);"); // make some fractions
        addLine(script, "A := input_form(" +OpEvaluator.TO_SET2 +  "A.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        return getStringValue("A", state);

    }

    /**
     * This will take a set (in input form), take a subset of it and add some random stuff.
     * This means that the resulting set willl contain part of A as a subset
     *
     * @param A
     * @return
     */
    protected String makeSuperSet(String A) throws Throwable {
        int n = 6;  // number of elements to use
        String B = makeTestSet(10);
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "A := " + A + ";");
        addLine(script, "B := " + B + ";");
        addLine(script, "X := sublist(B," + n + ")\\/sublist(A," + n + ");");
        addLine(script, "X := input_form(X);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        return getStringValue("X", state);

    }

    /**
     * Checks that looking inside a stem with the has_value aka ∈ works.
     *
     * @throws Throwable
     */
    public void testEpsilonOnStem() throws Throwable {
        State state = testUtils.getNewState();
        state.setAllowBaseFunctionOverrides(true);
        StringBuffer script = new StringBuffer();
        addLine(script, "q. := [[-14,-7,-14,5,-1],[-10,-10,10,-4,4,9,-2]];");
        addLine(script, "ok := reduce(@&&, [-14,9]∈q.);");
        addLine(script, "ok1 := reduce(@&&, [false,true,true] == ([-3,-7,-2]∈q.));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }
    /*
         q. := [[-14,-7,-14,5,-1],[-10,-10,10,-4,4,9,-2]]
              has_value([-14,9], q.)
    [true,true]
      has_value([3,-7,-2], q.)
    [false,true,true]
           q. := [[-14,-7,-14,5,-1],[-10,-10,10,-4,4,9,-2]]
              has_value([-14,9], q.)

      */

    protected String makeSingleNestedTestSet(int n, String A) throws Throwable {
        String out = makeTestSet(n);
        out = out.substring(0, out.lastIndexOf("}"));
        out = out + "," + A;
        return out + "}";
    }

    protected String makeNestedTestSet(int n, int depth) throws Throwable {
        String A = makeSingleNestedTestSet(n, makeTestSet(n));

        for (int i = 0; i < depth - 1; i++) {
            A = makeSingleNestedTestSet(n, A);

        }
        return A;
    }
}
