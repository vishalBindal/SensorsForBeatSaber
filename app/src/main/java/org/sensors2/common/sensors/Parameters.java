package org.sensors2.common.sensors;

import android.hardware.Sensor;

/**
 * Created by thomas on 05.11.14.
 */
public abstract class Parameters {
    private final int sensorType;
    private final int dimensions;
    private final String sensorName;
    private final float range;
    private final float resolution;

    public static final int MAX_DIMENSIONS = 16;
    public static final int FAKE_ORIENTATION = Integer.MAX_VALUE;
    public static final int INCLINATION = Integer.MAX_VALUE - 1;
    public static final String STRING_INCLINATION = "Inclination";

    public Parameters(int sensorType) {
        this.sensorType = sensorType;
        this.resolution = 0.01f;
        switch (sensorType) {
            case FAKE_ORIENTATION:
                this.dimensions = 3;
                this.sensorName = Sensor.STRING_TYPE_ORIENTATION;
                this.range = (float) Math.PI;
                break;
            case INCLINATION:
                this.dimensions = 1;
                this.sensorName = STRING_INCLINATION;
                this.range = (float) Math.PI / 2f;
                break;
            default:
                this.dimensions = MAX_DIMENSIONS;
                this.sensorName = "";
                this.range = -1f;
        }
    }


    public Parameters(Sensor sensor) {
        this.sensorType = sensor.getType();
        this.sensorName = sensor.getName();
        this.range = sensor.getMaximumRange();
        this.resolution = sensor.getResolution();
        switch (sensorType) {
            // 1 int TYPE_ACCELEROMETER A constant describing an accelerometer sensor type.
            case 1:
                this.dimensions = 3;
                break;
            // 4 int TYPE_GYROSCOPE A constant describing a gyroscope sensor type
            case 4:
                this.dimensions = 3;
                break;
            // 11 int TYPE_ROTATION_VECTOR A constant describing a rotation vector sensor type.
            case 11:
                this.dimensions = 3;
                break;
            default:
                this.dimensions = MAX_DIMENSIONS; // the maximum
                break;
            //throw new IllegalArgumentException();
        }
    }

    public int getSensorType() {
        return this.sensorType;
    }

    public int getDimensions() {
        return this.dimensions;
    }

}
