package org.qdl_lang.gui;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

import static org.qdl_lang.gui.SwingTerminal.syntaxEditStyle;

/**
 * Basic testing for GUI.
 * <p>Created by Jeff Gaynor<br>
 * on 8/9/22 at  7:16 AM
 */
public class QDLEditor2 extends JFrame {

    public QDLEditor2() {

        JPanel cp = new JPanel(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
        atmf.putMapping(syntaxEditStyle, "org.qdl_lang.gui.flex.QDLSyntax");
        textArea.setSyntaxEditingStyle(syntaxEditStyle);
        FoldParserManager.get().addFoldParserMapping(syntaxEditStyle, new QDLFoldParser());
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setLineNumbersEnabled(true);
        sp.setFoldIndicatorEnabled(true);
        cp.add(sp);

        setContentPane(cp);
        setTitle("Text Editor Demo");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

    }

    public static void main(String[] args) {
        // Start all Swing applications on the EDT.
        SwingUtilities.invokeLater(() -> new QDLEditor2().setVisible(true));
    }

}