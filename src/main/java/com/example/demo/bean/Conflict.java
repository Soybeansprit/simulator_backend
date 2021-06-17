package com.example.demo.bean;


import java.util.ArrayList;
import java.util.List;

public class Conflict {
	private double time;
	private List<Integer> conflictValues=new ArrayList<>();
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public List<Integer> getConflictValues() {
		return conflictValues;
	}
	public void setConflictValues(List<Integer> conflictValues) {
		this.conflictValues = conflictValues;
	} 
}
