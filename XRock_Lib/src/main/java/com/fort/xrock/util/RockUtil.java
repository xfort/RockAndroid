package com.fort.xrock.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;

/**
 * Created by Mac on 16/8/29.
 */
public class RockUtil {
//

    public static Object getMetaData(Context context, String key) {
        try {
            Object obj = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager
                    .GET_META_DATA).metaData.get(key);
            return obj;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMetaString(Context context, String key) {
        try {
            Object obj = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager
                    .GET_META_DATA).metaData.get(key);
            if (obj != null && obj instanceof String) {
                return (String) obj;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加图片到相册
     *
     * @param picFilePath
     * @param context
     */
    public static void galleryAddPic(String picFilePath, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(picFilePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 外部存储是否可用
     *
     * @return
     */
    public static boolean hasExternal() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    /**
     * 检查是否有权限
     *
     * @param appContext
     * @param permission
     * @return
     */
    public static boolean hasPermission(Context appContext, String permission) {
        int res = ContextCompat.checkSelfPermission(appContext, permission);
        if (res == PackageManager.PERMISSION_GRANTED) {//有权限
            return true;
        }
        return false;
    }
}