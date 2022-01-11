package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 存放每个场景的信息，包括场景序号（对应场景名）、场景仿真路径等
 * 冲突、抖动
 * */
public class Scenario {
    private int scenarioNum=0;   ////场景名
    private List<DataTimeValue> dataTimeValues=new ArrayList<>();

    public int getScenarioNum() {
        return scenarioNum;
    }

    public void setScenarioNum(int scenarioNum) {
        this.scenarioNum = scenarioNum;
    }

    public List<DataTimeValue> getDataTimeValues() {
        return dataTimeValues;
    }

    public void setDataTimeValues(List<DataTimeValue> dataTimeValues) {
        this.dataTimeValues = dataTimeValues;
    }
}
