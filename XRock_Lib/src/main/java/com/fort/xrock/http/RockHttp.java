package com.fort.xrock.http;

import android.util.Log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Mac on 16/8/29.
 * 对OkHttp的封装
 */
public class RockHttp {
    final String TAG = "RockHttp";
    OkHttpClient okHttpClient;
    ThreadPoolExecutor threadPoolExecutor;

    /**
     * 初始化
     *
     * @param threadPoolExecutor
     */
    private void initOkHttp(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(20, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        builder.addInterceptor(new OKLogInterceptor());
//        builder.addNetworkInterceptor()
        setHttpSSL(builder);
        okHttpClient = builder.build();
    }

    private void setHttpSSL(OkHttpClient.Builder builder) {
        X509TrustManager xm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[0];
                return x509Certificates;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{xm}, new SecureRandom());
            builder.sslSocketFactory(sslContext.getSocketFactory(), xm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    public void doRequest(RockRequest request) {
        doRequest(request.url, request.formMap, request.multiMap, request.headMap, request.callback, request.httpTag);
    }

    public void doRequest(String url, Map<String, String> body, RockUICallback callback, Object tag) {
        doRequest(url, body, null, null, callback, tag);
    }

    public void doRequest(String url, Map<String, String> body, Map<String, Object> multi, Map<String, String>
            header, Callback callback, Object tag) {
        Request okrequest = createOKRequest(url, body, multi, header, tag);
        okHttpClient.newCall(okrequest).enqueue(callback);
    }

    public String doRequestSync(RockRequest request) {
        return doRequestSync(request.url, request.formMap, request.multiMap, request.headMap, request.httpTag);
    }

    public String doRequestSync(String url, Map<String, String> body, Object tag) {
        return doRequestSync(url, body, null, null, tag);
    }

    public String doRequestSync(String url, Map<String, String> body, Map<String, Object> multi, Map<String, String>
            header, Object tag) {
        Request okrequest = createOKRequest(url, body, multi, header, tag);
        try {
            Response response = okHttpClient.newCall(okrequest).execute();
            if (response != null && response.isSuccessful()) {
                String result = response.body().string();
                response.close();
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Request createOKRequest(String url, Map<String, String> formMap, Map<String, Object> multiMap,
                                    Map<String, String> header, Object tag) {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.tag(tag);
        if (header != null && !header.isEmpty()) {
            for (Map.Entry<String, String> item : header.entrySet()) {
                builder.header(item.getKey(), item.getValue());
            }
        }
        RequestBody requestBody = null;
        if (formMap != null && !formMap.isEmpty()) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> item : formMap.entrySet()) {
                formBuilder.add(item.getKey(), item.getValue());
            }
            requestBody = formBuilder.build();
        } else if (multiMap != null && !multiMap.isEmpty()) {
            MultipartBody.Builder multiBuilder = new MultipartBody.Builder();

            for (Map.Entry<String, Object> item : multiMap.entrySet()) {
                String key = item.getKey();
                Object obj = item.getValue();
                if (obj instanceof String) {
                    multiBuilder.addFormDataPart(key, (String) obj);
                } else if (obj instanceof HttpFile) {
                    HttpFile file = (HttpFile) obj;
                    if (file.exists() && file.isFile()) {
                        multiBuilder.addFormDataPart(key, file.getName(), RequestBody.create(MediaType.parse(file
                                .getMediaType()), file));
                    } else {
                        Log.w(TAG, "File Error_" + file.getAbsolutePath());
                    }
                }
            }
            requestBody = multiBuilder.build();
        }

        if (requestBody != null) {
            builder.post(requestBody);
        }
        return builder.build();
    }

    public void cancel(Object tag) {
        threadPoolExecutor.execute(new HttpCancelRun(okHttpClient.dispatcher(), tag));
    }
}
