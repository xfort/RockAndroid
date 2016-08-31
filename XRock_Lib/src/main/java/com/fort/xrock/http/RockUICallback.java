package com.fort.xrock.http;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Mac on 16/8/30.
 */
public abstract class RockUICallback<T> implements Callback {
    Handler handler;

    @Override
    public void onFailure(Call call, IOException e) {
        if (!call.isCanceled()) {
            postUI(null);
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!call.isCanceled()) {

            if (response != null && response.isSuccessful()) {
                String resStr = response.body().string();
                T res = parseResponse(resStr);
                postUI(res);
            } else {
                postUI(null);
            }
        }
    }

    void postUI(T obj) {
        UIRun<T> uiRun = new UIRun<T>(obj) {
            @Override
            public void run() {
                onResult(obj);
            }
        };

        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        handler.post(uiRun);
    }

    public abstract T parseResponse(String result);

    public abstract void onResult(T obj);
}