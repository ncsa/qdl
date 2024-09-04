package org.qdl_lang.exceptions;

import org.qdl_lang.state.State;
import org.qdl_lang.module.Module;

/**
 * Thrown when {@link Module#newInstance(State)} fails.
 * <p>Created by Jeff Gaynor<br>
 * on 4/2/20 at  7:56 AM
 */
public class ModuleInstantiationException extends QDLException {
    public ModuleInstantiationException() {
    }

    public ModuleInstantiationException(Throwable cause) {
        super(cause);
    }

    public ModuleInstantiationException(String message) {
        super(message);
    }

    public ModuleInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
