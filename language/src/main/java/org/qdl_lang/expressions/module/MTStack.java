package org.qdl_lang.expressions.module;

import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XStack;
import org.qdl_lang.state.XTable;
import org.qdl_lang.state.XThing;
import org.qdl_lang.xml.SerializationConstants;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/13/21 at  7:17 AM
 */

public class MTStack<V extends MTTable<? extends MTKey, ? extends Module>> extends XStack<V> implements Serializable {

    public MTStack() {
        pushNewTable();
    }

    @Override
    public XStack newInstance() {
        return new MTStack();
    }

    @Override
    public XTable newTableInstance() {
        return new MTTable();
    }


    @Override
    public String getXMLStackTag() {
        return SerializationConstants.TEMPLATE_STACK;
    }

    @Override
    public String getXMLTableTag() {
        return SerializationConstants.MODULES_TAG;
    }

    @Override
    public void fromXML(XMLEventReader xer, QDLInterpreter qi) throws XMLStreamException {
          throw new NotImplementedException("implement version 1 serialization for new template stack");
    }

    public void clearChangeList() {
        changeList = new ArrayList<>();
    }

    // On updates, the change list will track additions or replacements.
    // clear it before updates, read it it after, then clear it again.
    public List<MTKey> getChangeList() {
        return changeList;
    }

    public void setChangeList(List<MTKey> changeList) {
        this.changeList = changeList;
    }

    List<MTKey> changeList = new ArrayList<>();

    @Override
    public XThing put(XThing value) {
        changeList.add(((Module) value).getMTKey());
        return super.put(value);
    }

    public Module getModule(MTKey mtKey) {
        return (Module) get(mtKey);
    }

    @Override
    public void setStateStack(State state, XStack xStack) {
        state.setMTemplates((MTStack) xStack);
    }

    @Override
    public XStack getStateStack(State state) {
        return null;
    }
}
