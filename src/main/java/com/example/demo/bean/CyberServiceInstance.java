package com.example.demo.bean;
/**
 * 网络服务类型实体的实例
 * */
public class CyberServiceInstance extends Instance{
    private CyberServiceType cyberServiceType=new CyberServiceType();  ///所属网络服务的类型

    public CyberServiceType getCyberServiceType() {
        return cyberServiceType;
    }

    public void setCyberServiceType(CyberServiceType cyberServiceType) {
        this.cyberServiceType = cyberServiceType;
    }
}
