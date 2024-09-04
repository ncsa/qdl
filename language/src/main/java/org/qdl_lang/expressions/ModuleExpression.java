package org.qdl_lang.expressions;

import org.qdl_lang.exceptions.IntrinsicViolation;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.exceptions.UnknownSymbolException;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;
import org.qdl_lang.state.StateUtils;
import org.qdl_lang.state.XKey;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.VThing;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;

import java.util.ArrayList;
import java.util.List;

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
                newState = StateUtils.clone(getModuleState());
                // Partial fix for https://github.com/ncsa/qdl/issues/45

                // (OLD!)Fuller discussion:
                // There is good argument that the next should add in all of the ambient state.
                // But probably not here. The local part definitely should be on the top of the stack,
                // Now we have
                //
                //  calling function local state :: current module ambient state :: module state
                //
                // but should this really be
                //
                //  calling function local state :: current module ambient state :: rest of calling function state :: module state
                //
                // Or something else?? This was discovered by the call

                // λat(requested)->test_util#at_lifetime(server_defaults., client., requested);
                //
                // where the LHS argument of requested was not getting set. But it would be possible to have something like
                // a(x) -> Q#f(x,3) -> W#g(1,x,4)
                // I.e. to have the RHS be a lambda function that does this and is called by another lambda function.
                //
                // NEW: Add full stacks or we cannot work with functions and variables defined within a module.
                // constructs like mutators then don't work. E.g. inside a module
                //  f(x)->x^2;
                //  g(x)->f(x)-x; // fails unless f is included in its state.

                //newState.getVStack().append(ambientState.getVStack().getLocal()); // add in any passed in state (e.g. function arguments to module functions)

                newState.getVStack().appendTables(getAmbientState().getVStack());  // add in the state of the module
                newState.getFTStack().appendTables(ambientState.getFTStack()); // add in any passed in state for functions
                newState.getVStack().appendTables(ambientState.getVStack()); // add in any passed in state for variables
                newState.setModuleState(true);
                if (mm != null) {
                    if (mm instanceof JavaModule) {
                        // the point is that these are added to the module state because the functions for the module
                        // live in the state for that module.
                        State state = ((JavaModule) mm).getState();
                        if (state != null) {
                            newState.getVStack().appendTables(state.getVStack());
                            newState.getFTStack().appendTables(state.getFTStack());
                        }
                    }
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
                    if (getExpression() instanceof DyadicFunctionReferenceNode) {
                        ((DyadicFunctionReferenceNode) getExpression()).setModule(getModule());
                    }

                }

                if (r == null) {
                    throw new IllegalArgumentException("no such value or unknown expression type");
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
            // short-circuit the function evaluation here so we don't have to
            // muck around with the implied vs. actual state in complex expressions.
            if (getExpression() instanceof Polyad) {
                Polyad f = (Polyad) getExpression();
                List evaluatedArgs = new ArrayList();
                for (int i = 0; i < f.getArgCount(); i++) {
                    evaluatedArgs.add(f.evalArg(i, ambientState));
                }
                f.setEvaluatedArgs(evaluatedArgs);
            }
            result = getExpression().evaluate(moduleState);

        }
        setResult(result);
        setResultType(Constant.getType(result));
        setEvaluated(true);
        return result;
    }

    /*
          q := module_load('org.qdl_lang.extensions.http.QDLHTTPLoader','java');
q := module_import(q);
suite := size(args()) == 1 ? args(0):'cm_local';//which test to run. Default is cm for local test
ini. := file_read('/home/ncsa/dev/csd/config/ini/cm-test.ini',2).suite;
http#host(ini.'address') ;
qqq(x)->x
http#host(qqq('https://foo'))
     */
    /* test apply operator on module.

       module['a:x'][module['a:y'][f(x)->x;];y:=import('a:y');]
       x:=import('a:x');
       x#y#f(3)
       ∂x#y#@f


       module['a:x'][g(x,y)->x*y;]
  z:=import('a:x')
  ∂z#@g
    [3,4]∂z#@g

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
        if (module == null) {
            return null;
        }
        State s = getModule().getState();
        if(s.getMetaEvaluator() == null){
            // Fix for https://github.com/ncsa/qdl/issues/75
            // edge case -- java serialization won't be able to track down huge networks of modules
            // in modules and the the meta evaluator is always a transient field. It is therefore
            // possible that the entire module state comes back nicely except for this which causes
            // NPEs.
            // Why not "fix" this at deserialization? Because modules are now variables and that
            // would involve crawling through every variable stack and in complex cases would
            // be an intolerable burden. Better to fix it as it is found.
            s.setMetaEvaluator(getAmbientState().getMetaEvaluator());
            s.setOpEvaluator(getAmbientState().getOpEvaluator());
        }
        return s;
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
            if (state.getVStack().containsKey(xKey)) {
                VThing vThing = (VThing) state.getVStack().get(xKey);
                if (vThing.getValue() instanceof Module) {
                    Module m = (Module) vThing.getValue();
                    setModule(m);
                    moduleState = m.getState();

                } else {
                    throw new NFWException("expected module for key " + xKey + ", but got a " + vThing.getValue().getClass().getSimpleName());
                }
            } else {
                if (!(alias.equals("this") || state.getMInstances().containsKey(xKey))) {
                    throw new IllegalArgumentException("no module named '" + getAlias() + "' was  imported");
                }

                moduleState = state.newLocalState(state.getMInstances().getModule(xKey).getState());
            }
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
            getModuleState(state); // sets the state for *this* module
            ModuleExpression nextME = (ModuleExpression) getExpression();
            XKey xKey = new XKey(nextME.getAlias());
            setNewModuleVersion(getModuleState() != null);
            if (isNewModuleVersion() && getModuleState().getVStack().containsKey(xKey)) {
                VThing vThing = (VThing) getModuleState().getVStack().get(xKey);
                if (vThing.getValue() instanceof Module) {
                    Module m = (Module) vThing.getValue();
                    nextME.setModule(m);
                    nextME.setModuleState(m.getState()); //sets the next one in the chain.
                } else {
                    throw new NFWException("expected module for " + xKey + " not found");
                }
            } else {
                if (getModuleState(state).getMInstances().containsKey(xKey)) {
                    nextME.setModuleState(getModuleState(state).getMInstances().getModule(xKey).getState());
                }

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
