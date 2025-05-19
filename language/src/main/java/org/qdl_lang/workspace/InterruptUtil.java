package org.qdl_lang.workspace;

import edu.uiuc.ncsa.security.util.cli.IOInterface;
import org.qdl_lang.exceptions.InterruptException;
import org.qdl_lang.exceptions.ReturnException;
import org.qdl_lang.state.SIEntry;
import org.qdl_lang.variables.Constant;

public class InterruptUtil {
    /**
     * For setting up the SIEntry and adding it to the set of siEntries.
     * This is the first stop in getting the SI (state indicator) working.
     * Both the {@link org.qdl_lang.workspace.WorkspaceCommands.SIEntries}
     * and {@link SIEntry} are updated, and information can be gotten from the
     * {@link InterruptException}.
     *
     * @param ie
     * @param siInterrupts set of interrupts user specified initially
     */
    public static void createInterrupt(InterruptException ie,
                                       SIInterrupts siInterrupts) {
        WorkspaceCommands.SIEntries siEntries = WorkspaceCommands.getInstance().getSIEntries(); // since it's static
        int nextPID = siEntries.nextKey();
        SIEntry siEntry = ie.getSiEntry();
        siEntry.pid = nextPID;
        siEntries.put(nextPID, siEntry);
        if (siInterrupts != null) {
            siEntry.setInterrupts(siInterrupts);
        }
    }

    /**
     *  After the resume command, this updates the entry with the next {@link org.qdl_lang.exceptions.InterruptException}'s
     * state. Print out the update message if needed.
     * @param ix
     * @param sie
     */
    public static void updateSIE(InterruptException ix, SIEntry sie) {
        sie.statementNumber = ix.getSiEntry().statementNumber;
        sie.message = ix.getSiEntry().message;
        sie.statement = ix.getSiEntry().statement;
        sie.timestamp = ix.getSiEntry().timestamp;
    }
    public static void printUpdateMessage(WorkspaceCommands workspaceCommands,SIEntry sie) {
        if (workspaceCommands.siMessagesOn) {
            if (sie.statement.hasTokenPosition()) {
                workspaceCommands.say(sie.message + ": at line " + sie.statement.getTokenPosition().line);
            } else {
                workspaceCommands.say(sie.message);
            }
        }
    }
    public static void printSetupMessage(WorkspaceCommands workspaceCommands, InterruptException ie) {
        if (workspaceCommands.siMessagesOn) {
            workspaceCommands.say(ie.getSiEntry().message + " at line " + ie.getStatement().getTokenPosition().line);
        }
        workspaceCommands.say("pid: " + ie.getSiEntry().pid);
    }

    public static class SIThread extends Thread {
        public SIThread(InterruptException ie, org.qdl_lang.state.State qdlState) {
            this.ie = ie;
            this.qdlState = qdlState;
        }

        InterruptException ie;
        public WorkspaceCommands getWorkspaceCommands() {
            return getQDLState().getWorkspaceCommands();
        }


        public org.qdl_lang.state.State getQDLState() {
            return qdlState;
        }

        org.qdl_lang.state.State qdlState;

        protected QDLWorkspace getWorkspace() {
            return getWorkspaceCommands().getWorkspace();
        }

        protected IOInterface getIO() {
            return getWorkspaceCommands().getIoInterface();
        }

        public Throwable getLastException() {
            return lastException;
        }

        public void setLastException(Throwable lastException) {
            this.lastException = lastException;
        }

        Throwable lastException;
        @Override
        public void run() {
            try {
                getWorkspace().mainLoop();
                return;
            } catch (Throwable e) {
                // it is possible the script has a return. Handle it gracefully
                if (e instanceof ReturnException) {
                    // script called return(X), so return the agument.
                    ReturnException rx = (ReturnException) e;
                    if (rx.resultType != Constant.NULL_TYPE) {
                        getIO().println(rx.result);
                        getIO().flush();
                    }
                //    System.exit(0); // exit once they exit the main loop
                }
                if(qdlState.isDebugOn()) {
                    e.printStackTrace();
                }
                lastException = e;
            }
        }
    }
}
