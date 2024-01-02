package edu.uiuc.ncsa.qdl.evaluate;

import edu.uiuc.ncsa.qdl.config.JavaModuleConfig;
import edu.uiuc.ncsa.qdl.exceptions.BadArgException;
import edu.uiuc.ncsa.qdl.exceptions.QDLExceptionWithTrace;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.module.MTKey;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.StateUtils;
import edu.uiuc.ncsa.qdl.util.ModuleUtils;
import edu.uiuc.ncsa.qdl.variables.Constant;
import edu.uiuc.ncsa.qdl.variables.QDLList;
import edu.uiuc.ncsa.qdl.variables.QDLNull;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.xml.SerializationConstants;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import static edu.uiuc.ncsa.qdl.evaluate.SystemEvaluator.MODULE_TYPE_JAVA;
import static edu.uiuc.ncsa.qdl.variables.QDLStem.STEM_INDEX_MARKER;

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
                    LOAD,
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

    public static final String DROP = "drop";
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


    public boolean dispatch(Polyad polyad, State state) {
        switch (polyad.getName()) {
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
                doDrop(polyad, state);
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
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        QDLStem renameStem;
        Object arg0 = polyad.evalArg(0, state);
        boolean isScalarArg = false;
        if (isStem(arg0)) {
            renameStem = (QDLStem) arg0;
        } else {
            if (polyad.getArgCount() != 2) {
                throw new BadArgException(RENAME + " requires two arguments or a stem", polyad.getArgAt(1));
            }
            Object arg1 = polyad.evalArg(1, state);
            if (isString(arg0)) {
                if (isString(arg1)) {
                    renameStem = new QDLStem();
                    renameStem.put((String) arg0, arg1);
                    isScalarArg = true;
                } else {
                    throw new BadArgException(RENAME + " requires a string as its second argument", polyad.getArgAt(1));
                }
            } else {
                if (isStemList(arg0)) {
                    if (isStemList(arg1)) {
                        renameStem = new QDLStem();
                        renameStem.setQDLList((QDLList) arg0);
                        renameStem.renameKeys((QDLStem) arg1, true);
                    } else {
                        throw new BadArgException(RENAME + " requires a list as its second argument", polyad.getArgAt(1));

                    }
                } else {
                    throw new BadArgException(RENAME + " illegal second argument", polyad.getArgAt(1));
                }
            }
        }
        QDLStem outStem = new QDLStem();
        for (Object kk : renameStem.keySet()) {
            URI uriKey;
            try {
                uriKey = URI.create((String) kk); // by construction, all the keys are strings.
            } catch (Throwable t) {
                throw new BadArgException(RENAME + " requires a valid URI for the old module identifier", polyad.getArgAt(0));
            }
            URI newKey;
            Object newValue = renameStem.get(kk);
            if (!isString(newValue)) {
                throw new BadArgException(RENAME + " requires the new module identifier be a string, not '" + newValue + "'", polyad.getArgAt(0));
            }
            try {
                newKey = URI.create((String) newValue);
            } catch (Throwable t) {
                throw new BadArgException(LOADED + " requires a valid URI for the new module identifier", polyad.getArgAt(0));

            }
            MTKey oldMTKey = new MTKey(uriKey);
            Module m = state.getMTemplates().getModule(oldMTKey);
            if (m == null) {
                outStem.putLongOrString(kk, Boolean.FALSE);
            } else {
                state.getMTemplates().remove(oldMTKey);
                MTKey newMTKey = new MTKey(newKey);
                state.getMTemplates().put(newMTKey, m);
                outStem.putLongOrString(kk, Boolean.TRUE);
            }
        }
        polyad.setEvaluated(true);
        if (isScalarArg) {
            polyad.setResult(outStem.getString((String) arg0));
        } else {
            polyad.setResult(outStem);
        }
        polyad.setResultType(Constant.getType(polyad.getResult()));
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
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        boolean isScalarArg = true;
        QDLStem inStem = null;
        if (polyad.getArgCount() == 1) {
            Object object = polyad.evalArg(0, state);
            if (isStem(object)) {
                inStem = (QDLStem) object;
                isScalarArg = false;
            } else {
                if (isString(object)) {
                    isScalarArg = true;
                    inStem = new QDLStem();
                    inStem.put(0L, (String) object);
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
            polyad.setResultType(Constant.getType(outStem));
            return;
        }
        QDLStem outStem = new QDLStem();
        for (Object k : inStem.keySet()) {
            URI uri;
            Object object = inStem.get(k);
            if (!isString(object)) {
                throw new BadArgException(LOADED + " arguments must be strings", polyad.getArgAt(0));
            }
            try {
                uri = URI.create((String) object);
                outStem.putLongOrString(k, state.getMTemplates().get(new MTKey(uri)) == null ? Boolean.FALSE : Boolean.TRUE);
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
        polyad.setResultType(Constant.getType(polyad.getResult()));
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
        Object obj = polyad.evalArg(0, state);
        Module m = null;
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
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        Module m = getMorT(polyad, state);
        QDLList outList = new QDLList();
        outList.addAll(m.getDocumentation());
        QDLStem outStem = new QDLStem();
        outStem.setQDLList(outList);
        polyad.setEvaluated(true);
        polyad.setResult(outStem);
        polyad.setResultType(Constant.getType(outStem));

    }

    private void doGetVariables(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            QDLStem qdlStem = new QDLStem();
            qdlStem.getQDLList().addAll(state.getVStack().listVariables());
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            polyad.setResultType(Constant.getType(polyad.getResult()));
            return;
        }

        Object arg0 = polyad.evalArg(0, state);

        if (arg0 instanceof QDLNull) {
            String regex = null;
            if (polyad.getArgCount() == 2) {
                Object arg1 = polyad.evalArg(1, state);
                if (!isString(arg1)) {
                    throw new BadArgException(GET_FUNCTIONS + " requires a string regexc as its second argument if the first is null", polyad.getArgAt(1));
                }
                regex = (String) arg1;
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
            polyad.setResultType(Constant.getType(polyad.getResult()));
            return;

        }
        Module m = getMorT(polyad, state);
        String regex = null;
        if (polyad.getArgCount() == 2) {
            Object arg2 = polyad.evalArg(1, state);
            if (isString(arg2)) {
                regex = (String) arg2;
            } else {
                throw new BadArgException(GET_FUNCTIONS + " second argument must be a string if present", polyad.getArgAt(1));
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
        polyad.setResultType(Constant.getType(outStem));

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
            polyad.setResult(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 0) {
            QDLStem qdlStem = new QDLStem();
            qdlStem.getQDLList().addAll(state.getFTStack().listFunctions(null));
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            polyad.setResultType(Constant.getType(polyad.getResult()));
            return;
        }

        Object arg0 = polyad.evalArg(0, state);

        if (arg0 instanceof QDLNull) {
            String regex = null;
            if (polyad.getArgCount() == 2) {
                Object arg1 = polyad.evalArg(1, state);
                if (!isString(arg1)) {
                    throw new BadArgException(GET_FUNCTIONS + " requires a string regexc as its second argument if the first is null", polyad.getArgAt(1));
                }
                regex = (String) arg1;
            }
            QDLStem qdlStem = new QDLStem();
            qdlStem.getQDLList().addAll(state.getFTStack().listFunctions(regex));
            polyad.setEvaluated(true);
            polyad.setResult(qdlStem);
            polyad.setResultType(Constant.getType(polyad.getResult()));
            return;
        }
        Module m = getMorT(polyad, state);
        String regex = null;
        if (polyad.getArgCount() == 2) {
            Object arg2 = polyad.evalArg(1, state);
            if (isString(arg2)) {
                regex = (String) arg2;
            } else {
                throw new BadArgException(GET_FUNCTIONS + " second argument must be a string if present", polyad.getArgAt(1));
            }
        }
        QDLList outList = new QDLList();
        TreeSet<String> funcs = m.getState().getFTStack().listFunctions(regex);
        outList.addAll(funcs);
        QDLStem outStem = new QDLStem();
        outStem.setQDLList(outList);
        polyad.setEvaluated(true);
        polyad.setResult(outStem);
        polyad.setResultType(Constant.getType(outStem));

    }

    /**
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
    private void doDrop(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        Object arg0 = polyad.evalArg(0, state);
        boolean isScalarArg = false;
        QDLStem inStem;
        if (isString(arg0)) {
            inStem = new QDLStem();
            inStem.put(0L, arg0);
            isScalarArg = true;
        } else {
            if (isStem(arg0)) {
                inStem = (QDLStem) arg0;
            } else {
                throw new BadArgException(DROP + " requires a string or stem as its argument", polyad.getArgAt(0));
            }
        }
        QDLStem outStem = new QDLStem();
        for (Object kk : inStem.keySet()) {
            Object value = inStem.get(kk);
            if (!isString(value)) {
                throw new BadArgException(DROP + " requires a string as its argument, not '" + value + "'", polyad.getArgAt(0));
            }
            URI uriKey;
            try {
                uriKey = URI.create((String) value);
            } catch (Throwable t) {
                throw new BadArgException(DROP + " requires a valid URI as its argument, not '" + value + "'", polyad.getArgAt(0));
            }
            MTKey oldMTKey = new MTKey(uriKey);
            Module m = state.getMTemplates().getModule(oldMTKey);
            if (m == null) {
                outStem.putLongOrString(kk, Boolean.TRUE);
            } else {
                try {
                    state.getMTemplates().remove(oldMTKey);
                    outStem.putLongOrString(kk, Boolean.TRUE);
                } catch (Throwable t) {
                    outStem.putLongOrString(kk, Boolean.FALSE);
                }

            }
        }
        polyad.setEvaluated(true);
        if (isScalarArg) {
            polyad.setResult(outStem.getString(0L));
        } else {
            polyad.setResult(outStem);
        }
        polyad.setResultType(Constant.getType(polyad.getResult()));
    }

    protected void doUse(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        Object arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0), state);
        if (arg == QDLNull.getInstance()) {
            // case that user tries to import a QDL null module. This can happen is
            // the load fails. Don't have it as an error, return QDL null to show nothing
            // happened (so user can check with conditional).
            polyad.setResult(QDLNull.getInstance());
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        if (!isString(arg)) {
            throw new BadArgException(USE + " requires a string as its argument", polyad.getArgAt(0));
        }
        URI moduleNS;
        try {
            moduleNS = URI.create((String) arg);
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
        state.getUsedModules().put(m.getNamespace(), m);
        polyad.setEvaluated(true);
        polyad.setResult(Boolean.TRUE);
        polyad.setResultType(Constant.getType(polyad.getResult()));
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
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }

        Object arg = polyad.evalArg(0, state);

        if (arg == QDLNull.getInstance()) {
            polyad.setResult(QDLNull.getInstance());
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        ModuleUtils moduleUtils = new ModuleUtils();
        QDLStem argStem = moduleUtils.convertArgsToStem(polyad, arg, state, LOAD);
        final int LOAD_UNKNOWN = -1;
        final int LOAD_FILE = 0;
        final int LOAD_JAVA = 1;
        QDLStem outStem = new QDLStem();
        for (Object key : argStem.keySet()) {
            Object value = argStem.get(key);
            int loadTarget = LOAD_UNKNOWN;
            String resourceName = null;
            if (isString(value)) {
                resourceName = (String) value;
            } else {
                QDLStem q = (QDLStem) value;
                resourceName = q.get(0L).toString();
                loadTarget = q.get(1L).toString().equals(MODULE_TYPE_JAVA) ? LOAD_JAVA : LOAD_FILE;

            }
            List<String> loadedQNames = null;
            // if they force the issue, do that and fail
            JavaModuleConfig jmc = new JavaModuleConfig();
            jmc.setImportOnStart(false);
            jmc.setUse(false);
            jmc.setVersion(SerializationConstants.VERSION_2_1_TAG);
            switch (loadTarget) {
                case LOAD_JAVA:
                    loadedQNames = moduleUtils.doJavaModuleLoad(state, resourceName, jmc);
                    break;
                case LOAD_FILE:
                    loadedQNames = moduleUtils.doQDLModuleLoad(state, resourceName);
                    break;
                case LOAD_UNKNOWN:
                    loadedQNames = moduleUtils.doQDLModuleLoad(state, resourceName);
                    if (loadedQNames == null) {
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
            outStem.putLongOrString(key, newEntry);
        }
        polyad.setEvaluated(true);
        if (outStem.size() == 1) {
            polyad.setResult(outStem.get(outStem.keySet().iterator().next()));
            polyad.setResultType(Constant.STRING_TYPE);
        } else {
            polyad.setResult(outStem);
            polyad.setResultType(Constant.STEM_TYPE);
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
    public final static int IMPORT_STATE_NONE_VALUE = 100;
    public final static int IMPORT_STATE_SNAPSHOT_VALUE = 101;
    public final static int IMPORT_STATE_SHARE_VALUE = 102;
    public final static int IMPORT_STATE_ANY_VALUE = 110;
    public static String getInheritanceMode(int x){
        switch (x){

            case IMPORT_STATE_SHARE_VALUE:
                return IMPORT_STATE_SHARE;
            case IMPORT_STATE_SNAPSHOT_VALUE:
                return IMPORT_STATE_SNAPSHOT;
            case IMPORT_STATE_ANY_VALUE:
                return IMPORT_STATE_ANY;
            default:
            case  IMPORT_STATE_NONE_VALUE:
              return IMPORT_STATE_NONE;
        }
    }
    public static int getInheritanceMode(String x){
        switch (x){
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
    protected void doImport(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        // Basic is to import a single module
        Object arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0), state);
        if (arg == QDLNull.getInstance()) {
            // case that user tries to import a QDL null module. This can happen is
            // the load fails. Don't have it as an error, return QDL null to show nothing
            // happened (so user can check with conditional).
            polyad.setResult(QDLNull.getInstance());
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        int inhertianceMode = IMPORT_STATE_NONE_VALUE;
        if (polyad.getArgCount() == 2) {
            Object arg1 = polyad.evalArg(1, state);
            if (isString(arg1)) {
                String a = (String) arg1;
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
                if (isLong(arg1)) {
                    inhertianceMode = ((Long) arg1).intValue();
                } else {
                    throw new BadArgException(IMPORT + " unknown state inheritance mode '" + arg1 + "'", polyad.getArgAt(1));
                }
            }
        }
        if (!isString(arg)) {
            throw new BadArgException(IMPORT + " requires a string as its argument", polyad.getArgAt(0));
        }
        URI moduleNS;
        try {
            moduleNS = URI.create((String) arg);
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
                    newState = newState.newSelectiveState(newState,false,true,true);

                    break;
                case IMPORT_STATE_SHARE_VALUE:
                    newState = state;
                    // put tables and such in the right place so ambient state is not altered.
                    // The next command creates a state with the inhereited functions and variables.

                    newState = newState.newSelectiveState(newState,false,true,true);
                    // now since these are shared, push on new tables to ensure nothing in the ambient state gets overwritten.
                    // We do not want moduile state to leak back into the ambient state.
                    newState.getVStack().pushNewTable();
                    newState.getFTStack().pushNewTable();
                    break;
                default:
                    throw new BadArgException(IMPORT + " with unknown state inheritene mode", polyad.getArgAt(1));
            }
            Module module = template.newInstance(newState);
            module.setInheritanceMode(inhertianceMode);
            newState.setModule(module);
            module.setTemplate(false);
            module.setParentTemplateID(template.getId());
            if(state.hasModule()){
                module.setParentInstanceID(state.getModule().getId());
                module.setParentInstanceAlias(state.getModule().getAlias());
            }
            polyad.setEvaluated(true);
            polyad.setResult(module);
            polyad.setResultType(Constant.MODULE_TYPE);
        } catch (Throwable t) {
           throw new QDLExceptionWithTrace("there was an issue creating the state of the module:" + t.getMessage(), polyad);
        }

    }

    /**
     * This is just module_import(module_load(x, 'java')). It happens so much we need an idiom.
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
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        Object arg = polyad.evalArg(0, state);
        boolean hasAlias = false;
        String alias = null;
        if (polyad.getArgCount() == 2) {
            Object object = polyad.evalArg(1, state);
            if (!isString(object)) {
                throw new BadArgException((isLoad?JAVA_MODULE_LOAD:JAVA_MODULE_USE) + " requires a string as its second argument if present", polyad.getArgAt(1));
            }
            alias = (String) object;
            hasAlias = true;
        }
        String possibleName = arg.toString();
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
                QDLStem libStem = state.getSystemInfo().getStem("lib");
                try {
                    for (int i = 0; i < toolPath.size() - 1; i++) {
                        libStem = libStem.getStem(toolPath.get(i));
                    }
                    if (libStem.containsKey(toolPath.get(toolPath.size() - 1))) {
                        possibleName = libStem.getString(toolPath.get(toolPath.size() - 1));
                    }
                }catch(Throwable t){
                    // ok, so parsing the path failed. This probably means they passed in the actual
                    // full java path, so try to process what they sent.
                }
            }
        }
        Polyad module_load = new Polyad(ModuleEvaluator.LOAD);
        module_load.addArgument(new ConstantNode(possibleName));
        module_load.addArgument(new ConstantNode(MODULE_TYPE_JAVA));
        module_load.evaluate(state);
        Polyad module_import = new Polyad(isLoad?ModuleEvaluator.IMPORT:ModuleEvaluator.USE);
        module_import.addArgument(new ConstantNode(module_load.getResult()));
        if (hasAlias) {
            module_import.addArgument(new ConstantNode(alias));
        }
        module_import.evaluate(state);
        polyad.setEvaluated(true);
        polyad.setResult(module_import.getResult());
        polyad.setResultType(module_import.getResultType());
        return;

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

