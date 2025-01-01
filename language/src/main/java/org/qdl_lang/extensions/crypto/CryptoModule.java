package org.qdl_lang.extensions.crypto;

import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  3:17 PM
 */
public class CryptoModule extends JavaModule {
    public CryptoModule() {
    }

    public CryptoModule(URI namespace, String alias) {
        super(namespace, alias);
    }

    @Override
    public Module newInstance(State state) {
        CryptoModule cryptoModule = new CryptoModule(URI.create("qdl:/tools/crypto"), "crypto");
        Crypto crypto = new Crypto();
        cryptoModule.setMetaClass(crypto);
        ArrayList<QDLFunction> funcs = new ArrayList<>();

        funcs.add(crypto.new ImportKey());
        funcs.add(crypto.new ImportCert());
        funcs.add(crypto.new ExportKeys());
        funcs.add(crypto.new Encrypt());
        funcs.add(crypto.new Decrypt());
        funcs.add(crypto.new CreateKey());
        funcs.add(crypto.new GetPublicKey());
        funcs.add(crypto.new ReadOID());
        funcs.add(crypto.new ToJWT());
        funcs.add(crypto.new FromJWT());
        funcs.add(crypto.new VerifyJWT());
        cryptoModule.addFunctions(funcs);
        ArrayList<QDLVariable> vars = new ArrayList<>();
        vars.add(crypto.new KeyType());
        cryptoModule.addVariables(vars);
        if (state != null) {
            cryptoModule.init(state);
        }
        setupModule(cryptoModule);
        return cryptoModule;
    }

    List<String> dd = new ArrayList<>();

    @Override
    public List<String> getDescription() {
        if (dd.isEmpty()) {
            dd.add("QDL's crypto graphic module. This has a variety of operations possible.");
            dd.add("It will allow you to create a Both Elliptic and RSA keys  (so includes a public and private key)");
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
            dd.add("\n\nAdditionally, you may import/export individual keys to various PKCS pem");
            dd.add("formats, PKCS 1, 8 and X509 (for public keys).");
            dd.add( "See also:" + Crypto.IMPORT_NAME + " and " + Crypto.EXPORT_NAME + " for details.");
        }
        return dd;
    }
}
