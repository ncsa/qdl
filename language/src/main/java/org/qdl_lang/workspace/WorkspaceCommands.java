package org.qdl_lang.workspace;

import org.qdl_lang.config.QDLConfigurationLoader;
import org.qdl_lang.config.QDLConfigurationLoaderUtils;
import org.qdl_lang.config.QDLEnvironment;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.extensions.QDLLoader;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.functions.FR_WithState;
import org.qdl_lang.functions.FStack;
import org.qdl_lang.functions.FunctionRecord;
import org.qdl_lang.gui.FontUtil;
import org.qdl_lang.gui.SwingTerminal;
import org.qdl_lang.gui.editor.EditDoneEvent;
import org.qdl_lang.gui.editor.QDLEditor;
import org.qdl_lang.expressions.module.MIStack;
import org.qdl_lang.expressions.module.MTKey;
import org.qdl_lang.expressions.module.MTStack;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.parsing.QDLParserDriver;
import org.qdl_lang.parsing.QDLRunner;
import org.qdl_lang.util.InputFormUtil;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.util.QDLVersion;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.VStack;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.vfs.AbstractVFSFileProvider;
import org.qdl_lang.vfs.VFSEntry;
import org.qdl_lang.vfs.VFSFileProvider;
import org.qdl_lang.vfs.VFSPaths;
import org.qdl_lang.xml.SerializationConstants;
import org.qdl_lang.xml.XMLUtils;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.security.core.Logable;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.exceptions.GeneralException;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.core.util.*;
import edu.uiuc.ncsa.security.util.cli.*;
import edu.uiuc.ncsa.security.util.cli.editing.EditorEntry;
import edu.uiuc.ncsa.security.util.cli.editing.EditorUtils;
import edu.uiuc.ncsa.security.util.cli.editing.Editors;
import edu.uiuc.ncsa.security.util.cli.editing.LineEditor;
import edu.uiuc.ncsa.security.util.configuration.TemplateUtil;
import edu.uiuc.ncsa.security.util.configuration.XMLConfigUtil;
import edu.uiuc.ncsa.security.util.terminal.ISO6429IO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.qdl_lang.evaluate.*;
import org.qdl_lang.state.*;
import org.w3c.dom.CharacterData;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.GZIPOutputStream;

import static org.qdl_lang.config.QDLConfigurationConstants.*;
import static org.qdl_lang.config.QDLConfigurationLoaderUtils.*;
import static org.qdl_lang.evaluate.SystemEvaluator.*;
import static org.qdl_lang.gui.FontUtil.findQDLFonts;
import static org.qdl_lang.util.InputFormUtil.*;
import static org.qdl_lang.util.QDLFileUtil.*;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;
import static org.qdl_lang.vfs.VFSPaths.SCHEME_DELIMITER;
import static org.qdl_lang.workspace.Banners.*;
import static edu.uiuc.ncsa.security.core.util.StringUtils.*;
import static edu.uiuc.ncsa.security.util.cli.CLIDriver.EXIT_COMMAND;
import static edu.uiuc.ncsa.security.util.cli.CLIDriver.HELP_SWITCH;

/**
 * This is the helper class to the {@link QDLWorkspace} that does the grunt work of the ) commands.
 * <p>Created by Jeff Gaynor<br>
 * on 1/30/20 at  9:21 AM
 */
public class WorkspaceCommands implements Logable, Serializable {

    private InputStream helpS;

    public WorkspaceCommands() {
    }

    public WorkspaceCommands(IOInterface ioInterface) {
        setIoInterface(ioInterface);
    }

    public static final String SWITCH = "-";
    public static final String DISPLAY_WIDTH_SWITCH = SWITCH + "w";
    public static final String FQ_SWITCH = SWITCH + "fq";
    public static final String REGEX_SWITCH = SWITCH + "r";
    public static final String COMPACT_ALIAS_SWITCH = SWITCH + "compact";
    public static final String COLUMNS_VIEW_SWITCH = SWITCH + "cols"; // force single column view
    public static final String SHOW_FAILURES = SWITCH + "show_failures"; // for displaying workspaces that don't load
    public static final String SHOW_ONLY_FAILURES = SWITCH + "only_failures"; // for displaying only workspaces that don't load
    public static final String SAVE_AS_JAVA_FLAG = SWITCH + "java";
    public static final String KEEP_WSF = SWITCH + "keep_wsf";
    public static final String LINE_EDITOR_NAME = "line";

    public MyLoggingFacade getLogger() {
        return logger;
    }

    public transient MyLoggingFacade logger;

    XProperties env;

    public XProperties getEnv() {
        return env;
    }

    public void setEnv(XProperties xp) {
        env = xp;
    }

    public boolean isSwingGUI() {
        return swingGUI;
    }

    public void setSwingGUI(boolean swingGUI) {
        this.swingGUI = swingGUI;
    }

    boolean swingGUI = false;

    CommandLineTokenizer CLT = new CommandLineTokenizer('\'');
    protected static final String FUNCS_COMMAND = ")funcs";
    protected static final String HELP_COMMAND = ")help"; // show various values of help
    public static final String OFF_COMMAND = ")off";
    protected static final String BUFFER2_COMMAND = ")buffer";
    protected static final String FONTS_COMMAND = ")fonts";
    protected static final String SHORT_BUFFER2_COMMAND = ")b";
    protected static final String EXECUTE_COMMAND = ")";
    protected static final String RESUME_COMMAND = "))";
    protected static final String MODULES_COMMAND = ")modules";
    protected static final String LOAD_COMMAND = ")load"; // grab a file and run it
    protected static final String SAVE_COMMAND = ")save";
    protected static final String CLEAR_COMMAND = ")clear";
    protected static final String IMPORTS_COMMAND = ")imports";
    protected static final String VARS_COMMAND = ")vars";
    protected static final String ENV_COMMAND = ")env";
    protected static final String WS_COMMAND = ")ws";
    protected static final String LIB_COMMAND = ")lib";
    protected static final String EDIT_COMMAND = ")edit";
    protected static final String FILE_COMMAND = ")file";
    protected static final String HISTORY_COMMAND = ")h";
    protected static final String REPEAT_COMMAND = ")r";
    protected static final String STATE_INDICATOR_COMMAND = ")si";
    protected static final String ECHO_COMMAND = ")echo";

    public static final int RC_NO_OP = -1;
    public static final int RC_RELOAD = -2;
    public static final int RC_CONTINUE = 1;
    public static final int RC_EXIT_NOW = 0;

    /*
    The pattern is
    command action args
    e.g.
    )ws load filename
    The next constants just name them so there aren't any boo-boos
     */
    protected int COMMAND_INDEX = 0; // e.g:   )ws
    protected int ACTION_INDEX = 1; // e.g:    load
    protected int FIRST_ARG_INDEX = 2; //e.g:  filename

    protected void splashScreen() {
        String separator = Banners.getDelimiter(logoName);
        int width = Banners.getLogoWidth(logoName);
        if (!logoName.equals(NONE_STYLE)) {
            say(Banners.getLogo(logoName));
        }
        if (showBanner) {
            say(separator.substring(0, width));
            say("Welcome to the QDL Workspace");
            say("Version " + QDLVersion.VERSION);
            QDLStem buildInfo = getState().getSystemInfo().getStem("build");
            if (buildInfo != null) {
                if (buildInfo.containsKey("implementation-build")) {
                    String info = buildInfo.getString("implementation-build");
                    // of form "#build_number (timestamp)"
                    String build = info.substring(0, info.lastIndexOf(" ")).trim();
                    String ts = info.substring(info.lastIndexOf(" ") + 1);
                    ts = ts.replace(("("), "");
                    ts = ts.replace((")"), "");
                    ts = ts.trim();
                    say(build);
                    say("Time: " + ts);
                }
            }
            say("Type " + HELP_COMMAND + " for help.");
            say(separator.substring(0, width));
        }
    }

    String STARS = "********************************************************************************************";
    String DASHES = "-------------------------------------------------------------------------------------------";
    String logoName;
    boolean showBanner = true;

    protected void showHelp4Help() {
        say(HELP_COMMAND + " syntax:");
        say(HELP_COMMAND + " - (no arg) print generic help for the workspace.");
        say(HELP_COMMAND + " -all - print a short summary of help for every user defined function.");
        say(HELP_COMMAND + " " + ONLINE_HELP_COMMAND + " - print a list of all online help topics.");
        say(HELP_COMMAND + " name [" + ONLINE_HELP_EXAMPLE_FLAG + "] - print short help for name. System functions will have a");
        say("        summary printed (read the manual for more). The optional " + ONLINE_HELP_EXAMPLE_FLAG + " flag will print out examples if any");
        say(HELP_COMMAND + " name arg_count - print out detailed information for the user-defined function and the given number of arguments.");
        say("\nExample. Show all the online help within a display with of 120 characters\n");
        say("   " + HELP_COMMAND + " " + ONLINE_HELP_COMMAND + " " + DISPLAY_WIDTH_SWITCH + " 120 ");
        say("Help is available for the following (231 topics):\n" +
                "!                         dir                       mkdir                     then\n... more");
        say(HELP_COMMAND + " -m module_var | namespace - list the module help for this module");
        say("                E.g.");
        say("                   load('/path/to/module.mdl');");
        say("                 my:/ns/foo  ");
        say("                   )help -m my:/ns/foo");
        say("                 (whatever help is for the module displays.)");
        say("                See also )funcs module_var to list all of the functions in a module");
    }

    protected void showGeneralHelp() {
        say("This is the QDL (pronounced 'quiddle') workspace.");
        say("You may enter commands and execute them much like any other interpreter.");
        say("There are several commands available to help you manage this workspace.");
        say("Generally these start with a right parenthesis, e.g., ')off' (no quotes) exits this program.");
        say("Here is a quick summary of what they are and do.");
        int length = 8;
        sayi(RJustify(BUFFER2_COMMAND, length) + " - commands relating to using buffers. Alias is " + SHORT_BUFFER2_COMMAND);
        sayi(RJustify(CLEAR_COMMAND, length) + " - clear the state of the workspace. All variables, functions etc. will be lost.");
        sayi(RJustify(ENV_COMMAND, length) + " - commands relating to environment variables in this workspace.");
        sayi(RJustify(ECHO_COMMAND, length) + " - echo whatever is passed in. Useful in ws_macro, e.g.");
        sayi(RJustify(FILE_COMMAND, length) + " - file operations");
        sayi(RJustify(FUNCS_COMMAND, length) + " - list all of the imported and user defined functions this workspace knows about.");
        sayi(RJustify(RESUME_COMMAND, length) + " - short hand to resume execution for a halted process. Note the argument is the process id (pid), not the buffer number. ");
        sayi(RJustify(HELP_COMMAND, length) + " - this message.");
        sayi(RJustify(IMPORTS_COMMAND, length) + " - (deprecated) Show modules imported with the " + MODULE_IMPORT + " command.");
        sayi(RJustify(MODULES_COMMAND, length) + " - lists all the loaded modules this workspace knows about.");
        sayi(RJustify(OFF_COMMAND, length) + " - exit the workspace.");
        sayi(RJustify(LOAD_COMMAND, length) + " - Load a file of QDL commands and execute it immediately in the current workspace.");
        sayi(RJustify(FONTS_COMMAND, length) + " - (Swing GUI only) list all of the QDL complete fonts on your system).");
        sayi(RJustify(STATE_INDICATOR_COMMAND, length) + " - commands relating to the state indicator.");
        sayi(RJustify(VARS_COMMAND, length) + " - lists all of the variables this workspace knows about.");
        sayi(RJustify(WS_COMMAND, length) + " - commands relating to this workspace.");
        sayi(RJustify(EDIT_COMMAND, length) + " - commands relating to running the current editor.");
        sayi(RJustify(EXECUTE_COMMAND, length) + " - short hand to execute whatever is in the current buffer.");
        say("Generally, supplying --help as a parameter to a command will print out something useful.");
        say("Full documentation is available in the docs directory of the distro or at https://qdl-lang.org/pdf/qdl_workspace.pdf");

    }

    /**
     * On command line, this tells the CLI to interpret the attached name as a variable
     * in QDL and retrieve its value
     */
    public static final String QDL_VARIABLE_REFERENCE_MARKER = ">";

    /**
     * Replaces ) commands prefixed with a by their value from the symbol table.
     *
     * @param inputLine
     * @return
     */
    protected InputLine variableLookup(InputLine inputLine) {
        for (int i = 1; i < inputLine.size(); i++) {
            String input = inputLine.getArg(i);
            if (input.startsWith(QDL_VARIABLE_REFERENCE_MARKER)) {
                Object rawValue = getState().getValue(input.substring(1));
                if (rawValue != null) {
                    switch (Constant.getType(rawValue)) {
                        // Special handling for QDL aggregates
                        case Constant.STEM_TYPE:
                        case Constant.SET_TYPE:
                        case Constant.MODULE_TYPE:
                        case Constant.FUNCTION_TYPE:
                        case Constant.NULL_TYPE:
                        case Constant.DYADIC_FUNCTION_TYPE:
                            inputLine.getOtherValues().put(i, rawValue);
                            break;
                        default:
                            // scalars, so inputline can function normally.
                            inputLine.setArg(i, rawValue.toString());
                    }
                }
            }
        }
        return inputLine;
    }

    public QDLWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(QDLWorkspace workspace) {
        this.workspace = workspace;
    }

    QDLWorkspace workspace;
    /**
     * The workspace commands are here so they can be serialized with the rest of the workspace.
     * However, since the mechanism has to intercept every command before it gets forwarded
     * to {@link #execute(String)}, the logic for managing this list is in {@link QDLWorkspace}.
     */
    public List<String> commandHistory = new LinkedList<>();

    public Object execute(String inline) {
        try {
            return execute2(inline);
        } catch (Throwable t) {
            if (t instanceof ReturnException) {
                throw (ReturnException) t;
            }
            say("uh-oh. That did not work:" + t.getMessage());
            if (isDebugOn()) {
                t.printStackTrace();
            }
            return RC_NO_OP;
        }

    }

    protected String applyTemplates(String inline) {
        if(!isPreprocessorOn()) return inline;
        return TemplateUtil.replaceAll(inline, getEnv()); // allow replacements in commands too...

    }
    public Object execute2(String inline) throws Throwable {



        inline = TemplateUtil.replaceAll(inline, env); // allow replacements in commands too...
        InputLine inputLine = new InputLine(CLT.tokenize(inline));
        inputLine = variableLookup(inputLine);
        switch (inputLine.getCommand()) {
            case FONTS_COMMAND:
                return doFontCommand(inputLine);
            case FILE_COMMAND:
                return doFileCommands(inputLine);
            case SHORT_BUFFER2_COMMAND:
            case BUFFER2_COMMAND:
                return doBufferCommand(inputLine);
            case CLEAR_COMMAND:
                return _wsClear(inputLine);
            case EDIT_COMMAND:
                try {
                    // if the last argument is an integer,
                    inputLine.getIntArg(1);
                    return _doBufferEdit(inputLine);
                } catch (ArgumentNotFoundException t) {
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                }
                return _doEditor(inputLine);
            case EXECUTE_COMMAND:
                return _doBufferRun(inputLine);
            case RESUME_COMMAND:
                return _doSIResume(inputLine);
            case ENV_COMMAND:
                return doEnvCommand(inputLine);
            case FUNCS_COMMAND:
                return doFuncs(inputLine);
            case IMPORTS_COMMAND:
                return _moduleImports(inputLine);
            case HELP_COMMAND:
                return doHelp(inputLine);
            case MODULES_COMMAND:
                return doModulesCommand(inputLine);
            case STATE_INDICATOR_COMMAND:
                return doSICommand(inputLine);
            case ECHO_COMMAND:
                // Fix https://github.com/ncsa/qdl/issues/121
                if (inputLine.hasArg(HELP_SWITCH)) {
                    say("echo rest of the command line back to the console.");
                    say("This can be useful with " + SystemEvaluator.WS_MACRO + " to give feedback");
                    return RC_NO_OP;
                }
                say(inline.substring(inputLine.getCommand().length()).trim());
                return RC_CONTINUE;
            case OFF_COMMAND:
                if (inputLine.hasArg(HELP_SWITCH)) {
                    say(OFF_COMMAND + " [y||n] - exit the system. If you do not supply an argument, you will be prompted.");
                    sayi("y = exit immediately without saving");
                    return RC_NO_OP;
                }
                if (inputLine.hasArg("y")) {
                    shutdown();
                    return RC_EXIT_NOW;
                }
                if (readline("Do you want to exit?" + (bufferManager.anyEdited() ? " There are unsaved buffers. " : " ") + "(y/n)").equals("y")) {
                    shutdown();
                    return RC_EXIT_NOW;
                }
                say("System exit cancelled.");
                return RC_CONTINUE;
            case VARS_COMMAND:
                return doVars(inputLine);
            case WS_COMMAND:
                return doWS(inputLine);
            case LIB_COMMAND:
                inline = inline.replace(LIB_COMMAND, WS_COMMAND + " lib ");
                inputLine = new InputLine(CLT.tokenize(inline));
                return doWS(inputLine);
            case SAVE_COMMAND:
                inline = inline.replace(SAVE_COMMAND, WS_COMMAND + " save ");
                inputLine = new InputLine(CLT.tokenize(inline));
                return _wsSave(inputLine);
            case LOAD_COMMAND:
                inline = inline.replace(LOAD_COMMAND, WS_COMMAND + " load ");
                inputLine = new InputLine(CLT.tokenize(inline));
                try {
                    return _wsLoad(inputLine);
                } catch (Throwable t) {
                    say("uh-oh, that didn't work:" + t.getMessage());
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                }
        }
        say("Unknown command.");
        return RC_NO_OP;
    }


    protected void shutdown() {
        if (autosaveThread != null) {
            autosaveThread.setStopThread(true);
            autosaveThread.interrupt();
        }

    }

    Map<UUID, Integer> currentEditorSessions = new HashMap<>();
    public static final String FONT_CHECK_COMMAND = "check";
    public static final String FONT_LIST_COMMAND = "list";

    protected Object doFontCommand(InputLine inputLine) {
        if (!_doHelp(inputLine) && inputLine.getArgCount() == 0) {
            // no help specified, nothing else on input line
            // say("Sorry, please supply an argument (e.g. --help)");
            return _doListFonts(inputLine);
        }
        // They supplied exactly a number.
        if (inputLine.getArgCount() == 1) {
            try {
                Double.parseDouble(inputLine.getArg(ACTION_INDEX));
                return _doListFonts(inputLine);
            } catch (NumberFormatException nfx) {

            }
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "--help":
                int length = 8;
                say("Font commands:");
                say(RJustify("(no arg)", length) + " - list all the fonts on your system them support all QDL characters.");
                say(RJustify("(number)", length) + " - list all the fonts on your system with the given QDLness.");
                say(RJustify(FONT_LIST_COMMAND, length) + " - list every file on the system that is QDL capable");
                say(getBlanks(length) + "   An optional number for weight, which is a integer percent, ");
                say(getBlanks(length) + "   and if given all fonts that have QDLness creater than or equal to");
                say(getBlanks(length) + "   the weight will be returned with a listing of how many characters");
                say(getBlanks(length) + "   fail and what they are.");
                say(RJustify(FONT_CHECK_COMMAND, length) + " - Check either the current font (if none specified)");
                say(getBlanks(length) + "   or give the name of a font.");
                say("E.g. ");
                say(FONTS_COMMAND);
                say("(no argument) Lists all the fonts on your system that are 100% QDL compatible");
                if (GraphicsEnvironment.isHeadless()) {
                    say("Note: font operations are unsupported in text mode");
                }
                return RC_CONTINUE;
            case FONT_CHECK_COMMAND:
                return _doFontCheck(inputLine);
            case FONT_LIST_COMMAND:
                return _doListFonts(inputLine);
            default:
                say("unrecognized font command");
                return RC_CONTINUE;
        }
    }

    private int _doFontCheck(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("\n" + FONTS_COMMAND + " " + FONT_CHECK_COMMAND + " {name} = check the named  font for QDLness. No name means check the current font.");
            say("\nE.g.");
            say(FONTS_COMMAND + " " + FONT_CHECK_COMMAND + " 'Monospaced bold'");
            say("Would check the default monospaced font. ");
            return RC_CONTINUE;
        }

        Font font;
        boolean defaultFont = !inputLine.hasNextArgFor("check");
        if (defaultFont) {
            font = getFont();
            say("current font: " + fontReport(font));
        } else {
            font = new Font(inputLine.getNextArgFor("check"), Font.PLAIN, 12);
            say("checking font: " + fontReport(font));
        }
        String unsupported = FontUtil.findUnsupportedCharacters(font, QDLConstants.ALL_CHARS);
        if (unsupported.isEmpty()) {
            say("all characters are supported");
            return RC_CONTINUE;
        }
        if (defaultFont) {
            say(unsupported.length() + " characters are not supported"); // can't print unsupported characters
        } else {
            say(unsupported.length() + " characters are not supported:" + unsupported); // assume default font can show unsupported characters
        }
        return RC_CONTINUE;
    }

    private int _doListFonts(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say(FONTS_COMMAND + " " + FONT_LIST_COMMAND + " {weight}");
            say("No argument means to list information about current fonts that are 100 %");
            say("QDL compatible.");
            say("The optional integer weight will list fonts with at least that percent of");
            say("compatibility plus a list of characters that fail.");
            say("\nE.g. ");
            say(FONTS_COMMAND + " " + FONT_LIST_COMMAND + " 90");
            say("would list all fonts that support 90% or more of QDLs character set.");
            say("You could also just enter");
            say(FONTS_COMMAND + " 90");

            if (GraphicsEnvironment.isHeadless()) {
                say("Note: font operations are unsupported in text mode");
            }
            return RC_CONTINUE;
        }
        try {
            inputLine.removeSwitch("list");
            List<String> fonts;
            boolean weighted = false;
            double weight = 100.00;
            if (inputLine.getArgCount() == 1) {
                try {
                    weight = Double.parseDouble(inputLine.getLastArg());
                    weighted = true;
                    fonts = findQDLFonts(weight);
                } catch (NumberFormatException bfx) {
                    fonts = findQDLFonts();
                }
            } else {
                fonts = findQDLFonts();
            }
            if (fonts.isEmpty()) {
                say("no founts found");
            } else {
                if (weighted) {
                    say("list of fonts that can display " + weight + "% or more of the QDL character set:");
                } else {
                    say("list of fonts that can display the entire QDL character set:");
                }
                for (String s : fonts) {
                    say(s);
                }
            }
            say("current font: " + fontReport(getFont()));
            return RC_CONTINUE;

        } catch (Throwable throwable) {
            if (isDebugOn()) {
                throwable.printStackTrace();
            }
            say("That didn't work: '" + throwable.getMessage() + "'");
            return RC_NO_OP;
        }
    }

    protected String fontReport(Font font) {
        return font.getName() + " " + FontUtil.getStyle(font) + " at " + font.getSize() + " pts.";
    }

    protected void _doGUIEditor(BufferManager.BufferRecord br) {

        QDLEditor qdlEditor = new QDLEditor(this, br.alias, bufferManager.getIndex(br));

        currentEditorSessions.put(qdlEditor.getID(), bufferManager.getIndex(br));
        if (br.getContent() == null) {
            qdlEditor.setup("");
        } else {
            qdlEditor.setup(StringUtils.listToString(br.getContent()));
        }
    }

    public void editDone(EditDoneEvent editDoneEvent) {
        switch (editDoneEvent.getType()) {
            case EditDoneEvent.TYPE_BUFFER:
                if (currentEditorSessions.isEmpty()) {
                    break; // do nothing. They hit save an extra time
                }
                int editorID = currentEditorSessions.get(editDoneEvent.id);
                BufferManager.BufferRecord br = bufferManager.getBufferRecord(editorID);
                //if (br.memoryOnly) {
                br.content = StringUtils.stringToList(editDoneEvent.content);
                br.edited = true;
                //}
                //   currentEditorSessions.remove(editDoneEvent.id);
                break;
            case EditDoneEvent.TYPE_FILE:
                throw new NotImplementedException("File saves in the QDL editor should be handled there.");
            case EditDoneEvent.TYPE_FUNCTION:
                restoreFunction(StringUtils.stringToList(editDoneEvent.content), editDoneEvent.getLocalName(), editDoneEvent.getArgState());
                break;
            case EditDoneEvent.TYPE_VARIABLE:
                int x = editDoneEvent.getArgState();
                boolean isText = (x % 2) == 1;
                boolean isStem = (2 <= x);
                restoreVariable(editDoneEvent.getLocalName(), StringUtils.stringToList(editDoneEvent.content), isText, isStem);
                break;
        }
    }

    /**
     * the interrupts (inclusions and exclusions) per pid.
     */
    HashMap<Integer, SIInterrupts> interruptList = new HashMap<>();

    /**
     * Set of SIEntries by Process ID. Note that there is one active {@link SIEntry} per pid.
     */
    public static class SIEntries extends TreeMap<Integer, SIEntry> {
        int maxKey = 100; // system pid is 0, but that is not stored here.

        HashMap<Object, SIEntry> labelMap = new HashMap<>();

        public void addLabel(SIEntry entry) {
            if (entry.hasLabel()) {
                labelMap.put(entry.getLabel(), entry);
            }
        }

        public boolean hasLabel(Object label) {
            return labelMap.containsKey(label);
        }

        public SIEntry getByLabel(Object label) {
            return labelMap.get(label);
        }

        @Override
        public SIEntry put(Integer key, SIEntry value) {
            int key0 = key; // since we have to do math with it and don't want the class
            if (key0 < 0) {
                // set the key to the next PID
                key0 = 1 + maxKey;
                maxKey++;
            } else {
              //  if (containsKey(key)) {
               //     throw new IllegalArgumentException("Error: PID is in use");
             //   }
            }
            maxKey = Math.max(maxKey, key0);
            addLabel(value);
            return super.put(key0, value);
        }

        public int nextKey() {
            return maxKey + 1;
        }
    }

    public SIEntries getSIEntries() {
        return siEntries;
    }

    SIEntries siEntries = new SIEntries();

    public static class WSInternals implements Serializable {
        State defaultState;
        Integer currentPID;
        SIEntries siEntries;
        State activeState;
        Date startTimestamp;
        String id;
        String description;
        boolean echoOn;
        boolean prettyPrint;
        String saveDir = null;
        boolean debugOn;

        public void toXML(XMLStreamWriter xsw) throws XMLStreamException {

        }

        public void fromXML(XMLEventReader xer) throws XMLStreamException {

        }
    }

    public static final String SI_MESSAGES = "messages";
    boolean siMessagesOn = true;

    protected Object doSICommand(InputLine inputLine) {
        if (!_doHelp(inputLine) && inputLine.getArgCount() == 0) {
            // no help specified, nothing else on input line
            // say("Sorry, please supply an argument (e.g. --help)");
            return _doSIList(inputLine);
        }

        switch (inputLine.getArg(ACTION_INDEX)) {
            case "--help":
                say("State indicator commands:");
                say("    (no arg) - same as the list action");
                say("   get [pid] - Get information about a specific PID or the currently active");
                say("               one if no arguments is given. Any breakpoints set will be shown");
                say("      --help - this message");
                say("    messages - on | off. Echo halt messages to the console. No argument will show the");
                say("               current setting.");
                say("        list - list all current states in the indicator by process id (pid)");
                say("       reset - clear the entire state indicator, restoring the system process as the default");
                say("resume [pid] - resume running the given process. No arguments means restart the current one.");
                say("               Supplying the -go switch means to run the process with no more breakpoints.");
                say("    )) [pid] - shorthand for: )si resume [pid]");
                say("      rm pid - remove the given state from the system, losing all of it. If it is the");
                say("               current state, the system default will replace it.");
                say("   set [pid] - set the current process id. No argument means to display the current pid.");
                say("               you may also supply a list of interrupts to exclude (i.e. skip) or ");
                say("               a list to include (i.e. only those are evaluated).");
                say("     threads - List threads by process id and name. Threads are only started with the");
                say("               fork command. Threads run asynchronously, and all you can do to stop one");
                say("               is use kill(pid) on it if it is misbehaving.");
                return RC_NO_OP;
            case "list":
                if (_doHelp(inputLine)) {
                    say("list  - list the current process ids. The system reserves 0 for itself");
                    return RC_NO_OP;
                }
                // list all current pids.
                return _doSIList(inputLine);
            case SI_MESSAGES:
                if (_doHelp(inputLine)) {
                    say(SI_MESSAGES + "  [on|off] - echo halt command messagfes to console.");
                    say("No argument shows current value. Passing in a value sets it.");
                    return RC_NO_OP;
                }
                if (inputLine.getArgCount() == 1) {
                    // no arguments => query
                    say("State indicator messages are " + (siMessagesOn ? "on" : "off"));
                    return RC_NO_OP;
                }
                Boolean yeahOrNay = inputLine.getBooleanLastArg();
                if (yeahOrNay == null) {
                    say("Sorry, but " + inputLine.getLastArg() + " could not be parsed as a boolean.");
                    return RC_NO_OP;
                }
                boolean oldSiMessage = siMessagesOn;
                siMessagesOn = yeahOrNay;
                say("State indicator messages are now " + (siMessagesOn ? "on" : "off") + ", were " + (oldSiMessage ? "on" : "off"));
                return RC_NO_OP;
            case "clear":
                if (_doHelp(inputLine)) {
                    say("clear - clears all pending entries to teh state indicator and restores the workspace ");
                    say("        with the default pid of 0");
                    return RC_NO_OP;
                }
                SIEntry sie = siEntries.get(0);
                siEntries = new SIEntries();
                siEntries.put(0, sie);
                currentPID = 0;
                say("state indicator reset.");
                return RC_CONTINUE;
            case "resume":

                // resume the execution of a process by pid
                return _doSIResume(inputLine);
            case "rm":
                if (_doHelp(inputLine)) {
                    say("rm pid - remove the given state from the system, losing all of it.");
                    say("         If the pid is the current one, the state will reset to the default system");
                    say("         pid of 0.");
                    return RC_NO_OP;
                }
                return _doSIRemove(inputLine);
            case "set":
                if (_doHelp(inputLine)) {
                    say("set [pid]-  set the current process id.");
                    say("No argument means to display the current pid.");
                    return RC_NO_OP;
                }
                return _doSISet(inputLine);
            case "get":
                return _doSIGet(inputLine);

            case "threads":
                if (_doHelp(inputLine)) {
                    say("threads - list the threads by process id. Threads are created with the fork command.");
                    return RC_NO_OP;
                }
                if (getState().getThreadTable().isEmpty()) {
                    say("(no active threads)");
                    return RC_CONTINUE;
                }
                for (Integer key : getState().getThreadTable().keySet()) {
                    say(key + " : " + state.getThreadTable().get(key).name);
                }
                return RC_CONTINUE;
            default:
                say("sorry, unknown command.");
        }

        return RC_NO_OP;
    }

    protected Object _doSIRemove(InputLine inputLine) {
        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("sorry, no pid given.");
            return RC_CONTINUE;
        }
        try {
            int pid = inputLine.getIntArg(FIRST_ARG_INDEX);
            if (pid == currentPID) {
                interpreter = defaultInterpreter;
                state = defaultState;
                currentPID = 0;
            }
            if (!siEntries.containsKey(pid)) {
                say("invalid pid " + pid);
                return RC_NO_OP;
            }

            siEntries.remove(pid);
            say("process id " + pid + " has been removed from the state indicator.");
            return RC_NO_OP;

        } catch (ArgumentNotFoundException ax) {
            say("Sorry, but that was not a valid pid");
        }

        return RC_NO_OP;
    }

    public static final String SI_INTERRUPT_INCLUDE = "-interrupt_include";
    public static final String SI_INTERRUPT_INCLUDE_SHORT = "-ii";
    public static final String SI_INTERRUPT_EXCLUDE = "-interrupt_exclude";
    public static final String SI_INTERRUPT_EXCLUDE_SHORT = "-xi";
    public static final String SI_INTERRUPT_GO = "-go";

    protected Object _doSIGet(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("Case 1. No argument means to display the current process id and any interrupts.");
            say("Case 2. Display the interrupts for a specific pid.");
            return RC_NO_OP;
        }
        if (inputLine.getArgCount() == 1) {
            if (currentPID == 0) {
                say("The system process is the current one, (pid = 0)");
                return RC_CONTINUE;
            }
            SIEntry si = siEntries.get(currentPID);
            SIInterrupts interrupts = si.getInterrupts();
            say("the current process id is " + currentPID);
            if (interrupts.hasInclusions()) {
                say("included interrupts are " + interrupts.getInclusions() + (interrupts.getInclusions().hasRegex() ? " (regex)" : ""));
            }
            if (interrupts.hasExclusions()) {
                say("excluded interrupts are " + interrupts.getExclusions() + (interrupts.getExclusions().hasRegex() ? " (regex)" : ""));
            }
            return RC_CONTINUE;
        }
        int pid = inputLine.getIntArg(FIRST_ARG_INDEX);
        if (!siEntries.containsKey(pid)) {
            say("invalid pid " + pid);
            return RC_NO_OP;
        }
        SIEntry si = siEntries.get(pid);
        SIInterrupts interrupts = si.getInterrupts();
        if (!interrupts.hasInterrupts()) {
            say("no interrupts set for pid " + pid);
            return RC_CONTINUE;
        }

        say("interrupts for pid " + pid);
        if (interrupts.hasInclusions()) {
            String msg;
            if (interrupts.getInclusions().hasList()) {
                msg = interrupts.getInclusions().interrupts.toString();
            } else {
                msg = interrupts.getInclusions().regex + " (regex)";
            }
            say("included interrupts are " + msg);
        }
        if (interrupts.hasExclusions()) {
            String msg;
            if (interrupts.getExclusions().hasList()) {
                msg = interrupts.getExclusions().interrupts.toString();
            } else {
                msg = interrupts.getExclusions().regex + " (regex)";
            }
            say("excluded interrupts are " + msg);
        }
        return RC_CONTINUE;
    }

    protected Object _doSISet(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("Case 1. Set the current pid -- no arguments");
            say("set pid");
            say("This makes this the current process.");
            say("Case 2. Set interrupts for a pid");
            say("set [pid] [" + SI_INTERRUPT_INCLUDE_SHORT + " list | regex] [" + SI_INTERRUPT_EXCLUDE_SHORT + " >list | regex]");
            say("   set the current process id and/or the excluded or included interrupt ");
            say("   labels. You may also use the long forms of the flags ");
            say("   " + SI_INTERRUPT_INCLUDE + " or " + SI_INTERRUPT_EXCLUDE);
            say("If interrupts have been set, e.g. in )b run, then these will be replace the current ones. ");
        }
        if (inputLine.getArgCount() == 2) {
            // Case 1. Only a pid is passed in.
            int pid = 0;
            try {
                pid = inputLine.getIntArg(FIRST_ARG_INDEX);
                if (pid == 0) {
                    interpreter = defaultInterpreter;
                    state = defaultState;
                    currentPID = 0;
                    say("system process restored ");
                    return RC_CONTINUE;
                }
                if (!siEntries.containsKey(pid)) {
                    say("invalid pid " + pid);
                    return RC_NO_OP;
                }
            } catch (ArgumentNotFoundException ax) {
                say("sorry but that is not a valid integer");
                return RC_NO_OP;
            }


            SIEntry sie = siEntries.get(pid);
            interpreter = sie.interpreter;
            // bare bones what new
            interpreter.setEchoModeOn(isEchoModeOn());
            interpreter.setPrettyPrint(isPrettyPrint());
            sie.state.setIoInterface(state.getIoInterface());
            state = sie.state;
            currentPID = pid;
            say("pid set to " + pid);
            return RC_CONTINUE;
        }
        // Case 2. Setting interrupts for a pid
        // whittle off interrupts
        SIInterruptList includes = getSIInterruptList(inputLine, SI_INTERRUPT_INCLUDE, SI_INTERRUPT_INCLUDE_SHORT);
        SIInterruptList excludes = getSIInterruptList(inputLine, SI_INTERRUPT_EXCLUDE, SI_INTERRUPT_EXCLUDE_SHORT);
        if(includes != null && excludes != null) {
            // If both sent, normalize to includes/excludes, zero out excludes.
            includes.interrupts.removeAll(excludes.interrupts);
            excludes = null;
        }
        boolean gotIncludes = includes != null;
        boolean gotExcludes = excludes != null;
        int pid = 0;
        if (inputLine.getArgCount() == 0) {
            pid = currentPID;
        } else {
            try {
                pid = inputLine.getIntArg(FIRST_ARG_INDEX);
                if (!siEntries.containsKey(pid)) {
                    say("invalid pid " + pid);
                    return RC_NO_OP;
                }
            } catch (ArgumentNotFoundException ax) {
                say("sorry but that is not a valid integer");
                return RC_NO_OP;
            }
        }


        SIInterrupts interrupts = siEntries.get(pid).getInterrupts();
        if (gotIncludes) {
            interrupts.setInclusions(includes);
        }
        if (gotExcludes) {
            interrupts.setExclusions(excludes);
        }

        String message;
        if (gotIncludes) {
            if (gotExcludes) {
                message = "updated excluded and included interrupts for pid " + currentPID;
            } else {
                message = "updated included interrupts for pid " + currentPID;
            }
        } else {
            if (gotExcludes) {
                message = "updated excluded interrupts for pid " + currentPID;
            } else {
                // no includes or excludes, so just a query
                message = "currently active process id is " + currentPID;
            }
        }
        say(message);
        return RC_CONTINUE;
    }

    /**
     * Create the interrupt list from the input line with any flags. A null response
     * means no such interrupts were found.
     * @param inputLine
     * @param flags
     * @return
     */
    protected SIInterruptList getSIInterruptList(InputLine inputLine, String... flags) {
        if (!inputLine.hasArg(flags)) {
            return null;
        }
        int iIndex = inputLine.getIndexOfKey(flags) + 1; // want next argument after flag
        // options are that the argument is a string, which means we have a regex, or it references
        // a list.
        SIInterruptList interrupts = null;
        if (-1 < iIndex) {
            if (inputLine.getOtherValues().containsKey(iIndex)) {
                Object qdlObject = inputLine.getOtherValues().get(iIndex);
                switch (Constant.getType(qdlObject)) {
                    case Constant.STEM_TYPE:
                        QDLStem stem = (QDLStem) qdlObject;
                        interrupts = new SIInterruptList(stem.valueSet());
                        break;
                    case Constant.SET_TYPE:
                        interrupts = new SIInterruptList((QDLSet) qdlObject);
                        break;
                    case Constant.STRING_TYPE:
                        interrupts = new SIInterruptList((String) qdlObject);
                        break;
                    case Constant.LONG_TYPE:
                    case Constant.DECIMAL_TYPE:
                    case Constant.BOOLEAN_TYPE:
                    case Constant.NULL_TYPE:
                        interrupts = new SIInterruptList(qdlObject.toString());
                        break;
                    default:
                        throw new IllegalArgumentException("unsupported label type " + qdlObject);
                }
            } else {
                String raw = inputLine.getNextArgFor(flags);
                //Special cases. These aren't really parsed and must be passed as it.
                // This allows to easily zero out these lists.
                if (raw.equals("∅") || raw.equals("[]") || raw.equals("{}")) {
                    // basically this means to clear the list
                    interrupts = null;
                } else {
                    interrupts = new SIInterruptList(inputLine.getNextArgFor(flags));
                }
            }
        }
        inputLine.removeSwitchAndValue(flags);

        return interrupts;
    }

    protected Object _doSIResume(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            int width = 12;
            say("resume ["+ SI_INTERRUPT_GO + "] [" + SI_INTERRUPT_INCLUDE_SHORT + " >list | regex] [" + SI_INTERRUPT_EXCLUDE_SHORT + " >list | regex]");
            say(getBlanks(width + 3) + "Resume the current process id, setting the included or excluded.");
            say(getBlanks(width + 3) + "interrupts, or clearing all using " + SI_INTERRUPT_GO + ".");
            interruptHelpBlock(width);
            return RC_NO_OP;
        }
        SIEntry sie = null; // needed for visibility in catch block
        int originalPID = currentPID;
        try {
            boolean noInterrupt = inputLine.hasArg("-go");
            inputLine.removeSwitch("-go");
            // whittle off interrupts
            SIInterruptList includes = getSIInterruptList(inputLine, SI_INTERRUPT_INCLUDE, SI_INTERRUPT_INCLUDE_SHORT);
            SIInterruptList excludes = getSIInterruptList(inputLine, SI_INTERRUPT_EXCLUDE, SI_INTERRUPT_EXCLUDE_SHORT);

            int pid = 0;
            if (inputLine.getCommand().equals(RESUME_COMMAND)) {
                if (inputLine.getArgCount() == 0) {
                    // They just passed in a ))
                    pid = currentPID;
                } else {
                    try {
                        pid = inputLine.getIntArg(1);
                        currentPID = pid;
                    } catch (Throwable n) {
                        say("sorry but the pid could not be determined");
                        return RC_CONTINUE;
                    }
                }
            } else {
                if (inputLine.hasArgAt(FIRST_ARG_INDEX)) {
                    pid = inputLine.getIntArg(FIRST_ARG_INDEX);
                } else {
                    // no arg. Use current pid
                    pid = currentPID;
                }
            }
            if (!siEntries.containsKey(pid)) {
                say("invalid pid " + pid);
                return RC_NO_OP;
            }
            sie = siEntries.get(pid);
            try {
                if (sie.qdlRunner == null) {
                    say("si damage"); // something is out of whack. Don't kill the workspace, just tell them.
                    return RC_NO_OP;
                }
                if (includes != null) {
                    sie.getInterrupts().setInclusions(includes);
                }
                if (excludes != null) {
                    sie.getInterrupts().setExclusions(excludes);
                }
                sie.qdlRunner.restart(sie, sie.getInterrupts(), noInterrupt);
                // if it finishes, then reset to default.
                endProcess(sie);
            } catch (InterruptException ix) {
                InterruptUtil.updateSIE(ix, sie);
                InterruptUtil.printUpdateMessage(this, sie);
            }
        } catch (ArgumentNotFoundException ax) {
            say("Sorry, but that was not a valid pid");
        } catch (Throwable x) {

            if (x instanceof ReturnException) {
                ReturnException rx = (ReturnException) x;
               /* if (rx.resultType != Constant.NULL_TYPE) {
                    getIoInterface().println(rx.result);
                    getIoInterface().flush();
                }*/
                if (sie != null) {
                    endProcess(sie);
                    throw rx;
                }

                return RC_NO_OP;
            }
            if (x instanceof QDLException) {
                throw (QDLException) x;
            }
            if (isDebugOn()) {
                x.printStackTrace();
            }
            say("sorry but the process could not be restarted");
        }
        return RC_NO_OP;
    }

    private void endProcess(SIEntry sie) {
        if (sie.state.getSuperState() == null) {
            state = defaultState;
        } else {
            state = sie.state.getSuperState();
        }
        currentPID = 0;
        interpreter = defaultInterpreter;
        siEntries.remove(sie.pid);
        say("exit pid " + sie.pid);
    }

    protected QDLInterpreter cloneInterpreter(InputLine inputLine) {
        try {
            State newState = StateUtils.clone(getState());
            QDLInterpreter qdlParser = new QDLInterpreter(newState);
            newState.setIoInterface(getIoInterface()); // or IO won't work
            qdlParser.setEchoModeOn(interpreter.isEchoModeOn());
            qdlParser.setPrettyPrint(interpreter.isPrettyPrint());
            return qdlParser;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    // formatting constants for si. These are the width of the fields in the si list command.
    // One of these days, if workspaces get too big or somesuch,
    protected int ___SI_PID = 5;
    protected int ___SI_ACTIVE = 6;
    protected int ___SI_TIMESTAMP = 25;
    protected int ___SI_LINE_NR = 5;
    protected int ___SI_SIZE = 6;

    protected Object _doSIList(InputLine inputLine) {
        // entry is
        // pid status  timestamp size message
        // pid is an integer
        // status is * for active -- for not
        // timestamp is when created
        // size is the size of the state object.
        // message is the message
        say(pad2("pid", ___SI_PID) +
                " | active | " +
                pad2("stmt", ___SI_LINE_NR) + " | " +
                pad2("line", ___SI_LINE_NR) + " | " +
                pad2("time", ___SI_TIMESTAMP) + " | " +
                pad2("size", ___SI_SIZE) + " | " +
                "message");
        // do system separate since it is never stored in the si entries
        String lineOut = pad2(0, ___SI_PID) +
                " | " + pad2((0 == currentPID ? "  * " : " ---"), ___SI_ACTIVE) +
                " | " + pad2(" ", ___SI_LINE_NR) + // statement number
                " | " + pad2(" ", ___SI_LINE_NR) + // line number
                " | " + pad2(startTimeStamp, ___SI_TIMESTAMP) + // timestamp
                " | " + pad2(StateUtils.size(getState()), ___SI_SIZE) +
                " | " + "system";
        say(lineOut);

        for (Integer key : siEntries.keySet()) {
            SIEntry siEntry = siEntries.get(key);
            int statementNumber = siEntry.statementNumber;
            int lineNumber = -1;
            if (siEntry.statement.hasTokenPosition()) {
                lineNumber = siEntry.statement.getTokenPosition().line;
            }
            ;
            lineOut = pad2(key, ___SI_PID) +
                    " | " + pad2((siEntry.pid == currentPID ? "  * " : " ---"), ___SI_ACTIVE) +
                    " | " + pad2(statementNumber, ___SI_LINE_NR) +
                    " | " + pad2(lineNumber == -1 ? "--" : Integer.toString(lineNumber), ___SI_LINE_NR) +
                    " | " + pad2(siEntry.timestamp, ___SI_TIMESTAMP) +
                    " | " + pad2(StateUtils.size(siEntry.state), ___SI_SIZE) +
                    " | " + siEntry.message;
            say(lineOut);
        }
        return RC_CONTINUE;
    }


    protected Object doFileCommands(InputLine inputLine) throws Throwable {
        if (inputLine.size() <= ACTION_INDEX) {
            say("Sorry, no default action, please supply an argument (e.g. --help)");
            return RC_NO_OP;
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "help":
            case "--help":
                say("File commands:");
                sayi("copy source target - copy the source to the target, overwriting its contents.");
                sayi("  delete file_name - Same as rm.");
                sayi("         ls or dir - list the contents of a directory. Directories end with a /");
                sayi("        mkdir path - make a directory. This makes all intermediate directories as needed.");
                sayi("      rm file_name - remove a single file.");
                sayi("        rmdir path - remove a single directory. This fails if there are any entries in the directory.");
                sayi("               vfs - list information about all currently mounted virtual file systems.");
                return RC_NO_OP;
            case "copy":
                return _fileCopy(inputLine);
            case "edit":
                return _doFileEdit(inputLine);
            case "rm":
            case "delete":
                return _fileDelete(inputLine);
            case "ls":
            case "dir":
                return _fileDir(inputLine);
            case "mkdir":
                return _fileMkDir(inputLine);
            case "rmdir":
                return _fileRmDir(inputLine);
            case "vfs":
                return _fileVFS(inputLine);
            default:
                say("unknown file command");
                return RC_NO_OP;
        }
    }

    protected boolean useExternalEditor() {
        return hasEditors() && !getQdlEditors().isEmpty() && isUseExternalEditor() && !getExternalEditorName().equals(LINE_EDITOR_NAME);
    }

    protected Object _doFileEdit(InputLine inputLine) throws Throwable {
        if (qdlEditors == null) {
            getQdlEditors();
        }
        String source = inputLine.getArg(FIRST_ARG_INDEX);
        List<String> content;
        if (QDLFileUtil.exists(getState(), source)) {
            content = QDLFileUtil.readTextFileAsLines(getState(), source);
        } else {
            content = new ArrayList<>();
        }
        if (useExternalEditor()) {
            _doExternalEdit(content);
        } else {
            List<String> output = new ArrayList<>();
            Object rc = editFile(content, output, source);
            if (rc instanceof Response) {
                return rc;
            }
            if (rc.equals(RC_CONTINUE)) {
                return rc;
            }
            restoreFile(output, source);
        }
        return RC_CONTINUE;
    }

    public Object editFile(List<String> input, List<String> output, String fileName) {
        if (isSwingGUI()) {
            File f = new File(fileName);
            try {
                QDLEditor qdlEditor = new QDLEditor(f);
                qdlEditor.setType(EditDoneEvent.TYPE_FILE);
                qdlEditor.setup();
            } catch (Throwable e) {
                say("Error editing file '" + f.getAbsolutePath() + "', " + e.getMessage());
            }
            return RC_CONTINUE;
        } else {
            LineEditor lineEditor = new LineEditor(input);

            try {
                lineEditor.execute();
                if (!QDLFileUtil.isVFSPath(fileName)) {
                    lineEditor.setTargetFile(new File(fileName));// in case they issue a write in the editor.
                }
                List<String> out = lineEditor.getBuffer();
                output.addAll(out);
                QDLFileUtil.writeTextFile(getState(), fileName, out);
                return RC_CONTINUE;
            } catch (Throwable e) {
                say("there was a problem editing the file " + e.getMessage());
                e.printStackTrace();
            }
        }

        return RC_CONTINUE;
    }

    public Object restoreFile(List<String> content, String fileName) throws Throwable {
        QDLFileUtil.writeTextFile(getState(), fileName, content);
        return RC_CONTINUE;
    }

    public BufferManager getBufferManager() {
        return bufferManager;
    }

    BufferManager bufferManager = new BufferManager();

    private Object doBufferCommand(InputLine inputLine) throws Throwable {
        if (inputLine.size() <= ACTION_INDEX) {
            return _doBufferList(inputLine);
        }
        int width = 25;
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "help":
            case "--help":
                say("buffer commands:");
                say("    # - refers to the buffer number aka index aka handle.");
                say("alias - refers to the buffer name.");
                say("#a - refers to the both the number or  name.");

                sayi(RJustify("create alias { path | " + BUFFER_IN_MEMORY_ONLY_SWITCH + "}", width) + " - create a new buffer.");
                sayi(RJustify("check (#a)", width) + " - run the buffer through the parser and check for syntax errors. Does not execute.");
                sayi(RJustify("delete or rm (#a)", width) + " - delete the buffer. This does not delete the file.");
                sayi(RJustify("clean [-show]", width) + " - remove all the files in the temp directory. This is best done when there are no open buffers.");
                sayi(RJustify("edit (#a)", width) + " - Start the built-in line editor and load the given buffer ");
                sayi(RJustify("link alias source target", width) + " - create a link for given source to the target. The target will be copied to source on save.");
                sayi(RJustify("ls | list", width) + " - display all the active buffers and their numbers");
                sayi(RJustify("path {new_path}", width) + " - (no arg) means to display current default save path, otherwise set it. Default is qdl temp dir.");
                sayi(RJustify("reload (#a)", width) + " - reload the buffer from disk.");
                sayi(RJustify("reset", width) + " - deletes all buffers and sets the start number to zero. This clears the buffer state.");
                sayi(RJustify("run (#a) {&| !}", width) + " -  Run the buffer. If & is there, do it in its own environment");
                sayi(RJustify("show (#a)", width) + " - display contents of buffer in the workspace. ");
                sayi(RJustify("save ", width) + " - Alias for write. See entry for write");
                sayi(RJustify("write (#a) {path}", width) + " - write the buffer. If linked, source is copied to target. ");
                sayi(getBlanks(width) + "   path only applies to in memory buffers. The buffer will be written to the path and will be converted");
                sayi(getBlanks(width) + "   to a regular file buffer.");
                return RC_NO_OP;
            case "reload":
                return _doBufferReload(inputLine);
            case "create":
                return _doBufferCreate(inputLine);
            case "check":
                return _doBufferCheck(inputLine);
            case "reset":
                return _doBufferReset(inputLine);
            case "delete":
            case "rm":  // for our unix friends
                return _doBufferDelete(inputLine);
            case "edit":
                return _doBufferEdit(inputLine);
            case "link":
                return _doBufferLink(inputLine);
            case "list":
            case "ls":
                return _doBufferList(inputLine);
            case "run":
                return _doBufferRun(inputLine);
            case "show":
                return _doBufferShow(inputLine);
            case "path":
                return _doBufferPath(inputLine);
            case "write":
            case "save":
                return _doBufferWrite(inputLine);
            case "clean":
                return _doCleanTempDir(inputLine);
            default:
                say("unrecognized buffer command");
                return RC_NO_OP;
        }
    }

    public static final String CLEAN_TEMP_DIR_SIZE_SWITCH = "-size";

    private Object _doCleanTempDir(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("clean [" + CLEAN_TEMP_DIR_SIZE_SWITCH + "]- clean temp directory that holds old edit sessions.");
            say(CLEAN_TEMP_DIR_SIZE_SWITCH + " = just give a cound of the number of files.");
            return RC_NO_OP;
        }
        boolean showSize = inputLine.hasArg(CLEAN_TEMP_DIR_SIZE_SWITCH);
        inputLine.removeSwitch(CLEAN_TEMP_DIR_SIZE_SWITCH);
        boolean isVFSPath = isVFSPath(getBufferDefaultSavePath());

        if ((!isVFSPath && getState().isServerMode())) {
            say("Operation not permitted in server mode");
            return RC_NO_OP;
        }
        if (!isVFSPath) {
            say("virtual file clean not supported at this time");
            return RC_NO_OP;
        }
        // To do: Add VFS support for virtual temp files.
      /*  try {
            VFSFileProvider vfs = getState().getVFS(getBufferDefaultSavePath())
                    vfs.
        } catch (Throwable e) {
            e.printStackTrace();
        }*/
        if (showSize) {
            File tempDir = new File(getBufferDefaultSavePath());
            long total = 0L;
            long count = 0;
            File[] files = tempDir.listFiles();
            if (files == null) {
                say("no such path");
                return RC_NO_OP;
            }
            if (files.length == 0) {
                say("(empty)");
                return RC_CONTINUE;
            }
            for (File f : files) {
                if (f.isFile()) {
                    total = total + f.length();
                    count++;
                }
            }
            say("temp directory : " + tempDir.getAbsolutePath());
            say("   total files : " + count);
            say("    total size : " + total);
            return RC_CONTINUE;
        }
        File tempDir = new File(getBufferDefaultSavePath());
        long total = 0L;
        long count = 0;
        long totalDeleted = 0L;
        long sizeDeleted = 0L;
        long sizeNotDeleted = 0L;
        long totalNotDeleted = 0L;
        File[] files = tempDir.listFiles();
        if (files == null) {
            say("no such path");
            return RC_NO_OP;
        }
        if (files.length == 0) {
            say("(empty)");
            return RC_CONTINUE;
        }
        for (File f : files) {
            if (f.isFile()) {
                long fileSize = f.length();
                total = total + fileSize;
                count++;
                if (f.delete()) {
                    sizeDeleted = sizeDeleted + fileSize;
                    totalDeleted++;
                } else {
                    totalNotDeleted++;
                    sizeNotDeleted = sizeNotDeleted + fileSize;
                }
            }
        }
        say("   temp directory : " + tempDir.getAbsolutePath());
        say("      total files : " + count);
        say("       total size : " + total);
        say("    total deleted : " + totalDeleted + " (" + sizeDeleted + " bytes)");
        say("total not deleted : " + totalNotDeleted + " (" + sizeNotDeleted + " bytes)");

        return RC_CONTINUE;
    }

    protected Object _doBufferReload(InputLine inputLine) throws Throwable {
        if (_doHelp(inputLine)) {
            say("reload handle | alias - re-read the buffer from storage.");
            say("If the buffer resides in memory, this has no effect");
            return RC_NO_OP;
        }
        BufferManager.BufferRecord br = getBR(inputLine);
        if (br == null) {
            say("buffer not found");
            return RC_NO_OP;
        }
        if (br.memoryOnly) {
            return RC_NO_OP;
        }

        if (br.isLink()) {
            br.setContent(bufferManager.readFile(br.linkSavePath));
        } else {
            br.setContent(bufferManager.readFile(br.srcSavePath));
        }
        say("done");
        return RC_CONTINUE;
    }

    public String getBufferDefaultSavePath() {
        if (bufferDefaultSavePath == null) {
            if (getTempDir() != null) {
                bufferDefaultSavePath = getTempDir().getAbsolutePath();
                if (!bufferDefaultSavePath.endsWith("/")) {
                    bufferDefaultSavePath = bufferDefaultSavePath + "/";
                }
            }
        }
        return bufferDefaultSavePath;
    }

    String bufferDefaultSavePath = null;

    protected Object _doBufferPath(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("path {new_path}");
            say("(no arg) - show the current default path for saving buffers. This is used ");
            say("           in the case that the buffer is not an absolute path.");
            say("new_path - sets the current path.");
            return RC_NO_OP;
        }
        if (1 == inputLine.getArgCount()) { // zeroth arg is "path" which got us here. Args start at index 1;
            say("current default save path for relative buffers is " + getBufferDefaultSavePath());
            return RC_CONTINUE;
        }
        String newPath = inputLine.getLastArg();
        if (newPath.contains(SCHEME_DELIMITER)) {
            // its a VFS entry
            try {
                AbstractVFSFileProvider vfs = (AbstractVFSFileProvider) getState().getVFS(newPath);
                if (vfs == null) {
                    say("\"" + newPath + "\" is not a mounted VFS. Please mount it first.");
                    return RC_NO_OP;

                }
                if (!AbstractVFSFileProvider.isAbsolute(newPath)) {
                    say("\"" + newPath + "\" must not be relative");
                    return RC_NO_OP;
                }
                if (!vfs.canWrite()) {
                    say("sorry but you do not have permission to write to \"" + newPath + "\".");
                    return RC_NO_OP;
                }
                if (vfs.get(newPath, AbstractEvaluator.FILE_OP_AUTO) != null) {
                    say("sorry but \"" + newPath + "\" is a file.");
                    return RC_NO_OP;
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            File f = new File(newPath);
            if (!f.isAbsolute()) {
                say("\"" + f.getAbsolutePath() + "\" must not be relative");
                return RC_NO_OP;

            }
            if (f.isFile()) {
                say("sorry but \"" + f.getAbsolutePath() + "\" is a file.");
                return RC_NO_OP;
            }
            if (!f.canWrite()) {
                say("sorry but you do not have permission to write to \"" + f.getAbsolutePath() + "\".");
                return RC_NO_OP;
            }
            if (!f.exists()) {
                say("warning. \"" + f.getAbsolutePath() + "\" does not exist. You should create it before saving.");
            }
            // just in case the OS does something to the path (like there are embedded .. that it resolves).
            newPath = f.getAbsolutePath();
        }
        String oldPath = getBufferDefaultSavePath();
        bufferDefaultSavePath = newPath;
        say("new default buffer save path is '" + newPath + "', was '" + oldPath + "'");
        return RC_CONTINUE;
    }

    protected Object _doBufferCheck(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("check n {-st} {-src} -- check the syntax of buffer n");
            say("-st - will print whole stack track if present, otherwise just the error message");
            say("-src - if the buffer is a link, will syntax check the source. Ignored otherwise.");
            return RC_NO_OP;
        }
        boolean showStackTrace = inputLine.hasArg("-st");
        List<String> content;
        boolean useSource = inputLine.hasArg("-src");
        inputLine.removeSwitch("-src");
        inputLine.removeSwitch("-st");
        BufferManager.BufferRecord br = getBR(inputLine);
        if (br.hasContent()) {
            content = br.getContent();
        } else {
            try {
                if (br.isLink()) {
                    if (useSource) {
                        content = bufferManager.read(inputLine.getIntArg(FIRST_ARG_INDEX), useSource);
                    } else {
                        content = bufferManager.readFile(br.link);
                    }
                } else {
                    content = bufferManager.readFile(br.srcSavePath);
                }
            } catch (Throwable t) {
                say("sorry, could not read the file:" + t.getMessage());
                return RC_NO_OP;
            }
        }
        if (content.isEmpty()) {
            say("empty buffer");
            return RC_NO_OP;
        }
        content = stripShebang(content);

        StringBuffer stringBuffer = new StringBuffer();
        for (String x : content) {
            stringBuffer.append(x + "\n");
        }
        StringReader r = new StringReader(stringBuffer.toString());
        QDLParserDriver driver = new QDLParserDriver(new XProperties(), state.newCleanState());
        try {
            QDLRunner runner = new QDLRunner(driver.parse(r));
        } catch (ParseCancellationException pc) {
            if (showStackTrace) {
                pc.printStackTrace();
            }
            say("syntax error:" + pc.getMessage());
            return RC_CONTINUE;
        } catch (Throwable t) {
            if (showStackTrace) {
                t.printStackTrace();
            }
            say("there was a non-syntax error:" + t.getMessage());
            return RC_CONTINUE;
        }
        say("syntax ok");
        return RC_NO_OP;
    }

    protected Object _doBufferReset(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("reset");
            sayi("Drops ALL buffers and the handle resets to zero. Do this only if you have to.");
            return RC_NO_OP;
        }
        if (!"y".equals(readline("Are you SURE you want to delete all buffers and reset them?"))) {
            say("aborted.");
            return RC_NO_OP;
        }
        bufferManager = new BufferManager();
        say("buffers reset");
        return RC_CONTINUE;
    }

    boolean isSI = true;

    protected String BUFFER_RUN_I_MESSAGE_FLAG = "-i_msg";
    protected String BUFFER_RUN_CLONE_STATE_FLAG = "&";
    protected String BUFFER_RUN_CLEAN_STATE_FLAG = "!";


    protected Object _doBufferRun(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            int width = 12;
            say("run (handle | alias) [-go] [-i_msg on|off] [& | !]");
            say("run (handle | alias) " +
                    "["+ BUFFER_RUN_I_MESSAGE_FLAG + " on|off] " +
                    "[" + BUFFER_RUN_CLONE_STATE_FLAG + " | " + BUFFER_RUN_CLEAN_STATE_FLAG + "] " +
                    "["+ SI_INTERRUPT_GO + "] " +
                    "[" + SI_INTERRUPT_INCLUDE_SHORT + " >list | regex] [" + SI_INTERRUPT_EXCLUDE_SHORT + " >list | regex]");
            say("Run the given buffer. This will execute as if you had typed the contents ");
            say("in to the current session.");
            interruptHelpBlock(width);
            say(RJustify(BUFFER_RUN_I_MESSAGE_FLAG,width) + " - (on/off), turn off or on interrupt messages. Default is on.");
            say( RJustify(BUFFER_RUN_CLONE_STATE_FLAG,width) + " - clone the current workspace state and run. ");
            say(RJustify(BUFFER_RUN_CLEAN_STATE_FLAG,width) + " - create completely clean state and run ");
            say(getBlanks(width + 3) + "Note that VFS and script path are still set,");
            say("N.B. "+ BUFFER_RUN_CLONE_STATE_FLAG +  " and " + BUFFER_RUN_CLEAN_STATE_FLAG + " are mutually exclusive.");
            say("See the state indicator documentation for more");
            say(" Synonyms: ");
            say("   ) index|name  - start running a buffer. You must start a process before it can be suspended.");
            say("   )) pid -- resume a suspended process by process if (pid)");
            return RC_NO_OP;
        }
        boolean noInterrupts = inputLine.hasArg(SI_INTERRUPT_GO);
        inputLine.removeSwitch(SI_INTERRUPT_GO);
        if (inputLine.hasArg(BUFFER_RUN_I_MESSAGE_FLAG)) {
            Boolean iMsg = inputLine.getBooleanNextArgFor(BUFFER_RUN_I_MESSAGE_FLAG);
            if (iMsg != null) {
                siMessagesOn = iMsg;
            }
        }
        // whittle off interrupts
        SIInterruptList includes = getSIInterruptList(inputLine, SI_INTERRUPT_INCLUDE, SI_INTERRUPT_INCLUDE_SHORT);
        SIInterruptList excludes = getSIInterruptList(inputLine, SI_INTERRUPT_EXCLUDE, SI_INTERRUPT_EXCLUDE_SHORT);

        boolean gotIncludes = includes != null;
        boolean gotExcludes = excludes != null;
        SIInterrupts siInterrupts = null;
        if (gotIncludes || gotExcludes) {
            siInterrupts = new SIInterrupts();
            siInterrupts.setExclusions(excludes);
            siInterrupts.setInclusions(includes);
        }

        if (inputLine.getArgCount() == 0) {
            say("you must supply either a buffer index or name to run");
            return RC_NO_OP;
        }
        BufferManager.BufferRecord br = getBR(inputLine);
        List<String> content = null;

        if (br == null || br.deleted) {
            File f = new File(inputLine.getLastArg());
            if (f.exists() && f.isFile()) {
                try {
                    content = readFileAsLines(f.getCanonicalPath());
                } catch (Throwable throwable) {
                }
            }
            if (content == null || content.isEmpty()) {
                say("no such file or buffer");
                return RC_NO_OP;
            }
        } else {
            if (br.hasContent()) {
                content = br.getContent();
            } else {
                try {
                    if (br.isLink()) {
                        content = bufferManager.readFile(br.link);
                    } else {
                        content = bufferManager.readFile(br.srcSavePath);
                    }
                } catch (Throwable t) {
                    say("sorry, could not read the file:" + t.getMessage());
                    return RC_NO_OP;
                }
            }
        }
        // Lead shebang for scripts is removed at execution
        content = stripShebang(content);
        int flag = (inputLine.hasArg(BUFFER_RUN_CLONE_STATE_FLAG) ? 1 : 0) + (inputLine.hasArg(BUFFER_RUN_CLEAN_STATE_FLAG) ? 2 : 0);
        if (flag == 3) {
            say("sorry, you have specified both to clone the workspace and ignore it. You can only do one of these.");
            return RC_NO_OP;
        }

        boolean origEchoMode = getInterpreter().isEchoModeOn();
        QDLInterpreter interpreter = null;
        switch (flag) {
            case 0:
                interpreter = getInterpreter();
                break;
            case 1:
                interpreter = cloneInterpreter(inputLine);
                break;

            case 2:
                State newState = state.newCleanState();
                newState.setStateID(state.getStateID() + 1); // anything other than zero
                newState.setIoInterface(getIoInterface()); // Or IO fails
                interpreter = new QDLInterpreter(newState);
                interpreter.setPrettyPrint(isPrettyPrint());
                interpreter.setEchoModeOn(isEchoModeOn());
                break;
        }

        if (interpreter == null) {
            say("could not create debug instance.");
            return RC_NO_OP;
        }

        boolean ppOn = interpreter.isPrettyPrint();
        interpreter.setEchoModeOn(false);
        StringBuffer stringBuffer = new StringBuffer();
        for (String x : content) {
            stringBuffer.append(x + "\n");
        }
        try {
            interpreter.execute(stringBuffer.toString(), true, siInterrupts, noInterrupts);
            interpreter.setEchoModeOn(origEchoMode);
            interpreter.setPrettyPrint(ppOn);
        } catch (Throwable t) {
            interpreter.setEchoModeOn(origEchoMode);
            interpreter.setPrettyPrint(ppOn);
            if (t instanceof ReturnException) {
                ReturnException rx = (ReturnException) t;
                if (rx.hasResult()) {
                    say(rx.result.toString());
                }
                return RC_CONTINUE;
            }

            if (!isSI) {
                boolean isHalt = t instanceof InterruptException;
                if (isHalt) {
                    getState().getLogger().error("interrupt in main workspace " + stringBuffer, t);
                    say("sorry, you cannot halt the main workspace. Consider starting a separate process.");
                } else {
                    getState().getLogger().error("Could not interpret buffer " + stringBuffer, t);
                    say("sorry, but there was an error:" + ((t instanceof NullPointerException) ? "(no message)" : t.getMessage()));
                }
            } else {
                if (t instanceof InterruptException) {
                    if (SystemEvaluator.newInterruptHandler) {

                    } else {
                        InterruptException ie = (InterruptException) t;
                        InterruptUtil.createInterrupt(ie, siInterrupts);
                        InterruptUtil.printSetupMessage(this, ie);
                    }

                }
            }
        }
        return RC_CONTINUE;
    }

    private void interruptHelpBlock(int width) {
        say(RJustify(SI_INTERRUPT_GO, width) +  " - run with no interrupts.");
        say(RJustify(SI_INTERRUPT_INCLUDE_SHORT, width) +  " - run only interrupts with given labels. You can also use the long version");
        say(getBlanks(width + 3) + SI_INTERRUPT_INCLUDE);
        say(RJustify(SI_INTERRUPT_EXCLUDE_SHORT, width) +  " - run interrupts except for the given labels. You can also use the long version");
        say(getBlanks(width + 3) + SI_INTERRUPT_EXCLUDE);
        say(getBlanks(width + 3) + "Note that setting both results in the set includes := includes ?~ excludes");
    }

    private List<String> stripShebang(List<String> content) {
        if (!content.isEmpty()) {
            if (content.get(0).startsWith(SHEBANG)) {
                content.remove(0);
            }
        }
        return content;
    }

    QDLInterpreter defaultInterpreter;
    State defaultState;

    public static int getCurrentPID() {
        return currentPID;
    }

    public void setCurrentPID(int currentPID) {
        this.currentPID = currentPID;
    }

    public static final int DEFAULT_WORKSPACE_PID = 0;
    protected static int currentPID = DEFAULT_WORKSPACE_PID;

    /**
     * Save all of the buffers. This just invokes the save method since there is a lot of state to ferret out
     * and it is best to hand it off.
     *
     * @throws Throwable
     */
    protected void _saveAllBuffers() throws Throwable {
        ArrayList<BufferManager.BufferRecord> bufferRecords = getBufferManager().getBufferRecords();
        List<String> successes = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (BufferManager.BufferRecord br : bufferRecords) {
            if (br.edited) {
                if (!br.memoryOnly) {
                    InputLine inputLine = new InputLine(BUFFER2_COMMAND, "save", Integer.toString(getBufferManager().getIndex(br)));
                    try {
                        _doBufferWrite(inputLine, false);
                        successes.add(br.alias);
                    } catch (Throwable t) {
                        failures.add(br.alias);
                        // do nothing
                    }
                }
            }
        }
        if (!successes.isEmpty()) {
            say("saved " + successes.size() + " buffers:" + successes);
        }
        if (!failures.isEmpty()) {
            say("failed to save " + failures.size() + " buffers: " + failures);
        }
    }

    protected Object _doBufferWrite(InputLine inputLine) throws Throwable {
        return _doBufferWrite(inputLine, true);
    }

    /**
     * Used internally, This has a flag to suppress certain messages.
     *
     * @param inputLine
     * @param doOuput
     * @return
     * @throws Throwable
     */
    protected Object _doBufferWrite(InputLine inputLine, boolean doOuput) throws Throwable {
        if (_doHelp(inputLine)) {
            say("(write | save) (index | alias) {path}");
            sayi("Write (aka save) the buffer. If there is a link, the target is written to the source.");
            sayi("path - (memory only) convert buffer to a regular file, save it and use that.");
            sayi("   The path must be fully qualified.");
            return RC_NO_OP;
        }
        // If it is a link, br.link is read and written to br.src
        String path = "";
        if (inputLine.getArgCount() == 3) {
            path = inputLine.getLastArg();
            inputLine.removeArgAt(3);
        }
        BufferManager.BufferRecord br = getBR(inputLine);
        if (br == null || br.deleted) {
            say("buffer not found");
            return RC_NO_OP;
        }
        if (br.memoryOnly) {
            if (path.isEmpty()) {
                say("no save path given");
                return RC_NO_OP;
            }
            File f = new File(path);
            if (f.exists()) {
                say("sorry, '" + f.getAbsolutePath() + "' exists");
                return RC_NO_OP;
            }
            try {
                FileUtil.writeStringToFile(f.getAbsolutePath(), StringUtils.listToString(br.getContent()));
            } catch (Throwable e) {
                say("that didn't work: " + e.getMessage());
                return RC_NO_OP;
            }
            br.srcSavePath = f.getAbsolutePath();
            br.memoryOnly = false;
            br.edited = false; // it was just saved.
            return RC_CONTINUE;
        }
        if (!br.edited) {
            say("buffer not edited, nothing to save");
            return RC_NO_OP;
        }
        boolean ok = bufferManager.write(br);

        if (ok) {
            if (doOuput) say("done");
        } else {
            if (doOuput) say("nothing was found to write.");
        }
        return RC_CONTINUE;

    }

    public boolean isUseExternalEditor() {
        return useExternalEditor;
    }

    public void setUseExternalEditor(boolean useExternalEditor) {
        this.useExternalEditor = useExternalEditor;
    }

    public String getExternalEditorName() {
        return externalEditorName;
    }

    public void setExternalEditorName(String externalEditorName) {
        this.externalEditorName = externalEditorName;
    }

    boolean useExternalEditor = false;
    String externalEditorName = LINE_EDITOR_NAME; // default

    List<String> editorClipboard = new LinkedList<>();

    public static final String EDITOR_ADD = "add";
    public static final String EDITOR_USE = "use";
    public static final String EDITOR_LIST = "list";
    public static final String EDITOR_REMOVE = "rm";
    public static String EDITOR_CLEAR_SCREEN;

    protected Object _doEditor(InputLine inputLine) {
        if (!_doHelp(inputLine) && inputLine.getArgCount() == 0) {
            // no help specified, nothing else on input line
            // say("Sorry, please supply an argument (e.g. --help)");
            listEditors();
            return RC_CONTINUE;
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "help":
            case "--help":
                int width = 20;
                sayi("list - list available editors");
                sayi(RJustify(EDITOR_ADD + " name exec", width) + "  - add a (basic) editor configuration.");
                sayi(RJustify(EDITOR_LIST, width) + " - list all current editors.");
                sayi(RJustify(EDITOR_REMOVE + " name", width) + " - remove an editor: note it cannot be the currently active one");
                sayi(RJustify(EDITOR_USE + " name", width) + " - use this as the default. Implicitly enables using external editors if needed.");
                return RC_NO_OP;
            case EDITOR_ADD:
                return _doEditorAdd(inputLine);
            case EDITOR_USE:
                return _doEditorUse(inputLine);
            case EDITOR_REMOVE:
                return _doEditorRemove(inputLine);
            case EDITOR_LIST:
                listEditors();
                return RC_NO_OP;
            default:
                say("unrecognized command");
                return RC_CONTINUE;
        }

    }

    private int _doEditorRemove(InputLine inputLine) {
        String name = inputLine.getNextArgFor(EDITOR_REMOVE);
        if (isTrivial(name)) {
            say("no name specified.");
            return RC_NO_OP;
        }
        if (getQdlEditors().hasEntry(name)) {
            if (getExternalEditorName().equals(name)) {
                say("removing default editor, reverting to line editor");
                setExternalEditorName(LINE_EDITOR_NAME);
            }

            getQdlEditors().remove(name);
            say(name + " removed.");
        } else {
            say(name + " not found.");
        }
        return RC_CONTINUE;
    }

    private int _doEditorUse(InputLine inputLine) {
        String name = inputLine.getNextArgFor(EDITOR_USE);
        if (isTrivial(name)) {
            say("no name specified.");
            return RC_NO_OP;
        }
        setExternalEditorName(name);
        setUseExternalEditor(!name.equals(LINE_EDITOR_NAME));
        say("editor set to " + name);
        return RC_CONTINUE;
    }

    private int _doEditorAdd(InputLine inputLine) {
        if (inputLine.getArgCount() < 3) {
            say("Sorry, wrong number of arguments for " + EDITOR_ADD);
            return RC_NO_OP;
        }
        boolean isClearScreen = inputLine.hasArg(EDITOR_CLEAR_SCREEN);
        inputLine.removeSwitch(EDITOR_CLEAR_SCREEN);
        String name = inputLine.getNextArgFor(EDITOR_ADD);
        if (isTrivial(name)) {
            say("no name specified.");
            return RC_NO_OP;
        }
        inputLine.removeSwitchAndValue(EDITOR_ADD);

        if (getQdlEditors().hasEntry(name)) {
            boolean ok = readline("The editor named \"" + name + "\" already exists. Do you want to over write it (y/n)?").equals("y");
            if (!ok) {
                say("aborted.");
                return RC_NO_OP;
            }
        }
        // At this point all that should be left is the executable (and a list of arguments.)
        String exec = inputLine.getArg(1);


        EditorEntry ee = new EditorEntry();
        ee.name = name;
        ee.exec = exec;
        ee.clearScreen = isClearScreen;
        getQdlEditors().put(ee);
        say("added '" + name + "' with executable '" + exec + "' ");
        File file = new File(exec);
        if (!file.exists()) {
            say("warn: '" + exec + "' does not exist");
        }
        if (file.isDirectory()) {
            say("warn: '" + exec + "' is a directory");
        }
        if (!file.canRead()) {
            say("warn: you do not have permission to access '" + exec + "'");
        }

        return RC_CONTINUE;
    }

    protected Object _doBufferEdit(InputLine inputLine) throws Throwable {
        if (_doHelp(inputLine)) {
            say("edit (handle | alias)");
            sayi("invoke the editor on the given buffer");
            return RC_NO_OP;
        }
        if (inputLine.getArgCount() == 0) {
            say("you must supply either an buffer handle or command.");
            return RC_NO_OP;
        }

        BufferManager.BufferRecord br = getBR(inputLine);
        if (br == null || br.deleted) {
            say("Sorry. No such buffer");
            return RC_CONTINUE;
        }
        List<String> content;
        if (br.hasContent()) {
            content = br.getContent();
        } else {
            String fName = br.isLink() ? br.linkSavePath : br.srcSavePath;
            if (br.memoryOnly) {
                content = br.getContent();
            } else {
                try {
                    content = bufferManager.readFile(fName);
                } catch (FileNotFoundException fileNotFoundException) {
                    // ok. Means create the file
                    say("new file '" + fName + "'");
                    content = new ArrayList<>();
                }
            }
        }
        br.setContent(content);
        // so no buffer. There are a couple ways to get it.
        List<String> result = new ArrayList<>();
        Object rc = invokeEditor(br, result);
/*        if (rc instanceof Response) {
            return rc;  // Swing editor
        }
        if (rc.equals(RC_CONTINUE)) {
            return RC_CONTINUE;
        }
        */
        if (result.isEmpty()) {
            return RC_NO_OP;
        }
        br.setContent(result);
        br.edited = true;
        return RC_CONTINUE;
    }

    protected Object invokeEditor(BufferManager.BufferRecord br, List<String> result) {
        if (useExternalEditor()) {
            result.addAll(_doExternalEdit(br.getContent()));
            return RC_CONTINUE;
        } else {
            if (isSwingGUI()) {
                _doGUIEditor(br);
            } else {
                result.addAll(_doLineEditor(br.getContent()));
            }
            return RC_CONTINUE;
        }
    }

    private List<String> _doLineEditor(List<String> content) {
        LineEditor lineEditor = new LineEditor(content);
        lineEditor.setClipboard(editorClipboard);
        lineEditor.setIoInterface(getIoInterface());
        try {
            lineEditor.execute();
            return lineEditor.getBuffer(); // Just to be sure it is the same.
        } catch (Throwable t) {
            t.printStackTrace();
            say("Sorry, there was an issue editing this buffer.");
            getState().warn("Error editing buffer:" + t.getMessage() + " for exception " + t.getClass().getSimpleName());
        }
        return content;
    }

    File tempDir = null;

    protected File getTempDir() {
        if (tempDir == null) {
            if (rootDir != null) {
                tempDir = new File(rootDir, "temp");
                if (!tempDir.exists()) {
                    if (!tempDir.mkdir()) {
                        return null;
                    }
                }
            }
        }
        return tempDir;
    }

    private List<String> _doExternalEdit(File tempFile) {
        EditorEntry qdlEditor = getQdlEditors().get(getExternalEditorName());
        List<String> content = new ArrayList<>();

        int exitCode = EditorUtils.editFile(qdlEditor, tempFile);
        if (exitCode == EditorUtils.EDITOR_RC_OK) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(tempFile));
                String currentLine = br.readLine();

                while (currentLine != null) {
                    content.add(currentLine);
                    currentLine = br.readLine();
                }
                br.close();

            } catch (Throwable e) {
                if (DebugUtil.isEnabled()) {
                    e.printStackTrace();
                }
                say("There was an error reading the file:" + e.getMessage());
            }
        }
        return content;
    }

    private List<String> _doExternalEdit(List<String> content) {
        File tempFile;
        File tempDir = getTempDir();

        try {
            if (tempDir == null) {
                tempFile = File.createTempFile("edit", ".qdl");
            } else {
                tempFile = File.createTempFile("edit", ".qdl", tempDir);
            }
            tempFile.deleteOnExit();
            FileWriter fw = new FileWriter(tempFile);
            if (content == null) {
                fw.write(""); // create empty file
            } else {
                for (String x : content) {
                    fw.write(x + "\n");
                }
            }
            fw.flush();
            fw.close();
        } catch (IOException iox) {
            say("could not create the temp file:'" + iox.getMessage() + "'");
            if (isDebugOn()) {
                iox.printStackTrace();
            }
            return null;
        }
        return _doExternalEdit(tempFile);
    }

    protected BufferManager.BufferRecord getBR(InputLine inputLine) {
        String rawArg = null;
        if (inputLine.getCommand().equals(EXECUTE_COMMAND) || inputLine.getCommand().equals(EDIT_COMMAND)) {
            // Since this is a shorthand, the input line looks like
            // ) 2
            rawArg = inputLine.getArg(ACTION_INDEX);
        } else {
            if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
                return null;
            }
            rawArg = inputLine.getArg(FIRST_ARG_INDEX);
        }

        int index = -1;
        try {
            index = Integer.parseInt(rawArg);
            return bufferManager.getBufferRecord(index);

        } catch (NumberFormatException t) {
            // no problem, maybe they used its name
        } catch (ArrayIndexOutOfBoundsException ai) {
            return null;
        }
        return bufferManager.getBufferRecord(rawArg);

    }

    protected Object _doBufferDelete(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("(delete | rm) handle | alias)");
            sayi("removes the buffer. This does NOT touch the physical file in any way.");
            sayi("If there are edits pending, they will be lost, so write it first if need be.");
            return RC_NO_OP;
        }
        BufferManager.BufferRecord br = getBR(inputLine);
        if (br == null) {
            say("sorry, I didn't understand that");
            return RC_NO_OP;
        }
        if (br.hasContent()) {
            if (!"y".equalsIgnoreCase(readline("buffer has not been saved. Do you still want to remove it?[y/n]"))) {
                say("aborted.");
                return RC_NO_OP;
            }
        }
        bufferManager.remove(br.src);

        return RC_CONTINUE;
    }

    protected Object _fileDir(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("(dir | ls) path");
            sayi("List contents of the directory. Note if this is a single file, nothing will be listed.");
            return RC_NO_OP;
        }

        Polyad request = new Polyad(IOEvaluator.DIR);
        try {
            request.addArgument(new ConstantNode(asQDLValue(inputLine.getArg(FIRST_ARG_INDEX))));
        } catch (Throwable t) {
            say("sorry. I didn't understand that.");
            return RC_CONTINUE;
        }
        getState().getMetaEvaluator().evaluate(request, getState());
        QDLValue result = request.getResult();
        int i = 0;
        if (result.isStem()) {
            QDLStem stemVariable = result.asStem();
            for (QDLKey key : stemVariable.keySet()) {
                i++;
                say(stemVariable.get(key).toString());
            }
            say(i + " entries");
        } else {
        }
        return RC_CONTINUE;
    }

    protected Object _fileMkDir(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("mkdir path");
            sayi("Make a directory in the file system. This creates all intermediate paths too if needed.");
            return RC_NO_OP;
        }

        try {
            String raw = IOEvaluator.MKDIR + "('" + inputLine.getArg(FIRST_ARG_INDEX) + "');";
            getInterpreter().execute(raw);
        } catch (Throwable throwable) {
            say("Error" + (throwable instanceof NullPointerException ? "." : ":" + throwable.getMessage()));
        }
        return RC_CONTINUE;

    }

    protected Object _fileRmDir(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("rmdir path");
            sayi("Removes the given path (but not the intermediate paths.)");
            return RC_NO_OP;
        }

        try {
            String raw = IOEvaluator.RMDIR + "('" + inputLine.getArg(FIRST_ARG_INDEX) + "');";
            getInterpreter().execute(raw);
        } catch (Throwable throwable) {
            say("Error" + (throwable instanceof NullPointerException ? "." : ":" + throwable.getMessage()));
        }
        return RC_CONTINUE;

    }

    protected Object _fileDelete(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("(delete | rm) filename");
            sayi("Delete the file.");
            return RC_NO_OP;
        }

        try {
            String raw = IOEvaluator.RM_FILE + "('" + inputLine.getArg(FIRST_ARG_INDEX) + "');";
            getInterpreter().execute(raw);
        } catch (Throwable throwable) {
            say("Error" + (throwable instanceof NullPointerException ? "." : ":" + throwable.getMessage()));
        }
        return RC_CONTINUE;
    }

    /**
     * Copies <i>any</i> two files on the system including between VFS.
     */

    protected Object _fileCopy(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("copy source target [-binary]");
            sayi("Copy a file from the source to the target. Note that the workspace is VFS aware.");
            sayi("-binary = treat the files as binary.");
            return RC_NO_OP;
        }
        boolean isBinary = inputLine.hasArg("-binary");
        inputLine.removeSwitch("-binary");
        String source = inputLine.getArg(FIRST_ARG_INDEX);
        String target = inputLine.getArg(FIRST_ARG_INDEX + 1);
        return _fileCopy(source, target, isBinary);
    }

    protected Object _fileCopy(String source, String target, boolean isBinary) {
        try {
            QDLFileUtil.copy(getState(), source, target);
        } catch (Throwable throwable) {
            say("Sorry, I couldn't do that: " + throwable.getMessage());
        }
        return RC_CONTINUE;
    }

    protected Object _doBufferList(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("ls | list");
            sayi("list all buffers with information about them.");
            return RC_NO_OP;

        }
        int count = 0;
        for (int i = 0; i < bufferManager.getBufferRecords().size(); i++) {
            BufferManager.BufferRecord br = bufferManager.getBufferRecords().get(i);
            if (!br.deleted) {
                count++;
                String x = formatBufferRecord(i, bufferManager.getBufferRecords().get(i));
                say(x);
            }
        }
        say("there are " + count + " active buffers.");
        return RC_CONTINUE;
    }

    // q. := ws_macro([')b create temp -m',')b create foo /tmp/foo.qdl',')b link f2 /tmp/foo2.qdl /tmp/lll.qdl',')b'])
    protected Object _doBufferShow(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("show (handle | alias) {-src}");
            sayi("Show the buffer. If the buffer is linked, it will show the target.");
            sayi("-src will only have an effect with links and will show the source rather than the target.");
            return RC_NO_OP;
        }
        BufferManager.BufferRecord br = null;
        try {
            br = getBR(inputLine);
            if (br == null || br.deleted) {
                say("buffer index not found");
                return RC_NO_OP;
            }
        } catch (Throwable t) {
            say("I can't find that buffer. Sorry.");
            return RC_NO_OP;

        }
        if (br.hasContent()) {
            for (String x : br.getContent()) {
                say(x);
            }
            return RC_CONTINUE;
        }
        // So nothing in the buffer. So the file.
        List<String> lines = null;
        try {
            if (br.isLink()) {
                // If this is a link but the user really wants to see the source, they need to supply a flag
                lines = bufferManager.read(inputLine.getIntArg(FIRST_ARG_INDEX), inputLine.hasArg("-src"));
            } else {
                lines = bufferManager.read(inputLine.getIntArg(FIRST_ARG_INDEX), true);
            }

        } catch (Throwable t) {
            say("error reading buffer");
            return RC_NO_OP;
        }
        if (lines.isEmpty()) {
            say("");

        } else {
            for (String x : lines) {
                say(x);
            }
        }
        return RC_CONTINUE;
    }

    public String BUFFER_RECORD_SEPARATOR = "|";

    protected String formatBufferRecord(int ndx, BufferManager.BufferRecord br) {
        // so a ? is shown if its a link, a * if its a buffer that hasn't been saved.
        String saved = " ";
        if (br.memoryOnly) {
            saved = "m";
        } else {
            if (br.isLink()) {
                saved = "?";
            } else {
                if (br.hasContent()) {
                    saved = "*";
                }

            }
        }

        String a = BUFFER_RECORD_SEPARATOR + saved + BUFFER_RECORD_SEPARATOR;
        return ndx + a + br;
    }

    protected Object _doBufferLink(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("link alias source target [-copy]");
            sayi("Creates a link (for external editing) from source to target. Source must exist.");
            sayi("A common pattern is that source is on a VFS and target is a local file that you edit");
            sayi("Generally this is only needed if for some reason your editor cannot be configured to");
            sayi("work as an external editor.");
            sayi("If the -copy flag is used, target will be overwritten. In subsequent commands, e.g. run, save this will resolve the link.");
            return RC_NO_OP;
        }
        boolean doCopy = inputLine.hasArg("-copy");
        inputLine.removeSwitch("-copy");
        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("sorry, missing arguments.");
            return RC_CONTINUE;
        }
        String alias = inputLine.getArg(FIRST_ARG_INDEX);
        String source = inputLine.getArg(FIRST_ARG_INDEX + 1);
        if (bufferManager.hasBR(source)) {
            say("sorry but a buffer for " + source + " already exists.");
            return RC_NO_OP;
        }
        String target = null;
        if (inputLine.hasArgAt(FIRST_ARG_INDEX + 2)) {
            target = inputLine.getArg(FIRST_ARG_INDEX + 2);
        }
        int ndx = bufferManager.link(alias, source, target);

        BufferManager.BufferRecord br = bufferManager.getBufferRecord(ndx);
        File sourceFile = new File(source);
        File targetFile = new File(target);
        if (sourceFile.isAbsolute()) {
            br.srcSavePath = source;
            ;
        } else {
            br.srcSavePath = bufferManager.figureOutSavePath(getBufferDefaultSavePath(), br.src);
        }
        if (targetFile.isAbsolute()) {
            br.linkSavePath = target;
        } else {
            br.linkSavePath = bufferManager.figureOutSavePath(getBufferDefaultSavePath(), br.link);
        }
        say(formatBufferRecord(ndx, br));

        if (doCopy) {
            try {
                _fileCopy(br.srcSavePath, br.linkSavePath, false);
                say(source + " copied to " + target);
            } catch (Throwable t) {
                say("could not copy " + source + "to " + target);
            }
        }
        return RC_CONTINUE;

    }

    /**
     * Boolean values function that returns true if the inputline has some form of help in it.
     *
     * @param inputLine
     * @return
     */
    protected boolean _doHelp(InputLine inputLine) {
        if (inputLine.hasArg("-help") || inputLine.hasArg("--help")) return true;
        return false;
    }

    protected static final String BUFFER_IN_MEMORY_ONLY_SWITCH = "-m";

    protected Object _doBufferCreate(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("create alias {path | " + BUFFER_IN_MEMORY_ONLY_SWITCH + "}");
            sayi("create a new buffer for the path and give it the local name of alias.");
            sayi(BUFFER_IN_MEMORY_ONLY_SWITCH + " (optional) set this to be an in-memory only buffer.");
            sayi("E.g.\n");
            sayi("   )b create foo\n");
            sayi("This will create a buffer with alias of foo and assign it a path");
            sayi("relative to the buffer save path. If the path has not been set (use )b path) then");
            sayi("an error will result.");
            sayi("\nE.g.\n");
            sayi("   )b create foo -m\n");
            sayi("This will create an in memory buffer that is saved as part of the workspace.");
            sayi("\nE.g.\n");
            sayi("    )b create foo /path/to/foo.qdl\n");
            sayi("This will create the buffer with alias 'foo'. Note that this is VFS aware so the path");
            sayi("may point to a file in the VFS. If the VFS is read-only, you will need to link to it");
            sayi("For any editing you may want to do (and read only means the original cannot be modified.");
            return RC_CONTINUE;
        }

        boolean inMemoryOnly = inputLine.hasArg(BUFFER_IN_MEMORY_ONLY_SWITCH);
        inputLine.removeSwitch(BUFFER_IN_MEMORY_ONLY_SWITCH);
        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("sorry, you must supply a file name.");
            return RC_CONTINUE;
        }
        String alias = inputLine.getArg(FIRST_ARG_INDEX);
        String source = null;

        if (FIRST_ARG_INDEX + 2 <= inputLine.size()) { // +2 because there is the ) command and the action,
            source = inputLine.getArg(FIRST_ARG_INDEX + 1);
        }
        boolean hasSource = source != null;
        if (FIRST_ARG_INDEX + 3 <= inputLine.size()) { // +2 because there is the ) command and the action,
            say("warning: Additional arguments detected. These are ignored");
        }

        if (bufferManager.hasBR(alias)) {
            say("sorry but a buffer for " + alias + " already exists.");
            return RC_NO_OP;
        }
        if (!hasSource) {
            source = alias;
        }
/*
        File sourceFile = null;
        File aliasFile = new File(alias);
        if (hasSource) {
            sourceFile = new File(source);
        } else {
            sourceFile = aliasFile;
        }
*/
        if ((!isVFSPath(source)) && getState().isServerMode()) {
            say("warning:" + source + " is not VFS file, so operations may fail");
        }
        if (!isAbsolute(source)) {
            // If they created a relative buffer, be sure there is a buffer record to create or you will get a dud one
            // and have to delete it later.
            if (!inMemoryOnly && getTempDir() == null) {
                say("You must set the buffer save path to create a relative file for the buffer '" + alias + "'");
                return RC_NO_OP;
            }
        }

        int ndx = bufferManager.create(alias);
        BufferManager.BufferRecord br = bufferManager.getBufferRecord(ndx);
        br.memoryOnly = inMemoryOnly;
        br.src = br.alias; // backwards compatibility
        if (!inMemoryOnly) {
            if (isAbsolute(source)) {
                br.srcSavePath = source;
            } else {
                br.srcSavePath = bufferManager.figureOutSavePath(getBufferDefaultSavePath(), br.src);
            }
        }
        say(formatBufferRecord(ndx, br));
        return RC_CONTINUE;
    }


    /**
     * This has several states:
     * <ul>
     *     <li>clear (no args) -- clear the environment</li>
     *     <li>drop name -- remove a variable</li>
     *     <li>get name - show the value of a variable</li>
     *     <li>list (no args) - print the entire environment</li>
     *     <li>load filename - add the variables in the file to the current state</li>
     *     <li>save [file] - save the environment to a file. No arg means to use the current env file.</li>
     *     <li>set key value - set a variable to a given value.</li>
     *     <li></li>
     * </ul>
     *
     * @param inputLine
     * @return
     */
    protected Object doEnvCommand(InputLine inputLine) {
        if (!inputLine.hasArgAt(ACTION_INDEX)) {
            return _envList(inputLine);
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "help":
            case "--help":
                say("Environment commands");
                sayi("        clear - remove all entries in the current environment.");
                sayi("     drop var - remove the variable.");
                sayi("      get var - show the value of the variable");
                sayi("         list - show the entire contents of the environment");
                sayi("         name - list the name (i.e. file path) of the currently loaded environment if there is one.");
                sayi("    load file - load a saved environment from a file");
                sayi("    save file - save the entire current environment to the file");
                sayi("set var value - set the given variable to the given value");
                sayi("Remember that the environment refers to a collection of key/value pairs you may");
                sayi("use as a pre-processor to commands and QDL expressions. See the Workspace reference");
                say("manual section on 'Templates, pre-processing and variables'.");
                return RC_NO_OP;
            case "clear":
                if (_doHelp(inputLine)) {
                    say("clear");
                    sayi("Clear all entries in the environment");
                    return RC_NO_OP;
                }

                env = new XProperties();
                say("Environment cleared.");
                return RC_CONTINUE;
            case "drop":
                return _envDrop(inputLine);
            case "get":
                return _envGet(inputLine);
            case "list":
                return _envList(inputLine);
            case "load":
                return _envLoad(inputLine);
            case "save":
                return _envSave(inputLine);
            case "set":
                return _envSet(inputLine);
            case "name":
                if (_doHelp(inputLine)) {
                    say("name");
                    sayi("list the file path and name (if any) of the current environment.");
                    return RC_NO_OP;
                }

                if (envFile == null) {
                    say("No environment file has been set.");
                } else {
                    say(envFile.getAbsolutePath());
                }
                return RC_CONTINUE;
            default:
                say("Unknown environment command.");
                return RC_CONTINUE;

        }
    }

    protected Object _envSave(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("save [filename]");
            sayi("Saves the environment. If there is a default file it will save to that.");
            sayi("Specifying the name forces the save to that file.");
            return RC_NO_OP;
        }

        File currentFile = envFile;
        if (inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            currentFile = new File(inputLine.getArg(FIRST_ARG_INDEX));
        }

        try {
            FileWriter fileWriter = new FileWriter(currentFile);
            String message = "Environment saved to \"" + currentFile.getAbsolutePath() + "\" at " + Iso8601.date2String(new Date());
            env.store(fileWriter, message);
            say(message);
        } catch (Throwable t) {
            say("Saving the environment to \"" + currentFile.getAbsolutePath() + "\" failed:" + t.getMessage());
        }

        return RC_CONTINUE;
    }

    protected static final String LOAD_ENV_AS_INPUT_FORM = "-input_form";

    protected Object _envLoad(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("load [" + LOAD_ENV_AS_INPUT_FORM + "] (" + QDL_DUMP_FLAG + " | " + JSON_FLAG + ") file ");
            sayi("Load the given file as the current environment. This adds to the current environment");
            sayi(LOAD_ENV_AS_INPUT_FORM + " if present for a stem will convert every value to its input form.");
            sayi("Otherwise, the values are given in their string representation.");
            sayi(QDL_DUMP_FLAG + " - if present, interpret the file as QDL. ");
            sayi(JSON_FLAG + " - if present, interpret the file as JSON. ");
            sayi("\nTip: If using QDL, it is interpreted like any other script, so it is goof practice");
            say("        to return the value you want to use.");
            return RC_NO_OP;
        }

        // load a file
        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("Sorry but you must specify a file to load it");
            return RC_CONTINUE;
        }
        boolean toInputForm = inputLine.hasArg(LOAD_ENV_AS_INPUT_FORM);
        inputLine.removeSwitch(LOAD_ENV_AS_INPUT_FORM);
        boolean loadAsQDL = inputLine.hasArg(QDL_DUMP_FLAG);
        inputLine.removeSwitch(QDL_DUMP_FLAG);
        boolean loadAsJSON = inputLine.hasArg(JSON_FLAG);
        inputLine.removeSwitch(JSON_FLAG);
        String filePath = inputLine.getLastArg();
        String realFilePath = filePath;
        if (!getState().isVFSFile(filePath)) {
            File f = resolveAgainstRoot(filePath);
            realFilePath = f.getAbsolutePath();
        }
        String content = null;
        try {
            content = QDLFileUtil.readTextFile(getState(), realFilePath);
        } catch (Throwable e) {
            if (isDebugOn()) {
                e.printStackTrace();
            }
            say("uh-oh, that didn't work:" + e.getMessage());
            return RC_NO_OP;
        }

// so at this point it is an absolute path
/*
        if(getState().isVFSFile(filePath)){
          QDLFileUtil.readTextFileAsLines(getState(), filePath);
        }else{
            File f = resolveAgainstRoot(filePath);
            if (!f.exists()) {
                say("Sorry, but +\"" + f.getAbsolutePath() + "\" does not exist.");
                return RC_CONTINUE;
            }
            if (!f.isFile()) {
                say("Sorry, but +\"" + f.getAbsolutePath() + "\" is not a file.");
                return RC_CONTINUE;
            }
            if (!f.canRead()) {
                say("Sorry, but +\"" + f.getAbsolutePath() + "\" is not readable.");
                return RC_CONTINUE;
            }


        }
*/
        boolean done = false;
        if (loadAsJSON || realFilePath.endsWith(".json")) {
            try {
                JSONObject jsonObject = JSONObject.fromObject(content);
                if (env == null) {
                    env = new XProperties();
                    env.add(jsonObject, true);
                }
                done = true;
            } catch (Throwable e) {
                say("tried to process as JSON failed.");
            }
        }

        if (loadAsQDL || realFilePath.endsWith(".qdl") && !done) {
            try {
                QDLInterpreter qi = new QDLInterpreter(getState().newLocalState());
                QDLRunner runner = qi.execute(content);
                Object obj = runner.getLastResult();
                if (obj != null) {
                    QDLStem qdlStem = null;
                    if (obj instanceof QDLValue) {
                        QDLValue qdlValue = (QDLValue) obj;
                        if (qdlValue.isStem()) {
                            qdlStem = qdlValue.asStem();
                        }
                    } else {
                        if (obj instanceof QDLStem) {
                            qdlStem = (QDLStem) obj;
                        }
                    }
                    if (qdlStem == null) {
                        say("QDL does not evaluate to  a stem. Cannot load environment.");
                        return RC_NO_OP;
                    }
                    if (toInputForm) {
                        for (QDLKey key : qdlStem.keySet()) {
                            env.put(key.toString(), InputFormUtil.inputForm(qdlStem.get(key)));
                        }
                    } else {
                        env.add(qdlStem, true);
                    }
                    done = true;
                }
            } catch (Throwable e) {
                if (e instanceof ReturnException) {
                    ReturnException re = (ReturnException) e;
                    if (re.hasResult()) {
                        QDLValue qdlValue = (QDLValue) re.result;
                        if (qdlValue.isStem()) {
                            QDLStem stemIn = qdlValue.asStem();
                            if(toInputForm){
                               for(QDLKey key : stemIn.keySet()){
                                   env.put(key.toString(), InputFormUtil.inputForm(stemIn.get(key)));
                               }
                            }else {
                                env.add(qdlValue.asStem(), true);
                            }
                            done = true;
                        }
                    }
                } else {
                    say("Unexpected error: " + e.getMessage());
                }
                if (isDebugOn()) {
                    e.printStackTrace();
                }
            }
        }
        if (!done) {
            StringReader stringReader = new StringReader(content);
            try {
                env.load(stringReader);
            } catch (IOException e) {
                if (isDebugOn()) {
                    e.printStackTrace();
                }
                say("uh-oh, that didn't work:" + e.getMessage());
                return RC_NO_OP;
            }
        }

        say(realFilePath + " loaded.");
        return RC_CONTINUE;
    }

    protected Object _envDrop(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("drop variable");
            say("Drop i.e. remove the named variable from the environment.");
            return RC_NO_OP;
        }

        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("Sorry, you must supply an environment variable name to remove it.");
            return RC_CONTINUE;
        }
        String pName = inputLine.getArg(FIRST_ARG_INDEX);
        env.remove(pName);
        say(pName + " rempved from the environment");
        return RC_CONTINUE;
    }

    protected Object _envGet(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("get variable");
            sayi("get the value for the given variable,");
            return RC_NO_OP;
        }

        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("Sorry, you must supply an environment variable name to get its value.");
            return RC_CONTINUE;
        }
        String pName = inputLine.getArg(FIRST_ARG_INDEX);
        say(env.getString(pName));
        return RC_CONTINUE;
    }

    protected Object _envSet(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("set variable value");
            sayi("sets the variable to the given value. Caution, only string values are " +
                    "allowed and if there are embedded blanks, surround it with double quotes");
            sayi("e.g.");
            sayi("set my_var \"mairzy doats\"");
            return RC_NO_OP;
        }

        String pName = inputLine.getArg(FIRST_ARG_INDEX);
        if (!inputLine.hasArgAt(1 + FIRST_ARG_INDEX)) {
            say("Sorry, no value supplied.");
            return RC_CONTINUE;
        }
        StringBuffer value = new StringBuffer();
        // REMEMBER that the getArgCount is the number of arguments and the 0th element is the command
        boolean isFirstPass = true;
        for (int i = FIRST_ARG_INDEX + 1; i < inputLine.getArgCount() + 1; i++) {
            if (isFirstPass) {
                value.append(inputLine.getArg(i));
                isFirstPass = false;
            } else {
                value.append(" " + inputLine.getArg(i));
            }
        }
        if (env == null) {
            env = new XProperties();
        }
        env.put(pName, value.toString());
        return RC_CONTINUE;
    }

    protected Object _envList(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("list");
            sayi("List all the variables and their values in the current environment.");
            return RC_NO_OP;
        }

        if (env == null || env.isEmpty()) {
            say("(empty)");
        } else {
            say("Current environment variables:");
            say(env.toString(1));
        }

        return RC_CONTINUE;
    }


    /**
     * Optional list argument. Print out the current modules the system knows about
     *
     * @param inputLine
     * @return
     */
    protected Object doModulesCommand(InputLine inputLine) {
        if (inputLine.hasArg(HELP_SWITCH)) {
            say("Modules commands:");
            sayi("     (no arg) - Same as list.");
            sayi("         list - list all loaded modules and their aliases. Default is to list modules.");
            sayi("(uri | alias) - list all full information for that module, including any documentation.");
            say("");
            say("Modules may have an optional preferred alias as part of their definition. If so you can  ");
            say("use that. However, there is no promise such aliases are unique. The uri, on the other hand,");
            say("is always unique.");
            sayi("\nIf the module was imported using the deprecated " + MODULE_IMPORT + " command, then");
            say("you may  use any of the aliases you specified, otherwise you need the uri");
            return RC_NO_OP;
        }
        if (inputLine.hasArg("-help")) {
            inputLine.removeSwitch("-help");
            _moduleHelp(inputLine);
            return RC_CONTINUE;
        }
        return _modulesList(inputLine);
    }

    /**
     * Contract is that the argument(s) are either URIs for a module or aliases. Print out the complete
     * help for each
     *
     * @param inputLine
     */
    private void _moduleHelp(InputLine inputLine) {
        for (int i = ACTION_INDEX; i < inputLine.getArgCount() + 1; i++) {

            String arg = inputLine.getArg(i);
            Module module = null;
            String importedString = "";
            if (arg.endsWith(State.NS_DELIMITER)) {
                arg = arg.substring(0, arg.length() - 1);
            }
            if (StringUtils.isTrivial(arg)) {
                // They are asking for documentation for the default module, and there is none.
            } else {
                XKey xKey = new XKey(arg);
                if (state.getMInstances().containsKey(xKey)) {
                    module = state.getMInstances().getModule(xKey);
                    if (module != null) {
                        // pull it off the template.
                        module = state.getMTemplates().getModule(new MTKey(module.getNamespace()));
                    }
                }
            }
            if (module == null) {
                try {
                    URI uri = URI.create(arg);
                    importedString = getImportString(uri);
                    module = state.getMTemplates().getModule(new MTKey(uri));

                } catch (Throwable t) {
                }

            }
            if (module != null) {
                say(importedString);
                List<String> docs = module.getListByTag();
                for (String x : docs) {
                    say(x);
                }
                if (1 < inputLine.getArgCount()) {
                    say("");
                } // spacer
            }
        }
    }

    protected Object _moduleImports(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("(Old import system -- now you can just set a variable for the module.)");
            sayi("A table of imported modules and their aliases. ");
            sayi("You must either directly create a module with QDL, or load from an external source ");
            say("using " + SystemEvaluator.MODULE_LOAD + " to make QDL aware of it before importing it");
            return RC_NO_OP;
        }

        if (state.getMInstances().isEmpty()) {
            say("(no imports)");
            return RC_CONTINUE;
        }
        TreeSet<String> aliases = new TreeSet<>();
        for (Object obj : state.getMInstances().keySet()) {
            Module module = state.getMInstances().getModule((XKey) obj);
            aliases.add(obj + "->" + state.getMInstances().getAliasesAsString(module.getMTKey()));
        }
        return printList(inputLine, aliases);
    }

    protected Object _modulesList(InputLine inputLine) {
        TreeSet<String> m = new TreeSet<>();
        if (getState().getMTemplates().keySet() == null || getState().getMTemplates().keySet().isEmpty()) {
            say("(no imported modules)");
            return RC_CONTINUE;
        }
        for (Object key : getState().getMTemplates().keySet()) {
            String out = getImportString(((MTKey) key).getUriKey());
            m.add(out);
        }
        // so these are sorted. Print them
        //return printList(inputLine, m);
        for (String x : m) {
            say(x);
        }
        return RC_CONTINUE;
    }

    /**
     * For a given URI, make the entry that is listed for the )modules -list command.
     *
     * @param key
     * @return
     */
    private String getImportString(URI key) {
        String out = key.toString();
        if (!state.getMInstances().isEmpty()) {
            out = out + " " + state.getMInstances().getAliasesAsString(new MTKey(key));
        } else {
            out = out + " -";
        }
        return out;
    }

    /**
     * Modes are
     * <ul>
     *     <li>[list] - list all of the variables</li>
     *     <li>help [name argCount] - no arguments means list all, otherwise, find the function with the signature.</li>
     *     <li>drop name - remove a local function. This does not remove a function from a module.</li>
     * </ul>
     *
     * @param inputLine
     * @return
     */
    protected Object doFuncs(InputLine inputLine) {
        if (!_doHelp(inputLine) && inputLine.getArgCount() == 0) {
            return _funcsList(inputLine);
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "--help":
                say("Function commands:");
                sayi("   (no arg) - Same as list");
                sayi("  drop name - Remove the (user defined) function from the system");
                sayi("edit {args} - edit the function with args (an integer) arguments");
                sayi("              No arg means it defaults to 0.");
                sayi("              You can, of course, redefine the function as you wish.");
                sayi("       help - display  ");
                sayi("              (1) help for a single function, given its name and arg count");
                sayi("              (2) short help for all functions with a given name");
                sayi("              Fully qualified names are allowed.");
                sayi("       list - list all known user-defined functions. Allows display options.");
                sayi("              Fully qualified names are supported as an argument.");
                sayi("              E.g.");
                sayi("                 )funcs list foo");
                sayi("              lists all of the functions in the imported module foo");
                sayi("     system - list all known system functions. Allows display options.");
                sayi("    -system - same as system.");
                return RC_NO_OP;
            case "drop":
                return _funcsDrop(inputLine);
            case "help":
                return _funcsHelp(inputLine);
            case "list":
                return _funcsList(inputLine);
            case "system":
            case "-system":
                return _funcsListSystem(inputLine);
            case "edit":
                return _doFuncEdit(inputLine);
            default:
                //     say("sorry, unrecognized command.");
                return _funcsList(inputLine);

        }
        //return RC_CONTINUE;
    }

    protected Object _doFuncEdit(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("edit {arg_count} - edit the function with arg_count arguments");
            say("arg_count - a zero or positive integer. If omitted, the default is 0");
            say("You may use this to define functions as well. Since the function");
            say("definition is re-interpreted on editor exit, you can change the signature");
            say("such as adding/removing arguments or even rename the function. All this does");
            say("is tell the editor where to start editing.");
            return RC_NO_OP;
        }
        String fName = inputLine.getArg(inputLine.getArgCount() - 1);
        int argCount = 0;
        try {
            argCount = Integer.parseInt(inputLine.getLastArg());
        } catch (Throwable t) {
//            say("Could not parse argument count of \"" + inputLine.getLastArg() + "\"");
//            return RC_NO_OP;
        }
        FR_WithState fr_withState = null;
        try {
            fr_withState = getState().resolveFunction(fName, argCount, true);
            if (fr_withState.isExternalModule) {
                say("cannot edit external functions.");
                return RC_NO_OP;
            }

        } catch (UndefinedFunctionException ufx) {
            // ok, they are defining it now
        }


        String inputForm = fr_withState == null ? "" : InputFormUtil.inputForm(fName, argCount, getState());

        if (isTrivial(inputForm)) {
            say("new function '" + fName + "'");
            StringBuilder argList = new StringBuilder();
            argList.append("(");
            boolean isFirst = true;
            for (int i = 0; i < argCount; i++) {
                if (isFirst) {
                    isFirst = false;
                    argList.append("x").append(i);
                } else {
                    argList.append(", x").append(i);
                }
            }
            argList.append(")");
            inputForm = fName + argList + "->null;";
//            return RC_NO_OP;
        }
        List<String> f = StringUtils.stringToList(inputForm);
        List<String> output = new ArrayList<>();
        Object rc = editFunction(f, output, fName, argCount);
        if (rc instanceof Response) {
            return rc;
        }
        if (rc.equals(RC_CONTINUE)) {
            return RC_CONTINUE;
        }
        return restoreFunction(output, fName, argCount);
    }

    public Object editFunction(List<String> inputForm, List<String> output, String fName, int argCount) {
        // return value is the return code or response, so pass in output as argument.
        if (useExternalEditor()) {
            inputForm = _doExternalEdit(inputForm);
        } else {
            if (isSwingGUI()) {
                // Fix https://github.com/ncsa/qdl/issues/71
                QDLEditor qdlEditor = new QDLEditor(this, fName, 0);
                qdlEditor.setWorkspaceCommands(this); // or callback fails
                qdlEditor.setType(EditDoneEvent.TYPE_FUNCTION);
                qdlEditor.setLocalName(fName);
                qdlEditor.setArgState(argCount);
                qdlEditor.setup(StringUtils.listToString(inputForm));
                return RC_CONTINUE;
            }
            inputForm = _doLineEditor(inputForm);
        }
        output.addAll(inputForm);
        return RC_NO_OP;
    }

    public Object restoreFunction(List<String> inputForm, String fName, int argCount) {
        try {
            getInterpreter().execute(inputForm);
            // Might not need these next two statements after revisions of FStack?
            FR_WithState fr_withState = getState().resolveFunction(fName, argCount, true); // get it again because it was overwritten
            fr_withState.functionRecord.setSourceCode(inputForm); // update source in the record.
        } catch (Throwable t) {
            if (DebugUtil.isEnabled()) {
                t.printStackTrace();
            }

            say("could not interpret function:" + t.getMessage());
        }
        return RC_CONTINUE;
    }

    protected Object _funcsDrop(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("drop fname [arg_count]");
            sayi("Removes the function from the current workspace. Note this applies to user-defined functions, not imported functions.");
            sayi("If you supply no arg_count, then *all* definitions of the functions are removed.");
            return RC_NO_OP;
        }

        String fName = inputLine.getArg(FIRST_ARG_INDEX);

        int argCount = -1;
        if (inputLine.getArgCount() == 3) {
            String rawCount = inputLine.getArg(FIRST_ARG_INDEX + 1);
            try {
                argCount = Integer.parseInt(rawCount);
            } catch (Throwable t) {
                say("sorry, but \"" + rawCount + "\" is not a number. Aborting...");
                return RC_NO_OP;
            }
        }

        getState().getFTStack().remove(new FKey(fName, argCount));
        if (getState().getFTStack().containsKey(new FKey(fName, -1))) {
            say(fName + " removed.");
        } else {
            say("Could not remove " + fName);
        }
        return RC_CONTINUE;
    }

    protected Object _funcsHelp(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("help [fname arg_count] [-r regex]");
            sayi("List help for functions.");
            sayi("help (no argument) - print off the first line of the embedded help.");
            sayi("help fname - print help for the given name ");
            sayi("help fname arg_count - print the complete embedded help for the function with the given argument count.");
            sayi("If the regex is included, apply that to the results per line.");
            return RC_NO_OP;
        }

        if (!inputLine.hasArgAt(FIRST_ARG_INDEX) || inputLine.getArg(FIRST_ARG_INDEX).startsWith("-")) {
            // so they entered )funcs help Print off first lines of help
            TreeSet<String> treeSet = new TreeSet<>();
            treeSet.addAll(getState().listAllDocumentation());
            return printList(inputLine, treeSet);
        }
        String fName = inputLine.getArg(FIRST_ARG_INDEX);

        int argCount = -1; // means return every similarly named function.
        String rawArgCount = null;
        if (inputLine.hasArgAt(1 + FIRST_ARG_INDEX)) {
            rawArgCount = inputLine.getArg(1 + FIRST_ARG_INDEX);
        }

        try {
            if (rawArgCount != null) {
                argCount = Integer.parseInt(rawArgCount);
            }
        } catch (Throwable t) {
            say("Sorry, but \"" + rawArgCount + "\" is not an integer");
            return RC_CONTINUE;
        }
        //List<String> doxx = getState().listFunctionDoc(fName, argCount);
        List<String> doxx = getFunctionDocFromVariable(fName, argCount);
        for (String x : doxx) {
            say(x);
        }
        return RC_CONTINUE;
    }

    /**
     * Any list of strings (functions, variables, modules, etc.) is listed using this formatting function.
     * If understands command line switches for width, columns and does some regex's too.
     *
     * @param inputLine
     * @param list      A simple list items, e.g., names of functions or variables.
     * @return
     */
    protected Object printList(InputLine inputLine, TreeSet<String> list) {
        if (list.isEmpty()) {
            return RC_CONTINUE;
        }
        if (list.size() == 1) {
            say(list.first());
            return RC_CONTINUE;
        }
        Pattern pattern = null;
        if (inputLine.hasArg(REGEX_SWITCH)) {
            try {
                pattern = Pattern.compile(inputLine.getNextArgFor(REGEX_SWITCH));
                TreeSet<String> list2 = new TreeSet<>();
                //pattern.
                for (String x : list) {
                    if (pattern.matcher(x).matches()) {
                        list2.add(x);
                    }
                }
                list.clear();
                list.addAll(list2);
                list = list2;
            } catch (Throwable t) {
                say("sorry but there was a problem with your regex \"" + inputLine.getNextArgFor(REGEX_SWITCH) + "\":" + t.getMessage());
            }

        }
        if (inputLine.hasArg(COLUMNS_VIEW_SWITCH)) {
            for (String func : list) {
                say(func); // one per line
            }
            return RC_CONTINUE;
        }
        int displayWidth = 120; // just to keep thing simple
        if (inputLine.hasArg(DISPLAY_WIDTH_SWITCH)) {
            try {
                displayWidth = Integer.parseInt(inputLine.getNextArgFor(DISPLAY_WIDTH_SWITCH));
            } catch (Throwable t) {
                say("sorry, but " + inputLine.getArg(0) + " is not a number. Formatting for default width of " + displayWidth);
            }
        }

        // Find longest entry
        int maxWidth = 0;
        for (String x : list) {
            maxWidth = Math.max(maxWidth, x.length());
        }
        // special case. If the longest element is too long, just print as columns
        if (displayWidth <= maxWidth) {
            for (String x : list) {
                say(x);
            }
            return RC_CONTINUE;
        }
        maxWidth = 2 + maxWidth; // so the widest + 2 chars to make it readable.
        // number of columns are display / width, possibly plus 1 if there is a remainder
        //int colCount = displayWidth / maxWidth + (displayWidth % maxWidth == 0 ? 0 : 1);
        int colCount = displayWidth / maxWidth;
        colCount = colCount + (colCount == 0 ? 1 : 0); // Make sure there is at least 1 columns
        int rowCount = list.size() / colCount;
        rowCount = rowCount + (rowCount == 0 ? 1 : 0); // and at least one row
        String[] output = new String[rowCount];
        for (int i = 0; i < rowCount; i++) {
            output[i] = ""; // initialize it
        }
        int pointer = 0;
        for (String func : list) {
            int currentLine = pointer++ % rowCount;
            if (rowCount == 1) {
                // single row, so don't pad, just a blank between entries
                output[currentLine] = output[currentLine] + func + "  ";
            } else {
                output[currentLine] = output[currentLine] + LJustify(func, maxWidth);
            }
        }
        for (String x : output) {
            say(x);
        }

        return RC_CONTINUE;
    }

    protected Object _funcsListSystem(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("system");
            sayi("List all system (built-in) functions.");
            return RC_NO_OP;
        }

        boolean listFQ = inputLine.hasArg(FQ_SWITCH);
        TreeSet<String> funcs = getState().getMetaEvaluator().listFunctions(listFQ);
        Object rc = printList(inputLine, funcs);
        say(funcs.size() + " total functions");
        return rc;
    }

    public static final String LIST_MODULES_SWITCH = "-m";
    public static final String LIST_INTRINSIC_SWITCH = "-intrinsic";
    public static final String LIST_EXTRINSIC_SWITCH = "-extrinsic";

    protected Object _funcsList(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("list [" + COMPACT_ALIAS_SWITCH + "|" + LIST_MODULES_SWITCH + "]");
            sayi("List all user defined functions.");
            sayi("list module - list all the functions in a given module");
            sayi("              Note that the module name must be the last argument if there are");
            sayi("              formatting switches.");
            sayi(COMPACT_ALIAS_SWITCH + " will collapse all old style modules to show by alias.");
            sayi(LIST_MODULES_SWITCH + " List old style module_import as well. Default is just what you've defined.");
            sayi(LIST_INTRINSIC_SWITCH + " List intrinsic functions as well. Default is not to show them.");
            sayi("    Note that you cannot modify or query them, simply see what they are named.");
            sayi("You  may omit the list command and any argument will be processed as a module");
            sayi("variable, so be aware.");
            return RC_NO_OP;
        }
        boolean listFQ = inputLine.hasArg(FQ_SWITCH);
        boolean includeModules = inputLine.hasArg(LIST_MODULES_SWITCH);
        inputLine.removeSwitch(LIST_MODULES_SWITCH);
        boolean showIntrinsic = inputLine.hasArg(LIST_INTRINSIC_SWITCH);
        inputLine.removeSwitch(LIST_INTRINSIC_SWITCH);
        boolean showExtrinsic = inputLine.hasArg(LIST_EXTRINSIC_SWITCH);
        inputLine.removeSwitch(LIST_EXTRINSIC_SWITCH);
        boolean useCompactNotation = inputLine.hasArg(COMPACT_ALIAS_SWITCH);
        inputLine.removeSwitch("list");
        TreeSet<String> funcs;
        if (0 < inputLine.getArgCount()) {
            String lastArg = inputLine.getLastArg();
            StringTokenizer stringTokenizer = new StringTokenizer(lastArg, State.NS_DELIMITER);
            State currentState = getState();
            Module module = null;
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                QDLValue obj = currentState.getValue(nextToken);
                if (obj.isModule()) {
                    module = obj.asModule();
                } else {
                    funcs = new TreeSet<>();
                }
            }
            if (module == null) {
                funcs = new TreeSet<>();
            } else {
                funcs = module.getState().listFunctions(useCompactNotation, null,
                        includeModules,
                        showIntrinsic,
                        showExtrinsic);
            }

        } else {
            funcs = getState().listFunctions(useCompactNotation, null,
                    includeModules,
                    showIntrinsic,
                    showExtrinsic);
        }
        // These are fully qualified.
        Object rc = -1;
        if (listFQ) {
            rc = printList(inputLine, funcs);
            say(funcs.size() + " total functions");
        } else {
            TreeSet<String> funcs2 = new TreeSet<>();
            for (String x : funcs) {
                int indexOf = x.lastIndexOf(SCHEME_DELIMITER);
                if (0 < indexOf) {
                    funcs2.add(x.substring(indexOf + 1));
                } else {
                    funcs2.add(x);
                }
            }
            rc = printList(inputLine, funcs2);
            if (0 < funcs2.size()) {
                say(funcs2.size() + " total functions");
            }

        }
        return rc;
    }

    /**
     * This is used in public facing queries to the workspace, from e.g. an SAS
     * instance getting/updating the current functions in the workspace.
     *
     * @return
     */
    public List<String> getFunctionList() {
        List<String> functions = new ArrayList<>();
        functions.addAll(state.getMetaEvaluator().listFunctions(false));
        functions.addAll(state.listFunctions(true,
                null, true, false, false));
        return functions;
    }

    protected String[] resolveRealHelpName(String text) {
        String realName = null;
        String altName = null;
        if (onlineHelp.containsKey(text) || altLookup.getByValue(text) != null) { // show single topic
            if (onlineHelp.containsKey(text)) {
                realName = text;
                altName = altLookup.get(realName);
            } else {
                altName = text;
                realName = altLookup.getByValue(text);
            }
            return new String[]{realName, altName};
        }
        return null;
    }

    public String getFunctionHelp(String text) {
        List<String> doxx = getState().listFunctionDoc(text, -1);
        if (doxx == null || doxx.isEmpty()) {
            return "";
        }
        return StringUtils.listToString(doxx);
    }

    public String getHelpTopic(String text) {
        String[] names = resolveRealHelpName(text);
        if (names == null) {
            return null;
        }
        String realName = names[0];
        String altName = names[1];
        // now we have the real name (main entry name) and the alt name.
        String out = onlineHelp.get(realName);
        if (altName != null) {
            String altKey = null;
            if (QDLTerminal.getReverseCharLookupMap().containsKey(altName)) {
                altKey = QDLTerminal.getReverseCharLookupMap().get(altName);
            }
            out = out + "\n" + "unicode: " + altName + " (" + StringUtils.toUnicode(altName) + ")" + (altKey == null ? "" : ", alt + " + altKey);
        }
        return out;
    }

    public String getHelpTopicExample(String text) {
        String[] names = resolveRealHelpName(text);
        if (names == null) {
            return null;
        }
        if (onlineExamples.containsKey(names[0])) {
            return onlineExamples.get(names[0]);
        }
        return null;
    }


    /**
     * Either show all variables (no arg or argument of "list") or <br/><br/>
     * drop name -- remove the given symbol from the local symbol table. This does not effect modules.
     *
     * @param inputLine
     * @return
     */
    protected Object doVars(InputLine inputLine) {
        if (!inputLine.hasArg(HELP_SWITCH) && (!inputLine.hasArgs() || inputLine.getArg(ACTION_INDEX).startsWith(SWITCH))) {
            return _varsList(inputLine);
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "help":
            case "--help":
                say("Variable commands");
                say("     drop - remove a variable from the system");
                say("     list - list all user defined variables. You can also request the variables for a module");
                say("            E.g. )vars list module_name");
                say("     size - try to guestimate the size of all the symbols used.");
                sayi("  system - list all system variables.");
                sayi("edit [" + EDIT_TEXT_FLAG + "]- edit the given variable. Adding the " + EDIT_TEXT_FLAG);
                sayi("          will treat the contents as text and set the variable to that.");
                sayi("          Note that you won't need quotes around the string if you edit it with " + EDIT_TEXT_FLAG + ".");
                sayi("          and you can enter linefeeds etc. which will be converted.");
                sayi("          If you omit the -x flag, then the variable is treated as a string.");
                sayi("\nE.g. Creating a variable of text\n");
                sayi(")vars edit readme -x\n");
                sayi("This would create a variable named readme (or edit it if it exists) in the external editor,");
                sayi("treating the contents as text. This allows for writing very complex text (such as a read me for");
                sayi("your workspace and is invaluable for writing documentation generally.");
                return RC_NO_OP;
            case "system":
                return _varsSystem(inputLine);
            case "list":
                return _varsList(inputLine);
            case "drop":
                return _varsDrop(inputLine);
            case "size":
                say(state.getStackSize() + " symbols defined.");
                return RC_CONTINUE;
            case "edit":
                return _doVarEdit(inputLine);
            default:
                return _varsList(inputLine);


        }
        //    return RC_CONTINUE;
    }

    public static final String EDIT_TEXT_FLAG = "-x";

    protected Object _doVarEdit(InputLine inputLine) {
        // process flags first
        boolean isText = inputLine.hasArg(EDIT_TEXT_FLAG);
        inputLine.removeSwitch(EDIT_TEXT_FLAG);
        String varName = inputLine.getLastArg();
        List<String> content = new ArrayList<>();
        boolean isDefined = getState().isDefined(varName);
        boolean isStem = varName.endsWith(QDLStem.STEM_INDEX_MARKER);
        if (isDefined) {
            if (isText) {
                if (isStem) {
                    // convert stem to list
                    QDLStem v = getState().getValue(varName).asStem();
                    if (!v.isList()) {
                        say("sorry, but only a list of strings can be edited as text");
                        return RC_NO_OP;
                    }
                    JSONArray jsonArray = (JSONArray) v.toJSON();
                    content = jsonArray;
                } else {
                    String v = getState().getValue(varName).toString();
                    v.replace("\n", "\\n");
                    content = StringUtils.stringToList(v);
                }

            } else {
                String inputForm = InputFormUtil.inputFormVar(varName, 2, getState());
                content.add(inputForm);
            }
        }
        List<String> output = new ArrayList<>();
        Object rc = editVariable(content, output, varName, isText, isStem);

        if (rc instanceof Response) {
            return rc;
        }
        if (rc.equals(RC_CONTINUE)) {
            return rc;
        }

        return restoreVariable(varName, output, isText, isStem);
    }

    public Object editVariable(List<String> inputForm, List<String> output, String varName, boolean isText, boolean isStem) {
        // Fixed https://github.com/ncsa/qdl/issues/55 update the returned output from the editor.
        if (useExternalEditor()) {
            output.clear();
            output.addAll(_doExternalEdit(inputForm));
        } else {
            if (isSwingGUI()) {
                // Fix https://github.com/ncsa/qdl/issues/71
                QDLEditor qdlEditor = new QDLEditor(this, varName, 0);
                qdlEditor.setType(EditDoneEvent.TYPE_VARIABLE);
                qdlEditor.setLocalName(varName);
                qdlEditor.setArgState((isText ? 1 : 0) + (isStem ? 2 : 0));
                qdlEditor.setup(StringUtils.listToString(inputForm));
                return RC_CONTINUE;
            }
            output.clear();
            output.addAll(_doLineEditor(inputForm));
        }
        return RC_NO_OP;
    }

    public Object restoreVariable(String varName, List<String> content, boolean isText, boolean isStem) {
        if (isText) {
            if (isStem) {
                QDLStem newStem = new QDLStem();
                newStem.addList(content);
                getState().setValue(varName, asQDLValue(newStem));
            } else {
                String newValue = StringUtils.listToString(content);
                getState().setValue(varName, asQDLValue(newValue));
            }
        } else {
            String newValue = StringUtils.listToString(content);
            newValue = newValue.trim();
            if (!newValue.endsWith(";")) {
                newValue = newValue + ";";
            }

            try {
                getInterpreter().execute(varName + " := " + newValue);
            } catch (Throwable throwable) {
                if (DebugUtil.isEnabled()) {
                    throwable.printStackTrace();
                }
                say("Sorry, could not update value of \"" + varName + "\"");
                return RC_NO_OP;
            }

        }
        return RC_CONTINUE;
    }

    protected Object _varsSystem(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("list system variables");
            return RC_NO_OP;
        }
        TreeSet<String> sysVars = new TreeSet<>();
        //  sysVars.addAll(getState().getSystemVars().listVariables());
        return printList(inputLine, sysVars);
    }

    protected Object _varsDrop(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("drop name");
            sayi("Drops i.e. removes the given variable from the current workspace.");
            return RC_NO_OP;
        }

        if (!inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            say("Sorry. You did not supply a variable name to drop");
            return RC_NO_OP;
        }
        getState().remove(inputLine.getArg(FIRST_ARG_INDEX));
        say(inputLine.hasArgAt(FIRST_ARG_INDEX) + " has been removed.");
        return RC_CONTINUE;
    }

    protected Object _varsList(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            int width = 15;
            say(RJustify("list [" + COMPACT_ALIAS_SWITCH + "]", width) + " - Lists the variables in the current workspace.");
            sayi(RJustify(LIST_INTRINSIC_SWITCH, width) + " - show intrinsic variables");
            sayi(RJustify(LIST_EXTRINSIC_SWITCH, width) + " - show extrinsic (i.e. global) variables");
            sayi("Command for old module system:");
            sayi("These only apply to modules imported using " + MODULE_IMPORT);
            sayi(RJustify(COMPACT_ALIAS_SWITCH, width) + " - collapse all modules to show by alias.");
            sayi(RJustify(LIST_MODULES_SWITCH, width) + " - list variables in modules");
            return RC_NO_OP;
        }
        boolean includeModules = inputLine.hasArg(LIST_MODULES_SWITCH);
        boolean useCompactNotation = inputLine.hasArg(COMPACT_ALIAS_SWITCH);
        boolean showIntrinsic = inputLine.hasArg(LIST_INTRINSIC_SWITCH);
        boolean showExtrinsic = inputLine.hasArg(LIST_EXTRINSIC_SWITCH);
        inputLine.removeSwitch(LIST_MODULES_SWITCH);
        inputLine.removeSwitch(COMPACT_ALIAS_SWITCH);
        inputLine.removeSwitch(LIST_INTRINSIC_SWITCH);
        inputLine.removeSwitch(LIST_EXTRINSIC_SWITCH);
        inputLine.removeSwitch("list"); // remove this so we can eyeball any other arguments
        // process if modules
        TreeSet<String> vars;
        if (0 < inputLine.getArgCount()) {
            String lastArg = inputLine.getLastArg();
            StringTokenizer stringTokenizer = new StringTokenizer(lastArg, State.NS_DELIMITER);
            State currentState = getState();
            Module module = null;
            while (stringTokenizer.hasMoreTokens()) {
                String nextToken = stringTokenizer.nextToken();
                QDLValue obj = currentState.getValue(nextToken);
                if (obj.isModule()) {
                    module = obj.asModule();
                } else {
                    vars = new TreeSet<>();
                }
            }
            if (module == null) {
                vars = new TreeSet<>();
            } else {
                vars = module.getState().listVariables(useCompactNotation,
                        includeModules,
                        showIntrinsic,
                        showExtrinsic);
            }
        } else {
            vars = getState().listVariables(useCompactNotation,
                    includeModules,
                    showIntrinsic,
                    showExtrinsic);
        }
        return printList(inputLine, vars);
    }

    public static final String ONLINE_HELP_EXAMPLE_FLAG = "-ex";
    public static final String ONLINE_HELP_COMMAND = "online";

    /**
     * Just print the general help
     * <pre>
     *     )help (* | name)  [arg_count]
     * </pre>
     * * will print the first line of all user defined functions in the workspace.<br/>
     * Otherwise name is the name of a function, system or user defined<br/>
     * The optional arg_count is ignored for system functions, but will call up detailed
     * information for the user functions.
     *
     * @param inputLine
     * @return
     */
    protected Object doHelp(InputLine inputLine) {
        if (!inputLine.hasArgs()) { // so no arguments
            showGeneralHelp();
            return RC_CONTINUE;
        }
        if (inputLine.getArg(ACTION_INDEX).equals("--help")) {
            showHelp4Help();
            return RC_CONTINUE;
        }
        boolean doOnlineExample = inputLine.hasArg(ONLINE_HELP_EXAMPLE_FLAG);
        inputLine.removeSwitch(ONLINE_HELP_EXAMPLE_FLAG);
        boolean checkAsModule = inputLine.hasArg(LIST_MODULES_SWITCH);
        inputLine.removeSwitch(LIST_MODULES_SWITCH);
        String name = inputLine.getArg(ACTION_INDEX);
        boolean isRegex = inputLine.hasArg(REGEX_SWITCH);

        if (name.equals("-all")) {
            // so they entered )funcs help Print off first lines of help
            TreeSet<String> treeSet = new TreeSet<>();
            treeSet.addAll(getState().listAllDocumentation());
            if (treeSet.isEmpty()) {
                say("(no user-defined functions)");
                return RC_CONTINUE;
            }
            return printList(inputLine, treeSet);
        }
        if (name.equals(ONLINE_HELP_COMMAND)) {  // show every topic
            TreeSet<String> treeSet = new TreeSet<>();
            // For display in full listing
            for (String key : onlineHelp.keySet()) {
                if (altLookup.containsKey(key)) {
                    treeSet.add(key + " (" + altLookup.get(key) + ")");
                } else {
                    treeSet.add(key);
                }
            }
            //treeSet.addAll(onlineHelp.keySet());
            if (treeSet.isEmpty()) {
                say("(no online help)");
                return RC_CONTINUE;
            }
            // if it's a regex, we have no idea what the display function will do, so don't have a count.
            say("Help is available for the following topics:");
            Object out = printList(inputLine, treeSet);
            say((isRegex ? "" : treeSet.size()) + " topics.");
            return out;
        }
        String[] names = resolveRealHelpName(name);

        String altName = null;
        if (names != null && names.length == 2) {
            altName = names[1];
        }

        if (names == null) {
            checkAsModule = true; // assume it might be a module and check it.
        } else {
            String realName = names[0];
            if (doOnlineExample) {
                String x = getHelpTopicExample(realName);
                if (x == null) {
                    say("no examples for " + name);
                } else {
                    say(x);
                }

            } else {
                String x = getHelpTopic(realName);
                if (x == null) {
                    say("no help for " + name);
                } else {
                    say(onlineHelp.get(realName));
                    if (altName != null) {
                        String altKey = QDLTerminal.getReverseCharLookupMap().get(altName);
                        say("alt: " + altName + " (" + StringUtils.toUnicode(altName) + ")" + (altKey == null ? "" : ", alt + " + altKey));
                    }
                    if (onlineExamples.containsKey(realName)) {
                        say("use -ex to see examples for this topic.");
                    }


                }
            }
            return RC_CONTINUE;
        }


        if (checkAsModule) {
            /*
             Cases are
             1. )help -m uri -- get help from the templates
             2. )help -m alias | var - if its
             3. )help alias# | var#var# -
            */
            List<String> doxx = null;
            try {
                URI uri = URI.create(name);
                Module template = getState().getMTemplates().getModule(new MTKey(uri));
                if (template != null) {
                    doxx = template.getDocumentation();
                }
            } catch (Throwable t) {

            }

            if (doxx == null) {
                doxx = getModuleDocFromVariable(name);
            }
            // that didn't work, see if it is an instance in the old system
            if (doxx == null) {
                String name0 = name;
                if (!name.endsWith(State.NS_DELIMITER)) {
                    name0 = name + State.NS_DELIMITER; // add it if the used the flag.
                }
                doxx = getState().listModuleDoc(name0);
            }
            if (!(doxx == null || doxx.isEmpty())) {
                for (String x : doxx) {
                    say(x);
                }
                return RC_CONTINUE;
            }
        }

        // Not a system function, so see if it is user defined. Find any arg count first
        int argCount = -1; // means return every similarly named function.
        String rawArgCount = null;
        if (inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            rawArgCount = inputLine.getArg(FIRST_ARG_INDEX);
        }

        try {
            if (rawArgCount != null) {
                argCount = Integer.parseInt(rawArgCount);
            }
        } catch (
                Throwable t) {
            say("Sorry, but \"" + rawArgCount + "\" is not an integer");
            return RC_CONTINUE;
        }
        List<String> doxx = getFunctionDocFromVariable(name, argCount);
        if (doxx.isEmpty()) {
            if (-1 < argCount) {
                say("sorry, no help for " + name + "(" + argCount + ")");
            } else {
                say("sorry, no help for '" + name + "'");
            }
        } else {
            for (String x : doxx) {
                say(x);
            }
        }

        return RC_CONTINUE;
    }

    protected List<String> getFunctionDocFromVariable(String name, int argCount) {
        List<String> doxx = null;
        if (name.contains(State.NS_DELIMITER)) {
            // a#b#c 2
            State currentState = getState();
            State previousState = null;
            StringTokenizer st = new StringTokenizer(name, State.NS_DELIMITER);
            while (st.hasMoreTokens()) {
                String currentToken = st.nextToken();
                QDLValue obj = currentState.getValue(currentToken);
                if (obj.isModule()) {
                    previousState = currentState;
                    currentState = obj.asModule().getState();
                } else {
                    doxx = currentState.listFunctionDoc(currentToken, argCount);
                }
            }
        } else {
            doxx = getState().listFunctionDoc(name, argCount);
        }
        return doxx;
    }

    protected List<String> getModuleDocFromVariable(String name) {
        StringTokenizer st = new StringTokenizer(name, State.NS_DELIMITER);
        State currentState = getState();
        String currentName = null;
        Module currentModule = null;
        while (st.hasMoreTokens()) {
            currentName = st.nextToken();
            QDLValue object = currentState.getValue(currentName);
            if (object != null && object.isModule()) {
                currentModule = object.asModule();
                currentState = currentModule.getState();
            } else {
                return null;
            }
        }
        return currentModule.getDocumentation();
    }

    public HashMap<String, String> getOnlineHelp() {
        return onlineHelp;
    }

    HashMap<String, String> onlineHelp = new HashMap<>();

    public HashMap<String, String> getOnlineExamples() {
        return onlineExamples;
    }

    HashMap<String, String> onlineExamples = new HashMap<>();

    public DoubleHashMap<String, String> getAltLookup() {
        return altLookup;
    }

    DoubleHashMap<String, String> altLookup = new DoubleHashMap<>();

    /**
     * Commands are:<br>
     * <ul>
     *     <li>load filename -- loads the given file, replacing the current state</li>
     *     <li>save filename -- serializes  the current workspace to the file</li>
     *     <li>clear -- clears the state completely.</li>
     *     <li>id -- print the currently named id, i.e., the name of the file if loaded</li>
     * </ul>
     *
     * @param inputLine
     * @return
     */
    protected Object doWS(InputLine inputLine) {
        if (!inputLine.hasArgs()) { // so no arguments
            say("no default command");
            return RC_CONTINUE;
        }
        switch (inputLine.getArg(ACTION_INDEX)) {
            case "help":
            case "--help":
                say("Workspace commands");
                sayi(" load file  - load a saved workspace.");
                sayi("save [file] - save the current workspace to the given file. If the current workspace");
                sayi("              has been loaded or saved, you may omit the file.");
                sayi("      clear - removes all user defined variables and functions");
                sayi("        get -  get a workspace value.");
                sayi("        lib - list the entries in a library.");
                sayi("     memory - give the amount of memory available to the workspace.");
                sayi("       name - give the file name of the currently loaded workspace.");
                say("               If no workspace has been loaded, no name is returned.");
                sayi("        set -  set a workspace value.");
                return RC_NO_OP;
            case "load":
                try {
                    return _wsLoad(inputLine);
                } catch (Throwable t) {
                    say("error loading ws:" + t.getMessage());
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                    return RC_NO_OP;
                }
            case "save":
                return _wsSave(inputLine);
            case "clear":
                return _wsClear(inputLine);
            case "get":
                return _wsGet(inputLine);
            case "set":
                return _wsSet(inputLine);
            case "lib":
                if (2 < inputLine.getArgCount() && inputLine.getArg(FIRST_ARG_INDEX).equals("drop")) {
                    return _wsListDrop(inputLine);
                }
                try {
                    return _wsLibList(inputLine);
                } catch (Throwable t) {
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                    say("error listing workspaces:" + t.getMessage());
                    return RC_NO_OP;
                }
            case "name":
                if (currentWorkspace == null) {
                    say("No workspace loaded");

                } else {
                    say(currentWorkspace);
                }
                return RC_CONTINUE;
            case "memory":
                say("memory used = " + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) +
                        " MB, free = " + (Runtime.getRuntime().freeMemory() / (1024 * 1024)) +
                        " MB, processors = " + Runtime.getRuntime().availableProcessors());
                return RC_CONTINUE;
            case "vfs":
                return _fileVFS(inputLine);
            default:
                say("unrecognized workspace command.");
                return RC_NO_OP;
        }

    }

    protected Object _wsListDrop(InputLine inputLine) {
        // Drop a workspace or collection of them
        if (inputLine.hasArg("--help")) {
            say(")lib drop file | " + REGEX_SWITCH + " regex [-f]");
            sayi("Drop, i.e. delete, either a single file or collection of them");
            sayi("-r means to use the regex to determine the file list for deletion");
            sayi("-f = force deletion flag. If this is not present, each file will get prompted");
            sayi("     If present, all files will be deleted.");
            return RC_CONTINUE;
        }
        String regex = null;
        inputLine.removeSwitch("drop"); // so it is not interpreted as a file name.
        if (inputLine.hasArg(REGEX_SWITCH)) {
            regex = inputLine.getNextArgFor(REGEX_SWITCH);
            inputLine.removeSwitchAndValue(REGEX_SWITCH);
        }
        boolean forceDeletes = inputLine.hasArg("-f");
        inputLine.removeSwitch("-f");
        String fullPath = _resolveLibFile(inputLine);
        if (fullPath == null) {
            say("Sorry, no file specified and no default file.");
            return RC_NO_OP;
        }
        File currentFile = new File(fullPath);
        if (currentFile.isFile()) {
            if (!forceDeletes) {
                forceDeletes = readline("Are you sure you want to delete the workspace '" + currentFile.getAbsolutePath() + "' (y/n)?").equals("y");
            }
            if (forceDeletes) {
                boolean rc = currentFile.delete();
                if (rc) {
                    say("deleted: " + currentFile.getAbsolutePath());
                } else {
                    say(currentFile.getAbsolutePath() + " could not be deleted");
                }
                return RC_CONTINUE;
            } else {
                return RC_NO_OP;
            }
        }
        // it's a directory. Apply any regex.
        Pattern pattern = null;

        FilenameFilter regexff = null;
        if (regex != null) {
            try {
                pattern = Pattern.compile(regex);
            } catch (PatternSyntaxException patternSyntaxException) {
                say("sorry, there is a problem with your regex: '" + regex + "':" + patternSyntaxException.getMessage());
                return RC_NO_OP;
            }
            regexff = new RegexFileFilter(pattern);
        }
        File[] files;
        if (regexff == null) {
            files = currentFile.listFiles();
        } else {
            files = currentFile.listFiles(regexff);
        }
        TreeSet<String> deletedFiles = new TreeSet<>();
        if (forceDeletes) {
            for (File file : files) {
                if (file.isFile()) { // don't delete directories!
                    if (file.delete()) {
                        deletedFiles.add(file.getAbsolutePath());
                    }
                }
            }
            for (String x : deletedFiles) {
                say("deleted " + x);
            }
            return RC_CONTINUE;
        }
        for (File f : files) {
            if (f.isFile()) {
                boolean doDelete = readline("Are you sure you want to delete the workspace '" + f.getAbsolutePath() + "' (y/n)?").equals("y");
                if (doDelete) {
                    if (f.delete()) {
                        deletedFiles.add(f.getAbsolutePath());
                    }
                }
            }
        }
        if (!deletedFiles.isEmpty()) {
            for (String x : deletedFiles) {
                say("deleted: " + x);
            }

        }
        //currentFile.l
        return RC_CONTINUE;
    }


    protected Object _fileVFS(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("vfs");
            sayi("Print any information about mounted virtual file systems.");
            return RC_NO_OP;
        }

        if (state.getVfsFileProviders().isEmpty()) {
            say("No installed virtual file systems");
            return RC_CONTINUE;
        }
        say("Installed virtual file systems");
        String indent = "                           "; // 25 blanks
        String shortSpaces = "           "; // 12 blanks
        for (String x : state.getVfsFileProviders().keySet()) {
            String output = "";
            VFSFileProvider y = state.getVfsFileProviders().get(x);
            output += makeColumn(indent, "type:" + y.getType());
            output += makeColumn(shortSpaces, "access:" + (y.canRead() ? "r" : "") + (y.canWrite() ? "w" : ""));
            output += makeColumn(indent, "scheme: " + y.getScheme());
            output += makeColumn(indent, "mount point:" + y.getMountPoint());
            output += makeColumn(indent, "current dir:" + (y.getCurrentDir() == null ? "(none)" : y.getCurrentDir()));
            sayi(output);
        }
        return RC_CONTINUE;
    }

    String makeColumn(String spaces, String text) {
        if (spaces.length() < text.length()) {
            return text;
        }
        return text + spaces.substring(0, spaces.length() - text.length());
    }

    public boolean isPreprocessorOn() {
        return preprocessorOn;
    }

    public void setPreprocessorOn(boolean preprocessorOn) {
        this.preprocessorOn = preprocessorOn;
    }

    boolean preprocessorOn = false;
    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    boolean prettyPrint = false;

    public static boolean isOnOrTrue(String x) {
        return x.equals("on") || x.equals("true") || x.equals("1") || x.equals("enable");
    }

    public static boolean isOffOrFalse(String x) {
        return x.equals("off") || x.equals("false") || x.equals("0") || x.equals("disable");
    }

    protected String onOrOff(boolean b) {
        return b ? "on" : "off";
    }

    protected void printAllWSVars() {
        Map<String, Object> allVars = new HashMap<>();
        for (String s : ALL_WS_VARS) {
            Object obj = getWSVariable(s);
            allVars.put(s, obj);
        }
        List<String> list = StringUtils.formatMap(allVars, null, true, true, 0, 72);
        for (String s : list) {
            say(s);
        }

    }

    String NOT_SET = "(not set)";

    /**
     * Get a workspace variable. These may be strings or booleans.
     *
     * @param key
     * @return
     */
    protected Object getWSVariable(String key) {
        switch (key) {
            case PRETTY_PRINT:
            case PRETTY_PRINT_SHORT:
                return isPrettyPrint();
            case ECHO:
                return isEchoModeOn();
            case JAVA_TRACE:
                return isDebugOn();
            case UNICODE_ON:
                return State.isPrintUnicode();
            case ASSERTIONS_ON:
                return isAssertionsOn();
            case ANSI_MODE_ON:
                return isAnsiModeOn();
            case PREPROCESSOR_ON:
                return isPreprocessorOn();
            case RUN_INIT_ON_LOAD:
                return runInitOnLoad;
            case START_TS:
                if (startTimeStamp != null) {
                    return Iso8601.date2String(startTimeStamp);
                }
                return NOT_SET;
            case EXTERNAL_EDITOR:
            case SHORT_EXTERNAL_EDITOR:
                return getExternalEditorName();
            case USE_EXTERNAL_EDITOR:
                return isUseExternalEditor();
            case ENABLE_LIBRARY_SUPPORT:
                return getState().isEnableLibrarySupport();
            case LIB_PATH_TAG:
                return getState().getLibPath();
            case DESCRIPTION:
                if (isTrivial(getDescription())) {
                    return NOT_SET;
                }
                return getDescription();
            case CURRENT_WORKSPACE_FILE:
                if (currentWorkspace == null) {
                    return NOT_SET;
                }
                return currentWorkspace;
            case WS_ID:
                if (isTrivial(getWSID())) {
                    return NOT_SET;
                }
                return getWSID();
            case COMPRESS_XML:
                return isCompressXML();
            case SAVE_DIR:
                if (saveDir == null) {
                    return NOT_SET;
                }
                return saveDir;
            case AUTOSAVE_ON:
                return isAutosaveOn();
            case AUTOSAVE_MESSAGES_ON:
                return isAutosaveMessagesOn();
            case AUTOSAVE_INTERVAL:
                return getAutosaveInterval();
            case ROOT_DIR:
                if (rootDir == null) {
                    return NOT_SET;
                }
                return rootDir;
            case OVERWRITE_BASE_FUNCTIONS_ON:
                return getState().isAllowBaseFunctionOverrides();
            default:
                return "unknown workspace variable";
        }
    }

    protected int _wsGet(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("get [ws_variable]");
            sayi("Retrieve the value of the given variable for the workspace.");
            sayi("No value means to return a list of supported variables.");
            sayi("You can get online help using the )help facility.");
            return RC_NO_OP;
        }
        // remember that the input line reads )ws get so the 1st argument is the name of this command
        if (inputLine.getArgCount() == 1) {
            printAllWSVars();
            return RC_CONTINUE;
        }
        String variable = inputLine.getArg(2);
        Object value = getWSVariable(variable);
        if (value instanceof Boolean) {
            say(variable + " is " + onOrOff((Boolean) value));
        } else {
            say(variable + " is " + value);

        }

        switch (inputLine.getArg(2)) {
            case OVERWRITE_BASE_FUNCTIONS_ON:
                say(onOrOff(getState().isAllowBaseFunctionOverrides()));
                break;
            case PRETTY_PRINT:
            case PRETTY_PRINT_SHORT:
                say(onOrOff(isPrettyPrint()));
                break;
            case ECHO:
                say(onOrOff(isEchoModeOn()));
                break;
            case JAVA_TRACE:
                say(onOrOff(isDebugOn()));
                break;
            case UNICODE_ON:
                say(onOrOff(State.isPrintUnicode()));
                break;
            case ASSERTIONS_ON:
                say(onOrOff(isAssertionsOn()));
                break;
            case RUN_INIT_ON_LOAD:
                say(onOrOff(runInitOnLoad));
                break;
            case ANSI_MODE_ON:
                say(onOrOff(ansiModeOn));
                break;
            case PREPROCESSOR_ON:
                say(onOrOff(preprocessorOn));
                break;
            case START_TS:
                if (startTimeStamp != null) {
                    say("startup time at " + Iso8601.date2String(startTimeStamp));
                } else {
                    say("(not set)");
                }
                break;
            case EXTERNAL_EDITOR:
            case SHORT_EXTERNAL_EDITOR:
                say(getExternalEditorName());
                break;
            case USE_EXTERNAL_EDITOR:
                say(isUseExternalEditor() ? "on" : "off");
                break;
            case ENABLE_LIBRARY_SUPPORT:
                say(getState().isEnableLibrarySupport() ? "on" : "off");
                break;
            case LIB_PATH_TAG:
                say("current " + LIB_PATH_TAG + "=" + getState().getLibPath());
                break;
            case DESCRIPTION:
                if (isTrivial(getDescription())) {
                    say("(no description set)");
                } else {
                    say(getDescription());
                }
                break;
            case CURRENT_WORKSPACE_FILE:
                if (currentWorkspace == null) {
                    say("not set");
                } else {
                    say(currentWorkspace);
                }
                break;
            case WS_ID:
                if (isTrivial(getWSID())) {
                    say("(workspace id not set)");
                } else {
                    say(getWSID());
                }
                break;
            case COMPRESS_XML:
                say(onOrOff(isCompressXML()));
                break;
            case SAVE_DIR:
                if (saveDir == null) {
                    say("(save directory not set)");
                } else {
                    say(saveDir);
                }
                break;
            case AUTOSAVE_ON:
                say("autosave is " + (isAutosaveOn() ? "on" : "off"));
                break;
            case AUTOSAVE_MESSAGES_ON:
                say("autosave messages are " + (isAutosaveMessagesOn() ? "on" : "off"));
                break;
            case AUTOSAVE_INTERVAL:
                say("autosave interval is " + getAutosaveInterval() + " ms.");
                break;
            case ROOT_DIR:
                if (rootDir == null) {
                    say("(root directory not set)");
                } else {
                    say(rootDir);
                }
                break;
            default:
                say("unknown workspace variable");
                break;
        }
        return RC_CONTINUE;
    }

    public static final String PRETTY_PRINT_SHORT = "pp";
    public static final String PRETTY_PRINT = "pretty_print";
    public static final String ECHO = "echo";
    public static final String UNICODE_ON = "unicode";
    public static final String JAVA_TRACE = "java_trace";
    public static final String START_TS = "start_ts";
    public static final String ROOT_DIR = "root_dir";
    public static final String SAVE_DIR = "save_dir";
    public static final String COMPRESS_XML = "compress_xml";
    public static final String WS_ID = "id";
    public static final String DESCRIPTION = "description";
    public static final String CURRENT_WORKSPACE_FILE = "ws_file";
    public static final String AUTOSAVE_ON = "autosave";
    public static final String AUTOSAVE_MESSAGES_ON = "autosave_messages";
    public static final String AUTOSAVE_INTERVAL = "autosave_interval";
    public static final String EXTERNAL_EDITOR = "external_editor";
    public static final String SHORT_EXTERNAL_EDITOR = "ee";
    public static final String USE_EXTERNAL_EDITOR = "use_external_editor";
    public static final String ENABLE_LIBRARY_SUPPORT = "library_support";
    public static final String ASSERTIONS_ON = "assertions";
    public static final String ANSI_MODE_ON = "ansi_mode";
    public static final String PREPROCESSOR_ON = "preprocessor";
    public static final String OVERWRITE_BASE_FUNCTIONS_ON = "overwrite_base_functions";
    public static final String LIB_LIST_FORCE_FORMAT_SWITCH = "-format";

    /**
     * This will either print out the information about a single workspace (if a file is given)
     * or every workspace in a directory. It accepts regexes as a file filter too
     * <br/>
     * <pre>
     * )ws lib /home/me/qdl/var/ws -r ws.*\\.*
     * </pre>
     * Prints out all the ws info for ws*.* in the directory. If a file is given, the regex is ignored.
     *
     * @param inputLine
     * @return
     */
    protected int _wsLibList(InputLine inputLine) throws Throwable {
        int displayWidth = 120; //default

        if (_doHelp(inputLine)) {
            say("lib [file] " + CLA_VERBOSE_ON + " | " + CLA_LONG_FORMAT_ON + " | "
                    + SHOW_FAILURES + " | [" + DISPLAY_WIDTH_SWITCH + " cols] | [" +
                    REGEX_SWITCH + " regex]");
            say("display information about the given file. If no file is specified, a listing of everything is printed");
            say(CLA_VERBOSE_ON + " = print out a very long listing");
            say(CLA_LONG_FORMAT_ON + " - print out a listing restricting everything property to a single line");
            say(SHOW_FAILURES + " = show output for files that cannot be deserialized and why.");
            say(SHOW_ONLY_FAILURES + " = show only output for files that cannot be deserialized and why.");
            say(DISPLAY_WIDTH_SWITCH + " cols = set the printed output to the given number of columns. Default is " + displayWidth);
            say(REGEX_SWITCH + " regex = filter output using the regex.");
            say(LIB_LIST_FORCE_FORMAT_SWITCH + " (java|xml|json|qdl) = force processing of every file in this format.");
            say("E.g.");
            say(")lib " + CLA_LONG_FORMAT_ON + " " + DISPLAY_WIDTH_SWITCH + " 80 " + REGEX_SWITCH + " wlcg.*");
            say("shows all workspaces that start with wlcg, restricting the per attributes output to a single line");
            say("(truncation is possible and denoted with an ellipsis), restricting the total width to 80 characters");
            say("Note that if you just ask it to list the directory, every file will be read, so for a very large");
            say("directory this may take some time");
            say("E.g.");
            say(")lib " + REGEX_SWITCH + " .*\\.ws");
            say("shows all files ending in .ws Since a period is special character in regexes, it must be escaped.");
            say(".* means match any character, \\.ws means it must end in '.ws'.");

            return RC_CONTINUE;
        }
        //       String fileName = null;
        String fullPath = null;
        boolean showOnlyFailures = inputLine.hasArg(SHOW_ONLY_FAILURES);
        boolean isVerbose = inputLine.hasArg(CLA_VERBOSE_ON); // print everything
        boolean isLongFormat = inputLine.hasArg(CLA_LONG_FORMAT_ON); // print long format
        boolean isShortFormat = !(isVerbose || isLongFormat);
        boolean showFailures = inputLine.hasArg(SHOW_FAILURES);
        if (showOnlyFailures) {
            showFailures = true;// so it gets ignored later
        }
        String forceFormat = "";
        if (inputLine.hasArg(LIB_LIST_FORCE_FORMAT_SWITCH)) {
            String ff = inputLine.getNextArgFor(LIB_LIST_FORCE_FORMAT_SWITCH);
            inputLine.removeSwitchAndValue(LIB_LIST_FORCE_FORMAT_SWITCH);
            switch (ff) {
                case "qdl":
                    forceFormat = DEFAULT_QDL_DUMP_FILE_EXTENSION;
                    break;
                case "xml":
                    forceFormat = DEFAULT_XML_SAVE_FILE_EXTENSION;
                    break;
                case "json":
                    forceFormat = "json";
                    break;
                case "java":
                    forceFormat = DEFAULT_JAVA_SERIALIZATION_FILE_EXTENSION;
                    break;
            }
        }
        if (inputLine.hasArg(DISPLAY_WIDTH_SWITCH)) {
            displayWidth = inputLine.getIntNextArg(DISPLAY_WIDTH_SWITCH);
        }
        // remove any switch so we can figure out what the arguments are.
        inputLine.removeSwitch(CLA_VERBOSE_ON);
        inputLine.removeSwitch(CLA_LONG_FORMAT_ON);
        inputLine.removeSwitch(SHOW_FAILURES);
        inputLine.removeSwitch(SHOW_ONLY_FAILURES);
        inputLine.removeSwitchAndValue(DISPLAY_WIDTH_SWITCH);
        Pattern pattern = null;
        //   String regex = null;

        RegexFileFilter regexff = null;
        if (inputLine.hasArg(REGEX_SWITCH)) {
            String rx = inputLine.getNextArgFor(REGEX_SWITCH);
            try {
                pattern = Pattern.compile(rx);
            } catch (PatternSyntaxException patternSyntaxException) {
                say("sorry, there is a problem with your regex: '" + rx + "':" + patternSyntaxException.getMessage());
                return RC_NO_OP;
            }
            regexff = new RegexFileFilter(pattern);
            inputLine.removeSwitchAndValue(REGEX_SWITCH);
        }
        fullPath = _resolveLibFile(inputLine);
        if (fullPath == null) {
            say("Sorry could not determine what the current library directory is. Did you set the " + SAVE_DIR + "?");
            return RC_NO_OP;
        }
        // That's been resolved.
        // fullpath is either a single file or a directory.
        int failureCount = 0;
        int successCount = 0;
        try {
            if (!isDirectory(getState(), fullPath)) {
                say("processing file " + fullPath);
                WSLibEntry w = _getWSLibEntry(fullPath, forceFormat);
                if (w != null) {
                    if (showOnlyFailures && !w.failed) {
                        return RC_CONTINUE;
                    }

                    if (!showFailures && w.failed) {
                        return RC_CONTINUE;
                    }
                    if (isShortFormat) {
                        say(w.shortFormat(displayWidth));
                        successCount++;
                    } else {
                        List<String> out = formatMap(w.toMap(),
                                null,
                                true, isVerbose, 1, displayWidth);
                        for (String x : out) {
                            say(x);
                            successCount++;
                        }
                    }
                }
                if (successCount == 0 && 0 < failureCount) {
                    say("(there were " + failureCount + " failures. Rerun with " + SHOW_FAILURES + " switch to see them.");
                }
                return RC_CONTINUE;
            }

        } catch (Throwable t) {
            say("uh-oh! That didn't work:" + t.getMessage());
            if (isDebugOn()) {
                t.printStackTrace();
            }
            return RC_NO_OP;
        }
        String[] wsFileList = dir(getState(), fullPath, regexff);


        //wsFileList = fullPath.listFiles(regexff);
        if (wsFileList == null || wsFileList.length == 0) {
            say("no workspaces found");
            return RC_CONTINUE;
        }
        TreeSet<String> sortedFiles = new TreeSet<>();
        for (String file : wsFileList) {
            sortedFiles.add(resolvePath(fullPath, file));
        }

        boolean firstPass = true;
        say("showing files for " + fullPath);
        for (String absPath : sortedFiles) {
            //File fff = sortedFiles.get(absPath);
            WSLibEntry w = _getWSLibEntry(absPath, forceFormat);
            if (w == null) continue;
            if (showOnlyFailures && !w.failed) {
                failureCount++;
                continue;
            }

            if (!showFailures && w.failed) {
                failureCount++;
                continue;
            }
            if (isShortFormat) {
                successCount++;
                say(w.shortFormat(displayWidth));
            } else {
                if (firstPass) {
                    firstPass = false;
                } else {
                    say("-----");
                }
                List<String> out = formatMap(w.toMap(),
                        null,
                        true, isVerbose, 1, displayWidth);
                for (String x : out) {
                    successCount++;
                    say(x);
                }

            }

        }
        if (showOnlyFailures) {
            say("found " + successCount + " failures");

        } else {
            say("found " + successCount + " workspaces" + (0 < failureCount ? (", " + failureCount + " failures") : ""));
        }
        return RC_CONTINUE;
    }

    protected String _resolveLibFile(InputLine inputLine) {
        String fileName = null;
        String currentFile = null;
        if (1 < inputLine.getArgCount()) {
            fileName = inputLine.getArg(FIRST_ARG_INDEX);
            currentFile = fileName;
        } else {
            if (saveDir != null) {
                currentFile = saveDir;
            } else {
                currentFile = rootDir;
            }
            if (currentFile == null) {
                return null;
            }
        }
        // current file absolute means its been resolved.
        if (!isAbsolute(currentFile)) {
            if (saveDir == null) {
                if (rootDir == null) {
                    return null;
                }
                if (fileName == null) {
                    return null;
                }
                currentFile = new File(rootDir, fileName).getAbsolutePath();
            } else {
                currentFile = new File(saveDir, fileName).getAbsolutePath();
            }
        }
        return currentFile;
    }

    public static class RegexFileFilter implements FilenameFilter {
        public RegexFileFilter(Pattern pattern) {
            this.pattern = pattern;
        }

        Pattern pattern;

        @Override
        public boolean accept(File dir, String name) {
            return pattern.matcher(name).matches();
        }
    }

    public static class WSLibEntry {
        Date ts;
        String id;
        String description;
        boolean isCompressed = false;
        String filename;
        String filepath;
        Date lastSaved_ts;
        String fileFormat;
        long length = -1L;
        boolean failed = false; // only set true when it actually fails.
        String failMessage;
        Throwable exception;

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            if (failed) {
                map.put("create_ts", ts);
                map.put("length", length);
                map.put("file_name", filename);
                map.put("file_path", filepath);
                map.put("last_modified", lastSaved_ts);
                map.put("message", failMessage);
                map.put("status", "FAILED");
                return map;
            }
            map.put("create_ts", ts);
            map.put(WS_ID, id);
            map.put(DESCRIPTION, description);
            map.put("compressed", isCompressed);
            map.put("file_name", filename);
            map.put("file_path", filepath);
            map.put("last_modified", lastSaved_ts);
            map.put("length", length);
            map.put("format", fileFormat);
            return map;
        }

        @Override
        public String toString() {
            return "WSLibEntry{" +
                    "ts=" + ts +
                    ", id='" + id + '\'' +
                    ", description='" + description + '\'' +
                    ", isCompressed=" + isCompressed +
                    ", filename='" + filename + '\'' +
                    ", filepath='" + filepath + '\'' +
                    ", lastSaved_ts=" + lastSaved_ts +
                    ", length=" + length +
                    '}';
        }

        public String shortFormat(int displayWidth) {
            if (failed) {
                String out = pad2(isTrivial(filename) ? "(no file)" : filename, 15);
                out = out + "   failed:" + pad2(failMessage, displayWidth - 23);
                return out;
            }
            String out = pad2(isTrivial(filename) ? "(no file)" : filename, 15);
            out = out + " " + (isCompressed ? "*" : " ");
            String lengthToken = "";
            NumberFormat formatter = new DecimalFormat("#0.000");
            double oneK = 1024.0;
            if (0 <= length && length < oneK) {
                lengthToken = length + "b";

            }
            if (oneK <= length && length < Math.pow(oneK, 2)) {
                lengthToken = formatter.format((length / oneK)) + "k";

            }
            if (Math.pow(oneK, 2) <= length && length < Math.pow(oneK, 3)) {
                lengthToken = formatter.format(length / Math.pow(oneK, 2)) + "m";
            }
            if (Math.pow(oneK, 3) <= length && length < Math.pow(oneK, 4)) {
                lengthToken = formatter.format(length / Math.pow(oneK, 3)) + "g";
            }


            out = out + " " + pad2(lengthToken, 10);

            if (isTrivial(id)) {
                //out = out + " " + pad2("(no id)", 10);
                out = out + " " + pad2("    -", 15);
            } else {
                out = out + " " + pad2(id, 15);
            }
            if (ts == null) {
                out = out + " " + pad2("(no date)", 25);
            } else {
                out = out + " " + pad2(ts, false, 30);
            }
            if (isTrivial(description)) {
                //out = out + " " + pad2("(no description)", displayWidth - 55);
                out = out + " " + pad2("    ----", displayWidth - 55);
            } else {
                out = out + " " + pad2(description, displayWidth - 55);
            }

            return out;
        }

    }


/*
    private WSLibEntry _getWSLibEntry(String fullPath) throws Throwable {
        if (isDirectory(getState(), fullPath)) {
            return null;
        }
        if (isVFSPath(fullPath)) {
            throw new NotImplementedException("Need to implement lib listing for VFS");
        } else {
            return _getWSLibEntry(fullPath);
        }
    }
*/

    /**
     * Reads a file and tries to figure out how it was serialized, then returns the information needed to
     * display basic information. Since there may be many files that have nothing to do with QDL, these are just skipped.
     *
     * @param fullPath
     * @return
     */
    private WSLibEntry _getWSLibEntry(String fullPath, String forceFormat) throws Throwable {
        if (isDirectory(getState(), fullPath)) {
            return null;
        }
        WSLibEntry wsLibEntry = null;
        boolean isVFS = isVFSPath(fullPath);
        File currentFile = null;
        if (!isVFS) {
            currentFile = new File(fullPath);
        }

        // force the processing if the user requires it.
        try {
            switch (forceFormat) {
                case DEFAULT_QDL_DUMP_FILE_EXTENSION:
                    return processQDLLibEntry(fullPath);
                case DEFAULT_JAVA_SERIALIZATION_FILE_EXTENSION:
                    return processJavaLibEntry(isVFS, fullPath, currentFile);
                case "json":
                    return processJSONLibEntry(isVFS, fullPath);
                case DEFAULT_XML_SAVE_FILE_EXTENSION:
                    return processXMLLibEntry(isVFS, fullPath, currentFile);
            }
        } catch (Throwable icx) {
            wsLibEntry = new WSLibEntry();
            wsLibEntry.failed = true;
            wsLibEntry.failMessage = icx.getMessage().replace('\n', ' ');// some messages have embedded line feeds
            wsLibEntry.exception = icx;
            wsLibEntry.filename = currentFile.getName();
            wsLibEntry.filepath = currentFile.getParent();

        }
        // now try to figure it out based on extension

        try {
            if (fullPath.endsWith(DEFAULT_QDL_DUMP_FILE_EXTENSION)) {
                wsLibEntry = processQDLLibEntry(fullPath);
            }
            if (fullPath.endsWith(DEFAULT_JAVA_SERIALIZATION_FILE_EXTENSION)) {
                wsLibEntry = processJavaLibEntry(isVFS, fullPath, currentFile);
            }
            if (fullPath.endsWith(JSON_SERIALIZATION_FILE_EXTENSION)) {
                wsLibEntry = processJSONLibEntry(isVFS, fullPath);
            }
            if (fullPath.endsWith(DEFAULT_XML_SAVE_FILE_EXTENSION)) {
                // Old format was XML so default to that. One day may swap that for JSON.
                try {
                    wsLibEntry = processXMLLibEntry(isVFS, fullPath, currentFile);
                } catch (Throwable t) {
                    try {
                        wsLibEntry = processJSONLibEntry(isVFS, fullPath);
                    } catch (Throwable tt) {
                        throw t; // assume for now that most workspaces are XML so that is the right error.
                    }
                    // fall through.
                }
            }
        } catch (Throwable icx) {
            wsLibEntry = new WSLibEntry();
            wsLibEntry.failed = true;
            wsLibEntry.failMessage = icx.getMessage().replace('\n', ' ');// some messages have embedded line feeds
            wsLibEntry.exception = icx;
            wsLibEntry.filename = currentFile.getName();
            wsLibEntry.filepath = currentFile.getParent();
        }

        return wsLibEntry;
    }

    private WSLibEntry processQDLLibEntry(String fullPath) throws Throwable {
        WSLibEntry wsLibEntry;
        wsLibEntry = new WSLibEntry();
        FileAttributes fileAttributes = QDLFileUtil.readAttributes(getState(), fullPath);
        wsLibEntry.ts = new Date(fileAttributes.timestamp);
        wsLibEntry.lastSaved_ts = wsLibEntry.ts;
        wsLibEntry.fileFormat = "QDL";
        wsLibEntry.length = fileAttributes.length;
        wsLibEntry.filename = fileAttributes.name;
        wsLibEntry.filepath = fileAttributes.parent;
        return wsLibEntry;
    }

    protected WSLibEntry processJSONLibEntry(boolean isVFS, String fullPath) throws Throwable {
        // try them both.
        try {
            return processJSONLibEntry(isVFS, fullPath, true);
        } catch (Throwable t) {

        }
        return processJSONLibEntry(isVFS, fullPath, false);
    }

    protected WSLibEntry processJSONLibEntry(boolean isVFS, String fullPath, boolean compressionOn) throws Throwable {
        WSLibEntry wsLibEntry = new WSLibEntry();
        byte[] bytes;
        if (isVFS) {
            bytes = readBinaryVFS(getState(), fullPath);
        } else {
            bytes = Files.readAllBytes(Paths.get(fullPath));
        }
        WSJSONSerializer wsjsonSerializer = new WSJSONSerializer();
        WorkspaceCommands workspaceCommands = wsjsonSerializer.fromJSON(bytes, compressionOn);
        wsLibEntry = getWsLibEntry(fullPath, compressionOn, workspaceCommands);
        wsLibEntry.fileFormat = "json";
        return wsLibEntry;
    }

    /**
     * Looks at a lib entry that was serialized in XML
     *
     * @param isVFS
     * @param fullPath
     * @param currentFile
     * @return
     */
    protected WSLibEntry processXMLLibEntry(boolean isVFS, String fullPath, File currentFile) throws Throwable {
        WSLibEntry wsLibEntry = new WSLibEntry();
        boolean gotCompressed = isCompressXML();
        XMLEventReader xer = null;
        xer = getXMLEventReader(fullPath, isCompressXML());
        if (xer == null) {
            gotCompressed = !isCompressXML();
            xer = getXMLEventReader(fullPath, !isCompressXML());
        }

        if (xer == null) {
            throw new IllegalStateException("could not create reader for XML file");
        }

        WSXMLSerializer serializer = new WSXMLSerializer();
        try {
            WorkspaceCommands tempWSC = serializer.fromXML(xer, true, false);
            xer.close();
            wsLibEntry = getWsLibEntry(fullPath, gotCompressed, tempWSC);
            wsLibEntry.fileFormat = "xml";
            return wsLibEntry;
        } catch (Throwable t) {

            try {
                xer.close();
            } catch (XMLStreamException e) {
                // fail silently, go to next.
            }
            throw t;
        }
    }


    /**
     * Looks for a lib entry that was serialized using Java serialization.
     *
     * @param isVFS
     * @param fullPath
     * @param currentFile
     * @return
     */
    protected WSLibEntry processJavaLibEntry(boolean isVFS, String fullPath, File currentFile) throws Throwable {
        WSLibEntry wsLibEntry = new WSLibEntry();
        byte[] bytes;
        InputStream inputStream = null;
        try {
            if (isVFS) {
                bytes = QDLFileUtil.readBinaryVFS(getState(), fullPath);
                inputStream = new ByteArrayInputStream(bytes);
            } else {
                inputStream = new FileInputStream(currentFile);
            }
            WSInternals wsInternals = (WSInternals) StateUtils.loadObject(inputStream);
            inputStream.close();
            wsLibEntry.ts = wsInternals.startTimestamp;
            wsLibEntry.id = wsInternals.id;
            wsLibEntry.description = wsInternals.description;
            wsLibEntry.isCompressed = true;
            wsLibEntry.filename = currentFile.getName();
            wsLibEntry.length = currentFile.length();
            Date lastMod = new Date();
            lastMod.setTime(currentFile.lastModified());
            wsLibEntry.lastSaved_ts = lastMod;
            wsLibEntry.filepath = currentFile.getParent();
            wsLibEntry.fileFormat = "java";
            return wsLibEntry;
        } catch (java.io.InvalidClassException icx) {
            // Means it was indeed serialized, but that blew up (probably due serialization change).
            // Just kick it back.
            try {
                inputStream.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            throw icx;
        }
    }

    private WSLibEntry getWsLibEntry(String fullPath, boolean gotCompressed, WorkspaceCommands tempWSC) throws Throwable {
        WSLibEntry wsLibEntry;
        wsLibEntry = new WSLibEntry();
        wsLibEntry.id = tempWSC.getWSID();
        wsLibEntry.description = tempWSC.getDescription();
        wsLibEntry.ts = tempWSC.startTimeStamp;
        wsLibEntry.isCompressed = gotCompressed;
        FileAttributes fileAttributes = QDLFileUtil.readAttributes(getState(), fullPath);
        wsLibEntry.filename = fileAttributes.name;
        wsLibEntry.length = fileAttributes.length;
        Date lastMod = new Date();
        lastMod.setTime(fileAttributes.timestamp);
        wsLibEntry.lastSaved_ts = lastMod;
        wsLibEntry.filepath = fileAttributes.parent;
        return wsLibEntry;
    }

    protected Object _wsSet(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("set ws_variable value");
            sayi("Set the value of the given workspace variable topo the given value.");
            sayi("Remember that strings should be in double quotes and you may also pipe in QDL variables.");
            return RC_NO_OP;
        }
        if (inputLine.getArgCount() == 1) {
            printAllWSVars();
            return RC_CONTINUE;
        }

        if (inputLine.getArgCount() < 3) {
            say("Missing argument. This requires two arguments.");
            return RC_NO_OP;

        }
        String value = inputLine.getArg(3);
        Boolean bValue = null;
        if(isOnOrTrue(value)) {
            bValue = Boolean.TRUE;
        }else{
            if(isOffOrFalse(value)){
                bValue = Boolean.FALSE;
            }
        }
        switch (inputLine.getArg(2)) {
            case OVERWRITE_BASE_FUNCTIONS_ON:
                if(bValue == null)return handleBadValue(value, OVERWRITE_BASE_FUNCTIONS_ON);
                getState().setAllowBaseFunctionOverrides(bValue);
                say("overwriting QDL base functions " + onOrOff(bValue));
                break;
            case PRETTY_PRINT:
            case PRETTY_PRINT_SHORT:
                if(bValue == null)return handleBadValue(value, PRETTY_PRINT);
                    setPrettyPrint(bValue);
                    getInterpreter().setPrettyPrint(bValue);
                    say("pretty print " + onOrOff(bValue));

                break;
            case ECHO:
                if(bValue == null)return handleBadValue(value, ECHO);
                setEchoModeOn(bValue);
                getInterpreter().setEchoModeOn(bValue);
                say("echo mode " + onOrOff(bValue));
                break;
            case JAVA_TRACE:
                if(bValue == null)return handleBadValue(value, JAVA_TRACE);
                setDebugOn(bValue);
                say("java trace is " + onOrOff(bValue));
                break;
            case UNICODE_ON:
                if(bValue == null)return handleBadValue(value, UNICODE_ON);
                State.setPrintUnicode(bValue);
                say("unicode printing of system constants is now " + onOrOff(bValue));
                break;
            case PREPROCESSOR_ON:
                // Fix https://github.com/ncsa/qdl/issues/127
                if(bValue == null)return handleBadValue(value, PREPROCESSOR_ON);
                setPreprocessorOn(bValue);
                say("QDL preprocessing is " + onOrOff(bValue));
                break;
            case ANSI_MODE_ON:

/*          Can't actually do this since the system InputStream gets munged and
            the whole JVM shuts down. There is probably a way to do it, but that is
            highly non-obvious, so I'll leave this here now as a later improvement
            if this gets important.
            Consideration 1: If a user is in text only mode because they have to be, loading
                             a workspace saved in ANSI or Swing mode may crash everything or
                             render the system unresponsive. Cannot preserve this across loads.
            Consideration 2 : There is no way to know if turning on ansi mode
                              will crash the JVM (some terminal values cannot use it), so
                              at best being able to toggle this is dicey. If the system starts,
                              keep it that way.
                if (isOnOrTrue(value)) {
                    try {
                        QDLTerminal qdlTerminal = new QDLTerminal(null);
                        ISO6429IO iso6429IO = new ISO6429IO(qdlTerminal, true);
                        setIoInterface(iso6429IO);
                        getIoInterface().setBufferingOn(true);
                        ansiModeOn = true;
                    } catch (IOException iox) {
                        say("sorry, could not switch to ansi mode:'" + iox.getMessage() + "'");
                    }
                } else {
                    setIoInterface(new BasicIO());
                    ansiModeOn = false;
                }*/
                say("ansi mode is read only and " + (ansiModeOn ? "on" : "off"));
                break;
            case USE_EXTERNAL_EDITOR:
                if(bValue == null)return handleBadValue(value, USE_EXTERNAL_EDITOR);
                setUseExternalEditor(bValue);
                say("use external editor " + onOrOff(bValue));
                break;
            case EXTERNAL_EDITOR:
            case SHORT_EXTERNAL_EDITOR:
                if (!value.equals(LINE_EDITOR_NAME)) {
                    EditorEntry x = getQdlEditors().get(value);
                    if (x == null) {
                        say("Sorry, but there is no such editor '" + value + "' available. Make sure it is configured.");
                        listEditors();
                        break;
                    }

                }
                String oldName = getExternalEditorName();
                setExternalEditorName(value);
                say("external editor was '" + oldName + "' now is '" + getExternalEditorName() + "'");
                break;
            case ENABLE_LIBRARY_SUPPORT:
                if(bValue == null)return handleBadValue(value, ENABLE_LIBRARY_SUPPORT);
                getState().setEnableLibrarySupport(bValue);
                say("library support is now " + onOrOff(bValue));
                break;
            case ASSERTIONS_ON:
                if(bValue == null)return handleBadValue(value, ASSERTIONS_ON);
                getState().setAssertionsOn(bValue);
                say("assertions are now " + onOrOff(bValue));
                break;
            case RUN_INIT_ON_LOAD:
                if(bValue == null)return handleBadValue(value, RUN_INIT_ON_LOAD);
                runInitOnLoad = bValue;
                say("run " + DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME + " on loading this workspace is " + onOrOff(bValue));
                break;
            case LIB_PATH_TAG:
                getState().setLibPath(value);
                say("library path updated");
                break;
            case START_TS:
                try {
                    Long rawDate = Long.parseLong(value);
                    startTimeStamp = new Date();
                    startTimeStamp.setTime(rawDate);
                } catch (NumberFormatException nfx) {
                    try {
                        Iso8601.string2Date(value);
                    } catch (ParseException e) {
                        say("sorry but '" + value + "' could not be parsed as a date");
                    }
                }
                say("start time for workspace changed to " + Iso8601.date2String(startTimeStamp));
                break;
            case DESCRIPTION:
                setDescription(value);
                say("description updated");
                break;
            case CURRENT_WORKSPACE_FILE:
                File temp = new File(value);
                if (temp.exists()) {
                    if (!temp.isFile()) {
                        say("sorry, " + temp.getAbsolutePath() + " is not a file.");
                        return RC_NO_OP;
                    }
                } else {
                    say("warning " + temp.getAbsolutePath() + " does not exist yet.");
                }
                currentWorkspace = value;
                break;
            case WS_ID:
                setWSID(value);
                say("workspace id set to '" + getWSID() + "'");
                break;
            case COMPRESS_XML:
                if(bValue == null)return handleBadValue(value, COMPRESS_FLAG);
                setCompressXML(bValue);
                say("xml compression " + onOrOff(bValue));
                break;
            case SAVE_DIR:
                saveDir = value;
                try {
                    if (!exists(getState(), saveDir)) {
                        say("warning the directory '" + saveDir + "' does not exist");
                        return RC_NO_OP;
                    }
                } catch (Throwable e) {
                    if (isDebugOn()) {
                        e.printStackTrace();
                    }
                    say("cannot find save dir ");
                    return RC_NO_OP;
                }
                try {
                    if (!isDirectory(getState(), saveDir)) {
                        say("warning  '" + saveDir + "' is not a directory");
                        return RC_NO_OP;
                    }
                    say("default save directory is now " + saveDir);
                } catch (Throwable t) {
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                    say("That didn't work:" + t.getMessage());
                }
                break;
            case ROOT_DIR:
                rootDir = value;
                try {
                    if (!exists(getState(), rootDir)) {
                        say("warning the directory '" + rootDir + "' does not exist");
                        return RC_NO_OP;
                    }
                    if (!isDirectory(getState(), rootDir)) {
                        say("warning  '" + rootDir + "' is not a directory");
                        return RC_NO_OP;
                    }
                    say("root directory is now " + rootDir);

                } catch (Throwable t) {
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                    say("that didn't work:" + t.getMessage());
                }

                break;
            case AUTOSAVE_ON:
                if (currentWorkspace == null) {
                    say("warning: you have not a set a file for saves. Please set " + CURRENT_WORKSPACE_FILE + " first.");
                } else {
                    if(bValue == null)return handleBadValue(value, AUTOSAVE_ON);
                    setAutosaveOn(bValue);
                    if (autosaveThread != null) {
                        autosaveThread.interrupt();
                        autosaveThread.setStopThread(true);
                        autosaveThread = null; // old one gets garbage collected, force a new one
                    }
                    if (isAutosaveOn()) {
                        initAutosave();
                    }
                    say("autosave is now " + onOrOff(bValue));

                }
                break;
            case AUTOSAVE_MESSAGES_ON:
                if(bValue == null)return handleBadValue(value, JAVA_TRACE);
               setAutosaveMessagesOn(bValue);
                say("autosave messages are now " + onOrOff(bValue));
                break;
            case AUTOSAVE_INTERVAL:
                String rawTime = value;
                if (4 <= inputLine.getArgCount()) {
                    rawTime = rawTime + " " + inputLine.getArg(4);
                }
                setAutosaveInterval(XMLConfigUtil.getValueSecsOrMillis(rawTime));
                say("autosave interval is now " + getAutosaveInterval() + " ms.");
                break;
            default:
                say("unknown ws variable '" + inputLine.getArg(2) + "'");
                break;
        }

        return RC_CONTINUE;
    }

    /**
     * If the raw value does not evaluate to a boolean, this is called to gracefully exit.
     * @param rawValue
     * @param varName
     * @return
     */
    // Fixes https://github.com/ncsa/qdl/issues/126
    int handleBadValue(String rawValue, String varName) {
        say("bad value of '" + rawValue + "' for " + varName);
        return RC_NO_OP;
    }
    protected void listEditors() {
        say("Available editors:");
        say(LINE_EDITOR_NAME);
        for (String name : getQdlEditors().listNames()) {
            say(name + (name.equals(getExternalEditorName()) ? " (active)" : ""));
        }
    }

    public static String[] ALL_WS_VARS = new String[]{
            ANSI_MODE_ON,
            ASSERTIONS_ON,
            AUTOSAVE_INTERVAL,
            AUTOSAVE_MESSAGES_ON,
            AUTOSAVE_ON,
            COMPRESS_XML,
            CURRENT_WORKSPACE_FILE,
            JAVA_TRACE,
            DESCRIPTION,
            ECHO,
            ENABLE_LIBRARY_SUPPORT,
            EXTERNAL_EDITOR,
            OVERWRITE_BASE_FUNCTIONS_ON,
            SHORT_EXTERNAL_EDITOR,
            LIB_PATH_TAG,
            PRETTY_PRINT,
            PRETTY_PRINT_SHORT,
            PREPROCESSOR_ON,
            ROOT_DIR,
            RUN_INIT_ON_LOAD,
            SAVE_DIR,
            START_TS,
            UNICODE_ON,
            USE_EXTERNAL_EDITOR,
            WS_ID
    };
    String wsID;

    public String getDescription() {
        return description;
    }

    /**
     * Human readable dscription of this workspace.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    String description;

    public String getWSID() {
        return wsID;
    }

    public void setWSID(String wsID) {
        this.wsID = wsID;
    }


    protected Object _wsEchoMode(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("echo (on | off) [-pp (on | off)]");
            sayi("Toggle the echo mode so every command is printed if it has output.");
            sayi("-pp = pretty print on or off. Stems should be printed horizontal by default.");
            return RC_NO_OP;
        }
        if (!inputLine.hasArgAt(FIRST_ARG_INDEX) || inputLine.getArg(FIRST_ARG_INDEX).startsWith("-")) {
            if (inputLine.hasArg("-pp")) {
                String pp = inputLine.getNextArgFor("-pp").toLowerCase();
                prettyPrint = pp.equals("true") || pp.equals("on");
                getInterpreter().setPrettyPrint(prettyPrint);
            }
            say("echo mode currently " + (isEchoModeOn() ? "on" : "off") + ", pretty print = " + (isPrettyPrint() ? "on" : "off"));
            return RC_CONTINUE;
        }
        String onOrOff = inputLine.getArg(FIRST_ARG_INDEX);
        if (inputLine.hasArg("-pp")) {
            String pp = inputLine.getNextArgFor("-pp").toLowerCase();
            prettyPrint = pp.equals("true") || pp.equals("on");
        }

        if (onOrOff.toLowerCase().equals("on")) {
            setEchoModeOn(true);
            getInterpreter().setEchoModeOn(true);
            getInterpreter().setPrettyPrint(prettyPrint);
            say("echo mode on, pretty print = " + (prettyPrint ? "on" : "off"));
        } else {
            setEchoModeOn(false);
            getInterpreter().setEchoModeOn(false);
            getInterpreter().setPrettyPrint(prettyPrint);
            say("echo mode off, pretty print = " + (prettyPrint ? "on" : "off"));
        }
        return RC_CONTINUE;
    }

    protected Object _wsClear(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("clear [" + RELOAD_FLAG + "]");
            sayi("Clear the state *completely*. This includes all virtual file systems and buffers.");
            sayi(RELOAD_FLAG + " = reload the current workspace from the configuration. Nothing current will be saved.");
            return RC_NO_OP;
        }
        if (inputLine.hasArg(RELOAD_FLAG)) {
            boolean clearIt = readline("Are you sure you want to lose all state and revert the workspace back to the initial load? (Y/n)[n]").equals("Y");
            if (clearIt) {
                return RC_RELOAD;
            }
        }
        boolean clearIt = readline("Are you sure you want to clear the workspace state? (Y/n)[n]").equals("Y");
        if (!clearIt) {
            say("WS clear aborted.");
            return RC_NO_OP;
        }
        clearWS();
        say("workspace cleared");
        return RC_CONTINUE;
    }

    private void clearWS() {
        State oldState = state;
        // Must preserve the IOInterface since whatever it is has hold of the system input stream
        // and that cannot be really transferred between instances -- attempts to do so will
        // result in the stream malfunctioning or simply throwing Exceptions on every use.
        // The most common sign is an almost silent JVM exit.
        IOInterface currentIOI = getIoInterface();
        // Get rid of everything.
        state = null;
        //MAliases.setmInstances(null); // zero this out or we have bogus entries.
        state = getState();
        state.setIoInterface(currentIOI);
        setIoInterface(currentIOI);
        state.createSystemConstants();
        state.setSystemInfo(oldState.getSystemInfo());
        commandHistory = new ArrayList<>();
        interpreter = new QDLInterpreter(state);
    }

    String JAVA_FLAG = SAVE_AS_JAVA_FLAG;
    String COMPRESS_FLAG = "-compress";
    String SHOW_FLAG = "-show";
    String QDL_DUMP_FLAG = "-qdl";
    String JSON_FLAG = "-json";
    String SAVE_AS_XML_FLAG = "-xml";
    public static String SILENT_SAVE_FLAG = "-silent";
    public static String NO_BUFFERS_SAVE_FLAG = "-no_buffers";

    /*
    Has to be public so save thread can access it.
     */
    protected int _wsSave(InputLine inputLine) {
        if (_doHelp(inputLine)) {
            say("save [filename] [" + JAVA_FLAG + "] | [" + SHOW_FLAG + "] | [" + COMPRESS_FLAG + " on|off] [" +
                    KEEP_WSF + "] | [" + SILENT_SAVE_FLAG + "]");
            sayi("Saves the current state (variables, loaded functions but not pending buffers of VFS) to a file.");
            sayi("If you have already loaded (or saved) a file, that is remembered in the " + KEEP_WSF + " variable");
            sayi("and you do not need to specify it henceforth.");
            sayi("The file should be either a relative path (resolved against the default save location) or an absolute path.");
            sayi(QDL_DUMP_FLAG + " = dump the contents of the workspace to a QDL file. You can just reload it using " + SystemEvaluator.LOAD_COMMAND);
            sayi(JAVA_FLAG + " = save using Java serialization format. The default is JSON.");
            sayi(SAVE_AS_XML_FLAG + " = save using XML serialization format. This does not support new module features. The default is JSON.");
            sayi(SHOW_FLAG + " = (XML format only) dump the (uncompressed) result to the console instead. No file is needed.");
            sayi(COMPRESS_FLAG + " = use to override compression setting of workspace. The resulting file will be a binary file.");
            sayi(KEEP_WSF + " = keep the current " + CURRENT_WORKSPACE_FILE + " rather than automatically updating it");
            sayi(NO_BUFFERS_SAVE_FLAG + " = do not save buffers when saving workspace. Default is to save open buffers. ");
            sayi(SILENT_SAVE_FLAG + " = print no messages when saving.");
            sayi("Note that a dump does not save any of the current workspace state, just the variables, functions and modules.");
            sayi("See the corresponding load command to recover it. It will print error messages, however.");
            say("See also: autosave_on (ws variable)");
            return RC_NO_OP;
        }
        long startTime = System.currentTimeMillis();
        boolean showFile = inputLine.hasArg(SHOW_FLAG);
        boolean doJava = inputLine.hasArg(SAVE_AS_JAVA_FLAG);
        boolean keepCurrentWS = inputLine.hasArg(KEEP_WSF);
        boolean silentMode = inputLine.hasArg(SILENT_SAVE_FLAG);
        boolean doQDL = inputLine.hasArg(QDL_DUMP_FLAG);
        boolean compressionOn = isCompressXML();
        boolean doJSON = inputLine.hasArg(JSON_FLAG);
        boolean doXML = inputLine.hasArg(SAVE_AS_XML_FLAG);
        boolean doNotsaveBuffers = inputLine.hasArg(NO_BUFFERS_SAVE_FLAG);


        if (inputLine.hasArg(COMPRESS_FLAG)) {
            compressionOn = inputLine.getNextArgFor(COMPRESS_FLAG).equalsIgnoreCase("on");
            inputLine.removeSwitchAndValue(COMPRESS_FLAG);
        }

        if (doQDL) {
            doJava = false; // QDL has preference, so if the user provides both, use QDL
        }
        inputLine.removeSwitch(SHOW_FLAG);
        inputLine.removeSwitch(SAVE_AS_JAVA_FLAG);
        inputLine.removeSwitch(KEEP_WSF);
        inputLine.removeSwitch(SILENT_SAVE_FLAG);
        inputLine.removeSwitch(QDL_DUMP_FLAG);
        inputLine.removeSwitch(JSON_FLAG);
        inputLine.removeSwitch(SAVE_AS_XML_FLAG);
        inputLine.removeSwitch(NO_BUFFERS_SAVE_FLAG);

        if (!(doXML || doJava || doQDL)) {
            doJSON = true; // set as default
        }

        // Remove switches before looking at positional arguments.

        String fName = null;
        if (showFile) {
            try {
                long[] sizes = new long[]{-1L, -1L};
                if (doJSON) {
                    WSJSONSerializer wsjsonSerializer = new WSJSONSerializer();
                    JSONObject json = wsjsonSerializer.toJSON(this);
                    String out = json.toString(1);
                    System.out.println(out);
                    sizes[UNCOMPRESSED_INDEX] = out.length();

                } else {
                    if (doQDL) {
                        StringWriter stringWriter = new StringWriter();
                        _xmlWSQDLSave(stringWriter);
                        System.out.println(stringWriter.getBuffer());
                        sizes[UNCOMPRESSED_INDEX] = stringWriter.getBuffer().length();
                    } else {
                        // defaults to XML
                        sizes = _xmlSave(null, compressionOn, showFile);
                    }

                }
                say("size: " + sizes[UNCOMPRESSED_INDEX] + "\n  elapsed time:" + ((System.currentTimeMillis() - startTime) / 1000.0D) + " sec.");
                return RC_CONTINUE;
            } catch (Throwable throwable) {
                say("warning. could not show file '" + throwable.getMessage() + "'");
                return RC_NO_OP;
            }

        }
        String fullPath;
        //  File target = null;
        try {

            if (inputLine.hasArgAt(FIRST_ARG_INDEX)) {
                fName = inputLine.getArg(FIRST_ARG_INDEX);
                try {
                    int index = Integer.parseInt(fName);
                    if (bufferManager.hasBR(index)) {
                        say("warning: There is a buffer '" + index + "'. If you want to save that, then use the buffer save command.");
                        if (!readline("save as workspace? (y/n)").equals("y")) {
                            say("ok. aborting save.");
                            return RC_NO_OP;
                        }
                    }

                } catch (NumberFormatException nfx) {
                    // rock on
                }
                // Figure out extension.
                if (!fName.contains(".")) {
                    if (doQDL) {
                        fName = fName + DEFAULT_QDL_DUMP_FILE_EXTENSION;
                    } else {
                        if (doJava) {
                            fName = fName + DEFAULT_JAVA_SERIALIZATION_FILE_EXTENSION;
                        } else {
                            fName = fName + DEFAULT_XML_SAVE_FILE_EXTENSION;
                        }
                    }
                }
                //target = new File(fName);
                fullPath = fName;
            } else {
                if (currentWorkspace == null) {
                    say("sorry, no workspace file set.");
                    return RC_NO_OP;
                } else {
                    fullPath = currentWorkspace;
                    fName = currentWorkspace;
                }
            }

            if (VFSPaths.isVFSPath(fName)) {
                if (VFSPaths.isAbsolute(fName)) {
                    fullPath = fName;
                    //target = new File(fName);
                } else {
                    if (saveDir == null) {
                        if (VFSPaths.isVFSPath(rootDir)) {
                            fullPath = VFSPaths.resolve(rootDir, fName);
                        } else {
                            say("Sorry, cannot save VFS file, save directory is not a VFS file");
                            return RC_NO_OP;
                        }
                    } else {
                        if (VFSPaths.isVFSPath(saveDir)) {
                            fullPath = VFSPaths.resolve(saveDir, fName);
                        } else {
                            say("Sorry, cannot save VFS file, root directory is not a VFS file");
                            return RC_NO_OP;

                        }

                    }
                }
            } else {
                if (!isAbsolute(fName)) {
                    if (saveDir == null) {
                        fullPath = QDLFileUtil.resolvePath(rootDir, fName);
                    } else {
                        fullPath = QDLFileUtil.resolvePath(saveDir, fName);
                    }
                }
                if (exists(getState(), fullPath) && isDirectory(getState(), fullPath)) {
                    say("sorry, but " + fullPath + " is a directory.");
                    return RC_NO_OP;

                }

            }

            if (!doNotsaveBuffers) {
                _saveAllBuffers();
            }
            if (doQDL || fullPath.endsWith(QDLVersion.DEFAULT_FILE_EXTENSION)) {
                //_doQDLDump(target);
                long length = _xmlWSQDLSave(fullPath);
                say("saved '" + fName + "'\n  bytes: " + length + "\n  elapsed time:" + ((System.currentTimeMillis() - startTime) / 1000.0D) + " sec.");
                return RC_CONTINUE;
            }
            long[] sizes = new long[]{-1L, -1L};

            if (doJava) {
                sizes = _xmlWSJavaSave(fullPath);
            } else {
                if (doJSON) {
                    sizes = _jsonWSSave(fullPath, compressionOn);
                } else {
                    sizes = _xmlSave(fullPath, compressionOn, showFile);
                }
            }
            if (!silentMode) {
                String out = "saved: '" + fullPath + "'" +
                        "\n  on: " + new Date();
                if (0 < sizes[UNCOMPRESSED_INDEX]) {
                    out = out + "\n uncompressed size: " + sizes[UNCOMPRESSED_INDEX];
                }
                if (0 < sizes[COMPRESSED_INDEX]) {
                    out = out + "\n compressed size: " + sizes[COMPRESSED_INDEX];
                }
                out = out + "\n  elapsed time: " + ((System.currentTimeMillis() - startTime) / 1000.0D) + " sec.";

                say(out);
                //say("Saved " + target.length() + " bytes to " + target.getCanonicalPath() + " on " + (new Date()) + head + ". Elapsed time " + ((System.currentTimeMillis() - startTime)/1000.0D) + " sec."  );
            }
            if (!keepCurrentWS) {
                currentWorkspace = fullPath;
            }

        } catch (Throwable t) {
            logger.error("could not save workspace.", t);
            say("could not save the workspace:" + t.getMessage());
        }
        return RC_NO_OP;
    }

    private long[] _jsonWSSave(String path, boolean compressionOn) throws Throwable {
        long[] sizes = new long[]{-1L, -1L};
        boolean isVFS = VFSPaths.isVFSPath(path);
        WSJSONSerializer wsjsonSerializer = new WSJSONSerializer();
        JSONObject json = wsjsonSerializer.toJSON(this);
        String raw = json.toString(1);
        sizes[COMPRESSED_INDEX] = writeFile(path, raw, compressionOn);

  /*      if (isVFS) {
            StringWriter stringWriter = new StringWriter();
            writeTextVFS(getState(), path, raw);
            sizes[UNCOMPRESSED_INDEX] = raw.length();
        } else {
            File f = new File(path);
            FileWriter fileWriter = new FileWriter(f);
            fileWriter.write(raw);
            fileWriter.flush();
            fileWriter.close();
            sizes[UNCOMPRESSED_INDEX] = f.length();
        }*/
        return sizes;
    }

    private Object _jsonWSSLoad(String path, boolean compressionOn) throws Throwable {
        JSONObject jsonObject;
        byte[] bytes = null;
        String raw = null;
        WorkspaceCommands newCommands;
        if (VFSPaths.isVFSPath(path)) {
            bytes = readBinaryVFS(getState(), path);
        } else {
            if (getState().isServerMode()) {
                throw new QDLServerModeException();
            }
            bytes = Files.readAllBytes(Paths.get(path));
        }
        WSJSONSerializer wsJSONSerializer = new WSJSONSerializer();
        newCommands = wsJSONSerializer.fromJSON(bytes, compressionOn);
        updateWSState(newCommands);
        if (runInitOnLoad && state.getFTStack().containsKey(new FKey(DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME, 0))) {
            String runnit = DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME + "();";
            // turn off echoing so __init only prints what it wants to.
            QDLInterpreter qi = getInterpreter();
            boolean oldPP = qi.isPrettyPrint();
            boolean oldEchoMode = qi.isEchoModeOn();
            qi.setEchoModeOn(false);
            qi.setPrettyPrint(false);
            getInterpreter().execute(runnit);
            qi.setPrettyPrint(oldPP);
            qi.setEchoModeOn(oldEchoMode);
        }
        return RC_CONTINUE;
    }

    public static final String DEFAULT_QDL_DUMP_FILE_EXTENSION = ".qdl";
    public static final String DEFAULT_XML_SAVE_FILE_EXTENSION = ".ws";
    public static final String ALTERNATE_XML_SAVE_FILE_EXTENSION = ".zml"; // for reads
    public static final String DEFAULT_JAVA_SERIALIZATION_FILE_EXTENSION = ".wsj";
    public static final String JSON_SERIALIZATION_FILE_EXTENSION = ".json";

    /**
     * Just loads and runs a {@link Reader}. This is mostly used in serialization tests.
     *
     * @param reader
     * @throws Throwable
     */
    public void _xmlWSQDLLoad(QDLInterpreter qdlInterpreter, Reader reader) throws Throwable {
        // Don't barf out everything to the command line when it loads.
        boolean pp = qdlInterpreter.isPrettyPrint();
        boolean echo = qdlInterpreter.isEchoModeOn();
        qdlInterpreter.setPrettyPrint(false);
        qdlInterpreter.setEchoModeOn(false);
        QDLRunner runner = qdlInterpreter.execute(reader);
        lastResult = runner.getLastResult();
        setPrettyPrint(pp);
        setEchoModeOn(echo);
        setDebugOn(debugOn);
        qdlInterpreter.setEchoModeOn(echo);
        qdlInterpreter.setPrettyPrint(pp);
        qdlInterpreter.setDebugOn(debugOn);
    }

    public Object getLastResult() {
        return lastResult;
    }

    Object lastResult;

    public long _xmlWSQDLSave(String path) throws Throwable {
        boolean isVFS = VFSPaths.isVFSPath(path);
        if (isVFS) {
            StringWriter stringWriter = new StringWriter();
            _xmlWSQDLSave(stringWriter);
            writeTextVFS(getState(), path, stringWriter.getBuffer().toString());
            return stringWriter.getBuffer().length();
        } else {
            File f = new File(path);
            FileWriter fileWriter = new FileWriter(f);
            _xmlWSQDLSave(fileWriter);
            return f.length();
        }
    }

    public long _xmlWSQDLSave(Writer fileWriter) throws Throwable {
        fileWriter.write("// QDL workspace " + (isTrivial(getWSID()) ? "" : getWSID()) + " dump, saved on " + (new Date()) + "\n");
        fileWriter.write("\n/* ** module definitions ** */\n");

        for (Object kk : getState().getMTemplates().keySet()) {
            MTKey key = (MTKey) kk;
            String output = inputFormModule(key.getUriKey(), state);
            if (output.startsWith(JAVA_CLASS_MARKER)) {
                output = SystemEvaluator.MODULE_LOAD + "('" + output.substring(JAVA_CLASS_MARKER.length())
                        + "' ,'" + SystemEvaluator.MODULE_TYPE_JAVA + "');";
            }
            fileWriter.write(output + "\n");
        }

        /*
          Order matters. The imports are done after the variables and functions since they may
          refer to them. 
         */
        /**
         * Have to be careful in listing only what is in the actual state, not
         * stuff in modules too since that is saved elsewhere and both bloats
         * the output and can make for NS conflicts on reload.
         */
        fileWriter.write("\n/* ** global variables ** */\n");
        for (Object varName : state.getExtrinsicVars().listVariables()) {
            String output = inputFormVar((String) varName, 2, state);
            fileWriter.write(varName + " := " + output + ";\n");
        }

        fileWriter.write("\n/* ** user defined variables ** */\n");
        for (Object varName : state.getVStack().listVariables()) {
            QDLValue qdlObject = state.getValue(varName.toString());
            String output = inputFormVar((String) varName, 2, state);
            fileWriter.write(varName + " := " + output + (output.endsWith(";") ? "" : ";" + "\n"));
        }
        if (!state.getIntrinsicVariables().isEmpty()) {
            fileWriter.write("\n/* ** user defined intrinsic variables ** */\n");
            for (Object varName : state.getIntrinsicVariables().listVariables()) {
                String output = inputFormVar((String) varName, 2, state);
                fileWriter.write(varName + " := " + output + ";\n");
            }
        }

        fileWriter.write("\n/* ** user defined functions ** */\n");

        for (XKey key : state.getFTStack().keySet()) {
            String output = inputForm((FunctionRecord) state.getFTStack().get(key));
            if (output != null && !output.startsWith(JAVA_CLASS_MARKER)) {
                // Do not write java functions as they live in a module.
                fileWriter.write(output + "\n");
            }
        }
        // now do the imports
        fileWriter.write("\n/* ** module " + SerializationConstants.VERSION_2_0_TAG + " imports ** */\n");

        for (Object kk : getState().getMInstances().keySet()) {
            XKey key = (XKey) kk;
            Module module = getState().getMInstances().getModule(key);
            List<String> aliases = getState().getMInstances().getAliasesAsString(module.getMTKey());
            for (String alias : aliases) {
                String output = MODULE_IMPORT + "('" + module.getNamespace() + "','" + alias + "');";
                fileWriter.write(output + "\n");
            }
        }

        // Todo -- Should at some point crawl through the module's state and make a table of variables.
        // These would have to be FQ (so a#b#c#d := 3) and done after all imports.


        fileWriter.flush();
        fileWriter.close();
        return 0L;
    }

    private void _doQDLDump(File target) throws Throwable {
        FileWriter fileWriter = new FileWriter(target);
        _xmlWSQDLSave(fileWriter);
    }

    public static int UNCOMPRESSED_INDEX = 0;
    public static int COMPRESSED_INDEX = 1;

    private long[] _xmlSave(String fullPath, boolean compressSerialization, boolean showIt) throws Throwable {
        long[] sizes = new long[]{-1L, -1L};

        Writer w = new StringWriter();

        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xsw = xof.createXMLStreamWriter(w);
        long uncompressedSize = -1L;
        boolean isTargetVFS = isVFSPath(fullPath);
        if (getState().isServerMode() && !isTargetVFS) {
            throw new QDLServerModeException("cannot write local file in server mode");
        }
        toXML(xsw);
        String payload = XMLUtils.prettyPrint(w.toString());
        sizes[UNCOMPRESSED_INDEX] = payload.length();
        w.flush();
        w.close();
        if (showIt) {
            say(payload);
            return sizes;
        } else {
            sizes[COMPRESSED_INDEX] = writeFile(fullPath, payload, compressSerialization);
        }
        return sizes;
    }

    /**
     * write the payload to the correct file (VFS or local), compressing as needed.
     *
     * @param fullPath
     * @param payload
     * @param compress
     * @return
     */
    protected long writeFile(String fullPath, String payload, boolean compress) throws Throwable {
        long writtenSize = payload.length();
        boolean isTargetVFS = isVFSPath(fullPath);
        if (getState().isServerMode() && !isTargetVFS) {
            throw new QDLServerModeException("Only to VFS supported in server mode");
        }
        if (compress) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos);
            gzipOutputStream.write(payload.getBytes("UTF-8"));
            gzipOutputStream.flush();
            gzipOutputStream.close();
            if (isTargetVFS) {
                VFSFileProvider vfsFileProvider = getState().getVFS(fullPath);
                String parentPath = fullPath.substring(0, fullPath.lastIndexOf(VFSPaths.PATH_SEPARATOR));
                if (!vfsFileProvider.isDirectory(parentPath)) {
                    vfsFileProvider.mkdir(parentPath);
                }
                saveDir = parentPath;
                writeBinaryVFS(getState(), fullPath, baos.toByteArray());
            } else {
                File fff = new File(fullPath);
                if (!fff.getParentFile().exists()) {
                    fff.getParentFile().mkdirs();
                }
                saveDir = fff.getParent();
                FileOutputStream fos = new FileOutputStream(fullPath);
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            }
        } else {
            if (isTargetVFS) {
                writeTextVFS(getState(), fullPath, payload);
            } else {
                FileWriter fw = new FileWriter(fullPath);
                fw.write(payload);
                fw.flush();
                fw.close();
            }
        }

        return QDLFileUtil.length(getState(), fullPath);
    }

    public long[] _xmlWSJavaSave(String path) throws Throwable {
        logger.info("saving workspace '" + path + "'");
        long[] sizes = new long[]{-1L, -1L};
        OutputStream outputStream = null;
        boolean isVFS = VFSPaths.isVFSPath(path);
        if (isVFS) {
            outputStream = new ByteArrayOutputStream();
        } else {
            if (getState().isServerMode()) {
                throw new QDLServerModeException("Only VFS operations supported in server mode");
            }
            outputStream = new FileOutputStream(path);
        }
        _xmlWSJavaSave(outputStream);
        sizes[UNCOMPRESSED_INDEX] = QDLFileUtil.length(getState(), path);
        if (isVFS) {
            ByteArrayOutputStream baos = (ByteArrayOutputStream) outputStream;
            sizes[UNCOMPRESSED_INDEX] = baos.size();
            writeBinaryVFS(getState(), path, baos.toByteArray());
        }
        return sizes;
    }


    public void _xmlWSJavaSave(OutputStream fos) throws IOException {
        WSInternals wsInternals = new WSInternals();
        wsInternals.defaultState = defaultState;
        wsInternals.currentPID = currentPID;
        wsInternals.activeState = state;
        wsInternals.siEntries = siEntries;
        wsInternals.startTimestamp = startTimeStamp;
        wsInternals.id = wsID;
        wsInternals.description = description;
        wsInternals.echoOn = echoModeOn;
        wsInternals.prettyPrint = prettyPrint;
        wsInternals.debugOn = debugOn;
        if (saveDir != null) {
            wsInternals.saveDir = saveDir;
        }
        StateUtils.saveObject(wsInternals, fos);
    }

    protected XMLEventReader getXMLEventReader(String fullPath, boolean isCompressXML) throws Throwable {
        XMLEventReader xer = null;
        if (VFSPaths.isVFSPath(fullPath)) {
            if (isCompressXML) {
                xer = XMLUtils.getZippedReader(readBinaryVFS(getState(), fullPath));
            } else {
                xer = XMLUtils.getXMLEventReader(new StringReader(readTextVFS(getState(), fullPath)));
            }
        } else {
            if (getState().isServerMode()) {
                throw new QDLServerModeException();
            }
            if (isCompressXML) {
                xer = XMLUtils.getZippedReader(new File(fullPath));
                // user is dictating to use compress.
            } else {
                xer = XMLUtils.getReader(new File(fullPath));
            }
        }
        return xer;
    }

    private boolean _xmlLoad(String fullPath, boolean skipBadModules) {
        // The file may be in XML format. If not, then it is assumed to be
        // zipped and binary.
        // First attempt is to assume no compression
        XMLEventReader xer = null;
        try {
            xer = getXMLEventReader(fullPath, isCompressXML());
        } catch (Throwable e) {
            if (isDebugOn()) {
                e.printStackTrace();
            }
            say("could not create XML reader:" + e.getMessage());
        }

        if (xer != null) {
            try {
                fromXML(xer, skipBadModules);
                xer.close();
                currentWorkspace = fullPath;
                getState().setWorkspaceCommands(this);
                if (runInitOnLoad && state.getFTStack().containsKey(new FKey(DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME, 0))) {
                    String runnit = DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME + "();";
                    getInterpreter().execute(runnit);
                }
                return true;
            } catch (Throwable t) {
                // First attempt can fail for, e.g., the default is compression but the file is not compressed.
                // So this might be benign.
                // A derserialization exception though means the structure of the file was
                // bad (e.g. missing java classes)
                if (t instanceof DeserializationException) {
                    throw (DeserializationException) t;
                }
            }
        }

        if (xer == null) {
            // so that didn't work, most likely because the file is or is not compressed,
            // try the other way
/*
            if (isCompressXML()) {
                // user is dictating to use compress.
                xer = XMLUtils.getReader(fullPath);
            } else {
                xer = XMLUtils.getZippedReader(fullPath);
            }
*/
            if (isCompressXML()) {
                xer = XMLUtils.getZippedReader(new File(fullPath));
                // user is dictating to use compress.
            } else {
                xer = XMLUtils.getReader(new File(fullPath));
            }

        }
        if (xer == null) {
            //say("sorry, cannot get the file '" + f.getAbsolutePath() + "'");
            return false;
        }

        try {
            fromXML(xer, skipBadModules);
            xer.close();
            currentWorkspace = fullPath;
            return true;
        } catch (XMLStreamException e) {
            say("error reading XML at line " + e.getLocation().getLineNumber() + ", col " + e.getLocation().getColumnNumber()
                    + ":'" + e.getMessage() + "'");
        } catch (Throwable t) {
            say("error reading XML file: " + t.getMessage());
        }
        return false;
    }

    protected InputStream inputStreamFromFile(File f) throws Throwable {
        InputStream inputStream = null;
        boolean isVFS = VFSPaths.isVFSPath(f.toString());
        if (isVFS) {
            byte[] bytes = readBinaryVFS(getState(), f.toString());
            inputStream = new ByteArrayInputStream(bytes);
        } else {
            if (getState().isServerMode()) {
                throw new QDLServerModeException("Only VFS operations supported in server mode");
            }
            inputStream = new FileInputStream(f);
        }
        return inputStream;

    }

    /*
    Does the actual work of loading a serialized file once the logic for what to do has been done.
     */
    public boolean _xmlWSJavaLoad(String fullPath) {
        try {
            InputStream inputStream = QDLFileUtil.readFileAsInputStream(getState(), fullPath);
            if (_xmlWSJavaLoad(inputStream)) {
                currentWorkspace = fullPath;
                return true;
            }
            return false;
        } catch (Throwable t) {
            if (DebugUtil.isEnabled()) {
                t.printStackTrace();
            }
            say("sorry, but '" + fullPath + "' does not exist");
        }
        return false;
    }

    public boolean _xmlWSJavaLoad(InputStream fis) {
        try {
            WSInternals wsInternals = (WSInternals) StateUtils.loadObject(fis);

            //State newState = StateUtils.load(fis);
            State newState = wsInternals.activeState;
            currentPID = wsInternals.currentPID;
            defaultState = wsInternals.defaultState;
            siEntries = wsInternals.siEntries;
            startTimeStamp = wsInternals.startTimestamp;
            wsID = wsInternals.id;
            description = wsInternals.description;
            echoModeOn = wsInternals.echoOn;
            prettyPrint = wsInternals.prettyPrint;
            debugOn = wsInternals.debugOn;
            if (wsInternals.saveDir != null) {
                saveDir = wsInternals.saveDir;
            }
            /*
            Now set the stuff that cannot be serialized.
             */
            newState.injectTransientFields(getState());
            //defaultState.injectTransientFields(getState());
            defaultState = newState;
            for (Integer key : siEntries.keySet()) {
                SIEntry sie = siEntries.get(key);
                sie.state.injectTransientFields(getState());
            }
            interpreter = new QDLInterpreter(env, newState);
            interpreter.setEchoModeOn(isEchoModeOn());
            interpreter.setDebugOn(isDebugOn());
            state = newState;
            if (runInitOnLoad && state.getFTStack().containsKey(new FKey(DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME, 0))) {
                String runnit = DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME + "();";
                getInterpreter().execute(runnit);
            }
            return true;
        } catch (Throwable t) {
            if (isDebugOn()) {
                t.printStackTrace();
            }
        }
        return false;
    }

    String DEFAULT_BOOT_FUNCTION_ON_LOAD_NAME = "__initialize";
    boolean runInitOnLoad = true;

    String currentWorkspace;
    public final String RELOAD_FLAG = SWITCH + "reload";
    public final String SKIP_BAD_MODULES_FLAG = SWITCH + "skip_bad_modules";

    protected Object _wsLoad(InputLine inputLine) throws Throwable {
        if (_doHelp(inputLine)) {
            say("load [filename] [" + KEEP_WSF + "] ");

            sayi("Loads a saved workspace. If the name is relative, it will be resolved against " +
                    "the default location or it may be an absolute path.");
            sayi(KEEP_WSF + " = keep the current " + CURRENT_WORKSPACE_FILE + " rather than automatically updating it");
            sayi(QDL_DUMP_FLAG + " = the format of the file is QDL. This loads it into the current workspace.");
            sayi(JAVA_FLAG + " = the format of the file is serialized java. default is XML");
            sayi(SAVE_AS_XML_FLAG + " = the format of the file is serialized java. default is XML");
            sayi(COMPRESS_FLAG + " = override compression settings with this");
            sayi(SKIP_BAD_MODULES_FLAG + " = (xml only) if a module is missing or fails to load, continue, otherwise abort the entire load.");
            sayi("   Note that skipping modules will cause many errors later and result in an not fully functional workspace." +
                    "\n   As such it should only be done except in dire cases.");
            sayi("If there is no file given, the current workspace is used.");
            sayi("If you dumped a workspace to QDL, you may simply load it as any other script");
            sayi("e.g.");
            say(")load my_ws -qdl");
            sayi("would load a file named my_ws.qdl ");
            sayi("See also: save, setting the current workspace.");
            return RC_NO_OP;
        }

        //  File target = null;
        String fName = null;
        String fullPath = null;
        boolean keepCurrentWS = inputLine.hasArg(KEEP_WSF);
        inputLine.removeSwitch(KEEP_WSF);
        boolean doQDL = inputLine.hasArg(QDL_DUMP_FLAG);
        inputLine.removeSwitch(QDL_DUMP_FLAG);
        boolean doJava = inputLine.hasArg(JAVA_FLAG) && !doQDL; // QDL has right of way
        inputLine.removeSwitch(JAVA_FLAG);
        boolean skipBadModules = inputLine.hasArg(SKIP_BAD_MODULES_FLAG);
        inputLine.removeSwitch(SKIP_BAD_MODULES_FLAG);
        boolean doJSON = inputLine.hasArg(JSON_FLAG);
        inputLine.removeSwitch(JSON_FLAG);
        boolean doXML = inputLine.hasArg(SAVE_AS_XML_FLAG);
        inputLine.removeSwitch(SAVE_AS_XML_FLAG);
        boolean compressionOn = isCompressXML();
        if (inputLine.hasArg(COMPRESS_FLAG)) {
            compressionOn = inputLine.getNextArgFor(COMPRESS_FLAG).equals("on");
            inputLine.removeSwitchAndValue(COMPRESS_FLAG);
        }
        if (inputLine.hasArgAt(FIRST_ARG_INDEX)) {
            fName = inputLine.getArg(FIRST_ARG_INDEX);
        } else {
            if (currentWorkspace == null) {
                say("sorry, no workspace file set.");
                return RC_NO_OP;
            } else {
                fullPath = currentWorkspace;
            }
        }
        if (!(doXML || doQDL || doJava)) {
            doJSON = true;
        }

        if (fullPath == null && fName.contains(".")) {
            // If there is an extension, we are done.
            fullPath = fName;
        }
        boolean loadOK = false;
        if (fullPath == null) {
            fullPath = fName;

            // At this point, the fName has no extension. Check for standard extensions
            String parentDir = null; // null parent is ignored in the File constructor below
            if (!isAbsolute(fullPath)) {
                if (saveDir == null) {
                    parentDir = rootDir;
                } else {
                    parentDir = saveDir;
                }
            }
            if (doQDL) {
                fullPath = resolvePath(parentDir, fName);
                //target = new File(parentDir, fName); // try it raw

                if (!exists(getState(), fullPath) || isDirectory(getState(), fullPath)) {
                    fullPath = resolvePath(parentDir, fName + DEFAULT_QDL_DUMP_FILE_EXTENSION); // only  possible extension
                }

            } else {
                if (doJava) {
                    fullPath = resolvePath(parentDir, fName + DEFAULT_JAVA_SERIALIZATION_FILE_EXTENSION);
                } else {
                    fullPath = resolvePath(parentDir, fName + DEFAULT_XML_SAVE_FILE_EXTENSION);
                    if (!exists(getState(), fullPath)) {
                        fullPath = resolvePath(parentDir, fName + ALTERNATE_XML_SAVE_FILE_EXTENSION);

                    }

                }
            }

        } else {
            if (!isAbsolute(fullPath)) {

                if (saveDir == null) {
                    fullPath = resolvePath(rootDir, fName);
                } else {
                    fullPath = resolvePath(saveDir, fName);
                }
                if (isDirectory(getState(), fullPath)) {
                    say("sorry, but " + fullPath + " is not a file.");
                    return RC_NO_OP;
                }
            }

        }         //     file_write('/tmp/data.csv',  to_cvs([['x','y']]~y.))

        if (fullPath == null) {
            say("sorry, could not determine file for '" + fName + "'");
            return RC_NO_OP;
        }
        if (!exists(getState(), fullPath)) {
            say("sorry, the target file '" + fullPath + "' does not exist");
            return RC_NO_OP;
        }
        if (isDirectory(getState(), fullPath)) {
            say("sorry, the target  '" + fullPath + "' is not a file");
            return RC_NO_OP;
        }
        if (!canRead(getState(), fullPath)) {
            say("sorry, cannot read  '" + fullPath + "'");
            return RC_NO_OP;
        }
        if (doQDL || fullPath.endsWith(QDLVersion.DEFAULT_FILE_EXTENSION)) {
            // Other load methods clear the workspace first. We do that here:
            // User experience is that if it was in echo mode and pretty print before the car
            // it should remain so, since QDL does not save WS state.
            boolean echo = isEchoModeOn();
            boolean pp = isPrettyPrint();
            boolean debugOn = isDebugOn();
            String saveDir = this.saveDir;
            clearWS();
            String command = SystemEvaluator.LOAD_COMMAND + "('" + fullPath + "');";
            try {
                // Don't barf out everything to the command line when it loads.
                getInterpreter().setPrettyPrint(false);
                getInterpreter().setEchoModeOn(false);
                getInterpreter().execute(command);
                setPrettyPrint(pp);
                setEchoModeOn(echo);
                setDebugOn(debugOn);
                getInterpreter().setEchoModeOn(echo);
                getInterpreter().setPrettyPrint(pp);
                getInterpreter().setDebugOn(debugOn);
                this.saveDir = saveDir;

                say(fullPath + " loaded (" + length(getState(), fullPath) + " bytes)");
                return RC_CONTINUE;
            } catch (Throwable throwable) {
                if (DebugUtil.isEnabled()) {
                    throwable.printStackTrace();
                }
                say("sorry, could not load QDL '" + fullPath + "': " + throwable.getMessage());
                return RC_NO_OP;
            }
        }
        if (doJSON) {
            Object rc = _jsonWSSLoad(fullPath, compressionOn);
            if (!keepCurrentWS) {
                currentWorkspace = fullPath;
            }
            return rc;
        }
        loadOK = _xmlWSJavaLoad(fullPath);
        if (!loadOK) {
            try {
                loadOK = _xmlLoad(fullPath, skipBadModules);
            } catch (DeserializationException deserializationException) {
                say("Could not deserialize workspace: " + deserializationException.getMessage());
                return RC_NO_OP;
            }
        }
        if (loadOK) {
            if (!isVFSPath(fullPath)) {
                say("Last saved on " + Iso8601.date2String((new File(fullPath)).lastModified()));
            }

            if (!isTrivial(getWSID())) {
                say(getWSID() + " loaded," + QDLFileUtil.length(getState(), fullPath) + " bytes read.");
            } else {
                say(fullPath + " loaded, " + QDLFileUtil.length(getState(), fullPath) + " bytes read.");
            }
            if (!isTrivial(getDescription())) {
                say(getDescription());
            }
            if (!keepCurrentWS) {
                currentWorkspace = fullPath;
            }
        } else {
            say("Could not load workspace for file " + fullPath);
        }
        return RC_CONTINUE;
    }

    QDLInterpreter interpreter = null;

    public QDLInterpreter getInterpreter() {
        return interpreter;
    }

    boolean debugOn = false;

    @Override
    public boolean isDebugOn() {
        return debugOn;
    }

    @Override
    public void setDebugOn(boolean debugOn) {
        this.debugOn = debugOn;
        DebugUtil.setIsEnabled(debugOn);
    }

    @Override
    public void debug(String x) {
        logger.debug(x);
    }

    @Override
    public void info(String x) {
        logger.info(x);
    }

    @Override
    public void warn(String x) {
        logger.warn(x);
    }

    @Override
    public void error(String x) {
        logger.error(x);
    }

    public void error(String x, Throwable t) {
        logger.error(x, t);
    }

    /**
     * Prints with the default indent and a linefeed.
     *
     * @param x
     */
    public void say(String x) {
        getIoInterface().println(defaultIndent + x);
    }

    public static final String INDENT = "  "; // use this in implementations for consistent indenting.
    protected String defaultIndent = "";

    /**
     * prints with the current indent and a linefeed.
     *
     * @param x
     */
    protected void sayi(String x) {
        say(INDENT + x);
    }

    /**
     * Double indent -- useful for lists.
     *
     * @param x
     */
    protected void sayii(String x) {
        say(INDENT + INDENT + x);
    }

    /**
     * This is used in serialization tests. Generally you should never set the state this way.
     *
     * @param state
     */
    public void setState(State state) {
        this.state = state;
    }

    State state;

    /**
     * Creates the top-level state object for the system. All other state objects are dervied from it.
     *
     * @return
     */
    public State getState() {
        if (state == null) {
            VStack stack = new VStack();
            state = new State(
                    stack,
                    new OpEvaluator(),
                    MetaEvaluator.getInstance(),
                    new FStack(),
                    new MTStack(),
                    new MIStack(),
                    logger,
                    false,
                    false,
                    isAssertionsOn()
            );// workspace is never in server mode, nor restricted IO
            state.setStateID(0);
            state.setWorkspaceCommands(this);
            state.setRootState(state);  // resolves https://github.com/ncsa/qdl/issues/24
        }
        return state;

    }

    public void runMacro(List<String> commands) {
        try {
            getWorkspace().runMacro(commands);
        } catch (Throwable t) {
            if (t instanceof ReturnException) {
                ReturnException re = (ReturnException) t;
                if (re.hasResult()) {
                    say(re.result.toString());
                }
            } else {
                say("could not execute macro");
            }
        }
    }

    protected void setupJavaModule(State state, QDLLoader loader, boolean importASAP) {
        for (Module m : loader.load()) {
            m.setTemplate(true);
            state.addModule(m); // done!  Add it to the templates
            if (importASAP) {
                // Add it to the imported modules, i.e., create an instance.
                state.getMInstances().put(m.newInstance(state));
            }
        }
    }

    /*

    CLA = Command Line Args. These are the switches used on the command line
     */
    public static final String CLA_MODULES = "-module"; // multiple classes comma separated allowed
    public static final String CLA_MACRO = "-macro"; // Marcos. New line separated
    public static final String CLA_ENVIRONMENT = "-env";
    public static final String CLA_HOME_DIR = "-qdl_root";
    public static final String CLA_LOG_DIR = "-log";
    public static final String CLA_BOOT_SCRIPT = "-boot_script";
    public static final String CLA_VERBOSE_ON = "-v";
    public static final String CLA_LONG_FORMAT_ON = "-l";
    public static final String CLA_NO_BANNER = "-no_banner";
    public static final String CLA_SHOW_BANNER = "-show_banner";
    public static final String CLA_LOGO = "-logo";
    public static final String CLA_DEBUG_ON = "-debug";
    public static final String CLA_RUN_SCRIPT_ON = "-run";
    public static final String CLA_PREPROCESSOR_ON = "-" + WorkspaceCommands.PREPROCESSOR_ON;
    public static final String CLA_SCRIPT_PATH = "-script_path";
    public static final String CLA_MODULE_PATH = "-module_path";
    public static final String CLA_LIB_PATH = "-lib_path";

    /**
     * If this is a relative file, it is resolved against the root directory. Otherwise, it
     * is just returned.<br/>
     * Absolute as per spec means it starts with a / on unix, or drive:\ on Windows.
     *
     * @param file
     * @return
     */
    protected File resolveAgainstRoot(String file) {
        File f = new File(file);
        if (!f.isAbsolute()) {
            // then we need to resolve it against the root.
            return new File(rootDir, file);
        }
        return f;
    }

    File envFile; // this is the name of the file holding the environment variables
    // turns on some low-level tracing of this class with DebugUtil when it initializes. Not for public use.
    String TRACE_ARG = "-trace";

    public boolean isCompressXML() {
        return compressXML;
    }

    public void setCompressXML(boolean compressXML) {
        this.compressXML = compressXML;
    }

    boolean compressXML = true;

    public boolean hasEditors() {
        return qdlEditors != null;
    }

    public Editors getQdlEditors() {
        if (qdlEditors == null) {
            qdlEditors = new Editors();
            EditorEntry ee = new EditorEntry();
            ee.name = "line";
            ee.exec = "line";
            qdlEditors.put(new EditorEntry());
        }
        return qdlEditors;
    }

    public void setQdlEditors(Editors qdlEditors) {
        this.qdlEditors = qdlEditors;
    }

    public void fromConfigFile(InputLine inputLine) throws Throwable {
        String cfgname = inputLine.hasArg(CONFIG_NAME_FLAG) ? inputLine.getNextArgFor(CONFIG_NAME_FLAG) : "default";
/*        if (inputLine.hasArg(CLA_LOGO)) {
            String logoName = inputLine.getNextArgFor(CLA_LOGO).toLowerCase();
            logo = getLogo(logoName);
            inputLine.removeSwitchAndValue(CLA_LOGO);
        }*/
        if (!isQELoaded()) {
            loadQE(inputLine, cfgname);
        }

        inputLine.removeSwitchAndValue(CONFIG_NAME_FLAG);
        inputLine.removeSwitchAndValue(CONFIG_FILE_FLAG);
        fromConfigFile(inputLine, qdlEnvironment);
    }

    public QDLEnvironment getQdlEnvironment() {
        return qdlEnvironment;
    }

    public void setQdlEnvironment(QDLEnvironment qdlEnvironment) {
        this.qdlEnvironment = qdlEnvironment;
    }

    QDLEnvironment qdlEnvironment = null;

    public void loadQE(InputLine inputLine, String cfgName) throws Throwable {
        if (qdlEnvironment == null) {
            // New style -- multi-inheritance.
            //     ConfigurationNode node = ConfigUtil.findMultiNode(inputLine.getNextArgFor(CONFIG_FILE_FLAG), cfgname, CONFIG_TAG_NAME );
            ConfigurationNode node = XMLConfigUtil.findMultiNode(inputLine.getNextArgFor(CONFIG_FILE_FLAG), cfgName, CONFIG_TAG_NAME);
            QDLConfigurationLoader loader = new QDLConfigurationLoader(inputLine.getNextArgFor(CONFIG_FILE_FLAG), node);

            qdlEnvironment = loader.load();
        }
    }

    protected boolean isQELoaded() {
        return qdlEnvironment != null;
    }

    public static final String HOME_DIR_ARG = "-home_dir";

    public void fromConfigFile(InputLine inputLine, QDLEnvironment qe) throws Throwable {
        // The state probably exists at this point if the user had to set the terminal type.
        // Make sure the logger ends up in the actual state.
        // The logger is created in the loader, so it happens automatically if there is a logging block in the config.
        if (state != null) {
            state.setLogger(qe.getMyLogger());
        }
        if (inputLine.hasArg(HOME_DIR_ARG)) {
            // The user might set the home directory here.
            // This is overrides configuration file.
            rootDir = inputLine.getNextArgFor(HOME_DIR_ARG);
            inputLine.removeSwitchAndValue(HOME_DIR_ARG);
        }
        compressXML = qe.isCompressionOn();
        // Setting this flag at the command line will turn on lower level debugging.
        // The actual option in the configuration file turns on logging debug (so info and trace are enabled).
        if (inputLine.hasArg(TRACE_ARG)) {
            say("trace enabled");
            setDebugOn(true);
            DebugUtil.setIsEnabled(true);
            DebugUtil.setDebugLevel(DebugConstants.DEBUG_LEVEL_TRACE);
            inputLine.removeSwitch(TRACE_ARG);
        }
        setFont(qe.getFont());
        figureOutFont(inputLine);
        MetaDebugUtil du = new MetaDebugUtil(WorkspaceCommands.class.getSimpleName(), MetaDebugUtil.DEBUG_LEVEL_OFF, true);
        state.setDebugUtil(du);
        state.setServerMode(qe.isServerModeOn());
        state.setRestrictedIO(qe.isRestrictedIO());
        // Next is for logging, which is not the same as debug.
        if (qe.isDebugOn()) {
            setDebugOn(true);
        }
        if (rootDir != null) {
            // This is where we let the command line override the configuration.
            qe.setWsHomeDir(rootDir);
            qe.getMyLogger().info("Overriding the root directory in the configuration with the argument from the command line.");
        }
        setEchoModeOn(qe.isEchoModeOn());
        setPrettyPrint(qe.isPrettyPrint());
        isRunScript = inputLine.hasArg(CLA_RUN_SCRIPT_ON);
        state.setAssertionsOn(qe.isAssertionsOn());
        assertionsOn = qe.isAssertionsOn();
        preprocessorOn = qe.isPreprocesserOn();
        if(inputLine.hasArg(CLA_PREPROCESSOR_ON)){
            Boolean x = inputLine.getBooleanNextArgFor(CLA_PREPROCESSOR_ON);
            if(x != null){
                preprocessorOn = x;
            }
            inputLine.removeSwitchAndValue(CLA_PREPROCESSOR_ON);
        }
        if (isRunScript) {
            runScriptPath = inputLine.getNextArgFor(CLA_RUN_SCRIPT_ON);
            inputLine.removeSwitchAndValue(CLA_RUN_SCRIPT_ON);
        }
        state.setAllowBaseFunctionOverrides(qe.isAllowOverwriteBaseFunctions());
        boolean isVerbose = qe.isWSVerboseOn();
        showBanner = qe.isShowBanner();
        if (inputLine.hasArg(CLA_SHOW_BANNER)) {
            // allows for override.
            Boolean raw = inputLine.getBooleanNextArgFor(CLA_SHOW_BANNER);
            if(raw != null){
                showBanner = raw;
            }
            inputLine.removeSwitchAndValue(CLA_SHOW_BANNER);
        }
        logoName = qe.getLogoName();
        if (inputLine.hasArg(CLA_LOGO)) {
            // allow override of logo from command line
            logoName = inputLine.getNextArgFor(CLA_LOGO).toLowerCase();
            inputLine.removeSwitchAndValue(CLA_LOGO);
        }
        logo = Banners.getLogo(logoName); // check for logo after show banner since they can select none and turn it anyway.

        logger = qe.getMyLogger();

        if (qe.getWSHomeDir().isEmpty() && rootDir == null) {
            // So no home directory was set on the command line either. Use the invocation directory
            rootDir = System.getProperty("user.dir");
            qe.setWsHomeDir(System.getProperty("user.dir"));
        } else {
            rootDir = qe.getWSHomeDir();
        }
        State state = getState(); // This sets it for the class it will be  put in the interpreter below.
        state.createSystemConstants();
        state.createSystemInfo(qe);
        state.getOpEvaluator().setNumericDigits(qe.getNumericDigits());
        bufferManager.state = getState();

        env = new XProperties();
        String loadEnv = null;
        if (inputLine.hasArg("-env")) {
            // overrides config file. Used by scripts e.g.
            loadEnv = inputLine.getNextArgFor("-env");
        } else {
            loadEnv = qe.getWSEnv();
        }
        if (loadEnv != null && !loadEnv.isEmpty()) {
            envFile = resolveAgainstRoot(loadEnv);
            if (envFile.exists()) {
                if (envFile.isFile()) {
                    if (envFile.canRead()) {
                        env.load(envFile);
                    } else {
                        warn("The specified environment file " + loadEnv + " is not readable!");

                    }
                } else {
                    warn("The specified environment file " + loadEnv + " is not a file!");

                }
            } else {
                warn("The specified environment file " + loadEnv + " does not exist");

            }
        }
        String testSaveDir;
        if (qe.getSaveDir() != null) {
            testSaveDir = qe.getSaveDir();
        } else {
            testSaveDir = resolvePath(rootDir, "var/ws");
        }
        if (!getState().isServerMode()) {
            if (exists(getState(), testSaveDir) && isDirectory(getState(), testSaveDir)) {
                saveDir = testSaveDir;
            }

            if (testSaveDir == null) {
                env.put("save_dir", "(empty)");
            } else {
                env.put("save_dir", testSaveDir);
            }
        }
        if (!isRunScript()) {
            splashScreen();
        }
        QDLConfigurationLoaderUtils.setupVFS(qe, getState());

        setEchoModeOn(qe.isEchoModeOn());
        setPrettyPrint(qe.isPrettyPrint());
        getState().setLibPath(qe.getLibPath());
        getState().setModulePaths(qe.getModulePath());
        String[] foundModules = setupModules(qe, getState());
        // Just so the user can see it in the properties after load.
        if (foundModules[JAVA_MODULE_INDEX] != null && !foundModules[JAVA_MODULE_INDEX].isEmpty()) {
            if (showBanner && !isRunScript && isVerbose) {
                say("loaded java modules:");
                StringTokenizer t = new StringTokenizer(foundModules[JAVA_MODULE_INDEX], ",");
                while (t.hasMoreTokens()) {
                    sayi(t.nextToken().trim());
                }
            }
            env.put("java_modules", foundModules[JAVA_MODULE_INDEX]);
        }
        if (foundModules[QDL_MODULE_INDEX] != null && !foundModules[QDL_MODULE_INDEX].isEmpty()) {
            if (showBanner && !isRunScript && isVerbose) {
                say("loaded QDL modules:");
                StringTokenizer t = new StringTokenizer(foundModules[QDL_MODULE_INDEX], ",");
                while (t.hasMoreTokens()) {
                    sayi(t.nextToken().trim());
                }
            }

            env.put("qdl_modules", foundModules[QDL_MODULE_INDEX]);
        }
        if (foundModules[MODULE_FAILURES_INDEX] != null && !foundModules[MODULE_FAILURES_INDEX].isEmpty()) {
            if (!isRunScript && isVerbose) {
                say("failed to load modules:");
                StringTokenizer t = new StringTokenizer(foundModules[MODULE_FAILURES_INDEX], ",");
                while (t.hasMoreTokens()) {
                    sayi(t.nextToken().trim());
                }
            }
            say("Check the log " + getLogger().getFileName() + " for more information");
        }
        String bf = QDLConfigurationLoaderUtils.runBootScript(qe, getState());
        if (bf != null) {
            if (isVerbose) {
                say("loaded boot script '" + bf + "'");
            }
            env.put("boot_script", bf);
        }
        interpreter = new QDLInterpreter(env, getState());
        interpreter.setEchoModeOn(qe.isEchoModeOn());
        interpreter.setPrettyPrint(qe.isPrettyPrint());
        getState().setScriptPaths(qe.getScriptPath());
        getState().setEnableLibrarySupport(qe.isEnableLibrarySupport());
        defaultInterpreter = interpreter;
        getState().setEnableLibrarySupport(qe.isEnableLibrarySupport());
        defaultState = state;
        runScript(inputLine); // run any script if that mode is enabled.
        setAutosaveOn(qe.isAutosaveOn());
        setAutosaveMessagesOn(qe.isAutosaveMessagesOn());
        setAutosaveInterval(qe.getAutosaveInterval());
        setExternalEditorName(qe.getExternalEditorPath());
        setUseExternalEditor(qe.isUseExternalEditor());
        setQdlEditors(qe.getQdlEditors());
        initAutosave();
    }

    protected void figureOutFont(InputLine inputLine) {
        if (inputLine.hasArg("-font")) {
            String fontName = inputLine.getNextArgFor("-font");
            inputLine.removeSwitchAndValue("-font");
            Font currentFont = getFont();
            if (!currentFont.getName().equals(fontName)) {
                try {
                    Font newFont = new Font(fontName, currentFont.getStyle(), currentFont.getSize());
                    setFont(newFont);
                } catch (Throwable t) {
                    if (isDebugOn()) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }

    AutosaveThread autosaveThread;

    protected void initAutosave() {
        if (getState().isServerMode() || getState().isRestrictedIO()) {
            return; // absolutely refuse to turn this feature on in server or restrict IO mode.
        }
        if (isAutosaveOn()) {
            if (autosaveThread == null) {
                autosaveThread = new AutosaveThread(this);
                autosaveThread.start();
            }
        }
    }

    private void testXMLWriter(boolean doFile, String filename) throws Throwable {
        Writer w = null;
        if (doFile) {
            if (isTrivial(filename)) {
                filename = DebugUtil.getDevPath() + "/qdl/language/src/main/resources/ws-test.xml";
            }
            File file = new File(filename);
            w = new FileWriter(file);
        } else {

            w = new StringWriter();
        }
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xsw = xof.createXMLStreamWriter(w);
        toXML(xsw);
        if (doFile) {
            System.out.println("wrote file " + filename);
        } else {
            System.out.println(XMLUtils.prettyPrint(w.toString()));
        }
        xsw.flush();
        xsw.close();
    }

    Date startTimeStamp = new Date(); // default is now

    /**
     * Bootstraps the whole thing.
     *
     * @param inputLine
     */
    public void init(InputLine inputLine) throws Throwable {
        if (getIoInterface() != null) {
            getIoInterface().setBufferingOn(true);
        }
        ClassMigrator.init(); // for now...
        if (!getLibLoaders().isEmpty()) {
            for (LibLoader loader : getLibLoaders()) {
                loader.add(getState());
            }
        }
        // Set up the help.
        InputStream helpStream = getClass().getResourceAsStream("/func_help.xml");
        if (helpStream == null) {
            say("No help available. Could not load help file.");
        } else {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(helpStream);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("entry");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getAttribute("id");
                    String altName = eElement.getAttribute("alt");
                    if (!StringUtils.isTrivial(altName)) {
                        altLookup.put(name, altName);
                    }
                    Node x = eElement.getElementsByTagName("body")
                            .item(0);
                    Node child = x.getFirstChild().getNextSibling();
                    CharacterData cd = (CharacterData) child;
                    if (cd != null && cd.getTextContent() != null) {
                        onlineHelp.put(name, cd.getTextContent());
                    }
                    // Process examples
                    //x = eElement.getElementsByTagName("basic").item(0);
                    x = eElement.getElementsByTagName("example").item(0);
                    if (x != null) {
                        child = x.getFirstChild().getNextSibling();
                        cd = (CharacterData) child;
                        if (cd != null && cd.getTextContent() != null) {
                            onlineExamples.put(name, cd.getTextContent());
                        }
                    }
                }
            }
            helpStream.close();
            // now add the editor help
            helpStream = getClass().getResourceAsStream("/editor_help.txt");
            String x = "(missing help)";
            try {
                x = QDLFileUtil.isToString(helpStream);
                helpStream.close();
            } catch (IOException iox) {
                if (isDebugOn()) {
                    iox.printStackTrace();
                }
            }
            onlineHelp.put("editor", x);
        }
        if (inputLine.hasArg(CONFIG_FILE_FLAG)) {
            fromConfigFile(inputLine);
            return;
        }
        fromCommandLine(inputLine);

    }

    public boolean isRunScript() {
        return isRunScript;
    }

    boolean isRunScript = false;
    String runScriptPath = null;
    Editors qdlEditors;
    String logo = Banners.TIMES;


    protected void fromCommandLine(InputLine inputLine) throws Throwable {
        boolean isVerbose = inputLine.hasArg(CLA_VERBOSE_ON);
        if (isVerbose) {
            say("Verbose mode enabled.");
        }
        inputLine.removeSwitch(CLA_VERBOSE_ON);
        boolean isDebug = inputLine.hasArg(CLA_DEBUG_ON);
        inputLine.removeSwitch(CLA_DEBUG_ON);
        if (isDebug) {
            say("Debug mode enabled.");
        }
        isRunScript = inputLine.hasArg(CLA_RUN_SCRIPT_ON);
        if (isRunScript) {
            runScriptPath = inputLine.getNextArgFor(CLA_RUN_SCRIPT_ON);
            inputLine.removeSwitchAndValue(CLA_RUN_SCRIPT_ON);
        }

        if (inputLine.hasArg(CLA_SHOW_BANNER)) {
            Boolean raw = inputLine.getBooleanNextArgFor(CLA_SHOW_BANNER);
            if(raw != null ) {
                showBanner = raw;
            }
            inputLine.removeSwitch(CLA_SHOW_BANNER);
        }
        if(inputLine.hasArg(CLA_PREPROCESSOR_ON)){
            Boolean raw = inputLine.getBooleanNextArgFor(CLA_PREPROCESSOR_ON);
            if(raw != null ) {
                preprocessorOn = raw;
            }
            inputLine.removeSwitch(CLA_PREPROCESSOR_ON);
        }
        logoName = "default";
        if (inputLine.hasArg(CLA_LOGO)) {
            logoName = inputLine.getNextArgFor(CLA_LOGO).toLowerCase();
            logo = Banners.getLogo(logoName);
            inputLine.removeSwitchAndValue(CLA_LOGO);
        }
        // Make sure logging is in place before actually setting up the state,
        // so the state has logging.
        LoggerProvider loggerProvider = null;
        if (inputLine.hasArg(CLA_HOME_DIR)) {
            File f = new File(inputLine.getNextArgFor(CLA_HOME_DIR));

            if (f.isAbsolute()) {
                rootDir = inputLine.getNextArgFor(CLA_HOME_DIR);
            } else {
                rootDir = System.getProperty("user.dir");
            }
            inputLine.removeSwitchAndValue(CLA_HOME_DIR);
        } else {
            rootDir = System.getProperty("user.dir");
        }
        figureOutFont(inputLine);
        if (inputLine.hasArg(CLA_LOG_DIR)) {
            // create the logger for this
            String rawLog = inputLine.getNextArgFor(CLA_LOG_DIR);
            File log = new File(rawLog);
            File f;
            if (log.isAbsolute()) {
                f = log;
            } else {
                f = resolveAgainstRoot(inputLine.getNextArgFor(CLA_LOG_DIR));
            }
            inputLine.removeSwitchAndValue(CLA_LOG_DIR);
            loggerProvider = new LoggerProvider(f.getAbsolutePath(),
                    "qdl logger",
                    1,
                    1000000,
                    true,
                    true,
                    Level.INFO);
        } else {
            File f = resolveAgainstRoot("qdl_log.xml");
            loggerProvider = new LoggerProvider(f.getAbsolutePath(),
                    "cli logger",
                    1,
                    1000000,
                    true,
                    true,
                    Level.INFO);
        }
        logger = loggerProvider.get();
        State state = getState();
        state.createSystemConstants();
        state.createSystemInfo(null);
        bufferManager.state = getState();  // make any file operations later will succeed.

        env = new XProperties();

        if (inputLine.hasArg(CLA_ENVIRONMENT)) {
            // try and see if the file resolves first.
            envFile = resolveAgainstRoot(inputLine.getNextArgFor(CLA_ENVIRONMENT));
            inputLine.removeSwitchAndValue(CLA_ENVIRONMENT);

            if (envFile.exists()) {
                env.load(envFile);
            }
            // set some useful things.
            env.put("qdl_root", rootDir);
        }


        // Do the splash screen here so any messages from a boot script are obvious.
        if (!isRunScript()) {
            // But no screen of any sort if running a single script.
            splashScreen();
        }
        // Get environment and set up the interpreter.
        if (inputLine.hasArg(TRACE_ARG)) {
            say("trace enabled");
            setDebugOn(true);
            DebugUtil.setIsEnabled(true);
            DebugUtil.setDebugLevel(DebugConstants.DEBUG_LEVEL_TRACE);
            inputLine.removeSwitch(TRACE_ARG);
        }
        interpreter = new QDLInterpreter(env, getState());
        if (inputLine.hasArg(CLA_MODULES)) {
            // -ext "edu.uiuc.ncsa.qdl.extensions.EGLoaderImpl"
            String loaderClasses = inputLine.getNextArgFor(CLA_MODULES);
            inputLine.removeSwitchAndValue(CLA_MODULES);
            StringTokenizer st = new StringTokenizer(loaderClasses, ",");
            String targetModule;
            String foundClasses = "";
            boolean isFirst = true;
            while (st.hasMoreTokens()) {
                targetModule = st.nextToken();
                try {
                    Class klasse = state.getClass().forName(targetModule);
                    QDLLoader loader = (QDLLoader) klasse.getDeclaredConstructor().newInstance();
                    // Do not import everything on start as default so user can set up aliases.
                    setupJavaModule(state, loader, false);

                    //setupJavaModule(state, loader, false);
                    if (isVerbose) {
                        say("loaded module:" + klasse.getSimpleName());
                    }
                    if (isFirst) {
                        isFirst = false;
                        foundClasses = targetModule;
                    } else {
                        foundClasses = foundClasses + "," + targetModule;
                    }
                } catch (Throwable t) {
                    // try it as a module
                    try {
                        interpreter.execute(SystemEvaluator.MODULE_LOAD + "('" + targetModule + "');");
                    } catch (Throwable tt) {
                        if (isDebug) {
                            tt.printStackTrace();
                        }

                        say("WARNING: module '" + targetModule + "' could not be loaded:" + tt.getMessage());
                    }
                }
            }
            if (!foundClasses.isEmpty()) {
                env.put("externalModules", foundClasses);
            }
        }

        interpreter.setEchoModeOn(true);
        if (inputLine.hasArg(CLA_BOOT_SCRIPT)) {
            String bootFile = inputLine.getNextArgFor(CLA_BOOT_SCRIPT);
            inputLine.removeSwitchAndValue(CLA_BOOT_SCRIPT);

            try {
                String bootScript = readFileAsString(bootFile);
                interpreter.execute(bootScript);
                if (isVerbose) {
                    say("loaded boot script " + bootFile);
                }
                env.put("boot_script", bootFile);
            } catch (Throwable t) {
                if (isDebug) {
                    t.printStackTrace();
                }
                say("warning: Could not load boot script'" + bootFile + "': " + t.getMessage());
            }
        }
        if (inputLine.hasArg(CLA_SCRIPT_PATH)) {
            getState().setScriptPaths(inputLine.getNextArgFor(CLA_SCRIPT_PATH));
            inputLine.removeSwitchAndValue(CLA_SCRIPT_PATH);
        }
        if (inputLine.hasArg(CLA_LIB_PATH)) {
            getState().setLibPath(inputLine.getNextArgFor(CLA_LIB_PATH));
            inputLine.removeSwitchAndValue(CLA_LIB_PATH);
        }
        if (inputLine.hasArg(CLA_MODULE_PATH)) {
            getState().setModulePaths(inputLine.getNextArgFor(CLA_MODULE_PATH));
            inputLine.removeSwitchAndValue(CLA_MODULE_PATH);
        }

        // Support for WS attributes
        // Format is -WS:key value, where key is a standard attribute like pp, debug, etc

        for (String x : inputLine.argsToStringArray()) {
            if (x.toUpperCase().startsWith(WS_ARG_CAPUT)) {
                String key = x.substring(WS_ARG_CAPUT.length());
                String value = inputLine.getNextArgFor(x);
                _wsSet(new InputLine(")ws", "set", key, value)); // construct set command
                inputLine.removeSwitchAndValue(x);
            }
        }
        if (inputLine.hasArg(CLA_MACRO)) {
            String macro = inputLine.getNextArgFor(CLA_MACRO);
            macro = macro.replace("\\n", "\n");

            inputLine.removeSwitchAndValue(CLA_MACRO);
            Polyad polyad = new Polyad(SystemEvaluator.WS_MACRO);
            polyad.addArgument(new ConstantNode(asQDLValue(macro)));
            try {
                //getInterpreter().execute(SystemEvaluator.WS_MACRO + "(" + macro + ");");
                polyad.evaluate(getState());
            } catch (Throwable t) {
                if (isDebug) {
                    t.printStackTrace();
                }
                say("There was a problem executing the macro '" + macro + "' at startup.");
            }

        }
        runScript(inputLine); // If there is a script, run it.
    }

    public static final String WS_ARG_CAPUT = "-WS:";

    /**
     * Runs the script from the command line if the -run argument is passed.
     * Contract is that there is the argument is of the form
     * <pre>-run path_to_script x y z ... </pre>
     * path_to_script is the name of QDL file. Below it is referred to as {@link #runScriptPath}
     * and x,y,z,... are passed to the script. This <b>must</b> be the last thing on the command line,
     * so it's processing is positional.
     * <br/><br/>
     * Note especially that {@link #runScriptPath} is set as state for the workspace, but normally not settable by
     * the user.
     *
     * @param inputLine
     */
    private void runScript(InputLine inputLine) {
        if (isRunScript) {

            ArrayList<String> argList = new ArrayList<>();

            // At this point, everything left in the input line is supposed
            // to be an argument to the script.
            // zero-th inputline argument is the name of the calling function. Omit
            for (int i = 1; i < inputLine.size(); i++) {
                argList.add(inputLine.getArg(i));
            }
            QDLStem argStem = new QDLStem();
            argStem.addList(argList);
            getState().setScriptArgStem(argStem);
            getState().setScriptName(runScriptPath);
            try {
                List<String> lines = readFileAsLines(runScriptPath);
                StringBuffer stringBuffer = new StringBuffer();
                for (String line : lines) {
                    if (!line.matches(VFSEntry.SHEBANG_REGEX)) {
                        stringBuffer.append(line + "\n");
                    }
                }
                //String runScript = QDLFileUtil.readFileAsString(runScriptPath);
                String runScript = stringBuffer.toString();
                if (runScript != null && !runScript.isEmpty()) {
                    interpreter.execute(runScript);
                    System.exit(0); // make sure to use this so external programs (like shell scripts) know all is ok
                }
            } catch (Throwable t) {
                if (!SystemEvaluator.newInterruptHandler && (t instanceof InterruptException)) {
                    InterruptException ie = (InterruptException) t;
                    InterruptUtil.createInterrupt(ie, null);
                    InterruptUtil.printSetupMessage(this, ie);
                    try {
                        getWorkspace().mainLoop();
                        System.exit(0); // exit once they exit the main loop

                    } catch (Throwable e) {
                        // it is possible the script has a return. Handle it gracefully
                        if (e instanceof ReturnException) {
                            // script cammed return(X), so return the agument.
                            ReturnException rx = (ReturnException) e;
                            if (rx.resultType != Constant.NULL_TYPE) {
                                getIoInterface().println(rx.result);
                                getIoInterface().flush();
                            }
                            System.exit(0); // exit once they exit the main loop
                        }
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }
                if (t instanceof ParsingException) {
                    ParsingException pe = (ParsingException) t;
                    say("Parsing exception: in " + pe.getScriptName() + " at line " + pe.getLineNumber() + ": '" + pe.getMessage() + "'");
                    return;
                }
                if (t instanceof ReturnException) {
                    // script cammed return(X), so return the agument.
                    ReturnException rx = (ReturnException) t;
                    if (rx.resultType != Constant.NULL_TYPE) {
                        getIoInterface().println(rx.result);
                        getIoInterface().flush();
                    }
                    System.exit(0); // Best we can do. Java does not allow for returned values.
                }
                getState().getLogger().error(t);
                say("Error executing script '" + runScriptPath + "'" + (t.getMessage() == null ? "." : ":" + t.getMessage()));
                System.exit(1); // So external programs can tell that something didn't work right.
            }
        }
    }

    String rootDir = null;
    String saveDir = null;


    public String readline(String prompt) {
        try {
            return getIoInterface().readline(prompt);
        } catch (IOException iox) {
            if (DebugUtil.isEnabled()) {
                iox.printStackTrace();
            }
            throw new QDLException("Error reading input:" + iox.getMessage());
        } catch (ArrayIndexOutOfBoundsException ax) {
            if (DebugUtil.isEnabled()) {
                ax.printStackTrace();
            }
            throw new QDLException("Error reading input:" + ax.getMessage());
        }
    }

    public boolean isEchoModeOn() {
        return echoModeOn;
    }

    public void setEchoModeOn(boolean echoModeOn) {
        this.echoModeOn = echoModeOn;
    }

    boolean echoModeOn = true;

    public String readline() {
        try {
            String x = getIoInterface().readline(null);
            if (x.equals(EXIT_COMMAND)) {
                throw new ExitException(EXIT_COMMAND + " encountered");
            }
            return x;

        } catch (IOException iox) {
            throw new GeneralException("Error, could not read the input line due to IOException", iox);
        }
    }

    public IOInterface getIoInterface() {
        return getState().getIoInterface();
    }

    public void setIoInterface(IOInterface ioInterface) {
        getState().setIoInterface(ioInterface);
    }

    public void toXML(XMLStreamWriter xsw) throws XMLStreamException {
        WSXMLSerializer serializer = new WSXMLSerializer();
        serializer.toXML(this, xsw);
    }

    public JSONObject toJSON() throws Throwable {
        WSJSONSerializer serializer = new WSJSONSerializer();
        return serializer.toJSON(this);
    }

    public WorkspaceCommands fromJSON(JSONObject jsonObject) throws Throwable {
        WSJSONSerializer serializer = new WSJSONSerializer();
        return serializer.fromJSON(jsonObject);
    }

    /**
     * This takes an updated {@link WorkspaceCommands} object and updates the currently
     * active workspace. When this is done, there is new state and all the values
     * ofr newCommnads have been migrated. This takes care to get the {@link IOInterface}
     * right since if that is not handled correctly, the entire workspace hangs unrecoverably.
     *
     * @param newCommands
     * @return
     */
    protected boolean updateWSState(WorkspaceCommands newCommands) {
        try {
            IOInterface ioInterface = getIoInterface();
            State oldState = getState();
            newCommands.getState().injectTransientFields(oldState);
            // later this is injected into the state. Set it here or custom IO fails later.
            newCommands.setIoInterface(ioInterface);
            state = newCommands.getState();
            state.setIoInterface(ioInterface);
            // now setup the workspace constants
            setDebugOn(newCommands.isDebugOn());
            setEchoModeOn(newCommands.isEchoModeOn());
            setPrettyPrint(newCommands.isPrettyPrint());
            state.createSystemInfo(null);
            state.createSystemConstants();

            currentPID = newCommands.currentPID;
            wsID = newCommands.wsID;
            description = newCommands.description;
            currentWorkspace = newCommands.currentWorkspace;
            rootDir = newCommands.rootDir;
            saveDir = newCommands.saveDir;
            commandHistory = newCommands.commandHistory;
            autosaveInterval = newCommands.getAutosaveInterval();
            autosaveMessagesOn = newCommands.isAutosaveMessagesOn();
            autosaveOn = newCommands.isAutosaveOn();
            runInitOnLoad = newCommands.runInitOnLoad;
            if (autosaveOn) {
                if (currentWorkspace == null) {
                    say("warning you need to set " + CURRENT_WORKSPACE_FILE + " then enable autosave. Autosave is off.");
                    autosaveOn = false;
                } else {
                    if (autosaveThread == null) {
                        autosaveThread = new AutosaveThread(this);
                        autosaveThread.setStopThread(false);
                        autosaveThread.start();
                    }
                }

            }
            startTimeStamp = newCommands.startTimeStamp;
            interpreter = new QDLInterpreter(env, newCommands.getState());
            interpreter.setEchoModeOn(newCommands.isEchoModeOn());
            interpreter.setPrettyPrint(newCommands.isPrettyPrint());
            bufferManager = newCommands.bufferManager;
            bufferManager.state = state;
            bufferDefaultSavePath = newCommands.bufferDefaultSavePath;
            if (ioInterface instanceof ISO6429IO) {
                ISO6429IO iso6429IO = (ISO6429IO) ioInterface;
                iso6429IO.clearCommandBuffer();
                iso6429IO.addCommandHistory(newCommands.commandHistory);
            }
            // Fix for https://github.com/ncsa/qdl/issues/70
            //QDLWorkspace qdlWorkspace = new QDLWorkspace(newCommands);
            QDLWorkspace qdlWorkspace = QDLWorkspace.newInstance(newCommands);
            newCommands.setWorkspace(qdlWorkspace);
            return true;
        } catch (Throwable t) {
            // This should return a nice message to display.
            // It is possible that the workspace cannot even pick itself off the floor in which case
            // the state or even the logger might not exist.
            if (getState() != null && getState().getLogger() != null) {
                getState().getLogger().error("Could not deserialize workspace:" + t.getMessage(), t);
            }
            if (isDebugOn()) {
                t.printStackTrace();
            }
            //return false;
            throw t;
        }
    }

    public boolean fromXML(XMLEventReader xer, boolean skipBadModules) throws XMLStreamException {
        WSXMLSerializer serializer = new WSXMLSerializer();
        WorkspaceCommands newCommands = null;
        newCommands = serializer.fromXML(xer, skipBadModules);
        return updateWSState(newCommands);
    }

    public boolean isAutosaveOn() {
        return autosaveOn;
    }

    public void setAutosaveOn(boolean autosaveOn) {
        this.autosaveOn = autosaveOn;
    }

    boolean autosaveOn;

    public long getAutosaveInterval() {
        return autosaveInterval;
    }

    public void setAutosaveInterval(long autosaveInterval) {
        this.autosaveInterval = autosaveInterval;
    }

    long autosaveInterval;

    boolean autosaveMessagesOn;

    public boolean isAutosaveMessagesOn() {
        return autosaveMessagesOn;
    }

    public void setAutosaveMessagesOn(boolean autosaveMessagesOn) {
        this.autosaveMessagesOn = autosaveMessagesOn;
    }

    public boolean isAssertionsOn() {
        return assertionsOn;
    }

    public void setAssertionsOn(boolean assertionsOn) {
        this.assertionsOn = assertionsOn;
        getState().setAssertionsOn(assertionsOn);
    }

    boolean assertionsOn = true;

    public boolean isAnsiModeOn() {
        return ansiModeOn;
    }

    public void setAnsiModeOn(boolean ansiModeOn) {
        this.ansiModeOn = ansiModeOn;
    }

    boolean ansiModeOn = false;

    /**
     * This is really only a {@link SwingTerminal} or SASterminal.
     *
     * @return
     */
    public SwingTerminal getSwingTerminal() {
        return swingTerminal;
    }

    public void setSwingTerminal(SwingTerminal swingTerminal) {
        this.swingTerminal = swingTerminal;
    }


    transient SwingTerminal swingTerminal;

    public static WorkspaceCommandsProvider getWorkspaceCommandsProvider() {
        if (workspaceCommandsProvider == null) {
            workspaceCommandsProvider = new WorkspaceCommandsProvider();
        }
        return workspaceCommandsProvider;
    }

    public static void setWorkspaceCommandsProvider(WorkspaceCommandsProvider workspaceCommandsProvider) {
        WorkspaceCommands.workspaceCommandsProvider = workspaceCommandsProvider;
    }

    static WorkspaceCommandsProvider workspaceCommandsProvider = null;

    static WorkspaceCommands workspaceCommands = null;

    /**
     * Factory method to create an instance. This is needed if you intend to override
     * this class, since there is a bootstrapping issue with the QDLWorkspace
     * otherwise. set this as needed first.
     *
     * @return
     */
    public static WorkspaceCommands getInstance() {
        if (workspaceCommands == null) {
            workspaceCommands = getWorkspaceCommandsProvider().get();
        }
        return workspaceCommands;
    }

    public static WorkspaceCommands getInstance(IOInterface ioInterface) {
        if (workspaceCommands == null) {
            workspaceCommands = getWorkspaceCommandsProvider().get(ioInterface);
        }
        return workspaceCommands;
    }

    public static void setInstance(WorkspaceCommands wc) {
        workspaceCommands = wc;
    }

    /**
     * Use this to create new instances of this with same {@link IOInterface} as the instance. The idea is
     * that the static factory creates
     * a single instance and that can be used to create others. This allows for overrides
     * to be used in the base classes. Set the static method once and override the non-static methods.
     *
     * @return
     */

    public WorkspaceCommands newInstance() {
        WorkspaceCommands ww = getWorkspaceCommandsProvider().get(getInstance().getIoInterface());
        return ww;
    }

    public WorkspaceCommands newInstance(IOInterface ioInterface) {
        //return new WorkspaceCommands(ioInterface);
        return getWorkspaceCommandsProvider().get(ioInterface);
    }


    public Font getFont() {
        if (font == null) {
            font = new Font("Monospaced", Font.BOLD, 14);
        }
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    Font font = null;

    /**
     * Override for custom {@link LibLoader}s. These will be processed in the order given.
     *
     * @return
     */
    public List<LibLoader> getLibLoaders() {
        return new ArrayList<>();
    }

    public SIEntry getCurrentSIEntry() {
        return getSIEntries().get(getCurrentPID());
    }
}
