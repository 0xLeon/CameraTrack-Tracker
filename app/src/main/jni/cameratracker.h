#ifndef CAMERATRACK_CAMERATRACKER_H_
#define CAMERATRACK_CAMERATRACKER_H_

#include <stdio.h>
#include <pthread.h>
#include <android/looper.h>

#ifdef __cplusplus
extern "C" {
#endif

enum CameraTrackerType {
	GAM_DEFAULT,
	GAM_NATIVE,
	RL_DEFAULT
};

typedef struct sensor_server {
	int destroyRequested;

	ASensorManager* sensorManager;
	ASensorEventQueue* sensorEventQueue;
	const ASensor* sensors[3];
	int sensorsEnabledAll;

	const char* trackingFilePath;
	FILE* trackingFile;

	pthread_mutex_t* mutex;
	ALooper* looper;
} sensor_server_t;
typedef sensor_server_t* sensor_server_p;

#ifdef __cplusplus
}
#endif

#endif
