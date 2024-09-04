package org.qdl_lang.gui;

import org.qdl_lang.util.InputFormUtil;
import edu.uiuc.ncsa.security.core.util.StringUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Do line operations on a text area. This allows you to insert a line,
 * cut, copy or paste a line and move lines. This is generally intended to be
 * used as a utility for the {@link SwingTerminal}.
 * <p>Created by Jeff Gaynor<br>
 * on 3/17/23 at  7:39 AM
 */
public class LineUtil {
    public static final String COPY_LINE = "copy"; // copy current line to clipboard
    public static final String DUPLICATE_LINE = "duplicate"; // take current line, insert copy
    public static final String PASTE_LINES = "paste"; // paste clipboard to current line position
    public static final String CUT_LINE = "cut"; // copy line to clipboard and remove it
    public static final String UP_LINE = "up"; // move the current line up one
    public static final String DOWN_LINE = "down"; // move current line down one.
    public static final String TOGGLE_COMMENT_LINE = "comment"; // Toggle line comment.
    public static final String JOIN_LINE = "join"; // join with next.

    public static String doOperation(String text, int lineNumber, String operation) throws IOException, UnsupportedFlavorException {
        ArrayList<String> lines = toLines(text);
        switch (operation) {
            case COPY_LINE:
                lines = doClipboardCopyorCut(lines, lineNumber, false);
                break;
            case CUT_LINE:
                lines = doClipboardCopyorCut(lines, lineNumber, true);
                break;
            case DUPLICATE_LINE:
                lines = duplicateLine(lines, lineNumber);
                break;
            case PASTE_LINES:
                lines = pasteLines(lines, lineNumber);
                break;
            case UP_LINE:
                lines = lineUpOrDown(lines, lineNumber, true);
                break;
            case DOWN_LINE:
                lines = lineUpOrDown(lines, lineNumber, false);
                break;
            case TOGGLE_COMMENT_LINE:
                lines = toggleComment(lines, lineNumber);
                break;
            case JOIN_LINE:
                lines = joinLine(lines, lineNumber);
                break;
        }
        return StringUtils.listToString(lines);
    }

    private static ArrayList<String> joinLine(ArrayList<String> lines, int lineNumber) {
        if (!checkPosition(lines, lineNumber)) {
            return lines;
        }
      if(lineNumber-1 == lines.size()){
          return lines; // no line to append.
      }
      String newLine = lines.get(lineNumber) + lines.get(lineNumber+1);
      lines.remove(lineNumber+1);
      lines.set(lineNumber, newLine);
      return lines;
    }

    private static ArrayList<String> lineUpOrDown(ArrayList<String> lines, int lineNumber, boolean moveUp) {
        if (!checkPosition(lines, lineNumber)) {
            return lines;
        }
        if (moveUp) {
            if (lineNumber == 0) {
                return lines;
            }
        } else {
            if (lineNumber == lines.size() - 1) {
                return lines;
            }
        }
        String old = lines.get(lineNumber);
        if (moveUp) {
            String otherLine = lines.get(lineNumber - 1);
            lines.set(lineNumber - 1, old);
            lines.set(lineNumber, otherLine);
        } else {
            String otherLine = lines.get(lineNumber + 1);
            lines.set(lineNumber + 1, old);
            lines.set(lineNumber, otherLine);

        }
        return lines;
    }

    /**
     * Has to be an array list to get remove() and other methods.
     *
     * @param text
     * @return
     */

    public static ArrayList<String> toLines(String text) {
        List<String> list = Arrays.asList(text.split("\n"));
        if (list instanceof ArrayList) {
            return (ArrayList) list;
        }
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(list);
        return arrayList;
    }

    /**
     * Checks that the position of the caret is in the list of lines. True if ok, false otherwise.
     *
     * @param lines
     * @param position
     * @return
     */
    protected static boolean checkPosition(List<String> lines, int position) {
        if (0 <= position && position < lines.size()) {
            return true;
        }
        return false;
    }

    public static Clipboard getClipboard() {
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return clipboard;
    }

    protected static boolean hasClipboard() {
        return getClipboard() != null;
    }

    static Clipboard clipboard = null;

    protected static ArrayList<String> doClipboardCopyorCut(ArrayList<String> lines, int position, boolean isCut) throws IOException, UnsupportedFlavorException {
        if (!checkPosition(lines, position)) {
            return lines;
        }
        StringSelection data = new StringSelection(lines.get(position));
        getClipboard().setContents(data, null);
        if (isCut) {
            lines.remove(position);
        }
        return lines;
    }

    protected static ArrayList<String> duplicateLine(ArrayList<String> lines, int position) {
        if (!checkPosition(lines, position)) {
            return lines;
        }
        String target = lines.get(position);
        lines.add(position, target);
        return lines;
    }

    protected static ArrayList<String> pasteLines(ArrayList<String> lines, int position) throws IOException, UnsupportedFlavorException {
        String out = (String) getClipboard().getData(DataFlavor.stringFlavor);
        out = out.trim(); // removes random line feeds at the end.
        List<String> newLines = StringUtils.stringToList(out);
        if (newLines.isEmpty()) {
            return lines;
        }
        if (newLines.size() == 1) {
            lines.add(position, newLines.get(0));
            return lines;
        }
        // Need some surgery

        ArrayList<String> result = new ArrayList<>();
        result.addAll(lines.subList(0, position));
        result.addAll(newLines);
        result.addAll(lines.subList(position, lines.size() - 1));
        return result;
    }


    protected static ArrayList<String> toggleComment(ArrayList<String> lines, int lineNumber) {
        if (!checkPosition(lines, lineNumber)) {
            return lines;
        }
        String currentLine = lines.get(lineNumber);
        if (-1 == currentLine.indexOf("//")) {
            currentLine = "//" + currentLine;
            lines.set(lineNumber, currentLine);
            return lines;
        }
        int non = getFirstNonwhitespaceIndex(currentLine);
        // have to rebuffer that
        currentLine = currentLine.trim();
        if (-1 == currentLine.indexOf("//")) {
            if (0 < non - 2) {
                currentLine = StringUtils.getBlanks(non) + "//" + currentLine;
            }
            lines.set(lineNumber, currentLine);
            return lines;
        }
        if (currentLine.startsWith("//")) {
            currentLine = currentLine.substring(2);
            if (0 < non) {
                currentLine = StringUtils.getBlanks(non) + currentLine;
            }
            lines.set(lineNumber, currentLine);
        }
        return lines;
    }

    public static int getFirstNonwhitespaceIndex(String string) {
        char[] characters = string.toCharArray();
        boolean lastWhitespace = false;
        if (characters[0] != ' ' && characters[0] != '\t') return 0; // intercept first
        for (int i = 0; i < string.length(); i++) {
            //if(Character.isSpaceChar(characters[i])){
            if (characters[i] == ' ' || characters[i] == '\t') {
                lastWhitespace = true;
            } else if (lastWhitespace) {
                return i;
            }
        }
        return -1;
    }
     protected static void testInputForm() throws Throwable{
         String test = "{\"tokens\": {\n" +
                   "   \"access\":  {\n" +
                   "    \"audience\": \"https://localhost/fermilab\",\n" +
                   "    \"lifetime\": 300000,\n" +
                   "       \"qdl\":   {\n" +
                   "        \"load\": \"ui-test/process.qdl\",\n" +
                   "        \"xmd\": {\"exec_phase\":    [\n" +
                   "         \"post_token\",\n" +
                   "         \"post_refresh\",\n" +
                   "         \"post_user_info\"\n" +
                   "        ]}\n" +
                   "       }\n" +
                   "    \"templates\": [  {\n" +
                   "     \"aud\": \"https://localhost/fermilab\",\n" +
                   "     \"paths\":    [\n" +
                   "          {\"op\": \"read\", \"path\": \"/home/${sub}\"},\n" +
                   "          {\"op\": \"x.y\",\"path\": \"/abc/def\"},\n" +
                   "          {\"op\": \"write\",\"path\": \"/data/${sub}/cluster\"}\n" +
                   "     ]\n" +
                   "    }],\n" +
                   "    \"type\": \"scitoken\"\n" +
                   "   },\n" +
                   "   \"refresh\":  {\n" +
                   "    \"audience\": \"https://localhost/test\",\n" +
                   "    \"lifetime\": 900000,\n" +
                   "    \"type\": \"refresh\"\n" +
                   "   }\n" +
                   "  }}";
         String out = (String) getClipboard().getData(DataFlavor.stringFlavor);

          System.out.println(toInputForm(out, true));

     }
     protected static void testLineModes() throws Throwable{
         String text = "mairzy doats\nand dozey\n     f(  x)->  x  ^2;\ndoats\n     //  g(c)->c;\nand liddle\nlambsie\nstoats";
         System.out.println("Original:\n" + text);
         text = doOperation(text, 2, TOGGLE_COMMENT_LINE);
         System.out.println("\ncomment on:\n" + text);
         text = doOperation(text, 2, TOGGLE_COMMENT_LINE);
         System.out.println("\ncomment off:\n" + text);
         text = doOperation(text, 4, TOGGLE_COMMENT_LINE);
         System.out.println("\ncomment off:\n" + text);
         text = doOperation(text, 4, TOGGLE_COMMENT_LINE);
         System.out.println("\ncomment on:\n" + text);

         text = doOperation(text, 2, UP_LINE);
         System.out.println("\nline up:\n" + text);
         text = doOperation(text, 2, DOWN_LINE);
         System.out.println("\nline down:\n" + text);
         text = doOperation(text, 2, CUT_LINE);
         System.out.println("\ncut line:\n" + text);
         text = doOperation(text, 2, DUPLICATE_LINE);
         System.out.println("\nduplicate line:\n" + text);
         text = doOperation(text, 2, PASTE_LINES);
         System.out.println("\npaste line:\n" + text);
         text = doOperation(text, 3, JOIN_LINE);
         System.out.println("\njoin line 3+4:\n" + text);
     }
    public static void main(String[] args) throws Throwable {
            //testLineModes();
            testInputForm();


    }

    /**
     * Converts a string (from the clipboard) to input form. This checks if it is a string and if
     * not just returns it. If it is, then shoirt form is a single line of input form, long form
     * break it up into lines and formats each one as a concatenation. The latter is really useful
     * when pasting things like long formatted JSON blobs.
     * @param in
     * @param isLongForm
     * @return
     */
      public static String toInputForm(String in, boolean isLongForm){
          if(!checkString(in.trim())){
              return in;  // so it's a long, decimal, null or boolean
          }
          if(!isLongForm){
              return InputFormUtil.inputForm(in);
          }
          List<String> lines = StringUtils.stringToList(in);
          StringBuilder stringBuilder = new StringBuilder();
          for(int i = 0; i < lines.size(); i++){
              String currentLine = lines.get(i);
             currentLine =  currentLine.replace("\t", "  ");
              //currentLine.replace("\\n", "\n");
              if(i +1 < lines.size()){
                  stringBuilder.append(InputFormUtil.inputForm(currentLine+"\n") +  "+\n" );
              }else{
                  // last line
                  stringBuilder.append(InputFormUtil.inputForm(currentLine));
              }
          }
          return stringBuilder.toString();
      }

    protected static boolean checkString(String out) {
           try {
               Long.parseLong(out);
               return false;
           } catch (Throwable t) {

           }
           try {
               new BigDecimal(out);
               return false;
           } catch (Throwable t) {

           }
           if (out.equals("null")) {
               return false;
           }
           if (out.equals("true")) {
               return false;
           }
           if (out.equals("false")) {
               return false;
           }
           return true;
       }
}
