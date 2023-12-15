package edu.uiuc.ncsa.qdl.module;

import edu.uiuc.ncsa.qdl.evaluate.ModuleEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.ModuleInstantiationException;
import edu.uiuc.ncsa.qdl.parsing.QDLInterpreter;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.ModuleStatement;
import edu.uiuc.ncsa.qdl.util.InputFormUtil;
import edu.uiuc.ncsa.qdl.xml.SerializationConstants;
import edu.uiuc.ncsa.qdl.xml.SerializationState;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static edu.uiuc.ncsa.qdl.xml.SerializationConstants.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/1/20 at  11:30 AM
 */
public class QDLModule extends Module {
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    String filePath;

    public ModuleStatement getModuleStatement() {
        return moduleStatement;
    }

    public void setModuleStatement(ModuleStatement moduleStatement) {
        this.moduleStatement = moduleStatement;
    }

    ModuleStatement moduleStatement;

    public List<String> getSource() {
        if (moduleStatement == null || moduleStatement.getSourceCode().isEmpty()) {
            return new ArrayList<>();
        }
        return moduleStatement.getSourceCode();
    }


    @Override
    public Module newInstance(State state) {
        if (state == null) {
            // return a barebones module -- everything that does not depend on the state that
            // the template has
            QDLModule qdlModule = new QDLModule();
            qdlModule.setParentTemplateID(getId());// this object is a template, so set the parent uuid accordingly
            qdlModule.setAlias(getAlias());
            qdlModule.setNamespace(getNamespace());
            qdlModule.setModuleStatement(getModuleStatement());
            return qdlModule;
        }
        State localState = state.newLocalState();
        try {
            localState.setModuleState(true);
            //p.execute(getModuleStatement().getSourceCode());
            localState.setImportMode(true);
            getModuleStatement().evaluate(localState);
            Module m = getModuleStatement().getmInstance();
            getModuleStatement().clearInstance();
            localState.setImportMode(false);
            setupModule(m);
            return m;
        } catch (Throwable throwable) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            throw new ModuleInstantiationException("Error: Could not create module:" + throwable.getMessage(), throwable);
        }
    }

    @Override
    public void writeExtraXMLElements(XMLStreamWriter xsw) throws XMLStreamException {
        super.writeExtraXMLElements(xsw);
        if (!getSource().isEmpty()) {
            xsw.writeStartElement(MODULE_SOURCE_TAG);
            xsw.writeCData(Base64.encodeBase64URLSafeString(StringUtils.listToString(getSource()).getBytes(StandardCharsets.UTF_8)));
            xsw.writeEndElement();
        }
    }

    @Override
    public JSONObject serializeToJSON(SerializationState serializationState) throws Throwable {
        JSONObject json = super.serializeToJSON(serializationState);
        json.put(SerializationConstants.MODULE_TYPE_TAG2, SerializationConstants.MODULE_TYPE_QDL_TAG);
        /*
            Only save the source code for this if the template is missing. The reason is
            that every loaded module has a URI and a random unique UUID. It is possible
            that the user reloads the module with an update, leaving the instance with a
            different content. Preserve the loaded content. However, always preserving it
            means a huge explosion in the size of the serialization. Only save it if there
            was a bonda fide change.
         */
        if(isTemplate()){
            json.put(MODULE_INPUT_FORM_TAG, Base64.encodeBase64URLSafeString(InputFormUtil.inputForm(this).getBytes()));
        }else{
            // not a template. be sure there is a template
            Module template = serializationState.getTemplate(getParentTemplateID());
            if(template == null){
                json.put(MODULE_INPUT_FORM_TAG, Base64.encodeBase64URLSafeString(InputFormUtil.inputForm(this).getBytes()));
            }
        }
        if (getState() != null) {
            if (inheritMode == ModuleEvaluator.IMPORT_STATE_SHARE_VALUE) {
                json.put(MODULE_STATE_TAG, getState().serializeLocalStateToJSON(serializationState));
            } else {
                json.put(MODULE_STATE_TAG, getState().serializeToJSON(serializationState));
            }
        }
        return json;
    }

    @Override
    public void deserializeFromJSON(JSONObject json, SerializationState serializationState) throws Throwable {
        super.deserializeFromJSON(json, serializationState);
        String source = null;
        if(isTemplate()){
            if (!json.containsKey(MODULE_INPUT_FORM_TAG)) {
                throw new NFWException("missing input form for module.");
            }
            source = new String(Base64.decodeBase64(json.getString(MODULE_INPUT_FORM_TAG)), StandardCharsets.UTF_8);
        }else{
            // so we are deserializing an instance. Now we check if there is template
            Module template = serializationState.getTemplate(getParentTemplateID());
            if (template != null) {
                source = InputFormUtil.inputForm(template);
            }else{
                // last resort. No template by UUID, so the
                if (!json.containsKey(MODULE_INPUT_FORM_TAG)) {
                    throw new NFWException("missing input form for module.");
                }
                source = new String(Base64.decodeBase64(json.getString(MODULE_INPUT_FORM_TAG)), StandardCharsets.UTF_8);
            }
        }
/*
        if (!isTemplate() && serializationState.hasTemplates()) {
            Module template = serializationState.getTemplate(getParentTemplateID());
            if (template != null) {
                source = InputFormUtil.inputForm(template);
            }
        }
*/
        if (source == null) {
            // Plan B, see if it was serialized
            if (!json.containsKey(MODULE_INPUT_FORM_TAG)) {
                throw new NFWException("missing input form for module.");
            }
//            source = new String(Base64.decodeBase64(json.getString(MODULE_INPUT_FORM_TAG)), StandardCharsets.UTF_8);
        }
        State newState = State.getRootState().newCleanState(); // remember that State can be overridden, so this is the right type
        QDLInterpreter qdlInterpreter = new QDLInterpreter(newState);
        try {
            // recreating the module statement is generally very hard, involving parsing QDL,
            // so let QDL do it, then harvest it.
            qdlInterpreter.execute(source);
            QDLModule tempM = (QDLModule) newState.getMTemplates().getAll().get(0);
            setModuleStatement(tempM.getModuleStatement());
            setDocumentation(tempM.getModuleStatement().getDocumentation());

        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (json.containsKey(MODULE_STATE_TAG)) {
            newState.deserializeFromJSON(json.getJSONObject(MODULE_STATE_TAG), serializationState);
        }
        setState(newState);
    }

    @Override
    public void readExtraXMLElements(XMLEvent xe, XMLEventReader xer) throws XMLStreamException {
        super.readExtraXMLElements(xe, xer);
    }

    List<String> documentation = new ArrayList<>();

    /**
     * Documentation resides in the module definition, so it is loaded here at parse time.
     *
     * @return
     */
    @Override
    public List<String> getListByTag() {
        return documentation;
    }

    @Override
    public void setDocumentation(List<String> documentation) {
        this.documentation = documentation;
    }

    @Override
    public List<String> getDocumentation() {
        return documentation;
    }
}
