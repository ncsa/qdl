package org.qdl_lang.extensions.example;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a sample of how to write a loader to get a module in to QDL.
 * All you need to do is override the {@link #load()} method.
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  5:44 PM
 */
public class EGLoaderImpl implements QDLLoader {
    @Override
    public List<Module> load(){
        EGModule EGModule = new EGModule();
        ArrayList<Module> modules = new ArrayList<>();
        modules.add(EGModule.newInstance(null));
        // Return this list of modules.
        return modules;
    }
}
