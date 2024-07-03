package edu.uiuc.ncsa.qdl.extensions.dynamodb;

import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.module.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 5/5/22 at  12:06 PM
 */
public class QDLDynamoDBLoader implements QDLLoader {
    @Override
    public List<Module> load() {
        List<Module> modules = new ArrayList<>();
        modules.add(new QDLDynamoDBModule().newInstance(null));
        return modules;
    }

}
