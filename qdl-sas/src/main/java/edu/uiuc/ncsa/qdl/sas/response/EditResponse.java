package edu.uiuc.ncsa.qdl.sas.response;

import edu.uiuc.ncsa.qdl.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.response.Response;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/26/22 at  2:28 PM
 */
public class EditResponse extends Response implements QDLSASConstants {

    public EditResponse() {
        super(RESPONSE_TYPE_EDIT);
    }

    public EditResponse(String content) {
        this();
        setActionType(ACTION_EXECUTE);
        setContent(content);
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    String content;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    String alias;

    public int getArgState() {
        return argState;
    }

    public void setArgState(int argState) {
        this.argState = argState;
    }

    int argState = -1;

    public int getEditObjectType() {
        return editObjectType;
    }

    public void setEditObjectType(int editObjectType) {
        this.editObjectType = editObjectType;
    }

    int editObjectType = -1;


    @Override
    public void deserialize(JSONObject json) {
        super.deserialize(json);
        if (json.containsKey(RESPONSE_CONTENT)) {
            content = new String(Base64.decodeBase64(json.getString(RESPONSE_CONTENT)));
        }
        editObjectType = json.getInt(KEY_EDIT_OBJECT_TYPE);
        alias = json.getString(KEY_EDIT_ALIAS);
        argState = json.getInt(KEY_EDIT_ARG_STATE);
    }

    @Override
    public JSONObject serialize() {
        JSONObject json = super.serialize();
        if (content != null) {
            json.put(RESPONSE_CONTENT, Base64.encodeBase64URLSafeString(content.getBytes(StandardCharsets.UTF_8)));
        }
        json.put(KEY_EDIT_ALIAS, alias == null ? "" : alias);
        json.put(KEY_EDIT_OBJECT_TYPE, getEditObjectType());
        json.put(KEY_EDIT_ARG_STATE, getArgState());
        return json;
    }
}
