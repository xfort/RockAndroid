package com.fort.xrock.http;

import android.support.v4.util.ArrayMap;

import java.util.Map;

import okhttp3.Callback;

/**
 * Created by Mac on 16/8/29.
 */
public class RockRequest {
    ArrayMap<String, String> headMap;
    ArrayMap<String, String> formMap;
    ArrayMap<String, Object> multiMap;

    String url;
    String method;
    Object httpTag;

    Callback callback;

    public RockRequest url(String url) {
        this.url = url;
        return this;
    }

    public RockRequest method(String method) {
        this.method = method;
        return this;
    }

    public RockRequest httpTag(Object obj) {
        httpTag = obj;
        return this;
    }

    public RockRequest addForm(String key, String value) {
        if (formMap == null) {
            formMap = new ArrayMap<>();
        }
        formMap.put(key, value);
        return this;
    }

    public RockRequest addMulti(String key, Object obj) {
        if (multiMap == null) {
            multiMap = new ArrayMap<>();
        }
        multiMap.put(key, obj);
        return this;
    }

    public RockRequest addHeader(String key, String value) {
        if (headMap == null) {
            headMap = new ArrayMap<>();
        }
        headMap.put(key, value);
        return this;
    }

    public RockRequest putAllForm(Map<String, String> map) {
        if (formMap == null) {
            formMap = new ArrayMap<>();
        }
        formMap.putAll(map);
        return this;
    }

    public RockRequest putAllMulti(Map<String, String> map) {
        if (multiMap == null) {
            multiMap = new ArrayMap<>();
        }
        multiMap.putAll(map);
        return this;
    }

    public RockRequest putAllHeader(Map<String, String> map) {
        if (headMap == null) {
            headMap = new ArrayMap<>();
        }
        headMap.putAll(map);
        return this;
    }

}
