/*
 * Copyright (c) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import org.lineageos.settings.device.utils.RootCmd;
import org.lineageos.settings.device.utils.SystemProperties;

public class SettingsUtils {
    public static final String TAG = "SettingsUtils";
//    public static final String CAMERA_FOCUS_FIX_ENABLED =
//            "CAMERA_FOCUS_FIX_ENABLED";
//    public static final String QUICK_CHARGE_ENABLED = "QUICK_CHARGE_ENABLED";

    public static final String CAMERA_FOCUS_FIX_SYSFS =
            "/sys/module/msm_actuator/parameters/use_focus_fix";
    public static final String QUICK_CHARGE_SYSFS =
            "/sys/class/power_supply/le_ab/le_quick_charge_mode";

    private static final String QC_SYSTEM_PROPERTY = "persist.sys.le_fast_chrg_enable";

    private static final String CAMERA_HAL3_ENABLE_PROPERTY = "persist.camera.HAL3.enabled";

    //public static final String PREFERENCES = "SettingsUtilsPreferences";

    public static void writeCameraFocusFixSysfs(boolean enabled) {
        if (!supportsCameraFocusFix()) return;
        if (!RootCmd.haveRoot()) {
            Log.e(TAG, "writeCameraFocusFixSysfs failed, no root privileges");
            return;
        }

        boolean status = RootCmd.execRootCmd("echo " + (enabled ? '1' : '0') + " > " + CAMERA_FOCUS_FIX_SYSFS + "\n");
        Log.d(TAG, "writeCameraFocusFixSysfs: "
                + enabled
                + " "
                + (status ? "success" : "failed"));


//        try {
//            FileOutputStream out = new FileOutputStream(new File(CAMERA_FOCUS_FIX_SYSFS));
//            OutputStreamWriter writer = new OutputStreamWriter(out);
//
//            writer.write(enabled ? '1' : '0');
//
//            writer.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static void writeQuickChargeProp(boolean enabled) {
        SystemProperties.set(QC_SYSTEM_PROPERTY, enabled ? "1" : "0");
    }

    public static void writeCameraHAL3Prop(boolean enable) {
        SystemProperties.set(CAMERA_HAL3_ENABLE_PROPERTY, enable ? "1" : "0");
        restartCameraServer();
    }

    public static boolean cameraHAL3Enable() {
        String enable = SystemProperties.get(CAMERA_HAL3_ENABLE_PROPERTY, "1");
        return "1".equals(enable);
    }

    public static boolean supportsCameraFocusFix() {
//        File focusFixPath = new File(CAMERA_FOCUS_FIX_SYSFS);
//        return focusFixPath.exists();
        return rootCheckFileExists(CAMERA_FOCUS_FIX_SYSFS);
    }

    public static boolean supportsQuickChargeSwitch() {
//        File QCPath = new File(QUICK_CHARGE_SYSFS);
//        return QCPath.exists();
        return rootCheckFileExists(QUICK_CHARGE_SYSFS);
    }

    public static boolean supportCamHalLevelSwitch() {
        return true;
    }

//    public static boolean setCameraFocusFixEnabled(Context context, boolean enabled) {
//        return putInt(context, CAMERA_FOCUS_FIX_ENABLED, enabled ? 1 : 0);
//    }

    public static boolean getCameraFocusFixEnabled() {
        String currentValue = rootReadFile(CAMERA_FOCUS_FIX_SYSFS);
        if (TextUtils.isEmpty(currentValue)) {
            return false;
        }
        return TextUtils.equals(currentValue, "1")
                || TextUtils.equals(currentValue.toUpperCase(), "Y");
    }

//    public static boolean setQuickChargeEnabled(Context context, boolean enabled) {
//        return putInt(context, QUICK_CHARGE_ENABLED, enabled ? 1 : 0);
//    }

    public static boolean getQuickChargeEnabled() {
        return TextUtils.equals("1", SystemProperties.get(QC_SYSTEM_PROPERTY, "0"));
    }

//    public static int getInt(Context context, String name, int def) {
//        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
//        return settings.getInt(name, def);
//    }
//
//    public static boolean putInt(Context context, String name, int value) {
//        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putInt(name, value);
//        return editor.commit();
//    }

//    public static void registerPreferenceChangeListener(Context context,
//            SharedPreferences.OnSharedPreferenceChangeListener preferenceListener) {
//        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
//        settings.registerOnSharedPreferenceChangeListener(preferenceListener);
//    }
//
//    public static void unregisterPreferenceChangeListener(Context context,
//            SharedPreferences.OnSharedPreferenceChangeListener preferenceListener) {
//        SharedPreferences settings = context.getSharedPreferences(PREFERENCES, 0);
//        settings.unregisterOnSharedPreferenceChangeListener(preferenceListener);
//    }

    public static void restartCameraServer() {
        boolean hasRoot = RootCmd.haveRoot();
        if (!hasRoot) {
            Log.e(TAG, "restartCameraServer: no root");
            return;
        }

        boolean success = RootCmd.execRootCmd("stop qcamerasvr ;"
                + " start qcamerasvr ;"
                + " stop vendor.camera-provider-2-4 ;"
                + " start vendor.camera-provider-2-4\n");
        Log.d(TAG, "restartCameraServer: " + (success ? "success" : "failed"));


    }

    public static boolean rootCheckFileExists(String filePath) {
        boolean hasRoot = RootCmd.haveRoot();
        if (!hasRoot) {
            Log.e(TAG, "restartCameraServer: no root");
            return false;
        }
        String result = RootCmd.execRootCmdWithResults("[[ -f " + filePath + " ]] && echo 1 || echo 0 \n");
        Log.d(TAG, "rootCheckFileExists: file = " + filePath + ", results = " + result);
        return TextUtils.equals("1", result);
    }

    public static String rootReadFile(String filePath) {
        if (!rootCheckFileExists(filePath)) {
            return null;
        }
        String content = RootCmd.execRootCmdWithResults("cat " + filePath + "\n");
        return content;
    }

}
