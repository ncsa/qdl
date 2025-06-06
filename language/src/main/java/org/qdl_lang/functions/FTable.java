package org.qdl_lang.functions;

import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.XKey;
import org.qdl_lang.state.XTable;
import org.qdl_lang.statements.Documentable;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLMissingCloseTagException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.xml.SerializationConstants.FUNCTIONS_TAG;
import static org.qdl_lang.xml.SerializationConstants.FUNCTION_TAG;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/19/21 at  7:48 AM
 */
public class FTable<K extends FKey, V extends FunctionRecord> extends XTable<K, V> implements Documentable {

    /**
     * If argCount === -1, remove all named functions, otherwise only remove the one with the
     * exact argCount.
     *
     * @param key
     * @return
     */
    @Override
    public V remove(Object key) {
        if (!(key instanceof FKey)) {
            throw new IllegalArgumentException(key + " is not an FKey");
        }
        FKey fKey = (FKey) key;
        if (fKey.getArgCount() == -1) {
            for (XKey key1 : keySet()) {
                if (((FKey) key1).hasName(fKey.getfName())) {
                    remove(key1);
                }
            }
            return null;
        }

        return super.remove(key);
    }

    public List<V> getByAllName(String name) {
        List<V> fList = new ArrayList<>();
        for (XKey key : keySet()) {
            if (((FKey) key).hasName(name)) {
                fList.add(get(key));
            }
        }
        return fList;
    }

    @Override
    public Collection<V> values() {
        List<V> fList = new ArrayList<>();
        for (XKey key : keySet()) {
            fList.add(get(key));
        }
        return fList;
    }


    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof FKey)) {
            throw new IllegalArgumentException(key + " is not an FKey");
        }
        FKey fkey = (FKey) key;
        if (fkey.getArgCount() == -1) {
            for (XKey key0 : keySet()) {
                if (((FKey) key0).hasName(fkey.getfName())) {
                    return true;
                }
            }

        }
        return super.containsKey(key);
    }

    /**
     * Return every function in this tables as a set of {@link DyadicFunctionReferenceNode}s.
     *
     * @param regex
     * @return
     */
    public Set<DyadicFunctionReferenceNode> listFunctionReferences(String regex) {
        Set<DyadicFunctionReferenceNode> fRefs = new TreeSet<>();
        for (XKey key : keySet()) {
            String name = ((FKey) key).getfName(); // de-munge
            FunctionRecordInterface fr = get(key);
            if (!(regex == null || regex.isEmpty() || name.matches(regex))) {
                continue;
            }

            DyadicFunctionReferenceNode dyadicFunctionReferenceNode = new DyadicFunctionReferenceNode();
            dyadicFunctionReferenceNode.setFunctionName(fr.getName());
            // Module stuff here?
            ArrayList<ExpressionInterface> args = new ArrayList<>();
            args.add(new ConstantNode(asQDLValue((fr.getArgCount()))));
            args.add(new ConstantNode(asQDLValue(fr.getName())));
            dyadicFunctionReferenceNode.setArguments(args);
            dyadicFunctionReferenceNode.setFunctionRecord(fr);
            dyadicFunctionReferenceNode.setEvaluated(true);
            fRefs.add(dyadicFunctionReferenceNode);
        }
        return fRefs;
    }


    @Override
    public TreeSet<String> listFunctions(String regex) {
        // The tree set keeps the argument counts in order since the functions are
        // iterated out of order. 
        HashMap<String, Set<Integer>> fAndArgs = new HashMap<>();

        for (XKey key : keySet()) {
            String name = ((FKey) key).getfName(); // de-munge
            FunctionRecordInterface fr = get(key);
            if (regex != null && !regex.isEmpty()) {
                if (name.matches(regex)) {
                    if (!fAndArgs.containsKey(name)) {
                        Set<Integer> list = new TreeSet<>();
                        fAndArgs.put(name, list);
                    }
                    fAndArgs.get(name).add(fr.getArgCount());
                }
            } else {
                if (!fAndArgs.containsKey(name)) {
                    Set<Integer> list = new TreeSet<>();
                    fAndArgs.put(name, list);
                }
                fAndArgs.get(name).add(fr.getArgCount());
            }
        }
        TreeSet<String> names = new TreeSet<>();
        for (String key : fAndArgs.keySet()) {
            String args = fAndArgs.get(key).toString();
            args = args.replace(" ", ""); // no blanks in arg list. Makes regexes easier
            names.add(key + "(" + args + ")");
        }
        return names;
    }

    /**
     * Just lists the first line of every function with documentation
     *
     * @return
     */
    @Override
    public List<String> listAllDocs() {
        ArrayList<String> docs = new ArrayList<>();
        for (XKey key : keySet()) {
            String name = ((FKey) key).getfName(); // de-munge
            FunctionRecord fr = get(key);
            name = name + "(" + fr.getArgCount() + ")";
            if (0 < fr.documentation.size()) {
                if (!fr.documentation.get(0).contains(name)) {
                    name = fr.documentation.get(0);
                } else {
                    name = name + ": " + fr.documentation.get(0);
                }
            } else {
                name = name + ": (none)";

            }
            docs.add(name);
        }

        return docs;
    }

    // Filter by fname.
    public List<String> listAllDocs(String fname) {
        ArrayList<String> docs = new ArrayList<>();
        for (XKey key : keySet()) {
            if (((FKey) key).hasName(fname)) {
                FunctionRecord fr = get(key);
                String name = fname + "(" + fr.getArgCount() + ")";
                if (0 < fr.documentation.size()) {
                    if (!fr.documentation.get(0).contains(name)) {
                        name = fr.documentation.get(0);
                    } else {
                        name = name + ": " + fr.documentation.get(0);
                    }
                } else {
                    name = name + ": (none)";

                }
                docs.add(name);
            }
        }
        return docs;
    }

    /**
     * Returns the specific documentation for a function. The request is of the form name(args);
     *
     * @param fName
     * @return
     */
    @Override
    public List<String> getDocumentation(String fName, int argCount) {
        throw new NotImplementedException("not implemented in XTables");
    }

    @Override
    public List<String> getDocumentation(FKey key) {
        if (get(key) == null) {
            return new ArrayList<>(); // never null
        } else {
            return get(key).documentation;
        }
    }

    /**
     * Writes every function in no particular order by its source code. Look at
     * {@link FStack#toXML(XMLStreamWriter, SerializationState)} for top level of functions
     */
    @Override
    public void toXML(XMLStreamWriter xsw, SerializationState SerializationState) throws XMLStreamException {
        for (XKey key : keySet()) {
            if (get(key).sourceCode.isEmpty()) {
                // No source code usually means it is from some external function
                // and we cannot recreate it.
                continue;
            }
            String name = ((FKey) key).getfName(); // de-munge

            xsw.writeStartElement(SerializationConstants.FUNCTION_TAG);
            xsw.writeAttribute(SerializationConstants.FUNCTION_NAME_TAG, name);
            xsw.writeAttribute(SerializationConstants.FUNCTION_ARG_COUNT_TAG, Integer.toString(get(key).getArgCount()));

            xsw.writeCData(StringUtils.listToString(get(key).sourceCode));
            xsw.writeEndElement();
        }
    }


    /**
     * Deserialize a single function using the interpreter (and its current state); This assumes that the function
     * will get stuffed into the current state and that will get processed.
     *
     * @param xer
     * @param qi
     * @throws XMLStreamException
     */
    public void processSingleFunction(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(FUNCTION_TAG)) {
                        return;
                    }
                case XMLEvent.CHARACTERS:
                    if (!xe.asCharacters().isIgnorableWhiteSpace()) {
                        String x = xe.asCharacters().getData();
                        x = FDOC_CONVERT ? convertFDOC(x) : x;
                        try {
                            qi.execute(x);
                        } catch (Throwable t) {
                            // should do something else??
                            System.err.println("Error deserializing function '" + x + "': " + t.getMessage());
                        }
                    }
            }
            xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(FUNCTION_TAG);
    }

    @Override
    public V deserializeElement(XMLEventReader xer, SerializationState SerializationState, QDLInterpreter qi) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        //Since this requires parsing from the source which can get extremely complex (that's why we have a parser)
        // about the only way to do this is to black box it, viz., look at the state (set of functions) beforehand
        // then let the magic happen and look at the set afterwords. Return the difference.
        Set<XKey> oldKeys = qi.getState().getFTStack().keySet();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    break;
                case XMLEvent.CHARACTERS:
                    if (!xe.asCharacters().isIgnorableWhiteSpace()) {
                        String x = xe.asCharacters().getData();
                        x = FDOC_CONVERT ? convertFDOC(x) : x;
                        try {
                            qi.execute(x);
                        } catch (Throwable t) {
                            // should do something else??
                            System.err.println("Error deserializing function '" + x + "': " + t.getMessage());
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(getXMLElementTag())) {
                        Set<XKey> newKeys = qi.getState().getFTStack().keySet();
                        newKeys.removeAll(oldKeys);
                        if (newKeys.isEmpty()) {
                            throw new IllegalStateException("no function found to deserialize");
                        }
                        if (newKeys.size() != 1) {
                            throw new IllegalStateException(newKeys.size() + " functions deserialized. A single one was expected");
                        }
                        return (V) qi.getState().getFTStack().get(newKeys.iterator().next());
                    }
                    break;

            }
            xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(FUNCTION_TAG);
    }

    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(FUNCTION_TAG)) {
                        processSingleFunction(xer, qi);
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(FUNCTIONS_TAG)) {
                        return;
                    }
            }
            xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(FUNCTIONS_TAG);
    }

    @Override
    public String getXMLTableTag() {
        return FUNCTIONS_TAG;
    }

    @Override
    public String getXMLElementTag() {
        return FUNCTION_TAG;
    }

    /*
     Big note on serializing functions. All that are serialized here are QDL native functions.
     The source is stored and on deserialization they are run through the interpreter again to
     populate this table. This is because such functions are extremely dynamic and can be redefined
     at any time. Java functions, however, live in Java code. When a module is deserialized, if it
     is a Java module, the function table is populated from the template since such functions live
     there and are immutable. 
     */
    @Override
    public String toJSONEntry(V xThing, SerializationState serializationState) {
        String src = StringUtils.listToString(xThing.sourceCode);
        return Base64.encodeBase64URLSafeString(src.getBytes(UTF_8));
    }

    public static final String FUNCTION_ENTRY_KEY = "entry";

    @Override
    public JSONObject serializeToJSON(V xThing, SerializationState serializationState) {
        JSONObject jsonObject = new JSONObject();
        String src = StringUtils.listToString(xThing.sourceCode);
        if (StringUtils.isTrivial(src)) {
            return null;
        } else {
            jsonObject.put(FUNCTION_ENTRY_KEY, Base64.encodeBase64URLSafeString(src.getBytes(UTF_8)));
        }
        return jsonObject;
    }


    @Override
    public void deserializeFromJSON(JSONObject json, QDLInterpreter qi, SerializationState serializationState) {
        String raw = new String(Base64.decodeBase64(json.getString(FUNCTION_ENTRY_KEY)), UTF_8);
        if (!StringUtils.isTrivial(raw)) {
            try {
                qi.execute(raw);
            } catch (Throwable t) {
                // should do something else??
                System.err.println("Error deserializing function '" + raw + "': " + t.getMessage());
            }

        }
    }

    @Override
    public String fromJSONEntry(String x, SerializationState serializationState) {
        // Conversion away from >> in function documentation. This allows for converting older
        // workspaces
        x = new String(Base64.decodeBase64(x));
        x = FDOC_CONVERT ? convertFDOC(x) : x;
        return x;
    }

}

