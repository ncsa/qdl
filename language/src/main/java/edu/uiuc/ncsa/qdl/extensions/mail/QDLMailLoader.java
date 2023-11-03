package edu.uiuc.ncsa.qdl.extensions.mail;

import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.module.Module;

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
