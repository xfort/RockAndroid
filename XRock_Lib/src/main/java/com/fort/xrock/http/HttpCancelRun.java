package com.fort.xrock.http;

import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Dispatcher;

/**
 * Created by Mac on 16/8/31.
 */
public class HttpCancelRun implements Runnable {
    WeakReference<Dispatcher> weak;
    Object obj;

    public HttpCancelRun(Dispatcher dispatcher, Object obj) {
        weak = new WeakReference<Dispatcher>(dispatcher);
        this.obj = obj;
    }

    @Override
    public void run() {
        if (weak != null && weak.get() != null) {
            Dispatcher dispatcher = weak.get();
            dispatcher.cancelAll();
            if (obj == null) {
                dispatcher.cancelAll();
            } else {
                for (Call call : dispatcher.runningCalls()) {
                    if (call.request().tag().equals(obj)) {
                        call.cancel();
                    }
                }

                for (Call call : dispatcher.queuedCalls()) {
                    if (call.request().tag().equals(obj)) {
                        call.cancel();
                    }
                }
            }
        }
    }
}
