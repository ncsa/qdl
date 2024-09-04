package org.qdl_lang.sas.response;

import org.qdl_lang.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONObject;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/29/22 at  11:23 AM
 */
public class BufferSaveResponse extends Response implements QDLSASConstants {
    public BufferSaveResponse() {
        super(RESPONSE_BUFFER_SAVE);
        setActionType(ACTION_BUFFER_SAVE);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    String message;

    @Override
    public void deserialize(JSONObject json) {
        super.deserialize(json);
        if(json.containsKey(RESPONSE_MESSAGE)){
            message = json.getString(RESPONSE_MESSAGE);
        }
    }

    @Override
    public JSONObject serialize() {
        JSONObject json = super.serialize();
        if(!StringUtils.isTrivial(message)){
            json.put(RESPONSE_MESSAGE, message);
        }
        return json;
    }
}
