package com.leon.hfu.cameratrack.tracker;

import android.hardware.Sensor;
import android.support.annotation.NonNull;

import com.leon.hfu.cameratrack.TrackerAdapter;

/**
 *
 */
public class GyroLinearTracker extends AbstractNonNativeTracker {
	public static final String TAG = "GLTracker";

	public GyroLinearTracker(@NonNull TrackerAdapter adapter) {
		super(adapter);
	}

	@Override
	public @NonNull int[] getUsedSensorTypes() {
		return new int[] {
			Sensor.TYPE_GYROSCOPE,
			Sensor.TYPE_LINEAR_ACCELERATION,
		};
	}

	@Override
	public @NonNull
	TrackerAdapter.TrackerType getTrackerType() {
		return TrackerAdapter.TrackerType.GL_DEFAULT;
	}

	@Override
	public @NonNull String getTag() {
		return TAG;
	}
}
