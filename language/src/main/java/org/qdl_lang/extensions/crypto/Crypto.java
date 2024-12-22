package org.qdl_lang.extensions.crypto;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import edu.uiuc.ncsa.security.util.crypto.CertUtil;
import edu.uiuc.ncsa.security.util.crypto.KeyUtil;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.util.crypto.DecryptUtils;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKey;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeyUtil;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeys;
import edu.uiuc.ncsa.security.util.jwk.JWKUtil2;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 8/16/22 at  1:34 PM
 */
public class Crypto implements QDLMetaModule {
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

    /*
    crypto := j_load('crypto');
      crypto#create_key({'type':'EC','alg':'ES256','curve':'P-256'})
crypto#create_key({'type':'AES','alg':'A256GCM','length':512})

     */
    public class CreateKey implements QDLFunction {
        @Override
        public String getName() {
            return CREATE_KEY_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            // default is RSA key. 1024 bits, RS256 alg.
            String type = "RSA";
            int keyLength = 1024;
            String alg = "RS256";
            String curve = "P-256";
            JSONWebKey webKey = null;

            if (objects.length == 0) {
                webKey = getJwkUtil().createRSAKey(keyLength, alg);
            }
            if (objects.length == 1) {
                if (objects[0] instanceof QDLStem) {
                    boolean unknownType = true;
                    QDLStem stem = (QDLStem) objects[0];
                    if (stem.containsKey("type")) {
                        type = stem.getString("type");
                    } else {
                        throw new IllegalArgumentException(getName() + " is missing the type of the key. Must be RSA or EC");
                    }
                    if (type.equals("RSA")) {
                        unknownType = false;
                        if (stem.containsKey("length")) {
                            keyLength = stem.getLong("length").intValue();
                        }
                        if (keyLength % 256 != 0) {
                            throw new IllegalArgumentException("the key size of " + keyLength + " must be a multiple of 256");
                        }
                        if (stem.containsKey("alg")) {
                            alg = stem.getString("alg");
                        }

                        webKey = getJwkUtil().createRSAKey(keyLength, alg);
                    }
                    if (type.equals("EC")) {
                        unknownType = false;
                        if (stem.containsKey("curve")) {
                            curve = stem.getString("curve");
                        }
                        if (stem.containsKey("alg")) {
                            alg = stem.getString("alg");
                        } else {
                            alg = "ES256"; // default for elliptic curves.
                        }
                        webKey = getJwkUtil().createECKey(curve, alg);
                    }
                    if (type.equals("AES")) {
                        // See https://www.rfc-editor.org/rfc/rfc7518.html#section-6.4
                        unknownType = false;
                        EncryptionMethod encryptionMethod = null;
                        if (stem.containsKey("alg")) {
                            alg = stem.getString("alg");
                            switch (alg) {
                                case "A128GCM":
                                    encryptionMethod = EncryptionMethod.A128GCM;
                                    break;
                                case "A192GCM":
                                    encryptionMethod = EncryptionMethod.A192GCM;
                                    break;
                                case "A256GCM":
                                    encryptionMethod = EncryptionMethod.A256GCM;
                                    break;
                                default:
                                    encryptionMethod = null;
                                    break;
                            }
                        }
                        int length = 256;
                        if (stem.containsKey("length")) {
                            length = Math.toIntExact(stem.getLong("length"));
                        }
                        OctetSequenceKey jwk;
                        if (encryptionMethod == null) {
                            jwk = new OctetSequenceKeyGenerator(length)
                                    .issueTime(new Date()) // issued-at timestamp (optional)
                                    .keyID(getRandomID())// give the key some ID (optional)
                                    .generate();

                        } else {
                            jwk = new OctetSequenceKeyGenerator(length)
                                    .issueTime(new Date()) // issued-at timestamp (optional)
                                    .keyID(getRandomID()) // give the key some ID (optional)
                                    .algorithm(encryptionMethod) // indicate the intended key alg (optional)
                                    .generate();
                        }
                        QDLStem out = new QDLStem();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.putAll(jwk.toJSONObject());
                        out.fromJSON(jsonObject);
                        return out;
                    }
                    if (unknownType) {
                        throw new IllegalArgumentException("unknown key type '" + stem.get("type") + "'");
                    }
                } else {
                    if (!(objects[0] instanceof Long)) {
                        throw new IllegalArgumentException("single argument must be the length of the RSA key");

                    }
                    keyLength = ((Long) objects[0]).intValue();
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
                    dd.add(getName() + "(key_size | params.) either an RSA or elliptic curve key.");
                    dd.add("key_size = bit count for an RSA RS256 key. ");
                    dd.add("Note that the key_size must be a multiple of 256.");
                    dd.add("If a stem of parameters is passed, it is of the form");
                    dd.add("  {'type' :'RSA'|'EC' | 'AES', 'alg':algorithm, 'length':rsa or aes key length, 'curve' : elliptic curve.}");
                    dd.add("E.g.");
                    dd.add("    " + getName() + "({'type':'EC':'curve':'P-256', 'alg':'ES256'})");
                    dd.add("would use the curve P-256 with the ES256 algorithm to create an elliptic curve key.");
                    dd.add("EC curves:P-256, P-256K, P-384, P-521, secp256k1");
                    dd.add("EC algortihms:ES256, ES256k, ES384, ES512");
                    dd.add("RSA algortihms: RS256, RS385, RS512");
                    dd.add("RSA key length is a multiple of 256.");
                    dd.add("AES algorithms: A128GCM, A192GCM, A256GCM");
                    dd.add("AES key length is creater then 112 and must be a multiple of 8.");
                    dd.add("\nE.g. to make an RSA key");
                    dd.add("    " + getName() + "({'length':4096, 'alg':'RS512', 'type':'RSA'})");
                    dd.add("{alg:RS512,...");
                    dd.add("\nE.g. An AES key");
                    dd.add(" crypto#create_key({'type':'AES','alg':'A256GCM','length':512})\n" +
                            "{alg:A256GCM, k:H4t50v....");
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

    public static final String IMPORT_PKCS_NAME = "import_pkcs";
    public static final String PKCS_1_TYPE = "pkcs_1";
    public static final String PKCS_8_TYPE = "pkcs_8";
    public static final String X509_TYPE = "x509";

    public class ImportPKCS implements QDLFunction {
        @Override
        public String getName() {
            return IMPORT_PKCS_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof String)) {
                throw new IllegalArgumentException("The first argument of " + getName() + " must be a string that is the path to the file");
            }
            String filePath = (String) objects[0];
            String type = PKCS_8_TYPE; //default
            String rawFile = QDLFileUtil.readTextFile(state, filePath);
            PrivateKey privateKey = null;
            PublicKey publicKey = null;
            if (objects.length != 1) {
                type = (String) objects[1];
                switch (type) {
                    case PKCS_1_TYPE:
                        privateKey = KeyUtil.fromPKCS1PEM(rawFile);
                        break;
                    case PKCS_8_TYPE:
                        privateKey = KeyUtil.fromPKCS8PEM(rawFile);
                        break;
                    case X509_TYPE:
                        publicKey = KeyUtil.fromX509PEM(rawFile);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown key type: " + type);
                }
            }

            JWK jwk = null;
            if (privateKey == null) {
                jwk = getJwk(publicKey);
            } else {
                RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
                RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
                System.out.println(getClass().getSimpleName() + ": priv key alg=" + privk.getAlgorithm());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(publicKeySpec);

                jwk = new RSAKey.Builder((RSAPublicKey) publicKey)
                        .privateKey((RSAPrivateKey) privateKey)
                        .keyID(getRandomID())
                        .issueTime(new Date())
                        .algorithm(JWSAlgorithm.RS256) // for use in signing, not from the key
                        .keyUse(new KeyUse("sig"))
                        .build();
            }
            JSONWebKey jsonWebKey = new JSONWebKey(jwk);
            QDLStem outStem = webKeyToStem(jsonWebKey);
            return outStem;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 1:
                    dd.add(getName() + "(file_path) - read a PKCS 1 (RSA private key) in PEM format and return as a stem");
                    dd.add("file_path -  path to the PEM encoded file");
                    break;
                case 2:
                    dd.add(getName() + "(file_path, type) - read a PEM format key, retuning a stem.");
                    dd.add("file_path -  path to the PEM encoded file");
                    dd.add("type -  supported types are ");
                    dd.add("        " + PKCS_1_TYPE + " - PKCS 1, PEM encoded RSA private key");
                    dd.add("        " + PKCS_8_TYPE + " - PKCS 8, PEM encoded unencrypted private key");
                    dd.add("        " + X509_TYPE + " - X509, PEM encoded public key");
                    break;
            }
            return dd;
        }
    }

    private JWK getJwk(PublicKey publicKey) {
        JWK jwk = null;
        if (publicKey instanceof RSAPublicKey) {
            jwk = new RSAKey.Builder((RSAPublicKey) publicKey)
                    .keyID(getRandomID())
                    .issueTime(new Date())
                    .algorithm(JWSAlgorithm.RS256) // for use in signing, not from the key
                    .keyUse(new KeyUse("sig"))
                    .build();
        }
/*        if(publicKey instanceof ECPublicKey){
            ECPublicKey ecPublicKey = (ECPublicKey)publicKey;
            EllipticCurve curve = ecPublicKey.getParams().getCurve();

             jwk = new ECKey.Builder(Curve.SECP256K1,
                     ecPublicKey)
                    .keyID(getRandomID())
                    .issueTime(new Date())
                    .algorithm(JWSAlgorithm.ES256)
                    .keyUse(new KeyUse("sig"))
                    .build();
            System.out.println(publicKey);
        }*/

        return jwk;
    }

    public static final String EXPORT_PKCS_NAME = "export_pkcs";

    public class ExportPKCS implements QDLFunction {
        @Override
        public String getName() {
            return EXPORT_PKCS_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException(getName() + " first argument must be a QDL stem that is they key");
            }
            QDLStem key = (QDLStem) objects[0];
            if (!(objects[1] instanceof String)) {
                throw new IllegalArgumentException(getName() + " - second argument must be a string that is the path to the file");
            }
            String path = (String) objects[1];
            String type = PKCS_8_TYPE;
            if (objects.length == 3) {
                if (!(objects[2] instanceof String)) {
                    throw new IllegalArgumentException(getName() + " third argument must be a string that is the path to the file");
                }
                type = (String) objects[2];
            }
            JSONWebKey jwk = JSONWebKeyUtil.getJsonWebKey(key.toJSON().toString());
            String content;
            switch (type) {
                case PKCS_1_TYPE:
                    content = KeyUtil.toPKCS1PEM(jwk.privateKey);
                    break;
                case PKCS_8_TYPE:
                    content = KeyUtil.toPKCS8PEM(jwk.privateKey);
                    break;
                case X509_TYPE:
                    content = KeyUtil.toX509PEM(jwk.publicKey);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown key type: " + type);
            }
            QDLFileUtil.writeTextFile(state, path, content);
            return Boolean.TRUE;
        }

        /*
          crypto := j_load('crypto');
            a. := crypto#import_pkcs('/home/ncsa/temp/public_key.pem', 'x509');
            b. := crypto#import_pkcs('/home/ncsa/temp/key.pem','pkcs_8');
            crypto#export_pkcs( b., '/tmp/pkcs8.pem', 'pkcs_8')
            crypto#export_pkcs( b., '/tmp/pkcs1.pem', 'pkcs_1')


         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + ("(key.,file_path) - write a single private key to a file in PKCS 8 format."));
                    dd.add("key. - the JWK representation of a single private key (all that PKCS supports)");
                    dd.add("file_path -  path to the resulting PEM encoded file");
                    dd.add("This function returns true if the operation worked, otherwise it throws an exception.");
                    break;
                case 3:
                    dd.add(getName() + ("(key., file_path, type) - write a single key to a file in PEM format."));
                    dd.add("key. - the JWK representation of a single key (all that PKCS supports)");
                    dd.add("file_path -  path to the resulting PEM encoded file");
                    dd.add("type - one of the following");
                    dd.add(PKCS_1_TYPE + " - PKCS 1, for a single private key");
                    dd.add(PKCS_8_TYPE + " - PKCS 8, for a single private key");
                    dd.add(X509_TYPE + " - X 509 format  for a single public key");
                    dd.add("This function returns true if the operation worked, otherwise it throws an exception.");
                    break;
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
            return new int[]{0, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (objects.length == 0) {
                // Query for supported ciphers
                QDLStem outStem = new QDLStem();
                ArrayList<String> ciphers = new ArrayList<>();
                ciphers.addAll(DecryptUtils.listCiphers());
                outStem.getQDLList().setArrayList(ciphers);
                return outStem;
            }

            if (!(objects[0] instanceof QDLStem)) {
                throw new IllegalArgumentException("The first argument of " + getName() + " must be a stem");
            }
            // arg 0 is either stem of the key or a cfg stem (which includes the key as 'key' entry)
            // arg 1 is either string or stem of strings to encrypt.
            QDLStem leftStem = (QDLStem) objects[0];
            if (leftStem.containsKey("kty") && leftStem.getString("kty").equals("oct")) {
                return sDeOrEnCrypt(objects, state, true, getName());
            }
            JSONWebKey jsonWebKey;
            String cipher = "RSA"; // There are several available.
            boolean usePrivateKey = true;
            if (leftStem.containsKey("key")) {
                jsonWebKey = getKeys(leftStem.getStem("key"));
                if (leftStem.containsKey("cipher")) {
                    cipher = leftStem.getString("cipher");
                }
                if (leftStem.containsKey("use_private")) {
                    leftStem.getBoolean("use_private");
                }
            } else {
                jsonWebKey = getKeys(leftStem);
                // just use defaults
            }
            if (usePrivateKey) {
                if (jsonWebKey.privateKey == null) {
                    throw new IllegalArgumentException("Missing private key");
                }
            } else {
                if (jsonWebKey.publicKey == null) {
                    throw new IllegalArgumentException("Missing public key");
                }
            }
            QDLStem rightArg = null;
            boolean stringArg = false;
            boolean gotOne = false;
            if (objects[1] instanceof QDLStem) {
                gotOne = true;
                rightArg = (QDLStem) objects[1];
            }
            if (objects[1] instanceof String) {
                gotOne = true;
                stringArg = true;
                rightArg = new QDLStem();
                rightArg.put(0L, objects[1]);
            }
            if (objects[1] instanceof QDLSet) {
                return encryptOrDecryptSet((QDLSet) objects[1], cipher, jsonWebKey, usePrivateKey, false);
            }

            if (!gotOne) {
                return objects[1]; // nix to do
            }

            QDLStem out = encryptOrDecryptStem(rightArg, cipher, jsonWebKey, usePrivateKey, false);
            if (stringArg) {
                return out.getString(0L);
            }
            return out;
        }

/*
   crypto:=j_load('crypto')
  rsa. := crypto#create_key()
  crypto#encrypt(rsa., 'woof woof')
  crypto#encrypt({'key':rsa.,'cipher':'DES'}, 'woof woof')

  set_test := crypto#encrypt(rsa.,{'a',{'b'}})
   crypto#decrypt(rsa., set_test)
 */

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + "(key., arg|arg.) - encrypt a string or stem of them. If the key is symmetric, do symmetric encryption, otherwise encrypt with the private key");
                    break;
                case 3:
                    dd.add(getName() + "(key., arg|arg., use_private) - encrypt a string or stem of them with the private key if use_private is true");
                    dd.add("   or use the public key if false. Default is true");
                    break;
            }
            dd.add("key. - the RSA or symmetric key to use. N.B. Elliptic keys are not supported at this time.");
            dd.add("arg|arg. - a string or a stem of strings");
            if (argCount == 3) {

                dd.add("use_private - use the private key (if true, this is the default) and the public key if false");
            }
            dd.add("NOTE: You can only encrypt a string with an RSA key that has fewer bits than the key.");
            dd.add("Symmetric encryption, however, is unlimited");
            dd.add("\nE.g.\nIf RSA your key is 1024 bits, then 1024/8 = 128 bytes or characters is the max length string.\n");
            dd.add("Note that the result is base 64 encoded, since the result of the encryption will be an array of bytes.");
            dd.add("One final reminder is that if encrypt/decrypt with one key and decrypt/encrypt with the" +
                    "\nother or you will get an error");
            dd.add("E.g.");
            dd.add("   " + getName() + "(key., 'marizy doats')");
            dd.add("(whole bunch of base 64 stuff that depends on the key)");
            dd.add("Since this was encrypted with the private key, you would need to specify using the");
            dd.add("public key in " + DECRYPT_NAME + " (which is, incidentally, the default there).");
            dd.add("\nE.g. Symmetric example");
            dd.add("Here, a symmetric key (AES) is created and used.");
            dd.add("    aes. := crypto#create_key({'type':'AES','alg':'A256GCM','length':512})\n" +
                    "    crypto#encrypt(aes., 'woof woof woof') \n" +
                    "67dmKZ6lqHwSt-mIZGs\n" +
                    "    crypto#decrypt(aes., '67dmKZ6lqHwSt-mIZGs')\n" +
                    "woof woof woof\n");
            return dd;
        }
    }

    /**
     * Encrypt or decrypt a stem. This will skip anything that is not a string or stem
     * and will do the correct recursion to get everything in the stem
     *
     * @param rightArg
     * @param cipher
     * @param jsonWebKey
     * @param usePrivateKey
     * @param doDecrypt
     * @return
     */
    protected QDLStem encryptOrDecryptStem(QDLStem rightArg,
                                           String cipher,
                                           JSONWebKey jsonWebKey,
                                           boolean usePrivateKey,
                                           boolean doDecrypt) {

        QDLStem outStem = new QDLStem();
        for (Object key : rightArg.keySet()) {
            Object obj = rightArg.get(key);
            String result;
            if (obj instanceof QDLStem) {
                outStem.putLongOrString(key, encryptOrDecryptStem((QDLStem) obj, cipher, jsonWebKey, usePrivateKey, doDecrypt));
            } else {
                if (obj instanceof String) {
                    String inString = (String) obj;

                    try {
                        if (usePrivateKey) {
                            if (doDecrypt) {
                                result = DecryptUtils.decryptPrivate(cipher, jsonWebKey.privateKey, inString);
                            } else {
                                result = DecryptUtils.encryptPrivate(cipher, jsonWebKey.privateKey, inString);
                            }
                        } else {
                            if (doDecrypt) {
                                result = DecryptUtils.decryptPublic(cipher, jsonWebKey.publicKey, inString);
                            } else {
                                result = DecryptUtils.encryptPublic(cipher, jsonWebKey.publicKey, inString);
                            }
                        }
                        outStem.putLongOrString(key, result);
                    } catch (RuntimeException rt) {
                        throw rt;
                    } catch (Throwable gsx) {
                        // Clean up exception with a better message
                        throw new IllegalArgumentException((doDecrypt ? DECRYPT_NAME : ENCRYPT_NAME) + " could not process argument for key='" + key + "' with value ='" + obj + "' (" + gsx.getMessage() + ")");
                    }
                } else {
                    if (obj instanceof QDLSet) {
                        outStem.putLongOrString(key, encryptOrDecryptSet((QDLSet) obj, cipher, jsonWebKey, usePrivateKey, doDecrypt));

                    } else {
                        outStem.putLongOrString(key, obj);
                    }
                }
            }
        }
        return outStem;
    }

    protected QDLSet encryptOrDecryptSet(QDLSet rightArg,
                                         String cipher,
                                         JSONWebKey jsonWebKey,
                                         boolean usePrivateKey,
                                         boolean doDecrypt) {

        QDLSet outSet = new QDLSet();
        for (Object obj : rightArg) {
            String result;
            if (obj instanceof QDLStem) {
                outSet.add(encryptOrDecryptStem((QDLStem) obj, cipher, jsonWebKey, usePrivateKey, doDecrypt));
            } else {
                if (obj instanceof String) {
                    String inString = (String) obj;

                    try {
                        if (usePrivateKey) {
                            if (doDecrypt) {
                                result = DecryptUtils.decryptPrivate(cipher, jsonWebKey.privateKey, inString);
                            } else {
                                result = DecryptUtils.encryptPrivate(cipher, jsonWebKey.privateKey, inString);
                            }
                        } else {
                            if (doDecrypt) {
                                result = DecryptUtils.decryptPublic(cipher, jsonWebKey.publicKey, inString);
                            } else {
                                result = DecryptUtils.encryptPublic(cipher, jsonWebKey.publicKey, inString);
                            }
                        }
                        outSet.add(result);
                    } catch (RuntimeException rt) {
                        throw rt;
                    } catch (Throwable gsx) {
                        // Clean up exception with a better message
                        throw new IllegalArgumentException((doDecrypt ? DECRYPT_NAME : ENCRYPT_NAME) + " could not process argument for set element ='" + obj + "' (" + gsx.getMessage() + ")");
                    }
                } else {
                    if (obj instanceof QDLSet) {
                        outSet.add(encryptOrDecryptSet((QDLSet) obj, cipher, jsonWebKey, usePrivateKey, doDecrypt));
                    } else {
                        outSet.add(obj);
                    }
                }
            }
        }
        return outSet;
    }

    /*
         crypto := j_load('crypto');
         aes. := crypto#create_key({'type':'AES','alg':'A256GCM','length':512})
         crypto#encrypt(aes., 'woof woof woof')
         67dmKZ6lqHwSt-mIZGs
         crypto#decrypt(aes., '67dmKZ6lqHwSt-mIZGs')
         woof woof woof

        // set test
          crypto:=j_load('crypto')
          rsa. := crypto#create_key()
          set_test := crypto#encrypt(rsa., {'a',{'b'}})
          crypto#decrypt(rsa., set_test) == {'a',{'b'}}; //returns true


         crypto := j_load('crypto');
        rsa. := crypto#create_key();
        z. := crypto#encrypt(rsa., {'a':{'b':'foo'}})
    crypto#decrypt(rsa., z.)

     */
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
            QDLStem leftArg = (QDLStem) objects[0];
            if (leftArg.containsKey("kty") && leftArg.getString("kty").equals("oct")) {
                return sDeOrEnCrypt(objects, state, false, getName());
            }
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

            if (objects.length == 3) {
                if (!(objects[2] instanceof Boolean)) {
                    throw new IllegalArgumentException("the last argument of " + getName() + " must be a boolean. Default is true");
                }
                usePrivateKey = (Boolean) objects[2];
            }
            JSONWebKey jsonWebKey = getKeys(leftArg);
            if (usePrivateKey) {
                if (jsonWebKey.privateKey == null) {
                    throw new IllegalArgumentException("Missing private key");
                }
            } else {
                if (jsonWebKey.publicKey == null) {
                    throw new IllegalArgumentException("Missing public key");
                }
            }
            String cipher = "RSA"; // There are several available.
            if (objects[1] instanceof QDLSet) {
                return encryptOrDecryptSet((QDLSet) objects[1], cipher, jsonWebKey, usePrivateKey, true);
            }

            if (!gotOne) {
                return objects[1]; // nix to do
            }
            QDLStem out = encryptOrDecryptStem(arg, cipher, jsonWebKey, usePrivateKey, true);
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
                    doxx.add(getName() + "(key., arg|arg.) - decrypt the argument. If the key is symmetric, use that, otherwise use the public key");
                    break;
                case 3:
                    doxx.add(getName() + "(key., arg|arg., use_private) - decrypt the argument using the private key if use_private == true");
            }
            if (doxx.isEmpty()) {
                doxx.add("key. - the RSA key you want to use. Only RSA keys are supported at this time.");
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

    /*
     crypto := j_load('crypto');
         aes. := crypto#create_key({'type':'AES','alg':'A256GCM','length':512})
         crypto#encrypt(aes., 'woof woof woof')
kazrnybI9mX73qv6NqA
  crypto#encrypt(aes., {'a':'woof woof woof'})
{a:kazrnybI9mX73qv6NqA}
  crypto#encrypt(aes., {'b',{'a':'woof woof woof'}})
{b,{a:woof woof woof}}
  crypto#encrypt(aes., {'woof woof woof'})
{woof woof woof}
    crypto#encrypt(aes., {'b',{'a':'woof woof woof'}})
     */
    public Object sDeOrEnCrypt(Object[] objects, State state, boolean isEncrypt, String name) {
        byte[] key = null;
        if (objects[0] instanceof QDLStem) {
            // check that it is a JWK of type octet
            QDLStem sKey = (QDLStem) objects[0];
            if (sKey.containsKey("kty")) {
                if (!sKey.getString("kty").equals("oct")) {
                    throw new IllegalArgumentException("Incorrect key type. Must be of type 'oct' (octet-encoded)");
                }
                if (sKey.containsKey("k")) {
                    key = Base64.decodeBase64(sKey.getString("k"));
                } else {
                    throw new IllegalArgumentException("Incorrect key format: missing 'k' entry for bytes");
                }
            }
        } else {
            if (!(objects[0] instanceof String)) {
                throw new IllegalArgumentException("the first argument to " + name + " must be a base64 encoded key");
            }
            key = Base64.decodeBase64((String) objects[0]);
        }
        QDLStem inStem = null;
        boolean isStringArg = false;
        if (objects[1] instanceof String) {
            isStringArg = true;
            inStem = new QDLStem();
            inStem.put(0L, (String) objects[1]);
        }
        if (objects[1] instanceof QDLStem) {
            inStem = (QDLStem) objects[1];
        }

        if (objects[1] instanceof QDLSet) {
            return sDeOrEncryptSet((QDLSet) objects[1], isEncrypt, key);
        }
        if (inStem == null) {
            return objects[1]; // nix to do
        }
        QDLStem outStem = sDeOrEncryptStem(inStem, isEncrypt, key);
        if (isStringArg) {
            return outStem.get(0L);
        }
        return outStem;
    }

    private QDLStem sDeOrEncryptStem(QDLStem inStem, boolean isEncrypt, byte[] key) {
        QDLStem outStem = new QDLStem();
        for (Object stemKey : inStem.keySet()) {
            Object obj = inStem.get(stemKey);
            if (obj instanceof String) {
                String target = (String) obj;
                outStem.putLongOrString(stemKey, decodeString(key, target, isEncrypt));
            } else {
                if (obj instanceof QDLStem) {
                    outStem.putLongOrString(stemKey, sDeOrEncryptStem((QDLStem) obj, isEncrypt, key));// don't touch if not string
                } else {
                    if (obj instanceof QDLSet) {
                        outStem.putLongOrString(stemKey, sDeOrEncryptSet((QDLSet) obj, isEncrypt, key));// don't touch if not string
                    } else {
                        outStem.putLongOrString(stemKey, obj);// don't touch if not string
                    }
                }
            }
        }
        return outStem;
    }

    private QDLSet sDeOrEncryptSet(QDLSet inSet, boolean isEncrypt, byte[] key) {
        QDLSet outSet = new QDLSet();
        for (Object obj : inSet) {
            if (obj instanceof String) {
                String target = (String) obj;
                outSet.add(decodeString(key, target, isEncrypt));
            } else {
                if (obj instanceof QDLSet) {
                    outSet.add(sDeOrEncryptSet((QDLSet) obj, isEncrypt, key));// don't touch if not string
                } else {
                    if (obj instanceof QDLStem) {
                        outSet.add(sDeOrEncryptStem((QDLStem) obj, isEncrypt, key));// don't touch if not string
                    } else {
                        outSet.add(obj); // do nothing
                    }
                }
            }
        }
        return outSet;
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

    protected String getRandomID() {
        return getRandomID(8);
    }

    /**
     * Creates a random id as an (upper case) hex number.
     * @param byteCount
     * @return
     */
    protected String getRandomID(int byteCount) {
        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);
        BigInteger bigInt = new BigInteger(bytes);
        bigInt = bigInt.abs();
        return bigInt.toString(16).toUpperCase();
    }

    @Override
    public JSONObject serializeToJSON() {
        return null;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {

    }

    public static String IMPORT_CERT = "import_x509";

    public class ImportCert implements QDLFunction {
        @Override
        public String getName() {
            return IMPORT_CERT;
        }

        /*
              crypto := j_load('crypto');
          cert. := crypto#import_x509('/home/ncsa/temp/cert.pem');
          print(cert.);

         */
        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            String path = (String) objects[0];
            QDLStem out = new QDLStem();
            String rawCert = QDLFileUtil.readTextFile(state, path);
            X509Certificate[] certs = CertUtil.fromX509PEM(rawCert);
            if (certs.length == 1) {
                QDLStem outStem = certToStem(certs[0]);
                outStem.put("encoded", rawCert);
                return outStem;
            }
            for (X509Certificate cert : certs) {
                out.getQDLList().add(certToStem(cert));
            }
            out.getStem(0L).put("encoded", rawCert);
            return out;
        }

        public String hexToASCII(String hex) {
            // initialize the ASCII code string as empty.
            String ascii = "";

            for (int i = 0; i < hex.length(); i += 2) {

                // extract two characters from hex string
                String part = hex.substring(i, i + 2);

                // change it into base 16 and typecast as the character
                char ch = (char) Integer.parseInt(part, 16);

                // add this char to final ASCII string
                ascii = ascii + ch;
            }

            return ascii;
        }

        /*
         crypto := j_load('crypto')
         print(cert.:=crypto#import_x509('/home/ncsa/Downloads/github-com.pem'))
         crypto#read_oid(cert., {'foo':'2.5.29.19'})
  decode(crypto#read_oid(cert., '1.3.6.1.4.1.5923.1.1.1.6'))

        */
        protected QDLStem certToStem(X509Certificate x509Certificate) throws CertificateEncodingException {
            QDLStem out = new QDLStem();
            QDLStem subject = new QDLStem();
            subject.put("x500", x509Certificate.getSubjectX500Principal().getName());
            subject.put("dn", x509Certificate.getSubjectDN().getName());
            try {
                if (x509Certificate.getSubjectAlternativeNames() != null) {
                    QDLStem altNames = processAltNames(x509Certificate.getSubjectAlternativeNames());
                    if (altNames != null) {
                        subject.put("alt_names", altNames);
                    }
                }
            } catch (CertificateParsingException e) {
                // throw new RuntimeException(e);
            }
            QDLStem criticalOIDS = new QDLStem();
            for (String x : x509Certificate.getCriticalExtensionOIDs()) {
                criticalOIDS.getQDLList().add(x);
            }
            QDLStem noncriticalOIDS = new QDLStem();
            for (String x : x509Certificate.getNonCriticalExtensionOIDs()) {
                noncriticalOIDS.getQDLList().add(x);
            }
            QDLStem oids = new QDLStem();
            oids.put("critical", criticalOIDS);
            oids.put("noncritical", noncriticalOIDS);
            out.put("oids", oids);
            out.put("subject", subject);
            QDLStem issuer = new QDLStem();
            issuer.put("x500", x509Certificate.getIssuerX500Principal().getName());
            issuer.put("dn", x509Certificate.getIssuerDN().getName());
            if (x509Certificate.getIssuerUniqueID() != null) {

                QDLStem issuerUniqueID = new QDLStem();
                ArrayList list = new ArrayList();
                list.add(x509Certificate.getIssuerUniqueID());
                issuerUniqueID.getQDLList().setArrayList(list);
                issuer.put("unique_id", issuerUniqueID);
            }
            try {
                if (x509Certificate.getSubjectAlternativeNames() != null) {
                    QDLStem altNames = processAltNames(x509Certificate.getSubjectAlternativeNames());
                    if (altNames != null) {
                        issuer.put("alt_names", altNames);
                    }
                }
            } catch (CertificateParsingException e) {
                // throw new RuntimeException(e);
            }
            out.put("issuer", issuer);
            out.put("not_before", x509Certificate.getNotBefore().getTime());
            out.put("not_after", x509Certificate.getNotAfter().getTime());
            QDLStem alg = new QDLStem();
            alg.put("name", x509Certificate.getSigAlgName());
            alg.put("oid", x509Certificate.getSigAlgOID());
            if (x509Certificate.getSigAlgParams() != null) {
                alg.put("parameters", Base64.encodeBase64URLSafe(x509Certificate.getSigAlgParams()));
            }
            out.put("algorithm", alg);
            out.put("serial_number", x509Certificate.getSerialNumber().toString());

            out.put("signature", Base64.encodeBase64URLSafeString(x509Certificate.getSignature()));
            out.put("version", "v" + x509Certificate.getVersion()); // standard way to write it
            String eppn = CertUtil.getEPPN(x509Certificate);
            if (eppn != null) {
                out.put("eppn", eppn);
            }
            String email = CertUtil.getEmail(x509Certificate);
            if (email != null) {
                out.put("email", email);
            }
            PublicKey publicKey = x509Certificate.getPublicKey();
            JWK jwk = getJwk(publicKey);
            if (jwk != null) {
                JSONWebKey jsonWebKey = new JSONWebKey(jwk);
                QDLStem pKeyStem = webKeyToStem(jsonWebKey);
                out.put("public_key", pKeyStem);
            }
            return out;
        }

        /**
         * Take a collection of lists of alt names and convert to something understandable.
         *
         * @param lists
         * @return
         */
        protected QDLStem processAltNames(Collection<List<?>> lists) {
            if (lists == null || lists.size() == 0) {
                return null;
            }
            QDLStem altNames = new QDLStem();
            for (List o : lists) {
                String[] altName = altName(o);
                altNames.put(altName[0], altName[1]);
            }
            return altNames;
        }

        protected String[] altName(List rawList) {
            int oid = (int) rawList.get(0);
            String[] altNames = new String[2];
            switch (oid) {
                case 0:
                    altNames[0] = "otherName";
                    break;
                case 1:
                    altNames[0] = "rfc822Name";
                    break;
                case 2:
                    altNames[0] = "dNSName";
                    break;
                case 3:
                    altNames[0] = "x400Address";
                    break;
                case 4:
                    altNames[0] = "directoryName";
                    break;
                case 5:
                    altNames[0] = "ediPartyName";
                    break;
                case 6:
                    altNames[0] = "uniformResourceIdentifier";
                    break;
                case 7:
                    altNames[0] = "registeredID";
                    break;
            }
            altNames[1] = processAltName(rawList.get(1));
            return altNames;
        }

        protected String processAltName(Object o) {
            if (o instanceof byte[]) {
                Base64.encodeBase64URLSafeString((byte[]) o);
            }
            return o.toString();
        }

        /*
         GeneralName ::= CHOICE {
      otherName                       [0]     OtherName,
      rfc822Name                      [1]     IA5String,
      dNSName                         [2]     IA5String,
      x400Address                     [3]     ORAddress,
      directoryName                   [4]     Name,
      ediPartyName                    [5]     EDIPartyName,
      uniformResourceIdentifier       [6]     IA5String,
      iPAddress                       [7]     OCTET STRING,
      registeredID                    [8]     OBJECT IDENTIFIER}

         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(full_path)  - read a cert or chain of certs");
            dd.add("full_path = full path to the file.");
            dd.add("This will read an X 509 cert and return a stem of its attributes");
            dd.add("Note that you cannot change a cert nor write one!");
            dd.add("This is not intended for certificate management, but just to let you view one easily");
            dd.add("At this point, only RSA public keys will be returned.");
            return dd;
        }
    }

    public static final String READ_OID = "read_oid";

    public class ReadOID implements QDLFunction {
        @Override
        public String getName() {
            return READ_OID;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            QDLStem certStem = (QDLStem) objects[0];
            if (!certStem.containsKey("encoded")) {
                throw new IllegalStateException("certs must contain encoded key");
            }
            String cert = certStem.getString("encoded");
            X509Certificate[] certs = CertUtil.fromX509PEM(cert);
            // better be one?
            boolean isScalar = false;
            String oid = null;
            QDLStem oidStem = null;
            if (objects[1] instanceof String) {
                oid = (String) objects[1];
                isScalar = true;
            } else {
                if (objects[1] instanceof QDLStem) {
                    oidStem = (QDLStem) objects[1];

                } else {
                    throw new IllegalArgumentException(getName() + "requires a string or stem as the second argument");
                }
            }
            X509Certificate x509Certificate = certs[0];
            QDLStem outStem = new QDLStem();
            if (isScalar) {
                byte[] bb = x509Certificate.getExtensionValue(oid);
                if (bb == null) {
                    return QDLNull.getInstance();
                }
                return Base64.encodeBase64URLSafeString(bb);
            }
            for (Object key : oidStem.keySet()) {
                String oidKey;
                byte[] bb;
                if (key instanceof Long) {
                    oidKey = oidStem.getString((Long) key);
                } else {
                    oidKey = oidStem.getString((String) key);
                }
                bb = x509Certificate.getExtensionValue(oidKey);
                if (bb == null) {
                    outStem.putLongOrString(key, QDLNull.getInstance());
                } else {
                    outStem.putLongOrString(key, Base64.encodeBase64URLSafeString(bb));
                }
            }
            return outStem;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(cert., oid | oids.) - get the given OIDs from a given cert.");
            dd.add("cert. - a single X509 certificate to be probed.");
            dd.add("  oid - a string that is the oid (object id), e.g. '2.5.29.15'");
            dd.add("oids. - a stem of oids whose keys are the return values. Only string-valued OIDs are supported.");
            dd.add("You pass in either an oid or stem of them");
            dd.add("This returns the base 64 encoded octet stream. Best we can do in general...");
            dd.add("If there is no such value, a null is returned.");
            dd.add("An OID (object identifier) is a bit of X 509 voodoo that allows for");
            dd.add("addressing attributes. These are very specific and not standardizes, hence are. ");
            dd.add("bona fide low-level operations, but often the only way to get certain custom values");
            dd.add("E.g. to get the EPPN (if present) from a cert");
            dd.add("   " + getName() + "(cert., {'eppn':'1.3.6.1.4.1.5923.1.1.1.6'}");
            dd.add("{eppn:bob@bigstate.edu}");
            dd.add("In this case the OID refers to the EPPN (EduPersonPrincipalName");
            dd.add("\n\nA typical document list these is at https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html");
            dd.add("\nE.g.");
            dd.add("   crypto#read_oid(cert., cert.oids.critical)");
            dd.add("[BAQDAgeA,BAIwAA]");
            dd.add("A list of the critical OIDs for this cert. Decoding them, really yields nothing:");
            dd.add("[\u0004\u0004\u0003\u0002\u0007�,\u0004\u00020]");
            dd.add("Looking them up, these are (2.5.29.15) the id-ce-keyUsage, and (2.5.29.19) the id-ce-basicConstraints ");
            dd.add("and interpreting them requires consuming the individual bits.");
            dd.add("\n\nNotes:");
            dd.add("* if there is no such value, then it is omitted from the result");
            dd.add("* this is an expensive operation, so it is best to have all the OIDs you need");
            dd.add("  done at once, rather than getting each value in a loop");
            return dd;
        }
    }

    public static final String CREATE_CERT_REQUEST = "create_cert_request";

    public class CreateCertRequest implements QDLFunction {
        @Override
        public String getName() {
            return CREATE_CERT_REQUEST;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {

            //   MyCertUtil.createCertRequest(MyCertUtil.createCertRequest(null));
            return null;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(params.) - create a cert request with the given parameters");
            dd.add("params. - a stem of parameters with the following keys");
            dd.add("----------------------------------");
            dd.add("    key = RSA key that is a JWK");
            dd.add("     dn = the distinguished name. Default is " + CertUtil.DEFAULT_PKCS10_DISTINGUISHED_NAME);
            dd.add("     cn = the country. Default is USA");
            dd.add("     ou = organizational unit. Default is OU.");
            dd.add("sig_alg = signature algorithm. Default is " + CertUtil.DEFAULT_PKCS10_SIGNATURE_ALGORITHM);
            dd.add("     on = organizational name. Default is OU");
            return dd;
        }
    }
}
