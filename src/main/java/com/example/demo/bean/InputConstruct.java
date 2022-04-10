package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.bean.ScenarioTree.ScenesTree;

public class InputConstruct {


//	public static class EnvironmentRule{
//		private EnvironmentModel environmentModel;
//		private List<Rule> rules=new ArrayList<Rule>();
//		public EnvironmentModel getEnvironmentModel() {
//			return environmentModel;
//		}
//		public void setEnvironmentModel(EnvironmentModel environmentModel) {
//			this.environmentModel = environmentModel;
//		}
//		public List<Rule> getRules() {
//			return rules;
//		}
//		public void setRules(List<Rule> rules) {
//			this.rules = rules;
//		}
//		public EnvironmentRule(EnvironmentModel environmentModel, List<Rule> rules) {
//			super();
//			this.environmentModel = environmentModel;
//			this.rules = rules;
//		}
//		public EnvironmentRule() {
//			super();
//		}
//
//	}
//
//	public static class SceneTreeDevice{
//		private List<DeviceDetail> devices=new ArrayList<>();
//		private ScenesTree scenesTree;
//
//		public List<DeviceDetail> getDevices() {
//			return devices;
//		}
//		public void setDevices(List<DeviceDetail> devices) {
//			this.devices = devices;
//		}
//		public ScenesTree getScenesTree() {
//			return scenesTree;
//		}
//		public void setScenesTree(ScenesTree scenesTree) {
//			this.scenesTree = scenesTree;
//		}
//
//	}
//
//
//	public static class SceneEnvironmentProperty{
//		private List<Scene> scenes=new ArrayList<>();
//		private EnvironmentModel environmentModel;
//		private List<String> properties=new ArrayList<>();
//		private List<Rule> rules=new ArrayList<>();
//
//		public List<Rule> getRules() {
//			return rules;
//		}
//		public void setRules(List<Rule> rules) {
//			this.rules = rules;
//		}
//		public List<String> getProperties() {
//			return properties;
//		}
//		public void setProperties(List<String> properties) {
//			this.properties = properties;
//		}
//		public List<Scene> getScenes() {
//			return scenes;
//		}
//		public void setScenes(List<Scene> scenes) {
//			this.scenes = scenes;
//		}
//		public EnvironmentModel getEnvironmentModel() {
//			return environmentModel;
//		}
//		public void setEnvironmentModel(EnvironmentModel environmentModel) {
//			this.environmentModel = environmentModel;
//		}
//
//	}
//
//
//	public static class SceneEnvironmentRule{
//		private Scene scene;
//		private EnvironmentModel environmentModel;
//		private List<Rule> rules=new ArrayList<>();
//		public Scene getScene() {
//			return scene;
//		}
//		public void setScene(Scene scene) {
//			this.scene = scene;
//		}
//		public EnvironmentModel getEnvironmentModel() {
//			return environmentModel;
//		}
//		public void setEnvironmentModel(EnvironmentModel environmentModel) {
//			this.environmentModel = environmentModel;
//		}
//		public List<Rule> getRules() {
//			return rules;
//		}
//		public void setRules(List<Rule> rules) {
//			this.rules = rules;
//		}
//
//	}



	public static class ModelInstanceLayerAndRuleStrLists{
		private ModelLayer modelLayer=new ModelLayer();
		private InstanceLayer instanceLayer=new InstanceLayer();
		private List<String> ruleTestLines=new ArrayList<>();

		public ModelLayer getModelLayer() {
			return modelLayer;
		}

		public void setModelLayer(ModelLayer modelLayer) {
			this.modelLayer = modelLayer;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}

		public List<String> getRuleTestLines() {
			return ruleTestLines;
		}

		public void setRuleTestLines(List<String> ruleTestLines) {
			this.ruleTestLines = ruleTestLines;
		}
	}

	public static class SingleScenarioGenerateInput{
		private String modelFileName="";
		private ModelLayer modelLayer=new ModelLayer();
		private InstanceLayer instanceLayer=new InstanceLayer();
		private InstanceLayer interactiveInstance=new InstanceLayer();
		private List<Rule> rules=new ArrayList<>();
		private String simulationTime="";
		private List<String[]> attributeValues=new ArrayList<>();

		public String getModelFileName() {
			return modelFileName;
		}

		public void setModelFileName(String modelFileName) {
			this.modelFileName = modelFileName;
		}

		public ModelLayer getModelLayer() {
			return modelLayer;
		}

		public void setModelLayer(ModelLayer modelLayer) {
			this.modelLayer = modelLayer;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}

		public InstanceLayer getInteractiveInstance() {
			return interactiveInstance;
		}

		public void setInteractiveInstance(InstanceLayer interactiveInstance) {
			this.interactiveInstance = interactiveInstance;
		}

		public List<Rule> getRules() {
			return rules;
		}

		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}

		public String getSimulationTime() {
			return simulationTime;
		}

		public void setSimulationTime(String simulationTime) {
			this.simulationTime = simulationTime;
		}

		public List<String[]> getAttributeValues() {
			return attributeValues;
		}

		public void setAttributeValues(List<String[]> attributeValues) {
			this.attributeValues = attributeValues;
		}
	}

	public static class BestScenarioGenerateInput{
		private String modelFileName="";
		private ModelLayer modelLayer=new ModelLayer();
		private InstanceLayer instanceLayer=new InstanceLayer();
		private InstanceLayer interactiveInstance=new InstanceLayer();
		private List<Rule> rules=new ArrayList<>();
		private String simulationTime="";
		private String ifdFileName="";

		public String getModelFileName() {
			return modelFileName;
		}

		public void setModelFileName(String modelFileName) {
			this.modelFileName = modelFileName;
		}

		public ModelLayer getModelLayer() {
			return modelLayer;
		}

		public void setModelLayer(ModelLayer modelLayer) {
			this.modelLayer = modelLayer;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}

		public InstanceLayer getInteractiveInstance() {
			return interactiveInstance;
		}

		public void setInteractiveInstance(InstanceLayer interactiveInstance) {
			this.interactiveInstance = interactiveInstance;
		}

		public List<Rule> getRules() {
			return rules;
		}

		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}

		public String getSimulationTime() {
			return simulationTime;
		}

		public void setSimulationTime(String simulationTime) {
			this.simulationTime = simulationTime;
		}

		public String getIfdFileName() {
			return ifdFileName;
		}

		public void setIfdFileName(String ifdFileName) {
			this.ifdFileName = ifdFileName;
		}
	}

	public static class EnergyConsumptionInput{
		private List<DataTimeValue> dataTimeValues=new ArrayList<>();
		private List<DeviceInstance> deviceInstances=new ArrayList<>();

		public List<DataTimeValue> getDataTimeValues() {
			return dataTimeValues;
		}

		public void setDataTimeValues(List<DataTimeValue> dataTimeValues) {
			this.dataTimeValues = dataTimeValues;
		}

		public List<DeviceInstance> getDeviceInstances() {
			return deviceInstances;
		}

		public void setDeviceInstances(List<DeviceInstance> deviceInstances) {
			this.deviceInstances = deviceInstances;
		}
	}

	public static class MultiScenarioGenerateInput {
		private String modelFileName="";
		private ModelLayer modelLayer=new ModelLayer();
		private InstanceLayer instanceLayer=new InstanceLayer();
		private InstanceLayer interactiveInstance=new InstanceLayer();
		private List<Rule> rules=new ArrayList<>();
		private String simulationTime="";

		public String getModelFileName() {
			return modelFileName;
		}

		public void setModelFileName(String modelFileName) {
			this.modelFileName = modelFileName;
		}

		public ModelLayer getModelLayer() {
			return modelLayer;
		}

		public void setModelLayer(ModelLayer modelLayer) {
			this.modelLayer = modelLayer;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}

		public InstanceLayer getInteractiveInstance() {
			return interactiveInstance;
		}

		public void setInteractiveInstance(InstanceLayer interactiveInstance) {
			this.interactiveInstance = interactiveInstance;
		}

		public List<Rule> getRules() {
			return rules;
		}

		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}

		public String getSimulationTime() {
			return simulationTime;
		}

		public void setSimulationTime(String simulationTime) {
			this.simulationTime = simulationTime;
		}
	}

	public static class MultiScenarioSimulateInput {
		private String modelFileName="";
		private InstanceLayer instanceLayer=new InstanceLayer();
		private ScenesTree scenesTree=new ScenesTree();

		public String getModelFileName() {
			return modelFileName;
		}

		public void setModelFileName(String modelFileName) {
			this.modelFileName = modelFileName;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}

		public ScenesTree getScenesTree() {
			return scenesTree;
		}

		public void setScenesTree(ScenesTree scenesTree) {
			this.scenesTree = scenesTree;
		}
	}

	public static class ConsumptionInput{
		private DataTimeValue dataTimeValue=new DataTimeValue();
		private DeviceInstance deviceInstance=new DeviceInstance();

		public DataTimeValue getDataTimeValue() {
			return dataTimeValue;
		}

		public void setDataTimeValue(DataTimeValue dataTimeValue) {
			this.dataTimeValue = dataTimeValue;
		}

		public DeviceInstance getDeviceInstance() {
			return deviceInstance;
		}

		public void setDeviceInstance(DeviceInstance deviceInstance) {
			this.deviceInstance = deviceInstance;
		}
	}

	public static class LocationInput{
		private List<DeviceInstance> deviceInstances=new ArrayList<>();
		private List<Scenario> scenarios=new ArrayList<>();
		private List<Rule> rules=new ArrayList<>();
		private String ifdFileName="";

		public List<DeviceInstance> getDeviceInstances() {
			return deviceInstances;
		}

		public void setDeviceInstances(List<DeviceInstance> deviceInstances) {
			this.deviceInstances = deviceInstances;
		}

		public List<Scenario> getScenarios() {
			return scenarios;
		}

		public List<Rule> getRules() {
			return rules;
		}

		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}

		public void setScenarios(List<Scenario> scenarios) {
			this.scenarios = scenarios;
		}

		public String getIfdFileName() {
			return ifdFileName;
		}

		public void setIfdFileName(String ifdFileName) {
			this.ifdFileName = ifdFileName;
		}
	}

	public static class StaticAnalysisInput{
		private List<Rule> rules=new ArrayList<>();
		private InstanceLayer instanceLayer=new InstanceLayer();

		public List<Rule> getRules() {
			return rules;
		}

		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}
	}

	public static class SatisfactionInput{
		private String attribute="";
		private String lowValue="";
		private String highValue="";
		private List<DataTimeValue> dataTimeValues=new ArrayList<>();

		public String getAttribute() {
			return attribute;
		}

		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}

		public String getLowValue() {
			return lowValue;
		}

		public void setLowValue(String lowValue) {
			this.lowValue = lowValue;
		}

		public String getHighValue() {
			return highValue;
		}

		public void setHighValue(String highValue) {
			this.highValue = highValue;
		}

		public List<DataTimeValue> getDataTimeValues() {
			return dataTimeValues;
		}

		public void setDataTimeValues(List<DataTimeValue> dataTimeValues) {
			this.dataTimeValues = dataTimeValues;
		}
	}

	public static class OtherAnalysisInput{
		private List<Scenario> scenarios=new ArrayList<>();
		private InstanceLayer instanceLayer=new InstanceLayer();

		public List<Scenario> getScenarios() {
			return scenarios;
		}

		public void setScenarios(List<Scenario> scenarios) {
			this.scenarios = scenarios;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}
	}

	public static class PropertyAnalysisInput{
		private List<Scenario> scenarios=new ArrayList<>();
		private List<Rule> rules=new ArrayList<>();
		private List<String> properties=new ArrayList<>();
		private InstanceLayer instanceLayer=new InstanceLayer();

		public List<Scenario> getScenarios() {
			return scenarios;
		}

		public void setScenarios(List<Scenario> scenarios) {
			this.scenarios = scenarios;
		}

		public List<Rule> getRules() {
			return rules;
		}

		public void setRules(List<Rule> rules) {
			this.rules = rules;
		}

		public List<String> getProperties() {
			return properties;
		}

		public void setProperties(List<String> properties) {
			this.properties = properties;
		}

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}
	}
	
}
