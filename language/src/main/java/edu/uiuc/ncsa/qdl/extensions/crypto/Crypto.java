package edu.uiuc.ncsa.qdl.extensions.crypto;

import edu.uiuc.ncsa.qdl.evaluate.MathEvaluator;
import edu.uiuc.ncsa.qdl.extensions.QDLFunction;
import edu.uiuc.ncsa.qdl.extensions.QDLModuleMetaClass;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.util.QDLFileUtil;
import edu.uiuc.ncsa.qdl.variables.QDLStem;
import edu.uiuc.ncsa.security.util.crypto.DecryptUtils;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKey;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeyUtil;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeys;
import edu.uiuc.ncsa.security.util.jwk.JWKUtil2;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  1:34 PM
 */
public class Crypto implements QDLModuleMetaClass {
    public JWKUtil2 getJwkUtil() {
        if (jwkUtil == null) {
            jwkUtil = new JWKUtil2();
        }
        return jwkUtil;
    }

    public void setJwkUtil(JWKUtil2 jwkUtil) {
        this.jwkUtil = jwkUtil;
    }

    JWKUtil2 jwkUtil;

    public static final String CREATE_KEY_NAME = "create_key";

    public class CreateKey implements QDLFunction {
        @Override
        public String getName() {
            return CREATE_KEY_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            // default is RSA key. 1024 bits, RS256 alg.
            String type = "RSA";
            int keyLength= 1024;
            String alg = "RS256";
            String curve = "P-256";
            JSONWebKey webKey = null;
            if(objects.length == 0){
                 webKey = getJwkUtil().createRSAKey(keyLength,alg);
            }
            if(objects.length == 1){
                if(objects[0] instanceof QDLStem){
                    boolean unknownType = true;
                    QDLStem stem = (QDLStem) objects[0];
                    if(stem.containsKey("type")){
                        type = stem.getString("type");
                    } else{
                        throw new IllegalArgumentException(getName() + " is missing the type of the key. Must be RSA or EC");
                    }
                    if(type.equals("RSA")){
                               unknownType = false;
                        if(stem.containsKey("length")){
                            keyLength = stem.getLong("length").intValue();
                        }
                        if (keyLength % 256 != 0) {
                            throw new IllegalArgumentException("the key size of " + keyLength + " must be a multiple of 256");
                        }
                        if(stem.containsKey("alg")){
                            alg = stem.getString("alg");
                        }

                        webKey = getJwkUtil().createRSAKey(keyLength, alg);
                    }
                    if(type.equals("EC")){
                        unknownType = false;
                        if(stem.containsKey("curve")){
                            curve = stem.getString("curve");
                        }
                        if(stem.containsKey("alg")){
                            alg = stem.getString("alg");
                        }else{
                            alg = "ES256"; // default for elliptic curves.
                        }
                        webKey = getJwkUtil().createECKey(curve, alg);
                    }

                    if(unknownType){
                        throw new IllegalArgumentException("unknown key type '" + stem.get("type") + "'");
                    }
                }else{
                    if(!(objects[0] instanceof Long)){
                        throw new IllegalArgumentException("single argument must be the length of the RSA key");

                    }
                    keyLength = ((Long)objects[0]).intValue();
                    if (keyLength % 256 != 0) {
                        throw new IllegalArgumentException("the key size of " + keyLength + " must be a multiple of 256");
                    }
                    webKey = getJwkUtil().createRSAKey(keyLength, alg);
                }
                // RSA key, gives size
            }
            JSONObject wk2 = JSONWebKeyUtil.toJSON(webKey);
            QDLStem stem = new QDLStem();
            stem.fromJSON(wk2);
            return stem;

        }


        /*
       )ws set debug on
            module_import(module_load(info().lib.tools.crypto, 'java'));
        keys. := create_rsa_key(2048, 3)
        get_rsa_public_key(keys.)
         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 0:
                    dd.add(getName() + "() create an RSA key with the default key size of 1024");
                    break;
                case 1:
                    dd.add(getName() + "(key_size | params.) create an RSA key with the given key_size > 1024.");
                    dd.add("Note that the key_size must be a multiple of 256.");
                    dd.add("If a stem of parameters is passed, it is of the form");
                    dd.add("  {'type' :'RSA'|'EC, 'alg':algorithm, 'length':rsa key length, 'curve' : elliptic curve.}");
                    dd.add("E.g.");
                    dd.add("    " + getName() + "({'type':'EC':'curve':'P-256', 'alg':'ES256'})");
                    dd.add("would use the curve P-256 with the ES256 algorithm to create an ellitpci curve key.");
                    break;
            }
            dd.add("One hears of 'key pairs', though in point of fact, the public bits of a key");
            dd.add("are always part of it, hence we do not explicitly create a public key, just a key");
            dd.add("from which you may extract a public key with  " + GET_PUBLIC_KEY_NAME);
            dd.add("Note that for RSA keys, the algorithm (for consumers of the key) defaults to RS256.");
            return dd;
        }
    }

    public static final String IMPORT_KEYS_NAME = "import_jwks";

    /**
     * Read key set from a file
     */
    public class ImportJWKS implements QDLFunction {
        @Override
        public String getName() {
            return IMPORT_KEYS_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof String)) {
                throw new IllegalArgumentException(getName() + " requires a file name as its first argument");
            }
            String out = QDLFileUtil.readTextFile(state, (String) objects[0]);
            JSONWebKeys jsonWebKeys = getJwkUtil().fromJSON(out);
            QDLStem keys = new QDLStem();
            if (jsonWebKeys.size() == 1) {
                return webKeyToStem(jsonWebKeys.getDefault());
            }
            // otherwise, loop
            for (String key : jsonWebKeys.keySet()) {
                JSONWebKey jsonWebKey = jsonWebKeys.get(key);
                if (jsonWebKeys.size() == 1) {
                    return jsonWebKeys;
                }
                keys.put(key, webKeyToStem(jsonWebKey));
            }

            return keys;
        }

        List<String> dd = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (dd.isEmpty()) {
                dd.add(getName() + "(file_path) - read a JSON webkey (as per RFC 7517)");
                dd.add("Import a key set from RFC7517 format");
            }
            return dd;
        }
    }

    public static final String EXPORT_KEYS_NAME = "export_jwks";

    public class ExportJWKS implements QDLFunction {
        @Override
        public String getName() {
            return EXPORT_KEYS_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException("The first argument of " + getName() + " must be a stem");
            }
            if (!(objects[1] instanceof String)) {
                throw new IllegalArgumentException("The second argument of " + getName() + " must be a string");
            }
            QDLStem inStem = (QDLStem) objects[0];
            String filePath = (String) objects[1];
            JSONArray array = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            if (isSingleKey(inStem)) {
                // single key
                array.add(inStem.toJSON());

            } else {
                for (Object k : inStem.keySet()) {
                    QDLStem currentStem = (k instanceof String) ? inStem.getStem((String) k) : inStem.getStem((Long) k);
                    // have to get the only entry
                    array.add(currentStem.toJSON());
                }
            }
            jsonObject.put(JWKUtil2.KEYS, array);
            QDLFileUtil.writeTextFile(state, filePath, jsonObject.toString(2));
            return Boolean.TRUE;
        }
        /*
                         module_import(module_load(info().lib.tools.crypto, 'java'));
                           keys. := create_rsa_key(2048, 3)
  export_jwks(keys., '/tmp/keys.jwks')
      q. := import_jwks('/tmp/keys.jwks')

         */

        List<String> dd = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (dd.isEmpty()) {
                dd.add(getName() + "(keys., file_path) - export a key or keyset to RFC 7517 format");
                dd.add("This will skip unrecognized entries in the stem");
            }
            return dd;
        }
    }

    public static final String GET_PUBLIC_KEY_NAME = "rsa_public_key";

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
        public Object evaluate(Object[] objects, State state) throws NoSuchAlgorithmException, InvalidKeySpecException {
            // allows for single key as a stem or stem of them
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " requires a stem as its argument");
            }
            QDLStem inStem = (QDLStem) objects[0];
            if (isSingleKey(inStem)) {
                JSONWebKey jsonWebKey = getJwkUtil().getJsonWebKey((JSONObject) inStem.toJSON());
                JSONWebKey pKey = JSONWebKeyUtil.makePublic(jsonWebKey);
                QDLStem outStem = new QDLStem();
                outStem.fromJSON(JSONWebKeyUtil.toJSON(pKey));
                return outStem;
            }
            QDLStem outStem = new QDLStem();
            // try to process each entry as a separate key
            for (Object kk : inStem.keySet()) {
                QDLStem currentStem = (kk instanceof String) ? inStem.getStem((String) kk) : inStem.getStem((Long) kk);
                JSONWebKey jsonWebKey = getJwkUtil().getJsonWebKey((JSONObject) currentStem.toJSON());
                JSONWebKey pKey = JSONWebKeyUtil.makePublic(jsonWebKey);
                QDLStem tempStem = new QDLStem();
                tempStem.fromJSON(JSONWebKeyUtil.toJSON(pKey));
                outStem.putLongOrString(kk, tempStem);
            }
            return outStem;
        }

        /*
         key. := rsa_create_key(2048); // create 2048 bit key pair
         rsa_public_key(key.)
         */
        List<String> dd = new ArrayList<>();

        @Override
        public List<String> getDocumentation(int argCount) {
            if (dd.isEmpty()) {
                dd.add(getName() + "(key.) - get the public part(s) of the key(s)");
                dd.add("If this is a single key, a public key is returned.");
                dd.add("if this is a key set, then all of them are converted");
            }
            return dd;
        }
    }

    public static final String ENCRYPT_NAME = "encrypt";


    public class Encrypt implements QDLFunction {
        @Override
        public String getName() {
            return ENCRYPT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
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
                        if (jsonWebKey.privateKey == null) {
                            throw new IllegalArgumentException("This is not a private key");
                        }
                        result = DecryptUtils.encryptPrivate(jsonWebKey.type,jsonWebKey.privateKey, inString);
                    } else {
                        if (jsonWebKey.publicKey == null) {
                            throw new IllegalArgumentException("Invalid public key");
                        }
                        result = DecryptUtils.encryptPublic(jsonWebKey.type,jsonWebKey.publicKey, inString);
                    }
                    out.putLongOrString(key, result);
                } catch (GeneralSecurityException gsx) {
                    // Clean up exception with a better message
                    throw new IllegalArgumentException(getName() + " could not encrypt argument for key='" + key + "' with value ='" + obj + "'");
                }
            }
            if (stringArg) {
                return out.getString(0L);
            }
            return out;
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + "(key., arg|arg.) - encrypt a string or stem of them with the private key");
                    break;
                case 3:
                    dd.add(getName() + "(key., arg|arg., use_private) - encrypt a string or stem of them with the private key if use_private is true");
                    dd.add("   or use the public key if false. Default is true");
                    break;
            }
            dd.add("key. - the RSA key to use");
            dd.add("arg|arg. - a string or a stem of strings");
            if (argCount == 3) {

                dd.add("use_private - use the private key (if true, this is the default) and the public key if false");
            }
            dd.add("NOTE: You can only encrypt a string that has fewer bits than the key.");
            dd.add("\nE.g.\nIf your key is 1024 bits, then 1024/8 = 128 bytes or characters is the max length string.\n");
            dd.add("Note that the result is base 64 encoded, since the result of the encryption will be an array of bytes.");
            dd.add("One final reminder is that if encrypt/decrypt with one key and decrypt/encrypt with the" +
                    "\nother or you will get an error");
            dd.add("E.g.");
            dd.add("   " + getName() + "(key., 'marizy doats')");
            dd.add("(whole bunch of base 64stuff that depends on the key)");
            dd.add("Since this was encrypted with the private key, you would need to specify using the");
            dd.add("public key in " + DECRYPT_NAME + " (which is, incidentally, the default there).");


            return dd;
        }
    }

    public static final String DECRYPT_NAME = "decrypt";

    public class Decrypt implements QDLFunction {
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
            boolean usePrivateKey = false;
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
                        if (jsonWebKey.privateKey == null) {
                            throw new IllegalArgumentException("This is not a private key");
                        }
                            result = DecryptUtils.decryptPrivate(jsonWebKey.type ,jsonWebKey.privateKey, inString);
                    } else {
                        if (jsonWebKey.publicKey == null) {
                            throw new IllegalArgumentException("Invalid public key");
                        }
                        result = DecryptUtils.decryptPublic(jsonWebKey.type,  jsonWebKey.publicKey, inString);
                    }
                    out.putLongOrString(key, result);
                } catch (GeneralSecurityException | UnsupportedEncodingException gsx) {
                    throw new IllegalArgumentException(getName() + " could not encrypt argument for key='" + key + "' with value ='" + obj + "'");
                }
            }
            if (stringArg) {
                return out.getString(0L);
            }
            return out;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 2:
                    doxx.add(getName() + "(key., arg|arg.) - decrypt the argument using the public key");
                    break;
                case 3:
                    doxx.add(getName() + "(key., arg|arg., use_private) - decrypt the argument using the private key if use_private == true");
            }
            if (doxx.isEmpty()) {
                doxx.add("key. - the RSA key you want to use");
                doxx.add("arg | arg. - the string or stem of strings you want to decrypt");
                if (argCount == 3) {
                    doxx.add("use_private - if true, decrypt using the private key. Note that this implies " + ENCRYPT_NAME);
                    doxx.add("was called using the public key. Default is false.");
                }
                doxx.add("RSA decryption of the argument. The default is to use the public key as a compliment");
                doxx.add("to the " + ENCRYPT_NAME + " which uses the private key, so operations are seamless");
                doxx.add("Remember that the length of the arg or each element of arg.");
                doxx.add("E.g.");
                doxx.add(getName() + "(key.,my_string)");
                doxx.add("E.g. (roundtrip)");
                doxx.add("   " + getName() + "(key., " + ENCRYPT_NAME + "(key., 'marizy doats'))");
                doxx.add("marizy doats");
                doxx.add("In this case, the encryption happens with the private key and the decryption with ");
                doxx.add("the public part of the key.");
                doxx.add("Note that the public private parts must be opposite, so ");
                doxx.add("E.g. (roundtrip, with keys type reverse)");
                doxx.add("   " + getName() + "(key., " + ENCRYPT_NAME + "(key., 'marizy doats', false), true)");
                doxx.add("marizy doats");


            }
            return doxx;
        }
    }

    protected JSONWebKey getKeys(QDLStem keys) {
        return JSONWebKeyUtil.getJsonWebKey(keys.toJSON().toString());
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
                dd.add(getName() + "(key, target) - Symmetric encryption on the target, returning a base 64 encoded byte string.");
                dd.add("Note 1: target may be a string or stem  of them");
                dd.add("Note 2: in QDL it is easy to make a key with " + MathEvaluator.RANDOM_STRING + "(n), n*8 is the bit count.");
                dd.add("\nE.g.");
                dd.add("key := " + MathEvaluator.RANDOM_STRING + "(64); // 64*8 == 512 bit strength");
                dd.add("target := 'mairzy doats and dozey doats';");
                dd.add("    " + getName() + "(key, target)");
                dd.add("HUhqqoHJc3-AqWbRGbS-6V2KnXQ26tiR9ivkmA");
                dd.add("//Note that the encrypted output will vary since the key is random.");
                dd.add("    " + SYMM_DECRYPT_NAME + "(key, 'HUhqqoHJc3-AqWbRGbS-6V2KnXQ26tiR9ivkmA')");
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
            if (dd.isEmpty()) {
                dd.add(getName() + "(key, target) - Symmetric key decryption for an encrypted, base 64 byte-string.");
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

    /**
     * Is the stem a single key or a stem of keys? This is a simple-minded test and just
     * checks if a required value for the keyis at the top level.
     *
     * @param stem
     * @return
     */
    protected boolean isSingleKey(QDLStem stem) {
        return stem.containsKey(JWKUtil2.KEY_TYPE);
    }


    SecureRandom secureRandom = new SecureRandom();
}
