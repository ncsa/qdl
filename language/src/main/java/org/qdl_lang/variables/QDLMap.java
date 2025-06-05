package org.qdl_lang.variables;

import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.values.StringValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/27/22 at  6:46 AM
 */
public class QDLMap extends HashMap<String, QDLValue> {
    public StemKeys stringKeySet(){
        StemKeys stemkeys = new StemKeys();
        TreeSet<StringValue> keys = new TreeSet<>();

        for(String key : keySet()){
            keys.add(new StringValue(key));
        }
        stemkeys.setStemKeys(keys);
        return stemkeys;
    }
}
