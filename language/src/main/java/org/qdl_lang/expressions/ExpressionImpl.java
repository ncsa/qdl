package org.qdl_lang.expressions;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:15 PM
 */
public abstract class ExpressionImpl implements ExpressionNode {
    public ExpressionImpl() {

    }

    public ExpressionImpl(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    TokenPosition tokenPosition = null;

    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    @Override
    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    @Override
    public boolean hasTokenPosition() {
        return tokenPosition != null;
    }

    public ExpressionImpl(int operatorType, TokenPosition tokenPosition) {
        this(operatorType);
        this.tokenPosition = tokenPosition;
    }

    @Override
    public boolean hasAlias() {
        return alias != null;
    }


    public ExpressionImpl(int operatorType) {
        this.operatorType = operatorType;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    String alias = null;

    @Override
    public int getArgCount() {
        return getArguments().size();
    }

    public ExpressionInterface getLastArg() {
        return getArgAt(getArgCount() - 1);
    }

    public Object evalLastArg(State state) {
        try {
            return getArguments().get(getArgCount() - 1).evaluate(state);
        } catch (QDLException returnException) {
            // These should be passed back, since they are needed for the internal operation of QDL
            // E.g. IndexError, NamespaceError, ReturnException,...
            throw returnException;
        } catch (Throwable t) {
            // Generate a bona fide error if there is a non-QDL one.
            throw new QDLExceptionWithTrace(t, this);
        }
    }

    /**
     * The valence is the number of arguments this expression allows. <br/><br/>
     * 0 = niladic
     * 1 = monadic
     * 2 = dyadic
     * 3 = polyadic (more than 2)
     */
    protected int valence = 0;

    protected ArrayList<ExpressionInterface> arguments = new ArrayList<>();

    @Override
    public ArrayList<ExpressionInterface> getArguments() {
        return arguments;
    }

    @Override
    public ExpressionInterface getArgAt(int index) {
        if ((index < 0) || (getArgCount() <= index)) {
            return null;
        }
        return getArguments().get(index);
    }

    public Object evalArg(int index, State state) {
        try {
            return getArguments().get(index).evaluate(state);
        } catch (QDLException returnException) {
            // These should be passed back, since they are needed for the internal operation of QDL
            // E.g. IndexError, NamespaceError, ReturnException,...
            throw returnException;
        } catch (Throwable t) {
            // Generate a bona fide error if there is a non-QDL one.
            throw new QDLExceptionWithTrace(t, this);
        }
    }

    @Override
    public void setArguments(ArrayList<ExpressionInterface> arguments) {
        this.arguments = arguments;
    }

    protected Object result;

    @Override
    public boolean isEvaluated() {
        return evaluated;
    }

    @Override
    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    @Override
    public Object getResult() {
        if (!evaluated) {
            throw new UnevaluatedExpressionException("source='" + (getSourceCode() == null ? "(none)" : getSourceCode()) + "'");
        }
        return result;
    }

    /**
     * Used when resolving function references to query the operator itself as to how many
     * arguments it accepts.
     *
     * @return
     */
    public boolean isSizeQuery() {
        return sizeQuery;
    }

    public void setSizeQuery(boolean sizeQuery) {
        this.sizeQuery = sizeQuery;
    }

    boolean sizeQuery = false;

    int resultType = Constant.UNKNOWN_TYPE;

    @Override
    public int getResultType() {
        return resultType;
    }

    @Override
    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public void setResultType(int resultType) {
        this.resultType = resultType;
    }

    boolean evaluated = false;

    public void setOperatorType(int operatorType) {
        this.operatorType = operatorType;
    }

    int operatorType = OpEvaluator.UNKNOWN_VALUE;

    /**
     * This is the operator equiavalent of a function. {@link OpEvaluator#UNKNOWN_VALUE} is the default.
     *
     * @return
     */
    @Override
    public int getOperatorType() {
        return operatorType;
    }

    @Override
    public List<String> getSourceCode() {
        if (sourceCode == null) {
            sourceCode = new ArrayList<>();
        }
        return sourceCode;
    }

    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    List<String> sourceCode;

    @Override
    public String toString() {
        return "source=" +
                "\"" + sourceCode + '\"';
    }

    /**
     * The arguments to this polyad that have been evaluated vis a vis a specific state.
     * The intent is that this is used in evaluating functions sent via a module, where the
     * arguments are evaluated in the ambient state and forwarded to the module for
     * later evaluation. This has to be done since there is no way to figure out the state
     * in general where these evaluate except at exactly the point where they are called.
     *
     * @return
     */
    public List<Object> getEvaluatedArgs() {
        return evaluatedArgs;
    }

    /**
     * Evaluate the arguments using the given state and set the {@link #setEvaluatedArgs(List)}
     * Calling this evaluates the args.
     * @param state
     * @return
     */
    public List<Object> evaluatedArgs(State state) {
        evaluatedArgs = new ArrayList<>(getArgCount());
        for (int i = 0; i < getArgCount(); i++) {
            evaluatedArgs.add(evalArg(i, state));
        }
        return evaluatedArgs;
    }


    public void setEvaluatedArgs(List<Object> evaluatedArgs) {
        this.evaluatedArgs = evaluatedArgs;
    }

    List<Object> evaluatedArgs = null;

    public boolean hasEvaluatedArgs() {
        return evaluatedArgs != null;
    }

}
