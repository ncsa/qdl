package org.qdl_lang.expressions;

import org.qdl_lang.variables.QDLStem;

import java.util.ArrayList;
import java.util.List;

/**
 * Marker class to show all the indices are to be used.
 */
public class AllIndices extends ConstantNode {
    public AllIndices() {
        super(new QDLStem());
    }

    @Override
    public List<String> getSourceCode() {
        List<String> a = new ArrayList<>();
        a.add("*");
        return a;
    }

    @Override
    public String toString() {
        return "*";
    }

    @Override
    public int getNodeType() {
        return ALL_INDICES_NODE;
    }
}
