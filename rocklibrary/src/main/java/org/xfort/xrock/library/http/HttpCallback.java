package org.xfort.xrock.library.http;

/**
 * Created by xubuntu on 18-3-20.
 */

public interface HttpCallback<T> {
    T onResult(byte[] resBytes, String errmsg);
    void onUI(T obj, String errmsg);
}
