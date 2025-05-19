package org.qdl_lang.workspace;

import java.io.Serializable;

/**
 * The set of current interrupt inclusions and exclusions. These are managed on per
 * pid
 */
public class SIInterrupts implements Serializable {
    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    int processID;
    public SIInterruptList getInclusions() {
        return inclusions;
    }

    public void setInclusions(SIInterruptList inclusions) {
        this.inclusions = inclusions;
    }

    public SIInterruptList getExclusions() {
        return exclusions;
    }

    public void setExclusions(SIInterruptList exclusions) {
        this.exclusions = exclusions;
    }

    SIInterruptList inclusions = null;
    SIInterruptList exclusions  = null;
    public boolean hasInclusions() {
        return inclusions != null;
    }
    public boolean hasExclusions() {
        return exclusions != null;
    }
    public boolean hasInterrupts(){
        return hasInclusions() || hasExclusions();
    }
    public boolean doInterrupt(Object label){
        if(!hasInterrupts()){return true;} //nothing set means do every interrupt
        if(hasInclusions()){
            if(hasExclusions()){
                // Include list say must stop only, exclude list says must skip
                // if on both, exclusions wins.
                return true;
            }else{
                return getInclusions().matches(label);
            }
        }else{
            if(hasExclusions()){
                return !getExclusions().matches(label);
            }else{
                return true;
            }
        }

    }
}
