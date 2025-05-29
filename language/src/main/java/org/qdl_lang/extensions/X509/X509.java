package org.qdl_lang.extensions.X509;

import net.sf.json.JSONObject;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.QDLValue;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/*
   Planned module for giving QDL an interface to keystores. No time to finish before
   release, so on hold.
 */
public class X509 implements QDLMetaModule {

    KeyStore keyStore;
    public static final String LOAD_KEYSTORE = "load_keystore";

    public class LoadKeystore implements QDLFunction {
        @Override
        public String getName() {
            return LOAD_KEYSTORE;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            String type = "jks"; // default
            if (qdlValues.length == 3) {
                if (qdlValues[2].isString()) {
                    type = qdlValues[2].asString().toLowerCase();
                } else {
                    throw new BadArgException("the type argument must be a string", 2);
                }
            }
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(new FileInputStream(qdlValues[0].toString()), qdlValues[1].toString().toCharArray());
            QDLStem stem = new QDLStem();
            stem.put("type", keyStore.getType());
            stem.put("size", (long) keyStore.size());
            Enumeration<String> aliases = keyStore.aliases();
            ArrayList<QDLValue> aliasesList = new ArrayList<>(100);
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                aliasesList.add(asQDLValue(alias));
            }
            QDLStem aliasStem = new QDLStem();
            aliasStem.getQDLList().setArrayList(aliasesList);
            stem.put("aliases", aliasStem);
            stem.put("provider", keyStore.getProvider().toString());
            return asQDLValue(stem);
        }

        /*
        jceks 	The proprietary keystore implementation provided by the SunJCE provider.
        jks 	The proprietary keystore implementation provided by the SUN provider.
        dks 	A domain keystore is a collection of keystores presented as a single logical keystore. It is specified by configuration data whose syntax is described in the DomainLoadStoreParameter class.
        pkcs11 	A keystore backed by a PKCS #11 token.
        pkcs12 	The transfer syntax for personal identity information as defined in PKCS #12.
         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + "(file_path, passphrase) - load the JKS keystore for this module.");
                    dd.add("file_path - full path to the keystore");
                    dd.add("passpharse - the passphrase for the store");
                    break;
                case 3:
                    dd.add(getName() + "(file_path, passphrase, type) - load the keystore for this module of the given type.");
                    dd.add("file_path - full path to the keystore");
                    dd.add("passpharse - the passphrase for the store");
                    dd.add("type - the type. Supported values are jks or pkcs12");
                    break;
            }
            dd.add("This loads a keystore for this module. You may query stored certs by alias and add them.");
            return dd;
        }
    }

    @Override
    public JSONObject serializeToJSON() {
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {

    }

}
