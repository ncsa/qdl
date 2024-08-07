package edu.uiuc.ncsa.qdl.extensions.example;

import edu.uiuc.ncsa.qdl.evaluate.StringEvaluator;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  1:24 PM
 */
public class Concat implements QDLFunction {
    @Override
    public String getName() {
        return "concat";
    }

    @Override
    public int[] getArgCount() {
        return new int[]{2};
    }

    @Override
    public Object evaluate(Object[] objects, State state) {
        QDLStem stem = new QDLStem();
        stem.getQDLList().add(objects[0].toString());
        stem.getQDLList().add(objects[1].toString());
        Polyad detokenize = new Polyad(StringEvaluator.DETOKENIZE);
        detokenize.addArgument(new ConstantNode(stem));
        detokenize.addArgument(new ConstantNode(" "));
        detokenize.evaluate(state);
      return objects[0].toString() + objects[1].toString();
    }

    @Override
    public List<String> getDocumentation(int argCount) {
        ArrayList<String> docs = new ArrayList<>();
        docs.add(getName() + "(string, string) will concatenate the two arguments");
        docs.add("This is identical in function to the built in '+' operator for two arguments. It is just part of the");
        docs.add("sample kit for writing a java extension to QDL that is shipped with the standard distro.");
        return docs;
    }
}
