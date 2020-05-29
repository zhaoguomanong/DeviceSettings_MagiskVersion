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
import android.preference.PreferenceActivity;
import android.widget.Toast;

import org.lineageos.settings.device.utils.RootCmd;

public class LeecoPreferenceActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LeecoPreferenceFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!RootCmd.haveRoot()) {
            Toast.makeText(this,
                    "Can't get root privileges, install magisk and grant this app with privileges",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
