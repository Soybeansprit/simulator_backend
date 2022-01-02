package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * human，只有一个实例
 * 模型名，状态标识符，【状态名、状态标识符取值】
 * human模型是根据空间位置信息自动生成的
 * 直接把time声明在human模型下，作为局部变量
 * */
public class Human extends EntityType{
    private List<String[]> stateValues=new ArrayList<>();  //////stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值



    public List<String[]> getStateValues() {
        return stateValues;
    }

    public void setStateValues(List<String[]> stateValues) {
        this.stateValues = stateValues;
    }
}
