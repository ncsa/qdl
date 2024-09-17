package org.qdl_lang.extensions;

import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import net.sf.json.JSONObject;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.qdl_lang.evaluate.ModuleEvaluator.*;
import static org.qdl_lang.state.VariableState.var_regex;
import static org.qdl_lang.xml.SerializationConstants.MODULE_JAVA_STATE_TAG;
import static org.qdl_lang.xml.SerializationConstants.MODULE_STATE_TAG;

/**
 * This will let you create your own extensions to QDL in Java. Simply implement the interfaces
 * {@link QDLFunction} for functions and {@link QDLVariable} for variables, add the module
 * and you can use it in QDL like any other module.
 * <p>Created by Jeff Gaynor<br>
 * on 1/27/20 at  12:03 PM
 */
public abstract class JavaModule extends Module {
    /**
     * Used by {@link QDLLoader}
     */
    public JavaModule() {
    }

    public JavaModule(URI uri) {
        super(uri, null);
    }

    @Override
    public boolean isExternal() {
        return true;
    }

    String className;

    public String getClassname() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getLoaderClassName() {
        return loaderClassName;
    }

    public void setLoaderClassName(String loaderClassName) {
        this.loaderClassName = loaderClassName;
    }

    String loaderClassName;

    /**
     * Used by the factory method {@link #newInstance(State)}
     * @deprecated alias is no longer needed. Just use the namespace only constructor
     * @param namespace
     * @param alias
     */
    protected JavaModule(URI namespace, String alias) {
        super(namespace, alias, null); // no state here -- it is injected later
    }

    public void addFunctions(List<QDLFunction> functions) {
        funcs.addAll(functions);
    }

    protected List<QDLVariable> vars = new ArrayList<>();
    protected List<QDLFunction> funcs = new ArrayList<>();

    public void addVariables(List<QDLVariable> variables) {
        vars.addAll(variables);
    }

    Pattern pattern = Pattern.compile(var_regex);
    boolean initialized = false;

    /**
     * This is critical in that it puts all the functions and variables (with their correct alias) in
     * to the state for this module. Normally this is called when module_import is invoked
     * on each module, so generally you do not need to call this ever. It is, however, what makes
     * any module work.
     *
     * @param state
     */
    public void init(State state) {
        if(state == null) {return;} // do nothing!
        init(state, true);
    }

    public void init(State state, boolean doVariables) {
        if (initialized) return;
        setDocumentation(createDefaultDocs());
        if (state == null) return;
        setState(state);
        // If this is being recreated from its serialization, skip the variables so whatever
        // the has set is not overwritten.
        if (doVariables) {
            for (QDLVariable v : vars) {
                if (Constant.getType(v.getValue()) == Constant.UNKNOWN_TYPE) {
                    throw new IllegalArgumentException("Error: The value of  " + v.getValue() + " is unknown.");
                }
                if (!pattern.matcher(v.getName()).matches()) {
                    throw new IllegalArgumentException("Error: The variable name \"" + v.getName() + "\" is not a legal variable name.");
                }
                state.setValue(v.getName(), v.getValue());
            }
        }
        for (QDLFunction f : funcs) {
            for (int i : f.getArgCount()) {
                QDLFunctionRecord fr = new QDLFunctionRecord();
                fr.qdlFunction = f;
                fr.argCount = i;
                fr.setName(f.getName());
                // There are no names for these, so these are created
                List<String> names = new ArrayList<>();
                for (int k = 0; k < i; k++) {
                    names.add("x_" + k);
                }
                fr.setArgNames(names);
                if (f.getDocumentation(i) != null && !f.getDocumentation(i).isEmpty()) {
                    fr.documentation = f.getDocumentation(i);
                }
               /* if(state.isExtrinsic(fr.getName())){
                    state.getExtrinsicFuncs().put(fr);
                }else{*/
                //  state.getFTStack().put(fr);
                state.putFunction(fr);
                //}

            }
        }
        setClassName(this.getClass().getCanonicalName());
        initialized = true;
    }


    @Override
    public void writeExtraXMLAttributes(XMLStreamWriter xsw) throws XMLStreamException {
        super.writeExtraXMLAttributes(xsw);
        xsw.writeAttribute(SerializationConstants.MODULE_TYPE_TAG, SerializationConstants.MODULE_TYPE_JAVA_TAG);
        xsw.writeAttribute(SerializationConstants.MODULE_CLASS_NAME_TAG, getClassname());
    }

    @Override
    public JSONObject serializeToJSON(SerializationState serializationState) throws Throwable {
        JSONObject json = super.serializeToJSON(serializationState);
        if (hasMetaClass()) {
            if (null != getMetaClass().serializeToJSON()) {
                json.put(MODULE_JAVA_STATE_TAG, getMetaClass().serializeToJSON());
            }
        }
        // https://github.com/ncsa/qdl/issues/79 booby trap
        // Should this ever raise its head again, send up flares all over the place.
        // Old module system imported shared state which might cause a recursive failure.
        // At issue: modules imported in the configuration using the old system can get a recursive
        // loop when serializing. This next bit of code should avoid that, and if it does happen,
        // there should be alls sorts of bells and whistles that go off, since this would be show-stopper
        // for OA4MP.
        // The solution is to use not use the old config import system, but use a boot script and create variables
        // that contain what you want. There is really no reason to use the old system!
        if (!isUsed()
                && getInheritMode() != IMPORT_STATE_ANY_VALUE // old modules whose inheritance was somehow not set
                && getInheritMode() != IMPORT_STATE_SHARE_VALUE // mostly old modules
                && getState() != null) {
            try {
                json.put(MODULE_STATE_TAG, getState().serializeToJSON(serializationState));
            }catch(StackOverflowError sox){
                System.out.println("***Caught StackOverflowError"  );
                System.out.println("in JavaModule.serializeToJSON:\n" + json.toString(2));
                System.out.println("JavaModule:" + this);
                System.out.println("state.getVStack:\n" + getState().getVStack().toString(true));
                throw sox;
            }catch(Throwable t){
                System.out.println("***Caught other exception:" + t);
                System.out.println("JavaModule:" + this);
                System.out.println("in JavaModule.serializeToJSON:\n" + json.toString(2));
                throw t;
            }
        }
        json.put(SerializationConstants.MODULE_TYPE_TAG2, SerializationConstants.MODULE_TYPE_JAVA_TAG);
        json.put(SerializationConstants.MODULE_CLASS_NAME_TAG, getClassname());
        return json;
    }

    @Override
    public void deserializeFromJSON(JSONObject json, SerializationState serializationState) throws Throwable {
        super.deserializeFromJSON(json, serializationState);
        deserializeStates(json, serializationState);
    }

    /**
     * This should centralize deserializing the state for a Java module. Hence this is public
     * and should be called whenever this as needed.
     *
     * @param jsonObject
     * @param serializationState
     * @throws Throwable
     */
    public void deserializeStates(JSONObject jsonObject, SerializationState serializationState) throws Throwable {
        // Fixes https://github.com/ncsa/qdl/issues/77
        if (hasMetaClass()) {
            getMetaClass().deserializeFromJSON(jsonObject.getJSONObject(MODULE_JAVA_STATE_TAG));
        }
        if (jsonObject.has(MODULE_STATE_TAG)) {
            State cleanState = State.getRootState().newCleanState();
            cleanState.deserializeFromJSON(jsonObject.getJSONObject(MODULE_STATE_TAG), serializationState);
            // In Java modules, the variable stack might have been updated, but nothing else
            // can really be used.
            getState().setvStack(cleanState.getVStack());
        }
    }

    /**
     * Creates the documentation from the first of each line of every function. Use this or
     * override as needed.
     *
     * @return
     */
    public List<String> createDefaultDocs() {
        List<String> docs = new ArrayList<>();
        docs.add("  module name : " + getClass().getSimpleName());
        docs.add("    namespace : " + getNamespace());
        docs.add("default alias : " + getAlias());
        docs.add("   java class : " + getClass().getCanonicalName());
        if (getDescription() != null) {
            docs.addAll(getDescription());
        }
        if (!funcs.isEmpty()) {
            docs.add("functions:");
            // Now sort the functions
            TreeSet<String> treeSet = new TreeSet<>();
            for (QDLFunction f : funcs) {
                for (int argCount : f.getArgCount()) {
                    List<String> x = f.getDocumentation(argCount);
                    if (x != null && !x.isEmpty()) {
                        treeSet.add("  " + f.getDocumentation(argCount).get(0));// indent too...
                    }
                }
            }
            docs.addAll(treeSet);
        }
        if (!vars.isEmpty()) {
            docs.add("variables:");
            TreeSet<String> treeSet = new TreeSet<>();
            for (QDLVariable variable : vars) {
                treeSet.add("  " + variable.getName()); // indent
            }
            docs.addAll(treeSet);
        }

        return docs;
    }

    /**
     * The {@link #createDefaultDocs()} will create basic documentation for functions and such,
     * and is called automatically during module {@link #init(State)},
     * but the actual description of this module -- if any -- is done here. Override and return your description.
     *
     * @return
     */
    public List<String> getDescription() {
        return null;
    }

    List<String> documentation = new ArrayList<>();

    @Override
    public List<String> getListByTag() {
        return documentation;
    }

    @Override
    public void setDocumentation(List<String> documentation) {
        this.documentation = documentation;
    }

    @Override
    public List<String> getDocumentation() {
        return documentation;
    }

    public QDLMetaModule getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(QDLMetaModule metaClass) {
        this.metaClass = metaClass;
    }

    QDLMetaModule metaClass = null;

    public boolean hasMetaClass() {
        return metaClass != null;
    }

    @Override
    public String toString() {
        return "JavaModule{" +
                "\nclassName='" + className + '\'' +
                ",\n loaderClassName='" + loaderClassName + '\'' +
                ",\n metaClass=" + metaClass +
                ",\n initialized=" + initialized +
                ",\n vars=" + vars +
                ",\n funcs=" + funcs +
                '}';
    }
}
