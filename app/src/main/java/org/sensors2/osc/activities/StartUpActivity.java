package org.sensors2.osc.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.SupportActivity;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
//https://github.com/SensorApps/Common/tree/master/src/main/java/org/sensors2/common
// The "common" package
import org.sensors2.common.dispatch.DataDispatcher;
import org.sensors2.common.dispatch.Measurement;
import org.sensors2.common.nfc.NfcActivity;
import org.sensors2.common.sensors.Parameters;
import org.sensors2.common.sensors.SensorActivity;
import org.sensors2.common.sensors.SensorCommunication;
// The user defined package in directory org.sensors2.osc
import org.sensors2.R;
import org.sensors2.osc.dispatch.OscConfiguration;
import org.sensors2.osc.dispatch.OscDispatcher;
import org.sensors2.osc.dispatch.SensorConfiguration;
import org.sensors2.osc.fragments.MultiTouchFragment;
import org.sensors2.osc.fragments.SensorFragment;
import org.sensors2.osc.fragments.StartupFragment;
import org.sensors2.osc.sensors.Settings;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StartUpActivity extends FragmentActivity implements SensorActivity, NfcActivity,
        CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    private Settings settings;
    private SensorCommunication sensorCommunication;
    private OscDispatcher dispatcher;
    private SensorManager sensorManager;
    private PowerManager.WakeLock wakeLock;
    private boolean active;
    private StartupFragment startupFragment;

    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private NdefMessage mNdefPushMessage;

    public Settings getSettings() {
        return this.settings;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.settings = this.loadSettings();
        this.dispatcher = new OscDispatcher();
        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.dispatcher.setSensorManager(this.sensorManager);
        this.sensorCommunication = new SensorCommunication(this);
        this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, this.getLocalClassName());
        // Check for alternatives for this

        resolveIntent(getIntent());
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(
                        Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        mNdefPushMessage = new NdefMessage(new NdefRecord[]{newTextRecord(
                "Message from NFC Reader :-)", Locale.ENGLISH, true)});


        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        startupFragment = (StartupFragment) fm.findFragmentByTag("sensorlist");
        if (startupFragment == null) {
            startupFragment = new StartupFragment();
            transaction.add(R.id.container, startupFragment, "sensorlist");
            transaction.commit();
        }
    }

    public List<Parameters> GetSensors(SensorManager sensorManager) {
        List<Parameters> parameters = new ArrayList<>();

        // add Nfc sensor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                parameters.add(new org.sensors2.osc.sensors.Parameters(nfcAdapter,
                        this.getApplicationContext()));
            }
        }
        // add device sensors
        parameters.addAll(org.sensors2.osc.sensors.Parameters.GetSensors(sensorManager,
                this.getApplicationContext()));
        return parameters;
    }

    public NfcAdapter getNfcAdapter() {
        return this.nfcAdapter;
    }

    @TargetApi(10)
    private NdefRecord newTextRecord(String text, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));

        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = text.getBytes(utfEncoding);

        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);

        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], data);
    }

    @TargetApi(10)
    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                byte[] payload = new byte[0];
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
//                msgs = new NdefMessage[rawMsgs.length];
//                for (int i = 1; i <= rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i-1];
//                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Parcelable tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }
            // Setup the views
            for (NdefMessage msg : msgs) {
                if (active) {
                    this.sensorCommunication.dispatch(msg);
                }
            }
        }
    }

    @TargetApi(10)
    private String dumpTagData(Parcelable p) {
        StringBuilder sb = new StringBuilder();
        Tag tag = (Tag) p;
        byte[] id = tag.getId();
        sb.append("Tag ID (hex): ").append(getHex(id)).append("\n");
        sb.append("Tag ID (dec): ").append(getDec(id)).append("\n");
        sb.append("ID (reversed): ").append(getReversed(id)).append("\n");

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                MifareClassic mifareTag = MifareClassic.get(tag);
                String type = "Unknown";
                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize());
                sb.append(" bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append("-");
            }
        }
        return sb.toString();
    }

    private long getDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long getReversed(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    public DataDispatcher getDispatcher() {
        return this.dispatcher;
    }

    @Override
    public SensorManager getSensorManager() {
        return this.sensorManager;
    }

    private Settings loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Settings settings = new Settings(preferences);
        OscConfiguration oscConfiguration = OscConfiguration.getInstance();
        oscConfiguration.setHost(settings.getHost());
        oscConfiguration.setPort(settings.getPort());
        return settings;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_settings) {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    @SuppressLint("NewApi")
    protected void onResume() {
        super.onResume();
        this.loadSettings();
        this.sensorCommunication.onResume();
        if (active && !this.wakeLock.isHeld()) {
            this.wakeLock.acquire();
        }

        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                nfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                        null);
                nfcAdapter.enableForegroundNdefPush(this, mNdefPushMessage);
            }

        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onPause() {
        super.onPause();
        this.sensorCommunication.onPause();
        if (this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }

        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            nfcAdapter.disableForegroundNdefPush(this);
        }
    }

    public void addSensorFragment(SensorFragment sensorFragment) {
        this.dispatcher.addSensorConfiguration(sensorFragment.getSensorConfiguration());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (active) {
            this.sensorCommunication.dispatch(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // We do not care about that
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (isChecked) {
            if (!this.wakeLock.isHeld()) {
                this.wakeLock.acquire();
            }
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            this.wakeLock.release();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        active = isChecked;
    }

    public List<Parameters> getSensors() {
        return sensorCommunication.getSensors();
    }

    public void onStartMultiTouch(View view) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.container, new MultiTouchFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (active) {
            int width = v.getWidth();
            int height = v.getHeight();
            for (Measurement measurement : Measurement.measurements(event, width, height)) {
                dispatcher.dispatch(measurement);
            }
        }

        return false;
    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            // setting mean value of Azimuth when either of volume keys are pressed
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    this.dispatcher.set();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    this.dispatcher.set();
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
}
