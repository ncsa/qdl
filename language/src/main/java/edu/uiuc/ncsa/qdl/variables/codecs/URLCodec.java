package edu.uiuc.ncsa.qdl.variables.codecs;

import edu.uiuc.ncsa.qdl.exceptions.QDLException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/27/23 at  7:59 AM
 */
public class URLCodec implements AbstractCodec{
    org.apache.commons.codec.net.URLCodec codec = new org.apache.commons.codec.net.URLCodec();

    @Override
    public String encode(String token) {
        if (token == null || token.isEmpty()) return token;
         String encoded = null;
         try {
             //replace + to %20
             return codec.encode(token).replace("+", "%20");
         } catch (EncoderException e) {
             throw new QDLException("Error: Could not encode string '" + token + "':" + e.getMessage(), e);
         }
    }

    @Override
    public String decode(String encoded) {
        if (encoded == null || encoded.isEmpty()) return encoded;
            try {
                return codec.decode(encoded);
            } catch (DecoderException e) {
                throw new QDLException("invalid escape sequence for'" + encoded + "'", e);
            }
    }
}
