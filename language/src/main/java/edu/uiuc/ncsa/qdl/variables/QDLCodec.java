package edu.uiuc.ncsa.qdl.variables;

import edu.uiuc.ncsa.qdl.variables.codecs.*;

/**
 * This will convert a string and encode or decode all characters that are not [a-z][A-Z]_. The encoding
 * is URL standard escaping in which the "%" is replaced by a "$" so that it forms a valid
 * variable.  E.g.,
 * <pre>
 *       ab&amp;(*c --&gt;  ab$26$28$2Ac
 * </pre>
 * Note that unlike certain forms of URL encoding, a blank is not turned in to a "+" but
 * encoded as "$20". Many utilities that claim to URL encode strings do not, but actually do
 * URL form encoding which permits that.
 * <p>Created by Jeff Gaynor<br>
 * on 3/9/20 at  6:13 AM
 */
public class QDLCodec {
    public QDLCodec() {
        realCodec = new VariableCodec();
    }

    public QDLCodec(int currentType) {
        this.currentType = currentType;
        switch (currentType) {
            case QDLCodec.ALGORITHM_VENCODE:
                realCodec = new VariableCodec();
                break;
            case QDLCodec.ALGORITHM_URLCODE:
                realCodec = new edu.uiuc.ncsa.qdl.variables.codecs.URLCodec();
                break;
            case QDLCodec.ALGORITHM_BASE16:
                realCodec = new Base16Codec();
                break;
            case QDLCodec.ALGORITHM_BASE32:
                realCodec = new Base32Codec();
                break;
            case ALGORITHM_BASE64:
                realCodec = new Base64Codec();
                break;
            default:
                throw new IllegalArgumentException("unknown codec type '" + currentType + "'");
        }
    }

    public final static int ALGORITHM_VENCODE = 0;
    public final static int ALGORITHM_URLCODE = 1;
    public final static int ALGORITHM_BASE16 = 16;
    public final static int ALGORITHM_BASE32 = 32;
    public final static int ALGORITHM_BASE64 = 64;

    public String encode(String token) {
        return realCodec.encode(token);
    }

    long currentType = ALGORITHM_BASE64;

    AbstractCodec realCodec;

    public String decode(String encoded) {
        return realCodec.decode(encoded);
    }

    public static void main(String[] args) {
        try {
            String a = "abc$%^.*_-";
            QDLCodec c = new QDLCodec();
            String encoded = c.encode(a);
            String decoded = c.decode(encoded);
            System.out.println(a + " -> " + encoded);
            System.out.println(a + " =? " + (a.equals(decoded)) + ": " + decoded);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
