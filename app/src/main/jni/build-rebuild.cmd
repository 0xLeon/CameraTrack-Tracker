@echo off
pushd ..
%ANDROID_NDK_HOME%\ndk-build clean
%ANDROID_NDK_HOME%\ndk-build
popd
