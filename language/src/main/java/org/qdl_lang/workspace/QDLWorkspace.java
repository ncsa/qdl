package org.qdl_lang.workspace;

import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.qdl_lang.config.QDLEnvironment;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.gui.SwingTerminal;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.uiuc.ncsa.security.util.cli.BasicIO;
import edu.uiuc.ncsa.security.util.cli.CommandLineTokenizer;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import edu.uiuc.ncsa.security.util.terminal.ISO6429IO;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;

import static org.qdl_lang.config.QDLConfigurationConstants.*;
import static org.qdl_lang.workspace.WorkspaceCommands.*;

/**
 * This has the machinery for getting lines of input from the user and then feeding them to the
 * parser. It will also hand off workspace commands as needed.
 * <p>Created by Jeff Gaynor<br>
 * on 1/11/20 at  4:21 PM
 */
public class QDLWorkspace implements Serializable {

    public QDLWorkspace(WorkspaceCommands workspaceCommands) {
        this.workspaceCommands = workspaceCommands;
    }

    public WorkspaceCommands getWorkspaceCommands() {
        return workspaceCommands;
    }

    public static QDLWorkspace newInstance(WorkspaceCommands workspaceCommands) {
        return getWorkspaceProvider().getWorkspace(workspaceCommands);
    }

    public static WorkspaceProvider getWorkspaceProvider() {
        if(workspaceProvider == null){
            workspaceProvider = new WorkspaceProviderImpl();
        }
        return workspaceProvider;
    }

    public static void setWorkspaceProvider(WorkspaceProvider workspaceProvider) {
        QDLWorkspace.workspaceProvider = workspaceProvider;
    }

    static WorkspaceProvider workspaceProvider;


    /**
     * This is used only during deserialization of the workspace. Setting it
     * any other time may lead to very bad results!
     * @param workspaceCommands
     */
    public void setWorkspaceCommands(WorkspaceCommands workspaceCommands) {
        this.workspaceCommands = workspaceCommands;
    }

    WorkspaceCommands workspaceCommands;

    protected MyLoggingFacade getLogger() {
        return workspaceCommands.logger;
    }

    protected void handleException(Throwable t) {
        if (workspaceCommands.isDebugOn()) {
            t.printStackTrace();
        }
        if (getLogger() != null) {
            getLogger().error(t);
        }
        String errorStatement = "";
        if (t instanceof QDLExceptionWithTrace) {
            QDLExceptionWithTrace qq = (QDLExceptionWithTrace) t;
            if (qq.hasStatement()) {
                if (qq.getStatement().hasTokenPosition()) {
                    errorStatement = getErrorCoordinates(qq);
                }
                if (t.getCause() != null) {
                    t = qq.getCause();
                }
            }
        }
        if (t instanceof ContinueException) {
            return; // ignore it
        }
        if (t instanceof BreakException) {
            return;//ignore it
        }
        if (t instanceof ReturnException) {
            // Can only get one here if the user enters it on the command line
            workspaceCommands.say(((ReturnException) t).result.toString());
            return;
        }
        if (t instanceof UnknownSymbolException) {
            workspaceCommands.say(t.getMessage() + errorStatement);
            return;
        }
        if (t instanceof StackOverflowError) {
            workspaceCommands.say("Error " + errorStatement + ": Stack overflow" );
            return;
        }
        if (t instanceof UndefinedFunctionException) {
            workspaceCommands.say(t.getMessage() + errorStatement);
            return;
        }
        if (t instanceof InterruptException) {
            InterruptException ie = (InterruptException)t;
            if(getWorkspaceCommands().getInterpreter() == ie.getSiEntry().interpreter){
                workspaceCommands.say("sorry, cannot interrupt main workspace process.");
                return;
            }
            InterruptUtil.createInterrupt(ie, null);
            InterruptUtil.printSetupMessage(getWorkspaceCommands(), ie);
            return; // or the interrupt exception gets a nicely printed stack trace
        }
        if (t instanceof ParsingException) {
            ParsingException parsingException = (ParsingException) t;
            String message;
            if(parsingException.isKeywordError()){
                 message = parsingException.getMessage();

            }else{
                 message = parsingException.getType() + " error";
            }
            if (parsingException.hasScriptName()) {
                message = message + " in script '" + parsingException.getScriptName() + "'";
            }
            if (parsingException.getLineNumber() == -1) {
                message = message + ":";
            } else {
                message = message + " at (" + parsingException.getLineNumber() + "," + parsingException.getCharacterPosition() + ") ";
            }
            message = message + (workspaceCommands.isDebugOn() ? t.getMessage() : "could not parse input");
            workspaceCommands.say(message);
            return;
        }
        if ((t instanceof ParseCancellationException)) {
            if (t.getMessage().contains("extraneous") || t.getMessage().contains("mismatched")) {
                workspaceCommands.say("syntax error: Unexpected or illegal character.");
            } else {
                workspaceCommands.say("syntax error: " + (workspaceCommands.isDebugOn() ? t.getMessage() : "could not parse input"));
            }
            return;
        }
        if (t instanceof IllegalStateException) {
            workspaceCommands.say("illegal state " + errorStatement + ": " + t.getMessage() );
            return;
        }
        if (t instanceof IllegalArgumentException) {
            workspaceCommands.say("illegal argument " + errorStatement + ": " + t.getMessage());
            return;
        }
        if (t instanceof AssertionException) {
            workspaceCommands.say("assertion failed " + errorStatement + ": '" + t.getMessage() + "'");
            return;
        }

        if (t instanceof QDLException) {
            workspaceCommands.say("exception " + errorStatement + ": " + t.getMessage());
            return;
        }
        // In case a jar is corrupted (e.g. maven builds it wrong, partial upgrade failed, so missing dependency)
        // since the first symptom is this. Without this case, it falls through and the user just gets a random error
        // since the first symptom is this. Without this case, it falls through and the user just gets a random error
        // that whatever component failed, not that the component was actually missing.
        if (t instanceof NoClassDefFoundError) {
            workspaceCommands.say("internal error: Missing classes. Did you try an upgrade while QDL is running? (" + t.getMessage() + ")");
            return;
        }
        // Final fall through case.
        if (t.getMessage() == null) {
            workspaceCommands.say("error!");
        } else {
            workspaceCommands.say("error: " + t.getMessage() + errorStatement);
        }
    }

    protected String getErrorCoordinates(QDLExceptionWithTrace qq) {
        Statement statement = qq.getStatement();
        if (!statement.hasTokenPosition()) {
            return "";
        }
        String out = " at (" + statement.getTokenPosition().line + ", " + statement.getTokenPosition().col + ")";
        if (qq.isScript()) {
            out = out + " in:\n" + qq.stackTrace();
        }
        return out;
    }

    public Object execute(String input) {
        if (input == null) {
            // about the only way to get a null here is if the user is piping in
            // something via std in and it hits the end of the stream.
            if (workspaceCommands.isDebugOn()) {
                workspaceCommands.say("exiting");
            }
            return true;
        }
        input = input.trim();
        boolean storeLine = true;
        String out = null;
        if (input.equals(HISTORY_COMMAND) || input.startsWith(HISTORY_COMMAND + " ")) {
            try {
                out = doHistory(input);
            } catch (Throwable t) {
                workspaceCommands.say("could not not do history command:" + t.getMessage());
            }
            if (out == null) {
                // nothing in the buffer
                return true;
            }
            storeLine = false;
        }
        if (input.equals(REPEAT_COMMAND) || input.startsWith(REPEAT_COMMAND + " ")) {
            out = doRepeatCommand(input);
            storeLine = out == null;
        }

        if (storeLine) {
            // Store it if it was not retrieved from the command history.
            workspaceCommands.commandHistory.add(0, input);
        } else {
            input = out; // repeat the command
        }

        // One idea is to strip off comments in parser, E.g. process
        //
        //    2+2 // basic
        // into
        //    2+2
        // so user does not have to type
        //   2+2; // basic
        //
        // but the regex here needs to be
        // quite clever to match ' and // within them (e.g. any url in a string fails at the command line).
        // Another option is to write a small parser in antlr for the command line...

        //    input = input.split("//")[0]; // if there is a line comment, strip it.
     /*            if (input.equals("%")) {
                     input = lastCommand;
                 } else {
                     lastCommand = input;
                 }

                 */

        if (input.startsWith(")")) {
            Object rc = workspaceCommands.execute(input);
            if (rc instanceof Response) {
                return rc;
            }
            if (rc instanceof Integer) {
                int rc12 = (Integer) rc;
                switch (rc12) {
                    case RC_EXIT_NOW:
                        return false; // exit now, darnit.
                    case RC_NO_OP:
                    case RC_CONTINUE:
                        return true;
                    case RC_RELOAD:
                        workspaceCommands.say("not quite ready for prime time. Check back later");
                }
            }
        }

        boolean echoMode = workspaceCommands.isEchoModeOn();
        boolean prettyPrint = workspaceCommands.isPrettyPrint();

        try {
            if (input != null && !input.isEmpty()) {
                if(workspaceCommands.isPreprocessorOn()){
                input = workspaceCommands.applyTemplates(input);
            }

                // if you try to evaluate only a ";" then you will get a syntax exception from
                // the parser for an empty statement.
                if (workspaceCommands.isEchoModeOn() && !input.endsWith(";")) {
                    input = input + ";"; // add it since they forgot
                }
                workspaceCommands.getInterpreter().execute(input);
            }
        } catch (Throwable t) {

            // If there is an exception while local mode is running, we don't want to trash the user's
            // echo mode, since that causes every subsequent command to fail at least until they
            // figure it `out and turn it back on.
            workspaceCommands.setEchoModeOn(echoMode);
            workspaceCommands.setPrettyPrint(prettyPrint);
            handleException(t);
        }
        return true;
    }

    /**
     * Execute main event loop.
     *
     * @throws Throwable
     */
    public void mainLoop() throws Throwable {
        boolean keepLooping = true;

        // Main loop. The default is to be running QDL commands and if there is a
        // command to the workspace, then it gets forwarded. 
        while (keepLooping) {
            String input;
            input = workspaceCommands.readline(INDENT);
            //  System.out.println("  got from readline:" + input);
            keepLooping = (Boolean) execute(input);
        }
    }

    public void runMacro(List<String> commands) throws Throwable {
        //   boolean isExit = false;
        for (String command : commands) {
            if (command.startsWith(")")) {
                Object r = workspaceCommands.execute(command);
                if (r instanceof Integer) {
                    int rc = (Integer) r;
                    switch (rc) {
                        case RC_EXIT_NOW:
                            // isExit = true;
                            return; // exit now, darnit.
                        case RC_NO_OP:
                        case RC_CONTINUE:
                            continue;
                        case RC_RELOAD:
                            workspaceCommands.say("not quite ready for prime time. Check back later");
                    }
                }

            }
            boolean echoMode = workspaceCommands.isEchoModeOn();
            boolean prettyPrint = workspaceCommands.isPrettyPrint();

            try {
                if (command != null && !command.isEmpty()) {
                    // if you try to evaluate only a ";" then you will get a syntax exception from
                    // the parser for an empty statement.
                    if (workspaceCommands.isEchoModeOn() && !command.endsWith(";")) {
                        command = command + ";"; // add it since they forgot
                    }
                    workspaceCommands.getInterpreter().execute(command);
                }
            } catch (Throwable t) {

                // If there is an exception while local mode is running, we don't want to trash the user's
                // echo mode, since that causes every subsequent command to fail at least until they
                // figure it `out and turn it back on.
                workspaceCommands.setEchoModeOn(echoMode);
                workspaceCommands.setPrettyPrint(prettyPrint);
                handleException(t);
            }
        }
    }

    protected String doRepeatCommand(String cmdLine) {
        if (cmdLine.contains("--help")) {
            workspaceCommands.say(REPEAT_COMMAND + " = repeat the last command. Identical to " + HISTORY_COMMAND + " 0");
            return null;
        }
        cmdLine = cmdLine.substring(REPEAT_COMMAND.length());
        if (0 < workspaceCommands.commandHistory.size()) {
            String current = workspaceCommands.commandHistory.get(0);
            if (cmdLine.trim().length() == 0) {
                return current;
            }
            current = current + " " + cmdLine;
            workspaceCommands.commandHistory.add(0, current);
            return current;
            //return workspaceCommands.commandHistory.get(0);
        }
        workspaceCommands.say("no commands found");
        return null;
    }

    public static String HISTORY_CLEAR_SWITCH = "-clear";
    public static String HISTORY_TRUNCATE_SWITCH = "-keep";
    public static String HISTORY_SAVE_SWITCH = "-save";
    public static String HISTORY_SIZE_SWITCH = "-size";
    public static String HISTORY_REVERSE_SWITCH = "-reverse";
    public static String SHORT_HISTORY_REVERSE_SWITCH = "-r";

    protected String doHistory(String cmdLine) {
        if (cmdLine.contains("--help")) {
            workspaceCommands.say(HISTORY_COMMAND + " [int | " + HISTORY_CLEAR_SWITCH + " | " + HISTORY_TRUNCATE_SWITCH + " n] [more]");

            workspaceCommands.say("(no args) {" + HISTORY_REVERSE_SWITCH + "|" + SHORT_HISTORY_REVERSE_SWITCH + "}" + " show complete history, executing nothing");
            workspaceCommands.say("    The normal order is from most recent. Including the switch reverses the order");
            // Funny spaces below make it all line up when printed.
            workspaceCommands.say("    int = the command at the given index.");
            workspaceCommands.say(" " + HISTORY_CLEAR_SWITCH + " = clear the entire history.");
            workspaceCommands.say(HISTORY_TRUNCATE_SWITCH + " n = keep n elements in the history, dropping the rest.");
            workspaceCommands.say(HISTORY_SAVE_SWITCH + " file_name {" + HISTORY_REVERSE_SWITCH + "|" + SHORT_HISTORY_REVERSE_SWITCH + "} {-m message} - write entire history to a file. Remember that the order");
            workspaceCommands.say("    is from most recent to first entered. If the " + HISTORY_REVERSE_SWITCH + " is present the order is ");
            workspaceCommands.say("    original entry order");
            workspaceCommands.say(HISTORY_SIZE_SWITCH + "  - returns the number of commands in the current history.");
            workspaceCommands.say("[more]  - you may add additional commands to be appended to the given history element.\n" +
                    "A new entry in the command history is made.");

            workspaceCommands.say("E.g.:");
            workspaceCommands.say("    )h 4 =: a");
            workspaceCommands.say("would execute element 4 from the command history and set the value to a.");
            workspaceCommands.say("See also:" + REPEAT_COMMAND);
            return null; // do nothing
        }

        CommandLineTokenizer CLT = workspaceCommands.CLT;
        Vector v = CLT.tokenize(cmdLine);
        InputLine inputLine = new InputLine(v);
        if (inputLine.hasArg(HISTORY_SIZE_SWITCH)) {
            workspaceCommands.say(workspaceCommands.commandHistory.size() + " entries");
            return null;
        }
        boolean truncate = inputLine.hasArg(HISTORY_TRUNCATE_SWITCH);
        boolean isSave = inputLine.hasArg(HISTORY_SAVE_SWITCH);
        boolean isReverse = inputLine.hasArg(HISTORY_REVERSE_SWITCH) || inputLine.hasArg(SHORT_HISTORY_REVERSE_SWITCH);
        boolean hasMessage = inputLine.hasArg("-m");
        String message = null;
        if (hasMessage) {

            message = inputLine.getNextArgFor("-m");
        }
        inputLine.removeSwitchAndValue("-m");
        // in case they sent one in the wrong place, remove and ignore.
        inputLine.removeSwitch(HISTORY_REVERSE_SWITCH);
        inputLine.removeSwitch(SHORT_HISTORY_REVERSE_SWITCH);
        String saveFile = null;
        if (isSave) {
            saveFile = inputLine.getNextArgFor(HISTORY_SAVE_SWITCH);
            inputLine.removeSwitchAndValue(HISTORY_SAVE_SWITCH);
        }
        int truncateIndex = -1;
        if (isSave) {
            List<String> newList;
            if (isReverse) {
                newList = new ArrayList<>();
                newList.addAll(workspaceCommands.commandHistory);
                Collections.reverse(newList);

            } else {
                newList = workspaceCommands.commandHistory;
            }
            QDLStem stemVariable = new QDLStem();
            if (hasMessage) {
                stemVariable.put(0, message);
            }
            stemVariable.addList(newList);
            try {
                QDLFileUtil.writeStemToFile(saveFile, stemVariable);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new QDLException("Could not save command history", t);
            }
            return null;
        }
        if (truncate) {
            String truncateIndexString = inputLine.getNextArgFor(HISTORY_TRUNCATE_SWITCH);

            try {
                truncateIndex = Integer.parseInt(truncateIndexString);
            } catch (NumberFormatException nfx) {
                workspaceCommands.say("sorry, but the argument to " + HISTORY_TRUNCATE_SWITCH + " was not an integer.");
                return null;

            }
            inputLine.removeSwitchAndValue(HISTORY_TRUNCATE_SWITCH);
        }
        if (truncate) {
            if (truncateIndex < 0) {
                workspaceCommands.say("negative index. skipping...");
                return null;
            }
            if (truncateIndex < workspaceCommands.commandHistory.size()) {
                // next is an idiom, to clear this range from the history. It uses the fact that
                // sublist is view of the list, not another object.
                int numberRemoved = workspaceCommands.commandHistory.size() - truncateIndex;
                workspaceCommands.commandHistory.subList(truncateIndex, workspaceCommands.commandHistory.size()).clear();
                workspaceCommands.say(numberRemoved + " history entries removed.");
                return null;

            }
        }
        boolean clearHistory = inputLine.hasArg(HISTORY_CLEAR_SWITCH);
        inputLine.removeSwitch(HISTORY_CLEAR_SWITCH);
        if (clearHistory) {
            workspaceCommands.commandHistory = new ArrayList<>();
            workspaceCommands.say("history cleared");
            return null;
        }

        boolean printIt = true;
        StringTokenizer st = new StringTokenizer(cmdLine, " ");
        st.nextToken(); // This is the "/h" which we already know about

        if (st.hasMoreTokens()) {
            try {
                int lineNo = Integer.parseInt(st.nextToken());
                String rest = "";
                while (st.hasMoreTokens()) {
                    rest = rest + " " + st.nextToken();
                }
                lineNo = (lineNo < 0) ? (workspaceCommands.commandHistory.size() + lineNo) : lineNo;

                if (rest.trim().length() == 0) {
                    if (0 <= lineNo && lineNo < workspaceCommands.commandHistory.size()) {
                        return workspaceCommands.commandHistory.get(lineNo);
                    }
                } else {
                    if (0 <= lineNo && lineNo < workspaceCommands.commandHistory.size()) {
                        String current = workspaceCommands.commandHistory.get(lineNo) + rest;
                        workspaceCommands.commandHistory.add(0, current);
                        return current;
                    }
                }

            } catch (Throwable t) {
                // do nothing, just print out the history.
            }
        }
        if (printIt) {
            List<String> h;
            if (isReverse) {
                h = new ArrayList<>();
                h.addAll(workspaceCommands.commandHistory);
                Collections.reverse(h);
            } else {
                h = workspaceCommands.commandHistory;
            }
            for (int i = 0; i < h.size(); i++) {
                // an iterator actually prints these in reverse order. Print them in order.
                workspaceCommands.say(i + ": " + h.get(i));
            }
        }
        return null;
    }


    //  {'x':{'a':'b'},'c':'d'} ~ {'y':{'p':'q'},'r':'s'}
    public static void main(String[] args) throws Throwable {
        // hook for extensions of this class. Do any setup, then call init.
             QDLWorkspace qdlWorkspace = init(args);
             if(qdlWorkspace != null){
                 // a null response means that this is being run as a script, so there
                 // is no workspace main event loop to run
                 qdlWorkspace.mainLoop();
             }
    }
    protected static QDLWorkspace init(String[] args) throws Throwable {
        Vector<String> vector = new Vector<>();
        vector.add("dummy"); // Dummy zero-th arg.
        for (String arg : args) {
            vector.add(arg);
        }
        InputLine argLine = new InputLine(vector); // now we have a bunch of utilities for this
        //WorkspaceCommands workspaceCommands = WorkspaceCommands.getInstance();
        WorkspaceCommands workspaceCommands = getWorkspaceCommandsProvider().get();
        if (argLine.hasArg(CONFIG_FILE_FLAG)) {
            String cfgname = argLine.hasArg(CONFIG_NAME_FLAG) ? argLine.getNextArgFor(CONFIG_NAME_FLAG) : "default";
            workspaceCommands.loadQE(argLine, cfgname);
        }
        String terminalType = WS_TERMINAL_TYPE_TEXT;
        if (null != workspaceCommands.qdlEnvironment) {
            // if there is no config file, e.g. command line startup only, then
            // the environment might be null.
            QDLEnvironment qdlEnvironment = workspaceCommands.qdlEnvironment;
            terminalType = qdlEnvironment.getTerminalType(); // has default text
        }

        boolean isSwingGui = false;
        boolean isoTerminal = false;
        // Old format had individual flags for each mode.
        if (argLine.hasArg("-text")) {
            terminalType = WS_TERMINAL_TYPE_TEXT;
            argLine.removeSwitch("-text");
        }
        if (argLine.hasArg("-ansi")) {
            terminalType = WS_TERMINAL_TYPE_ANSI;
            argLine.removeSwitch("-ansi");
        }
        if (argLine.hasArg("-gui")) {
            terminalType = WS_TERMINAL_TYPE_SWING;
            argLine.removeSwitch("-gui");
        }
        // Fix https://github.com/ncsa/qdl/issues/52 let it be overrideable from CLI
        if (argLine.hasArg("-tty")) {
            terminalType = argLine.getNextArgFor("-tty");
            argLine.removeSwitchAndValue("-tty");
        }
        switch (terminalType) {
            case WS_TERMINAL_TYPE_TEXT:
            default:
                break;
            case WS_TERMINAL_TYPE_ANSI:
                isoTerminal = true;
                break;
            case WS_TERMINAL_TYPE_SWING:
                isSwingGui = true;
                break;
        }
        //System.setProperty("org.jline.terminal.dumb", "true"); // kludge for jline
        ISO6429IO iso6429IO = null; // only make one of these if you need it because jLine takes over all IO!
        SwingTerminal swingTerminal = null;
        boolean supportsGUI = !GraphicsEnvironment.isHeadless();
        if (supportsGUI && isSwingGui) {
            try {
                swingTerminal = new SwingTerminal();

                //workspaceCommands = new WorkspaceCommands(swingTerminal.getQdlSwingIO());
                workspaceCommands.setIoInterface(swingTerminal.getQdlSwingIO());
                workspaceCommands.setSwingTerminal(swingTerminal);
            } catch (AWTError awtError) {
                System.out.println("warning -- could not start graphical environment: " + awtError.getMessage());
                isSwingGui = false;
                isoTerminal = false;
                //workspaceCommands = new WorkspaceCommands(new BasicIO());
                workspaceCommands.setIoInterface(new BasicIO());
            }
        } else {
            if (isSwingGui) {
                System.out.println("warning -- no graphics support, defaulting to ansi");
                isoTerminal = true;
                isSwingGui = false;
            }
            if (isoTerminal) {
                try {
                    QDLTerminal qdlTerminal = new QDLTerminal(null);
                    iso6429IO = new ISO6429IO(qdlTerminal, true);
                    workspaceCommands.setIoInterface(iso6429IO);
                    workspaceCommands.setAnsiModeOn(true);
                    WorkspaceCommands.setInstance(workspaceCommands); // Since IO is now setup!!
                    isoTerminal = true;
                } catch (Throwable t) {
                    System.out.println("could not load ANSI terminal: " + t.getMessage());
                    isoTerminal = false;
                    workspaceCommands.setIoInterface(new BasicIO());
                }
            } else {
                //workspaceCommands = workspaceCommands.newInstance(new BasicIO());
                workspaceCommands = getWorkspaceCommandsProvider().get(new BasicIO());
            }
        }
        QDLWorkspace qdlWorkspace = QDLWorkspace.newInstance(workspaceCommands);
        workspaceCommands.setWorkspace(qdlWorkspace);
        workspaceCommands.init(argLine);
        if (workspaceCommands.isRunScript()) {
            return null;
        }
        if ((workspaceCommands.showBanner) && isoTerminal) {
            //System.out.println("ISO 6429 terminal" + iso6429IO.getTerminal().getName());
            System.out.println("ISO 6429 terminal");
        }


        ArrayList<String> functions = new ArrayList<>();
        functions.addAll(qdlWorkspace.workspaceCommands.getState().getMetaEvaluator().listFunctions(false));
        functions.addAll(qdlWorkspace.workspaceCommands.getState().listFunctions(true,
                null, true, false, false));
        if (isoTerminal) {
            // set up command completion
            iso6429IO.setCommandCompletion(functions);
            iso6429IO.getScreenSize(); // Figure this out.
            iso6429IO.setLoggingFacade(workspaceCommands.logger);
        }
        if (isSwingGui) {
            JFrame jFrame = new JFrame();
            swingTerminal.setWorkspaceCommands(workspaceCommands);
            DefaultCompletionProvider defaultCompletionProvider = swingTerminal.setup(jFrame, functions);
            workspaceCommands.getState().setIoInterface(swingTerminal.getQdlSwingIO());
            workspaceCommands.getState().setCompletionProvider(defaultCompletionProvider);

            workspaceCommands.setSwingGUI(true);
            WorkspaceCommands.setInstance(workspaceCommands);// since IO is setup.
            // Add completion with current set of functions from workspace.
        }
      return  qdlWorkspace;
    }

    public static final String MACRO_COMMENT_DELIMITER = "//";

}
