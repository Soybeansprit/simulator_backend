package com.example.demo.bean;
/**
 * 设备（执行器）类型的实例
 * 包括实例名、所处的空间位置、设备类型、是否可被屋外观察
 * */
public class DeviceInstance extends Instance{
    private String location="";   //设备位置
    private int sequenceNumber=0;  //设备实例的序号
    private DeviceType deviceType=new DeviceType();  //设备类型
    private boolean visible=false; //设备是否可别外界观察

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
