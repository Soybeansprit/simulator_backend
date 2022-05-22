package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 某个场景下，某设备在整个仿真过程中的冲突
 * */
public class DeviceConflict {
    private String instanceName="";   ///实例名,设备名
    private List<List<Double>> conflictTimeValues=new ArrayList<>();  ///整个仿真过程中所有【冲突发生的时间和冲突的状态值】，每次冲突时conflictTimeValue.get(0)=time,conflictTimeValue.get(1...)=value

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public List<List<Double>> getConflictTimeValues() {
        return conflictTimeValues;
    }

    public void setConflictTimeValues(List<List<Double>> conflictTimeValues) {
        this.conflictTimeValues = conflictTimeValues;
    }
}
