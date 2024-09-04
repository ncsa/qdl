package org.qdl_lang.variables.codecs;


/**
 * <p>Created by Jeff Gaynor<br>
 * on 11/15/23 at  9:16 AM
 */
public class HTMLCodec implements AbstractCodec {
    public HTMLCodec(boolean isHTML4) {
        this.isHTML4 = isHTML4;
    }

    boolean isHTML4 = true;

    @Override
    public String encode(String token) {
        if (isHTML4) {
            return org.apache.commons.text.StringEscapeUtils.escapeHtml4(token);
        }
        return org.apache.commons.text.StringEscapeUtils.escapeHtml3(token);
    }

    @Override
    public String decode(String encoded) {
        if (isHTML4) {
            return org.apache.commons.text.StringEscapeUtils.unescapeHtml4(encoded);
        }
        return org.apache.commons.text.StringEscapeUtils.unescapeHtml3(encoded);
    }
}
