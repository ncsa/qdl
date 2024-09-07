package org.qdl_lang.extensions.inputLine;

import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/28/23 at  7:53 AM
 */
public class QDLCLITools implements QDLMetaModule {
    public static String TO_STEM_NAME = "to_stem";
    public static String DEFAULT_SWITCH_MARKER = "-";

    public class ToStem implements QDLFunction {
        @Override
        public String getName() {
            return TO_STEM_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1, 2, 3};
        }

        /*
              module_import(module_load(info().lib.cli, 'java'))
            cli#to_stem(['-foo','bar','-woof','arf',3,5])
            cli#to_stem(['-foo','bar','-v','-no_print', '-woof','arf',3,5], ['-v','-iso8601','-no_print'])

              has_keys('-woof', r.)
                has_keys('-foo', r.)
            ['my_key.pem', '-user', 'bob', '-version', '1.2', '-v', '-format', 'iso8601', '-in', 'input.txt', 'output.txt']
            ['-v','-use_ssl]
            to_stem(['my_key.pem', '--help', '-user', 'bob', '-version', '1.2', '-v', '-format', 'iso8601', '-in', 'input.txt', 'output.txt'],['-v','-use_ssl', '--help'])
         */
        @Override
        public Object evaluate(Object[] objects, State state) {
            QDLStem out = new QDLStem();
            QDLStem args = null;
            String marker = DEFAULT_SWITCH_MARKER;
            QDLStem flags = null;
            Object[] aa = null;

            if (objects.length == 0) {
                if (!state.hasScriptArgs()) {
                    return out;
                }
                aa = state.getScriptArgs();
                flags = new QDLStem();
            }
            if (objects.length == 1) {
                // two cases, argument is a stem or argument is a switch marker
                if (objects[0] instanceof String) {
                    if (!state.hasScriptArgs()) {
                        return out;
                    }
                    marker = (String) objects[0];
                    aa = state.getScriptArgs();
                } else {
                    if (objects[0] instanceof QDLStem) {
                        args = (QDLStem) objects[0];
                    } else {
                        throw new IllegalArgumentException(getName() + ": requires a stem or string as its argument");
                    }
                    aa = getObjects(args);
                    flags = new QDLStem();
                }
            }
            if (objects.length == 2) {
                // then the arguments are a stem and switch marker
                if (objects[0] instanceof QDLStem) {
                    args = (QDLStem) objects[0];
                    if (!args.isList()) {
                        throw new IllegalArgumentException(getName() + " requires a list as its first argument");
                    }
                } else {
                    throw new IllegalArgumentException(getName() + " requires a list as its first argument");
                }
                if (objects[1] instanceof String) {
                    marker = (String) objects[1];
                } else {
                    if (objects[1] instanceof QDLStem) {
                        flags = (QDLStem) objects[1];
                        if (!flags.isList()) {
                            throw new IllegalArgumentException(getName() + " requires a list as its second argument");
                        }
                    } else {
                        throw new IllegalArgumentException(getName() + " requires a string or list as its second argument");
                    }
                }
                aa = getObjects(args);
            }
            if (objects.length == 3) {
                if (objects[0] instanceof QDLStem) {
                    args = (QDLStem) objects[0];
                    if (!args.isList()) {
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
                if (objects[2] instanceof QDLStem) {
                    flags = (QDLStem) objects[2];
                    if (!flags.isList()) {
                        throw new IllegalArgumentException(getName() + " requires a list as its third argument");
                    }
                } else {
                    throw new IllegalArgumentException(getName() + " requires a list as its third argument");
                }
            }
            List<Object> flagList = flags.getQDLList().values();
            List<Object> foundFlags = new ArrayList<>();
            for (int i = 0; i < aa.length; i++) {
                Object next = aa[i];
                if (next instanceof String && next.toString().startsWith(marker)) {
                    String nextArg = (String) next;
                    if (flagList.contains(nextArg)) {
                        foundFlags.add(nextArg);
                        out.put(nextArg, Boolean.TRUE);
                        continue;
                    }
                    if (i + 1 < aa.length) {
                        Object nextNext = aa[i + 1];
/*
                        if (nextNext instanceof String && nextNext.toString().startsWith(marker)) {
                            // So the next entry is another switch
                            out.put(nextArg, Boolean.TRUE);
                        } else {
*/
                        out.put(nextArg, nextNext);
                        //}
                        i = i + 1; // skip to next
                    }
                } else {
                    // add to list in order
                    out.listAdd(next);
                }
            }
            // Now for final clean up.
            for (Object ff : flagList) {
                if (!foundFlags.contains(ff)) {
                    out.putLongOrString(ff, Boolean.FALSE);
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
                aa[index++] = inStem.getQDLList().get((Long) ooo);
            }
            return aa;
        }

        List<String> doxx = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> out = new ArrayList<>();
            switch (argCount) {
                case 0:
                    out.add(getName() + "() - use args() as the argument, default switch marker of " + DEFAULT_SWITCH_MARKER);
                    break;
                case 1:
                    out.add(getName() + "(args. | marker) - if a list, process that with the default switch marker");
                    out.add("          otherwise, use args() with the given switch marker");
                    break;
                case 2:
                    out.add(getName() + "(args., marker) - specify both the list of arguments and the switch marker");
                    out.add(getName() + "(marker, flags.) - use args(),  specify switch marker, list of flags");
                    out.add(getName() + "(args., flags.) - use list.,   default switch marker, have a list of flags");
                    break;
                case 3:
                    out.add(getName() + "(args., marker, flags.) - specify the list of arguments, switch marker and flags.");
                    break;
            }
            if (doxx.isEmpty()) {
                doxx.add("The result is a stem of switches (which start with the switch marker) and arguments (in order)");
                doxx.add("Definitions:");
                doxx.add("args. - a list of arguments to a program");
                doxx.add("marker - a string that starts every switch");
                doxx.add("flags. - a list of flags, i.e., switches with no argument");
                doxx.add("         The flags include the switch marker and may be anything.");
                doxx.add("E.g.");
                doxx.add("   r. := to_stem('-',['--help'])");
                doxx.add("Create a stem of switches, and treat --help as a specific flag, so it can be checked first.");
                doxx.add("E.g. Let us say that a script, foo.qdl, was invoked with the following ");
                doxx.add("\nfoo.qdl cert.pem -v -silent -woof arf -in input.txt -cfg /path/to/config.ini  output.txt\n");
                doxx.add("args() would return everything except foo.qdl as a list. To turn it into a stem");
                doxx.add("you would issue");
                doxx.add("   r. :=" + getName() + "('-',['-v','-silent', -iso8601])");
                doxx.add("   r.");
                doxx.add("[cert.pem, output.txt]~{-v:true, -woof:arf, -silent:true, -in:input.txt, -cfg:/path/to/config.ini, -iso8601:false}");
                doxx.add("The flags, if present, are given a value of true, any other switch is given the next argument as its value");
                doxx.add("\nTypical operations:");
                doxx.add("   r.(-1); // last argument");
                doxx.add("output.txt");
                doxx.add("   r.'-woof' == 'arf'?3:4; // check value of a switch");
                doxx.add("3");
                doxx.add("   has_key('-pi',r.)?r.'-pi':pi(); // check if a switch was passed");
                doxx.add("3.141592658979");
            }
            out.addAll(doxx);
            return out;
        }
    }

    @Override
    public JSONObject serializeToJSON() {
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {

    }
}
