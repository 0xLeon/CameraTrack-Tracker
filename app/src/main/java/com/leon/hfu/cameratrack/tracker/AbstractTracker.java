package com.leon.hfu.cameratrack.tracker;

import android.support.annotation.NonNull;
import android.util.Log;

import com.leon.hfu.cameratrack.TrackerAdapter;
import com.leon.hfu.cameratrack.exception.CameraTrackException;

/**
 *
 */
public abstract class AbstractTracker extends Thread {
	public static final String TAG = "AbstractTracker";

	private TrackerAdapter adapter = null;

	public AbstractTracker(@NonNull TrackerAdapter adapter) {
		this.adapter = adapter;
	}

	public TrackerAdapter getAdapter() {
		return this.adapter;
	}

	@Override
	public void run() {
		Log.v(TAG, "Starting thread");

		try {
			this.startTracking();
		}
		catch (CameraTrackException e) {
			this.adapter.handleError(e);
		}

		Log.v(TAG, "Stopped thread");
	}

	public abstract void startTracking() throws CameraTrackException;

	public abstract void startRecording() throws CameraTrackException;

	public abstract void stopRecording();

	public abstract void stopTracking() throws CameraTrackException;

	public abstract @NonNull TrackerAdapter.TrackerType getTrackerType();
}
