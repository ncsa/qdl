package org.qdl_lang.gui;

import org.qdl_lang.state.QDLConstants;
import edu.uiuc.ncsa.security.core.util.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static edu.uiuc.ncsa.security.core.util.StringUtils.RJustify;
import static edu.uiuc.ncsa.security.core.util.StringUtils.pad2;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 6/27/24 at  7:08 AM
 */
public class FontUtil {
    protected String fontReport(Font font) {
        int s = font.getStyle();
        String fontStyle = null;
        switch (s) {
            case Font.PLAIN:
                fontStyle = "plain";
                break;
            case Font.BOLD:
                fontStyle = "bold";
                break;
            case Font.ITALIC:
                fontStyle = "italic";
                break;
            default:
                if (s == (Font.BOLD + Font.ITALIC)) {
                    fontStyle = "bold+italic";
                }
        }
        return font.getName() + " " + fontStyle + " at " + font.getSize() + " pts.";
    }

    public static final String STYLE_PLAIN = "plain";
    public static final String STYLE_BOLD = "bold";
    public static final String STYLE_ITALIC = "italic";
    public static final String STYLE_BOLD_ITALIC = "bold+italic";
    public static final String STYLE_ITALIC_BOLD = "italic+bold";

    /**
     * Convert the Swing constant for the font style into the QDL name.
     * @param font
     * @return
     */
    public static String getStyle(Font font) {
        int s = font.getStyle();
        String fontStyle = null;
        switch (font.getStyle()) {
            case Font.PLAIN:
                return STYLE_PLAIN;
            case Font.BOLD:
                return STYLE_BOLD;
            case Font.ITALIC:
                return STYLE_ITALIC;
            default:
                if (s == (Font.BOLD + Font.ITALIC)) {
                    return STYLE_BOLD_ITALIC;
                }
        }

        throw new IllegalArgumentException("unknown font style");
    }

    /**
     * Convert the QDL names for font styles into the constant that Swing understands.<br/><br/>
     *  <b>Caveat</b> any unknown font style is rendered as the default, which is {@link Font#BOLD}
     * @param styleName
     * @return
     */
    public static int getStyle(String styleName) {
        switch (styleName) {
            default:
            case STYLE_BOLD:
                return Font.BOLD;
            case STYLE_PLAIN:
                return Font.PLAIN;
            case STYLE_ITALIC:
                return Font.ITALIC;
            case STYLE_BOLD_ITALIC:
            case STYLE_ITALIC_BOLD:
                return Font.BOLD + Font.ITALIC;
        }
    }
    /**
       * A utility that runs through all of the fons on your system and finds those that are
       * able to output all of the QDL character set.
       * <h3>The issue</h3>
       * <p>
       * The issue is that Swing is restricted to TrueType fonts and very few of those have all
       * the unicode characters in them. Really only the built-in Java is reliable. This checks by
       * using the {@link QDLConstants#UNICODE_CHARS} and checks how much of it is printable.
       * Any result from that greater than -1 flags which character Swing fails at.
       * </p>
       * <p>
       * This is compounded by the fact that other font formats might include more.
       * For instance "Ubuntu Mono" as an Open Type font has everything, but several characters
       * as missing in the True Type version. Applications may be clever too and auto switch to
       * replace a missing character from another font or member of a family of fonts. Can't do
       * that in Swing either on a per character basis.
       * </p>
       * <p>
       * Common failures are the APL symbols for the apply and floor operator, or the
       * alternate Greek letters for rho, theta, etc. Math symbol sets had the latter,
       * only APL character sets reliably have the first, but usually don't have any Math
       * symbols.
       * </p>
       *
       * @return
       * @throws Throwable
       */
      public static java.util.List<String> findQDLFonts() throws Throwable {
          java.util.List<String> ok = new ArrayList<>();
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          int[] types = new int[]{Font.BOLD};
          int[] sizes = new int[]{14};
          for (String xxx : ge.getAvailableFontFamilyNames()) {
              for (int t : types) {
                  for (int s : sizes) {
                      if (checkSingleFont(xxx, t, s)) {
                          ok.add(xxx);
                      }
                  }
              }
          }
          return ok;
      }

      public static java.util.List<String> findQDLFonts(double weight) throws Throwable {
          List<String> ok = new ArrayList<>();
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          int allCharCount = QDLConstants.ALL_CHARS.length();
          char[] allChars = QDLConstants.ALL_CHARS.toCharArray();
          int padWidth = 5;
          for (String xxx : ge.getAvailableFontFamilyNames()) {
              Font font = new Font(xxx, Font.BOLD, 14);
              String unsupported = findUnsupportedCharacters(font, allChars);
              if(unsupported.isEmpty()){
                  ok.add(RJustify(xxx, 32) + " : " + pad2(0,padWidth) + " : (all ok)");
              }else{
                  if(weight <= (100.00D*(allCharCount - unsupported.length()))/allCharCount ){
                      boolean add = ok.add(RJustify(xxx, 32) + " : " + pad2(unsupported.length(), padWidth) + " : " + unsupported);
                  }
              }
          }
          if(!ok.isEmpty()){
              ok.add(0, StringUtils.RJustify("Font name", 32) + " : " + pad2("bad #", padWidth) + " : " + "unsupported chars");
          }
          return ok;
      }

      public static boolean checkSingleFont(String name, int type, int size) {
          Font um12 = new Font(name, type, size);
          int offset = um12.canDisplayUpTo(QDLConstants.ALL_CHARS);
          return offset == -1;
      }

      /**
       * A font utility to check the support for a font. This returns the string of
       * characters that this font cannot display.
       *
       * @param font
       * @param x
       * @return
       */
      public static String findUnsupportedCharacters(Font font, String x) {
           return findUnsupportedCharacters(font, x.toCharArray());
      }
      public static String findUnsupportedCharacters(Font font, char[] y) {
          StringBuffer stringBuffer = new StringBuffer();
          for (char c : y) {
              if (!font.canDisplay(c)) {
                  stringBuffer.append(c);
              }
          }
          return stringBuffer.toString();
      }

}
