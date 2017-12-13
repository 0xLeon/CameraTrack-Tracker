package com.leon.hfu.cameratrack.tracker;

import android.hardware.Sensor;
import android.support.annotation.NonNull;

import com.leon.hfu.cameratrack.TrackerAdapter;

/**
 *
 */
public class GyroAccelMagnetTracker extends AbstractNonNativeTracker {
	public static final String TAG = "GyroAccelMagnetTracker";

	public GyroAccelMagnetTracker(@NonNull TrackerAdapter adapter) {
		super(adapter);
	}

	@Override
	public @NonNull int[] getUsedSensorTypes() {
		return new int[] {
			Sensor.TYPE_GYROSCOPE,
			Sensor.TYPE_ACCELEROMETER,
			Sensor.TYPE_MAGNETIC_FIELD,
		};
	}

	@Override
	public @NonNull
	TrackerAdapter.TrackerType getTrackerType() {
		return TrackerAdapter.TrackerType.GAM_DEFAULT;
	}

	@Override
	public @NonNull String getTag() {
		return TAG;
	}
}
