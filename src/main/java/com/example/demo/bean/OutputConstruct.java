package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class OutputConstruct {
	public static class EnvironmentStatic{
		private EnvironmentModel environmentModel;
		private StaticAnalysisResult staticAnalysisResult;
		
		public EnvironmentStatic() {
			super();
		}
		public EnvironmentStatic(EnvironmentModel environmentModel, StaticAnalysisResult staticAnalysisResult) {
			super();
			this.environmentModel = environmentModel;
			this.staticAnalysisResult = staticAnalysisResult;
		}
		public EnvironmentModel getEnvironmentModel() {
			return environmentModel;
		}
		public void setEnvironmentModel(EnvironmentModel environmentModel) {
			this.environmentModel = environmentModel;
		}
		public StaticAnalysisResult getStaticAnalysisResult() {
			return staticAnalysisResult;
		}
		public void setStaticAnalysisResult(StaticAnalysisResult staticAnalysisResult) {
			this.staticAnalysisResult = staticAnalysisResult;
		}
		
	}
	
	public static class ScenePropertyResult{
		private List<Scene> scenes=new ArrayList<Scene>();
		private List<PropertyVerifyResult> propertyVerifyResults=new ArrayList<>();
		public List<PropertyVerifyResult> getPropertyVerifyResults() {
			return propertyVerifyResults;
		}
		public void setPropertyVerifyResults(List<PropertyVerifyResult> propertyVerifyResults) {
			this.propertyVerifyResults = propertyVerifyResults;
		}
		public List<Scene> getScenes() {
			return scenes;
		}
		public void setScenes(List<Scene> scenes) {
			this.scenes = scenes;
		}

		
	}
	
	public static class DeclarationQueryResult{
		private List<List<Trigger>> attributesSameTriggers;
		private List<String[]> declarations;
		public List<List<Trigger>> getAttributesSameTriggers() {
			return attributesSameTriggers;
		}
		public void setAttributesSameTriggers(List<List<Trigger>> attributesSameTriggers) {
			this.attributesSameTriggers = attributesSameTriggers;
		}
		public List<String[]> getDeclarations() {
			return declarations;
		}
		public void setDeclarations(List<String[]> declarations) {
			this.declarations = declarations;
		}
		
	}

	public static class ScenesTreeAndEnvironmentInstance{
		private ScenarioTree.ScenesTree scenesTree=new ScenarioTree.ScenesTree();
		private InstanceLayer interactiveEnvironment=new InstanceLayer();

		public ScenarioTree.ScenesTree getScenesTree() {
			return scenesTree;
		}

		public void setScenesTree(ScenarioTree.ScenesTree scenesTree) {
			this.scenesTree = scenesTree;
		}

		public InstanceLayer getInteractiveEnvironment() {
			return interactiveEnvironment;
		}

		public void setInteractiveEnvironment(InstanceLayer interactiveEnvironment) {
			this.interactiveEnvironment = interactiveEnvironment;
		}
	}

	public static class InstanceLayerOutput{
		private InstanceLayer instanceLayer=new InstanceLayer();
		private ModelLayer modelLayer=new ModelLayer();

		public InstanceLayer getInstanceLayer() {
			return instanceLayer;
		}

		public void setInstanceLayer(InstanceLayer instanceLayer) {
			this.instanceLayer = instanceLayer;
		}

		public ModelLayer getModelLayer() {
			return modelLayer;
		}

		public void setModelLayer(ModelLayer modelLayer) {
			this.modelLayer = modelLayer;
		}
	}

	public static class InteractiveLayerAndRules{
		private InstanceLayer interactiveInstance=new InstanceLayer();
		private List<Rule> rules=new ArrayList<>();
		private String ifdFileName="";

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

		public String getIfdFileName() {
			return ifdFileName;
		}

		public void setIfdFileName(String ifdFileName) {
			this.ifdFileName = ifdFileName;
		}
	}
}
