package org.qdl_lang.xml;

import org.qdl_lang.exceptions.DeserializationException;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.functions.FStack;
import org.qdl_lang.expressions.module.MIStack;
import org.qdl_lang.expressions.module.MTStack;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.expressions.module.QDLModule;
import org.qdl_lang.state.State;
import org.qdl_lang.state.StateUtils;
import org.qdl_lang.state.XStack;
import org.qdl_lang.variables.VStack;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static org.qdl_lang.xml.SerializationConstants.*;

/**
 * XML Utilities for version 2 of serialization.
 * <p>Created by Jeff Gaynor<br>
 * on 2/15/22 at  6:36 AM
 */
public class XMLUtilsV2 {

    public static void deserializeTemplateStore(XMLEventReader xer, SerializationState SerializationState) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();

        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(MODULE_TAG)) {
                        // Have to get the attributes first because they are needed to determine which
                        // type of module to create.
                        XMLUtils.ModuleAttributes moduleAttributes = XMLUtils.getModuleAttributes(xe);
                        Module module = deserializeTemplate(xer, moduleAttributes, SerializationState);
                        if(module != null) {
                            SerializationState.addTemplate(module);
                        }
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(MODULE_TEMPLATE_TAG)) {
                        return;
                    }
            }
            xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(TEMPLATE_STACK);
    }

    public static void deserializeStateStore(XMLEventReader xer, SerializationState SerializationState) throws XMLStreamException {
        XMLEvent xe = xer.nextEvent();

        while (xer.hasNext()) {
            xe = xer.peek();
            switch (xe.getEventType()) {
                case XMLEvent.START_ELEMENT:
                    if (xe.asStartElement().getName().getLocalPart().equals(STATE_TAG)) {
                        // Have to get the attributes first because they are needed to determine which
                        // type of module to create.
                        StateAttributes stateAttributes = getStateAttributes(xe);
                        State state;
                        if (SerializationState.processedState(stateAttributes.uuid)) {
                            state = SerializationState.getState(stateAttributes.uuid);
                        } else {
                            state = StateUtils.newInstance();
                            state.setUuid(stateAttributes.uuid);
                            SerializationState.addState(state);
                        }
                        StateUtils.load(state, SerializationState, xer);
                    }
                    break;
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(STATES_TAG)) {
                        return;
                    }
            }
            xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(TEMPLATE_STACK);
    }

    /**
     * Deserializes a single template, either from sources or from the class reference.
     *
     * @param xer
     * @param moduleAttributes
     * @param serializationState
     * @return
     */
    public static Module deserializeTemplate(XMLEventReader xer, XMLUtils.ModuleAttributes moduleAttributes, SerializationState serializationState) throws XMLStreamException {
        Module module = null;

        if (moduleAttributes.isJavaModule()) {
            if (StringUtils.isTrivial(moduleAttributes.className)) {
                throw new IllegalStateException("Error: serialized java module without a classs -- cannot deserialize");
            }
            try {
                Class klasse = XMLUtilsV2.class.forName(moduleAttributes.className);
                module = ((JavaModule) klasse.newInstance()).newInstance(null); // this populates the functions and variables!!
                ((JavaModule) module).setClassName(moduleAttributes.className);
            } catch (Throwable t) {
                DebugUtil.trace(XMLUtilsV2.class, "Warn: cannot deserialize class \"" + moduleAttributes.className + "\". Skipping. '" + t.getMessage() + "' (" + t.getClass().getName() + ")");
                if(!serializationState.skipBadModules) {
                    throw new DeserializationException("cannot deserialize class \"" + moduleAttributes.className + "\".", t);
                }
                return null;
            }

        } else {
            module = new QDLModule();
        }
        module.fromXML(xer, serializationState, true);
        module.setId(moduleAttributes.uuid);
        module.setAlias(moduleAttributes.alias);
        module.setNamespace(moduleAttributes.ns);
        module.setTemplate(true);
        module.setParentTemplateID(null);

        // scorecard: at this point, enough information existed in the attributes to re-assemble the module
        // The cursor is still on this tag. Now send it to the module for further processing.
        //module.fromXML(xer, xp, qi);
        if (module instanceof JavaModule) {
            // call this at the right time -- after everything else has been done.
            ((JavaModule) module).init(null, false);
        }
        return module;
    }

    /**
     * Deserializes a template stack of references.
     *
     * @param xer
     * @param serializationState
     * @throws XMLStreamException
     */
    public static void deserializeTemplates(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException {
        deserializeXStack(xer, new MTStack(), state, serializationState);
    }

    public static void deserializeVariables(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException {
        deserializeXStack(xer, new VStack(), state, serializationState);
    }
    public static void deserializeFunctions(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException {
        deserializeXStack(xer, new FStack(), state, serializationState);
    }

    public static void deserializeInstances(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException {
        deserializeXStack(xer, new MIStack(), state, serializationState);
    }

    public static void deserializeExtrinsicVariables(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException{
        xer.nextEvent();// advance cursor
        VStack exx = new VStack();
        exx.fromXML(xer, serializationState);
    }
    public static void deserializeExtrinsicFunctions(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException{
        xer.nextEvent();// advance cursor
        FStack iFuncs = new FStack();
        iFuncs.fromXML(xer, serializationState);

    }

    public static void deserializeIntrinsicVariables(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException {
        xer.nextEvent();// advance cursor
        VStack iStack = new VStack();
        iStack.fromXML(xer, serializationState);
        state.setIntrinsicVariables( iStack);
        //xStack.setStateStack(state, xStack);
    }
    public static void deserializeIntrinsicFunctions(XMLEventReader xer, State state, SerializationState serializationState) throws XMLStreamException {
        xer.nextEvent();// advance cursor
        FStack iStack = new FStack<>();
        iStack.fromXML(xer, serializationState);
        state.setIntrinsicFunctions( iStack);
        //xStack.setStateStack(state, xStack);
    }

    protected static void deserializeXStack(XMLEventReader xer, XStack xStack, State state, SerializationState serializationState) throws XMLStreamException {
        xer.nextEvent();// advance cursor
        xStack.fromXML(xer, serializationState);
        xStack.setStateStack(state, xStack);
    }

    public static class StateAttributes {
        public UUID uuid;
    }

    public static StateAttributes getStateAttributes(XMLEvent xe) {
        StateAttributes stateAttributes = new StateAttributes();
        Iterator iterator = xe.asStartElement().getAttributes(); // Use iterator since it tracks state
        while (iterator.hasNext()) {
            Attribute a = (Attribute) iterator.next();
            String v = a.getValue();
            switch (a.getName().getLocalPart()) {
                case UUID_TAG:
                    stateAttributes.uuid = UUID.fromString(v);
                    break;
            }
        }
        return stateAttributes;
    }

    /**
     * This will read the text contents of a tag. The assumption is that you have the cursor on the
     * start tag then call this. It sifts through whitespace etc. until it finds actual characters, then
     * returns. If it finds only white space, it returns the empty string.
     * @param xer
     * @param closeTag
     * @return
     * @throws XMLStreamException
     */
    public static String getText(XMLEventReader xer, String closeTag) throws XMLStreamException {
        JSONArray jsonArray = null;
        XMLEvent xe = xer.nextEvent();
        while (xer.hasNext()) {
            switch (xe.getEventType()) {
                case XMLEvent.CHARACTERS:
                    if (xe.asCharacters().isWhiteSpace()) {
                        break;
                    }
                    return xe.asCharacters().getData();
                case XMLEvent.END_ELEMENT:
                    if (xe.asEndElement().getName().getLocalPart().equals(closeTag)) {
                        return ""; // empty tag
                    }
            }
            xe = xer.nextEvent();
        }
        throw new XMLMissingCloseTagException(closeTag);
    }

    /**
     * Converts a collection to a {@link JSONArray} then to a string. This is the
     * compliment to {@link #getText(XMLEventReader, String)}.
     * <h3>Use</h3>
     * You write the start element, invoke this, then write the end element.
     * @param xsw
     * @param collection
     */
    public static void toCDATA(XMLStreamWriter xsw, Collection collection) throws XMLStreamException {
           JSONArray jsonArray = new JSONArray();
           jsonArray.addAll(collection);
           xsw.writeCData(jsonArray.toString());
    }
}
