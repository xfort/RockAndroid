package org.xfort.xrock.library.image.compress;

/**
 * Created by tongying on 18-3-23.
 */

public interface CompressListener {
    void onFinished(String srcFilePath, String outFilePath, Exception e);
}
