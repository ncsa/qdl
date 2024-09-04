package org.qdl_lang.functions;

import org.qdl_lang.state.State;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.TokenPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/20 at  10:48 AM
 */
public class FunctionRecord implements FunctionRecordInterface {
    public FunctionRecord() {
    }

    public FunctionRecord(FKey key, List<String> sourceCode) {
        this.sourceCode = sourceCode;
        this.key = key;
    }
    boolean extrinsic = false;
    public boolean isExtrinsic(){
        return extrinsic;
    }
    public void setExtrinsic(boolean extrinsic){
        this.extrinsic = extrinsic;
    }
    @Override
    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    boolean anonymous = false;

    @Override
    public boolean isLambda() {
        return lambda;
    }

    public void setLambda(boolean lambda) {
        this.lambda = lambda;
    }

    boolean lambda = false;

    public void setName(String name) {
        this.name = name;
        if (name != null && !name.isEmpty()) {
            setExtrinsic(State.isExtrinsic(name));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    TokenPosition tokenPosition = null;

    public void setTokenPosition(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    @Override
    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    @Override
    public boolean hasTokenPosition() {
        return tokenPosition != null;
    }

    public static int FREF_ARG_COUNT = -10;
    public String name;

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    public List<String> sourceCode = new ArrayList<>();

    public void setDocumentation(List<String> documentation) {
        this.documentation = documentation;
    }

    @Override
    public List<String> getDocumentation() {
        return documentation;
    }

    public List<String> documentation = new ArrayList<>();

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> statements = new ArrayList<>();

    public List<String> getArgNames() {
        if (argNames == null) {
            argCount = argNames.size();
        }
        return argNames;
    }

    public void setArgNames(List<String> argNames) {
        this.argNames = argNames;
    }

    public List<String> argNames = new ArrayList<>();

    public String getfRefName() {
        return fRefName;
    }

    public void setfRefName(String fRefName) {
        this.fRefName = fRefName;
    }

    public String fRefName = null;

    public boolean isOperator() {
        return operator;
    }

    public void setOperator(boolean operator) {
        this.operator = operator;
    }

    public boolean operator = false;
    public boolean isFuncRef() {
        return funcRef;
    }

    public void setFuncRef(boolean funcRef) {
        this.funcRef = funcRef;
    }

    protected boolean funcRef = false;
    @Override
    public FKey getKey() {
        if (key == null) {
            key = new FKey(getName(), getArgCount());
        }
        return key;
    }

    protected FKey key = null;


    @Override
    public boolean hasName() {
        return name != null;
    }

    public void setArgCount(int argCount) {
        this.argCount = argCount;
    }

    Integer argCount = null;

    @Override
    public int getArgCount() {
        if (argCount == null) {
            if (argNames == null) {
                argCount = FREF_ARG_COUNT;
            } else {
                argCount = getArgNames().size();
            }
        }
        return argCount;
    }

    @Override
    public String toString() {
        return "FunctionRecord{" +
                "name='" + name + '\'' +
                ", sourceCode='" + sourceCode + '\'' +
                ", statements=" + statements +
                ", argNames=" + argNames +
                ", arg count = " + getArgCount() +
                ", f_ref? = " + isFuncRef() +
                (fRefName == null ? "" : ", ref_name = \"" + fRefName + "\"") +
                '}';
    }

    @Override
    public FunctionRecord clone() {
        FunctionRecord functionRecord = new FunctionRecord();
        functionRecord.setName(name);
        functionRecord.sourceCode = sourceCode;
        functionRecord.documentation = documentation;
        functionRecord.statements = statements;
        functionRecord.argNames = argNames;
        functionRecord.lambda = lambda;
        return functionRecord;
    }

}
