package org.qdl_lang.functions;

import org.qdl_lang.expressions.ExpressionImpl;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import org.qdl_lang.variables.values.QDLValue;

/**
 * To treat defined lambda expressions they must be {@link org.qdl_lang.expressions.ExpressionNode}s
 * This wraps a {@link FunctionDefinitionStatement}, which cannot be replaced. The strategy
 * is to swap out FDS for these at very specific places to allow for passing lambdas as arguments.
 * <p>Created by Jeff Gaynor<br>
 * on 6/3/21 at  8:45 AM
 */
public class LambdaDefinitionNode extends ExpressionImpl implements FunctionNodeInterface{

    public boolean hasName(){
        if(functionRecord == null){
            return false;
        }
        return functionRecord.hasName();
    }
    @Override
    public QDLValue evaluate(State state) {
        if(state.isImportMode()){
            state.getFTStack().localPut(functionRecord);
        } else{
           // state.getFTStack().put(functionRecord);
            state.putFunction(functionRecord);
        }
        return null; // for now
    }


    public LambdaDefinitionNode(FunctionDefinitionStatement fds) {
        functionRecord = fds.getFunctionRecord();
        setLambda(fds.isLambda());
        setSourceCode(fds.getSourceCode());
    }

    public LambdaDefinitionNode(int operatorType) {
        super(operatorType);
    }

    @Override
    public ExpressionInterface makeCopy() {
        throw new NotImplementedException();
    }
    public boolean isLambda() {
          return lambda;
      }

      public void setLambda(boolean lambda) {
          this.lambda = lambda;
      }

      boolean lambda = false;
      public FunctionRecord getFunctionRecord() {
          return functionRecord;
      }

      public void setFunctionRecord(FunctionRecord functionRecord) {
          this.functionRecord = functionRecord;
      }

      FunctionRecord functionRecord;

    @Override
        public int getNodeType() {
            return LAMBDA_DEFINITION_NODE;
        }
}
