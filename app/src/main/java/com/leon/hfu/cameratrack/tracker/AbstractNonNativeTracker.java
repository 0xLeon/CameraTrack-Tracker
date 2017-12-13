package com.leon.hfu.cameratrack.tracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.leon.hfu.cameratrack.R;
import com.leon.hfu.cameratrack.TrackerAdapter;
import com.leon.hfu.cameratrack.exception.CameraTrackException;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class AbstractNonNativeTracker extends AbstractTracker implements SensorEventListener {
	public static final String TAG = "AbstractNonNativeTracker";

	private PrintStream trackFile = null;

	private SensorManager sensorManager = null;
	private List<Sensor> sensors = null;
	private volatile boolean sensorsEnabledAll = false;

	public AbstractNonNativeTracker(@NonNull TrackerAdapter adapter) {
		super(adapter);
	}

	@Override
	public void startTracking() throws CameraTrackException {
		this.sensorManager = (SensorManager) this.getAdapter().getContext().getSystemService(Context.SENSOR_SERVICE);

		this.findSensors();

		try {
			this.trackFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.getAdapter().getTrackingFile())));
			this.trackFile.printf("%d\n", this.getTrackerType().ordinal());
		}
		catch (FileNotFoundException e) {
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorGenericIO));
		}

		this.enableSensors();

		while (this.sensorsEnabledAll) {
			synchronized (this) {
				try {
					this.wait();
				}
				catch (InterruptedException e) { }
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		if (!this.sensorsEnabledAll) {
			return;
		}

//		if (sensorEvent.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
//			this.getAdapter().handleError(new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorSensorUnreliable)));
//
//			try {
//				this.stopTracking();
//			}
//			catch (CameraTrackException e) {
//				this.getAdapter().handleError(e);
//			}
//
//			return;
//		}

		synchronized (this.sensorManager) {
			// Timestamp / SensorType / dataX / dataY / dataZ
			this.trackFile.printf("%d\t%d\t%f\t%f\t%f\n", sensorEvent.timestamp, sensorEvent.sensor.getType(), sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}

	@Override
	public void stopTracking() throws CameraTrackException {
		Log.v(this.getTag(), "Stopping tracking");

		this.disableSensors();

		this.trackFile.close();
		this.trackFile = null;

		synchronized (this) {
			this.notify();
		}

		Log.v(this.getTag(), "Stopped tracking");
	}

	private void findSensors() throws CameraTrackException {
		Log.v(this.getTag(), "Finding sensors");

		int[] sensorTypes = this.getUsedSensorTypes();
		this.sensors = new ArrayList<>(sensorTypes.length);

		Sensor sensor;

		for (int sensorType: sensorTypes) {
			sensor = this.sensorManager.getDefaultSensor(sensorType);

			if (null == sensor) {
				throw new CameraTrackException(this.getAdapter().getContext().getString(CameraTrackException.getMessageIDBySensorType(sensorType)));
			}

			this.sensors.add(sensor);
		}

		Log.v(this.getTag(), "Found sensors");
	}

	private void enableSensors() throws CameraTrackException {
		Log.v(this.getTag(), "Enabling sensors");

		boolean didRegister = false;

		for (Sensor sensor: this.sensors) {
			if (!this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)) {
				if (didRegister) {
					this.sensorManager.unregisterListener(this);
				}

				throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorCouldntStartSensor));
			}

			didRegister = true;
		}

		this.sensorsEnabledAll = true;

		Log.v(this.getTag(), "Enabled sensors");
	}

	private void disableSensors() {
		Log.v(this.getTag(), "Disabling sensors");

		this.sensorsEnabledAll = false;

		this.sensorManager.unregisterListener(this);

		Log.v(this.getTag(), "Disabled sensors");
	}

	public abstract @NonNull int[] getUsedSensorTypes();

	public abstract @NonNull TrackerType getTrackerType();

	public abstract @NonNull String getTag();
}
