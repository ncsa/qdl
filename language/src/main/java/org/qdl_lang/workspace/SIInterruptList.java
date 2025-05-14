package org.qdl_lang.workspace;

import java.util.Collection;

/**
 * Has a single collection or regex. This is used either for exclusions
 * or inclusions.
 */
public class SIInterruptList {
    public SIInterruptList(Collection interrupts) {
        this.interrupts = interrupts;
    }

    public SIInterruptList(String regex) {
        this.regex = regex;
    }

    Collection interrupts = null;
    String regex = null;
    public boolean hasList(){
        return interrupts != null;
    }
    public boolean hasRegex(){
        return regex != null;
    }

    /**
     * Returns true if the label matches something in this set. Automatically figures out
     * whether to use the regex or not.
     * @param label
     * @return
     */

    public boolean matches(Object label){
        if(label == null){
            return false;
        }
        if(hasList()){
            return interrupts.contains(label);
        }
        String sLabel = label.toString();
        return sLabel.matches(regex);
    }
}
