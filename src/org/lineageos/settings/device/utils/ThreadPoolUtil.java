package org.lineageos.settings.device.utils;

import android.os.Message;

import org.lineageos.settings.device.MainApplication;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class ThreadPoolUtil {
    private static final String TAG = "ThreadPoolUtil";
    private static volatile ExecutorService sThreadPool;

    private static ExecutorService getThreadPool() {
        if (sThreadPool == null) {
            sThreadPool = Executors.newCachedThreadPool();
        }
        return sThreadPool;
    }

    public static void post(Runnable task) {
        try {
            getThreadPool().execute(task);
        } catch (RejectedExecutionException e) {

        }
    }

    public static void postDelayed(final Runnable task, long delayMillis) {
        Message msg = Message.obtain();
        msg.obj = task;
        MainApplication.getInstance().mMainHandler.sendMessageDelayed(msg, delayMillis);
    }

    public static void removeCallbacks(Runnable task) {
        MainApplication.getInstance().mMainHandler.removeCallbacksAndMessages(task);
    }
}
