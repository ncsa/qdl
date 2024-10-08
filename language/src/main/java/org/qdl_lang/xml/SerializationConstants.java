package org.qdl_lang.xml;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 12/27/20 at  7:02 AM
 */
public interface SerializationConstants {
    String INTEGER_TAG = "integer";
    String BOOLEAN_TAG = "boolean";
    String DECIMAL_TAG = "decimal";
    String NULL_TAG = "null";
    String STRING_TAG = "string";
    String STEM_TAG = "stem";
    String STEM_KEY_TAG = "key";
    String STEM_ENTRY_TAG = "entry";
    String LIST_INDEX_ATTR = "index";
    String TYPE_TAG = "type";
    String QDL_TYPE_TAG = "qdl";

    String MODULE_TAG = "module";
    String MODULE_NS_ATTR = "namespace";
    String MODULE_ALIAS_ATTR = "alias";
    String MODULE_TYPE_TAG = "type";
    String MODULE_TYPE_TAG2 = "module_type";
    String MODULE_TYPE_JAVA_TAG = "java";
    String MODULE_TYPE_QDL_TAG = "qdl";
    String MODULE_CLASS_NAME_TAG = "class_name";
    String MODULE_SOURCE_TAG = "source";
    String MODULE_DOCUMENTATION_TAG = "documentation";
    String MODULE_STATE_TAG = "state";
    String MODULE_JAVA_STATE_TAG = "java_state";
    String MODULE_INHERITANCE_MODE_TAG = "inheritance_mode";
    String MODULE_INPUT_FORM_TAG = "input_form";
    String MODULE_IS_TEMPLATE_TAG = "is_template";
    /**
     * Flags the module as being an instance in the old system.
     */
    String MODULE_IS_INSTANCE_TAG = "is_instance";

    String WORKSPACE_TAG = "workspace";
    String WS_ENV_TAG = "env";
    String EXTRINSIC_VARIABLES_TAG = "extrinsic_variables";
    String EXTRINSIC_FUNCTIONS_TAG = "extrinsic_functions";
    String INTRINSIC_VARIABLES_TAG = "intrinsic_variables";
    String INTRINSIC_FUNCTIONS_TAG = "intrinsic_functions";
    String ENV_FILE = "env_file";
    String PRETTY_PRINT = "pretty_print";
    String OVERWRITE_BASE_FUNCTIONS = "overwrite_base_functions";
    String BUFFER_DEFAULT_SAVE_PATH = "buffer_default_save_path";
    String ECHO_MODE = "echo_mode";
    String DEBUG_MODE = "debug_mode";
    String DEBUG_UTIL = "debug_util";

    String AUTOSAVE_ON = "autosave_on";
    String AUTOSAVE_INTERVAL = "autosave_interval";
    String ENABLE_LIBRARY_SUPPORT = "enable_library_support";
    String EXTERNAL_EDITOR_NAME = "external_editor";
    String AUTOSAVE_MESSAGES_ON = "autosave_messages_on";
    String START_TS = "start_ts";
    String ROOT_DIR = "root_dir";
    String SAVE_DIR = "save_dir";
    String RUN_SCRIPT_PATH = "run_script_path";
    String RUN_INIT_ON_LOAD = "run_init_on_load";
    String CURRENT_PID = "current_pid";
    String CURRENT_WORKSPACE = "current_workspace"; // default file for saves
    String EDITOR_CLIPBOARD = "editor_clipboard";
    String ENV_PROPERTIES = "env_properties";
    String SCRIPT_PATH = "script_path";
    String MODULE_PATH = "module_path";
    String COMPRESS_XML = "compress_xml";
    String WS_ID = "ws_id";
    String DESCRIPTION = "description";
    String EXTERNAL_EDITOR_PATH = "external_editor_path";
    String USE_EXTERNAL_EDITOR = "use_external_editor";
    String COMMAND_HISTORY = "command_history";
    String ASSERTIONS_ON = "assertions_on";
    String DEBUG_LEVEL = "debug_level";


    String VARIABLE_TAG = "var";
    String VARIABLE_NAME_TAG = "name";
    String FUNCTION_TABLE_STACK_TAG = "function_stack";
    String FUNCTIONS_TAG = "functions";
    String FUNCTION_TAG = "func";
    String FUNCTION_NAME_TAG = "name";
    String FUNCTION_ARG_COUNT_TAG = "arg_count";


    String STACKS_TAG = "stacks";
    String STACK_TAG = "stack";


    String STATE_TAG = "state";
    String STATE_INTERNAL_ID_TAG = "internal_id";
    String OLD_IMPORTED_MODULES_TAG = "imports";
    String OLD_MODULE_TEMPLATE_TAG = "templates";

    String IMPORTED_MODULES_TAG = "module_imports";
    String MODULE_TEMPLATE_TAG = "module_templates";

    String MODULE_STACK_TAG = "module_stack";

    String BUFFER_MANAGER = "buffer_manager";
    String BUFFER_RECORDS = "records";
    String BUFFER_RECORD = "record";
    String BR_SOURCE = "src";
    String BR_ALIAS = "alias";
    String BR_SOURCE_SAVE_PATH = "src_save_path";
    String BR_LINK = "link";
    String BR_LINK_SAVE_PATH = "link_save_path";
    String BR_EDITED = "edited";
    String BR_MEMORY_ONLY = "memory_only";
    String BR_DELETED = "deleted";
    String BR_CONTENT = "content";

    // Version 2 tags
    String INSTANCE_REFERENCE_TAG = "instance_reference";
    String INSTANCE_STACK = "instance_stack";
    String MODULES_TAG = "modules";
    String MODULE_INSTANCES_TAG = "module_instances";
    String SERIALIZATION_VERSION_TAG = "serialization_version";
    String STATES_TAG = "states";
    String STATE_ASSERTIONS_ENABLED_TAG = "assertions_on";
    String STATE_CONSTANTS_TAG = "constants";
    String STATE_NUMERIC_DIGITS_TAG = "numeric_digits";
    String STATE_ID_TAG = "state_id";
    String STATE_REFERENCE_TAG = "state_reference";
    String STATE_RESTRICTED_IO_TAG = "restricted_io";
    String STATE_SERVER_MODE_TAG = "server_mode";
    String TEMPLATE_REFERENCE_TAG = "template_reference";
    String PARENT_INSTANCE_UUID_TAG = "parent_instance_uuid";
    String PARENT_TEMPLATE_UUID_TAG = "parent_template_uuid";
    String PARENT_INSTANCE_ALIAS_TAG = "parent_instance_alias";
    String TEMPLATE_STACK = "template_stack";
    String UUID_TAG = "uuid";
    String VARIABLES_TAG = "variables";
    String VARIABLE_STACK = "variable_stack";
    String INTRINSIC_VARIABLE_STACK = "intrinsic_variable_stack";
    /**
     * List of modules imported to local scope with QDL's use or jUse commands.
     */
    String USED_MODULES = "used_modules";
    String VERSION_2_0_TAG = "2.0";
    String VERSION_2_1_TAG = "2.1";

}
