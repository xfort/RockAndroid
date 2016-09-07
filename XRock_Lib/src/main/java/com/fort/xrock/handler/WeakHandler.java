package com.fort.xrock.handler;

import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by Mac on 16/9/7.
 */
public class WeakHandler<T> extends Handler {
    WeakReference<T> weakObj;

    public void with(T obj) {
        weakObj = new WeakReference<T>(obj);
    }

    public T getWeak() {
        if (weakObj != null) {
            return weakObj.get();
        }
        return null;
    }

    /**
     * 销毁所有Message
     */
    public void destroy() {
        if (weakObj != null) {
            weakObj.clear();
            weakObj = null;
        }
        removeCallbacksAndMessages(null);
    }
}