package com.example.demo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.example.demo.bean.*;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

//import com.example.demo.bean.DeviceType.StateEffect;
import com.example.demo.bean.IFDGraph.GraphNode;
import com.example.demo.bean.IFDGraph.GraphNodeArrow;

@Service
public class StaticAnalysisService {	
	

	/**
	 * 静态分析，验证 不可触发规则、冗余规则、循环规则、规则不完整
	 * */
	public static StaticAnalysisResult getStaticAnalysisResult(List<Rule> rules,String ifdFilePath,String ifdFileName,InstanceLayer instanceLayer) throws IOException {

		HashMap<String,Instance> instanceHashMap=InstanceLayerService.getInstanceMap(instanceLayer);
		HashMap<String,Rule> ruleHashMap=AnalysisService.getRuleHashMap(rules);
		///删除重复的
		List<Rule>  newRules=deleteRepeat(rules);
		////重新生成IFD图
		long t9=System.currentTimeMillis();
		HashMap<String,Trigger> triggerHashMap=SystemModelGenerationService.getTriggerMapFromRules(rules,instanceLayer);
		HashMap<String,Action> actionHashMap=SystemModelGenerationService.getActionMapFromRules(rules);
		generateIFD(triggerHashMap,actionHashMap,newRules,instanceLayer,instanceHashMap,ifdFileName,ifdFilePath);
		long t10=System.currentTimeMillis();
		System.out.println("信息流图生成时间："+(t10-t9));
		////解析ifd
		List<GraphNode> graphNodes=parseIFDAndGetIFDNode(ifdFilePath,ifdFileName);
		HashMap<String,GraphNode> graphNodeHashMap=AnalysisService.getGraphNodeHashMap(graphNodes);
		///找不能触发的规则
		long t1=System.currentTimeMillis();
		List<UnusedRuleAndReason> unusedRuleAndReasons=new ArrayList<>();
		for (int i=newRules.size()-1;i>=0;i--){
			Rule rule=newRules.get(i);
			GraphNode ruleNode=graphNodeHashMap.get(rule.getRuleName());
			////找
			String reason=ruleUnusedReason(ruleNode,instanceLayer.getDeviceInstances());
			if (!reason.equals("")){
				UnusedRuleAndReason unusedRuleAndReason=new UnusedRuleAndReason();
				unusedRuleAndReason.setUnusedRule(rule);
				unusedRuleAndReason.setReason(reason);
				unusedRuleAndReasons.add(unusedRuleAndReason);
				newRules.remove(i);
			}
		}
		long t2=System.currentTimeMillis();
		System.out.println("不可触发规则检测时间："+(t2-t1));

		////寻找loop
		long t7=System.currentTimeMillis();
		List<List<Rule>> loopRules=new ArrayList<>();
		for (Rule rule:rules){
			GraphNode ruleNode=graphNodeHashMap.get(rule.getRuleName());
			List<List<GraphNode>> loopNodeLists=getLoop(ruleNode);
			for (List<GraphNode> loopNodes:loopNodeLists){
				List<Rule> loopRule=new ArrayList<>();
				for (GraphNode node:loopNodes){
					loopRule.add(ruleHashMap.get(node.getName()));
				}
				boolean exist=false;
				for (List<Rule> existLoopRule:loopRules){
					if (sameRuleList(existLoopRule,loopRule)){
						exist=true;
						break;
					}
				}
				if (exist){
					continue;
				}
				loopRules.add(loopRule);
			}
//			if (loopNodeLists.size()>0){
//				///存在loop
//				List<Rule> loopRule=new ArrayList<>();
//				for (GraphNode node:loopNodes){
//					loopRule.add(ruleHashMap.get(node.getName()));
//				}
//				boolean exist=false;
//				for (List<Rule> existLoopRule:loopRules){
//					if (sameRuleList(existLoopRule,loopRule)){
//						exist=true;
//						break;
//					}
//				}
//				if (exist){
//					continue;
//				}
//				loopRules.add(loopRule);
//			}
		}
		long t8=System.currentTimeMillis();
		System.out.println("循环规则检测时间："+(t8-t7));


		long t3=System.currentTimeMillis();
		List<List<Rule>> redundantRules=new ArrayList<>();
		for (Rule rule:newRules){
			GraphNode ruleNode=graphNodeHashMap.get(rule.getRuleName());
			List<GraphNode> redundantNodes=getRedundant(ruleNode);
			if (redundantNodes.size()>1){
				List<Rule> redundantRule=new ArrayList<>();
				for (GraphNode graphNode:redundantNodes){
					redundantRule.add(ruleHashMap.get(graphNode.getName()));
				}
				redundantRules.add(redundantRule);
			}
		}
		long t4=System.currentTimeMillis();
		System.out.println("冗余规则检测时间："+(t4-t3));

		////重新生成IFD
		triggerHashMap=SystemModelGenerationService.getTriggerMapFromRules(newRules,instanceLayer);
		actionHashMap=SystemModelGenerationService.getActionMapFromRules(newRules);
		generateIFD(triggerHashMap,actionHashMap,newRules,instanceLayer,instanceHashMap,ifdFileName,ifdFilePath);
		graphNodes=parseIFDAndGetIFDNode(ifdFilePath,ifdFileName);
		graphNodeHashMap=AnalysisService.getGraphNodeHashMap(graphNodes);
		////找冗余

		////找incompleteness
		long t5=System.currentTimeMillis();
		List<DeviceInstance> cannotOffDevices=getIncomplete(graphNodes,instanceLayer.getDeviceInstances());
		long t6=System.currentTimeMillis();
		System.out.println("规则不完整检测时间："+(t6-t5));
		StaticAnalysisResult staticAnalysisResult=new StaticAnalysisResult();
		staticAnalysisResult.setCannotOffDevices(cannotOffDevices);
		staticAnalysisResult.setUnusedRuleAndReasons(unusedRuleAndReasons);
		staticAnalysisResult.setRedundantRules(redundantRules);
		staticAnalysisResult.setUsableRules(newRules);
		staticAnalysisResult.setLoopRules(loopRules);
		return staticAnalysisResult;
	}

	/**
	 * 判断两条TAP规则是否相同
	 * */
	public static boolean sameRuleList(List<Rule> rules1,List<Rule> rules2){
		if(rules1.size()!= rules2.size()){
			return false;
		}
		for(Rule rule1:rules1){
			boolean exist=false;
			for (Rule rule2:rules2){
				if (rule1.getRuleName().equals(rule2.getRuleName())){
					exist=true;
				}
			}
			if (!exist){
				return false;
			}
		}
		return true;
	}

	public static boolean sameRuleNodeList(List<GraphNode> ruleNodes1, List<GraphNode> ruleNodes2){
		if(ruleNodes1.size()!= ruleNodes2.size()){
			return false;
		}
		for(GraphNode ruleNode1:ruleNodes1){
			boolean exist=false;
			for (GraphNode ruleNode2:ruleNodes2){
				if (ruleNode1.getName().equals(ruleNode2.getName())){
					exist=true;
				}
			}
			if (!exist){
				return false;
			}
		}
		return true;
	}

	/**
	 * 循环规则
	 * 判断是否存在loop
	 * */
	public static List<List<GraphNode>> getLoop(GraphNode ruleNode){
		List<List<GraphNode>> loopNodesList=new ArrayList<>();
//		List<GraphNode> loopNodes=new ArrayList<>();
//		loopNodes.add(ruleNode);
		for (GraphNodeArrow cNodeArrow: ruleNode.getcNodeArrowList()){
			///actionNode
			GraphNode actionNode= cNodeArrow.getGraphNode();
			if(actionNode.getShape().equals("record")){
				///看后续的triggerNode
				for (GraphNodeArrow actionCNodeArrow: actionNode.getcNodeArrowList()){
					if (!actionCNodeArrow.getStyle().equals("")){
						///只看红色实线
						continue;
					}
					///triggerNode
					GraphNode triggerNode=actionCNodeArrow.getGraphNode();
					if (triggerNode.getShape().equals("oval")){
						///找到后继ruleNode
						for (GraphNodeArrow triggerCNodeArrow: triggerNode.getcNodeArrowList()){
							if (!(triggerCNodeArrow.getColor().equals("")||triggerCNodeArrow.getColor().equals("black"))){
								///只看后继ruleNode
								continue;
							}
							GraphNode otherRuleNode=triggerCNodeArrow.getGraphNode();
							///判断rule能否被otherRule触发，且otherRule能被rule触发
							///otherRule能被rule触发
							List<GraphNode> pathRuleNodes=canBeTriggeredByRuleNode(ruleNode,otherRuleNode,"",new ArrayList<>());
							///rule能被otherRule触发
							List<GraphNode> versePathRuleNodes=canBeTriggeredByRuleNode(otherRuleNode,ruleNode,"",new ArrayList<>());
							if (pathRuleNodes.size()>0&&pathRuleNodes.contains(ruleNode)&&versePathRuleNodes.size()>0&&versePathRuleNodes.contains(otherRuleNode)){
								///rule能被otherRule触发，且otherRule能被rule触发
								///同时还得要能反向获得loop
//								List<GraphNode> verseLoopNodes=getLoop(otherRuleNode);
//								if (sameRuleNodeList(pathRuleNodes,versePathRuleNodes)){
//									loopNodesList.add(pathRuleNodes);
//								}
								List<GraphNode> loopNodes=new ArrayList<>();
								for (GraphNode graphNode:pathRuleNodes){
									if (!loopNodes.contains(graphNode)){
										loopNodes.add(graphNode);
									}
								}
								for (GraphNode graphNode:versePathRuleNodes){
									if (!loopNodes.contains(graphNode)){
										loopNodes.add(graphNode);
									}
								}
								loopNodesList.add(loopNodes);
							}
						}
					}
				}
			}
		}
		return loopNodesList;
	}
	
	////////////////////获得不正确的规则
//	public static List<ErrorReason> getIncorrect(List<Rule> rules,List<DeviceDetail> devices){
//		List<ErrorReason> incorrectReason=new ArrayList<ErrorReason>();
//		for(Rule rule:rules) {
//
//			String reason="";
//			boolean incorrect=false;
//			for(String action:rule.getAction()) {
//				////看一条规则的action是不是存在问题   Bulb_0.turn_bulb_on
//				String actionPulse="";
//				String deviceName="";
//				if(action.indexOf(".")>0) {
//					actionPulse=action.substring(action.indexOf(".")).substring(1);		////////turn_bulb_on
//					deviceName=action.substring(0, action.indexOf("."));       /////////Bulb_0
//				}
//				boolean existAction=false;
//				for(DeviceDetail device:devices) {
//					if(device.getDeviceName().equals(deviceName)) {
//						////找到这个设备
//						for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
//							if(stateActionValue[1].equals(actionPulse)) {
//								////看action是否是对的
//								existAction=true;
//								break;
//							}
//						}
//						break;
//					}
//				}
//				if(!existAction) {
//					incorrect=true;
//					reason+=action.trim()+" ";   //////给出原因
//				}
//			}
//			if(incorrect) {
//				ErrorReason er=new ErrorReason();
//				er.rule=rule;
//				er.reason=reason+"cannot be executed!";
//				incorrectReason.add(er);
//				System.out.println(reason);
//			}
//
//		}
//		return incorrectReason;
//	}

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
/**
 * 不可触发规则判断
 * 判断一条规则是否能发生，即判断其triggers是否都能满足
 * 如果不能触发，则给出不能触发的原因，反之原因为""
 * */
	public static String ruleUnusedReason(GraphNode ruleNode,List<DeviceInstance> deviceInstances){
		ruleNode.setTraversed(true);
		List<GraphNode> triggerNodes=new ArrayList<GraphNode>();
		String reason="";
		for (GraphNodeArrow triggerArrow:ruleNode.getpNodeArrowList()){
			///triggerNode
			GraphNode triggerNode=triggerArrow.getGraphNode();   ///rule节点前面的节点为trigger节点
			///先判断是否合法
			boolean legal=false;
			if (!triggerNode.getRelatedInstanceAndColor()[1].equals("")){
				legal=true;
			}
//			for(GraphNodeArrow ppArrow:triggerNode.getpNodeArrowList()) {
//				if(ppArrow.getColor().indexOf("lightpink")>=0) {
//					legal=true;
//					break;
//				}
//			}
			if(!legal) {
				//////////trigger不合法
//				correct=false;
				reason="Trigger: "+triggerNode.getLabel()+" illegal.";
//				System.out.println(reason);
//				isUnused=true;
				ruleNode.setTraversed(false);
				return reason;
			}
			triggerNodes.add(triggerNode);
		}
		///////////trigger之间矛盾
		for(int i=0;i<triggerNodes.size();i++) {
			GraphNode triggerNode1=triggerNodes.get(i);
			for(int j=i+1;j<triggerNodes.size();j++) {
				GraphNode triggerNode2=triggerNodes.get(j);
				if(isContra(triggerNode1, triggerNode2)) {
//					isUnused=true;
					reason="Trigger: "+triggerNode1.getLabel()+" "+triggerNode2.getLabel()+" has a logical contradiction.";
//					System.out.println(reason);
					ruleNode.setTraversed(false);
					return reason;
				}
			}
		}
		////////////trigger是state，但是非初始状态且没有条件触发
		for (GraphNode triggerNode:triggerNodes){
			String[] triggerForm=RuleService.getTriggerForm(triggerNode.getLabel());
			if (triggerForm[1].contains(".")){
				///看是不是某个设备
				for (DeviceInstance deviceInstance:deviceInstances){
					if (deviceInstance.getInstanceName().equals(triggerForm[0])){
						///是设备状态
						///看是不是初始状态
						for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceInstance.getDeviceType().getStateSyncValueEffects()){
							if (stateSyncValueEffect.getStateName().equals(triggerForm[2])){
								///找到对应状态
								if (!stateSyncValueEffect.getValue().equals("0")){
									////不为0则不是初始状态
									///看是否有前驱规则
									boolean hasPreRule=false;
									for (GraphNodeArrow actionArrow:triggerNode.getpNodeArrowList()){
										GraphNode actionNode=actionArrow.getGraphNode();
										if (actionNode.getShape().equals("record")){
											////看触发该action的规则能否被触发
											boolean canRuleBeTriggered=false;
											for (GraphNodeArrow pRuleArrow:actionNode.getpNodeArrowList()){
												GraphNode pRuleNode= pRuleArrow.getGraphNode();
												if(pRuleNode.getShape().equals("hexagon")&&!pRuleNode.isTraversed()){
													hasPreRule=true;
													reason=ruleUnusedReason(pRuleNode,deviceInstances);
													if (reason.equals("")){
														canRuleBeTriggered=true;
														break;
													}
												}

											}
											if (!canRuleBeTriggered){
												///只要有规则能触发该triggerNode，则该trigger可满足，反之不能
												reason="No rule can satisfy "+triggerNode.getLabel()+".";
//												System.out.println(reason);
												ruleNode.setTraversed(false);
												return reason;
											}
										}
									}
									if (!hasPreRule){
										////如果没有前驱规则，则无法被触发
										reason="No rule can satisfy "+triggerNode.getLabel()+".";
//										System.out.println(reason);
										ruleNode.setTraversed(false);
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
		ruleNode.setTraversed(false);
		return reason;
	}
	///////////判断trigger之间矛盾
	public static boolean isContra(GraphNode triggerNode1,GraphNode triggerNode2){
		String[] triggerForm1=RuleService.getTriggerForm(triggerNode1.getLabel());
		String[] triggerForm2=RuleService.getTriggerForm(triggerNode2.getLabel());
		if (triggerForm1[0].equals(triggerForm2[0])){
			////只有相同属性或实体才可能冲突
			if (triggerForm1[1].equals(".")){
				if (!triggerForm1[2].equals(triggerForm2[2])){
					////相同实体不同状态，冲突
					return true;
				}
			}else {
				//同一属性的取值矛盾 attribute<(<=,>,>=)value
				double val1=Double.parseDouble(triggerForm1[2]);
				double val2=Double.parseDouble(triggerForm2[2]);
				if(triggerForm1[1].contains(">")) {
					if(triggerForm2[1].contains("<")) {
						if(val1>=val2) {
							return true;
						}
					}
				}else if(triggerForm1[1].contains("<")) {
					if(triggerForm2[1].contains(">")) {
						if(val1<=val2) {
							return true;
						}
					}
				}
			}

		}
		return false;
	}

	/**
	 * 冗余：一条规则r1与另一条规则r2冗余表明，
	 * r1的trigger满足的情况下，r2的trigger都能满足，
	 *且r1的action蕴含于r2的action中
	 * */
////////////////////获得冗余的规则
	public static List<GraphNode> getRedundant(GraphNode ruleNode) {
		List<GraphNode> redundantNodes=new ArrayList<GraphNode>();
		List<GraphNode> otherCauseRuleNodes=new ArrayList<GraphNode>();
		redundantNodes.add(ruleNode);

		boolean allActionHasOtherRule=true;
		first:
		for(GraphNodeArrow actionArrow:ruleNode.getcNodeArrowList()) {
			GraphNode actionNode=actionArrow.getGraphNode();
			boolean existOtherRule=false;
			for(GraphNodeArrow ruleArrow:actionNode.getpNodeArrowList()) {
				GraphNode otherRuleNode=ruleArrow.getGraphNode();
				/////////还有其他规则能发起该action
				if(otherRuleNode.getShape().equals("hexagon")&&!otherRuleNode.getName().equals(ruleNode.getName())) {
					existOtherRule=true;
					otherCauseRuleNodes.add(otherRuleNode);
					if(containActionNode(ruleNode, otherRuleNode)) {
						/////////////otherRule包含rule的所有action
						///看other规则是否最终能被该规则触发
						List<GraphNode> pathRuleList=canBeTriggeredByRuleNode(ruleNode,otherRuleNode,"redundant",new ArrayList<>());
						if(pathRuleList.size()>0) {
							////////////且otherRule的triggers都能回溯到ruleNode的triggers
							/////说明是冗余的
							for (GraphNode node:pathRuleList){
								if (!redundantNodes.contains(node)){
									redundantNodes.add(node);
								}
							}
//							redundantNodes.addAll(pathRuleLists);
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
				List<GraphNode> pathRuleList=canBeTriggeredByRuleNode(ruleNode,otherRuleNode,"redundant",new ArrayList<>());
//				redundantNodes.addAll(pathRuleList);
				for (GraphNode node:pathRuleList){
					if (!redundantNodes.contains(node)){
						redundantNodes.add(node);
					}
				}
			}
		}
		if(redundantNodes.size()>1) {
			///////在上述基础上看其他一组规则能否执行这条规则的所有actions
			for(GraphNodeArrow actionArrow:ruleNode.getcNodeArrowList()) {
				boolean existActionNode=false;
				second:
				for(int i=1;i<redundantNodes.size();i++) {
					for(GraphNodeArrow reActionArrow:redundantNodes.get(i).getcNodeArrowList()) {
						if(actionArrow.getGraphNode().getName().equals(reActionArrow.getGraphNode().getName())) {
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
	///看otherRuleNode的trigger是否都能在ruleNode触发的前提下触发
	public static List<GraphNode> canBeTriggeredByRuleNode(GraphNode ruleNode,GraphNode otherRuleNode,String function,List<GraphNode> cannotBeTriggeredRuleNodes){

		List<GraphNode> pathRuleNodes=new ArrayList<>();
		List<GraphNode> triggerNodes=new ArrayList<>();
		List<GraphNode> actionNodes=new ArrayList<>();
		////获得ruleNode的所有trigger节点
		for(GraphNodeArrow triggerArrow:ruleNode.getpNodeArrowList()) {
			triggerNodes.add(triggerArrow.getGraphNode());
		}
		for (GraphNodeArrow actionArrow:ruleNode.getcNodeArrowList()){
			actionNodes.add(actionArrow.getGraphNode());
		}
		if (function.equals("redundant")){
			ruleNode.setTraversed(true);
		}
		otherRuleNode.setTraversed(true);

		////看otherRule的每个triggerNode是否都能回溯到triggerNodes中的一个
		boolean canAllBeTriggered=true;
		first:
		for (GraphNodeArrow triggerArrow: otherRuleNode.getpNodeArrowList()){
			GraphNode triggerNode= triggerArrow.getGraphNode();
			boolean canBeTriggered=false;
			if (triggerNodes.contains(triggerNode)){
				////直接包含在triggerNodes中
				canBeTriggered=true;
				continue first;
			}

			for (GraphNodeArrow triggerPreArrow: triggerNode.getpNodeArrowList()){
				////前驱节点
				GraphNode preNode=triggerPreArrow.getGraphNode();
				if (preNode.getShape().equals("doubleoctagon")){
					continue ;
				}
				////preNode可能是trigger也可能是action
				if (triggerNodes.contains(preNode)){
					///如果是trigger节点
					/////前驱包含在triggerNodes中
					canBeTriggered=true;
					continue first;
				}
				if (preNode.getShape().equals("record")){
					////如果是action节点
					if (triggerPreArrow.getStyle().equals("")){
						///如果是action，对于静态分析的冗余分析，只考虑实线的action
						for (GraphNodeArrow preRuleArrow:preNode.getpNodeArrowList()){
							GraphNode preRuleNode=preRuleArrow.getGraphNode();
							if (!preRuleNode.isTraversed()&&preRuleNode.getShape().equals("hexagon")&&!cannotBeTriggeredRuleNodes.contains(preRuleNode)){
								////看是否存在前驱规则在该规则触发的情况下触发,非该规则
								List<GraphNode> otherPathRuleNodes= canBeTriggeredByRuleNode(ruleNode,preRuleNode,function,cannotBeTriggeredRuleNodes);
								if (function.equals("redundant")){
									///因为前面的方法会让ruleNode的isTraversed变为false，所以需要重新置为true
									ruleNode.setTraversed(true);
								}

								if (otherPathRuleNodes.size()>0){
									canBeTriggered=true;
									for (GraphNode pathRuleNode:otherPathRuleNodes){
										if (!pathRuleNodes.contains(pathRuleNode)){
											pathRuleNodes.add(pathRuleNode);
										}
									}
									continue first;
								}else {
									///加入不能被触发的规则，到时候就不考虑了
									cannotBeTriggeredRuleNodes.add(preRuleNode);
								}

							}

						}
					}
					if (function.equals("bestScenario")&&triggerPreArrow.getStyle().equals("dashed")){
						/////对于寻找最佳仿真场景的情况，还需要考虑虚线的action
						/**
						 * 查看tj红色实线的后继trigger tm节点（tm与tj涉及相同属性，
						 * 连接同一sensor或device），如果存在tm会触发某条规则Rm，
						 * Rm中存在某个action am，am与ak属于同一设备，且am到tj没有连线，
						 * 则ak无法触发tj，如果tj的所有红色虚线的前驱action 都无法触发tj，则tj不能满足，否则tj能满足
						 * */
						///先找相同设备的其他action
						List<GraphNode> otherActionRelatedRuleNodes=new ArrayList<>();
						for (GraphNodeArrow preArrow:preNode.getpNodeArrowList()){
							GraphNode deviceNode=preArrow.getGraphNode();
							if (deviceNode.getFillcolor().equals("darkseagreen1")){
								///设备节点
								for (GraphNodeArrow deActionArrow: deviceNode.getcNodeArrowList()){
									GraphNode otherActionNode=deActionArrow.getGraphNode();
									if (otherActionNode.getShape().equals("record")){
										////action
										if (!otherActionNode.getLabel().equals(preNode.getLabel())){
											///看是否不指向当前trigger
											boolean pointToTrigger=false;
											for (GraphNodeArrow nextOtherActionArrow: otherRuleNode.getcNodeArrowList()){
												GraphNode nextTriggerNode= nextOtherActionArrow.getGraphNode();
												if (nextTriggerNode.getLabel().equals(triggerNode.getLabel())){
													pointToTrigger=true;
													break;
												}
											}
											if (!pointToTrigger){
												////存在otheAction
												for (GraphNodeArrow otherRuleArrow: otherActionNode.getpNodeArrowList()){
													GraphNode otherActionRuleNode=otherRuleArrow.getGraphNode();
													if (otherActionRuleNode.getShape().equals("hexagon")){
														otherActionRelatedRuleNodes.add(otherActionRuleNode);
													}
												}
											}
										}
									}
								}
								break;
							}
						}
						for (GraphNode ruleNode1:pathRuleNodes){
							for (GraphNode ruleNode2:otherActionRelatedRuleNodes){
								if (ruleNode1.getName().equals(ruleNode2.getName())){
									////会被前面的一些规则所阻止，则该trigger无法被触发
									canAllBeTriggered=false;
									break first;
								}
							}
						}
						/////找后继trigger节点,是当前trigger能直接蕴含的
						for (GraphNodeArrow triggerChArrow:triggerNode.getcNodeArrowList()){
							GraphNode cTriggerNode=triggerChArrow.getGraphNode();
							if (cTriggerNode.getShape().equals("oval")){
								////找该trigger的rule，看这个rule能否触发其他action
								for (GraphNodeArrow cTriggerRuleArrow: cTriggerNode.getcNodeArrowList()){
									GraphNode cTriggerRuleNode=cTriggerRuleArrow.getGraphNode();
									if (cTriggerRuleNode.getShape().equals("hexagon")){
										////rule节点，判断otherActionRelatedRuleNodes能否在该rule前提下触发
										for (GraphNode ruleNode2:otherActionRelatedRuleNodes){
											List<GraphNode> otherPathRuleNodes=canBeTriggeredByRuleNode(cTriggerRuleNode,ruleNode2,"",new ArrayList<>());
											if (otherPathRuleNodes.size()>0){
												//会被该规则阻止，该trigger无法被触发
												canAllBeTriggered=false;
												break first;
											}
										}
									}
								}
							}
						}
						canBeTriggered=true;
						continue first;
					}


				}
			}

			canAllBeTriggered=false;
			break first;
		}
		if (canAllBeTriggered){
			pathRuleNodes.add(otherRuleNode);
		}else {
			pathRuleNodes.clear();
		}
		otherRuleNode.setTraversed(false);
		if (function.equals("redundant")){
			ruleNode.setTraversed(false);
		}
		return pathRuleNodes;
	}
	///////////otherRule的action是否包含rule的action
	public static boolean containActionNode(GraphNode ruleNode,GraphNode otherRuleNode) {
		boolean contain=true;
		for(GraphNodeArrow actionArrow:ruleNode.getcNodeArrowList()) {
			GraphNode actionNode=actionArrow.getGraphNode();
			boolean exist=false;
			for(GraphNodeArrow otherActionArrow:otherRuleNode.getcNodeArrowList()) {
				if(otherActionArrow.getGraphNode().getName().equals(actionNode.getName())) {
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


	/**
	 * 规则不完整
	 * 获得incompleteness
	 * 在删除unused rules之后
	 * 获取设备节点，然后看其action节点是否有关的action
	 * */
	public static List<DeviceInstance> getIncomplete(List<GraphNode> graphNodes,List<DeviceInstance> deviceInstances){
		List<DeviceInstance> cannotOffDevices=new ArrayList<>();
		HashMap<String,List<String>> instanceActionsHashMap=new HashMap<>();

		for (GraphNode graphNode:graphNodes){
			if (graphNode.getShape().equals("record")){
				if (graphNode.getRelatedInstanceAndColor()[1].equals("darkseagreen1")){
					String content=graphNode.getLabel();
					String instanceName=content.substring(0,content.indexOf("."));
					String action=content.substring(content.indexOf(".")+1);
					List<String> actions=instanceActionsHashMap.get(instanceName);
					if (actions==null){
						actions=new ArrayList<>();
						actions.add(action);
						instanceActionsHashMap.put(instanceName,actions);
					}else {
						actions.add(action);
					}
				}
			}
		}
		for (Map.Entry<String,List<String>> instanceActionEntry:instanceActionsHashMap.entrySet()){
			String instanceName=instanceActionEntry.getKey();
			List<String> actions=instanceActionEntry.getValue();
			for (DeviceInstance deviceInstance:deviceInstances){
				if (deviceInstance.getInstanceName().equals(instanceName)){
					////找到对应设备
					boolean existOff=false;  ////判断是否有关闭的action

					for (String action:actions){
						for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceInstance.getDeviceType().getStateSyncValueEffects()){
							if (stateSyncValueEffect.getSynchronisation().equals(action)){
								if (stateSyncValueEffect.getValue().equals("0")){
									existOff=true;
								}
								break;
							}
						}
					}
					if (!existOff){
						///不存在
						cannotOffDevices.add(deviceInstance);
					}
					break;
				}
			}
		}
//		for (GraphNode graphNode:graphNodes){
//			if (graphNode.getFillcolor().equals("darkseagreen1")){
//				///设备节点
//				///看其action节点有没有off的节点
//				DeviceInstance relatedDeviceInstance=new DeviceInstance();
//				for (DeviceInstance deviceInstance:deviceInstances){
//					if(deviceInstance.getInstanceName().equals(graphNode.getName())){
//						relatedDeviceInstance=deviceInstance;
//						break;
//					}
//				}
//				boolean existOff=false;
//				for (GraphNodeArrow actionArrow:graphNode.getcNodeArrowList()){
//					///看是否存在让该设备关闭的action
//					GraphNode actionNode=actionArrow.getGraphNode();
//					if (actionNode.getShape().equals("record")){
//						String content=actionNode.getLabel();
//						String action=content.substring(content.indexOf(".")+1);
//						for (DeviceType.StateSyncValueEffect stateSyncValueEffect:relatedDeviceInstance.getDeviceType().getStateSyncValueEffects()){
//							if (stateSyncValueEffect.getSynchronisation().equals(action)){
//								if (stateSyncValueEffect.getValue().equals("0")){
//									existOff=true;
//								}
//								break;
//							}
//						}
//					}
//				}
//				if (!existOff){
//					cannotOffDevices.add(relatedDeviceInstance);
//				}
//			}
//		}
		return cannotOffDevices;
	}



	

//	////////////////////获得冗余的规则
//	public static List<GraphNode> getRedundant(GraphNode ruleNode,List<GraphNode> graphNodes) {
//		List<GraphNode> redundantNodes=new ArrayList<GraphNode>();
//		List<GraphNode> otherCauseRuleNodes=new ArrayList<GraphNode>();
//		redundantNodes.add(ruleNode);
//		boolean allActionHasOtherRule=true;
//		first:
//		for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
//			GraphNode actionNode=cArrow.getGraphNode();
//			boolean existOtherRule=false;
//			for(GraphNodeArrow pArrow:actionNode.getpNodeList()) {
//				/////////还有其他规则能发起该action
//				if(!pArrow.getGraphNode().getShape().equals("hexagon")) {
//					continue;
//				}
//				if(!pArrow.getGraphNode().getName().equals(ruleNode.getName())) {
//					existOtherRule=true;
//					GraphNode otherRuleNode=pArrow.getGraphNode();
//					otherCauseRuleNodes.add(otherRuleNode);
//					if(containActionNode(ruleNode, otherRuleNode)) {
//						/////////////otherRule包含rule的所有action
//						List<GraphNode> pathRuleLists=canTraceBack(ruleNode, otherRuleNode,null,graphNodes,0);
//						if(pathRuleLists!=null) {
//							////////////且otherRule的triggers都能回溯到ruleNode的triggers
//							/////说明是冗余的
//							redundantNodes.addAll(pathRuleLists);
//							break first;
//						}
//					}
//				}
//			}
//			if(!existOtherRule) {
//				//////////////如果没有其他规则能发起该rule的某个action，那这条规则不会冗余
//				allActionHasOtherRule=false;
//				break;
//			}
//		}
//		if(allActionHasOtherRule&&redundantNodes.size()==1) {
//			////如果其action都能被其他一组rule执行，但是没有一条与它冗余
//			/////则看这一组规则各自的triggers能否回溯到该rule的triggers
//			for(GraphNode otherRuleNode:otherCauseRuleNodes) {
//				List<GraphNode> pathRuleList=canTraceBack(ruleNode, otherRuleNode,null,graphNodes,0);
//				if(pathRuleList!=null) {
//					redundantNodes.addAll(pathRuleList);
//				}
//			}
//		}
//		if(redundantNodes.size()>1) {
//			///////在上述基础上看其他一组规则能否执行这条规则的所有actions
//			for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
//				boolean existActionNode=false;
//				second:
//				for(int i=1;i<redundantNodes.size();i++) {
//					for(GraphNodeArrow recArrow:redundantNodes.get(i).getcNodeList()) {
//						if(cArrow.getGraphNode().getName().equals(recArrow.getGraphNode().getName())) {
//							existActionNode=true;
//							break second;
//						}
//					}
//				}
//				if(!existActionNode) {
//					redundantNodes.clear();
//					redundantNodes.add(ruleNode);
//					break;
//				}
//			}
//
//		}
//		return redundantNodes;
//	}
//
//	public static List<GraphNode> canTraceBack(GraphNode ruleNode,GraphNode otherRuleNode,GraphNode cRuleNode,List<GraphNode> graphNodes,int count){
//		List<GraphNode> ruleList=new ArrayList<GraphNode>();
//		List<GraphNode> triggerNodes=new ArrayList<GraphNode>();
//		////获得ruleNode的所有trigger节点
//		for(GraphNodeArrow pArrow:ruleNode.getpNodeList()) {
//			triggerNodes.add(pArrow.getGraphNode());
//		}
//		for(GraphNode node:graphNodes) {
//			if(node.flag) {
//				node.flag=false;
//			}
//		}
//		////otherRuleNode的trigger是不是都能回溯到ruleNode的trigger
//		////trigger1 -> ruleNode ; trigger2 -> otherRuleNode
//		////看trigger2==trigger1? 或者 trigger2 -> -> ->trigger1?
//		for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
//			GraphNode triggerNode=pArrow.getGraphNode();
//			Queue<GraphNode> nodeQueue=new LinkedList<GraphNode>();
//			nodeQueue.add(triggerNode);
//			triggerNode.flag=true;
//			boolean canTraceTo=false;
//			while(!nodeQueue.isEmpty()) {
//				GraphNode node=nodeQueue.poll();
//				if(triggerNodes.contains(node)) {
//					///判断是不是在triggers中
//					canTraceTo=true;
//					break;
//				}
//				for(GraphNodeArrow nodepArrow:node.getpNodeList()) {
//					GraphNode pNode=nodepArrow.getGraphNode();
//					if(nodepArrow.getStyle()==""&&pNode.getShape().indexOf("doubleoctagon")<0&&!pNode.flag) {
//						////实线
//						if(pNode.getShape().indexOf("hexagon")>=0 && !pNode.getName().equals(ruleNode.getName())&&pNode!=cRuleNode&&count<=5) {
//							List<GraphNode> pRuleList=new ArrayList<GraphNode>();
//							/////同样需要避免进入死循环，要跳出调度
//							count++;
//							if((pRuleList=canTraceBack(ruleNode, pNode,otherRuleNode, graphNodes,count))!=null) {
//								ruleList.addAll(pRuleList);
//							}
//						}else {
//							nodeQueue.add(pNode);
//							pNode.flag=true;
//						}
//					}
//				}
//
//			}
//			if(!canTraceTo&&ruleList.size()==0) {
//				return null;
//			}
//
//		}
//		ruleList.add(otherRuleNode);
//		return ruleList;
//	}
//
	

	
	//////////////////获得incompleteness
//	public static List<DeviceDetail> getIncompleteness(List<DeviceDetail> devices,List<Action> actions) {
//		List<DeviceDetail> cannotOffDevices=new ArrayList<DeviceDetail>();
//		for(DeviceDetail device:devices) {
//			boolean canOn=false;
//			String offAction="";
//			for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
//				/////////看该设备能否开
//				if(Integer.parseInt(stateActionValue[2])>0) {
//					/////获得该设备非初始状态的action
//					String action=device.getDeviceName()+"."+stateActionValue[1];
//					for(Action act:actions) {
//						if(act.action.equals(action)) {
//							canOn=true;
//						}
//					}
//				}else {
//					/////////获得该设备初始状态的action
//					offAction=device.getDeviceName()+"."+stateActionValue[1];
//				}
//			}
//			if(canOn) {
//				boolean canOff=false;
//				for(Action act:actions) {
//					////看有没有action能关闭该设备
//					if(act.action.equals(offAction)) {
//						canOff=true;
//						break;
//					}
//				}
//				if(!canOff) {
//					cannotOffDevices.add(device);
//				}
//			}
//		}
//		return cannotOffDevices;
//	}

	/**
	 * 解析IFD的节点和边
	 * */
	//////////////////////获得IFD节点，以及节点间的关系
	public static List<GraphNode> getIFDNode(String ifdFileName,String ifdPath) {
		String dotPath=ifdPath+ifdFileName;
		List<GraphNode> graphNodes=new ArrayList<GraphNode>();
		HashMap<String,GraphNode> graphNodeMap=new HashMap<>();
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
						graphNodeMap.put(graphNode.getName(), graphNode);
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
					GraphNode pGraphNode=graphNodeMap.get(nodes[0]);
					GraphNode cGraphNode=graphNodeMap.get(nodes[1]);
					GraphNodeArrow pNode=new GraphNodeArrow();
					GraphNodeArrow cNode=new GraphNodeArrow();
					pNode.setGraphNode(pGraphNode);
					cNode.setGraphNode(cGraphNode);
					if(features!=null) {
						for(String feature:features) {
							String[] featureValue=new String[2];
							featureValue[0]=feature.substring(0,feature.indexOf("="));
							featureValue[1]=feature.substring(feature.indexOf("=")+1);
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
					pGraphNode.addcNodeList(cNode);
					cGraphNode.addpNodeList(pNode);
//					for(int i=0;i<graphNodes.size();i++) {
//						if(nodes[0].equals(graphNodes.get(i).getName())){
//							//先找到trigger9
//							for(int j=0;j<graphNodes.size();j++) {
//								if(nodes[1].equals(graphNodes.get(j).getName())){
//								//再找到trigger1	
//									GraphNodeArrow pNode=new GraphNodeArrow();
//									GraphNodeArrow cNode=new GraphNodeArrow();
//									pNode.setGraphNode(graphNodes.get(i));
//									cNode.setGraphNode(graphNodes.get(j));
//									if(features!=null) {
//										for(String feature:features) {
//											String[] featureValue=feature.split("=");
//											if(featureValue[0].equals("label")) {
//												String label=featureValue[1].replaceAll("\"", "");
//												pNode.setLabel(label);
//												cNode.setLabel(label);
//											}
//											if(featureValue[0].equals("style")) {
//												String style=featureValue[1].replaceAll("\"", "");
//												pNode.setStyle(style);
//												cNode.setStyle(style);
//											}
//											if(featureValue[0].equals("color")) {
//												String color=featureValue[1].replaceAll("\"", "");
//												pNode.setColor(color);
//												cNode.setColor(color);
//											}
//										}
//									}
//									graphNodes.get(i).addcNodeList(cNode);
//									graphNodes.get(j).addpNodeList(pNode);
//									break;
//								}
//							}
//							break;
//						}
//					}
				}
			}
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return graphNodes;
	}

	/**
	 * 解析IFD的节点和边，但获得instance节点
	 * */
	///解析IFD，获得节点和边，不管实体节点
	public static List<GraphNode> parseIFDAndGetIFDNode(String ifdPath,String ifdFileName){
		String dotPath=ifdPath+ifdFileName;
		List<GraphNode> graphNodes=new ArrayList<GraphNode>();
		HashMap<String,GraphNode> graphNodeMap=new HashMap<>();
		HashMap<String,String> instanceColorMap=new HashMap<>();
		try(BufferedReader br=new BufferedReader(new FileReader(dotPath))){
			List<String> strings=new ArrayList<String>();
			String str="";
			while((str=br.readLine())!=null) {
				//获得各节点
				strings.add(str);
				if(str.indexOf("[")>0) {
					if(str.indexOf("->")<0) {
						///获得节点的信息
						///如action1[label="Bulb.turn_bulb_on",shape="record",style="filled",fillcolor="beige"]
						///trigger1[label="temperature<=15",shape="oval",style="filled",fillcolor="lightpink"]

						String nodeName=str.substring(0, str.indexOf("[")).trim();
						///action1
						String attr=str.substring(str.indexOf("["), str.indexOf("]")).substring("[".length());
						///label="Bulb.turn_bulb_on",shape="record",style="filled",fillcolor="beige"
						//去掉引号
						attr=attr.replace("\"","");
						String[] features=attr.split(",");
						String shape="";
						String label="";
						String fillColor="";
						for(String feature:features) {
							String[] featureName=new String[2];
							featureName[0]=feature.substring(0,feature.indexOf("="));
							featureName[1]=feature.substring(feature.indexOf("=")+1);
							if(featureName[0].equals("shape")) {
								shape=featureName[1].trim();
							} else if(featureName[0].equals("label")) {
								label=featureName[1].trim();
							} else if(featureName[0].equals("fillcolor")) {
								fillColor=featureName[1].trim();
							}
						}
						////不管实体节点，也就是shape="doubleoctagon"的节点直接跳过
						if (str.indexOf("shape=\"doubleoctagon\"")>=0){
							///对应实体名和颜色
							instanceColorMap.put(nodeName,fillColor);
//							continue;   选择添加实体节点
						}
						///创建新节点
						GraphNode graphNode=new GraphNode(nodeName,shape,fillColor,label);
						graphNodes.add(graphNode);
						graphNodeMap.put(graphNode.getName(), graphNode);
					}

				}
			}


			for(String string:strings) {
				///获得各边
				if(string.indexOf("->")>0) {
					////////如trigger9->trigger1[color="red",fontsize="18"]
					String arrow=string;
					String attrbutes="";
					String[] features=null;
					if(string.indexOf("[")>0) {
						//trigger9->trigger1
						arrow=string.substring(0, string.indexOf("["));
						//color="red",fontsize="18"
						attrbutes=string.substring(string.indexOf("["), string.indexOf("]")).substring("[".length());
						///去掉引号
						attrbutes=attrbutes.replace("\"","");
						features=attrbutes.split(",");
					}
					//trigger9->trigger1
					String[] nodes=arrow.split("->");
					for(int i=0;i<nodes.length;i++) {
						nodes[i]=nodes[i].trim();
					}
					/////获得前后关系的两个节点的边
					GraphNode pGraphNode=graphNodeMap.get(nodes[0]); ///前一个节点
					GraphNode cGraphNode=graphNodeMap.get(nodes[1]);  ///后一个节点
					if (pGraphNode.getShape().equals("doubleoctagon")){
						///表明是实体节点，给后一个节点添加实体信息，实体名和该节点颜色
						String color=instanceColorMap.get(nodes[0]);
						String[] relatedInstanceAndColor=new String[2];
						relatedInstanceAndColor[0]=nodes[0];
						relatedInstanceAndColor[1]=color;
						cGraphNode.setRelatedInstanceAndColor(relatedInstanceAndColor);
//						continue;
					}
					GraphNodeArrow pNodeArrow=new GraphNodeArrow();  ///对于后一个节点来说的前边，指向当前节点的边
					GraphNodeArrow cNodeArrow=new GraphNodeArrow();    ///对于前一个节点来说的后边，从当前节点出发的边
					pNodeArrow.setGraphNode(pGraphNode);    ///前边出发的节点
					cNodeArrow.setGraphNode(cGraphNode);    ///后边指向的节点
					if(features!=null) {
						for(String feature:features) {
							String[] featureValue=new String[2];
							featureValue[0]=feature.substring(0,feature.indexOf("="));
							featureValue[1]=feature.substring(feature.indexOf("=")+1);
							if(featureValue[0].equals("label")) {
								pNodeArrow.setLabel(featureValue[1].trim());
								cNodeArrow.setLabel(featureValue[1].trim());
							}
							if(featureValue[0].equals("style")) {
								pNodeArrow.setStyle(featureValue[1].trim());
								cNodeArrow.setStyle(featureValue[1].trim());
							}
							if(featureValue[0].equals("color")) {
								pNodeArrow.setColor(featureValue[1].trim());
								cNodeArrow.setColor(featureValue[1].trim());
							}
						}
					}
					pGraphNode.getcNodeArrowList().add(cNodeArrow); ///对于前一个节点来说，添加一条边，从该节点指向后一个节点
					cGraphNode.getpNodeArrowList().add(pNodeArrow);   ///对于后一个节点来说，添加一条边，从前一个节点指向该节点
				}
			}


		}catch(IOException e) {
			e.printStackTrace();
		}

		return graphNodes;
	}

	////2021/12/27
	/**
	 * 生成IFD
	 * */
	////生成IFD
	public static void generateIFD(HashMap<String,Trigger> triggerMap, HashMap<String,Action> actionMap, List<Rule> rules, InstanceLayer interactiveEnvironment,HashMap<String, Instance> interactiveInstanceMap,String ifdFileName,String filePath) throws IOException {
		StringBuilder sb = new StringBuilder();

		sb.append("digraph infoflow{\r\n");
		sb.append("rankdir=LR;\r\n");
		sb.append("\r\n");
		///////////////////生成sensor节点//////////////////
		sb.append("///////////////sensors////////////////\r\n");
		for (SensorInstance sensorInstance : interactiveEnvironment.getSensorInstances()) {
			String sensorDot = sensorInstance.getInstanceName() + "[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"azure3\"]";
			sb.append(sensorDot + "\r\n");
		}
		///////////////////////////////////////////////////
		sb.append("\r\n");

		//////////////生成device节点////////////////
		sb.append("//////////////devices//////////////\r\n");
		for (DeviceInstance deviceInstance : interactiveEnvironment.getDeviceInstances()) {
			String deviceDot = deviceInstance.getInstanceName() + "[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"darkseagreen1\"]";
			sb.append(deviceDot + "\r\n");
		}

		//////////////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("\r\n");

		//////////////生成cyber service节点//////////////////
		sb.append("////////////////////cyber services///////////////////\r\n");
		for (CyberServiceInstance cyberServiceInstance : interactiveEnvironment.getCyberServiceInstances()) {
			String cyberDot = cyberServiceInstance.getInstanceName() + "[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"mediumpurple1\"]";
			sb.append(cyberDot + "\r\n");
		}
		//////////////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("\r\n");

		/////////////////////生成rule节点////////////////////
		sb.append("////////////////////rules/////////////////////\r\n");
		for (Rule rule : rules) {
			String ruleDot = rule.getRuleName() + "[shape=\"hexagon\",style=\"filled\",fillcolor=\"lightskyblue\"]";
			sb.append(ruleDot + "\r\n");
		}
		//////////////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("\r\n");

		/////////////////////////生成action节点/////////////////////////////
		/////////////////////////rule->action  action->device//////////////
		sb.append("\r\n");
		sb.append("\r\n");
		sb.append("////////////////////actions/////////////////////\r\n");
		for (Map.Entry<String, Action> actionKeyValue : actionMap.entrySet()) {
			//action节点
			Action action = actionKeyValue.getValue();
			String actionDot = action.getActionId() + "[label=\"" + action.getActionContent() + "\",shape=\"record\",style=\"filled\",fillcolor=\"beige\"]";
			sb.append(actionDot + "\r\n");
			for (Rule actRule : action.getRelatedRules()) {
				//rule->action
				String ruleToActionDot = actRule.getRuleName() + "->" + action.getActionId();
				sb.append(ruleToActionDot + "\r\n");
			}
			//instance->action   device/cyber->action
			String actionToDevice = action.getInstanceName() + "->" + action.getActionId() + "[color=\"lemonchiffon3\"]";
			sb.append(actionToDevice + "\r\n");
		}

		/////////////////////////生成trigger节点//////////////////////////////////////
		/////////////////////////trigger->rule  device/sensor->trigger//////////////
		sb.append("\r\n");
		sb.append("////////////////////triggers/////////////////////\r\n");
		for (Map.Entry<String, Trigger> triggerKeyValue : triggerMap.entrySet()) {
			//trigger节点
			Trigger trigger = triggerKeyValue.getValue();
			String triggerDot = trigger.getTriggerId() + "[label=\"" + trigger.getTriggerContent() + "\",shape=\"oval\",style=\"filled\",fillcolor=\"lightpink\"]";
			sb.append(triggerDot + "\r\n");
			//trigger->rule
			for (Rule triRule : trigger.getRelatedRules()) {
				String triggerToRuleDot = trigger.getTriggerId() + "->" + triRule.getRuleName();
				sb.append(triggerToRuleDot + "\r\n");
			}


			//trigger受到的影响   ，trigger只能是attribute<(<=,>,>=)value or Instance.state  instance可以是人或不确定实体，也可以是设备实例
			if (trigger.getSensor().equals("")) {
				//sensor/device ->trigger
				String deviceToTriggerDot = trigger.getInstanceName() + "->" + trigger.getTriggerId() + "[color=\"lightpink\"]";
				sb.append(deviceToTriggerDot + "\r\n");

				///如果是设备状态，则可能受action直接影响
				//action->trigger
				///找到对应的实例
				Instance instance = interactiveInstanceMap.get(trigger.getInstanceName());
				if (instance instanceof DeviceInstance) {
					DeviceInstance deviceInstance = (DeviceInstance) instance;
					///找到状态对应的sync
					for (DeviceType.StateSyncValueEffect stateSyncValueEffect : deviceInstance.getDeviceType().getStateSyncValueEffects()) {
						if (stateSyncValueEffect.getStateName().equals(trigger.getTriggerForm()[2])) {
							String synchronisation = stateSyncValueEffect.getSynchronisation();
							///找到对应action
							StringBuilder actionContent = new StringBuilder();
							actionContent.append(trigger.getInstanceName());
							actionContent.append(".");
							actionContent.append(synchronisation);
							Action action = actionMap.get(actionContent.toString());
							//action->trigger
							if (action!=null){
								///如果有该action，则指向该trigger
								String actionToTriggerDot = action.getActionId() + "->" + trigger.getTriggerId() + "[color=\"red\",fontsize=\"18\"]";
								sb.append(actionToTriggerDot + "\r\n");
							}

							break;
						}
					}
				}

			} else {
				//sensor->trigger
				String sensorToTriggerDot = trigger.getSensor() + "->" + trigger.getTriggerId() + "[color=\"lightpink\"]";
				sb.append(sensorToTriggerDot + "\r\n");

				if (!trigger.getTriggerForm()[1].equals(".")) {
					//trigger 类型 是  attribute<(<=,>,>=)value

					///获得trigger->trigger和action->trigger
					if (trigger.getTriggerForm()[1].indexOf(">") >= 0) {
						getToTriggerDot(trigger,triggerMap,actionMap,interactiveInstanceMap,sb,">");
					} else if (trigger.getTriggerForm()[1].indexOf("<") >= 0) {
						getToTriggerDot(trigger,triggerMap,actionMap,interactiveInstanceMap,sb,"<");
					}
				}
			}

		}
		/////////////////////////////////////////////
		sb.append("\r\n");
		sb.append("}\r\n");
//		parse.write(graphvizFile, "", true);
//		parse.write(graphvizFile, "}", true);
		BufferedWriter bw=new BufferedWriter(new FileWriter(filePath+ifdFileName));
		bw.write(sb.toString());
		bw.close();
	}
	///找到指向trigger的节点和边
	public static void getToTriggerDot(Trigger trigger, HashMap<String,Trigger> triggerMap, HashMap<String,Action> actionMap, HashMap<String,Instance> interactiveInstanceMap,StringBuilder sb,String compare){
		///找其他蕴含该trigger的trigger，即otherTrigger是trigger的子集
		//trigger->trigger   相同属性
		for (Map.Entry<String,Trigger> otherTriggerKeyValue: triggerMap.entrySet()) {
			Trigger otherTrigger=otherTriggerKeyValue.getValue();
			if (!otherTrigger.getTriggerId().equals(trigger.getTriggerId())
					&& otherTrigger.getTriggerForm()[0].equals(trigger.getTriggerForm()[0])
					&& otherTrigger.getTriggerForm()[1].indexOf(compare) >= 0) {  ///<,>
				Double triVal = Double.parseDouble(trigger.getTriggerForm()[2]);
				Double othTriVal = Double.parseDouble(otherTrigger.getTriggerForm()[2]);
				boolean contain=false;
				if (compare.equals("<")&&triVal > othTriVal){
					/////如temperature<25 -> temperature<30
					contain=true;
				}else if (compare.equals(">")&&triVal < othTriVal){
					/////如temperature>30 -> temperature>25
					contain=true;
				} else if (triVal.toString().equals(othTriVal.toString())) {
					if (trigger.getTriggerForm()[1].equals(compare+"=")) {  ///<=,>=
						////如temperature<30 -> temperature<=30
						contain=true;
					}
				}
				if (contain){
					String triggerToTriggerDot = otherTrigger.getTriggerId() + "->" + trigger.getTriggerId() + "[color=\"red\",fontsize=\"18\"]";
					sb.append(triggerToTriggerDot + "\r\n");
				}
			}
		}

		///action--->trigger 隐性的正影响
		for (Map.Entry<String,Action> actionKeyValue:actionMap.entrySet()){
			Action action=actionKeyValue.getValue();
			Instance instance=interactiveInstanceMap.get(action.getInstanceName());
			boolean existEffect=false;
			if (instance instanceof DeviceInstance){
				DeviceInstance deviceInstance=(DeviceInstance) instance;
				DeviceType deviceType=deviceInstance.getDeviceType();
				for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
					if (stateSyncValueEffect.getSynchronisation().equals(action.getSync())){
						////找到该action的sync、state对应的对attribute的影响
						for (String[] effect:stateSyncValueEffect.getEffects()){
							///effect[0]=attribute, effect[1]=delta（对于会对总变化率产生影响的）, effect[2]=影响值
							if (effect[0].equals(trigger.getTriggerForm()[0])&&!effect[1].equals("")){
								double effectValue=Double.parseDouble(effect[2]);
								if (compare.equals(">")&&effectValue>0){
									///effect>0则说明该action对>的trigger有正影响
									/////存在隐性正影响
									existEffect=true;
								}else if (compare.equals("<")&&effectValue<0){
									///effect<0则说明该action对<的trigger有正影响
									/////存在隐性正影响
									existEffect=true;
								}
								break;
							}
						}
						break;
					}
				}
			}
			if (existEffect){
				String actionToTriggerDot=action.getActionId()+"->"+trigger.getTriggerId()+"[color=\"red\",fontsize=\"18\",style=\"dashed\"]";
				sb.append(actionToTriggerDot+"\r\n");
			}
		}

	}

	
//	/////////////////生成IFD
//	public static void generateIFD(List<Rule> rules,String ifdFileName,String ifdPath,List<DeviceDetail> devices,List<SensorType> sensors,List<BiddableType> biddables) throws IOException {
////		GetTemplate parse=new GetTemplate();
//
//
//		//-------------------写dot文件-----------------------------------
//
//		String dotPath=ifdPath+ifdFileName;
//		StringBuilder sb=new StringBuilder();
//
//
//		sb.append("digraph infoflow{\r\n");
//		sb.append("rankdir=LR;\r\n");
//		sb.append("\r\n");
//
//		///////////////////生成sensor节点//////////////////
//		sb.append("///////////////sensors////////////////\r\n");
//		for(SensorType sensor:sensors) {
//			String sensorDot=sensor.getName()+"[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"azure3\"]";
//			sb.append(sensorDot+"\r\n");
//
//		}
//		///////////////////////////////////////////////////
//		sb.append("\r\n");
//
//		//////////////生成controlled device节点////////////////
//		sb.append("//////////////controlled devices//////////////\r\n");
//		for(DeviceDetail device:devices) {
//			String controlledDot=device.getDeviceName()+"[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"darkseagreen1\"]";
//			sb.append(controlledDot+"\r\n");
//		}
//
//		//////////////////////////////////////////////////////
//		sb.append("\r\n");
//		sb.append("\r\n");
//
//		/////////////////////生成rule节点////////////////////
//		sb.append("////////////////////rulesNum/////////////////////\r\n");
//		sb.append("\r\n");
//		for(Rule rule:rules) {
//			String ruleDot=rule.getRuleName()+"[shape=\"hexagon\",style=\"filled\",fillcolor=\"lightskyblue\"]";
//			sb.append(ruleDot+"\r\n");
//		}
//		//////////////////////////////////////////////////////
//		sb.append("\r\n");
//		sb.append("\r\n");
//
//		///获得所有trigger和action
//		//getTriggers,getActions
//		List<Trigger> triggers=RuleService.getAllTriggers(rules,sensors,biddables);
//		List<Action> actions=RuleService.getAllActions(rules, devices);
//
//
//		/////////////////////////生成action节点/////////////////////////////
//		/////////////////////////rule->action  action->device//////////////
//		sb.append("\r\n");
//		sb.append("\r\n");
//		sb.append("////////////////////actions/////////////////////\r\n");
//		for(Action action:actions) {
//			//action节点
//			String actionDot=action.actionNum+"[label=\""+action.action+"\",shape=\"record\",style=\"filled\",fillcolor=\"beige\"]";
//			sb.append(actionDot+"\r\n");
//			for(Rule actRule:action.rules) {
//				//rule->action
//				String ruleToActionDot=actRule.getRuleName()+"->"+action.actionNum;
//				sb.append(ruleToActionDot+"\r\n");
//			}
//			//action->device
//			String actionToDevice=action.device+"->"+action.actionNum+"[color=\"lemonchiffon3\"]";
//			sb.append(actionToDevice+"\r\n");
//		}
//
//
//		/////////////////////////生成trigger节点//////////////////////////////////////
//		/////////////////////////trigger->rule  device/sensor->trigger//////////////
//		sb.append("\r\n");
//		sb.append("////////////////////triggers/////////////////////\r\n");
//		for(Trigger trigger:triggers) {
//			//trigger节点
//			String triggerDot=trigger.triggerNum+"[label=\""+trigger.trigger+"\",shape=\"oval\",style=\"filled\",fillcolor=\"lightpink\"]";
//			sb.append(triggerDot+"\r\n");
//			//trigger->rule
//			for(Rule triRule:trigger.rules) {
//				String triggerToRuleDot=trigger.triggerNum+"->"+triRule.getRuleName();
//				sb.append(triggerToRuleDot+"\r\n");
//			}
//			//sensor/device ->trigger
//			String deviceToTriggerDot=trigger.device+"->"+trigger.triggerNum+"[color=\"lightpink\"]";
//			sb.append(deviceToTriggerDot+"\r\n");
//
//			//trigger受到的影响
//			if(trigger.attrVal[1].equals(".")) {
//
//				//action->trigger
//				for(Action action:actions) {
//					if(trigger.device.equals(action.device)
//							&& trigger.attrVal[2].equals(action.toState)) {
//						String actionToTriggerDot=action.actionNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//						sb.append(actionToTriggerDot+"\r\n");
//					}
//				}
//			}else {
//				//trigger->trigger   相同属性
//				if(trigger.attrVal[1].indexOf(">")>=0) {
//					for(Trigger otherTrigger:triggers) {
//						if(otherTrigger!=trigger
//								&& otherTrigger.attrVal[0].equals(trigger.attrVal[0])
//								&& otherTrigger.attrVal[1].indexOf(">")>=0) {
//							Double triVal=Double.parseDouble(trigger.attrVal[2]);
//							Double othTriVal=Double.parseDouble(otherTrigger.attrVal[2]);
//							if(triVal<othTriVal) {
//								/////如temperature>30 -> temperature>25
//								String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//								sb.append(triggerToTriggerDot+"\r\n");
//							}else if(triVal.toString().equals(othTriVal.toString())) {
//								if(trigger.attrVal[1].equals(">=")) {
//									////如temperature>30 -> temperature>=30
//									String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//									sb.append(triggerToTriggerDot+"\r\n");
//								}
//							}
//						}
//					}
//				}else if(trigger.attrVal[1].indexOf("<")>=0) {
//					//trigger->trigger   相同属性
//					for(Trigger otherTrigger:triggers) {
//						if(otherTrigger!=trigger
//								&& otherTrigger.attrVal[0].equals(trigger.attrVal[0])
//								&& otherTrigger.attrVal[1].indexOf("<")>=0) {
//							Double triVal=Double.parseDouble(trigger.attrVal[2]);
//							Double othTriVal=Double.parseDouble(otherTrigger.attrVal[2]);
//							if(triVal>othTriVal) {
//							/////如temperature<25 -> temperature<30
//								String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//								sb.append(triggerToTriggerDot+"\r\n");
//							}else if(triVal.toString().equals(othTriVal.toString())
//									&& trigger.attrVal[1].equals(otherTrigger.attrVal[1])) {
//								if(trigger.attrVal[1].equals("<=")) {
//								////如temperature<30 -> temperature<=30
//									String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//									sb.append(triggerToTriggerDot+"\r\n");
//								}
//							}
//						}
//					}
//				}
//
//				//biddableToTrigger
//			}
//		}
//
//
//		/////////////////////////////////////////////
//		sb.append("\r\n");
//		sb.append("}\r\n");
////		parse.write(graphvizFile, "", true);
////		parse.write(graphvizFile, "}", true);
//		BufferedWriter bw=new BufferedWriter(new FileWriter(dotPath));
//		bw.write(sb.toString());
//		bw.close();
//	}
//
//	/////////////////生成IFD，用户寻找最佳场景（能触发最多规则的场景），也就是添加action对trigger的隐性影响
//	public static void generateIFDForBest(List<Rule> rules,String ifdFileName,String ifdPath,List<DeviceDetail> devices,List<SensorType> sensors,List<BiddableType> biddables) throws IOException {
//		////////创建带影响关系的IFD
//
//
//		//-------------------写dot文件-----------------------------------
//
//		String dotPath=ifdPath+ifdFileName;
//		StringBuilder sb=new StringBuilder();
//
//
//		sb.append("digraph infoflow{\r\n");
//		sb.append("rankdir=LR;\r\n");
//		sb.append("\r\n");
//
//		///////////////////生成sensor节点//////////////////
//		sb.append("///////////////sensors////////////////\r\n");
//		for(SensorType sensor:sensors) {
//			String sensorDot=sensor.getName()+"[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"azure3\"]";
//			sb.append(sensorDot+"\r\n");
//
//		}
//		///////////////////////////////////////////////////
//		sb.append("\r\n");
//
//		//////////////生成controlled device节点////////////////
//		sb.append("//////////////controlled devices//////////////\r\n");
//		for(DeviceDetail device:devices) {
//			String controlledDot=device.getDeviceName()+"[shape=\"doubleoctagon\",style=\"filled\",fillcolor=\"darkseagreen1\"]";
//			sb.append(controlledDot+"\r\n");
//		}
//
//		//////////////////////////////////////////////////////
//		sb.append("\r\n");
//		sb.append("\r\n");
//
//		/////////////////////生成rule节点////////////////////
//		sb.append("////////////////////rulesNum/////////////////////\r\n");
//		sb.append("\r\n");
//		for(Rule rule:rules) {
//			String ruleDot=rule.getRuleName()+"[shape=\"hexagon\",style=\"filled\",fillcolor=\"lightskyblue\"]";
//			sb.append(ruleDot+"\r\n");
//		}
//		//////////////////////////////////////////////////////
//		sb.append("\r\n");
//		sb.append("\r\n");
//
//		///获得所有trigger和action
//		//getTriggers,getActions
//		List<Trigger> triggers=RuleService.getAllTriggers(rules,sensors,biddables);
//		List<Action> actions=RuleService.getAllActions(rules, devices);
//
//
//		/////////////////////////生成action节点/////////////////////////////
//		/////////////////////////rule->action  action->device//////////////
//		sb.append("\r\n");
//		sb.append("\r\n");
//		sb.append("////////////////////actions/////////////////////\r\n");
//		for(Action action:actions) {
//			//action节点
//			String actionDot=action.actionNum+"[label=\""+action.action+"\",shape=\"record\",style=\"filled\",fillcolor=\"beige\"]";
//			sb.append(actionDot+"\r\n");
//			for(Rule actRule:action.rules) {
//				//rule->action
//				String ruleToActionDot=actRule.getRuleName()+"->"+action.actionNum;
//				sb.append(ruleToActionDot+"\r\n");
//			}
//			//action->device
//			String actionToDevice=action.device+"->"+action.actionNum+"[color=\"lemonchiffon3\"]";
//			sb.append(actionToDevice+"\r\n");
//		}
//
//
//		/////////////////////////生成trigger节点/////////////////////////////////////
//		/////////////////////////trigger->rule  device/sensor->trigger//////////////
//		///////////////////////添加//action->trigger的隐性影响///////////////////////////
//		sb.append("\r\n");
//		sb.append("////////////////////triggers/////////////////////\r\n");
//		for(Trigger trigger:triggers) {
//			//trigger节点
//			String triggerDot=trigger.triggerNum+"[label=\""+trigger.trigger+"\",shape=\"oval\",style=\"filled\",fillcolor=\"lightpink\"]";
//			sb.append(triggerDot+"\r\n");
//			//trigger->rule
//			for(Rule triRule:trigger.rules) {
//				String triggerToRuleDot=trigger.triggerNum+"->"+triRule.getRuleName();
//				sb.append(triggerToRuleDot+"\r\n");
//			}
//			//sensor/device ->trigger
//			String deviceToTriggerDot=trigger.device+"->"+trigger.triggerNum+"[color=\"lightpink\"]";
//			sb.append(deviceToTriggerDot+"\r\n");
//
//			//trigger受到的影响
//			if(trigger.attrVal[1].equals(".")) {
//
//				//action->trigger
//				for(Action action:actions) {
//					if(trigger.device.equals(action.device)
//							&& trigger.attrVal[2].equals(action.toState)) {
//						String actionToTriggerDot=action.actionNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//						sb.append(actionToTriggerDot+"\r\n");
//					}
//				}
//			}else {
//				if(trigger.attrVal[1].indexOf(">")>=0) {
//					//trigger->trigger   相同属性
//					for(Trigger otherTrigger:triggers) {
//						if(otherTrigger!=trigger
//								&& otherTrigger.attrVal[0].equals(trigger.attrVal[0])
//								&& otherTrigger.attrVal[1].indexOf(">")>=0) {
//							Double triVal=Double.parseDouble(trigger.attrVal[2]);
//							Double othTriVal=Double.parseDouble(otherTrigger.attrVal[2]);
//							if(triVal<othTriVal) {
//								/////如temperature>30 -> temperature>25
//								String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//								sb.append(triggerToTriggerDot+"\r\n");
//							}else if(triVal.toString().equals(othTriVal.toString())) {
//								if(trigger.attrVal[1].equals(">=")) {
//									////如temperature>30 -> temperature>=30
//									String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//									sb.append(triggerToTriggerDot+"\r\n");
//								}
//							}
//						}
//					}
//					///action--->trigger
//					for(Action action:actions) {
//						String devi=action.getDevice();
//						String toState=action.getToState();
//						for(DeviceDetail device:devices) {
//							if(devi.equals(device.getDeviceName())){
//								for(StateEffect stateEffect:device.getDeviceType().getStateEffects()) {
//									if(stateEffect.getState().equals(toState)) {
//										for(String[] effect:stateEffect.getEffects()) {
//											if(effect[0].equals(trigger.attrVal[0])&&effect[1].equals("'==")&&Double.parseDouble(effect[2])>0) {
//												/////存在隐性正影响
//												String actionToTriggerDot=action.actionNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\",style=\"dashed\"]";
//												sb.append(actionToTriggerDot+"\r\n");
//												break;
//											}
//										}
//										break;
//									}
//								}
//
//								break;
//							}
//						}
//					}
//
//				}else if(trigger.attrVal[1].indexOf("<")>=0) {
//					for(Trigger otherTrigger:triggers) {
//						//trigger->trigger   相同属性
//						if(otherTrigger!=trigger
//								&& otherTrigger.attrVal[0].equals(trigger.attrVal[0])
//								&& otherTrigger.attrVal[1].indexOf("<")>=0) {
//							Double triVal=Double.parseDouble(trigger.attrVal[2]);
//							Double othTriVal=Double.parseDouble(otherTrigger.attrVal[2]);
//							if(triVal>othTriVal) {
//							/////如temperature<25 -> temperature<30
//								String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//								sb.append(triggerToTriggerDot+"\r\n");
//							}else if(triVal.toString().equals(othTriVal.toString())
//									&& trigger.attrVal[1].equals(otherTrigger.attrVal[1])) {
//								if(trigger.attrVal[1].equals("<=")) {
//								////如temperature<30 -> temperature<=30
//									String triggerToTriggerDot=otherTrigger.triggerNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\"]";
//									sb.append(triggerToTriggerDot+"\r\n");
//								}
//							}
//						}
//					}
//
//					///action--->trigger
//					for(Action action:actions) {
//						String devi=action.getDevice();
//						String toState=action.getToState();
//						for(DeviceDetail device:devices) {
//							if(devi.equals(device.getDeviceName())){
//								for(StateEffect stateEffect:device.getDeviceType().getStateEffects()) {
//									if(stateEffect.getState().equals(toState)) {
//										for(String[] effect:stateEffect.getEffects()) {
//											if(effect[0].equals(trigger.attrVal[0])&&effect[1].equals("'==")&&Double.parseDouble(effect[2])<0) {
//												/////存在隐性正影响
//												String actionToTriggerDot=action.actionNum+"->"+trigger.triggerNum+"[color=\"red\",fontsize=\"18\",style=\"dashed\"]";
//												sb.append(actionToTriggerDot+"\r\n");
//												break;
//											}
//										}
//										break;
//									}
//								}
//
//								break;
//							}
//						}
//					}
//				}
//
//				//biddableToTrigger
//			}
//		}
//
//
//		/////////////////////////////////////////////
//		sb.append("\r\n");
//		sb.append("}\r\n");
////		parse.write(graphvizFile, "", true);
////		parse.write(graphvizFile, "}", true);
//		BufferedWriter bw=new BufferedWriter(new FileWriter(dotPath));
//		bw.write(sb.toString());
//		bw.close();
//	}
//

	/**
	 * 获得一条TAP规则能触发的其他TAP规则
	 * */
	///获得各规则能触发的所有规则
	public static List<List<Rule>> getRulesTriggeredRules(HashMap<String,Rule> ruleMap,List<GraphNode> graphNodes){
		List<List<Rule>> rulesTriggeredRules=new ArrayList<>();
		HashMap<String,List<Rule>> rulesTriggeredRulesMap=new HashMap<>();
		for(GraphNode graphNode:graphNodes) {
			if(graphNode.getShape().equals("hexagon")) {
				////初步获得每条规则能触发的规则
				List<Rule> triggeredRules=getRuleTriggeredRules(ruleMap, graphNode, graphNodes);
				rulesTriggeredRules.add(triggeredRules);
				rulesTriggeredRulesMap.put(triggeredRules.get(0).getRuleName(), triggeredRules);
			}
		}
		for(List<Rule> ruleTriggeredRules:rulesTriggeredRules) {
			for(int i=1;i<ruleTriggeredRules.size();i++) {
				List<Rule> init=new ArrayList<>();
				////获得每条规则能触发的所有规则
				getRuleTriggeredRulesAll(ruleTriggeredRules, init,rulesTriggeredRulesMap);				
			}
		}
		return rulesTriggeredRules;
	}
	
	////根据最初获得的每条规则能触发的规则获得每条规则能触发的所有规则
	public static void getRuleTriggeredRulesAll(List<Rule> ruleTriggeredRules,List<Rule> lastRuleTriggeredRules,HashMap<String,List<Rule>> rulesTriggeredRulesMap) {
		////根据最初获得的每条规则能触发的规则获得每条规则能触发的所有规则
		ListIterator<Rule> ruleTriggeredRulesIterator=ruleTriggeredRules.listIterator();
		ruleTriggeredRulesIterator.next();
		while(ruleTriggeredRulesIterator.hasNext()) {
			Rule rule=ruleTriggeredRulesIterator.next();
			List<Rule> subRuleTriggeredRules=rulesTriggeredRulesMap.get(rule.getRuleName());
			if(subRuleTriggeredRules.size()>1&&!lastRuleTriggeredRules.contains(subRuleTriggeredRules.get(0))) {
				///能触发其他规则,如果有两条规则之间能相互触发则不考虑。。。
				///即如果这条规则在已经能被上一条规则触发，则不考虑
				getRuleTriggeredRulesAll(subRuleTriggeredRules,ruleTriggeredRules, rulesTriggeredRulesMap);
				
			}
			for(int i=1;i<subRuleTriggeredRules.size();i++) {
				//添加 sub 后面能触发的规则
				if(!ruleTriggeredRules.contains(subRuleTriggeredRules.get(i))) {
					ruleTriggeredRulesIterator.add(subRuleTriggeredRules.get(i));
				}
			}
		}

	}
	
	/////获得每条规则能触发的规则数量,先找到每条规则直接能触发的规则
	public static List<Rule> getRuleTriggeredRules(HashMap<String,Rule> ruleMap,GraphNode ruleNode,List<GraphNode> graphNodes){
		///存本身以及本身能触发的ruleNodes
		List<GraphNode> triggeredRuleNodes=new ArrayList<>();
		triggeredRuleNodes.add(ruleNode);
		List<GraphNode> triggerNodes=new ArrayList<>();
		List<GraphNode> actionNodes=new ArrayList<>();
		for(GraphNodeArrow pArrow:ruleNode.getpNodeList()) {
			triggerNodes.add(pArrow.getGraphNode());
		}
		for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
			actionNodes.add(cArrow.getGraphNode());
		}
		////分别找到可能引发的其他rules
		for(GraphNode triggerNode:triggerNodes) {
			for(GraphNodeArrow cArrow:triggerNode.getcNodeList()) {
				if(cArrow.getGraphNode().getShape().equals("hexagon")&&!cArrow.getGraphNode().getName().equals(ruleNode.getName())) {
					//rule
					//如ti∈Ti存在黑色实线后继节点，即为rule Rj≠Ri，获得Rj的所有trigger节点Tj，判断Tj在Ri的情况下能否满足
					GraphNode otherRuleNode=cArrow.getGraphNode();
					boolean canAllBeTriggered=true;
					for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
						if(!canTriggerBeTriggered(ruleNode, pArrow.getGraphNode(), graphNodes)) {
							canAllBeTriggered=false;
						}
					}
					if(canAllBeTriggered) {
						triggeredRuleNodes.add(otherRuleNode);
					}
				}else if(cArrow.getGraphNode().getShape().equals("oval")) {
					//如ti∈Ti存在红色实线后继节点，即为trigger tj，获得tj的黑色实线后继节点rule Rj，获得Rj的所有trigger节点Tj，判断Tj在Ri的情况下能否满足
					for(GraphNodeArrow ccArrow:cArrow.getGraphNode().getcNodeList()) {
						if(ccArrow.getGraphNode().getShape().equals("hexagon")&&!ccArrow.getGraphNode().getName().equals(ruleNode.getName())) {
							///rule
							GraphNode otherRuleNode=ccArrow.getGraphNode();
							boolean canAllBeTriggered=true;
							for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
								//trigger
								//看这条trigger能否满足
								if(!canTriggerBeTriggered(ruleNode, pArrow.getGraphNode(), graphNodes)) {
									canAllBeTriggered=false;
								}
							}
							if(canAllBeTriggered) {
								triggeredRuleNodes.add(otherRuleNode);
							}
						}
					}
				}
			}
		}
		for(GraphNode actionNode:actionNodes) {
			for(GraphNodeArrow cArrow:actionNode.getcNodeList()) {
				if(cArrow.getGraphNode().getShape().equals("oval")) {
					//trigger
					//如ai∈Ai存在红色实线后继节点，即为trigger tj，获得tj的黑色实线后继节点rule Rj，获得Rj的所有trigger节点Tj，判断Tj在Ri的情况下能否满足
					for(GraphNodeArrow ccArrow:cArrow.getGraphNode().getcNodeList()) {
						if(ccArrow.getGraphNode().getShape().equals("hexagon")&&!ccArrow.getGraphNode().getName().equals(ruleNode.getName())) {
							//rule
							GraphNode otherRuleNode=ccArrow.getGraphNode();
							boolean canAllBeTriggered=true;
							for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
								//trigger
								//看这条trigger能否满足
								if(!canTriggerBeTriggered(ruleNode, pArrow.getGraphNode(), graphNodes)) {
									canAllBeTriggered=false;
								}
							}
							if(canAllBeTriggered) {
								triggeredRuleNodes.add(otherRuleNode);
							}
						}
					}
				}
//				else if(cArrow.getGraphNode().getShape().equals("oval")&&cArrow.getStyle().equals("dashed")) {
//					//trigger
//					//如ai∈Ai存在红色虚线后继节点，即为trigger tj，判断tj在Ri情况下能否满足，如能满足，获得tj的黑色实线后继节点rule Rj，获得Rj的所有trigger节点Tj，判断Tj在Ri的情况下能否满足
//					if(canTriggerBeTriggered(ruleNode, cArrow.getGraphNode(), graphNodes))
//					for(GraphNodeArrow ccArrow:cArrow.getGraphNode().getcNodeList()) {
//						if(ccArrow.getGraphNode().getShape().equals("hexagon")&&!ccArrow.getGraphNode().getName().equals(ruleNode.getName())) {
//							GraphNode otherRuleNode=ccArrow.getGraphNode();
//							boolean canAllBeTriggered=true;
//							for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
//								if(!canTriggerBeTriggered(ruleNode, pArrow.getGraphNode(), graphNodes)) {
//									canAllBeTriggered=false;
//								}
//							}
//							if(canAllBeTriggered) {
//								triggeredRuleNodes.add(otherRuleNode);
//							}
//						}
//					}
//				}
			}
		}
		List<Rule> ruleList=new ArrayList<>();
		for(GraphNode triggeredRuleNode:triggeredRuleNodes) {
			ruleList.add(ruleMap.get(triggeredRuleNode.getName()));
		}
		return ruleList;
	}
	
//	////
//	public static List<GraphNode> canBeTriggered(GraphNode ruleNode,GraphNode otherRuleNode,List<GraphNode> graphNodes){
//		List<GraphNode> ruleList=new ArrayList<GraphNode>();
//		List<GraphNode> triggerNodes=new ArrayList<GraphNode>();
//
//		////获得ruleNode的所有trigger节点
//		for(GraphNodeArrow pArrow:ruleNode.getpNodeList()) {
//			triggerNodes.add(pArrow.getGraphNode());
//		}
//		for(GraphNode node:graphNodes) {
//			if(node.flag) {
//				node.flag=false;
//			}
//		}
//		if(otherRuleNode.getName().equals(ruleNode.getName())) {
//			////同一个rule直接返回
//			return ruleList;
//		}
//		////otherRuleNode的trigger是不是都能回溯到ruleNode的trigger
//		////trigger1 -> ruleNode ; trigger2 -> otherRuleNode
//		////看trigger2==trigger1? 或者 trigger2 -> -> ->trigger1?
//		for(GraphNodeArrow pArrow:otherRuleNode.getpNodeList()) {
//			GraphNode triggerNode=pArrow.getGraphNode();
//			Queue<GraphNode> nodeQueue=new LinkedList<GraphNode>();
//			nodeQueue.add(triggerNode);
//			triggerNode.flag=true;
//			boolean canTraceTo=false;
//			while(!nodeQueue.isEmpty()) {
//				GraphNode node=nodeQueue.poll();
//				if(triggerNodes.contains(node)) {
//					///判断是不是在triggers中
//					canTraceTo=true;
//					break;
//				}
//				////获得前节点
//				for(GraphNodeArrow nodepArrow:node.getpNodeList()) {
//					GraphNode pNode=nodepArrow.getGraphNode();
//					if(pNode.getShape().indexOf("doubleoctagon")<0&&!pNode.flag) {
//						////获得前驱action或trigger
//						if(pNode.getShape().indexOf("hexagon")>=0 && !pNode.getName().equals(ruleNode.getName())) {
//							///对于ruleNode
//							List<GraphNode> pRuleList=new ArrayList<GraphNode>();
//							
//							if((pRuleList=canTraceBack(ruleNode, pNode, graphNodes))!=null) {
//								ruleList.addAll(pRuleList);
//							}
//						}else {							
//							nodeQueue.add(pNode);
//							pNode.flag=true;
//						}
//					}
//				}
//				
//			}
//			if(!canTraceTo&&ruleList.size()==0) {
//				////有一个没法trace back，就说明没法回溯
//				return null;
//			}
//			
//		}	
//		////都能trace back则把这条规则添加上去
//		
//		ruleList.add(otherRuleNode);		
//		return ruleList;
//	}
	
	////判断trigger在这条rule的前提下能否触发
	public static boolean canTriggerBeTriggered(GraphNode ruleNode,GraphNode triggerNode,List<GraphNode> graphNodes) {
		//判断在Ri的Ti和Ai下，rule Rj(≠Ri)的triggers Tj能否满足可分解为：
		//判断在Ri的Ti和Ai下，trigger tj∈Tj能否满足

		List<GraphNode> triggerNodes=new ArrayList<>();
		List<GraphNode> actionNodes=new ArrayList<>();
		for(GraphNodeArrow pArrow:ruleNode.getpNodeList()) {
			triggerNodes.add(pArrow.getGraphNode());
		}
		for(GraphNodeArrow cArrow:ruleNode.getcNodeList()) {
			actionNodes.add(cArrow.getGraphNode());
		}
		if(triggerNodes.contains(triggerNode)) {
			//如果tj属于Ti，则tj能满足
			return true;
		}
		for(GraphNodeArrow pArrow:triggerNode.getpNodeList()) {
			GraphNode pGraphNode=pArrow.getGraphNode();
			if(pGraphNode.getShape().equals("oval")) {
				//如果tj有红色实线的前驱trigger tk节点，tk属于Ti，则tj能满足
				if(triggerNodes.contains(pGraphNode)) {
					return true;
				}
			}else if(pGraphNode.getShape().equals("record")&&pArrow.getStyle().equals("")) {
				//如果tj有红色实线的前驱action ak节点，ak属于Ai，则tj能满足
				if(actionNodes.contains(pGraphNode)) {
					return true;
				}
			}else if(pGraphNode.getShape().equals("record")&&pArrow.getStyle().equals("dashed")) {
				//如果tj有红色虚线的前驱action ak节点， ak属于Ai，查看tj红色实线的后继trigger tm节点（tm与tj涉及相同属性，
				//连接同一sensor或device），如果存在tm会触发某条规则Rm，Rm中存在某个action am，am与ak属于同一设备，且am到tj没有连线，
				//则ak无法触发tj，如果tj的所有红色虚线的前驱action 都无法触发tj，则tj不能满足，否则tj能满足
				if(!actionNodes.contains(pGraphNode)) {
					continue;
				}
				boolean canTrigger=false;
				GraphNode deviceNode=new GraphNode();
				for(GraphNodeArrow apArrow:pGraphNode.getpNodeList()) {
					if(apArrow.getGraphNode().getShape().equals("doubleoctagon")) {
						///找到对应的device
						deviceNode=apArrow.getGraphNode();
						break;
					}
				}
				if(actionNodes.contains(pGraphNode)) {
					// ak属于Ai
					canTrigger=true;
					for(GraphNodeArrow cArrow:triggerNode.getcNodeList()) {
						if(cArrow.getGraphNode().getShape().equals("oval")) {
							///trigger
							for(GraphNodeArrow ccArrow:cArrow.getGraphNode().getcNodeList()) {
								///rule
								for(GraphNodeArrow cccArrow:ccArrow.getGraphNode().getcNodeList()) {
									///action
									for(GraphNodeArrow pcccArrow:cccArrow.getGraphNode().getpNodeList()) {
										if(pcccArrow.getGraphNode().getShape().equals("doubleoctagon")) {
											////device
											if(pcccArrow.getGraphNode().getName().equals(deviceNode.getName())&&cccArrow.getGraphNode()!=pGraphNode) {
												///设备相同但是action不同
												canTrigger=false;
											}
										}
									}
								}
							}
						}
					}
				}
				return canTrigger;
			}
		}
		return false;
	}
}
