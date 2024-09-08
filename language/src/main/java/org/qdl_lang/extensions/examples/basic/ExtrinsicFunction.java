package org.qdl_lang.extensions.examples.basic;

import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * An basic of an extrinsic (<i>aka</i> global, static in other languages) function.
 * This function will be treated like any other function. It is available everywhere
 * in the workspace.
 *
 * <p>When creating one, remember that only things that truly need to be accessible
 * the same everywhere should be made extrinsic. </p>
 *
 */
public class ExtrinsicFunction implements QDLFunction {
    public static String EX_NAME = "$$my_extrinsic";
    @Override
    public String getName() {
        return EX_NAME;
    }

    @Override
    public int[] getArgCount() {
        return new int[]{1};
    }

    @Override
    public Object evaluate(Object[] objects, State state) throws Throwable {
        return objects[0];
    }

    @Override
    public List<String> getDocumentation(int argCount) {
        ArrayList<String> documentation = new ArrayList<>();

        documentation.add(getName() + "(x) - a sample extrinsic function. It just returns it argument.");
        return documentation;
    }
}
