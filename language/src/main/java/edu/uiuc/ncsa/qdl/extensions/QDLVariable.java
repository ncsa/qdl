package edu.uiuc.ncsa.qdl.extensions;

import edu.uiuc.ncsa.qdl.variables.QDLStem;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * This represents a QDL variable.  At load time, the name will be read and the value will be assigned in the
 * symbol table. Once the variable is in the system, this is ignored since the user then has access to it and
 * can modify it. It is just another variable at that point.
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  12:02 PM
 */
public interface QDLVariable extends Serializable {
    /**
     * The name of the variable. This may be a simple name for a scalar, like "a",
     * or it may represent a stem, like "a." (yes, include the period for a stem).
     * You may even set specific stem values
     * by passing in the indexed stem, e.g. "a.3". It might make sense, if e.g. you had a stem
     * that modelled String Theory where the stem is an 11 dimensional object that required a great deal
     * of computation only do-able in Java (such as a specialized scientific library which
     * would be a hugely complex job to expose in QDL). You could then just set each
     * component of the stem.
     * <h2>A note on extrinsic values</h2>
     * <p>
     *     Extrinsic values start with a $$ ({@link edu.uiuc.ncsa.qdl.state.VariableState#EXTRINSIC_MARKER})
     *     and when the module is loaded, these are put into the workspace -- not at import.
     *     This lets you have module constants in advance of configuration the module.
     * </p>
     * @return
     */
    public String getName();

    /**
     * The value. The basic Java types that QDL knows are {@link Boolean},
     * {@link String}, {@link Long}, {@link BigDecimal}, {@link edu.uiuc.ncsa.qdl.variables.QDLSet}
     * and {@link QDLStem}. Again, this class sets the value and is used to create a
     * single instance of this variable in the workspace, so there should be no state
     * to manage in this class -- just return the value it should have.
     *
     * @return
     */
    public Object getValue();
}
