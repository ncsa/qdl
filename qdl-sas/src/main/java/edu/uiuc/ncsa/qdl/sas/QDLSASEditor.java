package edu.uiuc.ncsa.qdl.sas;

import edu.uiuc.ncsa.qdl.gui.editor.QDLEditor;
import edu.uiuc.ncsa.qdl.sas.action.BufferSaveAction;
import edu.uiuc.ncsa.qdl.sas.response.EditResponse;
import edu.uiuc.ncsa.sas.thing.response.Response;
import edu.uiuc.ncsa.sas.webclient.Client;

import javax.swing.*;

/**
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
        @Override
        protected void saveContent() {
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
        input.addKeyListener(new ControlOperations2());

    }
}
