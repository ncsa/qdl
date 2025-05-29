package org.qdl_lang.extensions.inputLine;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;
import net.sf.json.JSONObject;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.QDLValue;

import java.util.ArrayList;
import java.util.List;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            QDLStem out = new QDLStem();
            QDLStem args = null;
            String marker = DEFAULT_SWITCH_MARKER;
            QDLStem flags = null;
            QDLValue[] scriptArgs = null;

            if (qdlValues.length == 0) {
                if (!state.hasScriptArgs()) {
                    return asQDLValue(out);
                }
                scriptArgs = QDLValue.castToQDLValues(state.getScriptArgs());
                flags = new QDLStem();
            }
            if (qdlValues.length == 1) {
                // two cases, argument is a stem or argument is a switch marker
                if (qdlValues[0].isString()) {
                    if (!state.hasScriptArgs()) {
                        return asQDLValue(out);
                    }
                    marker = qdlValues[0].asString();
                    scriptArgs = QDLValue.castToQDLValues(state.getScriptArgs());
                } else {
                    if (qdlValues[0].isStem()) {
                        args = qdlValues[0].asStem();
                    } else {
                        throw new BadArgException(getName() + ": requires a stem or string as its argument", 0);
                    }
                    scriptArgs = QDLValue.castToQDLValues(getObjects(args));
                    flags = new QDLStem();
                }
            }
            if (qdlValues.length == 2) {
                // then the arguments are a stem and switch marker
                if (qdlValues[0].isStem()) {
                    args = qdlValues[0].asStem();
                    if (!args.isList()) {
                        throw new BadArgException(getName() + " requires a list as its first argument", 0);
                    }
                } else {
                    throw new BadArgException(getName() + " requires a list as its first argument", 0);
                }
                if (qdlValues[1].isString()) {
                    marker = qdlValues[1].asString();
                } else {
                    if (qdlValues[1].isStem()) {
                        flags = qdlValues[1].asStem();
                        if (!flags.isList()) {
                            throw new BadArgException(getName() + " requires a list as its second argument",1);
                        }
                    } else {
                        throw new BadArgException(getName() + " requires a string or list as its second argument",1);
                    }
                }
                scriptArgs = QDLValue.castToQDLValues(getObjects(args));
            }
            if (qdlValues.length == 3) {
                if (qdlValues[0].isStem()) {
                    args = qdlValues[0].asStem();
                    if (!args.isList()) {
                        throw new BadArgException(getName() + " requires a list as its first argument",0);
                    }
                } else {
                    throw new BadArgException(getName() + " requires a list as its first argument",0);
                }
                if (qdlValues[1].isString()) {
                    marker = qdlValues[1].asString();
                } else {
                    throw new BadArgException(getName() + " requires a string as its second argument",1);
                }
                if (qdlValues[2].isStem()) {
                    flags = qdlValues[2].asStem();
                    if (!flags.isList()) {
                        throw new BadArgException(getName() + " requires a list as its third argument",2);
                    }
                } else {
                    throw new BadArgException(getName() + " requires a list as its third argument",2);
                }
            }
            List<QDLValue> flagList = flags.getQDLList().values();
            List<QDLValue> foundFlags = new ArrayList<>();
            for (int i = 0; i < scriptArgs.length; i++) {
                QDLValue next = scriptArgs[i];
                if (next.isString() && next.asString().startsWith(marker)) {
                    //String nextArg = next.asString();
                    if (flagList.contains(next)) {
                        foundFlags.add(next);
                        out.put(next.asString(), BooleanValue.True);
                        continue;
                    }
                    if (i + 1 < scriptArgs.length) {
                        QDLValue nextNext = scriptArgs[i + 1];
                        out.put(next.asString(), nextNext);
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
                    out.putLongOrString(ff, BooleanValue.False);
                }
            }

            return asQDLValue(out);
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
