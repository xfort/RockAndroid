package com.xfort.xrock.rockhttp;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by xubuntu on 18-3-19.
 */

public class RockHttp {

    OkHttpClient okHttpClient;
    boolean debug = false;


    public void debug(boolean isDebug) {
        debug = isDebug;
    }

    public OkHttpClient getOkhttp() {
        if (okHttpClient == null) {
            okHttpClient = initOkhttp();
        }
        return okHttpClient;
    }

    private OkHttpClient initOkhttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        if (debug) {
            HttpLoggingInterceptor httpLog = new HttpLoggingInterceptor();
            httpLog.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLog);
        }
        return builder.build();
    }

    public void cancel(Object tag) {
        List<Call> running = okHttpClient.dispatcher().runningCalls();
        List<Call> queued = okHttpClient.dispatcher().queuedCalls();
        okHttpClient.dispatcher().executorService().execute(new HttpCancelTask(running, queued, tag));

    }

    public void get(String url,Object tag,String cacheKey) {
        Request.Builder builder=new Request.Builder();
        builder.url(url).tag(tag);
        okHttpClient.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
