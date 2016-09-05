package com.fort.xrock.handler;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mac on 16/9/5.
 *
 */
public class RockAppHandler {
    ThreadPoolExecutor threadPoolExecutor;

    /**
     * 线程池
     *
     * @return
     */
    public ThreadPoolExecutor getAppThreadPool() {
        if (threadPoolExecutor == null || threadPoolExecutor.isShutdown()) {
            int cpuSize = Runtime.getRuntime().availableProcessors();
            if (cpuSize <= 4) {
                cpuSize = 2;
            }
            threadPoolExecutor = new ThreadPoolExecutor(1, cpuSize * 5, 20, TimeUnit.SECONDS, new
                    LinkedBlockingDeque<Runnable>());
        }
        return threadPoolExecutor;
    }
}
