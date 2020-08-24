#!/bin/bash

cd `dirname $0`
cd ..
PROJECTROOT=`pwd`
adb root
adb shell mount -o rw,remount /
adb shell mkdir -p /system/priv-app/DeviceSettings_MagiskVersion
adb push ${PROJECTROOT}/tools/privapp-permissions-org.lineageos.settings.device.magisk.xml /etc/permissions/
adb push ${PROJECTROOT}/build/outputs/apk/release/DeviceSettings_Magisk.apk /system/priv-app/DeviceSettings_MagiskVersion/
adb reboot