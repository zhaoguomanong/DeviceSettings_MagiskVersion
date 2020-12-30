package org.lineageos.settings.device.utils;


import android.text.TextUtils;
import android.util.Log;

import org.lineageos.settings.device.MainApplication;

import java.io.File;

public class ZJLUtils {

    private static final String TAG = "ZJLUtils";
    private static final String STATUS_DISABLED = "啥都没开";
    private static final String ON = "开启.sh";
    private static final String OFF = "关闭.sh";
    private static final String STATUS = "检测.sh";


    private static String ZJL_BIN_PATH = null;
    private static String ENABLE_ZJL = null;
    private static String DISABLE_ZJL = null;
    private static String STATUS_ZJL = null;


    public static final boolean ZJL_SUPPORTED = isZJLSupported();
    public static boolean isSwitchingZJL = false;

    public static void enableZJL(final IZJLCallback listener, final boolean enable) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                isSwitchingZJL = true;
                final String status = RootCmd.execRootCmdWithResults("sh -c "
                        + (enable ? ENABLE_ZJL : DISABLE_ZJL)
                        + "\n");
                if (null == listener) {
                    isSwitchingZJL = false;
                    return;
                }
                Log.d(TAG, "enableZJL: status = " + status);
                MainApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(status)) {
                            listener.onResult(status, false);
                            isSwitchingZJL = false;
                            return;
                        }
                        final boolean success = enable
                                ? (!status.contains(STATUS_DISABLED))
                                : (status.contains(STATUS_DISABLED));
                        listener.onResult(status, success);
                        isSwitchingZJL = false;
                    }
                });
            }
        });

    }

    public static void queryZJLStatus(final IZJLCallback listener) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                if (null == listener) {
                    return;
                }
                final String status = RootCmd.execRootCmdWithResults("sh -c " + STATUS_ZJL + "\n");
                Log.d(TAG, "queryZJLStatus: status = " + status);
                MainApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(status)) {
                            listener.onResult(status, false);
                            return;
                        }
                        listener.onResult(status, !status.contains(STATUS_DISABLED));
                    }
                });
            }
        });

    }

    public interface IZJLCallback {
        void onResult(String status, boolean result);
    }


    private static boolean isZJLSupported() {
        //runs on UI Thread
        String ZJL_ROOT_PATH = RootCmd.execRootCmdWithResults(
                "find /system/xbin/ -name "
                        + ON + "  | awk -F '/[^/]*$' '{print $1}' | head -1");
        if (TextUtils.isEmpty(ZJL_ROOT_PATH)
                || !new File(ZJL_ROOT_PATH).exists()) {
            return false;
        }
        ZJL_BIN_PATH = ZJL_ROOT_PATH;
        ENABLE_ZJL = ZJL_BIN_PATH + "/" + ON;
        DISABLE_ZJL = ZJL_BIN_PATH + "/" + OFF;
        STATUS_ZJL = ZJL_BIN_PATH + "/" + STATUS;
        Log.d(TAG, "ZJL_BIN_PATH = " + ZJL_BIN_PATH);
        return new File(ENABLE_ZJL).exists()
                && new File(DISABLE_ZJL).exists()
                && new File(STATUS_ZJL).exists();
    }

}
