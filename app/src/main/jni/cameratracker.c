#include <malloc.h>
#include <stdio.h>
#include <pthread.h>
#include <inttypes.h>
#include <jni.h>
#include <android/log.h>
#include <android/looper.h>
#include <android/sensor.h>
#include "cameratracker.h"

#define TAG		"GAMNative-jni"

#define LOGI(...)	((void) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))
#define LOGE(...)	((void) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))

#ifndef NDEBUG
#define LOGV(...)	((void) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__))
#else
#define LOGV(...)	((void) 0U)
#endif

static sensor_server_p createSensorServer();
static void runSensorServer();
static void closeSensorServer();
static int enableSensors();

static int sensorEventQueueCallback(int fd, int events, void* data);

sensor_server_p sensorServer = NULL;

JNIEXPORT jint JNICALL Java_com_leon_hfu_cameratrack_tracker_GyroAccelMagnetNativeTracker_startNativeTracking(JNIEnv* env, jclass inClass) {
	LOGV("%s", "Starting tracker");

	if (NULL == createSensorServer()) {
		return 1U;
	}

	runSensorServer();

	closeSensorServer();
	
	return 0U;
}

JNIEXPORT jint JNICALL Java_com_leon_hfu_cameratrack_tracker_GyroAccelMagnetNativeTracker_startNativeRecording(JNIEnv* env, jclass inClass, jstring jTrackingFilePath) {
	LOGV("%s", "Starting recording");

	sensorServer->trackingFilePath = (*env)->GetStringUTFChars(env, jTrackingFilePath, 0U);
	sensorServer->trackingFile = fopen(sensorServer->trackingFilePath, "w+");

	if (NULL == sensorServer->trackingFile) {
		LOGE("Couldn't open tracking file for writing");
		return 1U;
	}

	fprintf(sensorServer->trackingFile, "%d\n", (int) GAM_NATIVE);

	pthread_mutex_lock(sensorServer->mutex);

	sensorServer->writeData = 1U;

	pthread_mutex_unlock(sensorServer->mutex);

	return 0U;
}

JNIEXPORT void JNICALL Java_com_leon_hfu_cameratrack_tracker_GyroAccelMagnetNativeTracker_stopNativeRecording(JNIEnv* env, jclass inClass) {
	LOGV("%s", "Stopping recording");
	
	pthread_mutex_lock(sensorServer->mutex);

	sensorServer->writeData = 0U;

	pthread_mutex_unlock(sensorServer->mutex);

	if (NULL != sensorServer->trackingFile) {
		fflush(sensorServer->trackingFile);
		fclose(sensorServer->trackingFile);
		sensorServer->trackingFile = NULL;
		sensorServer->trackingFilePath = NULL;
	}

	LOGV("%s", "Stopped native recording");
}

JNIEXPORT void JNICALL Java_com_leon_hfu_cameratrack_tracker_GyroAccelMagnetNativeTracker_stopNativeTracking(JNIEnv* env, jclass inClass) {
	LOGV("%s", "Stopping tracker");

	sensorServer->destroyRequested = 1U;
}

static sensor_server_p createSensorServer() {
	LOGV("%s", "Creating tracker");
	
	sensorServer = (sensor_server_p) malloc(sizeof(sensor_server_t));

	if (NULL == sensorServer) {
		LOGE("Couldn't create sensor server instance");
		return NULL;
	}

	memset(sensorServer, 0U, sizeof(sensor_server_t));

	sensorServer->destroyRequested = 0U;
	sensorServer->writeData = 0U;
	sensorServer->trackingFile = NULL;
	sensorServer->trackingFilePath = NULL;

	sensorServer->looper = ALooper_prepare(0U);

	if (NULL == sensorServer->looper) {
		LOGE("Couldn't create looper");
		free(sensorServer);
		return NULL;
	}

	sensorServer->sensorManager = ASensorManager_getInstance();

	sensorServer->sensors[0U] = ASensorManager_getDefaultSensor(sensorServer->sensorManager, ASENSOR_TYPE_GYROSCOPE);
	sensorServer->sensors[1U] = ASensorManager_getDefaultSensor(sensorServer->sensorManager, ASENSOR_TYPE_ACCELEROMETER);
	sensorServer->sensors[2U] = ASensorManager_getDefaultSensor(sensorServer->sensorManager, ASENSOR_TYPE_MAGNETIC_FIELD);

	if ((NULL == sensorServer->sensors[0U]) || (NULL == sensorServer->sensors[1U]) || (NULL == sensorServer->sensors[2U])) {
		LOGE("Couldn't find needed sensors");
		free(sensorServer);
		return NULL;
	}

	sensorServer->sensorEventQueue = ASensorManager_createEventQueue(sensorServer->sensorManager, sensorServer->looper, ALOOPER_POLL_CALLBACK, sensorEventQueueCallback, (void*) sensorServer);

	if (NULL == sensorServer->sensorEventQueue) {
		LOGE("Couldn't create sensor event queue");
		free(sensorServer);
		return NULL;
	}

	sensorServer->mutex = (pthread_mutex_t*) malloc(sizeof(pthread_mutex_t));

	if (NULL == sensorServer->mutex) {
		LOGE("Couldn't create sensor server mutex");
		ASensorManager_destroyEventQueue(sensorServer->sensorManager, sensorServer->sensorEventQueue);
		free(sensorServer);
		return NULL;
	}

	if (0U != pthread_mutex_init(sensorServer->mutex, NULL)) {
		LOGE("Couldn't initialize sensor server mutex");
		ASensorManager_destroyEventQueue(sensorServer->sensorManager, sensorServer->sensorEventQueue);
		free(sensorServer->mutex);
		free(sensorServer);
		return NULL;
	}

	LOGV("%s", "Created tracker");

	return sensorServer;
}

static void runSensorServer() {
	enableSensors();

	LOGV("%s", "Started tracker");

	while (!sensorServer->destroyRequested) {
		ALooper_pollAll(0U, NULL, NULL, NULL);
	}
}

static void closeSensorServer() {
	sensorServer->sensorsEnabledAll = 0U;

	pthread_mutex_lock(sensorServer->mutex);

	ASensorEventQueue_disableSensor(sensorServer->sensorEventQueue, sensorServer->sensors[0U]);
	ASensorEventQueue_disableSensor(sensorServer->sensorEventQueue, sensorServer->sensors[1U]);
	ASensorEventQueue_disableSensor(sensorServer->sensorEventQueue, sensorServer->sensors[2U]);

	ASensorManager_destroyEventQueue(sensorServer->sensorManager, sensorServer->sensorEventQueue);

	if (NULL != sensorServer->trackingFile) {
		fflush(sensorServer->trackingFile);
		fclose(sensorServer->trackingFile);
		sensorServer->trackingFile = NULL;
		sensorServer->trackingFilePath = NULL;
	}

	pthread_mutex_unlock(sensorServer->mutex);

	free(sensorServer);
	sensorServer = NULL;

	LOGV("%s", "Stopped tracker");
}

static int sensorEventQueueCallback(int fd, int events, void* data) {
	if (sensorServer->destroyRequested) {
		return 0U;
	}

	ASensorEvent sensorEvent;
	
	while (ASensorEventQueue_getEvents(sensorServer->sensorEventQueue, &sensorEvent, 1U) > 0U) {
		if (sensorServer->destroyRequested) {
			return 0U;
		}

		if (!sensorServer->sensorsEnabledAll || !sensorServer->writeData) {
			continue;
		}

		// pthread_mutex_lock(sensorServer->mutex);

		// Timestamp / SensorType / dataX / dataY / dataZ
		fprintf(sensorServer->trackingFile, "%" PRId64 "\t%" PRId32 "\t%f\t%f\t%f\n", sensorEvent.timestamp, sensorEvent.type, (double) sensorEvent.data[0U], (double) sensorEvent.data[1U], (double) sensorEvent.data[2U]);

		memset(&sensorEvent, 0U, sizeof(ASensorEvent));

		// pthread_mutex_unlock(sensorServer->mutex);
	}


	return 1U;
}

static int enableSensors() {
	LOGV("%s", "Enabling sensors");
	
	pthread_mutex_lock(sensorServer->mutex);

	int errorCode = 0U;

	errorCode = ASensorEventQueue_enableSensor(sensorServer->sensorEventQueue, sensorServer->sensors[0U]);
	if (0U != errorCode) {
		LOGE("Error trying to enable gyro sensor: %d", errorCode);
		return errorCode;
	}

	errorCode = ASensorEventQueue_setEventRate(sensorServer->sensorEventQueue, sensorServer->sensors[0U], ASensor_getMinDelay(sensorServer->sensors[0U]));
	if (0U != errorCode) {
		LOGE("Error trying to set gyro sensor event rate: %d", errorCode);
		return errorCode;
	}


	errorCode = ASensorEventQueue_enableSensor(sensorServer->sensorEventQueue, sensorServer->sensors[1U]);
	if (0U != errorCode) {
		LOGE("Error trying to enable accelerometer sensor: %d", errorCode);
		return errorCode;
	}

	errorCode = ASensorEventQueue_setEventRate(sensorServer->sensorEventQueue, sensorServer->sensors[1U], ASensor_getMinDelay(sensorServer->sensors[1U]));
	if (0U != errorCode) {
		LOGE("Error trying to set accelerometer sensor event rate: %d", errorCode);
		return errorCode;
	}


	errorCode = ASensorEventQueue_enableSensor(sensorServer->sensorEventQueue, sensorServer->sensors[2U]);
	if (0U != errorCode) {
		LOGE("Error trying to enable magnetometer sensor: %d", errorCode);
		return errorCode;
	}

	errorCode = ASensorEventQueue_setEventRate(sensorServer->sensorEventQueue, sensorServer->sensors[2U], ASensor_getMinDelay(sensorServer->sensors[2U]));
	if (0U != errorCode) {
		LOGE("Error trying to set magnetometer sensor event rate: %d", errorCode);
		return errorCode;
	}

	sensorServer->sensorsEnabledAll = 1U;

	pthread_mutex_unlock(sensorServer->mutex);

	LOGV("%s", "Enabled sensors");
}
