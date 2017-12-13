package com.leon.hfu.cameratrack.tracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.leon.hfu.cameratrack.CameraTracker;
import com.leon.hfu.cameratrack.CameraTrackerAdapter;
import com.leon.hfu.cameratrack.R;
import com.leon.hfu.cameratrack.exception.CameraTrackException;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GyroLinearTracker extends CameraTracker implements SensorEventListener {
	public static final String TAG = "GLTracker";

	private PrintStream trackingFile = null;

	private SensorManager sensorManager = null;
	private List<Sensor> sensors = new ArrayList<>(2);
	private volatile boolean sensorsEnabledAll = false;

	public GyroLinearTracker(@NonNull CameraTrackerAdapter adapter) {
		super(adapter);
	}

	@Override
	public void startTracking() throws CameraTrackException {
		this.sensorManager = (SensorManager) this.getAdapter().getContext().getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void stopTracking() throws CameraTrackException {
		Log.v(TAG, "Stopping tracking");

		this.disableSensors();

		this.trackingFile.close();
		this.trackingFile = null;

		synchronized (this) {
			this.notify();
		}

		Log.v(TAG, "Stopped tracking");
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (!this.sensorsEnabledAll) {
			return;
		}

		synchronized (this.sensorManager) {
			this.trackingFile.printf("%d\t%d\t%f\t%f\t%f\n", sensorEvent.timestamp, sensorEvent.sensor.getType(), sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}

	private void findSensors() throws CameraTrackException {
		Log.v(TAG, "Finding sensors");

		Sensor sensor;

		sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (null == sensor) {
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorGyroSensorNotFound));
		}
		this.sensors.add(sensor);

		sensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		if (null == sensor) {
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorLinearAccelSensorNotFound));
		}
		this.sensors.add(sensor);

		Log.v(TAG, "Found sensors");
	}

	private void enableSensors() throws CameraTrackException {
		Log.v(TAG, "Enabling sensors");

		if (!this.sensorManager.registerListener(this, this.sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST)) {
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorCouldntStartSensor));
		}

		if (!this.sensorManager.registerListener(this, this.sensors.get(1), SensorManager.SENSOR_DELAY_FASTEST)) {
			this.sensorManager.unregisterListener(this);
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorCouldntStartSensor));
		}

		this.sensorsEnabledAll = true;

		Log.v(TAG, "Enabled sensors");
	}

	private void disableSensors() {
		Log.v(TAG, "Disabling sensors");

		this.sensorsEnabledAll = false;

		this.sensorManager.unregisterListener(this);

		Log.v(TAG, "Disabled sensors");
	}
}
