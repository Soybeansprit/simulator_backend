package com.example.demo.bean;

public class UncertainEntityInstance extends Instance{
    private UncertainEntityType uncertainEntityType=new UncertainEntityType();

    public UncertainEntityType getUncertainEntityType() {
        return uncertainEntityType;
    }

    public void setUncertainEntityType(UncertainEntityType uncertainEntityType) {
        this.uncertainEntityType = uncertainEntityType;
    }
}
