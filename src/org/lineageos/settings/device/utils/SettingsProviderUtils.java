package org.lineageos.settings.device.utils;

/*
 * Copyright (C) 2020 The LineageOS Project
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


import android.provider.Settings;
import android.util.Log;

public class SettingsProviderUtils {

    private static String TAG = "SettingProviderUtils";

    private static final String PREFERRED_NETWORK_MODE_SETTING_GLOBAL_KEY = "preferred_network_mode";

    private static long SWITCHING_NET_BLOCKING_TIME = 30 * 1000;

    public static boolean setPreferredNetwork(int subId1, int network1, int subId2, int network2) {
        Log.d(TAG, "setPreferredNetwork: subId1 = " + subId1 + ", network1 = " + network1
                + ", subId2 = " + subId2 + ", network2 = " + network2);

        String cmd = "settings put global preferred_network_mode" + subId1 + " " + network1 + ";"
                + "settings put global preferred_network_mode" + subId2 + " " + network2
                + ";stop ril-daemon;start ril-daemon\n";
        Log.d(TAG, "setPreferredNetwork: cmd = " + cmd);
        boolean result = RootCmd.execRootCmd(cmd);
        if (result) {
            try {
                Thread.sleep(SWITCHING_NET_BLOCKING_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static int getPreferredNetwork(int subId) {
        return Settings.Global.getInt(Utils.applicationContext.getContentResolver(),
                PREFERRED_NETWORK_MODE_SETTING_GLOBAL_KEY + subId,
                Utils.NETWORK_MODE_GLOBAL);
    }
}