package org.lineageos.settings.device;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import org.lineageos.settings.device.utils.ThreadPoolUtil;
import org.lineageos.settings.device.utils.Utils;

public class MainApplication extends Application {

    public Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Runnable) {
                ThreadPoolUtil.post((Runnable) msg.obj);
            }
        }
    };

    private static MainApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.applicationContext = this;
        setInstance(this);
    }

    public static void post(Runnable run) {
        sInstance.mMainHandler.post(run);
    }

    public static void postDelayed(final Runnable task, long delayMillis) {
        sInstance.mMainHandler.postDelayed(task, delayMillis);
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }

    private void setInstance(MainApplication context) {
        sInstance = context;
    }

    public static MainApplication getInstance() {
        return sInstance;
    }
}
