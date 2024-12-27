package org.qdl_lang.extensions.http;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.vfs.VFSFileProvider;
import org.qdl_lang.vfs.VFSPassThruFileProvider;
import edu.uiuc.ncsa.security.core.exceptions.IllegalAccessException;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/**
 * Class that is the workhorse for {@link QDLHTTPModule}. See the blurb <br/>
 * /home/ncsa/dev/ncsa-git/qdl/language/src/main/docs/http-extension.odt
 * <p>Created by Jeff Gaynor<br>
 * on 10/5/21 at  7:02 AM
 */

/*
q := module_load('org.qdl_lang.extensions.http.QDLHTTPLoader','java') ;
     module_import(q) ;
       http#host('https://localhost:9443/oauth2/.well-known/openid-configuration/')
         http#host('https://localhost:9443/oauth2/.well-known/openid-confzzz')

       http#host('https://didact-patto.dev.umccr.org/api/visa') ;
       http#open();
        z. := http#get({'sub':'https://nagim.dev/p/wjaha-ppqrg-10000'});

  Ex. Doing a DB service (to CILogon) all to my local box with a self-signed cert.
      This approves the user with the user_code who has a pending flow

      q := module_load('org.qdl_lang.extensions.http.QDLHTTPLoader','java') ;
      module_import(q) ;
      http#host('https://localhost:9443/oauth2/dbService');
      http#open(true);
      http#get({'action':'userCodeApproved','approved':'1','user_code':'JJX-J6N-RJ6'})

{
 headers: {
  Transfer-Encoding:chunked,
  Server:Apache-Coyote/1.1,
  Date:Wed, 02 Mar 2022 12:16:18 GMT,
  Content-Type:oa4mp:form_encoding;charset=UTF-8
 },
 content: [status=0,client_id=localhost:test/df,grant=NB2HI4B2F4XWY33DMFWGQ33TOQ5DSNBUGMXW6YLVORUDELZXMI3GMYRSHFSGKNTEGBRWKOBWMQ4TSZJSMM3TAN3FHE3TEZBQGI7XI6LQMU6WC5LUNB5EO4TBNZ2CM5DTHUYTMNBWGIZDGMBRGI2TSNZGOZSXE43JN5XD25RSFYYCM3DJMZSXI2LNMU6TSMBQGAYDA,user_code=JJX-J6N-RJ6],
 status: {
  code:200,
  message:OK
 }
}

  Which returns a status of 0 (so all ok), the client_id and the current base 32 encoded grant.
  */
public class HTTPClient implements QDLMetaModule {
    transient CloseableHttpClient httpClient = null;
    String host = null;
    public String HOST_METHOD = "host";
    public String GET_METHOD = "get";
    JSONObject headers;
    public String HEADERS_METHOD = "headers";
    public String PUT_METHOD = "put";
    public String POST_METHOD = "post";
    public String DELETE_METHOD = "delete";
    public String CLOSE_METHOD = "close";
    public String OPEN_METHOD = "open";
    public String IS_OPEN_METHOD = "is_open";
    public String DOWNLOAD_METHOD = "download";
    public static final String CONTENT_FORM = "application/x-www-form-urlencoded";
    public static final int CONTENT_FORM_VALUE = 0;
    public static final String CONTENT_JSON = "application/json";
    public static final int CONTENT_JSON_VALUE = 1;
    public static final String CONTENT_HTML = "text/html";
    public static final int CONTENT_HTML_VALUE = 2;
    public static final String CONTENT_TEXT = "text/plain; charset=UTF-8";
    public static final int CONTENT_TEXT_VALUE = 3;
    public static final int CONTENT_TYPE_MISSING_VALUE = -1;

    public static final String CONTENT_KEY = "content";
    public static final String HEADERS_KEY = "headers";
    public static final String STATUS_KEY = "status";


    protected int getContentType(Set<String> contentType) {
        if (contentType.contains(CONTENT_FORM)) return CONTENT_FORM_VALUE;
        if (contentType.contains(CONTENT_JSON)) return CONTENT_JSON_VALUE;
        if (contentType.contains(CONTENT_HTML)) return CONTENT_HTML_VALUE;
        if (contentType.contains(CONTENT_TEXT)) return CONTENT_TEXT_VALUE;
        return CONTENT_TYPE_MISSING_VALUE;
    }

    protected void checkInit() {
        if (StringUtils.isTrivial(host)) {
            throw new IllegalStateException("you must set the host before doing a get");
        }
        if (httpClient == null) {
            throw new IllegalStateException("The connection has been closed. Please open a new one if you need to.");
        }
    }

    /**
     * Takes the array of objects for an evaluate method and creates the right url
     * This is used in get and delete. Options are
     * 0 args - return current host
     * 1 arg - stem, parameters
     * 2 args - uri path + stem of parameters
     *
     * @param objects
     * @return a valid get/delete string of host+uri_path+?key0=value0&amp;key1=value1...
     */
    protected String paramsToRequest(Object[] objects) throws UnsupportedEncodingException {
        String actualHost = host;
        QDLStem parameters = null;
        if (objects.length == 2) {
            if (!(objects[0] instanceof String)) {
                throw new BadArgException("uri_path must be a string", 0);
            }
            actualHost = getActualHost((String) objects[0]);
            parameters = (QDLStem) objects[1];
        }
        if (objects.length == 0) {
            parameters = new QDLStem(); // empty
        }
        if (objects.length == 1) {
            parameters = (QDLStem) objects[0];
        }
        // make the parameters.
        if (parameters == null) {
            return actualHost;
        }
        String p = parameters.size() == 0 ? "" : "?";
        boolean isFirst = true;
        for (Object key : parameters.keySet()) {
            String v = URLEncoder.encode(String.valueOf(parameters.get(key)), "UTF-8");
            if (isFirst) {
                p = p + key + "=" + v;
                isFirst = false;
            } else {
                // Always encode parameters or this bombs on even simple calls.
                p = p + "&" + key + "=" + v;
            }
        }
        return actualHost + p;
    }

    public class Host implements QDLFunction {
        @Override
        public String getName() {
            return HOST_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            String oldHost = host;
            if (objects.length == 1) {
                if (objects[0] instanceof String) {
                    host = (String) objects[0];
                } else {
                    throw new BadArgException("the argument to " + getName() + " must be a string, not a " + (objects[0] == null ? "null" : objects[0].getClass().getSimpleName()), 0);
                }
            }
            return oldHost == null ? "" : oldHost;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            return null;
        }
    }

    public class Get implements QDLFunction {
        @Override
        public String getName() {
            return GET_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            checkInit();
            String r = null;
            // Fix https://github.com/ncsa/qdl/issues/88
            Object[] obj2 = objects;
            if(objects.length == 1){
                if(objects[0] instanceof String){
                    obj2 = new Object[]{objects[0], new QDLStem()};
                }else{
                    if(!(objects[0] instanceof QDLStem)){
                        throw new BadArgException(getName() + " requires a stem if there is a single argument.", 0);
                    }
                }
            }
            r = paramsToRequest(obj2);
            HttpGet request = new HttpGet(r);
            if ((headers != null) && !headers.isEmpty()) {
                for (Object key : headers.keySet()) {
                    request.addHeader(key.toString(), headers.getString(key.toString()));
                }
            }
            CloseableHttpResponse response = httpClient.execute(request);
            return getResponseStem(response);
        }


        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 0:
                    doxx.add(getName() + "() - do an HTTP GET to the host with the current headers and no parameters.");
                    break;
                case 1:
                    doxx.add(getName() + "(uri_path | parameters.) - do an HTTP GET to the host with the current headers and use path ot the parameters.");
                    break;
                case 2:
                    doxx.add(getName() + "(uri_path, parameters.) - do an HTTP GET to host + uri_path with the current headers and use the parameters.");
                    break;
            }
            doxx.add("The two basic ways of accessing RESTful services are to have the uri overloaded or to send parameters (or both).");
            doxx.add("This function will do either of those. ");
            if (argCount == 2) {
                doxx.addAll(getURIPathBlurb());
            }
            doxx.add("E.g.");
            doxx.add("Let us say you needed to make a call to https://students.bsu.edu/user/123?format=json");
            doxx.add("In this case, you must supply the type of object ('user') and an identifier ('123') as part of the path");
            doxx.add("http#host('https://students.bsu.edu'); // sets up host with protocol");
            doxx.add("out. := http#get('user/123', {'format':'json'});");
            doxx.add("\nAlternately, if you only needed to make this call repeatedly (so never vary the user) you could set ");
            doxx.add("http#host:='https://students.bsu.edu/user/123'; // sets up host with protocol and user id");
            doxx.add("out. := http#get({'format':'json'});");
            doxx.add("\nThe response is always a 3 element stem with major keys");
            doxx.add("  status - the status of the response. out.status.code is the actual integer HTTP code");
            doxx.add("           This is always present.");
            doxx.add("  content - the actual content. This is a stem and may be either a list of lines.");
            doxx.add("            or be a stem if the response was JSON.");
            doxx.add("  headers - the headers in the response as a stem.");

            return doxx;
        }
    }

    /**
     * Utillity to turn the response, whatever it is, into a stem.
     *
     * @param response
     * @return
     */
    public QDLStem getResponseStem(HttpResponse response) throws IOException {
        QDLStem s = new QDLStem();
        QDLStem responseStem = new QDLStem();
        responseStem.put("code", (long) response.getStatusLine().getStatusCode());
        if (!StringUtils.isTrivial(response.getStatusLine().getReasonPhrase())) {
            responseStem.put("message", response.getStatusLine().getReasonPhrase());
        }
        s.put(STATUS_KEY, responseStem);
        HttpEntity entity = response.getEntity();
        QDLStem stemResponse = null;
        if (entity != null) {
            String rawResult = EntityUtils.toString(entity);
            if (!StringUtils.isTrivial(rawResult)) {
                if ((entity.getContentType() != null) && entity.getContentType().getValue().contains("application/json")) {
                    stemResponse = jsonToStemJSON(rawResult);
                } else {
                    // alternately, try to chunk it up
                    stemResponse = new QDLStem();
                    stemResponse.addList(StringUtils.stringToList(rawResult));
                }
            }
        }

        s.put(CONTENT_KEY, stemResponse == null ? QDLNull.getInstance() : stemResponse);
        Header[] headers = response.getAllHeaders();
        QDLStem h = new QDLStem();
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            h.put(header.getName(), header.getValue());
        }
        if (!h.isEmpty()) {
            s.put(HEADERS_KEY, h);
        }
        return s;
    }

    public class Headers implements QDLFunction {
        @Override
        public String getName() {
            return HEADERS_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            JSONObject oldHeaders = headers;
            if (objects.length == 1) {
                if (objects[0] instanceof QDLStem) {
                    headers = (JSONObject) ((QDLStem) objects[0]).toJSON();
                } else {
                    throw new BadArgException(getName() + " requires a stem as its argument if present", 0);
                }
            }
            QDLStem stemVariable = new QDLStem();
            if (oldHeaders != null) {
                stemVariable.fromJSON(oldHeaders);
            }
            return stemVariable;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 0:
                    doxx.add(getName() + "() - get current set of headers.");
                    break;
                case 1:
                    doxx.add(getName() + "(headers.) - Set the current headers. The previous headers are returned.");
            }
            doxx.add("The keys are the names of the headers, the value is its value");
            doxx.add("E.g.s of various random headers you can set");
            doxx.add("header.'Content-Type':= 'application/json;charset=UTF-8';");
            doxx.add("header.'Content-Type':= 'text/html;charset=UTF-8';");
            doxx.add("header.'Content-Type':= 'application/x-www-form-urlencoded';//mostly used in POST");
            doxx.add("header.'Authorization':= 'Bearer ' + bearer_token; // note the space!");
            doxx.add("header.'Authorization':= 'Basic ' + " + CREATE_CREDENTIALS_METHOD + "('bob','my_password'); // note the space"); // note the space");
            return doxx;
        }
    }

    protected QDLStem jsonToStemJSON(String rawJSON) {
        QDLStem stemVariable = new QDLStem();
        if (StringUtils.isTrivial(rawJSON)) {
            return stemVariable; // trivial response should be trivial JSON.
        }
        // So there is something there and it is asserted to be JSON.
        // Try to process it as such
        try {
            stemVariable.fromJSON(JSONObject.fromObject(rawJSON));
            return stemVariable;
        } catch (Throwable t) {

        }
        try {
            stemVariable.fromJSON(JSONArray.fromObject(rawJSON));
            return stemVariable;
        } catch (Throwable t) {
            throw new IllegalArgumentException("could not convert '" + rawJSON + "' to stem:" + t.getMessage());
        }
    }

    public class Close implements QDLFunction {
        @Override
        public String getName() {
            return CLOSE_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException("could not close connection: '" + e.getMessage() + "'");
            }
            httpClient = null;
            return true;

        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            doxx.add(getName() + "() - close the connection. You will not be able to do any operations until you call " + OPEN_METHOD);
            return doxx;
        }
    }

    public class Open implements QDLFunction {
        @Override
        public String getName() {
            return OPEN_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            boolean doInsecure = false;
            if (objects.length == 1) {
                if (!(objects[0] instanceof Boolean)) {
                    throw new BadArgException(getName() + " requires a boolean argument if present", 0);
                }
                doInsecure = (Boolean) objects[0];
            }
            if (httpClient != null) {
                throw new IllegalStateException("You must close the current connection before opening a new one.");
            }
            if (doInsecure) {
                try {
                    httpClient = createUnverified();
                } catch (Exception ex) {
                    throw new IllegalStateException("unable to create insecure http client: '" + ex.getMessage() + "'", ex);
                }
            } else {
                RequestConfig.Builder requestBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD);
                HttpClientBuilder clientbuilder = HttpClients.custom();
                clientbuilder.setDefaultRequestConfig(requestBuilder.build());
                httpClient = clientbuilder.build();
            }
            return true;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 0:
                    doxx.add(getName() + "() - opens a connection with standard ssl certs.");
                    doxx.add(" If the protocol is");
                    doxx.add("ssl, cert and hostname verification are done automatically.");
                    doxx.add("You should use this unless you have an explicit reason not to.");
                    break;
                case 1:
                    doxx.add(getName() + "(unverified) - if unverified is true will allow for connecting without SSL verfication");
                    doxx.add("Unless you have a very specific");
                    doxx.add("reason for this, such as you are testing a server with a self-signed cert, ");
                    doxx.add("you should not use this feature.");
                    doxx.add("This boosts neither speed nor performance and turns off essential security.");
                    break;
            }
            doxx.add("NOTE that if you serialize this workspace, you must re-open the connection on loading");
            return doxx;
        }

        /**
         * Internal method to create an SSL context that does no hostname verification or cert chain
         * checking. Mostly this is so people can debug servers they are writing before they get a real cert.
         * for it.
         *
         * @return
         * @throws KeyStoreException
         * @throws NoSuchAlgorithmException
         * @throws KeyManagementException
         */
        protected CloseableHttpClient createUnverified() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslsf)
                            .register("http", new PlainConnectionSocketFactory())
                            .build();
            RequestConfig.Builder requestBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD);
            HttpClientBuilder clientbuilder = HttpClients.custom();
            clientbuilder.setDefaultRequestConfig(requestBuilder.build());

            BasicHttpClientConnectionManager connectionManager =
                    new BasicHttpClientConnectionManager(socketFactoryRegistry);
            CloseableHttpClient httpClient = clientbuilder.setSSLSocketFactory(sslsf)
                    .setConnectionManager(connectionManager).build();
            return httpClient;
        }
    }

    public class IsOpen implements QDLFunction {
        @Override
        public String getName() {
            return IS_OPEN_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            return httpClient != null;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            doxx.add(getName() + "() - returns true if the connection is open, false otherwise.");
            return doxx;
        }
    }

    public class Post implements QDLFunction {
        @Override
        public String getName() {
            return POST_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            return doPostOrPut(objects, state, true);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 1:
                    break;
                case 2:
                    break;
            }
            doxx.add(getName() + "({uri_path,} string|stem.) do a post with the payload as a string or stem. ");
            if (argCount == 2) {
                doxx.addAll(getURIPathBlurb());
            }
            doxx.add("If you send along a simple string, it will be treated as the entire body of the post.");
            doxx.add("Various content types and their uses are:");
            doxx.add("(none)         \n  string only          \n   body of the post is the string, content type set to " + CONTENT_TEXT);
            doxx.add(CONTENT_TEXT + "\n  payload is a string. \n   body is the string");
            doxx.add(CONTENT_FORM + "\n  payload is a stem.   \n   body is form encoded key=value pairs. See eg. below");
            doxx.add(CONTENT_JSON + "\n  payload is a stem.   \n   body is the stem converted to JSON");
            doxx.add(CONTENT_HTML + "\n  payload is a string  \n   body is the string");
            doxx.add("anything else  \n  string only          \n   body is the string");
            doxx.add("E.g.");
            doxx.add("If you send the payload of {'a':'b', 'c':'d'} with content type '" + CONTENT_FORM + "',");
            doxx.add("the resulting post body would be \n");
            doxx.add("a=b&c=d");
            return doxx;
        }
    }

    protected List<String> getURIPathBlurb() {
        List<String> doc = new ArrayList<>();
        doc.add("uri_path is optional and will use whatever was set with the " + HOST_METHOD + " function.");
        doc.add("You may supply it and add query parameters to it, for instance.");
        return doc;
    }

    public class Put implements QDLFunction {
        @Override
        public String getName() {
            return PUT_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) {
            return doPostOrPut(objects, state, false);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 1:
                    doxx.add(getName() + "( string | payload.) do a put with the payload. ");
                    break;
                case 2:
                    doxx.add(getName() + "(uri_path, string | payload.) do a put using the uri_path and the payload. ");
                    doxx.addAll(getURIPathBlurb());
                    break;
            }
            doxx.add("Note that the payload will be the body of the post. If a string, the whole string is the body.");
            doxx.add("If you need to add authorization headers, set them in the header() function first.");
            doxx.add("See " + POST_METHOD + " for details of how payloads and content types are handled");
            return doxx;
        }
    }

    public Object doPostOrPut(Object[] objects, State state, boolean isPost) {
        // if the type is form encoded, escape each element in the payload.
        // If JSON, send the payload as a JSON blob.
        String uriPath = "";
        QDLStem payload = null;
        String stringPayload = null;
        checkInit();
        switch (objects.length) {
            case 1:
                if (objects[0] instanceof QDLStem) {
                    payload = (QDLStem) objects[0];
                } else {
                    if (objects[0] instanceof String) {
                        stringPayload = (String) objects[0];
                    } else {
                        throw new BadArgException("monadic " + (isPost ? POST_METHOD : PUT_METHOD) + " must have a stem or string as its argument", 0);
                    }
                }

                break;
            case 2:
                if (objects[0] instanceof String) {
                    uriPath = (String) objects[0];
                } else {
                    throw new BadArgException("dyadic " + (isPost ? POST_METHOD : PUT_METHOD) + " must have a string as it first argument",0);
                }
                if (objects[1] instanceof QDLStem) {
                    payload = (QDLStem) objects[1];
                } else {
                    if (objects[1] instanceof String) {
                        stringPayload = (String) objects[1];
                    } else {
                        throw new BadArgException("dyadic " + (isPost ? POST_METHOD : PUT_METHOD) + " must have a stem or string as its second argument",1);
                    }
                }
                break;
            default:
                throw new IllegalArgumentException((isPost ? POST_METHOD : PUT_METHOD) + " requires one or two arguments");
        }
        //Content-Type: text/plain; charset=UTF-8
        String contentType = "Content-Type";
        boolean isStringArg = stringPayload != null;
        String body = "";
        String actualHost = getActualHost(uriPath);
/*
        if (0 < uriPath.length()) {
            actualHost = actualHost + (actualHost.endsWith("/") ? "" : "/");
            // Fixes https://github.com/ncsa/qdl/issues/35
            actualHost = actualHost + (uriPath.startsWith("/") ? uriPath.substring(1) : uriPath);
        }
*/
        HttpEntityEnclosingRequest request;
        if (isPost) {
            request = new HttpPost(actualHost);
        } else {
            request = new HttpPut(actualHost);
        }
        HttpEntity httpEntity = null;

        if (headers.containsKey(contentType)) {
            /*
             Typical content types look like
              text/html; charset=utf-8
              application/json; charset=utf-8
              multipart/form-data; boundary=something
              So we have to pick them apart to see if there is anything useful.
             */
            StringTokenizer st = new StringTokenizer(headers.getString(contentType), ";");
            Set<String> contents = new HashSet<>();
            while (st.hasMoreTokens()) {
                contents.add(st.nextToken().trim());
            }
            switch (getContentType(contents)) {
                case CONTENT_FORM_VALUE:
                    if (isStringArg) {
                        throw new IllegalArgumentException("Cannot have " + contentType + " of " + CONTENT_FORM + " with a string. You must use a stem.");
                    }
                    boolean isFirst = true;
                    for (Object key : payload.keySet()) {
                        body = body + (isFirst ? "" : "&") + key + "=" + payload.get(key);
                        if (isFirst) isFirst = false;
                    }
                    break;
                case CONTENT_JSON_VALUE:
                    if (isStringArg) {
                        body = stringPayload;
                    } else {
                        body = payload.toJSON().toString();
                    }
                    break;
                case CONTENT_TEXT_VALUE:
                case CONTENT_HTML_VALUE:
                    if (!isStringArg) {
                        throw new IllegalArgumentException("Cannot have a stem argument for this content type.");
                    }
                    body = stringPayload;
                    break;
                case CONTENT_TYPE_MISSING_VALUE:
                    // There is a content-type header, but it does not have the text type in it
                    // (e.g. it's a mime type for mail).
                    // try to process as text.
                    if (StringUtils.isTrivial(stringPayload)) {
                        throw new IllegalArgumentException("Must specify " + contentType + " for payload stem.");
                    } else {
                        request.addHeader(contentType, CONTENT_TEXT);
                        body = stringPayload;
                    }
            }
        } else {
            if (StringUtils.isTrivial(stringPayload)) {
                throw new IllegalArgumentException("Must specify " + contentType + " for payload stem.");
            } else {
                request.addHeader(contentType, CONTENT_TEXT);
                body = stringPayload;
            }
        }
        try {
            httpEntity = new ByteArrayEntity(body.getBytes("UTF-8"));
        } catch (
                UnsupportedEncodingException unsupportedEncodingException) {
            throw new NFWException("UTF-8 is, apparently busted in Java:" + unsupportedEncodingException.getMessage());
        }
        request.setEntity(httpEntity);

        if ((headers != null) && !headers.isEmpty()) {
            for (Object key : headers.keySet()) {
                request.addHeader(key.toString(), headers.getString(key.toString()));
            }
        }
        try {
            CloseableHttpResponse response = httpClient.execute((HttpUriRequest) request);
            return getResponseStem(response);
        } catch (
                ClientProtocolException e) {
            throw new IllegalStateException((isPost ? POST_METHOD : PUT_METHOD) + " protocol error:'" + e.getMessage() + "'");
        } catch (
                IOException e) {
            throw new IllegalStateException((isPost ? POST_METHOD : PUT_METHOD) + " I/O error:'" + e.getMessage() + "'");
        }
    }

    public class Delete implements QDLFunction {
        @Override
        public String getName() {
            return DELETE_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1, 2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            checkInit();
            String r = paramsToRequest(objects);
            HttpDelete request = new HttpDelete(r);
            if ((headers != null) && !headers.isEmpty()) {
                for (Object key : headers.keySet()) {
                    request.addHeader(key.toString(), headers.getString(key.toString()));
                }
            }
            CloseableHttpResponse response = httpClient.execute(request);
            return getResponseStem(response);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 0:
                    doxx.add(getName() + "() - use only the current host ");
                    break;
                case 1:
                    doxx.add(getName() + "(parameters.) - use only the current host and append the parameters to the request uri ");
                    break;
                case 2:
                    doxx.add(getName() + "(uri_path, parameters.)  - use  current host + uri_path, then append the parameters to the request uri ");
                    break;
            }
            doxx.add("This uses the current host and headers.");
            doxx.add("This returns a response stem if the operation worked and throws an error if it did not.");
            return doxx;
        }
    }

    public static String CREATE_CREDENTIALS_METHOD = "credentials";

    public class CreateCredentials implements QDLFunction {
        @Override
        public String getName() {
            return CREATE_CREDENTIALS_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            String username = URLEncoder.encode(objects[0].toString(), "UTF-8");
            String password = URLEncoder.encode(objects[1].toString(), "UTF-8");
            String raw = username + ":" + password;
            return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            doxx.add(getName() + "(username, password) - create the correct credential for the basic auth header");
            doxx.add("The standard is to return encode_b64(url_escape(username) + ':' + url_escape(password))");
            return doxx;
        }
    }

    public static String IS_JSON = "is_json";

    public class IsJSON implements QDLFunction {
        @Override
        public String getName() {
            return IS_JSON;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof QDLStem)) {
                throw new BadArgException("argument must be a stem", 0);
            }
            QDLStem stem = (QDLStem) objects[0];

            if (stem.containsKey(HEADERS_KEY)) {
                stem = stem.getStem(HEADERS_KEY);
            }
            if (stem.containsKey("Content-Type")) {
                return stem.getString("Content-Type").contains("application/json");
            }
            throw new BadArgException("could not find content type", 0);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(resp.) - check if the response content is JSON.");
            dd.add("You may supply either the whole response or just the content part of it");
            return dd;
        }
    }

    public static String IS_TEXT = "is_text";

    public class IsText implements QDLFunction {
        @Override
        public String getName() {
            return IS_TEXT;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{1};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!(objects[0] instanceof QDLStem)) {
                throw new BadArgException("argument must be a stem", 0);
            }
            QDLStem stem = (QDLStem) objects[0];

            if (stem.containsKey(HEADERS_KEY)) {
                stem = stem.getStem(HEADERS_KEY);
            }
            if (stem.containsKey("Content-Type")) {
                String type = stem.getString("Content-Type");
                return type.contains("text") ||
                        type.contains("/xml") ||
                        type.contains("/java") ||
                        type.contains("/xhtml") ||
                        type.contains("/x-sh") ||
                        type.contains("/x-shellscript") ||
                        type.contains("/xhtml+xml") ||
                        type.contains("/javascript");
            }
            throw new BadArgException("could not find content type",0);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(resp.) - check if the response content is text.");
            dd.add("You may supply either the whole response or just the content part of it");
            dd.add("This includes types like text, html, java, javascript etc.");
            return dd;
        }
    }

    public class Download implements QDLFunction {
        @Override
        public String getName() {
            return DOWNLOAD_METHOD;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{2, 3};
        }

        @Override
        public Object evaluate(Object[] objects, State state) throws Throwable {
            if (!Constant.isString(objects[0])) throw new BadArgException("zeroth argument must be a string", 0);
            if (!Constant.isString(objects[1])) throw new BadArgException("first argument must be a string",1);
            boolean isZip = false;
            if (objects.length == 3) {
                if (!(objects[2] instanceof Boolean)) {
                    throw new BadArgException(getName() + " must have a boolean as its third argument if present",2);
                }
                isZip = (Boolean) objects[2];
            }
            String targetPath = (String) objects[1];
            if (QDLFileUtil.isVFSPath(targetPath)) {
                VFSFileProvider vfs = state.getVFS((String) objects[1]);
                if (!vfs.canWrite()) throw new IllegalAccessException("VFS is read only");
                if (vfs instanceof VFSPassThruFileProvider) {
                    VFSPassThruFileProvider vfsPassThruFileProvider = (VFSPassThruFileProvider) vfs;
                    targetPath = vfsPassThruFileProvider.getRealPath(targetPath);
                } else {
                    throw new BadArgException("can only download to a physical file",1);
                }
            } else {
                if (state.isServerMode()) throw new IllegalAccessException("cannot download in server mode");
            }
            URL url = new URL((String) objects[0]);
            File targetFile = new File(targetPath);
            Long totalBytes = -1L;
            try {
                if (isZip) {
                    totalBytes = downloadZip(url, targetFile);
                } else {
                    totalBytes = download(url, targetFile);
                }
            } catch (IOException iox) {
                if (state.isDebugOn()) {
                    iox.printStackTrace();
                }
                return -1L;
            }
            return totalBytes;
        }

        protected Long download(URL url, File targetFile) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream dis = new DataInputStream(connection.getInputStream());
            FileOutputStream fos = new FileOutputStream(targetFile);
            long totalBytes = QDLFileUtil.copyStream(dis, fos);
            fos.close();
            dis.close();
            return totalBytes;
        }

        protected Long downloadZip(URL url, File targetFile) throws IOException {
            long totalSize = 0L;
            if (targetFile.exists()) {
                if (!targetFile.isDirectory()) {
                    throw new IllegalArgumentException("The target of the download for a zip file must be a directory");
                }
            } else {
                if (!targetFile.mkdirs()) {
                    throw new IllegalArgumentException("unable to create directory '" + targetFile.getAbsolutePath() + "\'");
                }
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream dis = new DataInputStream(connection.getInputStream());
            String targetName = targetFile.getAbsolutePath();
            targetName = targetName.endsWith(File.separator) ? targetName : (targetName + File.separator);
            ZipInputStream stream = new ZipInputStream(dis);
            byte[] buffer = new byte[2048];
            try {
                ZipEntry entry;
                while ((entry = stream.getNextEntry()) != null) {
                    File outFile = new File(targetName + entry.getName());
                    if (entry.getName().endsWith("/")) {
                        outFile.mkdirs();
                        continue;
                    }
                    outFile.getParentFile().mkdirs();
                    FileOutputStream output = null;
                    try {
                        output = new FileOutputStream(outFile);
                        int len = 0;
                        while ((len = stream.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        // we must always close the output file
                        if (output != null) output.close();
                    }
                    totalSize = totalSize + outFile.length();

                }
            } finally {
                // we must always close the zip file.
                stream.close();
            }
            return totalSize;
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> d = new ArrayList<>();
            switch (argCount) {
                case 2:
                    d.add(getName() + "(url, target_file) - download from a site to a file");
                    d.add("Download a file from a site to a file.");
                    break;
                case 3:
                    d.add(getName() + "(url, target_file, is_zip) - download from a site to a file");
                    d.add("Download a zipped file (includes jars) directory. This will unzip the");
                    d.add("entire archive to the location you specify; If you just want the unzipped");
                    d.add("file, use the dyadic version or set is_zip to false.");
                    d.add("If the target directory does not exist, it will be created.");
                    break;
            }
            d.add("Note that this restricts the target file to a  physical file on your system,");
            d.add("which includes VFS files. The reason is that many VFS's are not writeable reliably");
            d.add("for large amounts of data.");
            d.add("This returns the total number of bytes downloaded, or -1 if the operation failed.");
            return d;
        }
    }

    /**
     * Given a uriPath, return the actual path to the service. This does the nitpicky things
     * to create the path.
     *
     * @param uriPath
     * @return
     */
    protected String getActualHost(String uriPath) {
        if (StringUtils.isTrivial(uriPath)) {
            return host;
        }
        String actualHost = host;
        actualHost = actualHost + (actualHost.endsWith("/") ? "" : "/");
        // Fixes https://github.com/ncsa/qdl/issues/35
        return actualHost + (uriPath.startsWith("/") ? uriPath.substring(1) : uriPath);
    }

    @Override
    public JSONObject serializeToJSON() {
        JSONObject json = new JSONObject();
        if (!StringUtils.isTrivial(host)) {
            json.put("host", host);
        }
        if (headers != null) {
            json.put("headers", headers);
        }
        return json;
    }

    @Override
    public void deserializeFromJSON(JSONObject json) {
        if (json.containsKey("host")) {
            host = json.getString("host");
        }
        if (json.containsKey("headers")) {
            headers = json.getJSONObject("headers");
        }
    }
}
