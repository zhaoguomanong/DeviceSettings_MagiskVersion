package org.lineageos.settings.device.utils;


import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class MobileDevice {
    private static final String TAG = "MobileDevice";

    private static final String UNKNOWN = "unknown";
    private static final String EMPTY = "";

    public static Devices CurrentDevice;

    public enum Devices {
        S2,
        X2,
        ZL1,
        UNKNOWN
    }

    static {
        String EXTRA_DEVICE_INFO = SystemProperties.get("ro.leeco.devinfo", EMPTY);
        if (isEmpty(EXTRA_DEVICE_INFO)) {
            EXTRA_DEVICE_INFO = SystemProperties.get("ro.config.product", EMPTY);
        }
        if (isEmpty(EXTRA_DEVICE_INFO)) {
            EXTRA_DEVICE_INFO = SystemProperties.get("ro.lineage.device", EMPTY);
        }
        if (isEmpty(EXTRA_DEVICE_INFO)) {
            EXTRA_DEVICE_INFO = SystemProperties.get("ro.display.series", EMPTY);
        }
        if (isEmpty(EXTRA_DEVICE_INFO)) {
            EXTRA_DEVICE_INFO = UNKNOWN;
        }
        EXTRA_DEVICE_INFO = EXTRA_DEVICE_INFO.toLowerCase();

        String BUILD_DEVICE = Build.DEVICE;
        if (TextUtils.isEmpty(BUILD_DEVICE)) {
            BUILD_DEVICE = UNKNOWN;
        }
        BUILD_DEVICE = BUILD_DEVICE.toLowerCase();

        if (BUILD_DEVICE.contains("x2")
                || EXTRA_DEVICE_INFO.contains("x2"))  {
            CurrentDevice = Devices.X2;
        } else if (BUILD_DEVICE.contains("s2")
                || EXTRA_DEVICE_INFO.contains("s2")) {
            CurrentDevice = Devices.S2;
        } else if (BUILD_DEVICE.contains("le_zl0")
                || BUILD_DEVICE.contains("le_zl1")) {
            CurrentDevice = Devices.ZL1;
        } else {
            CurrentDevice = Devices.UNKNOWN;
        }

        Log.d(TAG, "BUILD_DEVICE = " + BUILD_DEVICE
                + ", EXTRA_DEVICE_INFO = " + EXTRA_DEVICE_INFO
                + ", CurrentDevice = " + CurrentDevice);
    }

    private static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str)
                || "null".equalsIgnoreCase(str);
    }
}
