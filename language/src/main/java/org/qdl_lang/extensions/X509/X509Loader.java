package org.qdl_lang.extensions.X509;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.expressions.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  3:16 PM
 */
public class X509Loader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
         modules.add(new QDL509Module().newInstance(null));
         return modules;
    }
}
