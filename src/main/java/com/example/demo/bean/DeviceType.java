package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备类型，可以有多个实例
 * 设备类型名，状态标识符，【设备状态名、信号通道、状态标识符取值以及对各个属性的影响值】
 * */
public class DeviceType extends EntityType{
	// [0] state, [1]action [2]value
	//Bulb------bon, turn_bulb_on, 1
	public List<String[]> stateActionValues=new ArrayList<String[]>();
	public int deviceNumber=0;
	public List<StateEffect> stateEffects=new ArrayList<>();
	private String name="";


	public List<StateEffect> getStateEffects() {
		return stateEffects;
	}
	public void setStateEffects(List<StateEffect> stateEffects) {
		this.stateEffects = stateEffects;
	}
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

	public static class StateEffect{
		public StateEffect() {
		}

		private String state="";
		private List<String[]> effects=new ArrayList<>();
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public List<String[]> getEffects() {
			return effects;
		}
		public void setEffects(List<String[]> effects) {
			this.effects = effects;
		}

	}



	private int instanceNumber=0;  ///实例个数
	private List<StateSyncValueEffect> stateSyncValueEffects=new ArrayList<>();  ///各状态信息




	public int getInstanceNumber() {
		return instanceNumber;
	}

	public void setInstanceNumber(int instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	public List<StateSyncValueEffect> getStateSyncValueEffects() {
		return stateSyncValueEffects;
	}

	public void setStateSyncValueEffects(List<StateSyncValueEffect> stateSyncValueEffects) {
		this.stateSyncValueEffects = stateSyncValueEffects;
	}

	////状态信息
	public static class StateSyncValueEffect{
		public StateSyncValueEffect() {
		}

		private String stateName="";  ///状态名
		private String stateId="";    ///对应xml文件中的节点id
		private String synchronisation="";   ///对应同步信号通道
		private String value="";  ///identifier取值
		private List<String[]> effects=new ArrayList<>();  ///effect[0]=attribute, effect[1]=delta（对于会对总变化率产生影响的）, effect[2]=影响值

		public String getStateName() {
			return stateName;
		}

		public void setStateName(String stateName) {
			this.stateName = stateName;
		}

		public String getStateId() {
			return stateId;
		}

		public void setStateId(String stateId) {
			this.stateId = stateId;
		}

		public String getSynchronisation() {
			return synchronisation;
		}

		public void setSynchronisation(String synchronisation) {
			this.synchronisation = synchronisation;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public List<String[]> getEffects() {
			return effects;
		}

		public void setEffects(List<String[]> effects) {
			this.effects = effects;
		}
	}
}
