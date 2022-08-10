package edu.uiuc.ncsa.qdl.gui;

/**
 * For Swing implementation of {@link edu.uiuc.ncsa.security.util.cli.IOInterface}.
 * This manages to collect all lines of input from the Swing GUI and forward it
 * at the appropriate time to the {@link edu.uiuc.ncsa.qdl.workspace.QDLWorkspace}.
 * <p>Created by Jeff Gaynor<br>
 * on 8/4/22 at  11:17 AM
 */
public class Data {
    private String packet = null;

    // True if receiver should wait
    // False if sender should wait
    private boolean receiverWaits = true;

    public synchronized String receive() {
        while (receiverWaits) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        receiverWaits = true;

        String returnPacket = packet;
        notifyAll();
        return returnPacket;
    }

    public synchronized void send(String packet) {
        while (!receiverWaits) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        receiverWaits = false;

        this.packet = packet;
        notifyAll();
    }
}