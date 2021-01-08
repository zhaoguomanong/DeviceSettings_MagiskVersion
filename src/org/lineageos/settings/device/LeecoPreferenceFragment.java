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

package org.lineageos.settings.device;

import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import android.text.TextUtils;
import android.util.Log;
import org.lineageos.settings.device.utils.ISetPreferredNetworkResultListener;
import org.lineageos.settings.device.utils.SettingsProviderUtils;
import org.lineageos.settings.device.utils.ThreadPoolUtil;
import org.lineageos.settings.device.utils.ToastUtils;
import org.lineageos.settings.device.utils.Utils;
import org.lineageos.settings.device.utils.ZJLUtils;

import static org.lineageos.settings.device.SettingsUtils.HTTP_PROXY_PORT;

public class LeecoPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "LeecoPreferenceFragment";
    private static final String KEY_CAMHAL3_ENABLE = "key_camera_hal3_enable";
    private static final String KEY_CDMA_ENABLE = "key_cdma_enable";
    private static final String KEY_HTTP_PROXY_ENABLE = "key_http_proxy_enable";
    private static final String KEY_ZJL_ENABLE = "key_zjl_enable";
    private static final String KEY_DATA_PICK = "key_data_pick";

    private SwitchPreference mCamHal3Enable;
    private SwitchPreference mCDMA;
    private SwitchPreference mHttpProxy;
    private SwitchPreference mZJL;
    private Preference mDataPick;

    private boolean mPaused = true;
    private String mCurrentPublicIp = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.leeco_settings_panel);
        Log.d(TAG, "onCreate+++");
        final PreferenceScreen prefSet = getPreferenceScreen();
        mCamHal3Enable = (SwitchPreference) findPreference(KEY_CAMHAL3_ENABLE);
        if (SettingsUtils.supportCamHal3Toggle()) {
            mCamHal3Enable.setChecked(SettingsUtils.cameraHAL3Enable());
            Log.d(TAG, "onCreate: cam hal3 enable = " + SettingsUtils.cameraHAL3Enable());
            mCamHal3Enable.setOnPreferenceChangeListener(mPrefListener);
        } else {
            prefSet.removePreference(mCamHal3Enable);
            mCamHal3Enable = null;
        }
        mCDMA = findPreference(KEY_CDMA_ENABLE);
        mHttpProxy = findPreference(KEY_HTTP_PROXY_ENABLE);
        mHttpProxy.setOnPreferenceChangeListener(mPrefListener);
        mHttpProxy.setSummary(getString(R.string.http_proxy_summary,
                HTTP_PROXY_PORT));
        if (SettingsUtils.supportSwitchCDMAFeature()) {
            mCDMA.setOnPreferenceChangeListener(mPrefListener);
        } else {
            prefSet.removePreference(mCDMA);
            mCDMA = null;
        }
        mZJL = findPreference(KEY_ZJL_ENABLE);
        if (ZJLUtils.ZJL_SUPPORTED) {
            mZJL.setOnPreferenceChangeListener(mPrefListener);
        } else {
            prefSet.removePreference(mZJL);
            mZJL = null;
        }
        mDataPick = findPreference(KEY_DATA_PICK);
        if (SettingsUtils.supportDataPickFeature()) {
            mDataPick.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    SettingsProviderUtils.sendDataPickBroadcast();
                    return true;
                }
            });
        } else {
            prefSet.removePreference(mDataPick);
            mDataPick = null;
        }
        Log.d(TAG, "onCreate---");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        Log.d(TAG, "onResume+++");
        getListView().setPadding(0, 0, 0, 0);
        if (SettingsUtils.supportCamHal3Toggle()
                && null != mCamHal3Enable) {
            mCamHal3Enable.setChecked(SettingsUtils.cameraHAL3Enable());
            Log.d(TAG, "onResume: cam hal3 enable = " + SettingsUtils.cameraHAL3Enable());
        }
        if (null != mCDMA) {
            mCDMA.setChecked(SettingsUtils.isCDMAEnabled());
        }
        if (null != mHttpProxy) {
            mHttpProxy.setChecked(SettingsUtils.isHttpProxyEnabled());
        }
        if (ZJLUtils.ZJL_SUPPORTED) {
            if (ZJLUtils.isSwitchingZJL) {
                Log.e(TAG, "switching ZJL, no need ZJL query result");
            } else {
                ZJLUtils.queryZJLStatus(new ZJLUtils.IZJLCallback() {
                    @Override
                    public void onResult(String str, boolean enabled) {
                        Log.d(TAG, "queryZJLStatus: str = \n" + str + "\nenabled = " + enabled);
                        if (mPaused) {
                            return;
                        }
                        if (ZJLUtils.isSwitchingZJL) {
                            Log.e(TAG, "switching ZJL, abandon ZJL query result");
                            return;
                        }
                        if (null != mZJL) {
                            boolean isChecked = mZJL.isChecked();
                            if (isChecked != enabled) {
                                mZJL.setChecked(enabled);
                            }
                        }
                    }
                });
            }

        }
        getPublicIp(false);
        Log.d(TAG, "onResume---");
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    private Preference.OnPreferenceChangeListener mPrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            final String key = preference.getKey();

            if (KEY_CAMHAL3_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
                if (Utils.isSwitchingCamHal) {
                    ToastUtils.showLimited(getString(R.string.cam_hal_switching_hint));
                    if (null != mCamHal3Enable) {
                        mCamHal3Enable.setChecked(!enabled);
                    }
                    return false;
                }
                SettingsUtils.writeCameraHAL3Prop(enabled);
                Log.d(TAG, "onPreferenceChange: cam hal3 enable = " + enabled);
            } else if (KEY_CDMA_ENABLE.equals(key)) {
                final boolean enabled = (boolean) value;
                if (Utils.isSwitchingCDMA) {
                    ToastUtils.showLimited(getString(R.string.net_switching_hint));
                    if (null != mCDMA) {
                        mCDMA.setChecked(!enabled);
                    }
                    return false;
                }
                boolean cdmaOld = SettingsUtils.isCDMAEnabled();
                if (cdmaOld == enabled) {
                    Log.e(TAG, "the state of cdma toggle is messed up, return");
                    return false;
                }
                SettingsUtils.setCDMAEnable(enabled, new ISetPreferredNetworkResultListener() {
                    @Override
                    public void onCompleted(boolean result) {
                        if (!result) {
                            MainApplication.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPaused) {
                                        return;
                                    }
                                    ToastUtils.showLimited(getString(R.string.net_switch_failed));
                                    if (null != mCDMA) {
                                        mCDMA.setChecked(!enabled);
                                    }
                                }
                            });
                            Log.e(TAG, "onPreferenceChange: set CDMA state failed, no permission");
                        }
                    }
                });
                Log.d(TAG, "onPreferenceChange: CDMA enable = " + enabled);
            } else if (KEY_HTTP_PROXY_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
                SettingsUtils.setHttpProxyEnable(enabled);

            } else if (KEY_ZJL_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
                if (ZJLUtils.isSwitchingZJL) {
                    ToastUtils.showLimited(getString(R.string.zjl_switching_hint));
                    if (null != mZJL) {
                        mZJL.setChecked(!enabled);
                    }
                    Log.e(TAG, "switching zjl return");
                    return false;
                }
                getActivity().setTitle(getString(R.string.device_settings_app_name));
                ZJLUtils.enableZJL(new ZJLUtils.IZJLCallback() {
                    @Override
                    public void onResult(String status, boolean success) {
                        Log.d(TAG, (enabled ? "enableZJL: " : "disableZJL: ")
                                + "status = \n" + status + "\nsuccess = " + success);
                        if (mPaused) {
                            return;
                        }
                        int hintResId;
                        if (enabled) {
                            hintResId = success
                                    ? R.string.zjl_enable_success
                                    : R.string.zjl_enable_failed;
                        } else {
                            hintResId = success
                                    ? R.string.zjl_disable_success
                                    : R.string.zjl_disable_failed;
                        }
                        ToastUtils.show(
                                getString(hintResId));
                        if (!success) {
                            if (null != mZJL) {
                                mZJL.setChecked(!enabled);
                            }
                        } else {
                            getPublicIp(true);
                        }

                    }
                }, enabled);
            }
            return true;
        }
    };

    private void getPublicIp(final boolean triggeredBySwitchingZJL) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                final String ip = Utils.getPublicIp();
                if (triggeredBySwitchingZJL
                        && TextUtils.equals(mCurrentPublicIp, ip)
                        && !mPaused) {
                    Log.e(TAG, "triggered by switching ZJL, ip not changed, post delay msg, mCurrentPublicIp = "
                            + mCurrentPublicIp + ", ip = " + ip);
                    MainApplication.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPublicIp(true);
                        }
                    }, 1000);
                    return;
                }
                if (TextUtils.isEmpty(ip)) {
                    Log.d(TAG, "get ip failed");
                    return;
                }
                MainApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mPaused) {
                            return;
                        }
                        String originalTitle = getString(R.string.device_settings_app_name);
                        String newTitle = originalTitle + "  [" + ip + "]";
                        Log.d(TAG, "set title to " + newTitle);
                        getActivity().setTitle(newTitle);
                        mCurrentPublicIp = ip;
                    }
                });

            }
        });

    }
}
