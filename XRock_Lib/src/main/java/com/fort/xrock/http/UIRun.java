package com.fort.xrock.http;

/**
 * Created by Mac on 16/8/30.
 */
public abstract class UIRun<T> implements Runnable {
    T obj;

    public UIRun(T obj) {
        this.obj = obj;
    }

}
