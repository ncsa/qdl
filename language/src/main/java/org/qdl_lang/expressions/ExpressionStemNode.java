package org.qdl_lang.expressions;

import org.qdl_lang.state.State;
import org.qdl_lang.state.StemMultiIndex;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.QDLVariable;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static org.qdl_lang.variables.Constant.*;
import static org.qdl_lang.variables.QDLStem.STEM_INDEX_MARKER;
import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * Models a stem.
 * <p>Created by Jeff Gaynor<br>
 * on 3/5/21 at  5:58 AM
 */
public class ExpressionStemNode implements ExpressionInterface {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}

    @Override
    public boolean hasAlias() {
        return alias!=null;
    }
     String alias = null;

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
    this.alias = alias;
    }


    public ArrayList<ExpressionInterface> getArguments() {
        return arguments;
    }

    public void setArguments(ArrayList<ExpressionInterface> arguments) {
        this.arguments = arguments;
    }

    ArrayList<ExpressionInterface> arguments = new ArrayList<>();

    QDLValue  result = null;

    @Override
    public QDLValue getResult() {
        return result;
    }

    @Override
    public void setResult(QDLValue object) {
        this.result = object;
    }

    @Override
    public void setResult(Object result) {
        setResult(QDLValue.asQDLValue(result));
    }

    @Override
    public int getResultType() {
        return getResult().getType();
    }


    @Override
    public boolean isEvaluated() {
        return evaluated;
    }

    boolean evaluated = false;

    @Override
    public void setEvaluated(boolean evaluated) {
        this.evaluated = evaluated;
    }

    public ExpressionInterface getLeftArg() {
        if(getArguments().isEmpty()){
            return null;
        }
        return getArguments().get(0);
    }

    public void setLeftArg(ExpressionInterface swri){
            if(getArguments().size() == 0){
                getArguments().add(swri);
            }else{
                getArguments().set(0,swri);
            }
    }

    public void setRightArg(ExpressionInterface swri){
            if(getArguments().size() == 1){
                getArguments().add(swri);
            }else{
                getArguments().set(1,swri);
            }
    }

    public ExpressionInterface getRightArg() {
        if(getArguments().size() < 2){
            return null;
        }
        return getArguments().get(1);
    }

    /*
       Extended notes, just in case I have to revisit this later and figure out what it does.

       Test presets
       j:=2;k:=1;p:=4;q:=5;r:=6;a. := [n(4),n(5),n(6)];
       b. := [[n(4),n(5)],[n(6),n(7)]]
       Not working
       n(3).0 -- gives parser error for .0 since it thinks it is a decimal.
       b.i(0).i(1).i(2) --  syntax error at char 3, 'missing ; at ("

        b. := [[-n(4)^2,2+n(5)],[10 - 3*n(6),4+5*n(7)]];
        (b.).i(0).i(1).(2) == b.0.1.2

      ** TO DO -- Find stem ex in documentation and create one with functions. w.x.y.z

The following are working:
         (n(4)^2-5).i(3)
         [n(5),-n(6)].i(1).i(3)
         [n(5),n(4)].k
         [2+3*n(5),10 - n(4)].(k.0)
         {'a':'b','c':'d'}.i('a')
         {'a':'b','c':'d'}.'a'
         n(3).n(4).j
         n(3).n(4).n(5).n(6).n(7).n(8).j;
         n(3).n(4).n(5).n(6).i(2);
         n(3).n(4).n(5).(a.k).n(6).n(7).j
        (4*n(5)-21).(n(3).n(4).i(2))
         3 rank exx.
         [[-n(4),3*n(5)],[11+n(6), 4-n(5)^2]].i(0).i(1).i(2)
         (b.).i(0).i(1).i(2)

        Embedded stem basic. This get a.1.2 in a very roundabout way
        k:=1;j:=2;a.:=[-n(4),3*n(5),11+n(6)];
        x := n(12).n(11).n(10).(a.k).n(6).n(7).j;
        x==6;
     */
    /*
    E.g. in
    a. := [-n(4),3*n(5),11+n(6)];k:=1;j:=2;
    x := n(12).n(11).n(10).(a.k).n(6).n(7).j;
   the parse tree is (n(n) written in)

                                x
                              /  \
                            x     j = 2
                          /  \
                        x     i7
                      /  \
                    x    i6
                  /  \
               x     a.k  = [0,3,6,9,12]
             /  \
           x     i10
         /  \
        i12  i11

        This is a very elaborate way to get a.1.2 == 6, requiring pretty much all of the machinery.
    */


    /**
     * Since this is a dyadic operation in the parser, the result will be a tree of objects of the form
     * <pre>
     *              x
     *            /  \
     *        ...     scalar
     *      x
     *    / \
     *   A   B
     *  </pre>
     *
     * @param state
     * @return
     */
    @Override
    public QDLValue evaluate(State state) {
        return getOrSetValue(state, false, null);
    }

    public QDLValue setValue(State state, QDLValue newValue) {
        return getOrSetValue(state, true, newValue);
    }

    protected QDLValue getOrSetValue(State state, boolean setValue, QDLValue newValue) {
        getLeftArg().evaluate(state);
        getRightArg().evaluate(state);

        if (getRightArg().getResultType() == STEM_TYPE) {
            // See note above in comment block. Since we get the tree in the wrong order
            // (which is fine, since we require it evaluate from right to left and the parser
            // doesn't do that) we skip over everything until we finally have a scalar as
            // the right hand argument for the stem. This means the stem can start resolving.
            return null;
        }

        // Simplest case.
        if (getLeftArg().getResultType() == STEM_TYPE || (getLeftArg() instanceof VariableNode)) {
            QDLStem s = null;
            if(getLeftArg() instanceof VariableNode){
                VariableNode vNode = (VariableNode)getLeftArg();
                if(!state.isStem(vNode.getVariableReference())){
                    throw new IllegalArgumentException("Cannot assign stem value to non-stem variable \"" + vNode.getVariableReference() + "\"");
                }
                Object obj = state.getValue(vNode.getVariableReference());
                if(obj == null && setValue){
                    s = new QDLStem();
                    // add it to the symbol table
                    state.setValue(vNode.getVariableReference(), asQDLValue(s));
                }else{
                    s = (QDLStem)  obj;
                }
            }else{
                 s = getLeftArg().getResult().asStem();
            }
            if (setValue) {
                if (getRightArg().getResult().isLong()) {
                    s.put(from(getRightArg().getResult()), newValue);
                } else {
                    String targetKey = null;
                    if (getRightArg().getResult() == null) {
                        if (getRightArg() instanceof VariableNode) {
                            VariableNode v = (VariableNode) getRightArg();
                            if (v.getResult() == null) {
                                targetKey = v.getVariableReference();
                            } else {
                                targetKey = v.getResult().toString();
                            }
                        } else {
                            throw new IllegalArgumentException("could not determine key for stem");
                        }

                    } else {
                        targetKey = getRightArg().getResult().toString();
                    }
                    s.put(targetKey, newValue);
                }
                result = newValue;
            } else {
                result = new QDLValue(doLeftSVCase(getLeftArg(), getRightArg(), state));
            }
            setEvaluated(true);
            return result;
        }
        // other case is that the left hand argumement is this class.

        if (!(getLeftArg() instanceof ExpressionStemNode)) {

            // This means the user passed in something as the left most argument that
            // cannot be a stem, e.g. (1).(2).
            throw new IllegalStateException("left hand argument not a valid stem.");
        }
        ExpressionInterface swri = getLeftArg();

        ExpressionInterface lastSWRI = getRightArg();
        Object r = null;
        ExpressionStemNode esn = null;
        ArrayList<ExpressionInterface> indices = new ArrayList<>();

        while (swri instanceof ExpressionStemNode) {
            esn = (ExpressionStemNode) swri;
            indices.add(lastSWRI);

            if (esn.getRightArg().getResultType() == STEM_TYPE) {
                r = doLeftSVCase(esn.getRightArg(), indices, state);
                indices = new ArrayList<>();
            } else {
                r = esn.getRightArg().getResult();
            }
            esn.setResult(r);
            esn.setEvaluated(true);
            lastSWRI = esn;
            swri = esn.getLeftArg();
        }

        QDLStem stemVariable = esn.getLeftArg().getResult().asStem();
        Object r1 = null;
        if (setValue) {
            r1 = stemVariable.put(r.toString(), newValue);
        } else {

            r1 = stemVariable.get(r);
        }


        setResult(r1);
        setEvaluated(true);
        return getResult();

    }

    // a. := {'p':'x', 'q':'y', 'r':5, 's':[2,4,6], 't':{'m':true,'n':345.345}}
    //  (a.).query(a., '$..m',true).(0)
    protected Object doLeftSVCase(ExpressionInterface leftArg, ExpressionInterface rightArg, State state) {
        List<ExpressionInterface> x = new ArrayList<>();
        x.add(rightArg);
        return doLeftSVCase(leftArg, x, state);
    }

    /**
     * Case that the left hand argument is a stem variable. This does the lookup.
     * In the variable case, a {@link StemMultiIndex} is created and interacts with
     * the {@link State} to do the resolutions.
     *
     * @param leftArg
     * @param state
     * @return
     */
    protected Object doLeftSVCase(ExpressionInterface leftArg, List<ExpressionInterface> indices, State state) {
        QDLStem stemLeft = leftArg.getResult().asStem();
        if(stemLeft == null){
            if(indices.get(0) instanceof VariableNode) {
                return ((VariableNode)indices.get(0)).getVariableReference();
            }
        }
        String rawMI = "_"; // dummy name for stem
        for (ExpressionInterface rightArg : indices) {

            boolean gotOne = false;
            if (rightArg instanceof VariableNode) {
                VariableNode vn = (VariableNode) rightArg;
                vn.evaluate(state);
                if (vn.getResult() == null) {
                    // no such variable, so use name
                    StringTokenizer st = new StringTokenizer(vn.getVariableReference(), STEM_INDEX_MARKER);
                    while (st.hasMoreTokens()) {
                        String name = st.nextToken();
                        Object v = state.getValue(name);
                        if (v == null) {
                            rawMI = rawMI + STEM_INDEX_MARKER + name;
                        } else {
                            rawMI = rawMI + STEM_INDEX_MARKER + v;
                        }
                    }
                    gotOne = true;
                } else{
                    return stemLeft.get(vn.getResult());
                }
            }
            if (!gotOne && (rightArg instanceof ConstantNode)) {
                switch (rightArg.getResultType()) {
                    case LONG_TYPE:
                        return stemLeft.get(rightArg.getResult().asLong());
                    case STRING_TYPE:
                        return stemLeft.get(rightArg.getResult().toString());
                    default:
                        new IllegalArgumentException("unknown stem index type ");
                }
                return stemLeft.get(rightArg.getResult());

            }
            if (!gotOne && (rightArg.getResultType() == STRING_TYPE || rightArg.getResultType() == LONG_TYPE)) {
                rawMI = rawMI + STEM_INDEX_MARKER + rightArg.getResult().toString();
            } else {
                // do what?
                if (rightArg.getResultType() == STEM_TYPE) {
                    result = asQDLValue(getLeftArg());
                    return result;
                }
            }


        }

        StemMultiIndex multiIndex = new StemMultiIndex(rawMI); // dummy variable

        return stemLeft.get(multiIndex);
    }

    List<String> sourceCode = new ArrayList<>();

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public ExpressionInterface makeCopy() {
        return null;
    }
    @Override
        public int getNodeType() {
            return EXPRESSION_STEM_NODE;
        }
}
