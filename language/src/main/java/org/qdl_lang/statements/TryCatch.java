package org.qdl_lang.statements;

import org.qdl_lang.exceptions.*;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.QDLVariable;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.VThing;
import edu.uiuc.ncsa.security.core.util.DebugConstants;
import edu.uiuc.ncsa.security.core.util.DebugUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/20 at  10:36 AM
 */
public class TryCatch implements Statement {
    TokenPosition tokenPosition = null;

    @Override
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

    public static final Long RESERVED_SYSTEM_ERROR_CODE = -1L;
    public static final Long RESERVED_ASSERTION_CODE = -2L;
    public static final Long RESERVED_USER_ERROR_CODE = 0L;
    public static final String ERROR_CODE_NAME = "error_code";
    public static final String ERROR_MESSAGE_NAME = "error_message";
    public static final String ERROR_STATE_NAME = "error_state.";
     /*
       while[true][try[s:=scan('x');3/0;]catch[say('bar');];];

              while[
               true
               ][
                 try[
                    s:=scan('x');
                    3/0;
                   ]catch[
                     say('bar);
                   ];
                ];

      */
    @Override
    public QDLValue evaluate(State state) {
        State localState = state.newLocalState();
        try {
            for (Statement s : tryStatements) {
                s.evaluate(localState);
            }
        } catch (RaiseErrorException t) {
            // custom error handling
            Long errorCode = RESERVED_USER_ERROR_CODE;
            // https://github.com/ncsa/qdl/issues/7
            QDLStem errorState = new QDLStem();
            //QDLStem errorState = t.getState();
            String message = t.getPolyad().getArguments().get(0).getResult().toString();
            switch (t.getPolyad().getArgCount()) {
                case 3:
                    QDLValue sss = t.getPolyad().getArguments().get(2).getResult();
                    if (!(sss.isStem())) {
                        throw new IllegalArgumentException("the last argument must be a stem");
                    }
                    //localState.getVStack().localPut(new VThing(new XKey(ERROR_STATE_NAME), t.getPolyad().getArguments().get(2).getResult()));
                    errorState = t.getPolyad().getArguments().get(2).getResult().asStem();
                case 2:
                    if(t.getPolyad().getArguments().get(1).getResult().isLong()){
                        throw new IllegalArgumentException("second argument must be an integer");
                    }
                    errorCode = t.getPolyad().getArguments().get(1).getResult().asLong();
                    //localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME),  t.getPolyad().getArguments().get(1).getResult()));
                    break;
                case 1:
                    message =  t.getPolyad().getArguments().get(0).getResult().toString();
                    //localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME), RESERVED_USER_ERROR_CODE));
                    break;
            }
            localState.getVStack().localPut(new VThing(new XKey(ERROR_MESSAGE_NAME), new QDLVariable(message)));
            localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME),  new QDLVariable(errorCode)));
            localState.getVStack().localPut(new VThing(new XKey(ERROR_STATE_NAME), new QDLVariable(errorState)));

            for (Statement c : catchStatements) {
                c.evaluate(localState);
            }
        }catch(ReturnException | ContinueException | BreakException returnException){
            throw returnException;
        }catch (Throwable otherT) {
            // everything else.
            if(otherT.getMessage() == null){
                // This is really ugly, *BUT* there are a very few exceptions thrown in QDL that still do not
                // set the message and this is a diagnostic tool so I can track these down.
                if(DebugUtil.getDebugLevel() == DebugConstants.DEBUG_LEVEL_TRACE){
                    otherT.printStackTrace();
                }
                localState.getVStack().localPut(new VThing(new XKey(ERROR_MESSAGE_NAME), new QDLVariable( "(no message)")));
            }else {
                localState.getVStack().localPut(new VThing(new XKey(ERROR_MESSAGE_NAME), new QDLVariable(otherT.getMessage())));
            }
            if (otherT instanceof AssertionException) {
                AssertionException assertionException = (AssertionException)otherT;
                if(assertionException.hasPayload()){
                    localState.getVStack().localPut(new VThing(new XKey(ERROR_STATE_NAME), new QDLVariable(assertionException.getAssertionState())));
                }else{
                    localState.getVStack().localPut(new VThing(new XKey(ERROR_STATE_NAME), new QDLVariable(new QDLStem())));
                }
                localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME), new QDLVariable(RESERVED_ASSERTION_CODE)));
            } else {
                localState.getVStack().localPut(new VThing(new XKey(ERROR_STATE_NAME), new QDLVariable(new QDLStem())));
                localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME), new QDLVariable(RESERVED_SYSTEM_ERROR_CODE)));
            }

            for (Statement c : catchStatements) {
                c.evaluate(localState);
            }
        }
        return null;
    }

    /*
    g(x)->[raise_error('oops');];
    try[g(1);]catch[return(0);];
     */
    List<Statement> catchStatements = new ArrayList<>();
    List<Statement> tryStatements = new ArrayList<>();

    public List<Statement> getCatchStatements() {
        return catchStatements;
    }

    public void setCatchStatements(List<Statement> catchStatements) {
        this.catchStatements = catchStatements;
    }

    public List<Statement> getTryStatements() {
        return this.tryStatements;
    }

    public void setTryStatements(List<Statement> tryStatements) {
        this.tryStatements = tryStatements;
    }

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    List<String> sourceCode;
}

