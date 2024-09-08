package org.qdl_lang.extensions.examples.stateful;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.module.Module;

import java.util.ArrayList;
import java.util.List;

public class StatefulLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        ArrayList<Module> modules = new ArrayList<>();
        modules.add(new StatefulModule().newInstance(null));
        return modules;
    }
}
