package org.qdl_lang.extensions.examples.stateful;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class StatefulModule extends JavaModule {
    public StatefulModule(URI namespace) {
        super(namespace);
    }

    public StatefulModule() {
    }

    @Override
    public Module newInstance(State state) {
        // This is the module object QDL manages
        StatefulModule module = new StatefulModule(URI.create("ex:stateful"));
        ArrayList<QDLFunction> funcs = new ArrayList<>();
        // this is the actual implementation of the module
        StatefulExample stateful = new StatefulExample();
        module.setMetaClass(stateful);
        funcs.add(stateful.new GetS());
        funcs.add(stateful.new SetS());
        module.addFunctions(funcs);
        // Now for any variables:
        ArrayList<QDLVariable> vars = new ArrayList<>();
        vars.add(stateful.new ImportTimestamp());
        module.addVariables(vars);
        // the next two line are required
        module.init(state); // populates the instance's state with functions and variables
        setupModule(module); // Various accounting tasks for QDL to track this module. Call this last in this method.
        return module;
    }

    @Override
    public List<String> getDescription() {
        List<String> doc = new ArrayList<>();
        doc.add("A stateful module example. This is intended for programmers who ");
        doc.add("are learning how to write their own QDL modules in Java. It shows");
        doc.add("how to create an implementation class the contains inner classes which ");
        doc.add("are the functions and variables for the modules. The assumption is that");
        doc.add("all of these share some state in the Java class (which is a priori unknown");
        doc.add("to QDL) and must be serialized. ");
        doc.add("See the documentation https://qdl-lang.org/pdf/qdl_extensions.pdf");
        return doc;
    }
}
