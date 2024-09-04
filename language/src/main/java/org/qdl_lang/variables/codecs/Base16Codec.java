package org.qdl_lang.variables.codecs;

import org.qdl_lang.exceptions.QDLException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/27/23 at  8:02 AM
 */
public class Base16Codec implements AbstractCodec {
    @Override
    public String encode(String token) {
        return Hex.encodeHexString(token.getBytes());
    }

    @Override
    public String decode(String encoded) {
        try {
            byte[] decoded = Hex.decodeHex(encoded.toCharArray());
            return new String(decoded);
        }catch(DecoderException decoderException){
            throw new QDLException("could not decode '" + encoded + "':" + decoderException.getMessage());
        }
    }
}
