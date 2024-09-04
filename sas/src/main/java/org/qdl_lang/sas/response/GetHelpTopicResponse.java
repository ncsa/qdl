package org.qdl_lang.sas.response;

import org.qdl_lang.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.action.Action;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONObject;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/29/22 at  7:24 AM
 */
public class GetHelpTopicResponse extends Response implements QDLSASConstants {
    public GetHelpTopicResponse() {
        super(RESPONSE_TYPE_GET_HELP_TOPIC);
    }

    public GetHelpTopicResponse(Action action) {
        super(RESPONSE_TYPE_GET_HELP_TOPIC, action);
    }


    public boolean hasHelp() {
        return !StringUtils.isTrivial(help);
    }


    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public String getFunctionHelp() {
        return functionHelp;
    }

    public void setFunctionHelp(String functionHelp) {
        this.functionHelp = functionHelp;
    }

    String functionHelp;
    String example;

    public boolean hasExample() {
        return !StringUtils.isTrivial(example);
    }


    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    String help;

    @Override
    public JSONObject serialize() {
        JSONObject json = super.serialize();
        json.put(RESPONSE_CONTENT, help==null?"":help);
        json.put(KEY_HELP_EXAMPLE, example==null?"":example);
        json.put(KEY_FUNCTION_HELP, functionHelp==null?"":functionHelp);
        return json;
    }

    @Override
    public void deserialize(JSONObject json) {
        super.deserialize(json);
        if(json.containsKey(RESPONSE_CONTENT)){
            help = json.getString(RESPONSE_CONTENT);
        }
        if(json.containsKey(KEY_HELP_EXAMPLE)){
            example = json.getString(KEY_HELP_EXAMPLE);
        }
        if(json.containsKey(KEY_FUNCTION_HELP)){
            functionHelp = json.getString(KEY_FUNCTION_HELP);
        }
    }
}
