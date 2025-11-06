package org.qdl_lang.gui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.folding.Fold;
import org.fife.ui.rsyntaxtextarea.folding.FoldParser;
import org.fife.ui.rsyntaxtextarea.folding.FoldType;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fold parser for QDL. Formerly possible in 2.5.8, but seems to have been removed in
 * current version, 3.3.1. Seems that the preferred way is via Flex, which
 * is close to impossible to do from an ANTLR grammar.
 */
// Fix for https://github.com/ncsa/qdl/issues/147
public class QDLFoldParser implements FoldParser {
    public List<Fold> getFolds(RSyntaxTextArea textArea) {
        List<Fold> folds = new ArrayList<>();
        Document doc = textArea.getDocument();
        Pattern startBlockPattern = Pattern.compile("^.*\\[.*$");
        Pattern endBlockPattern = Pattern.compile("^.*\\].*$");
        Pattern startMLCommentPattern = Pattern.compile("^.*/\\*.*$");
        Pattern endMLCommentPattern = Pattern.compile("^.*\\*/.*$");

        int lineCount = textArea.getLineCount();
        System.out.println(getClass().getSimpleName() + " lineCount: " + lineCount);

        Fold currentFold = null;
        boolean inComment = false;

        for (int i = 0; i < lineCount; i++) {
            try {
                int startOffset = textArea.getLineStartOffset(i);
                int endOffset = textArea.getLineEndOffset(i);
                String line = doc.getText(startOffset, endOffset - startOffset);
                System.out.println(getClass().getSimpleName() + " line: " + line);
                Matcher endMLCommentMatcher = endMLCommentPattern.matcher(line);
                boolean isEndComment = endMLCommentMatcher.find();
                if(inComment){
                    if(isEndComment){
                        if (currentFold != null) {
                            currentFold.setEndOffset(endOffset);
                            currentFold = currentFold.getParent(); // Move up the hierarchy
                        }
                        inComment = false;
                        continue;
                    }
                }
                Matcher startMLCommentMatcher = startMLCommentPattern.matcher(line);
                inComment = startMLCommentMatcher.find();
                if(inComment && isEndComment){ // skip single line comments
                    inComment = false;
                    continue;
                }


                Matcher startBlockMatcher = startBlockPattern.matcher(line);
                Matcher endBlockMatcher = endBlockPattern.matcher(line);
                boolean isStartBlock = startBlockMatcher.find();
                boolean isEndBlock = endBlockMatcher.find();
                if(isStartBlock && isEndBlock){ // skip single line [], like lists
                    continue;
                }
                // in QDL, it is possible to have [] on a line (e.g., lists), so
                // only flag it as a fold if there is no closing bracket
                if (isStartBlock || inComment) {
                    // Found a start marker
                    int foldType = inComment ? FoldType.COMMENT : FoldType.CODE ;
                    Fold fold;
                    if (currentFold != null) {
                        fold = currentFold.createChild(foldType, startOffset);
                    } else {
                        fold = new Fold(foldType, textArea, startOffset);
                        folds.add(fold);
                    }
                    currentFold = fold;
                } else {

                    if (isEndBlock || isEndComment ) {
                        // Found an end marker
                        if (currentFold != null) {
                            currentFold.setEndOffset(endOffset-1);
                            currentFold = currentFold.getParent(); // Move up the hierarchy
                        }
                    }
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        return folds;
    }
 }
