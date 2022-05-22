package com.example.demo.bean;

/**
 * 各实体类型的parent类
 * 包括类型名和状态标识符
 * */
public class EntityType {
    private String typeName=""; ///类型名
    private String identifier=""; ///状态标识符

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
