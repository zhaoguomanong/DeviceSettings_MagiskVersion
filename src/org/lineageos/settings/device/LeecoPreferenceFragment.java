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

package org.lineageos.settings.device;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import org.lineageos.settings.device.utils.IPDetailBean;
import org.lineageos.settings.device.utils.ISetPreferredNetworkResultListener;
import org.lineageos.settings.device.utils.SettingsProviderUtils;
import org.lineageos.settings.device.utils.ThreadPoolUtil;
import org.lineageos.settings.device.utils.ToastUtils;
import org.lineageos.settings.device.utils.Utils;
import org.lineageos.settings.device.utils.ZJLUtils;
import android.text.format.DateUtils;
import java.lang.ref.WeakReference;
import static org.lineageos.settings.device.SettingsUtils.HTTP_PROXY_PORT;

public class LeecoPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "LeecoPreferenceFragment";
    private static final String KEY_CAMHAL3_ENABLE = "key_camera_hal3_enable";
    private static final String KEY_CDMA_ENABLE = "key_cdma_enable";
    private static final String KEY_HTTP_PROXY_ENABLE = "key_http_proxy_enable";
    private static final String KEY_ZJL_ENABLE = "key_zjl_enable";
    private static final String KEY_DATA_PICK = "key_data_pick";
    private static final String KEY_UPTIME = "up_time";

    private Activity mActivity;

    private SwitchPreference mCamHal3Enable;
    private SwitchPreference mCDMA;
    private SwitchPreference mHttpProxy;
    private SwitchPreference mZJL;
    private Preference mDataPick;
    private Preference mUptime;

    private boolean mPaused = true;
    private boolean mQueryIpWhenOnResume = false;

    private ConnectivityManager mConnectivityManager;

    private static final int EVENT_UPDATE_STATS = 500;
    private Handler mHandler;

    private ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            final boolean HAS_NET = Utils.hasNetWork();
            Log.d(TAG, "NetworkCallback: onAvailable ---> hasNet = " + HAS_NET);
            onNetworkChange(HAS_NET);

        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            final boolean HAS_NET = Utils.hasNetWork();
            Log.d(TAG, "NetworkCallback: onLost ---> hasNet = " + HAS_NET);
            onNetworkChange(HAS_NET);
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) {
                    Log.d(TAG, "onCapabilitiesChanged ---> WIFI");
                } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.d(TAG, "onCapabilitiesChanged ---> MOBILE");
                } else {
                    Log.d(TAG, "onCapabilitiesChanged ---> UNKNOWN");
                }
            }
        }
    };



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.leeco_settings_panel);
        Log.d(TAG, "onCreate+++");
        mActivity = getActivity();
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
        if (SettingsUtils.supportHttpProxyToggle()) {
            mHttpProxy.setOnPreferenceChangeListener(mPrefListener);
            mHttpProxy.setSummary(getString(R.string.http_proxy_summary,
                    HTTP_PROXY_PORT));
        } else {
            prefSet.removePreference(mHttpProxy);
            mHttpProxy = null;
        }
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
        mUptime = findPreference(KEY_UPTIME);
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
        if (ZJLUtils.ZJL_SUPPORTED) {
            if (Utils.hasNetWork()) {
                mQueryIpWhenOnResume = true;
                getPublicIpRegionName(true);
            } else {
                resetZJLSummary();
            }
        }
        if (ZJLUtils.ZJL_SUPPORTED) {
            mConnectivityManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null != mConnectivityManager) {
                mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(),
                        mNetworkCallback);
            }
        }
        Log.d(TAG, "onResume---");
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        if (ZJLUtils.ZJL_SUPPORTED) {
            if (null != mConnectivityManager) {
                mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            }
        }
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
                resetZJLSummary();
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
                            getPublicIpRegionName(false);
                        }

                    }
                }, enabled);
            }
            return true;
        }
    };

    private void getPublicIpRegionName(boolean calledByOnResume) {
        ThreadPoolUtil.post(new Runnable() {
            @Override
            public void run() {
                final IPDetailBean ipDetailBean = Utils.getPublicIpRegionName();
                if (null == ipDetailBean
                        || TextUtils.isEmpty(ipDetailBean.getAddr())
                        || TextUtils.isEmpty(ipDetailBean.getIp())) {
                    Log.d(TAG, "get ipDetailBean failed");
                    resetZJLSummary();
                    if (calledByOnResume) {
                        mQueryIpWhenOnResume = false;
                    }
                    return;
                }
                Log.d(TAG, "ip = " + ipDetailBean.getIp() + ", cityName = " + ipDetailBean.getAddr());
                MainApplication.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mPaused) {
                            if (calledByOnResume) {
                                mQueryIpWhenOnResume = false;
                            }
                            return;
                        }
                        String targetSummary = mActivity.getString(R.string.zjl_summary);
                        targetSummary = targetSummary +  "\n"
                                + "──────────────\n"
                                + ipDetailBean.getIp() + "\n"
                                + ipDetailBean.getAddr();
                        mZJL.setSummary(targetSummary);
                        if (calledByOnResume) {
                            mQueryIpWhenOnResume = false;
                        }

                    }
                });

            }
        });
    }

    private void resetZJLSummary() {
        MainApplication.post(new Runnable() {
            @Override
            public void run() {
                if (mPaused) {
                    return;
                }
                if (null == mZJL) {
                    return;
                }
                //reset the summary
                mZJL.setSummary(mActivity.getString(R.string.zjl_summary));
            }
        });

    }

    private void onNetworkChange(final boolean HAS_NET) {
        if (HAS_NET) {
            if (!mQueryIpWhenOnResume) {
                getPublicIpRegionName(false);
            } else {
                Log.d(TAG, "onNetworkChange onResume query ip in progress, ignore this msg");
            }
        } else {
            resetZJLSummary();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getHandler().sendEmptyMessage(EVENT_UPDATE_STATS);
    }

    @Override
    public void onStop() {
        super.onStop();
        getHandler().removeMessages(EVENT_UPDATE_STATS);
    }

    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new MyHandler(this);
        }
        return mHandler;
    }

    private void updateTimes() {
        mUptime.setSummary(DateUtils.formatElapsedTime(SystemClock.elapsedRealtime() / 1000));
    }

    private static class MyHandler extends Handler {
        private WeakReference<LeecoPreferenceFragment> mStatus;

        public MyHandler(LeecoPreferenceFragment fragment) {
            mStatus = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            LeecoPreferenceFragment status = mStatus.get();
            if (status == null) {
                return;
            }

            switch (msg.what) {
                case EVENT_UPDATE_STATS:
                    status.updateTimes();
                    sendEmptyMessageDelayed(EVENT_UPDATE_STATS, 1000);
                    break;

                default:
                    throw new IllegalStateException("Unknown message " + msg.what);
            }
        }
    }
}
