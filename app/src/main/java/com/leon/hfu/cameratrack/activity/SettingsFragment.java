package com.leon.hfu.cameratrack.activity;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.leon.hfu.cameratrack.R;

/**
 *
 */
public class SettingsFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.addPreferencesFromResource(R.xml.preferences);
	}
}
