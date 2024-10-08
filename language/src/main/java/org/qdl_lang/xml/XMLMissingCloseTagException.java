package org.qdl_lang.xml;

import org.qdl_lang.exceptions.QDLException;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/8/21 at  6:15 AM
 */
public class XMLMissingCloseTagException extends QDLException {
    public XMLMissingCloseTagException(String tagname) {
        super("Missing close tag \"" + tagname + "\"");
    }
}
