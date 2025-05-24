package org.qdl_lang.expressions;

import org.qdl_lang.exceptions.IndexError;
import org.qdl_lang.exceptions.NoDefaultValue;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;

/**
 * After a parser change to treat the dot as an operator, this was
 * introduced. It mostly has replaced the older {@link ExpressionStemNode},
 * but not quite everywhere yet.
 * <p>Created by Jeff Gaynor<br>
 * on 6/12/21 at  6:42 AM
 */
public class ESN2 extends ExpressionImpl {
    @Override
    public Object evaluate(State state) {
        return get(state);
    }

    public boolean isDefaultValueNode() {
        return defaultValueNode;
    }

    public void setDefaultValueNode(boolean defaultValueNode) {
        this.defaultValueNode = defaultValueNode;
    }

    boolean defaultValueNode = false;

    @Override
    public ExpressionInterface makeCopy() {
        throw new NotImplementedException();
    }

    public ExpressionInterface getLeftArg() {
        if (getArguments().isEmpty()) {
            return null;
        }
        return getArguments().get(0);
    }

    public void setLeftArg(ExpressionInterface swri) {
        if (getArguments().size() == 0) {
            getArguments().add(swri);
        } else {
            getArguments().set(0, swri);
        }
    }

    public void setRightArg(ExpressionInterface swri) {
        if (getArguments().size() == 1) {
            getArguments().add(swri);
        } else {
            getArguments().set(1, swri);
        }
    }

    public ExpressionInterface getRightArg() {
        if (getArguments().size() < 2) {
            return null;
        }
        return getArguments().get(1);
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    Object defaultValue;

    protected Object get(State state) {
        if (isDefaultValueNode()) {
            return processDefaultValue(state);
        }
        ArrayList<ExpressionInterface> leftArgs = new ArrayList<>();
        ArrayList<ExpressionInterface> rightArgs = new ArrayList<>();
        linearizeTree(leftArgs, rightArgs);

        // Evaluation pass. Make sure everything resolves w.r.t. the state
        IndexList indexList = getIndexList(state, rightArgs);
        if (indexList.size() == 0) {
            // just wants whole stem, no indices
            ExpressionInterface ei = leftArgs.get(leftArgs.size() - 1);
            Object r0 = ei.evaluate(state);
            if (r0 == null) {
                // try to make an error report
                if (ei instanceof VariableNode) {
                    VariableNode vn = (VariableNode) ei;
                    throw new UnknownSymbolException("the stem '" + vn.getVariableReference() + "' was not found.", this);
                }
                throw new UnknownSymbolException("left argument must evaluate to a stem ", this);
            }
            if (!(r0 instanceof QDLStem)) {
                if (ei instanceof VariableNode) {
                    VariableNode vn = (VariableNode) ei;
                    throw new UnknownSymbolException("the variable '" + vn.getVariableReference() + "' is not a stem.", this);
                }
                throw new UnknownSymbolException("left argument must evaluate to a stem ", this);
            }
            QDLStem stemVariable = (QDLStem) r0;
            setResult(stemVariable);
            setResultType(Constant.STEM_TYPE);
            setEvaluated(true);
            return stemVariable;
        }
        // Made it this far. Now we need to do this again, but handing off indices
        // to the stem as needed.
        try {
            computeNullity(indexList);
        } catch (IndexError indexError) {
            indexError.setStatement(this);
            throw indexError;
        }

        Object r0 = leftArgs.get(leftArgs.size() - 1).evaluate(state);
        if (r0 == null) {
            ExpressionInterface lll = leftArgs.get(leftArgs.size() - 1);
            if (lll instanceof VariableNode) {
                throw new UnknownSymbolException("variable '" + ((VariableNode) lll).getVariableReference() + "' is undefined", lll);
            }
            throw new IndexError("left argument is undefined", lll);
        }
        if (!(r0 instanceof QDLStem)) {
            throw new IndexError("error: left argument must evaluate to be a stem ", this);
        }
        QDLStem stemVariable = (QDLStem) r0;
        IndexList r;
        try {
            r = stemVariable.get(indexList, true);
        } catch (IndexError indexError) {
            if (stemVariable.hasDefaultValue()) {
                setResult(stemVariable.getDefaultValue());
                setResultType(Constant.getType(result));
                setEvaluated(true);
                return result;
            }
            indexError.setStatement(this);
            throw indexError;
        }
        Object result = r.get(0);
        setResult(result);
        setResultType(Constant.getType(result));
        setEvaluated(true);
        return result;

    }

    private Object processDefaultValue(State state) {
        getLeftArg().evaluate(state);
        Object defaultValue = null;
        ExpressionInterface ei = getLeftArg();
        while (ei instanceof ParenthesizedExpression) {
            ei = ((ParenthesizedExpression) ei).getExpression();
        }
        switch (ei.getNodeType()) {
            case ExpressionInterface.VARIABLE_NODE:
                VariableNode vn = (VariableNode) ei;
                if (vn.getResultType() == Constant.STEM_TYPE) {
                    QDLStem stem = (QDLStem) vn.getResult();
                    if (stem.hasDefaultValue()) {
                        defaultValue = stem.getDefaultValue();
                    } else {
                        throw new NoDefaultValue("No default value set for this stem", getLeftArg());
                    }
                } else {
                    throw new QDLExceptionWithTrace("Variable does not reference a stem", getLeftArg());
                }
                break;
            case ExpressionInterface.STEM_NODE:
                QDLStem stem = (QDLStem) ei.getResult();
                if (stem.hasDefaultValue()) {
                    defaultValue = stem.getDefaultValue();
                } else {
                    throw new NoDefaultValue("No default value set for this stem", getLeftArg());
                }
                break;
            case ExpressionInterface.EXPRESSION_STEM2_NODE:
                ESN2 esn2 = (ESN2) ei;
                if(isDefaultValueNode()){
                    if(esn2.getResult() instanceof QDLStem){
                        QDLStem stem2 = (QDLStem) esn2.getResult();
                        if(stem2.hasDefaultValue()){
                            defaultValue = ((QDLStem)esn2.getResult()).getDefaultValue();
                        }else{
                            throw new NoDefaultValue("No default value set for this stem", getLeftArg());
                        }
                    }else{
                        throw new QDLExceptionWithTrace("only stems have default values", getLeftArg());
                    }
                }else {
                    defaultValue = esn2.getResult();
                }
                break;
            default:
                throw new QDLExceptionWithTrace("variable does not support default", getLeftArg());
        }

        setEvaluated(true);
        setResult(defaultValue);
        setResultType(Constant.getType(defaultValue));
        return defaultValue;
    }

    /**
     * Actual stem contract: Evaluates the indices from right to left and does the
     * evaluations, Nullity refers to how many of the indices "go away" on actual evaluation.
     * When this is done, the index set is simply indices, ready for set or get in the stem:
     * <pre>
     *     rank(stem) + nullity(stem) == dim(stem)
     * </pre>
     * <i>for a given index.</i> So if we have
     * <pre>
     *     x.1.2 := 5; p := 1; q := 2;
     * </pre>
     * Computing
     * <pre>
     *     w.x.p.q
     *       |---|  = dim is 3
     *         => x.1.2 = 5
     *              |-|
     *                + - nullity is 2, since these 2 go away
     *     = w.5
     *         |
     *         +- rank is 1
     * </pre>
     * hence
     * <pre>
     *     rank + nullity = dim
     *       1  +   2     =  3
     * </pre>
     *
     * @param indexList
     */
    protected void computeNullity(IndexList indexList) {

        IndexList r;

        for (int i = indexList.size() - 1; 0 <= i; i--) {
            if (indexList.get(i) instanceof QDLStem) {
                if (i == indexList.size() - 1) {
                    continue;
                }
                r = ((QDLStem) indexList.get(i)).get(indexList.tail(i + 1), false);
                indexList.truncate(i);
                indexList.addAll(i, r);
            } else {
                // Case that left most argument is not a stem, but that the rhs is
                // which implies the user made a boo-boo
                if (i < indexList.size() - 1) {
                    if (indexList.get(i + 1) instanceof QDLStem) {
                        throw new IndexError("error: lhs is not a stem.", null);
                    }
                }
            }
        }
    }

    private IndexList getIndexList(State state, ArrayList<ExpressionInterface> rightArgs) {
        IndexList indexList = new IndexList(rightArgs.size());
        boolean isFirst = true;
        Object obj = null;

        for (int i = rightArgs.size() - 1; 0 <= i; i--) {
            if (isFirst) {
                obj = rightArgs.get(i).evaluate(state);
                isFirst = false;
            } else {
                // Stem contract: Assume everything is a stem, if not, check if scalar.
                if (rightArgs.get(i) instanceof VariableNode) {
                    VariableNode vNode = (VariableNode) rightArgs.get(i);
                    if (vNode.isStem()) {
                        obj = vNode.evaluate(state);

                    } else {
                        VariableNode vNode1 = new VariableNode(vNode.getVariableReference() + QDLStem.STEM_INDEX_MARKER);
                        obj = vNode1.evaluate(state);
                        if (obj == null) {
                            // try it as a simple scalar
                            obj = vNode.evaluate(state);
                        }

                    }

                } else {
                    obj = rightArgs.get(i).evaluate(state);

                }
            }

            if (obj == null) {
                VariableNode vNode = null;
                ExpressionInterface x;
                if (rightArgs.get(i) instanceof ParenthesizedExpression) {
                    x = ((ParenthesizedExpression) rightArgs.get(i)).getExpression();
                    if (x instanceof VariableNode) {
                        vNode = (VariableNode) x;
                    } else {
                        throw new IllegalStateException("Unknown/unexpected value for variable");
                    }
                }
                if (rightArgs.get(i) instanceof VariableNode) {
                    vNode = (VariableNode) rightArgs.get(i);
                }
                obj = vNode.getVariableReference();
            }
            indexList.set(i, obj);
        }
        return indexList;
    }

    /**
     * Takes the node (which is a tree) structure and converts it to a list structure.
     *
     * @param leftArgs
     * @param rightArgs
     */
    private void linearizeTree(ArrayList<ExpressionInterface> leftArgs, ArrayList<ExpressionInterface> rightArgs) {
        //     leftArgs.add(getLeftArg());
        if (getRightArg() != null) {
            // This can have a null right arg, e.g. a.b.
            rightArgs.add(getRightArg());
        }
        ExpressionInterface swri = getLeftArg();
        while (swri instanceof ESN2) {
            ESN2 esn2 = (ESN2) swri;
            leftArgs.add(esn2.getLeftArg());
            rightArgs.add(esn2.getRightArg());
            swri = esn2.getLeftArg();
        }
        leftArgs.add(swri);
        Collections.reverse(rightArgs);
    }

    public boolean remove(State state) {
        ArrayList<ExpressionInterface> leftArgs = new ArrayList<>();
        ArrayList<ExpressionInterface> rightArgs = new ArrayList<>();
        linearizeTree(leftArgs, rightArgs);
        // Evaluation pass. Make sure everything resolves w.r.t. the state
        IndexList indexList = getIndexList(state, rightArgs);

        // Made it this far. Now we need to do this again, but handing off indices
        // to the stem as needed.
        computeNullity(indexList);
        QDLStem stemVariable = null;
        boolean gotOne = false;
        ExpressionInterface realLeftArg = leftArgs.get(leftArgs.size() - 1);
        Object arg0 = realLeftArg.evaluate(state);
        if (arg0 instanceof QDLStem) {
            QDLStem arg = (QDLStem) arg0;
            try {
                return arg.remove(indexList);
            } catch (IndexError indexError) {
                // means they want something removed that is not there. Peachy
                return true;
            }
        }
        return false;
    }

    public void set(State state, Object newValue) {
        ArrayList<ExpressionInterface> leftArgs = new ArrayList<>();
        ArrayList<ExpressionInterface> rightArgs = new ArrayList<>();
        linearizeTree(leftArgs, rightArgs);
        // Evaluation pass. Make sure everything resolves w.r.t. the state
        IndexList indexList = getIndexList(state, rightArgs);

        // Made it this far. Now we need to do this again, but handing off indices
        // to the stem as needed.
        computeNullity(indexList);
        QDLStem stemVariable = null;
        boolean gotOne = false;
        ExpressionInterface realLeftArg = leftArgs.get(leftArgs.size() - 1);
        realLeftArg.evaluate(state);
        boolean isParenthesized = realLeftArg instanceof ParenthesizedExpression;
        if (isParenthesized) {
            realLeftArg = ((ParenthesizedExpression) realLeftArg).getExpression();
        }
        //leftArgs.get(leftArgs.size() - 1).evaluate(state);
        if (realLeftArg.getResult() instanceof QDLStem) {
            stemVariable = (QDLStem) realLeftArg.getResult();
            gotOne = true;
        }
        if (realLeftArg instanceof VariableNode) {
            VariableNode vNode = (VariableNode) realLeftArg;
            // Either it is not set or set to QDLNull
            if (vNode.getResult() == null || (vNode.getResult() instanceof QDLNull)) {
                // then this variable does not exist in the symbol table. Add it
                stemVariable = new QDLStem();
                state.setValue(vNode.getVariableReference(), stemVariable);
            }
            gotOne = true;
        }
        if (!gotOne) {
            throw new IndexError("no such element", null);
        }
        // it is possible that the left most expression is a stem node, so make sure there is
        // something to return
        if (stemVariable == null) {
            stemVariable = new QDLStem();
        }
        if (isDefaultValueNode()) {
            stemVariable.setDefaultValue(newValue);
        }
        try {
            if (isDefaultValueNode()) {
                stemVariable.setDefaultValue(newValue);
            } else {

                stemVariable.set(indexList, newValue); // let the stem set its value internally
            }
        } catch (IndexError indexError) {
            indexError.setStatement(this);
            throw indexError;
        }
        setResult(stemVariable);
        setResultType(Constant.STEM_TYPE);
        setEvaluated(true);

    }

    @Override
    public int getNodeType() {
        return EXPRESSION_STEM2_NODE;
    }
}
