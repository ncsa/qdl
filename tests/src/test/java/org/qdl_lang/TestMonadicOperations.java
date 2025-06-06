package org.qdl_lang;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Monad;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLVariable;
import org.qdl_lang.variables.VStack;
import org.qdl_lang.variables.VThing;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  7:18 PM
 */
public class TestMonadicOperations extends AbstractQDLTester {
    TestUtils testUtils = TestUtils.newInstance();

     
    public void testPlusPlusPostfix() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        String variableReference = "i"; // name of the variable
        Long initialValue = 2L;
        Long newValue = 1L + initialValue;
        vStack.put(new VThing(new XKey("i"), new QDLVariable(initialValue)));
        VariableNode var = new VariableNode(variableReference);
        Monad myMonad = new Monad(OpEvaluator.PLUS_PLUS_VALUE, var);
        myMonad.evaluate(state);

        assert myMonad.getResult().equals(initialValue);
        assert checkVThing(variableReference, newValue, state);

        myMonad.evaluate(state);
        myMonad.evaluate(state);
        assert checkVThing(variableReference, newValue+2L, state);
    }

     
    public void testMinusMinusPostfix() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        String variableReference = "i"; // name of the variable
        Long initialValue = 2L;
        Long newValue = initialValue - 1L;
        vStack.put(new VThing(new XKey("i"), new QDLVariable(initialValue)));
        OpEvaluator opEvaluator = new OpEvaluator();
        VariableNode var = new VariableNode(variableReference);
        Monad myMonad = new Monad( OpEvaluator.MINUS_MINUS_VALUE, var);
        myMonad.evaluate(state);

        assert myMonad.getResult().asLong().equals(initialValue);
        assert checkVThing(variableReference, newValue, state);
    }

     
    public void testPlusPlusPrefix() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        String variableReference = "i"; // name of the variable
        Long initialValue = 2L;
        Long newValue = 1L + initialValue;
        vStack.put(new VThing(new XKey("i"), new QDLVariable(initialValue)));
        VariableNode var = new VariableNode(variableReference);
        Monad myMonad = new Monad(OpEvaluator.PLUS_PLUS_VALUE, var, false);
        myMonad.evaluate(state);

        assert myMonad.getResult().asLong().equals(newValue);
        assert checkVThing(variableReference, newValue, state);
    }

     
    public void testMinusMinusPrefix() throws Exception {
        State state = testUtils.getNewState();
        VStack vStack = state.getVStack();
        String variableReference = "i"; // name of the variable
        Long initialValue = 2L;
        Long newValue = initialValue - 1L;
        vStack.put(new VThing(new XKey("i"), new QDLVariable(initialValue)));
        VariableNode var = new VariableNode(variableReference);
        Monad myMonad = new Monad( OpEvaluator.MINUS_MINUS_VALUE, var, false);
        myMonad.evaluate(state);

        assert myMonad.getResult().equals(newValue);
        assert checkVThing(variableReference, newValue, state);
    }


    public void testNotPrefix() throws Exception {
        State state = testUtils.getNewState();
        Boolean initialValue = Boolean.TRUE;
        ConstantNode constantNode = new ConstantNode(asQDLValue(initialValue));
        Boolean newValue = !initialValue;
        Monad myMonad = new Monad( OpEvaluator.NOT_VALUE, constantNode);
        myMonad.evaluate(state);

        assert myMonad.getResult().getValue().equals(newValue);
    }

     
     public void testMinusPrefix() throws Exception {
        // Tests that the opposite of a value is returned.
         String variableReference = "i"; // name of the variable
         Long initialValue = 25L;
         Long newValue = -initialValue;
         ConstantNode constantNode = new ConstantNode(asQDLValue(initialValue));
        State state = testUtils.getNewState();
         Monad myMonad = new Monad( OpEvaluator.MINUS_VALUE, constantNode, false);
         myMonad.evaluate(state);
         assert myMonad.getResult().asLong().equals(newValue);
     }
     
    public void testMMAndPPParsing() throws Throwable {
        StringBuffer script = new StringBuffer();
          addLine(script, "j := 5;");
          addLine(script, "a := ++j - j++;");
          addLine(script, "b := --j - j--;");
          addLine(script, "c := --j - --j;");
          addLine(script, "d := ++j - --j;");
        State state = testUtils.getNewState();

        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getLongValue("a" , state)== 0L;
        assert getLongValue("b" , state)== 0L;
        assert getLongValue("c" , state)== 1L;
        assert getLongValue("d" , state)== 1L;
        assert getLongValue("j" , state)== 3L;


    }
}
