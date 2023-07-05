package edu.uiuc.ncsa.qdl.expressions;

import edu.uiuc.ncsa.qdl.functions.FKey;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;
import edu.uiuc.ncsa.qdl.statements.TokenPosition;

/**
 * For multiple arguments. This is used, e.g., for all functions.
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  6:12 AM
 */
public class Polyad extends ExpressionImpl {
    public Polyad() {
    }

    public Polyad(TokenPosition tokenPosition) {
        super(tokenPosition);
    }

    /**
     * Human readable name for this.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public Polyad(String name) {
        this.name = name;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    boolean builtIn = true;

    public Polyad(int operatorType) {
        super(operatorType);
    }

    @Override
    public Object evaluate(State state) {
        /*
         Some finagling. If this is being evaluated in a module, check that
         there is an override in place. If not, kick it up to the main system
         (so evaluate with no alias). This allows for local resolution in a module
         for a function of unqualified names
         E.g.
         foo(x)->...; // defined outside of module
        module['a:a','a']
        body [foo(x)->....; // locally defined
              fnord(x)->foo(x)+...; // resolves foo to the one in the module
             ...];
        Otherwise there is no way to reference any local functions in the module.
        You should be able to define things in a module and work locally without having
        to be aware of any other state.
        Partial answer to https://github.com/ncsa/qdl/issues/23 -- no need for this# now.
         */
        if (state.isModuleState()) {
            if (state.getFTStack().containsKey(new FKey(getName(), getArgCount()))) {
                if (hasAlias()) {
                    state.getMetaEvaluator().evaluate(getAlias(), this, state);
                } else {
                    state.getMetaEvaluator().getFunctionEvaluator().evaluate(this, state);
                }
                return getResult();
            }
        }
        state.getMetaEvaluator().evaluate(this, state);


        return getResult();
    }

    public void addArgument(ExpressionInterface expr) {
        getArguments().add(expr);
    }

    @Override
    public String toString() {
        return "Polyad[" +
                "name='" + name + '\'' +
                ", operatorType=" + operatorType +
                ']';
    }


    @Override
    public ExpressionNode makeCopy() {
        Polyad polyad = new Polyad(operatorType);
        polyad.setName(getName());
        for (ExpressionInterface arg : getArguments()) {
            polyad.addArgument(arg.makeCopy());
        }
        return polyad;
    }
    @Override
        public int getNodeType() {
            return POLYAD_NODE;
        }    
}
