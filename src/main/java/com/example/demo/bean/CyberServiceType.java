package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * cyber service类型。类型名，【状态、信号通道】
 * */
public class CyberServiceType {
    private String cyberType=""; ///cyber service的类型名
    private List<String[]> stateSyncs=new ArrayList<>();  ///stateSync[0]=stateId,stateSync[1]=synchronization

    public String getCyberType() {
        return cyberType;
    }

    public void setCyberType(String cyberType) {
        this.cyberType = cyberType;
    }

    public List<String[]> getStateSyncs() {
        return stateSyncs;
    }

    public void setStateSyncs(List<String[]> stateSyncs) {
        this.stateSyncs = stateSyncs;
    }
}
