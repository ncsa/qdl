package edu.uiuc.ncsa.qdl.expressions;

import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Commonly used. Just have a wrapper for this
 * <p>Created by Jeff Gaynor<br>
 * on 3/6/23 at  6:31 AM
 */
public class ArgList extends ArrayList<ExpressionInterface> {
    public ArgList(int initialCapacity) {
        super(initialCapacity);
    }

    public ArgList() {
    }

    public ArgList(Collection<? extends ExpressionInterface> c) {
        super(c);
    }
}
