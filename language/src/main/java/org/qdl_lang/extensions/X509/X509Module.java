package org.qdl_lang.extensions.X509;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.crypto.Crypto;
import org.qdl_lang.expressions.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  3:17 PM
 */
public class X509Module extends JavaModule {
    public X509Module() {
    }

    public X509Module(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        X509Module x509Module = new X509Module(URI.create("qdl:/tools/x509"), "x509");
        X509 x509 = new X509();
        x509Module.setMetaClass(x509);
        ArrayList<QDLFunction> funcs = new ArrayList<>();

        funcs.add(x509.new LoadKeystore());
        x509Module.addFunctions(funcs);
/*
        ArrayList<QDLVariable> vars = new ArrayList<>();
        vars.add(crypto.new KeyType());
        x509Module.addVariables(vars);
*/
        if (state != null) {
            x509Module.init(state);
        }
        setupModule(x509Module);
        return x509Module;
    }

    List<String> dd = new ArrayList<>();

    @Override
    public List<String> getDescription() {
        if (dd.isEmpty()) {
            dd.add("QDL's cryptographic module. This has a variety of operations possible.");
            dd.add("It will allow you to create Elliptic,  RSA and AES (symmetric) keys,");
            dd.add("and encrypt both strings and stems of strings using these or symmetric keys.");
            dd.add("Note that if you de/en-crypt with one key, you en/de-crypt with the other:");
            dd.add("E.g.");
            dd.add("key. := " + Crypto.CREATE_KEY_NAME + "(2048); // create 2048 bit key pair");
            dd.add("  " + Crypto.DECRYPT_NAME + "(key., " + Crypto.ENCRYPT_NAME + "(key., ['a','b']))\n" +
                    "[a,b]");
            dd.add("\nhere a stem of two strings are encrypted with a key");
            dd.add("Note that the structure of the keys is essentially the same as a JSON Web key:\n");
            dd.add("https://www.rfc-editor.org/rfc/rfc7517\n");
            dd.add("You may have individual keys or have sets of them. A *key set* is a stem");
            dd.add("of keys whose id is used as its key in the stem. Operations generally work for");
            dd.add("individual keys or sets of them.");
            dd.add("\n\nThere is support to turn stems to/from signed JSON web tokens.");
            dd.add("There is basic support to read X 509 certificates.");
            dd.add("Finally, you may import/export individual keys to JWK or various PKCS");
            dd.add("formats, PKCS 1, 8 and X509 (for public keys).");
            dd.add( "See also:" + Crypto.IMPORT_NAME + " and " + Crypto.EXPORT_NAME + " for details.");
        }
        return dd;
    }
}
