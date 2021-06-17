package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class DeviceAnalysisResult {
	private String deviceName="";
	private List<ConflictReason> conflictReasons=new ArrayList<>();
	private List<JitterReason> jitterReasons=new ArrayList<>();
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public List<ConflictReason> getConflictReasons() {
		return conflictReasons;
	}
	public void setConflictReasons(List<ConflictReason> conflictReasons) {
		this.conflictReasons = conflictReasons;
	}
	public List<JitterReason> getJitterReasons() {
		return jitterReasons;
	}
	public void setJitterReasons(List<JitterReason> jitterReasons) {
		this.jitterReasons = jitterReasons;
	}
	
}
