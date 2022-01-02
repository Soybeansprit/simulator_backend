package com.example.demo.bean;

public class AttributeEntityInstance extends Instance{
    private AttributeEntityType attributeEntityType=new AttributeEntityType();

    public AttributeEntityType getAttributeEntityType() {
        return attributeEntityType;
    }

    public void setAttributeEntityType(AttributeEntityType attributeEntityType) {
        this.attributeEntityType = attributeEntityType;
    }
}
