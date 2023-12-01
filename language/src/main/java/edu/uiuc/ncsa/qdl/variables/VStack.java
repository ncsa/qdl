package edu.uiuc.ncsa.qdl.variables;

import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.state.XKey;
import edu.uiuc.ncsa.qdl.state.XStack;
import edu.uiuc.ncsa.qdl.state.XTable;
import edu.uiuc.ncsa.qdl.xml.XMLConstants;
import edu.uiuc.ncsa.qdl.xml.SerializationState;
import net.sf.json.JSONObject;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.TreeSet;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 2/20/22 at  6:14 AM
 */
public class VStack<V extends VTable<? extends XKey, ? extends VThing>> extends XStack<V> {
    public VStack() {
        pushNewTable();
    }

    @Override
    public XStack newInstance() {
        return new VStack();
    }

    @Override
    public XTable newTableInstance() {
        return new VTable();
    }

    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {

    }

    @Override
    public String getXMLStackTag() {
        return XMLConstants.VARIABLE_STACK;
    }

    @Override
    public String getXMLTableTag() {
        return XMLConstants.VARIABLES_TAG;
    }

    @Override
    public void setStateStack(State state, XStack xStack) {
        state.setvStack((VStack) xStack);
    }

    @Override
    public XStack getStateStack(State state) {
        return state.getVStack();
    }

    public TreeSet<String> listVariables() {
        TreeSet<String> vars = new TreeSet<>();
        for (XTable xTable : getStack()) {
            VTable vTable = (VTable) xTable;
            vars.addAll(vTable.listVariables());
        }
        return vars;
    }



    public final static String VSTACK_SERIALIZATION_VERSION_2_1 = "2.1";
    public final static String VSTACK_SERIALIZATION_2_1_TABLES_TAG = "tables";
    public final static String VSTACK_VERSION_TAG = "version";


    @Override
    protected void fromXMLNEW(XMLEventReader xer, SerializationState serializationState) throws XMLStreamException {
        if (!VSTACK_SERIALIZATION_VERSION_2_1.equals(serializationState.getVariablesSerializationVersion())) {
            super.fromXMLNEW(xer, serializationState);
            return;
        }
        getStack().clear();
        XStack scratch = newInstance();
        State state = new State();
        setStateStack(state, scratch);

        QDLInterpreter qi = new QDLInterpreter(state);
        XMLEvent xe;
        while (xer.hasNext()) {
            xe = xer.peek(); // May have to slog through a bunch of events (like whitespace)
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    switch (xe.asStartElement().getName().getLocalPart()) {
                        case VSTACK_SERIALIZATION_2_1_TABLES_TAG:
                            VTable vTable = new VTable();
                            push(vTable);
                            vTable.fromXML(xer, qi);
                    }
                case XMLEvent.END_ELEMENT:
                    break;
/*
                    if (xe.asEndElement().getName().getLocalPart().equals(WORKSPACE_TAG)) {
                              return testCommands;
                          }
*/
            } // end switch
        }//end while

    }

    @Override
    public JSONObject serializeToJSON(SerializationState serializationState) {
        JSONObject json = super.serializeToJSON(serializationState);
        json.put(VSTACK_VERSION_TAG, VSTACK_SERIALIZATION_VERSION_2_1);
        return json;
    }

}
