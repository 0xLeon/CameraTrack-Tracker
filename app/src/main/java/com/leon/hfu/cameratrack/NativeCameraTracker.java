package com.leon.hfu.cameratrack;

import android.support.annotation.NonNull;

import com.leon.hfu.cameratrack.exception.CameraTrackException;

import java.io.IOException;

/**
 *
 */
public class NativeCameraTracker extends CameraTracker {
	public static final String TAG = "NativeCameraTracker";

	static {
		System.loadLibrary("cameratracker");
	}

	public NativeCameraTracker(@NonNull CameraTrackerAdapter adapter) {
		super(adapter);
	}

	@Override
	public void startTracking() throws CameraTrackException {
		try {
			if (this.startNativeTracking(this.getAdapter().getTrackingFile().getCanonicalPath()) != 0) {
				throw new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorGenericTracking));
			}
		}
		catch (IOException e) {
			this.getAdapter().handleError(new CameraTrackException(this.getAdapter().getContext().getString(R.string.errorGenericIO)));
		}
	}

	@Override
	public void stopTracking() throws CameraTrackException {
		this.stopNativeTracking();
	}

	/**
	 * Starts the sensors and saves the tracking data into a file.
	 * Main loop of this thread happens in native code.
	 *
	 * @param trackingFilePath File to save the tracking data into
	 * @return Error code
	 */
	private native int startNativeTracking(String trackingFilePath);

	/**
	 *
	 */
	private native void stopNativeTracking();
}
