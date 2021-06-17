package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;



public class TemplGraphNode {
	public String name="";
	public String id="";
	public String invariant="";
	public String style="";       //location or branchpoint or committed  urgent?
	public boolean flag=false;
	public List<TemplTransition> inTransitions=new ArrayList<TemplTransition>();
	public List<TemplTransition> outTransitions=new ArrayList<TemplTransition>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getInvariant() {
		return invariant;
	}
	public void setInvariant(String invariant) {
		this.invariant = invariant;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public List<TemplTransition> getInTransitions() {
		return inTransitions;
	}
	public void setInTransitions(List<TemplTransition> inTransitions) {
		this.inTransitions = inTransitions;
	}
	public List<TemplTransition> getOutTransitions() {
		return outTransitions;
	}
	public void setOutTransitions(List<TemplTransition> outTransitions) {
		this.outTransitions = outTransitions;
	}
}
