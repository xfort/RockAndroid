package com.xfort.xrock.image;

/**
 * Created by tongying on 18-3-23.
 */

public interface CompressListener {
    void onFinished(String srcFilePath, String outFilePath, Exception e);
}
