package org.qdl_lang.variables.codecs;

import org.qdl_lang.variables.MetaCodec;

/**
 * This wraps various codec (encode/decode) methods used in QDL in a
 * single interface. {@link MetaCodec} is instantiated
 * with the correct argument to the constructor and does the right thing.
 * <p>Created by Jeff Gaynor<br>
 * on 3/27/23 at  7:56 AM
 */
public interface AbstractCodec {
    String encode(String token);
    String decode(String encoded);
}
