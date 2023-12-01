package edu.uiuc.ncsa.qdl.module;

import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.functions.FKey;
import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.state.XTable;
import edu.uiuc.ncsa.qdl.state.XThing;
import edu.uiuc.ncsa.qdl.statements.Documentable;
import edu.uiuc.ncsa.qdl.xml.SerializationState;
import edu.uiuc.ncsa.qdl.xml.XMLConstants;
import edu.uiuc.ncsa.qdl.xml.XMLUtils;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONObject;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import static edu.uiuc.ncsa.qdl.xml.XMLConstants.MODULE_TAG;

/**
 * Table of modules keyed by <b>alias</b>.
 * <p>Created by Jeff Gaynor<br>
 * on 12/1/21 at  1:03 PM
 */
public class MITable<K extends XKey, V extends MIWrapper> extends XTable<K, V> implements Documentable {


    public V put(XKey xKey, XThing xThing) {
        return super.put((K) xKey, (V) xThing);
    }

    @Override
    public void toXML(XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        for (XKey key : keySet()) {
            xsw.writeStartElement(getXMLElementTag());
            MIWrapper wrapper = get(key);
            Module module = wrapper.getModule();
            xsw.writeAttribute(XMLConstants.UUID_TAG, module.getId().toString());
            xsw.writeAttribute(XMLConstants.TEMPLATE_REFERENCE_TAG, module.getParentTemplateID().toString());
            if(module.getParentInstanceID()!=null) {
                xsw.writeAttribute(XMLConstants.PARENT_INSTANCE_ALIAS_TAG, module.getParentInstanceID().toString());
            }
            if(!StringUtils.isTrivial(module.getParentInstanceAlias())) {
                xsw.writeAttribute(XMLConstants.PARENT_INSTANCE_ALIAS_TAG, module.getParentInstanceAlias());
            }
            xsw.writeAttribute(XMLConstants.MODULE_ALIAS_ATTR, key.getKey()); // What this was imported as
            xsw.writeAttribute(XMLConstants.STATE_REFERENCE_TAG, module.getState().getInternalID());
            xsw.writeEndElement(); // end module tag
        }
    }

    @Override
    public String getXMLTableTag() {
        return XMLConstants.MODULES_TAG;
    }

    @Override
    public String getXMLElementTag() {
        return MODULE_TAG;
    }

    public V deserializeElement(XMLUtils.ModuleAttributes moduleAttributes, SerializationState serializationState) {
        if (serializationState.processedInstance(moduleAttributes.uuid)) {
            return (V) serializationState.getInstance(moduleAttributes.uuid);
        }
        if (!serializationState.processedTemplate(moduleAttributes.templateReference)) {
            throw new IllegalStateException("template '" + moduleAttributes.uuid + "' not found");
        }
        Module template = serializationState.getTemplate(moduleAttributes.templateReference);
        Module newInstance = template.newInstance(null);
        State state;
        if (serializationState.processedState(moduleAttributes.stateReference)) {
            state = serializationState.getState(moduleAttributes.stateReference);
        } else {
            // edge case that the state does not yet exist, so create a new one, assuming that it
            // will get populated later.
            state = new State();
            state.setUuid(moduleAttributes.stateReference);
            serializationState.addState(state);
        }
        newInstance.setState(state);
        newInstance.setId(moduleAttributes.uuid);
        // now stash it with whatever it was stashed with
        MIWrapper miWrapper = new MIWrapper(new XKey(moduleAttributes.alias), newInstance);
        serializationState.addInstance(miWrapper);
        return (V) miWrapper;
    }

    @Override
    public V deserializeElement(XMLEventReader xer, SerializationState serializationState, QDLInterpreter qi) throws XMLStreamException {
        XMLEvent xe = xer.peek();
        XMLUtils.ModuleAttributes moduleAttributes = XMLUtils.getModuleAttributes(xe);
        return deserializeElement(moduleAttributes, serializationState);
    }


    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {
        throw new NotImplementedException("implement me for XML version 1 legacy support.");
    }

    @Override
    public TreeSet<String> listFunctions(String regex) {
        return null;
    }

    @Override
    public List<String> listAllDocs() {
        return null;
    }

    @Override
    public List<String> listAllDocs(String functionName) {
        return null;
    }

    @Override
    public List<String> getDocumentation(String fName, int argCount) {
        return null;
    }

    @Override
    public List<String> getDocumentation(FKey key) {
        if (get(key) == null) {
            return new ArrayList<>(); // never null
        } else {
            return get(key).getModule().getListByTag();
        }
    }

    UUID id = UUID.randomUUID();

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public String toJSONEntry(V wrapper, SerializationState serializationState) {
        return serializeToJSON(wrapper, serializationState).toString();
    }

    @Override
    public JSONObject serializeToJSON(V wrapper, SerializationState serializationState) {
        Module module = wrapper.getModule();
        if(serializationState.getVersion().equals(XMLConstants.VERSION_2_0_TAG)){
            K key = (K) wrapper.getKey();
            XMLUtils.ModuleAttributes moduleAttributes = new XMLUtils.ModuleAttributes();
            moduleAttributes.uuid = module.getId();
            moduleAttributes.alias = key.getKey();
            moduleAttributes.templateReference = module.parentTemplateID;
            moduleAttributes.stateReference = module.getState().getUuid();
            return moduleAttributes.toJSON();
        }
        return module.serializeToJSON(serializationState);
    }

    @Override
    public String fromJSONEntry(String x, SerializationState serializationState) {
        XMLUtils.ModuleAttributes moduleAttributes = new XMLUtils.ModuleAttributes();
        moduleAttributes.fromJSON(x);
        V m = deserializeElement(moduleAttributes, serializationState);
        put(new XKey(moduleAttributes.alias), m);
        if(m.getModule() instanceof JavaModule){
            ((JavaModule)m.getModule()).init(m.getModule().getState(), false);
        }
        return null; // this returns what the interpreter should process. Nothing in this case.
    }

    @Override
    public void deserializeFromJSON(JSONObject json, QDLInterpreter qi, SerializationState serializationState) {

    }
}
