package edu.uiuc.ncsa.qdl.extensions.crypto;

import edu.uiuc.ncsa.qdl.evaluate.AbstractEvaluator;
import edu.uiuc.ncsa.qdl.exceptions.QDLException;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.extensions.QDLModuleMetaClass;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.qdl.vfs.VFSEntry;
import edu.uiuc.ncsa.security.core.util.FileUtil;
import edu.uiuc.ncsa.security.util.crypto.DecryptUtils;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKey;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeyUtil;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeys;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  1:34 PM
 */
public class Crypto implements QDLModuleMetaClass {

    public static final String RSA_CREATE_KEY_NAME = "create_rsa_key";

    public class RSACreateKey implements QDLFunction {
        @Override
        public String getName() {
            return RSA_CREATE_KEY_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            int keySize = -1;
            if (objects.length == 1) {
                if (!(objects[0] instanceof Long)) {
                    throw new QDLException("first argument of " + getName() + " must be an integer if present. Got '" + objects[0] + "'");
                }
                Long arg0 = (Long) objects[0];
                keySize = arg0.intValue();
                if (keySize % 256 != 0) {
                    throw new QDLException("the key size of " + keySize + " must be a multiple of 256");
                }
            }
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                if (0 < keySize) {
                    kpg.initialize(keySize);
                }
                KeyPair keyPair = kpg.generateKeyPair();
                JSONWebKey webKey = JSONWebKeyUtil.create(keyPair);
                JSONObject wk2 = JSONWebKeyUtil.toJSON(webKey);
                QDLStem stem = new QDLStem();
                stem.fromJSON(wk2);
                return stem;
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                throw new QDLException("error creating key pair: " + e.getMessage());
            }
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            if (argCount == 0) {
                dd.add(getName() + " create an RSA key with the default key size of 1024");
            }
            if (argCount == 1) {
                dd.add(getName() + "(key_size) create an RSA key with the given key_size > 1024.");
                dd.add("Note that the key_size must be a multiple of 256.");
            }
            dd.add("One hears of 'RSA key pairs', though in point of fact, the public bits of a key");
            dd.add("are always part of it, hence we do not explicitly create a public key, just an RSA key");
            dd.add("from which you may extract a public key with  " + GET_PUBLIC_KEY_NAME);
            return dd;
        }
    }

    public static final String READ_KEY_NAME = "read_keys";

    /**
     * Read key set from a file
     */
    public class ReadKeys implements QDLFunction {
        @Override
        public String getName() {
            return READ_KEY_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof String)) {
                throw new IllegalArgumentException(getName() + " requires a file name as its first argument");
            }
            VFSEntry vfsEntry = null;
            try {
                vfsEntry = state.getFileFromVFS((String) objects[0], AbstractEvaluator.FILE_OP_AUTO);
                String out = null;
                if (vfsEntry == null) {
                    out = FileUtil.readFileAsString((String) objects[0]);
                } else {
                    out = vfsEntry.getText();
                }
                JSONWebKeys jsonWebKeys = JSONWebKeyUtil.fromJSON(out);
                QDLStem keys = new QDLStem();
                if (jsonWebKeys.size() == 1) {
                    return webKeyToStem(jsonWebKeys.getDefault());
                }
                // otherwise, loop
                for (String key : jsonWebKeys.keySet()) {
                    JSONWebKey jsonWebKey = jsonWebKeys.get(key);
                    keys.put(key, webKeyToStem(jsonWebKey));
                }

                return keys;
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            return null;
        }
    }

    public static final String GET_PUBLIC_KEY_NAME = "get_rsa_public_key";

    /**
     * Get the public part of a key
     */
    public class GetPublicKey implements QDLFunction {
        @Override
        public String getName() {
            return GET_PUBLIC_KEY_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            // allows for single key as a stem or stem of them
            if(!(objects[0] instanceof QDLStem)){
                throw new IllegalArgumentException(getName() + " requires a stem as its argument");
            }
            QDLStem inStem = (QDLStem) objects[0];
            boolean singleKey = inStem.containsKey(JSONWebKeyUtil.KEY_TYPE);
            String id = null;
            if(singleKey){
                if(!inStem.containsKey(JSONWebKeyUtil.KEY_ID)){
                    throw new IllegalArgumentException("The argument to "+ getName() + " is not a properly formed key");
                }
                id = inStem.getString(JSONWebKeyUtil.KEY_ID);
             inStem = new QDLStem();
             inStem.put(id, objects[0]);
            }

            JSONWebKeys jsonWebKeys = null;

            JSONWebKeys pKeys = JSONWebKeyUtil.makePublic(jsonWebKeys);
            QDLStem outStem = new QDLStem();
            outStem.fromJSON(JSONWebKeyUtil.toJSON(pKeys));

           if(singleKey){
            return outStem.getString(id);
           }
            return outStem;
        }

        List<String> dd = new ArrayList<>();
        @Override
        public List<String> getDocumentation(int argCount) {
            if(dd.isEmpty()){
                dd.add(getName() + "(key.) ");
                dd.add("returns the public key(s). If this is a single key, a public key is returned.");
                dd.add("if this is a key set, then all of them are converted");
            }
            return dd;
        }
    }

    public static final String ENCRYPT_NAME = "rsa_encrypt";


    public class RSAEncrypt implements QDLFunction {
        @Override
        public String getName() {
            return ENCRYPT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException("The first argument of " + getName() + " must be a stem");
            }
            JSONWebKey jsonWebKey = getKeys((QDLStem) objects[0]);
            QDLStem arg = null;
            boolean usePrivateKey = true;
            boolean stringArg = false;
            boolean gotOne = false;
            if (objects[1] instanceof QDLStem) {
                gotOne = true;
                arg = (QDLStem) objects[1];
            }
            if (objects[1] instanceof String) {
                gotOne = true;
                stringArg = true;
                arg = new QDLStem();
                arg.put(0L, objects[1]);
            }

            if (!gotOne) {
                return objects[1]; // nix to do
            }
            if (objects.length == 3) {
                if (!(objects[2] instanceof Boolean)) {
                    throw new IllegalArgumentException("the last argument of " + getName() + " must be a boolean. Default is true");
                }
                usePrivateKey = (Boolean) objects[2];
            }
            QDLStem out = new QDLStem();
            for (Object key : arg.keySet()) {
                Object obj = arg.get(key);
                String result;
                String inString = (String) obj;
                if (!(obj instanceof String)) {
                    out.putLongOrString(key, obj);
                    continue;
                }
                try {
                    if (usePrivateKey) {
                        result = DecryptUtils.encryptPrivate(jsonWebKey.privateKey, inString);
                    } else {
                        result = DecryptUtils.encryptPublic(jsonWebKey.publicKey, inString);
                    }
                    out.putLongOrString(key, result);
                } catch (GeneralSecurityException gsx) {
                    throw new IllegalArgumentException("could not encrypt argument for key='" + key + "' with value ='" + obj + "'");
                }
            }
            if (stringArg) {
                return out.getString(0L);
            }
            return out;
        }

        List<String> dd = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (dd.isEmpty()) {
                dd.add(getName() + "(keys, string|stem.{, use_private})");
                dd.add("encrypt the string or stem of them using the given RSA key pair.");
                dd.add("NOTE: You can only encrypt a string that has fewer bits than the key.");
                dd.add("\nE.g.\nIf your key is 1024 bits, then 1024/8 = 128 bytes or characters is the max length string.\n");
                dd.add("A final, optional arg  is whether to use the private (true, default) or public (false) key");
                dd.add("Note that the result is base 64 encoded, since the result of the encryption will be an array of bytes.");
                dd.add("One final reminder is that if encrypt/decrypt with one key and decrypt/encrypt with the" +
                        "\nother or you will get an error");
            }
            return dd;
        }
    }

    public static final String DECRYPT_NAME = "rsa_decrypt";

    public class RSADecrypt implements QDLFunction {
        @Override
        public String getName() {
            return DECRYPT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException("The first argument of " + getName() + " must be a stem");
            }
            JSONWebKey jsonWebKey = getKeys((QDLStem) objects[0]);
            QDLStem arg = null;
            boolean usePrivateKey = true;
            boolean stringArg = false;
            boolean gotOne = false;
            if (objects[1] instanceof QDLStem) {
                gotOne = true;
                arg = (QDLStem) objects[1];
            }
            if (objects[1] instanceof String) {
                gotOne = true;
                stringArg = true;
                arg = new QDLStem();
                arg.put(0L, objects[1]);
            }

            if (!gotOne) {
                return objects[1]; // nix to do
            }
            if (objects.length == 3) {
                if (!(objects[2] instanceof Boolean)) {
                    throw new IllegalArgumentException("the last argument of " + getName() + " must be a boolean. Default is true");
                }
                usePrivateKey = (Boolean) objects[2];
            }
            QDLStem out = new QDLStem();
            for (Object key : arg.keySet()) {
                Object obj = arg.get(key);
                String result;
                String inString = (String) obj;
                if (!(obj instanceof String)) {
                    out.putLongOrString(key, obj);
                    continue;
                }
                try {
                    if (usePrivateKey) {
                        result = DecryptUtils.decryptPrivate(jsonWebKey.privateKey, inString);
                    } else {
                        result = DecryptUtils.decryptPublic(jsonWebKey.publicKey, inString);
                    }
                    out.putLongOrString(key, result);
                } catch (GeneralSecurityException | UnsupportedEncodingException gsx) {
                    throw new IllegalArgumentException("could not encrypt argument for key='" + key + "' with value ='" + obj + "'");
                }
            }
            if (stringArg) {
                return out.getString(0L);
            }
            return out;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            return null;
        }
    }

    protected JSONWebKey getKeys(QDLStem keys) {
        try {
            return JSONWebKeyUtil.getJsonWebKey(keys.toJSON().toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new QDLException("error creating keys:" + e.getMessage(), e);
        }
    }

    protected QDLStem webKeyToStem(JSONWebKey jsonWebKey) {
        QDLStem keys = new QDLStem();
        JSONObject json = JSONWebKeyUtil.toJSON(jsonWebKey);
        keys.fromJSON(json);
        return keys;
    }

    public static final String SYMM_ENCRYPT_NAME = "s_encrypt";

    public class SymmetricEncrypt implements QDLFunction {
        @Override
        public String getName() {
            return SYMM_ENCRYPT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            return sDeOrEnCrypt(objects, state, true, getName());
        }

        List<String> dd = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (dd.isEmpty()) {
                dd.add(getName() + "(key, target)");
                dd.add("Symmetric encryption on the target, returning a");
                dd.add("base 64 encoded byte string.");
                dd.add("Note 1: target may be a string or stem  of them");
                dd.add("Note 2: in QDL it is easy to make a key with random_string(n), n*8 is the bit count.");
                dd.add("\nE.g.");
                dd.add("key := random_string(64); // 64*8 == 512 bit strength");
                dd.add("target := 'mairzy doats and dozey doats';");
                dd.add("    " + getName() + "(key, target)");
                dd.add("HUhqqoHJc3-AqWbRGbS-6V2KnXQ26tiR9ivkmA");
                dd.add("//Note that the encrypted output will vary since the key is random.");
                dd.add("    " + SYMM_DECRYPT_NAME +"(key, 'HUhqqoHJc3-AqWbRGbS-6V2KnXQ26tiR9ivkmA')");
                dd.add("mairzy doats and dozey doats");
            }
            return dd;
        }
    }
         //module_import(module_load(info('lib').'crypto', 'java'))
    public static final String SYMM_DECRYPT_NAME = "s_decrypt";

    public class SymmetricDecrypt implements QDLFunction {
        @Override
        public String getName() {
            return SYMM_DECRYPT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            return sDeOrEnCrypt(objects, state, false, getName());
        }

        List<String> dd = new ArrayList<>();
        @Override
        public List<String> getDocumentation(int argCount) {
            if(dd.isEmpty()) {
                dd.add(getName() + "(key, target)");
                dd.add("Symmetric key decryption for an encrypted, base 64 byte string.");
                dd.add("This returns the original string.");
                dd.add("See also:" + SYMM_ENCRYPT_NAME);
            }
            return dd;
        }
    }

    /**
     * Utility for symmetric key encode/decode.
     *
     * @param key
     * @param s
     * @param isEncrypt
     * @return
     */
    protected String decodeString(byte[] key, String s, boolean isEncrypt) {
        if (isEncrypt) {
            return DecryptUtils.sEncrypt(key, s);
        }
        return DecryptUtils.sDecrypt(key, s);
    }

    public Object sDeOrEnCrypt(Object[] objects, State state, boolean isEncrypt, String name) {
        if (!(objects[0] instanceof String)) {
            throw new IllegalArgumentException("the first argument to " + name + " must be a base64 encoded key");
        }
        byte[] key = Base64.decodeBase64((String) objects[0]);
        QDLStem inStem = null;
        QDLStem outStem = new QDLStem();
        boolean isStringArg = false;
        if (objects[1] instanceof String) {
            isStringArg = true;
            inStem = new QDLStem();
            inStem.put(0L, (String) objects[1]);
        }
        if (objects[1] instanceof QDLStem) {
            inStem = (QDLStem) objects[1];
        }
        if (inStem == null) {
            throw new IllegalArgumentException("second argument of " + name + " must be a stem or string.");
        }
        for (Object stemKey : inStem.keySet()) {
            Object obj = inStem.get(stemKey);
            if (obj instanceof String) {
                String target = (String) obj;
                outStem.putLongOrString(stemKey, decodeString(key, target, isEncrypt));
            } else {
                outStem.putLongOrString(stemKey, outStem);// don't touch if not string
            }
        }
        if (isStringArg) {
            return outStem.get(0L);
        }
        return outStem;
    }

}
