#ifndef CAMERATRACK_CAMERATRACKER_H_
#define CAMERATRACK_CAMERATRACKER_H_

#include <stdio.h>
#include <pthread.h>
#include <android/looper.h>

#ifdef __cplusplus
extern "C" {
#endif

enum CameraTrackerType {
	RL_DEFAULT,
	GL_DEFAULT,
	GAM_DEFAULT,
	GAM_NATIVE,
};

typedef struct sensor_server {
	int destroyRequested;
	int writeData;

	ASensorManager* sensorManager;
	ASensorEventQueue* sensorEventQueue;
	const ASensor* sensors[3U];
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
