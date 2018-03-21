package com.xfort.xrock.rockhttp;


import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;

/**
 * Created by xubuntu on 18-3-21.
 */

public class UIHandler<T> extends Handler {
    public Object tag;
    public WeakReference<T> weakObj;

    public UIHandler(T obj) {
        super(Looper.getMainLooper());
        if (obj != null) {
            weakObj = new WeakReference<T>(obj);
        }
    }

    public void tag(Object tag) {
        this.tag = tag;
    }

    public void destroy() {
        removeCallbacksAndMessages(null);
        if (weakObj != null) {
            weakObj.clear();
            weakObj = null;
        }
    }

}
