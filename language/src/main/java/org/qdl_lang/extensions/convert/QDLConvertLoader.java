package org.qdl_lang.extensions.convert;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:29 AM
 */
public class QDLConvertLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
        modules.add(new QDLConvertModule().newInstance(null));
        return modules;
    }
}
