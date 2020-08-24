#!/bin/bash

adb root
adb shell mount -o rw,remount /
adb shell mkdir -p /system/priv-app/DeviceSettings_MagiskVersion
adb push ./privapp-permissions-org.lineageos.settings.device.magisk.xml /etc/permissions/
adb push ../build/outputs/apk/release/DeviceSettings_Magisk.apk /system/priv-app/DeviceSettings_MagiskVersion/
adb reboot