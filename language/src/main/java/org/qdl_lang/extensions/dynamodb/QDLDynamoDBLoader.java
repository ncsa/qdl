package org.qdl_lang.extensions.dynamodb;

import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.expressions.module.Module;

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
