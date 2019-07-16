package in.devclub;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;

import in.devclub.osc.activities.SettingsActivity;
import in.devclub.osc.activities.StartUpActivity;
import in.devclub.osc.dispatch.OscConfiguration;
import in.devclub.osc.sensors.Settings;

public class HomeActivity extends AppCompatActivity {
    String hostString;
    String qrstring;
    EditText hostBox;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);

        hostBox = findViewById(R.id.editTextHost);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        hostString = preferences.getString("pref_comm_host", "");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            qrstring = extras.getString("QR_IP");
            if(qrstring != null && !qrstring.isEmpty())
                hostString=qrstring;
        }
        hostBox.setText(hostString);

        hostBox.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            hostString = hostBox.getText().toString();
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("pref_comm_host", hostString);
                            editor.apply();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    public void proceed(View view) {
        hostString = hostBox.getText().toString();
        if (hostString.equals(""))
            Toast.makeText(getApplicationContext(), "No IP entered", Toast.LENGTH_SHORT).show();
        else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("pref_comm_host", hostString);
            editor.apply();
            Intent intent = new Intent(getBaseContext(), StartUpActivity.class);
            startActivity(intent);
        }
    }

    public void open_settings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void scan_qr(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    42);
        } else {
            Intent intent = new Intent(this, QRCodeScanner.class);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == 42) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, QRCodeScanner.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Unable to open camera", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
