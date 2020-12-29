package org.lineageos.settings.device.utils;


import android.text.TextUtils;
import android.util.Log;

import org.lineageos.settings.device.MainApplication;

import java.io.File;

public class ZJLUtils {

    private static final String TAG = "ZJLUtils";

    private static final String ZJL_BIN_PATH = "/system/xbin/ZJL2.0_magisk/";
    private static final String ENABLE_ZJL = ZJL_BIN_PATH + "开启.sh";
    private static final String DISABLE_ZJL = ZJL_BIN_PATH + "关闭.sh";
    private static final String STATUS_ZJL = ZJL_BIN_PATH + "检测.sh";
    private static final String STATUS_DISABLED = "啥都没开";


    public static final boolean ZJL_SUPPORTED = isZJLSupported();

    public static void enableZJL(IEnableZJLCallback listener) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                final String status = RootCmd.execRootCmdWithResults("sh -c " + ENABLE_ZJL + "\n");
                if (null == listener) {
                    return;
                }
                Log.d(TAG, "enableZJL: status = " + status);
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

    public static void disableZJL(IDisableZJLCallback listener) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                final String status = RootCmd.execRootCmdWithResults("sh -c " + DISABLE_ZJL + "\n");
                if (null == listener) {
                    return;
                }
                MainApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(status)) {
                            listener.onResult(status, false);
                            return;
                        }
                        listener.onResult(status, status.contains(STATUS_DISABLED));
                    }
                });


            }
        });

    }

    public static void queryZJLStatus(final IZJLStatusListener listener) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null == listener) {
                        return;
                    }
                    Log.d(TAG, "execRootCmdWithResults+++");

                    final String status = RootCmd.execRootCmdWithResults("sh " + STATUS_ZJL + "\n");
                    Log.d(TAG, "execRootCmdWithResults---");
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
                } catch (Exception E) {

                }

            }
        });

    }

    public interface IZJLStatusListener {
        void onResult(String str, boolean enabled);
    }

    public interface IEnableZJLCallback {
        void onResult(String str, boolean success);
    }

    public interface IDisableZJLCallback {
        void onResult(String str, boolean success);
    }

    private static boolean isZJLSupported() {
        boolean isSupported = new File(ENABLE_ZJL).exists()
                && new File(DISABLE_ZJL).exists()
                && new File(STATUS_ZJL).exists();
        return isSupported;
    }

}
