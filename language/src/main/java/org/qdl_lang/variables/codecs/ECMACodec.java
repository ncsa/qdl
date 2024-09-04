package org.qdl_lang.variables.codecs;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/15/23 at  9:28 AM
 */
public class ECMACodec implements AbstractCodec{
    @Override
    public String encode(String token) {
        return org.apache.commons.text.StringEscapeUtils.escapeEcmaScript(token);
    }

    @Override
    public String decode(String encoded) {
        return org.apache.commons.text.StringEscapeUtils.unescapeEcmaScript(encoded);
    }
}
