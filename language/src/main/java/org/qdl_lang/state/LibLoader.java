package org.qdl_lang.state;

import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.extensions.QDLLoader;

/**
 * Class that is used to inject libraries into the state info() stem.
 * This allows users (in the workspace) to issue
 * <pre>
 *     info().'lib'
 * </pre>
 * and get a listing of custom classes. This will also use the jload
 * mechanism of the workspace on them, so users can very conveniently
 * either list your extensions or load them.
 *
 * <p>The contract is simple. Create an instance of this, put a reference to it
 * in the modules section of the configuration.</p>
 * <h2>A compelete example (from OA4MP)</h2>
 * <pre>
 *  public class OA2LibLoader implements LibLoader {
 *     protected String libKey = "oa2";
 *
 *     // Calls the addEntries method from the current state. This just adds the QDLStem
 *     // of library entries, whose key is libKey.
 *
 *     public void add(State state) {
 *         QDLStem lib = new QDLStem();
 *         lib.put("description", "OA4MP tools for ACLs, JWTs, claims as well as token handlers");
 *         state.addLibEntries(libKey, createEntries());
 *     }
 *     // Creates the individual entries.
 *     protected QDLStem createEntries(){
 *         QDLStem lib = new QDLStem();
 *         lib.put("description", "OA4MP tools for ACLs, JWTs, claims as well as token handlers");
 *         QDLStem subLib = new QDLStem();
 *         subLib.put("claims", ClaimsLoader.class.getCanonicalName());
 *         subLib.put("jwt", JWTLoader.class.getCanonicalName());
 *         subLib.put("acl", ACLoader.class.getCanonicalName());
 *         lib.put("util", subLib);
 *         return lib;
 *     }
 * }
 * </pre>
 * The net effect is here, issuing info().'lib' in the OA4MP workspace yields
 * <pre>
 *      info().lib
 * {
 *  oa2: {
 *   description:OA4MP tools for ACLs, JWTs, claims as well as token handlers,
 *     util:  {
 *    jwt:edu.uiuc.ncsa.myproxy.oa4mp.qdl.util.JWTLoader,
 *    claims:edu.uiuc.ncsa.myproxy.oa4mp.qdl.claims.ClaimsLoader,
 *    acl:edu.uiuc.ncsa.myproxy.oa4mp.qdl.acl.ACLoader
 *   },//.. more
 * </pre>
 *
 * <p>Created by Jeff Gaynor<br>
 * on 4/4/23 at  7:46 AM
 */
public interface LibLoader {
    /**
     * Typically this just calls {@link State#addLibEntries(String, QDLStem)} or
     * {@link State#addLibEntry(String, String, String)}. The entries are loader,
     * (so extensions of {@link QDLLoader} not
     * modules!) Typically there is an entry called description that tells
     * what the library is/does.
     * @param state
     */
    void add(State state);
}
