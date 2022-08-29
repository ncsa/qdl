package edu.uiuc.ncsa.qdl.sas;

import edu.uiuc.ncsa.qdl.gui.Data;
import edu.uiuc.ncsa.qdl.gui.QDLSwingIO;
import edu.uiuc.ncsa.qdl.gui.QDLSwingUtil;
import edu.uiuc.ncsa.qdl.gui.SwingTerminal;
import edu.uiuc.ncsa.qdl.sas.action.GetHelpTopicAction;
import edu.uiuc.ncsa.qdl.sas.action.ListFunctionsAction;
import edu.uiuc.ncsa.qdl.sas.response.EditResponse;
import edu.uiuc.ncsa.qdl.sas.response.GetHelpTopicResponse;
import edu.uiuc.ncsa.qdl.sas.response.ListFunctionsResponse;
import edu.uiuc.ncsa.qdl.sas.response.QDLSASResponseDeserializer;
import edu.uiuc.ncsa.sas.thing.response.LogonResponse;
import edu.uiuc.ncsa.sas.thing.response.OutputResponse;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.sas.webclient.Client;
import edu.uiuc.ncsa.security.util.cli.InputLine;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

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
    protected void init() {
        getInput().getCaret().setVisible(true);
        getInput().addKeyListener(new QDLSASKeyCharAdapter());
        getInput().addKeyListener(new QDLSASHistoryKeyAdapter());

        getOutput().addKeyListener(new QDLHistoryKeyAdapter());

        // setup IO. Has to be done before everything else.
        data = new Data();
        qdlSwingIO = new QDLSwingIO(this, data);
        qdlioThread = new Thread(qdlSwingIO);
        //qdlioThread.start();
    }

    Data data;
    QDLSwingIO qdlSwingIO;
    Thread qdlioThread;

    public static void main(String[] args) throws Throwable {
        Vector v = new Vector();
        v.add("dummy"); // so the command line switches are all found. Dummy method name
        for (String arg : args) {
            v.add(arg);
        }
        InputLine inputLine = new InputLine(v); // now we can use this.
        JFrame frame = new JFrame("QDL Terminal");
        QDLSASTerminal qdlTerminal = new QDLSASTerminal();
        qdlTerminal.frame = frame;
        frame.setContentPane(qdlTerminal.getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/qdl", "edu.uiuc.ncsa.qdl.gui.flex.QDLSyntax");
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
    }

    /*
        protected void setupWS(InputLine inputLine) throws Throwable {

      State state = new State();
      CompletionProvider provider = QDLSwingUtil.createCompletionProvider(state);
      AutoCompletion ac = new AutoCompletion(provider);
      ac.install(input);
      state.setIoInterface(qdlSwingIO);
      workspaceCommands = new WorkspaceCommands(qdlSwingIO);
      workspaceCommands.setState(state);
      qdlWorkspace = new QDLWorkspace(workspaceCommands);
      workspaceCommands.setWorkspace(qdlWorkspace);
      try {
          workspaceCommands.init(new InputLine());
      } catch (Throwable e) {
          e.printStackTrace();
      }
      //qdlWorkspace.workspaceCommands = workspaceCommands;
      //this.qdlWorkspace = qdlWorkspace;

  }
     */
    public class QDLSASHistoryKeyAdapter extends QDLHistoryKeyAdapter {
        @Override
        protected String getHelp(String text) {
            GetHelpTopicAction getHelpTopicAction = new GetHelpTopicAction(text);
            try {
                GetHelpTopicResponse helpTopicResponse = (GetHelpTopicResponse) sasClient.execute(getHelpTopicAction);
                return createHelpMessage(helpTopicResponse.getHelp(), helpTopicResponse.getExample());

            } catch (Throwable t) {
                t.printStackTrace();
                return "uh-oh:" + t.getMessage();
            }
        }
    }

    public class QDLSASKeyCharAdapter extends QDLCharKeyAdapter {
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
                        EditResponse editResponse = (EditResponse)response;
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
