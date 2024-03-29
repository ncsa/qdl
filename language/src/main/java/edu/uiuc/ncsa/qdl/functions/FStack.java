package edu.uiuc.ncsa.qdl.functions;

import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XStack;
import edu.uiuc.ncsa.qdl.state.XTable;
import edu.uiuc.ncsa.qdl.statements.Documentable;
import edu.uiuc.ncsa.qdl.xml.SerializationConstants;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static edu.uiuc.ncsa.qdl.xml.SerializationConstants.FUNCTION_TABLE_STACK_TAG;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/15/21 at  6:22 AM
 */
public class FStack<V extends FTable<? extends FKey, ? extends FunctionRecord>> extends XStack<V> implements Serializable, Documentable {

    public FStack() {
        pushNewTable();
    }


    public FunctionRecord getFunctionReference(String name) {
         for (XTable functionTable : getStack()) {
             FunctionRecord fr = (FunctionRecord) functionTable.get(new FKey(name, -1));
             if (fr != null) {
                 return fr;
             }
         }
         return null;
     }

    /**
     * Returns all of the named functions for any arg count. This is needed
     * to populate copies of local state.
     * @param name
     * @return
     */
    public List<FunctionRecordInterface> getByAllName(String name) {
        List<FunctionRecordInterface> all = new ArrayList<>();
        // Note this walks backwards through the stack.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((FTable)getStack().get(i)).getByAllName(name));
        }
        return all;

    }

    /**
     * Writes each table. Resulting serialization is
     * <pre>
     *  &lt;function_stack&gt;
     *           &lt;!--The functions for this state.--&gt;
     *           &lt;functions&gt;
     *             &lt;func name="f" arg_count="2"&gt;&lt;![CDATA[...
     *             &lt;func name="g" arg_count="2"&gt;&lt;![CDATA[...
     *           &lt;/functions&gt;
     *           &lt;functions&gt;
     *           ...
     *           &lt;/functions&gt;
     *  &lt;/function_stack&gt;
     * </pre>
     * Each <code>functions</code> tags is a table of functions. These are serialized
     * in reverse order n,n-1,...0 so that at deserialization they can be read in the
     * correct order and the stack can be faithfully reconstructed. As such the serialization
     * format is not quite XML -- order matters.
     * @throws XMLStreamException
     */
/*    @Override
    public void toXML(XMLStreamWriter xsw, SerializationObjects serializationObjects) throws XMLStreamException {
        if (isEmpty()) {
            return;
        }
//        xsw.writeStartElement(SerializationConstants.FUNCTION_TABLE_STACK_TAG);
        xsw.writeStartElement(getXMLStackTag());
        xsw.writeComment("The functions for this state.");
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            XTable xTable =  getStack().get(i);
            if(xTable.isEmpty()){
                continue;
            }
            xsw.writeStartElement(getXMLTableTag());
            xsw.writeAttribute(SerializationConstants.LIST_INDEX_ATTR, Integer.toString(i));
            xTable.toXML(xsw, serializationObjects);
            xsw.writeEndElement(); // end of table.

          }
        xsw.writeEndElement(); // end of tables.
    }*/

    @Override
    public String getXMLStackTag() {
        return SerializationConstants.FUNCTION_TABLE_STACK_TAG;
    }

    @Override
    public String getXMLTableTag() {
        return SerializationConstants.FUNCTIONS_TAG;
    }

    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {
        // points to stacks tag
        XMLEvent xe = xer.nextEvent(); // moves off the stacks tag.
        // no attributes or such with the stacks tag.
        boolean foundStack = false;
        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    switch (xe.asStartElement().getName().getLocalPart()) {
                        // Legacy case -- just a single functions block, not a stack.
                        case SerializationConstants.FUNCTIONS_TAG:
                            if (foundStack) break; // if a stack is being processed, skip this
                            FTable functionTable1 = (FTable) qi.getState().getFTStack().peek();
                            functionTable1.fromXML(xer, qi);
                            break;
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(FUNCTION_TABLE_STACK_TAG)) {
                        return;
                    }
                    break;
            }
            xe = xer.nextEvent();
        }
        throw new IllegalStateException("Error: XML file corrupt. No end tag for " + FUNCTION_TABLE_STACK_TAG);


    }
    @Override
    public TreeSet<String> listFunctions(String regex) {
        TreeSet<String> all = new TreeSet<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V)getStack().get(i)).listFunctions(regex));
        }
        return all;
    }

    @Override
    public List<String> listAllDocs() {
        List<String> all = new ArrayList<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V)getStack().get(i)).listAllDocs());
        }
        return all;
    }

    @Override
    public List<String> listAllDocs(String functionName) {
        List<String> all = new ArrayList<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V)getStack().get(i)).listAllDocs(functionName));
        }
        return all;
    }

    @Override
    public List<String> getDocumentation(String fName, int argCount) {
    throw new NotImplementedException("not implemented in XStack");
    }

    @Override
    public List<String> getDocumentation(FKey key) {
        List<String> all = new ArrayList<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V)getStack().get(i)).getDocumentation(key));
        }
        return all;
    }

    @Override
    public XStack newInstance() {
        return new FStack();
    }

    @Override
    public XTable newTableInstance() {
        return new FTable<>();
    }

    @Override
    public void setStateStack(State state, XStack xStack) {
         state.setFTStack((FStack<? extends FTable<? extends FKey,? extends FunctionRecordInterface>>) xStack);
    }

    @Override
    public XStack getStateStack(State state) {
        return state.getFTStack();
    }

}
