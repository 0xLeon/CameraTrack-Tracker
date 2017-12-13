package com.leon.hfu.cameratrack.tracker;

import android.support.annotation.NonNull;

import com.leon.hfu.cameratrack.TrackerAdapter;
import com.leon.hfu.cameratrack.R;
import com.leon.hfu.cameratrack.exception.CameraTrackException;

import java.io.IOException;

/**
 *
 */
public class GyroAccelMagnetNativeTracker extends AbstractTracker {
	public static final String TAG = "GyroAccelMagnetNativeTracker";

	static {
		System.loadLibrary("cameratracker");
	}

	public GyroAccelMagnetNativeTracker(@NonNull TrackerAdapter adapter) {
		super(adapter);
	}

	@Override
	public void startTracking() throws CameraTrackException {
		if (this.startNativeTracking() != 0) {
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorGenericTracking));
		}
	}

	@Override
	public void startRecording() throws CameraTrackException {
		try {
			this.startNativeRecording(this.getAdapter().getTrackingFile().getCanonicalPath());
		}
		catch (IOException e) {
			throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorGenericIO));
		}
	}

	@Override
	public void stopRecording() {
		this.stopNativeRecording();
	}

	@Override
	public void stopTracking() throws CameraTrackException {
		this.stopNativeTracking();
	}

	/**
	 * Starts the sensors.
	 * Main loop of this thread happens in native code.
	 *
	 * @return	Error code
	 */
	private native int startNativeTracking();

	/**
	 *
	 */
	private native void stopNativeTracking();

	/**
	 * Initias writing of sensor data to a local file.
	 *
	 * @return	Error code
	 */
	private native int startNativeRecording(String trackingFilePath);

	/**
	 *
	 */
	private native void stopNativeRecording();

	@Override
	public @NonNull
	TrackerAdapter.TrackerType getTrackerType() {
		return TrackerAdapter.TrackerType.GAM_NATIVE;
	}
}
