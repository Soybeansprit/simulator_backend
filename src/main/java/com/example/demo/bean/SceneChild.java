package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class SceneChild {
	////场景树子节点的名字和其子节点取值
	private String name="";
	private List<AttributeValue> children=new ArrayList<AttributeValue>();
	
	
	public void addChildren(String attributeName,double value) {
		AttributeValue attributeValue=new AttributeValue();
		attributeValue.setName(attributeName);
		attributeValue.setValue(value);
		this.children.add(attributeValue);
	}
	public void addChildren(AttributeValue attributeValue) {
		this.children.add(attributeValue);
	}
	public void addChildrens(List<AttributeValue> attributeValues) {
		for(AttributeValue attributeValue:attributeValues) {
			this.addChildren(attributeValue);
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<AttributeValue> getChildren() {
		return children;
	}
	public void setChildren(List<AttributeValue> children) {
		this.children = children;
	}

}
