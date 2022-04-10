package com.example.demo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import java.util.Stack;

import com.example.demo.bean.Conflict;
import com.example.demo.bean.ConflictReason;
import com.example.demo.bean.DataTimeValue;
import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.IFDGraph.GraphNodeArrow;
import com.example.demo.bean.JitterReason;
import com.example.demo.bean.Rule;
import com.example.demo.bean.RuleNode;

@Service
public class DynamicAnalysisService {

	public static void main(String[] args) {
		String result="\r\n" + 
				"temperature:\r\n" + 
				"[0]: (0,29) (0,29) (1,29) (1,29) (20.00000000000031,19.50000000000189) (20.00000000000031,19.50000000000189) (300.0099999998721,19.50000000000189)\r\n" + 
				"dtemper:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,-0.5) (1.07,-0.5) (20.00000000000031,-0.5) (20.00000000000031,-0.5) (20.07000000000032,0) (20.07000000000032,0) (300.0099999998721,0)\r\n" + 
				"humidity:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"dhumi:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"pm_2_5:\r\n" + 
				"[0]: (0,19) (0,19) (300.0099999998721,19)\r\n" + 
				"dpm_2_5:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"co2ppm:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"dco2ppm:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"aqi:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"dapi:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"gas_level:\r\n" + 
				"[0]: (0,14) (0,14) (300.0099999998721,14)\r\n" + 
				"dgas_l:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"smoke_level:\r\n" + 
				"[0]: (0,14) (0,14) (300.0099999998721,14)\r\n" + 
				"dsmoke_l:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"coppm:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"dcoppm:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"fire:\r\n" + 
				"[0]: (0,0) (0,0) (0.07000000000000001,1) (0.07000000000000001,1) (30,1) (30,1) (30.07000000000185,0) (30.07000000000185,0) (300.0099999998721,0)\r\n" + 
				"wind:\r\n" + 
				"[0]: (0,0) (0,0) (0.07000000000000001,1) (0.07000000000000001,1) (30,1) (30.07000000000185,0) (30.07000000000185,0) (299.9999999998537,0) (299.9999999998537,0) (300.0099999998721,1)\r\n" + 
				"rain:\r\n" + 
				"[0]: (0,0) (0,0) (299.9999999998537,0) (300.0099999998721,1)\r\n" + 
				"leak:\r\n" + 
				"[0]: (0,0) (0,0) (0.07000000000000001,1) (0.07000000000000001,1) (30,1) (30,1) (30.07000000000185,0) (30.07000000000185,0) (149.9999999999993,0) (149.9999999999993,0) (150.0700000000085,1) (150.0700000000085,1) (179.9999999999702,1) (179.9999999999702,1) (180.0699999999812,0) (180.0699999999812,0) (269.9999999998828,0) (269.9999999998828,0) (270.0699999998993,1) (270.0699999998993,1) (299.9999999998537,1) (299.9999999998537,1) (300.0099999998721,0)\r\n" + 
				"position:\r\n" + 
				"[0]: (0,0) (0,0) (42,0) (42,0) (42.07000000000014,1) (42.07000000000014,1) (84,1) (84,1) (84.07000000000605,2) (84.07000000000605,2) (126,2) (126,2) (126.0700000000275,3) (126.0700000000275,3) (167.9999999999922,3) (168,3) (168.0699999999999,4) (168.0699999999999,4) (209.9999999999411,4) (210,4) (210.0699999999999,5) (210.0699999999999,5) (251.9999999999158,5) (252,5) (252.0699999999999,6) (252.0699999999999,6) (300.0099999998721,6)\r\n" + 
				"deviceName=Bulb_8,deviceType=Bulb,location=Location5:\r\n" + 
				"[0]: (0,0) (0,0) (210.999999999953,0) (210.999999999953,0) (211.069999999953,1) (211.069999999953,1) (252.9999999999148,1) (252.9999999999148,1) (253.0699999999148,0) (253.0699999999148,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_7,deviceType=Bulb,location=Location4:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_6,deviceType=Bulb,location=Location4:\r\n" + 
				"[0]: (0,0) (0,0) (168.9999999999912,0) (168.9999999999912,0) (169.0699999999912,1) (169.0699999999912,1) (210.999999999953,1) (210.999999999953,1) (211.069999999953,0) (211.069999999953,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_5,deviceType=Bulb,location=Location3:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_4,deviceType=Bulb,location=Location3:\r\n" + 
				"[0]: (0,0) (0,0) (126,0) (126.0000000000275,0) (126.0700000000275,1) (126.0700000000275,1) (168.9999999999912,1) (168.9999999999912,1) (169.0699999999912,0) (169.0699999999912,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_3,deviceType=Bulb,location=Location2:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_2,deviceType=Bulb,location=Location2:\r\n" + 
				"[0]: (0,0) (0,0) (84,0) (84.00000000000601,0) (84.07000000000605,1) (84.07000000000605,1) (126,1) (126.0000000000275,1) (126.0700000000275,0) (126.0700000000275,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_1,deviceType=Bulb,location=Location1:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Bulb_0,deviceType=Bulb,location=Location1:\r\n" + 
				"[0]: (0,0) (0,0) (42,0) (42.00000000000016,0) (42.07000000000014,1) (42.07000000000014,1) (84,1) (84.00000000000601,1) (84.07000000000605,0) (84.07000000000605,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Window_0,deviceType=Window,location=Location2:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Window_2,deviceType=Window,location=Location5:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Window_1,deviceType=Window,location=Location4:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Dehumidifier_0,deviceType=Dehumidifier,location=Location1:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Alarm_0,deviceType=Alarm,location=Location1:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=AirPurifier_0,deviceType=AirPurifier,location=Location1:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"deviceName=AirConditioner_0,deviceType=AirConditioner,location=Location1:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (20.00000000000031,1) (20.00000000000031,1) (20.07000000000032,0) (20.07000000000032,0) (300.0099999998721,0)\r\n" + 
				"deviceName=Ventilator_0,deviceType=Ventilator,location=Badroom:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"rule1:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (300.0099999998721,1)\r\n" + 
				"rule2:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"rule3:\r\n" + 
				"[0]: (0,0) (0,0) (9.999999999999828,0) (9.999999999999828,0) (10.06999999999983,1) (10.06999999999983,1) (300.0099999998721,1)\r\n" + 
				"rule4:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (3.999999999999956,1) (3.999999999999956,1) (4.069999999999955,0) (4.069999999999955,0) (300.0099999998721,0)\r\n" + 
				"rule5:\r\n" + 
				"[0]: (0,0) (0,0) (20.00000000000031,0) (20.00000000000031,0) (20.07000000000032,1) (20.07000000000032,1) (300.0099999998721,1)\r\n" + 
				"rule6:\r\n" + 
				"[0]: (0,0) (0,0) (299.9999999998537,0) (299.9999999998721,0) (300.0099999998721,1)\r\n" + 
				"rule7:\r\n" + 
				"[0]: (0,0) (0,0) (42,0) (42.00000000000016,0) (42.07000000000014,1) (42.07000000000014,1) (84,1) (84.00000000000601,1) (84.07000000000605,0) (84.07000000000605,0) (300.0099999998721,0)\r\n" + 
				"rule8:\r\n" + 
				"[0]: (0,0) (0,0) (84,0) (84.00000000000601,0) (84.07000000000605,1) (84.07000000000605,1) (126,1) (126.0000000000275,1) (126.0700000000275,0) (126.0700000000275,0) (300.0099999998721,0)\r\n" + 
				"rule9:\r\n" + 
				"[0]: (0,0) (0,0) (168.9999999999912,0) (168.9999999999912,0) (169.0699999999912,1) (169.0699999999912,1) (210.999999999953,1) (210.999999999953,1) (211.069999999953,0) (211.069999999953,0) (300.0099999998721,0)\r\n" + 
				"rule10:\r\n" + 
				"[0]: (0,0) (0,0) (126,0) (126.0000000000275,0) (126.0700000000275,1) (126.0700000000275,1) (168.9999999999912,1) (168.9999999999912,1) (169.0699999999912,0) (169.0699999999912,0) (300.0099999998721,0)\r\n" + 
				"rule11:\r\n" + 
				"[0]: (0,0) (0,0) (210.999999999953,0) (210.999999999953,0) (211.069999999953,1) (211.069999999953,1) (252.9999999999148,1) (252.9999999999148,1) (253.0699999999148,0) (253.0699999999148,0) (300.0099999998721,0)\r\n" + 
				"rule12:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (42,1) (42.00000000000016,1) (42.07000000000014,0) (42.07000000000014,0) (84,0) (84.00000000000601,0) (84.07000000000605,1) (84.07000000000605,1) (300.0099999998721,1)\r\n" + 
				"rule13:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (84,1) (84.00000000000601,1) (84.07000000000605,0) (84.07000000000605,0) (126,0) (126.0000000000275,0) (126.0700000000275,1) (126.0700000000275,1) (300.0099999998721,1)\r\n" + 
				"rule14:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (168.9999999999912,1) (168.9999999999912,1) (169.0699999999912,0) (169.0699999999912,0) (210.999999999953,0) (210.999999999953,0) (211.069999999953,1) (211.069999999953,1) (300.0099999998721,1)\r\n" + 
				"rule15:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (126,1) (126.0000000000275,1) (126.0700000000275,0) (126.0700000000275,0) (168.9999999999912,0) (168.9999999999912,0) (169.0699999999912,1) (169.0699999999912,1) (300.0099999998721,1)\r\n" + 
				"rule16:\r\n" + 
				"[0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (210.999999999953,1) (210.999999999953,1) (211.069999999953,0) (211.069999999953,0) (252.9999999999148,0) (252.9999999999148,0) (253.0699999999148,1) (253.0699999999148,1) (300.0099999998721,1)\r\n" + 
				"rule17:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"rule18:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"rule19:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"rule20:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"rule21:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,0)\r\n" + 
				"time:\r\n" + 
				"[0]: (0,0) (0,0) (300.0099999998721,300.0099999998721)\r\n" + 
				"";
		List<DataTimeValue> dataTimeValues=getAllDataTimeValues(result);
		System.out.println(dataTimeValues);
		HashMap<String, List<double[]>> dataTimeValuesHashMap=getDataTimeValuesHashMap(result);
		HashMap<String,List<double[]>> dataStartTimeValueEndTimeValuesMap=new HashMap<>();
		for(Entry<String,List<double[]>> dataTimeValue:dataTimeValuesHashMap.entrySet()) {
			dataStartTimeValueEndTimeValuesMap.put(dataTimeValue.getKey(), getStartTimeValueEndTimeValuesHashMap(dataTimeValue.getValue()));
			
		}
		
		for(Entry<String,List<double[]>> dataTimeValue:dataTimeValuesHashMap.entrySet()) {
			List<ConflictReason> conflictReasons=conflictAnalysis(dataTimeValue.getValue());
			System.out.println(conflictReasons);
			List<double[]> startTimeValueEndTimeValues=dataStartTimeValueEndTimeValuesMap.get(dataTimeValue.getKey());
			jitterAnalysis(startTimeValueEndTimeValues, "300", "24", "300");
			
		}
		
		
	}
	
	/////动态分析数据,包括设备状态冲突和设备抖动
//	public static void getAllScenariosDynamicAnalysis(List<Scene> scenes,List<DeviceDetail> devices,HashMap<String,Rule> rulesMap,String simulationTime,String equivalentTime,String intervalTime,List<GraphNode> graphNodes) {
//		/////rules=>rulesMap
////		HashMap<String,Rule> rulesMap=new HashMap<>();
////		for(Rule rule:rules) {
////			rulesMap.put(rule.getRuleName(), rule);
////		}
//
//		/////获得ifd上各节点
////		List<GraphNode> graphNodes=StaticAnalysisService.getIFDNode(ifdFileName, filePath);
//
//		/////获得每条规则的前提rules
////		List<RuleNode> rulePreRulesNodes=DynamicAnalysisService.getRulePreRules(graphNodes, rulesMap);
//		ExecutorService executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
//
////		for(Scene scene:scenes) {
////			/////单个场景分析
////			Runnable analysisWork=new Runnable() {
////
////				@Override
////				public void run() {
////					// TODO Auto-generated method stub
////					getSingleScenarioDynamicAnalysis(scene, devices, graphNodes, rulesMap,simulationTime,equivalentTime,intervalTime);
////				}
////			};
////			executorService.execute(analysisWork);
//////			getSingleScenarioDynamicAnalysis(scene, devices, graphNodes, rulesMap);
//////			////先获得场景dataTimeValue和startTimeValueEndTimeValue的hashmap
//////			getSceneDataHashMap(scene);
//////			List<DeviceAnalysisResult> deviceAnalysisResults=new ArrayList<>();
//////			for(DeviceDetail device:devices) {
//////				///找到对应设备节点
//////				GraphNode deviceNode=new GraphNode();
//////				for(GraphNode node:graphNodes) {
//////					if(node.getName().equals(device.getDeviceName())) {
//////						deviceNode=node;
//////						break;
//////					}
//////				}
//////				////分析设备
//////				DeviceAnalysisResult deviceAnalysisResult=new DeviceAnalysisResult();
//////				deviceAnalysisResult.setDeviceName(device.getDeviceName());
//////				List<ConflictReason> conflictReasons=DynamicAnalysisService.conflictAnalysis(scene.getDataTimeValuesHashMap().get(device.getDeviceName()));
//////				List<JitterReason> jitterReasons=DynamicAnalysisService.jitterAnalysis(scene.getDataStartTimeValueEndTimeValuesHashMap().get(device.getDeviceName()), "300", "24", "300");
//////				////分析问题原因
//////				if(conflictReasons!=null) {
//////					System.out.println(device.getDeviceName()+" conflict");
//////					for(ConflictReason conflictReason:conflictReasons) {
//////						////每次conflict原因
//////						getConflictReason(scene, conflictReason, device, deviceNode,graphNodes,rulesMap);
////////						System.out.println(causingRules);
//////
//////					}
//////				}
//////				if(jitterReasons.size()>0) {
//////					System.out.println(device.getDeviceName()+" jitter");
//////					for(JitterReason jitterReason:jitterReasons) {
//////						////每次jitter原因
//////						DynamicAnalysisService.getJitterReason(scene, jitterReason, device, deviceNode,graphNodes,rulesMap);
////////						System.out.println(causingRules);
//////					}
//////				}
//////
//////
//////				deviceAnalysisResult.setConflictReasons(conflictReasons);
//////				deviceAnalysisResult.setJitterReasons(jitterReasons);
//////				deviceAnalysisResults.add(deviceAnalysisResult);
//////			}
//////			scene.setDeviceAnalysisResults(deviceAnalysisResults);
////		}
////		executorService.shutdown();
////		try {
////			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
////		} catch (InterruptedException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//
//
//
//		HashMap<String,DeviceDetail> deviceHashMap=new HashMap<>();
//		HashMap<String,GraphNode> deviceNodeHashMap=new HashMap<>();
//		if(scenes.get(0)!=null) {
//			for(DeviceDetail device:devices) {
//				deviceHashMap.put(device.getDeviceName(), device);
//				for(GraphNode node:graphNodes) {
//					if(node.getName().equals(device.getDeviceName())) {
//						deviceNodeHashMap.put(device.getDeviceName(), node);
//						break;
//					}
//				}
//			}
//		}
//		//////测试时间用
//		long preProcessingStartTime=System.currentTimeMillis();
//		for(Scene scene:scenes) {
//			getSceneDataHashMap(scene);
//			List<DeviceAnalysisResult> deviceAnalysisResults=new ArrayList<>();
//			for(DeviceDetail device:devices) {
//				DeviceAnalysisResult deviceAnalysisResult=new DeviceAnalysisResult();
//				deviceAnalysisResult.setDeviceName(device.getDeviceName());
//				deviceAnalysisResults.add(deviceAnalysisResult);
//			}
//			scene.setDeviceAnalysisResults(deviceAnalysisResults);
//		}
//		System.out.println("preProcessTime:"+(System.currentTimeMillis()-preProcessingStartTime));
//		long conflcitStartTime=System.currentTimeMillis();
//		/////conflict
//		for(Scene scene:scenes) {
//			/////单个场景分析
//			Runnable analysisWork=new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					getSingleScenarioConflictAnalysis(scene, deviceHashMap, deviceNodeHashMap, graphNodes, rulesMap);
//				}
//			};
//			executorService.execute(analysisWork);
//		}
//		executorService.shutdown();
//		try {
//			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("conflictTime:"+(System.currentTimeMillis()-conflcitStartTime));
//		executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
//		long jitterStartTime=System.currentTimeMillis();
//		////jitter
//		for(Scene scene:scenes) {
//			/////单个场景分析
//			Runnable analysisWork=new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					getSingleScenarioJitterAnalysis(scene, deviceHashMap, deviceNodeHashMap, graphNodes, rulesMap, simulationTime, equivalentTime, intervalTime);
//				}
//			};
//			executorService.execute(analysisWork);
//		}
//		executorService.shutdown();
//		try {
//			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("jitterTime:"+(System.currentTimeMillis()-jitterStartTime));
//
//	}
	
//	public static void getSingleScenarioConflictAnalysis(Scene scene,HashMap<String,DeviceDetail> deviceHashMap,HashMap<String,GraphNode> deviceNodeHashMap,List<GraphNode> graphNodes,HashMap<String,Rule> rulesMap) {
//		for(DeviceAnalysisResult deviceAnalysisResult:scene.getDeviceAnalysisResults()) {
//			String deviceName=deviceAnalysisResult.getDeviceName();
//			DeviceDetail device=deviceHashMap.get(deviceName);
//			GraphNode deviceNode=deviceNodeHashMap.get(deviceName);
//			List<ConflictReason> conflictReasons=DynamicAnalysisService.conflictAnalysis(scene.getDataTimeValuesHashMap().get(device.getDeviceName()));
//			if(conflictReasons.size()>0) {
////				System.out.println(device.getDeviceName()+" conflict");
//				for(ConflictReason conflictReason:conflictReasons) {
//					////每次conflict原因
//					getConflictReason(scene, conflictReason, device, deviceNode,graphNodes,rulesMap);
////					System.out.println(causingRules);
//
//				}
//			}
//			deviceAnalysisResult.setConflictReasons(conflictReasons);
//		}
//
//	}
//	public static void getSingleScenarioJitterAnalysis(Scene scene,HashMap<String,DeviceDetail> deviceHashMap,HashMap<String,GraphNode> deviceNodeHashMap,List<GraphNode> graphNodes,HashMap<String,Rule> rulesMap,String simulationTime,String equivalentTime,String intervalTime) {
//		for(DeviceAnalysisResult deviceAnalysisResult:scene.getDeviceAnalysisResults()) {
//			String deviceName=deviceAnalysisResult.getDeviceName();
//			DeviceDetail device=deviceHashMap.get(deviceName);
//			GraphNode deviceNode=deviceNodeHashMap.get(deviceName);
//			//////分别获得check 冲突时间、jitter的时间；以及分别给出原因的时间
//			List<JitterReason> jitterReasons=DynamicAnalysisService.jitterAnalysis(scene.getDataStartTimeValueEndTimeValuesHashMap().get(device.getDeviceName()), simulationTime, equivalentTime, intervalTime);
//			////分析问题原因
//
//			if(jitterReasons.size()>0) {
////				System.out.println(device.getDeviceName()+" jitter");
//				for(JitterReason jitterReason:jitterReasons) {
//					////每次jitter原因
//					DynamicAnalysisService.getJitterReason(scene, jitterReason, device, deviceNode,graphNodes,rulesMap);
////					System.out.println(causingRules);
//				}
//			}
//			deviceAnalysisResult.setJitterReasons(jitterReasons);
//		}
//
//
//	}
//
//	public static void getSingleScenarioDynamicAnalysis(Scene scene,List<DeviceDetail> devices,List<GraphNode> graphNodes,HashMap<String,Rule> rulesMap,String simulationTime,String equivalentTime,String intervalTime) {
//		////先获得场景dataTimeValue和startTimeValueEndTimeValue的hashmap
//		getSceneDataHashMap(scene);
//		List<DeviceAnalysisResult> deviceAnalysisResults=new ArrayList<>();
//		for(DeviceDetail device:devices) {
//			///找到对应设备节点
//			GraphNode deviceNode=new GraphNode();
//			for(GraphNode node:graphNodes) {
//				if(node.getName().equals(device.getDeviceName())) {
//					deviceNode=node;
//					break;
//				}
//			}
//			////分析设备
//			DeviceAnalysisResult deviceAnalysisResult=new DeviceAnalysisResult();
//			deviceAnalysisResult.setDeviceName(device.getDeviceName());
//			//////分别获得check 冲突时间、jitter的时间；以及分别给出原因的时间
//			List<ConflictReason> conflictReasons=DynamicAnalysisService.conflictAnalysis(scene.getDataTimeValuesHashMap().get(device.getDeviceName()));
//			List<JitterReason> jitterReasons=DynamicAnalysisService.jitterAnalysis(scene.getDataStartTimeValueEndTimeValuesHashMap().get(device.getDeviceName()), simulationTime, equivalentTime, intervalTime);
//			////分析问题原因
//			if(conflictReasons.size()>0) {
//				System.out.println(device.getDeviceName()+" conflict");
//				for(ConflictReason conflictReason:conflictReasons) {
//					////每次conflict原因
//					getConflictReason(scene, conflictReason, device, deviceNode,graphNodes,rulesMap);
////					System.out.println(causingRules);
//
//				}
//			}
//			if(jitterReasons.size()>0) {
//				System.out.println(device.getDeviceName()+" jitter");
//				for(JitterReason jitterReason:jitterReasons) {
//					////每次jitter原因
//					DynamicAnalysisService.getJitterReason(scene, jitterReason, device, deviceNode,graphNodes,rulesMap);
////					System.out.println(causingRules);
//				}
//			}
//
//
//			deviceAnalysisResult.setConflictReasons(conflictReasons);
//			deviceAnalysisResult.setJitterReasons(jitterReasons);
//			deviceAnalysisResults.add(deviceAnalysisResult);
//		}
//		scene.setDeviceAnalysisResults(deviceAnalysisResults);
//	}
//
//	public static void  getJitterReason(Scene scene,JitterReason jitterReason,DeviceDetail device,GraphNode deviceNode,List<GraphNode> graphNodes,HashMap<String,Rule> rulesMap) {
//		////IFD上找所有规则的前提规则
//		List<RuleNode> rulePreRules=getRulePreRules(graphNodes, rulesMap);
//		HashMap<String,RuleNode> rulePreRulesMap=new HashMap<>();
//		List<CauseRule> causingRules=new ArrayList<>();
//		for(RuleNode rulePreRule:rulePreRules) {
//			rulePreRulesMap.put(rulePreRule.getRule().getRuleName(), rulePreRule);
//		}
//		List<Integer> differentValues=new ArrayList<>();  ///抖动的状态
//		for(double[] timeValue:jitterReason.getJitter()) {
//			boolean exist=false;
//			Integer stateValue=(int) timeValue[1];  ////状态值
//			for(Integer value:differentValues) {
//				if(value.equals(stateValue)) {
//					exist=true;
//					break;
//				}
//			}
//			if(!exist) {
//				differentValues.add(stateValue);
//			}
//		}
//		for(Integer value:differentValues) {
//			////找到对应的设备状态的action
//			for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
//				if(value==Integer.parseInt(stateActionValue[2])) {
//					String action=stateActionValue[1];
//					////找到IFD上的相关规则
//					List<RuleNode> relatedRulePreRules=getRelatedRulesFromIFD(action, deviceNode,rulePreRulesMap);
//					////再看能否被触发
//					List<RuleNode> stateCausingRules=getCanTriggeredRules(relatedRulePreRules, scene.getDataStartTimeValueEndTimeValuesHashMap(), jitterReason.getJitter().get(0)[0], jitterReason.getJitter().get(jitterReason.getJitter().size()-1)[0]);
//					List<RuleNode> newStateCausingRules=new ArrayList<>();
//					for(RuleNode causingRule:stateCausingRules) {
//						newStateCausingRules.add(avoidRecurse(causingRule, new ArrayList<>()));
//
//					}
//					CauseRule causingRule=new CauseRule();
//					causingRule.setState(stateActionValue[0]);
//					causingRule.setValue(Integer.parseInt(stateActionValue[2]));
//					causingRule.setStateCausingRules(newStateCausingRules);
//					causingRules.add(causingRule);
//				}
//			}
//		}
//		jitterReason.setCausingRules(causingRules);
//	}
//
//	//////寻找哪些规则导致的状态冲突和抖动
//	public static void getConflictReason(Scene scene,ConflictReason conflictReason,DeviceDetail device,GraphNode deviceNode,List<GraphNode> graphNodes,HashMap<String,Rule> rulesMap) {
//
//		////IFD上找所有规则的前提规则
//		List<RuleNode> rulePreRules=getRulePreRules(graphNodes, rulesMap);
//		HashMap<String,RuleNode> rulePreRulesMap=new HashMap<>();
////		List<List<RuleNode>> statesCausingRules=new ArrayList<>();
//		List<CauseRule> causingRules=new ArrayList<>();
//		for(RuleNode rulePreRule:rulePreRules) {
//			rulePreRulesMap.put(rulePreRule.getRule().getRuleName(), rulePreRule);
//		}
//		for(double value:conflictReason.getConflict().getConflictValues()) {
//			////找到值对应的action
//
//			for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
//				if((int)value==Integer.parseInt(stateActionValue[2])) {
//					String action=stateActionValue[1];
////					List<String> stateCausingRules=new ArrayList<>();
//					////找到IFD上的该action相关规则，以及前提规则
//					List<RuleNode> relatedRulePreRules=getRelatedRulesFromIFD(action, deviceNode,rulePreRulesMap);
//
//					////再看能否被触发
//					////找到相关规则
//					List<RuleNode> stateCausingRules=getCanTriggeredRules(relatedRulePreRules, scene.getDataStartTimeValueEndTimeValuesHashMap(), conflictReason.getConflict().getTime());
//					List<RuleNode> newStateCausingRules=new ArrayList<>();
//					for(RuleNode causingRule:stateCausingRules) {
//						newStateCausingRules.add(avoidRecurse(causingRule, new ArrayList<>()));
//
//					}
//					CauseRule causingRule=new CauseRule();
//					causingRule.setState(stateActionValue[0]);
//					causingRule.setValue(Integer.parseInt(stateActionValue[2]));
//					causingRule.setStateCausingRules(newStateCausingRules);
//					causingRules.add(causingRule);
////					statesCausingRules.add(stateCausingRules);
//				}
//			}
//		}
//		conflictReason.setCausingRules(causingRules);
//	}
	
	/////////为避免递归存在，删除循环递归的pre规则
	public static RuleNode avoidRecurse(RuleNode causingRule,List<RuleNode> causingRules){
		RuleNode newCausingRule=new RuleNode();
		newCausingRule.setRule(causingRule.getRule());
		causingRules.add(newCausingRule);
		for(RuleNode preNode:causingRule.getPreRules()) {
			boolean exist=false;
			for(RuleNode causeRule:causingRules) {
				if(preNode.getRule()==causeRule.getRule()) {
					exist=true;
					break;
				}
			}
			if(exist) {
				continue;
			}
			RuleNode newPreNode=new RuleNode();
			newPreNode.setRule(preNode.getRule());			
			newCausingRule.addPreRule(avoidRecurse(preNode, causingRules));
			
		}
		return newCausingRule;
	}
	
	/////冲突对应的
	public static List<RuleNode> getCanTriggeredRules(List<RuleNode> relatedRulePreRules,HashMap<String,List<double[]>> dataStartTimeValueEndTimeValuesMap,double conflictTime) {
		////再看能否被触发

		List<RuleNode> stateCausingRules=new ArrayList<>();
		////删除不能发生的，包括preRule
		for(RuleNode rule:relatedRulePreRules) {
			if(canTriggered(conflictTime, dataStartTimeValueEndTimeValuesMap.get(rule.getRule().getRuleName()))) {
				////如果当前规则能触发，则看是否有前提规则
				////为了跳出循环，搞个count，只迭代三层
				int count=0;
				stateCausingRules.add(rule);  ////存进去
				Stack<RuleNode> ruleStack=new Stack<>();
				ruleStack.push(rule);
				while(!ruleStack.isEmpty()) {
					////看当前这条规则后面的能否发生
					RuleNode currentRuleNode=ruleStack.pop();
					if(currentRuleNode!=null)
					for(int i=0;i<currentRuleNode.getPreRules().size();i++) {
						RuleNode currentPreRule=currentRuleNode.getPreRules().get(i);
						if(count>3) {
							continue;
						}
						if(canTriggered(0, conflictTime,dataStartTimeValueEndTimeValuesMap.get(rule.getRule().getRuleName()))) {
							////如果下个rule也能触发，就加进来
							ruleStack.push(currentPreRule);
							count++;
						}else {
							///不能触发则直接设置为null
							currentRuleNode.setPreRules(i, null);
						}
					}
				}
			}

		}
		return stateCausingRules;
	}
	
	/////jitter对应的
	public static List<RuleNode> getCanTriggeredRules(List<RuleNode> relatedRulePreRules,HashMap<String,List<double[]>> dataStartTimeValueEndTimeValuesMap,double startTime,double endTime) {
		////再看能否被触发

		List<RuleNode> stateCausingRules=new ArrayList<>();
		////删除不能发生的，包括preRule
		for(RuleNode rule:relatedRulePreRules) {
			if(canTriggered(startTime,endTime, dataStartTimeValueEndTimeValuesMap.get(rule.getRule().getRuleName()))) {
				////如果当前规则能触发，则看是否有前提规则
				////为了跳出循环，搞个count，只迭代三层
				int count=0;
				stateCausingRules.add(rule);  ////存进去
				Stack<RuleNode> ruleStack=new Stack<>();
				ruleStack.push(rule);
				while(!ruleStack.isEmpty()) {
					////看当前这条规则后面的能否发生
					RuleNode currentRuleNode=ruleStack.pop();
					if(currentRuleNode!=null)
					for(int i=0;i<currentRuleNode.getPreRules().size();i++) {
						RuleNode currentPreRule=currentRuleNode.getPreRules().get(i);
						if(count>3) {
							continue;
						}
						if(canTriggered(0, endTime,dataStartTimeValueEndTimeValuesMap.get(rule.getRule().getRuleName()))) {
							////如果下个rule也能触发，就加进来
							ruleStack.push(currentPreRule);
							count++;
						}else {
							///不能触发则直接设置为null
							currentRuleNode.setPreRules(i, null);
						}
					}
				}
			}

		}
		return stateCausingRules;
	}
	
	/////看对应的规则能否触发
	public static boolean canTriggered(double startTime,double endTime,List<double[]> startTimeValueEndTimeValues) {
		if(startTimeValueEndTimeValues==null)
		System.out.println(startTimeValueEndTimeValues);
		for(double[] startTimeValueEndTimeValue:startTimeValueEndTimeValues) {
			/////找到对应时间点是否触发
			if(startTimeValueEndTimeValue[1]>0 && startTimeValueEndTimeValue[3]>0) {
				if(!(startTimeValueEndTimeValue[0]>=endTime)&&!(startTimeValueEndTimeValue[2]<=startTime)) {
					///有在范围内，则表明触发了
					return true;
					
				}
			}
			if(startTimeValueEndTimeValue[0]>endTime) {
				////已经超出范围了
				break;
			}
		}
		return false;
	}
	public static boolean canTriggered(double conflictTime,List<double[]> startTimeValueEndTimeValues) {
		for(double[] startTimeValueEndTimeValue:startTimeValueEndTimeValues) {
			/////找到对应时间点是否触发
			if(!(startTimeValueEndTimeValue[0]>=conflictTime)&&!(startTimeValueEndTimeValue[2]<=conflictTime)
					&& startTimeValueEndTimeValue[1]>0 && startTimeValueEndTimeValue[3]>0) {
				/////(t1,v1,t2,v2)  if (t1<=conflictTime<=t2 && v1=v2=1) 则说明触发了
				return true;				
			}
			if(startTimeValueEndTimeValue[0]>conflictTime) {
				////已经超出范围了
				break;
			}
		}
		return false;
	}

	//////找抖动时触发的规则，获得对应时间范围内触发了哪些规则
	public static List<String> getTriggeredRules(double startTime,double endTime,HashMap<String,List<double[]>> startTimeValueEndTimeValuesMap){
		List<String> triggeredRules=new ArrayList<String>();
		for(Entry<String,List<double[]>> dataStartTimeValueEndTimeValues:startTimeValueEndTimeValuesMap.entrySet()) {
			if(dataStartTimeValueEndTimeValues.getKey().contains("rule")) {
				/////找到规则的仿真数据
				for(double[] startTimeValueEndTimeValue:dataStartTimeValueEndTimeValues.getValue()) {
					/////找到对应时间点是否触发
					if(startTimeValueEndTimeValue[1]>0 && startTimeValueEndTimeValue[3]>0) {
						if(!(startTimeValueEndTimeValue[0]>=endTime)&&!(startTimeValueEndTimeValue[2]<=startTime)) {
							///有在范围内，则表明触发了
							triggeredRules.add(dataStartTimeValueEndTimeValues.getKey());
							break;
						}
					}
				}
			}
		}
		
		return triggeredRules;
	}
	
	///////找冲突时触发的规则
	public static List<String> getTriggeredRules(double conflictTime,HashMap<String,List<double[]>> startTimeValueEndTimeValuesMap){
		List<String> triggeredRules=new ArrayList<String>();
		for(Entry<String,List<double[]>> dataStartTimeValueEndTimeValues:startTimeValueEndTimeValuesMap.entrySet()) {
			if(dataStartTimeValueEndTimeValues.getKey().contains("rule")) {
				/////找到规则的仿真数据
				for(double[] startTimeValueEndTimeValue:dataStartTimeValueEndTimeValues.getValue()) {
					/////找到对应时间点是否触发
					/////(t1,v1,t2,v2)  if (t1<=conflictTime<=t2 && v1=v2=1) 则说明触发了
					if(startTimeValueEndTimeValue[0]<=conflictTime&&startTimeValueEndTimeValue[2]>=conflictTime
							&& startTimeValueEndTimeValue[1]>0 && startTimeValueEndTimeValue[3]>0) {
						triggeredRules.add(dataStartTimeValueEndTimeValues.getKey());
						break;
					}
				}
			}
		}
		
		return triggeredRules;
	}
	
	///////先根据值从IFD上找相关规则，以及对应的前提规则
	public static List<RuleNode> getRelatedRulesFromIFD(String action,GraphNode deviceNode,HashMap<String,RuleNode> ruleNodesMap) {
		GraphNode actionNode=new GraphNode();
		List<String> relatedRules=new ArrayList<>();
		List<RuleNode> relatedRuleNodes=new ArrayList<>();
		for(GraphNodeArrow cArrow:deviceNode.getcNodeList()) {
			//////找到相关actionNode
			if(cArrow.getGraphNode().getLabel().endsWith(action)) {
				actionNode=cArrow.getGraphNode();
				break;
			}
		}
		////找到所有会执行该action的ruleNodes
		for(GraphNodeArrow pArrow:actionNode.getpNodeList()) {
			if(pArrow.getGraphNode().getShape().equals("hexagon")) {
				relatedRules.add(pArrow.getGraphNode().getName());
				/////找到规则和其前提规则
				relatedRuleNodes.add(ruleNodesMap.get(pArrow.getGraphNode().getName()));
			}
		}
		return relatedRuleNodes;
	}
	
	/////找到每条规则的前提规则
	public static List<RuleNode> getRulePreRules(List<GraphNode> graphNodes, HashMap<String,Rule> rulesMap){ 
		List<RuleNode> rulePreRulesNodes=new ArrayList<>();
		for(GraphNode graphNode:graphNodes) {
			if(graphNode.getShape().equals("hexagon")) {
				////找到规则节点
				RuleNode rulePreRulesNode=new RuleNode();
				Rule rule=rulesMap.get(graphNode.getName());
				rulePreRulesNode.setRule(rule);
				////preRules
				List<RuleNode> preRuleNodes=new ArrayList<>();
				////rule对应的triggers
				List<GraphNode> triggerNodes=new ArrayList<>();				
				for(GraphNodeArrow pArrow:graphNode.getpNodeList()) {
					////先找到triggerNode
					triggerNodes.add(pArrow.getGraphNode());
				}
				for(GraphNode triggerNode:triggerNodes) {
					boolean isDeviceState=false;   ////是设备状态才需要前提
					for(GraphNodeArrow pArrow:triggerNode.getpNodeList()) {
						if(pArrow.getGraphNode().getFillcolor().equals("darkseagreen1")) {
							isDeviceState=true;
							break;
						}
					}
					if(isDeviceState) {
						for(GraphNodeArrow pArrow:triggerNode.getpNodeList()) {
							////找前提规则,先找对应action
							if(pArrow.getGraphNode().getShape().equals("record")) {
								GraphNode actionNode=pArrow.getGraphNode();
								for(GraphNodeArrow ppArrow:actionNode.getpNodeList()) {
									////找到action对应的rules
									if(ppArrow.getGraphNode().getShape().equals("hexagon")) {
										RuleNode preRuleNode=new RuleNode();
										Rule preRule=rulesMap.get(ppArrow.getGraphNode().getName());
										preRuleNode.setRule(preRule);
										preRuleNodes.add(preRuleNode);
									}
								}
								break;
							}
						}
					}
				}
				rulePreRulesNode.setPreRules(preRuleNodes);
				rulePreRulesNodes.add(rulePreRulesNode);
			}
		}
		for(RuleNode rulePreRulesNode:rulePreRulesNodes) {
			for(int i=0;i<rulePreRulesNode.getPreRules().size();i++) {
				////替换成rulePreRulesNodes
				RuleNode preRuleNode=rulePreRulesNode.getPreRules().get(i);
				for(RuleNode ruleNode:rulePreRulesNodes) {
					if(preRuleNode.getRule().equals(ruleNode.getRule())) {
						///替换
						rulePreRulesNode.setPreRules(i, ruleNode);
						break;
					}
				}
			}
		}
		
		return rulePreRulesNodes;
	}


	
	/////找同一场景下满足时间段的交集
	public static List<double[]> getIntersaction(List<double[]> timeList,List<double[]> newTimeList){
		List<double[]> finalTimeList=new ArrayList<double[]>();
		for(int i=0,j=0;i<timeList.size()&&j<newTimeList.size();) {
			if(timeList.get(i)[0]>=newTimeList.get(j)[1]) {
				j++;
				continue;
			}else if(timeList.get(i)[1]<=newTimeList.get(j)[0]) {
				i++;
				continue;
			}
			if(timeList.get(i)[1]<newTimeList.get(j)[1]) {
				double[] time=new double[2];
				time[0]=Math.max(timeList.get(i)[0], newTimeList.get(j)[0]);
				time[1]=timeList.get(i)[1];
				if(time[1]-time[0]>1) {
					finalTimeList.add(time);
				}
				i++;
			}else {
				double[] time=new double[2];
				time[0]=Math.max(timeList.get(i)[0], newTimeList.get(j)[0]);
				time[1]=newTimeList.get(j)[1];
				if(time[1]-time[0]>1) {
					////交集区间大于1才考虑
					finalTimeList.add(time);
				}
				j++;
			}
					
		}
		return finalTimeList;
	}
	

	////////抖动分析，看当前场景下，该设备是否会发生抖动
	public static List<JitterReason> jitterAnalysis(List<double[]> startTimeValueEndTimeValues, String simulationTime, String equivalentTime, String intervalTime) {
		
		List<List<double[]>> jitters=new ArrayList<>();    
		
		
		///equivalentTime单位是h，simulationTime单位是clock，intervalTime单位是s
		double multiple=Double.parseDouble(equivalentTime)*3600/Double.parseDouble(simulationTime);
		///deltaTime单位是clock
		double deltaTime=Double.parseDouble(intervalTime)/multiple;
		
		///// [(0,0)(120.0000000000171,0)]，[(120.0000000000171,0) (120.0700000000245,1)]，[(120.0700000000245,1)(149.9999999999993,1)]， 
		////  [(149.9999999999993,1) (150.0700000000085,0)]， [(150.0700000000085,0) (300.0099999998721,0)]

		for(int i=1;i<startTimeValueEndTimeValues.size()-1;i++) {
			/////开头不考虑，最后一个也不考虑
			double[] startTimeValueEndTimeValue=startTimeValueEndTimeValues.get(i);
			double startTime=startTimeValueEndTimeValue[0];
			double endTime=startTimeValueEndTimeValue[2];
			double startValue=startTimeValueEndTimeValue[1];
			double endValue=startTimeValueEndTimeValue[3];
			if((endValue+"").equals(startValue+"")) {
				////如果首位值相等则为持续时间，否则不考虑
				if((endTime-startTime)<deltaTime) {
					/////某个状态持续时间很短，则算一次抖动？如何找原因呢这样，抖动必然是有其他规则使得该状态无法维持下去了
					//////记录startTime v1和nextEndTime v2
					/////如[(120.0700000000245,1)(149.9999999999993,1)]， [(149.9999999999993,1) (150.0700000000085,0)]   
					////  startTime=120.0700000000245 v1=1； nextEndTime=150.0700000000085  v2=0
					
//					double[] jitter=new double[4];
//					jitter[0]=startTime;     ////(120.0700000000245,1) 该状态开始时间
//					jitter[1]=startValue;
//					jitter[2]=startTimeValueEndTimeValues.get(i+1)[2];  ////(150.0700000000085,0)  另一个状态开始时间
//					jitter[3]=startTimeValueEndTimeValues.get(i+1)[3];
					
					
					
					List<double[]> jitterList=new ArrayList<>();
					double[] jitterStart=new double[2];  ////(120.0700000000245,1) 该状态开始时间
					jitterStart[0]=startTime;
					jitterStart[1]=startValue;
					double[] jitterNewStart=new double[2];    ////(150.0700000000085,0)  另一个状态开始时间
					jitterNewStart[0]=startTimeValueEndTimeValues.get(i+1)[2];
					jitterNewStart[1]=startTimeValueEndTimeValues.get(i+1)[3];
					jitterList.add(jitterStart);
					jitterList.add(jitterNewStart);
					jitters.add(jitterList);
				}
			}
		}
		/////实现分段，两个首位相同时间值的可拼接。
		List<JitterReason> jitterReasons=new ArrayList<>();
//		List<List<double[]>> newJitters=new ArrayList<>();
		for(List<double[]> jitter:jitters) {
			if(jitterReasons.isEmpty()) {  ////为空则说明是第一次抖动
				JitterReason jitterReason=new JitterReason();
				jitterReason.setJitter(jitter);
				jitterReasons.add(jitterReason);
			}else {
				////如果当前这次jitter的起始时间和状态和上一个jitter另一个状态的起始时间和状态相同
				String currentStartTime=jitter.get(0)[0]+"";
				String currentStartValue=jitter.get(0)[1]+"";
				List<double[]> lastPartJitter=jitterReasons.get(jitterReasons.size()-1).getJitter();   ////上一段连续抖动
				String lastStartTime=lastPartJitter.get(lastPartJitter.size()-1)[0]+"";  ////上一段连续抖动的最后一个时间值
				String lastStartValue=lastPartJitter.get(lastPartJitter.size()-1)[1]+"";
				if(currentStartTime.equals(lastStartTime)&&currentStartValue.equals(lastStartValue)) {
					/////如果当前jitter的第一个时间值和上一段连续抖动的最后一个时间值相同，则可拼接
					lastPartJitter.add(jitter.get(1));
				}else {
					/////否则作为新一段加入
					JitterReason jitterReason=new JitterReason();
					jitterReason.setJitter(jitter);
					jitterReasons.add(jitterReason);
				}
			}
		}
		
		
		
		return jitterReasons;
	}
	
	//////去掉重复的timeValue？ timeValue => double[2]; ===> startTimeValueEndTimeValue => double[4] ，为了便于抖动分析和property分析？。。
	public static List<double[]> getStartTimeValueEndTimeValuesHashMap(List<double[]> timeValues) {
		List<double[]> startTimeValueEndTimeValues=new ArrayList<>();
		for(int i=0;i<timeValues.size();) {
			/////double[0],[1] startTime,startValue;double[2],[3] endTime,endValue
			/////可能有startValue=endValue，也可能startValue！=endValue
			///// (0,0) (0,0) (120,0) (120.0000000000171,0) (120.0700000000245,1) (120.0700000000245,1) 
			////  (149.9999999999993,1) (150.0700000000085,0) (150.0700000000085,0) (300.0099999998721,0)
			double[] startTimeValueEndTimeValue=new double[4];
			startTimeValueEndTimeValue[0]=timeValues.get(i)[0];   
			startTimeValueEndTimeValue[1]=timeValues.get(i)[1];
			double startTime=timeValues.get(i)[0];   ////起始时间和值
			double startValue=timeValues.get(i)[1];
//			double currentTime=startTime;     ////对应当前指针
//			double currentValue=startValue;
			String t1=startTime+"";           ///(0,0)
			String v1=startValue+""; 
			String currentT=t1;
			String currentV=v1;
			int j=i+1;
			for(;j<timeValues.size();j++) {
				double endTime=timeValues.get(j)[0];
				double endValue=timeValues.get(j)[1];
				String t2=endTime+"";
				String v2=endValue+"";
				if(v2.equals(currentV)) {
					currentV=v2;                //// until (120.0000000000171,0)
					currentT=t2;
//					currentTime=endTime;
//					currentValue=endValue;
					startTimeValueEndTimeValue[2]=endTime;
					startTimeValueEndTimeValue[3]=endValue;
					i=j;
					continue;
				}else {
					if(currentT.equals(t2)) {   ////冲突的点不考虑
						continue;
					}
					if(!currentT.equals(t1)&&currentV.equals(v1)) {  /////如果值相等的区间不小 (0,0)  (120.0000000000171,0)
						break;
					}
					if(!currentV.equals(v2)) {   //////如果两个值不等  (120.0000000000171,0) (120.0700000000245,1)
						startTimeValueEndTimeValue[2]=endTime;
						startTimeValueEndTimeValue[3]=endValue;
						i=j;
						break;
					}
				}
			}
			startTimeValueEndTimeValues.add(startTimeValueEndTimeValue);
			if(j==timeValues.size()) {
				/////如果已经到最后一个timeValue了，则最后一个为end
				startTimeValueEndTimeValue[2]=timeValues.get(j-1)[0];
				startTimeValueEndTimeValue[3]=timeValues.get(j-1)[1];				
				break;
			}
		}
		///// 结果为[(0,0)(120.0000000000171,0)]，[(120.0000000000171,0) (120.0700000000245,1)]，[(120.0700000000245,1)(149.9999999999993,1)]， 
		////  [(149.9999999999993,1) (150.0700000000085,0)]， [(150.0700000000085,0) (300.0099999998721,0)]
		return startTimeValueEndTimeValues;
	}
	
	/////冲突分析，获得当前场景下是否冲突，冲突的时间
	public static List<ConflictReason> conflictAnalysis(List<double[]> timeValues) {
		List<ConflictReason> conflictReasons=new ArrayList<>();
		for(int i=0;i<timeValues.size();) {
			///看是否存在 t1=t2 而 v1!=v2
			Conflict conflict=new Conflict();
			conflict.setTime(timeValues.get(i)[0]);    ////设置冲突时间
			List<Integer> conflictValues=new ArrayList<>();
			double time1=timeValues.get(i)[0];
			double value1=timeValues.get(i)[1];
			String t1=time1+"";
			String v1=value1+"";
			String currentT=t1;
			String currentV=v1;
			int j=i+1;
			for(;j<timeValues.size();j++) {
				String t2=timeValues.get(j)[0]+"";
				String v2=timeValues.get(j)[1]+"";
				if(currentT.equals(t2)&&!currentV.equals(v2)) {
					////表明冲突，add冲突的值，不重复
					////看是否已经有了
					int timeValue=(int) timeValues.get(j)[1];
					boolean exist=false;
					for(Integer value:conflictValues) {
						if(timeValue==value) {
							exist=true;
							break;
						}
					}
					if(!exist) {
						conflictValues.add(timeValue);
					}
					currentT=t2;
					currentV=v2;
				}else {
					i=j;
					break;
				}
			}
			if(j>=timeValues.size()) {
				break;
			}
			
			
			if(conflictValues.size()>0) {
				////有冲突 
				//////add起始值，不重复
				int timeValue=(int) value1;
				boolean exist=false;
				for(Integer value:conflictValues) {
					if(timeValue==value) {
						exist=true;
						break;
					}
				}
				if(!exist) {
					conflictValues.add(timeValue);
				}
				conflict.setConflictValues(conflictValues);
				ConflictReason conflictReason=new ConflictReason();
				conflictReason.setConflict(conflict);
				conflictReasons.add(conflictReason);
			}
		}
//		if(conflictReasons.size()==0) {
//			conflictReasons=null;
//		}
		return conflictReasons;
	}
	
	
	/////生成各个场景的仿真结果
//	public static List<Scene> getAllSimulationResults(ScenesTree scenesTree,List<DeviceDetail> devices,String fileName,String modelFilePath,String uppaalPath,String simulateResultFilePath) {
//		final String fileNameWithoutSuffix=fileName.substring(0, fileName.lastIndexOf(".xml"));
//		final List<Scene> scenes=new ArrayList<>();
////		List<Thread> threads=new ArrayList<>();
//		long simulationStartTime=System.currentTimeMillis();
//		ExecutorService executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
//		for(int i=0;i<scenesTree.getChildren().size();i++) {
//			SimulationThreadService sRunnable=new SimulationThreadService(scenes, devices, uppaalPath, fileNameWithoutSuffix, i+"", modelFilePath,simulateResultFilePath);
//			executorService.execute(sRunnable);
//		}
//		executorService.shutdown();
////		for(Thread thread:threads) {
////			try {
////				thread.join();
////			} catch (InterruptedException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
////		}
//		try {
//			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if(scenes==null){
//			///仿真结果有误
//			return null;
//		}
//		if(scenes.size()>0){
//			if(scenes.get(0).getDataTimeValues().size()==0){
//				return null;
//			}
//		}
//		System.out.println("simulationTime:"+(System.currentTimeMillis()-simulationStartTime));
//		Comparator<Scene> c=new Comparator<Scene>() {
//
//			@Override
//			public int compare(Scene s1, Scene s2) {
//				int num1=Integer.parseInt(s1.getScenarioName().substring("scenario-".length()));
//				int num2=Integer.parseInt(s2.getScenarioName().substring("scenario-".length()));
//				if(num1<num2) {
//					return -1;
//				}else {
//					return 1;
//				}
//			}
//		};
//		Collections.sort(scenes, c);
//		return scenes;
//	}
//
//	public static Scene getSingleSimulationResult(List<DeviceDetail> devices,String uppaalPath,String fileNameWithoutSuffix,String scenarioNum,String modelFilePath,String simulationResultFilePath) {
//		///仿真
//		List<Scene> scenes=new ArrayList<>();
//		SimulationThreadService sRunnable=new SimulationThreadService(scenes, devices, uppaalPath, fileNameWithoutSuffix, scenarioNum, modelFilePath, simulationResultFilePath);
//		Thread thread=new Thread(sRunnable);
//		thread.start();
//		try {
//			thread.join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return scenes.get(0);
//	}
	
	/////将仿真结果解析成 <数据名，（时间，取值）List> 的格式存入hashmap,数据名作为key
	public static HashMap<String,List<double[]>> getDataTimeValuesHashMap(String simulationResult){
		HashMap<String,List<double[]>> dataTimeValuesHashMap=new HashMap<>();
		////按行分割
		List<String> datasValues=Arrays.asList(simulationResult.split("\n"));
		for(int i=0;i<datasValues.size();) {
			String dataValues=datasValues.get(i);
			if(dataValues.indexOf(":")>0 && dataValues.indexOf("[0]")<0) {
				//////数据名			
				String dataName="";  ////存数据名
				List<double[]> timeValues=new ArrayList<>();  ///存该数据的timeValues
				/////获得数据名
				if(dataValues.indexOf("deviceName")>=0) {
					String[] informations=dataValues.split(",");
					for(String information:informations) {
						if(information.contains("deviceName")) {
							///获得设备名，作为分析的标识符，即数据名
							dataName=information.substring(information.indexOf("=")).substring(1).trim();
							break;
						}
					}
				}else {
					dataName=dataValues.substring(0, dataValues.indexOf(":"));
				}				
				i++;
				String valueString=datasValues.get(i);
				if(valueString.indexOf("[0]:")>=0) {
					valueString=valueString.substring("[0]:".length());
//					System.out.println(valueString);
					List<String> timeValueStrs=Arrays.asList(valueString.split(" \\("));					
					for(String timeValueStr:timeValueStrs) {
						/////解析获得timeValue
						if(timeValueStr.indexOf(",")>0) {
							timeValueStr=timeValueStr.substring(0, timeValueStr.indexOf(")"));
							String[] splitTimeValue=timeValueStr.split(",");
							double[] timeValue=new double[2];
							timeValue[0]=Double.parseDouble(splitTimeValue[0]);
//							System.out.println(timeValue[0]);
							timeValue[1]=Double.parseDouble(splitTimeValue[1]);
//							System.out.println(timeValue[1]);
							timeValues.add(timeValue);
						}
					}					
				}
				dataTimeValuesHashMap.put(dataName, timeValues);				
			}
			i++;
		}
		return dataTimeValuesHashMap;
	}
	
//	//////获得对应的hashmap
//	public static void getSceneDataHashMap(Scene scene){
//		for(DataTimeValue dataTimeValue:scene.getDataTimeValues()) {
//			scene.getDataTimeValuesHashMap().put(dataTimeValue.getName(), dataTimeValue.getTimeValues());
//			scene.getDataStartTimeValueEndTimeValuesHashMap().put(dataTimeValue.getName(), getStartTimeValueEndTimeValuesHashMap(dataTimeValue.getTimeValues()));
//		}
//
//	}
	/////将仿真结果解析成 （数据名，（时间，取值）） 的格式
	public static List<DataTimeValue> getAllDataTimeValues(String simulationResult){
		List<DataTimeValue> dataTimeValues=new ArrayList<DataTimeValue>();
		////按行分割
		List<String> datasValues=Arrays.asList(simulationResult.split("\n"));
		for(int i=0;i<datasValues.size();) {
			String dataValues=datasValues.get(i);
			if(dataValues.indexOf(":")>0 && dataValues.indexOf("[0]")<0) {
				//////数据名
				DataTimeValue dataTimeValue=new DataTimeValue();
				String dataName="";
				if(dataValues.indexOf("deviceName")>=0) {
					String[] informations=dataValues.split(",");
					for(String information:informations) {
						if(information.contains("deviceName")) {
							///获得设备名，作为分析的标识符，即数据名
							dataName=information.substring(information.indexOf("=")).substring(1).trim();
							break;
						}
					}
				}else {
					dataName=dataValues.substring(0, dataValues.indexOf(":"));
				}
				
				dataTimeValue.name=dataName;
				i++;
				String valueString=datasValues.get(i);
				if(valueString.indexOf("[0]:")>=0) {
					valueString=valueString.substring("[0]:".length());
//					System.out.println(valueString);
					List<String> timeValues=Arrays.asList(valueString.split(" \\("));
					for(String timeValueStr:timeValues) {
						if(timeValueStr.indexOf(",")>0) {
							timeValueStr=timeValueStr.substring(0, timeValueStr.indexOf(")"));
							String[] splitTimeValue=timeValueStr.split(",");
							double[] timeValue=new double[2];
							timeValue[0]=Double.parseDouble(splitTimeValue[0]);
//							System.out.println(timeValue[0]);
							timeValue[1]=Double.parseDouble(splitTimeValue[1]);
//							System.out.println(timeValue[1]);
							dataTimeValue.getTimeValues().add(timeValue);
						}
					}
				}
				dataTimeValues.add(dataTimeValue);
			}
			i++;
		}
		return dataTimeValues;
	}
	
	////CMD 获得仿真结果
	public static String getSimulationResult(String uppaalPath,String fileName,String filePath) {
		  InputStream error = null;
		  	try {

//		  		System.out.println(command.toString());
		  		Process process = Runtime.getRuntime().exec(getLinuxCommand(uppaalPath, fileName, filePath));
		  		error = process.getErrorStream();
//		  		long startTime0=System.currentTimeMillis();
		  		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(),Charset.forName("UTF-8")));
//		  		long endTime0=System.currentTimeMillis();
//	  			System.out.println("simulationData time: "+(endTime0-startTime0));
		  		StringBuffer resultBuffer = new StringBuffer();
		  		String s = "";
		  		s = bufferedReader.readLine();
		  		if(s==null) {
		  			System.out.println("null");
		  		}
		  		while (s != null) {
					  if(s.indexOf("[error]")>=0){
						  ///如果模型有误直接返回null
						  return null;
					  }
//		  			System.out.println(s);
		  			resultBuffer.append(s+"\n");
//		  			long startTime=System.currentTimeMillis();
		  			s = bufferedReader.readLine();
//		  			long endTime=System.currentTimeMillis();
//		  			System.out.println("readline time: "+(endTime-startTime));
//		  			if(s!=null) {
//		  				System.out.println(s.toString());	
//		  			}
		  			
		  		}
		  		bufferedReader.close();
		  		process.waitFor();
		  		String result=resultBuffer.toString();
		  		int formulaIsSatisfiedIndex=result.indexOf("Formula is satisfied.");
		  		if(formulaIsSatisfiedIndex>=0) {
		  			result=result.substring(formulaIsSatisfiedIndex).substring("Formula is satisfied.".length());
		  		}
		  		return result;
		  	} catch (Exception ex) {
		  		if (error != null) {
		  			try {
		  				error.close();
		  			} catch (IOException e) {
		  				e.printStackTrace();
		  			}
		  		}
		  		return ex.getMessage();
		  	}
	}
	
	public static String getCMDCommand(String uppaalPath,String fileName,String filePath) {
  		StringBuffer command = new StringBuffer();
  		command.append("cmd /c d: ");
  		//这里的&&在多条语句的情况下使用，表示等上一条语句执行成功后在执行下一条命令，
  		//也可以使用&表示执行上一条后台就立刻执行下一条语句
  		command.append(String.format(" && cd %s", uppaalPath));
  		command.append(" && verifyta.exe -O std "+filePath+fileName);
  		return command.toString();
	}
	
	public static String getLinuxCommand(String uppaalPath,String fileName,String filePath) {
  		StringBuffer command = new StringBuffer();
  		//这里的&&在多条语句的情况下使用，表示等上一条语句执行成功后在执行下一条命令，
  		//也可以使用&表示执行上一条后台就立刻执行下一条语句
  		command.append(String.format("%s", uppaalPath));
  		command.append("./verifyta -O std "+filePath+fileName);
  		return command.toString();
	}
}
