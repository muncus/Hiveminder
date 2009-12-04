#!/bin/bash

# script to run tests.

adb shell am instrument -w \
    org.nerdcircus.android.hiveminder.tests/android.test.InstrumentationTestRunner
