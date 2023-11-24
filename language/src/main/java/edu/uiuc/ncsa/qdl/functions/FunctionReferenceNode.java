package edu.uiuc.ncsa.qdl.functions;

import edu.uiuc.ncsa.qdl.expressions.ExpressionImpl;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;
import edu.uiuc.ncsa.qdl.variables.Constant;

import java.util.List;

/**
 * Just a pointer to the function. Just has the name.
 * <p>Created by Jeff Gaynor<br>
 * on 3/14/21 at  3:26 PM
 */
public class FunctionReferenceNode extends ExpressionImpl {

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


    public List<FunctionRecord> getFunctionRecords() {
        return functionRecords;
    }

    public void setFunctionRecords(List<FunctionRecord> functionRecords) {
        this.functionRecords = functionRecords;
    }

    List<FunctionRecord> functionRecords= null;
    @Override
    public Object evaluate(State state) {
        setFunctionRecords(state.getFTStack().getByAllName(getFunctionName()));
        setResult(this);
        setResultType(Constant.getType(this));
        setEvaluated(true);
        return this;
    }

    public FunctionRecord getByArgCount(int argCount){
        if(getFunctionRecords() == null){
            return null;
        }
        for(FunctionRecord functionRecord : getFunctionRecords()){
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
        return "FunctionReferenceNode{" +
                "functionName='" + functionName + '\'' +
                ", anonymous=" + anonymous +
                '}';
    }
}
