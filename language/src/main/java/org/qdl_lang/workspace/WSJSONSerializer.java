package org.qdl_lang.workspace;

import org.qdl_lang.functions.FStack;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.expressions.module.QDLModule;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.VStack;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.SerializationState;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.qdl_lang.xml.SerializationConstants.*;
import static edu.uiuc.ncsa.security.core.util.StringUtils.isTrivial;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/28/23 at  3:33 PM
 */
public class WSJSONSerializer {
    /**
     * Assumes input stream is a gzipped stream of bytes.
     *
     * @param inputStream
     * @param compressionOn
     * @return
     * @throws Throwable
     */
    public WorkspaceCommands fromJSON(InputStream inputStream, boolean compressionOn) throws Throwable {
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream, 65536);
        Reader r = new InputStreamReader(gzipInputStream);
        BufferedReader bufferedReader = new BufferedReader(r);
        StringBuffer stringBuffer = new StringBuffer();
        String s = bufferedReader.readLine();
        while (s != null) {
            stringBuffer.append(s + "\n");
            s = bufferedReader.readLine();
        }
        bufferedReader.close();
        return fromJSON(stringBuffer.toString());
    }

    /**
     * Assumes the bytes represent either a compressed stream or not.
     *
     * @param bytes
     * @return
     * @throws Throwable
     */
    public WorkspaceCommands fromJSON(byte[] bytes, boolean compressionOn) throws Throwable {
        if (compressionOn) {
            return fromJSON(new ByteArrayInputStream(bytes), compressionOn);
        }
        return fromJSON(new String(bytes, StandardCharsets.UTF_8));
    }

    public WorkspaceCommands fromJSON(String rawJSON) throws Throwable {
        JSONObject jsonObject = JSONObject.fromObject(rawJSON);
        return fromJSON(jsonObject);
    }

    public WorkspaceCommands fromJSON(JSONObject json) throws Throwable {
        //WorkspaceCommands workspaceCommands = new WorkspaceCommands();
        // roundabout way to get a new instance, but this means that this suite of tests can be run against
        // an extension of the workspace commands
        WorkspaceCommands workspaceCommands = WorkspaceCommands.getInstance().newInstance();
        State state = workspaceCommands.getState();
        if (state == null) {
            state = State.getFactory().newInstance();
        }
        SerializationState serializationState = new SerializationState();
        if (json.containsKey(STATE_TAG)) {
            JSONObject s = json.getJSONObject(STATE_TAG);
            state.deserializeFromJSON(s, serializationState);
        }
        if (json.containsKey(BUFFER_MANAGER)) {
            workspaceCommands.getBufferManager().fromJSON(json.getJSONObject(BUFFER_MANAGER));
        }

        if (json.containsKey(COMMAND_HISTORY)) workspaceCommands.commandHistory = json.getJSONArray(COMMAND_HISTORY);
        if (json.containsKey(EDITOR_CLIPBOARD)) workspaceCommands.editorClipboard = json.getJSONArray(EDITOR_CLIPBOARD);
        if (json.containsKey(SCRIPT_PATH)) workspaceCommands.getState().setScriptPaths(json.getJSONArray(SCRIPT_PATH));
        if (json.containsKey(MODULE_PATH)) workspaceCommands.getState().setModulePaths(json.getJSONArray(MODULE_PATH));

        if (json.containsKey(WS_ENV_TAG)) {
            WSXMLSerializer.envFromJSON(workspaceCommands, json.getJSONObject(WS_ENV_TAG));
        }
        if (json.containsKey(ENV_PROPERTIES)) {
            XProperties xp = new XProperties();
            xp.putAll(json.getJSONObject(ENV_PROPERTIES));
            workspaceCommands.setEnv(xp);
        }
        if (json.containsKey(EXTRINSIC_VARIABLES_TAG)) {
            VStack xVars = new VStack();
            xVars.deserializeFromJSON(json.getJSONObject(EXTRINSIC_VARIABLES_TAG), serializationState, state);
            // Don't need to set explicitly since they are automatically put in the state on deserialization.
            //   state.setExtrinsicVars(xVars);
        }
        if (json.containsKey(EXTRINSIC_FUNCTIONS_TAG)) {
            FStack iFuncs = new FStack();
            iFuncs.deserializeFromJSON(json.getJSONObject(EXTRINSIC_FUNCTIONS_TAG), serializationState, state);
            // Don't need to set explicitly since they are automatically put in the state on deserialization.
            //   state.setExtrinsicVars(xVars);
        }
        if (json.containsKey(INTRINSIC_VARIABLES_TAG)) {
            VStack xVars = new VStack();
            xVars.deserializeFromJSON(json.getJSONObject(INTRINSIC_VARIABLES_TAG), serializationState, state);
            state.setIntrinsicVariables(xVars);
        }
        if (json.containsKey(INTRINSIC_FUNCTIONS_TAG)) {
            FStack iFuncs = new FStack();
            iFuncs.deserializeFromJSON(json.getJSONObject(INTRINSIC_FUNCTIONS_TAG), serializationState, state);
            state.setIntrinsicFunctions(iFuncs);
        }
        return workspaceCommands;
    }

    public JSONObject toJSON(WorkspaceCommands workspaceCommands) throws Throwable {
        JSONObject jsonObject = new JSONObject();
        SerializationState serializationState = new SerializationState();
        serializationState.setVersion(SerializationConstants.VERSION_2_1_TAG); // critical!
        String comment = "";
        String indent = " --->>";
        JSONArray comments = new JSONArray();
        if (!isTrivial(workspaceCommands.getWSID())) {
            comments.add(indent + "workspace id: " + workspaceCommands.getWSID());
        }
        if (!isTrivial(workspaceCommands.getDescription())) {
            comments.add(indent + "description:" + workspaceCommands.getDescription());
        }
        comments.add(indent + "serialized on " + Iso8601.date2String(System.currentTimeMillis()));
        jsonObject.put("comment", comments);
        State state = workspaceCommands.getState();
        state.buildSO(serializationState);
        serializationState.addState(state);
        jsonObject.put(WS_ENV_TAG, WSXMLSerializer.envToJSON(workspaceCommands));
        if (workspaceCommands.bufferManager != null && !workspaceCommands.bufferManager.isEmpty()) {
            jsonObject.put(BUFFER_MANAGER, workspaceCommands.bufferManager.toJSON());
        }
        if (workspaceCommands.env != null && !workspaceCommands.env.isEmpty()) {
            JSONObject env = new JSONObject();
            env.putAll(workspaceCommands.env);
            jsonObject.put(ENV_PROPERTIES, env);
        }
        setJA(jsonObject, COMMAND_HISTORY, workspaceCommands.commandHistory);
        setJA(jsonObject, SCRIPT_PATH, workspaceCommands.getState().getScriptPaths());
        setJA(jsonObject, MODULE_PATH, workspaceCommands.getState().getModulePaths());
        setJA(jsonObject, EDITOR_CLIPBOARD, workspaceCommands.editorClipboard);
        if (!state.getExtrinsicVars().isEmpty()) {
            jsonObject.put(EXTRINSIC_VARIABLES_TAG, state.getExtrinsicVars().serializeToJSON(serializationState));
        }
        if (!state.getExtrinsicFuncs().isEmpty()) {
            jsonObject.put(EXTRINSIC_FUNCTIONS_TAG, state.getExtrinsicFuncs().serializeToJSON(serializationState));
        }
        if (!state.getIntrinsicVariables().isEmpty()) {
            jsonObject.put(INTRINSIC_VARIABLES_TAG, state.getIntrinsicVariables().serializeToJSON(serializationState));
        }
        if (state.getIntrinsicFunctions().isEmpty()) {
            jsonObject.put(INTRINSIC_FUNCTIONS_TAG, state.getIntrinsicFunctions().serializeToJSON(serializationState));
        }
        jsonObject.put(STATE_TAG, state.serializeToJSON(serializationState));

        return jsonObject;
    }

    public Module createModule(JSONObject json) {
        Module m = null;
        boolean isJavaModule = json.containsKey(MODULE_TYPE_TAG2) && json.getString(MODULE_TYPE_TAG2).equals(MODULE_TYPE_JAVA_TAG);
        if (isJavaModule) {
            if (!json.containsKey(MODULE_CLASS_NAME_TAG)) {
                throw new NFWException("bad serialization, java class not stored");
            }
        } else {
            m = new QDLModule();
        }
        return m;
    }

    /*
         xsw.writeStartElement(MODULE_TEMPLATE_TAG);
        xsw.writeComment("templates for all modules");
        for (UUID key : xmlSerializationState.templateMap.keySet()) {
            Module module = xmlSerializationState.getTemplate(key);
            module.toXML(xsw, null, true, xmlSerializationState);
        }
        xsw.writeEndElement(); // end module templates

        // Save other states (in modules).
        xsw.writeStartElement(STATES_TAG);
        xsw.writeComment("module states");
          */
    void setJA(JSONObject json, String tag, List<String> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        JSONArray array = new JSONArray();
        array.addAll(list);
        json.put(tag, array);
    }
    /*
  $$FOO:='bar'
  g(y)->y^2
  stem.:=[;5]
  string:='mairzy doats'
  module['a:x'][f(x)->x^2;q:=3;]
  z:=import('a:x')
  w:=import('a:x','share')
  w#q := 7
  z#q := 11
  cli := jload('cli')

  )save -json /tmp/ws.json
  )save -json -show

  )load -json /tmp/ws.json

    module['a:x'][f(x)->x;q:=1;module['a:y'][f(x)->x^3;q:=3;];y:=import('a:y');]

// for testing old module
  module['a:a','a'][g(x)->x+1;h(x)->x-1;q:=4;]
  module_import('a:a')
  module_import('a:a', 'b')
  )save -json -show
  )save -json -compress off /tmp/ws.json

  )load -json -compress off /tmp/ws.json
     */
}
