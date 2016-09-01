package com.fort.xrock.handler;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.fort.xrock.listener.CallbackListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mac on 16/9/1.
 * 权限先关
 */
public class PermHandler {
    Activity context;
    CallbackListener callbackListener;
    int requestCode;

    public void with(Activity context) {
        this.context = context;
    }

    /**
     * 检查是否有权限
     *
     * @param perm
     * @return
     */
    public boolean hasPerm(String perm) {
        int res = ContextCompat.checkSelfPermission(context, perm);
        if (res != PackageManager.PERMISSION_DENIED) {//有权限
            return true;
        }
        return false;
    }

    /**
     * 申请权限，会先检查是否拥有，只申请未拥有的权限
     *
     * @param requestCode
     * @param permArray        权限数组
     * @param callbackListener msg.arg1=0代表获取所有权限成功，1代表获取失败
     */
    public void applyPerm(int requestCode, String[] permArray, CallbackListener callbackListener) {
        this.requestCode = requestCode;
        this.callbackListener = callbackListener;

        List<String> needRequest = new ArrayList<>(permArray.length);
        for (String item : permArray) {
            int res = ContextCompat.checkSelfPermission(context, item);
            if (res == PackageManager.PERMISSION_DENIED) {//没有权限
                needRequest.add(item);
            }
        }
        if (!needRequest.isEmpty()) {
            ActivityCompat.requestPermissions(context, permArray, requestCode);
        } else {
            if (callbackListener != null) {
                Message msg = Message.obtain();
                msg.what = requestCode;
                callbackListener.callback(msg);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        Message msg = Message.obtain();
        msg.what = requestCode;
        boolean hasAllPerm = true;
        if (grantResults.length > 0) {
            for (int resCode : grantResults) {
                if (resCode != PackageManager.PERMISSION_GRANTED) {
                    hasAllPerm = false;
                }
            }
        }
        if (hasAllPerm) {
            msg.arg1 = 0;
        } else {
            msg.arg1 = 1;
        }
        if (callbackListener != null) {
            callbackListener.callback(msg);
        }
    }
}
