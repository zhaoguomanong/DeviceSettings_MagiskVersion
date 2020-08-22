package org.lineageos.settings.device;

import android.app.Application;

import org.lineageos.settings.device.utils.Utils;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.applicationContext = this;
    }
}
