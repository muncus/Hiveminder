#!/bin/bash
# sign a klaxon release.

KEYSTORE="/home/muncus/release-keys.keystore"
KEY_ALIAS="hiveminder"

APK_FILE="bin/Braindump-unsigned.apk"

# a nice suffix for a release version.
VERSION_NAME=`grep versionName AndroidManifest.xml | awk -F\" '{print $2}'`

SIGNED_APK_FILENAME="Braindump-${VERSION_NAME}.apk"

jarsigner -verbose -keystore $KEYSTORE $APK_FILE $KEY_ALIAS

jarsigner -verify $APK_FILE | grep -i -q "jar verified"
ok=$?

if [[ $ok == 0 ]]; then
    echo "copying ${APK_FILE} to ${SIGNED_APK_FILENAME}"
    cp ${APK_FILE} ${SIGNED_APK_FILENAME}
fi
