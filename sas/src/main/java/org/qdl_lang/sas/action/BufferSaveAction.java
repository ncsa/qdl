package org.qdl_lang.sas.action;

import org.qdl_lang.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.action.Action;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/27/22 at  6:37 AM
 */
public class BufferSaveAction extends Action implements QDLSASConstants {
    public BufferSaveAction() {
        super(ACTION_BUFFER_SAVE);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setEditObjectType(int editObjectType) {
        this.editObjectType = editObjectType;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public int getArgState() {
        return argState;
    }

    public void setArgState(int argState) {
        this.argState = argState;
    }

    String content;

    public int getEditObjectType() {
        return editObjectType;
    }

    int editObjectType;
    String localName;
    int argState;

    @Override
    public JSONObject serialize() {
        JSONObject jsonObject = super.serialize();
        if (content != null) {
            jsonObject.put(RESPONSE_CONTENT, Base64.encodeBase64URLSafeString(content.getBytes(StandardCharsets.UTF_8)));
        }
        jsonObject.put(KEY_EDIT_ALIAS, localName == null ? "" : localName);
        jsonObject.put(KEY_EDIT_OBJECT_TYPE, getEditObjectType());
        jsonObject.put(KEY_EDIT_ARG_STATE, getArgState());
        return jsonObject;
    }

    @Override
    public void deserialize(JSONObject json) {
        super.deserialize(json);
        editObjectType = json.getInt(KEY_EDIT_OBJECT_TYPE);
        localName = json.getString(KEY_EDIT_ALIAS);
        argState = json.getInt(KEY_EDIT_ARG_STATE);
        if (json.containsKey(RESPONSE_CONTENT)) {
            content = new String(Base64.decodeBase64(json.getString(RESPONSE_CONTENT)));
        }
    }
}