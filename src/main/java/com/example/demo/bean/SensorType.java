package com.example.demo.bean;

/**
 * 传感器类型，只有一个实例，但其实最终并不用于运行，因为系统运行时各模型之间其实是使用共享变量或者信号通道来进行交流
 * 传感器类型名，检测属性，检测属性类型
 * */
public class SensorType extends Entity{
	private String sensorType=""; ///传感器类型名
	private String monitoredEntityType=""; //检测属性所属模型类型
	private String attribute="";  ///传感器检测什么属性
	private String style="";   ///该属性所属实体的类型 "causal" or "biddable"

	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}


	public String getSensorType() {
		return sensorType;
	}

	public void setSensorType(String sensorType) {
		this.sensorType = sensorType;
	}

	public String getMonitoredEntityType() {
		return monitoredEntityType;
	}

	public void setMonitoredEntityType(String monitoredEntityType) {
		this.monitoredEntityType = monitoredEntityType;
	}
}
