package edu.uiuc.ncsa.qdl.sas.action;

import edu.uiuc.ncsa.qdl.sas.QDLSASConstants;
import edu.uiuc.ncsa.sas.thing.action.Action;
import edu.uiuc.ncsa.sas.thing.action.ActionDeserializer;
import net.sf.json.JSONObject;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/27/22 at  6:39 AM
 */
public class QDLSASActionDeserializer extends ActionDeserializer implements QDLSASConstants {
    @Override
    public Action toAction(JSONObject json) {
        String a = getAction(json);
        Action action = null;
        switch (a) {
            case ACTION_BUFFER_SAVE:
                action = new BufferSaveAction();
                break;
            case ACTION_LIST_FUNCTIONS:
                action = new ListFunctionsAction();
                break;
            case ACTION_GET_HELP_TOPIC:
                action = new GetHelpTopicAction();
        }
        if (action != null) {
            action.deserialize(json);
            return action;
        }
        return super.toAction(json);
    }
}
