package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 不确定行为实体，只有一个实例
 * 实体类型名，状态标识符，【状态名、状态标识符取值】
 * */
public class UncertainEntityType extends EntityType{
    private List<String[]> stateValues=new ArrayList<>();  ///stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值



    public List<String[]> getStateValues() {
        return stateValues;
    }

    public void setStateValues(List<String[]> stateValues) {
        this.stateValues = stateValues;
    }
}
