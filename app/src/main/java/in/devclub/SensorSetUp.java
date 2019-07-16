package in.devclub;

import android.hardware.Sensor;
import android.os.Bundle;

import in.devclub.osc.dispatch.SensorConfiguration;
import in.devclub.osc.sensors.Parameters;

public class SensorSetUp {
    private SensorConfiguration sensorConfiguration;

    public SensorSetUp(Parameters parameters){
        this.sensorConfiguration=new SensorConfiguration();
        this.sensorConfiguration.setSensorType(parameters.getSensorType());
        this.sensorConfiguration.setOscParam(parameters.getOscPrefix());
        if (parameters.getSensorType() == Sensor.TYPE_LINEAR_ACCELERATION ||
                parameters.getSensorType()== Parameters.FAKE_ORIENTATION)
            sensorConfiguration.setSend(true);
    }

    public SensorConfiguration getSensorConfiguration(){
        return this.sensorConfiguration;
    }
}
