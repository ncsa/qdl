package org.qdl_lang.expressions.module;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.state.XTable;
import org.qdl_lang.state.XThing;
import org.qdl_lang.statements.Documentable;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLUtils;
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

import static org.qdl_lang.xml.SerializationConstants.*;

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
            xsw.writeAttribute(SerializationConstants.UUID_TAG, module.getId().toString());
            xsw.writeAttribute(SerializationConstants.TEMPLATE_REFERENCE_TAG, module.getParentTemplateID().toString());
            if (module.getParentInstanceID() != null) {
                xsw.writeAttribute(SerializationConstants.PARENT_INSTANCE_ALIAS_TAG, module.getParentInstanceID().toString());
            }
            if (!StringUtils.isTrivial(module.getParentInstanceAlias())) {
                xsw.writeAttribute(SerializationConstants.PARENT_INSTANCE_ALIAS_TAG, module.getParentInstanceAlias());
            }
            xsw.writeAttribute(SerializationConstants.MODULE_ALIAS_ATTR, key.getKey()); // What this was imported as
            xsw.writeAttribute(SerializationConstants.STATE_REFERENCE_TAG, module.getState().getInternalID());
            xsw.writeEndElement(); // end module tag
        }
    }

    @Override
    public String getXMLTableTag() {
        return SerializationConstants.MODULES_TAG;
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
    public String toJSONEntry(V wrapper, SerializationState serializationState) throws Throwable {
        return serializeToJSON(wrapper, serializationState).toString();
    }

    @Override
    public JSONObject serializeToJSON(V wrapper, SerializationState serializationState) throws Throwable {
        Module module = wrapper.getModule();
        K key = (K) wrapper.getKey();
        if (serializationState.getVersion().equals(SerializationConstants.VERSION_2_0_TAG)) {
            XMLUtils.ModuleAttributes moduleAttributes = new XMLUtils.ModuleAttributes();
            moduleAttributes.uuid = module.getId();
            moduleAttributes.alias = key.getKey();
            moduleAttributes.templateReference = module.parentTemplateID;
            moduleAttributes.stateReference = module.getState().getUuid();
            return moduleAttributes.toJSON();
        }
        JSONObject m = module.serializeToJSON(serializationState);
        m.put(MODULE_IS_INSTANCE_TAG, true);
        m.put(MODULE_IS_TEMPLATE_TAG, false);
        m.put(MODULE_ALIAS_ATTR,key.getKey() );
        return m;
    }

    @Override
    public String fromJSONEntry(String x, SerializationState serializationState) {
        if (serializationState.getVersion().equals(SerializationConstants.VERSION_2_0_TAG)) {
            XMLUtils.ModuleAttributes moduleAttributes = new XMLUtils.ModuleAttributes();
            moduleAttributes.fromJSON(x);
            V m = deserializeElement(moduleAttributes, serializationState);
            put(new XKey(moduleAttributes.alias), m);
            if (m.getModule() instanceof JavaModule) {
                ((JavaModule) m.getModule()).init(m.getModule().getState(), false);
            }
            return null; // this returns what the interpreter should process. Nothing in this case.
        }
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json, QDLInterpreter qi, SerializationState serializationState) throws Throwable {
        getModuleUtils().deserializeFromJSON(qi.getState(), json, serializationState);
    }

}
