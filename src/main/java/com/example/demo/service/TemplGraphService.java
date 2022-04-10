package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import com.example.demo.bean.*;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

//import com.example.demo.bean.DeviceType.StateEffect;
import com.example.demo.bean.ModelGraph.TemplGraph;
import com.example.demo.bean.ModelGraph.TemplGraphNode;
import com.example.demo.bean.ModelGraph.TemplTransition;
import com.example.demo.service.GetTemplate.Branchpoint;
import com.example.demo.service.GetTemplate.Label;
import com.example.demo.service.GetTemplate.Location;
import com.example.demo.service.GetTemplate.Template;
import com.example.demo.service.GetTemplate.Transition;
@Service
public class TemplGraphService {


	
	

	
	

	
	//////////////获得状态机
	public static List<TemplGraph> getTemplGraphs(String modelFileName,String modelFilePath) throws DocumentException{
		String modelPath=modelFilePath+modelFileName;
		GetTemplate getTemplate=new GetTemplate();
		List<Template> templates=getTemplate.getTemplate(modelPath);
		
		List<TemplGraph> templGraphs=new ArrayList<TemplGraph>();
		for(Template template:templates) {
			TemplGraph templGraph=getTemplGraph(template);
			templGraphs.add(templGraph);
		}
		
		return templGraphs;
	}

	//template->templGraph
	public static TemplGraph getTemplGraph(Template template) {
		TemplGraph templGraph=new TemplGraph();
		templGraph.setName(template.name);
		templGraph.setDeclaration(template.declaration);
		templGraph.setParameter(template.parameter);
		templGraph.setInit(template.init);
		List<TemplGraphNode> templGraphNodes=new ArrayList<TemplGraphNode>();
		for(Location location:template.locations) {
			////SHA的location---state
			TemplGraphNode templGraphNode=new TemplGraphNode();
			templGraphNode.name=location.name;
			templGraphNode.id=location.id;
			templGraphNode.invariant=location.invariant;
			templGraphNode.style=location.style;
			templGraphNodes.add(templGraphNode);
		}
		for(Branchpoint branchpoint:template.branchpoints) {
			TemplGraphNode templGraphNode=new TemplGraphNode();
			templGraphNode.id=branchpoint.id;
			templGraphNode.style="branchpoint";
			templGraphNodes.add(templGraphNode);
		}
		
		for(TemplGraphNode templGraphNode:templGraphNodes) {
			for(Transition transition:template.transitions) {
				if(transition.targetId.equals(templGraphNode.id)) {
					TemplTransition inTransition=new TemplTransition();
					inTransition.source=transition.sourceId;
					inTransition.target=transition.targetId;
					for(Label label:transition.labels) {
						if(label.kind.equals("assignment")) {
							inTransition.assignment=label.content;
						}
						if(label.kind.equals("synchronisation")) {
							inTransition.synchronisation=label.content;
						}
						if(label.kind.equals("probability")) {
							inTransition.probability=label.content;
						}
						if(label.kind.equals("guard")) {
							inTransition.guard=label.content;
						}
					}
					for(TemplGraphNode node:templGraphNodes) {
						if(transition.sourceId.equals(node.id)) {
							inTransition.node=node;
						}
					}
					templGraphNode.inTransitions.add(inTransition);
				}
				if(transition.sourceId.equals(templGraphNode.id)) {
					TemplTransition outTransition=new TemplTransition();
					outTransition.source=transition.sourceId;
					outTransition.target=transition.targetId;
					for(Label label:transition.labels) {
						if(label.kind.equals("assignment")) {
							outTransition.assignment=label.content;
						}
						if(label.kind.equals("synchronisation")) {
							outTransition.synchronisation=label.content;
						}
						if(label.kind.equals("probability")) {
							outTransition.probability=label.content;
						}
						if(label.kind.equals("guard")) {
							outTransition.guard=label.content;
						}
					}
					for(TemplGraphNode node:templGraphNodes) {
						if(transition.targetId.equals(node.id)) {
							outTransition.node=node;
						}
					}
					templGraphNode.outTransitions.add(outTransition);
				}
				
			}
		}
		templGraph.setTemplGraphNodes(templGraphNodes);;
		
		
		
		return templGraph;
	}
	
	
//	////////设置deviceDetail的deviceType
//	public static void setDeviceType(List<DeviceDetail> devices,List<DeviceType> deviceTypes) {
//		for(DeviceDetail device:devices) {
//			for(DeviceType deviceType:deviceTypes) {
//				if(device.getDeviceType().getName().equals(deviceType.getName())) {
//					device.setDeviceType(deviceType);
//					break;
//				}
//			}
//		}
//	}
	
//	////////设置constructionNum,并设置deviceType的device个数
//	public static void setDeviceConstructionNum(List<DeviceDetail> devices,List<DeviceType> deviceTypes) {
//		for(DeviceType deviceType:deviceTypes) {
//			List<DeviceDetail> relatedDevices=new ArrayList<DeviceDetail>();
//			for(DeviceDetail device:devices) {
//				if(device.getDeviceType().equals(deviceType)) {
//					relatedDevices.add(device);
//				}
//			}
//			for(int i=0;i<relatedDevices.size();i++) {
//				DeviceDetail device=relatedDevices.get(i);
//				if(device.getDeviceName().equals("")&&device.getConstructionNum()==-1) {
//					////名字未设置，设备号未设置
//					device.setDeviceName(device.getDeviceType().getName()+"_"+i);
//					device.setConstructionNum(i);
//				}else if(device.getDeviceName().equals("")) {
//					device.setDeviceName(device.getDeviceType().getName()+"_"+i);
//				}else if(device.getConstructionNum()==-1) {
//					device.setConstructionNum(i);
//				}
//			}
//			deviceType.deviceNumber=relatedDevices.size();
//		}
//	}
//
//	//////////device, state, action, value
//	public static List<DeviceType> getDeviceTypes(List<TemplGraph> controlledDevices,List<Attribute_> attributes){
//		List<DeviceType> devices=new ArrayList<DeviceType>();
//		for(TemplGraph controlledDevice:controlledDevices) {
//			if(controlledDevice.getDeclaration().indexOf("controlled_device")>=0) {
//				DeviceType device=getDeviceType(controlledDevice,attributes);
//				devices.add(device);
//			}
//		}
//		return devices;
//	}
	
//	public static DeviceType getDeviceType(TemplGraph controlledDevice,List<Attribute_> attributes) {
//		/////添加设备对环境属性的影响
//		DeviceType device=new DeviceType();
//		device.setName(controlledDevice.getName());;
//		for(TemplGraphNode stateNode:controlledDevice.getTemplGraphNodes()) {
//			////获得当前节点的状态、对应action和value
//			////添加设备对环境属性的影响
//			if(stateNode.getName()!=null) {
//				String[] stateActionValue=new String[3];
//				StateEffect stateEffect=new StateEffect();
//				stateEffect.setState(stateNode.getName());
//				stateActionValue[0]=stateNode.getName();
//				/////可以只需要看一条边
//				for(TemplTransition inTransition:stateNode.getInTransitions()) {
//					if(inTransition.synchronisation!=null&&inTransition.assignment!=null) {
//						////只看一条边
//						if(inTransition.synchronisation.indexOf("?")>0) {
//							String synchronisation=inTransition.synchronisation;
//							//synchronisation: turn_bulb_on[i]?   => turn_bulb_on
//							stateActionValue[1]=synchronisation.substring(0, synchronisation.indexOf("["));
//						}
//						String[] assisnments=inTransition.assignment.split(",");
//						for(String assignment:assisnments) {
//							assignment=assignment.trim();
//							String identifier=device.getName().substring(0, 1).toLowerCase()+device.getName().substring(1);
//							if(assignment.indexOf(identifier)>=0) {
//								stateActionValue[2]=assignment.substring(assignment.indexOf("=")).substring("=".length());
//							}else {
//								/////添加设备状态对环境属性的影响
//								///dtemper=dtemper+(cooldt-heatdt)
//								if(assignment.indexOf("(")>0) {
//									String delta=assignment.substring(0, assignment.indexOf("="));
//									String stateDelta=assignment.substring(assignment.indexOf("("), assignment.indexOf("-")).substring("(".length());   ///cooldt
//									for(Attribute_ attribute:attributes) {
//										if(attribute.getDelta().equals(delta)) {
//											////找到对应属性
//											String[] effect=new String[3];
//											effect[0]=attribute.getAttribute();
//											effect[1]="'==";
//											////从declaration里找到对应的值
//											effect[2]=controlledDevice.getDeclaration().substring(controlledDevice.getDeclaration().indexOf(stateDelta+"=")).substring((stateDelta+"=").length());
//											effect[2]=effect[2].substring(0,effect[2].indexOf(";"));
//											stateEffect.getEffects().add(effect);
//											break;
//										}
//									}
//								}
//
//							}
//						}
//						break;
//					}
//				}
//				device.getStateEffects().add(stateEffect);
//				device.stateActionValues.add(stateActionValue);
//			}
//		}
//		return device;
//	}
//
//	//////获得sensors类型的设备  sensor名字，检测什么属性
//	public static List<SensorType> getSensorTypes(List<TemplGraph> sensors){
//		List<SensorType> sensorTypes=new ArrayList<SensorType>();
//		for(TemplGraph sensor:sensors) {
//			if(sensor.getDeclaration().indexOf("sensor")>=0) {
//				SensorType sensorType=getSensorType(sensor);
//				sensorTypes.add(sensorType);
//			}
//		}
//		return sensorTypes;
//	}
////	//////获得sensor类型的设备  sensor名字，检测什么属性
////	public static SensorType getSensorType(TemplGraph sensor) {
////		SensorType sensorType=new SensorType();
////		sensorType.setName(sensor.getName());
////		if(sensor.getTemplGraphNodes().size()>0) {
////			TemplGraphNode node=sensor.getTemplGraphNodes().get(0);
////			if(node.getInTransitions().size()>0) {
////				TemplTransition inTransition=node.getInTransitions().get(0);
////				String[] assignments=inTransition.assignment.split(",");
////				for(String assignment:assignments) {
////					assignment=assignment.trim();
////					if(assignment.endsWith("=get()")){
////						String attribute=assignment.substring(0, assignment.indexOf("=get()"));
////						sensorType.setAttribute(attribute);
////					}
////				}
////			}
////		}
////		if(sensor.getDeclaration().indexOf("biddable")>=0) {
////			sensorType.setStyle("biddable");
////		}else if(sensor.getDeclaration().indexOf("causal")>=0) {
////			sensorType.setStyle("causal");
////		}
////		return sensorType;
////	}

//	////根据模型获得各属性参数,该模型只有一个状态，状态上是不变式
//	public static List<Attribute_> getAttributes(TemplGraph attributeTempl){
//
//		List<Attribute_> attributes=new ArrayList<>();
//		TemplGraphNode node=attributeTempl.getTemplGraphNodes().get(0);
//		String invariantContent=node.getInvariant();
//		String[] invariants=invariantContent.split("&&");
//		for(String invariant : invariants) {
//			invariant=invariant.trim();
//			Attribute_ attribute=new Attribute_();
//			attribute.setContent(invariant);
//			attribute.setAttribute(invariant.substring(0, invariant.indexOf("'==")));
//			attribute.setDelta(invariant.substring(invariant.indexOf("'==")+"'==".length()).trim());
//			attributes.add(attribute);
//		}
//		return attributes;
//
//	}
	
	/////////获得device在xml中的构造方法 deviceDetail 中的constructionNum
//	public static void getDeviceConstruction(List<DeviceDetail> devices,List<TemplGraph> templGraphs) {
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getDeclaration().contains("controlled_devcie")) {
//				List<DeviceDetail> relatedDevices=new ArrayList<DeviceDetail>();
//				for(DeviceDetail device:devices) {
//					if(device.getDeviceName().equals(templGraph.getName())) {
//						relatedDevices.add(device);
//					}
//				}
//				for(int i=0;i<relatedDevices.size();i++) {
//					DeviceDetail relatedDevice=relatedDevices.get(i);
//					relatedDevice.setConstructionNum(i);
//				}
//			}
//		}
//	}
	
	/////////生成person的templGraph
	public static TemplGraph getPersonTeml(List<String> locations) {
		/////doorway->locations->out
		if(locations.size()<=0) {
			return null;
		}
		TemplGraph person=new TemplGraph();
		person.setName("Person");
		TemplGraphNode startNode=new TemplGraphNode();
		startNode.id="id0";
		startNode.style="committed";
		person.addTemplGraphNode(startNode);
		person.setInit(startNode.id);
		person.setDeclaration("//biddable");
		
		StringBuilder parameter=new StringBuilder();
		//////////out
//		TemplGraphNode outNode=new TemplGraphNode();
//		outNode.id="id1";
//		outNode.name="Out";
//		outNode.invariant="time<=t0";
//		person.addTemplGraphNode(outNode);
//		parameter.append("double t0");
		//////////
		
		/////////doorway///////
		TemplGraphNode doorwayNode=new TemplGraphNode();
		doorwayNode.id="id1";
		doorwayNode.name="Doorway";
		doorwayNode.invariant="time<=t0";
		person.addTemplGraphNode(doorwayNode);
		parameter.append("double t0");
		//////location/////////
		getTwoNodesRelation(startNode, doorwayNode, "position=0", "", "", "");
		TemplGraphNode sourceNode=doorwayNode;
		for(int i=0;i<locations.size();i++) {
			TemplGraphNode locationNode=new TemplGraphNode();
			locationNode.id="id"+(i+2);
			locationNode.name=locations.get(i);
//			if(i<locations.size()-1) {
//				locationNode.invariant="time<=t"+(i+1);
//				parameter.append(",double t"+(i+1));
//			}
			locationNode.invariant="time<=t"+(i+1);
			parameter.append(",double t"+(i+1));
			person.addTemplGraphNode(locationNode);
			getTwoNodesRelation(sourceNode, locationNode, "position="+(i+1), "", "time>=t"+i, "");
			sourceNode=locationNode;
		}
		//////Out//////
		TemplGraphNode outNode=new TemplGraphNode();
		outNode.id="id"+(1+locations.size()+1);
		outNode.name="Out";
		person.addTemplGraphNode(outNode);
		getTwoNodesRelation(sourceNode, outNode, "position="+(1+locations.size()), "", "time>=t"+locations.size(), "");
		
		person.setParameter(parameter.toString());
		
		return person;
	}
	
	/////templGraph中source->target 的transition边
	public static void getTwoNodesRelation(TemplGraphNode sourceNode,TemplGraphNode targetNode, 
			String assignment, String synchronisation,String guard,String probability) {
		TemplTransition outTransition=new TemplTransition();
		TemplTransition inTransition=new TemplTransition();
		outTransition.assignment=assignment;
		outTransition.guard=guard;
		outTransition.synchronisation=synchronisation;
		outTransition.probability=probability;
		outTransition.source=sourceNode.id;
		outTransition.target=targetNode.id;
		
		inTransition.assignment=assignment;
		inTransition.guard=guard;
		inTransition.synchronisation=synchronisation;
		inTransition.probability=probability;
		inTransition.source=sourceNode.id;
		inTransition.target=targetNode.id;
		
		sourceNode.outTransitions.add(outTransition);
		outTransition.node=targetNode;
		targetNode.inTransitions.add(inTransition);
		inTransition.node=sourceNode;			
	}
	
	

	
//	//clone graph
//	public TemplGraph cloneTemplGraph(TemplGraph templGraph) {
//		TemplGraph cloneGraph=new TemplGraph();
//		for(TemplGraphNode templGraphNode:templGraph.getTemplGraphNodes()) {
//			TemplGraphNode cloneNode=cloneTemplGraphNode(templGraphNode);
//			cloneGraph.addTemplGraphNode(cloneNode);
//		}
//		connectNode(cloneGraph);
//		return cloneGraph;
//	}
//	
//	//connect
//	public void connectNode(TemplGraph templGraph) {
//		for(TemplGraphNode node:templGraph.getTemplGraphNodes()) {
//			Iterator<TemplTransition> inTransitions=node.inTransitions .iterator();
//			while(inTransitions.hasNext()) {
//				TemplTransition inTransition=inTransitions.next();
//				boolean hasNode=false;
//				for(TemplGraphNode otherNode:templGraph.getTemplGraphNodes()) {
//					if(inTransition.node.id.equals(otherNode.id)) {
//						hasNode=true;
//						inTransition.node=otherNode;
//						boolean existTran=false;
//						for(TemplTransition outTransition:otherNode.outTransitions) {
//							if(outTransition.node.id.equals(node.id)) {
//								existTran=true;
//								outTransition.node=node;
//								break;
//							}
//						}
//						if(!existTran) {
//							//如果没有这条边，就添加这条边
//							TemplTransition outTransition=new TemplTransition();
//							outTransition.assignment=inTransition.assignment;
//							outTransition.guard=inTransition.guard;
//							outTransition.probability=inTransition.probability;
//							outTransition.synchronisation=inTransition.synchronisation;
//							outTransition.source=inTransition.source;
//							outTransition.target=inTransition.target;
//							outTransition.node=node;
//							otherNode.outTransitions.add(outTransition);
//						}
//						break;
//					}
//				}
//				if(!hasNode) {
//					//delet
//					inTransitions.remove();
//				}
//			}
//			Iterator<TemplTransition> outTransitions=node.outTransitions.iterator();
//			while(outTransitions.hasNext()) {
//				TemplTransition outTransition=outTransitions.next();
//				boolean hasNode=false;
//				for(TemplGraphNode otherNode:templGraph.getTemplGraphNodes()) {
//					if(outTransition.node.id.equals(otherNode.id)) {
//						hasNode=true;
//						outTransition.node=otherNode;
//						boolean existTran=false;
//						for(TemplTransition inTransition:otherNode.inTransitions) {
//							if(inTransition.node.id.equals(node.id)) {
//								existTran=true;
//								inTransition.node=node;
//								break;
//							}
//						}
//						if(!existTran) {
//							//如果没有这条边，就添加这条边
//							TemplTransition inTransition=new TemplTransition();
//							inTransition.assignment=outTransition.assignment;
//							inTransition.guard=outTransition.guard;
//							inTransition.probability=outTransition.probability;
//							inTransition.synchronisation=outTransition.synchronisation;
//							inTransition.source=outTransition.source;
//							inTransition.target=outTransition.target;
//							inTransition.node=node;
//							otherNode.inTransitions.add(inTransition);
//						}
//						break;
//					}
//				}
//				if(!hasNode) {
//					outTransitions.remove();
//				}
//			}
//			
//		}
//	}
//	
//	//clone node
//	public TemplGraphNode cloneTemplGraphNode(TemplGraphNode node) {
//		TemplGraphNode cloneNode=new TemplGraphNode();
//		cloneNode.id=node.id;
//		cloneNode.invariant=node.invariant;
//		cloneNode.name=node.name;
//		cloneNode.style=node.style;
//		for(TemplTransition inTransition:node.inTransitions) {
//			TemplTransition cloneinT=new TemplTransition();
//			cloneinT.assignment=inTransition.assignment;
//			cloneinT.guard=inTransition.guard;
//			cloneinT.node=inTransition.node;
//			cloneinT.probability=inTransition.probability;
//			cloneinT.source=inTransition.source;
//			cloneinT.synchronisation=inTransition.synchronisation;
//			cloneinT.target=inTransition.target;
//			cloneNode.inTransitions.add(cloneinT);
//		}
//		for(TemplTransition outTransition:node.outTransitions) {
//			TemplTransition cloneoutT=new TemplTransition();
//			cloneoutT.assignment=outTransition.assignment;
//			cloneoutT.guard=outTransition.guard;
//			cloneoutT.node=outTransition.node;
//			cloneoutT.probability=outTransition.probability;
//			cloneoutT.source=outTransition.source;
//			cloneoutT.synchronisation=outTransition.synchronisation;
//			cloneoutT.target=outTransition.target;
//			cloneNode.outTransitions.add(cloneoutT);
//		}
//		return cloneNode;
//	}
//	
//	//删除节点的某条边
//	public void deletNodeEdge(TemplTransition transition,TemplGraphNode node) {
//		for(TemplTransition inTransition:node.inTransitions) {
//			if(inTransition==transition) {
//				node.inTransitions.remove(inTransition);
//				return;
//			}
//		}
//		for(TemplTransition outTransition:node.outTransitions) {
//			if(outTransition==transition) {
//				node.outTransitions.remove(outTransition);
//				return;
//			}
//		}
//	}
//	
//	//删除节点某条边以及对应节点该边
//	public void deletEdge(TemplTransition transition,TemplGraphNode node) {
//		deletNodeEdge(transition, node);
//		TemplGraphNode anotherNode=transition.node;
//		deletNodeEdge(transition, anotherNode);
//	}

}
