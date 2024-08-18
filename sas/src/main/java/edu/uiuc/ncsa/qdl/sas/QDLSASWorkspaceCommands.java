package edu.uiuc.ncsa.qdl.sas;

import edu.uiuc.ncsa.qdl.sas.response.EditResponse;
import edu.uiuc.ncsa.qdl.workspace.BufferManager;
import edu.uiuc.ncsa.qdl.workspace.WorkspaceCommands;

import java.util.List;

import static edu.uiuc.ncsa.security.core.util.StringUtils.listToString;

/**
 * Extends the QDL Workspace commands. These return {@link edu.uiuc.ncsa.sas.thing.response.Response}s
 * rather than op codes and the system will process responses accordingly.
 * <p>Created by Jeff Gaynor<br>
 * on 8/26/22 at  10:19 AM
 */
public class QDLSASWorkspaceCommands extends WorkspaceCommands {
    @Override
    protected Object invokeEditor(BufferManager.BufferRecord br, List<String> result) {
        String content = br.hasContent()?listToString(br.getContent()):"";
        EditResponse editResponse = new EditResponse(content);
        editResponse.setAlias(br.alias);
        editResponse.setEditObjectType(QDLSASConstants.KEY_EDIT_OBJECT_TYPE_BUFFER);
        return editResponse;
    }

    @Override
    public Object editVariable(List<String> inputForm, List<String> output, String varName, boolean isText, boolean isStem) {
        String content = listToString(inputForm);
        EditResponse editResponse = new EditResponse(content);
        editResponse.setAlias(varName);
        editResponse.setArgState((isText ? 1 : 0) + (isStem ? 2 : 0));
        editResponse.setEditObjectType(QDLSASConstants.KEY_EDIT_OBJECT_TYPE_VARIABLE);
        return editResponse;
        //return super.editVariable(inputForm, output, varName, isText, isStem);
    }

    @Override
    public Object editFunction(List<String> inputForm, List<String> output, String fName, int argCount) {
        String content = listToString(inputForm);
        EditResponse editResponse = new EditResponse(content);
        editResponse.setAlias(fName);
        editResponse.setArgState(argCount);
        editResponse.setEditObjectType(QDLSASConstants.KEY_EDIT_OBJECT_TYPE_FUNCTION);
        return editResponse;
    }

/*    @Override
    public Object restoreFile(List<String> input, String fileName) {
        String content = listToString(input);
        EditResponse editResponse = new EditResponse(content);
        editResponse.setAlias(fileName);
        editResponse.setEditObjectType(QDLSASConstants.KEY_EDIT_OBJECT_TYPE_FILE);
        return editResponse;
    }*/
    public Object editFile(List<String> input, List<String> output, String fileName) {
        String content = listToString(input);
        EditResponse editResponse = new EditResponse(content);
        editResponse.setAlias(fileName);
        editResponse.setEditObjectType(QDLSASConstants.KEY_EDIT_OBJECT_TYPE_FILE);
        return editResponse;

    }

}
