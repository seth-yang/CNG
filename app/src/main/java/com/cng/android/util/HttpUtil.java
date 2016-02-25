package com.cng.android.util;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.cng.android.CNG.D;

/**
 * Created by game on 2016/2/26
 */
public class HttpUtil {
    private static ClientConnectionManager manager;
    private static HttpParams params = new BasicHttpParams ();

    private static final String TAG = "cng.HttpUtil";

    static {
        SchemeRegistry registry = new SchemeRegistry ();
        registry.register (new Scheme ("http", PlainSocketFactory.getSocketFactory (), 80));
        registry.register (new Scheme ("https", SSLSocketFactory.getSocketFactory (), 443));
        params = new BasicHttpParams ();
        params.setParameter (CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
        params.setParameter (CoreConnectionPNames.SO_TIMEOUT, 30000);
        HttpConnectionParams.setConnectionTimeout (params, 30000);
        HttpConnectionParams.setSoTimeout (params, 30000);
        manager = new ThreadSafeClientConnManager (params, registry);
    }

    public static String get (String url) throws IOException {
        if (D)
            Log.v (TAG, "HTTP GET URL: " + url);
        HttpGet http = new HttpGet (url);
        HttpClient client = new DefaultHttpClient (manager, params);
        try {
            HttpResponse response = client.execute (http);
            StatusLine statusLine = response.getStatusLine ();
            if (statusLine.getStatusCode () != 200) {
                throw new IOException ("HTTP GET fail: " + statusLine.getReasonPhrase ());
            } else {
                return EntityUtils.toString (response.getEntity (), "utf-8");
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        } finally {
            http.abort ();
            manager.closeExpiredConnections ();
            manager.closeIdleConnections (10, TimeUnit.SECONDS);
            if (D)
                Log.d (TAG, "cleanup http client connections");
        }
    }

    public static Map<String, Object> getMap (String url) throws IOException {
        String expression = get (url);
        Gson g = new Gson ();
        return g.fromJson (expression, GsonHelper.TYPE_MAP_STRING_OBJECT);
    }

    public static String post (String url, String content) throws IOException {
        // 创建HttpPost对象。
        HttpPost post = new HttpPost (url);
        try {
            post.setEntity (new StringEntity (content, "utf-8"));
            // 发送POST请求
            HttpClient client = new DefaultHttpClient (manager, HttpUtil.params);
            client.getParams ().setParameter (CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
            client.getParams ().setParameter (CoreConnectionPNames.SO_TIMEOUT, 30000);
            HttpResponse httpResponse = client.execute (post);
            // 如果服务器成功地返回响应
            if (httpResponse.getStatusLine ().getStatusCode () == 200) {
                // 获取服务器响应字符串
                String result = EntityUtils.toString (httpResponse.getEntity ());
                dispose ();
                return result;
            }
            return null;
        } finally {
            post.abort ();
        }
    }

    public static String post (String url, Map<String, String> rawParams) throws IOException {
        Gson g = GsonHelper.getGson ();
        return post (url, g.toJson (rawParams));
    }

    public static<T> T post (String url, String content, Type typeOfT) throws IOException {
        Gson g = GsonHelper.getGson ();
        String result = post (url, content);
        return g.fromJson (result, typeOfT);
    }

    public static Map<String, Object> postMap (String url, String content) throws IOException {
        String result = post (url, content);
        Gson g = GsonHelper.getGson ();
        return g.fromJson (result, GsonHelper.TYPE_MAP_STRING_OBJECT);
    }

    public static Map<String, Objects> postMap (String url, Map<String, String> rawParams) throws IOException {
        Gson g = GsonHelper.getGson ();
        String result = post (url, rawParams);
        return g.fromJson (result, GsonHelper.TYPE_MAP_STRING_OBJECT);
    }

    public static void dispose () {
        manager.closeExpiredConnections ();
        manager.closeIdleConnections (30, TimeUnit.SECONDS);
        if (D)
            Log.d (TAG, "cleanup http client connections");
    }

    public static HttpClient getHttpClient () {
        return new DefaultHttpClient (manager, params);
    }
}