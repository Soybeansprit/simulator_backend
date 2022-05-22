package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 与执行指令相关的信息的类
 * */
public class Action {
//	public String action="";        ////////Bulb_0.turn_bulb_on
//	public String actionPulse="";   ////////turn_bulb_on
//	public String device="";        ////////Bulb_0
//	public String toState="";       ////////bon
//	public String value="";         ////////1  bulb=1
//	public String actionNum="";     ////////action1 用在生成IFD上作为节点id
//	 ////////////是不是会改变属性  attrVal[0]=attribute,attrVal[1]="'"||"=",attrVal[2]=value
////	public List<String[]> attrVal=new ArrayList<String[]>();
//	public List<Rule> rules=new ArrayList<Rule>();
//	public String getAction() {
//		return action;
//	}
//	public void setAction(String action) {
//		this.action = action;
//	}
//	public String getDevice() {
//		return device;
//	}
//	public String getActionPulse() {
//		return actionPulse;
//	}
//	public void setActionPulse(String actionPulse) {
//		this.actionPulse = actionPulse;
//	}
//	public String getActionNum() {
//		return actionNum;
//	}
//	public void setActionNum(String actionNum) {
//		this.actionNum = actionNum;
//	}
//	public void setDevice(String device) {
//		this.device = device;
//	}
//	public String getToState() {
//		return toState;
//	}
//	public void setToState(String toState) {
//		this.toState = toState;
//	}
////	public List<String[]> getAttrVal() {
////		return attrVal;
////	}
////	public void setAttrVal(List<String[]> attrVal) {
////		this.attrVal = attrVal;
////	}
//	public List<Rule> getRules() {
//		return rules;
//	}
//	public void setRules(List<Rule> rules) {
//		this.rules = rules;
//	}
//	public String getValue() {
//		return value;
//	}
//	public void setValue(String value) {
//		this.value = value;
//	}



	
//	@Override
//	public String toString() {
//		// TODO Auto-generated method stub
//		return this.actionNum+" : "+this.action;
//	}


	private String actionContent="";  ///action内容
	private String actionId="";   ///对应IFD中action节点的id
	private String instanceName="";  ///对应的实例名，可能是设备，也可能是cyber service
	private String sync="";   ///对应的synchronisation
	private List<Rule> relatedRules=new ArrayList<>();  ///会发送该执行指令action的TAP规则

	public String getActionContent() {
		return actionContent;
	}

	public void setActionContent(String actionContent) {
		this.actionContent = actionContent;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getSync() {
		return sync;
	}

	public void setSync(String sync) {
		this.sync = sync;
	}

	public List<Rule> getRelatedRules() {
		return relatedRules;
	}

	public void setRelatedRules(List<Rule> relatedRules) {
		this.relatedRules = relatedRules;
	}

}
