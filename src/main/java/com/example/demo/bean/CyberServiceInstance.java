package com.example.demo.bean;

public class CyberServiceInstance extends Instance{
    private CyberServiceType cyberServiceType=new CyberServiceType();

    public CyberServiceType getCyberServiceType() {
        return cyberServiceType;
    }

    public void setCyberServiceType(CyberServiceType cyberServiceType) {
        this.cyberServiceType = cyberServiceType;
    }
}
