package org.qdl_lang.functions;

import org.qdl_lang.state.XThing;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.statements.TokenPosition;

import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/7/23 at  9:43 AM
 */
public interface FunctionRecordInterface extends XThing {
    boolean isAnonymous();

    boolean isLambda();
boolean isExtrinsic();
    @Override
    String getName();
    void setName(String  name);

    TokenPosition getTokenPosition();

    boolean hasTokenPosition();

    @Override
    FKey getKey();

    boolean hasName();

    int getArgCount();

    FunctionRecordInterface clone();

    List<String> getArgNames();

    List<String> getSourceCode();
    void setSourceCode(List<String> sourceCode);

    List<String> getDocumentation();
   String getfRefName();

    void setfRefName(String fRefName) ;


    boolean isOperator();

    void setOperator(boolean operator);

     boolean isFuncRef();

    void setFuncRef(boolean funcRef);
    List<Statement> getStatements();

     void setStatements(List<Statement> statements);
}
