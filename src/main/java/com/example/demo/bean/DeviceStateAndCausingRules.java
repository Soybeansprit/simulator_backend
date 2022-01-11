package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 记录设备状态发生的触发规则的原因
 * 包括设备名、状态
 * 以及状态发生的原因（包括前驱规则）
 * */
public class DeviceStateAndCausingRules {
    private String deviceName="";  ////设备名
    private String stateName="";   ////状态名
    private int stateValue=0;  ///状态取值
    private List<RuleAndPreRule> causingRulesAndPreRules=new ArrayList<>();

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public int getStateValue() {
        return stateValue;
    }

    public void setStateValue(int stateValue) {
        this.stateValue = stateValue;
    }

    public List<RuleAndPreRule> getCausingRulesAndPreRules() {
        return causingRulesAndPreRules;
    }

    public void setCausingRulesAndPreRules(List<RuleAndPreRule> causingRulesAndPreRules) {
        this.causingRulesAndPreRules = causingRulesAndPreRules;
    }
}
