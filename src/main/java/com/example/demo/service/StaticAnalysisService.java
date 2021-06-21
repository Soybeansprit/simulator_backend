package com.example.demo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

import com.example.demo.bean.Action;
import com.example.demo.bean.BiddableType;
import com.example.demo.bean.DeviceDetail;
import com.example.demo.bean.EnvironmentModel;
import com.example.demo.bean.ErrorReason;
import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.IFDGraph.GraphNodeArrow;
import com.example.demo.bean.Rule;
import com.example.demo.bean.SensorType;
import com.example.demo.bean.StaticAnalysisResult;
import com.example.demo.bean.Trigger;
@Service
public class StaticAnalysisService {	
	
	public static StaticAnalysisResult getStaticAnalaysisResult(List<Rule> rules, String ifdFileName,String filePath,EnvironmentModel environmentModel ) throws DocumentException, IOException {

		StaticAnalysisResult staticAnalysisResult=new StaticAnalysisResult();
		staticAnalysisResult.setTotalRules(rules);
		////返回可用的rules和各种错误
		HashMap<String,Rule> mapRules=new HashMap<String,Rule>();
		for(Rule rule:rules) {
			mapRules.put(rule.getRuleName(), rule);
		}
		//转成可解析xml文件

		List<DeviceDetail> devices=environmentModel.getDevices();
		List<SensorType> sensors=environmentModel.getSensors();
		List<BiddableType> biddables=environmentModel.getBiddables();
		
		
		/////移除incorrect rules
//		List<Rule> incorrectRules=getIncorrect(rules, templGraphs);
		List<ErrorReason> incorrectRules=getIncorrect(rules, devices);
		
		
		Iterator<Rule> iteratorRules=rules.iterator();
		while(iteratorRules.hasNext()) {
			Rule rule=iteratorRules.next();			
			for(ErrorReason er:incorrectRules) {
				if(rule.getRuleName().equals(er.rule.getRuleName())) {
					iteratorRules.remove();
				}
			}
		}
		///////////////////删除重复的规则
		List<Rule> newRules=deleteRepeat(rules);
		
		generateIFD(newRules, ifdFileName, filePath, devices, sensors, biddables);
		List<GraphNode> nodes=getIFDNode(ifdFileName, filePath);
		List<GraphNode> ruleNodes=new ArrayList<GraphNode>();
		
		for(GraphNode node:nodes) {
			if(node.getShape().equals("hexagon")) {
				ruleNodes.add(node);
			}
		}
		///////////获得unused
		List<ErrorReason> unusedRules=getUnused(ruleNodes, devices,biddables,mapRules);
		System.out.println(unusedRules);
		Iterator<Rule> iteratorNewRules=newRules.iterator();
		//////////删掉unused
		while(iteratorNewRules.hasNext()) {
			Rule rule=iteratorNewRules.next();
			for(ErrorReason unusedRule:unusedRules) {
				if(unusedRule.rule.getRuleName().equals(rule.getRuleName())) {
					iteratorNewRules.remove();
					break;
				}
			}
		}
		generateIFD(newRules, ifdFileName, filePath, devices, sensors, biddables);
		ruleNodes.clear();
		nodes.clear();
		nodes=getIFDNode(ifdFileName, filePath);
		for(GraphNode node:nodes) {
			if(node.getShape().equals("hexagon")) {
				ruleNodes.add(node);
			}
		}

		//////获得redundant rules
		List<List<GraphNode>> redundantRuleNodes=new ArrayList<List<GraphNode>>();
		for(GraphNode ruleNode:ruleNodes) {
			redundantRuleNodes.add(getRedundant(ruleNode,nodes));
		}
		
		List<List<Rule>> redundantRules=new ArrayList<List<Rule>>();
		for(List<GraphNode> redundant:redundantRuleNodes) {
			List<Rule> reRules=new ArrayList<Rule>();
			for(GraphNode node:redundant) {
				Rule rule=mapRules.get(node.getName());
				reRules.add(rule);
			}
			if(reRules.size()>1) {
				redundantRules.add(reRules);
			}
		}
		System.out.println(redundantRuleNodes);
		List<Action> actions=RuleService.getAllActions(newRules, devices);
		List<DeviceDetail> cannotOffDevices=getIncompleteness(devices, actions);
		List<String> incompleteness=new ArrayList<String>();
		for(DeviceDetail device:cannotOffDevices) {
			for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
				if(Integer.parseInt(stateActionValue[2])==0) {
					String incomplete="Missing rule to turn off "+device.getDeviceName();
					incompleteness.add(incomplete);
					break;
				}
			}
		}
		
		staticAnalysisResult.setIncorrectRules(incorrectRules);
		staticAnalysisResult.setUnusedRules(unusedRules);
		staticAnalysisResult.setRedundantRules(redundantRules);
		staticAnalysisResult.setIncompleteness(incompleteness);
		staticAnalysisResult.setUsableRules(newRules);
		return staticAnalysisResult;
		
	}
	
	////////////////////获得不正确的规则
	public static List<ErrorReason> getIncorrect(List<Rule> rules,List<DeviceDetail> devices){
		List<ErrorReason> incorrectReason=new ArrayList<ErrorReason>();
		for(Rule rule:rules) {
			
			String reason="";
			boolean incorrect=false;
			for(String action:rule.getAction()) {
				////看一条规则的action是不是存在问题   Bulb_0.turn_bulb_on
				String actionPulse="";
				String deviceName="";
				if(action.indexOf(".")>0) {
					actionPulse=action.substring(action.indexOf(".")).substring(1);		////////turn_bulb_on 
					deviceName=action.substring(0, action.indexOf("."));       /////////Bulb_0
				}
				boolean existAction=false;
				for(DeviceDetail device:devices) {
					if(device.getDeviceName().equals(deviceName)) {
						////找到这个设备
						for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
							if(stateActionValue[1].equals(actionPulse)) {
								////看action是否是对的
								existAction=true;
								break;
							}
						}
						break;
					}
				}
				if(!existAction) {
					incorrect=true;	
					reason+=action.trim()+" ";   //////给出原因
				}
			}
			if(incorrect) {
				ErrorReason er=new ErrorReason();
				er.rule=rule;
				er.reason=reason+"cannot be executed!";
				incorrectReason.add(er);	
				System.out.println(reason);
			}
			
		}
		return incorrectReason;
	}

	//////////删除重复的规则
	public static List<Rule> deleteRepeat(List<Rule> rules){
		List<Rule> newRules=new ArrayList<Rule>();
		for(Rule rule:rules) {
			boolean equal=false;
			for(Rule newRule:newRules) {
				if(newRule.contentEquals(rule)) {
					equal=true;
					break;
				}
			}
			if(!equal) {
				newRules.add(rule);
			}
		}
		return newRules;
	}
	
	public static List<ErrorReason> getUnused(List<GraphNode> ruleNodes,List<DeviceDetail> devices,List<BiddableType> biddables,HashMap<String,Rule> mapRules){
		List<ErrorReason> unusedRules=new ArrayList<ErrorReason>();
		for(GraphNode ruleNode:ruleNodes) {
			String reason=isUnused(ruleNode,devices,biddables);
			if(!reason.equals("")) {
				////isUnused
				Rule unusedRule=mapRules.get(ruleNode.getName());
				ErrorReason er=new ErrorReason();
				er.reason=reason;
				er.rule=unusedRule;
				unusedRules.add(er);
			}
		}
		return unusedRules;
	}
	
	public static String isUnused(GraphNode ruleNode,List<DeviceDetail> devices,List<BiddableType> biddables) {
		String reason="";
		List<GraphNode> triggerNodes=new ArrayList<GraphNode>();
		for(GraphNodeArrow pArrow:ruleNode.getpNodeList()) {
			GraphNode triggerNode=pArrow.getGraphNode();
			boolean legal=false;
			for(GraphNodeArrow ppArrow:triggerNode.getpNodeList()) {
				if(ppArrow.getColor().indexOf("lightpink")>=0) {
					legal=true;
					break;
				}
			}
			if(!legal) {
				//////////trigger不合法
//				correct=false;
				reason="Trigger: "+triggerNode.getLabel()+" illegal.";
				System.out.println(reason);
//				isUnused=true;
				return reason;
			}
			triggerNodes.add(triggerNode);
		}
		///////////trigger之间矛盾
		for(int i=0;i<triggerNodes.size();i++) {
			GraphNode triggerNode1=triggerNodes.get(i);
			for(int j=i+1;j<triggerNodes.size();j++) {
				GraphNode triggerNode2=triggerNodes.get(j);
				if(isContra(triggerNode1, triggerNode2,biddables)) {
//					isUnused=true;
					reason="Trigger: "+triggerNode1.getLabel()+" "+triggerNode2.getLabel()+" has a logical contradiction.";
					System.out.println(reason);
					return reason;
				}
			}
		}
		////////////trigger是state，但是非初始状态且没有条件触发
		for(GraphNode triggerNode:triggerNodes) {
			String[] attrVal=RuleService.getTriAttrVal(triggerNode.getLabel(), biddables);
//			String[] attrVal=RuleService.getTriAttrVal(triggerNode.getLabel());
			/////Bulb_0.bon
			if(attrVal[1].equals(".")) {
//				boolean hasDevice=false;
				for(DeviceDetail device:devices) {
					if(device.getDeviceName().equals(attrVal[0])) {
						///Bulb_0
//						hasDevice=true;
						for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
							if(stateActionValue[0].equals(attrVal[2])) {
								///bon
								if(!stateActionValue[2].equals("0")) {
									///非初始状态
									boolean hasPreAction=false;
									for(GraphNodeArrow pArrow:triggerNode.getpNodeList()) {
										if(pArrow.getColor().equals("red")) {
											String pAction=pArrow.getGraphNode().getLabel();
											pAction=pAction.substring(pAction.indexOf(".")).substring(1);
											if(pAction.equals(stateActionValue[1])) {
												hasPreAction=true;
												//////////看这个action的rule有没有能触发的
												GraphNode actionNode=pArrow.getGraphNode();
												boolean cantriggered=false;
												for(GraphNodeArrow ppArrow:actionNode.getpNodeList()) {
													if(ppArrow.getGraphNode().getShape().equals("hexagon")) {
														GraphNode pruleNode=ppArrow.getGraphNode();
														if(isUnused(pruleNode, devices,biddables).equals("")) {
															cantriggered=true;
															break;
														}
													}
												}
												if(!cantriggered) {
//													isUnused=true;
													reason="No rule can satisfy "+triggerNode.getLabel()+".";
													System.out.println(reason);
													return reason;
												}
												break;
											}
										}
										
									}
									if(!hasPreAction) {
//										isUnused=true;
										reason="No rule can satisfy "+triggerNode.getLabel()+".";
										System.out.println(reason);
										return reason;
									}
								}
								break;
							}
						}
						
						break;
					}
				}
			}
		}
		return reason;
	}
	
	///////看trigger和trigger之间是否矛盾
 	public static boolean isContra(GraphNode triggerNode1,GraphNode triggerNode2,List<BiddableType> biddables) {
		String trigger1=triggerNode1.getLabel();
		String trigger2=triggerNode2.getLabel();
		String[] attrVal1=RuleService.getTriAttrVal(trigger1, biddables);
		String[] attrVal2=RuleService.getTriAttrVal(trigger2, biddables);

		if(attrVal1[0].equals(attrVal2[0])) {
			if(attrVal1[1].equals(".")) {
				////同一设备不同状态
				if(!attrVal2[2].equals(attrVal1[2])) {
					return true;
				}
			}else {
				//同一属性的取值矛盾
				double val1=Double.parseDouble(attrVal1[2]);
				double val2=Double.parseDouble(attrVal2[2]);
				if(attrVal1[1].equals("=")) {
					if(attrVal2[1].equals("=")) {
						if(!attrVal2[2].equals(attrVal1[2])) {
							return true;
						}
					}else if(attrVal2[1].contains(">")) {
						if(val2>=val1) {
							return true;
						}
					}else if(attrVal2[1].contains("<")) {
						if(val2<=val1) {
							return true;
						}
					}else if(attrVal2[1].equals("!=")) {
						if((int) val2==(int)val1) {
							return true;
						}
					}
				}else if(attrVal2[1].equals("=")) {
					if(attrVal1[1].contains(">")) {
						if(val1>=val2) {
							return true;
						}
					}else if(attrVal1[1].contains("<")) {
						if(val1<=val2) {
							return true;
						}
					}else if(attrVal1[1].equals("!=")) {
						if((int) val2==(int)val1) {
							return true;
						}
					}
				}else if(attrVal1[1].contains(">")) {
					if(attrVal2[1].contains("<")) {
						if(val1>=val2) {
							return true;
						}
					}
				}else if(attrVal1[1].contains("<")) {
					if(attrVal2[1].contains(">")) {
						if(val1<=val2) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	////////////////////获得冗余的规则
	public static List<GraphNode> getRedundant(GraphNode ruleNode,List<GraphNode> graphNodes) {
		List<GraphNode> redundantNodes=new ArrayList<GraphNode>();
		List<GraphNode> otherCauseRuleNodes=new ArrayList<GraphNode>();
		redundantNodes.add(ruleNode);
		boolean allActionHasOtherRule=true;
		first:
		for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
			GraphNode actionNode=cArrow.getGraphNode();
			boolean existOtherRule=false;
			for(GraphNodeArrow pArrow:actionNode.getpNodeList()) {
				/////////还有其他规则能发起该action		
				if(!pArrow.getGraphNode().getShape().equals("hexagon")) {
					continue;
				}
				if(!pArrow.getGraphNode().getName().equals(ruleNode.getName())) {
					existOtherRule=true;
					GraphNode otherRuleNode=pArrow.getGraphNode();
					otherCauseRuleNodes.add(otherRuleNode);
					if(containActionNode(ruleNode, otherRuleNode)) {
						/////////////otherRule包含rule的所有action
						List<GraphNode> pathRuleLists=canTraceBack(ruleNode, otherRuleNode,graphNodes);
						if(pathRuleLists!=null) {
							////////////且otherRule的triggers都能回溯到ruleNode的triggers
							/////说明是冗余的
							redundantNodes.addAll(pathRuleLists);							
							break first;
						}
					}
				}				
			}
			if(!existOtherRule) {
				//////////////如果没有其他规则能发起该rule的某个action，那这条规则不会冗余
				allActionHasOtherRule=false;
				break;
			}
		}
		if(allActionHasOtherRule&&redundantNodes.size()==1) {
			////如果其action都能被其他一组rule执行，但是没有一条与它冗余
			/////则看这一组规则各自的triggers能否回溯到该rule的triggers
			for(GraphNode otherRuleNode:otherCauseRuleNodes) {
				List<GraphNode> pathRuleList=canTraceBack(ruleNode, otherRuleNode,graphNodes);
				if(pathRuleList!=null) {
					redundantNodes.addAll(pathRuleList);
				}
			}
		}
		if(redundantNodes.size()>1) {
			///////在上述基础上看其他一组规则能否执行这条规则的所有actions
			for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
				boolean existActionNode=false;
				second:
				for(int i=1;i<redundantNodes.size();i++) {
					for(GraphNodeArrow recArrow:redundantNodes.get(i).getcNodeList()) {
						if(cArrow.getGraphNode().getName().equals(recArrow.getGraphNode().getName())) {
							existActionNode=true;
							break second;
						}
					}
				}
				if(!existActionNode) {
					redundantNodes.clear();
					redundantNodes.add(ruleNode);
					break;
				}
			}
			
		}
		return redundantNodes;
	}
		
	public static List<GraphNode> canTraceBack(GraphNode ruleNode,GraphNode otherRuleNode,List<GraphNode> graphNodes){
		List<GraphNode> ruleList=new ArrayList<GraphNode>();
		List<GraphNode> triggerNodes=new ArrayList<GraphNode>();
		for(GraphNodeArrow pArrow:ruleNode.getpNodeList()) {
			triggerNodes.add(pArrow.getGraphNode());
		}
		for(GraphNode node:graphNodes) {
			if(node.flag) {
				node.flag=false;
			}
		}
		////otherRuleNode的trigger是不是都能回溯到ruleNode的trigger
		////trigger1 -> ruleNode ; trigger2 -> otherRuleNode
		////看trigger2==trigger1? 或者 trigger2 -> -> ->trigger1?
		for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
			GraphNode triggerNode=pArrow.getGraphNode();
			Queue<GraphNode> nodeQueue=new LinkedList<GraphNode>();
			nodeQueue.add(triggerNode);
			triggerNode.flag=true;
			boolean canTraceTo=false;
			while(!nodeQueue.isEmpty()) {
				GraphNode node=nodeQueue.poll();
				if(triggerNodes.contains(node)) {
					canTraceTo=true;
					break;
				}
				for(GraphNodeArrow nodepArrow:node.getpNodeList()) {
					GraphNode pNode=nodepArrow.getGraphNode();
					if(nodepArrow.getStyle()==""&&pNode.getShape().indexOf("doubleoctagon")<0&&!pNode.flag) {
						if(pNode.getShape().indexOf("hexagon")>=0 && !pNode.getName().equals(ruleNode.getName())) {
							List<GraphNode> pRuleList=new ArrayList<GraphNode>();
							
							if((pRuleList=canTraceBack(ruleNode, pNode, graphNodes))!=null) {
								ruleList.addAll(pRuleList);
							}
						}else {
							nodeQueue.add(pNode);
							pNode.flag=true;
						}
					}
				}
				
			}
			if(!canTraceTo&&ruleList.size()==0) {
				return null;
			}
			
		}		
		ruleList.add(otherRuleNode);		
		return ruleList;
	}
	
	
	///////////otherRule的action是否包含rule的action
	public static boolean containActionNode(GraphNode ruleNode,GraphNode otherRuleNode) {
		boolean contain=true;
		for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
			GraphNode actionNode=cArrow.getGraphNode();
			boolean exist=false;
			for(GraphNodeArrow othercArrow:otherRuleNode.getcNodeList()) {
				if(othercArrow.getGraphNode().getName().equals(actionNode.getName())) {
					exist=true;
					break;
				}
			}
			if(!exist) {
				contain=false;
			}
		}
		return contain;
	}
	
	//////////////////获得incompleteness
	public static List<DeviceDetail> getIncompleteness(List<DeviceDetail> devices,List<Action> actions) {
		List<DeviceDetail> cannotOffDevices=new ArrayList<DeviceDetail>();
		for(DeviceDetail device:devices) {
			boolean canOn=false;
			String offAction="";
			for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
				/////////看该设备能否开
				if(Integer.parseInt(stateActionValue[2])>0) {
					/////获得该设备非初始状态的action
					String action=device.getDeviceName()+"."+stateActionValue[1];
					for(Action act:actions) {
						if(act.action.equals(action)) {
							canOn=true;
						}
					}
				}else {
					/////////获得该设备初始状态的action
					offAction=device.getDeviceName()+"."+stateActionValue[1];
				}
			}
			if(canOn) {
				boolean canOff=false;
				for(Action act:actions) {
					////看有没有action能关闭该设备
					if(act.action.equals(offAction)) {
						canOff=true;
						break;
					}
				}
				if(!canOff) {
					cannotOffDevices.add(device);
				}
			}
		}
		return cannotOffDevices;
	}
	
	//////////////////////获得IFD节点
	public static List<GraphNode> getIFDNode(String ifdFileName,String ifdPath) {
		String dotPath=ifdPath+ifdFileName;
		List<GraphNode> graphNodes=new ArrayList<GraphNode>();
		try(BufferedReader br=new BufferedReader(new FileReader(dotPath))){
			List<String> strings=new ArrayList<String>();
			String str="";
			while((str=br.readLine())!=null) {
				//获得各节点
				strings.add(str);
				if(str.indexOf("[")>0) {
					if(str.indexOf("->")<0) {
						///如action1[label="Bulb.turn_bulb_on",shape="record",style="filled",fillcolor="beige"]
						///trigger1[label="temperature<=15",shape="oval",style="filled",fillcolor="lightpink"]
						GraphNode graphNode=new GraphNode();
						String nodeName=str.substring(0, str.indexOf("[")).trim();
						///action1
						graphNode.setName(nodeName);
						String attr=str.substring(str.indexOf("["), str.indexOf("]")).substring("[".length());
						///label="Bulb.turn_bulb_on",shape="record",style="filled",fillcolor="beige"
						String[] features=attr.split(",");
						for(String feature:features) {
							String[] featureName=feature.split("=\"");
							featureName[1]=featureName[1].replaceAll("\"", "");
							if(featureName[0].equals("shape")) {
								graphNode.setShape(featureName[1]);
							}
							if(featureName[0].equals("label")) {
								graphNode.setLabel(featureName[1]);
							}
							if(featureName[0].equals("fillcolor")) {
								graphNode.setFillcolor(featureName[1]);
							}
						}
						graphNodes.add(graphNode);
					}
					
				}
			}
			
			for(String string:strings) {
				if(string.indexOf("->")>0) {
					////////如trigger9->trigger1[color="red",fontsize="18"]
					String arrow="";
					String attrbutes="";
					String[] features=null;
					if(string.indexOf("[")>0) {
						//trigger9->trigger1
						arrow=string.substring(0, string.indexOf("["));
						//color="red",fontsize="18"
						attrbutes=string.substring(string.indexOf("["), string.indexOf("]")).substring("[".length());
						features=attrbutes.split(",");
					}else {
						arrow=string;
					}
					//trigger9->trigger1
					String[] nodes=arrow.split("->");
					for(int i=0;i<nodes.length;i++) {            
						nodes[i]=nodes[i].trim();
					}
					/////获得前后关系的两个节点的边
					for(int i=0;i<graphNodes.size();i++) {
						if(nodes[0].equals(graphNodes.get(i).getName())){
							//先找到trigger9
							for(int j=0;j<graphNodes.size();j++) {
								if(nodes[1].equals(graphNodes.get(j).getName())){
								//再找到trigger1	
									GraphNodeArrow pNode=new GraphNodeArrow();
									GraphNodeArrow cNode=new GraphNodeArrow();
									pNode.setGraphNode(graphNodes.get(i));
									cNode.setGraphNode(graphNodes.get(j));
									if(features!=null) {
										for(String feature:features) {
											String[] featureValue=feature.split("=");
											if(featureValue[0].equals("label")) {
												String label=featureValue[1].replaceAll("\"", "");
												pNode.setLabel(label);
												cNode.setLabel(label);
											}
											if(featureValue[0].equals("style")) {
												String style=featureValue[1].replaceAll("\"", "");
												pNode.setStyle(style);
												cNode.setStyle(style);
											}
											if(featureValue[0].equals("color")) {
												String color=featureValue[1].replaceAll("\"", "");
												pNode.setColor(color);
												cNode.setColor(color);
											}
										}
									}
									graphNodes.get(i).addcNodeList(cNode);
									graphNodes.get(j).addpNodeList(pNode);
									break;
								}
							}
							break;
						}
					}
				}
			}
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return graphNodes;
	}

	
	/////////////////生成IFD
	public static void generateIFD(List<Rule> rules,String ifdFileName,String ifdPath,List<DeviceDetail> devices,List<SensorType> sensors,List<BiddableType> biddables) throws IOException {
//		GetTemplate parse=new GetTemplate();

		
		//-------------------写dot文件-----------------------------------
		
		String dotPath=ifdPath+ifdFileName;
		StringBuilder sb=new StringBuilder();

		
		sb.append("digraph infoflow{\r\n");
		sb.append("rankdir=LR;\r\n");
		sb.append("\r\n");

		///////////////////生成sensor节点//////////////////
		sb.append("///////////////sensors////////////////\r\n");
		for(SensorType sensor:sensors) {
			String sensorDot=sensor.getName()+"[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"azure3\"]";
			sb.append(sensorDot+"\r\n");

		}
		///////////////////////////////////////////////////
		sb.append("\r\n");
		
		//////////////生成controlled device节点////////////////
		sb.append("//////////////controlled devices//////////////\r\n");	
		for(DeviceDetail device:devices) {
			String controlledDot=device.getDeviceName()+"[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"darkseagreen1\"]";
			sb.append(controlledDot+"\r\n");
		}
		
		//////////////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("\r\n");
		
		/////////////////////生成rule节点////////////////////
		sb.append("////////////////////rulesNum/////////////////////\r\n");
		sb.append("\r\n");
		for(Rule rule:rules) {
			String ruleDot=rule.getRuleName()+"[shape=\"hexagon\",style=\"filled\",fillcolor=\"lightskyblue\"]";
			sb.append(ruleDot+"\r\n");
		}
		//////////////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("\r\n");
		
		///获得所有trigger和action
		//getTriggers,getActions
		List<Trigger> triggers=RuleService.getAllTriggers(rules,sensors,biddables);
		List<Action> actions=RuleService.getAllActions(rules, devices);
		
		
		/////////////////////////生成action节点/////////////////////////////
		/////////////////////////rule->action  action->device//////////////
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("////////////////////actions/////////////////////\r\n");
		for(Action action:actions) {
			//action节点
			String actionDot=action.actionNum+"[label=\""+action.action+"\",shape=\"record\",style=\"filled\",fillcolor=\"beige\"]";
			sb.append(actionDot+"\r\n");
			for(Rule actRule:action.rules) {
				//rule->action
				String ruleToActionDot=actRule.getRuleName()+"->"+action.actionNum;
				sb.append(ruleToActionDot+"\r\n");
			}
			//action->device
			String actionToDevice=action.device+"->"+action.actionNum+"[color=\"lemonchiffon3\"]";
			sb.append(actionToDevice+"\r\n");
		}
		

		/////////////////////////生成trigger节点//////////////////////////////////////
		/////////////////////////trigger->rule  device/sensor->trigger//////////////
		sb.append("\r\n");
		sb.append("////////////////////triggers/////////////////////\r\n");
		for(Trigger trigger:triggers) {
			//trigger节点
			String triggerDot=trigger.triggerNum+"[label=\""+trigger.trigger+"\",shape=\"oval\",style=\"filled\",fillcolor=\"lightpink\"]";
			sb.append(triggerDot+"\r\n");
			//trigger->rule
			for(Rule triRule:trigger.rules) {
				String triggerToRuleDot=trigger.triggerNum+"->"+triRule.getRuleName();
				sb.append(triggerToRuleDot+"\r\n");		
			}
			//sensor/device ->trigger
			String deviceToTriggerDot=trigger.device+"->"+trigger.triggerNum+"[color=\"lightpink\"]";
			sb.append(deviceToTriggerDot+"\r\n");
			
			//trigger受到的影响			
			if(trigger.attrVal[1].equals(".")) {
				
				//action->trigger
				for(Action action:actions) {
					if(trigger.device.equals(action.device)
							&& trigger.attrVal[2].equals(action.toState)) {
						String actionToTriggerDot=action.actionNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
						sb.append(actionToTriggerDot+"\r\n");
					}
				}
			}else {
				//trigger->trigger   相同属性
				if(trigger.attrVal[1].indexOf(">")>=0) {
					for(Trigger otherTrigger:triggers) {
						if(otherTrigger!=trigger 
								&& otherTrigger.attrVal[0].equals(trigger.attrVal[0])
								&& otherTrigger.attrVal[1].indexOf(">")>=0) {
							Double triVal=Double.parseDouble(trigger.attrVal[2]);
							Double othTriVal=Double.parseDouble(otherTrigger.attrVal[2]);
							if(triVal<othTriVal) {
								/////如temperature>30 -> temperature>25 
								String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
								sb.append(triggerToTriggerDot+"\r\n");
							}else if(triVal.toString().equals(othTriVal.toString())) {
								if(trigger.attrVal[1].equals(">=")) {
									////如temperature>30 -> temperature>=30
									String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
									sb.append(triggerToTriggerDot+"\r\n");
								}
							}
						}
					}
				}else if(trigger.attrVal[1].indexOf("<")>=0) {
					//trigger->trigger   相同属性
					for(Trigger otherTrigger:triggers) {
						if(otherTrigger!=trigger 
								&& otherTrigger.attrVal[0].equals(trigger.attrVal[0])
								&& otherTrigger.attrVal[1].indexOf("<")>=0) {
							Double triVal=Double.parseDouble(trigger.attrVal[2]);
							Double othTriVal=Double.parseDouble(otherTrigger.attrVal[2]);
							if(triVal>othTriVal) {
							/////如temperature<25 -> temperature<30 
								String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
								sb.append(triggerToTriggerDot+"\r\n");
							}else if(triVal.toString().equals(othTriVal.toString())
									&& trigger.attrVal[1].equals(otherTrigger.attrVal[1])) {
								if(trigger.attrVal[1].equals("<=")) {
								////如temperature<30 -> temperature<=30
									String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
									sb.append(triggerToTriggerDot+"\r\n");
								}
							}
						}
					}
				}
				
				//biddableToTrigger
			}
		}
		
		
		/////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("}\r\n");
//		parse.write(graphvizFile, "", true);
//		parse.write(graphvizFile, "}", true);
		BufferedWriter bw=new BufferedWriter(new FileWriter(dotPath));
		bw.write(sb.toString());
		bw.close();
	}
}
