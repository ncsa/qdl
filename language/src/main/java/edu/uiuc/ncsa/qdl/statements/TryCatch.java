package edu.uiuc.ncsa.qdl.statements;

import edu.uiuc.ncsa.qdl.exceptions.AssertionException;
import edu.uiuc.ncsa.qdl.exceptions.RaiseErrorException;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.variables.VThing;

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

    @Override
    public Object evaluate(State state) {
        State localState = state.newLocalState();
        try {
            for (Statement s : tryStatements) {
                s.evaluate(localState);
            }
        } catch (RaiseErrorException t) {
            // custom error handling
            localState.getVStack().localPut(new VThing(new XKey(ERROR_MESSAGE_NAME), t.getPolyad().getArguments().get(0).getResult().toString()));
            switch (t.getPolyad().getArgCount()) {
                case 3:
                    Object sss = t.getPolyad().getArguments().get(2).getResult();
                    if (!(sss instanceof QDLStem)) {
                        throw new IllegalArgumentException("the last argument must be a stem");
                    }
                    localState.getVStack().localPut(new VThing(new XKey(ERROR_STATE_NAME), t.getPolyad().getArguments().get(2).getResult()));
                case 2:
                    localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME),  t.getPolyad().getArguments().get(1).getResult()));
                    break;
                case 1:
                    localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME), RESERVED_USER_ERROR_CODE));
                    break;
            }

            for (Statement c : catchStatements) {
                c.evaluate(localState);
            }
        } catch (Throwable otherT) {
            // everything else.
            localState.getVStack().localPut(new VThing(new XKey(ERROR_MESSAGE_NAME), otherT.getMessage()));
            if (otherT instanceof AssertionException) {
                localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME), RESERVED_ASSERTION_CODE));
            } else {
                localState.getVStack().localPut(new VThing(new XKey(ERROR_CODE_NAME), RESERVED_SYSTEM_ERROR_CODE));
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

