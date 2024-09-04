package org.qdl_lang.functions;

import org.qdl_lang.state.State;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/11/24 at  12:23 AM
 */
public interface FunctionReferenceNodeInterface {
    String getFunctionName();

     void setFunctionName(String functionName);

     boolean isAnonymous();

     void setAnonymous(boolean anonymous);

     State getModuleState();

     void setModuleState(State moduleState);

     boolean hasModuleState();
     FunctionRecordInterface getFunctionRecord(int argCount);
     boolean hasFunctionRecord(int argCount);
}
