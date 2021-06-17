package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class StaticAnalysisResult {

	public List<ErrorReason> incorrectRules=new ArrayList<ErrorReason>();
	public List<ErrorReason> unusedRules=new ArrayList<ErrorReason>();
	public List<List<Rule>> redundantRules=new ArrayList<List<Rule>>();
	public List<String> incompleteness=new ArrayList<String>();
	public List<Rule> usableRules=new ArrayList<Rule>();
	public List<Rule> totalRules=new ArrayList<>();
	public List<Rule> getTotalRules() {
		return totalRules;
	}
	public void setTotalRules(List<Rule> totalRules) {
		this.totalRules = totalRules;
	}
	public List<ErrorReason> getIncorrectRules() {
		return incorrectRules;
	}
	public void setIncorrectRules(List<ErrorReason> incorrectRules) {
		this.incorrectRules = incorrectRules;
	}
	public List<ErrorReason> getUnusedRules() {
		return unusedRules;
	}
	public void setUnusedRules(List<ErrorReason> unusedRules) {
		this.unusedRules = unusedRules;
	}
	public List<List<Rule>> getRedundantRules() {
		return redundantRules;
	}
	public void setRedundantRules(List<List<Rule>> redundantRules) {
		this.redundantRules = redundantRules;
	}
	public List<String> getIncompleteness() {
		return incompleteness;
	}
	public void setIncompleteness(List<String> incompleteness) {
		this.incompleteness = incompleteness;
	}
	public List<Rule> getUsableRules() {
		return usableRules;
	}
	public void setUsableRules(List<Rule> usableRules) {
		this.usableRules = usableRules;
	}
	

	
}
