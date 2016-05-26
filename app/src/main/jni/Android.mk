LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := cameratracker
LOCAL_SRC_FILES := cameratracker.c
LOCAL_LDLIBS    := -llog -landroid

include $(BUILD_SHARED_LIBRARY)
