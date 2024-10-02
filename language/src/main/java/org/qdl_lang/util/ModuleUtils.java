package org.qdl_lang.util;

import org.qdl_lang.config.JavaModuleConfig;
import org.qdl_lang.config.QDLConfigurationLoaderUtils;
import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.functions.FTable;
import org.qdl_lang.module.MTKey;
import org.qdl_lang.module.Module;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.parsing.QDLParserDriver;
import org.qdl_lang.parsing.QDLRunner;
import org.qdl_lang.scripting.QDLScript;
import org.qdl_lang.state.ClassMigrator;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.VTable;
import org.qdl_lang.variables.VThing;
import org.qdl_lang.xml.SerializationState;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.qdl_lang.evaluate.AbstractEvaluator.checkNull;
import static org.qdl_lang.evaluate.SystemEvaluator.*;
import static org.qdl_lang.variables.Constant.isString;
import static org.qdl_lang.variables.VTable.KEY_KEY;
import static org.qdl_lang.variables.VTable.VALUE_KEY;
import static org.qdl_lang.xml.SerializationConstants.*;
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
    public List<String> doJavaModuleLoad(State state, String resourceName, JavaModuleConfig javaModuleConfig) {
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
                        javaModule.setInheritanceMode(ModuleEvaluator.IMPORT_STATE_NONE_VALUE);
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
            List<String> names = QDLConfigurationLoaderUtils.setupJavaModule(state, qdlLoader, javaModuleConfig);
            if (names.isEmpty()) {
                return null;
            }
            return names;
        } catch (RuntimeException rx) {
            throw rx;
        } catch (ReflectiveOperationException cnf) {
            throw new BadArgException("class not found for " + resourceName, cnf, null);
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

        //     try {
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
            try {
                Reader reader = new InputStreamReader(QDLFileUtil.readFileAsInputStream(state, resourceName));
                QDLRunner runner = new QDLRunner(parserDriver.parse(reader));
                runner.setState(state);
                runner.run();
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new BadArgException("could not parse module '" + t.getMessage() + "'", null);
            }
        } else {
            boolean importMode = state.isImportMode();
            state.setImportMode(false);
            script.execute(state);
            state.setImportMode(importMode);
        }
        List<String> afterLoad = new ArrayList<>();
        for (Object k : state.getMTemplates().getChangeList()) {
            MTKey mtKey = (MTKey) k;
            state.getMTemplates().getModule(mtKey).setTemplate(true);
            afterLoad.add(mtKey.getKey());
        }
        state.getMTemplates().clearChangeList();
        return afterLoad;
  /*      } catch (Throwable t) {
           t.printStackTrace();
        }
        return null;*/
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
        return deserializeFromJSON(state, json, false, serializationState);
    }

    /**
     * @param state
     * @param json
     * @param useModule          if this module is used (i.e. imported to current scope) vs. imported
     * @param serializationState
     * @return
     * @throws Throwable
     */
    public Module deserializeFromJSON(State state,
                                      JSONObject json,
                                      boolean useModule,
                                      SerializationState serializationState) throws Throwable {
        Module m = null;
        // Fix https://github.com/ncsa/oa4mp/issues/208, https://github.com/ncsa/qdl/issues/82
        ClassMigrator.updateSerializedJSON(json);
        QDLInterpreter qi = new QDLInterpreter(state);
        Polyad polyad;
        polyad = new Polyad(ModuleEvaluator.LOAD);
        if (json.getString(MODULE_TYPE_TAG2).equals(MODULE_TYPE_JAVA)) {
            polyad.addArgument(new ConstantNode(json.getString(MODULE_CLASS_NAME_TAG)));
            polyad.addArgument(new ConstantNode("java"));
            polyad.evaluate(state);
        }
        boolean isTemplate = json.getBoolean(MODULE_IS_TEMPLATE_TAG);
        String source = null;
        if (json.getString(MODULE_TYPE_TAG2).equals(QDL_TYPE_TAG)) {
            // This is a module[] statement and needs to be loaded directly.
            if (json.containsKey(MODULE_INPUT_FORM_TAG)) {
                source = new String(Base64.decodeBase64(json.getString(MODULE_INPUT_FORM_TAG)), UTF_8);
            } else {
                if (serializationState.hasTemplates()) {
                    UUID templateUUID = UUID.fromString(json.getString(PARENT_TEMPLATE_UUID_TAG));
                    Module template = serializationState.getTemplate(templateUUID);
                    if (template != null) {
                        source = InputFormUtil.inputForm(template);
                    }
                }
            }
            if (source == null) {
                throw new IllegalStateException("missing source for module");
            }
            try {
                qi.execute(source);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (isTemplate) {
            Module t = (Module) qi.getState().getMTemplates().get(new MTKey(URI.create(json.getString(MODULE_NS_ATTR))));
            t.setId(UUID.fromString(json.getString(UUID_TAG)));
            return t;
        }
        String y;
        String varName = null;
        boolean isOldInstance = json.containsKey(MODULE_IS_INSTANCE_TAG) && json.getBoolean(MODULE_IS_INSTANCE_TAG);
        if (isOldInstance) {
            y = MODULE_IMPORT + "('" + json.getString(MODULE_NS_ATTR) + "', '" + json.getString(MODULE_ALIAS_ATTR) + "');";
        } else {
            String inheritanceMode = ModuleEvaluator.IMPORT_STATE_NONE;
            inheritanceMode = json.containsKey(MODULE_INHERITANCE_MODE_TAG) ? json.getString(MODULE_INHERITANCE_MODE_TAG) : inheritanceMode;
            if (useModule) {
                y = ModuleEvaluator.USE + "('" + json.getString(MODULE_NS_ATTR) + "', '" + inheritanceMode + "');";
            } else {
                varName = json.getString(VTable.KEY_KEY);
                y = varName + OpEvaluator.ASSIGNMENT + ModuleEvaluator.IMPORT + "('" + json.getString(MODULE_NS_ATTR) + "', '" + inheritanceMode + "');";
            }
        }
        try {
            // Note that if there are embedded modules, this will create a network of them
            qi.execute(y);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (useModule) {
            updateUsedModuleState(json, state, serializationState);
            return null;
        }
        if (isOldInstance) {
            m = qi.getState().getMInstances().getModule(new XKey(json.getString(MODULE_ALIAS_ATTR)));
        } else {

            m = (Module) qi.getState().getValue(varName);
        }
        m.setId(UUID.fromString(json.getString(UUID_TAG)));
        updateSerializedState(json, m.getState(), serializationState);
        return m;
    }

    public void updateUsedModuleState(JSONObject jsonObject, State state, SerializationState serializationState) throws Throwable {
        // at this point, QDL has recreated the system and the state of the stored module needs to be adpated.
        Module m = state.getUsedModules().get(URI.create(jsonObject.getString(MODULE_NS_ATTR)));
        if (m instanceof JavaModule) {
            JavaModule javaModule = (JavaModule) m;
            // fix https://github.com/ncsa/qdl/issues/77
            javaModule.deserializeStates(jsonObject, serializationState);
        }
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
        //if (jsonObject.getString(TYPE_TAG).equals(MODULE_TAG) && jsonObject.containsKey(MODULE_TYPE_TAG2)) {
        if (jsonObject.containsKey(MODULE_TYPE_TAG2)) {
            if (jsonObject.getString(MODULE_TYPE_TAG2).equals(MODULE_TYPE_JAVA)) {
                // Then this is straight up a java module and the state is the entire content
                Module module = state.getModule();
                jsonObject.getJSONObject(MODULE_STATE_TAG);// checks if state stack is a JSON object.
                if ((module == null) || !(module instanceof JavaModule)) {
                    throw new NFWException("serialization error. Expected a java module");
                }
                JavaModule javaModule = (JavaModule) module;
                // Fix https://github.com/ncsa/qdl/issues/77
                javaModule.deserializeStates(jsonObject, serializationState);
                return;
            }
        }
        JSONObject jState = jsonObject.getJSONObject(MODULE_STATE_TAG);
        QDLInterpreter qi = new QDLInterpreter(state);
        JSONArray funcs = getStack(jState, FUNCTION_TABLE_STACK_TAG);
        doFunctions(funcs, qi);
        funcs = getStack(jState, INTRINSIC_FUNCTIONS_TAG);
        doFunctions(funcs, qi);
        JSONArray vars = getStack(jState, VARIABLE_STACK);
        doVariables(state, serializationState, vars, qi);
        vars = getStack(jState, INTRINSIC_VARIABLES_TAG);
        doVariables(state, serializationState, vars, qi);

    }

    private static void doFunctions(JSONArray funcs, QDLInterpreter qi) throws Throwable {
        for (int i = 0; i < funcs.size(); i++) {
            String source = new String(Base64.decodeBase64(funcs.getJSONObject(i).getString(FTable.FUNCTION_ENTRY_KEY)), UTF_8);
            qi.execute(source);
        }
    }

    private void doVariables(State state, SerializationState serializationState, JSONArray vars, QDLInterpreter qi) throws Throwable {
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
                    javaModule.deserializeStates(var, serializationState);
                } else {
                    updateSerializedState(var, module.getState(), serializationState);
                }
            }
        }
    }

    public JSONArray serializeUsedModules(State state,
                                          SerializationState serializationState
    ) throws Throwable {
        if (state.getUsedModules().isEmpty()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (URI key : state.getUsedModules().keySet()) {
            Module m = state.getUsedModules().get(key);
            array.add(m.serializeToJSON(serializationState));
        }
        return array;
    }

    public void deserializeUsedModules(State state, JSONArray jsonArray, SerializationState serializationState) throws Throwable {
        if (jsonArray == null || jsonArray.isEmpty()) {
            return;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            deserializeFromJSON(state, json, true, serializationState); // should be added to ambient state
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
