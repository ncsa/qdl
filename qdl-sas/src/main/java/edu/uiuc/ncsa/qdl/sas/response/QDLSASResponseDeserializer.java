package edu.uiuc.ncsa.qdl.sas.response;

import edu.uiuc.ncsa.qdl.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.exceptions.SASException;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.sas.webclient.ResponseDeserializer;
import net.sf.json.JSONObject;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/26/22 at  5:09 PM
 */
public class QDLSASResponseDeserializer extends ResponseDeserializer implements QDLSASConstants {
    @Override
    public Response deserialize(JSONObject jsonObject) {
        Response response = null;
        if(!jsonObject.containsKey(RESPONSE_TYPE)){
            if(jsonObject.containsKey(RESPONSE_STATUS)){
                throw new SASException("error status = " + jsonObject.get(RESPONSE_STATUS) + ":" + jsonObject.toString(1));
            }
            throw new SASException("malformed response:" + jsonObject.toString(1));
        }
        switch (jsonObject.getString(RESPONSE_TYPE)){
            case RESPONSE_TYPE_EDIT:
                response = new EditResponse();
                break;
            case RESPONSE_TYPE_LIST_FUNCTIONS:
                response = new ListFunctionsResponse();
                break;
            case RESPONSE_TYPE_GET_HELP_TOPIC:
                response = new GetHelpTopicResponse();
                break;
            case RESPONSE_BUFFER_SAVE:
                response = new BufferSaveResponse();
                break;
        }
        if(response != null){
            response.deserialize(jsonObject);
            return response;
        }
        return super.deserialize(jsonObject); // throws an exception
    }
}
