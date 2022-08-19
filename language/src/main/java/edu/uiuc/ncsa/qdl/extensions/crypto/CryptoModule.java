package edu.uiuc.ncsa.qdl.extensions.crypto;

import edu.uiuc.ncsa.qdl.extensions.JavaModule;
import edu.uiuc.ncsa.qdl.module.Module;
import edu.uiuc.ncsa.qdl.state.State;

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
        funcs.add(crypto.new ReadKeys());
        funcs.add(crypto.new RSAEncrypt());
        funcs.add(crypto.new RSADecrypt());
        funcs.add(crypto.new RSACreateKey());
        funcs.add(crypto.new GetPublicKey());
        funcs.add(crypto.new SymmetricEncrypt());
        funcs.add(crypto.new SymmetricDecrypt());
        cryptoModule.addFunctions(funcs);
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
            dd.add("QDL's crypto graphic module. This has a variety of operations possible");
            dd.add("it will allow you to create a RSA keys  (so includes a public and private key)");
            dd.add("and encrypt both strings and stems of strings using these or symmetric keys.");
            dd.add("Note that if you de/en-crypt with one RSA key, you en/de-crypt with the other:");
            dd.add("E.g.");
            dd.add("key. := " + Crypto.RSA_CREATE_KEY_NAME + "(2048); // create 2048 bit key pair");
            dd.add("  " + Crypto.DECRYPT_NAME + "(key., " + Crypto.ENCRYPT_NAME + "(key., ['a','b']))\n" +
                    "[a,b]");
            dd.add("\nhere a stem of two strings are encrypted with a key");
            dd.add("Note that the structure of the keys is essentially the same as a JSON Web key:\n");
            dd.add("https://www.rfc-editor.org/rfc/rfc7517\n");
            dd.add("You may have individual keys or have sets of them. A *key set* is a stem");
            dd.add("of keys whose id is used as its key in the stem. Operations generally work for");
            dd.add("individual keys or sets of them.");
        }
        return dd;
    }
}
