package org.qdl_lang.sas;

import org.qdl_lang.sas.action.GetHelpTopicAction;
import org.qdl_lang.sas.action.ListFunctionsAction;
import org.qdl_lang.sas.response.EditResponse;
import org.qdl_lang.sas.response.GetHelpTopicResponse;
import org.qdl_lang.sas.response.ListFunctionsResponse;
import org.qdl_lang.sas.response.QDLSASResponseDeserializer;
import org.qdl_lang.workspace.WorkspaceCommands;
import edu.uiuc.ncsa.sas.thing.response.LogonResponse;
import edu.uiuc.ncsa.sas.thing.response.OutputResponse;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.sas.webclient.Client;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.qdl_lang.gui.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import static edu.uiuc.ncsa.sas.webclient.Client.FLAG_NEW;
import static edu.uiuc.ncsa.sas.webclient.Client.say;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/26/22 at  9:16 AM
 */
public class QDLSASTerminal extends SwingTerminal implements QDLSASConstants {
    public QDLSASTerminal() {
        // note that init is called in the super of this constructor,
        // so adding it here results in multiple listeners.
    }

    @Override
    protected void setupListeners() {
        getInput().getCaret().setVisible(true);
        getInput().addKeyListener(new QDLSASKeyCharAdapter(getWorkspaceCommands().getSwingTerminal()));
        getInput().addKeyListener(new QDLSASHistoryKeyAdapter(getWorkspaceCommands(),
                frame, getInput(), getOutput()));
        getOutput().addKeyListener(new QDLSASHistoryKeyAdapter(getWorkspaceCommands(), frame, getInput(), getOutput()));
    }

    /*
        protected void setupListeners() {
        input.getCaret().setVisible(true);
        //  input.setSyntaxEditingStyle(SYNTAX_STYLE_JAVA); // does weird things to comments. Makes them all javadoc
        input.addKeyListener(new QDLCharKeyAdapter(this));
        input.addKeyListener(new QDLHistoryKeyAdapter(getWorkspaceCommands(), frame, getInput(), getOutput()));
        output.addKeyListener(new QDLHistoryKeyAdapter(getWorkspaceCommands(), frame, getInput(), getOutput()));
    }
     */
    @Override
    protected void init() {

        // setup IO. Has to be done before everything else.
        data = new Data();
        qdlSwingIO = new QDLSwingIO(this, data);
        qdlioThread = new Thread(qdlSwingIO);
        //  qdlioThread.start();
    }

    Data data;
    QDLSwingIO qdlSwingIO;
    Thread qdlioThread;

    protected static void showHelp() {
        say(QDLSASTerminal.class.getSimpleName() + " [" + FLAG_NEW + " [filename]] | " + Client.FLAG_CONFIG + " filename");
        say("Starts the GUI OR allows you to create a configuration first.");
        say("To create a new configuration, start with the " + FLAG_NEW + " and fill in the configuration.");
        say("You will be prompted if you want to start the GUI");
    }

    /**
     * If this is started with the -new flag (may or may not have an argument that is the file name)
     * then the user will be prompted at the command line to create a new configuration and then asked
     * if they want to start the system.
     *
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable {
        Vector v = new Vector();
        v.add("dummy"); // so the command line switches are all found. Dummy method name
        for (String arg : args) {
            v.add(arg);
        }

        InputLine inputLine = new InputLine(v); // now we can use this.
        if (inputLine.hasArg(Client.FLAG_HELP)) {
            showHelp();
            return;
        }
        if (inputLine.hasArg(FLAG_NEW)) {
            String filename = null;
            if (inputLine.hasNextArgFor(FLAG_NEW)) {
                filename = inputLine.getNextArgFor(FLAG_NEW);
                if (filename.startsWith("-")) {
                    // assumption is that they did not actually supply a filename.
                    filename = null;
                    inputLine.removeSwitch(FLAG_NEW);
                } else {
                    inputLine.removeSwitchAndValue(FLAG_NEW);
                }
            } else {
                inputLine.removeSwitch(FLAG_NEW);
            }
            filename = Client.createConfig(filename, true);
            if (Client.getInput("Did you want to run this configuration(y/n)?").equals("n")) {
                return;
            }
            String[] allArgs = inputLine.argsToStringArray();
            ArrayList<String> argList = new ArrayList();
            argList.add(Client.FLAG_CONFIG);
            argList.add(filename);
            argList.addAll(Arrays.asList(allArgs));
            inputLine = new InputLine(argList);
        }
        JFrame frame = new JFrame("QDL Terminal");
        QDLSASTerminal qdlTerminal = new QDLSASTerminal();
        qdlTerminal.frame = frame;
        frame.setContentPane(qdlTerminal.getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/qdl", "org.qdl_lang.gui.flex.QDLSyntax");
        qdlTerminal.getInput().setSyntaxEditingStyle("text/qdl");
        qdlTerminal.setupSAS(inputLine);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) dimension.getWidth() * 3 / 4;
        int h = (int) dimension.getHeight() * 3 / 4;
        frame.setSize(w, h);
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
        String laf = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(laf);
        frame.setVisible(true);
        qdlTerminal.runSAS();
    }

    private void runSAS() throws Throwable {
        sasClient.doLogon();
    }

    protected void setupSAS(InputLine inputLine) throws Throwable {
        sasClient = Client.newInstance(inputLine);
        sasClient.setResponseDeserializer(new QDLSASResponseDeserializer());
        // shut it down. It has to start first
        if (qdlioThread != null) {
            qdlioThread.interrupt();
        }
        LogonResponse logonResponse = (LogonResponse) sasClient.doLogon();
        ListFunctionsResponse listFunctionsResponse = (ListFunctionsResponse) sasClient.execute(new ListFunctionsAction());
        CompletionProvider provider = QDLSwingUtil.createCompletionProvider(listFunctionsResponse.getFunctions());
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(getInput());
        setupWS(new InputLine());
        getWorkspaceCommands().setSwingTerminal(this);
        setupListeners();
    }

    public class QDLSASHistoryKeyAdapter extends QDLHistoryKeyAdapter {
        public QDLSASHistoryKeyAdapter(WorkspaceCommands workspaceCommands, JFrame frame, RSyntaxTextArea input, JTextArea output) {
            super(workspaceCommands, frame, input, output);
        }

        @Override
        protected String getHelp(String text) {
            GetHelpTopicAction getHelpTopicAction = new GetHelpTopicAction(text);
            try {
                GetHelpTopicResponse helpTopicResponse = (GetHelpTopicResponse) sasClient.execute(getHelpTopicAction);
                return createHelpMessage(helpTopicResponse.getFunctionHelp(), helpTopicResponse.getHelp(), helpTopicResponse.getExample());

            } catch (Throwable t) {
                t.printStackTrace();
                return "uh-oh:" + t.getMessage();
            }
        }
    }

    public class QDLSASKeyCharAdapter extends QDLCharKeyAdapter {
        public QDLSASKeyCharAdapter(SwingTerminal swingTerminal) {
            super(swingTerminal);
        }

        @Override
        protected void doSend(String current) {
            Response response = null;
            try {
                response = sasClient.doExecute(current);
                clearCurrentLine();
                getInput().setText(null);
                getPrompt().setText(null);
                switch (response.getResponseType()) {
                    case QDLSASConstants.RESPONSE_TYPE_OUTPUT:
                        getOutput().setText(((OutputResponse) response).getContent());
                        break;
                    case RESPONSE_TYPE_EDIT:
                        EditResponse editResponse = (EditResponse) response;
                        new QDLSASEditor(sasClient, editResponse);
                        break;
                    default:
                        getOutput().setText("");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    Client sasClient;

    protected void processResponse(Response response) {
        switch (response.getResponseType()) {
            case RESPONSE_TYPE_EDIT:
                EditResponse editResponse = (EditResponse) response;
                QDLSASEditor qdlEditor = new QDLSASEditor(sasClient, editResponse);
                qdlEditor.setup(editResponse.getContent());
                break;
        }
    }

}
