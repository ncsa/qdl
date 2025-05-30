package org.qdl_lang.functions;

import org.qdl_lang.exceptions.UndefinedFunctionException;
import org.qdl_lang.expressions.ExpressionImpl;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.values.QDLValue;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/10/24 at  12:43 PM
 */
public class DyadicFunctionReferenceNode extends ExpressionImpl implements FunctionReferenceNodeInterface, Comparable{
    @Override
    public int compareTo(Object object) {
        if(object instanceof DyadicFunctionReferenceNode){
            DyadicFunctionReferenceNode df = (DyadicFunctionReferenceNode)object;
            String x = toString();
            String y = df.toString();
            return x.compareTo(y);
        }
        throw new ClassCastException("the object '" + object.getClass().getSimpleName() + "' is not comparable.");
    }

    @Override
    public ExpressionInterface makeCopy() {
        return null;
    }

    /**
     * This is the left argument. The contract is that the left argument evaluates
     * into an integer (long, really, but java does not accept longs in many places)
     * so this is distinct from {@link #getArgCount()} which is always 2 since this is
     * a dyad.
     * @return
     */
    public int getFunctionArgCount(){
        return getArgAt(0).getResult().asLong().intValue();
    }
    @Override
    public int getNodeType() {
        return DYADIC_FUNCTION_REFERENCE_NODE;
    }

    // Note get(0) is the left argument, the name is set at parsing, use the name!
    // The getArg(1) is used to generate the position of the error, but should never be used
    // directly for anything else!
    //
    @Override
    public QDLValue evaluate(State state) {
        getArguments().get(0).evaluate(state);
        QDLValue lArg = getArgAt(0).getResult();
        getArguments().get(1).setEvaluated(true); // so things don't bomb elsewhere
        if ((lArg.isLong())) {
            int argCount = lArg.asLong().intValue();
            setFunctionRecord(getFRByArgCount(state,argCount,getFunctionName()));
        }
        Map<Integer, FunctionRecord> foundFRs = new HashMap<>();

        // if this was e.g. in a module, it might have an arbitraily complex path to get here.
        // set the state that was finally constructed elsewhere for this specific call.
        if (state.isModuleState()) {
            setModuleState(state);
        }
        setResult(new QDLValue(this));
        setEvaluated(true);
        return getResult();
    }
protected FunctionRecord getFRByArgCount(State state, int argCount, String functionName){
    FunctionRecord functionRecord = (FunctionRecord) state.getFTStack().get(new FKey(getFunctionName(), argCount));
    if(functionRecord == null){
        if(!state.getFTStack().getByAllName(getFunctionName()).isEmpty()){
            throw new UndefinedFunctionException("unknown valence of " + getFunctionArgCount() + " for " + getFunctionName(), getArgAt(0));
        }
        throw new UndefinedFunctionException("no such function " + getFunctionName(), getArgAt(1));
    }
    return functionRecord;
}
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    boolean anonymous = false;

    String functionName;

    public FunctionRecordInterface getFunctionRecord() {
        return functionRecord;
    }

    public void setFunctionRecord(FunctionRecordInterface functionRecord) {
        this.functionRecord = functionRecord;
    }

    @Override
    public FunctionRecordInterface getFunctionRecord(int argCount) {
        if(functionRecord == null ) return null;
         if(functionRecord.getArgCount() == argCount) return functionRecord;
        return null;
    }

    @Override
    public boolean hasFunctionRecord(int argCount) {
        if(functionRecord == null) return false;
        return functionRecord.getArgCount() == argCount;
    }

    FunctionRecordInterface functionRecord = null;

    public State getModuleState() {
        return moduleState;
    }

    public void setModuleState(State moduleState) {
        this.moduleState = moduleState;
    }

    State moduleState = null;

    public boolean hasModuleState() {
        return moduleState != null;
    }

    @Override
    public String toString() {
        if (!isEvaluated()) {
            return "unevaluated reference for " + getFunctionName();
        }
        return getArguments().get(0).getResult() + "@" + getFunctionName();
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
        if(module !=null) {
            moduleState = module.getState();
        }
    }

    Module module;
}
    /*
        f(x)->x^2
  f(x,y)->x*y
  [3,4]∂@f; // does dyadic automatically
  [3,4]∂2@f;
  [3,4]∂1@f; // should return applied to each element

     */