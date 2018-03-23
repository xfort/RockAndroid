package com.xfort.xrock.rockhttp;


import android.os.Handler;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**285  139 802
 * Created by xubuntu on 18-3-21.
 */

public abstract class UICallback<Result, Object> implements Callback, HttpCallback<Result> {

    public boolean destroyed = false;
    WeakReference<Handler> handler;
    public WeakReference<Object> weakObj;

    public UICallback handler(Handler handler) {
        this.handler = new WeakReference<Handler>(handler);
        return this;
    }

    public UICallback attachObj(Object obj) {
        weakObj = new WeakReference<Object>(obj);
        return this;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
        if (destroyed) {
            return;
        }
        if (call == null || call.isCanceled()) {
            destroy();
            return;
        }
        Result resObj = onResult(null, "网络通信异常");
        if (!destroyed) {
            postUI(resObj, "网络通信异常");
        }
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        byte[] resData = null;
        String errmsg = null;
        try {
            if (destroyed) {

            } else {
                if (call == null || call.isCanceled()) {
                    destroy();
                } else {
                    resData = response.body().bytes();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            errmsg = "读取数据失败";
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!destroyed) {
                Result resObj = onResult(resData, errmsg);
                postUI(resObj, errmsg);
            }
        }
    }

    public void destroy() {
        destroyed = true;
        if (handler != null && handler.get() != null) {
            handler.get().removeCallbacksAndMessages(null);
            handler.clear();
            handler = null;
        }
        if (weakObj != null) {
            weakObj.clear();
            weakObj = null;
        }
    }

    void postUI(Result obj, String errmsg) {
        if (destroyed) {
            return;
        }
        if (handler != null && handler.get() != null) {
            handler.get().post(new UIRun<>(this, obj, errmsg));
        }

    }

    class UIRun<T, Obj> implements Runnable {
        WeakReference<UICallback<T, Obj>> weakUIcallback;
        T resultObj;
        String errmsg;

        public UIRun(UICallback<T, Obj> uicallback, T result, String errmsg) {
            weakUIcallback = new WeakReference<UICallback<T, Obj>>(uicallback);
            this.resultObj = result;
            this.errmsg = errmsg;
        }

        @Override
        public void run() {
            if (weakUIcallback != null) {
                UICallback<T, Obj> uiCallback = weakUIcallback.get();
                if (uiCallback != null && !uiCallback.destroyed) {
                    uiCallback.onUI(resultObj, errmsg);
                }
            }
        }
    }
}
