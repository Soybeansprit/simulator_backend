package com.example.demo.bean;
/**
 * 各实例的parent类
 * 包括实例名和实例类型名
 * */
public class Instance {
    private String instanceName="";  ///实例名
    private String entityTypeName="";  ///实力类型

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }
}
