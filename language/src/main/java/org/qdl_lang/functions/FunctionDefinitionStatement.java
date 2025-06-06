package org.qdl_lang.functions;

import org.qdl_lang.state.State;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.TokenPosition;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.values.QDLNullValue;
import org.qdl_lang.variables.values.QDLValue;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/21 at  8:46 AM
 */
public class FunctionDefinitionStatement implements Statement {
    TokenPosition tokenPosition = null;
    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {this.tokenPosition=tokenPosition;}

    @Override
    public TokenPosition getTokenPosition() {return tokenPosition;}

    @Override
    public boolean hasTokenPosition() {return tokenPosition!=null;}
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
    public QDLValue evaluate(State state) {
        // Explicitly intercept an intrinsic function in a module so it does not end
        // up in the local state. Bad form to define extrinsics in a module, but
        // this is the right behavior if a user does.
        if(functionRecord.isExtrinsic()){
            state.getExtrinsicFuncs().put(functionRecord);
            return QDLNullValue.getNullValue(); // for now
        }
        if(state.isImportMode()){
            state.getFTStack().localPut(functionRecord);
        } else {
            state.putFunction(functionRecord);
        }
        return QDLValue.getNullValue(); // for now
    }

    List<String> source;

    @Override
    public List<String> getSourceCode() {
        return source;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.source = sourceCode;
    }
}
