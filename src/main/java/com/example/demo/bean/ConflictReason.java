package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class ConflictReason {
	private Conflict conflict=new Conflict();
	private List<CauseRule> causingRules=new ArrayList<>();
	public Conflict getConflict() {
		return conflict;
	}
	public void setConflict(Conflict conflict) {
		this.conflict = conflict;
	}
	public List<CauseRule> getCausingRules() {
		return causingRules;
	}
	public void setCausingRules(List<CauseRule> causingRules) {
		this.causingRules = causingRules;
	}

	
}
