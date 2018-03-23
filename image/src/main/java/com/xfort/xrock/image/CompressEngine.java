package com.xfort.xrock.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by tongying on 18-3-23.
 */

public class CompressEngine {
    String srcFilePath;
    File outFile;
    Bitmap.CompressFormat outFormat;
    int ignorSize;

    int srcWidth, srcHeight;
    private ExifInterface srcExif;


    public CompressEngine(String srcfilepath, File outFile, Bitmap.CompressFormat outFormat, int
            ignorSize) throws Exception {
        this.srcFilePath = srcfilepath;
        this.outFile = outFile;
        this.outFormat = outFormat;
        this.ignorSize = ignorSize;


        this.srcExif = new ExifInterface(srcfilepath);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeFile(srcfilepath, options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;

    }

    public File compress() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = computeSize();

            Bitmap tagBitmap = BitmapFactory.decodeFile(srcFilePath, options);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            File srcFile = new File(srcFilePath);

            int quality = 80;
            if (ignorSize > 0) {
                if (srcFile.length() < (ignorSize << 10)) {
                    quality = 100;
                } else if (srcFile.length() >= (ignorSize << 10) * 2) {
                    quality = 60;
                }
            } else {
                quality = 60;
            }

            srcFile = null;

            tagBitmap = rotatingImage(tagBitmap);

            tagBitmap.compress(outFormat, quality, stream);
            tagBitmap.recycle();

            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(stream.toByteArray());
            fos.flush();
            fos.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int computeSize() {
        srcWidth = srcWidth % 2 == 1 ? srcWidth + 1 : srcWidth;
        srcHeight = srcHeight % 2 == 1 ? srcHeight + 1 : srcHeight;

        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide >= 1664 && longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }

    private Bitmap rotatingImage(Bitmap bitmap) {
        if (srcExif == null) return bitmap;

        Matrix matrix = new Matrix();
        int angle = 0;

        int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface
                .ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
        }

        matrix.postRotate(angle);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
    }


}
