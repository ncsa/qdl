package org.qdl_lang.variables.values;

import org.qdl_lang.variables.QDLList;
import org.qdl_lang.variables.QDLStem;

import java.util.StringTokenizer;

public class StemValue extends QDLKey{
    public StemValue(QDLStem value) {
        super(value);
        type = STEM_TYPE;
    }
    public StemValue(QDLList value) {
        this(new QDLStem(value));
    }

    /**
     * Create this as the empty stem
     */
    public StemValue() {
        this(new QDLStem());
    }

    /**
     * Sets the stem from a path like a.b.c.d, resulting in a value of<br/><br/>
     * ['a','b','c','d'] <br/><br/>
     * <b>N.B.</b> that the first element is <i>not</i> the stem. The result is a simple
     * path that can be used to access the element. This crops up a fair bit in system programming.
     * @param path
     */
    public void fromPath(String path){
        StringTokenizer tokens = new StringTokenizer(path, ".");
        QDLStem stem = new QDLStem();
        while(tokens.hasMoreTokens()){
            String token = tokens.nextToken();
            stem.listAdd(new StringValue(token));
        }
        type = STEM_TYPE;
        setValue(stem);
    }
}
