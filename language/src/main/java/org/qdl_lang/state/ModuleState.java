package org.qdl_lang.state;

import org.qdl_lang.evaluate.MetaEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.expressions.module.MIStack;
import org.qdl_lang.expressions.module.MTStack;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.variables.VStack;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Modules that the user has imported to the current scope. Note that
     * this is mostly used for serializing a workspace: In particular,
     * for Java modules, there is no other way than to serialize the module
     * then re-use it on deserialization.
     * @return
     */
    public Map<URI,Module> getUsedModules() {
        if(usedModules == null)         {
            usedModules = new HashMap<>();
        }
        return usedModules;
    }

    public void setUsedModules(Map<URI,Module> usedModules) {
        this.usedModules = usedModules;
    }

    Map<URI, Module> usedModules;

}