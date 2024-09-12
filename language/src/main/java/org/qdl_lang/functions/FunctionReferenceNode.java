package org.qdl_lang.functions;

import org.qdl_lang.expressions.ExpressionImpl;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a pointer to the collection of functions for a given name. Just has the name.
 * <p>Created by Jeff Gaynor<br>
 * on 3/14/21 at  3:26 PM
 */
public class
FunctionReferenceNode extends ExpressionImpl implements FunctionReferenceNodeInterface {

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


    public List<FunctionRecordInterface> getFunctionRecords() {
        return functionRecords;
    }

    public void setFunctionRecords(List<FunctionRecordInterface> functionRecords) {
        this.functionRecords = functionRecords;
    }

    List<FunctionRecordInterface> functionRecords= null;

    public FunctionRecordInterface getFunctionRecord(int argCount){
        if(functionRecords == null || functionRecords.isEmpty()) return null;
        for(FunctionRecordInterface fri : functionRecords){
            if(fri.getArgCount() == argCount) return fri;
        }
        return null;
    }

    @Override
    public boolean hasFunctionRecord(int argCount) {
        return getFunctionRecord(argCount) != null;
    }

    @Override
    public Object evaluate(State state) {
        setFunctionRecords(state.getFTStack().getByAllName(getFunctionName()));
        // if this was e.g. in a module, it might have an arbitraily complex path to get here.
        // set the state that was finally constructed elsewhere for this specific call.
        if(state.isModuleState()) {
            setModuleState(state);
        }
        setResult(this);
        setResultType(Constant.getType(this));
        setEvaluated(true);
        return this;
    }

    public State getModuleState() {
        return moduleState;
    }

    public void setModuleState(State moduleState) {
        this.moduleState = moduleState;
    }

    State moduleState = null;

    public boolean hasModuleState(){
        return moduleState != null;
    }

    public FunctionRecordInterface getByArgCount(int argCount){
        if(getFunctionRecords() == null){
            return null;
        }
        for(FunctionRecordInterface functionRecord
                : getFunctionRecords()){
            if(functionRecord.getArgCount() == argCount){
                return functionRecord;
            }
        }
        return null;
    }
    @Override
        public int getNodeType() {
            return FUNCTION_REFERENCE_NODE;
        }

    @Override
    public ExpressionInterface makeCopy() {
        FunctionReferenceNode functionReferenceNode = new FunctionReferenceNode();
        functionReferenceNode.setFunctionName(getFunctionName());
        functionReferenceNode.setAnonymous(isAnonymous());
        functionReferenceNode.setOperatorType(getOperatorType());
        return functionReferenceNode;
    }

    @Override
    public String toString() {
        if(!isEvaluated()){
            return "FunctionReferenceNode{" +
                    "functionName='" + functionName + '\'' +
                    ", anonymous=" + anonymous +
                    ", evaluated=" + isEvaluated() +
                    '}';
        }
        List<Integer> argCounts = new ArrayList();
        for(FunctionRecordInterface functionRecord:getFunctionRecords()){
            argCounts.add(functionRecord.getArgCount());
        }
           return "@" + (isAnonymous()?"anon":getFunctionName()) + "(" + argCounts + ")";
    }
}
