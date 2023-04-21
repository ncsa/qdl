package edu.uiuc.ncsa.qdl.sas;

import edu.uiuc.ncsa.qdl.exceptions.QDLException;
import edu.uiuc.ncsa.qdl.gui.editor.EditDoneEvent;
import edu.uiuc.ncsa.qdl.sas.action.BufferSaveAction;
import edu.uiuc.ncsa.qdl.sas.action.GetHelpTopicAction;
import edu.uiuc.ncsa.qdl.sas.response.BufferSaveResponse;
import edu.uiuc.ncsa.qdl.sas.response.GetHelpTopicResponse;
import edu.uiuc.ncsa.qdl.sas.response.ListFunctionsResponse;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.workspace.BufferManager;
import edu.uiuc.ncsa.qdl.workspace.QDLWorkspace;
import edu.uiuc.ncsa.sas.Executable;
import edu.uiuc.ncsa.sas.StringIO;
import edu.uiuc.ncsa.sas.exceptions.SASException;
import edu.uiuc.ncsa.sas.thing.action.Action;
import edu.uiuc.ncsa.sas.thing.action.ExecuteAction;
import edu.uiuc.ncsa.sas.thing.response.OutputResponse;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.security.core.exceptions.NotImplementedException;
import edu.uiuc.ncsa.security.util.cli.IOInterface;
import edu.uiuc.ncsa.security.util.cli.InputLine;

import java.util.List;

import static edu.uiuc.ncsa.security.core.util.StringUtils.stringToList;

/**
 * This wraps the QDL workspace and turns it into a SAS executable. All QDL commands are run here.
 * <h3>Adding more actions</h3>
 * <p>To add an action,</p>
 * <ol>
 *     <li> extend an {@link Action} and a {@link Response}</li>
 *     <li>Override the {@link edu.uiuc.ncsa.qdl.workspace.WorkspaceCommands}
 *         action you want (e.g. {@link QDLSASWorkspaceCommands#editVariable(List, List, String, boolean, boolean)}
 *         to return the response you made</li>
 *     <li>Add the action to the main event loop in {@link #execute(Action)}</li>
 *     <li>Finally, implement creating the action in the correct component. </li>
 * </ol>
 * <p>Created by Jeff Gaynor<br>
 * on 8/24/22 at  12:01 PM
 */
public class QDLExe implements Executable, QDLSASConstants {
    public QDLExe() {
        init();
    }

    QDLWorkspace qdlWorkspace;

    QDLSASWorkspaceCommands workspaceCommands;
    public State createState(){
        return new State();
    }
    protected void init(){
        State state = createState();
        workspaceCommands = new QDLSASWorkspaceCommands();
        workspaceCommands.setState(state);
        try {
            workspaceCommands.fromConfigFile(new InputLine("foo", "-cfg", "/home/ncsa/dev/csd/config/qdl-cfg.xml"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        qdlWorkspace = new QDLWorkspace(workspaceCommands);

        StringIO stringIO = new StringIO("");
        workspaceCommands.setIoInterface(stringIO);
        try {
            workspaceCommands.init(new InputLine());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        setIO(stringIO);
    }

    IOInterface ioInterface;

    @Override
    public Response execute(Action action) {
        Object rr = null;
        switch (action.getType()) {
            case ACTION_EXECUTE:
                ExecuteAction executeAction = (ExecuteAction) action;
                rr = qdlWorkspace.execute(executeAction.getArg());
                if (rr instanceof Response) {
                    return (Response) rr;
                }
                return new OutputResponse(action, ((StringIO) getIO()).getOutput().toString());
            case ACTION_INVOKE:
                throw new NotImplementedException("invoke needs to be implemented");
            case ACTION_LIST_FUNCTIONS:
                return new ListFunctionsResponse(action, workspaceCommands.getFunctionList());
            case ACTION_GET_HELP_TOPIC:
                GetHelpTopicAction getHelpTopicAction = (GetHelpTopicAction) action;
                GetHelpTopicResponse helpTopicResponse = new GetHelpTopicResponse(action);
                helpTopicResponse.setFunctionHelp(workspaceCommands.getFunctionHelp(getHelpTopicAction.getName()));
                helpTopicResponse.setHelp(workspaceCommands.getHelpTopic(getHelpTopicAction.getName()));
                helpTopicResponse.setExample(workspaceCommands.getHelpTopicExample(getHelpTopicAction.getName()));
                return helpTopicResponse;
            case ACTION_BUFFER_SAVE:
                try {
                    return bufferSave((BufferSaveAction) action);
                } catch (Throwable e) {
                    throw new QDLException("buffer save failed", e);
                }
            default:
                throw new SASException("unknown action \"" + action.getType());
        }

    }

    protected Response bufferSave(BufferSaveAction bsa) throws Throwable {
        BufferSaveResponse bufferSaveResponse = new BufferSaveResponse();
        BufferManager.BufferRecord br;
        switch (bsa.getEditObjectType()) {
            case EditDoneEvent.TYPE_BUFFER:
                br = workspaceCommands.getBufferManager().getBufferRecord(bsa.getLocalName());
                br.setContent(stringToList(bsa.getContent()));
                return bufferSaveResponse;
            case EditDoneEvent.TYPE_FILE:
                workspaceCommands.restoreFile(stringToList(bsa.getContent()), bsa.getLocalName());
                return bufferSaveResponse;
            case EditDoneEvent.TYPE_FUNCTION:
                workspaceCommands.restoreFunction(stringToList(bsa.getContent()), bsa.getLocalName(), bsa.getArgState());
                return bufferSaveResponse;
            case EditDoneEvent.TYPE_VARIABLE:
                int x = bsa.getArgState();
                boolean isText = (x % 2) == 1;
                boolean isStem = (2 <= x);
                workspaceCommands.restoreVariable(bsa.getLocalName(), stringToList(bsa.getContent()), isText, isStem);
                return bufferSaveResponse;
        }
        return bufferSaveResponse;
    }


    @Override
    public IOInterface getIO() {
        return ioInterface;
    }

    @Override
    public void setIO(IOInterface io) {
        this.ioInterface = io;
    }
}
