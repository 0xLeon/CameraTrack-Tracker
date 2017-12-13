package com.leon.hfu.cameratrack.tracker;

import android.support.annotation.NonNull;
import android.util.Log;

import com.leon.hfu.cameratrack.CameraTrackerAdapter;
import com.leon.hfu.cameratrack.exception.CameraTrackException;

/**
 *
 */
public abstract class AbstractTracker extends Thread {
	public static final String TAG = "AbstractTracker";

	private CameraTrackerAdapter adapter = null;

	public enum CameraTrackerType {
		GAM_DEFAULT,
		GAM_NATIVE,
		RL_DEFAULT,
		GL_DEFAULT,
	}

	public AbstractTracker(@NonNull CameraTrackerAdapter adapter) {
		this.adapter = adapter;
	}

	public CameraTrackerAdapter getAdapter() {
		return this.adapter;
	}

	@Override
	public void run() {
		Log.v(TAG, "thread started");

		try {
			this.startTracking();
		}
		catch (CameraTrackException e) {
			this.adapter.handleError(e);
		}

		Log.v(TAG, "thread stopped");
	}

	public abstract void startTracking() throws CameraTrackException;

	public abstract void stopTracking() throws CameraTrackException;
}
