package com.example.mc.assignment2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;

public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    int count = 0;
    List<AccelerometerData> accelerometerDataList = new ArrayList<>();

    //whenever activity is created it is called
    @Override
    public void onCreate(){
        //Sensors of the device are accessed through here
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, 1000000);
    }

    //data is inserseted from the accelerometer to the database from here
    private void insertDataToDB(List<AccelerometerData>list) {

        if (DBManager.commonInstance().isDatabaseAvialable()) {
            DBManager.commonInstance().saveAccelerometerList(list);
        }
        list.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            count++;
            AccelerometerData accelerometerData = new AccelerometerData();
            //acceleromter gives three values against x, y and z axis
            accelerometerData.setX(sensorEvent.values[0]);
            accelerometerData.setY(sensorEvent.values[1]);
            accelerometerData.setZ(sensorEvent.values[2]);
            accelerometerData.setTimestamp(System.currentTimeMillis());

            accelerometerDataList.add(accelerometerData);
            //adding 10 values to the database
            if(count >= 10){
                count = 0;
                //unregister the sensor event listner for the sensor
                sensorManager.unregisterListener(this);
                insertDataToDB(accelerometerDataList);
                //register the sensor event listner for the sensor at sampling frequency of 1Hz
                sensorManager.registerListener(this, accelerometerSensor, 1000000);
            }
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




}
