package org.qdl_lang.extensions.examples.basic;

import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.Date;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * A sample stem that has various values added to it. This shows you how to work with
 * QDL values in Java.
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  1:26 PM
 */
public class StemVar implements QDLVariable {
    @Override
    public String getName() {
        return "eg.";
    }

    @Override
    public Object getValue() {
        QDLStem stemVariable = new QDLStem();
        setStemValue(stemVariable,"help", "This is an basic stem variable that shows how to make one and  is shipped with the standard distro.");
        setStemValue(stemVariable,"time", "Current time in ms is " + new Date().getTime());
        setStemValue(stemVariable,"integer", 456456546L);
        setStemValue(stemVariable,"decimal", new BigDecimal("3455476.987654567654567"));
        setStemValue(stemVariable,"boolean", Boolean.TRUE);
        QDLSet<QDLValue> set = new QDLSet();
        set.add(asQDLValue("one"));
        set.add(asQDLValue("two"));
        set.add(asQDLValue(3)); // Remember all "integers" in QDL are 64 bit, i.e. longs in Java! This gets converted
        setStemValue(stemVariable,"set", set);
        QDLStem nestedStem = new QDLStem();
        setStemValue(nestedStem,0, 10L);
        setStemValue(nestedStem,1, 11L);
        setStemValue(nestedStem,2, 12L);
        setStemValue(nestedStem,3, "foo");
        setStemValue(stemVariable,"list.", nestedStem);
        return stemVariable;
    }

    /** Utility call to create the specific keys and values.
     *
     * @param stem
     * @param key
     * @param value
     */
    protected void setStemValue(QDLStem stem, Object key, Object value) {
        stem.put(QDLKey.from(key), asQDLValue(value));
    }
}
