package com.leon.hfu.cameratrack.exception;

import android.hardware.Sensor;

import com.leon.hfu.cameratrack.R;

/**
 *
 */
public class CameraTrackException extends Exception {
	public static int getMessageIDBySensorType(int sensorType) throws CameraTrackException {
		switch (sensorType) {
			case Sensor.TYPE_GYROSCOPE:
				return R.string.errorGyroSensorNotFound;
			case Sensor.TYPE_ACCELEROMETER:
				return R.string.errorAccelSensorNotFound;
			case Sensor.TYPE_MAGNETIC_FIELD:
				return R.string.errorMagnetSensorNotFound;
			case Sensor.TYPE_ROTATION_VECTOR:
				return R.string.errorRotationSensorNotFound;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				return R.string.errorLinearAccelSensorNotFound;
			default:
				throw new CameraTrackException();
		}
	}

	public CameraTrackException(String message) {
		super(message);
	}

	public CameraTrackException() {
		super();
	}
}
