package org.qdl_lang.util.aggregate;

import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

import java.util.List;

/**
 * Process a stem but only to a specific rank. This then hands off processing to the main method, returning
 * a value
 */
public interface ProcessStemAxisRestriction extends ProcessStemValues {
    Object process(List<Object> index, Object key, QDLSet sset);
    Object  process(List<Object> index,Object key, QDLStem value);

    /**
     *
     * @return
     */
    int getAxis();

    /**
     * Used to mean no rank restrictions for this processor.
     */
   int ALL_AXES = -1;
}
