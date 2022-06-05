package com.example.demo.controller;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


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

import javax.servlet.http.HttpServletResponse;
//import com.example.demo.bean.OutputConstruct.EnvironmentStatic;
//import com.example.demo.bean.ScenarioTree.ScenesTree;
//import com.example.demo.bean.InputConstruct.EnvironmentRule;
//import com.example.demo.bean.InputConstruct.SceneEnvironmentProperty;
//import com.example.demo.bean.InputConstruct.SceneEnvironmentRule;
//import com.example.demo.bean.InputConstruct.SceneTreeDevice;


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



	/**
	 * 上传模型层文件（.xml），并解析各类实体模型
	 * */

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
		List<String> locations=objectMapper.readValue(locationsStr,List.class);    ////人的位置信息
		/////////////模型层解析
		long t1=System.currentTimeMillis();
		ModelLayer modelLayer= ModelLayerService.getModelLayer(filePath,fileName,fileName,locations);
		long t2=System.currentTimeMillis();
		System.out.println("模型层解析："+(t2-t1));
		System.out.println(fileName + "上传成功");
		return modelLayer;
	}
/**
 * 上传实例层文件，并解析获得各实例
 * */
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
		//////////实例层解析
		long t1=System.currentTimeMillis();
		InstanceLayer instanceLayer= InstanceLayerService.getInstanceLayer(filePath,fileName,modelLayer);
		long t2=System.currentTimeMillis();
		System.out.println("实例层生成："+(t2-t1));
		///-------------------------输出-----------------------------------
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
		//----------------输入-----------------------------------
		ModelLayer modelLayer=modelInstanceLayerAndRuleStrLists.getModelLayer();
		InstanceLayer instanceLayer=modelInstanceLayerAndRuleStrLists.getInstanceLayer();
		List<String> ruleTextLines=modelInstanceLayerAndRuleStrLists.getRuleTestLines();
		//---------------------------------------------------------------

		//-----------------解析TAP规则---------------------------
		List<Rule> rules=RuleService.getRuleList(ruleTextLines);
		//--------------------------------------------------------------
		//------------------解析trigger和action的信息-----------------------
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		//--------------------------------------------------------------------
		/////获得交互环境模型
		long t1=System.currentTimeMillis();
		InstanceLayer interactiveEnvironment=SystemModelGenerationService.getInteractiveEnvironment(instanceLayer,modelLayer,triggerMap,actionMap);
		long t2=System.currentTimeMillis();
		System.out.println("交互环境模型生成："+(t2-t1));
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String ifdFileName="ifd.dot";
		long t3=System.currentTimeMillis();
		/////生成信息流图
		StaticAnalysisService.generateIFD(triggerMap,actionMap,rules,interactiveEnvironment,interactiveInstanceMap,ifdFileName,AddressService.IFD_FILE_PATH);
		long t4=System.currentTimeMillis();
		System.out.println("信息流图生成："+(t4-t3));
		///-------------------------输出-----------------------------------
		OutputConstruct.InteractiveLayerAndRules interactiveLayerAndRules=new OutputConstruct.InteractiveLayerAndRules();
		interactiveLayerAndRules.setInteractiveInstance(interactiveEnvironment);
		interactiveLayerAndRules.setRules(rules);
		interactiveLayerAndRules.setIfdFileName(ifdFileName);
		return interactiveLayerAndRules;
	}

	/**
	 * 获得信息流图png
	 * */
	@RequestMapping(value="/getIFDPng",method=RequestMethod.GET)
	@ResponseBody
	public void getIFDPng(String pngFilePath, HttpServletResponse response){
//		String ifdFileName=fileNames.get(0);
//		String pngFileName=ifdFileName.substring(0,ifdFileName.indexOf(".dot"))+".png";
//		StaticAnalysisService.generateIFDPng(AddressService.IFD_FILE_PATH,ifdFileName,AddressService.ASSETS_FILE_PATH,pngFileName);
//		List<String> pngResults=new ArrayList<>();
//		pngResults.add(pngFileName);
		StaticAnalysisService.accessPng(pngFilePath,response);

	}

	/**
	 * 静态分析
	 * */
	@RequestMapping("/getStaticAnalysis")
	@ResponseBody
	public StaticAnalysisResult getStaticAnalysis(@RequestBody InputConstruct.StaticAnalysisInput staticAnalysisInput) throws IOException {
		//----------------输入-----------------------------------
		List<Rule> rules=staticAnalysisInput.getRules();
		InstanceLayer interactiveEnvironment= staticAnalysisInput.getInstanceLayer();
		//---------------------------------------------------------------
		///////静态分析
		StaticAnalysisResult staticAnalysisResult=StaticAnalysisService.getStaticAnalysisResult(rules,AddressService.IFD_FILE_PATH,"temp-ifd.dot",interactiveEnvironment);
		return staticAnalysisResult;
	}


	/**
	 * 生成单仿真场景，用户自己设置各属性取值
	 * */
	@RequestMapping("/generateSingleScenario")
	@ResponseBody
	public List<String> generateSingleScenario(@RequestBody InputConstruct.SingleScenarioGenerateInput singleScenarioGenerateInput)  {
		////生成多个场景的模型文件，运行时环境模型，还需要返回场景树
		////先解析规则
		//----------------输入-----------------------------------
		ModelLayer modelLayer=singleScenarioGenerateInput.getModelLayer();
		InstanceLayer instanceLayer=singleScenarioGenerateInput.getInstanceLayer();
		List<Rule> rules=singleScenarioGenerateInput.getRules();
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		InstanceLayer interactiveEnvironment= singleScenarioGenerateInput.getInteractiveInstance();
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String simulationTime=singleScenarioGenerateInput.getSimulationTime();    ///仿真时长
		String modelFileName=singleScenarioGenerateInput.getModelFileName();
		String tempModelFileName="temp-"+modelFileName;
		List<String[]> attributeValues=singleScenarioGenerateInput.getAttributeValues();
		//---------------------------------------------------------------

		long t1=System.currentTimeMillis();
		///设置人的行为模型，进入各空间的时间点
		String[] intoLocationTime=SystemModelGenerationService.getIntoLocationTime(simulationTime,instanceLayer.getHumanInstance());
		SystemModelGenerationService.generateCommonModelFile(simulationTime,intoLocationTime,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
		long t2=System.currentTimeMillis();
		System.out.println("通用系统模型生成："+(t2-t1));
		String singleModelFileName="single-scenario-"+modelFileName;
		long t3=System.currentTimeMillis();
		///生成一个仿真场景
		SystemModelGenerationService.generateSingleScenario(AddressService.MODEL_FILE_PATH,tempModelFileName,AddressService.MODEL_FILE_PATH,singleModelFileName,modelLayer,rules,attributeValues);
		long t4=System.currentTimeMillis();
		System.out.println("单场景生成："+(t4-t3));
		///-------------------------输出单仿真场景的文件名-----------------------------------
		List<String> fileName=new ArrayList<>();
		fileName.add(singleModelFileName);
		return fileName;
	}

	/**
	 * 生成能触发规则数最多的仿真场景
	 * */
	@RequestMapping("/generateBestScenario")
	@ResponseBody
	public OutputConstruct.BestScenarioOutput generateBestScenario(@RequestBody InputConstruct.BestScenarioGenerateInput bestScenarioGenerateInput) {
		//----------------输入-----------------------------------
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
		//---------------------------------------------------------------


		////计算能触发最多规则数的场景的各属性取值
		List<String[]> attributeValues=SystemModelGenerationService.setAttributeValues(AddressService.IFD_FILE_PATH,ifdFileName);
		long t1=System.currentTimeMillis();
		///设置人的行为模型，进入各空间的时间点
		String[] intoLocationTime=SystemModelGenerationService.getIntoLocationTime(simulationTime,instanceLayer.getHumanInstance());
		SystemModelGenerationService.generateCommonModelFile(simulationTime,intoLocationTime,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
		long t2=System.currentTimeMillis();
		System.out.println("通用系统模型生成："+(t2-t1));
		String bestModelFileName="best-scenario-"+modelFileName;
		long t3=System.currentTimeMillis();
		SystemModelGenerationService.generateSingleScenario(AddressService.MODEL_FILE_PATH,tempModelFileName,AddressService.MODEL_FILE_PATH,bestModelFileName,modelLayer,rules,attributeValues);
		long t4=System.currentTimeMillis();
		System.out.println("单场景生成："+(t4-t3));
		///-------------------------输出单仿真场景的文件名-----------------------------------
		OutputConstruct.BestScenarioOutput bestScenarioOutput=new OutputConstruct.BestScenarioOutput();
		bestScenarioOutput.setBestScenarioFileName(bestModelFileName);
		bestScenarioOutput.setAttributeValues(attributeValues);
		return bestScenarioOutput;
	}

	/**
	 * 仿真单个场景，并获得仿真轨迹
	 * */
	@RequestMapping("/simulateSingleScenario")
	@ResponseBody
	public Scenario simulateSingleScenario(@RequestBody InstanceLayer instanceLayer,String modelFileName)  {
		long t1=System.currentTimeMillis();
		String simulationResult=SimulationService.getSimulationResult(AddressService.UPPAAL_PATH,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.SYSTEM);
		String modelFilePrefix=modelFileName.substring(0,modelFileName.indexOf(".xml"));
		String resultFileName=modelFilePrefix+".txt";
		///////仿真单个场景并获得轨迹
		List<DataTimeValue> dataTimeValues=SimulationService.parseSimulationResult(simulationResult,instanceLayer,AddressService.SIMULATE_RESULT_FILE_PATH,resultFileName);
		long t2=System.currentTimeMillis();
		System.out.println("单场景仿真："+(t2-t1));
		///-------------------------输出一个场景的信息----------------------------------
		Scenario scenario=new Scenario();
		scenario.setScenarioName(modelFilePrefix);
		scenario.setDataTimeValues(dataTimeValues);
		return scenario;
	}



	/**
	 * 生成多仿真场景
	 * */
	@RequestMapping("/genereteMultipleScenarios")
	@ResponseBody
	public ScenarioTree.ScenesTree genereteMultipleScenarios(@RequestBody InputConstruct.MultiScenarioGenerateInput multiScenarioGenerateInput)  {
		////生成多个场景的模型文件，运行时环境模型，还需要返回场景树
		////先解析规则
		//----------------输入-----------------------------------
		ModelLayer modelLayer=multiScenarioGenerateInput.getModelLayer();
		InstanceLayer instanceLayer=multiScenarioGenerateInput.getInstanceLayer();
		List<Rule> rules=multiScenarioGenerateInput.getRules();
		HashMap<String,Trigger> triggerMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionMap=SystemModelGenerationService.getActionMapFromRules(rules);
		InstanceLayer interactiveEnvironment= multiScenarioGenerateInput.getInteractiveInstance();
		HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
		String simulationTime=multiScenarioGenerateInput.getSimulationTime();
		String modelFileName=multiScenarioGenerateInput.getModelFileName();
		//---------------------------------------------------------------

		String tempModelFileName="temp-"+modelFileName;
		long t1=System.currentTimeMillis();
		String[] intoLocationTime=SystemModelGenerationService.getIntoLocationTime(simulationTime,instanceLayer.getHumanInstance());
		SystemModelGenerationService.generateCommonModelFile(simulationTime,intoLocationTime,AddressService.MODEL_FILE_PATH,modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
		long t2=System.currentTimeMillis();
		System.out.println("通用系统模型生成时间："+(t2-t1));
		long t3=System.currentTimeMillis();
		////生成多个仿真场景，每个场景属性取值不同，最终获得场景树
		ScenarioTree.ScenesTree scenesTree=SystemModelGenerationService.generateMultiScenariosAccordingToTriggers(modelFileName,AddressService.MODEL_FILE_PATH,tempModelFileName,AddressService.MODEL_FILE_PATH,modelLayer,rules,triggerMap);
		long t4=System.currentTimeMillis();
		System.out.println("多场景生成时间："+(t4-t3));

		return scenesTree;
	}

	/**
	 * 仿真所有场景
	 * */
	@RequestMapping("/simulateMultipleScenario")
	@ResponseBody
	public List<Scenario> simulateMultipleScenario(@RequestBody InputConstruct.MultiScenarioSimulateInput multiScenarioSimulateInput)  {
		//----------------输入-----------------------------------
		ScenarioTree.ScenesTree scenesTree=multiScenarioSimulateInput.getScenesTree();
		String initModelFileName= multiScenarioSimulateInput.getModelFileName();
		InstanceLayer instanceLayer= multiScenarioSimulateInput.getInstanceLayer();
		//---------------------------------------------------------------

		long t1=System.currentTimeMillis();
		///如果resultFilePath为空则不生成仿真轨迹文件   AddressService.SIMULATE_RESULT_FILE_PATH
		///////对所有仿真场景进行仿真，并获得各场景的仿真轨迹
		List<Scenario> scenarios=SimulationService.getScenesTreeScenarioSimulationDataTimeValues(scenesTree,initModelFileName,instanceLayer,AddressService.UPPAAL_PATH,AddressService.MODEL_FILE_PATH,AddressService.SYSTEM,"");
		long t2=System.currentTimeMillis();
		System.out.println("多场景仿真时间："+(t2-t1));
		return scenarios;
	}


	/**
	 * 冲突验证
	 * */

	@RequestMapping("/searchAllScenariosConflict")
	@ResponseBody
	public List<Scenario> searchAllScenariosConflict(@RequestBody List<Scenario> scenarios) {
		long t1=System.currentTimeMillis();
		ExecutorService executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(scenarios.size()));
		for (Scenario scenario:scenarios){
			Runnable runnable= () -> {
				////分析仿真轨迹记录各设备每次冲突的信息
				List<DeviceConflict> deviceConflicts=AnalysisService.getDevicesConflict(scenario.getDataTimeValues());
				scenario.setDeviceConflicts(deviceConflicts);
			};
			executorService.submit(runnable);
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long t2=System.currentTimeMillis();
		System.out.println("冲突验证时间："+(t2-t1));
		return scenarios;
	}

	/**
	 * 抖动验证
	 * */
	@RequestMapping("/searchAllScenariosJitter")
	@ResponseBody
	public List<Scenario> searchAllScenariosJitter(@RequestBody List<Scenario> scenarios,String intervalTime,String simulationTime,String equivalentTime) {
		/////抖动的时间间隔
		double interval=Double.parseDouble(intervalTime)/(Double.parseDouble(equivalentTime)*3600)*Double.parseDouble(simulationTime);
		long t1=System.currentTimeMillis();
		ExecutorService executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(scenarios.size()));
		for (Scenario scenario:scenarios){
			Runnable runnable=()->{
				////分析仿真场景，记录各设备的每次抖动信息
				List<DeviceJitter> deviceJitters=AnalysisService.getDevicesJitter(scenario.getDataTimeValues(),interval);
				scenario.setDeviceJitters(deviceJitters);
			};
			executorService.submit(runnable);
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long t2=System.currentTimeMillis();
		System.out.println("抖动验证时间："+(t2-t1));
		return scenarios;
	}

	/**
	 * 冲突定位，获得导致冲突的TAP规则
	 * */
	@RequestMapping("/locateAllScenariosConflict")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateAllScenariosConflict(@RequestBody InputConstruct.LocationInput locationInput) {
		//-------------------输入----------------------------
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		//----------------------------------------------------
		long t1=System.currentTimeMillis();
		///获得每次冲突的直接原因TAP规则和前驱TAP
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceConflictAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);

		///综合冲突的直接原因TAP规则和前驱TAP
		List<List<List<DeviceStateAndCausingRules>>> allSynthesizedDeviceAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			if (deviceAllStatesRuleAndPreRules.size()>0){
				AnalysisService.getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllStatesRuleAndPreRules);
				allSynthesizedDeviceAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
			}
		}
		long t2=System.currentTimeMillis();
		System.out.println("冲突定位时间："+(t2-t1));
		return allSynthesizedDeviceAllStatesRuleAndPreRules;
	}

	/**
	 * 抖动定位，获得导致抖动的TAP规则
	 * */
	@RequestMapping("/locateAllScenariosJitter")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateAllScenariosJitter(@RequestBody InputConstruct.LocationInput locationInput) {
		//-------------------输入----------------------------
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		//----------------------------------------------------
		long t1=System.currentTimeMillis();
		///获得每次抖动的直接原因TAP规则和前驱TAP
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceJitterAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);

		///综合抖动的直接原因TAP规则和前驱TAP
		List<List<List<DeviceStateAndCausingRules>>> allSynthesizedDeviceAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			if (deviceAllStatesRuleAndPreRules.size()>0){
				///综合原因
				AnalysisService.getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllStatesRuleAndPreRules);

				allSynthesizedDeviceAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
			}
		}
		long t2=System.currentTimeMillis();
		System.out.println("抖动定位时间："+(t2-t1));
		return allSynthesizedDeviceAllStatesRuleAndPreRules;
	}

	/**
	 * 单场景的每次冲突
	 * */
	@RequestMapping("/searchSingleScenarioConflict")
	@ResponseBody
	public Scenario searchSingleScenarioConflict(@RequestBody Scenario scenario) {
		List<DeviceConflict> deviceConflicts=AnalysisService.getDevicesConflict(scenario.getDataTimeValues());
		scenario.setDeviceConflicts(deviceConflicts);
		return scenario;
	}

	/**
	 * 单场景的每次抖动
	 * */
	@RequestMapping("/searchSingleScenarioJitter")
	@ResponseBody
	public Scenario searchSingleScenarioJitter(@RequestBody Scenario scenario,String intervalTime,String simulationTime,String equivalentTime) {
		double interval=Double.parseDouble(intervalTime)/(Double.parseDouble(equivalentTime)*3600)*Double.parseDouble(simulationTime);
		List<DeviceJitter> deviceJitters=AnalysisService.getDevicesJitter(scenario.getDataTimeValues(),interval);
		scenario.setDeviceJitters(deviceJitters);
		return scenario;
	}

	/**
	 * 单场景的冲突定位
	 * */
	@RequestMapping("/locateSingleScenariosAllConflict")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateSingleScenariosAllConflict(@RequestBody InputConstruct.LocationInput locationInput) {
		//-------------------输入----------------------------
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		//-------------------------------------------------
		///获得每次冲突的直接原因TAP规则和前驱TAP
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceConflictAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);
		///综合冲突的直接原因TAP规则和前驱TAP
		List<List<List<DeviceStateAndCausingRules>>> devicesAllStatesRuleAndPreRules=new ArrayList<>();
		for (Map.Entry<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesEntry:deviceAllStatesRuleAndPreRulesHashMap.entrySet()){
			List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesEntry.getValue();
			devicesAllStatesRuleAndPreRules.add(deviceAllStatesRuleAndPreRules);
		}
		return devicesAllStatesRuleAndPreRules;
	}



	/**
	 * 单场景的抖动定位
	 * */
	@RequestMapping("/locateSingleScenariosAllJitter")
	@ResponseBody
	public List<List<List<DeviceStateAndCausingRules>>> locateSingleScenariosAllJitter(@RequestBody InputConstruct.LocationInput locationInput) {
		//-------------------输入----------------------------
		List<Scenario> scenarios=locationInput.getScenarios();
		List<Rule> rules=locationInput.getRules();
		List<DeviceInstance> deviceInstances=locationInput.getDeviceInstances();
		String ifdFileName=locationInput.getIfdFileName();
		//-------------------------------------------------
		///获得每次抖动的直接原因TAP规则和前驱TAP
		HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=AnalysisService.getDeviceJitterAllStatesRuleAndPreRulesHashMap(rules,ifdFileName,scenarios,deviceInstances);
		///综合抖动的直接原因TAP规则和前驱TAP
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

	/**
	 * 计算某个属性的舒适度
	 * */
	@RequestMapping("/getAttributeSatisfaction")
	@ResponseBody
	public double AttributeSatisfaction(@RequestBody InputConstruct.SatisfactionInput satisfactionInput) {
		String attribute= satisfactionInput.getAttribute();
		double lowValue=-Double.MAX_VALUE;   ///最低值
		double highValue=Double.MAX_VALUE;   ///最高值
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
	/**
	 * 计算设备状态持续时间
	 * */
	@RequestMapping("/calculateDeviceStatesDuration")
	@ResponseBody
	public List<String[]> calculateDeviceStatesDuration(@RequestBody InputConstruct.ConsumptionInput consumptionInput){
		DataTimeValue dataTimeValue=consumptionInput.getDataTimeValue();
		DeviceInstance deviceInstance= consumptionInput.getDeviceInstance();
		List<String[]> deviceStatesDuration=AnalysisService.getDeviceStatesDuration(dataTimeValue,deviceInstance);
		return deviceStatesDuration;
	}


	/**
	 * 计算能耗
	 * */
	@RequestMapping("/getEnergyConsumption")
	@ResponseBody
	public List<String[]> getEnergyConsumption(@RequestBody InputConstruct.EnergyConsumptionInput energyConsumptionInput){
		//-------------------输入----------------------------
		List<DataTimeValue> dataTimeValues=energyConsumptionInput.getDataTimeValues();
		List<DeviceInstance> deviceInstances=energyConsumptionInput.getDeviceInstances();
		//--------------------------------------------------
		List<String[]> deviceConsumptions=new ArrayList<>();
		HashMap<String,DataTimeValue> dataTimeValueHashMap=new HashMap<>();
		for (DataTimeValue dataTimeValue:dataTimeValues){
			dataTimeValueHashMap.put(dataTimeValue.getInstanceName(),dataTimeValue);
		}
		for (DeviceInstance deviceInstance:deviceInstances){
			double consumption=0;
			String[] deviceConsumption=new String[2];
			deviceConsumption[0]=deviceInstance.getInstanceName();
			DataTimeValue dataTimeValue=dataTimeValueHashMap.get(deviceInstance.getInstanceName());
			///计算各设备各状态的时间及能耗  deviceStateDuration[0]设备名  deviceStateDuration[1]状态名  deviceStateDuration[2]该状态保持时间 [3]用来存功率  [4]存能耗（仿真时间下的）
			List<String[]> deviceStatesDuration=AnalysisService.getDeviceStatesDuration(dataTimeValue,deviceInstance);
			////计算能耗综合
			for (String[] deviceStateDuration:deviceStatesDuration){
				consumption+=Double.parseDouble(deviceStateDuration[4]);
			}
			if(consumption<=0){
				continue;
			}
			deviceConsumption[1]=consumption+"";
			deviceConsumptions.add(deviceConsumption);
		}
		return deviceConsumptions;
	}


	/**
	 * 分析隐私性、还能分析不完整性、不可触发规则
	 *
	 * */
	@RequestMapping("/getOtherAnalysis")
	@ResponseBody
	public OutputConstruct.OtherAnalysisOutput getOtherAnalysis(@RequestBody InputConstruct.OtherAnalysisInput otherAnalysisInput) {
		//-------------------输入----------------------------
		List<Scenario> scenarios=otherAnalysisInput.getScenarios();
		InstanceLayer instanceLayer= otherAnalysisInput.getInstanceLayer();
		//--------------------------------------------------------
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
		long t1=System.currentTimeMillis();
		List<List<String>[]> homeBoundedOutBoundedResults=AnalysisService.privacyVerification(instanceLayer.getDeviceInstances(),instanceLayer.getHumanInstance(),scenarios,instanceLayer);
		long t2=System.currentTimeMillis();
		System.out.println("隐私性验证时间："+(t2-t1));
		//-------------------输出----------------------------
		OutputConstruct.OtherAnalysisOutput otherAnalysisOutput=new OutputConstruct.OtherAnalysisOutput();
		otherAnalysisOutput.setDeviceCannotBeTurnedOffList(deviceCannotBeTurnedOffList);
		otherAnalysisOutput.setNotTriggeredRulesInAll(notTriggeredRulesInAll);
		otherAnalysisOutput.setHomeBoundedOutBoundedResults(homeBoundedOutBoundedResults);
		return otherAnalysisOutput;
	}

	/**
	 * 安全性验证
	 * */
	@RequestMapping("/getPropertiesAnalysis")
	@ResponseBody
	public List<PropertyAnalysisResult> getPropertiesAnalysis(@RequestBody InputConstruct.PropertyAnalysisInput propertyAnalysisInput) {
		//-------------------输入----------------------------
		List<Scenario> scenarios=propertyAnalysisInput.getScenarios();
		List<Rule> rules=propertyAnalysisInput.getRules();
		List<String> properties=propertyAnalysisInput.getProperties();
		InstanceLayer instanceLayer= propertyAnalysisInput.getInstanceLayer();
		//----------------------------------------------------
//		properties=InstanceLayerService.generateSafetyProperties(instanceLayer);   ///做实验用
		////////安全性验证，验证每一条性质
		long t1=System.currentTimeMillis();
		List<PropertyAnalysisResult> propertyAnalysisResults=AnalysisService.getPropertiesAnalysisResultAllScenarios(scenarios,instanceLayer,rules,properties);
		long t2=System.currentTimeMillis();
		System.out.println("安全性验证时间："+(t2-t1));
		return propertyAnalysisResults;

	}

}
