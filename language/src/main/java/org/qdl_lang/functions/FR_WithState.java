package org.qdl_lang.functions;

import org.qdl_lang.extensions.QDLFunctionRecord;
import org.qdl_lang.state.AbstractState;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.TokenPosition;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;

import java.util.List;

/**
 * A facade for a function record. This however has the local state. It is used as a function
 * reference and passed as e.g. an argument. So in f(@foo) @foo would be one of these.
 * <p>Created by Jeff Gaynor<br>
 * on 1/26/20 at  7:30 AM
 */
public class FR_WithState implements FunctionRecordInterface {
    @Override
    public String getName() {
        return functionRecord.getName();
    }

    @Override
    public FKey getKey() {
        return functionRecord.getKey();
    }


    public FR_WithState() {
    }

    public FR_WithState(FunctionRecordInterface functionRecord, AbstractState state, boolean isModule) {
        this(functionRecord, state);
        this.isModule = isModule;
    }

    public FR_WithState(FunctionRecordInterface functionRecord, AbstractState state) {
        this.functionRecord = functionRecord;
        this.state = state;
        isExternalModule = isJavaFunction();
    }

    public boolean hasState() {
        return state != null;
    }

    public FunctionRecordInterface functionRecord = null;
    public AbstractState state;
    public boolean isExternalModule = false;
    public boolean isModule = false;

    public boolean isJavaFunction() {
        return functionRecord instanceof QDLFunctionRecord;
    }

    @Override
    public String toString() {
        return "FR_WithState{" +
                "functionRecord=" + functionRecord +
                ", state=" + state +
                ", isExternalModule=" + isExternalModule +
                ", isModule=" + isModule +
                '}';
    }

    // In order to get the inheritance right, this class had to be turned into a delegate
    // for FunctionRecord via an interface.
    @Override
    public boolean isAnonymous() {
        return functionRecord.isAnonymous();
    }

    @Override
    public boolean isLambda() {
        return functionRecord.isLambda();
    }

    @Override
    public boolean isExtrinsic() {
        return functionRecord.isExtrinsic();
    }

    @Override
    public void setName(String name) {
        functionRecord.setName(name);
    }

    @Override
    public TokenPosition getTokenPosition() {
        return functionRecord.getTokenPosition();
    }

    @Override
    public boolean hasTokenPosition() {
        return functionRecord.hasTokenPosition();
    }

    @Override
    public boolean hasName() {
        return functionRecord.hasName();
    }

    @Override
    public int getArgCount() {
        return functionRecord.getArgCount();
    }

    @Override
    public FunctionRecordInterface clone() {
        throw new NotImplementedException();
    }

    @Override
    public List<String> getArgNames() {
        return functionRecord.getArgNames();
    }

    @Override
    public List<String> getSourceCode() {
        return functionRecord.getSourceCode();
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        functionRecord.setSourceCode(sourceCode);
    }

    @Override
    public List<String> getDocumentation() {
        return functionRecord.getDocumentation();
    }

    @Override
    public String getfRefName() {
        return functionRecord.getfRefName();
    }

    @Override
    public void setfRefName(String fRefName) {
        functionRecord.setfRefName(fRefName);
    }

    @Override
    public boolean isOperator() {
        return functionRecord.isOperator();
    }

    @Override
    public void setOperator(boolean operator) {
        functionRecord.setOperator(operator);
    }

    @Override
    public boolean isFuncRef() {
        return functionRecord.isFuncRef();
    }

    @Override
    public void setFuncRef(boolean funcRef) {
        functionRecord.setFuncRef(funcRef);
    }

    @Override
    public List<Statement> getStatements() {
        return functionRecord.getStatements();
    }

    @Override
    public void setStatements(List<Statement> statements) {
        functionRecord.setStatements(statements);
    }
}
