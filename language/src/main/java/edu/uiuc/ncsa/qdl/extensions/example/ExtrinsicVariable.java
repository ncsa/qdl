package edu.uiuc.ncsa.qdl.extensions.example;

import edu.uiuc.ncsa.qdl.extensions.QDLVariable;

/**
 * An extrinsic (<i>aka</i> global, static in other langauges) variable in the workspace.
 * Note that this is available after the module loads, hence before import. It has a value
 * of 42, since, as we all know, that is the answer to everything.
 */
public class ExtrinsicVariable implements QDLVariable {
    public static String EX_NAME = "$$EG";
    @Override
    public String getName() {
        return "$$EG";
    }

    @Override
    public Object getValue() {
        return 42L;
    }
}
