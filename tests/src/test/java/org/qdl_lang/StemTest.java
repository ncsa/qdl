package org.qdl_lang;

import org.qdl_lang.evaluate.ListEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.evaluate.StemEvaluator;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.generated.QDLParserParser;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.*;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.qdl_lang.parsing.QDLListener;
import org.qdl_lang.variables.values.QDLKey;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.qdl_lang.variables.StemUtility.put;
import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/17/20 at  12:51 PM
 */
public class StemTest extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();


    public void testSizeStem() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");

        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));

        Polyad polyad = new Polyad(StemEvaluator.SIZE);
        VariableNode arg = new VariableNode("sourceStem.");

        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().asLong() == 4L;
    }


    public void testListKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");

        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));

        Polyad polyad = new Polyad(StemEvaluator.LIST_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");

        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().isStem();
        QDLStem result = polyad.getResult().asStem();
        for (int i = 0; i < 4; i++) {
            String key = Integer.toString(i);
            assert sourceStem.containsKey(result.get(key));
        }
    }


    public void testKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");

        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));

        Polyad polyad = new Polyad(StemEvaluator.KEYS);
        VariableNode arg = new VariableNode("sourceStem.");

        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().isStem();

        QDLStem result = polyad.getResult().asStem();
        assert result.size() == sourceStem.size();
        for (QDLKey key : sourceStem.keySet()) {
            assert result.containsKey(result.get(key));
            assert result.get(key).getValue().equals(key.getValue());
        }
    }


    public void testSizeString() throws Exception {
        String input = "One Ring to rule them all, One Ring to find them";

        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(StemEvaluator.SIZE);
        ConstantNode arg = new ConstantNode(asQDLValue(input));
        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().asLong() == input.length();
    }


    public void testSizeLong() throws Exception {
        State state = testUtils.getNewState();

        Polyad polyad = new Polyad(StemEvaluator.SIZE);
        ConstantNode arg = new ConstantNode(asQDLValue(123456L));
        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().asLong() == 0L;
    }


    public void testSizeKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");

        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        Polyad polyad = new Polyad(StemEvaluator.LIST_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");
        polyad.addArgument(arg);
        polyad.evaluate(state);
        QDLStem keys = polyad.getResult().asStem();
        assert keys.size() == 4;
        assert keys.containsKey("0");
        assert keys.containsKey("1");
        assert keys.containsKey("2");
        assert keys.containsKey("3");
        for (Object key : keys.keySet()) {
            assert sourceStem.containsKey(keys.get(key));
        }
    }


    public void testCommonKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();


        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");

        QDLStem sourceStem2 = new QDLStem();
        put(sourceStem2,"rule", "mairzy doats");
        put(sourceStem2,"find", "and dozey");
        put(sourceStem2,"bring", "doats");
        put(sourceStem2,"3", "and in the darkness bind them");
        put(sourceStem2,"5", "whatever");


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        vStack.put(new VThing(new XKey("sourceStem2."), new QDLVariable(sourceStem2)));
        Polyad polyad = new Polyad(StemEvaluator.COMMON_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");
        VariableNode arg2 = new VariableNode("sourceStem2.");
        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        QDLStem keys = polyad.getResult().asStem();
        assert keys.size() == 3;
        assert keys.containsValue("rule");
        assert keys.containsValue("find");
        assert !keys.containsValue("bind");
        assert keys.containsValue("bring");

    }


    public void testIncludeKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();


        QDLStem sourceStem = new QDLStem();
        QDLStem keys = new QDLStem();
        int count = 5;
        int j = 0;
        MetaCodec codec = new MetaCodec();
        for (int i = 0; i < 2 * count; i++) {
            String key = geter();
            if (0 == i % 2) {
                keys.put(from(j++), key);
            }
            sourceStem.put(from(key), geter());
        }


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        vStack.put(new VThing(new XKey("keys."), new QDLVariable(keys)));
        Polyad polyad = new Polyad(StemEvaluator.INCLUDE_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");
        VariableNode arg2 = new VariableNode("keys.");
        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        QDLStem result = polyad.getResult().asStem();
        assert result.size() == count;
        for (int i = 0; i < count; i++) {
            assert result.containsKey(keys.getString(Integer.toString(i)));
        }
    }

    /**
     * Test the keys() commands for filtering using the parser.
     *
     * @throws Throwable
     */

    public void testParserKeyFiltering() throws Throwable {
        String cf = " a. := ['a',null,['x','y'],2]~{'p':123.34, 'q': -321, 'r':false};";
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, cf);
        addLine(script, "b. := keys(a., 0);");    // null
        addLine(script, "c. := keys(a., 1);");    // boolean
        addLine(script, "d. := keys(a., 2);");    // integer
        addLine(script, "e. := keys(a., 3);");    // string
        addLine(script, "f. := keys(a., 4);");    // stem
        addLine(script, "g. := keys(a., 5);");    //decimal
        addLine(script, "h. := keys(a., true);");    //scalars only
        addLine(script, "i. := keys(a., false);");    // stems only
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getStemValue("b.", state).size() == 1;
        assert getStemValue("b.", state).get(1L).asLong() == 1L;

        assert getStemValue("c.", state).size() == 1;
        assert getStemValue("c.", state).get("r").asString().equals("r");

        assert getStemValue("d.", state).size() == 2;
        assert getStemValue("d.", state).get(3L).asLong() == 3L;
        assert getStemValue("d.", state).get("q").equals("q");

        assert getStemValue("e.", state).size() == 1;
        assert getStemValue("e.", state).get(0L).asLong() == 0L;

        assert getStemValue("f.", state).size() == 1;
        assert getStemValue("f.", state).get(2L).asLong() == 2L;

        assert getStemValue("g.", state).size() == 1;
        assert getStemValue("g.", state).get("p").equals("p");

        assert getStemValue("h.", state).size() == 6;
        assert getStemValue("h.", state).get("p").equals("p");
        assert getStemValue("h.", state).get("q").equals("q");
        assert getStemValue("h.", state).get("r").equals("r");
        assert getStemValue("h.", state).get(0L).asLong() == 0L;
        assert getStemValue("h.", state).get(1L).asLong() == 1L;
        assert getStemValue("h.", state).get(3L).asLong() == 3L;

        assert getStemValue("i.", state).size() == 1;
        assert getStemValue("i.", state).get(2L).asLong() == 2L;

    }

    public void testParserListKeyFiltering() throws Throwable {
        String cf = " a. := ['a',null,['x','y'],2]~{'p':123.34, 'q': -321, 'r':false};";
        String cf2;
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, cf);
        addLine(script, "b. := list_keys(a., 0);");    // null
        addLine(script, "c. := list_keys(a., 1);");    // boolean
        addLine(script, "d. := list_keys(a., 2);");    // integer
        addLine(script, "e. := list_keys(a., 3);");    // string
        addLine(script, "f. := list_keys(a., 4);");    // stem
        addLine(script, "g. := list_keys(a., 5);");    //decimal
        addLine(script, "h. := list_keys(a., true);");    //scalars only
        addLine(script, "i. := list_keys(a., false);");    // stems only
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());

        assert getStemValue("b.", state).size() == 1;
        assert getStemValue("b.", state).get(0L).asLong() == 1L;

        assert getStemValue("c.", state).size() == 1;
        assert getStemValue("c.", state).get(0L).equals("r");

        assert getStemValue("d.", state).size() == 2;
        assert getStemValue("d.", state).get(0L).asLong() == 3L;
        assert getStemValue("d.", state).get(1L).equals("q");

        assert getStemValue("e.", state).size() == 1;
        assert  getStemValue("e.", state).get(0L).asLong() == 0L;

        assert getStemValue("f.", state).size() == 1;
        assert getStemValue("f.", state).get(0L).asLong() == 2L;

        assert getStemValue("g.", state).size() == 1;
        assert getStemValue("g.", state).get(0L).equals("p");

        assert getStemValue("h.", state).size() == 6;
        assert getStemValue("h.", state).get(0L).asLong() == 0L;
        assert getStemValue("h.", state).get(1L).asLong() == 1L;
        assert getStemValue("h.", state).get(2L).asLong() == 3L;
        assert getStemValue("h.", state).get(3L).equals("p");
        assert getStemValue("h.", state).get(4L).equals("q");
        assert getStemValue("h.", state).get(5L).equals("r");

        assert getStemValue("i.", state).size() == 1;
        assert getStemValue("i.", state).get(0L).asLong() == 2L;

    }


    public void testRemapRenameKeys2() throws Throwable {
        // use two args
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ.  := ['Ur-A5sk','QCmVTOY','kmglAq8','3d2LTC4','lWkOYqU','GBzwNLo','hnWb1C8','7FCUL9U'];");
        addLine(script, "ξ0. := remap(ξ., [5,3,4,2,0,1,7,6]);");
        addLine(script, "ok := reduce(@&&, ξ0.== ['GBzwNLo','3d2LTC4','lWkOYqU','kmglAq8','Ur-A5sk','QCmVTOY','7FCUL9U','hnWb1C8']);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.REMAP + " operator failed for simple list.";
    }

    public void testRemapRenameKeys3() throws Throwable {
        // use 3 args
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // remaps [0,2,4,6,8] to having odd numbers .
        addLine(script, "ξ.  := remap(2*[;5], [;5],  1+3*[;5]);");
        addLine(script, "ok := reduce(@&&, ξ.== {1:0, 4:2, 7:4, 10:6, 13:8});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.REMAP + " operator failed for simple list.";
    }

    public void testRenameKeys() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := [;5];");
        addLine(script, "rename_keys(a., [42]);");
        addLine(script, "ok := reduce(@∧, a. == {1:1, 2:2, 3:3, 4:4, 42:0});");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.RENAME_KEYS + " did not rename keys correctly.";
    }

    /*
     b.'a':='X'; b.x_y := 'Y';
     ndx. := {'x_y':'x'};
     rename_keys(b., ndx.)

     c.'x':='X'; c.x_y := 'Y';
     ndx. := {'x_y':'x'};
     rename_keys(c., ndx.); //fails
     rename_keys(c., ndx., true); //works

     */
    public void testRenameKeysNoOverWrite() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "c.'x':='X'; c.x_y := 'Y';");
        addLine(script, "ndx. := {'x_y':'x'};");
        addLine(script, "rename_keys(c., ndx.); ");  // fails
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean passed = false;
        try {
            interpreter.execute(script.toString());
        } catch (QDLExceptionWithTrace QDlExceptionWithTrace) {
            passed = (QDlExceptionWithTrace.getCause() instanceof IllegalArgumentException);
        }
        assert passed : "was able to rename keys in a destructive way";
    }

    public void testRenameKeysWithOverWrite() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "c.'x':='X'; c.x_y := 'Y';");
        addLine(script, "ndx. := {'x_y':'x'};");
        addLine(script, "z.:=rename_keys(c., ndx., true);");
        addLine(script, "ok := size(z.)==1 && z.'x' == 'Y';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.RENAME_KEYS + " failed when overwrite enabled.";
    }

    /**
     * This is a direct test that a claims-like stem gets handled correctly since it is a very
     * common use case in practice. In this case, it replaces an older sub claim and renames
     * the isMemberOf claim. There is an extra claim (eppn) to check that <code>rename_keys</code>
     * does not lose a claim.
     *
     * @throws Throwable
     */
    public void testRenameKeysWithOverWrite2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "claims.sub:='X';claims.vop:='Y';claims.isMemberOf:=['A','B'];claims.eppn:='Q';");
        addLine(script, "ndx. := {'vop':'sub', 'isMemberOf':'is_member_of'};");
        addLine(script, "rename_keys(claims., ndx., true);");
        addLine(script, "ok := size(claims.)==3 && claims.'sub' == 'Y' && size(claims.is_member_of)==2 && claims.eppn=='Q';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.RENAME_KEYS + " failed when overwrite enabled.";
    }


    public void testExcludeKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        QDLStem keys = new QDLStem();
        int count = 5;
        int j = 0;
        for (int i = 0; i < 2 * count; i++) {
            String key = geter();
            if (0 == i % 2) {
                keys.put(from(j++), key);
            }
            sourceStem.put(from(key), geter());
        }


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        vStack.put(new VThing(new XKey("keys."),new QDLVariable( keys)));
        Polyad polyad = new Polyad(StemEvaluator.EXCLUDE_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");
        VariableNode arg2 = new VariableNode("keys.");
        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        QDLStem result = polyad.getResult().asStem();
        assert result.size() == count : "incorrect size. Expected " + count + " got " + result.size();
        for (int i = 0; i < count; i++) {
            assert !result.containsKey(keys.getString(Integer.toString(i)));
        }
    }


    public void testExcludeScalarKey() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();


        QDLStem sourceStem = new QDLStem();
        QDLStem keys = new QDLStem();
        String targetKey = geter();

        put(sourceStem, targetKey, geter());
        int count = 5;
        for (int i = 0; i < count; i++) {
            String key = geter();
            put(sourceStem, key, geter());
        }


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        Polyad polyad = new Polyad(StemEvaluator.EXCLUDE_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");
        ConstantNode arg2 = new ConstantNode(asQDLValue(targetKey));
        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        QDLStem result = polyad.getResult().asStem();
        assert result.size() == count; // we added one, then removed it.
        assert !result.containsKey(targetKey);
    }

    /**
     * Make a new stem filled with legal variable keys and values.
     *
     * @param count
     * @return
     */
    protected QDLStem randomStem(int count) {
        QDLStem s = new QDLStem();
        randomStem(s, count);
        return s;
    }

    /**
     * Add a given number of random legal varaables to a stem
     *
     * @param s
     * @param count
     * @return
     */
    protected QDLStem randomStem(QDLStem s, int count) {
        for (int i = 0; i < count; i++) {
            put(s,geter(), geter());
        }
        return s;
    }


    public void testIncludeScalarKey() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();


        QDLStem sourceStem = new QDLStem();

        String targetKey = geter();

        put(sourceStem,targetKey, geter());
        int count = 5;
        randomStem(sourceStem, count);


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        Polyad polyad = new Polyad(StemEvaluator.INCLUDE_KEYS);
        VariableNode arg = new VariableNode("sourceStem.");
        ConstantNode arg2 = new ConstantNode(asQDLValue(targetKey));
        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        QDLStem result = polyad.getResult().asStem();
        assert result.size() == 1;
        assert result.containsKey(targetKey);
    }


    public void testHasKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        int count = 5;
        QDLStem keys = new QDLStem();
        int j = 0;
        for (int i = 0; i < 2 * count; i++) {
            String key = geter();
            if (0 == i % 2) {
                put(keys,key, Integer.toString(j++));
            }
            put(sourceStem,key, geter());
        }
        // add a few that aren't in the target stem.
        for (int i = 0; i < count; i++) {
            put(keys, j++, geter());
        }
        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        vStack.put(new VThing(new XKey("keys."), new QDLVariable(keys)));
        Polyad polyad = new Polyad(StemEvaluator.HAS_KEY);
        VariableNode arg2 = new VariableNode("sourceStem.");
        VariableNode arg = new VariableNode("keys.");
        polyad.addArgument(arg2);
        polyad.addArgument(arg);
        polyad.evaluate(state);
        QDLStem result = polyad.getResult().asStem();
        assert result.size() == count * 2;
        for (Object k : sourceStem.keySet()) {
            Boolean b = result.getBoolean(k.toString()); // only string keys
            if (keys.containsKey(k)) {
                assert b : "wrong key marked as existing in has_keys";
            } else {
                assert !b : "wrong key markes as abset in has_keys";
            }
        }
    }


    public void testHasScalarKeys() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();

        QDLStem sourceStem = new QDLStem();
        QDLStem keys = new QDLStem();
        int count = 5;
        String targetKey = geter();
        put(sourceStem, targetKey, geter());
        int j = 0;
        for (int i = 0; i < count; i++) {
            String key = geter();
            if (0 == i % 2) {
                put(keys, j++, key);
            }
            put(sourceStem, key, geter());
        }
        // add a few that aren't in the target stem.
        for (int i = 0; i < count; i++) {
            put(keys, j++, geter());
        }
        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        vStack.put(new VThing(new XKey("keys."), new QDLVariable(keys)));
        Polyad polyad = new Polyad(StemEvaluator.HAS_KEY);
        VariableNode arg = new VariableNode("sourceStem.");
        ConstantNode arg2 = new ConstantNode(asQDLValue(targetKey));
        polyad.addArgument(arg2);
        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert polyad.getResult().asBoolean();
    }


    public void testmakeIndex() throws Exception {
        State state = testUtils.getNewState();
        Polyad polyad = new Polyad(StemEvaluator.SHORT_MAKE_INDICES);
        ConstantNode arg = new ConstantNode(asQDLValue(4L));
        polyad.addArgument(arg);
        polyad.evaluate(state);
        QDLStem indices = polyad.getResult().asStem();
        assert indices.containsKey("0");
        assert indices.containsKey("1");
        assert indices.containsKey("2");
        assert indices.containsKey("3");
        assert indices.getLong("0").equals(0L);
        assert indices.getLong("1").equals(1L);
        assert indices.getLong("2").equals(2L);
        assert indices.getLong("3").equals(3L);
    }


    public void testRemoveStem() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");

        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));

        Polyad polyad = new Polyad(StemEvaluator.REMOVE);
        VariableNode arg = new VariableNode("sourceStem.");

        polyad.addArgument(arg);
        polyad.evaluate(state);
        assert !vStack.containsKey(new XKey("sourceStem."));
    }


    public void testdefaultValue() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();


        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable((sourceStem))));
        Polyad polyad = new Polyad(StemEvaluator.SET_DEFAULT);
        Long expectedResult = new Long(42L);
        VariableNode arg = new VariableNode("sourceStem.");
        ConstantNode arg2 = new ConstantNode(asQDLValue(expectedResult));
        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        assert polyad.getResult().isNull();
        assert sourceStem.getDefaultValue().equals(expectedResult);
        assert sourceStem.get("foo").equals(expectedResult);
    }


    public void testBadValue() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();


        QDLStem sourceStem = new QDLStem();
        put(sourceStem,"rule", "One Ring to rule them all");
        put(sourceStem,"find", "One Ring to find them");
        put(sourceStem,"bring", "One Ring to bring them all");
        put(sourceStem,"bind", "and in the darkness bind them");


        vStack.put(new VThing(new XKey("sourceStem."), new QDLVariable(sourceStem)));
        Polyad polyad = new Polyad(StemEvaluator.SET_DEFAULT);
        VariableNode arg = new VariableNode("sourceStem.");
        polyad.addArgument(arg);
        polyad.addArgument(arg); // set second to be a stem so it fails
        polyad.evaluate(state); // Should work.
        assert polyad.getResult().isNull();
    }


    public void testDefault_NoStem() throws Exception {
        State state = testUtils.getNewState();
        Long expectedResult = new Long(42L);
        Polyad polyad = new Polyad(StemEvaluator.SET_DEFAULT);
        VariableNode arg = new VariableNode("sourceStem.");
        ConstantNode arg2 = new ConstantNode(asQDLValue(expectedResult));

        polyad.addArgument(arg);
        polyad.addArgument(arg2);
        polyad.evaluate(state);
        QDLStem sourceStem = ((VThing) state.getVStack().get(new XKey("sourceStem."))).getStemValue();
        assert polyad.getResult().isNull();
        assert sourceStem.getDefaultValue().equals(expectedResult);
        assert sourceStem.get("foo").equals(expectedResult);
        assert !sourceStem.containsKey("foo");

    }

    public void testSetDefault() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := null == set_default(ϱ., [1,2]);"); // old value is null
        addLine(script, "ok1 := reduce(⊗∧, [1,2]== set_default(ϱ., [3,4]));");
        addLine(script, "ok2 := reduce(⊗∧, [3,4]== ϱ.7.8);");// random element returns default
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "set_default did not return null";
        assert getBooleanValue("ok1", state) : "set_default did not return previous default";
        assert getBooleanValue("ok2", state) : "set_default did not set default for stem";
    }

    /**
     * Sets a default and shows that for a complex stem it works on each element
     *
     * @throws Throwable
     */
    public void testEvaluteSetDefault() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "set_default(ϱ., [2,3]);"); // set default
        addLine(script, "a. := [[0,0],[0,1],[0,2],[2,0],[2,1],[2,2]];"); // target of operation
        addLine(script, "result. := [[2,3],[2,4],[2,5],[4,3],[4,4],[4,5]];"); // expected result
        addLine(script, "ok := reduce(⊗∧,reduce(⊗∧, result.== a. + ϱ.));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "set_default did not propagte on addition";
    }

    /**
     * Regression test for converting stems to JSON where there is a stem list of stems.
     * Extra elements were being added.
     *
     * @throws Exception
     */

    public void testJSONArray() throws Exception {
        String rawJSON = "{\n" +
                "  \"isMemberOf\":   [" +
                "  {\n" +
                "      \"name\": \"all_users\",\n" +
                "      \"id\": 13002\n" +
                "    },\n" +
                "        {\n" +
                "      \"name\": \"staff_reporting\",\n" +
                "      \"id\": 16405\n" +
                "    },\n" +
                "        {\n" +
                "      \"name\": \"list_allbsu\",\n" +
                "      \"id\": 18942\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        QDLStem stemVariable = new QDLStem();
        stemVariable.fromJSON(JSONObject.fromObject(rawJSON));

        JSON j = stemVariable.toJSON();
        QDLStem x = stemVariable.get("isMemberOf.").asStem();
        assert x.size() == 3;
        assert x.containsKey("0") : "Spurious element added to stem on serialization to JSON.";
        assert x.containsKey("1") : "Spurious element added to stem on serialization to JSON.";
        assert x.containsKey("2") : "Spurious element added to stem on serialization to JSON.";

    }

    /**
     * Critical test. This shows that a stem with integer indices (so it looks like a list)
     * gets converted completely faithfully.
     * This is a different structure than a JSON object in that the list elements 0,1,2,3 are
     * just entries, so it is necessary to show the entries end up as part of the JSON object and
     * are not stashed in a list someplace.
     *
     * @throws Exception
     */

    public void testMixedJSON() throws Exception {
        QDLStem s = new QDLStem();
        String name = "bob";
        String issuer = "https://localhost:9443/oauth2";
        String tokenID = "https://localhost:9443/oauth2/idToken/7e3318d9e03b19a2a38ba88542abab0a/1591271860588";
        put(s,"sub", name);
        put(s,"iss", issuer);
        put(s,"token_id", tokenID);
        put(s,0L, 3L);
        put(s,1L, "foo");
        put(s,2L, new BigDecimal("23.4"));
        put(s,3L, Boolean.TRUE);
        JSON json = s.toJSON();
        assert json instanceof JSONObject;
        JSONObject jo = (JSONObject) json;
        assert jo.size() == s.size();
        assert jo.getString("sub").equals(name);
        assert jo.getString("iss").equals(issuer);
        assert jo.getString("token_id").equals(tokenID);
        assert jo.getLong("0") == 3L;
        assert jo.getString("1").equals("foo");
        assert jo.getDouble("2") == 23.4;
        assert jo.getBoolean("3");
    }

    /**
     * Test that arrays are faithfully translated to and from stems
     *
     * @throws Exception
     */

    public void testJSONArray2() throws Exception {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 2 * 2; i++) {
            array.add(makeRandomArray(i));
        }
        verifyJSONArrayRoundtrip(array);
    }

    protected void verifyJSONArrayRoundtrip(JSONArray array) {
        // options are this has single strings as elements or JSON Arrays of strings
        QDLStem stemVariable = new QDLStem();
        stemVariable.fromJSON(array);
        JSON json = stemVariable.toJSON();
        assert json instanceof JSONArray : "Did not get back a JSON array";
        JSONArray array2 = (JSONArray) json;
        for (int i = 0; i < array.size(); i++) {
            Object temp = array.get(i);
            if (temp instanceof JSONArray) {
                JSONArray innerA = (JSONArray) temp;
                for (int j = 0; j < innerA.size(); j++) {
                    assert array2.getJSONArray(i).getString(j).equals(innerA.getString(j));
                }

            } else {
                assert temp.equals(array2.get(i));
            }
        }
    }

    /**
     * This tests a mixture of arrays of arrays and single items to show that order in
     * the original list is preserved.
     *
     * @throws Exception
     */

    public void testJSONArrayOrder() throws Exception {
        JSONArray array = new JSONArray();
        for (int i = 0; i < 2 * count; i++) {
            if (0 == i % 2) {
                array.add(getRandomString());
            } else {
                array.add(makeRandomArray(i));
            }
        }
        verifyJSONArrayRoundtrip(array);
    }

    protected JSONArray makeRandomArray(int n) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < n; i++) {
            array.add(getRandomString());
        }
        return array;
    }


    public void testAddList() throws Throwable {
        QDLStem s = new QDLStem();
        ArrayList<Object> list = new ArrayList<>();
        int max = 6;
        for (int i = 0; i < max; i++) {
            list.add(i + 10L); // so we have different values from keys
        }
        s.addList(list);
        for (int i = 0; i < max; i++) {
            Long v = i + 10L;
            assert s.get(Integer.toString(i)).equals(v) : "expected " + v + " and got " + s.get(Integer.toString(i));
        }

    }


    public void testListAppend() throws Throwable {
        QDLList qdlList1 = new QDLList();
        QDLList qdlList2 = new QDLList();
        long count1 = 10L;
        long count2 = 5L;

        for (long i = 0L; i < count1; i++) {
            //qdlList1.add(new SparseEntry(i, new BigDecimal("0." + i)));
            qdlList1.add(asQDLValue(new BigDecimal("0." + i)));
        }
        for (long i = 0L; i < count2; i++) {
            //qdlList2.add(new SparseEntry(i, i * i));
            qdlList2.add(asQDLValue(i * i));
        }
        QDLStem stem1 = new QDLStem();
        QDLStem stem2 = new QDLStem();
        stem1.setQDLList(qdlList1);
        stem2.setQDLList(qdlList2);
        stem1.listAppend(stem2);
        QDLList result = stem1.getQDLList();
        // should return sorted set
        Object expectedValues[] = new Object[]{
                new BigDecimal("0.0"),
                new BigDecimal("0.1"),
                new BigDecimal("0.2"),
                new BigDecimal("0.3"),
                new BigDecimal("0.4"),
                new BigDecimal("0.5"),
                new BigDecimal("0.6"),
                new BigDecimal("0.7"),
                new BigDecimal("0.8"),
                new BigDecimal("0.9"),
                0L, 1L, 4L, 9L, 16L};
        assert stem1.size() == count1 + count2;
        for (int i = 0; i < expectedValues.length; i++) {
            assert result.get(i).equals(expectedValues[i]);
        }
    }


    public void testListCopy() throws Throwable {
        QDLList qdlList1 = new QDLList();
        QDLList qdlList2 = new QDLList();
        long count1 = 10L;
        long count2 = 5L;
        for (long i = 0L; i < count1; i++) {
            qdlList1.add(asQDLValue(new BigDecimal(Double.toString(i / 10.0))));
        }
        for (long i = 0L; i < count2; i++) {
            qdlList2.add(asQDLValue(i * i));
        }
        QDLStem stem1 = new QDLStem();
        QDLStem stem2 = new QDLStem();
        stem1.setQDLList(qdlList1);
        stem2.setQDLList(qdlList2);
        stem1.listCopy(3, 5, stem2, 2);
        QDLList result = stem2.getQDLList();
        // should return sorted set
        Object expectedValues[] = new Object[]{0L, 1L,
                new BigDecimal(".3"),
                new BigDecimal(".4"),
                new BigDecimal(".5"),
                new BigDecimal(".6"),
                new BigDecimal(".7")};
        for (int i = 0; i < expectedValues.length; i++) {
            assert result.get(i).getValue().equals(expectedValues[i]);
        }
    }

    /*
  a.:= [;5]
  a.100 := 11
  a.200 := 12
  a.300 := 14
  a.400 := 15
  a.(-2)
     */

    /**
     * Test for signed indices in stems.
     *
     * @throws Throwable
     */
    public void testRelativeIndices() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "  a.:= [;5];\n" +
                "  a.100 := 11;\n" +
                "  a.200 := 12;\n" +
                "  a.300 := 14;\n" +
                "  a.400 := 15;");
        addLine(script, "ok0 := a.(-1)==15;"); // gets the last sparse index
        addLine(script, "ok1 := a.(-2)==14;"); // gets the nest to last sparse index
        addLine(script, "ok2 := a.(-4)==11;"); // gets the first sparse index
        addLine(script, "ok3 := a.(-5)==4;"); // gets the last contiguous index
        addLine(script, "ok4 := a.(-9)==0;"); // gets the first contiguous index
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed to get last sparse entry using relative index -1";
        assert getBooleanValue("ok2", state) : "failed to get next to last sparse entry using relative index -2";
        assert getBooleanValue("ok1", state) : "failed to get first sparse entry using relative index -4";
        assert getBooleanValue("ok3", state) : "failed to get last contiguous entry for relative index -5";
        assert getBooleanValue("ok4", state) : "failed to get first contiguous entry for relative index -9";
    }

    public void testBadRelativeIndex() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "  a.:= [;5];\n" +
                "  a.100 := 11;\n" +
                "  a.200 := 12;\n" +
                "  a.300 := 14;\n" +
                "  a.400 := 15;");
        addLine(script, "a.(-100);"); // way outside range. Has to fail
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean testOK = true;
        try {
            interpreter.execute(script.toString());
            testOK = false;
        } catch (IndexError t) {
        }
        assert testOK : "attempt to get relative index outside of range should have failed";
    }

    public void testRelativeIndicesSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "  a.:= [;5];\n" +
                "  a.100 := 11;\n" +
                "  a.200 := 12;\n" +
                "  a.300 := 14;\n" +
                "  a.400 := 15;");
        addLine(script, "a.(-1):=42;");
        addLine(script, "a.(-4):=1000;");
        addLine(script, "a.(-6):=1111;");
        addLine(script, "ok0 := a.400==42;"); // gets the last sparse index
        addLine(script, "ok1 := a.100==1000;"); // gets the nest to last sparse index
        addLine(script, "ok2 := a.3==1111;"); // gets the first sparse index
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "failed to set last sparse entry using relative index -1";
        assert getBooleanValue("ok2", state) : "failed to set next to last sparse entry using relative index -4";
        assert getBooleanValue("ok1", state) : "failed to set contiguous entry using relative index -6";
    }

    public void testBadRelativeIndicesSet() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "  a.:= [;5];\n" +
                "  a.100 := 11;\n" +
                "  a.200 := 12;\n" +
                "  a.300 := 14;\n" +
                "  a.400 := 15;");
        addLine(script, "a.(-100):=0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean testOk = true;
        try {
            interpreter.execute(script.toString());
            testOk = false;
        } catch (IndexError indexError) {

        }
        assert testOk : "Was able to set relative index outside of list";
    }

    /*
    a.:=[;10];
    remove(a.3)
    b.:=[-10;0]
    test.:={0:0,1:1,2:2,4:4,5:-9,6:-8,7:-7,8:8,9:9};
    copy(b., 1,3,a., 5)
     */
    public void testSparseListCopy() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.:=[;10];");
        addLine(script, "remove(a.3);"); // remove an element
        addLine(script, "b.:=[-10;0];");
        addLine(script, "test.:={0:0,1:1,2:2,4:4,5:-9,6:-8,7:-7,8:8,9:9};");
        addLine(script, "copy(b., 1,3,a., 5);");
        addLine(script, "ok := size(a.) == size(test.) && reduce(@&&, a. == test.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to round trip JSON array with QDL nulls in it.";
    }


    public void testSparseListCopySourceFail() throws Throwable {
        // The source is missing some entries, so this should fail with an index error
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.:=[;10];");
        addLine(script, "remove(a.3);"); // remove an element
        addLine(script, "b.:=[-10;0];");
        addLine(script, "copy(a., 1,3,b., 5);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
            assert false : "Was able to copy over a gap in the source list";
        } catch (QDLException x) {
            assert true;
        }
    }

    /*
    a.:=[;10];
    remove(a.3)
    b.:=[-10;0]
    insert_at(b., 1,3,a., 5)
    test.:={0:0, 1:1, 2:2, 4:4, 5:-9, 6:-8, 7:-7, 8:5, 9:6, 10:7, 11:8, 12:9};
     */
    public void testSparseListInsert() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.:=[;10];");
        addLine(script, "remove(a.3);"); // remove an element
        addLine(script, "b.:=[-10;0];");
        addLine(script, "test.:={0:0, 1:1, 2:2, 4:4, 5:-9, 6:-8, 7:-7, 8:5, 9:6, 10:7, 11:8, 12:9};");
        addLine(script, "insert_at(b., 1,3,a., 5);");
        addLine(script, "ok := size(a.) == size(test.) && reduce(@&&, a. == test.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed sparse list insert_at.";
    }

    /*

     */
    public void testSparseListCopy2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.2 := 1;");
        addLine(script, "b.:=[-10;0];");
        addLine(script, "test.:={2:1,100:-9,101:-8,102:-7};");
        addLine(script, "copy(b., 1,3,a., 100);");
        addLine(script, "ok := size(a.) == size(test.) && reduce(@&&, a. == test.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to round trip JSON array with QDL nulls in it.";
    }

    /*
          a.:=[;10];
    remove(a.3)
    remove(a.4)
    b.:=[-10;0]
    test.:={0:0,1:1,2:2,4:4,5:-9,6:-8,7:-7,8:8,9:9};
    copy(b., 1,3,a., 2)
     */
    public void testSparseListCopySpanGap() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.:=[;10];");
        addLine(script, "remove(a.3);"); // remove an element
        addLine(script, "remove(a.4);"); // remove an element
        addLine(script, "b.:=[-10;0];");
        addLine(script, "test.:={0:0,1:1,2:-9,3:-8,4:-7, 5:5,6:6,7:7,8:8,9:9};");
        addLine(script, "copy(b., 1,3, a., 2);");
        addLine(script, "ok := size(a.) == size(test.) && reduce(@&&, a. == test.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to span gap in copy.";
    }

    public void testListInsert() throws Throwable {
        QDLList sourceSL = new QDLList();
        QDLList targetSL = new QDLList();
        long count1 = 10L;
        long count2 = 5L;
        for (long i = 0L; i < count1; i++) {
            sourceSL.append(i / 10.0);
        }
        for (long i = 0L; i < count2; i++) {
            targetSL.append(i * i);
        }
        QDLStem sourceStem = new QDLStem();
        QDLStem targetStem = new QDLStem();
        sourceStem.setQDLList(sourceSL);
        targetStem.setQDLList(targetSL);
        sourceStem.listInsertAt(2, 5, targetStem, 3);
        QDLList result = targetStem.getQDLList();
        // should return sorted set
        Object expectedValues[] = new Object[]{0L, 1L, 4L, .2, .3, .4, .5, .6, 9L, 16L};
        assert result.size() == count2 + 5; // original plus number inserted
        for (int i = 0; i < expectedValues.length; i++) {
            assert result.get(i).equals(expectedValues[i]);
        }
    }


    public void testListSubset() throws Throwable {
        QDLList sourceSL = new QDLList();
        long count1 = 10L;
        for (long i = 0L; i < count1; i++) {
            sourceSL.append(i + 20);
        }
        QDLStem sourceStem = new QDLStem();
        sourceStem.setQDLList(sourceSL);
        QDLStem targetStem = sourceStem.listSubset(2, 3);
        QDLList result = targetStem.getQDLList();
        // should return sorted set
        Object expectedValues[] = new Object[]{22L, 23L, 24L};
        assert result.size() == 3; // original plus number inserted
        for (int i = 0; i < expectedValues.length; i++) {
            assert result.get(i).equals(expectedValues[i]);
        }
    }


    public void testListSubset2() throws Throwable {
        QDLList sourceSL = new QDLList();
        long count1 = 10L;
        for (long i = 0L; i < count1; i++) {
            sourceSL.append(i + 20);
        }
        QDLStem sourceStem = new QDLStem();
        sourceStem.setQDLList(sourceSL);
        // Test copying the tail of the list from the given index.
        QDLStem targetStem = sourceStem.listSubset(7);
        QDLList result = targetStem.getQDLList();
        // should return sorted set
        Object expectedValues[] = new Object[]{27L, 28L, 29L};
        assert result.size() == 3; // original plus number inserted
        for (int i = 0; i < expectedValues.length; i++) {
            assert result.get(i).equals(expectedValues[i]);
        }
    }

    /**
     * test basic functionality that a list can be used to specify the subset
     *
     * @throws Throwable
     */
    public void testRemap3() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "r. := " + StemEvaluator.REMAP + "(3*[;15], 2*[;5]+1);");
        addLine(script, "ok := reduce(@&&, r. == [3,9,15,21,27]);");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Failed to get correct subset of a list.";
    }

    /**
     * test that a stem of simple indices can be used to do a remap.
     *
     * @throws Throwable
     */
    public void testGenericRemap() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "r. := " + StemEvaluator.REMAP + "(3*[;15], {'foo':3,'bar':5,'baz':7});");
        addLine(script, "ok := reduce(@&&, r. == {'bar':15, 'foo':9, 'baz':21});");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Failed to get correct subset of a list.";
    }

    /*
           remap(a., [[0,1],[1,1],[2,3]])
     [1,5,11]
        remap(a., {'foo':[0,1],'bar':[1,1], 'baz':[2,3]})
    {bar:5, foo:1, baz:11}
     */

    /**
     * Tests that a stem with non-integer indices can be used to extract a subset.
     *
     * @throws Throwable
     */
    public void testRemapIndexList() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := n(3,4,n(12));");
        addLine(script, "r. := " + StemEvaluator.REMAP + "(a., {'foo':[0,1],'bar':[1,1], 'baz':[2,3]});");
        addLine(script, "ok := reduce(@&&, r. == {'bar':5, 'foo':1, 'baz':11});");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Failed to get correct subset of higher rank stem an index stem.";
    }

    /**
     * Tests that a higher rank stem can have subsets extracted.
     *
     * @throws Throwable
     */
    public void testRemapIndexList1() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := n(3,4,n(12));");
        addLine(script, "r. := " + StemEvaluator.REMAP + "(a., [[0,1],[1,1],[2,3]]);");
        addLine(script, "ok := reduce(@&&, r. == [1,5,11]);");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Failed to get correct subset of higher rank stem from index list.";
    }


    /**
     * Create a mixed stem and show that the elements can be addressed and returned as a list.
     *
     * @throws Throwable
     */
    public void testRemapAddressing() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "r. := (3*[;7]-5)~{'a':42, 'b':43, 'woof':44};");
        addLine(script, "ok := reduce(@&&, " + StemEvaluator.REMAP + "(r.,'woof'~[2,5,6]~'a') == [44,1,10,13,42]);");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Failed to get correct subset with mixed list addressing.";
    }


    public void testobjectAppend() throws Throwable {
        QDLStem stemVariable = new QDLStem();
        stemVariable.listAdd(asQDLValue("foo"));
        assert stemVariable.size() == 1;
        assert stemVariable.get(0L).asString().equals("foo");
    }
    //      x := 0; y.0 := 1; z.1 := 2; w.2 := 3; w.2.0 :='a'; w.2.1 :='b';

    /**
     * Thse two tests make sure that w.z.y.x and w.z.y.x. (so long non-stem and long stem) resolutions work.
     * This creates two variables
     * <pre>
     *      w.2
     *      w.2.
     *  </pre>
     * and has a complicated resolution to get this. This requires that the multi-indices work
     * and keep track of a fair amount of state.
     *
     * @throws Throwable
     */

    public void testMultiIndex() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "x := 0; y.0 := 1; z.1 := 2;  w.2.0 :='a'; w.2.1 :='b';");
        // Point with this is that the stem resolution knows that y.0 is a stem and looks up the value of 1
        // in the resolution, so z.1 can resolve to the right index.
        addLine(script, "test. := w.z.y.x.;"); // resolves to w.2 which is a stem
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStemValue("test.", state).size() == 2;
    }


    public void testMultiIndex2() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "x := 0; y.0 := 1; z.1 := 2; w.2 := 3;");
        addLine(script, "ok := w.z.y.x == 3;"); // resolves to w.2 which is an integer here.
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);

    }

    /**
     * The contract for stem evaluation is that w.z.y.x uses stems unles told otherwise.
     * This is an basic that tests that. Passing in (y) tells the system to
     * evaluate that and use it rather than fall back on the default.
     *
     * @throws Throwable
     */

    public void testMultiIndexOverride() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "x := 0; y.0 := 1; z.1 := 2; w.2 := 3; w.3 := -1; z.7.0 := 3; y :=7;");
        addLine(script, "ok := w.z.(y).x == -1;"); // resolves to w.2 which is an integer here.
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);

    }

    /**
     * Test the has_value function. This test checks for conformability as well as results.
     * It is a bit long but it is critical that this work and in particular, if there is regression
     * it is found immediately.
     *
     * @throws Throwable
     */

    public void testHasValue() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := n(3); b.:=2+n(5);c.foo:=1;c.bar:='arf';");
        addLine(script, "test_a_b. := has_value(a.,b.);");
        addLine(script, "test_b_a. := has_value(b.,a.);");
        addLine(script, "test_a_c. := has_value(a.,c.);");
        addLine(script, "test_c_a. := has_value(c.,a.);");
        addLine(script, "test_c := has_value('arf',c.);"); // scalar result
        addLine(script, "test_1 := has_value(1,a.);"); // scalar result
        addLine(script, "test_bad := has_value(42,b.);"); // scalar result
        addLine(script, "test_bad2 := has_value(42,'woof');"); // scalar result

        State state = testUtils.getNewState();

        // really detailed tests since this is probably one of the most used functions.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        // tests for scalar left arg:
        assert getBooleanValue("test_c", state);
        assert getBooleanValue("test_1", state);
        assert !getBooleanValue("test_bad", state);
        assert !getBooleanValue("test_bad2", state);

        QDLStem test_a_b = getStemValue("test_a_b.", state);
        assert test_a_b.size() == 3;
        assert !test_a_b.getBoolean(0L);
        assert !test_a_b.getBoolean(1L);
        assert test_a_b.getBoolean(2L);

        QDLStem test_b_a = getStemValue("test_b_a.", state);
        assert test_b_a.size() == 5;
        assert test_b_a.getBoolean(0L);
        assert !test_b_a.getBoolean(1L);
        assert !test_b_a.getBoolean(2L);
        assert !test_b_a.getBoolean(3L);
        assert !test_b_a.getBoolean(4L);

        QDLStem test_a_c = getStemValue("test_a_c.", state);
        assert test_a_c.size() == 3;
        assert !test_a_c.getBoolean(0L);
        assert test_a_c.getBoolean(1L);
        assert !test_a_c.getBoolean(2L);

        QDLStem test_c_a = getStemValue("test_c_a.", state);
        assert test_c_a.size() == 2;
        assert test_c_a.getBoolean("foo");
        assert !test_c_a.getBoolean("bar");

    }

    /*
       Test presets
       j(n)->n;j:=2;k:=1;p:=4;q:=5;r:=6;a. := [i(4),i(5),i(6)];
     Not working
     i(3).0 -- gives parser error for .0 since it thinks it is a decimal.

  The following are working and have tests below:
      (i(4)^2-5).j(3)
     [i(5),-i(6)].j(1).j(3)
     [i(5),i(4)].k
     [2+3*i(5),10 - i(4)].(k.0)
     {'a':'b','c':'d'}.j('a')
     {'a':'b','c':'d'}.'a'
     i(3).i(4).j
     i(3).i(4).i(5).i(6).i(7).i(8).j;
     i(3).i(4).i(5).i(6).j(2);
     i(3).i(4).i(5).(a.k).i(6).i(7).j
    (4*i(5)-21).(i(3).i(4).j(2))
     3 rank exx.
     [[-i(4),3*i(5)],[11+i(6), 4-i(5)^2]].j(0).j(1).j(2)
     (b.).j(0).j(1).j(2)

     Embedded stem basic. This get a.1.2 in a very roundabout way
    k:=1;j:=2;a.:=[-i(4),3*i(5),11+i(6)];
    x := i(12).i(11).i(10).(a.k).i(6).i(7).j;
    x==6;

       */
    /* *********** Here below are test for functional stem notation e.g. f(x).j(n) ******* */

    /**
     * Test a define dfunction as index
     *
     * @throws Throwable
     */

    public void testSimpleSF0() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "j(n)->n;");
        addLine(script, "x := {'a':'b','c':'d'}.j('a');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStringValue("x", state).equals("b");
    }

    /**
     * Test a string constant index.
     *
     * @throws Throwable
     */

    public void testSimpleSF1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x := {'a':'b','c':'d'}.'a';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStringValue("x", state).equals("b");
    }


    public void testSimpleSF2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "k := 1;");
        addLine(script, "a := (n(4)^2-5).i(3);");
        // Previous version required parentheses. Now they actually work
        //addLine(script, "b := [2+3*n(5),10 - n(4)].(k.0);");
        addLine(script, "b := [2+3*n(5),10 - n(4)].k.0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("a", state) == 4L;
        assert getLongValue("b", state) == 10L;
    }


    public void testSimpleSF3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "j :=2;");
        addLine(script, "x :=n(3).n(4).j;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 2L;

    }


    public void testSimpleSFReturnsStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "k := 1;");
        addLine(script, "x. := [n(5),n(4)].k;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getStemValue("x.", state).size() == 4; // Noit a great check, but sufficient.
    }


    public void testSFEmbeddedStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := [-n(4),3*n(5),11+n(6)];");
        addLine(script, " k := 1;");
        addLine(script, " j := 2;");
        addLine(script, " x := n(12).n(11).n(10).(a.k).n(6).n(7).j;");
        addLine(script, " y := x == a.1.2;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 6L;
        assert getBooleanValue("y", state);
    }


    /**
     * In this case, the stem variable resolves correctly to the value of 6, but
     * there is no 6th index, so an error condition should be raised.
     *
     * @throws Throwable
     */

    public void testBadSFEmbeddedStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := [-n(4),3*n(5),11+n(6)];");
        addLine(script, " k := 1;");
        addLine(script, " j := 2;");
        addLine(script, " x := n(2).n(3).n(4).(a.k).n(6).n(7).j;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (Throwable t) {
            bad = false;
        }
        if (bad) {
            assert false : "bad index resolved in a stem";
        }

    }

    /**
     * Test that supplying the stem as the left most argument can be resolved.
     *
     * @throws Throwable
     */

    public void testInitialStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " x := (4*n(5)-21).(n(3).n(4).i(2));");
        addLine(script, " y := -13 == (4*n(5)-21).(n(3).n(4).i(2));");
        // redundant check is to ensure that everything is being run right as expressions
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == -13L;
        assert getBooleanValue("y", state);
    }


    public void testThreeRankStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " x := [[-n(4),3*n(5)],[11+n(6), 4-n(5)^2]].i(0).i(1).i(2);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 6L;
    }

    public void testRank() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " ok := 1 == rank([;5]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testDimension() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " v. := dim([;5]) == [5];");
        addLine(script, " ok := (size(v.)==1) && v.0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testDimension3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " v. := dim(n(3,4,5)) == [3,4,5];");
        addLine(script, " ok := reduce(@&&, v.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }


    public void testRank2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " ok := 3 == rank(n(2,3,4));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    /**
     * In the manual, the basic given is
     * <pre>
     *       x := 0;
     *     y.0 := 1;
     *     z.1 := 2;
     *     w.2 := 3;
     *     w.z.y.x
     *  3
     * </pre>
     * This next test does that with functions, showing that tail resolution works.
     *
     * @throws Throwable
     */

    public void testTailResolution() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " x := (1+n(4)).(1+n(3)).(1+n(2)).i(0);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 3L;
    }


    public void testTailResolution2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " b. := [[ -n(4)^2,2+n(5)],[10 - 3*n(6),4+5*n(7)]];");
        addLine(script, " x := (b.).(0).i(1).(3);");
        addLine(script, " y := b.0.1.3 == (b.).(0).i(1).(3);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 5L;
        assert getBooleanValue("y", state);
    }

    /**
     * Test heavily parenthesized tail.
     *
     * @throws Throwable
     */

    public void testTailParentheses() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Expr is (A).((B).(C).(D)))
        addLine(script, " x := 3 == (1+n(4)).((1+n(3)).((1+n(2)).(i(0))));");
        // Expr is (A).((B).(C)).(D))
        addLine(script, " y := 3 == (1+n(4)).((1+n(3)).((1+n(2))).(i(0)));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("x", state);
        assert getBooleanValue("y", state);
    }

    /*
      a. := n(2,3,4,5, n(120))
      a.1.2.3.4 := 0
      b. := 10+n(3,3,n(9))
      b.a.1.2.3.4.1; // same as b.0.1 == 11
      b.(a.1.2.3.4).1; // same as b.0.1 == 11
      b.a.1.2.3.4.2 := -1;
      b.0.2 == -1;

     */

    /**
     * Tests that in stem resolution, only what is understood by a stem is consumed,
     *
     * @throws Throwable
     */
    public void testMidTailResolution() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " x := (1+n(4)).(1+n(3)).(1+n(2)).i(0);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("x", state) == 3L;
    }

    /**
     * Test that a list of indices like ['a.b.c'] can be used to access stems. This permits passing around
     * indices
     *
     * @throws Throwable
     */

    public void testEmbeddedIndex() throws Throwable {
        /*
               x. := ['store.bicycle.price','store.book.0.price','store.book.1.price','store.book.2.price','store.book.3.price']
  test.:={'comment':'This is taken from the JSON Path spec https://tools.ietf.org/id/draft-goessner-dispatch-jsonpath-00.html for testing', 'store':{'bicycle':{'color':'red', 'price':19.95}, 'book':[{'author':'Nigel Rees', 'price':8.95, 'category':'reference', 'title':'Sayings of the Century'},{'author':'Evelyn Waugh', 'price':12.99, 'category':'fiction', 'title':'Sword of Honour'},{'author':'Herman Melville', 'price':8.99, 'isbn':'0-553-21311-3', 'category':'fiction', 'title':'Moby Dick'},{'author':'J. R. R. Tolkien', 'price':22.99, 'isbn':'0-395-19395-8', 'category':'fiction', 'title':'The Lord of the Rings'}]}, 'expensive':10}
  test.x.0
19.95
  test.x.1
8.95
         */
        StringBuffer script = new StringBuffer();
        addLine(script, "test.:={'comment':'This is taken from the JSON Path spec https://tools.ietf.org/id/draft-goessner-dispatch-jsonpath-00.html for testing', " +
                "'store':{'bicycle':{'color':'red', 'price':19.95}, " +
                "'book':[" +
                "{'author':'Nigel Rees', 'price':8.95, 'category':'reference', 'title':'Sayings of the Century'}," +
                "{'author':'Evelyn Waugh', 'price':12.99, 'category':'fiction', 'title':'Sword of Honour'}," +
                "{'author':'Herman Melville', 'price':8.99, 'isbn':'0-553-21311-3', 'category':'fiction', 'title':'Moby Dick'}," +
                "{'author':'J. R. R. Tolkien', 'price':22.99, 'isbn':'0-395-19395-8', 'category':'fiction', 'title':'The Lord of the Rings'}" +
                "]}, " +
                "'expensive':10};");
        addLine(script, " x. := ['`store`bicycle`price','`store`book`0`price','`store`book`1`price','`store`book`2`price','`store`book`3`price'];");
        addLine(script, "a := test.x.0;"); // resolves to test.store.bicycle.price
        addLine(script, "b := test.x.1;"); // resolves to test.store.book.0.price
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert areEqual(getBDValue("a", state), new BigDecimal("19.95"));
        assert areEqual(getBDValue("b", state), new BigDecimal("8.95"));


    }

    public void testJPathQuery() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := {'p':'x', 'q':'y', 'r':5, 's':[2,4,6], 't':{'m':true,'n':345.345}};");
        addLine(script, "x. := query(a., '$..m');");
        addLine(script, "ndx. := query(a., '$..m',true);");
        addLine(script, "ok := reduce(@&&, ndx.0 == ['t','m']);");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        QDLStem x = getStemValue("x.", state);
        assert x.size() == 1;
        assert x.getBoolean(0L);
        QDLStem ndx = getStemValue("ndx.", state);
        assert ndx.size() == 1;
        assert getBooleanValue("ok", state);
    }

    /**
     * Critical regression test that tests that {@link org.qdl_lang.expressions.ExpressionStemNode}s
     * resolve constants on the LHS, so
     * <pre>
     * (a.).(0) := 1 <=> a.0 := 1
     * </pre>
     *
     * @throws Throwable
     */
    public void testBasicExpressionStemNodeAssignment() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "(a.).(0) := 1;");
        addLine(script, "x := a.0 == 1;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("x", state);
    }

    /**
     * Probabilistic test for for_each applied to a dyadic argument.
     *
     * @throws Throwable
     */
    public void testForEach2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z(x,y)->x^2+y^2;");
        addLine(script, "a. := for_each(@z, n(5),n(7));");
        // The test is that a.i.j == w(i,j) testing all of them is a pain, so we test some
        addLine(script, "b.0 := a.0.0 == z(0,0);");
        addLine(script, "b.1 := a.1.1 == z(1,1);");
        addLine(script, "b.2 := a.2.2 == z(2,2);");
        addLine(script, "b.3 := a.3.3 == z(3,3);");
        addLine(script, "b.4 := a.4.4 == z(4,4);");
        addLine(script, "b.5 := a.0.5 == z(0,5);");
        addLine(script, "b.6 := a.1.6 == z(1,6);");
        addLine(script, "ok:= reduce(@∧, b.);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testBadForEach3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "w(x,y,z)->x^2+y^2 + z^3;");
        addLine(script, "a. := for_each(2@w, n(5),n(7),n(9));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean good = false;
        try {
            interpreter.execute(script.toString());
        } catch (UndefinedFunctionException e) {
            good = true;
        }
        assert good : "Was able to invoke for_each on function with wrong airty";
    }

    public void testForEach3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "w(x,y,z)->x^2+y^2 + z^3;");
        addLine(script, "a. := for_each(3@w, n(5),n(7),n(9));");
        // The test is that a.i.j.k == w(i,j,ka) testing all of them is a pain, so we test some
        addLine(script, "b.0 := a.0.0.0 == w(0,0,0);");
        addLine(script, "b.1 := a.1.1.1 == w(1,1,1);");
        addLine(script, "b.2 := a.2.2.2 == w(2,2,2);");
        addLine(script, "b.3 := a.3.3.3 == w(3,3,3);");
        addLine(script, "b.4 := a.4.4.4 == w(4,4,4);");
        addLine(script, "b.5 := a.0.5.5 == w(0,5,5);");
        addLine(script, "b.6 := a.1.6.6 == w(1,6,6);");
        addLine(script, "b.7 := a.1.6.7 == w(1,6,7);");
        addLine(script, "b.8 := a.3.4.5 == w(3,4,5);");
        addLine(script, "b.9 := a.4.2.3 == w(4,2,3);");
        addLine(script, "b.10 := a.2.0.1 == w(2,0,1);");
        addLine(script, "ok:= reduce(@∧, b.);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }
     /*
      a.user.b.4.foo.5 := 2
  a.
{user:{b:{4:{foo:{5:2}}}}}
  a.'0' := 1
  a.
[1]~{user:{b:{4:{foo:{5:2}}}}}
      */

    /**
     * Create a stem in two ways and verify that they are the same
     *
     * @throws Throwable
     */
    public void testExpressionStemNodeAssignment() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. := {'user':{'b':{'4':{'foo':{'5':2}}}}};");
        addLine(script, " a.user.b.4.foo.5 := 2;");
        addLine(script, " ok := a.user.b.4.foo.5 == x.user.b.4.foo.5;");
        addLine(script, " ok0 := size(a.) == size(x.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok0", state);
    }

    /**
     * (See related note in {@link QDLListener#exitDotOp(QDLParserParser.DotOpContext)})
     * This tests that b. - 2 (subtracting 2 from a stem) works. This is a simple
     * regression test.
     *
     * @throws Throwable
     */
    public void testDyadicStemSubtraction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "b. := [;5];");
        addLine(script, " ok := reduce(@&&, [-2,-1,0,1,2] == (b. - 2));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testExtraIndices() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "b. := [;5].2.4;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (IndexError ie) {
            bad = false;
        }
        if (bad) {
            assert false : "was able to access a non-existant index in a stem";
        }

    }

    public void testExtraIndices2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "b. := [;5].i(2).i(4);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean bad = true;
        try {
            interpreter.execute(script.toString());
        } catch (IndexError ie) {
            bad = false;
        }
        if (bad) {
            assert false : "was able to access a non-existent index in a stem";
        }
    }

    /**
     * Tests the ~ applies to a list just reorders the list, i.e., it is fully
     * equivalent to
     * ~a. == []~.a
     *
     * @throws Throwable
     */
    public void testUnaryTilde() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := mod(random(10), 97);"); // completely random set of numbers
        addLine(script, "ξ1. := ~mask(ξ., ξ. < 0);");
        addLine(script, "ξ2. := []~mask(ξ., ξ. < 0);");
        addLine(script, "ok := reduce(@∧, ξ1. ≡ ξ2.);");
        addLine(script, "ok2 := reduce(@∧, [4,5,7,-2] ≡ ~{2:4,3:5}~{1:7,11:-2}); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Monadic ~ failed";
        assert getBooleanValue("ok2", state) : "Monadic and dyadic ~ failed";

    }

    public void testMasklShortCircuit() throws Throwable {
        // Test for https://github.com/ncsa/qdl/issues/25
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := mask([],null);"); // tests that it is a stem on assignment
        addLine(script, "ok := size(ξ.) == 0;"); // test that it is empty
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "short circuit for mask() failed";
    }

    /*
      mask([],null)

     */

    /**
     * Because ~ does not fit into the order of operations, expressions with a
     * . and a ~ like
     * x. ~ y.
     * get parsed as
     * (x) . (~y.)
     * {@link QDLListener} special cases this to handle it
     * (rather than a complete rewrite of the parser).  This has to do with order of operations
     * for QDL operators. Could try and tweak the parser to handle this, but that usually plays
     * hob with other standard order of operations, which must be avoided at all costs.
     * This test checks this works right.
     *
     * @throws Throwable
     */
    public void testTildeWithDot() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := [;5];"); // stem of numbers
        addLine(script, "ξ1. := [10;15];");
        addLine(script, "ξ2. := ξ. ~ ξ1.;"); // do on one line to isolate this
        addLine(script, "ok := reduce(@∧, [0,1,2,3,4,10,11,12,13,14] ≡ ξ2.); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "stem. ~ stem. failed";

    }

    public void testTildeWithDot2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := [;5];"); // stem of numbers
        addLine(script, "ξ2. := ξ. ~ [10;15];"); // do on one line to isolate this
        addLine(script, "ok := reduce(@∧, [0,1,2,3,4,10,11,12,13,14] ≡ ξ2.); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "stem. ~ stem. failed";

    }

    /*
        a. := [;5]~n(2,3, n(6))
    a.
[0,1,2,3,4,[0,1,2],[3,4,5]]
    indices(a., 0); // get the first axis
[0,1,2,3,4]
    indices(a., 1); // get the last axis
[[5,0],[5,1],[5,2],[6,0],[6,1],[6,2]]
    a.[6,2]
5
     */
    public void testAllIntKeys() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := [;5]~n(2,3, n(6));"); // stem of numbers
        addLine(script, "α. := indices(ξ.,0);"); // axis 0
        addLine(script, "β. := indices(ξ.,2);"); // axis 2
        addLine(script, "ok0 := reduce(@∧, [0,1,2,3,4] ≡ α.); ");
        addLine(script, "ok1 := reduce(@∧,reduce(@∧, [[5,0],[5,1],[5,2],[6,0],[6,1],[6,2]] ≡ β.)); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : StemEvaluator.ALL_KEYS + " on axis 0 failed";
        assert getBooleanValue("ok1", state) : StemEvaluator.ALL_KEYS + " on axis 1 failed";
    }

     /*
     ['foo','bar']~{'a':'b', 's':'n', 'd':'m', 'foo':['qwe','eee','rrr']~{'tyu':'ftfgh', 'rty':'456', 'woof':{'a3tyu':'ftf222gh', 'a3rty':'456222', 'a3ghjjh':'422256456'}, 'ghjjh':'456456'}}

    Index sets for various axes.
   [0,1,'a','s','d']
   [['foo',0],['foo',1],['foo',2],['foo','tyu'],['foo','rty'],['foo','ghjjh']]
   [['foo','woof','a3tyu'],['foo','woof','a3rty'],['foo','woof','a3ghjjh']]
      */

    public void testAllKeys() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := ['foo','bar']~{'a':'b', 's':'n', 'd':'m', 'foo':['qwe','eee','rrr']~{'tyu':'ftfgh', 'rty':'456', 'woof':{'a3tyu':'ftf222gh', 'a3rty':'456222', 'a3ghjjh':'422256456'}, 'ghjjh':'456456'}};"); // stem of numbers
        addLine(script, "α. := indices(ξ.,0);"); // rank 1 keys as scalars
        addLine(script, "β. := indices(ξ.,2);"); // rank 2 keys
        addLine(script, "γ. := indices(ξ., -1);"); // rank 3 keys, actually
        // Next line is a simple regression test that the keys and indices coincide with the rank 1 case.
        // A change to how QDL lists were handled silently broke this at one point so this was added
        addLine(script, "ok := ξ.keys(ξ.).0 == 'foo' == ξ.indices(ξ.).0;");
        addLine(script, "ok0 := reduce(@∧,  [0,1,'a','d','s'] ≡ α.); ");
        addLine(script, "ok1 := reduce(@∧,reduce(@∧, [['foo',0],['foo',1],['foo',2],['foo','ghjjh'],['foo','rty'],['foo','tyu']] ≡ β.)); ");
        addLine(script, "ok2 := reduce(@∧,reduce(@∧, [['foo','woof','a3ghjjh'] ,['foo','woof','a3rty'], ['foo','woof','a3tyu']] ≡ γ.)); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.ALL_KEYS + " and " + StemEvaluator.KEYS + " check failed";
        assert getBooleanValue("ok0", state) : StemEvaluator.ALL_KEYS + " on rank 1 failed";
        assert getBooleanValue("ok1", state) : StemEvaluator.ALL_KEYS + " on rank 2 failed";
        assert getBooleanValue("ok2", state) : StemEvaluator.ALL_KEYS + " on all ranks = -1 failed";
    }
/*
        a. := n(3,5,n(15))
  old. := indices(a.-1)
  new. := for_each(@reverse,  old.)
  subset(a., new., old.)
 */

    /**
     * Test subset command to create the transpose of a matrix.
     *
     * @throws Throwable
     */
    public void testGeneralRemap() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ. := n(3,5,n(15));"); // matrix
        addLine(script, "ω. := indices(ξ.);"); // old indices
        addLine(script, "ϖ. := null; while[k∋ω.][ϖ.k:=reverse(ω.k);];"); // new indices
        // Correspondence is that η.ϖ..k := ξ.ω.k
        addLine(script, "η. := " + StemEvaluator.REMAP + "(ξ.,   ω., ϖ.);"); // axis 1
        addLine(script, "ok := reduce(@∧,reduce(@∧, [[0,5,10],[1,6,11],[2,7,12],[3,8,13],[4,9,14]] ≡ η.)); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : ListEvaluator.LIST_SUBSET + " did not create matrix transpose.";
    }

    /**
     * Test moadic transpose operator. This also uses the operator form of reduce.
     *
     * @throws Throwable
     */
    public void testMonadicTransposeOperator() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. := µn(3,4,n(12));");
        addLine(script, "ok := ⊗∧⊙(⊗∧⊙(x.==[[0,4,8],[1,5,9],[2,6,10],[3,7,11]]));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.TRANSPOSE + " operator failed.";

    }

    /*
            a. := n(3,4,5,n(60))
            reduce(@+, transpose(a., 2))
            reduce(@+, axis(a., 0))
            reduce(@+, axis(a., 1))
    [[10,35,60,85],[110,135,160,185],[210,235,260,285]]
     */
    public void testAxisOperator() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ.  := n(3,4,5,n(60));");
        addLine(script, "ξ0. := reduce(@+," + StemEvaluator.TRANSPOSE + "(ξ.,0));");
        addLine(script, "ξ1. := reduce(@+," + StemEvaluator.TRANSPOSE + "(ξ.,1));");
        addLine(script, "ξ2. := reduce(@+," + StemEvaluator.TRANSPOSE + "(ξ.,2));");
        // Check against computed output. We break this up in statements or these get really long
        addLine(script, "η0. := [[60,63,66,69,72],[75,78,81,84,87],[90,93,96,99,102],[105,108,111,114,117]];");
        addLine(script, "η1. := [[30,34,38,42,46],[110,114,118,122,126],[190,194,198,202,206]];");
        addLine(script, "η2. := [[10,35,60,85],[110,135,160,185],[210,235,260,285]];");

        addLine(script, "ok0 := reduce(@∧, reduce(@∧, η0. ≡ ξ0.));");
        addLine(script, "ok1 := reduce(@∧, reduce(@∧, η1. ≡ ξ1.));");
        addLine(script, "ok2 := reduce(@∧, reduce(@∧, η2. ≡ ξ2.));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : StemEvaluator.TRANSPOSE + " operator failed for axis = 0.";
        assert getBooleanValue("ok1", state) : StemEvaluator.TRANSPOSE + " operator failed for axis = 1.";
        assert getBooleanValue("ok2", state) : StemEvaluator.TRANSPOSE + " operator failed for axis = 2.";
    }

    /**
     * Previous test using axis and other operators.
     *
     * @throws Throwable
     */
    public void testAxisOperator2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ξ.  := n(3,4,5,n(60));");
        addLine(script, "ξ0. := ⊗+ ⊙ µξ`0;");
        addLine(script, "ξ1. := ⊗+ ⊙ µξ`1;");
        addLine(script, "ξ2. := ⊗+ ⊙ µξ`2;");
        // Check against computed output. We break this up in statements or these get really long
        addLine(script, "η0. := [[60,63,66,69,72],[75,78,81,84,87],[90,93,96,99,102],[105,108,111,114,117]];");
        addLine(script, "η1. := [[30,34,38,42,46],[110,114,118,122,126],[190,194,198,202,206]];");
        addLine(script, "η2. := [[10,35,60,85],[110,135,160,185],[210,235,260,285]];");

        addLine(script, "ok0 := @∧ ⊙ (η0. ≡ ξ0.)`*;"); // reduce all with axis operator
        addLine(script, "ok1 := @∧ ⊙ (η1. ≡ ξ1.)`*;");
        addLine(script, "ok2 := @∧ ⊙ (η2. ≡ ξ2.)`*;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : StemEvaluator.TRANSPOSE + " operator failed for axis = 0.";
        assert getBooleanValue("ok1", state) : StemEvaluator.TRANSPOSE + " operator failed for axis = 1.";
        assert getBooleanValue("ok2", state) : StemEvaluator.TRANSPOSE + " operator failed for axis = 2.";
    }

    /**
     * Test that a scalar as an argument to for_each does not change the shape of the result.
     *
     * @throws Throwable
     */
    public void testForEachScalar() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " ss(x,y,z)->x*y-z; ");
        addLine(script, "y. := @ss" + OpEvaluator.FOR_ALL_KEY + " [[1;6], 4, [;5]];");
        addLine(script, "check. := [[4,3,2,1,0],[8,7,6,5,4],[12,11,10,9,8],[16,15,14,13,12],[20,19,18,17,16]];"); // matrix
        addLine(script, "ok := reduce(@∧, reduce(@∧, check. ≡ y.));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process scalar in argument list.";
    }

    /**
     * Shows that the trivial case of a single scalar argument.
     *
     * @throws Throwable
     */
    public void testForEachScalar2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " ok := 3 == (@size" + OpEvaluator.FOR_ALL_KEY + "['asd']); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process single scalar case.";
    }

    /**
     * test ∀ for only scalar arguments edge case
     *
     * @throws Throwable
     */
    public void testForEachAllScalars() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "g(x,y,n)->x^n+y^n;");
        addLine(script, " ok := 5 == (@g" + OpEvaluator.FOR_ALL_KEY + "[3,2,1]); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process single scalar case.";
    }

    /**
     * Test that this works if the very first argument is a scalar (this is what kicks off
     * recursion generally in the code.)
     *
     * @throws Throwable
     */
    public void testForEachInitialScalar() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "g(x,y,n)->x^n+y^n;");
        addLine(script, " ok := reduce(@&&, [4,5,6,7,8] == (@g" + OpEvaluator.FOR_ALL_KEY + "[4,[;5],1])); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process initial scalar case.";
    }

    public void testForEachMultipleScalar() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "g(x,y,n)->x^n+y^n;");
        addLine(script, " ok := reduce(@&&, [16,17,20,25,32] == (@g" + OpEvaluator.FOR_ALL_KEY + "[[;5],4,2])); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process multiple trailing scalar case.";
    }

    public void testForEachMonad() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z.:=@size" + OpEvaluator.FOR_ALL_KEY + "[n(4,3,['xxxx','xx','xxx'])];");
        addLine(script, " ok := rank(z.)==2 && reduce(@&&,  dim(z.)==[4,3]);");
        addLine(script, " ok1 := reduce(@&&,reduce(@&&, n(4,3,[4,2,3])==z.));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process all elements in monadic case.";
        assert getBooleanValue("ok1", state) : OpEvaluator.FOR_ALL_KEY + " applied monad incorrectly.";
    }

    /**
     * The contract for for_each os that a built-in dyadic function, δ, will be extended to each argument
     * if there are more than 2 arguments, i.e. a δ b δ c δ ...
     * <p>
     * Random note: For an n-ary function we could generalize this to apply in sequence to the next n-1 args,
     * so for a ternary function, we'd need 5 arguments
     * <pre>[a,b,c,d,e] --> f(f(a,b,c),d,e)</pre>
     * but then the hoops to jump through get larger, e.g., if the function f has definitions for multiple
     * arg counts, which do we use? No canonical solution, so we stick with the dyadic case (or figure out a way to
     * specify exactly which function to use...)
     * </p>
     *
     * @throws Throwable
     */
    public void testForEachMultiDyad() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z.:=@*" + OpEvaluator.FOR_ALL_KEY + "[[1;5],[-3;0],[4;7]];");
        addLine(script, "test.:=[" +
                "[[-12,-15,-18],[-8,-10,-12],[-4,-5,-6]]," +
                "[[-24,-30,-36],[-16,-20,-24],[-8,-10,-12]]," +
                "[[-36,-45,-54],[-24,-30,-36],[-12,-15,-18]]," +
                "[[-48,-60,-72],[-32,-40,-48],[-16,-20,-24]]];");
        addLine(script, " ok := rank(z.)==3 && reduce(@&&,  dim(z.)==[4,3,3]);");
        addLine(script, " ok1 := reduce(@&&,reduce(@&&,reduce(@&&, test.==z.)));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.FOR_ALL_KEY + " failed to process all elements in extended dyadic case.";
        assert getBooleanValue("ok1", state) : OpEvaluator.FOR_ALL_KEY + " applied dyad '*' incorrectly.";
    }

    /**
     * Test case that a scalar is at the end of the argument list in for_each.
     *
     * @throws Throwable
     */
    public void testForEachTrailingScalar() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := for_each(@*, [1;5], 3); ");
        addLine(script, "check. := 3*[1;5];");
        addLine(script, "ok :=  reduce(@∧, check. ≡ a.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.FOR_EACH + " failed to handle trailing scalar in argument list.";
    }

    /**
     * In this case, there are two 2-rank stems. Ensure for each is applied to all of them
     *
     * @throws Throwable
     */
    public void testForEach4Axes() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := for_each(@*, n(2,3, [;6]), n(3,4,[;12]+100)); ");
        addLine(script, "check. := [[[[0,0,0,0],[0,0,0,0],[0,0,0,0]],[[100,101,102,103],[104,105,106,107],[108,109,110,111]],[[200,202,204,206],[208,210,212,214],[216,218,220,222]]],[[[300,303,306,309],[312,315,318,321],[324,327,330,333]],[[400,404,408,412],[416,420,424,428],[432,436,440,444]],[[500,505,510,515],[520,525,530,535],[540,545,550,555]]]];");
        addLine(script, "ok :=  reduce(@∧,reduce(@∧,reduce(@∧,reduce(@∧, check. ≡ a.))));"); // 4 axes
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.FOR_EACH + " failed for 4 axes ";
    }

    /**
     * Previous test using nothing but operators
     *
     * @throws Throwable
     */
    public void testForEach4Axes2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := @* ∀ [n(2,3,[;6]), n(3,4,[;12]+100)]; ");
        addLine(script, "check. := [" +
                "[[[0,0,0,0],[0,0,0,0],[0,0,0,0]]," +
                "[[100,101,102,103],[104,105,106,107],[108,109,110,111]],[[200,202,204,206],[208,210,212,214],[216,218,220,222]]]," +
                "[[[300,303,306,309],[312,315,318,321],[324,327,330,333]],[[400,404,408,412],[416,420,424,428],[432,436,440,444]],[[500,505,510,515],[520,525,530,535],[540,545,550,555]]]];");
        addLine(script, "ok :=  ⊗∧⊙(check. ≡ a.)`*;"); // 4 axes
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.FOR_EACH + " failed for 4 axes ";
    }

    /**
     * Here three 1 rank stems are passed in and create a 3 rank stem
     *
     * @throws Throwable
     */
    public void testForEach3Axes() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x,y,z)->x+'_' + y + '_' + z;");
        addLine(script, "a.:=@f∀[[;5],[5;9],[9;12]];");
        addLine(script, "check. := [[['0_5_9','0_5_10','0_5_11'],['0_6_9','0_6_10','0_6_11'],['0_7_9','0_7_10','0_7_11'],['0_8_9','0_8_10','0_8_11']],[['1_5_9','1_5_10','1_5_11'],['1_6_9','1_6_10','1_6_11'],['1_7_9','1_7_10','1_7_11'],['1_8_9','1_8_10','1_8_11']],[['2_5_9','2_5_10','2_5_11'],['2_6_9','2_6_10','2_6_11'],['2_7_9','2_7_10','2_7_11'],['2_8_9','2_8_10','2_8_11']],[['3_5_9','3_5_10','3_5_11'],['3_6_9','3_6_10','3_6_11'],['3_7_9','3_7_10','3_7_11'],['3_8_9','3_8_10','3_8_11']],[['4_5_9','4_5_10','4_5_11'],['4_6_9','4_6_10','4_6_11'],['4_7_9','4_7_10','4_7_11'],['4_8_9','4_8_10','4_8_11']]];");
        addLine(script, "ok :=  reduce(@∧,reduce(@∧,reduce(@∧, check. ≡ a.)));"); // 3 axes
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.FOR_EACH + " failed for 3 axes ";
    }
// ((x)→rank(x))∀[n(2,3,5,7)`1]

    /**
     * This uses a lambda with the for_each operator to check that the axis restriction
     * works. It computes the rank of each returned element, so as the axis increases,
     * the size of the result increases. This is actually a pretty slick test and checks
     * the rank an axis. This is for a single argument and monadic function.
     *
     * @throws Throwable
     */
    // Fix https://github.com/ncsa/qdl/issues/109
    public void testForEachAxisRestriction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := n(3,3,3,3); // really any higher-order stem works");
        addLine(script, "define[check(a., axis)][\n" +
                "   r ≔ rank(a.); \n" +
                "   z. ≔ ((x)→rank(x))∀[a`(axis)]; // actual loop over stem\n" +
                "   ok ≔ axis+1 ≡ rank(z.); // check the shape of the result  \n" +
                "   q ≔  ⊗∧⊙((r-axis-1) ≡ z.)`*; // every entry is the same \n" +
                "    return(ok ∧ q);\n" +
                "  ];\n");
        addLine(script, "ok.:=[];");
        addLine(script, "while[j∈[;rank(a.)]][ok.j := check(a.,j);];");
        addLine(script, "ok := ⊗∧⊙ok.; // checks every axis worked."); // 3 axes
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.FOR_EACH + " failed to loop over axis correctly";
    }

    public void testSizeOnAxis() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := n(2,2,2,2);");
        addLine(script, "ok0 := 2 == size(a`0); "); // size on axis 0
        addLine(script, "ok1 := 4 == size(a`1); "); // size on axis 1
        addLine(script, "ok2 := 8 == size(a`2); "); // size on axis 2
        addLine(script, "ok3 := 16 == size(a`3); "); // size on axis 3
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : StemEvaluator.SIZE + " failed for axis 0";
        assert getBooleanValue("ok1", state) : StemEvaluator.SIZE + " failed for axis 1";
        assert getBooleanValue("ok2", state) : StemEvaluator.SIZE + " failed for axis 2";
        assert getBooleanValue("ok3", state) : StemEvaluator.SIZE + " failed for axis 3";
    }

    public void testForEachOnAxes() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "w. ≔ n(2,3,4,[;24]);");
        addLine(script, "f(x,y)→rank(x)+rank(y); ");
        addLine(script, "a. ≔ 2@f∀[w`1,w`2];");
        addLine(script, "ok0 ≔ (5 ≡ rank(a.)) ∧ (⊗∧⊙(a.≡1)`*) ; "); // rank is 5, every element is a 1
        addLine(script, "b. ≔ 2@f∀[w`0,w`0];");
        addLine(script, "ok1 ≔ (2 ≡ rank(b.)) ∧ (⊗∧⊙(b.≡4)`*) ; "); // rank is 2, every element is a 4
        addLine(script, "c. ≔ 2@f∀[w`0,w`1];");
        addLine(script, "ok2 ≔ (3 ≡ rank(c.)) ∧ (⊗∧⊙(c.≡3)`*) ; "); // rank is 3, every element is a 3
        addLine(script, "d. ≔ 2@f∀[w`(¯1),w`(¯1)];"); // Same as specifying no axes. Should reduce all to scalars
        addLine(script, "ok3 ≔ (6 ≡ rank(d.)) ∧ (⊗∧⊙(d.≡0)`*) ; "); // rank is 6, every element is a 0
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "∀ failed for axes 1,2";
        assert getBooleanValue("ok1", state) : "∀ failed for axes 0,0";
        assert getBooleanValue("ok2", state) : "∀ failed for axes 0,1";
        assert getBooleanValue("ok3", state) : "∀ failed for axes -1,-1";
    }

    /**
     * regression test to show that LHS λ function is processed correctly.
     *
     * @throws Throwable
     */
    public void testForEachOnAxesWithLambda() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "w. ≔ n(2,3,4,[;24]);");
        addLine(script, "f(x,y)→rank(x)+rank(y); ");
        addLine(script, "a. ≔ ((x,y)→rank(x)+rank(y))∀[w`1,w`2];");
        addLine(script, "ok0 ≔ (5 ≡ rank(a.)) ∧ (⊗∧⊙(a.≡1)`*) ; "); // rank is 5, every element is a 1
        addLine(script, "b. ≔ ((x,y)→rank(x)+rank(y))∀[w`0,w`0];");
        addLine(script, "ok1 ≔ (2 ≡ rank(b.)) ∧ (⊗∧⊙(b.≡4)`*) ; "); // rank is 2, every element is a 4
        addLine(script, "c. ≔ ((x,y)→rank(x)+rank(y))∀[w`0,w`1];");
        addLine(script, "ok2 ≔ (3 ≡ rank(c.)) ∧ (⊗∧⊙(c.≡3)`*) ; "); // rank is 3, every element is a 3
        addLine(script, "d. ≔ ((x,y)→rank(x)+rank(y))∀[w`(¯1),w`(¯1)];"); // Same as specifying no axes. Should reduce all to scalars
        addLine(script, "ok3 ≔ (6 ≡ rank(d.)) ∧ (⊗∧⊙(d.≡0)`*) ; "); // rank is 6, every element is a 0
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : "∀ with λ failed for axes 1,2";
        assert getBooleanValue("ok1", state) : "∀ with λ failed for axes 0,0";
        assert getBooleanValue("ok2", state) : "∀ with λ failed for axes 0,1";
        assert getBooleanValue("ok3", state) : "∀ with λ failed for axes -1,-1";
    }


    /**
     * The contract for for_each allows for extending dyadic functions to multiple arguments. This tests
     * that lambdas and FQ references work. Unqualified references do not work.
     *
     * @throws Throwable
     */
    public void testDyadicForEach() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "f(x,y)→x*y;");
        addLine(script, "r. := [[-2,0,2],[-4,0,4],[-6,0,6],[-8,0,8]];");
        addLine(script,"x.:= 2@f∀[[1;5],2,[-1;2]];");
        addLine(script,"y. := ((x,y)→x*y)∀[[1;5],2,[-1;2]];");
        addLine(script,"ok_x := (rank(x.) ≡ 2) ∧ (⊗∧⊙(dim(x.)≡[4,3])) ∧ (⊗∧⊙(x.≡r.)`*); ");
        addLine(script,"ok_y := (rank(y.) ≡ 2) ∧ (⊗∧⊙(dim(y.)≡[4,3])) ∧ (⊗∧⊙(y.≡r.)`*); ");
        addLine(script,"ok_z := false;");
        addLine(script,"try[@f∀[[1;5],2,[-1;2]];]catch[ok_z:=true;];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok_x", state) : "dyadic extension of ∀ failed for fully qualified function reference ";
        assert getBooleanValue("ok_y", state) : "dyadic extension of ∀ failed for lambda ";
        assert getBooleanValue("ok_z", state) : "dyadic extension of ∀ worked for unqualified function reference. Should fail. ";
    }
    /* All of these should work and return
    f(x,y)→x*y;
   2@f∀[[1;5],2,[-1;2]]
   ((x,y)→x*y)∀[[1;5],2,[-1;2]]
   and return
   r. :=[[-2,0,2],[-4,0,4],[-6,0,6],[-8,0,8]];

   @f∀[[1;5],2,[-1;2]]
   fails
     */

    /**
     * The contract is that <i>only</i> the number of elements at a given axis are returned.
     * So [0,[;2]] =: b. means size(b`0) == 1 and size(b`1) == 2
     *
     * @throws Throwable
     */
    public void testSizeOnAxisContract() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "b. :=[0,[0,1,2,3,4,5,6],{1,{10,11,12},2,3,4,5,6},[[0,1,2],[0,1,2]],4, true, 2/17]~{'a':['a','b','c']};");
        addLine(script, "ok0 := 8 == size(b`0); "); // size on axis 0
        addLine(script, "ok1 := 12 == size(b`1); "); // size on axis 1
        addLine(script, "ok2 := 6 == size(b`2); "); // size on axis 2
        addLine(script, "ok3 := 0 == size(b`3); "); // size on axis 3
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        /*
            Compare with
            indices(b.)
        [[0],[2],[4],[5],[6],[1,0],[1,1],[1,2],[1,3],[1,4],[1,5],[1,6],[a,0],[a,1],[a,2],[3,0,0],[3,0,1],[3,0,2],[3,1,0],[3,1,1],[3,1,2]]

        */
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : StemEvaluator.SIZE + " failed for axis 0";
        assert getBooleanValue("ok1", state) : StemEvaluator.SIZE + " failed for axis 1";
        assert getBooleanValue("ok2", state) : StemEvaluator.SIZE + " failed for axis 2";
        assert getBooleanValue("ok3", state) : StemEvaluator.SIZE + " failed for axis 3";
    }

    /**
     * Previous test but with all operators and lambdas
     *
     * @throws Throwable
     */
    public void testForEach3Axes2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.:=((x,y,z)->x+'_' + y + '_' + z)∀[[;5],[5;9],[9;12]];");
        addLine(script, "check. := [[['0_5_9','0_5_10','0_5_11'],['0_6_9','0_6_10','0_6_11'],['0_7_9','0_7_10','0_7_11'],['0_8_9','0_8_10','0_8_11']],[['1_5_9','1_5_10','1_5_11'],['1_6_9','1_6_10','1_6_11'],['1_7_9','1_7_10','1_7_11'],['1_8_9','1_8_10','1_8_11']],[['2_5_9','2_5_10','2_5_11'],['2_6_9','2_6_10','2_6_11'],['2_7_9','2_7_10','2_7_11'],['2_8_9','2_8_10','2_8_11']],[['3_5_9','3_5_10','3_5_11'],['3_6_9','3_6_10','3_6_11'],['3_7_9','3_7_10','3_7_11'],['3_8_9','3_8_10','3_8_11']],[['4_5_9','4_5_10','4_5_11'],['4_6_9','4_6_10','4_6_11'],['4_7_9','4_7_10','4_7_11'],['4_8_9','4_8_10','4_8_11']]];");
        addLine(script, "ok :=  ⊗∧⊙( check. ≡ a.)`*;"); // 3 axes
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.FOR_EACH + " failed for 3 axes ";
    }

/*
                     for_each(@*, [1;5], 3)
  input_form(for_each(@*, n(2,3, [;6]), n(3,4,[;12]+100)))
[[[[0,0,0,0],[0,0,0,0],[0,0,0,0]],[[100,101,102,103],[104,105,106,107],[108,109,110,111]],[[200,202,204,206],[208,210,212,214],[216,218,220,222]]],[[[300,303,306,309],[312,315,318,321],[324,327,330,333]],[[400,404,408,412],[416,420,424,428],[432,436,440,444]],[[500,505,510,515],[520,525,530,535],[540,545,550,555]]]]

     unique(['a','b',0,3,true]~[['a','b',0,3,true]]~[[['a','b',0,3,true]]])
     [0,a,b,c,3,true]
 */

    public void testUnique() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ϱ. := unique(['a','b',0,3,true]~'c'~[['a','b',0,3,true]]~[[['a','b',0,3,true]]]);"); // matrix
        addLine(script, "ok := reduce(⊗∧, reduce(⊗∨, for_each(⊗≡, ['a','b','c',0,3,true], ϱ.))); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);

        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.UNIQUE_VALUES + " failed.";
    }


    public void testSetStemToScalar() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ϱ. := 'foo';"); // matrix
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean isOk = false;
        try {
            interpreter.execute(script.toString());
        } catch (IndexError ix) {
            isOk = true;
        }
        assert isOk : "could set stem variable to non-null scalar";
    }

    public void testSetScalarToStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ϱ := [;5];"); // matrix
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean isOk = false;
        try {
            interpreter.execute(script.toString());
        } catch (IndexError ix) {
            isOk = true;
        }
        assert isOk : "could set scalar variable to stem value";
    }

    /**
     * Tests that the pick function works on stems
     *
     * @throws Throwable
     */
    public void testSubsetMonadicPick() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok := reduce(@&&, [-1,0,1] == pick((x)->x<2, [-1;5]));");
        addLine(script, "z. := {'a':'x_baz', 'b':3, 'c':'x_bar', 'd':'woof'};");
        addLine(script, "q. := pick((x)->index_of(x, 'x_').0==0, z.);");
        addLine(script, "ok1 := reduce(@&&, {'a':'x_baz', 'c':'x_bar'}==q.);");
        addLine(script, "");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }

    public void testSubsetDyadicPick() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Get elements with even indices
        addLine(script, "ok := reduce(@&&, {0:-4, 2:-2, 4:0, 6:2, 8:4} == pick((x,y)->mod(x,2)==0, [-4;5]));");
        // get elements whose key + value is divisible by 3, showing both are passed along and available
        addLine(script, "q. := pick((key,value)->mod(key+value,3)==0, [-4;5]);");
        addLine(script, "ok1 := reduce(@&&, {2:-2, 5:1, 8:4}==q.);");
        addLine(script, "my_f(x,y)->2<x;");
        addLine(script, "");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }

    /**
     * Tests that pick will raise an error if the user requests a pick function with the
     * wrong valence
     *
     * @throws Throwable
     */
    // https://github.com/ncsa/qdl/issues/107
    public void testPickFQFunction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Get elements with even indices
        addLine(script, "f(x,y)-> true;");
        addLine(script, "pick(1@f,n(3,4));"); // no function 1@f defined
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean good = false;
        try {
            interpreter.execute(script.toString());
        } catch (UndefinedFunctionException e) {
            good = true;
        }
        assert good : "Could specify incorrect valence of function for pick";
    }

    /**
     * If there are multiple functions and the valence is not given, raise an error
     *
     * @throws Throwable
     */
    // https://github.com/ncsa/qdl/issues/107
    public void testPickNoQFunction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Get elements with even indices
        addLine(script, "f(x,y)-> true;f(x)->false;");
        addLine(script, "pick(@f,n(3,4));"); // two functions @f defined
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        boolean good = false;
        try {
            interpreter.execute(script.toString());
        } catch (BadArgException e) {
            good = true;
        }
        assert good : "Could specify incorrect valence of function for pick";
    }

    // Managed to break sublist doing a refactor, so these are the regression tests to detect that
    // should something like it happen again.  These test a contiguous list
    public void testSublistContract() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := reduce(@&&, sublist([;10],7) == [7,8,9]);");
        addLine(script, "ok1 := reduce(@&&, sublist([;10],2,4) == [2,3,4,5]);");
        addLine(script, "ok2 := reduce(@&&, sublist([;10],-3) == [7,8,9]);");
        addLine(script, "ok3 := reduce(@&&, sublist([;10],-3,2) == [7,8]);");
        addLine(script, "ok4 := reduce(@&&, sublist('a',-3,2) == ['a']);"); // scalars are just returned
        addLine(script, "ok5 := size(sublist([;10],-3,0)) == 0;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : " reqesting tail of list failed";
        assert getBooleanValue("ok1", state) : " requesting 4 elements from middle of list failed";
        assert getBooleanValue("ok2", state) : "negative count should return tail";
        assert getBooleanValue("ok3", state) : "negative start index failed";
        assert getBooleanValue("ok4", state) : "subset of scalar should return list with single value ";
        assert getBooleanValue("ok5", state) : "count of 0 should return empty list";
    }

    public void testSublistSparseContract() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "b. := [;15];remove(b.4);remove(b.7);remove(b.10);remove(b.11);"); // sparse list with gaps
        addLine(script, "ok0 := reduce(@&&, sublist(b., 10) == [12,13,14]);");
        addLine(script, "ok1 := reduce(@&&, sublist(b., 10, 10) == [12,13,14]);");
        addLine(script, "ok2 := size(sublist(b., 1000)) == 0;");
        addLine(script, "ok3 := reduce(@&&, sublist(b., 3, 6) == [3,5,6,8,9,12]);");
        addLine(script, "ok4 := reduce(@&&, sublist(b., -4, 2) == [12,13]);");
        addLine(script, "ok5 := reduce(@&&, sublist(b., -3) == [12,13,14]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : " reqesting tail of list failed";
        assert getBooleanValue("ok1", state) : "requesting more than number of elements should just return rest of list";
        assert getBooleanValue("ok2", state) : "request non-existent index returns empty list";
        assert getBooleanValue("ok3", state) : "request of finite subset spanning gaps in list failed.";
        assert getBooleanValue("ok4", state) : "request of finite subset from end, finite count  failed.";
        assert getBooleanValue("ok5", state) : "request of subset from end spanning gaps in list failed.";

    }
    /*
    b. := [;15];remove(b.4);remove(b.7);remove(b.10);remove(b.11);
      sublist(b., -4, 2)
  [12,13]

  sublist(b., -3)
    [12,13,14]



     */

    /*
       sublist((x,y)->2<x, [;10])
 {3:3, 4:4, 5:5, 6:6, 7:7, 8:8, 9:9}
   sublist((x)->x<0, [-2;3])
 [-2,-1]
     sublist((x)->x<0, [-2;3])
 [-2,-1]
   my_f(x,y)->2<x
   sublist(@my_f, [;10])
 {3:3, 4:4, 5:5, 6:6, 7:7, 8:8, 9:9}
   my_f(x)->x<0
   sublist(@my_f, [-2;5])
     sublist((x,y)->mod(x,2)==0, [-4;5])
 {0:-4, 2:-2, 4:0, 6:2, 8:4}
      */

    public void testGenericReduce() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z. := {'a':'x_baz', 'b':3, 'c':'x_bar', 'd':'woof'};");
        addLine(script, "ok := reduce(@&&, z.==z.);"); // result of == is stem with keys
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
    }

    public void testSort() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z. :=   sort([-3/5, 4==5, 'abc', 'SPQR', {3,4,5}]) == [false,'SPQR','abc',-0.6,{3,4,5}];");
        addLine(script, "z1. :=   sort([-3/5, 4==5, 'abc', 'SPQR', {3,4,5}], false) == [{3,4,5},-0.6,'abc','SPQR',false];");
        addLine(script, "ok := reduce(@&&, z.);"); // z. is boolean
        addLine(script, "ok1 := reduce(@&&, z1.);"); // z1. is boolean
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
    }

    /*
        copy([1,2,3,4,5,6],1,2,[10,11,12,13,14,15], 3)
    [10,11,12,2,3,15]
     */
    public void testCopy() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " ok ≔ ⊗∧⊙(copy([1,2,3,4,5,6],1,2,[10,11,12,13,14,15], 3) ≡ [10,11,12,2,3,15]);");
        addLine(script, "ok1 ≔ ⊗∧⊙(copy([1;5],1,[10;17])≡[2,3,4,13,14,15,16]);");
        addLine(script, "ok2 ≔ ⊗∧⊙(copy([1;5],1,[10;17],3)≡[10,11,12,2,3,4,16]);");
        addLine(script, "ok3 ≔ ⊗∧⊙(copy([1;5],[10;17],-1)≡[10,11,12,13,14,15,1,2,3,4]);");
        addLine(script, "copy([;5],z.);");
        addLine(script, "ok4:= ⊗∧⊙z.==[;5];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert getBooleanValue("ok4", state) : "Did not create a new variable on " + ListEvaluator.LIST_COPY2;
    }

    public void testInsertAt() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, " ok ≔ ⊗∧⊙(insert_at([1,2,3,4,5,6],1,2,[10,11,12,13,14,15], 3) ≡ [10,11,12,2,3,13,14,15]);");
        addLine(script, "ok1 ≔ ⊗∧⊙(insert_at([1;5],1,[10;17])≡[2,3,4,10,11,12,13,14,15,16]);");
        addLine(script, "ok2 ≔ ⊗∧⊙(insert_at([1;5],1,[10;17],3)≡[10,11,12,2,3,4,13,14,15,16]);");
        addLine(script, "ok3 ≔ ⊗∧⊙(insert_at([1;5],[10;17],-1)≡[10,11,12,13,14,15,1,2,3,4,16]);");
        addLine(script, "insert_at([;5],z.);");
        addLine(script, "ok4:= ⊗∧⊙z.==[;5];");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state);
        assert getBooleanValue("ok1", state);
        assert getBooleanValue("ok2", state);
        assert getBooleanValue("ok3", state);
        assert getBooleanValue("ok4", state) : "Did not create a new variable on " + ListEvaluator.LIST_INSERT_AT;
    }

    public void testRoundtripJSONWithNull() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a.:=[null,null,null,1,-2,'q'];");
        addLine(script, "ok := reduce(@&&, from_json(to_json(a.)) == a.);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to round trip JSON array with QDL nulls in it.";
    }

    /**
     * Makes sure that ~ handles default values in stems
     *
     * @throws Throwable
     */
    public void testTildeDefaultValues() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "qq. := {*:4}~[2];");
        addLine(script, "ok := 4 ==  interpret(input_form(qq.)).42;"); // round trip it, check default value
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "failed to round trip default value with a ~.";
    }

    protected String SUBSTEM_LIST_QDL_SETUP = "b.0 := [;3];b.1 := [;5]+10;b.2 := [;7] + 20;b.3 := [;11] + 100;";
    protected String SUBSTEM_QDL_SETUP = "a.'p'.'r':='pr';a.'p'.'s':='ps';a.'p'.'t':='pt';" +
            "a.'q'.'r':='qr';a.'q'.'s':='qs';a.'q'.'t':='qt';" +
            "a.'q'.'z':='qz';a.'p'.'s':='ps';a.'n'.'m':='nm'; a.0.'s':='0s';a.4.'s':='4s';";
     /*  b. has different length elements, so the keys have to be actually done right.
        b.
        [
         [0,1,2],
         [10,11,12,13,14],
         [20,21,22,23,24,25,26],
         [100,101,102,103,104,105,106,107,108,109,110]
         ]

     a.'p'.'r':='pr';a.'p'.'s':='ps';a.'p'.'t':='pt';a.'q'.'r':='qr';
     a.'q'.'s':='qs';a.'q'.'t':='qt';"a.'q'.'z':='qz';a.'p'.'s':='ps';a.'n'.'m':='nm';

      */

    /**
     * test subsetting with the \ operator for all non-wildcard cases.
     * Case is for a scalar
     *
     * @throws Throwable
     */

    public void testExtractionFiniteCase() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "d.:=[;200];");
        addLine(script, "ok :=reduce(@&&, b\\1\\[1,2] == [11,12]);");
        addLine(script, "ok1 :=reduce(@&&, b\\[1,3]\\2 == [12,102]);");
        addLine(script, "ok2 :=reduce(@&&, d\\[3,99] == [3,99]);");
        addLine(script, "ok3 :=reduce(@&&, reduce(@&&, b\\![1,3]\\![2,1] == {1:{1:11, 2:12}, 3:{1:101, 2:102}}));");
        addLine(script, "ok4 :=reduce(@&&, reduce(@&&,  b\\[1,37]\\* == [[10,11,12,13,14]]));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "b\\1\\[1,2] failed";
        assert getBooleanValue("ok1", state) : "b\\[1,3]\\2 failed";
        assert getBooleanValue("ok2", state) : "d\\[3,99] -- sparse index set -- failed";
        assert getBooleanValue("ok3", state) : "b\\![1,3]\\![2,1] failed";
        assert getBooleanValue("ok4", state) : "b\\[1,37]\\* -- missing indices ignored --failed";
    }

    public void testExtractionFiniteCase2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "d.:=[;200];");
        addLine(script, "ok :=reduce(@&&, b\\>[1,[1,2]] == [11,12]);");
        addLine(script, "ok1 :=reduce(@&&, b\\>[[1,3],2] == [12,102]);");
        addLine(script, "ok2 :=reduce(@&&, d\\>[[3,99]] == [3,99]);");
        addLine(script, "ok3 :=reduce(@&&, reduce(@&&, b\\!>[[1,3],[2,1]] == {1:{1:11, 2:12}, 3:{1:101, 2:102}}));");
        addLine(script, "ok4 :=reduce(@&&, reduce(@&&,  b\\>[[1,37]]\\* == [[10,11,12,13,14]]));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "b\\>[1,[1,2]] failed";
        assert getBooleanValue("ok1", state) : "b\\>[[1,3],2] failed";
        assert getBooleanValue("ok2", state) : "d\\>[[3,99]] -- sparse index set -- failed";
        assert getBooleanValue("ok3", state) : "b\\!>[[1,3],[2,1]] failed";
        assert getBooleanValue("ok4", state) : "b\\>[[1,37]]\\* -- missing indices ignored --failed";
    }
    //  reduce(@&&, b\1\[1,2] == [11,12])

    public void testExtractionWildcard() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "ok :=reduce(@&&, b\\*\\2 == [2,12,22,102]);");
        addLine(script, "ok1 :=reduce(@&&, b\\2\\* == [20,21,22,23,24,25,26]);"); // same as b.2, essentially
        addLine(script, "ok2 :=reduce(@&&, reduce(@&&, b\\*\\* == b.));"); // same as b.
        addLine(script, "ok3 :=reduce(@&&, reduce(@&&, b\\!* == b.));"); // same as b.

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "b\\*\\2 failed";
        assert getBooleanValue("ok1", state) : "b\\2\\* failed";
        assert getBooleanValue("ok2", state) : "b\\*\\* failed";
        assert getBooleanValue("ok3", state) : "b\\!* failed";
    }

    public void testExtractionWildcard2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "ok :=reduce(@&&, b\\>[star(),2] == [2,12,22,102]);");
        addLine(script, "ok1 :=reduce(@&&, b\\>[2,star()] == [20,21,22,23,24,25,26]);"); // same as b.2, essentially
        addLine(script, "ok2 :=reduce(@&&, reduce(@&&, b\\>[star(),star()] == b.));"); // same as b.
        addLine(script, "ok3 :=reduce(@&&, reduce(@&&, b\\!>[star()] == b.));"); // same as b.

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "b\\>[star(),2] failed";
        assert getBooleanValue("ok1", state) : "b\\>[2,star()] failed";
        assert getBooleanValue("ok2", state) : "b\\>[star(),star()] failed";
        assert getBooleanValue("ok3", state) : "b\\!>[star()]* failed";
    }


    public void testStemExtraction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_QDL_SETUP);
        addLine(script, "ok :=reduce(@&&, a\\p\\* == {'r':'pr', 's':'ps', 't':'pt'});");
        addLine(script, "ok1 :=reduce(@&&, a\\*\\s == {'p':'ps', 'q':'qs'});"); // same as b.2, essentially
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : " a\\p\\* failed";
        assert getBooleanValue("ok1", state) : "a\\*\\s failed";
    }

    public void testMixedExtraction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_QDL_SETUP);
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "c. := b.~a.;");
        addLine(script, "ok :=reduce(@&&, c\\*\\s == {4:'0s', 'p':'ps', 'q':'qs'});");
        addLine(script, "ok1 :=reduce(@&&,reduce(@&&, c\\[2,'n']\\[0,'s'] == [[20]]));"); // not all present is ok
        addLine(script, "dd.:=c\\[2,'n']\\[0,'m'];");
        addLine(script, "ok2 := dd.0.0==20 && dd.n.m=='nm';"); // can't use repeated reduce here because lists and stems can't be compared.

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "c\\*\\s failed";
        assert getBooleanValue("ok1", state) : "c\\[2,'n']\\[0,'s'] failed";
        assert getBooleanValue("ok2", state) : "c\\[2,'n']\\[0,'m'] failed";
    }

    public void testMixedExtraction2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_QDL_SETUP);
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "z.:=n(3,4,n(12));");

        addLine(script, "c. := b.~a.;");
        addLine(script, "ok :=reduce(@&&, c\\>[star(),'s'] == {4:'0s',5:'4s', 'p':'ps', 'q':'qs'});");
        addLine(script, "ok1 :=reduce(@&&,reduce(@&&, c\\>[[2,'n'],[0,'s']] == [[20]]));"); // not all present is ok
        addLine(script, "dd.:=c\\>[[2,'n'],[0,'m']];");
        addLine(script, "ok2 := dd.0.0==20 && dd.n.m=='nm';"); // can't use repeated reduce here because lists and stems can't be compared.
        addLine(script, "ok3 := reduce(@&&, z\\>(1~(size(z.)<4?star():2)) == [4,5,6,7]);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "c\\>[star(),'s'] failed";
        assert getBooleanValue("ok1", state) : "c\\>[[2,'n'],[0,'s']] failed";
        assert getBooleanValue("ok2", state) : "c\\>[[2,'n'],[0,'s']] failed";
        assert getBooleanValue("ok3", state) : "z\\>(1~(size(z.)<4?star():2)) failed";
    }


    /**
     * This compares *, lists and hard coded extractions to make sure nothing is getting lost
     * Mostly this is a regression test that must be passed.
     *
     * @throws Throwable
     */
    public void testExtractionCompare() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, SUBSTEM_QDL_SETUP);
        addLine(script, SUBSTEM_LIST_QDL_SETUP);
        addLine(script, "a.:=n(5,5,n(25));");
        addLine(script, "rr(x.)->reduce(@&&, reduce(@&&, x.));");

        addLine(script, "check.0 :=[[3,4],[8,9],[13,14],[18,19],[23,24]];");
        addLine(script, "ok00 := rr( a\\[;5]\\[3;7] == check.0);"); // extra trailing indices
        addLine(script, "ok01 := rr( a\\*\\[3;7] == check.0);");
        addLine(script, "ok02 := rr( a\\>[star(),[3;7]] == check.0);");

        addLine(script, "check.1 :=[[1,3],[6,8],[11,13],[16,18],[21,23]];");
        addLine(script, "ok10 := rr( a\\[;5]\\[1,3,5,7] == check.1);"); // extra trailing indices w/gaps
        addLine(script, "ok11 := rr( a\\*\\[1,3,5,7] == check.1);");
        addLine(script, "ok12 := rr( a\\>[star(),[1,3,5,7]] == check.1);");

        addLine(script, "check.2 :=check.1;");
        addLine(script, "ok20 := rr( a\\[;5]\\[8,9,1,3] == check.2);");
        addLine(script, "ok21 := rr( a\\*\\[8,9,1,3] == check.2);");
        addLine(script, "ok22 := rr( a\\>[star(),[8,9,1,3]] == check.2);");

        addLine(script, "check.3 :=[[3,2,1],[8,7,6],[13,12,11],[18,17,16],[23,22,21]];");
        addLine(script, "ok30 := rr( a\\[;5]\\[3,2,1] == check.3);"); // reorder
        addLine(script, "ok31 := rr( a\\*\\[3,2,1] == check.3);");
        addLine(script, "ok32 := rr( a\\>[star(),[3,2,1]] == check.3);");

        addLine(script, "check.4 :=[[0,0,0],[5,5,5],[10,10,10],[15,15,15],[20,20,20]];");
        addLine(script, "ok40 := rr( a\\[;5]\\[0,0,0] == check.4);"); // repeat
        addLine(script, "ok41 := rr( a\\*\\[0,0,0] == check.4);");
        addLine(script, "ok42 := rr( a\\>[star(),[0,0,0]] == check.4);");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok00", state) : "failure processing a\\[;5]\\[3;7]";
        assert getBooleanValue("ok01", state) : "failure processing a\\*\\[3;7]";
        assert getBooleanValue("ok02", state) : "failure processing a\\[star(),[3;7]]";
        assert getBooleanValue("ok10", state) : "failure processing a\\[;5]\\[1,3,5,7]";
        assert getBooleanValue("ok11", state) : "failure processing a\\*\\[1,3,5,7]";
        assert getBooleanValue("ok12", state) : "failure processing a\\>[star(),[1,3,5,7]]";
        assert getBooleanValue("ok20", state) : "failure processing a\\[;5]\\[8,9,1,3]";
        assert getBooleanValue("ok21", state) : "failure processing a\\*\\[8,9,1,3]";
        assert getBooleanValue("ok22", state) : "failure processing a\\>[star(),[8,9,1,3]]";
        assert getBooleanValue("ok30", state) : "failure processing a\\[;5]\\[3,2,1]";
        assert getBooleanValue("ok31", state) : "failure processing a\\*\\[3,2,1]";
        assert getBooleanValue("ok32", state) : "failure processing a\\>[star(),[3,2,1]]";
        assert getBooleanValue("ok40", state) : "failure processing a\\[;5]\\[0,0,0]";
        assert getBooleanValue("ok41", state) : "failure processing a\\*\\[0,0,0]";
        assert getBooleanValue("ok42", state) : "failure processing a\\>[star(),[0,0,0]]";
    }

    /**
     * Tests that using functions for extractions works for multiple axes.
     *
     * @throws Throwable
     */
    public void testFunctionExtraction() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "even(k,v)→mod(k,2)≡0;\n" +
                "odd(k,v)→mod(k,2)≡1;\n");
        addLine(script, "z. := n(4,4,n(16))\\2@even\\2@odd;");
        addLine(script, "w. := n(4,4,n(16))\\!2@even\\!2@odd;"); // preserve indices
        addLine(script, "ok_size0 := rank(z.)==2 && (@&& ⊙ 2==dim(z.));");
        addLine(script, "ok_values0 := @&& ⊙ ([[1,3],[9,11]] == z.)`*;");
        addLine(script, "ok_size1 := rank(w.)==2 && (@&& ⊙ 2==dim(w.));");
        addLine(script, "ok_values1 := @&& ⊙ ({0:{1:1, 3:3}, 2:{1:9, 3:11}} == w.)`*;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok_size0", state) : "extraction failed to give correct shape of result";
        assert getBooleanValue("ok_values0", state) : "extraction failed to give correct values of result";
        assert getBooleanValue("ok_size1", state) : "extraction failed to give correct shape of strict result";
        assert getBooleanValue("ok_values1", state) : "extraction failed to give correct values of strict result";
    }

    /**
     * The contract is that extractions using stem indices, \> must be lists and not
     * just function references, so trying to do one with a function should raise an error.
     *
     * @throws Throwable
     */
    public void testFunctionExtractionStemIndexFailure() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "even(k,v)→mod(k,2)≡0;\n" +
                "odd(k,v)→mod(k,2)≡1;\n");
        addLine(script, "n(3,4)\\>2@even;");
        boolean good = false;
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        try {
            interpreter.execute(script.toString());
        } catch (BadArgException bax) {
            good = true;
        }
        assert good : "passing a function reference to the \\> operator should fail.";

    }

    public static String BIG_JSON_OBJECT = DebugUtil.getDevPath() + "/qdl/language/src/main/resources/test.json";

    /**
     * This grabs a large randomly generated JSON object (which is stashed for reproducibility)
     * turns it into a stem, then interrogates it using the extraction operator. It compares
     * \ with \> and double checks that a bunch of cases work right.
     *
     * @throws Throwable
     */
    public void testJSONExtractions() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := from_json(file_read('" + BIG_JSON_OBJECT + "'));");

        addLine(script, "q.:=a\\[1,2]\\friends\\*\\name;");
        addLine(script, "q2.:=a\\>[[1,2],'friends',star(),'name'];");
        addLine(script, "q_value.:=[['Leola Mcdowell','Pate Vaughn','Bennett Kane'],['Jackie Webb','Baker Hartman','Sandra Johns']];");
        addLine(script, "ok :=reduce(@&&, reduce(@&&,q.==q_value.));");
        addLine(script, "ok_2 :=reduce(@&&, reduce(@&&,q2.==q_value.));");
        addLine(script, "ok1 := reduce(@&&, a\\*\\index == [0,1,2,3,4,5,6,7,8,9]);");
        addLine(script, "ok1_2 := reduce(@&&, a\\>[star(),'index'] == [0,1,2,3,4,5,6,7,8,9]);");
        addLine(script, "ok2 := reduce(@&&, 10 + a\\*\\age == [46,48,50,30,45,31,32,36,49,37]);");
        addLine(script, "ok2_2 := reduce(@&&,  a\\>[star(),'age'] + 10 == [46,48,50,30,45,31,32,36,49,37]);");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "a\\[1,2]\\friends\\*\\name failed";
        assert getBooleanValue("ok_2", state) : "a\\>[[1,2],'friends',star(),name] failed";
        assert getBooleanValue("ok1", state) : "a\\*\\index failed";
        assert getBooleanValue("ok1_2", state) : "a\\[star(),'index'] failed";
        assert getBooleanValue("ok2", state) : " 10 + a\\*\\age failed";
        assert getBooleanValue("ok2_2", state) : "a\\>[star(),'age'] + 10 failed";
    }
      /*
         // x. is a very messy configuration from an OA4MP script.
         // This is everything from the test below.

         x. := from_json(file_read('/home/ncsa/dev/ncsa-git/qdl/language/src/test/resources/extract.json'));

          z. := x\qdl\*\xmd
          size(z.)==2 && (z.0.'exec_phase'=='pre_auth') && (size(z.1.'exec_phase')==3);
          size(x\qdl\*) == 2;

          w. := x\qdl\*\load
          w_check. := ['COmanageRegistry/default/identity_token_ldap_claim_source.qdl','COmanageRegistry/default/identity_token_ldap_claim_process.qdl'];
          reduce(@&&, w. == w_check.)

          s. := x\qdl\0\args
          size(s.) == 8 && s.'server_port'==636;

          x\qdl\0\args\return_attributes << List
          (x.qdl.0.args.return_attributes.0) == (x\qdl\0\args\return_attributes\0);
          (x.qdl.0.args.bind_dn) == (x\qdl\0\args\bind_dn)

       */

    //

    /**
     * This is a JSON snippet from OA4MP  that was particularly troublesome to get working right (in OA4MP that is),
     * so it should end up as a regression test, since a lot of forensics were required to explore it and
     * find out why it was malformed -- a first great success for the \ operator.
     * These are several partial tests, since a full test would be a lot. Will probably write them later though...
     *
     * @throws Throwable
     */
    public void testJSONExtractions2() throws Throwable {
        String jsonFile = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/extract.json";
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "x. := from_json(file_read('" + jsonFile + "'));");
        addLine(script, "z. := x\\qdl\\*\\xmd;"); // should give list
        addLine(script, "okz := size(z.)==2 && (z.0.'exec_phase'=='pre_auth') && (size(z.1.'exec_phase')==3);");

        addLine(script, "oky := size(x\\qdl\\*) == 2;"); // should give x.qdl

        addLine(script, "w. := x\\qdl\\*\\load;"); // should give x.qdl.[0,2].load
        addLine(script, "w_check. := ['COmanageRegistry/default/identity_token_ldap_claim_source.qdl'," +
                "'COmanageRegistry/default/identity_token_ldap_claim_process.qdl'];");
        addLine(script, "okw := reduce(@&&, w. == w_check.);");

        addLine(script, "s. := x\\qdl\\0\\args;"); // should give x.qdl.0.args
        addLine(script, "oks := size(s.) == 8 && s.'server_port'==636;");
        addLine(script, "s. := x\\qdl\\0\\args;"); // should give x.qdl.0.args

        // next is checking that mining various attributes return the right value and shape
        addLine(script, "ok0:=(x.qdl.0.args.bind_dn) == (x\\qdl\\0\\args\\bind_dn);");
        addLine(script, "ok0a:=(x.qdl.0.args.bind_dn) == (x\\>['qdl',0,'args','bind_dn']);");
        addLine(script, "ok1 :=(x.qdl.0.args.return_attributes.0) == (x\\qdl\\0\\args\\return_attributes\\0);");
        addLine(script, "ok1a :=(x.qdl.0.args.return_attributes.0) == (x\\>['qdl',0,'args','return_attributes',0]);");
        addLine(script, "ok2 := x\\qdl\\0\\args\\return_attributes << List;");
        addLine(script, "ok3 := x\\woof == null;");
        addLine(script, "ok4 := x\\['woof'] << Stem;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("oky", state) : "size(x\\qdl\\*) == 2 failed";
        assert getBooleanValue("okz", state) : "x\\qdl\\*\\xmd failed";
        assert getBooleanValue("okw", state) : "x\\qdl\\*\\load failed";
        assert getBooleanValue("oks", state) : "x\\qdl\\0\\args failed";
        assert getBooleanValue("ok0", state) : "(x.qdl.0.args.bind_dn.0) == ((x\\qdl\\0\\args\\bind_dn).0) failed";
        assert getBooleanValue("ok0a", state) : "(x.qdl.0.args.bind_dn.0) == (x\\>['qdl',0,'args','bind_dn',0]) failed";
        assert getBooleanValue("ok1", state) : "(x.qdl.0.args.return_attributes.0) == (x\\qdl\\0\\args\\return_attributes\\0) failed";
        assert getBooleanValue("ok1a", state) : "(x.qdl.0.args.return_attributes.0) == (x\\>['qdl',0,'args','return_attributes',0]) failed";
        assert getBooleanValue("ok2", state) : "x\\qdl\\0\\args\\return_attributes << List failed";
        assert getBooleanValue("ok3", state) : "x\\woof == null failed";
        assert getBooleanValue("ok4", state) : "x\\['woof'] << Stem failed";
    }

    /*
zeta.'Communities:LSCVirgoLIGOGroupMembers' := ['read:/DQSegDB' ,'read:/frames', 'read:/GraceDB'];
zeta.'Communities:LVC:SegDB:SegDBWriter' := 'write:/DQSegDB';
zeta.'gw-astronomy:KAGRA-LIGO:members' := ['read:/GraceDB', 'read:/frames'];
g. := [{'name': 'Services:MailingLists:Testing:eligible_factor'},{'name': 'Communities:LSCVirgoLIGOGroupMembers'},{'name':'Communities:LVC:SegDB:SegDBWriter'}];
i. := g\*\name;
zeta\i.
     */
    public void testExtractionStemKey() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,
                "           zeta.'Communities:LSCVirgoLIGOGroupMembers' := ['read:/DQSegDB' ,'read:/frames', 'read:/GraceDB'];\n" +
                "      zeta.'Communities:LVC:SegDB:SegDBWriter' := 'write:/DQSegDB';\n" +
                "        zeta.'gw-astronomy:KAGRA-LIGO:members' := ['read:/GraceDB', 'read:/frames'];\n" +
                "  g. := [{'name': 'Services:MailingLists:Testing:eligible_factor'},{'name': 'Communities:LSCVirgoLIGOGroupMembers'},{'name':'Communities:LVC:SegDB:SegDBWriter'}];");
        addLine(script, "i. := g\\*\\name;"); // should be a simple 3 element list of names only. 2 of these are keys in g.
        addLine(script, "w.0 := zeta.;"); // have one a level down too
        addLine(script, "okz0 := 2== size(zeta\\i.);");
        // WANT:  zeta\i, == {Communities:LSCVirgoLIGOGroupMembers:[read:/DQSegDB,read:/frames,read:/GraceDB], Communities:LVC:SegDB:SegDBWriter:write:/DQSegDB}
        addLine(script, "okw0 := 2== size(w\\0\\i.);"); // have one a level down too
        addLine(script, "okz1 := (zeta\\i.).'Communities:LVC:SegDB:SegDBWriter' == 'write:/DQSegDB';");
        addLine(script, "okz2 := (zeta\\i.).'Communities:LSCVirgoLIGOGroupMembers'.0 == 'read:/DQSegDB';");
        addLine(script, "okw1 := (w\\0\\i.).'Communities:LVC:SegDB:SegDBWriter' == 'write:/DQSegDB';");
        addLine(script, "okw2 := (w\\0\\i.).'Communities:LSCVirgoLIGOGroupMembers'.0 == 'read:/DQSegDB';");

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("okz0", state) : "2== size(zeta\\i.) failed";
        assert getBooleanValue("okz1", state) : "(zeta\\i.).'Communities:LVC:SegDB:SegDBWriter' == 'write:/DQSegDB' failed";
        assert getBooleanValue("okz2", state) : "(zeta\\i.).'Communities:LSCVirgoLIGOGroupMembers'.0 == 'read:/DQSegDB' failed";
        assert getBooleanValue("okw0", state) : "2== size(w\\0\\i.) failed";
        assert getBooleanValue("okw1", state) : "(w\\0\\i.).'Communities:LVC:SegDB:SegDBWriter' == 'write:/DQSegDB' failed";
        assert getBooleanValue("okw2", state) : "(w\\0\\i.).'Communities:LSCVirgoLIGOGroupMembers'.0 == 'read:/DQSegDB' failed";
    }

    /*
          a. := n(5,5,[;25])~ {'p':{'t':'a', 'u':'b', 'v':'c'}, 'q':{'t':'d', 'u':'e', 'v':'f'}, 'r':{'t':'g', 'u':'h', 'v':'i'}}
       a\[1,2,'p']\[3,'t','q',1]
    {0:[8,6], 1:[13,11], p:{t:a}}
     */

    /**
     * Test mixed data extraction with some gaps.
     *
     * @throws Throwable
     */
    public void testMixedExtraction3() throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "a. := n(5,5,[;25])~ {'p':{'t':'a', 'u':'b', 'v':'c'}, 'q':{'t':'d', 'u':'e', 'v':'f'}, 'r':{'t':'g', 'u':'h', 'v':'i'}};");
        addLine(script, "b. := a\\[1,2,'p']\\[3,'t','q',1];");
        addLine(script, "ok := b.0.0 == 8 && b.0.1 == 6 && b.1.0 == 13 && b.1.1 == 11 && b.p.t=='a';");
        addLine(script, "ok := size(b.)==3 && ok;");


        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "testing ever element in an extraction failed failed";
    }

    /**
     * Tests using a function for extraction with another axis
     *
     * @throws Throwable
     */
    public void testExtractionWithFunction1() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "z. := n(5,5,n(25))\\((k,v)→k<2)\\[1,3];");
        addLine(script, "ok1 := (rank(z.) == 2) && reduce(@&&,2==dim(z.) );"); // result of == is stem with keys
        addLine(script, "ok2 := reduce(@&&, (z.== [[1,3],[6,8]])`*);"); // result of == is stem with keys
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok1", state) : "wrong dimension from extract with function";
        assert getBooleanValue("ok2", state) : "wrong value(s) from extract with function";
    }

    /**
     * Tests using a function to extract non-lists. This has two functions embedded in a nested
     * extraction and the test shows that they are kept distinct.
     *
     * @throws Throwable
     */
    public void testExtractionWithFunction2() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // (a\*\((k,v)→!is_list(v)))\*\((k)→k≡'a')
        addLine(script, "a. ≔ n(3,4,5,6,[;3*4*5*6]);\n" +
                "a.0.'a' ≔ 'a';\n" +
                "a.1.'b' ≔ 'b';\n" +
                "a.0.'c' ≔ 'c';\n" +
                "a.2.'a' ≔ 'd';\n" +
                "a.3.'a' ≔ 'e';"); // big stem that has mixed types
        /*
        Note that in the next expression it is of the form (X)\*\@f the reason is that X is an extraction
        of non-lists, so the shape of X is very different from a. This is computed first then the rest of
        the extraction is using the new stem.
         */
        addLine(script, "z. := (a\\*\\((k,v)→!is_list(v)))\\*\\((k)→k≡'a');"); // result is [{a:a}]
        addLine(script, "ok1 := (rank(z.) == 1) && reduce(@&&,1==dim(z.) );"); // result of == is stem with keys
        addLine(script, "ok2 := z.0.'a' == 'a';"); // result of == is stem with keys
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok1", state) : "wrong dimension from extract with function";
        assert getBooleanValue("ok2", state) : "wrong value(s) from extract with function";
    }
/*
a. ≔ n(3,4,5,6,[;3*4*5*6]);
a.0.'a' ≔ 'a';
a.1.'b' ≔ 'b';
a.0.'c' ≔ 'c';
a.2.'a' ≔ 'd';
a.3.'a' ≔ 'e';

(a\*\((k,v)→!is_list(v)))\*\((k)→k≡'a'); // extracts [{'a':'a'}]
input_form((a\*\((k,v)→!is_list(v)))); // extracts all non-lists elements
 */

    /**
     * This is a pretty complex example. It has a 4 rank stem with various
     * functions at each axis to extract bits. The QDL is
     * <pre>
     * i ≔ 0;
     * X. ≔[];
     * nn()&rarr;n(3,4,[12*i;12*(++i)]);
     * first. ≔ ['a','b','c'];
     * second. ≔ ['A','B','C'];
     * while[k&isin;first.]
     *    [while[m&isin;second.]
     *          [X.k.m ≔ nn();
     *          ]; // end inner
     *    ];// end outer
     * print(X.);
     * Y. ≔ X\*\((k,v)&rarr;k&equiv;'A')\((k,v)&rarr;k&lt;3)\((k,v)&rarr;mod(v,3)&equiv;1);
     * </pre>
     * It selects per axis (all) (key = A) (key < 2) (value mod(3)==1)
     * The result is
     * <pre>
     *     check. ≔ {
     *      'a':[[[1],[4,7],[10]]],
     *      'b':[[[37],[40,43],[46]]],
     *      'c':[[[73],[76,79],[82]]]
     *      };
     * </pre>
     * and the function that checks these across a specific axis then checks all values match is
     * <pre>
     *     cc(a) → ⊗∧⊙(Y.a.0 ≡ check.a.0)`*
     * </pre>
     * @throws Throwable
     */
    public void testExtractionWithFunction3() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,"i ≔ 0;\n" +
                "X. ≔[];\n" +
                "nn()→n(3,4,[12*i;12*(++i)]);\n" +
                "first. ≔ ['a','b','c'];\n" +
                "second. ≔ ['A','B','C'];\n" +
                "while[k∈first.]\n" +
                "   [while[m∈second.]\n" +
                "         [X.k.m ≔ nn();\n" +
                "         ]; // end inner\n" +
                "   ];// end outer");
        addLine(script,"Y. ≔ X\\*\\((k,v)→k≡'A')\\((k,v)→k<3)\\((k,v)→mod(v,3)≡1);\n"); // result
        addLine(script,"check.≔{'a':[[[1],[4,7],[10]]], 'b':[[[37],[40,43],[46]]], 'c':[[[73],[76,79],[82]]]};"); // check
        addLine(script,"cc(a) → ⊗∧⊙(Y.a.0 ≡ check.a.0)`*;"); // checks a stem value
        addLine(script,"ok1 := 12 == size(Y`*);");
        addLine(script,"ok2 := cc('a') ∧ cc('b') ∧ cc('c');");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok1", state) : "wrong size from extract with function";
        assert getBooleanValue("ok2", state) : "wrong  elements extract with function";
    }
/*Zzdrfjk,,*/

    /**
     * Tests that passing in a stem restricted to an axis is preserved
     * @throws Throwable
     */
    public void testAxisAsFunctionArgument() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,"qqq(x)→@size∀[x];");
        addLine(script,"ok := ⊗∧⊙(qqq(n(3,4)`0) ≡ [4,4,4])`*;"); // tests that the axis is preserved
       QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : "Did not preserve passing axis as argument to a function";
    }
    /**
     * test contract for {@link StemEvaluator#DIFF} function.
     *
     * @throws Throwable
     */
    public void testDiff() throws Throwable {

        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ok0 := size(diff('a', 'a')) == 0;"); // trivial scalar check
        addLine(script, "ok1 := reduce(@&&, diff('a', 'b').0 == ['a','b']);"); // scalar check, return [[a,b]]
        addLine(script, "out0. := diff({'a':'p','b':'q'},{'a':'p','b':'r', 'c':'t'});");
        addLine(script, "ok2 := size(out0.)==1 && reduce(@&&, out0.'b' == ['q','r']);"); // {b:[q,r]} is expected result
        addLine(script, "out1. := diff({'a':'p','b':'q'},{'a':'p','b':'r', 'c':'t'}, false);"); // test subsetting off
        addLine(script, "ok3 := size(out1.) == 2 && reduce(@&&,out1.'b'==['q','r']) && reduce(@&&,out1.'c'==[null,'t']);");
        addLine(script, "out2. := diff({'a':'p','b':'q'},'p');"); // test diff with a scalar
        addLine(script, "ok4 := size(out2.) == 1 && reduce(@&&,out2.'b'==['q','p']);");
        addLine(script, "out3. := diff('p', {'a':'p','b':'q'});"); // test diff with a scalar
        addLine(script, "ok5 := size(out3.) == 1 && reduce(@&&,out3.'b'==['p','q']);");


        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok0", state) : StemEvaluator.DIFF + " for equal scalars fails ";
        assert getBooleanValue("ok1", state) : StemEvaluator.DIFF + " for different scalars fails";
        assert getBooleanValue("ok2", state) : StemEvaluator.DIFF + " for basic stems with subsetting on fails";
        assert getBooleanValue("ok3", state) : StemEvaluator.DIFF + " for basic stems with subsetting off fails";
        assert getBooleanValue("ok4", state) : StemEvaluator.DIFF + " for stem vs scalar fails";
        assert getBooleanValue("ok5", state) : StemEvaluator.DIFF + " for scalar vs stem fails";
    }

    public void testExciseList() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ϱ. := excise(n(5,5,[;3]),[0,2]);"); // matrix
        addLine(script, "ok:=reduce(@&&, dim(ϱ.)==[5,2]) && rank(ϱ.)==2;");
        addLine(script, "ok1:=reduce(@&&, reduce(@&&,ϱ.==[[1,1],[1],[1,1],[1,1],[1]]));");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.EXCISE + " incorrect shape of result";
        assert getBooleanValue("ok1", state) : StemEvaluator.EXCISE + " incorrect values for result";
    }
    public void testExciseOperator() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "ϱ. := n(5,5,[;3]) !~ [0,2];"); // matrix
        addLine(script, "ok:=reduce(@&&, dim(ϱ.)==[5,2]) && rank(ϱ.)==2;");
        addLine(script, "ok1:=reduce(@&&, reduce(@&&,ϱ.==[[1,1],[1],[1,1],[1,1],[1]]));");
        addLine(script, "ok2 := ⊗∧⊙[0,2] ∉ ϱ.;"); // actual contract for excise. a. !~ b. <==> b. ∉ a.
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.EXCISE + " incorrect shape of result";
        assert getBooleanValue("ok1", state) : OpEvaluator.EXCISE + " incorrect values for result";
        assert getBooleanValue("ok2", state) : OpEvaluator.EXCISE + " contract not fulfilled";
    }

    /**
     * The parser has the . operator outranking everything since that usuall works. Special case is
     * <pre>
     *     x. !~ y
     * </pre>
     * which would get parsed normally as
     * <pre>
     *      .
     *    /  \
     *   x    !~ y  <--- monadic operator
     * </pre>
     * We fix it in {@link QDLListener#exitDotOp(QDLParserParser.DotOpContext)} to specifically
     * do the right thing. The alternative is to change OOO in the parser which is complex and
     * has seemingly unending consequences.
     * @throws Throwable
     */
    public void testExciseOperatorOOO() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script, "w ≔ 'wlcg.groups';");
        addLine(script, "g. ≔ [w,'/cms/uscms','/cms/ALARM','/cms/users'];");
        addLine(script, "h.:=g. !~ w;");
        addLine(script, "ok:=⊗∧⊙h.≡['/cms/uscms','/cms/ALARM','/cms/users'];");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.EXCISE + " incorrect parsing of g. !~ w (?)";
    }

    public void testExciseStem() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Note that the second argument is a stem. The function only looks at the values and this
        // tests that as well.
        addLine(script, "ϱ. := excise({'a':{'p':'c','q':'d'},'f':'c','c':'w'}, {'a':'c'});");
        addLine(script, "ok:= size(ϱ.)==2;"); // dim and rank only work on lists. Best we can do is count
        addLine(script, "b.:=ϱ. == {'a':{'q':'d'}, 'c':'w'};"); // resule is the stem  {a:{q:true}, c:true}
        addLine(script, "ok1 := b.'a'.'q' && b.'c';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : StemEvaluator.EXCISE + " incorrect shape of result";
        assert getBooleanValue("ok1", state) : StemEvaluator.EXCISE + " incorrect values for result";
    }

    public void testExciseStemOperator() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Note that the second argument is a stem. The function only looks at the values and this
        // tests that as well.
        addLine(script, "ϱ. := {'a':{'p':'c','q':'d'},'f':'c','c':'w'} !~ {'a':'c'};");
        addLine(script, "ok:= size(ϱ.)==2;"); // dim and rank only work on lists. Best we can do is count
        addLine(script, "b.:=ϱ. == {'a':{'q':'d'}, 'c':'w'};"); // resule is the stem  {a:{q:true}, c:true}
        addLine(script, "ok1 := b.'a'.'q' && b.'c';");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.EXCISE + " incorrect shape of result";
        assert getBooleanValue("ok1", state) : OpEvaluator.EXCISE + " incorrect values for result";
    }

    /**
     * There is an idiom of !~list. which reorders a list then negates it. We have to handle this explicitly
     * in the parser as a monadic function. This is a simple regression test.
     * @throws Throwable
     */
    public void testNotJoin() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Note that the second argument is a stem. The function only looks at the values and this
        // tests that as well.
        addLine(script, "ϱ. := !~{2:false,4:true,5:false};");
        addLine(script,"ok := ⊗∧⊙ϱ. ≡ [true,false,true];" );
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) : OpEvaluator.EXCISE + " as monad incorrect result";

    }

    /**
     * This is a regression test that setting the default value for a higher rank stem
     * has the default value faithfully propagated to all included stems.
     * @throws Throwable
     */
    /*
       Test for https://github.com/ncsa/qdl/issues/122
       A. ≔ [[9,0,-8],[-6,1,-4],[6,7,9]];
       B. ≔ {*:0}~[[1],{1:2},{2:3},{3:3}];
       C. := A. + B.;
       ⊗∧⊙(C. ≡ [[10,0,-8],[-6,3,-4],[6,7,12]])`*;
    */

    public void testDefaultValueMatrix() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Note that the second argument is a stem. The function only looks at the values and this
        // tests that as well.
        addLine(script, "A. ≔ [[9,0,-8],[-6,1,-4],[6,7,9]];");
        addLine(script,"B. ≔ {*:0}~[[1],{1:2},{2:3},{3:3}];");
        addLine(script,"C. ≔ A. + B.;");
        addLine(script,"D. ≔ B. + A.;");// the code has to decide which set of indices to take
        addLine(script,"ok := ⊗∧⊙(C. ≡ [[10,0,-8],[-6,3,-4],[6,7,12]])`*;");
        addLine(script,"ok1 := size(indices(A.)) ≡ size(indices(C.)); ");
        addLine(script,"ok2 := ⊗∧⊙(D. ≡ [[10,0,-8],[-6,3,-4],[6,7,12]])`*;");
        addLine(script,"ok3 := size(indices(A.)) ≡ size(indices(D.)); ");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) :  "Default value second test failed for some values";
        assert getBooleanValue("ok1", state) : "Default value second test failed to return correct size of result";
        assert getBooleanValue("ok2", state) :  "Default value first test failed for some values ";
        assert getBooleanValue("ok3", state) : "Default value first test failed to return correct size of result";

    }

    /**
     * In this test, both stems have default values and none of the actual keys are common. This should create
     * a new stem that is all of the keys and carry out the given operation on the default
     * values of one or the other. The test graphically:
     * <pre>
     *      P.                          Q.                            R.
     *     2 | 3                     |    | 11                  17 | 18 | 18
     *   ----+---                ----+----+-----               ----+----+-----
     *     4 | 5                     |    | 12                  19 | 20 | 19
     *   ----+---      +         ----+----+-----      =        ----+----+-----
     *                               |    | 14                     |    | 21
     *                           ----+----+-----               ----+----+-----
     *    * = 7                  * = 15
     * </pre>
     * where the sum is the given value and the default of the <i>other</i> stem, The test
     * is also repeated with the order swapped as a regression test for internal bookkeeping.
     * @throws Throwable
     */
    public void testTwoDefaultValueMatrix() throws Throwable{
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        // Note that the second argument is a stem. The function only looks at the values and this
        // tests that as well.
        addLine(script, "P. ≔ [[2,3],[4,5]]; P.* ≔ 7;");
        addLine(script,"Q.* ≔ 15; Q.0.2 ≔11; Q.1.2≔12; Q.2.2≔14;");
        addLine(script,"R. ≔ P. + Q.;");
        addLine(script,"ok ≔ ⊗∧⊙(R.  ≡ [[17,18,18],[19,20,19],{2:21}])`*;");
        addLine(script,"ok1 ≔ ⊗∧⊙(indices(R.)≡ [[0,0],[0,1],[0,2],[1,0],[1,1],[1,2],[2,2]])`*;" );
        addLine(script,"S. ≔ Q. + P.;"); // test with order swapped, just in case
        addLine(script,"ok2 ≔ ⊗∧⊙(S.  ≡ [[17,18,18],[19,20,19],{2:21}])`*;");
        addLine(script,"ok3 ≔ ⊗∧⊙(indices(S.)≡ [[0,0],[0,1],[0,2],[1,0],[1,1],[1,2],[2,2]])`*;" );
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) :  "test for 2 stems with default values failed for some values";
        assert getBooleanValue("ok1", state) : "test for 2 stems with default values failed to return indices";
        assert getBooleanValue("ok2", state) :  "test for 2 stems with default values failed for some values";
        assert getBooleanValue("ok3", state) : "test for 2 stems with default values failed to return indices";

    }
    /*
       a. ≔ [;5];
       a.(15.0 * 27.00) ≔ 100;
       ⊨ a.405 ≡ 100;
     */

    /**
     * Tests that decimals that evaluate to integers can be used as stem indices. This way
     * if the internal representation is not a Long the user can still use it rather than getting
     * some strange message.
     * @throws Throwable
     */
    public void testDecimalStemIndex() throws Throwable {
        State state = testUtils.getNewState();
        StringBuffer script = new StringBuffer();
        addLine(script,"a. ≔ [;5];");
        addLine(script,"a.(15.0 * 27.00) ≔ 100;");
        addLine(script,"ok ≔ a.405 ≡ 100;");
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("ok", state) :  "Could not set stem value using exact decimals";

    }
}
/*
   zeta.'Communities:LSCVirgoLIGOGroupMembers' := ['read:/DQSegDB' ,'read:/frames', 'read:/GraceDB'];
      zeta.'Communities:LVC:SegDB:SegDBWriter' := 'write:/DQSegDB';
        zeta.'gw-astronomy:KAGRA-LIGO:members' := ['read:/GraceDB', 'read:/frames'];
  g. := [{'name': 'Services:MailingLists:Testing:eligible_factor'},{'name': 'Communities:LSCVirgoLIGOGroupMembers'},{'name':'Communities:LVC:SegDB:SegDBWriter'}]
  i. := g\*\name
  w.0 := zeta.
  w\0\i.
 */

