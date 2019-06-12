package org.sensors2.osc.dispatch;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;

import org.sensors2.common.dispatch.Measurement;
import org.sensors2.common.dispatch.DataDispatcher;
import org.sensors2.osc.sensors.Parameters;

import java.util.ArrayList;
import java.util.List;


public class OscDispatcher implements DataDispatcher {
    private List<SensorConfiguration> sensorConfigurations = new ArrayList<SensorConfiguration>();
    private OscCommunication communication;
    private float[] rotationMatrix = new float[16];
    private float[] orientations = new float[3];
    private float[] projections = new float[3];
    private float meanAzimuth = 0;
    private float[] gravity;
    private float[] geomagnetic;
    private SensorManager sensorManager;
    private float[] curAccelerations = new float[3];
    private float[] filteredAccelerations = new float[3];


    public OscDispatcher() {
        communication = new OscCommunication("OSC dispatcher thread", Thread.MIN_PRIORITY);
        communication.start();
        for(int i=0;i<3;i++)
        {
            curAccelerations[i]=0;
            filteredAccelerations[i]=0;
        }
    }

    public void addSensorConfiguration(SensorConfiguration sensorConfiguration) {
        this.sensorConfigurations.add(sensorConfiguration);
    }

    public void set()
    {
        this.meanAzimuth = this.orientations[0];
    }

    @Override
    public void dispatch(Measurement sensorData) {
        for (SensorConfiguration sensorConfiguration : this.sensorConfigurations) {
            if (sensorConfiguration.getSensorType() == sensorData.getSensorType()) {
                if (sensorData.getValues() != null) {
                    if (sensorConfiguration.getSensorType()==Sensor.TYPE_ROTATION_VECTOR)
                    {
                        float x, y, z, length=20;
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorData.getValues());
                        SensorManager.getOrientation(rotationMatrix, orientations);
                        x = length * (float) Math.sin(orientations[0] - meanAzimuth);
                        //Since azimuth is angle from the plane perpendicular to ground and along North,
                        // azimuth - meanAzimuth gives angle moved from mean position along horizontal
                        // So x = const * sin of that angle
                        y = length * (float) Math.sin(orientations[1]);
                        //Since pitch is angle from plane parallel to ground,
                        // y = const * sin of that angle
                        y = y * (-1);
                        // To make y positive when phone is tilted upwards
                        //Units of x and y = unknown ( depend on units of variable "length")
                        z = length * (float) (Math.cos(orientations[0] - meanAzimuth) + Math.cos(orientations[1]));
                        projections[0] = x;
                        projections[1] = y;
                        projections[2] = z;
                        trySend(sensorConfiguration, projections);
                    }
                    else if(sensorConfiguration.getSensorType()==Sensor.TYPE_ACCELEROMETER)
                    {
                        curAccelerations = sensorData.getValues();
                        boolean first = true;
                        for(int i=0;i<3;i++)
                            if (filteredAccelerations[i]!=0)
                            {
                                first = false;
                                break;
                            }
                        if (first)
                            System.arraycopy(curAccelerations, 0, filteredAccelerations, 0, 3);
                        else
                            for(int i=0;i<3;i++)
                                filteredAccelerations[i] = (float)(0.9*(filteredAccelerations[i]) + 0.1*(curAccelerations[i]));
                        trySend(sensorConfiguration, filteredAccelerations);
                    }
                    else
                        trySend(sensorConfiguration, sensorData.getValues());
                }

                else
                    {
                    trySend(sensorConfiguration, sensorData.getStringValue());
                }
            }
            if (sensorConfiguration.getSensorType() == Parameters.FAKE_ORIENTATION || sensorConfiguration.getSensorType() == Parameters.INCLINATION) {
                // Fake orientation
                if (sensorData.getSensorType() != Sensor.TYPE_ACCELEROMETER && sensorData.getSensorType() != Sensor.TYPE_MAGNETIC_FIELD) {
                    continue;
                }
                if (sensorData.getSensorType() == Sensor.TYPE_ACCELEROMETER) {
                    this.gravity = sensorData.getValues();
                }

                if (sensorData.getSensorType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    this.geomagnetic = sensorData.getValues();
                }
                if (this.gravity != null && this.geomagnetic != null) {
                    float rotationMatrix[] = new float[9];
                    float inclinationMatrix[] = new float[9];

                    boolean success = this.sensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, this.gravity, this.geomagnetic);
                    if (success) {
                        if (sensorConfiguration.getSensorType() == Parameters.FAKE_ORIENTATION) {
                            float orientation[] = new float[3];
                            this.sensorManager.getOrientation(rotationMatrix, orientation);
                            this.trySend(sensorConfiguration, orientation);
                        }
                        if (sensorConfiguration.getSensorType() == Parameters.INCLINATION) {
                            float inclination[] = new float[1];
                            inclination[0] = this.sensorManager.getInclination(inclinationMatrix);
                            this.trySend(sensorConfiguration, inclination);
                        }
                    }
                }
            }
        }
    }

    private void trySend(SensorConfiguration sensorConfiguration, float[] values) {
        if (!sensorConfiguration.sendingNeeded(values)) {
            return;
        }
        Message message = new Message();
        Bundle data = new Bundle();
        data.putFloatArray(Bundling.VALUES, values);
        data.putString(Bundling.OSC_PARAMETER, sensorConfiguration.getOscParam());
        message.setData(data);
        OscHandler handler = communication.getOscHandler();
        handler.sendMessage(message);
    }

    private void trySend(SensorConfiguration sensorConfiguration, String value) {
        if (!sensorConfiguration.sendingNeeded(new float[0])) {
            return;
        }
        Message message = new Message();
        Bundle data = new Bundle();
        data.putString(Bundling.STRING_VALUE, value);
        data.putString(Bundling.OSC_PARAMETER, sensorConfiguration.getOscParam());
        message.setData(data);
        OscHandler handler = communication.getOscHandler();
        handler.sendMessage(message);
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
    }
}
