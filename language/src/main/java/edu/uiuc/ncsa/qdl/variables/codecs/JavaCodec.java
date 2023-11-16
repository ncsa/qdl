package edu.uiuc.ncsa.qdl.variables.codecs;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/15/23 at  9:29 AM
 */
public class JavaCodec implements AbstractCodec {
    @Override
    public String encode(String token) {
        return org.apache.commons.text.StringEscapeUtils.escapeJava(token);
    }

    @Override
    public String decode(String encoded) {
        return org.apache.commons.text.StringEscapeUtils.unescapeJava(encoded);
    }
}
