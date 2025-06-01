package org.qdl_lang.extensions.inputLine;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/28/23 at  7:54 AM
 */
public class QDLCLIToolsModule extends JavaModule {
    public QDLCLIToolsModule() {
    }

    public QDLCLIToolsModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        QDLCLIToolsModule ilm = new QDLCLIToolsModule(URI.create("qdl:/tools/cli"), "cli");
        QDLCLITools il = new QDLCLITools();
        ilm.setMetaClass(il);
        ArrayList<QDLFunction> funcs = new ArrayList<>();

        funcs.add(il.new ToStem());
        ilm.addFunctions(funcs);
        if (state != null) {
            ilm.init(state);
        }
        setupModule(ilm);
        return ilm;
    }

    List<String> doxx = new ArrayList<>();
    @Override
    public List<String> getDescription() {
        if(doxx.isEmpty()){
           doxx.add("Command line interface tools.");
           doxx.add("This module contains tools that help work with command lines and their arguments");
           doxx.add(QDLCLITools.TO_STEM_NAME + " - convert a list (including the args()) to a stem of switches and arguments.");
           doxx.add("A common idiom for command line arguments is to have switches that are prefixed with a marker, ");
           doxx.add("such as '-' as well as arguments. ");
           doxx.add("If this is invoked with no arguments, it will attempt to process the value of args() with marker '-'");
           doxx.add("into a stem.");
           doxx.add("");
           doxx.add("");
        }
        return doxx;
    }
}