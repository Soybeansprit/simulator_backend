package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class CauseRule {

	private String state="";
	private int value=-1;
	private List<RuleNode> stateCausingRules=new ArrayList<RuleNode>();
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public List<RuleNode> getStateCausingRules() {
		return stateCausingRules;
	}
	public void setStateCausingRules(List<RuleNode> stateCausingRules) {
		this.stateCausingRules = stateCausingRules;
	}
	
}
