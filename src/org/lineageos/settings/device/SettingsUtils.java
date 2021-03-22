/*
 * Copyright (c) 2021 The LineageOS Project
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

import android.annotation.SuppressLint;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import org.lineageos.settings.device.utils.ISetPreferredNetworkResultListener;
import org.lineageos.settings.device.utils.MobileDevice;
import org.lineageos.settings.device.utils.Operator;
import org.lineageos.settings.device.utils.RootCmd;
import org.lineageos.settings.device.utils.SettingsProviderUtils;
import org.lineageos.settings.device.utils.SystemProperties;
import org.lineageos.settings.device.utils.ThreadPoolUtil;
import org.lineageos.settings.device.utils.Utils;
import java.util.List;

@SuppressLint("MissingPermission")
public class SettingsUtils {
    public static final String TAG = "SettingsUtils";

    private static final String CAMERA_HAL3_ENABLE_PROPERTY = "persist.camera.HAL3.enabled";
    public static final String HTTP_PROXY_PORT
            = MainApplication.getInstance().getString(R.string.http_proxy_port);
    private static final String HTTP_PROXY_ADDRESS_ENABLED = "127.0.0.1:" + HTTP_PROXY_PORT;
    private static final String HTTP_PROXY_ADDRESS_DISABLED = ":0";

    public static void writeCameraHAL3Prop(final boolean enable) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                Utils.isSwitchingCamHal = true;
                try {
                    SystemProperties.set(CAMERA_HAL3_ENABLE_PROPERTY, enable ? "1" : "0");
                    restartCameraServer();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Utils.isSwitchingCamHal = false;
                }

            }
        });

    }

    public static boolean cameraHAL3Enable() {
        String enable = SystemProperties.get(CAMERA_HAL3_ENABLE_PROPERTY, "1");
        return TextUtils.equals("1", enable);
    }

    public static boolean supportHttpProxyToggle() {
        return RootCmd.haveRoot();
    }

    public static boolean supportCamHal3Toggle() {
        if (!RootCmd.haveRoot()) {
            return false;
        }
        return isLeEcoCustomROM();
    }

    public static boolean isLeEcoCustomROM() {
        switch (MobileDevice.CurrentDevice) {
            case X2:
            case S2:
            case ZL1:
                return Build.VERSION.SDK_INT > Build.VERSION_CODES.M;
        }
        return false;
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

    public static void setCDMAEnable(boolean enable, final ISetPreferredNetworkResultListener listener) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                Utils.isSwitchingCDMA = true;
                try {
                    Log.d(TAG, "setCDMAEnable: " + enable);
                    boolean hasRoot = RootCmd.haveRoot();
                    if (!hasRoot) {
                        Log.e(TAG, "setCDMAEnable: no root");
                        return;
                    }
                    List<SubscriptionInfo> list = SubscriptionManager.from(Utils.applicationContext).getActiveSubscriptionInfoList();
                    int chinaTelecomSubId = -1;
                    int chinaUnicomSubId = -1;
                    for (SubscriptionInfo subscriptionInfo : list) {
                        int subId = subscriptionInfo.getSubscriptionId();
                        Operator operator = Utils.toOperator(subscriptionInfo);
                        if (operator == Operator.CHINA_TELECOM) {
                            chinaTelecomSubId = subId;
                        } else if (operator == Operator.CHINA_UNICOM) {
                            chinaUnicomSubId = subId;
                        }
                        Log.d(TAG, "subId = " + subId
                                + ", operator = " + operator);
                    }
                    if (chinaTelecomSubId < 0
                            || chinaUnicomSubId < 0) {
                        Log.e(TAG, "find subId failed");
                        return;
                    }
                    final int CT_SUBID = chinaTelecomSubId;
                    final int CU_SUBID = chinaUnicomSubId;

                    boolean result;
                    if (enable) {
                        result = SettingsProviderUtils.setPreferredNetwork(
                                CU_SUBID, Utils.NETWORK_MODE_GSM_ONLY,
                                CT_SUBID, Utils.NETWORK_MODE_GLOBAL);
                    } else {
                        result = SettingsProviderUtils.setPreferredNetwork(
                                CU_SUBID, Utils.NETWORK_MODE_GLOBAL,
                                CT_SUBID, Utils.NETWORK_MODE_LTE_ONLY);
                    }
                    if (null != listener) {
                        listener.onCompleted(result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Utils.isSwitchingCDMA = false;
                }
            }
        });

    }

    public static boolean supportSwitchCDMAFeature() {
        if (!RootCmd.haveRoot()) {
            return false;
        }
        if (!isLeEcoCustomROM()) {
            return false;
        }
        //only dual simcards and china telecom + china unicom support this feature
        boolean hasChinaTelecom = false;
        boolean hasChinaUnicom = false;

        List<SubscriptionInfo> list = SubscriptionManager.from(Utils.applicationContext)
                .getActiveSubscriptionInfoList();
        if (null == list || list.isEmpty()) {
            return false;
        }
        for (SubscriptionInfo subscriptionInfo : list) {
            int subId = subscriptionInfo.getSubscriptionId();
            Operator operator = Utils.toOperator(subscriptionInfo);
            if (operator == Operator.CHINA_TELECOM) {
                hasChinaTelecom = true;
            } else if (operator == Operator.CHINA_UNICOM) {
                hasChinaUnicom = true;
            }
        }
        return hasChinaTelecom && hasChinaUnicom;
    }

    public static boolean isCDMAEnabled() {
        List<SubscriptionInfo> list = SubscriptionManager.from(Utils.applicationContext)
                .getActiveSubscriptionInfoList();
        if (null == list || list.isEmpty()) {
            return false;
        }
        int chinaTelecomSubId = -1;
        int chinaUnicomSubId = -1;
        for (SubscriptionInfo subscriptionInfo : list) {
            int subId = subscriptionInfo.getSubscriptionId();
            Operator operator = Utils.toOperator(subscriptionInfo);
            if (operator == Operator.CHINA_TELECOM) {
                chinaTelecomSubId = subId;
            } else if (operator == Operator.CHINA_UNICOM) {
                chinaUnicomSubId = subId;
            }
            Log.d(TAG, "subId = " + subId
                    + ", operator = " + operator);
        }
        if (chinaTelecomSubId < 0
                || chinaUnicomSubId < 0) {
            Log.e(TAG, "find subId failed");
            return false;
        }
        int cTPrefNet = SettingsProviderUtils.getPreferredNetwork(chinaTelecomSubId);
        int cUPrefNet = SettingsProviderUtils.getPreferredNetwork(chinaUnicomSubId);
        return cUPrefNet == Utils.NETWORK_MODE_GSM_ONLY
                && cTPrefNet == Utils.NETWORK_MODE_GLOBAL;

    }

    public static void setHttpProxyEnable(boolean enable) {
        String cmd = "settings put global http_proxy "
                + (enable ? HTTP_PROXY_ADDRESS_ENABLED : HTTP_PROXY_ADDRESS_DISABLED) + "\n";
        RootCmd.execRootCmd(cmd);
    }

    public static boolean isHttpProxyEnabled() {
        String cmd = "settings get global http_proxy";
        String result = RootCmd.execRootCmdWithResults(cmd);
        return TextUtils.equals(result, HTTP_PROXY_ADDRESS_ENABLED);
    }

    public static boolean supportDataPickFeature() {
        if (!RootCmd.haveRoot()) {
            return false;
        }
        if (!isLeEcoCustomROM()) {
            return false;
        }
        List<SubscriptionInfo> list = SubscriptionManager.from(Utils.applicationContext)
                .getActiveSubscriptionInfoList();
        if (null == list || list.isEmpty()) {
            return false;
        }
        //only dual SIM Card case needs this feature
        return list.size() >= 2;
    }

}
