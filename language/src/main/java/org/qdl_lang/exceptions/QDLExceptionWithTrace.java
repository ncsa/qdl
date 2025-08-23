package org.qdl_lang.exceptions;

import org.qdl_lang.state.AbstractState;
import org.qdl_lang.statements.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * An exception when evaluating a set of statements. This gives the statement number
 * (not the line number, which is close to impossible to determine given how Antlr
 * handles whitespace).
 * <p>Created by Jeff Gaynor<br>
 * on 11/3/21 at  4:08 PM
 */
public class QDLExceptionWithTrace extends QDLException{

    public boolean hasStatement(){
        return statement != null;
    }
    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    Statement statement;

    public QDLExceptionWithTrace(Statement statement) {
        this.statement = statement;
    }

    public QDLExceptionWithTrace(Throwable cause, Statement statement) {
        super(cause);
        this.statement = statement;    }

    public QDLExceptionWithTrace(String message, Statement statement) {
        super(message);
        this.statement = statement;    }

    public QDLExceptionWithTrace(String message, Throwable cause, Statement statement) {
        super(message, cause);
        this.statement = statement;    }
    boolean script = false;
    String scriptName;

    public boolean isScript() {
        return script;
    }

    public void setScript(boolean script) {
        this.script = script;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public List<AbstractState.QDLStackTraceElement> getScriptStack() {
        if(scriptStack == null){
             scriptStack = new ArrayList<>();
         }
        return scriptStack;
    }

    public void setScriptStack(List<AbstractState.QDLStackTraceElement> scriptStack) {

        this.scriptStack = scriptStack;
    }

    public boolean hasScriptStack(){
        return scriptStack != null;
    }

    /**
     * Uses the {@link #scriptStack} to create a trace of scripts.
     * @return
     */
    public String stackTrace() {
        if (getScriptStack().isEmpty()) {
            return "";
        }
       StringBuilder stringBuilder = new StringBuilder();
        String indent = "";
        // https://github.com/ncsa/qdl/issues/91
        int size = getScriptStack().size();
        for(int i = 0; i < size; i++) {
            AbstractState.QDLStackTraceElement se = getScriptStack().get(size-i-1);
            String s;
            if(se!=null) {
                if (se.position != null) {

                    s = se.resource + " called at (" + se.position.line + "," + se.position.col + ") in";
                }else{
                    s = se.resource + " called  in";

                }
            }else{
                s = "";
            }
            stringBuilder.append(indent + s + "\n");
            indent = indent + " ";
        }
        stringBuilder.append(indent + "main\n");
        return stringBuilder.toString();
    }

    List<AbstractState.QDLStackTraceElement> scriptStack;



}
