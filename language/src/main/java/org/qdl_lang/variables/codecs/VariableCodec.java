package org.qdl_lang.variables.codecs;

import org.qdl_lang.exceptions.QDLException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/27/23 at  7:57 AM
 */
public class VariableCodec implements AbstractCodec {
    org.apache.commons.codec.net.URLCodec codec = new org.apache.commons.codec.net.URLCodec();

    @Override
    public String encode(String token) {
        if (token == null || token.isEmpty()) return token;
        String encoded = null;
        try {
            //replace + to %20
            encoded = codec.encode(token).replace("+", "%20");
            encoded = encoded.replace("$", "%24");
            encoded = encoded.replace("*", "%2A");
            encoded = encoded.replace("-", "%2D");
            encoded = encoded.replace(".", "%2E");
            encoded = encoded.replace("%", "$");

        } catch (EncoderException e) {
            throw new QDLException("Error: Could not encode string'" + token + "':" + e.getMessage(), e);
        }
        return encoded;
    }

    @Override
    public String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return encoded;
        String token = null;
        try {
            token = encoded.replace("$", "%");

            token = codec.decode(token);
        } catch (DecoderException e) {
            throw new QDLException("invalid escape sequence for'" + encoded + "'", e);
        }
        return token;
    }
}
