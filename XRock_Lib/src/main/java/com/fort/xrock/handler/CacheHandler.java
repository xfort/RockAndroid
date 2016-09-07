package com.fort.xrock.handler;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.fort.xrock.util.RockUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Mac on 16/9/7.
 * 缓存管理。内存缓存+磁盘缓存
 */
public class CacheHandler {
    public static final int Cache_Memory = 10;
    public static final int Cache_Disk = 20;
    public static final int Cache_DiskAndMemory = 30;
    DiskLruCache diskLruCache;
    ArrayMap<String, Object> cacheMap = new ArrayMap<>(30);

    public DiskLruCache initDiskCache(Context appContext) {

        try {
            File cacheDir = null;
            if (RockUtil.hasExternal()) {
                cacheDir = new File(appContext.getExternalCacheDir(), "appcache");
            } else {
                cacheDir = new File(appContext.getCacheDir(), "appcache");
            }
            cacheDir.mkdirs();
            diskLruCache = DiskLruCache.open(cacheDir, 2, 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return diskLruCache;
    }

    /**
     * 添加缓存数据
     *
     * @param key
     * @param obj       null代表删除数据
     * @param cacheType 缓存类别
     */
    public void put(String key, Object obj, int cacheType) {
        if (cacheType == Cache_Memory) {
            if (obj == null) {
                cacheMap.remove(key);
            } else {
                cacheMap.put(key, obj);
            }
        } else if (cacheType == Cache_Disk) {
            if (obj instanceof String) {
                diskPut(key, (String) obj);
            }
        } else if (cacheType == Cache_DiskAndMemory) {
            if (obj == null) {
                cacheMap.remove(key);
                diskPut(key, null);
            } else {
                cacheMap.put(key, obj);
                if (obj instanceof String) {
                    diskPut(key, (String) obj);
                }
            }
        }
    }

    public void putMemory(String key, Object obj) {
        put(key, obj, Cache_Memory);
    }

    public void putDisk(String key, Object obj) {
        put(key, obj, Cache_Disk);
    }


    public void putDiskMemory(String key, Object obj) {
        put(key, obj, Cache_DiskAndMemory);
    }

    public Object getMemory(String key) {
        return get(key, Cache_Memory);
    }

    public Object getDisk(String key) {
        return get(key, Cache_Disk);
    }

    public Object getMemoryDisk(String key) {
        return get(key, Cache_DiskAndMemory);
    }

    /**
     * 读取内存，磁盘的缓存数据
     *
     * @param key
     * @param cacheType 缓存类别
     * @return 可能为NULL
     */
    public Object get(String key, int cacheType) {
        Object obj = null;
        if (cacheType == Cache_Memory) {
            obj = cacheMap.get(key);
        } else if (cacheType == Cache_Disk) {
            obj = diskGet(key);
        } else {
            obj = cacheMap.get(key);
            if (obj == null) {
                obj = diskGet(key);
            }
        }
        return obj;
    }

    public String getStr(String key, int type) {
        Object obj = get(key, type);
        if (obj != null && obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    /**
     * 添加数据到磁盘缓存
     *
     * @param key
     * @param obj 必须为String
     */
    private void diskPut(String key, String obj) {
        if (obj == null) {
            try {
                diskLruCache.remove(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (!TextUtils.isEmpty(obj)) {
                diskPutStream(key, new ByteArrayInputStream((obj).getBytes()));
            }
        }
    }

    public Object diskGet(String key) {
        if (diskLruCache != null && !diskLruCache.isClosed()) {
            try {
                DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
                if (snapshot != null) {
                    return snapshot.getString(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void diskPutStream(String key, InputStream obj) {
        if (diskLruCache != null && !diskLruCache.isClosed()) {
            BufferedInputStream input = null;
            BufferedOutputStream output = null;
            try {
                DiskLruCache.Editor editor = diskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    input = new BufferedInputStream(obj);
                    output = new BufferedOutputStream(outputStream);
                    int len;
                    while ((len = input.read()) != -1) {
                        output.write(len);
                    }
                    editor.commit();
                }
                diskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}