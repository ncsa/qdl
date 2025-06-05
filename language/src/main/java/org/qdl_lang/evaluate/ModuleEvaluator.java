package org.qdl_lang.evaluate;

import org.qdl_lang.config.JavaModuleConfig;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.exceptions.IndexError;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.exceptions.WrongArgCountException;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.ModuleExpression;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.functions.DyadicFunctionReferenceNode;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.functions.FStack;
import org.qdl_lang.expressions.module.MTKey;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.expressions.module.QDLModule;
import org.qdl_lang.state.State;
import org.qdl_lang.state.StateUtils;
import org.qdl_lang.util.ModuleUtils;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.BooleanValue;
import org.qdl_lang.variables.values.LongValue;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.xml.SerializationState;
import net.sf.json.JSONArray;

import java.net.URI;
import java.util.*;

import static org.qdl_lang.config.QDLConfigurationConstants.MODULE_ATTR_VERSION_2_0;
import static org.qdl_lang.evaluate.SystemEvaluator.MODULE_TYPE_JAVA;
import static org.qdl_lang.variables.QDLStem.STEM_INDEX_MARKER;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.variables.values.QDLValue.getNullValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/21/23 at  6:42 AM
 */
public class ModuleEvaluator extends AbstractEvaluator {
    public static final String MODULE_NAMESPACE = "module";

    @Override
    public String getNamespace() {
        return MODULE_NAMESPACE;
    }

    public static final int MODULE_FUNCTION_BASE_VALUE = 12000;

    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{
                    ADD_LIB_ENTRIES,
                    LOAD,
                    DROP,
                    LOADED,
                    IMPORT,
                    USE,
                    RENAME,
                    GET_FUNCTIONS,
                    GET_VARIABLES,
                    GET_DOCUMENTATION,
                    JAVA_MODULE_LOAD,
                    JAVA_MODULE_USE
            };
        }
        return fNames;
    }


    @Override
    public int getType(String name) {
        switch (name) {
            case LOAD:
                return LOAD_TYPE;
            case IMPORT:
                return IMPORT_TYPE;
            case USE:
                return USE_TYPE;
            case RENAME:
                return RENAME_TYPE;
            case GET_FUNCTIONS:
                return GET_FUNCTIONS_TYPE;
            case GET_VARIABLES:
                return GET_VARIABLES_TYPE;
            case GET_DOCUMENTATION:
                return GET_DOCUMENTATION_TYPE;
            case LOADED:
                return LOADED_TYPE;
            case JAVA_MODULE_LOAD:
                return JAVA_MODULE_LOAD_TYPE;
            case JAVA_MODULE_USE:
                return JAVA_MODULE_USE_TYPE;
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }

    public static final String LOAD = "load";
    public static final int LOAD_TYPE = 1 + MODULE_FUNCTION_BASE_VALUE;
    public static final String IMPORT = "import";
    public static final int IMPORT_TYPE = 2 + MODULE_FUNCTION_BASE_VALUE;

    public static final String USE = "use";
    public static final int USE_TYPE = 3 + MODULE_FUNCTION_BASE_VALUE;

    public static final String DROP = "unload";
    public static final int DROP_TYPE = 4 + MODULE_FUNCTION_BASE_VALUE;
    public static final String RENAME = "rename";
    public static final int RENAME_TYPE = 4 + MODULE_FUNCTION_BASE_VALUE;

    public static final String GET_FUNCTIONS = "funcs";
    public static final int GET_FUNCTIONS_TYPE = 5 + MODULE_FUNCTION_BASE_VALUE;

    public static final String GET_VARIABLES = "vars";
    public static final int GET_VARIABLES_TYPE = 6 + MODULE_FUNCTION_BASE_VALUE;
    public static final String GET_DOCUMENTATION = "docs";
    public static final int GET_DOCUMENTATION_TYPE = 7 + MODULE_FUNCTION_BASE_VALUE;
    public static final String LOADED = "loaded";
    public static final int LOADED_TYPE = 8 + MODULE_FUNCTION_BASE_VALUE;

    public static final String JAVA_MODULE_LOAD = "j_load";
    public static final int JAVA_MODULE_LOAD_TYPE = 9 + MODULE_FUNCTION_BASE_VALUE;

    public static final String JAVA_MODULE_USE = "j_use";
    public static final int JAVA_MODULE_USE_TYPE = 10 + MODULE_FUNCTION_BASE_VALUE;

    public static final String ADD_LIB_ENTRIES = "lib_entries";
    public static final int ADD_LIB_ENTRIES_TYPE = 11 + MODULE_FUNCTION_BASE_VALUE;


    public boolean dispatch(Polyad polyad, State state) {
        switch (polyad.getName()) {
            case ADD_LIB_ENTRIES:
                doLibEntries(polyad, state);
                return true;
            case IMPORT:
                doImport(polyad, state);
                return true;
            case LOAD:
                doLoad(polyad, state);
                return true;
            case USE:
                doUse(polyad, state);
                return true;
            case RENAME:
                doRenameURIs(polyad, state);
                return true;
            case DROP:
                doUnload(polyad, state);
                return true;
            case GET_FUNCTIONS:
                doGetFunctions(polyad, state);
                return true;
            case GET_VARIABLES:
                doGetVariables(polyad, state);
                return true;
            case GET_DOCUMENTATION:
                doGetDocumentation(polyad, state);
                return true;
            case LOADED:
                doLoaded(polyad, state);
                return true;
            case JAVA_MODULE_LOAD:
                doJLoad(polyad, state, true);
                return true;
            case JAVA_MODULE_USE:
                doJLoad(polyad, state, false);
                return true;
        }
        return false;
    }

    /**
     * Exposes the {@link State#addLibEntries(String, QDLStem)} so the user can add them.
     *
     * @param polyad
     * @param state
     */
    private void doLibEntries(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
            QDLStem info = state.getSystemInfo();
            polyad.setResult(info.get("lib"));
            return;
        }
        if (polyad.getArgCount() == 1) {
            throw new WrongArgCountException(ADD_LIB_ENTRIES + " requires either no or two arguments", polyad.getArgAt(0));
        }
        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException(ADD_LIB_ENTRIES + " requires at most two arguments", polyad.getArgAt(2));
        }
        QDLValue x = polyad.evalArg(0, state);
        if (!x.isString()) {
            throw new BadArgException("the first argument of " + ADD_LIB_ENTRIES + " must be a (string) key", polyad.getArgAt(0));
        }
        String name = x.asString();
        x = polyad.evalArg(1, state);
        if (!x.isStem()) {
            throw new BadArgException("the first argument of " + ADD_LIB_ENTRIES + " must be a (string) key", polyad.getArgAt(0));
        }
        QDLStem stem = x.asStem();
        if (x.asStem().isList()) {
            throw new BadArgException("the second argument of " + ADD_LIB_ENTRIES + " must not be a list", polyad.getArgAt(0));
        }
        state.addLibEntries(name, stem);
        polyad.setEvaluated(true);
        QDLStem info = state.getSystemInfo();
        polyad.setResult(info.get("lib"));
    }

    /**
     * Rename loaded module(s). This accepts an old value (that is the unique URI for the loaded modules)
     * and chnages the name to the new value. Currently imported modules are not altered. <br/><br/>
     * Arguments are:
     * <ul>
     *
     * <li>(string, string)</li>
     * <li>stem., with stem.old_value := new_value</li>
     * <li>old_list., new_list.</li>
     * </ul>
     *
     * @param polyad
     * @param state
     */
    private void doRenameURIs(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }

        if (polyad.getArgCount() == 0) {
            throw new WrongArgCountException(RENAME + " requires an argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException(RENAME + " requires at most two arguments", polyad.getArgAt(2));
        }
        QDLStem renameStem;
        QDLValue arg0 = polyad.evalArg(0, state);
        boolean isScalarArg = false;
        if (arg0.isStem()) {
            renameStem = arg0.asStem();
        } else {
            if (polyad.getArgCount() != 2) {
                throw new BadArgException(RENAME + " requires two arguments or a stem", polyad.getArgAt(1));
            }
            QDLValue arg1 = polyad.evalArg(1, state);
            if (arg0.isString()) {
                if (arg1.isString()) {
                    renameStem = new QDLStem();
                    renameStem.put(arg0.asString(), asQDLValue(arg1));
                    isScalarArg = true;
                } else {
                    throw new BadArgException(RENAME + " requires a string as its second argument", polyad.getArgAt(1));
                }
            } else {
                if (isStemList(arg0)) {
                    if (isStemList(arg1)) {
                        renameStem = new QDLStem();
                        renameStem.setQDLList(arg0.asStem().getQDLList());
                        renameStem.renameKeys(arg1.asStem(), true);
                    } else {
                        throw new BadArgException(RENAME + " requires a list as its second argument", polyad.getArgAt(1));

                    }
                } else {
                    throw new BadArgException(RENAME + " illegal second argument", polyad.getArgAt(1));
                }
            }
        }
        QDLStem outStem = new QDLStem();
        for (QDLKey kk : renameStem.keySet()) {
            URI uriKey;
            try {
                uriKey = URI.create( kk.asString()); // by construction, all the keys are strings.
            } catch (Throwable t) {
                throw new BadArgException(RENAME + " requires a valid URI for the old module identifier", polyad.getArgAt(0));
            }
            URI newKey;
            QDLValue newValue = renameStem.get(kk);
            if (!newValue.isString()) {
                throw new BadArgException(RENAME + " requires the new module identifier be a string, not '" + newValue + "'", polyad.getArgAt(0));
            }
            try {
                newKey = URI.create(newValue.asString());
            } catch (Throwable t) {
                throw new BadArgException(LOADED + " requires a valid URI for the new module identifier", polyad.getArgAt(0));

            }
            MTKey oldMTKey = new MTKey(uriKey);
            Module m = state.getMTemplates().getModule(oldMTKey);
            if (m == null) {
                outStem.put(kk, asQDLValue(Boolean.FALSE));
            } else {
                state.getMTemplates().remove(oldMTKey);
                MTKey newMTKey = new MTKey(newKey);
                state.getMTemplates().put(newMTKey, m);
                outStem.put(kk, asQDLValue(Boolean.TRUE));
            }
        }
        polyad.setEvaluated(true);
        if (isScalarArg) {
            polyad.setResult(outStem.getString(arg0.asString()));
        } else {
            polyad.setResult(outStem);
        }
    }

    /*
        jload('http') =: http
    rename('qdl:/ext/math', 'qdl:/ext/math2')
     */

    /**
     * Takes no argument = return all URIs that are loaded. Or it may take a single argument.
     * result is conformable boolean result with true for loaded, false otherwise.
     *
     * @param polyad
     * @param state
     */
    private void doLoaded(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException(LOADED + " requires at most two arguments", polyad.getArgAt(2));
        }
        boolean isScalarArg = true;
        QDLStem inStem = null;
        if (polyad.getArgCount() == 1) {
            QDLValue object = polyad.evalArg(0, state);
            if (object.isStem()) {
                inStem = object.asStem();
                isScalarArg = false;
            } else {
                if (object.isString()) {
                    isScalarArg = true;
                    inStem = new QDLStem();
                    inStem.put(LongValue.Zero, asQDLValue(object));
                }
            }
        }
        if (inStem == null) {
            QDLStem outStem = new QDLStem();
            for (Object xkey : state.getMTemplates().allKeys()) {
                MTKey key = (MTKey) xkey;
                outStem.getQDLList().append(key.getUriKey().toString());
            }
            polyad.setEvaluated(true);
            polyad.setResult(outStem);
            return;
        }
        QDLStem outStem = new QDLStem();
        for (QDLKey k : inStem.keySet()) {
            URI uri;
            QDLValue object = inStem.get(k);
            if (!object.isString()) {
                throw new BadArgException(LOADED + " arguments must be strings", polyad.getArgAt(0));
            }
            try {
                uri = URI.create(object.asString());
                outStem.put(k, asQDLValue(state.getMTemplates().get(new MTKey(uri)) == null ? Boolean.FALSE : Boolean.TRUE));
            } catch (Throwable t) {
                throw new BadArgException(LOADED + " arguments must be valid URIs, not '" + object + "'", polyad.getArgAt(0));
            }
        }
        polyad.setEvaluated(true);
        if (isScalarArg) {
            polyad.setResult(outStem.get(0L));
        } else {
            polyad.setResult(outStem);
        }
    }

    /**
     * Utility for interrogatives to get the module from either the variable
     * or URI. This returns the module or throws an exception,
     *
     * @param polyad
     * @param state
     * @return
     */
    private Module getMorT(Polyad polyad, State state) {
        for (int i = 0; i < polyad.getArgCount(); i++) {
            polyad.evalArg(i, state);
        }
        //QDLValue obj = polyad.getArgAt(0).getResult();
        Object obj = polyad.getArgAt(0);
        if (obj instanceof ModuleExpression) {
            return ((ModuleExpression) obj).getModule();
        }
        Module m = null;
        if (obj instanceof DyadicFunctionReferenceNode) {
            return ((DyadicFunctionReferenceNode) obj).getModule();
        }
        if (Constant.isModule(obj)) {
            return (Module) obj;
        }
        if (Constant.isString(obj)) {
            URI uri;
            try {
                uri = URI.create((String) obj);
            } catch (Throwable t) {
                throw new BadArgException("The argument must be the URI of a module:'" + t.getMessage() + "'", polyad.getArgAt(0));
            }
            return state.getMTemplates().getModule(new MTKey(uri));

        }
        throw new BadArgException("The argument must be a module", polyad.getArgAt(0));

    }

    private void doGetDocumentation(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (0 == polyad.getArgCount()) {
            throw new WrongArgCountException(GET_DOCUMENTATION + " requires an arguments", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new WrongArgCountException(GET_DOCUMENTATION + " requires at most one arguments", polyad.getArgAt(1));
        }
        QDLValue output = polyad.evalArg(0, state);
        QDLStem outStem;
        switch (output.getType()){
            case Constant.MODULE_TYPE:
                outStem =getDoc(output.asModule());
                break;
            case Constant.DYADIC_FUNCTION_TYPE:
                outStem =getDoc(output.asDyadicFunction());
                break;
            case Constant.STEM_TYPE:
                outStem = getDoc(output.asStem());
                break;
            default:
                throw new BadArgException("wrong argument type, must be a stem, module or function reference", polyad.getArgAt(0));
        }
        polyad.setEvaluated(true);
        polyad.setResult(outStem);
    }

    protected QDLStem getDoc(Module m) {
        QDLList outList = new QDLList();
        outList.addAll(m.getDocumentation());
        QDLStem outStem = new QDLStem();
        outStem.setQDLList(outList);
        return outStem;
    }

    protected QDLStem getDoc(DyadicFunctionReferenceNode df) {
        QDLList outList = new QDLList();
        outList.addAll(df.getModule().getState().getFTStack().getDocumentation(new FKey(df.getFunctionName(), df.getFunctionArgCount())));
        QDLStem outStem = new QDLStem();
        outStem.setQDLList(outList);
        return outStem;
    }
     protected QDLStem getDoc(QDLStem inStem){
        QDLStem out = new QDLStem();
        for(QDLKey key : inStem.keySet()){
            QDLValue v = inStem.get(key);
             switch (v.getType()){
                 case Constant.STEM_TYPE:
                     out.put(key, getDoc(v.asStem()));
                     break;
                     case Constant.MODULE_TYPE:
                         out.put(key, getDoc(v.asModule()));
                         break;
                 case Constant.DYADIC_FUNCTION_TYPE:
                     out.put(key, getDoc(v.asDyadicFunction()));
                     break;
                 default:
                     out.put(key, v); // don't touch it
             }
        }
        return out;
     }
    /*
      module['a:a'][ff(x)->x^2;gg(x)->x^3;]
  A := import('a:a')
      A#1@ff
  [4,5]∂funcs(A)
1@ff
  docs(A#1@ff)


   c := j_load('convert');
funcs(c)  ;
docs(c#2@ini_out) ;

     */
    private void doGetVariables(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            QDLStem qdlStem = new QDLStem();
            qdlStem.getQDLList().addAll(state.getVStack().listVariables());
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            return;
        }
        if (3 < polyad.getArgCount()) {
            throw new WrongArgCountException(GET_VARIABLES + " requires at most 3 arguments", polyad.getArgAt(3));
        }
        QDLValue arg0 = polyad.evalArg(0, state);

        if (arg0.isNull()) {
            String regex = null;
            if (polyad.getArgCount() == 2) {
                QDLValue arg1 = polyad.evalArg(1, state);
                if (!arg1.isString()) {
                    throw new BadArgException(GET_VARIABLES + " requires a string regexc as its second argument if the first is null", polyad.getArgAt(1));
                }
                regex = arg1.asString();
            }
            QDLStem qdlStem = new QDLStem();
            TreeSet<String> v = state.getVStack().listVariables();
            if (regex == null) {
                qdlStem.getQDLList().addAll(state.getVStack().listVariables());
            } else {
                TreeSet<String> out = new TreeSet<>();
                for (String x : v) {
                    if (x.matches(regex)) {
                        out.add(x);
                    }
                }
                qdlStem.getQDLList().addAll(out);
            }
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            return;

        }
        Module m = getMorT(polyad, state);
        if (m == null || m.getState() == null) {
            throw new BadArgException(GET_VARIABLES + " cannot find the module", polyad.getArgAt(0));
        }
        String regex = null;
        if (polyad.getArgCount() == 2) {
            QDLValue arg2 = polyad.evalArg(1, state);
            if (arg2.isString()) {
                regex = arg2.asString();
            } else {
                throw new BadArgException(GET_VARIABLES + " second argument must be a string if present", polyad.getArgAt(1));
            }
        }

        QDLList outList = new QDLList();
        TreeSet<String> vars = m.getState().getVStack().listVariables();
        if (regex == null) {
            outList.addAll(vars);
        } else {
            TreeSet<String> out = new TreeSet<>();
            for (String x : vars) {
                if (x.matches(regex)) {
                    out.add(x);
                }
            }
            outList.addAll(out);
        }
        QDLStem outStem = new QDLStem();
        outStem.setQDLList(outList);
        polyad.setEvaluated(true);
        polyad.setResult(outStem);
    }

    /**
     * Contract is
     * <ul>
     *     <li>no arg = full set of ambient state</li>
     *     <li>null = full set of ambient state</li>
     *     <li>null, regex = full set from ambient state w/ regex applied</li>
     *     <li>variable or string of module URI = all functions in module </li>
     *     <li>variable or string of module URI, regex = all functions in module with regex applied </li>
     * </ul>
     *
     * @param polyad
     * @param state
     */
    private void doGetFunctions(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            QDLStem qdlStem = new QDLStem();
            qdlStem.getQDLList().addAll(state.getFTStack().listFunctionReferences(null));
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            return;
        }
        if (3 < polyad.getArgCount()) {
            throw new WrongArgCountException(GET_FUNCTIONS + " requires at most 3 arguments", polyad.getArgAt(3));
        }
        QDLValue arg0 = polyad.evalArg(0, state);

        if (arg0.isNull()) {
            String regex = null;
            if (polyad.getArgCount() == 2) {
                QDLValue arg1 = polyad.evalArg(1, state);
                if (!arg1.isString()) {
                    throw new BadArgException(GET_FUNCTIONS + " requires a string regexc as its second argument if the first is null", polyad.getArgAt(1));
                }
                regex = arg1.asString();
            }
            QDLStem qdlStem = new QDLStem();
            qdlStem.getQDLList().addAll(state.getFTStack().listFunctions(regex));
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            return;
        }
        Module m = getMorT(polyad, state);
        if (m == null || m.getState() == null) {
            // we found the template they gave, but templates do not have state, hence we cannot
            // list their functions.
            throw new BadArgException(GET_FUNCTIONS + " cannot find the module", polyad.getArgAt(0));
        }
        String regex = null;
        if (polyad.getArgCount() == 2) {
            QDLValue arg2 = polyad.evalArg(1, state);
            if (arg2.isString()) {
                regex = arg2.asString();
            } else {
                throw new BadArgException(GET_FUNCTIONS + " second argument must be a string if present", polyad.getArgAt(1));
            }
        }
        QDLList outList = new QDLList();

        //TreeSet<String> funcs = m.getState().getFTStack().listFunctions(regex);
        Set<DyadicFunctionReferenceNode> funcs = m.getState().getFTStack().listFunctionReferences(regex);
        for (DyadicFunctionReferenceNode ddd : funcs) {
            ddd.setModule(m);
            ddd.setModuleState(m.getState());

        }
        outList.addAll(funcs);
        QDLStem outStem = new QDLStem();
        outStem.setQDLList(outList);
        polyad.setEvaluated(true);
        polyad.setResult(outStem);
    }

    /*
  module['a:a'][ff(x)->x^2;gg(x)->x^3;]
    A := import('a:a')
    [4,5]∂funcs(A)
     */

    /**
     * Drop i.e., remove a loaded template from the system. This returns a list of uris that were
     * removed.<br/><br/>
     * Contract is
     * <ul>
     *     <li>string</li>
     *     <li>list.</li>
     *     <li>stem</li>
     * </ul>
     * result is conformable to argument.
     *
     * @param polyad
     * @param state
     */
    private void doUnload(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new WrongArgCountException(DROP + " requires an argument", polyad);
        }
        if (1 < polyad.getArgCount()) {
            throw new WrongArgCountException(DROP + " requires at most one argument", polyad);
        }
        QDLValue arg0 = polyad.evalArg(0, state);
        boolean isScalarArg = false;
        QDLStem inStem;
        if (arg0.isString()) {
            inStem = new QDLStem();
            inStem.put(LongValue.Zero, asQDLValue(arg0));
            isScalarArg = true;
        } else {
            if (arg0.isStem()) {
                inStem = arg0.asStem();
            } else {
                throw new BadArgException(DROP + " requires a string or stem as its argument", polyad.getArgAt(0));
            }
        }
        QDLStem outStem = new QDLStem();
        for (QDLKey kk : inStem.keySet()) {
            QDLValue value = inStem.get(kk);
            if (!value.isString()) {
                throw new BadArgException(DROP + " requires a string as its argument, not '" + value + "'", polyad.getArgAt(0));
            }
            URI uriKey;
            try {
                uriKey = URI.create( value.asString());
            } catch (Throwable t) {
                throw new BadArgException(DROP + " requires a valid URI as its argument, not '" + value + "'", polyad.getArgAt(0));
            }
            MTKey oldMTKey = new MTKey(uriKey);
            Module m = state.getMTemplates().getModule(oldMTKey);
            if (m == null) {
                outStem.put(kk, BooleanValue.True);
            } else {
                try {
                    state.getMTemplates().remove(oldMTKey);
                    outStem.put(kk, BooleanValue.True);
                } catch (Throwable t) {
                    outStem.put(kk, BooleanValue.False);
                }

            }
        }
        polyad.setEvaluated(true);
        if (isScalarArg) {
            polyad.setResult(outStem.get(LongValue.Zero));
        } else {
            polyad.setResult(outStem);
        }
    }

    protected void doUse(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new WrongArgCountException(USE + " requires an argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException(USE + " requires at most two arguments", polyad.getArgAt(2));
        }
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0), state);
        if (arg.isNull()) {
            // case that user tries to import a QDL null module. This can happen is
            // the load fails. Don't have it as an error, return QDL null to show nothing
            // happened (so user can check with conditional).
            polyad.setResult(QDLNull.getInstance());
            polyad.setEvaluated(true);
            return;
        }
        if (!arg.isString()) {
            throw new BadArgException(USE + " requires a string as its argument", polyad.getArgAt(0));
        }
        URI moduleNS;
        try {
            moduleNS = URI.create(arg.asString());
        } catch (Throwable t) {
            throw new BadArgException(USE + " requires a valid URI as its argument:'" + t.getMessage() + "'", polyad.getArgAt(0));
        }
        Module m = state.getMTemplates().getModule(new MTKey(moduleNS));
        if (m == null) {
            throw new BadArgException(" the module '" + moduleNS + "' has not been loaded", polyad.getArgAt(0));
        }
        m = m.newInstance(state);
        // Now here is where we diverge from import. We add everything in the module's state to the ambient state
        State moduleState = m.getState();
        state.getVStack().appendTables(moduleState.getVStack());
        state.getFTStack().appendTables(moduleState.getFTStack());
        m.setUsed(true);
        state.getUsedModules().put(m.getNamespace(), m);
        polyad.setEvaluated(true);
        polyad.setResult(Boolean.TRUE);
    }
    //module['A:X'][f(x)->x;y:='foo';];

    /**
     * Contract is that the type as an explicit argument is no longer required.
     * Assumption is that a file is
     * being loaded and if that fails, then the request is for a Java class.
     * This accepts a single value or stem of them.
     *
     * @param polyad
     * @param state
     */
    protected void doLoad(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new WrongArgCountException(LOAD + " requires an argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException(LOAD + " requires at most two arguments", polyad.getArgAt(2));
        }
        QDLValue arg = polyad.evalArg(0, state);

        if (arg.isNull()) {
            polyad.setResult(getNullValue());
            polyad.setEvaluated(true);
            return;
        }
        ModuleUtils moduleUtils = new ModuleUtils();
        QDLStem argStem = moduleUtils.convertArgsToStem(polyad, arg, state, LOAD);
        final int LOAD_UNKNOWN = -1;
        final int LOAD_FILE = 0;
        final int LOAD_JAVA = 1;
        QDLStem outStem = new QDLStem();
        for (QDLKey key : argStem.keySet()) {
            QDLValue value = argStem.get(key);
            int loadTarget = LOAD_UNKNOWN;
            String resourceName = null;
            if (value.isString()) {
                resourceName = value.asString();
            } else {
                QDLStem q = value.asStem();
                resourceName = q.get(LongValue.Zero).toString();
                loadTarget = q.get(LongValue.One).toString().equals(MODULE_TYPE_JAVA) ? LOAD_JAVA : LOAD_FILE;

            }
            if (resourceName.endsWith(".qdl") || resourceName.endsWith(".mdl")) {
                loadTarget = LOAD_FILE;
            }
            List<String> loadedQNames = null;
            // if they force the issue, do that and fail
            JavaModuleConfig jmc = new JavaModuleConfig();
            jmc.setImportOnStart(false);
            jmc.setUse(false);
            jmc.setVersion(MODULE_ATTR_VERSION_2_0);
            switch (loadTarget) {
                case LOAD_JAVA:
                    loadedQNames = moduleUtils.doJavaModuleLoad(state, resourceName, jmc);
                    break;
                case LOAD_FILE:
                    loadedQNames = moduleUtils.doQDLModuleLoad(state, resourceName);
                    break;
                case LOAD_UNKNOWN:
                    try {
                        loadedQNames = moduleUtils.doQDLModuleLoad(state, resourceName);
                    } catch (Throwable t) {
                        loadedQNames = moduleUtils.doJavaModuleLoad(state, resourceName, jmc);
                    }
                    break;
            }
            Object newEntry = null;
            if (loadedQNames == null || loadedQNames.isEmpty()) {
                newEntry = QDLNull.getInstance();
            } else {
                if (loadedQNames.size() == 1) {
                    newEntry = loadedQNames.get(0);
                } else {
                    QDLStem innerStem = new QDLStem();
                    innerStem.addList(loadedQNames);
                    newEntry = innerStem;
                }
            }
            outStem.put(key, newEntry);
        }
        polyad.setEvaluated(true);
        if (outStem.size() == 1) {
            polyad.setResult(outStem.get(outStem.keySet().iterator().next()));
        } else {
            polyad.setResult(outStem);
        }
    }

    /**
     * Bais contract is
     * <ul>
     *     <li>URI - import loaded module</li>
     *     <li>URI, state - import, using state that is inherited, none, extended</li>
     * </ul>
     * The state means that
     * <ul>
     *     <li>inherited - uses active live ambient state</li>
     *     <li>extended - snapshot of current state</li>
     *     <li>none - clean state, so nothing from ambient state</li>
     * </ul>
     * default is <b>none</b>.
     *
     * @param polyad
     * @param state
     */
    /**
     * Imported module has completey clean state, nothing from ambient state
     */
    public final static String IMPORT_STATE_NONE = "none";
    /**
     * Imported module gets snapshot of current ambient state, but is independent of it.
     */
    public final static String IMPORT_STATE_SNAPSHOT = "inherit";
    /**
     * Imported module shares ambient state. Any changes to ambient state are reflected in module's state.
     */

    public final static String IMPORT_STATE_SHARE = "share";
    public final static String IMPORT_STATE_ANY = "any"; // used in module definition
    /**
     * Used if no state is shared, i.e. completely clean state in the module (default)
     */
    public final static int IMPORT_STATE_NONE_VALUE = 100;
    /**
     * Am,bient state cloned and then added to module state.
     */
    public final static int IMPORT_STATE_SNAPSHOT_VALUE = 101;
    /**
     * Ambient state used by module. Note that this causes serialization problems potentially...
     */
    public final static int IMPORT_STATE_SHARE_VALUE = 102;
    public final static int IMPORT_STATE_ANY_VALUE = 110;

    public static String getInheritanceMode(int x) {
        switch (x) {

            case IMPORT_STATE_SHARE_VALUE:
                return IMPORT_STATE_SHARE;
            case IMPORT_STATE_SNAPSHOT_VALUE:
                return IMPORT_STATE_SNAPSHOT;
            case IMPORT_STATE_ANY_VALUE:
                return IMPORT_STATE_ANY;
            default:
            case IMPORT_STATE_NONE_VALUE:
                return IMPORT_STATE_NONE;
        }
    }

    public static int getInheritanceMode(String x) {
        switch (x) {
            default:
            case IMPORT_STATE_NONE:
                return IMPORT_STATE_NONE_VALUE;
            case IMPORT_STATE_ANY:
                return IMPORT_STATE_ANY_VALUE;
            case IMPORT_STATE_SHARE:
                return IMPORT_STATE_SHARE_VALUE;
            case IMPORT_STATE_SNAPSHOT:
                return IMPORT_STATE_SNAPSHOT_VALUE;
        }

    }

    /**
     * Eithjer
     * <ul>
     *     <li>import(string) - namespace</li>
     *     <li>import(string, mode) - mode is a string or integer</li>
     * </ul>
     * returns the instance of the module.
     *
     * @param polyad
     * @param state
     */
    protected void doImport(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new WrongArgCountException(IMPORT + " requires an argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException(IMPORT + " requires at most two arguments", polyad.getArgAt(2));
        }
        // Basic is to import a single module
        QDLValue arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0), state);
        if (arg.isNull()) {
            // case that user tries to import a QDL null module. This can happen is
            // the load fails. Don't have it as an error, return QDL null to show nothing
            // happened (so user can check with conditional).
            polyad.setResult(getNullValue());
            polyad.setEvaluated(true);
            return;
        }
        int inhertianceMode = IMPORT_STATE_NONE_VALUE;
        if (polyad.getArgCount() == 2) {
            QDLValue arg1 = polyad.evalArg(1, state);
            if (arg1.isString()) {
                String a = arg1.asString();
                switch (a) {
                    case IMPORT_STATE_NONE:
                        inhertianceMode = IMPORT_STATE_NONE_VALUE;
                        break;
                    case IMPORT_STATE_ANY: // probably means the module was imported by the old template system
                    case IMPORT_STATE_SHARE:
                        inhertianceMode = IMPORT_STATE_SHARE_VALUE;
                        break;
                    case IMPORT_STATE_SNAPSHOT:
                        inhertianceMode = IMPORT_STATE_SNAPSHOT_VALUE;
                        break;
                    default:
                        throw new BadArgException(IMPORT + " unknown state inheritance mode '" + arg1 + "'", polyad.getArgAt(1));
                }
            } else {
                if (arg1.isLong()) {
                    inhertianceMode = arg1.asLong().intValue();
                } else {
                    throw new BadArgException(IMPORT + " unknown state inheritance mode '" + arg1 + "'", polyad.getArgAt(1));
                }
            }
        }
        if (!arg.isString()) {
            throw new BadArgException(IMPORT + " requires a string as its argument", polyad.getArgAt(0));
        }
        URI moduleNS;
        try {
            moduleNS = URI.create(arg.asString());
        } catch (Throwable t) {
            throw new BadArgException(IMPORT + " requires a valid URI as its argument:'" + t.getMessage() + "'", polyad.getArgAt(0));
        }
        Module template = state.getMTemplates().getModule(new MTKey(moduleNS));
        if (template == null) {
            throw new BadArgException(" the module '" + moduleNS + "' has not been loaded", polyad.getArgAt(0));
        }
        State newState;
        try {
            switch (inhertianceMode) {
                case IMPORT_STATE_NONE_VALUE:
                    newState = state.newCleanState();
                    break;
                case IMPORT_STATE_SNAPSHOT_VALUE:
                    newState = StateUtils.clone(state);
                    // put tables and such in the right place so ambient state is not altered.
                    newState = newState.newSelectiveState(newState, false, true, true, true);
                    break;
                case IMPORT_STATE_SHARE_VALUE:
                    newState = state;
                    // put tables and such in the right place so ambient state is not altered.
                    // The next command creates a state with the inhereited functions and variables.

                    newState = newState.newSelectiveState(newState, false, true, true, true);
                    // now since these are shared, push on new tables to ensure nothing in the ambient state gets overwritten.
                    // We do not want moduile state to leak back into the ambient state.
                    newState.getVStack().pushNewTable();
                    newState.getFTStack().pushNewTable();
                    break;
                default:
                    throw new BadArgException(IMPORT + " with unknown state inheritene mode", polyad.getArgAt(1));
            }
            Module module = template.newInstance(newState);
            if(template instanceof QDLModule) {
                ((QDLModule)module).setFilePath(((QDLModule)template).getFilePath());
            }
            module.setInheritanceMode(inhertianceMode);
            newState.setModule(module);
            switch (inhertianceMode){
                case IMPORT_STATE_NONE_VALUE:
                case IMPORT_STATE_SNAPSHOT_VALUE:
                    newState.setIntrinsicVariables(cloneIntrinsicVariables(module.getState()));
                    newState.setIntrinsicFunctions(cloneIntrinsicFunctions(module.getState()));
                    break;
                    case IMPORT_STATE_SHARE_VALUE:
                        newState.setIntrinsicVariables(state.getIntrinsicVariables());
                        newState.setIntrinsicFunctions(state.getIntrinsicFunctions());
                        break;
            }
            module.setTemplate(false);
            module.setParentTemplateID(template.getId());
            if (state.hasModule()) {
                module.setParentInstanceID(state.getModule().getId());
                module.setParentInstanceAlias(state.getModule().getAlias());
            }
            polyad.setEvaluated(true);
            polyad.setResult(module);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new QDLExceptionWithTrace("there was an issue creating the state of the module:" + t.getMessage(), polyad);
        }

    }
protected VStack cloneIntrinsicVariables(State state) throws Throwable {
    SerializationState ss = new SerializationState(); // just need a non-null dummy
    if(state == null || state.getIntrinsicVariables() == null || state.getIntrinsicVariables().isEmpty()){
        return new VStack();
    }
    JSONArray iVars = state.getIntrinsicVariables().toJSON(ss);
    VStack iStack = new VStack();
    iStack.fromJSON(iVars, ss);
    return iStack;
}
    protected FStack cloneIntrinsicFunctions(State state) throws Throwable {
        SerializationState ss = new SerializationState(); // just need a non-null dummy
        if(state == null || state.getIntrinsicFunctions() == null || state.getIntrinsicFunctions().isEmpty()){
            return new FStack();
        }
        JSONArray iFuncs = state.getIntrinsicFunctions().toJSON(ss);
        FStack iStack = new FStack();
        iStack.fromJSON(iFuncs, ss);
        return iStack;
    }
    /**
     * This is just import(load(x, 'java')). It happens so much we need an idiom.
     * this will try to look up the argument in the system lib table, so you can do things like
     * <pre>
     *     jload('http')
     * http
     * </pre>
     * and get the entire module loaded.
     *
     * @param polyad
     * @param state
     */
    private void doJLoad(Polyad polyad, State state, boolean isLoad) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            throw new WrongArgCountException((isLoad ? JAVA_MODULE_LOAD : JAVA_MODULE_USE) + " requires an argument", polyad);
        }
        if (2 < polyad.getArgCount()) {
            throw new WrongArgCountException((isLoad ? JAVA_MODULE_LOAD : JAVA_MODULE_USE) + " requires at most two arguments", polyad.getArgAt(2));
        }
        QDLValue arg = polyad.evalArg(0, state);
        boolean hasMode = false;
        String mode = null;
        if (polyad.getArgCount() == 2) {
            QDLValue object = polyad.evalArg(1, state);
            if (!object.isString()) {
                throw new BadArgException((isLoad ? JAVA_MODULE_LOAD : JAVA_MODULE_USE) + " requires a string for the mode as its second argument if present", polyad.getArgAt(1));
            }
            mode = object.asString();
            hasMode = true;
        }
        String possibleName = null;
        if (arg.isStem()) {
            // allow for index stem, so [a,b,c] -> a.b.c -> lib.a.b.c for lookup
            QDLStem args = arg.asStem();
            if (!args.isList()) {
                throw new BadArgException((isLoad ? JAVA_MODULE_LOAD : JAVA_MODULE_USE) + " requires an index list as its argument if present", polyad.getArgAt(0));
            }
            QDLList list = args.getQDLList();
            // special case, there is one element. It is assumed that is in tools
            if (list.size() == 1) {
                QDLValue obj = state.getLibMap().get(list.get(0L));
                if (obj.isString()) {
                    // It is possible they mis-state the path and the result is a stem
                    // E.g. they give the path as 'oa2' assuming everything get loaded.
                    // That is not the contract for this function!
                    possibleName = obj.asString();
                }

            } else {
                List<String> xxx = new ArrayList<>(args.getQDLList().size());
                for(QDLValue qdlValue : args.getQDLList()){
                    xxx.add(qdlValue.asString());
                }
                possibleName = getClassPathFromToolPath(xxx, state);
            }
            if (possibleName == null) {
                // This means lookup failed.
                throw new IndexError("no such index found", polyad.getArgAt(0));
            }
        } else {
            // process it as a string
            if (!arg.isString()) {
                throw new BadArgException((isLoad ? JAVA_MODULE_LOAD : JAVA_MODULE_USE) + " requires an string or index list as its argument if present", polyad.getArgAt(0));
            }
            possibleName = arg.asString();
            // Meaning of next: if like .tools.oa2.woof, shave off leading .
            // if there is an embedded ., process that.
            possibleName = possibleName.indexOf(STEM_INDEX_MARKER) == 0 ? possibleName.substring(1) : possibleName;
            if (state.getLibMap().containsKey(possibleName)) { // look for it directly in tools
                possibleName = state.getLibMap().getString(possibleName);
            } else {
                // This looks in the extensions added to the lib element, e.g. oa2.woof in OA4MP
                // These can be defined in extensions to QDL and can be arbitrarily complex.
                // Do a path lookup
                if (0 < possibleName.indexOf(STEM_INDEX_MARKER)) {
                    StringTokenizer stringTokenizer = new StringTokenizer(possibleName, "."); // NOT the stem marker!
                    ArrayList<String> toolPath = new ArrayList<>();
                    while (stringTokenizer.hasMoreTokens()) {
                        toolPath.add(stringTokenizer.nextToken());
                    }
                    String cp = getClassPathFromToolPath(toolPath, state);
                    if (cp != null) {
                        possibleName = cp;
                    }
                }
            }
        }

        Polyad module_load = new Polyad(ModuleEvaluator.LOAD);
        module_load.addArgument(new ConstantNode(asQDLValue(possibleName)));
        module_load.addArgument(new ConstantNode(asQDLValue(MODULE_TYPE_JAVA)));
        module_load.evaluate(state);
        Polyad module_import = new Polyad(isLoad ? ModuleEvaluator.IMPORT : ModuleEvaluator.USE);
        module_import.addArgument(new ConstantNode(module_load.getResult()));
        if (hasMode) {
            module_import.addArgument(new ConstantNode(asQDLValue(mode)));
        }
        module_import.evaluate(state);
        polyad.setEvaluated(true);
        polyad.setResult(module_import.getResult());
    }

    protected String getClassPathFromToolPath(List<String> toolPath, State state) {
        String possibleCP = null;
        if (state == null) {
            // it is possible in a module there is no state
            // default to ambient state for resolving path
            state = State.getRootState();
        }
        QDLStem libStem = state.getSystemInfo().getStem("lib");
        try {
            for (int i = 0; i < toolPath.size() - 1; i++) {
                libStem = libStem.getStem(toolPath.get(i));
            }
            if (libStem.containsKey(toolPath.get(toolPath.size() - 1))) {
                possibleCP = libStem.getString(toolPath.get(toolPath.size() - 1));
            }
        } catch (Throwable t) {
            // ok, so parsing the path failed. This probably means they passed in the actual
            // full java path, so try to process what they sent.
        }
        return possibleCP;
    }
    /*
  module['A:X'][f(x)->x;s:='foo';];
  z := import('A:X')
  ww(e,s)->e#f(s)
  ww(z,3)

  z#f(3)
3
  z#y
foo

  module['A:Y'][module['A:X'][f(x)->x;y:='foo';];z:=import('A:X');];
  y := import('A:Y');
  y#z#f(3)

  w(z)->z^2
  module['A:Y'][f(x)->w(2*x);]
  import('A:Y') =: h
  h#f(3)
    ww(e,s)->e#f(s)
  ww(z,3)

  module['A:X'][g(x)->f(2*x);y:='foo';];
  f(x)->x^2;
  z := import('A:X')
   z#g(2)
   // test state in function calls

     module['A:X'][a:=3;f(x)->a*x;];
     z := import('A:X');
     a:=5;
     z#f(3)

     */
}

