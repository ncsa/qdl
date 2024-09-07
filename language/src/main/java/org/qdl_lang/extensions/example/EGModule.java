package org.qdl_lang.extensions.example;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The module for the example. This sets the namespace and an alias (for people using
 * the old module system). It is charged with creating new instances of itself and holds
 * module level documentation.
 * <p>Created by Jeff Gaynor<br>
 * on 4/2/20 at  8:09 AM
 */
public class EGModule extends JavaModule {
    public EGModule() {
    }

    protected EGModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        // Step 1: create the module with the URI and alias
        EGModule EGModule = new EGModule(URI.create("ex:eg"), "eg");
        // Step 2: create a list of functions and populate it
        ArrayList<QDLFunction> funcs = new ArrayList<>();
        funcs.add(new Concat());
        funcs.add(new FunctionReferenceExample());
        funcs.add(new ExtrinsicFunction());
        // Once the list of functions is populated, add the functions to the module
        EGModule.addFunctions(funcs);

        // Step 3: create a list of variables and populate it
        ArrayList<QDLVariable> vars = new ArrayList<>();
        vars.add(new EGStem());
        vars.add(new StemEntry());
        vars.add(new ExtrinsicVariable());
        // Once the list of variables is populated, add it to the module
        EGModule.addVariables(vars);
        // Step 4: If this has a State object passed in initialize it. Note that
        // if you do not, then this instance will not be usable.
        // If the state is null, this implies that the state will be injected later and
        // the init method will be called too. 
        if(state != null){
            EGModule.init(state);
        }
        setupModule(EGModule);
        return EGModule;
    }
    List<String> description = null;

    @Override
    public List<String> getDescription() {
        if (description == null) {
            description = new ArrayList<>();
            description.add("This module is a simple example from the toolkit to show how");
            description.add("to create a module and import it and use it. It has an extrinsic function,");
            description.add(ExtrinsicFunction.EX_NAME + " and an extrinsic variable " + ExtrinsicVariable.EX_NAME);
            description.add("which are available on load (so no import needed).");
            description.add("It also has an example where a single stem value is set for " + StemEntry.STEM_NAME);
        }
        return description;
    }

}
