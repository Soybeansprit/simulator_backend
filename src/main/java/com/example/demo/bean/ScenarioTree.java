package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 对应前端中的场景树结构
 * 是树结构
 * */
public class ScenarioTree {
	public static class ScenesTree {
		////场景树
		private String name="";
		private List<SceneChild> children=new ArrayList<SceneChild>();
		
		
		public void addChildren(String sceneName,List<AttributeValue> sceneChildChildren) {
			SceneChild sceneChild=new SceneChild();
			sceneChild.setName(sceneName);
			sceneChild.setChildren(sceneChildChildren);
			this.children.add(sceneChild);
		}
		public void addChildren(SceneChild sceneChild) {
			this.children.add(sceneChild);
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<SceneChild> getChildren() {
			return children;
		}
		public void setChildren(List<SceneChild> children) {
			this.children = children;
		}

	}
	
	public static class SceneChild {
		////场景树子节点的名字和其子节点取值
		private String name="";     ////场景名
		private List<AttributeValue> children=new ArrayList<AttributeValue>();  ///各属性取值
		
		
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

	
	public static class AttributeValue {
		///////场景树中各可设置属性赋值
		private String name="";   ////属性名
		private double value=0;    ////属性值
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
	}
	
}
