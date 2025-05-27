package org.qdl_lang.variables;

import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.values.QDLNullValue;

import java.util.ArrayList;
import java.util.List;

/**
 * This represents when the user explicitly sets a variable to null. This just exists. It does nothing.
 * Note that this is a static class -- there is exactly one null object in QDL.
 * <p>Created by Jeff Gaynor<br>
 * on 4/9/20 at  9:08 AM
 */
public class QDLNull extends ConstantNode {
    static QDLNull qdlNull = null;

    public static QDLNull getInstance() {
        if (qdlNull == null) {
            qdlNull = new QDLNull();
        }
        return qdlNull;
    }

    private QDLNull() {
        super(null);  // Have to set since the result is QDLNode which does not exist yet.
        QDLNullValue nullValue = new QDLNullValue();
        nullValue.setValue(this);
        setResult(nullValue);
        List<String> source = new ArrayList<>();
        if (State.isPrintUnicode()) {
            source.add("∅");
        } else {
            source.add(Constants.NULL_NAME);
        }
        setSourceCode(source);
        setEvaluated(true);
    }

    @Override
    public String toString() {
        if (State.isPrintUnicode()) {
            return "∅";
        }
        return "null";
    }
    @Override
        public int getNodeType() {
            return QDL_NULL_NODE;
        }
}
