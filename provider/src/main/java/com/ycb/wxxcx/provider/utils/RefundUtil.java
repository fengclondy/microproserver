package com.ycb.wxxcx.provider.utils;

import com.ycb.wxxcx.provider.constant.GlobalConfig;
import com.ycb.wxxcx.provider.utils.http.HttpClientConnectionManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;


public class RefundUtil {

    public static final Logger logger = LoggerFactory.getLogger(RefundUtil.class);

    public static CloseableHttpClient httpclient;

    static {
        httpclient = HttpClients.createDefault();
        //httpclient = HttpClientConnectionManager.getSSLInstance(httpclient);
    }

    public static Map<String, Object> getPreOrder(String url, String xmlParam) {
        //DefaultHttpClient client = new DefaultHttpClient();
        //httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
        try {
            httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
            HttpResponse response = httpclient.execute(httpost);
            String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(jsonStr);
            Map<String, Object> map = XmlUtil.doXMLParse(jsonStr);
            return map;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String, Object> forRefund(String url, String xmlParam, String mch_id) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException {
        Map doXMLtoMap = new HashMap();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        String P12_PASSWORD = mch_id;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(GlobalConfig.APICLIENT_CERT_P12);
        try {
            keyStore.load(inputStream, P12_PASSWORD.toCharArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, P12_PASSWORD.toCharArray()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        //DefaultHttpClient client = new DefaultHttpClient();
        //client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
        try {
            httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
            HttpResponse response = httpclient.execute(httpost);
            String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            if (jsonStr.indexOf("FAIL") >= 0) {
                logger.error("提现请求失败:"+jsonStr);
                return null;
            }
            doXMLtoMap = XmlUtil.doXMLParse(jsonStr);
            return doXMLtoMap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpclient.close();
        }
        return null;
    }

    public static InputStream String2Inputstream(String str) {
        return new ByteArrayInputStream(str.getBytes());
    }

    /*public Map<String, String> getOrderquery(String url, String xml) {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        HttpPost httpost = HttpClientConnectionManager.getPostMethod(url);
        try {
            httpost.setEntity(new StringEntity(xml, "UTF-8"));
            HttpResponse response = httpclient.execute(httpost);
            String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            System.out.println(jsonStr);
            // Map<String, String> map = doXMLParse(jsonStr);
            return null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }*/
}