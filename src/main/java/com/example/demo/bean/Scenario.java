package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 存放每个场景的信息，包括场景序号（对应场景名）、场景仿真路径等
 * 冲突、抖动
 * */
public class Scenario {
    private String scenarioName="";  ////场景名
    private List<DataTimeValue> dataTimeValues=new ArrayList<>();  ///仿真路径
    private List<DeviceConflict> deviceConflicts=new ArrayList<>(); ///设备冲突分析
    private List<DeviceJitter> deviceJitters=new ArrayList<>();///设备抖动分析


    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        this.scenarioName = scenarioName;
    }

    public List<DataTimeValue> getDataTimeValues() {
        return dataTimeValues;
    }

    public void setDataTimeValues(List<DataTimeValue> dataTimeValues) {
        this.dataTimeValues = dataTimeValues;
    }

    public List<DeviceConflict> getDeviceConflicts() {
        return deviceConflicts;
    }

    public void setDeviceConflicts(List<DeviceConflict> deviceConflicts) {
        this.deviceConflicts = deviceConflicts;
    }

    public List<DeviceJitter> getDeviceJitters() {
        return deviceJitters;
    }

    public void setDeviceJitters(List<DeviceJitter> deviceJitters) {
        this.deviceJitters = deviceJitters;
    }
}
