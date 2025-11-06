package org.qdl_lang.gui.editor;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.folding.CurlyFoldParser;

public class QDLFoldParser2 extends CurlyFoldParser {
    public QDLFoldParser2() {
        super(true, false);
    }

    @Override
    public boolean isLeftCurly(Token t) {
       // return super.isLeftCurly(t);

        //String X = t.getLexeme().substring(t.getOffset());
        String X = t.getLexeme();
        System.out.println(getClass().getSimpleName() + ".isLeftCurly: " + X);
        return X.trim().startsWith("[");
    }

    @Override
    public boolean isRightCurly(Token t) {
//        return super.isRightCurly(t);
        String X = t.toString().substring(t.getOffset());
        System.out.println(getClass().getSimpleName() + ".isRightCurly: " + X);
        return X.trim().startsWith("]");

    }
}
