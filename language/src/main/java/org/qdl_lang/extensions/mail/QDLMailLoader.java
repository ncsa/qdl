package org.qdl_lang.extensions.mail;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/2/23 at  11:17 AM
 */
public class QDLMailLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
        modules.add(new QDLMailModule().newInstance(null));
        return modules;
    }
}
