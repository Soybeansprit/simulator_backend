package com.example.demo.bean;

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
