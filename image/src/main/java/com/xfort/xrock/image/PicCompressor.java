package com.xfort.xrock.image;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by tongying on 18-3-23.
 * 图片压缩类
 */

public class PicCompressor {
    String srcPicFilePath;
    File outPicFile;
    String outFilePath;
    int ignorSize = 200;
    Bitmap.CompressFormat format;
    CompressListener compressListener;

    String outDirPath;
    boolean destroy = false;

    public PicCompressor loadPic(String picfilepath) {
        this.srcPicFilePath = picfilepath;
        return this;
    }


    public PicCompressor loadPic(File picfile) {
        this.srcPicFilePath = picfile.getAbsolutePath();
        return this;
    }

    /**
     * 图片小于 size KB则不压缩
     *
     * @param size
     * @return
     */
    public PicCompressor ignorBy(int size) {
        ignorSize = size;
        if (ignorSize <= 0) {
            ignorSize = 200;
        }
        return this;
    }

    public PicCompressor format(Bitmap.CompressFormat format) {
        this.format = format;
        return this;
    }

    public PicCompressor outPath(String outfilepath, String outDirPath) {
        this.outFilePath = outfilepath;
        this.outDirPath = outDirPath;
        return this;
    }


    public void destroy() {
        destroy = true;
    }

    /**
     * 同步执行图片压缩
     *
     * @return
     */
    public String startSync() {
        File outFile = null;
        try {
            String srcFile = srcPicFilePath;
            do {
                if (destroy) {
                    break;
                }
                outFile = getOutPicFile();
                if (outFile == null) {
                    return null;
                }
                CompressEngine compressEngine = new CompressEngine(srcFile, outFile, format,
                        ignorSize);
                outFile = compressEngine.compress();
                if (outFile != null) {
                    srcFile = outFile.getAbsolutePath();
                } else {
                    srcFile = null;
                }
            } while (outFile != null && ignorSize > 0 && outFile.length() > (ignorSize << 10));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return outFile.getAbsolutePath();
    }

    /**
     * 异步图片压缩
     *
     * @param compressListener
     */
    public void start(CompressListener compressListener) {
        this.compressListener = compressListener;
        //TODO
    }

    /**
     * 最终文件存放路径
     *
     * @return
     */
    private File getOutPicFile() {
        String fileSufix = "jpeg";
        if (format == Bitmap.CompressFormat.JPEG) {
            fileSufix = ".jpeg";
        } else if (format == Bitmap.CompressFormat.PNG) {
            fileSufix = ".png";
        } else if (format == Bitmap.CompressFormat.WEBP) {
            fileSufix = ".webp";
        }
        try {
            if (TextUtils.isEmpty(outFilePath)) {
                outPicFile = new File(outDirPath, System.currentTimeMillis() + "" + (int) (Math
                        .random() * 1000) + fileSufix);
            } else {
                outPicFile = new File(outFilePath + fileSufix);
            }
        } catch (Exception e) {
            return null;
        }
        return outPicFile;
    }

}
