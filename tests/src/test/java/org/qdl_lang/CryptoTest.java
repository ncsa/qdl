package org.qdl_lang;

import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.extensions.crypto.Crypto;
import org.qdl_lang.parsing.QDLInterpreter;
import org.qdl_lang.state.State;

public class CryptoTest extends AbstractQDLTester {
    /*
       These files contain QDl scripts that just return the keys. This is so the test for
       importing them is a separate test.
     */
    String EC_PUBLIC_KEY = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/ec_public.qdl";
    String EC_KEY = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/ec.qdl";
    String RSA_KEY = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/rsa.qdl";
    String RSA_PUBLIC_KEY = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/rsa_public.qdl";
    String AES_KEY = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/aes.qdl";
    /*
        These are test items in PEM format to check reading, import, etc.
    */
    String TEST_CERT = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/github.pem";
    String TEST_PKCS1 = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/pkcs1.pem";
    String TEST_PKCS8 = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/pkcs8.pem";
    String TEST_PKCS8_PUBLIC = DebugUtil.getDevPath() + "/qdl/tests/src/test/resources/crypto/pkcs8_public.pem";

    public void testSymmetricEncryption() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "aes. := script_load('" + AES_KEY + "');");
        addLine(script, "c := j_load('crypto');");
        addLine(script, "test_string := 'woof woof woof';");
        addLine(script, "encrypted_string := 'LXwYRcBgwmIpFUtAF10';");
        addLine(script, "test_set := {'arf',42,{'fnord'}};");
        addLine(script, "encrypted_set := {{'PH0YUYQ'},'O2ER',42};");
        addLine(script, "encrypt_string_ok := c#encrypt(test_string, aes.) == encrypted_string;");
        addLine(script, "decrypt_string_ok := c#decrypt(encrypted_string, aes.) == test_string;");
        addLine(script, "encrypt_set_ok := c#encrypt(test_set, aes.) == encrypted_set;");
        addLine(script, "decrypt_set_ok := c#decrypt(encrypted_set, aes.) == test_set;");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("encrypt_string_ok", state) : "symmetric encryption for string failed";
        assert getBooleanValue("decrypt_string_ok", state) : "symmetric decryption for string failed";
        assert getBooleanValue("encrypt_set_ok", state) : "symmetric encryption for set failed";
        assert getBooleanValue("decrypt_set_ok", state) : "symmetric decryption for set failed";
    }

    public void testRSAEncryption() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "rsa. := script_load('" + RSA_KEY + "');");
        addLine(script, "c := j_load('crypto');");
        addLine(script, "test_string := 'woof woof woof';");
        addLine(script, "encrypted_string := 'FWSjwdYsxmoZK0fHjb7pzo0y0_Cw1VDUNC4_LIpJRqJP3uwDbA6z2_8SE72Tn70cOFzLP45Ydhj4Fetbyz6EV6P-S4-FX5Kqj0KlR37FEQ3IlPs3boemZfn8RpBUQ5zZDTwRLjeDLG34iL1hk3bC2JLefFxzCbvVgK0EnMpCa7PGdsHAbcu_2mm4rw8uNrCAzFaNUBRZ_LYVnP2PwzM34Pl09OEr4E10q41YpXeNMSexxJzfUnkubmxNaTomY_iETElgrtOLeNcf8k-tGP9P9_zB7fgvE4ujJJCT3mvT0Fl1EhHa0ji2vN7vsiu2L8Qmh2SNNOHiUZoGoySNzMMWkw';");
        addLine(script, "test_set := {'arf',42,{'fnord'}};");
        addLine(script, "encrypted_set := {'D9aV2v7iXkdaHUcxmREh2btp35mFiiZpgdhK_CqMG46PNM2RATwZIUJMv1KvxIPmZwu0H2Fx1y-wBAEqeqVG0gopvk-Ts3MbsIwdt6Bq_znN54gPzZJSIp9jbwdiqM1HOkipAnwjTlA5WQ-glFgs_bFl-yHaIifnDtvRBDWoRYbOQGvAuo_HDi40_FT7xSYV-haRO4ofqINMnz-geeVt-KnSEvv_43pqDLLlOBTwdeLvkFJ4OASQ3jY6bKM4NmgAD-2ne9jgH2so7JgoFugWVQhIi2Jpjr2zr3Aw6RfAUCjbOdmxYFxwr0FIoxTUKuS38Pv9iociKZ2hobxgJDH7hA',{'ZBPnmBx8bC1Dwm8YcbCrkTRat14RdUDWsw7UDtBLYH1JuCxkdSqqb7nicyMVIFMvkv5T21dLSCtdQxfkoHFSEmWlPtW6KISdXQPVSHILehhvWYKAAZ52d7kbIgRmJnAsrL_3DvnRX05sGuWRIAQ3dQH3h4P0OliT7b-GvhPVEe6Wjxyi6LQnH3gcK2SbkQ5OyQtvkVNVVOt8ZZrPtemPc3jehNcgHX_U19ORVNmYajj9awxX_C32QsTAl6QveQaA-4_UmCVkDzZZRL66liaAjPo9hSbLChoADvSEDRLi96IJd2FW_J0M0qX_DghsHy0mS0qCehgpI54XOnwIdzz7EA'},42};");
        addLine(script, "encrypt_string_ok := c#encrypt(test_string, rsa.) == encrypted_string;");
        addLine(script, "decrypt_string_ok := c#decrypt(encrypted_string, rsa.) == test_string;");
        addLine(script, "encrypt_set_ok := c#encrypt(test_set, rsa.) == encrypted_set;");
        addLine(script, "decrypt_set_ok := c#decrypt(encrypted_set, rsa.) == test_set;");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("encrypt_string_ok", state) : "RSA encryption for string failed";
        assert getBooleanValue("decrypt_string_ok", state) : "RSA decryption for string failed";
        assert getBooleanValue("encrypt_set_ok", state) : "RSA encryption for set failed";
        assert getBooleanValue("decrypt_set_ok", state) : "RSA decryption for set failed";
    }

    public void testToPublic() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "rsa. := script_load('" + RSA_KEY + "');");
        addLine(script, "rsa_public. := script_load('" + RSA_PUBLIC_KEY + "');");
        addLine(script, "ec. := script_load('" + EC_KEY + "');");
        addLine(script, "ec_public. := script_load('" + EC_PUBLIC_KEY + "');");
        addLine(script, "aes. := script_load('" + AES_KEY + "');");
        addLine(script, "c := j_load('crypto');");
        addLine(script, "rsa_ok := false ∉ (c#to_public(rsa.) == rsa_public.);");
        addLine(script, "ec_ok := false ∉ (c#to_public(ec.) == ec_public.);");
        addLine(script, "aes_ok := false ∉ (c#to_public(aes.) == aes.);"); // symmetric key is public
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("rsa_ok", state) : "convert RSA to public key failed";
        assert getBooleanValue("ec_ok", state) : "convert EC to public key failed";
        assert getBooleanValue("aes_ok", state) : "convert AES to public key failed";
    }

    public void testJWTRSA() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "rsa. := script_load('" + RSA_KEY + "');");
        addLine(script, "c := j_load('crypto');");
        addLine(script, "test. := {'A':'p','B':{'integer':42},'C':3.1415};");
        addLine(script, "jwt := c#to_jwt(test., rsa.);");
        addLine(script, "verified := c#verify(jwt, rsa.);");
        addLine(script, "payload. := c#from_jwt(jwt);");
        addLine(script, "ok := false ∉ test. == payload.;");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("verified", state) : "JWT with RSA verified failed";
        assert getBooleanValue("ok", state) : "JWT payload failed";
    }

    public void testJWTEC() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "ec. := script_load('" + EC_KEY + "');");
        addLine(script, "c := j_load('crypto');");
        addLine(script, "test. := {'A':'p','B':{'integer':42},'C':3.1415};");
        addLine(script, "jwt := c#to_jwt(test., ec.);");
        addLine(script, "verified := c#verify(jwt, ec.);");
        addLine(script, "payload. := c#from_jwt(jwt);");
        addLine(script, "ok := false ∉ test. == payload.;");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("verified", state) : "JWT with EC key verified failed";
        assert getBooleanValue("ok", state) : "JWT payload failed";
    }

    public void testReadCert() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "c := j_load('crypto');");
        addLine(script, "git. := c#read_x509('" + TEST_CERT + "');");
        addLine(script, "alg_ok := git.'algorithm'.'name' == 'SHA256withECDSA';");
        addLine(script, "iss_ok := git.'issuer'.'alt_names'.'dNSName' == 'www.github.com';");
        addLine(script, "expires_at_ok := git.'not_after' ==  1741391999000;");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("alg_ok", state) : "JWT with EC key verified failed";
        assert getBooleanValue("iss_ok", state) : "JWT with EC key verified failed";
        assert getBooleanValue("expires_at_ok", state) : "JWT with EC key verified failed";
    }
    public void testReadCertOIDs() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "c := j_load('crypto');");
        addLine(script, "git. := c#read_x509('" + TEST_CERT + "');");
        addLine(script, "critical. :=['BAQDAgeA','BAIwAA'];");
        addLine(script, "noncritical. :=['BIIBcASCAWwBagB3AM8RVu7VLnyv84db2Wkum-kacWdKsBfsrAHSW3fOzDsIAAABjhY68BkAAAQDAEgwRgIhAPug3P_ag7xUZpZauquwFAHNAfSFGEwubXWh4ymDV81rAiEApZzSrtn6bENVhX_qi_t_-LQf9oBwdIIiL9AlwQKto6kAdgCi4wrkRe-9rZt-OO1HZ3dT14JbhJTXK14bLMS5UKRH5wAAAY4WOu_4AAAEAwBHMEUCIQDK6kQhUAyTRzwFVWkXRBuKx-gTDLnElApA57wS8xThbwIgYAgi7OPEEWUemSpyxrtRnLbjL8HrFmeS1TD817mrmEIAdwBOdaMnXJoQwzhbbNTfP1LrHfDgjhuNacCx-mSxYpo53wAAAY4WOu_3AAAEAwBIMEYCIQD7w69DOmBF_fW4sGwITyS0JR--yJFPvNZKp5eWIDT1NQIhANwHtef3toQMwEpcht2bkpn0aO9HKgX2yQPn_gad6gxb','BHgwdjBPBggrBgEFBQcwAoZDaHR0cDovL2NydC5zZWN0aWdvLmNvbS9TZWN0aWdvRUNDRG9tYWluVmFsaWRhdGlvblNlY3VyZVNlcnZlckNBLmNydDAjBggrBgEFBQcwAYYXaHR0cDovL29jc3Auc2VjdGlnby5jb20'," +
                "'BBYEFDtoPzQ69Uc0yu-mTj2avV5uesyf'," +
                "'BB4wHIIKZ2l0aHViLmNvbYIOd3d3LmdpdGh1Yi5jb20'," +
                "'BEIwQDA0BgsrBgEEAbIxAQICBzAlMCMGCCsGAQUFBwIBFhdodHRwczovL3NlY3RpZ28uY29tL0NQUzAIBgZngQwBAgE'," +
                "'BBgwFoAU9oUKOxGG4QR9DqoLLNLuzGR7e64'," +
                "'BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMC'];");
        addLine(script, "read_critical. :=  c#read_oid(git., git.'oids'.'critical');");
        addLine(script, "c_count_ok := size(critical.) == size(read_critical.);");
        addLine(script, "critical_ok := false ∉  read_critical. == critical.;");

        addLine(script, "read_noncritical. ≔ c#read_oid(git., git.'oids'.'noncritical');");
        addLine(script, "nonc_count_ok := size(noncritical.) == size(read_noncritical.);");
        addLine(script, "noncritical_ok := false ∉  read_noncritical. == noncritical.;");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("c_count_ok", state) : "missing critical OIDs";
        assert getBooleanValue("critical_ok", state) : "incorrect critical OIDs";
        assert getBooleanValue("nonc_count_ok", state) : "missing noncritical OIDs";
        assert getBooleanValue("noncritical_ok", state) : "incorrect noncritical OIDs";
    }
    public void testReadPKCS1() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "c := j_load('crypto');");
        addLine(script, "pkcs1. := c#import('" + TEST_PKCS1 + "', '" + Crypto.PKCS_1_TYPE + "');");
        // testing whole thing is messy, so we test parts that must work.
        addLine(script, "dp_ok := pkcs1.'dp' == 'zhnLPN9F1Lqj-rWPry2fjfDulOpXCc4Q5NSlhvoYIxAoI0K7uaP4oGfRfHAFFCP4Q0NTpHIaFUNpf0ZiHfpz1hIaLHoClRGfw9J3elBImbYBdbVh5BIKZQ3QQl4lrXun3MlStoq5KILrgjby5SCqQtCrX7r_HvdBSmeixzbj0AU';");
        addLine(script, "e_ok := pkcs1.'e' == 'AQAB';");
        addLine(script, "q_ok := pkcs1.'q' == 'xwb2ZPwgCDVUSBYZxZdbrVOujrkW2Uk4KE7KVKMWLpDaCK2-c_LYXbsAJJ6JRecy-36XCUBgbzhFnFMdpoz9zOQ4_-t9DLvspwjy7jjLoSNAeUm26jTNVi5rNdySWJLdM0g5hPEOdbxSy8FrSaSfKKYca3DKeFPz1QCBg0jd_NE';");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("dp_ok", state) : "PKCS1 import key failed dp";
        assert getBooleanValue("e_ok", state) : "PKCS1 import key failed e";
        assert getBooleanValue("q_ok", state) : "PKCS1 import key failed q";
    }
    public void testReadPKCS8() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "c := j_load('crypto');");
        addLine(script, "pkcs8. := c#import('" + TEST_PKCS8 + "', '" + Crypto.PKCS_8_TYPE + "');");
        // testing whole thing is messy, so we test parts that must work.
        addLine(script, "dp_ok := pkcs8.'dp' == 'Q1E8HMJjtSp_vpg8Dgu8jLVBSWcpQ9wBjKaiYhC85BmNRTsE8QWLuIwUWam5lWqAdbM4Q31-ZKtj8cr4quD80ugx2Y_8c43N4-3MVErALf1agFZ52uZbkJSHJ7EytRaMbmy3cJ2raZ3ssd_-2AyBGHTbSsyzW2nnmHGycOSZm6E';");
        addLine(script, "e_ok := pkcs8.'e' == 'AQAB';");
        addLine(script, "q_ok := pkcs8.'q' == 'um0SMkYShdxFHLHWV1J9KqHpYH2kYx13lNhSRfspRKwjfsNyQWyIAAOdL8tW_O7458r9efkDv0MGR2I64nz2kdXMO7t3NLy2zP3HMVXws3HKjODiHD-3ebo6hxWLAiBqZ3KRsZChMzBPjK5bcXZdE7joGrmMOc2FbG7dSghfVwc';");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("dp_ok", state) : "PKCS8 import key failed dp";
        assert getBooleanValue("e_ok", state) : "PKCS8 import key failed e";
        assert getBooleanValue("q_ok", state) : "PKCS8 import key failed q";
    }
    public void testReadPKCS8Public() throws Throwable {
        StringBuffer script = new StringBuffer();
        addLine(script, "c := j_load('crypto');");
        addLine(script, "pkcs8. := c#import('" + TEST_PKCS8_PUBLIC + "', '" + Crypto.PKCS_8_PUBLIC_TYPE + "');");
        // testing whole thing is messy, so we test parts that must work.
        addLine(script, "n_ok := 'jn-jMHBqZoAsQ9EkBd4iwx9QckvJe1gUf9K_0iH37fHypqXkxSnITv4Hw4P-HZTWa94Re9ZLv223LJETEOoc5inK-rm1h-R5UVvJIWz7HU' < pkcs8.'n';");
        addLine(script, "e_ok := pkcs8.'e' == 'AQAB';");
        State state = testUtils.getNewState();
        QDLInterpreter interpreter = new QDLInterpreter(null, state);
        interpreter.execute(script.toString());
        assert getBooleanValue("n_ok", state) : "PKCS8 public key import failed n";
        assert getBooleanValue("e_ok", state) : "PKCS8 public key import failed e";
    }
}
