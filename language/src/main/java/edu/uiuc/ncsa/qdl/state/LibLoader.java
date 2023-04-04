package edu.uiuc.ncsa.qdl.state;

import edu.uiuc.ncsa.qdl.variables.QDLStem;

/**
 * Class that is used to inject libraries into the state info() stem.
 * Contract is simple. Create an instance of this, put a refernce to it
 * in the modules section of the configuration.
 * <p>Created by Jeff Gaynor<br>
 * on 4/4/23 at  7:46 AM
 */
public interface LibLoader {
    /**
     * Typically this just calls {@link State#addLibEntries(String, QDLStem)} or
     * {@link State#addLibEntry(String, String, String)}. The entries are loader,
     * (so extensions of {@link edu.uiuc.ncsa.qdl.extensions.QDLLoader} not
     * modules!) Typically there is an entry called description that tells
     * what the library is/does.
     * @param state
     */
    void add(State state);
}
