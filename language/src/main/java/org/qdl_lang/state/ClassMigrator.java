package org.qdl_lang.state;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static org.qdl_lang.xml.SerializationConstants.MODULE_CLASS_NAME_TAG;
import static org.qdl_lang.xml.SerializationConstants.MODULE_NS_ATTR;

/**
 * With the package renames from 1.5.6 to 1.6.0, serialized classes cannot be
 * reconstructed, This tool is used for that
 */
// Class fixes https://github.com/ncsa/qdl/issues/82, https://github.com/ncsa/oa4mp/issues/208
public class ClassMigrator {
    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ClassMigrator.enabled = enabled;
    }

    static boolean enabled = false;

    public static Map<String, String> getClasses() {
        return classes;
    }

    static public void setClasses(Map<String, String> newClasses) {
        classes = newClasses;
    }

    static Map<String, String> classes = new HashMap<>();

    static public Map<String, String> getNamespaces() {
        return namespaces;
    }

    static public void setNamespaces(Map<String, String> newNamespaces) {
        namespaces = newNamespaces;
    }

    static Map<String, String> namespaces = new HashMap<>();

    public static String renameClass(String className) {
        if (!isEnabled()) return className;
        if (getClasses().containsKey(className)) {
            return getClasses().get(className);
        }
        return className;
    }

    public static String renameNamespace(String namespace) {
        if (!isEnabled()) return namespace;
        if (getNamespaces().containsKey(namespace)) {
            return getNamespaces().get(namespace);
        }
        return namespace;
    }

    static public void init() {
         // Fix https://github.com/ncsa/qdl/issues/82
        HashMap<String, String> c = new HashMap<>();
        c.put("edu.uiuc.ncsa.qdl.extensions.inputLine.QDLCLIToolsModule", "org.qdl_lang.extensions.inputLine.QDLCLIToolsModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.convert.QDLConvertModule", "org.qdl_lang.extensions.convert.QDLConvertModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.crypto.CryptoModule", "org.qdl_lang.extensions.crypto.CryptoModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.database.QDLDBModule", "org.qdl_lang.extensions.database.QDLDBModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.dynamodb.QDLDynamoDBModule", "org.qdl_lang.extensions.dynamodb.QDLDynamoDBModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.example.EGModule", "org.qdl_lang.extensions.examples.basic.EGModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.http.QDLHTTPModule", "org.qdl_lang.extensions.http.QDLHTTPModule");
        c.put("edu.uiuc.ncsa.qdl.extensions.mail.QDLMailModule", "org.qdl_lang.extensions.mail.QDLMailModule");

        // Fix https://github.com/ncsa/oa4mp/issues/208 here since it is a lot easier.
        // At some point we just turn this all off by removing call to this method
        // that is in the WorkspaceCommands.init method.
        c.put("edu.uiuc.ncsa.myproxy.oa4mp.qdl.acl.ACModule", "org.oa4mp.server.loader.qdl.acl.ACModule");
        c.put("edu.uiuc.ncsa.myproxy.oa4mp.qdl.claims.ClaimsModule", "org.oa4mp.server.loader.qdl.claims.ClaimsModule");
        c.put("edu.uiuc.ncsa.myproxy.oa4mp.qdl.util.JWTModule", "org.oa4mp.server.loader.qdl.util.JWTModule");
        c.put("edu.uiuc.ncsa.oa2.qdl.CLCModule", "org.oa4mp.server.qdl.CLCModule");
        c.put("edu.uiuc.ncsa.oa2.qdl.CMModule", "org.oa4mp.server.qdl.CMModule");
        c.put("edu.uiuc.ncsa.oa2.qdl.storage.PStoreAccessModule", "org.oa4mp.server.qdl.storage.PStoreAccessModule");
        c.put("edu.uiuc.ncsa.oa2.qdl.storage.StoreAccessModule", "org.oa4mp.server.qdl.storage.StoreAccessModule");
        c.put("edu.uiuc.ncsa.oa2.qdl.testUtils.TestUtilModule", "org.oa4mp.server.qdl.testUtils.TestUtilModule");
        setClasses(c);

        HashMap<String, String> ns = new HashMap<>();
        ns.put("oa2:/qdl/acl", "oa4mp:/qdl/acl");
        ns.put("oa2:/qdl/jwt", "oa4mp:/qdl/jwt");
        ns.put("oa2:/qdl/oidc/claims", "oa4mp:/qdl/oidc/claims");
        ns.put("oa2:/qdl/oidc/client", "oa4mp:/qdl/oidc/client");
        ns.put("oa2:/qdl/oidc/client/manage", "oa4mp:/qdl/oidc/client/manage");
        ns.put("oa2:/qdl/oidc/test/util", "oa4mp:/qdl/oidc/test/util");
        ns.put("oa2:/qdl/oidc/token", "oa4mp:/qdl/oidc/token");
        ns.put("oa2:/qdl/p_store", "oa4mp:/qdl/p_store");
        ns.put("oa2:/qdl/store", "oa4mp:/qdl/store");
        setNamespaces(ns);
        setEnabled(true);
      /*
           cli : "edu.uiuc.ncsa.qdl.extensions.inputLine.QDLCLIToolsLoader",
       convert : "edu.uiuc.ncsa.qdl.extensions.convert.QDLConvertLoader",
        crypto : "edu.uiuc.ncsa.qdl.extensions.crypto.CryptoLoader",
            db : "edu.uiuc.ncsa.qdl.extensions.database.QDLDBLoader",
   description : "System tools for http, conversions and other very useful things.",
        dynamo : "edu.uiuc.ncsa.qdl.extensions.dynamodb.QDLDynamoDBLoader",
            eg : "edu.uiuc.ncsa.qdl.extensions.example.EGLoaderImpl",
          http : "edu.uiuc.ncsa.qdl.extensions.http.QDLHTTPLoader",
          mail : "edu.uiuc.ncsa.qdl.extensions.mail.QDLMailLoader",



           cli : "org.qdl_lang.extensions.inputLine.QDLCLIToolsLoader",
       convert : "org.qdl_lang.extensions.convert.QDLConvertLoader",
        crypto : "org.qdl_lang.extensions.crypto.CryptoLoader",
            db : "org.qdl_lang.extensions.database.QDLDBLoader",
   description : "System tools for http, conversions and other very useful things.",
        dynamo : "org.qdl_lang.extensions.dynamodb.QDLDynamoDBLoader",
            eg : "org.qdl_lang.extensions.examples.basic.EGLoader",
          http : "org.qdl_lang.extensions.http.QDLHTTPLoader",
          mail : "org.qdl_lang.extensions.mail.QDLMailLoader",

        acl : "org.oa4mp.server.loader.qdl.acl.ACLoader"
     claims : "org.oa4mp.server.loader.qdl.claims.ClaimsLoader"
        jwt : "org.oa4mp.server.loader.qdl.util.JWTLoader"
        clc : "org.oa4mp.server.qdl.CLCLoader"
         cm : "org.oa4mp.server.qdl.CMLoader"
    p_store : "org.oa4mp.server.qdl.storage.PStoreAccessLoader"
      store : "org.oa4mp.server.qdl.storage.StoreAccessLoader"
 test_utils : "org.oa4mp.server.qdl.testUtils.TestUtilModule"

          acl : "edu.uiuc.ncsa.myproxy.oa4mp.qdl.acl.ACLoader",
       claims : "edu.uiuc.ncsa.myproxy.oa4mp.qdl.claims.ClaimsLoader",
          jwt : "edu.uiuc.ncsa.myproxy.oa4mp.qdl.util.JWTLoader",
          clc : "edu.uiuc.ncsa.oa2.qdl.CLCLoader",
           cm : "edu.uiuc.ncsa.oa2.qdl.CMLoader",
      p_store : "edu.uiuc.ncsa.oa2.qdl.storage.PStoreAccessLoader",
        store : "edu.uiuc.ncsa.oa2.qdl.storage.StoreAccessLoader",
   test_utils : "edu.uiuc.ncsa.oa2.qdl.testUtils.TestUtilModule",

       */
    }
    public static void updateSerializedJSON(JSONObject json){
        if(!isEnabled()) return;
        if(json.containsKey(MODULE_NS_ATTR)){
            json.put(MODULE_NS_ATTR, renameNamespace(json.getString(MODULE_NS_ATTR)));
        }
        if(json.containsKey(MODULE_CLASS_NAME_TAG)){
            json.put(MODULE_CLASS_NAME_TAG, renameClass(json.getString(MODULE_CLASS_NAME_TAG)));
        }
    }
}
