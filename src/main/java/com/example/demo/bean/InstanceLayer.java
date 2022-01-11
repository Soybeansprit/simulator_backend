package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 实例层
 * */
public class InstanceLayer {
    private HumanInstance humanInstance=new HumanInstance();
    private AttributeEntityInstance attributeEntityInstance=new AttributeEntityInstance();
    private List<UncertainEntityInstance> uncertainEntityInstances=new ArrayList<>();
    private List<CyberServiceInstance> cyberServiceInstances=new ArrayList<>();
    private List<DeviceInstance> deviceInstances=new ArrayList<>();
    private List<SensorInstance> sensorInstances=new ArrayList<>();

    public HumanInstance getHumanInstance() {
        return humanInstance;
    }

    public void setHumanInstance(HumanInstance humanInstance) {
        this.humanInstance = humanInstance;
    }

    public AttributeEntityInstance getAttributeEntityInstance() {
        return attributeEntityInstance;
    }

    public void setAttributeEntityInstance(AttributeEntityInstance attributeEntityInstance) {
        this.attributeEntityInstance = attributeEntityInstance;
    }

    public List<UncertainEntityInstance> getUncertainEntityInstances() {
        return uncertainEntityInstances;
    }

    public void setUncertainEntityInstances(List<UncertainEntityInstance> uncertainEntityInstances) {
        this.uncertainEntityInstances = uncertainEntityInstances;
    }

    public List<CyberServiceInstance> getCyberServiceInstances() {
        return cyberServiceInstances;
    }

    public void setCyberServiceInstances(List<CyberServiceInstance> cyberServiceInstances) {
        this.cyberServiceInstances = cyberServiceInstances;
    }

    public List<DeviceInstance> getDeviceInstances() {
        return deviceInstances;
    }

    public void setDeviceInstances(List<DeviceInstance> deviceInstances) {
        this.deviceInstances = deviceInstances;
    }

    public List<SensorInstance> getSensorInstances() {
        return sensorInstances;
    }

    public void setSensorInstances(List<SensorInstance> sensorInstances) {
        this.sensorInstances = sensorInstances;
    }
}
