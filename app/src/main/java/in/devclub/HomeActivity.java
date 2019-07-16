package in.devclub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import in.devclub.osc.activities.StartUpActivity;

public class HomeActivity extends AppCompatActivity {
    String hostString;
    EditText hostBox;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);

        hostBox=findViewById(R.id.editTextHost);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        hostString=preferences.getString("pref_comm_host", "");
        hostBox.setText(hostString);
    }

    public void proceed(View view){
        hostString=hostBox.getText().toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("pref_comm_host", hostString);
        editor.apply();
        Intent intent=new Intent(getBaseContext(), StartUpActivity.class);
        startActivity(intent);
    }
}
