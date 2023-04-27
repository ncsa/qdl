package edu.uiuc.ncsa.qdl.statements;

/**
 * The actual top-level interface for an expression. This adds information about whether this is
 * executing in a module and has an alias.
 * <p>Created by Jeff Gaynor<br>
 * on 9/28/20 at  4:18 PM
 */
public interface ExpressionInterface extends Statement, HasResultInterface {
    public ExpressionInterface makeCopy(); // would prefer clone, but there is a conflict in packages because it has protected access
    boolean hasAlias();
    String getAlias();
    void setAlias(String alias);
}
