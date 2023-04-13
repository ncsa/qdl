package edu.uiuc.ncsa.qdl.expressions;

import edu.uiuc.ncsa.qdl.evaluate.OpEvaluator;
import edu.uiuc.ncsa.qdl.statements.ExpressionInterface;

import java.util.ArrayList;

/**
 * This class mostly manages the structure of expressions (so arguments are the children) and
 * evaluating them is delegating the result to the {@link OpEvaluator} class.
 * <p>Created by Jeff Gaynor<br>
 * on 1/13/20 at  3:02 PM
 */
public interface ExpressionNode extends ExpressionInterface {

    ArrayList<ExpressionInterface> getArguments(); // need this to preserve order of lists

    ExpressionInterface getArgAt(int index);

    void setArguments(ArrayList<ExpressionInterface> arguments);

    int getArgCount();

    int valence = 0;

    int getOperatorType();

    void setOperatorType(int operatorType);

}
