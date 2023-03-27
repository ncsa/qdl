package edu.uiuc.ncsa.qdl.variables.codecs;

import edu.uiuc.ncsa.security.core.util.TokenUtil;

import java.util.Locale;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/27/23 at  8:02 AM
 */
public class Base32Codec implements AbstractCodec{
    @Override
    public String encode(String token) {
        return TokenUtil.b32EncodeToken(token);
    }

    @Override
    public String decode(String encoded) {
        return TokenUtil.b32DecodeToken(encoded.toUpperCase(Locale.ROOT));
    }
}
