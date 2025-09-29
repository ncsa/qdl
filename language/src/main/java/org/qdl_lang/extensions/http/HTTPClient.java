package org.qdl_lang.extensions.http;

import edu.uiuc.ncsa.security.core.util.DebugUtil;
import edu.uiuc.ncsa.security.servlet.HeaderUtils;
import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLMetaModule;
import org.qdl_lang.state.State;
import org.qdl_lang.util.QDLFileUtil;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.values.*;
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

import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

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
     * @param qdlValues
     * @return a valid get/delete string of host+uri_path+?key0=value0&amp;key1=value1...
     */
    protected String paramsToRequest(QDLValue[] qdlValues) throws UnsupportedEncodingException {
        String actualHost = host;
        QDLStem parameters = null;
        if (qdlValues.length == 2) {
            if (!(qdlValues[0].isString())) {
                throw new BadArgException("uri_path must be a string", 0);
            }
            actualHost = getActualHost(qdlValues[0].asString());
            parameters = qdlValues[1].asStem();
        }
        if (qdlValues.length == 0) {
            parameters = new QDLStem(); // empty
        }
        if (qdlValues.length == 1) {
            parameters = qdlValues[0].asStem();
        }
        // make the parameters.
        if (parameters == null) {
            return actualHost;
        }
        String p = parameters.size() == 0 ? "" : "?";
        boolean isFirst = true;
        for (QDLKey key : parameters.keySet()) {
            // Fix https://github.com/ncsa/qdl/issues/135
            QDLValue value = parameters.get(key);
            String v;
            switch (value.getType()) {
                case Constant.STEM_TYPE:
                    QDLStem stem = value.asStem();
                    if (stem.isList()) {
                        v = toParamList(key.toString(), stem.getQDLList().values());
                    } else {
                        throw new BadArgException("only lists or sets supported as multiple values, not stems", 0);
                    }
                    break;
                case Constant.SET_TYPE:
                    v = toParamList(key.toString(), value.asSet());
                    break;
                case Constant.FUNCTION_TYPE:
                case Constant.DYADIC_FUNCTION_TYPE:
                case Constant.AXIS_RESTRICTION_TYPE:
                case Constant.ALL_INDICES_TYPE:
                    throw new BadArgException("unsupported parameter value", 0);
                default:
                    v = key + "=" + URLEncoder.encode(value.toString(), "UTF-8");
            }
            if (isFirst) {
                p = p + v;
                isFirst = false;
            } else {
                // Always encode parameters or this bombs on even simple calls.
                p = p + "&" + v;
            }
        }
        return actualHost + p;
    }

    public String toParamList(String key, Collection<? extends QDLValue> values) throws UnsupportedEncodingException {
        String v = "";
        boolean first = true;
        if (values.size() == 1) {
            v = key + "=" + URLEncoder.encode(values.iterator().next().toString(), "UTF-8");
            return URLEncoder.encode(v, "UTF-8");
        }
        // Next encodes multiple values in the form key[]=v0&key[]=v2&...
        if (LIST_ENCODE_TYPE.equals(LIST_ENCODE_AS_VALUE)) {
            for (QDLValue vv : values) {
                if (first) {
                    v = key + "=" + URLEncoder.encode(vv.toString(), "UTF-8");
                    first = false;
                } else {
                    v = v + LIST_VALUE_SEPARATOR + URLEncoder.encode(vv.toString(), "UTF-8");
                }
            }
        } else {
            boolean asArray = LIST_ENCODE_TYPE.equals(LIST_ENCODE_AS_ARRAY);
            for (QDLValue vv : values) {
                if (first) {
                    v = key + (asArray ? "[]=" : "=") + URLEncoder.encode(vv.toString(), "UTF-8");
                    first = false;
                } else {
                    v = v + "&" + key + (asArray ? "[]=" : "=") + URLEncoder.encode(vv.toString(), "UTF-8");
                }
            }
        }

        return URLEncoder.encode(v, "UTF-8");
    }

    final protected String LIST_ENCODE_AS_ARRAY = "array";
    final protected String LIST_ENCODE_AS_PARAMETER = "parameter";
    final protected String LIST_ENCODE_AS_VALUE = "value";
    protected String LIST_ENCODE_TYPE = LIST_ENCODE_AS_ARRAY;
    protected String LIST_VALUE_SEPARATOR = ",";

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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            String oldHost = host;
            if (qdlValues.length == 1) {
                if (qdlValues[0].isString()) {
                    host = qdlValues[0].asString();
                } else {
                    throw new BadArgException("the argument to " + getName() + " must be a string, not a " + (qdlValues[0] == null ? "null" : qdlValues[0].getClass().getSimpleName()), 0);
                }
            }
            return asQDLValue(oldHost == null ? "" : oldHost);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            checkInit();
            String r = null;
            // Fix https://github.com/ncsa/qdl/issues/88
            QDLValue[] obj2 = qdlValues;
            if (qdlValues.length == 1) {
                if (qdlValues[0].isString()) {
                    obj2 = new QDLValue[]{qdlValues[0], new StemValue()};
                } else {
                    if (!(qdlValues[0].isStem())) {
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
            if (echoHttpRequest) {
                HeaderUtils.echoRequest(request);
            }
            try {
                CloseableHttpResponse response = httpClient.execute(request);
                return asQDLValue(getResponseStem(response));
            } catch (Throwable e) {
                if (DebugUtil.isTraceEnabled()) {
                    e.printStackTrace();
                }
                if (echoHttpResponse) {
                    HeaderUtils.echoErrorResponse(e);
                }
                throw e;
            }

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
     * Utility to turn the response, whatever it is, into a stem.
     *
     * @param response
     * @return
     */
    public QDLStem getResponseStem(HttpResponse response) throws IOException {
        QDLStem s = new QDLStem();
        QDLStem responseStem = new QDLStem();
        responseStem.put(from("code"), (long) response.getStatusLine().getStatusCode());
        if (!StringUtils.isTrivial(response.getStatusLine().getReasonPhrase())) {
            responseStem.put(from("message"), response.getStatusLine().getReasonPhrase());
        }
        s.put(from(STATUS_KEY), responseStem);
        HttpEntity entity = response.getEntity();
        QDLStem stemResponse = null;
        if (entity != null) {
            String rawResult = null;
            if (echoHttpResponse) {
                rawResult = HeaderUtils.echoResponse(entity, response.getStatusLine());
            } else {
                rawResult = EntityUtils.toString(entity);
            }
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

        s.put(from(CONTENT_KEY), stemResponse == null ? QDLNull.getInstance() : stemResponse);
        Header[] headers = response.getAllHeaders();
        QDLStem h = new QDLStem();
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            h.put(from(header.getName()), header.getValue());
        }
        if (!h.isEmpty()) {
            s.put(from(HEADERS_KEY), h);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            JSONObject oldHeaders = headers;
            if (qdlValues.length == 1) {
                if (qdlValues[0].isStem()) {
                    headers = (JSONObject) qdlValues[0].asStem().toJSON();
                } else {
                    throw new BadArgException(getName() + " requires a stem as its argument if present", 0);
                }
            }
            QDLStem stemVariable = new QDLStem();
            if (oldHeaders != null) {
                stemVariable.fromJSON(oldHeaders);
            }
            return asQDLValue(stemVariable);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException("could not close connection: '" + e.getMessage() + "'");
            }
            httpClient = null;
            return BooleanValue.True;
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            boolean doInsecure = false;
            if (qdlValues.length == 1) {
                if (!(qdlValues[0].isBoolean())) {
                    throw new BadArgException(getName() + " requires a boolean argument if present", 0);
                }
                doInsecure = qdlValues[0].asBoolean();
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
            return BooleanValue.True;
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
        public QDLValue evaluate(QDLValue[] objects, State state) {
            return new BooleanValue(httpClient != null);
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            return asQDLValue(doPostOrPut(qdlValues, state, true));
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> doxx = new ArrayList<>();
            switch (argCount) {
                case 1:
                    doxx.add(getName() + "(body | parameters.) - does an HTTP POST to the host with either the string as the body or encodes the parameters as the body.");
                    break;
                case 2:
                    doxx.add(getName() + "(uri_path , parameters.) - does an HTTP POST to the host with  the path and encodes the parameters as the body.");
                    break;
            }
            if (argCount == 2) {
                doxx.addAll(getURIPathBlurb());
            }
            doxx.add("If you send along a simple string, it will be treated as the entire body of the post.");
            doxx.add("Various content values and their uses are:");
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) {
            return doPostOrPut(qdlValues, state, false);
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
            doxx.add("See " + POST_METHOD + " for details of how payloads and content values are handled");
            return doxx;
        }
    }

    public QDLValue doPostOrPut(QDLValue[] qdlValues, State state, boolean isPost) {
        // if the type is form encoded, escape each element in the payload.
        // If JSON, send the payload as a JSON blob.
        String uriPath = "";
        QDLStem payload = null;
        String stringPayload = null;
        checkInit();
        switch (qdlValues.length) {
            case 1:
                if (qdlValues[0].isStem()) {
                    payload = qdlValues[0].asStem();
                } else {
                    if (qdlValues[0].isString()) {
                        stringPayload = qdlValues[0].asString();
                    } else {
                        throw new BadArgException("monadic " + (isPost ? POST_METHOD : PUT_METHOD) + " must have a stem or string as its argument", 0);
                    }
                }

                break;
            case 2:
                if (qdlValues[0].isString()) {
                    uriPath = qdlValues[0].asString();
                } else {
                    throw new BadArgException("dyadic " + (isPost ? POST_METHOD : PUT_METHOD) + " must have a string as it first argument", 0);
                }
                if (qdlValues[1].isStem()) {
                    payload = qdlValues[1].asStem();
                } else {
                    if (qdlValues[1].isString()) {
                        stringPayload = qdlValues[1].asString();
                    } else {
                        throw new BadArgException("dyadic " + (isPost ? POST_METHOD : PUT_METHOD) + " must have a stem or string as its second argument", 1);
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
             Typical content values look like
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
                    for (QDLKey key : payload.keySet()) {
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
            if (echoHttpRequest) {
                if (request instanceof HttpRequestBase) {
                    HeaderUtils.echoRequest((HttpRequestBase) request); // Only two options here are PUT or POST, so should always work
                }
            }
            CloseableHttpResponse response = httpClient.execute((HttpUriRequest) request);
            return asQDLValue(getResponseStem(response));
        } catch (ClientProtocolException e) {
            if (DebugUtil.isTraceEnabled()) {
                e.printStackTrace();
            }
            if (echoHttpResponse) {
                HeaderUtils.echoErrorResponse(e);
            }
            throw new IllegalStateException((isPost ? POST_METHOD : PUT_METHOD) + " protocol error:'" + e.getMessage() + "'");
        } catch (IOException e) {
            if (DebugUtil.isTraceEnabled()) {
                e.printStackTrace();
            }
            if (echoHttpResponse) {
                HeaderUtils.echoErrorResponse(e);
            }
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            checkInit();
            String r = paramsToRequest(qdlValues);
            HttpDelete request = new HttpDelete(r);
            if ((headers != null) && !headers.isEmpty()) {
                for (Object key : headers.keySet()) {
                    request.addHeader(key.toString(), headers.getString(key.toString()));
                }
            }
            if (echoHttpRequest) {
                HeaderUtils.echoRequest(request);
            }
            try {
                CloseableHttpResponse response = httpClient.execute(request);
                return asQDLValue(getResponseStem(response));
            } catch (Throwable t) {
                if (DebugUtil.isTraceEnabled()) {
                    t.printStackTrace();
                }
                if (echoHttpResponse) {
                    HeaderUtils.echoErrorResponse(t);
                }
                throw t;
            }
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            String username = URLEncoder.encode(qdlValues[0].asString(), "UTF-8");
            String password = URLEncoder.encode(qdlValues[1].asString(), "UTF-8");
            String raw = username + ":" + password;
            return asQDLValue(Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8)));
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException("argument must be a stem", 0);
            }
            QDLStem stem = qdlValues[0].asStem();

            if (stem.containsKey(HEADERS_KEY)) {
                stem = stem.getStem(HEADERS_KEY);
            }
            if (stem.containsKey("Content-Type")) {
                return asQDLValue(stem.getString("Content-Type").contains("application/json"));
            }
            throw new BadArgException("could not find content type", 0);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(resp.) - check if the response content type is JSON.");
            dd.add("You may supply either the whole response or just the headers");
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
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (!(qdlValues[0].isStem())) {
                throw new BadArgException("argument must be a stem", 0);
            }
            QDLStem stem = qdlValues[0].asStem();

            if (stem.containsKey(HEADERS_KEY)) {
                stem = stem.getStem(HEADERS_KEY);
            }
            if (stem.containsKey("Content-Type")) {
                String type = stem.getString("Content-Type");
                return new BooleanValue(type.contains("text") ||
                        type.contains("/xml") ||
                        type.contains("/java") ||
                        type.contains("/xhtml") ||
                        type.contains("/x-sh") ||
                        type.contains("/x-shellscript") ||
                        type.contains("/xhtml+xml") ||
                        type.contains("/javascript"));
            }
            throw new BadArgException("could not find content type", 0);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            dd.add(getName() + "(resp.) - check if the response content type is text.");
            dd.add("You may supply either the whole response or just the headers.");
            dd.add("This includes values like text, html, java, javascript etc.");
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
        public QDLValue evaluate(QDLValue[] objects, State state) throws Throwable {
            if (!objects[0].isString()) throw new BadArgException("zeroth argument must be a string", 0);
            if (!objects[1].isString()) throw new BadArgException("first argument must be a string", 1);
            boolean isArchive = false;
            boolean isTriadic  = objects.length == 3;
            if (isTriadic) {
                if (!(objects[2].isBoolean())) {
                    throw new BadArgException(getName() + " must have a boolean as its third argument if present", 2);
                }
                isArchive = objects[2].asBoolean();
            }
            String targetPath = objects[1].asString();
            if (QDLFileUtil.isVFSPath(targetPath)) {
                VFSFileProvider vfs = state.getVFS(objects[1].asString());
                if (!vfs.canWrite()) throw new IllegalAccessException("VFS is read only");
                if (vfs instanceof VFSPassThruFileProvider) {
                    VFSPassThruFileProvider vfsPassThruFileProvider = (VFSPassThruFileProvider) vfs;
                    targetPath = vfsPassThruFileProvider.getRealPath(targetPath);
                } else {
                    throw new BadArgException("can only download to a physical file", 1);
                }
            } else {
                if (state.isServerMode()) throw new IllegalAccessException("cannot download in server mode");
            }
            URL url = new URL(objects[0].asString());
            File targetFile = new File(targetPath);
            Long totalBytes = -1L;
            try {
                if (isArchive) {
                    totalBytes = downloadArchive(url, targetFile);
                } else {
                    if(isTriadic) {
                        // In this case, the target is a directory, but they want the archive downloaded to that.
                        String path = url.getPath();
                        String fileName = path.substring(1+path.lastIndexOf("/"));
                        targetFile = new File(targetPath, fileName);
                    }
                        totalBytes = download(url, targetFile);
                }
            } catch (IOException iox) {
                if (state.isDebugOn()) {
                    iox.printStackTrace();
                }
                return LongValue.MinusOne;
            }
            return new LongValue(totalBytes);
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

        protected Long downloadArchive(URL url, File targetDirectory) throws IOException {
            long totalSize = 0L;
            if (targetDirectory.exists()) {
                if (!targetDirectory.isDirectory()) {
                    throw new IllegalArgumentException("The target of the download for a zip file must be a directory");
                }
            } else {
                if (!targetDirectory.mkdirs()) {
                    throw new IllegalArgumentException("unable to create directory '" + targetDirectory.getAbsolutePath() + "\'");
                }
            }

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream dis = new DataInputStream(connection.getInputStream());
            String targetName = targetDirectory.getAbsolutePath();
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
                    d.add(getName() + "(url, target_directory, is_archive) - download from a site to a file");
                    d.add("Download a zipped file (includes jars) to a directory. This will unzip the");
                    d.add("entire archive to the location you specify; If you just want the unzipped");
                    d.add("file, use the dyadic version or set is_archive to false.");
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
        json.put(ECHO_HTTP_REQUEST, echoHttpRequest);
        json.put(ECHO_HTTP_RESPONSE, echoHttpResponse);
        json.put("encode", LIST_ENCODE_TYPE);
        json.put("separator", LIST_VALUE_SEPARATOR);
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
        if(json.containsKey("separator")) {
            LIST_VALUE_SEPARATOR = json.getString("separator");
        }
        if(json.containsKey("encode")) {
            LIST_ENCODE_TYPE = json.getString("encode");
        }
        if(json.containsKey(ECHO_HTTP_RESPONSE)) {
            echoHttpResponse = json.getBoolean(ECHO_HTTP_RESPONSE);
        }
        if(json.containsKey(ECHO_HTTP_REQUEST)) {
            echoHttpRequest = json.getBoolean(ECHO_HTTP_REQUEST);
        }

    }

    boolean echoHttpRequest = false;
    boolean echoHttpResponse = false;
    public static final String ECHO_HTTP_REQUEST = "echo_request";
    public static final String ECHO_HTTP_RESPONSE = "echo_response";

    public class EchoHTTPRequest implements QDLFunction {
        @Override
        public String getName() {
            return ECHO_HTTP_REQUEST;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 0) {
                return asQDLValue(echoHttpRequest);
            }
            if (!qdlValues[0].isBoolean()) {
                throw new IllegalArgumentException("The argument for " + getName() + " must be a boolean");
            }
            boolean oldValue = echoHttpRequest;
            echoHttpRequest = qdlValues[0].asBoolean();
            return asQDLValue(oldValue);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 0:
                    dd.add(getName() + "() - query if on (true) or off (false).");
                    break;
                case 1:
                    dd.add(getName() + "(boolean) - sets to on (true) or off (false).");
            }
            dd.add("This toggles or queries echoing the entire http request to standard out");
            dd.add("before sending it. It is intended as a low-level debugging aid.");
            dd.add("This may be very chatty so be warned.");
            dd.add("See also:" + ECHO_HTTP_RESPONSE);
            return dd;
        }
    }

    public class EchoHTTPResponse implements QDLFunction {
        @Override
        public String getName() {
            return ECHO_HTTP_RESPONSE;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {
            if (qdlValues.length == 0) {
                return asQDLValue(echoHttpResponse);
            }
            if (!qdlValues[0].isBoolean()) {
                throw new IllegalArgumentException("The argument for " + getName() + " must be a boolean");
            }
            boolean oldValue = echoHttpResponse;
            echoHttpResponse = qdlValues[0].asBoolean();
            return asQDLValue(oldValue);
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 0:
                    dd.add(getName() + "() - query if on (true) or off (false).");
                    break;
                case 1:
                    dd.add(getName() + "(boolean) - sets to on (true) or off (false).");
            }
            dd.add("This toggles or queries echoing the entire http response to standard out");
            dd.add("It is intended as a low-level debugging aid.");
            dd.add("This may be very chatty so be warned.");
            dd.add("See also:" + ECHO_HTTP_REQUEST);
            return dd;
        }
    }

    public static final String CONFIGURATION = "configure";

    public class Configuration implements QDLFunction {
        @Override
        public String getName() {
            return CONFIGURATION;
        }

        @Override
        public int[] getArgCount() {
            return new int[]{0, 1};
        }

        @Override
        public QDLValue evaluate(QDLValue[] qdlValues, State state) throws Throwable {

            QDLStem out = new QDLStem();
            if(echoHttpResponse == echoHttpRequest){
                out.put("echo", asQDLValue(echoHttpRequest));
            }else {
                QDLStem echos = new QDLStem();
                echos.put("request", asQDLValue(echoHttpRequest));
                echos.put("response", asQDLValue(echoHttpResponse));
                out.put("echo", asQDLValue(echos));
            }
            QDLStem lists = new QDLStem();
            lists.put("encode", asQDLValue(LIST_ENCODE_TYPE));
            lists.put("separator", asQDLValue(LIST_VALUE_SEPARATOR));
            out.put("list", asQDLValue(lists));
            if (qdlValues.length == 0) {
                return asQDLValue(out);
            }
            if (!qdlValues[0].isStem()) {
                throw new IllegalArgumentException("The argument for " + getName() + " must be a stem");
            }
            QDLStem stem = qdlValues[0].asStem();
            try {
                setConfigurationValues(stem);
            }catch(Throwable ex){
                // There was an error, rollback
                setConfigurationValues(out);
                throw ex;
            }
            return asQDLValue(out);
        }

        /**
         * Sets the configuration values from a stem.
         * @param stem
         */
        private void setConfigurationValues(QDLStem stem) {
            if (stem.containsKey("echo")) {
                QDLValue eValue = stem.get("echo");
                if (eValue.isBoolean()) {
                    echoHttpRequest = eValue.asBoolean();
                    echoHttpResponse = eValue.asBoolean();
                } else {
                    if (!eValue.isStem()) {
                        throw new IllegalArgumentException("The echo argument for " + getName() + " must be a stem");
                    }
                    QDLStem es = eValue.asStem();
                    if (es.containsKey("request")) {
                        QDLValue reqValue = es.get("request");
                        if (!reqValue.isBoolean()) {
                            throw new IllegalArgumentException("echo request argument for " + getName() + " must be a boolean");
                        }
                        echoHttpRequest = reqValue.asBoolean();
                    }
                    if (es.containsKey("response")) {
                        QDLValue reqValue = es.get("response");
                        if (!reqValue.isBoolean()) {
                            throw new IllegalArgumentException("echo response argument for " + getName() + " must be a boolean");
                        }
                        echoHttpResponse = reqValue.asBoolean();
                    }

                }

            }
            if(stem.containsKey("list")) {
                QDLValue listValue = stem.get("list");
                if(!listValue.isStem()) {
                    throw new IllegalArgumentException("list argument for " + getName() + " must be a stem");
                }
                QDLStem listStem = listValue.asStem();
                if(listStem.containsKey("encode")) {
                    QDLValue encodeValue = listStem.get("encode");
                    if(!encodeValue.isString()) {
                        throw new IllegalArgumentException("list encode argument for " + getName() + " must be a string");
                    }
                    switch(encodeValue.asString()) {
                        case "array":
                        case "parameter":
                        case "value":
                            LIST_ENCODE_TYPE = encodeValue.asString();
                            break;
                        default:
                            throw new IllegalArgumentException("unknown list encode argument for " + getName() + " \"" + encodeValue.asString() + "\"");
                    }
                }
                if(listStem.containsKey("separator")) {
                    QDLValue separatorValue = listStem.get("separator");
                    if(!separatorValue.isString()) {
                            throw new IllegalArgumentException("list separator argument for " + getName() + " must be a string");
                    }
                    LIST_VALUE_SEPARATOR = separatorValue.asString();
                }
            }
        }

        @Override
        public List<String> getDocumentation(int argCount) {
            List<String> dd = new ArrayList<>();
            switch (argCount) {
                case 0:
                    dd.add(getName() + "() - query current configuration. Returns a stem.");
                    break;
                case 1:
                    dd.add(getName() + "(arg.) - sets the current configuration.");
            }
            dd.add("The configuration stem has the following structure;");
            dd.add("key0  key1      value     ");
            dd.add("---------------------------");
            dd.add("echo  request   true|false");
            dd.add("      response  true|false");
            dd.add("                true|false");
            dd.add("list  encode    'array' | 'parameter' | 'value'");
            dd.add("list  separator ',' | other");
            dd.add("If the value of echo is a boolean, then both are toggled. Otherwise you specify");
            dd.add("which you want. You may also toggle them using the calls for " + ECHO_HTTP_REQUEST + " and " + ECHO_HTTP_RESPONSE + ".");
            dd.add("Encoding multiple values is a tricky thing since there is no standard. This configuration gives");
            dd.add("the major ways that some servers support it.");
            dd.add("array - means each value is ecoded as key[]=v0&key[]=v1&... ");
            dd.add("parameter - values are encoed without [], so key=v0&key=v1&...");
            dd.add("value - values are encoded as key=v0,v1,... ");
            dd.add("separator - if the encoding is 'value', this is what goes between the elements");
            dd.add("            Note that this should be a reserved subdelimiter character in the URL specfication");
            dd.add("            So one of !$&'()+,;= with a strong preference for ',' unless the target server has a");
            dd.add("            very specific requirement. If your server wants something else, you should encoded it first");
            return dd;
        }
    }

}
