package com.example.demo.controller;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
		StaticAnalysisResult1 staticAnalysisResult1 =StaticAnalysisService.getStaticAnalaysisResult(rules, AddressService.IFD_FILE_NAME,  AddressService.IFD_FILE_PATH, environmentModel);
		EnvironmentStatic environmentStatic=new EnvironmentStatic(environmentModel, staticAnalysisResult1);
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
		List<Attribute_> attributes=environmentRule.getEnvironmentModel().getAttributes();
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
		List<Attribute_> attributes=environmentRule.getEnvironmentModel().getAttributes();
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

	@RequestMapping("/uploadInstanceInformationFile")
	@ResponseBody
	public OutputConstruct.InstanceLayerOutput uploadInstanceInformationFile(@RequestParam("file") MultipartFile uploadedFile,@RequestParam("modelLayer") String modelLayerStr) throws DocumentException, IOException {
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
		OutputConstruct.InstanceLayerOutput instanceLayerOutput=new OutputConstruct.InstanceLayerOutput();
		instanceLayerOutput.setInstanceLayer(instanceLayer);
		instanceLayerOutput.setModelLayer(modelLayer);
		System.out.println(fileName + "上传成功");
		return instanceLayerOutput;
	}

	/**
	 * 解析规则，生成交互环境模型，并生成信息流图
	 * */
	@RequestMapping("/genererateInteractiveEnvironment")
	@ResponseBody
	public OutputConstruct.InteractiveLayerAndRules genererateInteractiveEnvironment(@RequestBody InputConstruct.ModelInstanceLayerAndRuleStrLists modelInstanceLayerAndRuleStrLists) throws IOException {

		ModelLayer modelLayer=modelInstanceLayerAndRuleStrLists.getModelLayer();
		InstanceLayer instanceLayer=modelInstanceLayerAndRuleStrLists.getInstanceLayer();
		List<String> ruleTextLines=modelInstanceLayerAndRuleStrLists.getRuleTestLines();
		List<Rule> rules=RuleService.getRuleList(ruleTextLines);
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		InstanceLayer interactiveEnvironment=SystemModelGenerationService.getInteractiveEnvironment(instanceLayer,modelLayer,triggerMap,actionMap);
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String ifdFileName="ifd.dot";
		StaticAnalysisService.generateIFD(triggerMap,actionMap,rules,interactiveEnvironment,interactiveInstanceMap,ifdFileName,AddressService.IFD_FILE_PATH);
		OutputConstruct.InteractiveLayerAndRules interactiveLayerAndRules=new OutputConstruct.InteractiveLayerAndRules();
		interactiveLayerAndRules.setInteractiveInstance(interactiveEnvironment);
		interactiveLayerAndRules.setRules(rules);
		interactiveLayerAndRules.setIfdFileName(ifdFileName);
		return interactiveLayerAndRules;
	}

	/**
	 * 静态分析
	 * */
	@RequestMapping("/getStaticAnalysis")
	@ResponseBody
	public StaticAnalysisResult getStaticAnalysis(@RequestBody InputConstruct.StaticAnalysisInput staticAnalysisInput) throws IOException {
		List<Rule> rules=staticAnalysisInput.getRules();
		InstanceLayer interactiveEnvironment= staticAnalysisInput.getInstanceLayer();
		StaticAnalysisResult staticAnalysisResult=StaticAnalysisService.getStaticAnalysisResult(rules,AddressService.IFD_FILE_PATH,"temp-ifd.dot",interactiveEnvironment);
		return staticAnalysisResult;
	}


	/**
	 * 生成单个场景对应的环境模型
	 * */
	@RequestMapping("/generateSingleScenario")
	@ResponseBody
	public List<String> generateSingleScenario(@RequestBody InputConstruct.SingleScenarioGenerateInput singleScenarioGenerateInput)  {
		////生成多个场景的模型文件，运行时环境模型，还需要返回场景树
		////先解析规则
		ModelLayer modelLayer=singleScenarioGenerateInput.getModelLayer();
		InstanceLayer instanceLayer=singleScenarioGenerateInput.getInstanceLayer();
		List<Rule> rules=singleScenarioGenerateInput.getRules();
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		InstanceLayer interactiveEnvironment= singleScenarioGenerateInput.getInteractiveInstance();
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String simulationTime=singleScenarioGenerateInput.getSimulationTime();
		String modelFileName=singleScenarioGenerateInput.getModelFileName();
		String tempModelFileName="temp-"+modelFileName;
		List<String[]> attributeValues=singleScenarioGenerateInput.getAttributeValues();
		String[] intoLocationTime=SystemModelGenerationService.getIntoLocationTime(simulationTime,instanceLayer.getHumanInstance());
		SystemModelGenerationService.generateCommonModelFile(simulationTime,intoLocationTime,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
		String singleModelFileName="single-scenario-"+modelFileName;
		SystemModelGenerationService.generateSingleScenario(AddressService.MODEL_FILE_PATH,tempModelFileName,AddressService.MODEL_FILE_PATH,singleModelFileName,modelLayer,rules,attributeValues);
		List<String> fileName=new ArrayList<>();
		fileName.add(singleModelFileName);
		return fileName;
	}

	@RequestMapping("/generateBestScenario")
	@ResponseBody
	public OutputConstruct.BestScenarioOutput generateBestScenario(@RequestBody InputConstruct.BestScenarioGenerateInput bestScenarioGenerateInput) {
		ModelLayer modelLayer=bestScenarioGenerateInput.getModelLayer();
		InstanceLayer instanceLayer=bestScenarioGenerateInput.getInstanceLayer();
		List<Rule> rules=bestScenarioGenerateInput.getRules();
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		InstanceLayer interactiveEnvironment= bestScenarioGenerateInput.getInteractiveInstance();
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String simulationTime=bestScenarioGenerateInput.getSimulationTime();
		String modelFileName=bestScenarioGenerateInput.getModelFileName();
		String tempModelFileName="temp-"+modelFileName;
		String ifdFileName= bestScenarioGenerateInput.getIfdFileName();
		List<String[]> attributeValues=SystemModelGenerationService.setAttributeValues(AddressService.IFD_FILE_PATH,ifdFileName);
		String[] intoLocationTime=SystemModelGenerationService.getIntoLocationTime(simulationTime,instanceLayer.getHumanInstance());
		SystemModelGenerationService.generateCommonModelFile(simulationTime,intoLocationTime,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
		String bestModelFileName="best-scenario-"+modelFileName;
		SystemModelGenerationService.generateSingleScenario(AddressService.MODEL_FILE_PATH,tempModelFileName,AddressService.MODEL_FILE_PATH,bestModelFileName,modelLayer,rules,attributeValues);
		OutputConstruct.BestScenarioOutput bestScenarioOutput=new OutputConstruct.BestScenarioOutput();
		bestScenarioOutput.setBestScenarioFileName(bestModelFileName);
		bestScenarioOutput.setAttributeValues(attributeValues);
		return bestScenarioOutput;
	}

	/**
	 * 仿真单个场景
	 * */
	@RequestMapping("/simulateSingleScenario")
	@ResponseBody
	public Scenario simulateSingleScenario(@RequestBody InstanceLayer instanceLayer,String modelFileName)  {
		String simulationResult=SimulationService.getSimulationResult(AddressService.UPPAAL_PATH,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.SYSTEM);
		String modelFilePrefix=modelFileName.substring(0,modelFileName.indexOf(".xml"));
		String resultFileName=modelFilePrefix+".txt";
		List<DataTimeValue> dataTimeValues=SimulationService.parseSimulationResult(simulationResult,instanceLayer,AddressService.SIMULATE_RESULT_FILE_PATH,resultFileName);
		Scenario scenario=new Scenario();
		scenario.setScenarioName(modelFilePrefix);
		scenario.setDataTimeValues(dataTimeValues);
		return scenario;
	}



	/**
	 * 生成单个场景对应的环境模型
	 * */
	@RequestMapping("/genereteMultipleScenarios")
	@ResponseBody
	public ScenarioTree.ScenesTree genereteMultipleScenarios(@RequestBody InputConstruct.MultiScenarioGenerateInput multiScenarioGenerateInput)  {
		////生成多个场景的模型文件，运行时环境模型，还需要返回场景树
		////先解析规则
		ModelLayer modelLayer=multiScenarioGenerateInput.getModelLayer();
		InstanceLayer instanceLayer=multiScenarioGenerateInput.getInstanceLayer();
		List<Rule> rules=multiScenarioGenerateInput.getRules();
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		InstanceLayer interactiveEnvironment= multiScenarioGenerateInput.getInteractiveInstance();
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String simulationTime=multiScenarioGenerateInput.getSimulationTime();
		String modelFileName=multiScenarioGenerateInput.getModelFileName();
		String tempModelFileName="temp-"+modelFileName;
		String[] intoLocationTime=SystemModelGenerationService.getIntoLocationTime(simulationTime,instanceLayer.getHumanInstance());
		SystemModelGenerationService.generateCommonModelFile(simulationTime,intoLocationTime,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
		ScenarioTree.ScenesTree scenesTree=SystemModelGenerationService.generateMultiScenariosAccordingToTriggers(modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,AddressService.MODEL_FILE_PATH,modelLayer,rules,triggerMap);

		return scenesTree;
	}

	/**
	 * 仿真所有场景
	 * */
	@RequestMapping("/simulateMultipleScenario")
	@ResponseBody
	public List<Scenario> simulateMultipleScenario(@RequestBody InputConstruct.MultiScenarioSimulateInput multiScenarioSimulateInput)  {
		ScenarioTree.ScenesTree scenesTree=multiScenarioSimulateInput.getScenesTree();
		String initModelFileName= multiScenarioSimulateInput.getModelFileName();
		InstanceLayer instanceLayer= multiScenarioSimulateInput.getInstanceLayer();
		List<Scenario> scenarios=SimulationService.getScenesTreeScenarioSimulationDataTimeValues(scenesTree,initModelFileName,instanceLayer,AddressService.UPPAAL_PATH,AddressService.MODEL_FILE_PATH,AddressService.SYSTEM,AddressService.SIMULATE_RESULT_FILE_PATH);

		return scenarios;
	}

	@RequestMapping("/calculateDeviceStatesDuration")
	@ResponseBody
	public List<String[]> calculateDeviceStatesDuration(@RequestBody InputConstruct.ConsumptionInput consumptionInput){
		DataTimeValue dataTimeValue=consumptionInput.getDataTimeValue();
		DeviceInstance deviceInstance= consumptionInput.getDeviceInstance();
		List<String[]> deviceStatesDuration=AnalysisService.getDeviceStatesDuration(dataTimeValue,deviceInstance);
		return deviceStatesDuration;
	}

	@RequestMapping("/searchAllScenariosConflict")
	@ResponseBody
	public List<Scenario> searchAllScenariosConflict(@RequestBody List<Scenario> scenarios) {
		for (Scenario scenario:scenarios){
			List<DeviceConflict> deviceConflicts=AnalysisService.getDevicesConflict(scenario.getDataTimeValues());
			scenario.setDeviceConflicts(deviceConflicts);
		}
		return scenarios;
	}

	@RequestMapping("/searchAllScenariosJitter")
	@ResponseBody
	public List<Scenario> searchAllScenariosJitter(@RequestBody List<Scenario> scenarios,String intervalTime,String simulationTime,String equivalentTime) {
		double interval=Double.parseDouble(intervalTime)/(Double.parseDouble(equivalentTime)*3600)*Double.parseDouble(simulationTime);
		for (Scenario scenario:scenarios){
			List<DeviceJitter> deviceJitters=AnalysisService.getDevicesJitter(scenario.getDataTimeValues(),interval);
			scenario.setDeviceJitters(deviceJitters);
		}
		return scenarios;
	}

	@RequestMapping("/locateAllScenariosConflict")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateAllScenariosConflict(@RequestBody InputConstruct.LocationInput locationInput) {
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceConflictAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);

		List<List<List<DeviceStateAndCausingRules>>> allSynthesizedDeviceAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			if (deviceAllStatesRuleAndPreRules.size()>0){
				AnalysisService.getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllStatesRuleAndPreRules);
				allSynthesizedDeviceAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
			}
		}

		return allSynthesizedDeviceAllStatesRuleAndPreRules;
	}

	@RequestMapping("/locateAllScenariosJitter")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateAllScenariosJitter(@RequestBody InputConstruct.LocationInput locationInput) {
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceJitterAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);

		List<List<List<DeviceStateAndCausingRules>>> allSynthesizedDeviceAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			if (deviceAllStatesRuleAndPreRules.size()>0){
				///综合原因
				AnalysisService.getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllStatesRuleAndPreRules);

				allSynthesizedDeviceAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
			}
		}

		return allSynthesizedDeviceAllStatesRuleAndPreRules;
	}

	@RequestMapping("/searchSingleScenarioConflict")
	@ResponseBody
	public Scenario searchSingleScenarioConflict(@RequestBody Scenario scenario) {
		List<DeviceConflict> deviceConflicts=AnalysisService.getDevicesConflict(scenario.getDataTimeValues());
		scenario.setDeviceConflicts(deviceConflicts);
		return scenario;
	}

	@RequestMapping("/searchSingleScenarioJitter")
	@ResponseBody
	public Scenario searchSingleScenarioJitter(@RequestBody Scenario scenario,String intervalTime,String simulationTime,String equivalentTime) {
		double interval=Double.parseDouble(intervalTime)/(Double.parseDouble(equivalentTime)*3600)*Double.parseDouble(simulationTime);
		List<DeviceJitter> deviceJitters=AnalysisService.getDevicesJitter(scenario.getDataTimeValues(),interval);
		scenario.setDeviceJitters(deviceJitters);
		return scenario;
	}

	@RequestMapping("/locateSingleScenariosAllConflict")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateSingleScenariosAllConflict(@RequestBody InputConstruct.LocationInput locationInput) {
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceConflictAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);
		List<List<List<DeviceStateAndCausingRules>>> devicesAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			devicesAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
		}
		return devicesAllStatesRuleAndPreRules;
	}



	@RequestMapping("/locateSingleScenariosAllJitter")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateSingleScenariosAllJitter(@RequestBody InputConstruct.LocationInput locationInput) {
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceJitterAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);
		List<List<List<DeviceStateAndCausingRules>>> devicesAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			devicesAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
		}
		return devicesAllStatesRuleAndPreRules;
	}

	@RequestMapping("/getCausingRulesSynthesized")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> getCausingRulesSynthesized(@RequestBody List<List<List<DeviceStateAndCausingRules>>> devicesAllStatesRuleAndPreRules) {

		for (List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules:devicesAllStatesRuleAndPreRules){
			AnalysisService.getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllStatesRuleAndPreRules);
		}
		return devicesAllStatesRuleAndPreRules;
	}

	@RequestMapping("/getAttributeSatisfaction")
	@ResponseBody
	public double AttributeSatisfaction(@RequestBody InputConstruct.SatisfactionInput satisfactionInput) {
		String attribute= satisfactionInput.getAttribute();
		double lowValue=-Double.MAX_VALUE;
		double highValue=Double.MAX_VALUE;
		if (!satisfactionInput.getLowValue().equals("")){
			lowValue=Double.parseDouble(satisfactionInput.getLowValue());
		}
		if (!satisfactionInput.getHighValue().equals("")){
			highValue=Double.parseDouble(satisfactionInput.getHighValue());
		}
		List<DataTimeValue> dataTimeValues=satisfactionInput.getDataTimeValues();
		double satisfaction=AnalysisService.getSatisfaction(attribute,lowValue,highValue,dataTimeValues);
		System.out.println(satisfaction);
		return satisfaction;
	}

	@RequestMapping("/getOtherAnalysis")
	@ResponseBody
	public OutputConstruct.OtherAnalysisOutput getOtherAnalysis(@RequestBody InputConstruct.OtherAnalysisInput otherAnalysisInput) {
		List<Scenario> scenarios=otherAnalysisInput.getScenarios();
		InstanceLayer instanceLayer= otherAnalysisInput.getInstanceLayer();
		List<List<String[]>> deviceCannotBeTurnedOffOrOnListOfDifferentScenarios=new ArrayList<>();
		List<List<String>> notTriggeredRulesOfDifferentScenarios=new ArrayList<>();
		for (Scenario scenario:scenarios){
			List<String[]> deviceCannotBeTurnedOffOrOnList=AnalysisService.getDeviceCannotBeTurnedOffOrOnListInAScenario(scenario.getDataTimeValues());
			deviceCannotBeTurnedOffOrOnListOfDifferentScenarios.add(deviceCannotBeTurnedOffOrOnList);
			List<String> notTriggeredRules=AnalysisService.getNotTriggeredRulesInAScenario(scenario.getDataTimeValues());
			notTriggeredRulesOfDifferentScenarios.add(notTriggeredRules);
		}
		///规则不完整
		List<String> deviceCannotBeTurnedOffList=AnalysisService.getDeviceCannotBeTurnedOffListInAll(deviceCannotBeTurnedOffOrOnListOfDifferentScenarios);
		///不可触发规则
		List<String> notTriggeredRulesInAll=AnalysisService.getNotTriggeredRulesInAll(notTriggeredRulesOfDifferentScenarios);
		///隐私性验证
		List<List<String>[]> homeBoundedOutBoundedResults=AnalysisService.privacyVerification(instanceLayer.getDeviceInstances(),instanceLayer.getHumanInstance(),scenarios,instanceLayer);

		OutputConstruct.OtherAnalysisOutput otherAnalysisOutput=new OutputConstruct.OtherAnalysisOutput();
		otherAnalysisOutput.setDeviceCannotBeTurnedOffList(deviceCannotBeTurnedOffList);
		otherAnalysisOutput.setNotTriggeredRulesInAll(notTriggeredRulesInAll);
		otherAnalysisOutput.setHomeBoundedOutBoundedResults(homeBoundedOutBoundedResults);
		return otherAnalysisOutput;
	}
	@RequestMapping("/getPropertiesAnalysis")
	@ResponseBody
	public List<PropertyAnalysisResult> getPropertiesAnalysis(@RequestBody InputConstruct.PropertyAnalysisInput propertyAnalysisInput) {
		List<Scenario> scenarios=propertyAnalysisInput.getScenarios();
		List<Rule> rules=propertyAnalysisInput.getRules();
		List<String> properties=propertyAnalysisInput.getProperties();
		InstanceLayer instanceLayer= propertyAnalysisInput.getInstanceLayer();
		List<PropertyAnalysisResult> propertyAnalysisResults=AnalysisService.getPropertiesAnalysisResultAllScenarios(scenarios,instanceLayer,rules,properties);
		return propertyAnalysisResults;

	}

}
