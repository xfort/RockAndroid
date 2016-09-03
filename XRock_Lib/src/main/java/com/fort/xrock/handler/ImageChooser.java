package com.fort.xrock.handler;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.fort.xrock.bean.ImageMessage;
import com.fort.xrock.util.RockUtil;

import java.io.File;
import java.net.URI;

/**
 * Created by Mac on 16/9/1.
 * 图片选择，启动相机拍照+相册选取图片
 */
public class ImageChooser {

    public static final int Result_Error = 1111;
    final String TAG = "ImageChooser";

    public static final int Request_Camera = 1113;
    public static final int Request_Gallery = 1114;
    public static final int Request_Crop = 1115;

    //    XCallBack<ImageMessage> callbackListener;
    Activity activity;
    int imageWidth, imageHeight;
    boolean needCrop = true;

    public ImageChooser with(Activity activity) {
        this.activity = activity;
//        this.callbackListener = callbackListener;
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
     * 启动相机拍照
     *
     * @param requestCode
     * @param bd
     */
    public boolean startCamera(int requestCode, Bundle bd) {
        if (RockUtil.hasExternal()) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File picFile = new File(dir, System.currentTimeMillis() + ".png");
            return startCamera(requestCode, Uri.fromFile(picFile), bd);
        } else {
            Log.e(TAG, "startCamera() 外部存储无法使用");
        }
        return false;
    }

    /**
     * 启动相机
     */
    public boolean startCamera(int requestCode, Uri imageFileUri, Bundle bd) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
        intentCamera.putExtra("return-data", false);
        intentCamera.putExtra("outputFormat", "PNG");
        if (bd != null) {
            intentCamera.putExtras(bd);
        }
        if (intentCamera.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intentCamera, requestCode);
            return true;
        }
        return false;
    }

    /**
     * 打开相册
     *
     * @param requestCode
     */
    public boolean startGallery(int requestCode, Bundle bd) {
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
            return true;
        }
        return false;
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
    public boolean cropImage(int requestCode, Uri picFileUri, Uri resultUri, int outWidth, int outHeight, Bundle bd) {
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
            return true;
        } else {
            Log.w(TAG, "cropImage() fail");
//            onResult(picFileUri, requestCode, Result_Error);
        }
        return false;
    }

    public boolean cropImage(int requestCode, Uri picFileUri, int outWidth, int outHeight, Bundle bd) {
        File file = new File(URI.create(picFileUri.toString()));
        File outFile = new File(file.getParentFile(), file.getName() + "_" + System.currentTimeMillis() + ".png");
        return cropImage(requestCode, picFileUri, Uri.fromFile(outFile), outWidth, outHeight, bd);
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     * @return ImageMessage.requestCode小于0代表未完成所有操作
     */
    public ImageMessage onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri fileUri = null;
        ImageMessage imageMessage = new ImageMessage();
        imageMessage.requestCode = requestCode;
        imageMessage.resultCode = resultCode;
        if (requestCode == Request_Camera) {
            fileUri = parseCameraResult(requestCode, resultCode, data);
            if (needCrop && fileUri != null && cropImage(Request_Crop, fileUri, imageWidth, imageHeight, null)) {
                imageMessage.requestCode = -1;
            }
        } else if (requestCode == Request_Crop) {
            fileUri = parseCropResult(requestCode, resultCode, data);
        } else if (requestCode == Request_Gallery) {
            fileUri = parseGalleryResult(requestCode, resultCode, data);
            if (needCrop && fileUri != null && cropImage(Request_Crop, fileUri, imageWidth, imageHeight, null)) {
                imageMessage.requestCode = -1;
            }
        }
        imageMessage.imageUri = fileUri;
        return imageMessage;
    }

    /**
     * 解析处理拍照结果
     *
     * @param data
     */
    public Uri parseCameraResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            if (fileUri != null) {
                File imageFileTmp = new File(URI.create(fileUri.toString()));
                if (imageFileTmp.exists() && imageFileTmp.isFile()) {
                    return fileUri;
                }
            }
        }
        return null;
    }

    /**
     * 解析图片裁剪结果,若裁剪失败会返回原图Uri
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public Uri parseCropResult(int requestCode, int resultCode, Intent data) {
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
                        return outUri;
                    }
                }
            }
        }
        return sourceUri;
    }

    /**
     * 解析图片库结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public Uri parseGalleryResult(int requestCode, int resultCode, Intent data) {
        Uri fileUri = null;
        if (resultCode == Activity.RESULT_OK) {
            Uri uriData = data.getData();

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
        return fileUri;
//        onResult(fileUri, requestCode, resultCode);
    }


}