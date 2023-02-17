package edu.uiuc.ncsa.qdl.state;

import edu.uiuc.ncsa.qdl.exceptions.ReturnException;
import edu.uiuc.ncsa.qdl.scripting.QDLScript;

/**
 * This is used to fork scripts and run them in their own threads.
 * <p>Created by Jeff Gaynor<br>
 * on 2/7/23 at  7:02 AM
 */
public class QDLThread extends Thread{
    public QDLThread(edu.uiuc.ncsa.qdl.state.State qdlState,
                     QDLScript qdlScript,
                     int pid) {
        this.qdlState = qdlState;
        this.qdlScript = qdlScript;
        this.pid = pid;
    }

    edu.uiuc.ncsa.qdl.state.State qdlState; // Note this class contains Thread.State, so FQ the QDL State.


    QDLScript qdlScript;
    int pid;

    @Override
    public void run() {
        try {
            qdlScript.execute(qdlState);
        }catch(Throwable re){
            System.out.println("In QDLThread");
            if(re instanceof ReturnException){
                return;   // cannot return anything... So eat it.
            }
            re.printStackTrace();
        }
        cleanup();
    }

    protected void cleanup(){
        qdlState.getThreadTable().remove(pid);
    }
}
