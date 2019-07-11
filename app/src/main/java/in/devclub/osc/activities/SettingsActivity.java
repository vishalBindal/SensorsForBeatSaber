package in.devclub.osc.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(in.devclub.R.xml.preferences);
		addPreferencesFromResource(in.devclub.R.xml.common_preferences);

	}

}
