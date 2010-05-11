#!/bin/bash

#ant install
#cd tests && ant install
#cd ..
# script to run tests.

adb shell am instrument -w \
    org.nerdcircus.android.hiveminder.tests/android.test.InstrumentationTestRunner
