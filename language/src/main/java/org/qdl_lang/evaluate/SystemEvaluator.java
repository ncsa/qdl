package org.qdl_lang.evaluate;

import org.qdl_lang.config.JavaModuleConfig;
import org.qdl_lang.config.QDLConfigurationLoaderUtils;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.*;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLFunctionRecord;
import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.functions.FR_WithState;
import org.qdl_lang.functions.FunctionRecordInterface;
import org.qdl_lang.functions.FunctionReferenceNodeInterface;
import org.qdl_lang.module.MIWrapper;
import org.qdl_lang.module.MTKey;
import org.qdl_lang.module.Module;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.parsing.QDLParserDriver;
import org.qdl_lang.parsing.QDLRunner;
import org.qdl_lang.scripting.QDLScript;
import org.qdl_lang.statements.Element;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.TryCatch;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.util.aggregate.IdentityScalarImpl;
import org.qdl_lang.util.aggregate.QDLAggregateUtil;
import org.qdl_lang.variables.*;
import org.qdl_lang.vfs.VFSPaths;
import org.qdl_lang.workspace.QDLWorkspace;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.MetaDebugUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import org.qdl_lang.state.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

import static org.qdl_lang.config.QDLConfigurationConstants.MODULE_ATTR_VERSION_1_0;
import static org.qdl_lang.variables.StemUtility.axisWalker;
import static org.qdl_lang.vfs.VFSPaths.SCHEME_DELIMITER;
import static edu.uiuc.ncsa.security.core.util.DebugConstants.*;

/**
 * For control structure in loops, conditionals etc.
 * <p>Created by Jeff Gaynor<br>
 * on 1/18/20 at  11:49 AM
 */
public class SystemEvaluator extends AbstractEvaluator {
    public static final String SYS_NAMESPACE = "sys";
    public static final String SYS_FQ = SYS_NAMESPACE + State.NS_DELIMITER;
    public static final int SYSTEM_BASE_VALUE = 5000;
    public static final String DEBUGGER_PROPERTY_NAME_LEVEL = "level";
    public static final String DEBUGGER_PROPERTY_NAME_DELIMITER = "delimiter";
    public static final String DEBUGGER_PROPERTY_NAME_TS_ON = "ts_on";
    public static final String DEBUGGER_PROPERTY_NAME_TITLE = "title";
    public static final String DEBUGGER_PROPERTY_NAME_HOST = "host";

    @Override
    public String getNamespace() {
        return SYS_NAMESPACE;
    }

    // Looping stuff
    public static final String CONTINUE = "continue";
    public static final int CONTINUE_TYPE = 1 + SYSTEM_BASE_VALUE;

    public static final String BREAK = "break";
    public static final int BREAK_TYPE = 2 + SYSTEM_BASE_VALUE;

    public static final String FOR_KEYS = "for_keys";
    public static final int FOR_KEYS_TYPE = 3 + SYSTEM_BASE_VALUE;

    public static final String FOR_NEXT = "for_next";
    public static final int FOR_NEXT_TYPE = 4 + SYSTEM_BASE_VALUE;

    public static final String CHECK_AFTER = "check_after";
    public static final int CHECK_AFTER_TYPE = 5 + SYSTEM_BASE_VALUE;

    public static final String INTERRUPT = "halt";
    public static final int INTERRUPT_TYPE = 6 + SYSTEM_BASE_VALUE;

    public static final String VAR_TYPE = "var_type";
    public static final int VAR_TYPE_TYPE = 7 + SYSTEM_BASE_VALUE;
    public static final String IS_DEFINED = "is_defined";
    public static final int IS_DEFINED_TYPE = 8 + SYSTEM_BASE_VALUE;

    public static final String INTERPRET = "interpret";
    public static final int INTERPRET_TYPE = 9 + SYSTEM_BASE_VALUE;

    public static final String CHECK_SYNTAX = "check_syntax";
    public static final int CHECK_SYNTAX_TYPE = 10 + SYSTEM_BASE_VALUE;

    public static final String INPUT_FORM = "input_form";
    public static final int INPUT_FORM_TYPE = 11 + SYSTEM_BASE_VALUE;

    public static final String REDUCE = "reduce";
    public static final int REDUCE_TYPE = 12 + SYSTEM_BASE_VALUE;

    public static final String EXPAND = "expand";
    public static final int EXPAND_TYPE = 14 + SYSTEM_BASE_VALUE;

    public static final String SAY_FUNCTION = "say";
    public static final int SAY_TYPE = 15 + SYSTEM_BASE_VALUE;

    public static final String TO_STRING = "to_string";
    public static final int TO_STRING_TYPE = 16 + SYSTEM_BASE_VALUE;

    public static final String IS_NULL = "is_null";
    public static final int IS_NULL_TYPE = 17 + SYSTEM_BASE_VALUE;

    public static final String TO_NUMBER = "to_number";
    public static final int TO_NUMBER_TYPE = 20 + SYSTEM_BASE_VALUE;

    public static final String TO_BOOLEAN = "to_boolean";
    public static final int TO_BOOLEAN_TYPE = 21 + SYSTEM_BASE_VALUE;

    public static final String FOR_LINES = "for_lines";
    public static final int FOR_LINES_TYPE = 22 + SYSTEM_BASE_VALUE;

    // function stuff
    public static final String RETURN = "return";
    public static final int RETURN_TYPE = 100 + SYSTEM_BASE_VALUE;
    public static final String SLEEP = "sleep";
    public static final int SLEEP_TYPE = 101 + SYSTEM_BASE_VALUE;

    public static final String MODULE_IMPORT = "module_import";
    public static final int IMPORT_TYPE = 203 + SYSTEM_BASE_VALUE;

    public static final String MODULE_LOAD = "module_load";
    public static final int LOAD_MODULE_TYPE = 205 + SYSTEM_BASE_VALUE;


    public static final String MODULE_PATH = "module_path";
    public static final int MODULE_PATH_TYPE = 211 + SYSTEM_BASE_VALUE;

    public static final String MODULE_REMOVE = "module_remove";
    public static final int MODULE_REMOVE_TYPE = 212 + SYSTEM_BASE_VALUE;

/*    public static final String MODULE_CREATE = "module";
    public static final int MODULE_CREATE_TYPE = 214 + SYSTEM_BASE_VALUE;*/


    // For system constants
    public static final String CONSTANTS = "constants";
    public static final int CONSTANTS_TYPE = 206 + SYSTEM_BASE_VALUE;

    // For system info
    public static final String SYS_INFO = "info";
    public static final int SYS_INFO_TYPE = 207 + SYSTEM_BASE_VALUE;

    // for os environment
    public static final String OS_ENV = "os_env";
    public static final int OS_ENV_TYPE = 208 + SYSTEM_BASE_VALUE;

    // logging
    public static final String SYSTEM_LOG = "logger";
    public static final int SYSTEM_LOG_TYPE = 209 + SYSTEM_BASE_VALUE;


    // logging
    public static final String DEBUG = "debugger";
    public static final int DEBUG_TYPE = 210 + SYSTEM_BASE_VALUE;

    // try ... catch
    public static final String RAISE_ERROR = "raise_error";
    public static final int RAISE_ERROR_TYPE = 300 + SYSTEM_BASE_VALUE;

    // For external programs
    public static final String RUN_COMMAND = "script_run";
    public static final int RUN_COMMAND_TYPE = 400 + SYSTEM_BASE_VALUE;

    public static final String LOAD_COMMAND = "script_load";
    public static final int LOAD_COMMAND_TYPE = 401 + SYSTEM_BASE_VALUE;

    public static final String SCRIPT_ARGS_COMMAND = "script_args";
    public static final int SCRIPT_ARGS_COMMAND_TYPE = 402 + SYSTEM_BASE_VALUE;

    public static final String SCRIPT_PATH_COMMAND = "script_path";
    public static final int SCRIPT_PATH_COMMAND_TYPE = 403 + SYSTEM_BASE_VALUE;
    public static final String FORK = "fork";
    public static final int FORK_TYPE = 404 + SYSTEM_BASE_VALUE;
    public static final String KILL_PROCESS = "kill";
    public static final int KILL_PROCESS_TYPE = 405 + SYSTEM_BASE_VALUE;

    // WS macro
    public static final String WS_MACRO = "ws_macro";
    public static final int WS_MACRO_COMMAND_TYPE = 404 + SYSTEM_BASE_VALUE;

    public static final String HAS_CLIPBOARD = "cb_exists";
    public static final int HAS_CLIPBOARD_COMMAND_TYPE = 405 + SYSTEM_BASE_VALUE;

    public static final String CLIPBOARD_COPY = "cb_read";
    public static final int CLIPBOARD_COPY_COMMAND_TYPE = 406 + SYSTEM_BASE_VALUE;

    public static final String CLIPBOARD_PASTE = "cb_write";
    public static final int CLIPBOARD_PASTE_COMMAND_TYPE = 407 + SYSTEM_BASE_VALUE;

    public static final String SCRIPT_ARGS2_COMMAND = "args";

    public static final String SCRIPT_NAME_COMMAND = "script_name";
    public static final int SCRIPT_NAME_COMMAND_TYPE = 408 + SYSTEM_BASE_VALUE;


    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{
                    SCRIPT_NAME_COMMAND,
                    KILL_PROCESS,
                    FORK,
                    SLEEP,
                    HAS_CLIPBOARD,
                    CLIPBOARD_COPY,
                    CLIPBOARD_PASTE,
                    WS_MACRO,
                    IS_DEFINED,
                    IS_NULL,
                    VAR_TYPE,
                    TO_NUMBER,
                    TO_STRING,
                    TO_BOOLEAN,
                    SAY_FUNCTION,
                    REDUCE, EXPAND,
                    SCRIPT_PATH_COMMAND,
                    SCRIPT_ARGS_COMMAND,
                    SCRIPT_ARGS2_COMMAND,
                    SYS_INFO,
                    OS_ENV,
                    SYSTEM_LOG,
                    DEBUG,
                    CONSTANTS,
                    CONTINUE,
                    INTERRUPT,
                    BREAK,
                    INTERPRET,
                    CHECK_SYNTAX,
                    INPUT_FORM,
                    FOR_KEYS,
                    FOR_NEXT,
                    FOR_LINES,
                    CHECK_AFTER,
                    RETURN,
                    MODULE_IMPORT,
                    MODULE_REMOVE,
                    MODULE_LOAD,
                    MODULE_PATH,
                    RAISE_ERROR,
                    RUN_COMMAND,
                    LOAD_COMMAND};
        }
        return fNames;
    }


    @Override
    public int getType(String name) {
        switch (name) {
            case SCRIPT_NAME_COMMAND:
                return SCRIPT_NAME_COMMAND_TYPE;
            case KILL_PROCESS:
                return KILL_PROCESS_TYPE;
            case FORK:
                return FORK_TYPE;
            case SLEEP:
                return SLEEP_TYPE;
            case IS_NULL:
                return IS_NULL_TYPE;
            case HAS_CLIPBOARD:
                return HAS_CLIPBOARD_COMMAND_TYPE;
            case CLIPBOARD_COPY:
                return CLIPBOARD_COPY_COMMAND_TYPE;
            case CLIPBOARD_PASTE:
                return CLIPBOARD_PASTE_COMMAND_TYPE;
            case WS_MACRO:
                return WS_MACRO_COMMAND_TYPE;
            case VAR_TYPE:
                return VAR_TYPE_TYPE;
            case IS_DEFINED:
                return IS_DEFINED_TYPE;
            case SAY_FUNCTION:
                return SAY_TYPE;
            case TO_NUMBER:
                return TO_NUMBER_TYPE;
            case TO_STRING:
                return TO_STRING_TYPE;
            case TO_BOOLEAN:
                return TO_BOOLEAN_TYPE;
            case EXPAND:
                return EXPAND_TYPE;
            case REDUCE:
                return REDUCE_TYPE;
            case SCRIPT_PATH_COMMAND:
                return SCRIPT_PATH_COMMAND_TYPE;
            case SCRIPT_ARGS_COMMAND:
                return SCRIPT_ARGS_COMMAND_TYPE;
            case SCRIPT_ARGS2_COMMAND:
                return SCRIPT_ARGS_COMMAND_TYPE;
            case OS_ENV:
                return OS_ENV_TYPE;
            case DEBUG:
                return DEBUG_TYPE;
            case SYSTEM_LOG:
                return SYSTEM_LOG_TYPE;
            case SYS_INFO:
                return SYS_INFO_TYPE;
            case CONSTANTS:
                return CONSTANTS_TYPE;
            case CONTINUE:
                return CONTINUE_TYPE;
            case INTERPRET:
                return INTERPRET_TYPE;
            case INPUT_FORM:
                return INPUT_FORM_TYPE;
            case CHECK_SYNTAX:
                return CHECK_SYNTAX_TYPE;
            case INTERRUPT:
                return INTERRUPT_TYPE;
            case BREAK:
                return BREAK_TYPE;
            case RETURN:
                return RETURN_TYPE;
            case FOR_KEYS:
                return FOR_KEYS_TYPE;
            case FOR_NEXT:
                return FOR_NEXT_TYPE;
            case FOR_LINES:
                return FOR_LINES_TYPE;
            case CHECK_AFTER:
                return CHECK_AFTER_TYPE;
            // Module stuff
            case MODULE_IMPORT:
                return IMPORT_TYPE;
            case MODULE_LOAD:
                return LOAD_MODULE_TYPE;
            case MODULE_PATH:
                return MODULE_PATH_TYPE;
            case MODULE_REMOVE:
                return MODULE_REMOVE_TYPE;
            case RAISE_ERROR:
                return RAISE_ERROR_TYPE;
            case RUN_COMMAND:
                return RUN_COMMAND_TYPE;
            case LOAD_COMMAND:
                return LOAD_COMMAND_TYPE;
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }

    @Override
    public boolean dispatch(Polyad polyad, State state) {
        // NOTE NOTE NOTE!!! The for_next, has_keys, check_after functions are NOT evaluated here. In
        // the WhileLoop class they are picked apart for their contents and the correct looping strategy is
        // done. Look at the WhileLoop's evaluate method. All this evaluator
        // does is mark them as built-in functions.
        boolean printIt = false;

        switch (polyad.getName()) {
            case SCRIPT_NAME_COMMAND:
                doScriptName(polyad, state);
                return true;
            case KILL_PROCESS:
                doKillProcess(polyad, state);
                return true;
            case FORK:
                doFork(polyad, state);
                return true;
            case SLEEP:
                doSleep(polyad, state);
                return true;
            case IS_NULL:
                doIsNull(polyad, state);
                return true;
            case FOR_LINES:
                doForLines(polyad, state);
                return true;
            case FOR_NEXT:
                doForNext(polyad, state);
                return true;
            case FOR_KEYS:
                doForKeys(polyad, state);
                return true;
            case CHECK_AFTER:
                doCheckAfter(polyad, state);
                return true;
            case HAS_CLIPBOARD:
                doHasClipboard(polyad, state);
                return true;
            case CLIPBOARD_COPY:
                doClipboardRead(polyad, state);
                return true;
            case CLIPBOARD_PASTE:
                doClipboardWrite(polyad, state);
                return true;
            case WS_MACRO:
                doWSMacro(polyad, state);
                return true;
            case VAR_TYPE:
                doVarType(polyad, state);
                return true;
            case IS_DEFINED:
                isDefined(polyad, state);
                return true;
            case TO_NUMBER:
                doToNumber(polyad, state);
                return true;
            case TO_BOOLEAN:
                doToBoolean(polyad, state);
                return true;

            case SAY_FUNCTION:
                printIt = true;
            case TO_STRING:
                doSay(polyad, state, printIt);
                return true;
            case EXPAND:
                doReduceOrExpand(polyad, state, false);
                return true;
            case REDUCE:
                doReduceOrExpand(polyad, state, true);
                return true;
            case SCRIPT_PATH_COMMAND:
                doScriptPaths(polyad, state);
                return true;
            case SCRIPT_ARGS_COMMAND:
                doScriptArgs(polyad, state);
                return true;
            case SCRIPT_ARGS2_COMMAND:
                doScriptArgs2(polyad, state);
                return true;

            case BREAK:
                if (polyad.isSizeQuery()) {
                    polyad.setResult(new int[]{0, 1});
                    polyad.setEvaluated(true);
                    return true;
                }
                boolean doBreak = true;
                if (0 == polyad.getArgCount()) {
                    // niladic break -- just break
                }
                if (1 == polyad.getArgCount()) {
                    Object output = polyad.evalArg(0, state);
                    if (!(output instanceof Boolean)) {
                        throw new BadArgException(BREAK + " requires a boolean as its argument", polyad.getArgAt(0));
                    }
                    doBreak = (Boolean) output;
                }
                if (1 < polyad.getArgCount()) {
                    throw new ExtraArgException(BREAK + " takes at most an argument", polyad.getArgAt(1));
                }

                polyad.setEvaluated(true);
                polyad.setResultType(Constant.BOOLEAN_TYPE);
                polyad.setResult(doBreak);
                if (doBreak) {
                    throw new BreakException();
                }
                return true;
            case CONSTANTS:
                doConstants(polyad, state);
                return true;
            case SYS_INFO:
                doSysInfo(polyad, state);
                return true;
            case OS_ENV:
                doOSEnv(polyad, state);
                return true;
            case SYSTEM_LOG:
                doSysLog(polyad, state, false);
                return true;
            case DEBUG:
                doSysLog(polyad, state, true);
                return true;
            case CONTINUE:
                if (polyad.isSizeQuery()) {
                    polyad.setResult(new int[]{0, 1});
                    polyad.setEvaluated(true);
                    return true;
                }
                boolean doContinue = false;
                if (0 == polyad.getArgCount()) {
                    doContinue = true;
                }
                if (1 == polyad.getArgCount()) {
                    Object output = polyad.evalArg(0, state);
                    if (!(output instanceof Boolean)) {
                        throw new BadArgException(CONTINUE + " require an boolean as its argument", polyad.getArgAt(0));
                    }
                    doContinue = (Boolean) output;
                }
                if (1 < polyad.getArgCount()) {
                    throw new ExtraArgException(CONTINUE + " does not take an argument", polyad.getArgAt(0));
                }
                polyad.setEvaluated(true);
                polyad.setResultType(Constant.BOOLEAN_TYPE);
                polyad.setResult(doContinue);
                if (doContinue) {
                    throw new ContinueException();
                }
                return true;
            case INTERRUPT:
                doInterrupt(polyad, state);
                return true;
            case RETURN:
                doReturn(polyad, state);
                return true;
            case RUN_COMMAND:
                runScript(polyad, state);
                return true;
            case LOAD_COMMAND:
                loadScript(polyad, state);
                return true;
            case MODULE_IMPORT:
                doModuleImport(polyad, state);
                return true;
            case MODULE_LOAD:
                doLoadModule(polyad, state);
                return true;
            case MODULE_PATH:
                doModulePaths(polyad, state);
                return true;
            case MODULE_REMOVE:
                doModuleRemove(polyad, state);
                return true;
            case RAISE_ERROR:
                doRaiseError(polyad, state);
                return true;
            case INTERPRET:
                doInterpret(polyad, state);
                return true;
            case CHECK_SYNTAX:
                doCheckSyntax(polyad, state);
                return true;
            case INPUT_FORM:
                doInputForm(polyad, state);
                return true;
        }
        return false;
    }

    private void doScriptName(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0});
            polyad.setEvaluated(true);
            return;
        }
        String name = "";
        if (state.hasScriptName()) {
            name = state.getScriptName();
        }
        polyad.setResult(name);
        polyad.setResultType(Constant.STRING_TYPE);
        polyad.setEvaluated(true);
    }


    protected void doKillProcess(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        Object out = polyad.evalArg(0, state);
        if (!isLong(out)) {
            throw new BadArgException(KILL_PROCESS + " requires an integer argument", polyad);
        }
        Long pid = (Long) out;
        if (state.getThreadTable().containsKey(pid.intValue())) {
            try {
                state.getThreadTable().get(pid.intValue()).qdlThread.interrupt();
                state.getThreadTable().remove(pid.intValue());
                polyad.setResult(1L);
                polyad.setEvaluated(true);
                polyad.setResultType(Constant.LONG_TYPE);
                return;
            } catch (Throwable t) {

                // t.printStackTrace();
            }
        }
        polyad.setResult(0L);
        polyad.setEvaluated(true);
        polyad.setResultType(Constant.LONG_TYPE);
    }

    private void doSleep(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        Object out = polyad.evalArg(0, state);
        if (!isLong(out)) {
            throw new BadArgException(SLEEP + " requires an integer argument", polyad);
        }
        long start = System.currentTimeMillis();
        try {
            Thread.currentThread().sleep((Long) out);
            polyad.setEvaluated(true);
            polyad.setResult((Long) (System.currentTimeMillis() - start));
            polyad.setResultType(Constant.LONG_TYPE);
        } catch (InterruptedException e) {
            //  e.printStackTrace();
            // Do nothing. (???))
        }

    }


    /**
     * Run a script in its own thread. Passed variables are shared.
     *
     * @param polyad
     * @param state
     */
    protected void doFork(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(AbstractEvaluator.getBigArgList());
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLExceptionWithTrace(FORK + " not supported in server mode.", polyad);
        }
        int pid = runnit(polyad, state, state.getScriptPaths(), false, true);
        polyad.setEvaluated(true);
        polyad.setResult((long) pid);
        polyad.setResultType(Constant.LONG_TYPE);
    }

    private void doIsNull(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }

        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(IS_NULL + " requires an argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(IS_NULL + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object result = polyad.evalArg(0, state);
        polyad.setEvaluated(true);
        polyad.setResult(result instanceof QDLNull ? Boolean.TRUE : Boolean.FALSE);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
    }

    private void doForLines(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        throw new NotImplementedException(FOR_LINES + " can only be executed in a loop");
    }

    private void doCheckAfter(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        throw new NotImplementedException(CHECK_AFTER + " can only be executed in a loop");
    }

    private void doForKeys(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2});
            polyad.setEvaluated(true);
            return;
        }
        throw new NotImplementedException(FOR_KEYS + " can only be executed in a loop");
    }

    private void doForNext(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3, 4});
            polyad.setEvaluated(true);
            return;
        }
        throw new NotImplementedException(FOR_NEXT + " can only be executed in a loop");
    }


    private void doClipboardRead(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException(CLIPBOARD_COPY + " not supported in server mode");
        }


        if (0 < polyad.getArgCount()) {
            throw new ExtraArgException(CLIPBOARD_COPY + " takes no arguments", polyad);
        }


        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String out = (String) clipboard.getData(DataFlavor.stringFlavor);
            out = out.trim(); // removes random line feeds at the end.
            polyad.setEvaluated(true);
            polyad.setResult(out);
            polyad.setResultType(Constant.STRING_TYPE);
            return;
        } catch (Throwable t) {

        }
        polyad.setEvaluated(true);
        polyad.setResult(QDLNull.getInstance());
        polyad.setResultType(Constant.NULL_TYPE);
    }

    private void doClipboardWrite(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException(CLIPBOARD_PASTE + " not supported in server mode");
        }

        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(CLIPBOARD_PASTE + " requires an argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(CLIPBOARD_PASTE + " requires at most 1 argument", polyad.getArgAt(1));
        }

        polyad.evalArg(0, state);
        Object obj = polyad.getArgAt(0).getResult();

        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (clipboard != null) {
                StringSelection data = new StringSelection(obj.toString());
                clipboard.setContents(data, data);
                polyad.setEvaluated(true);
                polyad.setResult(Boolean.TRUE);
                polyad.setResultType(Constant.BOOLEAN_TYPE);
            }
        } catch (Throwable t) {
            // there was a problem with the clipboard. Skip it.
            polyad.setEvaluated(true);
            polyad.setResult(Boolean.FALSE);
            polyad.setResultType(Constant.BOOLEAN_TYPE);
        }
    }

    /**
     * Checks if there is clipboard support.
     *
     * @param polyad
     * @param state
     */
    private void doHasClipboard(Polyad polyad, State state) {
        // Annoying thing #42. we check if the clipboard exists by trying to read from it
        // this is the most reliable cross platform way to do it. The problem is that
        // error messages can be generated very deep in the stack that cannot be intercepted
        // with a try...catch block and sent to std err. So we have to turn redirect the error
        // then reset the std err. Pain in the neck, but users should not see large random
        // stack traces that the last thing someone left on their clipboard can't be
        // easily converted to a string.
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException(HAS_CLIPBOARD + " not supported in server mode");
        }


        if (0 < polyad.getArgCount()) {
            throw new ExtraArgException(HAS_CLIPBOARD + " requires no arguments", polyad);
        }

        PrintStream errStream = System.err;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(byteArrayOutputStream));

        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.getData(DataFlavor.stringFlavor);
            System.setErr(errStream);
            polyad.setEvaluated(true);
            polyad.setResult(Boolean.TRUE);
            polyad.setResultType(Constant.BOOLEAN_TYPE);
            return;
        } catch (Throwable t) {
            if (state.getLogger() != null) {
                state.getLogger().info("Probably benign message from checking clipboard:");
            }
        }
        System.setErr(errStream);
        polyad.setEvaluated(true);
        polyad.setResult(Boolean.FALSE);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
    }

    private void doWSMacro(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (state.getWorkspaceCommands() == null) {
            throw new IllegalStateException("no workspace active");
        }

        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(WS_MACRO + " requires 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(WS_MACRO + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object obj = polyad.evalArg(0, state);
        List<String> commands = null;
        // options are a string of commands, separated by CR/LF or a stem of them
        if (obj instanceof String) {
            commands = new ArrayList<>();
            StringTokenizer stringTokenizer = new StringTokenizer(obj.toString(), "\n");
            while (stringTokenizer.hasMoreTokens()) {
                String line = stringTokenizer.nextToken();
                if (isMarcoLineAComment(line)) {
                    continue;
                }
                commands.add(line);
            }
        }
        if (obj instanceof QDLStem) {
            QDLStem stemVariable = (QDLStem) obj;
            if (!stemVariable.isList()) {
                throw new BadArgException(WS_MACRO + " requires a list", polyad.getArgAt(0));
            }
            commands = new ArrayList<>();

            for (Object key : stemVariable.keySet()) {
                Object line = stemVariable.get(key);
                if (!(line instanceof String)) {
                    throw new BadArgException(WS_MACRO + " the argument '" + line + "' is not a string", polyad.getArgAt(0));
                }

                if (isMarcoLineAComment((String) line)) {
                    continue;
                }
                commands.add((String) line);
            }
        }
        state.getWorkspaceCommands().runMacro(commands);
        polyad.setResult(Boolean.TRUE);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
        polyad.setEvaluated(true);
    }

    private boolean isMarcoLineAComment(String line) {
        return line.trim().startsWith(QDLWorkspace.MACRO_COMMENT_DELIMITER);
    }

    /**
     * Module remove works ONLY for old modules. New system uses unload.
     *
     * @param polyad
     * @param state
     */
    private void doModuleRemove(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(MODULE_REMOVE + " requires  1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(MODULE_REMOVE + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object result = polyad.evalArg(0, state);
        QDLStem aliases = null;
        // normalize argument
        if (result instanceof QDLStem) {
            aliases = (QDLStem) result;
        }
        if (result instanceof String) {
            aliases = new QDLStem();
            aliases.put(0L, (String) result);
        }
        if (aliases == null) {
            throw new BadArgException(MODULE_REMOVE + " unknown argument type '" + result + "'", polyad.getArgAt(0));
        }
        for (Object key : aliases.keySet()) {
            Object object2 = aliases.get(key);
            if (!isString(object2)) {
                throw new BadArgException(MODULE_REMOVE + " second argument must be a string.", polyad.getArgAt(1));
            }
            XKey xKey = new XKey(object2.toString());
            state.getMInstances().remove(xKey);
        }
        polyad.setEvaluated(true);
        polyad.setResult(Boolean.TRUE);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
    }

    /*
      times(x,y)->x*y
      reduce(@times(), 1+n(5))

  z. := {'a':'x_baz', 'b':3, 'c':'x_bar', 'd':'woof'}
      q. :=  subset((x)->index_of(x, 'x_')==0, z.)
        reduce(@&&, q. == q.)

     */
    private void doReduceOrExpand(Polyad polyad, State state0, boolean doReduce) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{2, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 2) {
            throw new MissingArgException((doReduce ? REDUCE : EXPAND) + " requires at least 2 arguments", polyad.getArgCount() == 1 ? polyad.getArgAt(0) : polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException((doReduce ? REDUCE : EXPAND) + " requires at most 3 arguments", polyad.getArgAt(3));
        }

        State state = state0.newLocalState();
        FunctionReferenceNodeInterface frn;
        ExpressionInterface arg0 = polyad.getArguments().get(0);

        frn = getFunctionReferenceNode(state, polyad.getArguments().get(0), true);
        Object arg1 = polyad.evalArg(1, state);
        checkNull(arg1, polyad.getArgAt(1), state);
        if (isScalar(arg1)) {
            polyad.setResult(arg1);
            polyad.setResultType(Constant.getType(arg1));
            polyad.setEvaluated(true);
            return;
        }
        if (doReduce && isSet(arg1)) {
            QDLSet argSet = (QDLSet) arg1;
            ExpressionImpl f;
            try {
                f = getOperator(state, frn, 2);
            } catch (UndefinedFunctionException ufx) {
                ufx.setStatement(polyad.getArgAt(0));
                throw ufx;
            }
            Object lastResult = null;
            ArrayList<Object> args = new ArrayList<>();
            for (Object element : argSet) {
                if (lastResult == null) {
                    // first pass
                    lastResult = element;
                    continue;
                }
                args.clear();
                args.add(lastResult);
                args.add(element);
                f.setArguments(toConstants(args));
                lastResult = f.evaluate(state);
            }
            polyad.setResult(lastResult);
            polyad.setResultType(Constant.getType(lastResult));
            polyad.setEvaluated(true);
            return;
        }

        if (!doReduce && !isStem(arg1)) {
            throw new BadArgException(EXPAND + " requires a list as its argument", polyad.getArgAt(1));
        }
        int axis = 0; // default
        boolean hasAxisExpression = arg1 instanceof AxisExpression;
        AxisExpression ax = null;
        boolean isStar = false;
        QDLStem stemVariable;
        if (hasAxisExpression) {
            ax = (AxisExpression) arg1;
            if (ax.isStar()) {
                isStar = true;
            } else {
                axis = ax.getAxis().intValue();
            }
            stemVariable = ax.getStem();
        } else {
            stemVariable = (QDLStem) arg1;
        }


        if (!doReduce && !stemVariable.isList()) {
            throw new BadArgException(EXPAND + " requires a list as its argument", polyad.getArgAt(1));
        }


        if (doReduce && !stemVariable.isList()) {
            // allow for reducing stems to a scalar
            Object rrr = null;
            ExpressionImpl f;
            try {
                f = getOperator(state, frn, 2);
            } catch (UndefinedFunctionException ufx) {
                ufx.setStatement(polyad.getArgAt(0));
                throw ufx;
            }
            // Special case. Axis expression and has star as the axis ==> reduce all. This uses
            // the QDLAggregate Util and is way simpler than the walker below.
            if (hasAxisExpression && isStar) {
                doReduceAll(polyad, f, state, stemVariable);
                return;
            }
            Object lastResult = null;
            ArrayList<Object> args = new ArrayList<>();
            for (Object key : stemVariable.keySet()) {
                Object nextValue = stemVariable.get(key);
                if (lastResult == null) {
                    // first pass
                    lastResult = nextValue;
                    continue;
                }
                args.clear();
                args.add(lastResult);
                args.add(nextValue);
                f.setArguments(toConstants(args));
                lastResult = f.evaluate(state);
            }
            polyad.setResult(lastResult);
            polyad.setResultType(Constant.getType(lastResult));
            polyad.setEvaluated(true);
            return;
        }

        if (!hasAxisExpression && polyad.getArgCount() == 3) {
            Object axisObj = polyad.evalArg(2, state);
            if (axisObj instanceof AllIndices) {
                isStar = true;
            } else {
                checkNull(axisObj, polyad.getArgAt(2));

                if (!isLong(axisObj)) {
                    throw new BadArgException("third argument of " + (doReduce ? REDUCE : EXPAND) + ", the axis, must be an integer", polyad.getArgAt(2));
                }
                axis = ((Long) axisObj).intValue();
            }
        }

        if (stemVariable.size() == 0) {
            polyad.setEvaluated(true);
            if (doReduce) {
                // result is a scalar
                polyad.setResult(QDLNull.getInstance());
                polyad.setResultType(Constant.NULL_TYPE);
            } else {
                polyad.setResult(new QDLStem());
                polyad.setResultType(Constant.STEM_TYPE);
            }
            // result is an empty stem
            return;
        }
        if (stemVariable.size() == 1) {
            SparseEntry sparseEntry = stemVariable.getQDLList().first();
            polyad.setEvaluated(true);

            if (doReduce) {
                // return scalar of the value
                polyad.setResult(sparseEntry.entry);
                polyad.setResultType(Constant.getType(sparseEntry.entry));

            } else {
                QDLStem output = new QDLStem();
                output.listAdd(sparseEntry.entry);
                polyad.setResult(output);
                polyad.setResultType(Constant.STEM_TYPE);
            }
            return;
        }
// @*⊙n(3,3,[;9])`*
        //if(doReduce && hasAxisExpression && ax.isStar()){
        if (doReduce && isStar) {
            doReduceAll(polyad, getOperator(state, frn, 2), state, stemVariable);
            return;
        }
        StemUtility.StemAxisWalkerAction1 axisWalker;
        try {
            if (doReduce) {
                axisWalker = this.new AxisReduce(getOperator(state, frn, 2), state);
            } else {
                axisWalker = this.new AxisExpand(getOperator(state, frn, 2), state);
            }
        } catch (UndefinedFunctionException ufx) {
            ufx.setStatement(polyad.getArgAt(0));
            throw ufx;
        }
        Object result = axisWalker(stemVariable, axis, axisWalker);
        polyad.setResult(result);
        polyad.setResultType(Constant.getType(result));
        polyad.setEvaluated(true);
    }

    private static void doReduceAll(Polyad polyad, ExpressionImpl f, State state, QDLStem stemVariable) {
        Object rrr;
        ReduceAll reduceAll = new ReduceAll(f, state);
        QDLAggregateUtil.process(stemVariable, reduceAll);
        rrr = reduceAll.out;
        polyad.setResult(rrr);
        polyad.setResultType(Constant.getType(rrr));
        polyad.setEvaluated(true);
    }

    public static class ReduceAll extends IdentityScalarImpl {
        public ReduceAll(ExpressionImpl f, State state) {
            this.f = f;
            this.state = state;
        }

        Object out = null;
        ExpressionImpl f;
        State state;

        @Override
        public Object getDefaultValue(List<Object> index, Object key, Object value) {
            if (out == null) {
                out = value;
            } else {
                ConstantNode arg1 = new ConstantNode(out);
                ConstantNode arg2 = new ConstantNode(value);
                f.getArguments().clear();
                f.getArguments().add(arg1);
                f.getArguments().add(arg2);
                f.evaluate(state);
                out = f.getResult();
            }
            return super.getDefaultValue(index, key, value);
        }
    }

    public class AxisReduce implements StemUtility.StemAxisWalkerAction1 {
        ExpressionImpl operator;
        State state;

        public AxisReduce(ExpressionImpl operator, State state) {
            this.operator = operator;
            this.state = state;
        }

        @Override
        public Object action(QDLStem inStem) {
            Object reduceOuput = null;
            // contract is that a single list is returned unaltered.
            // At this point, we know there are at least 2 entries.
            Iterator iterator = inStem.getQDLList().iterator(true);
            Object lastValue = iterator.next();

            while (iterator.hasNext()) {
                Object currentValue = iterator.next();
                ArrayList<ExpressionInterface> argList = new ArrayList<>();
                argList.add(new ConstantNode(lastValue, Constant.getType(lastValue)));
                argList.add(new ConstantNode(currentValue, Constant.getType(currentValue)));
                operator.setArguments(argList);
                operator.evaluate(state);
                reduceOuput = operator.getResult();
                lastValue = operator.getResult();
            }
            return reduceOuput;
        }
    }

    public class AxisExpand implements StemUtility.StemAxisWalkerAction1 {
        ExpressionImpl operator;
        State state;

        public AxisExpand(ExpressionImpl operator, State state) {
            this.operator = operator;
            this.state = state;
        }

        @Override
        public Object action(QDLStem inStem) {
            QDLStem output = new QDLStem();
            //Set<String> keySet = inStem.keySet();
            Iterator iterator = inStem.keySet().iterator();

            Object lastValue = inStem.get(iterator.next()); // grab one before loop starts
            output.listAdd(lastValue);

            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object currentValue = inStem.get(key);
                ArrayList<ExpressionInterface> argList = new ArrayList<>();
                argList.add(new ConstantNode(lastValue, Constant.getType(lastValue)));
                argList.add(new ConstantNode(currentValue, Constant.getType(currentValue)));
                operator.setArguments(argList);
                operator.evaluate(state);
                output.putLongOrString(key, operator.getResult());
                lastValue = operator.getResult();
            }
            return output;
        }
    }


    private void doInputForm(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(INPUT_FORM + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(INPUT_FORM + " requires at most 2 arguments", polyad.getArgAt(2));
        }

        if (polyad.getArguments().get(0).getNodeType() == ExpressionInterface.CONSTANT_NODE) {
            Object arg = polyad.evalArg(0, state);
            String out = InputFormUtil.inputForm(arg);
            if (out == null) {
                out = "";
            }
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setResult(out);
            return;
        }
        boolean gotOne = false;
        String argName = null;
        // This ferrets out the input form for something inside a module, so the argument
        // is a#b, and the input form of b will be returned.
        if (polyad.getArguments().get(0) instanceof ModuleExpression) {
            ModuleExpression moduleExpression = (ModuleExpression) polyad.getArguments().get(0);
            Module module;
            Object object = state.getValue(moduleExpression.getAlias());
            if (object instanceof Module) {
                module = (Module) object;
            } else {
                module = state.getMInstances().getModule(new XKey(moduleExpression.getAlias()));
            }
            if (module == null) {
                throw new BadArgException("no module named '" + moduleExpression.getAlias() + "' found.", moduleExpression);
            }
            if (!(moduleExpression.getExpression() instanceof VariableNode)) {
                throw new BadArgException(INPUT_FORM + " requires a variable or function name", moduleExpression.getExpression());
            }

            argName = ((VariableNode) moduleExpression.getExpression()).getVariableReference();
            moduleExpression.setModuleState(module.getState());
            state = moduleExpression.getModuleState(state);
            gotOne = true;
        }

        if (polyad.getArguments().get(0) instanceof VariableNode) {
            argName = ((VariableNode) polyad.getArguments().get(0)).getVariableReference();
            gotOne = true;
        } else {
            if ((!gotOne) && (polyad.getArguments().get(0) instanceof ExpressionImpl || polyad.getArguments().get(0) instanceof ExpressionNode)) {
                String out = InputFormUtil.inputForm(polyad.evalArg(0, state));
                if (out == null) {
                    out = "";
                }
                polyad.setEvaluated(true);
                polyad.setResultType(Constant.STRING_TYPE);
                polyad.setResult(out);
                return;
            } else {
            }
        }

        if (!gotOne) {
            throw new BadArgException(INPUT_FORM + " requires a variable or function name", polyad.getArgAt(0));
        }

        if (argName == null) {
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            polyad.setResult(QDLNull.getInstance());
            return;

        }
        // So a single argument is resolved first to see if it is a module. If so that
        // is returned. If not, then check to see if the argument is a variable.
        // If there is a name conflict (such as an alias is the same as a variable name)
        // the module is returned. In that case, the user should use the dyadic version
        // of this function to disambiguate.
        if (polyad.getArgCount() == 1) {
            String out = InputFormUtil.inputFormModule(argName, state);
            if (out != null) {
                polyad.setResult(out);
                polyad.setResultType(Constant.STRING_TYPE);
                polyad.setEvaluated(true);
                return;
            }
            // simple variable case, no indent

            String output = InputFormUtil.inputFormVar(argName, state);
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setEvaluated(true);
            polyad.setResult(output);
            return;
        }

        if (polyad.getArgCount() != 2) {
            throw new BadArgException(INPUT_FORM + " requires the argument count or boolean as the second parameter", polyad.getArgAt(1));
        }
        Object arg2 = polyad.evalArg(1, state);
        checkNull(arg2, polyad.getArgAt(1), state);
        boolean doIndent = false;
        int argCount = -1;

        switch (Constant.getType(arg2)) {
            case Constant.LONG_TYPE:
                argCount = ((Long) arg2).intValue();
                break;
            case Constant.BOOLEAN_TYPE:
                doIndent = (Boolean) arg2;
                break;
            default:
                throw new BadArgException(INPUT_FORM + " requires an argument count for functions or a boolean ", polyad.getArgAt(1));
        }
        if (polyad.getArgCount() == 2 && isBoolean(arg2)) {
            // process as variable with indent factor
            String output = InputFormUtil.inputFormVar(argName, 2, state);
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setEvaluated(true);
            polyad.setResult(output);
            return;
        }
        // case here is that second arg is not a boolean (==> must be arg count) OR there is more than one arg.
        if (!isLong(arg2)) {
            throw new BadArgException(INPUT_FORM + " second argument must be an integer", polyad.getArgAt(1));
        }

        FR_WithState fr_withState = state.resolveFunction(argName, argCount, true);
        if (fr_withState == null) {
            // no such critter
            polyad.setResult("");
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        FunctionRecordInterface fr = fr_withState.functionRecord;
        String output = "";
        if (fr != null) {
            if (fr instanceof QDLFunctionRecord) {
                QDLFunction qf = ((QDLFunctionRecord) fr).qdlFunction;
                if (qf != null) {
                    output = "java:" + qf.getClass().getCanonicalName();
                }
            } else {
                output = StringUtils.listToString(fr.getSourceCode());
            }
        }
        polyad.setResult(output);
        polyad.setResultType(Constant.STRING_TYPE);
        polyad.setEvaluated(true);


    }


    public static final String SHEBANG = "#!";

    private void doCheckSyntax(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(CHECK_SYNTAX + " requires an argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(CHECK_SYNTAX + " requires at most 1 argument", polyad.getArgAt(1));
        }
        Object arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0), state);
        if (!isString(arg0)) {
            throw new BadArgException("argument to " + CHECK_SYNTAX + " must be a string.", polyad.getArgAt(0));
        }
        String rawString = ((String) arg0).trim();
        if (rawString.startsWith(SHEBANG)) {
            // if it's a script, drop the very first line or the parser chokes.
            rawString = rawString.substring(rawString.indexOf("\n"));
        }
        StringReader r = new StringReader(rawString);
        String message = "";
        QDLParserDriver driver = new QDLParserDriver(new XProperties(), state.newCleanState());
        try {
            new QDLRunner(driver.parse(r));
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new QDLException("non-syntax error:" + t.getMessage());
        }
        polyad.setEvaluated(true);
        polyad.setResult(message);
        polyad.setResultType(Constant.STRING_TYPE);
    }

    protected void doInterrupt(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            // no interrupts in server mode.
            // These may be present, but are ignored rather than throwing an exception. This way debugging aids
            // can stay in place but don't do anything on the server.
            polyad.setResult(-1L);
            polyad.setResultType(Constant.LONG_TYPE);
            polyad.setEvaluated(true);
            return;
        }


        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(INTERRUPT + " requires at most 1 argument", polyad.getArgAt(1));
        }

        String message = "";
        switch (polyad.getArgCount()) {
            case 0:
                // do nothing.
                break;
            case 1:
                Object obj = polyad.evalArg(0, state);
                if (obj == null || (obj instanceof QDLNull)) {
                    message = "(null)";
                } else {
                    message = obj.toString();
                }
                break;
            default:
                throw new ExtraArgException(INTERRUPT + " accepts at most one argument.", polyad.getArgAt(1));
        }
        SIEntry sie = new SIEntry();
        sie.state = state;
        sie.message = message;

        throw new InterruptException(sie);
    }


    public static final int LOG_LEVEL_UNKNOWN = -100;
    public static final int LOG_LEVEL_NONE = DEBUG_LEVEL_OFF;
    public static final int LOG_LEVEL_INFO = DEBUG_LEVEL_INFO;
    public static final int LOG_LEVEL_WARN = DEBUG_LEVEL_WARN;
    public static final int LOG_LEVEL_ERROR = DEBUG_LEVEL_ERROR;
    public static final int LOG_LEVEL_SEVERE = DEBUG_LEVEL_SEVERE;
    public static final int LOG_LEVEL_TRACE = DEBUG_LEVEL_TRACE;

    private Level getLogLevel(Long myLevel) {
        return getLogLevel(myLevel.intValue());
    }

    /**
     * Just checks that the logging level is set in the right range,
     *
     * @param longLevel
     * @return
     */
    private boolean isValidLoggingLevel(Long longLevel) {
        int value = longLevel.intValue();
        switch (value) {
            case LOG_LEVEL_NONE:
            case LOG_LEVEL_TRACE:
            case LOG_LEVEL_INFO:
            case LOG_LEVEL_WARN:
            case LOG_LEVEL_ERROR:
            case LOG_LEVEL_SEVERE:
                return true;
            default:
                return false;
        }
    }

    private Level getLogLevel(int myLevel) {
        switch (myLevel) {
            default:
            case LOG_LEVEL_NONE:
                return Level.OFF;
            case LOG_LEVEL_TRACE:
                return Level.FINEST;
            case LOG_LEVEL_INFO:
                return Level.INFO;
            case LOG_LEVEL_WARN:
                return Level.WARNING;
            case LOG_LEVEL_ERROR:
                return Level.ALL;
            case LOG_LEVEL_SEVERE:
                return Level.SEVERE;
        }
    }

    protected int getMyLogLevel(Level level) {
        int value = level.intValue();
        if (value == Level.OFF.intValue()) return LOG_LEVEL_NONE;
        if (value == Level.FINEST.intValue()) return LOG_LEVEL_TRACE;
        if (value == Level.INFO.intValue()) return LOG_LEVEL_INFO;
        if (value == Level.WARNING.intValue()) return LOG_LEVEL_WARN;
        if (value == Level.ALL.intValue()) return LOG_LEVEL_ERROR;
        if (value == Level.SEVERE.intValue()) return LOG_LEVEL_SEVERE;

        return LOG_LEVEL_UNKNOWN;
    }

    /**
     * Write to the system log.
     * log(message) - info
     * log(message, int) 0 - 5
     *
     * @param polyad
     * @param state
     */
    private void doSysLog(Polyad polyad, State state, boolean isDebug) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException((isDebug ? DEBUG : SYSTEM_LOG) + " requires at most 2 arguments", polyad.getArgAt(2));
        }

        int currentIntLevel = LOG_LEVEL_NONE;
        if (isDebug) {
            currentIntLevel = state.getDebugUtil().getDebugLevel();
        } else {
            if (state.getLogger() != null) {
                currentIntLevel = getMyLogLevel(state.getLogger().getLogger().getLevel());
            }
        }
        Long currentLongLevel = (long) currentIntLevel;
        // Contract is that if there is no logger, no logger operations should work
        if ((!isDebug) && state.getLogger() == null) {
            if (polyad.getArgCount() == 0) {
                QDLStem stem = new QDLStem();
                stem.put(DEBUGGER_PROPERTY_NAME_LEVEL, (long) LOG_LEVEL_NONE);
                polyad.setResult(stem);
                polyad.setResultType(Constant.STEM_TYPE);
                polyad.setEvaluated(true);
                return;
            }

            if (polyad.getArgCount() == 1) {
                polyad.setResult(LOG_LEVEL_NONE);
                polyad.setResultType(Constant.LONG_TYPE);
                polyad.setEvaluated(true);
                return;
            }
            polyad.setResult(Boolean.FALSE);
            polyad.setResultType(Constant.BOOLEAN_TYPE);
            polyad.setEvaluated(true);
            return;


        }
        if (polyad.getArgCount() == 0) {
            QDLStem stem = new QDLStem();
            if (isDebug) {
                stem.fromJSON(state.getDebugUtil().toJSON());
/*
                stem.put(DEBUGGER_PROPERTY_NAME_TITLE, state.getDebugUtil().getTitle());
                stem.put(DEBUGGER_PROPERTY_NAME_TS_ON, state.getDebugUtil().isPrintTS());
                long lll = (long) state.getDebugUtil().getDebugLevel();
                stem.put(DEBUGGER_PROPERTY_NAME_LEVEL, lll);
                stem.put(DEBUGGER_PROPERTY_NAME_DELIMITER, state.getDebugUtil().getDelimiter());
                if(state.getDebugUtil().hasHost()) {
                    stem.put(DEBUGGER_PROPERTY_NAME_HOST, state.getDebugUtil().getHost());
                }
*/
            } else {
                Level lll = state.getLogger().getLogLevel();
                long llll = (long) getMyLogLevel(lll);
                stem.put(DEBUGGER_PROPERTY_NAME_LEVEL, llll); // I hate coming up with names...
                stem.put(DEBUGGER_PROPERTY_NAME_TITLE, state.getLogger().getClassName());
            }
            polyad.setResult(stem);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setEvaluated(true);
            return;
        }

        int newLogLevel = currentIntLevel;

        Object arg0 = polyad.evalArg(0, state);
        checkNull(arg0, polyad.getArgAt(0), state);
        String message = null;

        if (polyad.getArgCount() == 1) {
            if (isStem(arg0)) {
                QDLStem oldCfg = new QDLStem();

                QDLStem cfg = (QDLStem) arg0;
                if (cfg.containsKey(DEBUGGER_PROPERTY_NAME_TITLE)) {
                    if (isDebug) {
                        oldCfg.put(DEBUGGER_PROPERTY_NAME_TITLE, state.getDebugUtil().getTitle());
                        state.getDebugUtil().setTitle(cfg.getString(DEBUGGER_PROPERTY_NAME_TITLE));
                    } // can't change the title of the logger
                }
                if (cfg.containsKey(DEBUGGER_PROPERTY_NAME_HOST)) { // options are name, address or off.
                    String host = null;
                    try {
                        switch (cfg.getString(DEBUGGER_PROPERTY_NAME_HOST).toLowerCase()) {
                            default:
                            case "name":
                                host = InetAddress.getLocalHost().getHostName();
                                break;
                            case "address":
                                host = InetAddress.getLocalHost().getHostAddress();
                                break;
                            case "off":
                                host = null;
                                break;
                        }
                    } catch (UnknownHostException e) {
                        if (DebugUtil.isEnabled()) {
                            e.printStackTrace();
                        }
                    }
                    if (isDebug && host != null) {
                        if (state.getDebugUtil().hasHost()) {
                            oldCfg.put(DEBUGGER_PROPERTY_NAME_HOST, state.getDebugUtil().getHost());
                        } else {
                            oldCfg.put(DEBUGGER_PROPERTY_NAME_HOST, "");
                        }
                        state.getDebugUtil().setHost(host);
                    }
                }
                if (cfg.containsKey(DEBUGGER_PROPERTY_NAME_DELIMITER)) {
                    if (isDebug) {
                        oldCfg.put(DEBUGGER_PROPERTY_NAME_DELIMITER, state.getDebugUtil().getDelimiter());
                        state.getDebugUtil().setDelimiter(cfg.getString(DEBUGGER_PROPERTY_NAME_DELIMITER));
                    }
                }
                if (cfg.containsKey(DEBUGGER_PROPERTY_NAME_TS_ON)) {
                    if (isDebug) {
                        oldCfg.put(DEBUGGER_PROPERTY_NAME_TS_ON, state.getDebugUtil().isPrintTS());
                        state.getDebugUtil().setPrintTS(cfg.getBoolean(DEBUGGER_PROPERTY_NAME_TS_ON));
                    }
                }
                if (cfg.containsKey(DEBUGGER_PROPERTY_NAME_LEVEL)) {
                    Object newLevel = cfg.get(DEBUGGER_PROPERTY_NAME_LEVEL);
                    if (newLevel instanceof Long) {
                        if (isDebug) {
                            oldCfg.put(DEBUGGER_PROPERTY_NAME_LEVEL, (long) state.getDebugUtil().getDebugLevel());
                            state.getDebugUtil().setDebugLevel(((Long) newLevel).intValue());
                        } else {
                            Level ll = state.getLogger().getLogLevel();
                            oldCfg.put(DEBUGGER_PROPERTY_NAME_LEVEL, (long) getMyLogLevel(ll));
                            state.getLogger().setLogLevel(getLogLevel(((Long) newLevel).intValue()));
                        }
                    } else {

                        if (newLevel instanceof String) {
                            if (isDebug) {
                                oldCfg.put(DEBUGGER_PROPERTY_NAME_LEVEL, MetaDebugUtil.toLabel(state.getDebugUtil().getDebugLevel()));
                                state.getDebugUtil().setDebugLevel((String) newLevel);
                            } else {
                                Level ll = state.getLogger().getLogLevel();
                                oldCfg.put(DEBUGGER_PROPERTY_NAME_LEVEL, MetaDebugUtil.toLabel(getMyLogLevel(ll)));
                                Level lll = getLogLevel(MetaDebugUtil.toLevel((String) newLevel));
                                state.getLogger().setLogLevel(lll);
                            }
                        } else {
                            throw new BadArgException("Illegal argument type for " + DEBUG + " level", polyad.getArgAt(0));
                        }

                    }
                }
                polyad.setResult(oldCfg);
                polyad.setResultType(Constant.STEM_TYPE);
                polyad.setEvaluated(true);
                return;
            }
            if (isLong(arg0)) {
                // Cannot reset logging levels in server mode or server loses control of logging
                if (!state.isRestrictedIO()) {
                    // Then they are setting the logging level
                    if (!isValidLoggingLevel((Long) arg0)) {
                        throw new BadArgException("unknown logging level of " + arg0 + " encountered.", polyad.getArgAt(0));
                    }
                    newLogLevel = ((Long) arg0).intValue();

                    if (isDebug) {
                        state.getDebugUtil().setDebugLevel(newLogLevel);
                    } else {
                        state.getLogger().getLogger().setLevel(getLogLevel(newLogLevel)); // look up Java log level
                    }
                }
                polyad.setResult(currentLongLevel);
                polyad.setResultType(Constant.LONG_TYPE);
                polyad.setEvaluated(true);
                return;
            }
            // Use whatever the current level is at as the default for the message
            if (isString(arg0)) {

                int newLevel = MetaDebugUtil.toLevel((String) arg0);
                if (newLevel == DEBUG_LEVEL_UNKNOWN) {
                    // Idiom -- log or debug at info level if not a level
                    if (isDebug) {
                        state.getDebugUtil().info((String) arg0);
                    } else {
                        state.getLogger().info((String) arg0);
                    }
                    polyad.setResult(Boolean.TRUE);
                    polyad.setResultType(Constant.BOOLEAN_TYPE);
                    polyad.setEvaluated(true);
                    return;

                }
                if (isDebug) {
                    state.getDebugUtil().setDebugLevel((String) arg0);
                } else {
                    state.getLogger().setLogLevel(getLogLevel(newLevel));
                }
                polyad.setResult(currentIntLevel);
                polyad.setResultType(Constant.LONG_TYPE);
                polyad.setEvaluated(true);
                return;
            }
            throw new BadArgException("unknown logging type of " + arg0 + " encountered.", polyad.getArgAt(0));
        }


        if (polyad.getArgCount() == 2) {
            // then the arguments are 0 is the level, 1 is the message
            Object arg1 = polyad.evalArg(1, state);
            checkNull(arg1, polyad.getArgAt(1), state);
            if (isLong(arg0)) {
                newLogLevel = ((Long) arg0).intValue();
                if (!isValidLoggingLevel((Long) arg0)) {
                    throw new BadArgException("unknown logging level of " + arg0 + " encountered.", polyad.getArgAt(0));
                }

            } else {
                if (isString(arg0)) {
                    newLogLevel = MetaDebugUtil.toLevel((String) arg0);
                    if (newLogLevel == DEBUG_LEVEL_UNKNOWN) {
                        throw new BadArgException("unknown logging level of " + arg0 + " encountered.", polyad.getArgAt(0));
                    }
                } else {
                    throw new BadArgException("unknown logging level of " + arg0 + " encountered.", polyad.getArgAt(0));
                }
            }
            message = arg1.toString();
        }

        Boolean ok = Boolean.TRUE;
        switch (newLogLevel) {
            case LOG_LEVEL_NONE:
                // do nothing.
                break;
            case LOG_LEVEL_TRACE:
                if (isDebug) {
                    state.getDebugUtil().trace(message);
                } else {
                    state.getLogger().debug(message);
                }
                break;
            case LOG_LEVEL_INFO:
                if (isDebug) {
                    state.getDebugUtil().info(message);
                } else {
                    state.getLogger().info(message);
                }
                break;
            case LOG_LEVEL_WARN:
                if (isDebug) {
                    state.getDebugUtil().warn(message);
                } else {
                    state.getLogger().warn(message);
                }
                break;
            case LOG_LEVEL_ERROR:
                if (isDebug) {
                    state.getDebugUtil().error(message);
                } else {
                    state.getLogger().error(message);
                }
                break;
            case LOG_LEVEL_SEVERE:
                if (isDebug) {
                    state.getDebugUtil().severe(message);
                } else {
                    state.getLogger().warn(message); // no other options
                }
                break;

            default:
                ok = Boolean.FALSE;
        }
        polyad.setResult(ok);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
        polyad.setEvaluated(true);
    }

    protected void doModulePaths(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(MODULE_PATH + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.STEM_TYPE);
            QDLStem stemVariable = new QDLStem();
            if (state.getModulePaths().isEmpty()) {
                polyad.setResult(stemVariable);
                return;
            }
            ArrayList<Object> sp = new ArrayList<>();
            sp.addAll(state.getModulePaths());
            stemVariable.addList(sp);
            polyad.setResult(stemVariable);
            return;
        }
        if (polyad.getArgCount() == 1) {
            Object obj = polyad.evalArg(0, state);
            checkNull(obj, polyad.getArgAt(0), state);
            if (isString(obj)) {
                state.setModulePaths(obj.toString());
                polyad.setEvaluated(true);
                polyad.setResult(Boolean.TRUE);
                polyad.setResultType(Constant.BOOLEAN_TYPE);
                return;
            }
            if (!isStem(obj)) {
                throw new BadArgException(MODULE_PATH + " requires a stem as its argument.", polyad.getArgAt(0));
            }
            QDLStem stemVariable = (QDLStem) obj;
            // now we have to turn it in to list, tending to the semantics.
            QDLList qdlList = stemVariable.getQDLList();
            List<String> sp = new ArrayList<>();
            for (int i = 0; i < qdlList.size(); i++) {
                Object entry = qdlList.get(i);
                if (entry != null && !(entry instanceof QDLNull)) {
                    String newPath = entry.toString();
                    newPath = newPath + (newPath.endsWith(VFSPaths.PATH_SEPARATOR) ? "" : VFSPaths.PATH_SEPARATOR);
                    sp.add(newPath);
                }
            }
            state.setModulePaths(sp);
            polyad.setEvaluated(true);
            polyad.setResult(Boolean.TRUE);
            polyad.setResultType(Constant.BOOLEAN_TYPE);
            return;
        }
        throw new BadArgException(SCRIPT_PATH_COMMAND + " requires at most one argument, not " + polyad.getArgCount(), polyad.getArgAt(1));

    }


    /**
     * This accepts either a stem of paths or a single string that is parsed.
     *
     * @param polyad
     * @param state
     */
    protected void doScriptPaths(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(SCRIPT_PATH_COMMAND + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.STEM_TYPE);
            QDLStem stemVariable = new QDLStem();
            if (state.getScriptPaths().isEmpty()) {
                polyad.setResult(stemVariable);
                return;
            }
            ArrayList<Object> sp = new ArrayList<>();
            sp.addAll(state.getScriptPaths());
            stemVariable.addList(sp);
            polyad.setResult(stemVariable);
            return;
        }
        if (polyad.getArgCount() == 1) {
            Object obj = polyad.evalArg(0, state);
            checkNull(obj, polyad.getArgAt(0), state);
            if (isString(obj)) {
                state.setScriptPaths(obj.toString());
                polyad.setEvaluated(true);
                polyad.setResult(Boolean.TRUE);
                polyad.setResultType(Constant.BOOLEAN_TYPE);
                return;
            }
            if (!isStem(obj)) {
                throw new BadArgException(SCRIPT_PATH_COMMAND + " requires a stem as its argument.", polyad.getArgAt(0));
            }
            QDLStem stemVariable = (QDLStem) obj;
            // now we have to turn it in to list, tending to the semantics.
            QDLList qdlList = stemVariable.getQDLList();
            List<String> sp = new ArrayList<>();
            for (int i = 0; i < qdlList.size(); i++) {
                Object entry = qdlList.get(i);
                if (entry != null && !(entry instanceof QDLNull)) {
                    String newPath = entry.toString();
                    newPath = newPath + (newPath.endsWith(VFSPaths.PATH_SEPARATOR) ? "" : VFSPaths.PATH_SEPARATOR);
                    sp.add(newPath);
                }
            }
            state.setScriptPaths(sp);
            polyad.setEvaluated(true);
            polyad.setResult(Boolean.TRUE);
            polyad.setResultType(Constant.BOOLEAN_TYPE);
            return;
        }
    }

    /**
     * This will return a given script arg for a given index. No arg returns then number of arguments;
     *
     * @param polyad
     * @param state
     */
    protected void doScriptArgs(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(SCRIPT_ARGS_COMMAND + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.LONG_TYPE);
            polyad.setResult(state.hasScriptArgs() ? (long) state.getScriptArgs().length : 0L);
            return;
        }
        Object obj = polyad.evalArg(0, state);
/*        if (!state.hasScriptArgs()) {
            throw new BadArgException("index out of bounds for " + SCRIPT_ARGS_COMMAND + "-- no arguments found.", polyad.getArgAt(0));
        }*/
        checkNull(obj, polyad.getArgAt(0), state);
        if (!isLong(obj)) {
            throw new BadArgException(SCRIPT_ARGS_COMMAND + " requires an integer argument.", polyad.getArgAt(0));
        }
        int index = ((Long) obj).intValue();
        if (index == -1L) {
            QDLStem args = state.getScriptArgStem();
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setResult(args);
            return;
        }
        if (index < 0) {
            throw new BadArgException(SCRIPT_ARGS_COMMAND + " requires a non-negative integer argument.", polyad.getArgAt(0));
        }
        if (state.getScriptArgStem().size() <= index) {
            throw new BadArgException("index " + index + " out of bounds for " + SCRIPT_ARGS_COMMAND + " with " + state.getScriptArgStem().size() + " arguments.", polyad.getArgAt(0));
        }

        polyad.setEvaluated(true);
        polyad.setResultType(Constant.STRING_TYPE);
        polyad.setResult(state.getScriptArgStem().get((long) index));
        return;
    }

    /**
     * New function for script arguments. Better contract.
     * <p>
     * Fixes <a href="https://github.com/ncsa/qdl/issues/3">github issue 3.</a>
     * </p>
     *
     * @param polyad
     * @param state
     */
    protected void doScriptArgs2(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        boolean hasArg = polyad.getArgCount() == 1;
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(SCRIPT_ARGS2_COMMAND + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Long index = 0L;
        if (hasArg) {
            Object obj = polyad.evalArg(0, state);
            checkNull(obj, polyad.getArgAt(0), state);
            if (!isLong(obj)) {
                throw new BadArgException(SCRIPT_ARGS2_COMMAND + " requires an integer argument.", polyad.getArgAt(0));
            }
            index = (Long) obj;
        }
        if (!state.hasScriptArgs()) {
            polyad.setResult(new QDLStem());
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.getType(polyad.getResult()));
            return;
        }
        if (hasArg) {
            Object result = state.getScriptArgStem().get(index);
            polyad.setResultType(Constant.getType(result));
            polyad.setResult(result);
            polyad.setEvaluated(true);
        } else {
            polyad.setEvaluated(true);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setResult(state.getScriptArgStem());
        }
    }


    // Contract is
    // no arg -- return all as stem
    // 1 or more, return environment variable for each. Single one returns the value, otherwise a stem
    // Empty result at all times in server mode.
    protected void doOSEnv(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(getBigArgList0());
            polyad.setEvaluated(true);
            return;
        }
        if (state.isServerMode()) {
            throw new QDLServerModeException(OS_ENV + " not supported in server mode");
        }


        QDLStem env = new QDLStem();
        MetaCodec codec = new MetaCodec();

        int argCount = polyad.getArgCount();
        if (argCount == 0) {
            // system variables.
            Map<String, String> map = System.getenv();
            for (String key : map.keySet()) {
                env.put(codec.encode(key), System.getenv(key));
            }
            polyad.setResult(env);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        for (int i = 0; i < argCount; i++) {
            Object obj = polyad.evalArg(i, state);
            checkNull(obj, polyad.getArgAt(i));
            if (!isString(obj)) {
                throw new BadArgException("argument with index " + i + " was not a string.", polyad.getArgAt(i));
            }

            String arg = (String) obj;
            String value = System.getenv(arg);

            if (argCount == 1) {
                polyad.setEvaluated(true);
                polyad.setResultType(Constant.STRING_TYPE);
                polyad.setResult(value == null ? "" : value); // Don't return java null objects.
                return;
            }
            if (value != null) {
                env.put(codec.encode(arg), value);
            }
        }

        polyad.setEvaluated(true);
        polyad.setResult(env);
        polyad.setResultType(Constant.STEM_TYPE);
    }

    protected void doSysInfo(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(SYS_INFO + " requires at most 1 argument", polyad.getArgAt(1));
        }
        getConst(polyad, state, state.getSystemInfo());
    }

    protected void doConstants(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }
        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(CONSTANTS + " requires at most 1 argument", polyad.getArgAt(1));
        }
        getConst(polyad, state, state.getSystemConstants());
    }

    protected void getConst(Polyad polyad, State state, QDLStem values) {
        if (polyad.getArgCount() == 0) {
            polyad.setEvaluated(true);
            polyad.setResult(values);
            polyad.setResultType(Constant.STEM_TYPE);
            return;
        }
        Object obj = polyad.evalArg(0, state);
        checkNull(obj, polyad.getArgAt(0), state);
        if (!isString(obj)) {
            throw new BadArgException("This requires a string as its argument.", polyad.getArgAt(0));
        }
        // A little trickery here. The multi-index is assumed to be of the form
        // stem.index0.index1....  so it will parse the first component as the name of the
        // variable. Since the variable here is un-named, we put in a dummy value of sys. for it which is ignored.
        StemMultiIndex multiIndex = new StemMultiIndex("sys." + obj.toString());
        Object rc = null;
        try {
            rc = values.get(multiIndex);

        } catch (IndexError ie) {
            // The user requested a non-existent property. Don't blow up, just return an empty string.
        }
        polyad.setEvaluated(true);

        if (rc == null) {
            polyad.setResult("");
            polyad.setResultType(Constant.STRING_TYPE);
        } else {
            polyad.setResult(rc);
            polyad.setResultType(Constant.getType(rc));
        }
    }

    /**
     * Run an external qdl script with its own state. Ouput goes to the current console.
     *
     * @param polyad
     * @param state
     */
    protected void runScript(Polyad polyad, State state) {
        runnit(polyad, state, true);
    }

    protected void loadScript(Polyad polyad, State state) {
        runnit(polyad, state, false);

    }

    /**
     * This will use the path information and try to resolve the script based on that.
     * Contract is
     * <ol>
     *     <li>name start with a # ==&gt; this is a file. If server mode, fail. if abs, do it, if rel resolve against file path</li>
     *     <li>name is scheme qualified: if absolute, just get it, if relative, resolve against same scheme</li>
     *     <li>no qualification: if absolute path and server mode, fail, if relative, try against each path  </li>
     * </ol>
     * <b>NOTE:</b> First resolution wins. <br/>
     * <b>NOTE:</b> If this returns a null, then there is no such script anywhere.
     *
     * @param name
     * @param state
     * @return
     */
    public static QDLScript resolveScript(String name, List<String> paths, State state) throws Throwable {
        // case 1. This starts with a # so they want to force getting a regular file
        QDLScript qdlScript;
        if (name.startsWith(SCHEME_DELIMITER)) {
            if (state.isServerMode()) {
                throw new IllegalArgumentException("File access forbidden in server mode.");
            }
            name = name.substring(1);
            qdlScript =  new QDLScript(QDLFileUtil.readTextFileAsLines(state, name), null);
            qdlScript.setPath(name);
            return qdlScript;

        }
        // case 2: Scheme qualified.
        if (name.contains(SCHEME_DELIMITER)) {
            String tempName = name.substring(1 + name.indexOf(SCHEME_DELIMITER));
            File testFile = new File(tempName);
            if (QDLFileUtil.isAbsolute(tempName)) {
                qdlScript = new QDLScript(QDLFileUtil.readTextFileAsLines(state, name), null);
                qdlScript.setPath(name);
                return qdlScript;
            }
            // So it is of the form volume#relative/path
            String caput = name.substring(0, name.indexOf(SCHEME_DELIMITER));
            for (String p : paths) {
                if (p.startsWith(caput)) {
                    //         DebugUtil.trace(SystemEvaluator.class, " trying path = " + p + tempName);
                    try {
                        List<String> lines = QDLFileUtil.readTextFileAsLines(state, p + tempName);
                        qdlScript = new QDLScript(lines, null);
                        qdlScript.setPath(p + tempName);
                        return qdlScript;
                    } catch (FileNotFoundException | QDLFileNotFoundException fnf) {
                        // rock on
                    }
                }
            }

        } else {
            // case 3: No qualifications, just a string. Try everything.
            File testFile;
            if (name.startsWith("./")) {
                // special case starting from current directory for scripts
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();
                testFile = new File(s + name.substring(1));
            } else {
                testFile = new File(name);
            }
            if (testFile.isAbsolute()) {
                if (state.isServerMode()) {
                    throw new QDLServerModeException("File access forbidden in server mode.");
                } else {
                    qdlScript = new QDLScript(QDLFileUtil.readFileAsLines(name), null);
                    qdlScript.setPath(name);
                    return qdlScript;
                }
            }
            for (String p : paths) {
                String resourceName = p + name;
                //    DebugUtil.trace(SystemEvaluator.class, " path = " + resourceName);
                if (QDLFileUtil.isVFSPath(resourceName)) {
                    try {
                        qdlScript = new QDLScript(QDLFileUtil.readTextFileAsLines(state, resourceName), null);
                        qdlScript.setPath(resourceName);
                        return qdlScript;
                    } catch (FileNotFoundException | QDLFileNotFoundException fnf) {
                        // rock on
                    }
                } else {
                    testFile = new File(resourceName);
                    if (testFile.exists() && testFile.isFile() && testFile.canRead()) {
                        qdlScript = new QDLScript(QDLFileUtil.readFileAsLines(testFile.getCanonicalPath()), null);
                        qdlScript.setPath(resourceName);
                        return qdlScript;
                    }
                }
            }
        }
        return null;
    }

    public static int runnit(Polyad polyad, State state, boolean hasNewState) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(AbstractEvaluator.getBigArgList());
            polyad.setEvaluated(true);
            return 0;
        }
        return runnit(polyad, state, state.getScriptPaths(), hasNewState, false);

    }

    public static int runnit(Polyad polyad, State state, List<String> paths, boolean hasNewState, boolean newThread) {

        if (polyad.getArgCount() == 0) {
            throw new MissingArgException((hasNewState ? RUN_COMMAND : LOAD_COMMAND) + " requires at least 1 argument", polyad);
        }
        // Contract is that script_load runs in the current state -- it does not make its own. This
        // allows scripts to inject state and have it remain.
        State localState = state;
        if (hasNewState) {
            localState = state.newCleanState();
        }

        Object arg1 = polyad.evalArg(0, state);
        checkNull(arg1, polyad.getArgAt(0), state);
        Object[] argList = new Object[0];
        // https://github.com/ncsa/qdl/issues/20
        state.setTargetState(localState);
        if (2 <= polyad.getArgCount()) {
            ArrayList<Object> aa = new ArrayList<>();
            // zero-th argument is the name of the script, so start with element 1.
            for (int i = 1; i < polyad.getArgCount(); i++) {
                Object arg;
                arg = polyad.evalArg(i, state);
                checkNull(arg, polyad.getArgAt(i), state);
                aa.add(arg);
            }
            argList = aa.toArray(new Object[0]);
        }
        state.setTargetState(null);
        String resourceName = arg1.toString();
        QDLScript script;
        // stash stack trace elements, but don't do anything unless there is an error, then format result
        AbstractState.QDLStackTraceElement stackTraceElement = new AbstractState.QDLStackTraceElement(resourceName, polyad.getArgAt(0).getTokenPosition());
        state.getScriptStack().add(stackTraceElement);
        try {
            script = resolveScript(resourceName, paths, state);
        } catch (Throwable t) {
            state.warn("Could not find script '" + resourceName + "': " + t.getMessage());
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new QDLRuntimeException("Could not find  '" + resourceName + "'. Is your script path set?", t);
        }
        if (script == null) {
            throw new QDLRuntimeException("Could not find '" + resourceName + "'. Is your script path set?");
        } else {
            try {
                //Object[] oldArgs = localState.getScriptArgs();
                QDLStem oldArgs = localState.getScriptArgStem();
                String oldScriptName = localState.getScriptName();
                QDLStem newArgs = new QDLStem();
                localState.setScriptName(resourceName);
                List list = Arrays.asList(argList);
                newArgs.addList(list);
                localState.setScriptArgStem(newArgs);
                if (newThread) {
                    int pid = (int) (System.currentTimeMillis() % 999983); // largest 6 digit prime number.
                    if (state.getThreadTable().containsKey(pid)) {
                        for (int i = 0; i < 10; i++) {
                            pid = (int) (System.currentTimeMillis() % 999983); // largest 6 digit prime number.
                            if (!state.getThreadTable().containsKey(pid)) {
                                break;
                            }
                        }
                    }
                    if (state.getThreadTable().containsKey(pid)) {
                        throw new QDLExceptionWithTrace("could not set pid for " + FORK, polyad);
                    }
                    QDLThread qdlThread = new QDLThread(localState, script, pid);
                    QDLThreadRecord record = new QDLThreadRecord();
                    record.qdlThread = qdlThread;
                    record.name = resourceName;
                    state.getThreadTable().put(pid, record);
                    qdlThread.start();
                    return pid;
                } else {
                    script.execute(localState);
                    localState.setScriptArgStem(oldArgs);
                    localState.setScriptName(oldScriptName);
                }
            } catch (QDLException qe) {
                if (qe instanceof ParsingException) {
                    ParsingException parsingException = (ParsingException) qe;
                    parsingException.setScriptName(resourceName);   // make sure this gets propagated back
                }
                if (qe instanceof QDLExceptionWithTrace) {
                    QDLExceptionWithTrace qq = (QDLExceptionWithTrace) qe;
                    qq.setScriptName(resourceName);
                    qq.getScriptStack().addAll(state.getScriptStack()); // clone the stack so the trace has it
                    qq.setScript(true);
                    state.getScriptStack().clear(); // or all errors just accumulate
                    throw qq;
                }
                if (qe instanceof ReturnException) {
                    ReturnException rx = (ReturnException) qe;
                    polyad.setResult(rx.result);
                    polyad.setResultType(rx.resultType);
                    polyad.setEvaluated(true);
                    return 0;
                }
                throw qe;
            } catch (Throwable t) {
                QDLExceptionWithTrace qq = new QDLExceptionWithTrace("in resource '" + resourceName + "':" + t.getMessage(), t, polyad);
                qq.setScript(true);
                qq.setScriptName(resourceName);
                throw qq;
            }
        }
        state.getScriptStack().remove(state.getScriptStack().size() - 1);
        polyad.setEvaluated(true);
        polyad.setResultType(Constant.NULL_TYPE);
        polyad.setResult(QDLNull.getInstance());
        return 0;
    }

    protected void doRaiseError(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 3});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(RAISE_ERROR + " requires at least 1 argument", polyad);
        }

        if (3 < polyad.getArgCount()) {
            throw new ExtraArgException(RAISE_ERROR + " requires at most 2 arguments", polyad.getArgAt(3));
        }
        Object arg1 = polyad.evalArg(0, state);
        if (arg1 == null) {
            arg1 = "(no message)";
        }
        Object arg2 = null;
        String message = arg1.toString();
        state.setValue(TryCatch.ERROR_MESSAGE_NAME, message);
        QDLStem stateStem = new QDLStem(); // default is empty stem
        Long stateCode = TryCatch.RESERVED_USER_ERROR_CODE; // default
        switch (polyad.getArgCount()) {
            case 3:
                arg2 = polyad.evalArg(2, state);
                checkNull(arg2, polyad.getArgAt(2), state);
                if (!isStem(arg2)) {
                    throw new BadArgException(RAISE_ERROR + ": the final argument must be a stem", polyad);
                }
                stateStem = (QDLStem) arg2;
            case 2:
                arg2 = polyad.evalArg(1, state);
                checkNull(arg2, polyad.getArgAt(1), state);
                if (!isLong(arg2)) {
                    throw new BadArgException(RAISE_ERROR + ": the second argument must be an integer", polyad);
                }
                stateCode = (Long) arg2;
                break;
            case 1:
                //state.getVStack().put(new VThing(new XKey(TryCatch.ERROR_CODE_NAME), stateCode));
        }
        state.getVStack().put(new VThing(new XKey(TryCatch.ERROR_STATE_NAME), stateStem == null ? new QDLStem() : stateStem));
        state.getVStack().put(new VThing(new XKey(TryCatch.ERROR_CODE_NAME), stateCode));

        polyad.setResult(Boolean.TRUE);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
        polyad.setEvaluated(true);
        // set the message in the exception to the message from the error, so if it happens in the course of execution
        // they get a message
        if (stateCode == TryCatch.RESERVED_ASSERTION_CODE) {
            throw new AssertionException(message, stateStem);
        }
        throw new RaiseErrorException(polyad, message, stateCode, stateStem);
    }

    protected void doReturn(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(RETURN + " requires at most 1 argument", polyad.getArgAt(1));
        }

        switch (polyad.getArgCount()) {
            case 0:
                polyad.setEvaluated(true);
                polyad.setResult(QDLNull.getInstance());
                polyad.setResultType(Constant.NULL_TYPE);
                break;
            case 1:
                Object r = polyad.evalArg(0, state);
                checkNull(r, polyad.getArgAt(0), state);
                polyad.setResult(r);
                polyad.setResultType(polyad.getArguments().get(0).getResultType());
                polyad.setEvaluated(true);
                break;
        }

        // Magic. Sort of. Since it is impossible to tell when or how a function will invoke its
        // return, we throw an exception and catch it at the right time.
        ReturnException rx = new ReturnException();
        rx.result = polyad.getResult();
        rx.resultType = polyad.getResultType();
        throw rx;
    }

    static final int LOAD_FILE = 0;
    static final int LOAD_JAVA = 1;

    public final static String MODULE_TYPE_JAVA = "java";
    public final static String MODULE_DEFAULT_EXTENSION = ".mdl";

    protected void doLoadModule(Polyad polyad, State state) {
        doNewLoadModule(polyad, state);
    }

    protected void doNewLoadModule(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        // CIL-1313 NEVER check for server mode here. That is handled later since the modules are
        // simply loaded as scripts. Checking here can make it impossible to load any modules.
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(MODULE_LOAD + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(MODULE_LOAD + " requires at most 2 arguments", polyad.getArgAt(2));
        }
        Object arg = polyad.evalArg(0, state);

        if (arg == QDLNull.getInstance()) {
            polyad.setResult(QDLNull.getInstance());
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }

        QDLStem argStem = convertArgsToStem(polyad, arg, state, MODULE_LOAD);
        QDLStem outStem = new QDLStem();
        for (Object key : argStem.keySet()) {
            Object value = argStem.get(key);
            int loadTarget = LOAD_FILE;
            String resourceName = null;
            if (isString(value)) {
                resourceName = (String) value;
            } else {
                QDLStem q = (QDLStem) value;
                resourceName = q.get(0L).toString();
                loadTarget = q.get(1L).toString().equals(MODULE_TYPE_JAVA) ? LOAD_JAVA : LOAD_FILE;

            }
            List<String> loadedQNames = null;
            if (loadTarget == LOAD_JAVA) {
                loadedQNames = doJavaModuleLoad(state, resourceName);
            } else {
                loadedQNames = doQDLModuleLoad(state, resourceName);
            }
            Object newEntry = null;
            if (loadedQNames == null || loadedQNames.isEmpty()) {
                newEntry = QDLNull.getInstance();
            } else {
                if (loadedQNames.size() == 1) {
                    newEntry = loadedQNames.get(0);
                } else {
                    QDLStem innerStem = new QDLStem();
                    innerStem.addList(loadedQNames);
                    newEntry = innerStem;
                }
            }
            outStem.putLongOrString(key, newEntry);
        }
        polyad.setEvaluated(true);
        if (outStem.size() == 1) {
            polyad.setResult(outStem.get(outStem.keySet().iterator().next()));
            polyad.setResultType(Constant.STRING_TYPE);
        } else {
            polyad.setResult(outStem);
            polyad.setResultType(Constant.STEM_TYPE);
        }
    }

    /**
     * Load a single java module, returning a null if it failed or the FQ name if it worked.
     *
     * @param state
     * @param resourceName
     */
    private List<String> doJavaModuleLoad(State state, String resourceName) {
        try {
            Class klasse = state.getClass().forName(resourceName);
            Object newThingy = klasse.newInstance();
            QDLLoader qdlLoader;
            if (newThingy instanceof JavaModule) {
                // For a single instance, just create a barebones loader on the fly.
                qdlLoader = new QDLLoader() {
                    @Override
                    public List<Module> load() {
                        List<Module> m = new ArrayList<>();
                        JavaModule javaModule = (JavaModule) newThingy;
                        State newState = state.newCleanState();
                        javaModule = (JavaModule) javaModule.newInstance(newState);
                        javaModule.init(newState); // set it up
                        m.add(javaModule);
                        return m;
                    }
                };
            } else {
                if (!(newThingy instanceof QDLLoader)) {
                    throw new IllegalArgumentException("'" + resourceName + "' is neither a module nor a loader.");
                }
                qdlLoader = (QDLLoader) newThingy;
            }
            JavaModuleConfig jmc = new JavaModuleConfig();
            jmc.setImportOnStart(false);
            jmc.setVersion(MODULE_ATTR_VERSION_1_0);

            List<String> names = QDLConfigurationLoaderUtils.setupJavaModule(state, qdlLoader, jmc);
            if (names.isEmpty()) {
                return null;
            }
            return names;
        } catch (RuntimeException rx) {
            throw rx;
        } catch (ClassNotFoundException cnf) {
            throw new QDLException("could not find Java class '" + resourceName + "' in the current classpath.");
        } catch (Throwable t) {
            throw new QDLException("could not load Java class " + resourceName + ": '" + t.getMessage() + "'.", t);
        }
    }

    /**
     * Load the module(s) in a single resource.
     *
     * @param state
     * @param resourceName
     * @return
     */
    private List<String> doQDLModuleLoad(State state, String resourceName) {
        QDLScript script = null;
        try {
            script = resolveScript(resourceName, state.getModulePaths(), state);
            if (script == null) {
                script = resolveScript(resourceName + MODULE_DEFAULT_EXTENSION, state.getModulePaths(), state);
            }
        } catch (Throwable t) {
            state.warn("Could not find module:" + t.getMessage());
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new QDLRuntimeException("Could not find  '" + resourceName + "'. Is your module path set?", t);
        }

        File file = null;

        if (script == null) {
            file = new File(resourceName);
            if (!file.exists()) {
                throw new IllegalArgumentException("file, '" + file.getAbsolutePath() + "' does not exist");
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("'" + file.getAbsolutePath() + "' is not a file");
            }

            if (!file.canRead()) {
                throw new IllegalArgumentException("You do not have permission to read '" + file.getAbsolutePath() + "'.");
            }

        }
        try {
            QDLParserDriver parserDriver = new QDLParserDriver(new XProperties(), state);
            // Exceptional case where we just run it directly.
            // note that since this is QDL there may be multiple modules, etc.
            // in a single file, so there is no way to know what the user did except
            // to look at the state before, then after. This should return the added
            // modules fq paths.
            state.getMTemplates().clearChangeList();
            if (script == null) {
                if (state.isServerMode()) {
                    throw new QDLServerModeException("File operations are not permitted in server mode");
                }
                FileReader fileReader = new FileReader(file);
                QDLRunner runner = new QDLRunner(parserDriver.parse(fileReader));
                runner.setState(state);
                runner.run();
            } else {
                script.execute(state);
            }
            List<String> afterLoad = new ArrayList<>();
            for (Object k : state.getMTemplates().getChangeList()) {
                afterLoad.add(((MTKey) k).getKey());
            }
            state.getMTemplates().clearChangeList();
            return afterLoad;
        } catch (Throwable t) {
            if (DebugUtil.isEnabled()) {
                t.printStackTrace();
            }
        }
        return null;
    }


    /**
     * The general contract for import. You may import everything in a module by using the uri.
     * You may import items using the FQ name (uri + # + name). <br/><br/>
     * Imported items are added to the current stack only. it is not possible to un-import items since
     * it is impossible to track down references to them and it is dangerous to have programs be able
     * to unload things willy-nilly since that can cause hard to track down failures.<br/><br/>
     * If you need to unload, your best bet is to clear the workspace.
     *
     * @param polyad
     */
    protected void doModuleImport(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(MODULE_IMPORT + " requires an argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(MODULE_IMPORT + " requires at most 1 argument", polyad.getArgAt(2));
        }

        Object arg = polyad.evalArg(0, state);
        checkNull(arg, polyad.getArgAt(0), state);
        if (arg == QDLNull.getInstance()) {
            // case that user tries to import q QDL null module. This can happen is
            // the load fails. Don't have it as an error, return QDl null to show nothing
            // happened (so user can check with conditional).
            polyad.setResult(QDLNull.getInstance());
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        QDLStem argStem = convertArgsToStem(polyad, arg, state, MODULE_IMPORT);
        boolean gotOne;
        // The arguments has been regularized so it is a stem of lists of the form
        // [[fq0{,alias0}],[fq1{,alias1}],...,[fqn{,aliasn}]]
        // {,alias} means that may or may not be present. If missing or null, use default.
        // If there is no aliasj, then [fqj] can be replaced with fqj,
        // E.g.
        // [['fq:0','alias0'],'fq:1',['fq:2'],['fq:3','alias3']]
        // should work
        gotOne = false;
        QDLStem outputStem = new QDLStem();
        for (Object key : argStem.keySet()) {
            Object value = argStem.get(key);
            URI moduleNS = null;
            String alias = null;
            if (isString(value)) {
                moduleNS = URI.create((String) value); // if this bombs it throws an illegal argument exception, which is ok.
                gotOne = true;
            }
            if (isStem(value)) {
                QDLStem innerStem = (QDLStem) value;
                Object q = innerStem.get(0L);

                if (!isString(q)) {
                    throw new BadArgException(MODULE_IMPORT + ": the fully qualified name must be a string", polyad);
                }
                moduleNS = URI.create((String) q);

                if (innerStem.containsKey(1L)) {
                    q = innerStem.get(1L);
                    if (!isString(q)) {
                        throw new BadArgException(MODULE_IMPORT + ": the alias for \"" + moduleNS + " must be a string", polyad);
                    }
                    alias = (String) q;
                }
                gotOne = true;
            }
            if (!gotOne) {
                throw new BadArgException(MODULE_IMPORT + ": unknown argument type", polyad);
            }
            Module m = state.getMTemplates().getModule(new MTKey(moduleNS));
            if (m == null) {
                throw new IllegalStateException("no such module '" + moduleNS + "'");
            }
            if (m.getAlias() == null && alias == null) {
                //no alias was set as a default and none was specified.
                throw new QDLExceptionWithTrace("No default alias for " + moduleNS + " and none specified for import.", polyad);
            }
            // QDLModules create the local state, java modules assume the state is exactly the local state.
            // Get a new instance and then set the state to the local state later for Java modules.
            State newModuleState = null;
            newModuleState = state.newSelectiveState(null, true, true, true, true);
            newModuleState.setModuleState(true);

/*
            try {
                newModuleState = StateUtils.clone(state);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
*/
            // put tables and such in the right place so ambient state is not altered.
  /*          if(newModuleState != null) {
                // null possible on system load.
//                newModuleState = newModuleState.newLocalState(newModuleState);
                newModuleState.setModuleState(true);
            }*/
            //State newModuleState = StateUtils.clone(state).newLocalState(state);
            Module newInstance;// = m.newInstance((m instanceof JavaModule) ? null : state);
            //Module newInstance = m.newInstance(null);
            if (m instanceof JavaModule) {
                //State newModuleState = state.newLocalState(state);
                newInstance = m.newInstance(null);
                ((JavaModule) newInstance).init(newModuleState);
            } else {
                newInstance = m.newInstance(newModuleState);
            }
            newInstance.setInheritanceMode(ModuleEvaluator.IMPORT_STATE_SHARE_VALUE); // default for old system
            if (alias == null) {
                alias = m.getAlias();
            }
            //  newModuleState.setSuperState(null); // get rid of it now.
            state.getMInstances().localPut(new MIWrapper(new XKey(alias), newInstance));
            if (isLong(key)) {
                outputStem.put((Long) key, alias);
            } else {
                outputStem.put((String) key, alias);
            }

        }
        switch (outputStem.size()) {
            case 0:
                polyad.setResult(QDLNull.getInstance());
                polyad.setResultType(Constant.NULL_TYPE);
                break;
            case 1:
                polyad.setResult(outputStem.get(0L));
                polyad.setResultType(Constant.STRING_TYPE);
                break;
            default:
                polyad.setResult(outputStem);
                polyad.setResultType(Constant.STEM_TYPE);
        }
        polyad.setEvaluated(true);

    }


    protected void doInterpret(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(INTERPRET + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(INTERPRET + " requires at most 1 argument", polyad.getArgAt(1));
        }

        Object result = polyad.evalArg(0, state);
        checkNull(result, polyad.getArgAt(0), state);
        StringReader stringReader = null;

        if (isString(result)) {
            String string = ((String) result).trim();
            // if its interested, don't let the machinery rumble to life, just return
            if (string.length() == 0) {
                polyad.setResultType(Constant.STRING_TYPE);
                polyad.setResult(string);
                polyad.setEvaluated(true);
                return;
            }
            if (!string.endsWith(";")) {
                string = string + ";";
            }
            stringReader = new StringReader(string);
        }
        if (isStemList(result)) {
            stringReader = new StringReader(StemUtility.stemListToString((QDLStem) result, false));
        }
        if (stringReader == null) {
            throw new BadArgException("No executable argument found.", polyad.getArgAt(0));
        }
        QDLRunner runner;
        QDLInterpreter p = new QDLInterpreter(new XProperties(), state);
        try {
            runner = p.execute(stringReader);
        } catch (Throwable t) {
            // Fix for https://github.com/ncsa/qdl/issues/103
            if (t instanceof ReturnException) {
                ReturnException re = (ReturnException) t;
                // they've set the value directly.
                polyad.setEvaluated(true);
                polyad.setResult(re.result);
                polyad.setResultType(re.resultType);
                return;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new QDLExceptionWithTrace(INTERPRET + " failed:'" + t.getMessage() + "'", t, polyad);
        }
        List<Element> elements = runner.getElements();
        if (elements.size() == 0) {
            // nothing done, for whatever reason, e.g. they sent in a list of comments
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setResult(QDLNull.getInstance());
            polyad.setEvaluated(true);
            return;
        }
        Element lastElement = elements.get(elements.size() - 1);
        if (lastElement.getStatement() instanceof ExpressionInterface) {
            ExpressionInterface swri = (ExpressionInterface) lastElement.getStatement();
            if (swri.getNodeType() == ExpressionInterface.ASSIGNMENT_NODE) {
                polyad.setResult("");
                polyad.setResultType(Constant.STRING_TYPE);
                polyad.setEvaluated(true);
                return;
            }
            polyad.setResult(swri.getResult());
            polyad.setResultType(swri.getResultType());
            polyad.setEvaluated(true);
            return;
        }
        // QDLParserDriver driver = new QDLParserDriver(null, state);
        polyad.setResultType(Constant.NULL_TYPE);
        polyad.setResult(QDLNull.getInstance());
        polyad.setEvaluated(true);

    }


    // Convert a wide variety of inputs to boolean. This is useful in scripts where the arguments might
    // be string from external sources, e.g.
    private void doToBoolean(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(TO_BOOLEAN + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(TO_BOOLEAN + " requires at most 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                switch (Constant.getType(objects[0])) {
                    case Constant.BOOLEAN_TYPE:
                        r.result = ((Boolean) objects[0]);
                        r.resultType = Constant.BOOLEAN_TYPE;
                        break;
                    case Constant.STRING_TYPE:
                        String x = (String) objects[0];
                        r.result = x.equals("true");
                        r.resultType = Constant.BOOLEAN_TYPE;
                        break;
                    case Constant.LONG_TYPE:
                        Long y = (Long) objects[0];
                        r.result = y.equals(1L);
                        r.resultType = Constant.BOOLEAN_TYPE;
                        break;
                    case Constant.DECIMAL_TYPE:
                        BigDecimal bd = (BigDecimal) objects[0];

                        r.result = bd.longValue() == 1;
                        r.resultType = Constant.BOOLEAN_TYPE;
                        break;
                    case Constant.NULL_TYPE:
                        throw new BadArgException("" + TO_BOOLEAN + " cannot convert null.", polyad.getArgAt(0));
                    case Constant.STEM_TYPE:
                        throw new BadArgException("" + TO_BOOLEAN + " cannot convert a stem.", polyad.getArgAt(0));
                    case Constant.UNKNOWN_TYPE:
                        throw new BadArgException("" + TO_BOOLEAN + " unknown argument type.", polyad.getArgAt(0));
                }
                return r;
            }
        };
        process1(polyad, pointer, TO_BOOLEAN, state);


    }

    private void doToNumber(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(TO_NUMBER + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(TO_NUMBER + " requires at most 1 argument", polyad.getArgAt(1));
        }

        AbstractEvaluator.fPointer pointer = new AbstractEvaluator.fPointer() {
            @Override
            public AbstractEvaluator.fpResult process(Object... objects) {
                AbstractEvaluator.fpResult r = new AbstractEvaluator.fpResult();
                switch (Constant.getType(objects[0])) {
                    case Constant.BOOLEAN_TYPE:
                        r.result = ((Boolean) objects[0]) ? 1L : 0L;
                        r.resultType = Constant.LONG_TYPE;
                        break;
                    case Constant.STRING_TYPE:
                        String x = (String) objects[0];
                        try {
                            r.result = Long.parseLong(x);
                            r.resultType = Constant.LONG_TYPE;
                        } catch (NumberFormatException nfx0) {
                            try {
                                r.result = new BigDecimal(x);
                                r.resultType = Constant.DECIMAL_TYPE;
                            } catch (NumberFormatException nfx2) {
                                // ok, kill it here.
                                throw new BadArgException("" + objects[0] + " is not a number.", polyad.getArgAt(0));
                            }
                        }
                        break;
                    case Constant.LONG_TYPE:
                        r.result = objects[0];
                        r.resultType = Constant.LONG_TYPE;
                        break;
                    case Constant.DECIMAL_TYPE:
                        r.result = objects[0];
                        r.resultType = Constant.DECIMAL_TYPE;
                        break;
                    case Constant.NULL_TYPE:
                        throw new BadArgException("" + TO_NUMBER + " cannot convert null.", polyad.getArgAt(0));
                    case Constant.STEM_TYPE:
                        throw new BadArgException("" + TO_NUMBER + " cannot convert a stem.", polyad.getArgAt(0));
                    case Constant.UNKNOWN_TYPE:
                        throw new BadArgException("" + TO_NUMBER + " unknown argument type.", polyad.getArgAt(0));
                }
                return r;
            }
        };
        process1(polyad, pointer, TO_NUMBER, state);
    }

    /**
     * Does print, say and to_string commands.
     *
     * @param polyad
     * @param state
     * @param printIt
     */
    protected void doSay(Polyad polyad, State state, boolean printIt) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (printIt && state.isRestrictedIO()) {
            polyad.setResult(QDLNull.getInstance());
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(SAY_FUNCTION + " requires at most 2 arguments", polyad.getArgAt(2));
        }
        if (polyad.getArgCount() == 0) {
            // print a blank line
            if (printIt) {
                state.getIoInterface().println("");
            }
            polyad.setResult("");
            polyad.setResultType(Constant.STRING_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        String result = "";
        boolean prettyPrintForStems = false;
        if (polyad.getArgCount() != 0) {
            Object temp = null;
            temp = polyad.evalArg(0, state);
            checkNull(temp, polyad.getArgAt(0));

            // null ok here. Means undefined variable
            if (polyad.getArgCount() == 2) {
                // assume pretty print for stems.
                Object flag = polyad.evalArg(1, state);
                checkNull(flag, polyad.getArgAt(1));
                if (flag instanceof Boolean) {
                    prettyPrintForStems = (Boolean) flag;
                } else {
                    throw new BadArgException("the second argument to " + SAY_FUNCTION + " requires a boolean", polyad.getArgAt(1));
                }
            }
            if (temp == null || temp instanceof QDLNull) {
                result = "null";

            } else {
                if (temp instanceof QDLStem) {
                    QDLStem s = ((QDLStem) temp);
                    if (prettyPrintForStems) {

                        result = ((QDLStem) temp).toString(1);
                    } else {
                        result = String.valueOf(temp);
                    }
                } else {
                    if (temp instanceof BigDecimal) {
                        result = InputFormUtil.inputForm((BigDecimal) temp);

                    } else {
                        result = temp.toString();
                    }
                }
            }
        }


        if (printIt) {
            state.getIoInterface().println(result);
            polyad.setResult(polyad.getArgAt(0).getResult());
            polyad.setResultType(polyad.getArgAt(0).getResultType());

        } else {
            polyad.setResult(result);
            polyad.setResultType(Constant.STRING_TYPE);
        }

        polyad.setEvaluated(true);
    }

    /**
     * Get the type of the argument.
     *
     * @param polyad
     * @param state
     */
    public void doVarType(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{0, 1});
            polyad.setEvaluated(true);
            return;
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(VAR_TYPE + " requires at most 1 argument", polyad.getArgAt(1));
        }

        if (polyad.getArgCount() == 0) {
            polyad.setResult(Constant.NULL_TYPE);
            polyad.setResultType(Constant.NULL_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        polyad.evalArg(0, state);
        if (polyad.getArgCount() == 1) {
            polyad.setResult(new Long(Constant.getType(polyad.getArgAt(0).getResult())));
            polyad.setResultType(Constant.LONG_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        QDLStem output = new QDLStem();
        output.put(0L, new Long(Constant.getType(polyad.getArgAt(0).getResult())));
        for (int i = 1; i < polyad.getArgCount(); i++) {
            Object r = polyad.evalArg(i, state);
            output.put(new Long(i), new Long(Constant.getType(r)));
        }
        polyad.setResultType(Constant.STEM_TYPE);
        polyad.setResult(output);
        polyad.setEvaluated(true);
    }

    protected void isDefined(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setResult(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(IS_DEFINED + " requires at least 1 argument", polyad);
        }

        if (1 < polyad.getArgCount()) {
            throw new ExtraArgException(IS_DEFINED + " requires at most 1 argument", polyad.getArgAt(1));
        }

        boolean isDef = false;
        if (polyad.getArguments().get(0) instanceof StemListNode) {
            StemListNode stemListNode = (StemListNode) polyad.getArguments().get(0);
            // check each node.
            QDLList list = new QDLList();
            for (int i = 0; i < stemListNode.getStatements().size(); i++) {
                list.add(checkDefined(stemListNode.getStatements().get(i), state));
            }
            QDLStem stem = new QDLStem(list);
            polyad.setResult(stem);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArguments().get(0) instanceof StemVariableNode) {
            StemVariableNode stemVariableNode = (StemVariableNode) polyad.getArguments().get(0);
            // check each node.
            QDLStem stem = new QDLStem();
            for (StemEntryNode sem : stemVariableNode.getStatements()) {
                stem.putLongOrString(sem.getKey().evaluate(state), checkDefined((ExpressionInterface) sem.getValue(), state));
            }
            polyad.setResult(stem);
            polyad.setResultType(Constant.STEM_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        try {
            polyad.evalArg(0, state);
        } catch (IndexError | UnknownSymbolException |
                 IllegalStateException exception) { // ESN's can throw illegal arg exception
            polyad.setResult(false);
            polyad.setResultType(Constant.BOOLEAN_TYPE);
            polyad.setEvaluated(true);
            return;
        }
        isDef = checkDefined(polyad.getArgAt(0), state);
/*
        if (polyad.getArgAt(0) instanceof VariableNode) {
            VariableNode variableNode = (VariableNode) polyad.getArgAt(0);
            // Don't evaluate this because it might not exist (that's what we are testing for). Just check
            // if the name is defined.
            isDef = state.isDefined(variableNode.getVariableReference());
        }
        if (polyad.getArgAt(0) instanceof ConstantNode) {
            ConstantNode variableNode = (ConstantNode) polyad.getArgAt(0);
            Object x = variableNode.getResult();
            if (x == null) {
                isDef = false;
            } else {
                isDef = state.isDefined(x.toString());
            }
        }
        if (polyad.getArgAt(0) instanceof ESN2) {
            Object object = polyad.getArgAt(0).getResult();
            if (object == null) {
                isDef = false;
            } else {
                isDef = true;
            }
        }
*/
        polyad.setResult(isDef);
        polyad.setResultType(Constant.BOOLEAN_TYPE);
        polyad.setEvaluated(true);
    }

    protected boolean checkDefined(ExpressionInterface exp, State state) {
        boolean isDef = false;
        if (exp instanceof VariableNode) {
            VariableNode variableNode = (VariableNode) exp;
            // Don't evaluate this because it might not exist (that's what we are testing for). Just check
            // if the name is defined.
            isDef = state.isDefined(variableNode.getVariableReference());
        }
        if (exp instanceof ConstantNode) {
            ConstantNode variableNode = (ConstantNode) exp;
            Object x = variableNode.getResult();
            if (x == null) {
                isDef = false; // oddball edge case
            } else {
                // isDef = state.isDefined(x.toString());
                isDef = true; // constants are always defined, actually.
            }
        }
        if (exp instanceof ESN2) {
            Object object = exp.getResult();
            if (object == null) {
                isDef = false;
            } else {
                isDef = true;
            }
        }
        return isDef;
    }
}
