package org.lineageos.settings.device.utils;

import android.widget.Toast;
import org.lineageos.settings.device.MainApplication;

public class ToastUtils {
    private static final String TAG = "ToastUtils";

    private static long sLastClickTime = 0;
    public static final int SECOND_2 = 2000;

    private static String getString(int resId) {
        return MainApplication.getInstance().getString(resId);
    }

    public static void showLimited(int resId) {
        showLimited(getString(resId));
    }

    public static void showLimited(String str) {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - sLastClickTime) > SECOND_2) {
            sLastClickTime = curTime;
            show(str);
        }
    }

    public static void show(int resId) {
        show(getString(resId));
    }

    public static void show(final String str) {
        MainApplication.post(new Runnable() {

            @Override
            public void run() {
                try {
                    Toast.makeText(MainApplication.getInstance(), str, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - sLastClickTime < SECOND_2) {
            return true;
        }
        sLastClickTime = time;
        return false;
    }
}
