package org.qdl_lang.workspace;

import edu.uiuc.ncsa.security.util.cli.IOInterface;

import javax.inject.Provider;

public class WorkspaceCommandsProvider implements Provider<WorkspaceCommands> {
    @Override
    public WorkspaceCommands get() {
        return new WorkspaceCommands();
    }

    public WorkspaceCommands get(IOInterface ioInterface) {
        return new WorkspaceCommands(ioInterface);
    }
}
