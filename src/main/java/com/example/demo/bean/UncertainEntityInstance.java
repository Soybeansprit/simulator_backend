package com.example.demo.bean;

/**
 * 不确定实体实例
 * */
public class UncertainEntityInstance extends Instance{
    private UncertainEntityType uncertainEntityType=new UncertainEntityType();

    public UncertainEntityType getUncertainEntityType() {
        return uncertainEntityType;
    }

    public void setUncertainEntityType(UncertainEntityType uncertainEntityType) {
        this.uncertainEntityType = uncertainEntityType;
    }
}
