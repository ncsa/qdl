package edu.uiuc.ncsa.qdl.variables;

import edu.uiuc.ncsa.qdl.variables.codecs.*;

/**
 * This will convert a string and encode or decode all characters that are not [a-z][A-Z] as
 * per various standards. This allows QDL to write scripts in other languages or pass
 * around escaped strings.
 * <p>Created by Jeff Gaynor<br>
 * on 3/9/20 at  6:13 AM
 */
public class MetaCodec {
    public MetaCodec() {
        realCodec = new VariableCodec();
    }

    public MetaCodec(int currentType) {
        this.currentType = currentType;
        realCodec = createCodec(currentType);
    }
    public static AbstractCodec createCodec(Object object) {
        if(object instanceof String){
            return createCodec((String)object);
        }
        if(object instanceof Long){
            return createCodec(((Long)object).intValue());
        }
        if(object instanceof Integer){
            return createCodec(((Integer)object));
        }
        throw new IllegalArgumentException("unknown codec type'" + object + "'");
    }

    public static AbstractCodec createCodec(int currentType) {
        switch (currentType) {
            case MetaCodec.ALGORITHM_VENCODE:
                return new VariableCodec();
            case MetaCodec.ALGORITHM_URLCODE:
                return new edu.uiuc.ncsa.qdl.variables.codecs.URLCodec();
            case MetaCodec.ALGORITHM_BASE16:
                return new Base16Codec();
            case MetaCodec.ALGORITHM_BASE32:
                return new Base32Codec();
            case ALGORITHM_BASE64:
                return new Base64Codec();
            case ALGORITHM_CSV:
                return new CSVCodec();
            case ALGORITHM_ECMA:
                return new ECMACodec();
            case ALGORITHM_HTML3:
                return new HTMLCodec(false);
            case ALGORITHM_HTML4:
                return new HTMLCodec(true);
            case ALGORITHM_XML_1_0:
                return new XMLCodec(false);
            case ALGORITHM_XML_1_1:
                return new XMLCodec(true);
            case ALGORITHM_JAVA:
                return new JavaCodec();
            case ALGORITHM_JSON:
                return new JSONCodec();
            case ALGORITHM_XSI:
                return new XSICodec();
            default:
                throw new IllegalArgumentException("unknown codec type '" + currentType + "'");
        }
    }

    public static AbstractCodec createCodec(String currentType) {
        switch (currentType) {
            case MetaCodec.ALGORITHM_VENCODE_NAME:
                return new VariableCodec();
            case MetaCodec.ALGORITHM_URLCODE_NAME:
                return new edu.uiuc.ncsa.qdl.variables.codecs.URLCodec();
            case MetaCodec.ALGORITHM_BASE16_NAME:
                return new Base16Codec();
            case MetaCodec.ALGORITHM_BASE32_NAME:
                return new Base32Codec();
            case ALGORITHM_BASE64_NAME:
                return new Base64Codec();
            case ALGORITHM_CSV_NAME:
                return new CSVCodec();
            case ALGORITHM_ECMA_NAME:
                return new ECMACodec();
            case ALGORITHM_HTML3_NAME:
                return new HTMLCodec(false);
            case ALGORITHM_HTML4_NAME:
                return new HTMLCodec(true);
            case ALGORITHM_XML_1_0_NAME:
                return new XMLCodec(true);
            case ALGORITHM_XML_1_1_NAME:
                return new XMLCodec(false);
            case ALGORITHM_JAVA_NAME:
                return new JavaCodec();
            case ALGORITHM_JSON_NAME:
                return new JSONCodec();
            case ALGORITHM_XSI_NAME:
                return new XSICodec();
            default:
                throw new IllegalArgumentException("unknown codec type '" + currentType + "'");
        }
    }

    public final static int ALGORITHM_VENCODE = 0;
    public final static int ALGORITHM_URLCODE = 1;

    public final static int ALGORITHM_BASE16 = 16;
    public final static int ALGORITHM_BASE32 = 32;
    public final static int ALGORITHM_BASE64 = 64;
    public final static int ALGORITHM_XML_1_0 = 100;
    public final static int ALGORITHM_XML_1_1 = 101;
    public final static int ALGORITHM_JSON = 110;
    public final static int ALGORITHM_JAVA = 120;
    public final static int ALGORITHM_HTML3 = 130;
    public final static int ALGORITHM_HTML4 = 131;
    public final static int ALGORITHM_CSV = 140;
    public final static int ALGORITHM_ECMA = 150;
    public final static int ALGORITHM_XSI = 160;

    public final static String ALGORITHM_BASE16_NAME = "b16";
    public final static String ALGORITHM_BASE32_NAME = "b32";
    public final static String ALGORITHM_BASE64_NAME = "b64";
    public final static String ALGORITHM_CSV_NAME = "csv";
    public final static String ALGORITHM_ECMA_NAME = "ecma";
    public final static String ALGORITHM_HTML3_NAME = "html3";
    public final static String ALGORITHM_HTML4_NAME = "html";
    public final static String ALGORITHM_JAVA_NAME = "java";
    public final static String ALGORITHM_JSON_NAME = "json";
    public final static String ALGORITHM_URLCODE_NAME = "url";
    public final static String ALGORITHM_VENCODE_NAME = "qdl_var";
    public final static String ALGORITHM_XML_1_0_NAME = "xml";
    public final static String ALGORITHM_XML_1_1_NAME = "xml1.1";
    public final static String ALGORITHM_XSI_NAME = "xsi";

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
            MetaCodec c = new MetaCodec();
            String encoded = c.encode(a);
            String decoded = c.decode(encoded);
            System.out.println(a + " -> " + encoded);
            System.out.println(a + " =? " + (a.equals(decoded)) + ": " + decoded);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
