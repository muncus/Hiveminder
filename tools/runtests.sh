#!/bin/bash
# script to run tests.

ant install
cd tests && ant install
cd ..

adb shell am instrument -w \
    org.nerdcircus.android.hiveminder.tests/android.test.InstrumentationTestRunner
