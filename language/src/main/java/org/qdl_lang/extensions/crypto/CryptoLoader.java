package org.qdl_lang.extensions.crypto;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.expressions.module.Module;

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
