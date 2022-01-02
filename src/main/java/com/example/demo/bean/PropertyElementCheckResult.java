package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class PropertyElementCheckResult {
    private String dataName="";   ///涉及数据名
    private String instanceName="";  ///涉及实例名
    private String elementContent="";  ///内容
    private boolean neg=false;   ///是否是取反
    private List<double[][]> satTimeValues=new ArrayList<>();  ///记录每次满足状态的开始时间和结束时间 [0]开始时间,[1]结束时间

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getElementContent() {
        return elementContent;
    }

    public void setElementContent(String elementContent) {
        this.elementContent = elementContent;
    }

    public boolean isNeg() {
        return neg;
    }

    public void setNeg(boolean neg) {
        this.neg = neg;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public List<double[][]> getSatTimeValues() {
        return satTimeValues;
    }

    public void setSatTimeValues(List<double[][]> satTimeValues) {
        this.satTimeValues = satTimeValues;
    }
}
