package edu.uiuc.ncsa.qdl.variables.codecs;


/**
 * For encoding/decoding XSI Shell Language strings. Useful for command line
 * arguments and such to shell scripts.
 * <p>Created by Jeff Gaynor<br>
 * on 11/15/23 at  1:13 PM
 */
public class XSICodec implements AbstractCodec{
    @Override
    public String encode(String token) {
        return org.apache.commons.text.StringEscapeUtils.escapeXSI(token);
    }

    @Override
    public String decode(String encoded) {
        return org.apache.commons.text.StringEscapeUtils.unescapeXSI(encoded);
    }
}
