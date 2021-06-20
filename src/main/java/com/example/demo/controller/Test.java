package com.example.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.dom4j.DocumentException;

import com.example.demo.bean.BiddableType;
import com.example.demo.bean.Conflict;
import com.example.demo.bean.ConflictReason;
import com.example.demo.bean.DeviceDetail;
import com.example.demo.bean.DeviceType;
import com.example.demo.bean.EnvironmentModel;
import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.JitterReason;
import com.example.demo.bean.PropertyVerifyResult;
import com.example.demo.bean.Rule;
import com.example.demo.bean.RuleNode;
import com.example.demo.bean.ScenarioTree.ScenesTree;
import com.example.demo.bean.Scene;
import com.example.demo.bean.SensorType;
import com.example.demo.bean.StaticAnalysisResult;
import com.example.demo.service.DynamicAnalysisService;
import com.example.demo.service.GetTemplate;
import com.example.demo.service.RuleService;
import com.example.demo.service.StaticAnalysisService;
import com.example.demo.service.SystemModelService;
import com.example.demo.service.TemplGraphService;

public class Test {

	public static void main(String[] args) throws DocumentException, IOException {
		// TODO Auto-generated method stub
//		Scanner s=new Scanner(System.in);
//		String[] locations= {"Lobby","Kitchen","Bathroom","Bedroom"};
//		List<DeviceDetail> devices=new ArrayList<DeviceDetail>();
//		for(int i=0;i<20;i++) {			
//			String location=locations[new Random().nextInt(4)];
//			System.out.println("deviceTypeName");
//			String deviceTypeName=s.next();
//			DeviceDetail device=new DeviceDetail("", location, deviceTypeName);
//			devices.add(device);
//		}
//		System.out.println(devices);
		String modelFileName="anewtry.xml";
		String filePath="D:\\example";
		String modelFileName2="anewtry2.xml";
		String ifdFileName="ifd.dot";
		String propertyFileName="properties.properties";
		
		EnvironmentModel environmentModel=TemplGraphService.getEnvironmentModel(modelFileName,modelFileName2, filePath, propertyFileName);
		
//		GetTemplate.deleteFileLine(filePath+"\\"+modelFileName, filePath+"\\"+modelFileName2, 2);
//		List<TemplGraph> templGraphs=TemplGraphService.getTemplGraphs(modelFileName2, filePath);
//		List<TemplGraph> controlledDevices=new ArrayList<TemplGraph>();
//		List<TemplGraph> sensors=new ArrayList<TemplGraph>();
//		List<TemplGraph> biddables=new ArrayList<TemplGraph>();
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getDeclaration().indexOf("controlled_device")>=0) {
//				controlledDevices.add(templGraph);
//			}else if(templGraph.getDeclaration().indexOf("sensor")>=0) {
//				sensors.add(templGraph);
//			}else {
//				if(templGraph.getName().equals("Person")) {
//					TemplGraph person=templGraph=TemplGraphService.getPersonTeml(Arrays.asList(locations));
//					if(person!=null) {
//						templGraph=person;
//					}
//					SystemModelService.generatePersonModel(templGraph, filePath+"\\"+modelFileName2);
//				}
//				biddables.add(templGraph);
//			}			
//		}
//		List<DeviceType> deviceTypes=TemplGraphService.getDeviceTypes(controlledDevices);
//		List<SensorType> sensorTypes=TemplGraphService.getSensorTypes(sensors);
//		List<BiddableType> biddableTypes=TemplGraphService.getBiddableTypes(biddables, sensorTypes);
//		TemplGraphService.setDeviceType(devices, deviceTypes);
//		TemplGraphService.setDeviceConstructionNum(devices, deviceTypes);
//		
//		EnvironmentModel environmentModel=new EnvironmentModel();
//		environmentModel.setBiddables(biddableTypes);
//		environmentModel.setDevices(devices);
//		environmentModel.setSensors(sensorTypes);
		
		String ruleText="1. IF SmartHomeSecurity_0.homeMode AND temperature<=15 THEN Heater_0.turn_heat_on\r\n" + 
				"\r\n" + 
				"2. IF SmartHomeSecurity_0.homeMode AND temperature>=30 THEN AirConditioner_0.turn_ac_cool\r\n" + 
				"\r\n" + 
				"3. IF SmartHomeSecurity_0.homeMode AND humidity<20 THEN Humidifier_0.turn_hum_on\r\n" + 
				"\r\n" + 
				"4. IF SmartHomeSecurity_0.homeMode AND humidity>=45 THEN Humidifier_0.turn_hum_off\r\n" + 
				"\r\n" + 
				"5. IF SmartHomeSecurity_0.homeMode AND humidity>65 THEN Fan_0.turn_fan_on\r\n" + 
				"\r\n" + 
				"6. IF SmartHomeSecurity_0.homeMode AND temperature>28 THEN Fan_0.turn_fan_on\r\n" + 
				"\r\n" + 
				"7. IF SmartHomeSecurity_0.homeMode AND temperature<20 THEN Fan_0.turn_fan_off\r\n" + 
				"\r\n" + 
				"15. IF SmartHomeSecurity_0.awayMode THEN Fan_0.turn_fan_on\r\n" + 
				"\r\n" + 
				"17. IF Window_0.wopen THEN Heater_0.turn_heat_off\r\n" + 
				"\r\n" + 
				"IF Window_0.wopen AND temperature>28 THEN AirConditioner_0.turn_ac_cool, Bulb_0.turn_bulb_off\r\n"+
				"\r\n"+
				"18. IF SmartHomeSecurity_0.awayMode THEN Heater_0.turn_heat_off,AirConditioner_0.turn_ac_off,Fan_0.turn_fan_off,Blind_0.close_blind,Bulb_0.turn_bulb_off\r\n" + 
				"\r\n" + 
				"19. IF SmartHomeSecurity_0.homeMode AND temperature<18 THEN AirConditioner_0.turn_ac_heat\r\n" + 
				"\r\n" + 
				"20. IF SmartHomeSecurity_0.homeMode AND temperature>30 THEN AirConditioner_0.turn_ac_cool\r\n" + 
				"\r\n" + 
				"21. IF SmartHomeSecurity_0.homeMode THEN Robot_0.dock_robot\r\n" + 
				"\r\n" + 
				"22. IF SmartHomeSecurity_0.awayMode THEN Robot_0.start_robot\r\n" + 
				"\r\n" + 
				"23. IF SmartHomeSecurity_0.awayMode THEN Window_0.close_window\r\n" + 
				"\r\n" + 
				"24. IF Person.Lobby THEN SmartHomeSecurity_0.turn_sms_home\r\n" + 
				"\r\n" + 
				"25. IF Person.Out THEN SmartHomeSecurity_0.turn_sms_away\r\n" + 
				"\r\n" + 
				"26. IF SmartHomeSecurity_0.homeMode AND temperature>28 THEN Blind_0.open_blind\r\n" + 
				"\r\n" + 
				"27. IF SmartHomeSecurity_0.homeMode THEN Bulb_0.turn_bulb_on\r\n" + 
				"\r\n" + 
				"28. IF SmartHomeSecurity_0.homeMode AND co2ppm>=800 THEN Fan_0.turn_fan_on,Window_0.open_window\r\n" + 
//				"\r\n" + 
//				"29. IF AirConditioner_0.cool THEN Window_0.close_window\r\n" + 
				"\r\n" + 
				"30. IF AirConditioner_0.heat THEN Window_0.close_window\r\n"+
				"\r\n" +
				"19. IF SmartHomeSecurity_0.homeMode AND temperature<18 THEN AirConditioner_0.turn_ac_heat\r\n"+ 
				"\r\n" + 
				"19. IF temperature>20 AND temperature<18 THEN AirConditioner_0.turn_heat_ac\r\n" + 
				"\r\n" + 
				"19. IF SmartHomeSecurity_0.homeMode AND SmartHomeSecurity_0.awayMode THEN AirConditioner_0.turn_ac_heat\r\n"+ 
				"\r\n" + 
				"19. IF temperature<18 AND temperature>21 THEN AirConditioner_0.turn_ac_heat\r\n"+
				"\r\n"+
				"IF temperature>18 THEN Window_0.open_window,AirConditioner_0.turn_ac_heat\r\n"+
				"\r\n"+
				"IF temperature>18 THEN Window_0.open_window\r\n"+
				"\r\n"+
				"IF temperature>18 THEN Fan_0.turn_fan_on\r\n"+
				"\r\n"+
				"IF Fan_0.fon THEN AirConditioner_0.turn_ac_heat\r\n";

		List<Rule> rules=RuleService.getRuleList(ruleText);
		HashMap<String,Rule> rulesMap=new HashMap<>();
		for(Rule rule:rules) {
			rulesMap.put(rule.getRuleName(), rule);
		}
		////静态分析
		StaticAnalysisResult staticAnalsisResult=StaticAnalysisService.getStaticAnalaysisResult(rules, ifdFileName, filePath, environmentModel);
		
		SystemModelService.generateContrModel(filePath+"\\"+modelFileName2, staticAnalsisResult.getUsableRules(), environmentModel.getBiddables(), environmentModel.getDevices());
//		List<String[]> declarations=SystemModelService.generateDeclaration(rules, biddableTypes, deviceTypes, sensorTypes,controlledDevices);
//		List<Action> actions=RuleService.getAllActions(staticAnalsisResult.usableRules, devices);
//		List<Trigger> triggers=RuleService.getAllTriggers(staticAnalsisResult.usableRules, sensorTypes, biddableTypes);
//		SystemModelService.generateAllScenarios(actions, triggers, declarations, devices, deviceTypes, biddableTypes, sensorTypes, modelFileName2, filePath, "300");
		ScenesTree scenesTree=SystemModelService.generateAllScenarios(staticAnalsisResult.getUsableRules(), environmentModel.getDevices(), environmentModel.getDeviceTypes(), environmentModel.getBiddables(), environmentModel.getSensors(), modelFileName2, filePath, "300");
		////动态分析
		List<Scene> scenes=DynamicAnalysisService.getAllSimulationResults(scenesTree,environmentModel.getDevices(), modelFileName2, filePath, "D:\\tools\\uppaal-4.1.24\\uppaal-4.1.24\\bin-Windows",filePath);
		System.out.println(environmentModel.getDevices());
//		s.close();
		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(ifdFileName, filePath);
		List<RuleNode> rulePreRulesNodes=DynamicAnalysisService.getRulePreRules(graphNodes, rulesMap);
		System.out.println(rulePreRulesNodes);
		for(Scene scene:scenes) {
			DynamicAnalysisService.getSceneDataHashMap(scene);
			for(DeviceDetail device:environmentModel.getDevices()) {
				GraphNode deviceNode=new GraphNode();
				for(GraphNode node:graphNodes) {
					if(node.getName().equals(device.getDeviceName())) {
						deviceNode=node;
						break;
					}
				}
				List<ConflictReason> conflictReasons=DynamicAnalysisService.conflictAnalysis(scene.getDataTimeValuesHashMap().get(device.getDeviceName()));
				List<JitterReason> jitterReasons=DynamicAnalysisService.jitterAnalysis(scene.getDataStartTimeValueEndTimeValuesHashMap().get(device.getDeviceName()), "300", "24", "300");
				System.out.println(conflictReasons);
				System.out.println(jitterReasons);
				///获得原因
				if(conflictReasons!=null) {
					System.out.println(device.getDeviceName()+" conflict");
					for(ConflictReason conflictReason:conflictReasons) {
						/////但这个还没有将状态和规则对应起来
						DynamicAnalysisService.getConflictReason(scene, conflictReason, device, deviceNode,graphNodes,rulesMap);
//						System.out.println(causingRules);
						
					}
				}
				if(jitterReasons.size()>0) {
					System.out.println(device.getDeviceName()+" jitter");
					for(JitterReason jitterReason:jitterReasons) {
						DynamicAnalysisService.getJitterReason(scene, jitterReason, device, deviceNode,graphNodes,rulesMap);
//						System.out.println(causingRules);
					}
				}
			}
		}
		String property="AirConditioner_0.cool & Window_2.wopen";
		PropertyVerifyResult propertyVerifyResult=DynamicAnalysisService.propertyAnalysis(property, scenes, environmentModel.getDevices(), environmentModel.getBiddables(), graphNodes, rulesMap);
		System.out.println(propertyVerifyResult);
		

	}

}
