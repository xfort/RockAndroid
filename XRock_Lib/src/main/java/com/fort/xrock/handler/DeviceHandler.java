package com.fort.xrock.handler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Mac on 16/9/1.
 * 设备数据
 */
public class DeviceHandler {
    final String TAG = "DeviceHandler";
    Context appContext;
    int screenWidth, screenHeight, windowWidth, windowHeigh;

    public void with(Context context) {
        appContext = context;
    }


    /**
     * Mac地址
     *
     * @return
     */
    public String getMacAddress() {
        String macAddressRes = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            int res = ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_WIFI_STATE);
            if (res == PackageManager.PERMISSION_GRANTED) {//有权限
                Object obj = appContext.getSystemService(Context.WIFI_SERVICE);
                if (obj != null) {
                    WifiManager wifiManager = (WifiManager) obj;
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();//需要权限
                    if (wifiInfo != null) {
                        String macAddress = wifiInfo.getMacAddress();
                        if (!TextUtils.isEmpty(macAddress) && !macAddress.equalsIgnoreCase("null")) {
                            macAddressRes = macAddress;
                        }
                    }
                }
            }
        }
        return macAddressRes;
    }

    public void initScreen() {
        DisplayMetrics dm = appContext.getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        WindowManager wm = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics mert = new DisplayMetrics();
        display.getMetrics(mert);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point outSize = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(outSize);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    display.getSize(outSize);
                }
            }
            windowHeigh = outSize.y;
            windowWidth = outSize.x;
        } else {
            windowHeigh = display.getHeight();
            windowWidth = display.getWidth();
        }
    }

    /**
     * 屏幕
     *
     * @return
     */
    public int getScreenWidth() {
        if (screenWidth <= 0) {
            initScreen();
        }
        return screenWidth;
    }

    /**
     * 屏幕
     *
     * @return
     */
    public int getScreenHeight() {
        if (screenHeight <= 0) {
            initScreen();
        }
        return screenHeight;
    }

    /**
     * 屏幕
     *
     * @return
     */
    public int getWindowWidth() {
        if (windowWidth <= 0) {
            initScreen();
        }
        return windowWidth;
    }

    /**
     * 屏幕
     *
     * @return
     */
    public int getWindowHeight() {
        if (windowHeigh <= 0) {
            initScreen();
        }
        return windowHeigh;
    }

    public String getPhoneID() {
        String deviceUID = null;
        int res = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_PHONE_STATE);
        if (res == PackageManager.PERMISSION_GRANTED) {//有权限
            Object obj = appContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (obj != null && obj instanceof TelephonyManager) {
                TelephonyManager telephonyManager = (TelephonyManager) obj;
                String teleID = telephonyManager.getDeviceId();
                if (!TextUtils.isEmpty(teleID) && !teleID.equalsIgnoreCase("null")) {
                    deviceUID = teleID;
                }
                String simNum = telephonyManager.getSimSerialNumber();
                if (!TextUtils.isEmpty(simNum)) {
                    deviceUID = deviceUID + simNum;
                }
            }
        }
        return deviceUID;
    }

    public String getAndroidID() {
        String androidID = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.equals(androidID, "9774d56d682e549c") || androidID.equalsIgnoreCase("null")) {
            androidID = null;
        }
        return androidID;
    }

    public String getCPUInfo() {
        String cpuInfp = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            cpuInfp = Build.SUPPORTED_ABIS;
//        }
//        if (cpuInfp == null || cpuInfp.length <= 0) {
        try {
            InputStream fileInputStream = new FileInputStream("/proc/cpuinfo");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] params = line.split("\\:");
                if (params.length >= 2) {
                    String key = params[0];
                    if (key.trim().equals("Processor")) {
                        cpuInfp = params[1].trim();
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cpuInfp;
    }
}
