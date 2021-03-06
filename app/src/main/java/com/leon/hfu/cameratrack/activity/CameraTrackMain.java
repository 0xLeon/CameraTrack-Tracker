package com.leon.hfu.cameratrack.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leon.hfu.cameratrack.TrackerAdapter;
import com.leon.hfu.cameratrack.R;
import com.leon.hfu.cameratrack.exception.CameraTrackException;

import java.io.IOException;

/**
 *
 */
public class CameraTrackMain extends AppCompatActivity {
	public static final String TAG = "CameraTrackMain";

	private static final int CAMERATRACK_PERMISSIONS_REQUEST_STORAGE = 1;

	private SharedPreferences prefs = null;

	private Toolbar toolbar = null;
	private EditText etShotName = null;
	private String sShotName = "";
	private EditText etShotNumber = null;
	private int iShotNumber = 0;
	private Button btStartStop = null;

	private boolean trackingRunning = false;
	private TrackerAdapter trackerAdapter = null;

	private Handler messageHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "onCreate");

		this.setContentView(R.layout.activity_camera_track_main);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);

		this.toolbar = (Toolbar) this.findViewById(R.id.toolbar);
		this.etShotName = (EditText) this.findViewById(R.id.etShotName);
		this.etShotNumber = (EditText) this.findViewById(R.id.etShotNumber);
		this.btStartStop = (Button) this.findViewById(R.id.btStartStop);

		if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			String[] permissions;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				permissions = new String[] {
					Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.WRITE_EXTERNAL_STORAGE
				};
			}
			else {
				permissions = new String[]{
					Manifest.permission.WRITE_EXTERNAL_STORAGE
				};
			}

			ActivityCompat.requestPermissions(this, permissions, CAMERATRACK_PERMISSIONS_REQUEST_STORAGE);
		}

		this.setSupportActionBar(this.toolbar);

		this.messageHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message message) {
				CameraTrackMain $this = CameraTrackMain.this;

				switch (message.what) {
					case -1:
						Toast.makeText($this.getApplicationContext(), ((Exception) message.obj).getMessage(), Toast.LENGTH_SHORT).show();

						$this.activateUIElements();
						$this.trackerAdapter.stopRecording();
						$this.trackingRunning = false;

						break;
				}
			}
		};

		this.btStartStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CameraTrackMain $this = CameraTrackMain.this;

				if (CameraTrackMain.this.trackingRunning) {
					$this.activateUIElements();
					$this.trackerAdapter.stopRecording();
					$this.trackingRunning = false;
				}
				else {
					try {
						$this.sShotName = $this.etShotName.getText().toString();
						$this.iShotNumber = Integer.parseInt($this.etShotNumber.getText().toString());

						if ($this.sShotName.length() < 1) {
							throw new CameraTrackException($this.getApplicationContext().getString(R.string.errorInvalidShotName));
						}

						if ($this.iShotNumber < 1) {
							throw new NumberFormatException();
						}

						$this.trackerAdapter.setTrackingFileData($this.sShotName, $this.iShotNumber);
						$this.trackerAdapter.startRecording();

						$this.trackingRunning = true;

						$this.deactivateUIElements();
					}
					catch (NumberFormatException e) {
						Toast.makeText($this, R.string.errorInvalidShotNumber, Toast.LENGTH_SHORT).show();
					}
					catch (CameraTrackException | IOException e) {
						$this.trackerAdapter.stopRecording();
						Toast.makeText($this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.v(TAG, "onStart");

		if (null != this.trackerAdapter) {
			this.trackerAdapter.stopTracking();
		}

		try {
			this.trackerAdapter = new TrackerAdapter(this.getApplicationContext(), this.messageHandler, TrackerAdapter.TrackerType.values()[Integer.parseInt(CameraTrackMain.this.prefs.getString("pref_trackerType", "-1"))]);
			this.trackerAdapter.startTracking();
		}
		catch (CameraTrackException | IOException e) {
			this.trackerAdapter = null;
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		Log.v(TAG, "onStop");

		if (null != this.trackerAdapter) {
			this.trackerAdapter.stopTracking();
		}

		this.trackerAdapter = null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy");
	}

	private void activateUIElements() {
		this.enableUIElements();
		this.etShotNumber.setText(Integer.toString(this.iShotNumber + 1));
		this.btStartStop.setText(R.string.btStartStop_Start);
	}

	private void enableUIElements() {
		this.etShotName.setInputType(InputType.TYPE_CLASS_TEXT);
		this.etShotName.setAlpha(1.f);
		this.etShotNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
		this.etShotNumber.setAlpha(1.f);
	}

	private void deactivateUIElements() {
		this.disableUIElement();
		this.btStartStop.setText(R.string.btStartStop_Stop);
	}

	private void disableUIElement() {
		this.etShotName.setInputType(InputType.TYPE_NULL);
		this.etShotName.setAlpha(.5f);
		this.etShotNumber.setInputType(InputType.TYPE_NULL);
		this.etShotNumber.setAlpha(.5f);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			View view = this.getCurrentFocus();

			if (view instanceof EditText) {
				Rect outRect = new Rect();
				view.getGlobalVisibleRect(outRect);

				if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
					view.clearFocus();

					InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
				}
			}
		}

		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.menu_camera_track_main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			this.startActivity(new Intent(this, SettingsActivity.class));

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
