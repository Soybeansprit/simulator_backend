package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录某个设备在整个仿真过程中的抖动
 * */
public class DeviceJitter {
    private String instanceName="";   ///实例名，设备名
    private List<List<double[]>> jitterTimeValues=new ArrayList<>();   ///整个仿真过程发生的所有抖动【时间状态、时间另一个状态。。。】 List<double[]>, 一次抖动jitterTimeValue.get(0)[0]=time, jitterTimeValue.get(0)[1]=value...

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public List<List<double[]>> getJitterTimeValues() {
        return jitterTimeValues;
    }

    public void setJitterTimeValues(List<List<double[]>> jitterTimeValues) {
        this.jitterTimeValues = jitterTimeValues;
    }
}
