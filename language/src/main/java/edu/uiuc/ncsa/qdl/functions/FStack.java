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
import java.util.*;

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
     *
     * @param name
     * @return
     */
    public List<FunctionRecordInterface> getByAllName(String name) {
        List<FunctionRecordInterface> all = new ArrayList<>();
        // Note this walks backwards through the stack.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((FTable) getStack().get(i)).getByAllName(name));
        }
        return all;

    }


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
            all.addAll(((V) getStack().get(i)).listFunctions(regex));
        }
        return all;
    }

    public Set<DyadicFunctionReferenceNode> listFunctionReferences(String regex) {
        Set<DyadicFunctionReferenceNode> all = new HashSet<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V) getStack().get(i)).listFunctionReferences(regex));
        }
        return all;
    }

    @Override
    public List<String> listAllDocs() {
        List<String> all = new ArrayList<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V) getStack().get(i)).listAllDocs());
        }
        return all;
    }

    @Override
    public List<String> listAllDocs(String functionName) {
        List<String> all = new ArrayList<>();
        // Note this walks backwards through the stack since this means that if
        // there is local documentation it overwrites the global documentation.
        for (int i = getStack().size() - 1; 0 <= i; i--) {
            all.addAll(((V) getStack().get(i)).listAllDocs(functionName));
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
            all.addAll(((V) getStack().get(i)).getDocumentation(key));
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
        state.setFTStack((FStack<? extends FTable<? extends FKey, ? extends FunctionRecordInterface>>) xStack);
    }

    @Override
    public XStack getStateStack(State state) {
        return state.getFTStack();
    }
  /*  public TreeSet<FunctionReferenceNode> listFunctionRefs(String regex) {
     TreeSet<FunctionReferenceNode> all = new TreeSet<>();
     // Note this walks backwards through the stack since this means that if
     // there is local documentation it overwrites the global documentation.
     for (int i = getStack().size() - 1; 0 <= i; i--) {
         all.addAll(((V)getStack().get(i)).listFunctions(regex));
     }
     return all;
 }*/
}
