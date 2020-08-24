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
import android.util.Log;

import org.lineageos.settings.device.utils.ISetPreferredNetworkResultListener;
import org.lineageos.settings.device.utils.ToastUtils;
import org.lineageos.settings.device.utils.Utils;

public class LeecoPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "LeecoPreferenceFragment";
    private static final String KEY_CAMHAL3_ENABLE = "key_camera_hal3_enable";
    private static final String KEY_CDMA_ENABLE = "key_cdma_enable";

    private SwitchPreference mCamHal3Enable;
    private SwitchPreference mCDMA;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.leeco_settings_panel);
        Log.d(TAG, "onCreate+++");
        final PreferenceScreen prefSet = getPreferenceScreen();
        mCamHal3Enable = (SwitchPreference) findPreference(KEY_CAMHAL3_ENABLE);
        mCamHal3Enable.setChecked(SettingsUtils.cameraHAL3Enable());
        mCDMA = findPreference(KEY_CDMA_ENABLE);
        if (SettingsUtils.supportSwitchCDMAFeature()) {
            mCDMA.setOnPreferenceChangeListener(mPrefListener);
        } else {
            prefSet.removePreference(mCDMA);
            mCDMA = null;
        }
        Log.d(TAG, "onCreate: cam hal3 enable = " + SettingsUtils.cameraHAL3Enable());
        mCamHal3Enable.setOnPreferenceChangeListener(mPrefListener);
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
        Log.d(TAG, "onResume+++");
        getListView().setPadding(0, 0, 0, 0);
        if (null != mCamHal3Enable) {
            mCamHal3Enable.setChecked(SettingsUtils.cameraHAL3Enable());
            Log.d(TAG, "onResume: cam hal3 enable = " + SettingsUtils.cameraHAL3Enable());
        }
        if (null != mCDMA) {
            mCDMA.setChecked(SettingsUtils.isCDMAEnabled());
        }
        Log.d(TAG, "onResume---");
    }

    private Preference.OnPreferenceChangeListener mPrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            final String key = preference.getKey();

            if (KEY_CAMHAL3_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
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
            }
            return true;
        }
    };
}
