package edu.uiuc.ncsa.qdl.extensions.xml;

import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/13/23 at  7:29 AM
 */
public class QDLXMLLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
        modules.add(new QDLXMLModule().newInstance(null));
        return modules;
    }
}
