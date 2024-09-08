package org.qdl_lang.extensions.examples.stateful;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;

public class StatefulModule extends JavaModule {
    public StatefulModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    public StatefulModule() {
    }

    @Override
    public Module newInstance(State state) {
        // This is the module object QDL managges
        StatefulModule module = new StatefulModule(URI.create("ex:stateful"), "stateful");
        ArrayList<QDLFunction> funcs = new ArrayList<>();
        // this is the actual implementation of the module
        StatefulExample stateful = new StatefulExample();
        module.setMetaClass(stateful);
        funcs.add(stateful.new GetS());
        funcs.add(stateful.new SetS());
        module.addFunctions(funcs);
        // Now for any variables:
        ArrayList<QDLVariable> vars = new ArrayList<>();
        vars.add(stateful.new LoadTimestamp());
        module.addVariables(vars);
        // the next two line are required
        if (state != null) {
            module.init(state); // populates the instance's state with functions and variables
        }
        setupModule(module); // Various accouting tasks for QDL to track this module. Call this last in this method.
        return module;
    }
}
