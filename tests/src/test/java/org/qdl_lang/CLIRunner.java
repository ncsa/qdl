package org.qdl_lang;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  10:35 AM
 */
public class CLIRunner {
    // This just exists to run a few tests/debug stuff from the command line
    public static void main(String[] args) {
        try {
            JSONObject jsonObject = JSONObject.fromObject(memberOf);
            System.out.println(jsonObject.toString());
          //  testReadChar();
            // JSON Path work ok except some dependency named slfj seems to blow up. Not sure why.
            // Might not be stable enough to use...
            String configPath = "$.config[*]"; // read all lines from the config array
            // Next path specifically looks at one element in the sourceConfig.
            String claimsPath = "$.claims.sourceConfig[0].ldap.preProcessing.script[*]";
            DocumentContext jsonContext = JsonPath.parse(jsonCfg);
            List<String> x = jsonContext.read(configPath);
            List<String> script = jsonContext.read(claimsPath);
            System.out.println(x);
            for (String y : script) {
                System.out.println(y);
            }

        } catch (Throwable e
        ) {
            e.printStackTrace();
        }
    }
        static String memberOf = "{\"isMemberOf\":  [\n" +
                "    {\n" +
                "   \"name\": \"org_ici\",\n" +
                "   \"id\": 1282\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"list_apcs\",\n" +
                "   \"id\": 1898\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"all_ncsa_employe\",\n" +
                "   \"id\": 1097\n" +
                "  },\n" +
                "  {\"name\": \"jira-users\"},\n" +
                "    {\n" +
                "   \"name\": \"lsst_users\",\n" +
                "   \"id\": 1363\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"iam_sec_testing\",\n" +
                "   \"id\": 1342\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"list_staffsanscenaffl\",\n" +
                "   \"id\": 1931\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"lsst_int_lspdev\",\n" +
                "   \"id\": 1618\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"org_csd\",\n" +
                "   \"id\": 1288\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"all_lsst\",\n" +
                "   \"id\": 1616\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"grp_bldg_ncsa\",\n" +
                "   \"id\": 1166\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"list_staff\",\n" +
                "   \"id\": 1895\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"prj_cerb_users\",\n" +
                "   \"id\": 1301\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"opnsrc_users\",\n" +
                "   \"id\": 1603\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"all_building\",\n" +
                "   \"id\": 1469\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"org_cisr\",\n" +
                "   \"id\": 1213\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"grp_bldg_both\",\n" +
                "   \"id\": 1168\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"cilogon_help\",\n" +
                "   \"id\": 1683\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"org_all_groups\",\n" +
                "   \"id\": 1032\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"grp_jira_users\",\n" +
                "   \"id\": 1103\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"all_users\",\n" +
                "   \"id\": 1002\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"lsst_reporting\",\n" +
                "   \"id\": 1605\n" +
                "  },\n" +
                "    {\n" +
                "   \"name\": \"list_allncsa\",\n" +
                "   \"id\": 1892\n" +
                "  }\n" +
                " ]}";
    static Scanner scanner = new Scanner(System.in,"UTF-8").useDelimiter("\\A");
    static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\p{javaWhitespace}+");
    static final Pattern EMPTY_PATTERN = Pattern.compile("\\^");

    protected static int readChar() {
        // me trying to read a single char. This still requires the user hit return after each character
        // but it will send back a character at a time.
        //scanner.useDelimiter(EMPTY_PATTERN);
        scanner.useDelimiter(EMPTY_PATTERN);
        String ch = scanner.next();
        scanner.useDelimiter(WHITESPACE_PATTERN);
        return ch.charAt(0);
    }
    protected static void testReadChar() throws Throwable{
        int x=0;
        while(true){
            x = readChar();
            System.out.print("x=" + x );
        }
    }


    static String jsonCfg = "{\"config\":  [ \"Configuration that contains new scripting notation 2/25/2019\", \"This is currently deployed on production for LSST clients to link users, setting voPersonExternalID\", \"then searching ldap-test for this value.\"]," +
            "\"claims\":  { " +
            "\"sourceConfig\": [" +
            "{\"ldap\":   " +
            "{  \"preProcessing\":    " +
            "{   \"script\":     [    \"# Set some variables to keep the verbosity down. These are mostly the IDPs.\",    \"# Note that this must run only before the first LDAP query.\",    \"setEnv('vo','voPersonExternalID');\",    \"setEnv('github','http://github.com/login/oauth/authorize');\",    \"setEnv('google','http://google.com/accounts/o8/id');\",    \"setEnv('orcid','http://orcid.org/oauth/authorize');\",    \"setEnv('ncsa','https://idp.ncsa.illinois.edu/idp/shibboleth');\",    \"#  Now figure out which IDP was used and set voPersonExternalID so it may be searched for.\",    \"xor{\",    \"    if[equals(get('idp'),'${github}')]then[set('${vo}',concat(get('oidc'),'@github.com'))],\",    \"    if[equals(get('idp'),'${google}')]then[set('${vo}',concat(get('oidc'),'@accounts.google.com'))],\",    \"    if[equals(get('idp'),'${orcid}')]then[set('${vo}',replace(get('oidc'),'http://','https://'))],\",    \"    if[hasClaim('eppn')]then[set('${vo}',get('eppn'))],\",    \"    if[hasClaim('eptid')]then[set('${vo}',get('eptid'))]\",    \"};\"   ],   \"version\": \"1.0\"  },  \"failOnError\": \"true\",  \"address\": \"ldap.ncsa.illinois.edu\",  \"port\": 636,  \"enabled\": \"true\",  \"authorizationType\": \"none\",  \"searchName\": \"voPersonExternalID\",  \"searchFilterAttribute\": \"voPersonExternalID\",  \"searchAttributes\": [   {   \"name\": \"uid\",   \"returnAsList\": false,   \"returnName\": \"uid\"  }],  \"searchBase\": \"ou=People,dc=ncsa,dc=illinois,dc=edu\",  \"contextName\": \"\",  \"ssl\":    {   \"tlsVersion\": \"TLS\",   \"useJavaTrustStore\": true  },  \"id\": \"3258ed63b62d1a78\" }}], \"preProcessing\": {\"script\":   [  \"setEnv('vo','voPersonExternalID');\",  \"setEnv('ncsa','https://idp.ncsa.illinois.edu/idp/shibboleth');\",  \"# if the IDP is NCSA, just set the uid and do the second LDAP search, otherwise, search first on voPersonExternalID to set it.\",  \"if[\",  \"equals(get('idp'),'${ncsa}')\",  \"]then[\",  \"set('uid',drop('@ncsa.illinois.edu',get('eppn'))),\",  \"set('${vo}',get('eppn'))\",  \"]else[\",  \"set_claim_source('LDAP','3258ed63b62d1a78')\",  \"];\",  \"set_claim_source('ncsa-default','uid');\" ]}},\"isSaved\": true}";
}
