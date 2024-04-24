package edu.uiuc.ncsa.qdl.gui.editor;

import edu.uiuc.ncsa.qdl.gui.LineUtil;
import edu.uiuc.ncsa.qdl.parsing.QDLParserDriver;
import edu.uiuc.ncsa.qdl.parsing.QDLRunner;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.util.QDLFileUtil;
import edu.uiuc.ncsa.qdl.workspace.WorkspaceCommands;
import edu.uiuc.ncsa.security.core.configuration.XProperties;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;

import static edu.uiuc.ncsa.qdl.gui.LineUtil.getClipboard;
import static edu.uiuc.ncsa.security.core.util.StringUtils.isTrivial;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/20/23 at  3:56 PM
 */
public class EditorKeyPressedAdapter extends KeyAdapter {
    protected RSyntaxTextArea input;
    protected JTextArea output;
    WorkspaceCommands workspaceCommands;
    JFrame frame;

    public EditorKeyPressedAdapter(WorkspaceCommands workspaceCommands,
                                   JFrame frame, RSyntaxTextArea input, JTextArea output) {
        this.workspaceCommands = workspaceCommands;
        this.frame = frame;
        this.input = input;
        this.output = output;
    }

    /**
     * Returns the line number (starting at 0) of the current cursor position.
     *
     * @return
     */
    public int getLineNumber(int position) {
        Element root = input.getDocument().getDefaultRootElement();
        return root.getElementIndex(position); // line 0 is first
    }

    protected String toggleSelectionComment(String selection) {
        if (StringUtils.isTrivial(selection)) {
            return selection;
        }
        if (selection.trim().startsWith("/*")) {
            // turn off selection
            int position = selection.indexOf("/*");
            String s = selection.substring(0, position);
            if (position + 2 < selection.length()) {
                s = s + selection.substring(position + 2);
            }
            position = s.lastIndexOf("*/");
            if (position == -1) {
                return null; // no termination for comment
            }
            s = s.substring(0, position) + s.substring(position + 2);
            return s;
        } else {
            if (selection.endsWith("\n")) {
                // If it ends with a line feed, preserve that, but stick the end comment in the line.
                // Just looks better.
                selection = "/*" + selection.substring(0, selection.length() - 1) + "*/\n";

            } else {

                selection = "/*" + selection + "*/";
            }
            return selection;
        }
    }

    protected String getHelp(String text) {
        return createHelpMessage(workspaceCommands.getFunctionHelp(text),
                workspaceCommands.getHelpTopic(text),
                workspaceCommands.getHelpTopicExample(text));
    }

    String guiHelp = null;

    protected String getGUIHelp() {
        if (guiHelp == null) {
            guiHelp = "F1 help for selected text or this message\n" +
                    "ctrl+space autocomplete\n" +
                    "ctrl+s saves\n" +
                    "ctrl+q exits (no save!)\n" +
                    "ctrl+shift+page up/down navigate history."; // default if nothing loads.

            InputStream helpStream = getClass().getResourceAsStream("/editor_help.txt");
            if (helpStream != null) {
                try {
                    guiHelp = QDLFileUtil.isToString(helpStream);
                    helpStream.close();
                } catch (IOException e) {
                    if (DebugUtil.isEnabled()) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return guiHelp;
    }

    protected String createHelpMessage(String functionHelp, String text, String example) {
        if (isTrivial(text) && isTrivial(functionHelp)) {
            return "no help available for " + (isTrivial(text) ? "this topic" : "' + text + '");
        }

        String message = "";
        if (!isTrivial(functionHelp)) {
            message = "\n--------\nUser defined functions\n--------\n" + functionHelp + "\n";
        }

        if (!isTrivial(text)) {
            message = message + "\n--------\nOnline help\n--------\n" + text;
        }
        if (!isTrivial(example)) {
            message = message + "\n--------\nExamples\n--------\n" + example;
        }
        return message;
    }

    /**
     * <h1>NOTE</h1>
     * This is not just a text area, it is an {@link RSyntaxTextArea} which masks off
     * various keystroked for itself. A symptom of this is if you attempt to use one of
     * the (not well documented) reserved keystrokes, you will get mysterious enter key
     * events as it tries to reformat (or whatever) the input area. So far the list of reserved
     * keys are
     * <ul>
     *     <li>a -- select all</li>
     *     <li>c -- copy select to clipboard</li>
     *     <li>d -- delete current line</li>
     *     <li>j --(justify?)</li>
     *     <li>k -- beeps?</li>
     *     <li>v -- paste from clipbaord</li>
     *     <li>x -- cut selected to clipboard</li>
     * </ul>
     *
     * @param e
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // Things like cursor keys register as "key pressed" not "key typed"
        // hence can be handled separately.
        super.keyPressed(e);


        switch (e.getKeyCode()) {

            case KeyEvent.VK_I:
                // Insert in input form
                if (e.isControlDown()) {
                    try {
                        String out = (String) getClipboard().getData(DataFlavor.stringFlavor);
                        out = LineUtil.toInputForm(out, e.isShiftDown());
                        String content = input.getText();
                        int position = input.getCaretPosition();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(content.substring(0, position));
                        stringBuilder.append(out);
                        if (position + 1 < content.length()) {
                            stringBuilder.append(content.substring(position + 1));
                        }
                        input.setText(null);
                        input.setText(stringBuilder.toString());
                        input.repaint();
                        input.setCaretPosition(position);

                    } catch (UnsupportedFlavorException | IOException ex) {
                        if (DebugUtil.isEnabled()) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                }
                break;
            case KeyEvent.VK_H:
                // check syntax. Display result
                if (e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
                    String message = null;
                    String content = input.getText();
                    String selected = input.getSelectedText();
                    if (!StringUtils.isTrivial(selected)) {
                        content = selected;
                    }
                    StringReader r = new StringReader(content);
                    QDLParserDriver driver = new QDLParserDriver(new XProperties(), new State());
                    try {
                        new QDLRunner(driver.parse(r));
                        message = "syntax ok";
                    } catch (ParseCancellationException pc) {
                        message = pc.getMessage();
                    } catch (Throwable t) {
                        message = "non-syntax error:" + t.getMessage();
                    }
                    JOptionPane.showMessageDialog(frame, message, "syntax check results", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            case KeyEvent.VK_C:
                if (e.isControlDown() && !e.isShiftDown()) {
                    String currentSelection = input.getSelectedText();
                    if (!StringUtils.isTrivial(currentSelection)) {
                        return;  // Let component do a standard copy.
                    }
                    // otherwise, do a line copy
                    String current = input.getText();
                    try {
                        int currentCaret = input.getCaretPosition();
                        int lineNumber = getLineNumber(currentCaret);
                        LineUtil.doOperation(current, lineNumber, LineUtil.COPY_LINE); // don't care about output
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (UnsupportedFlavorException ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            case KeyEvent.VK_M:
                if (e.isControlDown() && !e.isAltDown()) {
                    String newText = null;
                    String current = input.getText();
                    int currentCaret = input.getCaretPosition();
                    String currentSelection = input.getSelectedText();

                    if (StringUtils.isTrivial(currentSelection)) {
                        int lineNumber = getLineNumber(currentCaret);
                        try {
                            newText = LineUtil.doOperation(current, lineNumber, LineUtil.TOGGLE_COMMENT_LINE);
                        } catch (IOException | UnsupportedFlavorException ex) {
                            if (DebugUtil.isEnabled()) {
                                ex.printStackTrace();
                            }
                            newText = null;
                        }
                    } else {
                        String s = toggleSelectionComment(currentSelection);
                        if (s == null) {
                            newText = null; // do nothing.
                        } else {
                            int start = input.getSelectionStart();
                            int end = input.getSelectionEnd();
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(current.substring(0, start));
                            stringBuilder.append(s);
                            if (end < current.length()) {
                                stringBuilder.append(current.substring(end));
                            }
                            newText = stringBuilder.toString();
                        }
                    }
                    if (newText == null) {
                        break; // do nothing
                    }
                    input.setText(null);
                    input.setText(newText);
                    input.repaint();
                    input.setCaretPosition(currentCaret);
                    break;

                }
                break;
            case KeyEvent.VK_F1:
                // If no selected text, put up a generic help message. Otherwise,
                // search online help.
                if (e.isControlDown() && !e.isAltDown()) {
                    String x = getHelp("keyboard");
                    if (x != null) {
                        String helpMessage = x;
                    }
                    showHelp("QDL keyboard layout", x);
                    break;
                }
                String text = input.getSelectedText();
                if (text == null && output != null) {
                    // See if they selected something in output
                    text = output.getSelectedText();
                }
                if (text != null) {
                    // just in case they swiped a little too much.
                    text = text.trim();
                }

                if (isTrivial(text)) {
                    showHelp("Help for GUI", getGUIHelp());
                } else {
                    String title = "Help for " + text;
                    String helpMessage = "no help available for " + (isTrivial(text) ? "this topic" : "' + text + '");
                    String x = getHelp(text);
                    if (x != null) {
                        helpMessage = x;
                    }
                    showHelp(title, getHelp(text));
                }
                break;
            case KeyEvent.VK_Q:
                if (e.isControlDown()) {
                    doQuit(e.isShiftDown());
                }
                break;
            case KeyEvent.VK_R:
                // replicate selection or line
                if (e.isControlDown() && !e.isAltDown()) {
                    String newText = null;
                    String current = input.getText();
                    int currentCaret = input.getCaretPosition();
                    String currentSelection = input.getSelectedText();

                    if (StringUtils.isTrivial(currentSelection)) {
                        try {
                            newText = LineUtil.doOperation(current, getLineNumber(currentCaret), LineUtil.DUPLICATE_LINE);
                        } catch (IOException | UnsupportedFlavorException ex) {
                            if (DebugUtil.isEnabled()) {
                                ex.printStackTrace();
                            }
                        }
                    } else {
                        int start = input.getSelectionStart();
                        int end = input.getSelectionEnd();
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(current.substring(0, start));
                        stringBuilder.append(currentSelection); // duplicates it
                        stringBuilder.append(current.substring(start + 1)); // gets original again
                        newText = stringBuilder.toString();
                    }
                    if (newText == null) {
                        break; // do nothing
                    }
                    input.setText(null);
                    input.setText(newText);
                    input.repaint();
                    input.setCaretPosition(currentCaret);
                    break;
                }
                break;
            case KeyEvent.VK_S:
                if (e.isControlDown() && !e.isShiftDown()) {
                    doSave();
                }
                break;
        }
    }


    protected void doQuit(boolean forceQuit) {
        if (forceQuit) {
            System.exit(0);
            return;
        }
        int out = JOptionPane.showConfirmDialog(frame, "Are you sure you want to quit? No save is done.", "quit workspace", JOptionPane.WARNING_MESSAGE);
        if (out == JOptionPane.YES_OPTION || out == JOptionPane.OK_OPTION) {
            frame.dispose();
            System.exit(0);
        }
    }

    protected void doSave() {
        String x = ")save";
        String old = null;
        if (output != null) {
            old = output.getText();
        }
        Object rc = workspaceCommands.execute(x);
        String resp = rc.equals(WorkspaceCommands.RC_CONTINUE) ? "save ok" : "not saved";
        if (output != null) {
            resp = output.getText();
            output.setText(old);
        }
        JOptionPane.showMessageDialog(frame, resp);
    }

    /**
     * Show's help for the selected area. Note that this allows for editing and copy paste
     * to the main window (standard Swing dialog does not)
     *
     * @param title
     * @param message
     */
    protected void showHelp(String title, String message) {
        JTextArea textArea = new JTextArea(25, 100);
        textArea.setFont(new Font("DialogInput", Font.PLAIN, 12));
        textArea.setText(message);
        textArea.setCaretPosition(0); // put it at the top
        textArea.setEditable(false);

        // wrap a scrollpane around it
        JScrollPane scrollPane = new JScrollPane(textArea);

        JOptionPane helpPane = new JOptionPane();
        JDialog dialog = helpPane.createDialog(null, title);
        dialog.setModal(false);
        dialog.setResizable(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(scrollPane);
        AbstractAction escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dialog.dispose();
            }
        };
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE_KEY");
        dialog.getRootPane().getActionMap().put("ESCAPE_KEY", escapeAction);


        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) dimension.getWidth() / 2;
        int h = (int) dimension.getHeight() / 2;
        dialog.setSize(w, h);
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        dialog.setLocation(x, y);
        dialog.setVisible(true);

    }
}
