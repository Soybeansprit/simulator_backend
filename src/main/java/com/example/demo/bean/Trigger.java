package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class Trigger {
	public String trigger="";                ///////temperature>=30         //////Bulb_0.bon     ///Person.lobby(人在客厅)
	public String triggerNum="";			 ///////trigger1                //////trigger2       ///trigger3
	public String device="";                 ///////temperature_sensor      //////Bulb_0         ///person_sensor
	public String[] attrVal=new String[3];   ///////temperature >= 30       //////Bulb_0 . bon   ///position = 1
	public List<Rule> rules=new ArrayList<Rule>();
	public String getTrigger() {
		return trigger;
	}
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	public String getTriggerNum() {
		return triggerNum;
	}
	public void setTriggerNum(String triggerNum) {
		this.triggerNum = triggerNum;
	}
	public String[] getAttrVal() {
		return attrVal;
	}
	public void setAttrVal(String[] attrVal) {
		this.attrVal = attrVal;
	}
	public List<Rule> getRules() {
		return rules;
	}
	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.triggerNum+" : "+this.trigger;
	}


	private String triggerContent="";  ///trigger内容                                                    ///////temperature>=30         //////Bulb_0.bon     ///Emma.lobby(人在客厅)
	private String triggerId="";   ///对应IFD中trigger节点的id                                            ///////trigger1                //////trigger2       ///trigger3
	private String instanceName="";  ///对应的实例名                                                      //////Air                      /////Bulb_0          ////Emma
	private String sensor="";   ///对应的sensor                                                         ///////temperature_sensor     //////                ///person_sensor
	private String[] triggerForm=new String[3];  /// attribute<(<=,>,>=)value or Instance.state        ///////temperature >= 30       //////Bulb_0 . bon   ///Emma . lobby
	private String forTime="";  ///表示for多长时间  instance.state for 3这种
	private List<Rule> relatedRules=new ArrayList<>();

	public String getTriggerContent() {
		return triggerContent;
	}

	public void setTriggerContent(String triggerContent) {
		this.triggerContent = triggerContent;
	}

	public String getTriggerId() {
		return triggerId;
	}

	public void setTriggerId(String triggerId) {
		this.triggerId = triggerId;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getSensor() {
		return sensor;
	}

	public void setSensor(String sensor) {
		this.sensor = sensor;
	}

	public String[] getTriggerForm() {
		return triggerForm;
	}

	public void setTriggerForm(String[] triggerForm) {
		this.triggerForm = triggerForm;
	}

	public String getForTime() {
		return forTime;
	}

	public void setForTime(String forTime) {
		this.forTime = forTime;
	}

	public List<Rule> getRelatedRules() {
		return relatedRules;
	}

	public void setRelatedRules(List<Rule> relatedRules) {
		this.relatedRules = relatedRules;
	}
}
