package org.lineageos.settings.device.utils;


import android.text.TextUtils;

public class SystemProperties {

    public static void set(String key, String value) {
        ReflectionUtils.invokeStaticMethod("android.os.SystemProperties",
                "set",
                new Class[] {String.class, String.class},
                new Object[] {key, value});
    }

    public static String get(String key, String defValue) {
        String result = (String) ReflectionUtils.invokeStaticMethod("android.os.SystemProperties",
                "get",
                new Class[] {String.class, String.class},
                new Object[] {key, defValue});
        return TextUtils.isEmpty(result) ? defValue : result;
    }
}
