package org.lineageos.settings.device.utils;


import android.text.TextUtils;
import android.util.Log;

public class SystemProperties {

    private static final String TAG = "SystemProperties";

    public static void set(String key, String value) {
        boolean hasRoot = RootCmd.haveRoot();

        if (hasRoot) {
            boolean status = RootCmd.execRootCmd("setprop " + key + " " + value + "\n");
            Log.d(TAG, "using root to setprop " + key
                    + " " + value + " " + (status ? "success" : "failed"));
            return;
        } else {
            Log.e(TAG, "setprop failed no root privileges");
        }

//        ReflectionUtils.invokeStaticMethod("android.os.SystemProperties",
//                "set",
//                new Class[] {String.class, String.class},
//                new Object[] {key, value});
    }

    public static String get(String key, String defValue) {
        String result = (String) ReflectionUtils.invokeStaticMethod("android.os.SystemProperties",
                "get",
                new Class[] {String.class, String.class},
                new Object[] {key, defValue});
        return TextUtils.isEmpty(result) ? defValue : result;
    }
}
