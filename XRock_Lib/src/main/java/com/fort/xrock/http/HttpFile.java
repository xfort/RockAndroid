package com.fort.xrock.http;

import java.io.File;
import java.net.URI;

/**
 * Created by Mac on 16/8/31.
 */
public class HttpFile extends File {
    String mediaType = "application/octet-stream";

    public HttpFile(String pathname) {
        super(pathname);
    }

    public HttpFile(String parent, String child) {
        super(parent, child);
    }

    public HttpFile(File parent, String child) {
        super(parent, child);
    }

    public HttpFile(URI uri) {
        super(uri);
    }

    public void setMediaType(String type) {
        mediaType = type;
    }

    public String getMediaType() {
        return mediaType;
    }

}
