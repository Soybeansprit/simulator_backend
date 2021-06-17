package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class ScenesTree {
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
