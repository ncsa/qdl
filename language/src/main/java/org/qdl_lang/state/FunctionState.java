package org.qdl_lang.state;

import org.fife.ui.autocomplete.BasicCompletion;
import org.qdl_lang.evaluate.MetaEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.NamespaceException;
import org.qdl_lang.exceptions.QDLIllegalAccessException;
import org.qdl_lang.exceptions.UndefinedFunctionException;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.expressions.UserFunction;
import org.qdl_lang.expressions.module.MIStack;
import org.qdl_lang.expressions.module.MIWrapper;
import org.qdl_lang.expressions.module.MTStack;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.variables.VStack;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import org.qdl_lang.functions.*;

import java.util.*;


/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/2/20 at  6:48 AM
 */
public abstract class FunctionState extends VariableState {
    public FunctionState(VStack vStack,
                         OpEvaluator opEvaluator,
                         MetaEvaluator metaEvaluator,
                         FStack fStack,
                         MTStack mtStack,
                         MIStack miStack,
                         MyLoggingFacade myLoggingFacade) {
        super(
                vStack,
                opEvaluator,
                metaEvaluator,
                mtStack,
                miStack,
                myLoggingFacade);
        this.fStack = fStack;
    }

    private static final long serialVersionUID = 0xcafed00d4L;

    public void setFTStack(FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>> fStack) {
        this.fStack = fStack;
    }

    public FStack<? extends FTable> getFTStack() {
        return fStack;
    }

    /**
     * Adds a function to the correct stack (intrinsic, extrinsic or regular. To get the
     * function, call {@link #resolveFunction(Polyad, boolean)} or one of its variants.
     *
     * @param function
     */
    public void putFunction(FunctionRecordInterface function) {
        if (function.isExtrinsic() || isExtrinsic(function.getName())) {
            getExtrinsicFuncs().put(function);
        } else {
            if (isIntrinsic(function.getName())) {
                getIntrinsicFunctions().put(function);
            } else {
                getFTStack().put(function);
            }
        }
        if (hasCompletionProvider()) {
            getCompletionProvider().addCompletion(new BasicCompletion(getCompletionProvider(), function.getName() + "("));
        }
    }

    FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>> fStack = new FStack();

    /**
     * Convenience, just looks up name and arg count. If this is a {@link UserFunction}, then any existing
     * {@link FR_WithState} is just returned since the actual lookup is quite expensive.
     *
     * @param polyad - polyad
     * @param checkForDuplicates - check for duplicates across other modules
     * @return
     */
    public FR_WithState resolveFunction(Polyad polyad, boolean checkForDuplicates) {
        if(polyad instanceof UserFunction && (((UserFunction) polyad).hasFR_WithState())){
            return (FR_WithState) ((UserFunction) polyad).getFunctionRecord();
        }
        return resolveFunction(polyad.getName(), polyad.getArgCount(), checkForDuplicates);
    }

    public List<FR_WithState> getAllFunctionsByName(String name) throws CloneNotSupportedException {
        List<FR_WithState> list = new ArrayList<>();
        int nsDelimIndex = name.indexOf(NS_DELIMITER);
        if (nsDelimIndex == 0) {
            name = name.substring(1);
        }

        if (0 < nsDelimIndex) {
            // This is at least of the form x#y
            StringTokenizer st = new StringTokenizer(name, NS_DELIMITER);
            MIWrapper wrapper = (MIWrapper) getMInstances().get(new XKey(st.nextToken()));
            if (wrapper == null) {
                throw new QDLIllegalAccessException("module not found");
            }
            // We need the last token
            String lastName = st.nextToken();
            State currentState = wrapper.getModule().getState();
            while (st.hasMoreTokens()) {
                if (st.hasMoreTokens()) {
                    wrapper = (MIWrapper) currentState.getMInstances().get(new XKey(lastName));
                    if (wrapper == null) {
                        throw new QDLIllegalAccessException("module not found");
                    }
                    currentState = wrapper.getModule().getState();
                }
                lastName = st.nextToken();
            }
            if (wrapper != null) {
                for (FunctionRecordInterface fr : wrapper.getModule().getState().getFTStack().getByAllName(lastName)) {
                    // list.add(new FR_WithState(fr, wrapper.getModule().getState(), true));
                    list.add(new FR_WithState(fr.clone(), wrapper.getModule().getState(), true));
                }
            }
            return list;
        }   // just a name, look for all of them
        for (FunctionRecordInterface fr : getFTStack().getByAllName(name)) {
            list.add(new FR_WithState(fr.clone(), this, false));
            //list.add(new FR_WithState(fr, this, false));
        }

        for (Object key : getMInstances().keySet()) {
            MIWrapper wrapper = (MIWrapper) getMInstances().get((XKey) key);
            if (wrapper == null) {
                throw new QDLIllegalAccessException("module not found");
            }
            for (FunctionRecordInterface fr : wrapper.getModule().getState().getFTStack().getByAllName(name)) {
                list.add(new FR_WithState(fr.clone(), wrapper.getModule().getState(), true));
            }
        }
        return list;
    }

    /**
     * Takes the name and arg count and resolves across extrinsics, modules, intrinsics and returns the
     * {@link FR_WithState} with a pointer to this state object, since at the point of invocation, that is
     * the right one vis-a-vis scope considerations.
     * @param name - name of the function
     * @param argCount - number of arguments
     * @param checkForDuplicates - check for like-named functions in other modules
     * @return
     */
    public FR_WithState resolveFunction(String name, int argCount, boolean checkForDuplicates) {
        if (name == null || name.isEmpty()) {
            throw new NFWException(("Internal error: The function has not been named"));
        }
        if (isExtrinsic(name)) {
            FR_WithState frs = new FR_WithState();
            XThing xThing = null;
            FKey fKey = new FKey(name, argCount);
            if (getExtrinsicFuncs().containsKey(new FKey(name, -1))) {
                xThing = getExtrinsicFuncs().get(fKey);
                if (xThing == null) {
                    return null;
                }
                if (xThing instanceof FunctionRecord) {
                    frs.functionRecord = (FunctionRecord) xThing;
                } else {
                    if (xThing instanceof FR_WithState) {
                        frs = (FR_WithState) xThing;
                    } else {
                        frs.functionRecord = null;
                    }
                }
                return frs;
            } else {
                throw new UndefinedFunctionException("no extrinsic function named '" + name + "' with " + argCount + " argument" + (argCount == 1 ? "." : "s."), null);
            }
        }
        if (getMInstances().isEmpty()) {
            FR_WithState frs = new FR_WithState();

            // Nothing imported, so nothing to look through.
            frs.state = this;
            XThing xThing = null;
            FKey fKey = new FKey(name, argCount);
            if (getFTStack().containsKey(new FKey(name, -1))) {
                // xThing = getFTStack().get(fKey);
                xThing = getFTStack().get(fKey);
            } else {
                if (getIntrinsicFunctions().containsKey(new FKey(name, -1))) {
                    //xThing = getIntrinsicFunctions().get(fKey);
                    xThing = getIntrinsicFunctions().get(fKey);
                }
            }
            if (xThing != null) {
                // First case. Since cannot specify which arguments are in a function ref
                // E.g. f(@g, x, y) -> g(x)*g(x,y)
                // f(@-, 6, 3) yields (-6)*(6 - 3) == -18.
                // may have several other functions named g in the state, we have to
                // find the (in this case) monadic function or dyadic function
                // for this. Such arguments are stored with no arg count (so < 0)
                // The next bit gets that and, if this is a function ref with the right number
                // arguments, uses that.
/*
                if (xThing instanceof FunctionRecord) {
                    FunctionRecordInterface functionRecordInterface = (FunctionRecordInterface) xThing;
                }
*/
                if (xThing instanceof FunctionRecord) {
                    frs.functionRecord = (FunctionRecord) xThing;
                } else {
                    if (xThing instanceof FR_WithState) {
                        frs = (FR_WithState) xThing;
                    } else {
                        frs.functionRecord = null;
                    }
                }
            } else {
                frs.functionRecord = null;
            }
            return frs;
        }
        // check for unqualified names.
        FR_WithState fr_withState = new FR_WithState();
        XThing xThing = getFTStack().get(new FKey(name, argCount));
        if (xThing instanceof FunctionRecord) {
            fr_withState.functionRecord = (FunctionRecord) getFTStack().get(new FKey(name, argCount));
            fr_withState.state = this;
        } else {
            if (xThing instanceof FR_WithState) {
                fr_withState = (FR_WithState) xThing;
            }
        }
        // if there is an unqualified named function, return it.
        // Note that there cannot ever be an actual new definition of a function in a module
        // since QDLListener will flag FunctionDefinitionStatements as illegal.
        // So e.g.
        //    X#f(x,y)->x*y;
        // will fail.

        if (fr_withState.functionRecord != null) {
            return fr_withState;
        }
        // No UNQ function, so try to find one, but check that it is actually unique.
        if (!isIntrinsic(name)) {
            for (Object xx : getMInstances().keySet()) {
                XKey xkey = (XKey) xx;
                Module module = getMInstances().getModule(xkey);

                if (fr_withState.functionRecord == null) {
                    FunctionRecord tempFR = (FunctionRecord) getImportedModule(xkey.getKey()).getState().getFTStack().get(new FKey(name, argCount));
                    if (tempFR != null) {
                        fr_withState.functionRecord = tempFR;
                        fr_withState.state = module.getState();
                        fr_withState.isExternalModule = module.isExternal();
                        fr_withState.isModule = true;
                        if (!checkForDuplicates) {
                            return fr_withState;
                        }
                    }
                } else {
                    FunctionRecordInterface tempFR = (FunctionRecordInterface) module.getState().getFTStack().get(new FKey(name, argCount));
                    if ((checkForDuplicates) && tempFR != null) {
                        throw new NamespaceException("Error: There are multiple modules with a function named \"" + name + "\". You must fully qualify which one you want.");
                    }
                }
            }

        }
        if (fr_withState.functionRecord == null) {
            // edge case is that it is actually a built-in function reference.
            fr_withState.functionRecord = getFTStack().getFunctionReference(name);
            //fr.isExternalModule = false; // just to be sure.
            if (fr_withState.functionRecord == null) {
                throw new UndefinedFunctionException("no such function  " + name + "(" + argCount + ")" + (argCount == 1 ? "" : "s"), null);
            }
        }
        return fr_withState;
    }
     /*
             module_import(module_load('test'))
               module_import('a:/b', 'Y')

           X#u := 5
             X#f(2,3)

  module['a:/b','X'][u := 2;v := 3;times(x,y)->x*y;f(x,y)->times(x,u)+times(y,v);g()->u+v;];
  module_import('a:/b','X')
  module_import('a:/b','Y')
    X#u := 5;X#v := 7
  X#g()
      */


    /**
     * Lists the functions for various components.
     *
     * @param useCompactNotation
     * @param regex
     * @return
     */
    public TreeSet<String> listFunctions(boolean useCompactNotation, String regex,
                                         boolean includeModules,
                                         boolean showIntrinsic,
                                         boolean showExtrinsic) {
        HashSet<XKey> processedAliases = new HashSet<>();
        TreeSet<String> output = new TreeSet<>();
        if (showExtrinsic) {
            output = getExtrinsicFuncs().listFunctions(regex);
        }
        output.addAll(listFunctions(useCompactNotation, regex, includeModules, showIntrinsic, processedAliases));
        return output;
    }

    /**
     * Since multiple aliases may be imported, just stop at the first on in the stack
     * rather then trying to list all of them.
     *
     * @param useCompactNotation
     * @param regex
     * @param includeModules
     * @param showIntrinsic
     * @param processedAliases
     * @return
     */
    protected TreeSet<String> listFunctions(boolean useCompactNotation, String regex,
                                            boolean includeModules,
                                            boolean showIntrinsic,
                                            Set<XKey> processedAliases
    ) {
        TreeSet<String> out = getFTStack().listFunctions(regex);
        // no module templates, so no need to snoop through them
        if ((!includeModules) || getMTemplates().isEmpty()) {
            return out;
        }
        // Get the functions for active (current) module instances
        for (Object key : getMInstances().keySet()) {
            XKey xKey = (XKey) key;

            Module m = ((MIWrapper) getMInstances().get(xKey)).getModule();
            //        processedAliases.add(xKey);
            TreeSet<String> uqFuncs = m.getState().getFTStack().listFunctions(regex);
            extractFunctionList(useCompactNotation, uqFuncs, out, m);
            if (showIntrinsic && !m.getState().getIntrinsicFunctions().isEmpty()) {
                uqFuncs = m.getState().getIntrinsicFunctions().listFunctions(regex);
                extractFunctionList(useCompactNotation, uqFuncs, out, m);
            }

        }
        return out;
    }

    private void extractFunctionList(boolean useCompactNotation, TreeSet<String> uqFuncs, TreeSet<String> out, Module m) {
        for (String x : uqFuncs) {
            if (useCompactNotation) {
                out.add(getMInstances().getAliasesAsString(m.getMTKey()) + NS_DELIMITER + x);
            } else {
                for (Object alias : getMInstances().getAliasesAsString(m.getMTKey())) {
                    out.add(alias + NS_DELIMITER + x);
                }
            }
        }
    }

    public List<String> listAllDocumentation() {
        List<String> out = getFTStack().listAllDocs();
        for (Object kk : getMInstances().keySet()) {
            XKey xkey = (XKey) kk;
            MIWrapper wrapper = (MIWrapper) getMInstances().get(xkey);
            Module module = wrapper.getModule();

            List<String> docs = module.getState().getFTStack().listAllDocs();
            List<String> aliases = getMInstances().getAliasesAsString(module.getMTKey());
            for (String x : docs) {
                if (aliases.size() == 1) {
                    // don't put list notation in if there is no list.
                    out.add(aliases.get(0) + NS_DELIMITER + x);
                } else {
                    out.add(aliases + NS_DELIMITER + x);
                }
            }
        }
        return out;
    }

    /**
     * This list the documentation templates by their preferred alias
     *
     * @param fname
     * @return
     */
    public List<String> listModuleDoc(String fname) {
        if (!fname.contains(NS_DELIMITER)) {
            return new ArrayList<>(); // no help
        }
        String alias = fname.substring(0, fname.indexOf(NS_DELIMITER));
        XKey xKey = new XKey(alias);
        Module module = getMInstances().getModule(xKey);
        if (module == null) {
            return new ArrayList<>();

        }
        List<String> docs = module.getListByTag();
        if (docs == null) {
            return new ArrayList<>();
        }
        return docs;
    }

    public List<String> listFunctionDoc(String fname, int argCount) {
        if (fname.contains(NS_DELIMITER)) {
            String alias = fname.substring(0, fname.indexOf(NS_DELIMITER));
            XKey aliasKey = new XKey(alias);
            String realName = fname.substring(1 + fname.indexOf(NS_DELIMITER));
            if (alias == null || alias.isEmpty()) {
                List<String> out;
                if (argCount == -1) {
                    out = getFTStack().listAllDocs(realName);
                } else {
                    out = getFTStack().getDocumentation(new FKey(realName, argCount));
                }
                if (out == null) {
                    return new ArrayList<>();
                }
                return out;
            }
            if (!getMInstances().containsKey(aliasKey)) {
                // so they asked for something that didn't exist
                return new ArrayList<>();
            }
            MIWrapper wrapper = (MIWrapper) getMInstances().get(aliasKey);
            Module module = wrapper.getModule();

            List<String> docs;

            if (argCount == -1) {
                docs = module.getState().getFTStack().listAllDocs(realName);
            } else {
                docs = module.getState().getFTStack().getDocumentation(new FKey(realName, argCount));
            }
            if (docs == null) {
                return new ArrayList<>();
            }
            return docs;
            // Should only ever be exactly one since this is fully qualified
            // easy cases.

        }
        // No imports, not qualified, hand back whatever we have
        if (getMInstances().isEmpty()) {
            List<String> out;
            if (argCount == -1) {
                out = getFTStack().listAllDocs(fname);
            } else {
                out = getFTStack().getDocumentation(new FKey(fname, argCount));
            }
            if (out == null) {
                return new ArrayList<>();
            }
            return out;
        }
        // Final case, unqualified name and there are imports. Return all that match.
        List<String> out;
        if (argCount == -1) {
            out = getFTStack().listAllDocs(fname);
        } else {
            out = getFTStack().getDocumentation(new FKey(fname, argCount));
        }

        if (out == null) {
            out = new ArrayList<>();
        }
        for (Object key : getMInstances().keySet()) {
            XKey xKey = (XKey) key;
            Module m = getMInstances().getModule(xKey);
            if (m.getState().getFTStack().containsKey(new FKey(fname, argCount))) {
                List<String> doxx;
                if (argCount == -1) {
                    doxx = m.getState().getFTStack().listAllDocs(fname);
                } else {
                    doxx = m.getState().getFTStack().getDocumentation(new FKey(fname, argCount));
                }
                if (doxx == null) {
                    String caput = xKey.getKey() + NS_DELIMITER + fname;
                    if (0 <= argCount) {
                        caput = caput + "(" + argCount + "):";
                    }
                    out.add(caput + " none");
                } else {
                    out.addAll(doxx);
                }
            }
        }
        return out;
    }

    FStack intrinsicFunctions;

    public FStack getIntrinsicFunctions() {
        if (intrinsicFunctions == null) {
            intrinsicFunctions = new FStack();
        }
        return intrinsicFunctions;
    }

    public void setIntrinsicFunctions(FStack intrinsicFunctions) {
        this.intrinsicFunctions = intrinsicFunctions;
    }

    public FStack getExtrinsicFuncs() {
        return null;
    }
}
