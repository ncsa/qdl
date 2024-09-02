package edu.uiuc.ncsa.qdl.extensions.example;

import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.state.State;

import java.util.List;

public class ExtrinsicFunction implements QDLFunction {
    @Override
    public String getName() {
        return "$$my_extrinsic";
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
        return List.of();
    }
}
