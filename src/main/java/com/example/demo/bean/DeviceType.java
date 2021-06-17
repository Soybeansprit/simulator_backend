package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class DeviceType extends Entity{
	// [0] state, [1]action [2]value
	//Bulb------bon, turn_bulb_on, 1
	public List<String[]> stateActionValues=new ArrayList<String[]>();
	public int deviceNumber=0;

	public int getDeviceNumber() {
		return deviceNumber;
	}
	public void setDeviceNumber(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String[]> getStateActionValues() {
		return stateActionValues;
	}
	public void setStateActionValues(List<String[]> stateActionValues) {
		this.stateActionValues = stateActionValues;
	}

	
}
