package edu.uiuc.ncsa.qdl.extensions.inputLine;

import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/28/23 at  7:53 AM
 */
public class QDLCLITools {
    public static String TO_STEM_NAME = "to_stem";
    public static String DEFAULT_SWITCH_MARKER =  "-";

    public class ToStem implements QDLFunction {
        @Override
        public String getName() {
            return TO_STEM_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1, 2};
        }

        /*
              module_import(module_load(info().lib.cli, 'java'))
            cli#to_stem(['-foo','bar','-woof','arf',3,5])

              has_keys('-woof', r.)
                has_keys('-foo', r.)


         */
        @Override
        public Object evaluate(Object[] objects, State state) {
            QDLStem out = new QDLStem();
            QDLStem inStem = null;
            String marker = DEFAULT_SWITCH_MARKER;
            Object[] aa = null;
            if (objects.length == 0) {
                if(state.getScriptArgs().length == 0){
                    return out;
                }
                aa = state.getScriptArgs();
            }
            if (objects.length == 1) {
                // two cases, arguyment is a stem or argument is a switch marker
                if (objects[0] instanceof String) {
                    marker = (String) objects[0];
                    aa = state.getScriptArgs();
                } else {
                    if (objects[0] instanceof QDLStem) {
                        inStem = (QDLStem) objects[0];
                    } else {
                        throw new IllegalArgumentException(getName() + ": requires a stem or string as its argument");
                    }
                    aa = getObjects(inStem);
                }
            }
            if (objects.length == 2) {
                // then the arguments are a stem and switch marker
                if (objects[0] instanceof QDLStem) {

                    inStem = (QDLStem) objects[0];
                    if(!inStem.isList()){
                        throw new IllegalArgumentException(getName() + " requires a list as its first argument");
                    }
                } else {
                    throw new IllegalArgumentException(getName() + " requires a list as its first argument");
                }
                if (objects[1] instanceof String) {
                    marker = (String) objects[1];
                } else {
                    throw new IllegalArgumentException(getName() + " requires a string as its second argument");
                }
                aa = getObjects(inStem);
            }
             for (int i = 0; i < aa.length; i++) {
                Object next = aa[i];
                if (next instanceof String && next.toString().startsWith(marker)) {
                    String nextArg = (String) next;
                    if (i + 1 < aa.length) {
                        Object nextNext = aa[i + 1];
                        if (nextNext instanceof String && nextNext.toString().startsWith(marker)) {
                            // So the next entry is another switch
                            out.put(nextArg, Boolean.TRUE);
                        } else {
                            out.put(nextArg, nextNext);
                        }
                        i = i + 1; // skip to next
                    }
                }else{
                    // add to list in order
                    out.listAdd(next);
                }
            }

            return out;
        }

        private Object[] getObjects(QDLStem inStem) {
            Object[] aa;
            aa = new Object[inStem.size()];
            // fill up aa;
            int index = 0;
            for (Object ooo : inStem.getQDLList().orderedKeys()) {
                aa[index++] = inStem.getQDLList().get((Long)ooo);
            }
            return aa;
        }

        List<String> doxx = new ArrayList<>();
        @Override
        public List<String> getDocumentation(int argCount) {
            if(doxx.isEmpty()){
                doxx.add(getName() + " - convert a list of arguments to a script to a stem");
                doxx.add(getName()+"() - use args() as the argument, default switch marker of " + DEFAULT_SWITCH_MARKER);
                doxx.add(getName() + "(list. | marker) - if a list, process that with the default switch marker");
                doxx.add("          otherwise, use args() with the given switch marker");
                doxx.add(getName() + "(list., marker) - specify both the list of arguments and the switch marker");
                doxx.add("The result is a stem of switches (which start with the switch marker) and arguments (in order)");
                doxx.add("E.g.");
                doxx.add("   r. :=" + getName() + "(['-v', '-foo','bar',5, 'arf'])");
                doxx.add("   r.");
                doxx.add("[5,arf]~{-foo:bar, -v : true}");
                doxx.add("Which lets you easily manage command line switches to your program");
                doxx.add("Typical operations:");
                doxx.add("   r.(-1); // last argument");
                doxx.add("arf");
                doxx.add("   r.'-woof' == 'arf'?3:4; // check value of a switch");
                doxx.add("3");
                doxx.add("   has_key('-pi',r.)?r.'-pi':pi(); // check if a switch was passed");
                doxx.add("3.141592658979");
                doxx.add("Notes: * switches without arguments are given a default value of true, e.g. -v above.");
                doxx.add("       * switches with no arguments should be grouped at the end of the line so they are not");
                doxx.add("         given the next argument as their value, or followed immediately by other switches.");
            }
            return doxx;
        }
    }
}
