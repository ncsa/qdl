package org.qdl_lang.parsing;

import org.qdl_lang.expressions.ExpressionNode;
import org.qdl_lang.expressions.ExpressionStemNode;
import org.qdl_lang.exceptions.InterruptException;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.state.SIEntry;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.Element;
import org.qdl_lang.statements.ModuleStatement;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLSetNode;
import org.qdl_lang.variables.StemListNode;
import org.qdl_lang.variables.StemVariableNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.qdl_lang.evaluate.SystemEvaluator.SAY_FUNCTION;

/**
 * /**
 * The main class that runs a parse tree
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/20 at  6:15 AM
 */
public class QDLRunner implements Serializable {
    public boolean isEchoModeOn() {
        return echoModeOn;
    }

    public void setEchoModeOn(boolean echoModeOn) {
        this.echoModeOn = echoModeOn;
    }

    boolean echoModeOn = false;

    public State getState() {
        return state;
    }

    Boolean prettyPrint = Boolean.FALSE;

    public Boolean getPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(Boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    /**
     * You may inject state at runtime if you need this to start with some existing state.
     *
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    State state;

    public QDLRunner(ArrayList<Element> elements) {
        this.elements = elements;
    }

    public QDLInterpreter getInterpreter() {
        return interpreter;
    }

    public void setInterpreter(QDLInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    QDLInterpreter interpreter;

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(ArrayList<Element> elements) {
        this.elements = elements;
    }

    ArrayList<Element> elements;

    public void run() throws Throwable {
        State currentState = getState();
        run(0, currentState, false);

    }

    public void restart(SIEntry siEntry, boolean noInterrupt) throws Throwable {
        run(siEntry.statementNumber + 1, siEntry.state, noInterrupt);
    }

    public Object getLastResult() {
        return lastResult;
    }

    Object lastResult = null;
    protected void run(int startIndex, State currentState, boolean noInterrupt) throws Throwable {
        for (int i = startIndex; i < elements.size(); i++) {
            //for (Element element : elements) {
            Element element = elements.get(i);
            if (element.getStatement() != null) {

                // it can happen that the parser returns an empty statement. Skip it.
                Statement stmt = element.getStatement();
                if (stmt instanceof ModuleStatement) {
                    ModuleStatement ms = (ModuleStatement) stmt;
                    ms.evaluate(currentState);
                    lastResult = null;
                } else {
                    if (isEchoModeOn()) {
                        // used by the workspace to print each statement's result to the console.
                        // Checking for expression nodes allows for printing things like
                        // (((2+2)))
                        // correctly
                        if (((stmt instanceof ExpressionNode) && (((ExpressionNode) stmt).getNodeType()!=ExpressionInterface.ASSIGNMENT_NODE))) {
                            ExpressionNode expression = (ExpressionNode) stmt;
                            if (expression instanceof Polyad) {
                                // so if this is already a print statement, don't wrap it in one
                                boolean isPrint = ((Polyad) expression).getName().equals(SAY_FUNCTION);
                                if (!isPrint) {
                                    Polyad p = new Polyad(SAY_FUNCTION);
                                    p.addArgument(expression);
                                    p.addArgument(new ConstantNode(prettyPrint, Constant.BOOLEAN_TYPE));
                                    stmt = p;

                                }
                            } else {
                                Polyad p = new Polyad(SAY_FUNCTION);
                                p.addArgument(expression);
                                p.addArgument(new ConstantNode(prettyPrint, Constant.BOOLEAN_TYPE));
                                stmt = p;
                            }
                        }
                        if (stmt instanceof QDLSetNode || stmt instanceof StemVariableNode || stmt instanceof StemListNode || stmt instanceof ExpressionStemNode) {
                            stmt.evaluate(state);
                            ConstantNode cNode = new ConstantNode(((ExpressionInterface) stmt).getResult(), Constant.STEM_TYPE);
                            Polyad p = new Polyad(SAY_FUNCTION);
                            p.addArgument(cNode);
                            p.addArgument(new ConstantNode(prettyPrint, Constant.BOOLEAN_TYPE));
                            stmt = p;
                        }
                    }
                    try {
                       lastResult = stmt.evaluate(currentState);
                    } catch (InterruptException ix) {
                        if(!noInterrupt) {
                        if (!ix.getSiEntry().initialized) {
                            // if it was set up, pass it up the stack
                            ix.getSiEntry().qdlRunner = this;
                            ix.getSiEntry().statementNumber = i; // number where this happened.
                            ix.getSiEntry().interpreter = getInterpreter();
                            ix.getSiEntry().initialized = true;
                        }
                        throw ix;
                        }
                    }
                }
            }
        }
    }

}
