package com.fort.xrock.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by Mac on 16/8/29.
 */
public class RockUtil {


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
}
