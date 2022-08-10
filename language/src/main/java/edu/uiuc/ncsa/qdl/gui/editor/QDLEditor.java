package edu.uiuc.ncsa.qdl.gui.editor;

import edu.uiuc.ncsa.qdl.gui.QDLSwingUtil;
import edu.uiuc.ncsa.qdl.workspace.QDLTerminal;
import edu.uiuc.ncsa.qdl.workspace.WorkspaceCommands;
import edu.uiuc.ncsa.security.core.util.FileUtil;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Map;
import java.util.UUID;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

/**
 * A standalone editor for QDL. This can be invoked by the GUI.
 * <p>Created by Jeff Gaynor<br>
 * on 8/10/22 at  1:36 PM
 */
public class QDLEditor {
    public QDLEditor(WorkspaceCommands workspaceCommands, String alias) throws HeadlessException {
        this.workspaceCommands = workspaceCommands;
        this.alias = alias;
        type = EditDoneEvent.TYPE_BUFFER;
        init();
    }

    WorkspaceCommands workspaceCommands;
    String alias; // name of buffer

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public int getArgCount() {
        return argCount;
    }

    public void setArgCount(int argCount) {
        this.argCount = argCount;
    }

    String localName; // for functions or variables
    int argCount; // for functions
    /**
     * The type of object, as per {@link EditDoneEvent}.
     * @return
     */
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    int type;
    public UUID getID() {
        return uuid;
    }

    UUID uuid = UUID.randomUUID();
    private RSyntaxTextArea input;
    private JPanel mainPanel;

    int altMask = ALT_DOWN_MASK;
    int ctrlMask = CTRL_DOWN_MASK;
    JFrame jFrame;

    public QDLEditor() {
        init();
    }

    File file;

    public QDLEditor(File file) {
        this.file = file;
        alias = file.getAbsolutePath();
        init();
    }

    protected void init() {
        jFrame = new JFrame();
        input.addKeyListener(new MyKeyAdapter());
        input.addKeyListener(new ControlOperations());
    }

    public class ControlOperations extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_F1:
                    JOptionPane.showMessageDialog(jFrame, "ctrl+s saves the buffer, ctrl+q exits.");
                    break;
                case KeyEvent.VK_S:
                    if ((e.getModifiersEx() & (altMask | ctrlMask)) == ctrlMask) {
                        if(file == null){
                            EditDoneEvent editDoneEvent = new EditDoneEvent(uuid, input.getText());
                            editDoneEvent.setType(getType());
                            workspaceCommands.editDone(editDoneEvent);
                        }else{
                            try {
                                FileUtil.writeStringToFile(file.getAbsolutePath(), input.getText());
                            } catch (Throwable ex) {
                                JOptionPane.showMessageDialog(jFrame, "could not save file:" + ex.getMessage());
                            }
                        }
                        // ctrl s == save
                    }

                    break;
                case KeyEvent.VK_Q:
                    if ((e.getModifiersEx() & (altMask | ctrlMask)) == ctrlMask) {
                        // ctrl q == quit
                        jFrame.dispose();
                    }
                    break;
            }
            super.keyPressed(e);
        }

    }

    public class MyKeyAdapter extends KeyAdapter {

        @Override
        public void keyTyped(KeyEvent e) {
            String keyValue = String.valueOf(e.getKeyChar());
            int position = input.getCaretPosition();

            // masks off that alt key is down, ctrl key is up.
            boolean gotOne = false;

            if ((e.getModifiersEx() & (altMask | ctrlMask)) == altMask) {
                // only alt mask is down
                if (getCharMap().containsKey(keyValue)) {
                    keyValue = getCharMap().get(keyValue); // exactly one
                    gotOne = true;
                }
            }

            if (gotOne) {
                // Only handle special characters if you have one, otherwise
                // let Swing do the work
                String x = input.getText();
                x = x.substring(0, position) + keyValue + (x.length() == position ? "" : x.substring(position));
                input.setText(null);
                input.setText(x);
                input.repaint();
                try {
                    if (position < x.length()) {
                        input.setCaretPosition(position + 1);
                    }
                } catch (Throwable t) {
                    // sometime caret position can be wrong if user has moved mouse. Bail
                }

            } else {
                super.keyTyped(e);
            }

        }

    }

    public static void main(String[] args) {
        QDLEditor qdlEditor = new QDLEditor();
        qdlEditor.setup("test");

    }

    public void setup() throws Throwable {
        String content = null;
        if (file != null) {
            if (file.exists()) {
                content = FileUtil.readFileAsString(file.getAbsolutePath());
            }
        }
        setup(content);
    }

    public void setup(String content) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        jFrame.setTitle("QDL Editor:" + alias);
        jFrame.setContentPane(mainPanel);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping("text/qdl", "edu.uiuc.ncsa.qdl.gui.flex.QDLSyntax");
        CompletionProvider provider = QDLSwingUtil.createCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(input);
        input.setSyntaxEditingStyle("text/qdl");
        if (content != null) {
            input.setText(content);
        }
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) dimension.getWidth() / 2;
        int h = (int) dimension.getHeight() / 2;
        jFrame.setSize(w, h);
        int x = (int) ((dimension.getWidth() - jFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - jFrame.getHeight()) / 2);
        jFrame.setLocation(x, y);
        String laf = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Throwable e) {
            // really should never happen
            e.printStackTrace();
        }
        jFrame.setVisible(true);
    }

    public Map<String, String> getCharMap() {
        return QDLTerminal.getCharLookupMap();
    }
}
