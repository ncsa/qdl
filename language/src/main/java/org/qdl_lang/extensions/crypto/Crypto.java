package org.qdl_lang.extensions.crypto;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import edu.uiuc.ncsa.security.core.exceptions.UnsupportedProtocolException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import edu.uiuc.ncsa.security.util.crypto.CertUtil;
import edu.uiuc.ncsa.security.util.crypto.KeyUtil;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.extensions.QDLVariable;
import org.qdl_lang.state.State;
import org.qdl_lang.util.aggregate.IdentityScalarImpl;
import org.qdl_lang.util.aggregate.QDLAggregateUtil;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import edu.uiuc.ncsa.security.util.crypto.DecryptUtils;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKey;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeyUtil;
import edu.uiuc.ncsa.security.util.jwk.JSONWebKeys;
import edu.uiuc.ncsa.security.util.jwk.JWKUtil2;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.qdl_lang.variables.StemUtility;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            // default is RSA key. 1024 bits, RS256 alg.
            String type = "RSA";
            int keyLength = 1024;
            String alg = "RS256";
            String curve = "P-256";
            JSONWebKey webKey = null;

            if (qdlValues.length == 0) {
                webKey = getJwkUtil().createRSAKey(keyLength, alg);
            }
            if (qdlValues.length == 1) {
                if (qdlValues[0].isStem()) {
                    boolean unknownType = true;
                    QDLStem stem = qdlValues[0].asStem();
                    if (stem.containsKey("type")) {
                        type = stem.getString("type");
                    } else {
                        throw new BadArgException(getName() + " is missing the type of the key. Must be RSA or EC", 0);
                    }
                    if (type.equals(RSA_TYPE)) {
                        unknownType = false;
                        if (stem.containsKey("length")) {
                            keyLength = stem.getLong("length").intValue();
                        }
                        if (keyLength % 256 != 0) {
                            throw new BadArgException("the key size of " + keyLength + " must be a multiple of 256", 0);
                        }
                        if (stem.containsKey("alg")) {
                            alg = stem.getString("alg");
                        }

                        webKey = getJwkUtil().createRSAKey(keyLength, alg);
                    }
                    if (type.equals(EC_TYPE)) {
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
                    if (type.equals(AES_TYPE)) {
                        // See https://www.rfc-editor.org/rfc/rfc7518.html#section-6.4
                        alg = "AES";
                        int length = 256;
                        if (stem.containsKey("length")) {
                            length = Math.toIntExact(stem.getLong("length"));
                        }

                        if (stem.containsKey("alg")) {
                            alg = stem.getString("alg");
                    /*
                            c := j_load('crypto');
                            c#create_key({'type':'aes', 'length':1024, 'alg':'AES_128/ECB/NoPadding'})
                            */
                        }
                        SecretKey key = getSecureRandomKey(alg, length);
                        JWK jwk = new OctetSequenceKey.Builder(key)
                                .keyID(getRandomID())
                                .issueTime(new Date())
                                .build();

System.out.println("JOSE:"+ jwk.toJSONString());
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("key","oct");
                        jsonObject.put("iat",new Date().getTime()/1000);
                        jsonObject.put("alg" , alg);
                        jsonObject.put("kid" , getRandomID());
                        jsonObject.put("k", Base64.encodeBase64URLSafeString(key.getEncoded()));

                        QDLStem out = new QDLStem();
                        out.fromJSON(jsonObject);
                        return asQDLValue(out);
                    }
                    if (unknownType) {
                        throw new BadArgException("unknown key type '" + stem.get("type") + "'", 0);
                    }
                } else {

                    if (!(qdlValues[0].isLong())) {
                        throw new BadArgException("single integer argument must be the integer length of the RSA key", 0);
                    }
                    keyLength = qdlValues[0].asLong().intValue();
                    if (keyLength % 256 != 0) {
                        throw new BadArgException("the key size of " + keyLength + " must be a multiple of 256", 0);
                    }
                    webKey = getJwkUtil().createRSAKey(keyLength, alg);
                }
                // RSA key, gives size
            }
            JSONObject wk2 = JSONWebKeyUtil.toJSON(webKey);
            QDLStem stem = new QDLStem();
            stem.fromJSON(wk2);
            return asQDLValue(stem);

        }
        protected SecretKey getSecureRandomKey(String cipher, int keySize) {
            byte[] secureRandomKeyBytes = new byte[keySize / 8];
            secureRandom = new SecureRandom();
            secureRandom.nextBytes(secureRandomKeyBytes);
            return new SecretKeySpec(secureRandomKeyBytes, cipher);
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
                    dd.add(getName() + "(key_size | params.) either an RSA, EC (elliptic curve) or AES (symmetric) key.");
                    dd.add("key_size = bit count for an RSA RS256 key. ");
                    dd.add("Note that the key_size must be a multiple of 256.");
                    dd.add("If a stem of parameters is passed, it is of the form");
                    dd.add("  {'type' :'" + RSA_TYPE +"'|'" + EC_TYPE+ "' | '" + AES_TYPE + "', 'alg':algorithm, 'length':rsa or aes key length, 'curve' : elliptic curve.}");
                    dd.add("E.g.");
                    dd.add("    " + getName() + "({'type':'"+ EC_TYPE + "':'curve':'P-256', 'alg':'ES256'})");
                    dd.add("would use the curve P-256 with the ES256 algorithm to create an elliptic curve key.");
                    dd.add("EC curves:P-256, P-256K, P-384, P-521, secp256k1");
                    dd.add("EC algortihms:ES256, ES256k, ES384, ES512");
                    dd.add("RSA algortihms: RS256, RS385, RS512");
                    dd.add("RSA key length is a multiple of 256.");
                    dd.add("A complete list of supported AES algorithms (aka ciphers) can be gotten by calling " + ENCRYPT_NAME + "()");
                    dd.add("AES basic algorithms: A128GCM, A192GCM, A256GCM, default is");
                    dd.add("AES key length is creater then 112 and must be a multiple of 8.");
                    dd.add("\nE.g. to make an RSA key");
                    dd.add("    " + getName() + "({'length':4096, 'alg':'RS512', 'type':'" + RSA_TYPE + "'})");
                    dd.add("{alg:RS512,...");
                    dd.add("\nE.g. An AES key");
                    dd.add(" crypto#create_key({'type':'" + AES_TYPE + "','alg':'A256GCM','length':512})\n" +
                            "{alg:A256GCM, k:H4t50v....");
                    break;
            }
            dd.add("\nOne hears of 'key pairs', for RSA and EC keys, though in point of fact, the public bits of a key");
            dd.add("are always part of it, hence we do not explicitly create a public key, just a key");
            dd.add("from which you may extract a public key with  " + GET_PUBLIC_KEY_NAME);
            dd.add("Note that for RSA keys, the algorithm (for consumers of the key) defaults to RS256.");
            return dd;
        }
    }

    public static final String IMPORT_NAME = "import";
        /*
          crypto := j_load('crypto');
            a. := crypto#import('/home/ncsa/temp/public_key.pem', 'x509');
            b. := crypto#import('/home/ncsa/temp/key.pem','pkcs_8');
            crypto#export( b., '/tmp/pkcs8.pem', 'pkcs_8');
            crypto#export( b., '/tmp/pkcs1.pem', 'pkcs_1');
         */

    public class ImportKey implements QDLFunction {
        @Override
        public String getName() {
            return IMPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 1) {
                return asQDLValue(importJWKS(qdlValues, state));
            }
            if (!(qdlValues[1].isString())) {
                throw new BadArgException(getName() + " the second argument must be a string", 1);
            }
            String arg2 = qdlValues[1].asString();
            if (arg2.equals(JWKS_TYPE)) {
                return asQDLValue(importJWKS(qdlValues, state));
            }
            return asQDLValue(importPKCS(qdlValues, state));
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 1:
                    dd.add(getName() + "(file_path) - load a key in JWKS format");
                    dd.add("file_path - the path to the key file");
                    break;
                case 2:
                    dd.add(getName() + "(file_path, type) - load a key of a given type");
                    dd.add("file_path - the path to the key file");
                    dd.add("type - the type of the file. ");
                    addTypeHelp(dd);
                    break;
            }
            dd.add("This returns a stem that is the key or key set.");

            return dd;
        }
    }

    protected void addTypeHelp(List<String> dd) {
        dd.add("Supported values are");
        dd.add("        " + JWKS_TYPE + " - (default) JWKS key or set of keys");
        dd.add("        " + PKCS_1_TYPE + " - PKCS 1, PEM encoded RSA private key");
        dd.add("        " + PKCS_8_TYPE + " - PKCS 8, PEM encoded unencrypted private key");
        dd.add("        " + PKCS_8_PUBLIC_TYPE + " - PKCS 8 public, PEM encoded public key");
        dd.add("        " + X509_TYPE + " - \"X509\", PEM encoded public key. This is really PKCS 8 public, but used in X 509 certificates.");
    }

    public Object importPKCS(QDLValue[] qdlValues, State state) throws Throwable {
        if (!(qdlValues[0].isString())) {
            throw new BadArgException("The first argument of " + IMPORT_NAME + " must be a string that is the path to the file", 0);
        }
        String filePath = qdlValues[0].asString();
        String type = PKCS_8_TYPE; //default
        String rawFile = QDLFileUtil.readTextFile(state, filePath);
        PrivateKey privateKey = null;
        PublicKey publicKey = null;
        if (qdlValues.length != 1) {
            type = qdlValues[1].asString();
            switch (type) {
                case PKCS_1_TYPE:
                    privateKey = KeyUtil.fromPKCS1PEM(rawFile);
                    break;
                case PKCS_8_TYPE:
                    privateKey = KeyUtil.fromPKCS8PEM(rawFile);
                    break;
                case X509_TYPE:
                case PKCS_8_PUBLIC_TYPE:
                    publicKey = KeyUtil.fromX509PEM(rawFile);
                    break;
                default:
                    throw new BadArgException("Unknown key type: " + type, 1);
            }
        }

        JWK jwk = null;
        if (privateKey == null) {
            jwk = getJwk(publicKey);
        } else {
            RSAPrivateCrtKey privk = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
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

    public Object importJWKS(QDLValue[] qdlValues, State state) throws Throwable {
        if (!(qdlValues[0].isString())) {
            throw new BadArgException(IMPORT_NAME + " requires a file name as its first argument", 0);
        }
        String out = QDLFileUtil.readTextFile(state, qdlValues[0].asString());
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
            keys.put(from(key), webKeyToStem(jsonWebKey));
        }

        return keys;
    }

    public static final String EXPORT_NAME = "export";

    public class ExportKeys implements QDLFunction {
        @Override
        public String getName() {
            return EXPORT_NAME;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 2) {
                return asQDLValue(exportJWKS(qdlValues, state));
            }
            if (!(qdlValues[2].isString())) {
                throw new BadArgException("The third argument of " + EXPORT_NAME + " must be a string", 2);
            }
            String type = qdlValues[2].asString();
            if (type.equals(JWKS_TYPE)) {
                return asQDLValue(exportJWKS(qdlValues, state));
            }
            return asQDLValue(exportPKCS(qdlValues, state));
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + "(key., file_path) - export, i.e. save a key in JWKS format");
                    dd.add("key. - the stem containing a key or set of keys");
                    dd.add("file_path - the path to the key file");
                    break;
                case 3:
                    dd.add(getName() + "(key., file_path, type) - export, i.e. save a key of a given type");
                    dd.add("key. - the stem containing a key or set of keys");
                    dd.add("file_path - the path to the key file");
                    dd.add("type - the type of the file. ");
                    dd.add("Note that saving a file in PKCS 1 format is not supported. Use PKCS 8.");
                    addTypeHelp(dd);
                    break;
            }
            dd.add("Returns true if it worked, throws an exception if it did not.");

            return dd;
        }
    }

    /**
     * Does the actual work of exporting a JWKS set.
     *
     * @param qdlValues
     * @param state
     * @return
     * @throws Throwable
     */
    protected Object exportJWKS(QDLValue[] qdlValues, State state) throws Throwable {
        if (!qdlValues[0].isStem()) {
            throw new BadArgException("The first argument of " + EXPORT_NAME + " must be a stem", 0);
        }
        if (!qdlValues[1].isString()) {
            throw new BadArgException("The second argument of " + EXPORT_NAME + " must be a string", 1);
        }
        QDLStem inStem =  qdlValues[0].asStem();
        String filePath =  qdlValues[1].asString();
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        if (isSingleKey(inStem)) {
            // single key
            array.add(inStem.toJSON());

        } else {
            for (QDLKey k : inStem.keySet()) {
                QDLStem currentStem = inStem.get(k).asStem();
                // have to get the only entry
                array.add(currentStem.toJSON());
            }
        }
        jsonObject.put(JWKUtil2.KEYS, array);
        QDLFileUtil.writeTextFile(state, filePath, jsonObject.toString(2));
        return Boolean.TRUE;
    }

    /**
     * Does the actual work or exporting various PKCS files.
     *
     * @param qdlValues
     * @param state
     * @return
     * @throws Throwable
     */
    protected Object exportPKCS(QDLValue[] qdlValues, State state) throws Throwable {
        if (!qdlValues[0].isStem()) {
            throw new BadArgException(EXPORT_NAME + " first argument must be a QDL stem that is they key", 0);
        }
        QDLStem key = qdlValues[0].asStem();
        if (!qdlValues[1].isString()) {
            throw new BadArgException(EXPORT_NAME + " - second argument must be a string that is the path to the file", 1);
        }
        String path = qdlValues[1].asString();
        String type = PKCS_8_TYPE;
        if (qdlValues.length == 3) {
            if (!(qdlValues[2].isString())) {
                throw new BadArgException(EXPORT_NAME + " third argument must be a string that is the type of key", 2);
            }
            type = qdlValues[2].asString();
            if(type.equals(PKCS_1_TYPE)) {
                throw new BadArgException(EXPORT_NAME + " - does not support writing PKCS 1 files. Use PKCS 8", 1);
            }
        }
        JSONWebKey jwk = JSONWebKeyUtil.getJsonWebKey(key.toJSON().toString());
        String content;
        switch (type) {
            case PKCS_1_TYPE:
                throw new BadArgException(EXPORT_NAME + " - does not support writing PKCS 1 files. Use PKCS 8", 1);
            case PKCS_8_TYPE:
                content = KeyUtil.toPKCS8PEM(jwk.privateKey);
                break;
            case X509_TYPE:
            case PKCS_8_PUBLIC_TYPE:
                content = KeyUtil.toX509PEM(jwk.publicKey);
                break;
            default:
                throw new BadArgException("Unknown key type: " + type, 0);
        }
        QDLFileUtil.writeTextFile(state, path, content);
        return Boolean.TRUE;
    }

    public static final String JWKS_TYPE = "jwks";
    public static final String PKCS_1_TYPE = "pkcs1";
    public static final String PKCS_8_TYPE = "pkcs8";
    public static final String PKCS_8_PUBLIC_TYPE = "public";
    public static final String X509_TYPE = "x509";
    public static final String RSA_TYPE = "rsa";
    public static final String EC_TYPE = "elliptic";
    public static final String AES_TYPE = "aes";
    QDLStem types;

    public QDLStem getKeyTypes() {
        if (types == null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("jwks", JWKS_TYPE);
            map.put("pkcs1", PKCS_1_TYPE);
            map.put("pkcs8", PKCS_8_TYPE);
            map.put("public", PKCS_8_PUBLIC_TYPE);
            map.put("x509", X509_TYPE);
            map.put("rsa", RSA_TYPE);
            map.put("ec", EC_TYPE);
            map.put("aes", AES_TYPE);
            types = new QDLStem();
            StemUtility.setStemValue(types, map);
        }
        return types;
    }

    public static String KEY_TYPES_STEM_NAME = "$$KEY_TYPE.";

    public class KeyType implements QDLVariable {
        QDLStem keyTypes = null;

        @Override
        public String getName() {
            return KEY_TYPES_STEM_NAME;
        }

        @Override
        public Object getValue() {
            return getKeyTypes();
        }
    }


    public static JWK getJwk(PublicKey publicKey) {
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

    public static final String GET_PUBLIC_KEY_NAME = "to_public";

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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws NoSuchAlgorithmException, InvalidKeySpecException {
            // allows for single key as a stem or stem of them
            if (!qdlValues[0].isStem()) {
                throw new BadArgException(getName() + " requires a stem as its argument", 0);
            }
            QDLStem inStem = qdlValues[0].asStem();
            if (isSingleKey(inStem)) {
                if (isAES(inStem)) {
                    return asQDLValue(inStem);
                }
                JSONWebKey jsonWebKey = getJwkUtil().getJsonWebKey((JSONObject) inStem.toJSON());
                JSONWebKey pKey = JSONWebKeyUtil.makePublic(jsonWebKey);
                QDLStem outStem = new QDLStem();
                outStem.fromJSON(JSONWebKeyUtil.toJSON(pKey));
                return asQDLValue(outStem);
            }
            QDLStem outStem = new QDLStem();
            // try to process each entry as a separate key
            for (QDLKey kk : inStem.keySet()) {
                //QDLStem currentStem = (kk instanceof String) ? inStem.getStem((String) kk) : inStem.getStem((Long) kk);
                QDLStem currentStem =  inStem.get(kk).asStem();
                if (isAES(currentStem)) {
                    outStem.put(kk, currentStem);
                    continue;
                }
                JSONWebKey jsonWebKey = getJwkUtil().getJsonWebKey((JSONObject) currentStem.toJSON());
                JSONWebKey pKey = JSONWebKeyUtil.makePublic(jsonWebKey);
                QDLStem tempStem = new QDLStem();
                tempStem.fromJSON(JSONWebKeyUtil.toJSON(pKey));
                outStem.put(kk, tempStem);
            }
            return asQDLValue(outStem);
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
                dd.add("Note that AES keys are always public since there are no private parts.");
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
            return new int[]{0,2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if(qdlValues.length ==0){
                    // Query for supported ciphers.
                    // we used to allow a query for this, but really cannot support them all
                    QDLStem outStem = new QDLStem();
                    ArrayList<QDLValue> ciphers = new ArrayList<>();
                    for(String x : DecryptUtils.listCiphers()){
                        ciphers.add(asQDLValue(x));
                    }
                    outStem.getQDLList().setArrayList(ciphers);
                    return asQDLValue(outStem);
            }
            if (!qdlValues[1].isStem()) {
                throw new BadArgException("The key for " + getName() + " must be a stem", 1);
            }
            // arg 0 is either stem of the key or a cfg stem (which includes the key as 'key' entry)
            // arg 1 is either string or stem of strings to encrypt.
            QDLStem keyStem =  qdlValues[1].asStem();
            if (isAES(keyStem)) {
                return asQDLValue(sDeOrEnCrypt(qdlValues, true, getName()));
            }
            if (isEC(keyStem)) {
                throw new BadArgException(getName() + " unsupported key type", 1);
            }
            JSONWebKey jsonWebKey;
            String cipher = "RSA"; // There are several available.
            boolean usePrivateKey = true;
            if(qdlValues.length == 3 ) {
                if(qdlValues[2].isBoolean()) {
                    usePrivateKey = qdlValues[2].asBoolean();
                }else{
                    throw new BadArgException(getName() + " final argument must be a boolean if present", 2);
                }
            }
            if (keyStem.containsKey("key")) {
                jsonWebKey = getKeys(keyStem.getStem("key"));
                if (keyStem.containsKey("cipher")) {
                    cipher = keyStem.getString("cipher");
                }
            } else {
                jsonWebKey = getKeys(keyStem);
            }
            if (usePrivateKey) {
                if (jsonWebKey.privateKey == null) {
                    throw new BadArgException("Missing private key", 1);
                }
            } else {
                if (jsonWebKey.publicKey == null) {
                    throw new BadArgException("Missing public key", 1);
                }
            }
            IdentityEncryptDecrypt processEncryptDecrypt = new IdentityEncryptDecrypt(jsonWebKey, cipher, usePrivateKey, false);
            return asQDLValue(QDLAggregateUtil.process(qdlValues[0], processEncryptDecrypt));
        }

/*
   crypto:=j_load('crypto')
  rsa. := crypto#create_key()
  crypto#encrypt(rsa., 'woof woof')
  crypto#encrypt({'key':rsa.,'cipher':'RSA'}, 'woof woof')

  set_test := crypto#encrypt(rsa.,{'a',{'b'}})
   crypto#decrypt(rsa., set_test)
 */

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 0:
                    dd.add(getName() + "() - query for list of supported AES ciphers.");
                    dd.add("Use any of these to create the encryption key.");
                    return dd;
                case 2:
                    dd.add(getName() + "(arg|arg., key.) - encrypt a string or stem of them. If the key is symmetric, do symmetric encryption, otherwise encrypt with the private key");
                    break;
                case 3:
                    dd.add(getName() + "(arg|arg., key., use_private) - encrypt a string or stem of them with the private key if use_private is true");
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
            dd.add("   " + getName() + "('marizy doats', key.)");
            dd.add("(whole bunch of base 64 stuff that depends on the key)");
            dd.add("Since this was encrypted with the private key, you would need to specify using the");
            dd.add("public key in " + DECRYPT_NAME + " (which is, incidentally, the default there).");
            dd.add("\nE.g. Symmetric example");
            dd.add("Here, a symmetric key (AES) is created and used.");
            dd.add("    aes. := crypto#create_key({'type':'" + AES_TYPE + "','alg':'A256GCM','length':512})\n" +
                    "    crypto#encrypt('woof woof woof', aes.) \n" +
                    "67dmKZ6lqHwSt-mIZGs\n" +
                    "    crypto#decrypt('67dmKZ6lqHwSt-mIZGs', aes.)\n" +
                    "woof woof woof\n");
            return dd;
        }
    }

    protected class IdentityEncryptDecrypt extends IdentityScalarImpl {
        String cipher;
        JSONWebKey jsonWebKey;
        boolean usePrivateKey;
        boolean doDecrypt;

        public IdentityEncryptDecrypt(JSONWebKey jsonWebKey,
                                      String cipher,
                                      boolean usePrivateKey,
                                      boolean doDecrypt) {
            this.cipher = cipher;
            this.jsonWebKey = jsonWebKey;
            this.usePrivateKey = usePrivateKey;
            this.doDecrypt = doDecrypt;
        }

        @Override
        public Object process(String stringValue) {
            return process(null, null, stringValue);
        }

        @Override
        public Object process(List index, Object key, String inString) {
            String result;
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
                return result;
            } catch (RuntimeException rt) {
                throw rt;
            } catch (Throwable gsx) {
                // Clean up exception with a better message
                if (key == null) {
                    throw new IllegalArgumentException((doDecrypt ? DECRYPT_NAME : ENCRYPT_NAME) + " could not process value ='" + inString + "' (" + gsx.getMessage() + ")");
                }
                throw new IllegalArgumentException((doDecrypt ? DECRYPT_NAME : ENCRYPT_NAME) + " could not process argument for key='" + key + "' with value ='" + inString + "' (" + gsx.getMessage() + ")");
            }
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            if (!qdlValues[1].isStem()) {
                throw new BadArgException("The second argument of " + getName() + " must be a stem", 1);
            }
            QDLStem leftArg = qdlValues[1].asStem();
            if (isAES(leftArg)) {
                return asQDLValue(sDeOrEnCrypt(qdlValues, false, getName()));
            }
            if (isEC(leftArg)) {
                throw new BadArgException(getName() + " unsupported key type", 1);
            }
            boolean usePrivateKey = false;

            if (qdlValues.length == 3) {
                if (!(qdlValues[2].isBoolean())) {
                    throw new BadArgException("the last argument of " + getName() + " must be a boolean. Default is true", 2);
                }
                usePrivateKey = qdlValues[2].asBoolean();
            }
            JSONWebKey jsonWebKey = getKeys(leftArg);
            if (usePrivateKey) {
                if (jsonWebKey.privateKey == null) {
                    throw new BadArgException("Missing private key", 1);
                }
            } else {
                if (jsonWebKey.publicKey == null) {
                    throw new BadArgException("Missing public key", 1);
                }
            }
            String cipher = "RSA"; // There are several available.
            IdentityEncryptDecrypt processEncryptDecrypt = new IdentityEncryptDecrypt(jsonWebKey,
                    cipher, usePrivateKey, true);
            return asQDLValue(QDLAggregateUtil.process(qdlValues[0], processEncryptDecrypt));
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 2:
                    doxx.add(getName() + "(arg|arg., key.) - decrypt the argument. If the key is symmetric, use that, otherwise use the public key");
                    break;
                case 3:
                    doxx.add(getName() + "(arg|arg., key., use_private) - decrypt the argument using the private key if use_private == true");
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
                doxx.add(getName() + "(my_string, key.)");
                doxx.add("E.g. (roundtrip)");
                doxx.add("   " + getName() + "(" + ENCRYPT_NAME + "('marizy doats', key.), key.)");
                doxx.add("marizy doats");
                doxx.add("In this case, the encryption happens with the private key and the decryption with ");
                doxx.add("the public part of the key.");
                doxx.add("Note that the public private parts must be opposite, so ");
                doxx.add("E.g. (roundtrip, with keys type reverse)");
                doxx.add("   " + getName() + "(" + ENCRYPT_NAME + "('marizy doats', key., false), key., true)");
                doxx.add("marizy doats");
            }
            return doxx;
        }
    }

    protected JSONWebKey getKeys(QDLStem keys) {
        return JSONWebKeyUtil.getJsonWebKey(keys.toJSON().toString());
    }

    public static QDLStem webKeyToStem(JSONWebKey jsonWebKey) {
        QDLStem keys = new QDLStem();
        JSONObject json = JSONWebKeyUtil.toJSON(jsonWebKey);
        keys.fromJSON(json);
        return keys;
    }

    /*
     crypto := j_load('crypto');
         aes. := crypto#create_key({'type':'AES','alg':'A256GCM','length':512})
         crypto#encrypt(aes., 'woof woof woof');
kazrnybI9mX73qv6NqA
  crypto#encrypt(aes., {'a':'woof woof woof'})
{a:kazrnybI9mX73qv6NqA}
  crypto#encrypt(aes., {'b',{'a':'woof woof woof'}})
{b,{a:woof woof woof}}
  crypto#encrypt(aes., {'woof woof woof'})
{woof woof woof}
    crypto#encrypt(aes., {'b',{'a':'woof woof woof'}})
     */
    public Object sDeOrEnCrypt(QDLValue[] objects, boolean isEncrypt, String name) {
        byte[] key = null;
        if (objects[1].isStem()) {
            // check that it is a JWK of type octet
            QDLStem sKey = objects[1].asStem();
            if (sKey.containsKey("kty")) {
                if (!sKey.getString("kty").equals("oct")) {
                    throw new BadArgException("Incorrect key type. Must be of type 'oct' (octet-encoded)", 1);
                }
                if (sKey.containsKey("k")) {
                    key = Base64.decodeBase64(sKey.getString("k"));
                } else {
                    throw new BadArgException("Incorrect key format: missing 'k' entry for bytes", 1);
                }
            }
        } else {
            throw new BadArgException("the second argument to " + name + " must be a key stem", 1);
/*          The utilities accept a byte string as the key, but that breaks the module's contract, so we disallow it here.
            if (!(objects[1] instanceof String)) {
                throw new BadArgException("the first argument to " + name + " must be a base64 encoded key", 1);
            }
            key = Base64.decodeBase64((String) objects[1]);
*/
        }

        IdentitySymmetricDeorEncrypt processSymmetricDeorEncrypt = new IdentitySymmetricDeorEncrypt(key, isEncrypt);
        return QDLAggregateUtil.process(objects[0], processSymmetricDeorEncrypt);
    }

    protected class IdentitySymmetricDeorEncrypt extends IdentityScalarImpl {
        boolean isEncrypt = false;
        byte[] key;

        public IdentitySymmetricDeorEncrypt(byte[] key, boolean isEncrypt) {
            this.isEncrypt = isEncrypt;
            this.key = key;
        }

        @Override
        public Object process(String stringValue) {
            if (isEncrypt) {
                return DecryptUtils.sEncrypt(key, stringValue);
            }
            return DecryptUtils.sDecrypt(key, stringValue);
        }

        @Override
        public Object process(List index, Object key, String stringValue) {
            return process(stringValue);
        }
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


    static SecureRandom secureRandom = new SecureRandom();

    public static String getRandomID() {
        return getRandomID(8);
    }

    /**
     * Creates a random id as an (upper case) hex number.
     *
     * @param byteCount
     * @return
     */
    public static String getRandomID(int byteCount) {
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

    public static String READ_CERT = "read_x509";

    public class ReadCert implements QDLFunction {
        @Override
        public String getName() {
            return READ_CERT;
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            String path = qdlValues[0].asString();
            QDLStem out = new QDLStem();
            String rawCert = QDLFileUtil.readTextFile(state, path);
            X509Certificate[] certs = CertUtil.fromX509PEM(rawCert);
            if (certs.length == 1) {
                QDLStem outStem = certToStem(certs[0]);
                outStem.put(from("encoded"), rawCert);
                return asQDLValue(outStem);
            }
            for (X509Certificate cert : certs) {
                out.getQDLList().add(asQDLValue(certToStem(cert)));
            }
            out.getStem(0L).put(from("encoded"), rawCert);
            return asQDLValue(out);
        }


        /*
         crypto := j_load('crypto')
         print(cert.:=crypto#read_x509('/home/ncsa/Downloads/github-com.pem'))
         crypto#read_oid(cert., {'foo':'2.5.29.19'})
  decode(crypto#read_oid(cert., '1.3.6.1.4.1.5923.1.1.1.6'))

        */
        protected QDLStem certToStem(X509Certificate x509Certificate) {
            QDLStem out = new QDLStem();
            QDLStem subject = new QDLStem();
            subject.put(from("x500"), x509Certificate.getSubjectX500Principal().getName());
            subject.put(from("dn"), x509Certificate.getSubjectDN().getName());

            try {
                if (x509Certificate.getSubjectAlternativeNames() != null) {
                    QDLStem altNames = processAltNames(x509Certificate.getSubjectAlternativeNames());
                    if (altNames != null) {
                        subject.put(from("alt_names"), altNames);
                    }
                }
            } catch (CertificateParsingException e) {
                // throw new RuntimeException(e);
            }
            QDLStem criticalOIDS = new QDLStem();
            for (String x : x509Certificate.getCriticalExtensionOIDs()) {
                criticalOIDS.getQDLList().add(asQDLValue(x));
            }
            QDLStem noncriticalOIDS = new QDLStem();
            for (String x : x509Certificate.getNonCriticalExtensionOIDs()) {
                noncriticalOIDS.getQDLList().add(asQDLValue(x));
            }

            QDLStem oids = new QDLStem();
            oids.put(from("critical"), criticalOIDS);
            oids.put(from("noncritical"), noncriticalOIDS);
            out.put(from("oids"), oids);
            out.put(from("subject"), subject);
            QDLStem issuer = new QDLStem();
            issuer.put(from("x500"), x509Certificate.getIssuerX500Principal().getName());
            issuer.put(from("dn"), x509Certificate.getIssuerDN().getName());
            if (x509Certificate.getIssuerUniqueID() != null) {

                QDLStem issuerUniqueID = new QDLStem();
                ArrayList list = new ArrayList();
                list.add(x509Certificate.getIssuerUniqueID());
                issuerUniqueID.getQDLList().setArrayList(list);
                issuer.put(from("unique_id"), issuerUniqueID);
            }
            try {
                if (x509Certificate.getSubjectAlternativeNames() != null) {
                    QDLStem altNames = processAltNames(x509Certificate.getSubjectAlternativeNames());
                    if (altNames != null) {
                        issuer.put(from("alt_names"), altNames);
                    }
                }
            } catch (CertificateParsingException e) {
                // throw new RuntimeException(e);
            }
            out.put(from("issuer"), issuer);
            out.put(from("not_before"), x509Certificate.getNotBefore().getTime());
            out.put(from("not_after"), x509Certificate.getNotAfter().getTime());
            QDLStem alg = new QDLStem();
            alg.put(from("name"), x509Certificate.getSigAlgName());
            alg.put(from("oid"), x509Certificate.getSigAlgOID());
            if (x509Certificate.getSigAlgParams() != null) {
                alg.put(from("parameters"), Base64.encodeBase64URLSafe(x509Certificate.getSigAlgParams()));
            }
            out.put(from("algorithm"), alg);
            out.put(from("serial_number"), x509Certificate.getSerialNumber().toString());
            out.put(from("signature"), Base64.encodeBase64URLSafeString(x509Certificate.getSignature()));
            out.put(from("version"), "v" + x509Certificate.getVersion()); // standard way to write it
            String eppn = CertUtil.getEPPN(x509Certificate);
            if (eppn != null) {
                out.put(from("eppn"), eppn);
            }
            String email = CertUtil.getEmail(x509Certificate);
            if (email != null) {
                out.put(from("email"), email);
            }
            PublicKey publicKey = x509Certificate.getPublicKey();
            JWK jwk = getJwk(publicKey);
            if (jwk != null) {
                JSONWebKey jsonWebKey = new JSONWebKey(jwk);
                QDLStem pKeyStem = webKeyToStem(jsonWebKey);
                out.put(from("public_key"), pKeyStem);
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
                altNames.put(from(altName[0]), altName[1]);
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
            dd.add("A certificate chain is reutned as a list of certs.");
            dd.add("Note that you can neither change a cert nor write one!");
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            QDLStem certStem = qdlValues[0].asStem();
            if (!certStem.containsKey("encoded")) {
                throw new BadArgException("certs must contain 'encoded' key", 0);
            }
            String cert = certStem.getString("encoded");
            X509Certificate[] certs = CertUtil.fromX509PEM(cert);

            IdentityOIDS processOIDS = new IdentityOIDS(certs[0]);
            return asQDLValue(QDLAggregateUtil.process(qdlValues[1], processOIDS));
        }

        protected class IdentityOIDS extends IdentityScalarImpl {
            public IdentityOIDS(X509Certificate x509Certificate) {
                this.x509Certificate = x509Certificate;
            }

            X509Certificate x509Certificate;
            @Override
            public Object getDefaultValue(List<Object> index,Object key, Object value) {
                return QDLNull.getInstance();
            }

            @Override
            public Object process(String oidKey) {
                byte[] bb = x509Certificate.getExtensionValue(oidKey);
               if(bb == null){
                   return QDLNull.getInstance();
               }
                return Base64.encodeBase64URLSafeString(bb);
            }

            @Override
            public Object process(List index, Object key, String oidKey) {
                return process(oidKey);
            }
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(cert., oid | oids.) - get the given OIDs from a given cert.");
            dd.add("cert. - a single X509 certificate to be probed.");
            dd.add("  oid - a string or (set of such strings) that is the oid (object id), e.g. '2.5.29.15'");
            dd.add("oids. - a stem of oids whose keys are the return values. Only string-valued OIDs are supported.");
            dd.add("This returns the base 64 encoded octet stream. Best we can do in general...");
            dd.add("If there is no such value, a null is returned.");
            dd.add("An OID (object identifier) is a bit of X 509 voodoo that allows for");
            dd.add("addressing attributes. These are simply byte arrays that may have really any structure, hence require");
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

    /**
     * Tests if a given stem that is a key is an AES i.e., symmetric key.
     *
     * @param key
     * @return
     */
    protected boolean isAES(QDLStem key) {
        return key.containsKey("kty") && key.getString("kty").equals("oct");
    }

    protected boolean isEC(QDLStem key) {
        return key.containsKey("kty") && key.getString("kty").equals("EC");
    }

    protected boolean isRSA(QDLStem key) {
        return key.containsKey("kty") && key.getString("kty").equals("RSA");
    }

    public static String SIGN_JWT = "to_jwt";
    public static String JWT_TYPE = "typ";
    public static String JWT_KEY_ID = "kid";
    public static String JWT_ALGORITHM = "alg";
    public static String JWT_DEFAULT_TYPE = "JWT";
    public static String JWT_ALGORITHM_NONE = "none";

    public class ToJWT implements QDLFunction {
        @Override
        public String getName() {
            return SIGN_JWT;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2, 3};
        }

        @Override
        public QDLValue evaluate(QDLValue[] objects, State state) throws Throwable {
            JSONObject header = null;
            JSONObject payload = null;
            JSONWebKey webkey = null;
            int argIndex = 0;
            if (objects.length == 1) {
                header = new JSONObject();
                header.put(JWT_TYPE, JWT_DEFAULT_TYPE);
                header.put(JWT_ALGORITHM, JWT_ALGORITHM_NONE);
                if (!(objects[argIndex].isStem())) {
                    throw new BadArgException(getName() + " requires a stem as its first argument", 0);
                }
                payload = (JSONObject) objects[argIndex++].asStem().toJSON();
            } else {

                if (objects.length == 3) {
                    if (!(objects[argIndex].isStem())) {
                        throw new BadArgException(getName() + " requires a stem as its header", argIndex);
                    }
                    header = (JSONObject) objects[argIndex++].asStem().toJSON();
                }
                if (!(objects[argIndex].isStem())) {
                    throw new BadArgException(getName() + " requires a stem as its payload", argIndex);
                }

                payload = (JSONObject) objects[argIndex++].asStem().toJSON();
                if (!(objects[argIndex].isStem())) {
                    throw new BadArgException(getName() + " requires a stem as its key", argIndex);
                }
                webkey = getKeys(objects[argIndex++].asStem());

            }
            // If the algorithm is none, then do not sign the JWT, just return the encoded header + "." +  payload + "."
            // (note the trailing period!)
            if (header != null && header.containsKey(JWT_ALGORITHM) && header.get(JWT_ALGORITHM).equals(JWT_ALGORITHM_NONE)) {
                return asQDLValue(Base64.encodeBase64URLSafeString(header.toString().getBytes()) + "." +
                        Base64.encodeBase64URLSafeString(payload.toString().getBytes()) + ".");

            }
            if (webkey.isOctetKey()) {
                throw new BadArgException("cannot sign with octet keys", argIndex - 1);
            }
            if (header == null) {
                // create one
                header = new JSONObject();
            }
            if (!header.containsKey(JWT_TYPE)) {
                header.put(JWT_TYPE, JWT_DEFAULT_TYPE);
            }
            if (!header.containsKey(JWT_ALGORITHM)) {
                header.put(JWT_ALGORITHM, webkey.algorithm);
            }
            if (!StringUtils.isTrivial(webkey.id)) {
                // ID is not required, so only add if present
                header.put(JWT_KEY_ID, webkey.id);
            }
            JWSHeader jwsHeader = JWSHeader.parse(header);
            JWTClaimsSet jwsPayload = JWTClaimsSet.parse(payload);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwsPayload);
            JWSSigner signer = null;
            boolean unsupportedSigner = true;
            if (webkey.isRSAKey()) {
                signer = new RSASSASigner(webkey.privateKey);
                unsupportedSigner = false;
            }
            if (webkey.isECKey()) {
                signer = new ECDSASigner((ECPrivateKey) webkey.privateKey);
                unsupportedSigner = false;
            }
            if (unsupportedSigner) {
                throw new UnsupportedProtocolException("unsupported key type for signature verification");
            }
            signedJWT.sign(signer);
            return asQDLValue(signedJWT.serialize());
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 1:
                    dd.add(getName() + "(payload.) - create an unsigned JWT from the stem.");
                    dd.add("payload. - the stem that will be the payload of the JWT");
                    dd.add("A header will be created automatically");
                    break;
                case 2:
                    dd.add(getName() + "(payload., key.) - create a JWT from the stem, signing it with the key.");
                    dd.add("payload. - the stem that will be the payload of the JWT");
                    dd.add("key. - the key to use. RSA and EC curves are supported.");
                    dd.add("A header will be created automatically");
                    break;
                case 3:
                    dd.add(getName() + "(header., payload., key.) - create a JWT from the stem, signing it with the key.");
                    dd.add("header. - the stem that will be the header of the JWT");
                    dd.add("payload. - the stem that will be the payload of the JWT");
                    dd.add("key. - the key to use. RSA and EC curves are supported.");
                    dd.add("If you need the JWT to be unsigned and require a custom header, set the header to have {'alg':'none'}");
                    break;
            }
            return dd;
        }
    }

    public static String FROM_JWT = "from_jwt";

    public class FromJWT implements QDLFunction {
        @Override
        public String getName() {
            return FROM_JWT;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public QDLValue  evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            //   JSONWebKey webKey = getKeys((QDLStem) objects[1]);
            IdentityJWT processJWT = new IdentityJWT();
            return asQDLValue(QDLAggregateUtil.process(qdlValues[0], processJWT));
        }


        /*
        p. ≔ {'a':'q','b':{'s':'t'}};
        crypto ≔ j_load('crypto');
        rsa. ≔ crypto#create_key(2048);
        rr ≔ crypto#to_jwt(p., rsa.);
        crypto#verify(rr,rsa.);
        crypto#verify({'A':rr,'B':{'C':rr}},rsa.);
         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + "(jwt | jwt., key.) - Convert the jwt strings to their payload");
                    dd.add("jwt - a JWT (a string) or set of them");
                    dd.add("jwt. - a stem of JWTs");
                    dd.add("key. - the key to verify against");
                    dd.add("Returns the stem of the payload. No verification is done. Any non-strings will");
                    dd.add("result in an error.");
                    break;
            }
            return dd;
        }
    }

    public static String VERIFY_JWT = "verify";

    public class VerifyJWT implements QDLFunction {
        @Override
        public String getName() {
            return VERIFY_JWT;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            JSONWebKey webKey = getKeys(qdlValues[1].asStem());
            DoJWTVerify doJWTVerify = new DoJWTVerify(webKey);
            return asQDLValue(QDLAggregateUtil.process(qdlValues[0], doJWTVerify));
        }


        /*
        p. ≔ {'a':'q','b':{'s':'t'}};
        crypto ≔ j_load('crypto');
        rsa. ≔ crypto#create_key(2048);
        rr ≔ crypto#to_jwt(p., rsa.);
       crypto#verify('woof woof woof',rsa.); // scalar bad example
       crypto#verify({'A':rr,'B':{'C':rr,'D':'arf'}},rsa.); // Bad JWT at B.D
        crypto#verify(rr,rsa.); // good scalar example
        crypto#verify({'A':rr,'B':{'C':rr}},rsa.); // Good stem example
        A. ≔ {'a':rr,'b':{'c':rr,'d':{'e':123}}};
        crypto#verify(A., rsa.); // bad stem example at b.d.e
         */
        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 2:
                    dd.add(getName() + "(jwt | jwt., key.) - verify the signature of a JWT against a given key");
                    dd.add("jwt - the JWT or set of them");
                    dd.add("jwt. - a stem of JWT");
                    dd.add("key. - the key to verify against");
                    dd.add("Returns a left conformable output with true if the verification worked and false");
                    dd.add("otherwise. Note that unknown entries fail verification.");
                    dd.add("Verification here means solely that the signature for the JWT corresponds");
                    dd.add("to that for the given key. In many cases (such as OAuth) \"verification\" also implies");
                    dd.add("a variety of other checks on the content of the payload and header.");
                    dd.add("which this function does not do.");
                    dd.add("If you supply a set of JWTs do note that the result is a set with at most two values,");
                    dd.add("{true,false}. ");
                    break;
            }
            return dd;
        }
    }

    //public class ProcessJWT extends ProcessScalarImpl {
    public class DoJWTVerify extends IdentityScalarImpl {
        public DoJWTVerify(JSONWebKey webKey) {
            this.webKey = webKey;
        }

        JSONWebKey webKey;

        @Override
        public Object getDefaultValue(List<Object> index,Object key, Object value) {
            return Boolean.FALSE;
        }

        @Override
        public Object process(List index, Object key, String jwt) {
            try {
                return process(jwt);
            } catch (Throwable ex) {
                throw new BadStemValueException("error processing JWT:" + ex.getMessage());
            }

        }

        @Override
        public Object process(String jwt) {
            String[] b64s = jwt.split("\\.");
            if (b64s.length != 2 && b64s.length != 3) {
                return Boolean.FALSE;
            }
            if (b64s.length == 2) {
                // Maybe an unsigned JWT? Verification consists of checking the algorithm
                // is indeed "none"
                try {
                    String header = new String(Base64.decodeBase64(b64s[0]));
                    JSONObject h = JSONObject.fromObject(header);
                    if (h.containsKey(JWT_ALGORITHM) && h.getString(JWT_ALGORITHM).equals(JWT_ALGORITHM_NONE)) {
                        String payload = new String(Base64.decodeBase64(b64s[1]));
                        QDLStem out = new QDLStem();
                        out.fromJSON(JSONObject.fromObject(payload));
                        return Boolean.TRUE;
                    }
                } catch (Throwable ex) {
                    return Boolean.FALSE;
                }
            }
            try {

                SignedJWT signedJWT = new SignedJWT(new Base64URL(b64s[0]),
                        new Base64URL(b64s[1]),
                        new Base64URL(b64s[2]));
                JWSVerifier verifier = null;
                boolean unsupportedProtocol = true;
                if (webKey.isRSAKey()) {
                    verifier = new RSASSAVerifier((RSAPublicKey) webKey.publicKey);
                    unsupportedProtocol = false;
                }
                if (webKey.isECKey()) {
                    verifier = new ECDSAVerifier((ECPublicKey) webKey.publicKey);
                    unsupportedProtocol = false;
                }
                if (unsupportedProtocol) {
                    throw new UnsupportedProtocolException("unsupported protocol");
                }
                return signedJWT.verify(verifier);

            } catch (Throwable throwable) {
            }
            return Boolean.FALSE;
        }
    }

    //public class ProcessJWT extends ProcessScalarImpl {
    public class IdentityJWT extends IdentityScalarImpl {

        public IdentityJWT() {
        }

        @Override
        public Object process(List index, Object key, String jwt) {
            try {
                return process(jwt);
            } catch (Throwable ex) {
                throw new BadStemValueException("error processing JWT:" + ex.getMessage());
            }

        }

        @Override
        public Object process(String jwt) {
            String[] b64s = jwt.split("\\.");
            if (b64s.length != 2 && b64s.length != 3) {
                return jwt; // not a JWT
            }
            if (b64s.length == 2) {
                // Maybe an unsigned JWT? Verification consists of checking the algorithm
                // is indeed "none"
                try {
                    String header = new String(Base64.decodeBase64(b64s[0]));
                    JSONObject h = JSONObject.fromObject(header);
                    if (h.containsKey(JWT_ALGORITHM) && h.getString(JWT_ALGORITHM).equals(JWT_ALGORITHM_NONE)) {
                        String payload = new String(Base64.decodeBase64(b64s[1]));
                        QDLStem out = new QDLStem();
                        out.fromJSON(JSONObject.fromObject(payload));
                        return out;
                    }
                } catch (Throwable t) {
                    return jwt;
                }
            }
            // has 3 elements, so it is a JWT.
            try {
                String payload = new String(Base64.decodeBase64(b64s[1]));
                QDLStem out = new QDLStem();
                out.fromJSON(JSONObject.fromObject(payload));
                return out;
            } catch (Throwable throwable) {
                return jwt; // pass it back unaltered
            }
        }
    }

}
