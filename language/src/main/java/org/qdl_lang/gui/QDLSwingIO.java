package org.qdl_lang.gui;

import edu.uiuc.ncsa.security.util.cli.IOInterface;

import java.io.IOException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/4/22 at  7:50 AM
 */
public class QDLSwingIO implements IOInterface, Runnable   {
    TerminalInterface terminal;

    private Data load;

    public QDLSwingIO(TerminalInterface terminal, Data load) {
        this.load = load;
        this.terminal = terminal;
    }

    String receivedMessage;
    @Override
    public void run() {
 /*  If running a test, enable this, otherwise it runs in the readline method.
     for(String receivedMessage = load.receive();
          !"End".equals(receivedMessage);
          receivedMessage = load.receive()) {
            System.out.println("input = " + receivedMessage);
        }*/

    }


    @Override
    public String readline(String s) throws IOException {
        terminal.setPrompt(s);
        return readline();
    }

    @Override
    public String readline() throws IOException {
        for(String receivedMessage = load.receive();
            !"End".equals(receivedMessage);
            receivedMessage = load.receive()) {
            return receivedMessage;
        }
        return receivedMessage;
    }

    @Override
    public void print(Object o) {
        terminal.setResultText(terminal.getResultText() + o);
    }

    @Override
    public void println(Object o) {
       print(o + "\n");
    }

    @Override
    public void flush() {

    }

    @Override
    public void clearQueue() {

    }

    @Override
    public boolean isQueueEmpty() {
        return false;
    }

    @Override
    public void setBufferingOn(boolean b) {

    }

    @Override
    public boolean isBufferingOn() {
        return false;
    }
}
