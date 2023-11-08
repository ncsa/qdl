package edu.uiuc.ncsa.qdl;

import edu.uiuc.ncsa.qdl.evaluate.StringEvaluator;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.expressions.VariableNode;
import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.variables.Constant;
import edu.uiuc.ncsa.qdl.variables.VStack;
import edu.uiuc.ncsa.qdl.variables.VThing;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  1:11 PM
 */
public class StringFunctionTests extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();


    public void testContainsStringString() throws Exception {
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(StringEvaluator.CONTAINS);
        ConstantNode left = new ConstantNode("abcdef", Constant.STRING_TYPE);
        ConstantNode right = new ConstantNode("de", Constant.STRING_TYPE);

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        assert (Boolean) polyad.getResult();
    }


    public void testContainsStringStem() throws Exception {
        // test that the first argument is a string and the second is a stem variable.
        // result should be conformable with the second argument
        String source = "Four score and seven years ago...";
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem snippets = new QDLStem();
        snippets.put("score", "score");
        snippets.put("Four", "Four");
        snippets.put("ago", "woof");
        snippets.put("7", "seven");
        vStack.put(new VThing(new XKey("snippets."), snippets));
        vStack.put(new VThing(new XKey("source"), source));

        Polyad polyad = new Polyad(StringEvaluator.CONTAINS);
        VariableNode left = new VariableNode("source");
        VariableNode right = new VariableNode("snippets.");

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        // A left scalar is propagate to all the elements of the right argument if it is compound.
        assert result.containsKey("score");
        assert (Boolean) result.get("score");
        assert result.containsKey("Four");
        assert (Boolean) result.get("Four");
        assert result.containsKey("7");
        assert (Boolean) result.get("7");
        assert result.containsKey("ago");
        assert !(Boolean) result.get("ago");

    }

    /**
     * Case is concat(stem. string) <br/><br/>
     * Anticipated result is that there will be a stem variable that results with the same keys and
     * booleans telling whether or not the string is included.
     *
     * @throws Exception
     */

    public void testContainsStemString() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "One Ring to rule them all");
        sourceStem.put("find", "One Ring to find them");
        sourceStem.put("bring", "One Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        String targetString = "One";

        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("targetString"), targetString));

        Polyad polyad = new Polyad(StringEvaluator.CONTAINS);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode right = new VariableNode("targetString");

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.containsKey("rule");
        assert result.getBoolean("rule");
        assert result.containsKey("find");
        assert result.getBoolean("find");
        assert result.containsKey("bring");
        assert result.getBoolean("bring");
        assert result.containsKey("bind");
        assert !result.getBoolean("bind");
    }


    public void testContainsStemStringCaseInsensitive() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "oNe Ring to rule them all");
        sourceStem.put("find", "OnE Ring to find them");
        sourceStem.put("bring", "one Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        String targetString = "One";

        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("targetString"), targetString));

        Polyad polyad = new Polyad(StringEvaluator.CONTAINS);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode right = new VariableNode("targetString");
        ConstantNode ignoreCase = new ConstantNode(false, Constant.BOOLEAN_TYPE);

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.addArgument(ignoreCase);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.containsKey("rule");
        assert result.getBoolean("rule");
        assert result.containsKey("find");
        assert result.getBoolean("find");
        assert result.containsKey("bring");
        assert result.getBoolean("bring");
        assert result.containsKey("bind");
        assert !result.getBoolean("bind");
    }


    public void testContainsStemStem() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "One Ring to rule them all");
        sourceStem.put("find", "One Ring to find them");
        sourceStem.put("bring", "One Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        QDLStem targetStem = new QDLStem();
        targetStem.put("all", "all");
        targetStem.put("One", "One");
        targetStem.put("bind", "woof");
        targetStem.put("7", "seven");
        vStack.put(new VThing(new XKey("snippets."), targetStem));
        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("targetStem."), targetStem));

        Polyad polyad = new Polyad(StringEvaluator.CONTAINS);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode right = new VariableNode("targetStem.");

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        // The contract is that the answer is conformable to the left argument, so only keys from the left
        // argument appear in the result.
        assert result.size() == 1;
        assert result.containsKey("bind");
        assert !result.getBoolean("bind");
    }


    public void testStringTrim() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        String arg = "   my   my   my    ";
        vStack.put(new VThing(new XKey("arg"), arg));
        String result = arg.trim();
        Polyad polyad = new Polyad(StringEvaluator.TRIM);
        VariableNode left = new VariableNode("arg");

        polyad.addArgument(left);
        polyad.evaluate(state);
        assert polyad.getResult().equals(result);

    }


    public void testNumberTrim() throws Exception {
        State state = testUtils.getNewState();
        VStack symbolTable = state.getVStack();

        Long arg = System.currentTimeMillis();
        symbolTable.put(new VThing(new XKey("arg"), arg));
        Polyad polyad = new Polyad(StringEvaluator.TRIM);
        VariableNode left = new VariableNode("arg");

        polyad.addArgument(left);
        polyad.evaluate(state);
        assert polyad.getResult().equals(arg); // no change

    }


    public void testStemTrim() throws Exception {
        State state = testUtils.getNewState();
        VStack symbolTable = state.getVStack();

        QDLStem stem = new QDLStem();
        stem.put("1", "  foo");
        stem.put("woof", "      ");
        stem.put("warp", "foo           ");
        stem.put("9", "       foo           ");
        symbolTable.put(new VThing(new XKey("stem."), stem));
        Polyad polyad = new Polyad(StringEvaluator.TRIM);
        VariableNode left = new VariableNode("stem.");

        polyad.addArgument(left);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.get("1").equals("foo");
        assert result.get("woof").equals("");
        assert result.get("warp").equals("foo");
        assert result.get("9").equals("foo");

    }


    public void testIndexOfStringString() throws Exception {
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(StringEvaluator.INDEX_OF);
        ConstantNode left = new ConstantNode("abcdef", Constant.STRING_TYPE);
        ConstantNode right = new ConstantNode("de", Constant.STRING_TYPE);
        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.getLong(0L) == 3L;
    }


    public void testIndexOfStringStem() throws Exception {
        State state = testUtils.getNewState();

        String source = "Four score and seven years ago...";
        VStack vStack = state.getVStack();

        QDLStem snippets = new QDLStem();
        snippets.put("score", "score");
        snippets.put("Four", "Four");
        snippets.put("ago", "woof");
        snippets.put("7", "seven");
        vStack.put(new VThing(new XKey("snippets."), snippets));
        vStack.put(new VThing(new XKey("source"), source));

        Polyad polyad = new Polyad(StringEvaluator.INDEX_OF);
        VariableNode left = new VariableNode("source");
        VariableNode right = new VariableNode("snippets.");

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert testOldIndexOf(result, "score", 5L);
        assert testOldIndexOf(result, "Four", 0L);
        assert testOldIndexOf(result, "7", 15L);
        assert testOldIndexOf(result, "ago", -1L);
    }


    public void testIndexOfStemString() throws Exception {
        State state = testUtils.getTestState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "One Ring to rule them all");
        sourceStem.put("find", "One Ring to find them");
        sourceStem.put("bring", "One Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        String targetString = "One";

        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("targetString"), targetString));

        Polyad polyad = new Polyad(StringEvaluator.INDEX_OF);
        polyad.setName(StringEvaluator.INDEX_OF);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode right = new VariableNode("targetString");

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.size() == 4;
        assert testOldIndexOf(result, "rule", 0L);
        assert testOldIndexOf(result, "find", 0L);
        assert testOldIndexOf(result, "bind", -1L);
        assert testOldIndexOf(result, "bring", 0L);
    }

    private boolean testOldIndexOf(QDLStem result, String key, Long index) {
        // Really awkward not to do it in QDL
        QDLStem stemVariable = (QDLStem) result.get(key);
        Object obj1 = stemVariable.get(0L);
        return index.equals(obj1);
    }

    public void testIndexOf() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. ≔ index_of('qwasdwerasdrrasdvvasderasd','asd') == [2,8,13,18,23];");
        addLine(script, "y. ≔ index_of('aaaaaaa','a') == [0,1,2,3,4,5,6] ;");
        addLine(script, "ok := reduce(@&&, x.);");
        addLine(script, "ok1 := reduce(@&&, y.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);

    }

    /**
     * This tests index_of for lists where there are gaps. This is taken from a script where this
     * was bombing
     *
     * @throws Throwable
     */
    public void testIndexOfGaps() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "tx_scopes.≔['org.cilogon.userinfo','openid','profile','email','wlcg.capabilityset:/duneetf','offline_access'];");
        addLine(script, "CS_HEAD ≔'wlcg.capabilityset:/';");
        addLine(script, "x. ≔ mask(tx_scopes., starts_with(tx_scopes., CS_HEAD)!= 0);"); // CS_HEAD removed, so gap in indices
        addLine(script, "y. ≔ starts_with(x., 'wlcg.groups:');"); // all are -1, but there are gaps in the indices
        addLine(script, "ok := reduce(@&&, y. == -1);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testIndexOfStemString_ignoreCase() throws Exception {
        State state = testUtils.getTestState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "one Ring to rule them all");
        sourceStem.put("find", "onE Ring to find them");
        sourceStem.put("bring", "oNE Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        String targetString = "ONE";

        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("targetString"), targetString));

        Polyad polyad = new Polyad(StringEvaluator.INDEX_OF);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode right = new VariableNode("targetString");
        ConstantNode ignoreCase = new ConstantNode(Boolean.FALSE, Constant.BOOLEAN_TYPE);

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.addArgument(ignoreCase);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.size() == 4;
        assert testOldIndexOf(result, "rule", 0L);
        assert testOldIndexOf(result, "find", 0L);
        assert testOldIndexOf(result, "bind", -1L);
        assert testOldIndexOf(result, "bring", 0L);
    }


    public void testIndexOfStemStem() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "One Ring to rule them all");
        sourceStem.put("find", "One Ring to find them");
        sourceStem.put("bring", "One Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        QDLStem targetStem = new QDLStem();
        targetStem.put("all", "all");
        targetStem.put("One", "One");
        targetStem.put("bind", "darkness");
        targetStem.put("7", "seven");
        vStack.put(new VThing(new XKey("snippets."), targetStem));
        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("targetStem."), targetStem));

        Polyad polyad = new Polyad(StringEvaluator.INDEX_OF);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode right = new VariableNode("targetStem.");

        polyad.addArgument(left);
        polyad.addArgument(right);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        // The contract is that the answer is conformable to the left argument, so only keys from the left
        // argument appear in the result.
        assert result.size() == 1;
        //assert result.getLong("bind") == 11L;
        assert testOldIndexOf(result, "bind", 11L);

    }
       /*
         q.'rule':='One Ring to rule them all'
  q.'find' := 'One Ring to find them'
  q.'bring' :='One Ring to bring them all'
  q.'bind' := 'and in the darkness bind them'
  z.'all' := 'all'
  z.'One' := 'One';
  z.'bind' := 'darkness'
  z.7 := 'seven'
  index_of(q., z.)
        */

    public void testStringToUpper() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        String arg = "mairzy doats";
        vStack.put(new VThing(new XKey("arg"), arg));
        String result = "MAIRZY DOATS";
        Polyad polyad = new Polyad(StringEvaluator.TO_UPPER);
        VariableNode left = new VariableNode("arg");

        polyad.addArgument(left);
        polyad.evaluate(state);
        assert polyad.getResult().equals(result);

    }


    public void testStringToLower() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        String arg = "MAIRZY DOATS";
        vStack.put(new VThing(new XKey("arg"), arg));
        String result = "mairzy doats";
        Polyad polyad = new Polyad(StringEvaluator.TO_LOWER);
        VariableNode left = new VariableNode("arg");

        polyad.addArgument(left);
        polyad.evaluate(state);
        assert polyad.getResult().equals(result);

    }


    public void testStemToLower() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        QDLStem stem = new QDLStem();
        String arg1 = "HAlt WHO";
        String arg2 = "GoeS";
        String arg3 = "THeRe";
        String arg4 = "donCHa know?";
        stem.put("1", arg1);
        stem.put("woof", arg2);
        stem.put("warp", arg3);
        stem.put("9", arg4);
        vStack.put(new VThing(new XKey("stem."), stem));
        Polyad polyad = new Polyad(StringEvaluator.TO_LOWER);
        VariableNode left = new VariableNode("stem.");

        polyad.addArgument(left);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.get("1").equals(arg1.toLowerCase());
        assert result.get("woof").equals(arg2.toLowerCase());
        assert result.get("warp").equals(arg3.toLowerCase());
        assert result.get("9").equals(arg4.toLowerCase());

    }


    public void testStemToUpper() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        QDLStem stem = new QDLStem();
        String arg1 = "HAlt WHO";
        String arg2 = "GoeS";
        String arg3 = "THeRe";
        String arg4 = "donCHa know?";
        stem.put("1", arg1);
        stem.put("woof", arg2);
        stem.put("warp", arg3);
        stem.put("9", arg4);
        vStack.put(new VThing(new XKey("stem."), stem));
        Polyad polyad = new Polyad(StringEvaluator.TO_UPPER);
        VariableNode left = new VariableNode("stem.");

        polyad.addArgument(left);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        assert result.get("1").equals(arg1.toUpperCase());
        assert result.get("woof").equals(arg2.toUpperCase());
        assert result.get("warp").equals(arg3.toUpperCase());
        assert result.get("9").equals(arg4.toUpperCase());

    }


    public void testAllStringReplace() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'abcholy cowf' == replace('abcdef','de','holy cow');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testStringReplace() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'xxcyy' == replace('abcde',{'ab':'xx','de':'yy'});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testStringReplace2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "out. := replace(['abccc','pqr','wwdede'],{'ab':'xx','de':'yy'});");
        addLine(script, "ok := reduce(@&&,['xxccc','pqr','wwyyyy']==out.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testStringReplace2a() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "out. := replace(['abcde','pqrcde','ababcdcd'],['ab','cd'],['xx','yy']);");
        addLine(script, "ok := reduce(@&&,['xxyye','pqryye','xxxxyyyy']==out.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Tests replace with a regex that removes duplicate spaces.
     *
     * @throws Throwable
     */
    public void testStringReplaceWithRegex() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'there are too many spaces' == replace('there   are too many   spaces', ' {2,}', ' ',true);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * Replace test to show application of replace to a general stem.
     *
     * @throws Throwable
     */
    public void testStringReplace3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        String source = "s.:={'bind':'and in the darkness bind them', 'bring':'One Ring to bring them all', 'find':'One Ring to find them', 'rule':'One Ring to rule them all'};";
        String target = "t.:={7:'seven', 'One':'One', 'all':'All', 'bind':'darkness'};";
        String out = "u.:={'bind':'and in the darkness darkness them', 'bring':'One Ring to bring them All', 'find':'One Ring to find them', 'rule':'One Ring to rule them All'};";
        addLine(script, source);
        addLine(script, target);
        addLine(script, out);

        addLine(script, "ok := ⊗∧⊙u. == replace(s., t.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }


    public void testInsertStringString() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := 'abcGAAH!def' == insert('abcdef','GAAH!',3);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * This takes a stem and a string and a single index and insert the same
     * String at the same place.
     *
     * @throws Exception
     */

    public void testInsertStemString() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "One Ring to rule them all");
        sourceStem.put("find", "One Ring to find them");
        sourceStem.put("bring", "One Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        String newS = "GAAH!";
        ConstantNode snippet = new ConstantNode(newS, Constant.STRING_TYPE);
        ConstantNode index = new ConstantNode(new Long(3L), Constant.LONG_TYPE);

        /**/
        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));

        Polyad polyad = new Polyad(StringEvaluator.INSERT);
        VariableNode source = new VariableNode("sourceStem.");
        polyad.addArgument(source);
        polyad.addArgument(snippet);
        polyad.addArgument(index);

        polyad.evaluate(state);
        QDLStem stem = (QDLStem) polyad.getResult();
        assert stem.get("rule").toString().startsWith("One" + newS);
        assert stem.get("find").toString().startsWith("One" + newS);
        assert stem.get("bring").toString().startsWith("One" + newS);
        assert stem.get("bind").toString().startsWith("and" + newS);
    }

    /**
     * This takes a set of stems, another set of stems and a set of indices then does insertions
     * Part of the test is that not all stems have all the same keys, so the result is a subset.
     *
     * @throws Exception
     */

    public void testInsertStemStem() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        sourceStem.put("rule", "One Ring to rule them all");
        sourceStem.put("find", "One Ring to find them");
        sourceStem.put("bring", "One Ring to bring them all");
        sourceStem.put("bind", "and in the darkness bind them");
        QDLStem snippets = new QDLStem();
        snippets.put("all", "all");
        snippets.put("One", "One");
        snippets.put("bind", "darkness");
        snippets.put("7", "seven");
        QDLStem indices = new QDLStem();
        indices.setDefaultValue(new Long(4L));
        // This sticks the work "darkness" in the string associated with the key bind
        String expectedResult = "and darknessin the darkness bind them";
        vStack.put(new VThing(new XKey("sourceStem."), sourceStem));
        vStack.put(new VThing(new XKey("snippets."), snippets));
        vStack.put(new VThing(new XKey("indices."), indices));

        Polyad polyad = new Polyad(StringEvaluator.INSERT);
        VariableNode left = new VariableNode("sourceStem.");
        VariableNode snippetVar = new VariableNode("snippets.");
        VariableNode indexVar = new VariableNode("indices.");

        polyad.addArgument(left);
        polyad.addArgument(snippetVar);
        polyad.addArgument(indexVar);
        polyad.evaluate(state);
        QDLStem result = (QDLStem) polyad.getResult();
        // The contract is that the answer is conformable to the left argument, so only keys from the left
        // argument appear in the result.

        assert result.size() == 1;
        assert result.getString("bind").startsWith(expectedResult);
    }


    public void testDetokenize() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := [;5];");
        addLine(script, "t0 := detokenize(a., ':');");
        addLine(script, "t1 := detokenize(a., 'k=', 1);"); // prepend
        addLine(script, "t2 := detokenize(a., ':', 2);"); // omit dangling
        addLine(script, "t3 := detokenize(a., 'k=', 3);"); // omit dangling and prepend
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStringValue("t0", state).equals("0:1:2:3:4");
        assert getStringValue("t1", state).equals("k=0k=1k=2k=3k=4");
        assert getStringValue("t2", state).equals("0:1:2:3:4");
        assert getStringValue("t3", state).equals("0k=1k=2k=3k=4");
    }


    public void testSubstring() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := substring('abcd', 2);"); // rest of string starting at 2
        addLine(script, "b := substring('abcd', 1,2);"); // two chars, starting at 1
        addLine(script, "c := substring('abcd', 1, 10,'.');"); // 5 chars starting at 1 + padding
        addLine(script, "d := substring('abcd', 1, 1000);"); // rest of string starting at 1 since 1000 > length
        addLine(script, "f := substring('abcdefg', 3, 11,'pqr');"); // cyclical extension
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStringValue("a", state).equals("cd");
        assert getStringValue("b", state).equals("bc");
        assert getStringValue("c", state).equals("bcd.......");
        assert getStringValue("d", state).equals("bcd");
        assert getStringValue("f", state).equals("defgpqrpqrp");
    }

    /**
     * Tests that using a regex to tokenize a string with multiple types of delimiters works.
     *
     * @throws Throwable
     */

    public void testRegexTokenize() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // delimiters are space , .
        addLine(script, "x. := tokenize('a d, m, i.n','\\\\s+|,\\\\s*|\\\\.\\\\s*' ,true);"); // rest of string starting at 2
        addLine(script, "y := reduce(@∧, x. == ['a','d','m','i','n']);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("y", state);

    }


    public void testRegexMatches() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x ≔ '[a-zA-Z]{3}' =~ 'aBc';"); // Check that rhs has 3 letters
        addLine(script, "y ≔ '[Yy][Ee][Ss]' =~ 'yEs';"); // checks rhs is case insensitive 'yes'
        addLine(script, "z ≔ '[0-9]{5}' =~ 23456;"); // checks rhs integer is treated as string, then checked for 5 digit.

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("x", state);
        assert getBooleanValue("y", state);
        assert getBooleanValue("z", state);
    }

    /**
     * Checks a few random unicode escapes mostly as regression if we break it.
     *
     * @throws Throwable
     */

    public void testUnicodeEscapes() throws Throwable {
        // π
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x ≔ '\u03c0' == 'π';");
        addLine(script, "y ≔ '≔' == '\u2254';");
        addLine(script, "z ≔ '∧' == '\u2227';");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("x", state);
        assert getBooleanValue("y", state);
        assert getBooleanValue("z", state);

    }


    public void testDiff() throws Throwable {
        // π
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 ≔ differ_at('abcde', 'ab') == 2;");
        addLine(script, "ok1 ≔ differ_at('abcde', 'abcde') == -1;");
        addLine(script, "ok2 ≔ reduce(@∧, differ_at(['abcd','efghij'],['abq','efgp'])==[2,3]);");
        addLine(script, "ok3 ≔ reduce(@∧, differ_at(['abcde','abed'], 'abcq')==[3,2]);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "diff failed for two strings";
        assert getBooleanValue("ok1", state) : "diff equality failed";
        assert getBooleanValue("ok2", state) : "diff of two stems failed";
        assert getBooleanValue("ok3", state) : "diff of stem and string failed";

    }

    public void testStringMultiplication() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok ≔ 'aaa' == 3*'a';");
        addLine(script, "ok1 ≔ 'bbbb' == 'b'*4;");
        addLine(script, "ok2 ≔ '' == 0*'b';");
        addLine(script, "ok3 ≔ 'arba'<3*'bar';");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "pre-multiplication failed for strings";
        assert getBooleanValue("ok1", state) : "post-multiplication failed for strings";
        assert getBooleanValue("ok2", state) : "multiplication by zero failed for strings";
        assert getBooleanValue("ok3", state) : "substring check for multiplication of int * strings";
    }

    public void testStringComparisons() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok ≔ 'abc' < 'abcd';");
        addLine(script, "ok1 ≔ 'abcd' <= 'abcd';");
        addLine(script, "ok2 ≔ !('abd' <= 'abcd');");
        addLine(script, "ok3 ≔ 'abcd'>'abc';");
        addLine(script, "ok4 ≔ 'abcd'>='abc';");
        addLine(script, "ok5 ≔ !('abcd'>='abcde');");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert getBooleanValue("ok4", state);
        assert getBooleanValue("ok5", state);
    }

    public void testEncodeAndDecode() throws Throwable {
        // π
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a := 'foo &&*bar baz.';");
        //input_form(@encode∀[a,[0,1,16,32,64]])
        addLine(script, "v. := ['foo$20$26$26$2Abar$20baz$2E','foo%20%26%26*bar%20baz.','666f6f2026262a6261722062617a2e','MZXW6IBGEYVGEYLSEBRGC6RO','Zm9vICYmKmJhciBiYXou'];");
        addLine(script, "f(x,k)->encode(a,k)==x && decode(encode(a,k),k) == a;");
        addLine(script, "ok0 := f(v.0, 0);");
        addLine(script, "ok1 := f(v.1, 1);");
        addLine(script, "ok16 := f(v.2, 16);");
        addLine(script, "ok32 := f(v.3, 32);");
        addLine(script, "ok64 := f(v.4, 64);");
/*        addLine(script, "ok1 ≔ encode(a,0) == v.0;");
        addLine(script, "ok4 ≔ decode(v.0, 0) == a;");
        addLine(script, "ok2 ≔ encode(a, 1) == v.1;"); // URL encode
        addLine(script, "ok3 ≔ decode(v.1, 1) == a;");
        addLine(script, "ok16 ≔ decode(encode(a, 16), 16) ;");
        addLine(script, "ok32 ≔ decode(encode(a, 32), 32) ;");
        addLine(script, "ok64 ≔ decode(encode(a, 64), 64) ;");*/

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok16", state);
        assert getBooleanValue("ok32", state);
        assert getBooleanValue("ok64", state);

    }
}
