package org.qdl_lang.workspace;

public class WorkspaceProviderImpl implements WorkspaceProvider{
    @Override
    public QDLWorkspace getWorkspace(WorkspaceCommands workspaceCommands) {
        return new QDLWorkspace(workspaceCommands);
    }
}
