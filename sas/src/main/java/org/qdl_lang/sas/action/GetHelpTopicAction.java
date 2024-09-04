package org.qdl_lang.sas.action;

import org.qdl_lang.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.action.Action;
import net.sf.json.JSONObject;

/**
 * Gets help for a given topic.
 * <p>Created by Jeff Gaynor<br>
 * on 8/29/22 at  7:17 AM
 */
public class GetHelpTopicAction extends Action implements QDLSASConstants {
    public GetHelpTopicAction() {
        super(ACTION_GET_HELP_TOPIC);
    }

    public GetHelpTopicAction(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @Override
    public JSONObject serialize() {
        JSONObject json = super.serialize();
        json.put(KEYS_ARGUMENT, name == null?"":name);
        return json;
    }

    @Override
    public void deserialize(JSONObject json) {
        super.deserialize(json);
        if(json.containsKey(KEYS_ARGUMENT)){
            name = json.getString(KEYS_ARGUMENT);
        }
    }
}
