package org.lineageos.settings.device.utils;

/*
 * Copyright (C) 2021 The LineageOS Project
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


import android.annotation.SuppressLint;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.lineageos.settings.device.MainApplication;
import static android.content.Context.TELEPHONY_SERVICE;

@SuppressLint("MissingPermission")
public class SettingsProviderUtils {

    private static String TAG = "SettingProviderUtils";

    private static final String PREFERRED_NETWORK_MODE_SETTING_GLOBAL_KEY = "preferred_network_mode";
    private static final int SWITCH_NET_BLOCKING_MAX_SECONDS = 60;
    private static final int ONE_SECOND = 1000;

    private static final String CMD_DEFAULT_DATA_PICK = "am broadcast " +
            "-a android.telephony.action.PRIMARY_SUBSCRIPTION_LIST_CHANGED " +
            "-n com.android.settings/com.android.settings.sim.SimSelectNotification " +
            "--ei android.telephony.extra.DEFAULT_SUBSCRIPTION_SELECT_TYPE 1\n";

    public static boolean setPreferredNetwork(int subIdCU, int networkCU, int subIdCT, int networkCT) {
        Log.d(TAG, "setPreferredNetwork: subIdCU = " + subIdCU + ", networkCU = " + networkCU
                + ", subIdCT = " + subIdCT + ", networkCT = " + networkCT);
        final TelephonyManager tm = (TelephonyManager) MainApplication.getInstance().getSystemService(TELEPHONY_SERVICE);
        if (null == tm) {
            return false;
        }
        final TelephonyManager tm1 = tm.createForSubscriptionId(subIdCU);
        final TelephonyManager tm2 = tm.createForSubscriptionId(subIdCT);
        if (null == tm1
                || null == tm2) {
            return false;
        }
        String cmd = "settings put global preferred_network_mode" + subIdCU + " " + networkCU + ";"
                + "settings put global preferred_network_mode" + subIdCT + " " + networkCT
                + ";stop ril-daemon;start ril-daemon\n";
        Log.d(TAG, "setPreferredNetwork: cmd = " + cmd);
        boolean result = RootCmd.execRootCmd(cmd);
        final boolean enableCDMA = networkCT == Utils.NETWORK_MODE_GLOBAL;
        if (result) {
            try {
                int waitedSeconds = 0;
                while (true) {
                    Thread.sleep(ONE_SECOND);
                    waitedSeconds++;
                    int simStateCU = tm1.getServiceState().getState();
                    int simStateCT = tm2.getServiceState().getState();
                    Log.d(TAG, "switching network: simStateCU = " + Utils.serviceState2Str(simStateCU)
                            + ", simStateCT = " + Utils.serviceState2Str(simStateCT)
                            + ", enableCDMA = " + enableCDMA);
                    if (enableCDMA) {
                        if (simStateCU == ServiceState.STATE_OUT_OF_SERVICE
                                && simStateCT == ServiceState.STATE_IN_SERVICE) {
                            Log.d(TAG, "enableCDMA: switch done");
                            break;
                        }
                    } else {
                        if (simStateCU == ServiceState.STATE_IN_SERVICE
                                && simStateCT == ServiceState.STATE_OUT_OF_SERVICE) {
                            Log.d(TAG, "disableCDMA switch done");
                            break;
                        }
                    }
                    if (waitedSeconds > SWITCH_NET_BLOCKING_MAX_SECONDS) {
                        Log.d(TAG, "setPreferredNetwork: wait timeout");
                        break;
                    }
                }
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

    public static void sendDataPickBroadcast() {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                RootCmd.execRootCmdSilent(CMD_DEFAULT_DATA_PICK);
            }
        });

    }
}