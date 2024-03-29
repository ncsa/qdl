package edu.uiuc.ncsa.qdl;

import edu.uiuc.ncsa.qdl.evaluate.MetaEvaluator;
import edu.uiuc.ncsa.qdl.evaluate.OpEvaluator;
import edu.uiuc.ncsa.qdl.functions.FStack;
import edu.uiuc.ncsa.qdl.functions.FTable;
import edu.uiuc.ncsa.qdl.functions.FunctionRecordInterface;
import edu.uiuc.ncsa.qdl.module.MIStack;
import edu.uiuc.ncsa.qdl.module.MTStack;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.variables.VStack;
import edu.uiuc.ncsa.qdl.variables.VThing;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  1:12 PM
 */
public class TestUtils {

    public static void set_instance(TestUtils _instance) {
        TestUtils._instance = _instance;
    }

    static TestUtils _instance;

    static public TestUtils newInstance() {
        if (_instance == null) {
            _instance = new TestUtils();
        }
        return _instance;
    }


    public State createStateObject(
            VStack vStack,
            OpEvaluator opEvaluator,
            MetaEvaluator metaEvaluator,
            FStack<? extends FTable<? extends XKey, ? extends FunctionRecordInterface>> ftStack,
            MTStack mTemplates,
            MIStack mInstances,
            MyLoggingFacade myLoggingFacade,
            boolean isServerMode,
            boolean assertionsOn) {
        return new State(
                vStack,
                new OpEvaluator(),
                MetaEvaluator.getInstance(),
                new FStack(),
                new MTStack(),
                new MIStack(),
                null,
                false,
                false,
                true);
    }

    public State getNewState() {
        VStack stack = new VStack();
        stack.pushNewTable();
        State state = createStateObject(
                stack,
                new OpEvaluator(),
                MetaEvaluator.getInstance(),
                new FStack(),
                new MTStack(),
                new MIStack(),
                null,
                false,
                true);
        state.setRootState(state);
        return state;
    }

    /**
     * Send along the state but with the pre-populated symbol stack.
     *
     * @return
     */
    public State getTestState() {

        State state = createStateObject(
                getTestSymbolStack(),
                new OpEvaluator(),
                MetaEvaluator.getInstance(),
                new FStack(),
                new MTStack(),
                new MIStack(),
                null,
                false,
                true);
        return state;
    }


    /**
     * This is a test symbol table that is pre-populated with values called string, long, boolean, stem.var
     * and random.0 random.1 and random.2. It is used for testing and every call will return a new table (!),
     * so stash it if you need to reuse it. The intent is that this has a known state at the beginning of a test.
     *
     * @return
     */
    public VStack getTestSymbolStack() {
        VStack vStack = new VStack();
        vStack.put(new VThing(new XKey("string"), "a string"));
        vStack.put(new VThing(new XKey("long"), 2468L));
        vStack.put(new VThing(new XKey("boolean"), Boolean.TRUE));
        QDLStem stemVariable =new QDLStem();
        stemVariable.put(0, AbstractQDLTester.getRandomString());
        stemVariable.put(1, AbstractQDLTester.getRandomString());
        stemVariable.put(2, AbstractQDLTester.getRandomString());
        vStack.put(new VThing(new XKey("random."), stemVariable));
        return vStack;
    }
}
