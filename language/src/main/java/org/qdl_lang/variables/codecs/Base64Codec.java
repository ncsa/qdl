package org.qdl_lang.variables.codecs;

import org.apache.commons.codec.binary.Base64;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/27/23 at  8:03 AM
 */
public class Base64Codec implements AbstractCodec {
    @Override
    public String encode(String token) {
        return Base64.encodeBase64URLSafeString(token.getBytes());
    }

    @Override
    public String decode(String encoded) {
        return new String(Base64.decodeBase64(encoded));

    }
}
