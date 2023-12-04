package edu.uiuc.ncsa.qdl.state;

import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.util.ModuleUtils;
import edu.uiuc.ncsa.qdl.xml.SerializationState;
import edu.uiuc.ncsa.qdl.xml.XMLMissingCloseTagException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * A symbol table. This should hold functions modules and (eventually) variables.
 * Sequences of these are managed by {@link XStack}.
 * <p>Created by Jeff Gaynor<br>
 * on 11/7/21 at  5:14 AM
 */
public abstract class XTable<K extends XKey, V extends XThing> extends HashMap<K, V> implements Cloneable, Serializable {
    protected boolean FDOC_CONVERT = true; // allows for older workspaces to be read. Remove this later

    protected String convertFDOC(String x) {
        return x.replace(">>", "»");
    }

    /**
     * Should add the {@link XThing} based on its {@link XThing#getName()} as the key.
     *
     * @param value
     * @return
     */
    public V put(XThing value) {
        return put((K) value.getKey(), (V) value);
    }

    /**
     * @param xsw
     * @param SerializationState
     * @throws XMLStreamException
     * @deprecated
     */
    public abstract void toXML(XMLStreamWriter xsw, SerializationState SerializationState) throws XMLStreamException;

    public abstract String toJSONEntry(V xThing, SerializationState serializationState);

    public abstract String fromJSONEntry(String x, SerializationState serializationState);

    /**
     * @param xer
     * @param qi
     * @throws XMLStreamException
     * @deprecated
     */
    public abstract void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException;

    /**
     * Version 2.0 serialization
     *
     * @param xer
     * @param SerializationState
     * @throws XMLStreamException
     */

    public void fromXML(XMLEventReader xer, SerializationState SerializationState) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(getXMLElementTag())) {
                        put(deserializeElement(xer, SerializationState, null)); // no interpreter needed
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(getXMLTableTag())) {
                        return;
                    }
            }
            xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(getXMLTableTag());
    }

    /**
     * @return
     * @deprecated
     */
    public abstract String getXMLTableTag();

    /**
     * @return
     * @deprecated
     */
    public abstract String getXMLElementTag();

    /**
     * @param xer
     * @param SerializationState
     * @param qi
     * @return
     * @throws XMLStreamException
     * @deprecated
     */
    public abstract V deserializeElement(XMLEventReader xer, SerializationState SerializationState, QDLInterpreter qi) throws XMLStreamException;


    UUID uuid = UUID.randomUUID();

    public UUID getID() {
        return uuid;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "uuid=" + uuid +
                "size=" + size() +
                '}';
    }

    public JSONArray serializeToJSON(SerializationState serializationState) {
        if(isEmpty()){return null;}
        JSONArray array = new JSONArray();
        for (XKey xKey : keySet()) {
            V v = get(xKey);
            // in some cases there is nothing to serialize (e.g. java module functions). Skip them
            JSONObject x = serializeToJSON(v, serializationState);
            if(x!=null)array.add(x);
        }
        if(array.isEmpty()) return null;
        return array;
    }

    public ModuleUtils getModuleUtils() {
        if(moduleUtils == null){
            moduleUtils = new ModuleUtils();
        }
        return moduleUtils;
    }

    ModuleUtils moduleUtils = null;
    public abstract JSONObject serializeToJSON(V xThing, SerializationState serializationState) ;
    public abstract void deserializeFromJSON(JSONObject json,  QDLInterpreter qi, SerializationState serializationState) throws Throwable;
}
