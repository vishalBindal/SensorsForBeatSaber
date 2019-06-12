package org.sensors2.osc.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(org.sensors2.R.xml.preferences);
		addPreferencesFromResource(org.sensors2.R.xml.common_preferences);

	}

}
