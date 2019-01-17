#!/bin/sh

testApkPath=`echo $PWD/app/build/outputs/apk/androidTest/debug/*.apk`
apkPath=`echo $PWD/app/build/outputs/apk/debug/*.apk`

# Run tests on test lab
gcloud firebase test android run \
  --app ${apkPath} \
  --test ${testApkPath} \
  --timeout 90s