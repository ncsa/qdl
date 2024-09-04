package org.qdl_lang.sas.response;

import org.qdl_lang.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.action.Action;
import edu.uiuc.ncsa.sas.thing.response.Response;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/27/22 at  5:15 PM
 */
public class ListFunctionsResponse extends Response implements QDLSASConstants {
    public ListFunctionsResponse() {
    }

    public ListFunctionsResponse(Action action, List<String> functions) {
        super(RESPONSE_TYPE_LIST_FUNCTIONS, action);
        this.functions = functions;
    }

    public List<String> getFunctions() {
        if (functions == null) {
            functions = new ArrayList<>();
        }
        return functions;
    }

    public void setFunctions(List<String> functions) {
        this.functions = functions;
    }

    List<String> functions;

    @Override
    public JSONObject serialize() {
        JSONObject json = super.serialize();
        if (functions instanceof JSONArray) {
            json.put(KEY_FUNCTION_LIST, functions);
        } else {
            JSONArray array = new JSONArray();
            array.addAll(functions);
        }
        return json;
    }

    @Override
    public void deserialize(JSONObject json) {
        super.deserialize(json);
        if (json.containsKey(KEY_FUNCTION_LIST)) {
            functions = json.getJSONArray(KEY_FUNCTION_LIST);
        }
    }
}
