package org.qdl_lang.sas;

import org.qdl_lang.gui.editor.EditDoneEvent;
import edu.uiuc.ncsa.sas.SASConstants;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/26/22 at  2:28 PM
 */
public interface QDLSASConstants extends SASConstants {
    public static final String RESPONSE_TYPE_EDIT = "edit";
    public static final String KEY_EDIT_OBJECT_TYPE = "edit_object_type";
    public static final int KEY_EDIT_OBJECT_TYPE_FILE = EditDoneEvent.TYPE_FILE;
    public static final int KEY_EDIT_OBJECT_TYPE_VARIABLE = EditDoneEvent.TYPE_VARIABLE;
    public static final int KEY_EDIT_OBJECT_TYPE_FUNCTION = EditDoneEvent.TYPE_FUNCTION;
    public static final int KEY_EDIT_OBJECT_TYPE_BUFFER = EditDoneEvent.TYPE_BUFFER;
    public static final String KEY_EDIT_ALIAS = "alias";
    /**
     * Used for either the arg count (functions) or variable type. Ignored for files and buffers
     */
    public static final String KEY_EDIT_ARG_STATE = "arg_state";

    public static final String ACTION_BUFFER_SAVE = "buffer_save";
    public static final String RESPONSE_BUFFER_SAVE = "buffer_save";

    public static final String ACTION_LIST_FUNCTIONS = "list_functions";
    public static final String RESPONSE_TYPE_LIST_FUNCTIONS = ACTION_LIST_FUNCTIONS;
    public static final String KEY_FUNCTION_LIST = "functions";

    public static final String ACTION_GET_HELP_TOPIC = "get_help_topic";
    public static final String RESPONSE_TYPE_GET_HELP_TOPIC = ACTION_GET_HELP_TOPIC;
    public static final String KEY_HELP_EXAMPLE = "example";
    public static final String KEY_FUNCTION_HELP = "function_help";

}
