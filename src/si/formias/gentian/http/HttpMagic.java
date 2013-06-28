package si.formias.gentian.http;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SSLSocketFactory;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;

import org.apache.http.entity.mime.MultipartEntity;

import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/** HTTP transmission layer using <a href="http://hc.apache.org/">HttpClient</a> library
 * <br/><br/>Needs <ul>
 * <li><a href="http://hc.apache.org/downloads.cgi">HttpClient</a></li>
 * <li><a href="http://hc.apache.org/downloads.cgi">HttpCore</a></li>
 * <li><a href="http://james.apache.org/download.cgi#Apache_Mime4J">Mime4J</a></li></ul>
 */
public class HttpMagic {

    private HttpClient client;
    private String charset;
    String cookie;

    /** Creates a new instance of HttpMagic
     *
     * @param charSet charset to be used (for example "UTF-8")
     */
    public HttpMagic(String charSet) {
    	
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (X11; U; Linux x86_64; sl; rv:1.9.2.8) Gecko/20100723 Ubuntu/10.04 (lucid) Firefox/3.6.8");

        ConnManagerParams.setMaxTotalConnections(params, 200);
        // Increase default max connection per route to 20
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        HttpProtocolParams.setHttpElementCharset(params, "UTF-8");

        //final SchemeRegistry registry = new SchemeRegistry();
        //registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        //ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);
        HttpClientParams.setCookiePolicy(params, org.apache.http.client.params.CookiePolicy.BEST_MATCH);
        client = new DefaultHttpClient(params);

        client.getParams().setParameter(
                ClientPNames.COOKIE_POLICY, CookiePolicy.RFC_2965);




        /*/*HttpParams params = new BasicHttpParams();
        params.setParameter(
        "http.useragent",
        "Mozilla/5.0 (Linux; U; Android 1.1; en-us;dream) AppleWebKit/525.10+ (KHTML, like Gecko) Version/3.0.4 Mobile Safari/523.12.2");
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "UTF-8");
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        //SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        //registry.register(new Scheme("https", (SocketFactory) sslSocketFactory, 443));


        final ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params,
        registry);
        client = new DefaultHttpClient(manager, params);
         */
        client = new DefaultHttpClient();






        charset = charSet;



    }

    /** Creates POST data
     *
     * @param formData form variables as map:key->value
     * @return POST query
     */
    public NameValuePair[] getPostData(Map<String, String> formData) {

        NameValuePair[] data = null;
        try {
            if (formData != null) {

                Set<String> formDataKeys = formData.keySet();
                data = new NameValuePair[formDataKeys.size()];
                int i = 0;
                for (String s : formDataKeys) {
                    data[i] = new BasicNameValuePair(s, formData.get(s));
                    i++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**Opens multipart POST connection to given form url, sends paramaters and returns response as stream. See <a href="ProgressPartSource.html">ProgressPartSource</a>, <a href="getParts(java.util.Map,%20java.util.List)">getParts(Map,List)</a>
     *
     * @param url form url
     * @param body request body - post parameters - use <a href="#getParts(java.util.Map,%20java.util.List)">getParts(Map,List)</a>
     * @return response body as input stream
     * @throws IOException Network error

     */
    /*public HttpEntity postURLMultiPart(String url, MultipartEntity body) throws IOException {

        HttpPost httppost = new HttpPost(url);

        httppost.setEntity(body);
        return client.execute(httppost).getEntity();




    }*/

    /** Creates multipart POST data . To be used as argument of <a href="#postURLMultiPart(java.lang.String,%20org.apache.commons.httpclient.methods.multipart.Part[])">postURLMultiPart(java.lang.String,%20org.apache.commons.httpclient.methods.multipart.Part[])</a>
     *
     *
     * @param textData text form data
     * @param binaryData binary attachments
     * @return multipart POST data
     */
    /*public MultipartEntity getParts(Map<String, String> textData, List<ProgressPartSource> binaryData) throws UnsupportedEncodingException, FileNotFoundException {
        NameValuePair[] textpairs = getPostData(textData);
        MultipartEntity reqEntity = new MultipartEntity();


        for (String key : textData.keySet()) {


            reqEntity.addPart(key, new StringBody(textData.get(key)));
            if (cookie != null) {
                reqEntity.addPart("cookie", new StringBody(cookie));
            }
        }





        int i = 0;




        if (binaryData != null) {
            for (ProgressPartSource part : binaryData) {
                part.addTo(reqEntity);
            }
        }
        return reqEntity;
    }*/

    final HttpHost host=new HttpHost("localhost",	8118,"http");
    /** Opens POST connection to given form url, sends paramaters and returns response as stream
     *
     * @param url form url
     * @param params post parameters - use <a href="#getPostData(java.util.Map)">getPostData(java.util.Map)</a> to prepare.
     * @return response body as input stream
     * @throws IOException Network error
     */
    public HttpEntity postURL(String url, NameValuePair[] params, String referer) throws IOException {



        HttpPost httppost = new HttpPost(url);
       
        ConnRouteParams.setDefaultProxy(httppost.getParams(), host);
       
        
        if (referer != null) {
            httppost.addHeader("Referer", referer);
        }
        List<NameValuePair> list = Arrays.asList(params);

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
        httppost.setEntity(entity);
        //System.out.println("Posting to " + url + ", Request " + entity.getContentType() + " length " + entity.getContentLength());
        HttpResponse response = client.execute(httppost);
        HttpEntity responseEntity = response.getEntity();
   //     System.out.println("Response length: " + responseEntity.getContentLength() + " Type " + responseEntity.getContentType());

        return responseEntity;
    }

    public String fetchURL(String url, NameValuePair[] params) throws UnsupportedEncodingException, IOException {
        HttpEntity entity = postURL(url, params,null);
        InputStreamReader r = new InputStreamReader(entity.getContent(), "UTF-8");
        char[] buffer = new char[4096];
        int number;
        StringBuilder sb = new StringBuilder();
        while ((number = r.read(buffer)) != -1) {
            sb.append(buffer, 0, number);
        }
        entity.consumeContent();
        return sb.toString();
    }
}
