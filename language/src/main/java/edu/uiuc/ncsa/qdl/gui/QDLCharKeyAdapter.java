package edu.uiuc.ncsa.qdl.gui;

import edu.uiuc.ncsa.qdl.workspace.WorkspaceCommands;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * This will listen for key strokes that are remapped to special characters
 * E.g. alt+e is ∈. This might be re-writable as a key binding but won't
 * be able to use the mechanisms in other terminals, adding the maintenence issues.
 * So it looks to be a lot of work.
 * The main argument for doing this is speed -- at some point this is going to get
 * slow if there is a lot of text to wade through since it has to do surgery
 * on the entire text area. If that happens, a rewrite is in order.
 */
public class QDLCharKeyAdapter extends KeyAdapter {
    private final SwingTerminal swingTerminal;


    public QDLCharKeyAdapter(SwingTerminal swingTerminal) {
        this.swingTerminal = swingTerminal;
    }

    protected void doSend(String current) {
        swingTerminal.data.send(current);
        swingTerminal.clearCurrentLine();
        swingTerminal.getInput().setText(null);
        swingTerminal.getOutput().setText(null);
    }


    @Override
    public void keyTyped(KeyEvent e) {
        String keyValue = String.valueOf(e.getKeyChar());
        int position = swingTerminal.getInput().getCaretPosition();
        switch (e.getKeyChar()) {
            case KeyEvent.VK_PLUS:
           if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD && e.isControlDown()) {
               System.out.println("Numpad pressed!");
               Font oldFont = swingTerminal.getInput().getFont();
               Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), oldFont.getSize() + 2);
               swingTerminal.getInput().setFont(newFont);
           }
           break;
            case KeyEvent.VK_ENTER:
                //if ((e.getModifiersEx() & (altMask | ctrlMask)) == ctrlMask) {
                if (e.isControlDown() && e.isAltDown()) {
                    String current = swingTerminal.getInput().getText();
                    if (current.equals(WorkspaceCommands.OFF_COMMAND + " y")) {
                        swingTerminal.shutdown();
                    }
                    // previousResults.add(0, getResultText());
                    swingTerminal.previousLines.add(0, current);
                    swingTerminal.previousLineIndex = 0; // reset this
                    swingTerminal.getOutput().setText(null);
                    doSend(current);
                    return;
                }

            default:

                // masks off that alt key is down, ctrl key is up.
                boolean gotOne = false;

                if ((e.getModifiersEx() & (swingTerminal.altMask | swingTerminal.ctrlMask)) == swingTerminal.altMask) {
                    // only alt mask is down
                    if (swingTerminal.getCharMap().containsKey(keyValue)) {
                        keyValue = swingTerminal.getCharMap().get(keyValue); // exactly one
                        gotOne = true;
                    }
                }
                if ((e.getModifiersEx() & (swingTerminal.ctrlMask)) == swingTerminal.ctrlMask) {
                    //ignore ctrl + keys
                    return;
                }


                if (gotOne) {
                    // Only handle special characters if you have one, otherwise
                    // let Swing do the work
                    String x = swingTerminal.getInput().getText();
                    x = x.substring(0, position) + keyValue + (x.length() == position ? "" : x.substring(position));
                    swingTerminal.getInput().setText(null);
                    swingTerminal.getInput().setText(x);
                    swingTerminal.getInput().repaint();
                    try {
                        if (position < x.length()) {
                            swingTerminal.getInput().setCaretPosition(position + 1);
                        }
                    } catch (Throwable t) {
                        // sometime caret position can be wrong if user has moved mouse. Bail
                    }

                } else {
                    super.keyTyped(e);
                }
        } // end switch
    }
}
