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

import android.text.TextUtils;
import android.util.Log;
import org.lineageos.settings.device.utils.RootCmd;
import org.lineageos.settings.device.utils.SystemProperties;

public class SettingsUtils {
    public static final String TAG = "SettingsUtils";

    private static final String CAMERA_HAL3_ENABLE_PROPERTY = "persist.camera.HAL3.enabled";

    public static void writeCameraHAL3Prop(boolean enable) {
        SystemProperties.set(CAMERA_HAL3_ENABLE_PROPERTY, enable ? "1" : "0");
        restartCameraServer();
    }

    public static boolean cameraHAL3Enable() {
        String enable = SystemProperties.get(CAMERA_HAL3_ENABLE_PROPERTY, "1");
        return TextUtils.equals("1", enable);
    }

    public static void restartCameraServer() {
        boolean hasRoot = RootCmd.haveRoot();
        if (!hasRoot) {
            Log.e(TAG, "restartCameraServer: no root");
            return;
        }

        boolean success = RootCmd.execRootCmd("stop vendor.camera-provider-2-4;"
                + " start vendor.camera-provider-2-4\n");
        Log.d(TAG, "restartCameraServer: " + (success ? "success" : "failed"));


    }

}
