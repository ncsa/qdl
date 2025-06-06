package org.qdl_lang.expressions.module;

import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.XKey;
import org.qdl_lang.state.XTable;
import org.qdl_lang.state.XThing;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLUtils;
import net.sf.json.JSONObject;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.qdl_lang.xml.SerializationConstants.MODULE_TAG;

/**
 * Module template table.
 * <p>Created by Jeff Gaynor<br>
 * on 12/14/21 at  12:22 PM
 */
public class MTTable<K extends MTKey, V extends Module>  extends   XTable<K, V> {
    @Override
    public V put(XThing value) {
        MTKey mtKey = ((Module) value).getMTKey();
        return put(mtKey,  value);
    }

    private V put(MTKey mtKey, XThing value) {
        return super.put( (K)mtKey,  (V)value);

    }

    @Override
    public String getXMLTableTag() {
        return SerializationConstants.MODULES_TAG;
    }

    @Override
    public String getXMLElementTag() {
        return MODULE_TAG;
    }

    @Override
    public void toXML(XMLStreamWriter xsw, SerializationState SerializationState) throws XMLStreamException {
          for(XKey key : keySet()){
              xsw.writeStartElement(getXMLElementTag());
              Module module = get(key);
              xsw.writeAttribute(SerializationConstants.UUID_TAG, module.getId().toString());
              SerializationState.templateMap.put(module.getId(), module);
              xsw.writeEndElement(); // end module tag
          }
    }

    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {

    }

    public V deserializeElement(XMLUtils.ModuleAttributes moduleAttributes, SerializationState SerializationState)  {
        if(!SerializationState.processedTemplate(moduleAttributes.uuid)){
            throw new IllegalStateException("template '" + moduleAttributes.uuid + "' not found");
        }
        return (V) SerializationState.getTemplate(moduleAttributes.uuid);
    }


    @Override
    public V deserializeElement(XMLEventReader xer, SerializationState SerializationState, QDLInterpreter qi) throws XMLStreamException {
        XMLEvent xe = xer.peek();
        XMLUtils.ModuleAttributes moduleAttributes = XMLUtils.getModuleAttributes(xe);
        return deserializeElement(moduleAttributes, SerializationState);
    }

    public void clearChangeList(){
        changeList = new ArrayList<>();
    }
    // On updates, the change list will track additions or replacements.
    // clear it before updates, read it it after, then clear it again.
    public List<URI> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<URI> changeList) {
        this.changeList = changeList;
    }

    List<URI> changeList = new ArrayList<>();

    public V put(URI key, V value) {
        changeList.add(key);
        return super.put((K) new MTKey(key), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(K key : m.keySet()){
            getChangeList().add(key.getUriKey());
        }
        super.putAll(m);
    }

    UUID id = UUID.randomUUID();

    @Override
    public UUID getID() {
        return id;
    }

    @Override
    public String toJSONEntry(V xThing, SerializationState serializationState) throws Throwable {
        serializationState.templateMap.put(xThing.getId(), xThing);
        return serializeToJSON(xThing, serializationState).toString();
    }

    @Override
    public JSONObject serializeToJSON(V xThing, SerializationState serializationState) throws Throwable {
        if(serializationState.getVersion().equals(SerializationConstants.VERSION_2_0_TAG)){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(SerializationConstants.UUID_TAG, xThing.getId().toString());
            return jsonObject;
        }
        return xThing.serializeToJSON(serializationState);
    }

    @Override
    public String fromJSONEntry(String x, SerializationState serializationState) {
        XMLUtils.ModuleAttributes moduleAttributes = new XMLUtils.ModuleAttributes();
        moduleAttributes.fromJSON(x);
        V m = deserializeElement(moduleAttributes, serializationState);
        put(new MTKey(m.getNamespace()), m);
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json, QDLInterpreter qi, SerializationState serializationState) throws Throwable {
        getModuleUtils().deserializeFromJSON(qi.getState(), json, serializationState);

    }
}
