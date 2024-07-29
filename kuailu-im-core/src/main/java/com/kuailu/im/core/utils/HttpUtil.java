package com.kuailu.im.core.utils;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.net.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
@Slf4j
public class HttpUtil {
    /**
     * http get请求
     *
     * @param url
     * @return
     */
   /* public static String httpGet(String url) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return httpGet(url, null, false);
    }*/

    /**
     * 、
     * https get 请求
     *
     * @param url
     * @return
     * @throws ClassNotFoundException
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
   /* public static String httpsGet(String url) throws ClassNotFoundException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return httpGet(url, null, true);
    }*/

    /**
     * http post 请求
     *
     * @param url
     * @param params
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
  /*  public static String httpPost(String url, HashMap<String, String> params) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return httpPost(url, params, null, false);
    }*/

    /**
     * https post 请求
     *
     * @param url
     * @param params
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
  /*  public static String httpsPost(String url, HashMap<String, String> params) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return httpPost(url, params, null, true);
    }*/

    /**
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: httpGet
     * @Description: TODO(http get请求)
     */
   /* public static String httpGet(String url, HashMap<String, Object> maps, boolean https) throws IOException, ClassNotFoundException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 创建HttpClient上下文
        HttpClientContext context = HttpClientContext.create();

        // 创建一个CloseableHttpClient
        CloseableHttpClient httpClient = null;
        if (https) {
            httpClient = getCloseableHttpClient();
        } else {
            httpClient = HttpClients.createDefault();
        }
        // get method
        HttpGet httpGet = new HttpGet(url);

        //设置header
        if (maps != null) {
            if (maps.containsKey(HttpHeaders.REFERER)) {
                httpGet.setHeader(HttpHeaders.REFERER, (String) maps.get(HttpHeaders.REFERER));
            }
            if (maps.containsKey(HttpHeaders.HOST)) {
                httpGet.setHeader(HttpHeaders.HOST, (String) maps.get(HttpHeaders.HOST));
            }
            if (maps.containsKey(HttpHeaders.USER_AGENT)) {
                httpGet.setHeader(HttpHeaders.USER_AGENT, (String) maps.get(HttpHeaders.USER_AGENT));
            }
            if (maps.containsKey(HttpHeaders.ACCEPT)) {
                httpGet.setHeader(HttpHeaders.ACCEPT, (String) maps.get(HttpHeaders.ACCEPT));
            }
        }

        //response
        CloseableHttpResponse response = null;
        //get response into String
        String result = "";

        response = httpClient.execute(httpGet, context);
        HttpEntity entity = response.getEntity();
        result = EntityUtils.toString(entity, "UTF-8");

        //释放连接
        httpGet.releaseConnection();
        httpClient.close();
        return result;
    }*/

    /**
     * 创建CloseableHttpClient
     *
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
   /* private static CloseableHttpClient getCloseableHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 全局请求设置
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setConnectionRequestTimeout(10000).setConnectTimeout(10000).setSocketTimeout(10000).build();

        //SSL 过滤
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates()).build();
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(200);

        // http 请求默认设置
        HttpClientBuilder custom = HttpClients.custom();
        custom.setDefaultRequestConfig(globalConfig);
        custom.setSSLSocketFactory(sslConnectionSocketFactory);
        custom.setConnectionManager(manager);
        custom.setConnectionManagerShared(true);
        return custom.build();
    }*/

    /**
     * SSL  https 构建
     *
     * @return
     */
   /* private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");//sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return socketFactory;
    }*/

   /* static class miTM implements TrustManager, X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }

    }*/


    /**
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: httpPost
     * @Description: TODO(http post请求)
     */
 /*   public static String httpPost(String url, HashMap<String, String> params, HashMap<String, Object> config, boolean https) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        //httpClient
        //HttpClient httpClient = getCloseableHttpClient();
        HttpClient httpClient = null;
        if (https) {
            httpClient = getCloseableHttpClient();
        } else {
            httpClient = HttpClients.createDefault();
        }

        // get method
        HttpPost httpPost = new HttpPost(url);

        //设置header
        if (config != null) {
            if (config.containsKey(HttpHeaders.REFERER)) {
                httpPost.setHeader(HttpHeaders.REFERER, (String) config.get(HttpHeaders.REFERER));
            }
            if (config.containsKey(HttpHeaders.HOST)) {
                httpPost.setHeader(HttpHeaders.HOST, (String) config.get(HttpHeaders.HOST));
            }
            if (config.containsKey(HttpHeaders.USER_AGENT)) {
                httpPost.setHeader(HttpHeaders.USER_AGENT, (String) config.get(HttpHeaders.USER_AGENT));
            }
            if (config.containsKey(HttpHeaders.ACCEPT)) {
                httpPost.setHeader(HttpHeaders.ACCEPT, (String) config.get(HttpHeaders.ACCEPT));
            }
            if (config.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpPost.setHeader(HttpHeaders.CONTENT_TYPE, (String) config.get(HttpHeaders.CONTENT_TYPE));
            }
            if (config.containsKey("Origin")) {
                httpPost.setHeader("Origin", (String) config.get("Origin"));
            }
        }

        //set params post参数
        List<NameValuePair> listParams = new ArrayList<NameValuePair>();

        //添加post参数
        for (Map.Entry<String, String> entry : params.entrySet()) {
            listParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        String result = "";
        try {
            //response
            httpPost.setEntity(new UrlEncodedFormEntity(listParams, "UTF-8"));
            HttpResponse response = null;
            response = httpClient.execute(httpPost);
            //get response into String

            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
            ((CloseableHttpClient) httpClient).close();
        }
        return result;
    }*/


    public static String doPostBody(String url, String paramString) {
//        log.info("PASS 接口开始.url:{},paramString:{}", url, paramString);
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        // 为httpPost设置封装好的请求参数
        try {
            httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            // 配置请求参数实例
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 设置连接主机服务超时时间
                    .setConnectionRequestTimeout(35000)// 设置连接请求超时时间
                    .setSocketTimeout(60000)// 设置读取数据连接超时时间
                    .build();
            // 为httpPost实例设置配置
            httpPost.setConfig(requestConfig);
            // 设置请求头
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(paramString, "UTF-8"));
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            log.error("调用pass的http接口异常", e);
        } catch (IOException e) {
            log.error("调用pass的http接口异常", e);
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    log.error("关闭http 接口异常", e);
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("关闭http 接口异常", e);
                }
            }
        }
        log.info("PASS 接口返回 result：{} ", result);
        return result;
    }

    public static String post(String url, String userId) {
        log.info("调用pass服务 url:{},userId:{}", url, userId);
        String resData = "";
        //发送请求的URL
        //编码格式
        String charset = "UTF-8";
        //请求内容
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        String content = jsonObject.toJSONString();
        //使用HttpClientBuilder创建CloseableHttpClient对象并设置代理.
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 设置连接主机服务超时时间
                .setConnectionRequestTimeout(35000)// 设置连接请求超时时间
                .setSocketTimeout(60000)// 设置读取数据连接超时时间
                .build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        //HTTP请求类型创建HttpPost实例
        HttpPost post = new HttpPost(url);
        //使用addHeader方法添加请求头部,诸如User-Agent, Accept-Encoding等参数.
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        // 组织数据
        StringEntity se = null;
        try {
            //设置编码格式
            se = new StringEntity(content, charset);
            //设置数据类型
            se.setContentType("application/json");
            //对于POST请求,把请求体填充进HttpPost实体.
            post.setEntity(se);
            //通过执行HttpPost请求获取CloseableHttpResponse实例 ,从此CloseableHttpResponse实例中获取状态码,错误信息,以及响应页面等等.
            CloseableHttpResponse response = client.execute(post);


            log.info("调用pass服务 返回response{}", response);
            //通过HttpResponse接口的getEntity方法返回响应信息，并进行相应的处理
            HttpEntity entity = response.getEntity();
            resData = EntityUtils.toString(entity);
            log.info(resData);
        } catch (Exception e) {
            log.error("调用pass的http接口异常,userId:{}", userId, e);
        } finally {
            //最后关闭HttpClient资源.
            try {
                client.close();
            } catch (IOException e) {
                log.error("关闭http 接口异常", e);
            }
        }
        log.info("调用pass服务 返回resData{}", resData);
        return resData;

    }


}
