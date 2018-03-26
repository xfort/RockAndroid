package org.xfort.xrock.library.http;

import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.Call;

/**
 * Created by xubuntu on 18-3-19.
 * 取消Okhttp的Call
 */

public class HttpCancelTask implements Runnable {
    WeakReference<List<Call>> running, queued;
    Object tag;

    public HttpCancelTask(List<Call> running, List<Call> queued, Object cancelTag) {
        if (running != null && !running.isEmpty()) {
            this.running = new WeakReference<>(running);
        }
        if (queued != null && !queued.isEmpty()) {
            this.queued = new WeakReference<List<Call>>(queued);
        }
        tag = cancelTag;
    }

    @Override
    public void run() {
        if (running != null) {
            cancelCall(running.get(), tag);
        }
        if (queued != null) {
            cancelCall(queued.get(), tag);
        }
    }

    private void cancelCall(List<Call> calls, Object tag) {
        if (calls != null && !calls.isEmpty()) {
            for (Call item : calls) {
                if (item.isCanceled()) {
                    continue;
                }
                if (item.request().tag().equals(tag)) {
                    item.cancel();
                }
            }
        }
    }
}
