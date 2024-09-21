package org.qdl_lang.workspace;

/**
 * Provides a new instance of a {@link QDLWorkspace}.
 */
public interface WorkspaceProvider {
    QDLWorkspace getWorkspace(WorkspaceCommands workspaceCommands);
}
