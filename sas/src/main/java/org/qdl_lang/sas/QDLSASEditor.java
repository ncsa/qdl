package org.qdl_lang.sas;

import org.qdl_lang.gui.editor.QDLEditor;
import org.qdl_lang.sas.action.BufferSaveAction;
import org.qdl_lang.sas.response.EditResponse;
import org.qdl_lang.workspace.WorkspaceCommands;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.sas.webclient.Client;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;

/**
 * Slight tweak on the standard {@link QDLEditor} to work with SAS clients.
 * <p>Created by Jeff Gaynor<br>
 * on 8/26/22 at  10:57 PM
 */
public class QDLSASEditor extends QDLEditor {
    Client sasClient;

    public QDLSASEditor(Client sasClient, EditResponse editResponse) {
        this.sasClient = sasClient;
        setLocalName(editResponse.getAlias());
        setType(editResponse.getEditObjectType());
        setArgState(editResponse.getArgState());
        setup(editResponse.getContent());
    }

    public class ControlOperations2 extends ControlOperations {
        public ControlOperations2(WorkspaceCommands workspaceCommands, JFrame frame, RSyntaxTextArea input, JTextArea output) {
            super(workspaceCommands, frame, input, output);
        }

        @Override
        protected void doSave() {
           BufferSaveAction bufferSaveAction = new BufferSaveAction();
            bufferSaveAction.setContent(input.getText());
            bufferSaveAction.setArgState(getArgState());
            bufferSaveAction.setEditObjectType(getType());
            bufferSaveAction.setLocalName(getLocalName());
            try {
                Response response = sasClient.doPost(bufferSaveAction);
            } catch (Throwable ex) {
                JOptionPane.showMessageDialog(jFrame, "could not save buffer:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void init() {
        jFrame = new JFrame();
        input.addKeyListener(new MyKeyAdapter());
        input.addKeyListener(new ControlOperations2(getWorkspaceCommands(),jFrame,input,null));

    }
}
