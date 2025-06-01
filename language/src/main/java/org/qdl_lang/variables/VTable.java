package org.qdl_lang.variables;

import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.XKey;
import org.qdl_lang.state.XTable;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.qdl_lang.xml.SerializationConstants.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/20/22 at  6:12 AM
 */
public class VTable<K extends XKey, V extends VThing> extends XTable<K, V> {
    public void toXML2(XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        xsw.writeStartElement("table");
        for (K key : keySet()) {
            toXMLEntry(get(key), xsw, serializationState);
        }
        xsw.writeEndElement();
    }

    /**
     * New format (version 2.1+) for non-modules is
     * <pre>
     *     &lt;e type="qdl"&gt;
     *        &lt;v&gt;base64_encoded_QDL&lt;/v&gt;
     *     &lt;/e&gt;
     * </pre>
     * and for modules it is
     * <pre>
     *     &lt;e type="module"&gt;
     *        &lt;v&gt;base64_encoded_QDL&lt;/v&gt;
     *        &lt;state inheritanceMode="..."&gt;serialized_module_state&lt;/state&gt;
     *     &lt;/e&gt;
     * </pre>
     *
     * @param vThing
     * @param xsw
     * @param serializationState
     * @throws XMLStreamException
     */
    protected void toXMLEntry(V vThing, XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        xsw.writeStartElement("e");
        xsw.writeAttribute("k", vThing.getKey().getKey());
        Module m = null;
        if (vThing.getVariable().getQDLValue().isModule()) {
            m = vThing.getVariable().getQDLValue().asModule();
        }
        if (m == null) {
            xsw.writeAttribute("type", "qdl");

        } else {
            xsw.writeAttribute("type", "module");
            xsw.writeAttribute("inheritanceMode", ModuleEvaluator.getInheritanceMode(m.getInheritMode()));
            xsw.writeAttribute("uuid", m.getId().toString());
            xsw.writeAttribute("namespace", m.getNamespace().toString());

        }
        xsw.writeStartElement("v");
        xsw.writeCData(toJSONEntry(vThing, serializationState));
        if (m != null) {
            if (m.getState() != null) {
                m.getState().toXML(xsw, serializationState);
            }
        }
        xsw.writeEndElement(); // end the value entry
        xsw.writeEndElement(); //end the variable entry
    }

    public static final String KEY_KEY = "key";
    public static final String VALUE_KEY = "value";

    /**
     * This serializes an entry. The contract is that these are knitted into an array.
     *
     * @param xThing
     * @param serializationState
     * @return
     */
    @Override
    public JSONObject serializeToJSON(V xThing, SerializationState serializationState) throws Throwable {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_KEY, xThing.getKey().getKey());
        String raw;
        Module m = null;
        if (xThing.getVariable().getQDLValue().isModule()) {
            m =  xThing.getVariable().getQDLValue().asModule();
        }
        if (m == null) {
            raw = xThing.getKey().getKey() + ":=" + InputFormUtil.inputForm(xThing.getVariable().getQDLValue()) + ";";
            jsonObject.put(TYPE_TAG, QDL_TYPE_TAG);
            // Base 64 encode the raw QDL since it is possible to get escaping issues.
            // Just side-step the whole issue.
            raw = Base64.encodeBase64URLSafeString(raw.getBytes(StandardCharsets.UTF_8));
            jsonObject.put(VALUE_KEY, raw);
        } else {
            jsonObject.putAll(m.serializeToJSON(serializationState));
            jsonObject.put(TYPE_TAG, MODULE_TAG);
        }
        return jsonObject;
    }


    @Override
    public void deserializeFromJSON(JSONObject json, QDLInterpreter qi, SerializationState serializationState) throws Throwable {

        if (json.getString(TYPE_TAG).equals(QDL_TYPE_TAG)) {
            try {
                String raw = new String(Base64.decodeBase64(json.getString(VALUE_KEY)), StandardCharsets.UTF_8);
                qi.execute(raw);
            } catch (Throwable e) {
              //  e.printStackTrace();
            }
        }
        if (json.getString(TYPE_TAG).equals(MODULE_TAG)) {
            getModuleUtils().deserializeFromJSON(qi.getState(), json, serializationState);
        }
    }

    /*
            nested module for testing
                module['a:x'][module['a:y'][f(x,y)->x*y;q:=3;];y:=import('a:y');r:=5;];
                x:=import('a:x');
                x#y#q:=11
                x#r=7
                )save -show
             */
    protected void fromXMLEntry(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();

    }

    @Override
    public void toXML(XMLStreamWriter xsw, SerializationState SerializationState) throws XMLStreamException {

    }

    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {

    }

    // Next two tags are old

    @Override
    public String getXMLTableTag() {
        return SerializationConstants.FUNCTIONS_TAG;
    }

    @Override
    public String getXMLElementTag() {
        return SerializationConstants.FUNCTION_TAG;
    }

    @Override
    public V deserializeElement(XMLEventReader xer, SerializationState SerializationState, QDLInterpreter qi) throws XMLStreamException {
        return null;
    }

    @Override
    public String toJSONEntry(V xThing, SerializationState serializationState) {
        String raw;
        if (xThing.getVariable().getQDLValue().isModule()) {
            raw = InputFormUtil.inputForm(xThing.getVariable().getQDLValue()) + ";";
        } else {
            raw = xThing.getKey().getKey() + ":=" + InputFormUtil.inputForm(xThing.getVariable().getQDLValue()) + ";";
        }
        return Base64.encodeBase64URLSafeString(raw.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String fromJSONEntry(String x, SerializationState serializationState) {
        if (serializationState == null) {
            // old form
            return x;
        }
        return new String(Base64.decodeBase64(x));
    }

    public Set<String> listVariables() {
        Set<String> vars = new HashSet<>();
        for (XKey xKey : keySet()) {
            vars.add(xKey.getKey());
        }
        return vars;
    }

}
