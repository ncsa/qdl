package org.qdl_lang.state;

import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.functions.FStack;
import org.qdl_lang.functions.FunctionRecord;
import org.qdl_lang.module.QDLModule;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.statements.ModuleStatement;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.VStack;
import org.qdl_lang.variables.VThing;
import org.qdl_lang.xml.SerializationState;
import org.qdl_lang.xml.XMLUtilsV2;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import static org.qdl_lang.xml.SerializationConstants.STACK_TAG;

/**
 * A stateful stack of things, such as functions. This is the method by which local state
 * is preserved. The zero-th element is the current local table. It is used for functions,
 * variables, modules etc., hence the prefix of <b>X</b> for it and things related to it.
 * <h3>Usage</h3>
 * <p>Create a subclass as needed for your objects. This involves an {@link XStack},
 * {@link XTable}, {@link XKey} and an {@link XThing}.</p>
 * <h3>How state is managed</h3>
 * If we had the following QDL:
 * <pre>
 *     f(x)-&gt;x^2;
 *     block[f(x)-&gt;x^3;...]
 * </pre>
 * Then {@link XStack} subclass for functions inside the block would look like
 * <pre>
 *     table       entry
 *     0           f(x)-&gt;x^3
 *     1           f(x)-&gt;x^2
 * </pre>
 * Calls to  {@link XStack#get(XKey)} would peek at 0 and return f(x)-&gt;x^3
 * inside the block. This is how local state overrides the parent state.
 * Blocks of course can be for loops, functions, conditionals etc. Were there no
 * entry for f(x) in the block, then {@link XStack} would return f(x)-&gt;x^2.
 *
 * <p>Created by Jeff Gaynor<br>
 * on 11/8/21 at  6:27 AM
 */
public abstract class XStack<V extends XTable<? extends XKey, ? extends XThing>> implements Serializable {
    /**
     * Clears the entire stack and resets it.
     */
    public void clear() {
        getStack().clear();
        pushNewTable();
    }

    /**
     * Take an XStack and prepend in the correct order
     * to the front of the stack. If xStack is [A,B,C,...] And the existing stack is
     * [P,Q,...] the result is [A,B,C,...,P,Q,...]
     * This is needed when, e.g., creating new local state for function reference resolution
     * <br/><br/>
     * <b>Note:</b> {@link #get(XKey)} starts from index 0, so local overrides are first!
     *
     * @param xStack
     */
    public void addTables(XStack xStack) {
        // add backwards from stack
        for (int i = xStack.getStack().size() - 1; 0 <= i; i--) {
            XTable xt = (XTable) xStack.getStack().get(i);
            if (addedTables.contains(xt.getID())) {
                continue;
            }
            if (!xt.isEmpty()) {
                push((XTable) xStack.getStack().get(i)); // puts at 0th elements each time
                addedTables.add(xt.getID());
            }
        }
    }

    Set<UUID> addedTables = new HashSet<>();

    /**
     * Similar to {@link #addTables(XStack)}, but this appends them to the existing
     * set of tables. If XStack is [A,B,C,...] And the existing stack is
     * [P,Q,...] the result is [P,Q,...,A,B,C,...,]
     * <br/><br/>
     * <b>Note:</b> {@link #get(XKey)} starts from index 0, so local overrides are first!
     *
     * @param xStack
     */
    public void appendTables(XStack xStack) {
        //for (int i = xStack.getStack().size() - 1; 0 <= i; i--) {
        for (int i = 0; i < xStack.getStack().size(); i++) {
            XTable xt = (XTable) xStack.getStack().get(i);
            if (addedTables.contains(xt.getID())) {
                continue;
            }
            if (!xt.isEmpty()) {
                append((V) xStack.getStack().get(i)); // puts at 0th elements each time
                addedTables.add(xt.getID());
            }
        }
    }

    abstract public XStack newInstance();

    abstract public XTable newTableInstance();

    /**
     * Append the table to the end of the stack -- this sets the root for the table.
     *
     * @param v
     */
    public void append(V v) {
        getStack().add(v);
    }

    @Override
    public XStack clone() {
        XStack cloned = newInstance();
        for (XTable ft : getStack()) {
            cloned.append(ft);
        }
        return cloned;
    }

    /**
     * Check that a specific key is in a table starting at the index.
     * This lets you, e.g., skip local state if the start index is positive.
     *
     * @param key
     * @param startTableIndex
     * @return
     */
    public boolean containsKey(XKey key, int startTableIndex) {
        for (int i = startTableIndex; i < getStack().size(); i++) {
            if (getStack().get(i).containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsKey(XKey key) {
        return containsKey(key, 0);
    }

    /**
     * Only returns a non-null element if it is defined in the local (index 0) table.
     *
     * @param key
     * @return
     */
    public XThing localGet(XKey key) {
        if (getStack().isEmpty()) {
            return null;
        }
        return getLocal().get(key);
    }

    /**
     * Get the local table for this stack.
     *
     * @return
     */
    public XTable<? extends XKey, ? extends XThing> getLocal() {
        return getStack().get(0);
    }

    public boolean localHas(XKey xkey) {
        if (getStack().isEmpty()) {
            return false;
        }
        return getLocal().get(xkey) != null;

    }

    public XThing get(XKey key) {
        for (XTable<? extends XKey, ? extends XThing> xTable : getStack()) {
            XThing xThing = xTable.get(key);
            if (xThing != null) {
                return xThing;
            }
        }
        return null;
    }

    /**
     * searches for the entry every place except the most local state.
     *
     * @param key
     * @return
     */
    public XThing nonlocalGet(XKey key) {
        for (int i = 1; i < getStack().size(); i++) {
            XThing xThing = getStack().get(i).get(key);
            if (xThing != null) {
                return xThing;
            }
        }
        return null;
    }

    /**
     * Get all of the values from all tables.This returns a flat list.
     *
     * @return
     */
    public List<? extends XThing> getAll() {
        List<? extends XThing> list = new ArrayList<>();
        for (XTable xTable : getStack()) {
            list.addAll(xTable.values());
        }
        return list;
    }

    /**
     * Since all new tables are added at 0, the initial one, called the root, is last. This gets
     * the root {@link XTable}.
     *
     * @return
     */
    public XTable<? extends XKey, ? extends XThing> getRoot() {
        return getStack().get(getStack().size() - 1);
    }

    public List<XTable<? extends XKey, ? extends XThing>> getStack() {
        if (stack == null) {
            stack = new ArrayList();
        }
        return stack;
    }

    public void setStack(List<XTable<? extends XKey, ? extends XThing>> stack) {
        this.stack = stack;
    }

    List<XTable<? extends XKey, ? extends XThing>> stack = new ArrayList<>();

    public boolean isEmpty() {
        boolean empty = true;
        for (XTable<? extends XKey, ? extends XThing> xTable : getStack()) {
            empty = empty && xTable.isEmpty();
        }
        return empty;
    }

    public XTable<XKey, XThing> peek() {
        return (XTable<XKey, XThing>) getStack().get(0);
    }

    public void push(XTable<? extends XKey, ? extends XThing> xTable) {
        getStack().add(0, xTable);
    }

    public void pushNewTable() {
        push(newTableInstance());
    }


    /**
     * Only add this to the local state.
     *
     * @param value
     * @return
     */
    public XThing localPut(XThing value) {
        XThing oldValue = getStack().get(0).get(value);
        getStack().get(0).put(value);
        return oldValue;
    }

    public XThing put(XKey xKey, XThing xThing) {
        for (XTable<? extends XKey, ? extends XThing> xTable : getStack()) {
            if (xTable.containsKey(xKey)) {
                xTable.put(xThing);
                return xThing;
            }
        }
        return peek().put(xKey, xThing);
    }

    public XThing put(XThing value) {
        for (XTable<? extends XKey, ? extends XThing> xTable : getStack()) {
            if (xTable.containsKey(value.getKey())) {
                xTable.put(value);
                return value;
            }
        }
        return peek().put(value);
    }

    /**
     * Removes only the most local entry.
     *
     * @param key
     */
    public void localRemove(XKey key) {
        peek().remove(key);
    }

    /**
     * Removes <i>all</i> references from all tables. This includes all overrides
     * so at the end of this operation there are no references any place.
     *
     * @param key
     */
    public void remove(XKey key) {
        for (XTable<? extends XKey, ? extends XThing> xTable : getStack()) {
            xTable.remove(key);
            addedTables.remove(xTable.getID());
        }
    }

    public int size() {
        int totalSymbols = 0;
        for (XTable xTable : getStack()) {
            totalSymbols += xTable.size();
        }
        return totalSymbols;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean showKeys) {
        String out = "[" + getClass().getSimpleName();
        out = out + ", table#=" + getStack().size();
        int i = 0;
        int totalSymbols = 0;
        boolean isFirst = true;
        String keys = "keys=[";
        out = ", counts=[";
        for (XTable xTable : getStack()) {
            if (isFirst) {
                isFirst = false;
                out = out + xTable.size();
                keys = keys + xTable.keySet();
            } else {
                out = out + "," + xTable.size();
                keys = keys + "," + xTable.keySet();
            }
            totalSymbols += xTable.size();
        }
        out = out + "], total=" + totalSymbols;
        keys = keys + "]";
        out = out + "]";
        if (showKeys) {
            out = out + "," + keys;
        }
        return out;
    }

    /**
     * Does the grunt work of writing the stack in the right order. You write the start tag,
     * any comments, invoke this, then the end tag. See {@link FStack#toXML(XMLStreamWriter, SerializationState)}
     * for a canonical basic.
     *
     * @param xsw
     * @throws XMLStreamException
     */
    public void toXML(XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        toXMLNEW(xsw, serializationState);
    }


    public void fromXML(XMLEventReader xer, SerializationState SerializationState) throws XMLStreamException {
        fromXMLNEW(xer, SerializationState);
    }


    abstract public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException;

    abstract public String getXMLStackTag();

    /**
     * @return
     * @deprecated
     */
    abstract public String getXMLTableTag();

    /**
     * Returns the <b><i>unique</i></b> set of keys over the tables.
     *
     * @return
     */
    public Set<XKey> keySet() {
        HashSet<XKey> uniqueKeys = new HashSet<>();
        for (XTable<? extends XKey, ? extends XThing> table : getStack()) {
            uniqueKeys.addAll(table.keySet());
        }
        return uniqueKeys;
    }

    /**
     * Returns the a list of keys (including redundancies) for this stack.
     *
     * @return
     */
    public List<XKey> allKeys() {
        ArrayList arrayList = new ArrayList();
        for (XTable<? extends XKey, ? extends XThing> table : getStack()) {
            arrayList.addAll(table.keySet());
        }
        return arrayList;
    }

    public JSONArray toJSON(SerializationState serializationState) throws Throwable {
        JSONArray array = new JSONArray();
        for (XTable xTable : getStack()) {
            JSONArray jsonArray = new JSONArray();
            for (Object key : xTable.keySet()) {
                XThing xThing = (XThing) xTable.get(key);
                String x = xTable.toJSONEntry(xThing, serializationState);
                jsonArray.add(x);
            }
            array.add(jsonArray);
        }
        return array;
    }

    /**
     * This sets the stack corresponding to this class from the state with the given stack.
     * If the stack is not of the correct type, a class cast exception will result. <br/><br/>
     * We <i>could</i> have tried this with some type of dynamic casting, but that is messy and fragile in Java
     *
     * @param state
     * @param xStack
     */
    public abstract void setStateStack(State state, XStack xStack);

    /**
     * This gets the stack corresponding to this class from the state..
     *
     * @param state
     * @return
     */

    public abstract XStack getStateStack(State state);


    /**
     * Processes the array of arrays. Each table is turned into an array of entries.
     * The stack is then an array of these (so we have order of the tables).
     * This will take the entire set of items and recreate the stack structure.
     *
     * @param array
     * @param serializationState
     */
    public void fromJSON(JSONArray array, SerializationState serializationState) {
        // To recreate the various states, we still need to use the parser and essentially
        // create local state repeatedly. The aim is to be as faithful as possible to
        // recreating the serialized stack.
        getStack().clear();
        XStack scratch = newInstance();
        State state = State.getRootState().newCleanState();
        setStateStack(state, scratch);

        // remember that we just need an interpreter to operate on the state, so any will do.
        QDLInterpreter qi = new QDLInterpreter(state);

        for (int i = 0; i < array.size(); i++) {
            // these were put in, in reverse order, so have to pop them out.
            scratch.getStack().clear();
            XTable currentST = newTableInstance();
            scratch.push(currentST);

            JSONArray jsonArray = array.getJSONArray(i);
            for (int k = 0; k < jsonArray.size(); k++) {
                try {
                    String xx = jsonArray.getString(k);
                    qi.execute(currentST.fromJSONEntry(xx, serializationState));
                } catch (Throwable e) {
                    // For now
                    e.printStackTrace();
                }
            }
            // since this is the stack in the interpreter state, this pushes a new table there
            getStack().add(currentST);
        }
    }

    protected void fromXMLNEW(XMLEventReader xer, SerializationState serializationState) throws XMLStreamException {
        // points to stacks tag
        JSONArray jsonArray = getJSON(xer);
        fromJSON(jsonArray, serializationState);
        // All the work is done in the previous call. Now advance the cursor (should only be whitespace
        // left) until you can exit for the next element.
        // no attributes or such with the stacks tag.
        while (xer.hasNext()) {
            XMLEvent xe = xer.peek();
            switch (xe.getEventType()) {

                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(getXMLStackTag())) {
                        if (getStack().isEmpty()) {
                            // Just make sure there is at least one.
                            getStack().add(newTableInstance());
                        }
                        return;
                    }
                    break;
            }
            xer.nextEvent();
        }
        throw new IllegalStateException("Error: XML file corrupt. No end tag for " + getXMLStackTag());
    }

    protected JSONArray getJSON(XMLEventReader xer) throws XMLStreamException {
        return JSONArray.fromObject(XMLUtilsV2.getText(xer, getXMLStackTag()));
    }

    protected void toXMLNEW(XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        if (isEmpty()) return;
        xsw.writeStartElement(getXMLStackTag());
        serializeContent(xsw, serializationState);
        xsw.writeEndElement();
    }

    /**
     * Very simple (and simple-minded) way to do it. Hand off the serialization so that the tables
     * are converted to JSON arrays with base64 encoded QDL as the entries.. Deserialization then is
     * just interpreting that back. This allows for serializing enormously complex stems and such
     * without having to try and get some XML format for them.
     *
     * @param xsw
     * @param serializationState
     * @throws XMLStreamException
     */
    protected void serializeContent(XMLStreamWriter xsw, SerializationState serializationState) throws XMLStreamException {
        try {
            xsw.writeCData(toJSON(serializationState).toString());
        } catch (Throwable t) {
            if (t instanceof XMLStreamException) {
                throw (XMLStreamException) t;
            }
            throw new NFWException("problem serializing string", t);
        }

    }

    public static void main(String[] args) throws Throwable {
        // Roundtrip test for JSON serialization. Should populate a stack, print it out, deserialize it, then
        // print out the exact same stack.
        testVStack();
        // testFStack2();
    }

    static void testFStack() throws Throwable {
        FStack fStack = new FStack();
        fStack.put(new FunctionRecord(new FKey("f", 1), Arrays.asList("f(x)->x^2;")));
        fStack.put(new FunctionRecord(new FKey("g", 1), Arrays.asList("g(x)->x^3;")));
        fStack.put(new FunctionRecord(new FKey("h", 2), Arrays.asList("h(x,y)->x*y;")));
        fStack.pushNewTable();
        fStack.localPut(new FunctionRecord(new FKey("f", 1), Arrays.asList("f(x)->x^4;")));
        fStack.localPut(new FunctionRecord(new FKey("h", 1), Arrays.asList("h(x)->x+1;")));
        fStack.pushNewTable();
        JSONArray serialized = fStack.toJSON(null);
        System.out.println(serialized.toString(2));

        FStack fStack1 = new FStack();
        fStack1.fromJSON(serialized, null);
        System.out.println(fStack1.toJSON(null).toString(2));

    }

    static void testFStack2() throws Throwable {
        FStack fStack = new FStack();
        fStack.put(new FunctionRecord(new FKey("f", 1), Arrays.asList("f(x)->x^2;")));
        fStack.put(new FunctionRecord(new FKey("g", 1), Arrays.asList("g(x)->x^3;")));
        fStack.put(new FunctionRecord(new FKey("h", 2), Arrays.asList("h(x,y)->x*y;")));
        fStack.pushNewTable();
        fStack.localPut(new FunctionRecord(new FKey("f", 1), Arrays.asList("f(x)->x^4;")));
        fStack.localPut(new FunctionRecord(new FKey("h", 1), Arrays.asList("h(x)->x+1;")));
        fStack.pushNewTable();
        JSONObject serialized = fStack.serializeToJSON(null);
        System.out.println(serialized.toString(2));

        FStack fStack1 = new FStack();
        fStack1.deserializeFromJSON(serialized, null, null);
        System.out.println(fStack1.toJSON(null).toString(2));

    }

    static void testVStack() throws Throwable {
        // Roundtrip test for JSON serialization. Should populate a stack, print it out, deserialize it, then
        // print out the exact same stack.
        VStack vStack = new VStack();
        vStack.pushNewTable();
        vStack.put(new VThing(new XKey("a"), Boolean.TRUE));
        vStack.put(new VThing(new XKey("b"), "foo"));
        vStack.put(new VThing(new XKey("x"), new BigDecimal("123.456789")));
        QDLStem s = new QDLStem();
        s.put("a", 5L);
        s.put("foo", "bar");
        vStack.put(new VThing(new XKey("s."), s));
        vStack.pushNewTable();
        vStack.localPut(new VThing(new XKey("a"), Boolean.FALSE));
        vStack.localPut(new VThing(new XKey("x"), new BigDecimal("9.87654321")));
        QDLModule qdlModule = new QDLModule();
        State state = new State();
        qdlModule.setState(state);
        qdlModule.setNamespace(URI.create("a:x"));
        qdlModule.setAlias("zzz");
        qdlModule.getSource();
        ModuleStatement moduleStatement = new ModuleStatement();
        List<String> source = new ArrayList<>();
        source.add("module['a:x'][f(x)->x^2;zzz:=foo;];");
        moduleStatement.setSourceCode(source);
        qdlModule.setModuleStatement(moduleStatement);
        qdlModule.getState().getVStack().put(new VThing(new XKey("m_x"), "module string"));
        vStack.localPut(new VThing(new XKey("zzz"), qdlModule));

        vStack.pushNewTable();

        // One reason to use VStack here is that the xmlSerializationState can be null, since it is not used
        JSONObject serialized = vStack.serializeToJSON(null);
        System.out.println(serialized.toString(2));
        VStack vStack1 = new VStack();
        vStack1.deserializeFromJSON(serialized, null, state);
        System.out.println(vStack1.toJSON(null).toString(2));

    }

    public JSONObject serializeToJSON(SerializationState serializationState) throws Throwable {
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        for (XTable xTable : getStack()) {
            JSONArray array1 = xTable.serializeToJSON(serializationState);
            if (array1 != null) {
                array.add(array1);
            }
        }
        if (array.isEmpty()) {
            return null;
        }
        jsonObject.put("stack", array);
        return jsonObject;
    }

    public void deserializeFromJSON(JSONObject jsonObject, SerializationState serializationState, State state) {
        deserializeFromJSONNEW(jsonObject, serializationState, state);
    }

    public void deserializeFromJSONNEW(JSONObject jsonObject, SerializationState serializationState, State state) {
        JSONArray array = jsonObject.getJSONArray(STACK_TAG);
        // remember that we just need an interpreter to operate on the state, so any will do.
        QDLInterpreter qi = new QDLInterpreter(state);
        XTable currentST = getStack().get(0);// start at root
        for (int i = 0; i < array.size(); i++) {
            // these were put in, in reverse order, so have to pop them out.
            if (0 < i) {
                currentST = newTableInstance();
                getStack().add(currentST);
            }

            JSONArray jsonArray = array.getJSONArray(i);
            for (int k = 0; k < jsonArray.size(); k++) {
                try {
                    currentST.deserializeFromJSON(jsonArray.getJSONObject(k), qi, serializationState);
                } catch (Throwable e) {
                    if(serializationState.isFailOnMissingModules()){
                        if(e instanceof RuntimeException){
                            throw (RuntimeException)e;
                        }
                        throw new QDLException("error deserializing module", e);
                    }
                    // For now
                    //   e.printStackTrace();
                }
            }
            // since this is the stack in the interpreter state, this pushes a new table there
        }

    }

    public void deserializeFromJSONOLD(JSONObject jsonObject, SerializationState serializationState, State state) {
        getStack().clear();
        XStack scratch = newInstance();
        if (state == null) {
            state = State.getRootState().newCleanState();
        }
        setStateStack(state, scratch);
        JSONArray array = jsonObject.getJSONArray(STACK_TAG);
        // remember that we just need an interpreter to operate on the state, so any will do.
        QDLInterpreter qi = new QDLInterpreter(state);

        for (int i = 0; i < array.size(); i++) {
            // these were put in, in reverse order, so have to pop them out.
            scratch.getStack().clear();
            XTable currentST = newTableInstance();
            scratch.push(currentST);

            JSONArray jsonArray = array.getJSONArray(i);
            for (int k = 0; k < jsonArray.size(); k++) {
                try {
                    currentST.deserializeFromJSON(jsonArray.getJSONObject(k), qi, serializationState);
                } catch (Throwable e) {
                    // For now
                    e.printStackTrace();
                }
            }
            // since this is the stack in the interpreter state, this pushes a new table there
            getStack().add(currentST);
        }
    }
}
