package org.qdl_lang.extensions.examples.basic;

import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * Example of a basic QDL function implemented in Java.
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  1:24 PM
 */
public class ConcatFunction implements QDLFunction {
    @Override
    public String getName() {
        return "concat";
    }

    @Override
    public int[] getArgCount() {
        return new int[]{2};
    }

    @Override
    public QDLValue evaluate(QDLValue[] qdlValues, State state) {
      return asQDLValue(qdlValues[0].toString() + qdlValues[1]); // call toString so it compiles. Can't add objects
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
