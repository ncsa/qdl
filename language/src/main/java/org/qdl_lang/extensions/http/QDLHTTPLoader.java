package org.qdl_lang.extensions.http;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.expressions.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/5/21 at  10:20 AM
 */
public class QDLHTTPLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
        modules.add(new QDLHTTPModule().newInstance(null));
        return modules;
    }
}
