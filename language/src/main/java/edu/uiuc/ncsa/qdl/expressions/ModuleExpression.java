package edu.uiuc.ncsa.qdl.expressions;

import edu.uiuc.ncsa.qdl.exceptions.IntrinsicViolation;
import edu.uiuc.ncsa.qdl.exceptions.QDLExceptionWithTrace;
import edu.uiuc.ncsa.qdl.exceptions.UnknownSymbolException;
import edu.uiuc.ncsa.qdl.functions.FKey;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.StateUtils;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;
import edu.uiuc.ncsa.qdl.variables.Constant;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;

import java.io.IOException;

/**
 * Models a single module expression of the form <b>A</b>#<i>expression</i> where <b>A</b>
 * is the alias giving the instance of the current module
 * and <i>expression</i> is a general expression to be evaluated against
 * the state of the module.
 * <p>Created by Jeff Gaynor<br>
 * on 9/23/21 at  6:10 AM
 */
public class ModuleExpression extends ExpressionImpl {
    public boolean isDefaultNamespace() {
        return defaultNamespace;
    }

    public void setDefaultNamespace(boolean defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    boolean defaultNamespace = false;

    public ModuleExpression() {
    }

    public ModuleExpression(int operatorType) {
        super(operatorType);
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    String alias;

    public boolean isNewModuleVersion() {
        return newModuleVersion;
    }

    public void setNewModuleVersion(boolean newModuleVersion) {
        this.newModuleVersion = newModuleVersion;
    }

    boolean newModuleVersion = false;

    @Override
    public Object evaluate(State ambientState) {
        // resolves https://github.com/ncsa/qdl/issues/24
        if (isDefaultNamespace()) {
            if (getExpression() instanceof ConstantNode) {
                ConstantNode cNode = (ConstantNode) getExpression();
                setResult(cNode.getResult());
                setResultType(cNode.getResultType());
                setEvaluated(true);
                return getResult();
            }
            if (getExpression() instanceof VariableNode) {
                VariableNode vvv = (VariableNode) getExpression();
                Object obj = ambientState.getRootState().getValue(vvv.getVariableReference());
                if (obj == null) {
                    throw new UnknownSymbolException("'" + vvv.getVariableReference() + "'   not found", vvv);
                } else {
                    setResult(obj);
                    setResultType(Constant.getType(obj));
                    setEvaluated(true);
                    return getResult();
                }
            }
            if (getExpression() instanceof Polyad) {
                // in this case, the user is explicitly telling us where to get the function from
                Polyad polyad = (Polyad) getExpression();
                if (null != ambientState.getRootState().getFTStack().get(new FKey(polyad.getName(), polyad.getArgCount()))) {
                    ambientState.getRootState().getMetaEvaluator().getFunctionEvaluator().evaluate(polyad, ambientState.getRootState());

                } else {
                    throw new QDLExceptionWithTrace("no such function " + polyad.getName() + "(" + polyad.getArgCount() + ")", polyad);
                }

            } else {
                // Since this should not happen if the parser is working right, it implies
                // that something in the parser changed and non-expressions are not
                // being sent along.
                throw new IllegalArgumentException("cannot evaluate expression '" + getExpression().getSourceCode() + "' in this module");
            }
            setResult(getExpression().getResult());
            setResultType(getExpression().getResultType());
            setEvaluated(true);
            return getResult();

        }
        if (ambientState.getMetaEvaluator().isSystemNS(getAlias())) {
            // In this case, it is a built in function and there are no constants
            // or variables defined in those modules.
            if (getExpression() instanceof ConstantNode) {
                ConstantNode cNode = (ConstantNode) getExpression();
                setResult(cNode.getResult());
                setResultType(cNode.getResultType());
                setEvaluated(true);
                return getResult();
            }
            if (getExpression() instanceof VariableNode) {
                VariableNode vvv = (VariableNode) getExpression();
                throw new UnknownSymbolException("'" + vvv.getVariableReference() + "'   not found", vvv);
            }
            if (getExpression() instanceof Polyad) {
                ambientState.getMetaEvaluator().evaluate(getAlias(), (Polyad) getExpression(), ambientState);
            } else {
                // Since this should not happen if the parser is working right, it implies
                // that something in the parser changed and non-expressions are not
                // being sent along.
                throw new IllegalArgumentException("cannot evaluate expression '" + getExpression().getSourceCode() + "' in this module");
            }
            setResult(getExpression().getResult());
            setResultType(getExpression().getResultType());
            setEvaluated(true);
            return getResult();
        }
        Object mm = ambientState.getValue(getAlias());
        if (mm != null && mm instanceof Module) {
            setModule((Module) mm);
            setNewModuleVersion(true);
            /* The ambient state refers to the state in which the root module was called.
             It is set with each module expression call, so if it is null, then this is the
             first of a chain. E.g. in x#y#z#w#f(s) x is the root and the state passed to that
             is the ambient state for everything else. Since in the new system, y is a variable
             in the state of x, x's module state has to be passed along in addition to the
             ambient state so that, eventually, f(s) can be evaluated (s is  in the ambient space
             or it would be NS qualified)
            */
            if (getAmbientState() == null) {
                setAmbientState(ambientState);
            }
        } else {
            XKey xKey = new XKey(getAlias());
            if (!(alias.equals("this") || ambientState.getMInstances().containsKey(xKey))) {
                throw new IllegalArgumentException("no module named '" + getAlias() + "' was  imported");
            }
            setModule(ambientState.getMInstances().getModule(xKey));
            setNewModuleVersion(false);
        }
        Object result = null;
        // no module state means to look at global state to find the module state.
        if (getExpression() instanceof ModuleExpression) {
            // Modules expression like a#b#c work within their scope, so b must be an imported module.
            ModuleExpression nextME = (ModuleExpression) getExpression();
            if (isNewModuleVersion()) {
                //result = nextME.evaluate(module.getState().newLocalState(ambientState));
                nextME.setAmbientState(getAmbientState());
                result = nextME.evaluate(getModuleState());
            } else {
                // old module system
                XKey xKey = new XKey(nextME.getAlias());
                if (getModuleState(ambientState).getMInstances().containsKey(xKey)) {
                    nextME.setModuleState(getModuleState(ambientState).getMInstances().getModule(xKey).getState());
                }
                //getExpression().setAlias(getAlias());
                getExpression().setAlias(nextME.getAlias());
                result = getExpression().evaluate(getModuleState(ambientState));
            }
        } else {

            if (isNewModuleVersion()) {

                // create a new state object for function resolution. This should have the
                // current variables in the ambient state, but the variables, imported modules and functions
                // in the module (or encapsulation breaks!!)
                State newState = null;
                try {
                    newState = StateUtils.clone(getModuleState());
                    newState.getVStack().appendTables(getAmbientState().getVStack());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Object r = null;
                if (getExpression() instanceof Polyad) {
                    ((Polyad) getExpression()).evaluatedArgs(newState);
                }
                if (getExpression() instanceof Polyad) {
                    // send along evaluated args with ambient state, but do not allow
                    // ambient state to override internal module state for functions, loaded modules etc.
                    r = getExpression().evaluate(getModuleState());
                } else {
                    r = getExpression().evaluate(newState); // gets local overrides from ambient state
                }

                if (r == null) {
                    throw new NFWException("unknown expression type");
                }
                setResult(r);
                setResultType(Constant.getType(r));
                setEvaluated(true);
                return r;
            }
/*
            if(obj instanceof FunctionReferenceNode){
                setResult(obj);
                setResultType(Constant.FUNCTION_TYPE);
                setEvaluated(true);
                return obj;
            }
*/
            // Simple expressions like a#b must work within the scope of a
            getExpression().setAlias(getAlias());
            State moduleState = getModuleState(ambientState); // clean state
            // https://github.com/ncsa/qdl/issues/34 pass along variables
            if (getExpression() instanceof Polyad) {
                Polyad f = (Polyad) getExpression();
                for (int i = 0; i < f.getArgCount(); i++) {
                    if (f.getArgAt(i) instanceof VariableNode) {
                        VariableNode vNode = (VariableNode) f.getArgAt(i);
                        Object v = f.evalArg(i, ambientState);
                        moduleState.setValue(vNode.getVariableReference(), v);
                    }
                }
            }
            result = getExpression().evaluate(moduleState);
        }
        setResult(result);
        setResultType(Constant.getType(result));
        setEvaluated(true);
        return result;
    }

    /* test apply operator on module.

       module['a:x'][module['a:y'][f(x)->x;];y:=import('a:y');]
       x:=import('a:x');
       x#y#f(3)
       ⍺x#y#@f


       module['a:x'][g(x,y)->x*y;]
  z:=import('a:x')
  ⍺z#@g
    [3,4]⍺z#@g

       module['a:x'][g(x,y)->x*y;]
       module['a:x'][module['a:y'][module['a:z'][module['a:w'][g(x,y)->x*y;foo:='bar';];w:=import('a:w');];z:=import('a:z');];y:=import('a:y');]
       x:=import('a:x');
       x#y#z#w#foo
       x#y#z#w#g(2,3)
     */
    public ExpressionInterface getExpression() {
        if (getArguments().isEmpty()) {
            throw new IllegalStateException("no expression set for module reference");
        }
        return getArguments().get(0);
    }

    public void setExpression(ExpressionInterface expressionInterface) {
        if (getArguments().isEmpty()) {
            getArguments().add(expressionInterface);
        } else {
            getArguments().set(0, expressionInterface);
        }
    }

    @Override
    public ExpressionInterface makeCopy() {
        ModuleExpression moduleExpression = new ModuleExpression();
        moduleExpression.setAlias(getAlias());
        moduleExpression.setExpression(getExpression().makeCopy());
        moduleExpression.setModuleState(getModuleState(null)); // can't be cloned
        return moduleExpression;
    }

    /**
     * The module associated with this expression.
     *
     * @return
     */
    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    Module module = null;

    public State getModuleState() {
        return getModule().getState();
    }

    /**
     * The state of the current module only. This is used to construct the local state.
     *
     * @return
     */
    public State getModuleState(State state) {
        if (state == null) {
            return null;
        }
        if (moduleState == null) {
            XKey xKey = new XKey(getAlias());
            if (!(alias.equals("this") || state.getMInstances().containsKey(xKey))) {
                throw new IllegalArgumentException("no module named '" + getAlias() + "' was  imported");
            }
            moduleState = state.newLocalState(state.getMInstances().getModule(xKey).getState());
        }
        return moduleState;
    }

    public void setModuleState(State moduleState) {
        this.moduleState = moduleState;
    }

    public State getLocalState(State state) {
        return state.newLocalState(getModuleState(state));
        //   return getModuleState(state);
    }

    State moduleState;

    /**
     * Set the value of this expression using the state
     *
     * @param newValue
     */
    public void set(State state, Object newValue) {
        if (getExpression() instanceof VariableNode) {
            Object mm = state.getValue(getAlias());
            if (mm != null && mm instanceof Module) {
                setModule((Module) mm);
                setNewModuleVersion(true);
                VariableNode vNode = (VariableNode) getExpression();
                String variableName = vNode.getVariableReference();
                ((Module) mm).getState().setValue(variableName, newValue);
                return;
            }
            VariableNode vNode = (VariableNode) getExpression();
            String variableName = vNode.getVariableReference();
            if (state.isIntrinsic(variableName) && !getModuleState(state).isDefined(variableName)) {
                // check that if this is private
                throw new IntrinsicViolation("cannot set an intrinsic variable", getExpression());
            }
            if (getModuleState(state).isDefined(variableName)) {
                getModuleState(state).setValue(variableName, newValue);
            } else {
                throw new IllegalArgumentException("Cannot define new variables in a module.");
            }
            return;
        }
        if (getExpression() instanceof ConstantNode) {
            throw new IllegalArgumentException("cannot assign a constant a value.");
        }
        if (getExpression() instanceof ModuleExpression) {
            ModuleExpression nextME = (ModuleExpression) getExpression();
            XKey xKey = new XKey(nextME.getAlias());
            if (getModuleState(state).getMInstances().containsKey(xKey)) {
                nextME.setModuleState(getModuleState(state).getMInstances().getModule(xKey).getState());
            }
            ((ModuleExpression) getExpression()).set(state, newValue);
            return;
        }
        throw new IllegalArgumentException("unknown left assignment argument.");
    }

    @Override
    public int getNodeType() {
        return MODULE_NODE;
    }

    public State getAmbientState() {
        return ambientState;
    }

    public void setAmbientState(State ambientState) {
        this.ambientState = ambientState;
    }

    State ambientState;
}
