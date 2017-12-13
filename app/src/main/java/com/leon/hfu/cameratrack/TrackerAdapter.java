package com.leon.hfu.cameratrack;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.leon.hfu.cameratrack.tracker.AbstractTracker;
import com.leon.hfu.cameratrack.exception.CameraTrackException;
import com.leon.hfu.cameratrack.tracker.GyroAccelMagnetTracker;
import com.leon.hfu.cameratrack.tracker.GyroAccelMagnetNativeTracker;
import com.leon.hfu.cameratrack.tracker.GyroLinearTracker;
import com.leon.hfu.cameratrack.tracker.RotationLinearTracker;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 *
 */
public class TrackerAdapter {
	public static final String TAG = "TrackerAdapter";

	public enum TrackerType {
		RL_DEFAULT,
		GL_DEFAULT,
		GAM_DEFAULT,
		GAM_NATIVE,
	}

	private String shotName = "";
	private int shotNumber = 0;
	private File trackingDirectory = null;
	private File trackingFile = null;

	private Context context = null;
	private Handler messageHandler = null;
	private TrackerType trackerType;
	private AbstractTracker tracker = null;

	public TrackerAdapter(@NonNull Context context, @NonNull Handler messageHandler, TrackerType trackerType) {
		this.context = context;
		this.messageHandler = messageHandler;
		this.trackerType = trackerType;
	}

	public String getShotName() {
		return this.shotName;
	}

	public int getShotNumber() {
		return this.shotNumber;
	}

	public File getTrackingFile() {
		return this.trackingFile;
	}

	public Context getContext() {
		return this.context;
	}

	public AbstractTracker getTracker() {
		return this.tracker;
	}

	public TrackerType getTrackerType() {
		return this.trackerType;
	}

	public void setTrackingFileData(@NonNull String shotName, int shotNumber) throws CameraTrackException, IOException {
		this.shotName = shotName;
		this.shotNumber = shotNumber;

		if (this.shotName.length() < 1) {
			throw new CameraTrackException(this.context.getString(R.string.errorInvalidShotName));
		}

		if (this.shotNumber < 1) {
			throw new CameraTrackException(this.context.getString(R.string.errorInvalidShotNumber));
		}

		this.checkTrackingFile();
	}

	public synchronized void startTracking() throws IOException, CameraTrackException {
		this.checkDirectory();

		switch (this.trackerType) {
			case RL_DEFAULT:
				this.tracker = new RotationLinearTracker(this);
				Log.d(TAG, "RL Default");
				break;
			case GL_DEFAULT:
				this.tracker = new GyroLinearTracker(this);
				Log.d(TAG, "GL Default");
				break;
			case GAM_DEFAULT:
				this.tracker = new GyroAccelMagnetTracker(this);
				Log.d(TAG, "GAM Default");
				break;
			case GAM_NATIVE:
				this.tracker = new GyroAccelMagnetNativeTracker(this);
				Log.d(TAG, "GAM Native");
				break;
			default:
				throw new CameraTrackException(this.context.getString(R.string.errorUnsupportedTrackerType));
		}

		this.tracker.start();
	}

	public synchronized void startRecording() throws CameraTrackException {
		this.tracker.startRecording();
	}

	public synchronized void stopRecording() {
		this.tracker.stopRecording();
	}

	public synchronized void stopTracking() {
		try {
			this.tracker.stopRecording();
			this.tracker.stopTracking();
		}
		catch (CameraTrackException e) {
			this.handleError(e);
		}

		while (this.tracker.isAlive()) {
			try {
				this.tracker.join();
			}
			catch (InterruptedException e) { }
		}

		this.tracker = null;
	}

	public void handleError(Exception e) {
		this.messageHandler.obtainMessage(-1, e).sendToTarget();
	}

	private void checkDirectory() throws IOException {
		String state = Environment.getExternalStorageState();

		if (!Environment.MEDIA_MOUNTED.equals(state)) {
			throw new IOException(this.context.getString(R.string.externalStorageUnavailable));
		}

		this.trackingDirectory = new File(new File(Environment.getExternalStorageDirectory(), "./CameraTracking/").getCanonicalPath());

		if (this.trackingDirectory.exists() && !this.trackingDirectory.isDirectory()) {
			throw new IOException(this.context.getString(R.string.errorDirectoryNameAlreadyExists));
		}

		if (!this.trackingDirectory.exists() && !this.trackingDirectory.mkdirs()) {
			throw new IOException(this.context.getString(R.string.errorFolderNotCreatable));
		}
	}

	private void checkTrackingFile() throws IOException {
		this.trackingFile = new File(this.trackingDirectory.getCanonicalFile(), this.getTrackingFileFilename());

		if (this.trackingFile.exists() && this.trackingFile.isFile()) {
			throw new IOException(this.context.getString(R.string.errorFileAlreadyExists));
		}

		if (!this.trackingFile.createNewFile()) {
			throw new IOException(this.context.getString(R.string.errorFileNotCreateable));
		}
	}

	private String getTrackingFileFilename() {
		return "Tracking-" + this.shotName + "-" + String.format(Locale.ENGLISH, "%05d", this.shotNumber) + ".track";
	}
}
