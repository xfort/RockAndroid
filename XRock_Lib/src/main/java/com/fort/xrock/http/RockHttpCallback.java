package com.fort.xrock.http;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Mac on 16/8/29.
 */
public abstract class RockHttpCallback<T> implements Callback {

    @Override
    public void onFailure(Call call, IOException e) {
        if (e != null) {
            e.printStackTrace();
        }
        if (!call.isCanceled()) {
            try {
                onResponse(call, null);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (!call.isCanceled()) {
            if (response != null && response.isSuccessful()) {
                String resStr = response.body().string();
                response.close();
                parseResponse(resStr);
            } else {
                parseResponse(null);
            }
        }
    }

    public abstract T parseResponse(String resStr);
}
