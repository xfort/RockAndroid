package com.fort.xrock.handler;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.fort.xrock.bean.ImageMessage;
import com.fort.xrock.listener.XCallBack;

import java.io.File;
import java.net.URI;

/**
 * Created by Mac on 16/9/1.
 * 图片选择，启动相机拍照+相册选取图片
 */
public class ImageChooser {

    public static final int Result_Error = 121;
    final String TAG = "ImageChooser";
    public static final int ImageChooser = 122;
    public static final int Request_Camera = 123;
    public static final int Request_Gallery = 124;
    public static final int Request_Crop = 125;

    XCallBack<ImageMessage> callbackListener;
    Activity activity;
    int imageWidth, imageHeight;
    boolean needCrop = true;

    public ImageChooser with(Activity activity, XCallBack<ImageMessage> callbackListener) {
        this.activity = activity;
        this.callbackListener = callbackListener;
        return this;
    }

    /**
     * 设置拍照或裁减后的图片尺寸
     *
     * @param width
     * @param height
     * @return
     */
    public ImageChooser setImageSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;
        return this;
    }

    /**
     * 是否要裁剪图片
     *
     * @param needCrop
     * @return
     */
    public ImageChooser crop(boolean needCrop) {
        this.needCrop = needCrop;
        return this;
    }

    /**
     * 启动相机
     */
    public void startCamera(int requestCode, Uri imageFileUri, Bundle bd) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        intentCamera.putExtra("return-data", false);
        intentCamera.putExtra("outputFormat", "PNG");
        if (bd != null) {
            intentCamera.putExtras(bd);
        }
        if (intentCamera.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intentCamera, requestCode);
        } else {
            onResult(null, requestCode, Result_Error);
            Log.e(TAG, "startCamera_NOT found Activity");
        }
    }

    /**
     * 打开相册
     *
     * @param requestCode
     */
    public void startGallery(int requestCode, Bundle bd) {
        Intent fileIntent = null;
        if (Build.VERSION.SDK_INT < 19) {
            fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileIntent.setType("image/*");
        } else {
            fileIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }

        fileIntent.putExtra("return-data", false);

        if (bd != null) {
            fileIntent.putExtras(bd);
        }

        if (fileIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(fileIntent, requestCode);
        } else {
            onResult(null, requestCode, Result_Error);
        }
    }

    /**
     * 裁剪图片
     *
     * @param requestCode
     * @param picFileUri  图片源文件
     * @param resultUri   裁剪后输出文件
     * @param outWidth
     * @param outHeight
     * @param bd          其它参数
     */
    public void cropImage(int requestCode, Uri picFileUri, Uri resultUri, int outWidth, int outHeight, Bundle bd) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(picFileUri, "image/*");
        intent.putExtra("crop", "true");
        // intent.putExtra("aspectX", 1);
        // intent.putExtra("aspectY", 1);
        if (outWidth > 0 && outHeight > 0) {
            intent.putExtra("outputX", outWidth);
            intent.putExtra("outputY", outHeight);
        }
        intent.putExtra("scale", false);//裁剪时是否保留图片的比例
        intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", "PNG");
        intent.putExtra("noFaceDetection", true); // no face detection
        intent.putExtra("outputUri", resultUri);
        intent.putExtra("sourcetUri", picFileUri);
        if (bd != null) {
            intent.putExtras(bd);
        }

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            onResult(picFileUri, requestCode, Result_Error);
        }
    }

    public void cropImage(int requestCode, Uri picFileUri, int outWidth, int outHeight, Bundle bd) {
        File file = new File(URI.create(picFileUri.toString()));
        File outFile = new File(file.getParentFile(), file.getName() + "_" + System.currentTimeMillis() + ".png");
        cropImage(requestCode, picFileUri, Uri.fromFile(outFile), outWidth, outHeight, bd);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Request_Camera) {
            parseCameraResult(requestCode, resultCode, data);
        } else if (requestCode == Request_Crop) {
            parseCropResult(requestCode, resultCode, data);
        } else if (requestCode == Request_Gallery) {
            parseGalleryResult(requestCode, resultCode, data);
        }
    }

    /**
     * 解析处理拍照结果
     *
     * @param data
     */
    public void parseCameraResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            if (fileUri != null) {
                if (needCrop) {
                    cropImage(Request_Crop, fileUri, imageWidth, imageHeight, null);
                    return;
                } else {
                    File imageFileTmp = new File(URI.create(fileUri.toString()));
                    if (imageFileTmp.exists() && imageFileTmp.isFile()) {
                        onResult(fileUri, requestCode, resultCode);
                        return;
                    }
                }
            }
            resultCode = Result_Error;
        }
        onResult(null, requestCode, resultCode);
    }

    /**
     * 解析图片裁剪结果,若裁剪失败会返回原图Uri
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void parseCropResult(int requestCode, int resultCode, Intent data) {
        Uri sourceUri = null, outUri;
        if (data != null) {
            sourceUri = data.getParcelableExtra("sourcetUri");
            if (resultCode == Activity.RESULT_OK) {
                outUri = data.getParcelableExtra("outputUri");
                if (outUri == null) {
                    outUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                }
                if (outUri != null) {
                    File imageFileTmp = new File(URI.create(outUri.toString()));
                    if (imageFileTmp.exists() && imageFileTmp.isFile()) {
                        onResult(outUri, requestCode, resultCode);
                        return;
                    }
                }
                resultCode = Result_Error;
            }
        }
        onResult(sourceUri, requestCode, resultCode);
    }

    /**
     * 解析图片库结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void parseGalleryResult(int requestCode, int resultCode, Intent data) {
        Uri fileUri = null;
        Uri uriData = data.getData();
        if (resultCode == Activity.RESULT_OK) {
            Cursor cursor = activity.getContentResolver().query(uriData, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex("_data");
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                File picFile = new File(picturePath);
                if (picFile.exists() && picFile.isFile()) {
                    fileUri = Uri.fromFile(picFile);
                }
            } else {
                File file = new File(URI.create(uriData.toString()));
                if (file.exists() && file.isFile()) {
                    fileUri = uriData;
                }
            }
            if (fileUri == null) {
                resultCode = Result_Error;
            }
        }
        onResult(fileUri, requestCode, resultCode);
    }

    private void onResult(Uri resFileUri, int requestCode, int resultCode) {
        if (callbackListener != null) {
            ImageMessage message = new ImageMessage();
            message.requestCode = requestCode;
            message.resultCode = resultCode;
            message.imageUri = resFileUri;
            callbackListener.callback(message);
        }
    }
}