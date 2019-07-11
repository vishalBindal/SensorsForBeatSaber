package in.devclub.osc.sensors;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import in.devclub.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomas on 05.11.14.
 */
public class Parameters extends in.devclub.common.sensors.Parameters {
    private final String oscPrefix;
    private final String name;

    public static List<Parameters> GetSensors(SensorManager sensorManager, Context applicationContext) {
        List<Parameters> parameters = new ArrayList<>();
        List<Sensor> required_sensors = new ArrayList<>();
        parameters.add(createFakeOrientationSensor(applicationContext));
        required_sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        required_sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        required_sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR));
        required_sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        required_sensors.add(sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        for (Sensor sensor : required_sensors) {
            parameters.add(new Parameters(sensor, applicationContext));
        }
        parameters.add(createInclinationSensor(applicationContext));
        return parameters;
    }

    private static Parameters createFakeOrientationSensor(Context applicationContext) {
        return new Parameters("orientation", getString(R.string.sensor_orientation, applicationContext), FAKE_ORIENTATION);
    }

    private static Parameters createInclinationSensor(Context applicationContext) {
        return new Parameters("inclination", getString(R.string.sensor_inclination, applicationContext), INCLINATION);
    }

    private Parameters(String oscPrefix, String name, int sensorType) {
        super(sensorType);
        this.name = name;
        this.oscPrefix = oscPrefix;
    }


    public Parameters(Sensor sensor, Context applicationContext) {
        super(sensor);
        switch (sensor.getType()) {
            // 1 int TYPE_ACCELEROMETER A constant describing an accelerometer sensor type.
            case 1:
                this.name = getString(R.string.sensor_accelerometer, applicationContext);
                this.oscPrefix = "acceleration";
                break;
            // 4 int TYPE_GYROSCOPE A constant describing a gyroscope sensor type
            case 4:
                this.name = getString(R.string.sensor_gyroscope, applicationContext);
                this.oscPrefix = "gyroscope";
                break;

            // 11 int TYPE_ROTATION_VECTOR A constant describing a rotation vector sensor type.
            case 11:
                this.name = getString(R.string.sensor_rotation_vector, applicationContext);
                this.oscPrefix = "components";
                break;
            case 10:
                this.name = "Linear Acceleration";
                this.oscPrefix = "filteredLinearAcc";
                break;
            case 2:
                this.name = "Magnetic field";
                this.oscPrefix = "magfield";
                break;
            default:
                this.name = sensor.getName();
                this.oscPrefix = Integer.toString(sensor.getType());
                break;
            //throw new IllegalArgumentException();
        }
    }


    private static String getString(int stringId, Context context) {
        Resources res = context.getResources();
        return res.getString(stringId);
    }

    public String getOscPrefix() {
        return oscPrefix;
    }

    public String getName() {
        return name;
    }
}
