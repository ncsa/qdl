package org.qdl_lang.extensions.inputLine;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.expressions.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/28/23 at  7:53 AM
 */
public class QDLCLIToolsLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
            modules.add(new QDLCLIToolsModule().newInstance(null));
            return modules;
    }
}
