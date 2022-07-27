package edu.uiuc.ncsa.qdl.util;

import edu.uiuc.ncsa.security.util.cli.IOInterface;

import java.io.IOException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 7/27/22 at  2:55 PM
 */
public class SwingTerminal extends Console implements IOInterface {
    @Override
    public String readline(String prompt) throws IOException {
        return null;
    }

    @Override
    public String readline() throws IOException {
        return null;
    }

    @Override
    public void print(Object x) {

    }

    @Override
    public void println(Object x) {

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
    public void setBufferingOn(boolean bufferOn) {

    }

    @Override
    public boolean isBufferingOn() {
        return false;
    }
}
