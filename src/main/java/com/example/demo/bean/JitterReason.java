package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class JitterReason {
	private List<double[]> jitter=new ArrayList<>();
	private List<CauseRule> causingRules=new ArrayList<>();
	public List<double[]> getJitter() {
		return jitter;
	}
	public void setJitter(List<double[]> jitter) {
		this.jitter = jitter;
	}
	public List<CauseRule> getCausingRules() {
		return causingRules;
	}
	public void setCausingRules(List<CauseRule> causingRules) {
		this.causingRules = causingRules;
	}

	
}
