package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class PropertyVerifyResult {

	private String property="";   ////property内容
	private boolean reachable=false;   ////是否可达
	private List<ReachableReason> reachableReasons=new ArrayList<>();    ///可达原因
	private boolean hasCorrespondRule=false;   ////有无对应的使该property不满足的规则
	private List<Rule> correspondingRules=new ArrayList<>();    ////如果有使不满足的规则，放使不满足的规则，如果无，放建议添加的使 不满足的规则
	public boolean isHasCorrespondRule() {
		return hasCorrespondRule;
	}
	public void setHasCorrespondRule(boolean hasCorrespondRule) {
		this.hasCorrespondRule = hasCorrespondRule;
	}
	public List<Rule> getCorrespondingRules() {
		return correspondingRules;
	}
	public void setCorrespondingRules(List<Rule> correspondingRules) {
		this.correspondingRules = correspondingRules;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public boolean isReachable() {
		return reachable;
	}
	public void setReachable(boolean reachable) {
		this.reachable = reachable;
	}
	public List<ReachableReason> getReachableReasons() {
		return reachableReasons;
	}
	public void setReachableReasons(List<ReachableReason> reachableReasons) {
		this.reachableReasons = reachableReasons;
	}
	
}
