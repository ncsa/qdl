package edu.uiuc.ncsa.qdl.state;

import edu.uiuc.ncsa.qdl.evaluate.MetaEvaluator;
import edu.uiuc.ncsa.qdl.evaluate.OpEvaluator;
import edu.uiuc.ncsa.qdl.module.MIStack;
import edu.uiuc.ncsa.qdl.module.MTStack;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.variables.VStack;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

/**
 * Handles all the module related operations for the state.
 * <p>Created by Jeff Gaynor<br>
 * on 1/31/22 at  7:26 AM
 */
public abstract class ModuleState extends AbstractState {
    public ModuleState(VStack vStack,
                       OpEvaluator opEvaluator,
                       MetaEvaluator metaEvaluator,
                       MTStack MTemplates,
                       MIStack mInstances,
                       MyLoggingFacade myLoggingFacade
                       ) {
        super(vStack, opEvaluator, metaEvaluator, myLoggingFacade);
        this.MTemplates = MTemplates;
        this.MInstances = mInstances;
    }

    public void setMTemplates(MTStack MTemplates) {
        this.MTemplates = MTemplates;
    }

    public MTStack getMTemplates() {
           return MTemplates;
       }

       protected MTStack MTemplates = new MTStack();
    /**
      * Modules (with their state) that have been imported and are keyed by alias.
      *
      * @return
      */
     public MIStack getMInstances() {
         return MInstances;
     }

     public void setMInstances(MIStack mInstances) {
         this.MInstances = mInstances;
     }

     MIStack MInstances = new MIStack();

     /**
      * Get a single imported module by alias or null if there is no such module.
      *
      * @param alias
      * @return
      */
     public Module getImportedModule(String alias) {
         if (alias == null) {
             return null;
         }
         return  getMInstances().getModule(new XKey(alias));
     }

     boolean importMode = false;

    public boolean isImportMode() {
        return importMode;
    }

    public void setImportMode(boolean importMode) {
        this.importMode = importMode;
    }
    public boolean isModuleState() {
        return moduleState;
    }

    /**
     * Flag this if it is used as the local state for a module. This is used to enforce
     * local resolutions at runtime.
     *
     * @param moduleState
     */
    public void setModuleState(boolean moduleState) {
        this.moduleState = moduleState;
    }

    boolean moduleState = false;

    public boolean hasModule(){
        return module!=null;
    }
    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    Module module = null;

}