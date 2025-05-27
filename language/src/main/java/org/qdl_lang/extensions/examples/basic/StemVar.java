package org.qdl_lang.extensions.examples.basic;

import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

import java.math.BigDecimal;
import java.util.Date;

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
        stemVariable.put("help", "This is an basic stem variable that shows how to make one and  is shipped with the standard distro.");
        stemVariable.put("time", "Current time in ms is " + new Date().getTime());
        stemVariable.put("integer", 456456546L);
        stemVariable.put("decimal", new BigDecimal("3455476.987654567654567"));
        stemVariable.put("boolean", Boolean.TRUE);
        QDLSet set = new QDLSet();
        set.add("one");
        set.add("two");
        set.add(3L); // Remember all "integers" in QDL are 64 bit, i.e. longs in Java!
        stemVariable.put("set", set);
        QDLStem nestedStem = new QDLStem();
        nestedStem.put("0", 10L);
        nestedStem.put("1", 11L);
        nestedStem.put("2", 12L);
        nestedStem.put("3", "foo");
        stemVariable.put("list.", nestedStem);
        return stemVariable;
    }
}
