package com.example.demo.bean;

public class SensorInstance extends Instance{
    private SensorType sensorType=new SensorType();

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }
}
