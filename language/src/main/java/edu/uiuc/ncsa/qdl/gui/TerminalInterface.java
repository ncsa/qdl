package edu.uiuc.ncsa.qdl.gui;

import java.util.Map;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/9/22 at  12:41 PM
 */
public interface TerminalInterface {
    void setPrompt(String text);

    StringBuffer getCurrentLine();

    void clearCurrentLine();

    void setResultText(String x);

    String getResultText();

     Map<String, String> getCharMap() ;

     void shutdown();
}
