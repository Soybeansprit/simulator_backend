package com.example.demo.controller;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.example.demo.bean.*;
import com.example.demo.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.OutputConstruct.DeclarationQueryResult;
import com.example.demo.bean.OutputConstruct.EnvironmentStatic;
import com.example.demo.bean.OutputConstruct.ScenePropertyResult;
import com.example.demo.bean.ScenarioTree.ScenesTree;
import com.example.demo.bean.InputConstruct.EnvironmentRule;
import com.example.demo.bean.InputConstruct.SceneEnvironmentProperty;
import com.example.demo.bean.InputConstruct.SceneEnvironmentRule;
import com.example.demo.bean.InputConstruct.SceneTreeDevice;


@CrossOrigin
@RestController
//@Controller
@RequestMapping("/analysis")
public class Controller {

	@Autowired
	AddressService addressService;
	@Autowired
	RuleService ruleService;
	@Autowired
	TemplGraphService templGraphService;
	@Autowired
	StaticAnalysisService staticAnalysisService;
	@Autowired
	DynamicAnalysisService dynamicAnalysisService;
	
	@RequestMapping("/upload")
	@ResponseBody
	public void uploadFile(@RequestParam("file") MultipartFile uploadedFile) throws DocumentException, IOException {
		//////////////上传的环境本体文件，存储在D:\\workspace位置
		if (uploadedFile == null) {
            System.out.println("上传失败，无法找到文件！");
        }
        //上传xml文件和properties文件		
        String fileName = uploadedFile.getOriginalFilename();
        String filePath=AddressService.MODEL_FILE_PATH+fileName;
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));

        outputStream.write(uploadedFile.getBytes());
        outputStream.flush();
        outputStream.close();
        //逻辑处理
        System.out.println(fileName + "上传成功");
	}
	
	/////静态分析
	@RequestMapping(value="/getStaticAnalysisResult",method = RequestMethod.POST)
	@ResponseBody
	public EnvironmentStatic getStaticAnalysisResult(@RequestBody List<String> ruleTextLines,String initModelFileName,String propertyFileName) throws DocumentException, IOException {
		/////生成规则结构
		List<Rule> rules=RuleService.getRuleList(ruleTextLines);
//		System.out.println(initModelFileName);
		////设置更改后的模型文件名
		AddressService.setChangedModelFileName(initModelFileName);
		////获得环境模型
		long environmentStartTime=System.currentTimeMillis();
		EnvironmentModel environmentModel=TemplGraphService.getEnvironmentModel(initModelFileName, AddressService.changed_model_file_Name, AddressService.MODEL_FILE_PATH, propertyFileName);
		System.out.println("time getting environmentModel:"+(System.currentTimeMillis()-environmentStartTime));
		////静态分析
		StaticAnalysisResult staticAnalysisResult=StaticAnalysisService.getStaticAnalaysisResult(rules, AddressService.IFD_FILE_NAME,  AddressService.IFD_FILE_PATH, environmentModel);
		EnvironmentStatic environmentStatic=new EnvironmentStatic(environmentModel, staticAnalysisResult);
		return environmentStatic;
	}
	
	/////生成多个场景模型,并返回场景树结构
	@RequestMapping(value="/generateAllScenarioModels",method = RequestMethod.POST)
	@ResponseBody
	public ScenesTree generateAllScenarioModels(@RequestBody EnvironmentRule environmentRule,String initModelFileName,String simulationTime) throws DocumentException, IOException {
		////更改后的模型文件名
		AddressService.setChangedModelFileName(initModelFileName);
		List<Rule> rules=environmentRule.getRules();
		List<DeviceDetail> devices=environmentRule.getEnvironmentModel().getDevices();
		List<DeviceType> deviceTypes=environmentRule.getEnvironmentModel().getDeviceTypes();
		List<BiddableType> biddableTypes=environmentRule.getEnvironmentModel().getBiddables();
		List<SensorType> sensorTypes=environmentRule.getEnvironmentModel().getSensors();
		List<Attribute> attributes=environmentRule.getEnvironmentModel().getAttributes();
		////先生成控制器模型，，包括一些参数声明，modelDeclaration，query；
		long generateStartTime=System.currentTimeMillis();
		SystemModelService.generateContrModel(AddressService.MODEL_FILE_PATH,AddressService.changed_model_file_Name, rules, devices, biddableTypes);
		System.out.println("controllerGenerationTime:"+(System.currentTimeMillis()-generateStartTime));
		DeclarationQueryResult declarationQueryResult=SystemModelService.generateModelDeclarationAndQuery(AddressService.MODEL_FILE_PATH,AddressService.changed_model_file_Name, rules, devices, deviceTypes, biddableTypes, sensorTypes, attributes, simulationTime);
//		List<List<String>> attributesSameTriggers=SystemModelService.generateContrModel(AddressService.MODEL_FILE_PATH,AddressService.changed_model_file_Name, rules, biddableTypes, deviceTypes, biddableTypes, sensorTypes, attributes,simulationTime);
		/////再分别设置某些参数初始值。
		System.out.println("modelDeclaration:"+(System.currentTimeMillis()-generateStartTime));
		ScenesTree scenesTree=SystemModelService.generateAllScenarios(AddressService.changed_model_file_Name, AddressService.MODEL_FILE_PATH,declarationQueryResult);
		long generateTime=System.currentTimeMillis()-generateStartTime;
		System.out.println(generateTime);
		return scenesTree;
	}
	
	/////生成单个场景模型,并仿真获得分析结果
	@RequestMapping(value="/generateBestScenarioModelAndSimulate",method = RequestMethod.POST)
	@ResponseBody
	public static Scene generateBestScenarioModelAndSimulate(@RequestBody EnvironmentRule environmentRule,String initModelFileName,String simulationTime) throws DocumentException, IOException {
		////更改后的模型文件名
		AddressService.setChangedModelFileName(initModelFileName);
		String fileNameWithoutSuffix=initModelFileName.substring(0, initModelFileName.lastIndexOf(".xml"));
		List<Rule> rules=environmentRule.getRules();
		List<DeviceDetail> devices=environmentRule.getEnvironmentModel().getDevices();
		List<DeviceType> deviceTypes=environmentRule.getEnvironmentModel().getDeviceTypes();
		List<BiddableType> biddableTypes=environmentRule.getEnvironmentModel().getBiddables();
		List<SensorType> sensorTypes=environmentRule.getEnvironmentModel().getSensors();
		List<Attribute> attributes=environmentRule.getEnvironmentModel().getAttributes();
		////ruleMap
		HashMap<String,Rule> ruleMap=new HashMap<>();
		for(Rule rule:rules) {
			ruleMap.put(rule.getRuleName(), rule);
		}
		/////获得ifd上各节点
		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(AddressService.IFD_FILE_NAME, AddressService.IFD_FILE_PATH);
		SystemModelService.generateContrModel(AddressService.MODEL_FILE_PATH,AddressService.changed_model_file_Name, rules, devices, biddableTypes);
		SystemModelService.generateBestScenarioModel(rules, devices, deviceTypes, biddableTypes, sensorTypes, attributes, AddressService.changed_model_file_Name, AddressService.MODEL_FILE_PATH, graphNodes, fileNameWithoutSuffix+"-scenario-best.xml", simulationTime);
		
		///仿真
		Scene scene=DynamicAnalysisService.getSingleSimulationResult(devices, AddressService.UPPAAL_PATH, fileNameWithoutSuffix, "best", AddressService.MODEL_FILE_PATH, AddressService.SIMULATE_RESULT_FILE_PATH);
		///动态分析
		DynamicAnalysisService.getSingleScenarioDynamicAnalysis(scene, devices, graphNodes, ruleMap,simulationTime,"24","300");
		return scene;
	}
	
	////仿真进行动态分析
	@RequestMapping(value="/simulateAllScenarioModels",method = RequestMethod.POST)
	@ResponseBody
	public List<Scene> simulateAllScenarioModels(@RequestBody SceneTreeDevice sceneTreeDevice, String initModelFileName) {
		ScenesTree scenesTree=sceneTreeDevice.getScenesTree();
		List<DeviceDetail> devices=sceneTreeDevice.getDevices();
		AddressService.setChangedModelFileName(initModelFileName);
		List<Scene> scenes=DynamicAnalysisService.getAllSimulationResults(scenesTree, devices, AddressService.changed_model_file_Name, AddressService.MODEL_FILE_PATH, AddressService.UPPAAL_PATH,AddressService.SIMULATE_RESULT_FILE_PATH);
		return scenes;
	}
	
//	/////所有场景动态分析，返回有哪些错误以及错误原因
//	@RequestMapping(value="/getAllDynamicAnalysisResult",method = RequestMethod.POST)
//	@ResponseBody
//	public ScenePropertyResult getAllDynamicAnalysisResult(@RequestBody SceneEnvironmentProperty sceneEnvironmentProperty,String simulationTime,String equivalentTime,String intervalTime){
//		long t1=System.currentTimeMillis();
//		List<Scene> scenes=sceneEnvironmentProperty.getScenes();
//		EnvironmentModel environmentModel=sceneEnvironmentProperty.getEnvironmentModel();
//		List<String> properties =sceneEnvironmentProperty.getProperties();
//		List<Rule> rules=sceneEnvironmentProperty.getRules();
//		/////rules=>rulesMap
//		HashMap<String,Rule> rulesMap=new HashMap<>();
//		for(Rule rule:rules) {
//			rulesMap.put(rule.getRuleName(), rule);
//		}
//		/////获得ifd上各节点
//		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(AddressService.IFD_FILE_NAME, AddressService.IFD_FILE_PATH);
//		/////解耦各种性质分析。conflict、jitter、property，property每条分析时间
//		DynamicAnalysisService.getAllScenariosDynamicAnalysis(scenes, environmentModel.getDevices(), rulesMap, simulationTime, equivalentTime, intervalTime, graphNodes);
//		List<PropertyVerifyResult> propertyVerifyResults=DynamicAnalysisService.analizeAllproperties(properties, scenes, environmentModel.getDevices(), environmentModel.getBiddables(), graphNodes, rulesMap);
//		ScenePropertyResult scenePropertyResult=new ScenePropertyResult();
//		scenePropertyResult.setPropertyVerifyResults(propertyVerifyResults);
//		scenePropertyResult.setScenes(scenes);
//		System.out.println(System.currentTimeMillis()-t1);
//		return scenePropertyResult;
//	}
	/////所有场景动态分析，返回有哪些错误以及错误原因
	@RequestMapping(value="/getAllDynamicAnalysisResult",method = RequestMethod.POST)
	@ResponseBody
	public List<Scene> getAllDynamicAnalysisResult(@RequestBody SceneEnvironmentProperty sceneEnvironmentProperty,String simulationTime,String equivalentTime,String intervalTime){
		long t1=System.currentTimeMillis();
		List<Scene> scenes=sceneEnvironmentProperty.getScenes();
		EnvironmentModel environmentModel=sceneEnvironmentProperty.getEnvironmentModel();
		List<Rule> rules=sceneEnvironmentProperty.getRules();
		/////rules=>rulesMap
		HashMap<String,Rule> rulesMap=new HashMap<>();
		for(Rule rule:rules) {
			rulesMap.put(rule.getRuleName(), rule);
		}
		/////获得ifd上各节点
		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(AddressService.IFD_FILE_NAME, AddressService.IFD_FILE_PATH);
		/////解耦各种性质分析。conflict、jitter、property，property每条分析时间
		DynamicAnalysisService.getAllScenariosDynamicAnalysis(scenes, environmentModel.getDevices(), rulesMap, simulationTime, equivalentTime, intervalTime, graphNodes);

		System.out.println(System.currentTimeMillis()-t1);
		return scenes;
	}
	/////所有场景动态分析，返回有哪些错误以及错误原因
	@RequestMapping(value="/getPropertyVerificationResult",method = RequestMethod.POST)
	@ResponseBody
	public List<PropertyVerifyResult> getPropertyVerificationResult(@RequestBody SceneEnvironmentProperty sceneEnvironmentProperty){
		long t1=System.currentTimeMillis();
		List<Scene> scenes=sceneEnvironmentProperty.getScenes();
		EnvironmentModel environmentModel=sceneEnvironmentProperty.getEnvironmentModel();
		List<String> properties =sceneEnvironmentProperty.getProperties();
		List<Rule> rules=sceneEnvironmentProperty.getRules();
		/////rules=>rulesMap
		HashMap<String,Rule> rulesMap=new HashMap<>();
		for(Rule rule:rules) {
			rulesMap.put(rule.getRuleName(), rule);
		}
		/////获得ifd上各节点
		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(AddressService.IFD_FILE_NAME, AddressService.IFD_FILE_PATH);
		/////解耦各种性质分析。conflict、jitter、property，property每条分析时间
		List<PropertyVerifyResult> propertyVerifyResults=DynamicAnalysisService.analizeAllproperties(properties, scenes, environmentModel.getDevices(), environmentModel.getBiddables(), graphNodes, rulesMap);
		System.out.println(System.currentTimeMillis()-t1);
		
		return propertyVerifyResults;
	}
	
	////单个场景的分析
	@RequestMapping(value="/getSingleDynamicAnalysisResult",method = RequestMethod.POST)
	@ResponseBody
	public Scene getSingleDynamicAnalysisResult(@RequestBody SceneEnvironmentRule sceneEnvironmentRule,String simulationTime,String equivalentTime,String intervalTime ) {
		Scene scene=sceneEnvironmentRule.getScene();
		EnvironmentModel environmentModel=sceneEnvironmentRule.getEnvironmentModel();
		List<Rule> rules=sceneEnvironmentRule.getRules();
		/////rules=>rulesMap
		HashMap<String,Rule> rulesMap=new HashMap<>();
		for(Rule rule:rules) {
			rulesMap.put(rule.getRuleName(), rule);
		}
		/////获得ifd上各节点
		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(AddressService.IFD_FILE_NAME, AddressService.IFD_FILE_PATH);
		/////场景分析
		DynamicAnalysisService.getSingleScenarioDynamicAnalysis(scene, environmentModel.getDevices(), graphNodes, rulesMap,simulationTime,equivalentTime,intervalTime);
		return scene;
	}

	@RequestMapping("/uploadModelFile")
	@ResponseBody
	public ModelLayer uploadModelFile(@RequestParam("file") MultipartFile uploadedFile,@RequestParam("locations") String locationsStr) throws DocumentException, IOException {
		//////////////上传的环境本体文件，存储在D:\\workspace位置
		if (uploadedFile == null) {
			System.out.println("上传失败，无法找到文件！");
		}
		//上传xml文件
		String fileName = uploadedFile.getOriginalFilename();
		String filePath=AddressService.MODEL_FILE_PATH;
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath+fileName));

		outputStream.write(uploadedFile.getBytes());
		outputStream.flush();
		outputStream.close();
		//逻辑处理
		///解析模型文件，获得模型层
		ObjectMapper objectMapper=new ObjectMapper();
		List<String> locations=objectMapper.readValue(locationsStr,List.class);
		ModelLayer modelLayer= ModelLayerService.getModelLayer(filePath,fileName,fileName,locations);
		System.out.println(fileName + "上传成功");
		return modelLayer;
	}

	@RequestMapping("/uploadInstanceInfomationFile")
	@ResponseBody
	public InstanceLayer uploadInstanceInfomationFile(@RequestParam("file") MultipartFile uploadedFile,@RequestParam("modelLayer") String modelLayerStr) throws DocumentException, IOException {
		//////////////上传的环境本体文件，存储在D:\\workspace位置
		if (uploadedFile == null) {
			System.out.println("上传失败，无法找到文件！");
		}
		//上传xml文件
		String fileName = uploadedFile.getOriginalFilename();
		String filePath=AddressService.MODEL_FILE_PATH;
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath+fileName));

		outputStream.write(uploadedFile.getBytes());
		outputStream.flush();
		outputStream.close();
		//逻辑处理
		///解析模型文件，获得模型层
		ObjectMapper objectMapper=new ObjectMapper();
		ModelLayer modelLayer=objectMapper.readValue(modelLayerStr,ModelLayer.class);
		InstanceLayer instanceLayer= InstanceLayerService.getInstanceLayer(filePath,fileName,modelLayer);
		System.out.println(fileName + "上传成功");
		return instanceLayer;
	}
	
}
