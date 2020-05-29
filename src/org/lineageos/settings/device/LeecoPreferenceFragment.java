/*
 * Copyright (C) 2018 The LineageOS Project
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

public class LeecoPreferenceFragment extends PreferenceFragment {

    private static final String TAG = "LeecoPreferenceFragment";

    private static final String KEY_CAMERA_FOCUS_FIX_ENABLE = "camera_focus_enable";
    private static final String KEY_QUICK_CHARGE_ENABLE = "quick_charge_enable";
    private static final String KEY_CAMHAL3_ENABLE = "key_camera_hal3_enable";

    private SwitchPreference mCameraFocusFixEnable;
    private SwitchPreference mQuickChargeEnable;
    private SwitchPreference mCamHal3Enable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.leeco_settings_panel);
        Log.d(TAG, "onCreate+++");
        final PreferenceScreen prefSet = getPreferenceScreen();
        mCameraFocusFixEnable = (SwitchPreference) findPreference(KEY_CAMERA_FOCUS_FIX_ENABLE);
        mQuickChargeEnable = (SwitchPreference) findPreference(KEY_QUICK_CHARGE_ENABLE);
        mCamHal3Enable = (SwitchPreference) findPreference(KEY_CAMHAL3_ENABLE);

        if (SettingsUtils.supportCamHalLevelSwitch()) {
            mCamHal3Enable.setChecked(SettingsUtils.cameraHAL3Enable());
            Log.d(TAG, "onCreate: cam hal3 enable = " + SettingsUtils.cameraHAL3Enable());
            mCamHal3Enable.setOnPreferenceChangeListener(mPrefListener);
        } else {
            prefSet.removePreference(mCamHal3Enable);
        }


        if (SettingsUtils.supportsCameraFocusFix()) {
            mCameraFocusFixEnable.setChecked(SettingsUtils.getCameraFocusFixEnabled());
            mCameraFocusFixEnable.setOnPreferenceChangeListener(mPrefListener);
            Log.d(TAG, "onCreate: cam focus fix enable = "
                    + SettingsUtils.getCameraFocusFixEnabled());
        } else {
            prefSet.removePreference(mCameraFocusFixEnable);
        }

        if (SettingsUtils.supportsQuickChargeSwitch()) {
            mQuickChargeEnable.setChecked(SettingsUtils.getQuickChargeEnabled());
            mQuickChargeEnable.setOnPreferenceChangeListener(mPrefListener);
            Log.d(TAG, "onCreate: quick charge enable = "
                    + SettingsUtils.getQuickChargeEnabled());
        } else {
            prefSet.removePreference(mQuickChargeEnable);
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
        Log.d(TAG, "onResume+++");
        getListView().setPadding(0, 0, 0, 0);
//        //prop maybe modify by adb when onpause, so check from prop again
//        if (SettingsUtils.supportCamHalLevelSwitch()
//                && null != mCamHal3Enable) {
//            mCamHal3Enable.setChecked(SettingsUtils.cameraHAL3Enable());
//            Log.d(TAG, "onResume: cam hal3 enable = " + SettingsUtils.cameraHAL3Enable());
//        }
//
//        if (SettingsUtils.supportsCameraFocusFix()
//                && null != mCameraFocusFixEnable) {
//            mCameraFocusFixEnable.setChecked(SettingsUtils.getCameraFocusFixEnabled());
//            Log.d(TAG, "onResume: cam focus fix enable = "
//                    + SettingsUtils.getCameraFocusFixEnabled());
//        }
//
//        if (SettingsUtils.supportsQuickChargeSwitch()
//                && null != mQuickChargeEnable) {
//            mQuickChargeEnable.setChecked(SettingsUtils.getQuickChargeEnabled());
//            Log.d(TAG, "onResume: quick charge enable = "
//                    + SettingsUtils.getQuickChargeEnabled());
//        }
        Log.d(TAG, "onResume---");
    }

    private Preference.OnPreferenceChangeListener mPrefListener =
        new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            final String key = preference.getKey();

            if (KEY_CAMERA_FOCUS_FIX_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
//                SettingsUtils.setCameraFocusFixEnabled(getActivity(), enabled);
                SettingsUtils.writeCameraFocusFixSysfs(enabled);
                Log.d(TAG, "onPreferenceChange: cam focus fix enable = " + enabled);
            } else if (KEY_QUICK_CHARGE_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
//                SettingsUtils.setQuickChargeEnabled(getActivity(), enabled);
                SettingsUtils.writeQuickChargeProp(enabled);
                Log.d(TAG, "onPreferenceChange: quick charge enable = " + enabled);
            } else if (KEY_CAMHAL3_ENABLE.equals(key)) {
                boolean enabled = (boolean) value;
                SettingsUtils.writeCameraHAL3Prop(enabled);
                Log.d(TAG, "onPreferenceChange: cam hal3 enable = " + enabled);
            }

            return true;
        }
    };
}
