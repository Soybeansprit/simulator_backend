package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**直接获得的仿真结果数据如下所示，对其进行解析
 * coppm:
 * [0]: (0,70) (0,70) (300.0099999998721,70)
 * bulb[0]:
 * [0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (300.0099999998721,1)
 * blind[0]:
 * [0]: (0,0) (0,0) (30,0) (30.00000000000184,0) (30.07000000000185,1) (30.07000000000185,1) (300.0099999998721,1)
 * */
/**
 * 仿真路径解析结果，每个变量的仿真轨迹。
 * 数据名、所属实例名、标识符随时间的变化
 * coppm:[0,70] [300.0099999998721,70]
 * ……
 * */


public class DataTimeValue {
	/////仿真结果数据格式
//	public String name="";

	private String dataName=""; ////数据名
	private String instanceName="";  ////所属实例名
	private boolean isDevice=false;  ///是否是设备
	private List<double[]> timeValues=new ArrayList<double[]>();   ////(time,value)列表，为仿真结果存储格式  timeValue[0]是时间，timeValue[1]是取值


//	public String getName() {
//		return name;
//	}
//	public void setName(String name) {
//		this.name = name;
//	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public boolean isDevice() {
		return isDevice;
	}

	public void setDevice(boolean device) {
		isDevice = device;
	}

	public List<double[]> getTimeValues() {
		return timeValues;
	}
	public void setTimeValues(List<double[]> timeValues) {
		this.timeValues = timeValues;
	}

//	@Override
//	public String toString() {
//		return "DataTimeValue{" +
//				"name='" + name + '\'' +
//				", dataName='" + dataName + '\'' +
//				", instanceName='" + instanceName + '\'' +
//				", timeValues=" + timeValues +
//				'}';
//	}
}
