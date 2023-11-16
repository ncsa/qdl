package edu.uiuc.ncsa.qdl.variables.codecs;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/15/23 at  9:22 AM
 */
public class XMLCodec implements AbstractCodec {
    public XMLCodec(boolean isVersion1_1) {
        this.isVersion1_1 = isVersion1_1;
    }

    boolean isVersion1_1 = true;

    @Override
    public String encode(String token) {
        if (isVersion1_1) {
            return org.apache.commons.text.StringEscapeUtils.escapeXml11(token);
        }
        return org.apache.commons.text.StringEscapeUtils.escapeXml10(token);
    }

    @Override
    public String decode(String encoded) {
        // only one unescape version.
        return org.apache.commons.text.StringEscapeUtils.unescapeXml(encoded);
    }
}
