package in.devclub.common.dispatch;

/**
 * Created by thomas on 03.11.14.
 */
public class Measurement {
    private final int sensorType;
    private final float[] values;
    private final String name;
    private final MeasurementType type;
    private final String stringValue;

    public Measurement(int sensorType, float[] values, String name, MeasurementType type, String stringValue) {
        this.sensorType = sensorType;
        this.values = values;
        this.name = name;
        this.type = type;
        this.stringValue = stringValue;
    }

    public Measurement(int sensorType, float[] values) {
        this(sensorType, values, "", MeasurementType.Sensor, null);
    }


    public float[] getValues() {
        return values;
    }

    public int getSensorType() {
        return sensorType;
    }

    public MeasurementType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getStringValue() {
        return stringValue;
    }
}
