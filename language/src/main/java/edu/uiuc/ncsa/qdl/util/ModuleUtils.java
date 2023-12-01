package edu.uiuc.ncsa.qdl.util;

import edu.uiuc.ncsa.qdl.config.QDLConfigurationLoaderUtils;
import edu.uiuc.ncsa.qdl.evaluate.ModuleEvaluator;
import edu.uiuc.ncsa.qdl.evaluate.OpEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.*;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.Polyad;
import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.extensions.QDLLoader;
import edu.uiuc.ncsa.qdl.functions.FTable;
import edu.uiuc.ncsa.qdl.module.MTKey;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.parsing.QDLParserDriver;
import edu.uiuc.ncsa.qdl.parsing.QDLRunner;
import edu.uiuc.ncsa.qdl.scripting.QDLScript;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.variables.Constant;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.variables.VTable;
import edu.uiuc.ncsa.qdl.variables.VThing;
import edu.uiuc.ncsa.qdl.xml.SerializationState;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static edu.uiuc.ncsa.qdl.evaluate.AbstractEvaluator.checkNull;
import static edu.uiuc.ncsa.qdl.evaluate.SystemEvaluator.*;
import static edu.uiuc.ncsa.qdl.variables.Constant.isString;
import static edu.uiuc.ncsa.qdl.variables.VTable.KEY_KEY;
import static edu.uiuc.ncsa.qdl.variables.VTable.VALUE_KEY;
import static edu.uiuc.ncsa.qdl.xml.XMLConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/21/23 at  7:55 AM
 */
public class ModuleUtils implements Serializable {
    /**
     * Converts a couple of different arguments to the form
     * [[a0{,b0}],[a1{,b1}],...,[an{,bn}] or (if a single argument that is
     * a stem) can pass back:
     * <p>
     * {key0:[[a0{,b0}], key1:[a1{,b1}],...}
     * <p>
     * where the bk are optional. All ak, bk are strings.
     * a,b -> [[a,b]] (pair of arguments, function is dyadic
     * [a,b] ->[[a,b]] (simple list, convert to nested list
     * [a0,a1,...] -> [[a0],[a1],...] allow for scalars
     * Use in both module import and load for consistent arguments
     *
     * @param polyad
     * @param state
     * @param component
     * @return
     */
    public QDLStem convertArgsToStem(Polyad polyad, Object arg, State state, String component) {
        QDLStem argStem = null;

        boolean gotOne = false;

        switch (polyad.getArgCount()) {
            case 0:
                throw new MissingArgException(component + " requires an argument", polyad);
            case 1:
                // single string arguments
                if (isString(arg)) {
                    argStem = new QDLStem();
                    argStem.listAdd(arg);
                    gotOne = true;
                }
                if (Constant.isStem(arg)) {
                    argStem = (QDLStem) arg;
                    gotOne = true;
                }
                break;
            case 2:
                if (!isString(arg)) {
                    throw new BadArgException("Dyadic " + component + " requires string arguments only", polyad.getArgAt(0));
                }
                Object arg2 = polyad.evalArg(1, state);
                checkNull(arg2, polyad.getArgAt(1), state);
                if (!isString(arg2)) {
                    throw new BadArgException("Dyadic " + component + " requires string arguments only", polyad.getArgAt(1));
                }

                argStem = new QDLStem();
                QDLStem innerStem = new QDLStem();
                innerStem.listAdd(arg);
                innerStem.listAdd(arg2);
                argStem.put(0L, innerStem);
                gotOne = true;
                break;
            default:
                throw new ExtraArgException(component + ": too many arguments", polyad.getArgAt(2));
        }
        if (!gotOne) {
            throw new BadArgException(component + ": unknown argument type", polyad);
        }
        return argStem;
    }

    /**
     * Load a single java module, returning a null if it failed or the FQ name if it worked.
     *
     * @param state
     * @param resourceName
     */
    public List<String> doJavaModuleLoad(State state, String resourceName) {
        try {
            Class klasse = state.getClass().forName(resourceName);
            Object newThingy = klasse.newInstance();
            QDLLoader qdlLoader;
            if (newThingy instanceof JavaModule) {
                // For a single instance, just create a barebones loader on the fly.
                qdlLoader = new QDLLoader() {
                    @Override
                    public List<Module> load() {
                        List<Module> m = new ArrayList<>();
                        JavaModule javaModule = (JavaModule) newThingy;
                        State newState = state.newCleanState();
                        javaModule = (JavaModule) javaModule.newInstance(newState);
                        javaModule.init(newState); // set it up
                        javaModule.setTemplate(true);

                        m.add(javaModule);
                        return m;
                    }
                };
            } else {
                if (!(newThingy instanceof QDLLoader)) {
                    throw new IllegalArgumentException("'" + resourceName + "' is neither a module nor a loader.");
                }
                qdlLoader = (QDLLoader) newThingy;
            }
            List<String> names = QDLConfigurationLoaderUtils.setupJavaModule(state, qdlLoader, false);
            if (names.isEmpty()) {
                return null;
            }
            return names;
        } catch (RuntimeException rx) {
            throw rx;
        } catch (ReflectiveOperationException cnf) {
            return null; // if it is not found, just return null;
        } catch (Throwable t) {
            throw new QDLException("could not load Java class " + resourceName + ": '" + t.getMessage() + "'.", t);
        }
    }

    /**
     * Load the module(s) in a single resource.
     *
     * @param state
     * @param resourceName
     * @return
     */
    public List<String> doQDLModuleLoad(State state, String resourceName) {
        QDLScript script = null;
        try {
            script = resolveScript(resourceName, state.getModulePaths(), state);
            if (script == null) {
                script = resolveScript(resourceName + MODULE_DEFAULT_EXTENSION, state.getModulePaths(), state);
            }
        } catch (Throwable t) {
            state.warn("Could not find module:" + t.getMessage());
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new QDLRuntimeException("Could not find  '" + resourceName + "'. Is your module path set?", t);
        }

        try {
            QDLParserDriver parserDriver = new QDLParserDriver(new XProperties(), state);
            // Exceptional case where we just run it directly.
            // note that since this is QDL there may be multiple modules, etc.
            // in a single file, so there is no way to know what the user did except
            // to look at the state before, then after. This should return the added
            // modules fq paths.
            state.getMTemplates().clearChangeList();
            if (script == null) {
                if (state.isServerMode()) {
                    throw new QDLServerModeException("File operations are not permitted in server mode");
                }
                Reader reader = new InputStreamReader(QDLFileUtil.readFileAsInputStream(state, resourceName));
                QDLRunner runner = new QDLRunner(parserDriver.parse(reader));
                runner.setState(state);
                runner.run();
            } else {
                script.execute(state);
            }
            List<String> afterLoad = new ArrayList<>();
            for (Object k : state.getMTemplates().getChangeList()) {
                MTKey mtKey = (MTKey) k;
                state.getMTemplates().getModule(mtKey).setTemplate(true);
                afterLoad.add(mtKey.getKey());
            }
            state.getMTemplates().clearChangeList();
            return afterLoad;
        } catch (Throwable t) {

        }
        return null;
    }

    /**
     * This starts the load from the JSON since which type of module to instantiate is needed, so
     * the right module has to exist before {@link Module#deserializeFromJSON(JSONObject, SerializationState)} can be called.
     *
     * @param state
     * @param json
     * @return
     */
    public Module deserializeFromJSON(State state, JSONObject json, SerializationState serializationState) throws Throwable {
        Module m = null;
        QDLInterpreter qi = new QDLInterpreter(state);
        Polyad polyad;
        polyad = new Polyad(ModuleEvaluator.LOAD);
        if (json.getString(MODULE_TYPE_TAG2).equals(MODULE_TYPE_JAVA)) {
            polyad.addArgument(new ConstantNode(json.getString(MODULE_CLASS_NAME_TAG)));
            polyad.addArgument(new ConstantNode("java"));
            polyad.evaluate(state);
        }
        if (json.getString(MODULE_TYPE_TAG2).equals(QDL_TYPE_TAG)) {
            // This is a module[] statement and needs to be loaded directly.
            String source = new String(Base64.decodeBase64(json.getString(MODULE_INPUT_FORM_TAG)), UTF_8);
            try {
                qi.execute(source);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        String varName = json.getString(VTable.KEY_KEY);
        String inheritanceMode = ModuleEvaluator.IMPORT_STATE_NONE;
        inheritanceMode = json.containsKey(MODULE_INHERITANCE_MODE_TAG) ? json.getString(MODULE_INHERITANCE_MODE_TAG) : inheritanceMode;
        String y = varName + OpEvaluator.ASSIGNMENT + "import('" + json.getString(MODULE_NS_ATTR) + "', '" + inheritanceMode + "');";
        try {
            // Note that if there are embedded modules, this will create a network of them
            qi.execute(y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        m = (Module) qi.getState().getValue(varName);
        m.setId(UUID.fromString(json.getString(UUID_TAG)));

        updateSerializedState(json, m.getState(), serializationState);
        return m;
    }

    /**
     * There are two steps to deserialzing a workspace. First, run the QDL which sets up the
     * (arbitrarily complex) network of objects. Then make a second pass with any state
     * objects that have been updated.
     *
     * @param jsonObject
     * @param serializationState
     */
    public void updateSerializedState(JSONObject jsonObject, State state, SerializationState serializationState) throws Throwable {
        if (!jsonObject.containsKey(MODULE_STATE_TAG)) return;
        // special case single module that is Java only.
        if (jsonObject.getString(TYPE_TAG).equals(MODULE_TAG) && jsonObject.containsKey(MODULE_TYPE_TAG2)) {
            if (jsonObject.getString(MODULE_TYPE_TAG2).equals(MODULE_TYPE_JAVA)) {
                // Then this is straight up a java module and the state is the entire content
                Module module = state.getModule();
                jsonObject.getJSONObject(MODULE_STATE_TAG);
                if ((module == null) || !(module instanceof JavaModule)) {
                    throw new NFWException("serialization error. Expected a java module");
                }
                JavaModule javaModule = (JavaModule) module;
                if (javaModule.hasMetaClass()) {
                    javaModule.getMetaClass().deserializeFromJSON(jsonObject.getJSONObject(MODULE_STATE_TAG));
                }
                return;
            }
        }
        JSONObject jState = jsonObject.getJSONObject(MODULE_STATE_TAG);
        QDLInterpreter qi = new QDLInterpreter(state);
        JSONArray funcs = getStack(jState, FUNCTION_TABLE_STACK_TAG);
        for (int i = 0; i < funcs.size(); i++) {
            String source = new String(Base64.decodeBase64(funcs.getJSONObject(i).getString(FTable.FUNCTION_ENTRY_KEY)), UTF_8);
            qi.execute(source);
        }
        JSONArray vars = getStack(jState, VARIABLE_STACK);
        for (int i = 0; i < vars.size(); i++) {
            JSONObject var = vars.getJSONObject(i);
            if (var.getString(TYPE_TAG).equals(QDL_TYPE_TAG)) {
                String source = new String(Base64.decodeBase64(vars.getJSONObject(i).getString(VALUE_KEY)), UTF_8);
                qi.execute(source);
            }
            if (var.getString(TYPE_TAG).equals(MODULE_TAG)) {
                String key = var.getString(KEY_KEY);
                VThing vThing = (VThing) state.getVStack().get(new XKey(key));
                if (!(vThing.getValue() instanceof Module)) {
                    throw new NFWException("Incorrect serialization. Expected a module for variable " + key + " but got a " + vThing.getValue().getClass().getSimpleName());
                }
                Module module = (Module) vThing.getValue();
                if (module instanceof JavaModule) {
                    JavaModule javaModule = (JavaModule) module;
                    if (javaModule.hasMetaClass()) {
                        javaModule.getMetaClass().deserializeFromJSON(var.getJSONObject(MODULE_STATE_TAG));
                    }
                } else {
                    updateSerializedState(var, module.getState(), serializationState);
                }
            }
        }

    }

    JSONArray emptyArray = new JSONArray(); // just have one of these so we don't keep creating empty ones

    // This
    protected JSONArray getStack(JSONObject state, String tag) {
        if (!state.containsKey(tag)) {
            return emptyArray;
        }
        return state.getJSONObject(tag).getJSONArray(STACK_TAG).getJSONArray(0);
    }
      /*
         setMInstances((MIStack) makeStack(new MIStack(), jsonObject, MODULE_INSTANCES_TAG, serializationState));
         setMTemplates((MTStack) makeStack(new MTStack(), jsonObject, MODULE_TEMPLATE_TAG, serializationState));
         setFTStack((FStack) makeStack(new FStack(), jsonObject, FUNCTION_TABLE_STACK_TAG, serializationState));
         setvStack((VStack) makeStack(new VStack(), jsonObject, VARIABLE_STACK, serializationState));
  */

    /**
     * For modules <b>only</b>. Applies the serialized module state. If the module was created with the
     * shared state, then only the local tables are updated.
     *
     * @param module
     * @param serializedState
     */
    protected void applyState(Module module, JSONObject serializedState) {

    }
}
