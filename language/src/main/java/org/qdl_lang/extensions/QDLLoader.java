package org.qdl_lang.extensions;

import org.qdl_lang.expressions.module.Module;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for loading your classes. To use this you need to make it available (along with yourc
 * classes in the classpath). The contract is that this takes a zero argument constructor. All state
 * needed will be injected. So populate you {@link JavaModule}s as you see fit.
 * Programatically, you can set this in the CLI before starting it or specify it on the command line
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  5:47 PM
 */
public interface QDLLoader extends Serializable {
    /**
     * This will do all the work for creating modules and will return a list of them.
     * @return
     */
    List<Module> load();
}
