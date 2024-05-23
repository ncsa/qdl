package edu.uiuc.ncsa.qdl.extensions.example;

import edu.uiuc.ncsa.qdl.extensions.QDLVariable;
import edu.uiuc.ncsa.qdl.variables.QDLStem;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  1:26 PM
 */
public class EGStem implements QDLVariable {
    @Override
    public String getName() {
        return "eg.";
    }

    @Override
    public Object getValue() {
        QDLStem stemVariable = new QDLStem();
        stemVariable.put("help", "This is an example stem variable that shows how to make one and  is shipped with the standard distro.");
        stemVariable.put("time", "Current time in ms is " + new Date().getTime());
        stemVariable.put("integer", 456456546L);
        stemVariable.put("decimal", new BigDecimal("3455476.987654567654567"));
        stemVariable.put("boolean", Boolean.TRUE);
        QDLStem nestedStem = new QDLStem();
        nestedStem.put("0", 10L);
        nestedStem.put("1", 11L);
        nestedStem.put("2", 12L);
        nestedStem.put("3", "foo");
        stemVariable.put("list.", nestedStem);
        return stemVariable;
    }
}
