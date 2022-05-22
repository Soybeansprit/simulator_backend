package com.example.demo.bean;
/**
 * 环境属性实体的实例
 * */
public class AttributeEntityInstance extends Instance{
    private AttributeEntityType attributeEntityType=new AttributeEntityType(); ///环境属性实体类型

    public AttributeEntityType getAttributeEntityType() {
        return attributeEntityType;
    }

    public void setAttributeEntityType(AttributeEntityType attributeEntityType) {
        this.attributeEntityType = attributeEntityType;
    }
}
