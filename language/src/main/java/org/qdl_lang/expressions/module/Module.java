package org.qdl_lang.expressions.module;

import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.state.XThing;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLMissingCloseTagException;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.qdl_lang.xml.SerializationConstants.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/21/20 at  11:03 AM
 */
public abstract class Module implements XThing, Serializable {
    @Override
    public String getName() {
        return getAlias();
    }

    XKey key = null;

    @Override
    public XKey getKey() {
        if (key == null) {
            if (StringUtils.isTrivial(getAlias())) {
                if (isTemplate()) {
                    key = getMTKey();
                } else {
                    throw new IllegalStateException("No alias for module '" + getNamespace() + "'");
                }
            } else {
                key = new XKey(getName()); // for instances, stash with the alias
            }
        }
        return key;
    }

    MTKey mtKey = null;

    public MTKey getMTKey() {
        if (mtKey == null) {
            mtKey = new MTKey(getNamespace());
        }
        return mtKey;
    }

    /**
     * This returns true only if the module is from another language than a QDL module.
     *
     * @return
     */
    public boolean isExternal() {
        return false;
    }

    /**
     * The system will mark loaded modules as template.
     *
     * @return
     */
    public boolean isTemplate() {
        return template;
    }

    public void setTemplate(boolean template) {
        this.template = template;
    }

    boolean template = false;

    public Module() {
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    State state;

    public Module(URI namespace, String alias, State state) {
        this.state = state;
        this.alias = alias;
        this.namespace = namespace;
    }
    public Module(URI namespace,  State state) {
        this.state = state;
        this.namespace = namespace;
    }
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    String alias;


    public URI getNamespace() {
        return namespace;
    }

    public void setNamespace(URI namespace) {
        this.namespace = namespace;
    }

    URI namespace;

    public JSONArray toJSON() {
        JSONArray array = new JSONArray();
        array.add(getNamespace().toString());
        array.add(alias);
        return array;
    }

    public void fromJSON(JSONArray array) {
        namespace = URI.create(array.getString(0));
        alias = array.getString(1);
    }

    @Override
    public String toString() {
        return "Module{" +
                "namespace=" + namespace +
                ", alias='" + alias + '\'' +
                (isExternal() ? ", isJava" : "") +
                '}';
    }

    /**
     * If this was imported by the use command.
     * @return
     */
    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    boolean used = false;

    /**
     * Modules are effectively templates. This passes in the state of the parser at the point a new
     * module is required and the contract is to create a new instance of this module with the state.
     * Note that the state passed in may have nothing to do with the state here. You are creating
     * a new module for the given state using this as a template.  <br/><br/>
     * All implementations should gracefully handle a null state with the assumption that
     * the full state will be set later. This is because of bootstrapping networks of modules
     * during deserialization.
     *
     * @param state
     * @return
     */
    public abstract Module newInstance(State state);

    /**
     * Called in {@link #newInstance(State)} to finish setting up the module for things like
     * serialization. When you write {@link #newInstance(State)} the very last thing you should
     * do is invoke this on your new module.
     *
     * @param module
     */
    protected void setupModule(Module module) {
        module.setParentTemplateID(getId());
    }

    public void toXML(XMLStreamWriter xsw, String alias) throws XMLStreamException {

    }

    public void toXML2(XMLStreamWriter xsw,
                       String alias,
                       boolean fullSerialization,
                       SerializationState SerializationState) throws XMLStreamException {
        xsw.writeStartElement(MODULE_TAG);
        xsw.writeAttribute(SerializationConstants.MODULE_NS_ATTR, getNamespace().toString());
        if (!(getAlias() == null && alias == null)) {
            xsw.writeAttribute(SerializationConstants.MODULE_ALIAS_ATTR, StringUtils.isTrivial(alias) ? getAlias() : alias);
        }
        xsw.writeAttribute(SerializationConstants.UUID_TAG, getId().toString());
        writeExtraXMLAttributes(xsw);
        xsw.writeStartElement("inputForm");
        xsw.writeCData(Base64.encodeBase64URLSafeString(InputFormUtil.inputForm(this).getBytes()));
        xsw.writeEndElement();
        if (getState() != null) {
            state.toXML(xsw);
        }
        writeExtraXMLElements(xsw); // Do this first so they get read first later on deserialization
        xsw.writeEndElement();
    }

    /**
     * The result of this is a json object
     *
     * @param serializationState
     * @return
     */
    public JSONObject serializeToJSON(SerializationState serializationState) throws Throwable {
        JSONObject json = new JSONObject();
        json.put(MODULE_NS_ATTR, getNamespace().toString());
        if (!StringUtils.isTrivial(getAlias())) json.put(MODULE_ALIAS_ATTR, getAlias());
        json.put(UUID_TAG, getId().toString());
        if (getParentInstanceID() != null) json.put(PARENT_INSTANCE_UUID_TAG, getParentInstanceID().toString());
        if (getParentTemplateID() != null) json.put(PARENT_TEMPLATE_UUID_TAG, getParentTemplateID().toString());
        if (!StringUtils.isTrivial(getParentInstanceAlias()))
            json.put(PARENT_INSTANCE_ALIAS_TAG, getParentInstanceAlias());
        json.put(MODULE_INHERITANCE_MODE_TAG, ModuleEvaluator.getInheritanceMode(getInheritMode()));
        json.put(MODULE_IS_TEMPLATE_TAG, isTemplate());
        return json;
    }

    /**
     * Deserializes a JSON object into the current module. You must check the type in the json
     * object o know which class (e.g., {@link org.qdl_lang.extensions.JavaModule} to instantiate
     * first.
     * @param json
     * @param serializationState
     * @throws Throwable
     */
    public void deserializeFromJSON(JSONObject json,
                                    SerializationState serializationState) throws Throwable {
        setNamespace(URI.create(json.getString(MODULE_NS_ATTR)));
        if (json.containsKey(MODULE_ALIAS_ATTR)) setAlias(json.getString(MODULE_ALIAS_ATTR));
        setId(UUID.fromString(json.getString(UUID_TAG)));
        if (json.containsKey(MODULE_DOCUMENTATION_TAG)) setDocumentation(json.getJSONArray(MODULE_DOCUMENTATION_TAG));
        if (json.containsKey(PARENT_INSTANCE_ALIAS_TAG))
            setParentInstanceAlias(json.getString(PARENT_INSTANCE_ALIAS_TAG));
        if (json.containsKey(PARENT_TEMPLATE_UUID_TAG))
            setParentTemplateID(UUID.fromString(json.getString(PARENT_TEMPLATE_UUID_TAG)));
        if (json.containsKey(PARENT_INSTANCE_UUID_TAG))
            setParentTemplateID(UUID.fromString(json.getString(PARENT_INSTANCE_UUID_TAG)));
        setTemplate(json.getBoolean(MODULE_IS_TEMPLATE_TAG));
        setInheritanceMode(ModuleEvaluator.getInheritanceMode(json.getString(MODULE_INHERITANCE_MODE_TAG)));
    }


    public void toXML(XMLStreamWriter xsw,
                      String alias,
                      boolean fullSerialization,
                      SerializationState SerializationState) throws XMLStreamException {
        xsw.writeStartElement(MODULE_TAG);

        if (fullSerialization) {
            xsw.writeAttribute(SerializationConstants.MODULE_NS_ATTR, getNamespace().toString());
            if (!(getAlias() == null && alias == null)) {
                xsw.writeAttribute(SerializationConstants.MODULE_ALIAS_ATTR, StringUtils.isTrivial(alias) ? getAlias() : alias);
            }
            xsw.writeAttribute(SerializationConstants.UUID_TAG, getId().toString());
            writeExtraXMLAttributes(xsw);
            State state = getState();
            // if(state != null) {
            if (!(isTemplate() || state == null)) {
                if (SerializationState.addState(state)) {
                    state.toXML(xsw, SerializationState);
                }
                //serializationObjects.stateMap.put(state.getUuid(), state);
            }

        } else {
            xsw.writeAttribute(SerializationConstants.UUID_TAG, getId().toString());
            if (!(isTemplate() || state == null)) {
                xsw.writeAttribute(SerializationConstants.STATE_REFERENCE_TAG, getState().getUuid().toString());
                SerializationState.stateMap.put(getState().getUuid(), getState());
            }

        }
        // Note there is documentation in the source code, but since we save the source,
        // there is no need to serialize it (or anything else in the source for that matter).
        writeExtraXMLElements(xsw); // Do this first so they get read first later on deserialization

        xsw.writeEndElement();
    }

    /**
     * Add any attributes you want to the module tag (you must read them later).
     *
     * @param xsw
     * @throws XMLStreamException
     */
    public void writeExtraXMLAttributes(XMLStreamWriter xsw) throws XMLStreamException {
        xsw.writeAttribute("inheritanceMode", ModuleEvaluator.getInheritanceMode(getInheritMode()));
    }

    /**
     * Write extra elements. You must control for the opening and closing tags. These are
     * inserted right after the state element.
     *
     * @param xsw
     * @throws XMLStreamException
     */
    public void writeExtraXMLElements(XMLStreamWriter xsw) throws XMLStreamException {
    }

    public boolean FDOC_CONVERT = true;

    /**
     * Used in version 2,0 serialization
     *
     * @param xer
     * @param SerializationState
     * @param isTemplate
     * @throws XMLStreamException
     */
    public void fromXML(XMLEventReader xer, SerializationState SerializationState, boolean isTemplate) throws XMLStreamException {
        readExtraXMLAttributes(xer.peek());
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    // *** IF *** it has a state object, process it.
                    switch (xe.asStartElement().getName().getLocalPart()) {
                        case MODULE_DOCUMENTATION_TAG:
                            setDocumentation(getListByTag(xer, MODULE_DOCUMENTATION_TAG));
                            break;
                        case MODULE_SOURCE_TAG:
                            QDLModule qdlModule = (QDLModule) this;
                            List<String> source = getListByTag(xer, MODULE_SOURCE_TAG);
                            // Version 2: The source is stored. In order to recover this module
                            // the only reliable way is to re-interpret the source and pilfer the
                            // ModuleStatement which contains the documentation, executable statements etc.
                            // Again, this is because any QDL module statement can be quite complex and
                            // attempting to somehow serialize its module statements is vastly harder (and twitchier)
                            // than using the parser.
                            State state = new State();
                            QDLInterpreter qdlInterpreter = new QDLInterpreter(state);
                            try {
                                String x = StringUtils.listToString(source);
                                x = FDOC_CONVERT ? x.replace(">>", "»") : x;
                                qdlInterpreter.execute(x);
                                if (state.getMTemplates().isEmpty()) {
                                    // fall through case -- nothing resulted.
                                    throw new IllegalStateException("no module found");
                                } else {
                                    // Get the actual interpreted ModuleStatement
                                    QDLModule tempM = (QDLModule) state.getMTemplates().getAll().get(0);
                                    qdlModule.setModuleStatement(tempM.getModuleStatement());
                                    qdlModule.setDocumentation(tempM.getModuleStatement().getDocumentation());
                                    qdlModule.setAlias(tempM.getAlias());
                                    qdlModule.setNamespace(tempM.getNamespace());
                                    qdlModule.setParentTemplateID(tempM.getParentTemplateID());
                                    qdlModule.setParentInstanceID(tempM.getParentInstanceID());
                                    qdlModule.setParentInstanceAlias(tempM.getParentInstanceAlias());
                                    // Note that the source is gotten from the ModuleStatement
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }

                            break;
                    }

                    if (xe.asStartElement().getName().getLocalPart().equals(STATE_TAG)) {
                        //    getState().fromXML(xer, xp);
                    } else {
                        readExtraXMLElements(xe, xer);  // contract is that it gets the start tag...
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(MODULE_TAG)) {
                        return;
                    }
                default:
                    break;
            }
            xer.next();
        }
        throw new XMLMissingCloseTagException(MODULE_TAG);

    }

    /**
     * Getting the documentation from the XML serialization 2.0.
     *
     * @param xer
     * @return
     * @throws XMLStreamException
     */
    protected List<String> getListByTag(XMLEventReader xer, String tag) throws XMLStreamException {
        List<String> documentation = null;
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            switch (xe.getEventType()) {
                case XMLEvent.CHARACTERS:
                    if (xe.asCharacters().isWhiteSpace()) {
                        break;
                    }
                    String raw = new String(Base64.decodeBase64(xe.asCharacters().getData()));
                    documentation = StringUtils.stringToList(raw);
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(tag)) {

                        return documentation;
                    }
            }
            xe = xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(tag);
    }

    public void fromXML(XMLEventReader xer, XProperties xp, QDLInterpreter qi) throws XMLStreamException {
        readExtraXMLAttributes(xer.peek());
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    // *** IF *** it has a state object, process it.
                    if (xe.asStartElement().getName().getLocalPart().equals(STATE_TAG)) {
                        getState().fromXML(xer, xp);
                    } else {
                        readExtraXMLElements(xe, xer);  // contract is that it gets the start tag...
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(MODULE_TAG)) {
                        return;
                    }
                default:
                    break;
            }
            xer.next();
        }
        throw new XMLMissingCloseTagException(MODULE_TAG);

    }

    /**
     * This is passed the current event and should only have calls to read the attributes.
     *
     * @param xe
     * @throws XMLStreamException
     */
    public void readExtraXMLAttributes(XMLEvent xe) throws XMLStreamException {

    }

    /**
     * This passes in the current start event so you can add your own event loop and cases.
     * Note you need have only a switch on the tag names you want.
     *
     * @param xe
     * @param xer
     * @throws XMLStreamException
     */
    public void readExtraXMLElements(XMLEvent xe, XMLEventReader xer) throws XMLStreamException {

    }

    public abstract List<String> getListByTag();

    public abstract void setDocumentation(List<String> documentation);

    public abstract List<String> getDocumentation();

    public UUID getParentTemplateID() {
        return parentTemplateID;
    }

    public void setParentTemplateID(UUID parentTemplateID) {
        this.parentTemplateID = parentTemplateID;
    }

    UUID parentTemplateID;

    public UUID getParentInstanceID() {
        return parentInstanceID;
    }

    public void setParentInstanceID(UUID parentInstanceID) {
        this.parentInstanceID = parentInstanceID;
    }

    UUID parentInstanceID = null;

    public String getParentInstanceAlias() {
        return parentInstanceAlias;
    }

    public void setParentInstanceAlias(String parentInstanceAlias) {
        this.parentInstanceAlias = parentInstanceAlias;
    }

    String parentInstanceAlias = null;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    UUID id = UUID.randomUUID();

    public int getInheritMode() {
        return inheritMode;
    }

    public void setInheritanceMode(int inheritMode) {
        this.inheritMode = inheritMode;
    }

    int inheritMode = ModuleEvaluator.IMPORT_STATE_ANY_VALUE;

// {@link #createDefaultDocs()}
    /**
     * The  will create basic documentation for functions and such,
     * and is called automatically during module initialization,
     * but the actual description of this module -- if any -- is done here. Override and return your description.
     *
     * @return
     */
    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    List<String> description;
    public abstract List<String> createDefaultDocs();

}
