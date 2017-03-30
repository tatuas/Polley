package com.tatuas.android.polley;

import android.support.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class UploadManager {

    private static UploadManager sUploadManager;
    private static boolean mIsShutDownedFlag;
    private ThreadPoolExecutor mPoolExecutor;

    @NonNull
    public static UploadManager getInstance() {
        if (sUploadManager == null || mIsShutDownedFlag) {
            sUploadManager = new UploadManager();
        }
        return sUploadManager;
    }

    public int numberOfThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    public UploadManager() {
        mPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads());
        mIsShutDownedFlag = false;
    }

    public void terminate() {
        /*
         * ShutdownNow is a method that dequeue all and stop threads gently.
         */
        mPoolExecutor.shutdownNow();
        mIsShutDownedFlag = true;
        /*
         * TODO: awaitTerminationが問題ないか検討
        int shutDownTimeOut = 100000;
        try {
            mPoolExecutor.awaitTermination(shutDownTimeOut, TimeUnit.SECONDS);
        } catch (Exception e) {
            return;
        }
        */
    }

    public boolean enQueue(UploadTask task) {
        try {
            mPoolExecutor.execute(task.newUploadThread());
            return true;
        } catch (Exception e) {
            // Rejected or Null Exception
            return false;
        }
    }

    public boolean deQueue(UploadTask task) {
        return mPoolExecutor.getQueue().contains(task.newUploadThread())
                && mPoolExecutor.remove(task.newUploadThread());
    }

    public void deQueueAll() {
        final BlockingQueue<Runnable> blockingQueue = mPoolExecutor.getQueue();
        blockingQueue.clear();
    }
}
