package org.lineageos.settings.device.utils;


import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class MobileDevice {
    private static final String TAG = "MobileDevice";

    public static Devices CurrentDevice;

    public enum Devices {
        S2,
        X2,
        X6,
        X7,
        ZL1,
        UNKNOWN
    }

    static {
        String device = Build.DEVICE;
        if (TextUtils.isEmpty(device)) {
            device = "";
        }
        device = device.toLowerCase();

        if (device.contains("x2"))  {
            CurrentDevice = Devices.X2;
        } else if (device.contains("le_x6")) {
            CurrentDevice = Devices.X6;
        } else if (device.contains("x7")) {
            CurrentDevice = Devices.X7;
        } else if (device.contains("s2")) {
            CurrentDevice = Devices.S2;
        } else if (device.contains("le_zl0")
                || device.contains("le_zl1")) {
            CurrentDevice = Devices.ZL1;
        } else {
            CurrentDevice = Devices.UNKNOWN;
        }

        Log.d(TAG, "CurrentDevice = " + CurrentDevice);
    }
}
