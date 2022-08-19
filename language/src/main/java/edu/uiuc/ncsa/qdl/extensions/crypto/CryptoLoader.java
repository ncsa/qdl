package edu.uiuc.ncsa.qdl.extensions.crypto;

import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  3:16 PM
 */
public class CryptoLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
         modules.add(new CryptoModule().newInstance(null));
         return modules;
    }
}
