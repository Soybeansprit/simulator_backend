package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型层
 * 包括 人、不确定实体、包含各环境属性的实体（air）、设备、传感器
 * */
public class ModelLayer {
    private Human human=new Human();  ///一个人类型模型
    private AttributeEntityType attributeEntity=new AttributeEntityType();  ///一个具有各环境属性的实体类型模型
    private List<UncertainEntityType> uncertainEntityTypes=new ArrayList<>();  ///不确定实体类型
    private List<DeviceType> deviceTypes=new ArrayList<>();   ///设备类型
    private List<SensorType> sensorTypes=new ArrayList<>();   ///传感器类型
    private List<CyberServiceType> cyberServiceTypes=new ArrayList<>();  ///cyber service类型

    public Human getHuman() {
        return human;
    }

    public void setHuman(Human human) {
        this.human = human;
    }

    public AttributeEntityType getAttributeEntity() {
        return attributeEntity;
    }

    public void setAttributeEntity(AttributeEntityType attributeEntity) {
        this.attributeEntity = attributeEntity;
    }

    public List<UncertainEntityType> getUncertainEntityTypes() {
        return uncertainEntityTypes;
    }

    public void setUncertainEntityTypes(List<UncertainEntityType> uncertainEntityTypes) {
        this.uncertainEntityTypes = uncertainEntityTypes;
    }

    public List<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public List<SensorType> getSensorTypes() {
        return sensorTypes;
    }

    public void setSensorTypes(List<SensorType> sensorTypes) {
        this.sensorTypes = sensorTypes;
    }

    public List<CyberServiceType> getCyberServiceTypes() {
        return cyberServiceTypes;
    }

    public void setCyberServiceTypes(List<CyberServiceType> cyberServiceTypes) {
        this.cyberServiceTypes = cyberServiceTypes;
    }
}
