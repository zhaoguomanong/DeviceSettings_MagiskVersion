#!/bin/bash

cd `dirname $0`
PROJECTROOT=`pwd`
moduleRoot=${PROJECTROOT}/magisk_module

apkFile=${PROJECTROOT}/build/outputs/apk/release/DeviceSettings_Magisk.apk
moduleApkPath=${moduleRoot}/system/priv-app/DeviceSettings_MagiskVersion/

if [[ ! -f "$apkFile" ]];then
    echo "$apkFile not exist, fatal error"
    exit 1
fi

if [[ ! -d "$moduleApkPath" ]];then
    mkdir -p "$moduleApkPath"
fi

cp "$apkFile" "$moduleApkPath"

source ${moduleRoot}/module.prop
zipFileName="magisk-module-DeviceSettings_MagiskVersion-${version}.zip"
outputPath="${PROJECTROOT}/build/outputs/magisk"
[[ -f "$zipFileName" ]] && rm -f "$zipFileName"
[[ ! -d "$outputPath" ]] && mkdir -p "$outputPath"

cd ${moduleRoot}
zip -r "$outputPath"/${zipFileName} *
cd ${PROJECTROOT}