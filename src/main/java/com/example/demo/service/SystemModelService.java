package com.example.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.example.demo.bean.ModelGraph.TemplGraph;
import com.example.demo.bean.ModelGraph.TemplGraphNode;
import com.example.demo.bean.ModelGraph.TemplTransition;
import com.example.demo.bean.OutputConstruct.DeclarationQueryResult;
import com.example.demo.bean.Trigger;
import com.example.demo.bean.ScenarioTree.ScenesTree;
import com.example.demo.bean.ScenarioTree.SceneChild;
import com.example.demo.bean.ScenarioTree.AttributeValue;
public class SystemModelService {
	

	///////////////生成多个场景，根据temperature等biddable类型的的trigger取值分段获得
	public static ScenesTree generateAllScenarios(String fileName,String filePath,DeclarationQueryResult declarationQueryResult) throws DocumentException, IOException {
		/////生成场景树
		ScenesTree scenesTree=new ScenesTree();
		scenesTree.setName("smart home");
		List<List<Trigger>> attributesSameTriggers=declarationQueryResult.getAttributesSameTriggers();
		List<String[]> declarations=declarationQueryResult.getDeclarations();
		//////////--------------放到控制器生成那部分
//		/////获得所有模型
//		List<TemplGraph> templGraphs=TemplGraphService.getTemplGraphs(fileName, filePath);
//		List<TemplGraph> controllers=new ArrayList<TemplGraph>();
//		List<TemplGraph> controlledDevices=new ArrayList<>();
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getName().indexOf("Rule")>=0) {
//				controllers.add(templGraph);
//			}else if(templGraph.getDeclaration().contains("controlled_device")) {
//				controlledDevices.add(templGraph);
//			}
//		}
//		/////声明的参数
//		List<String[]> declarations=generateDeclaration(rules, biddableTypes, deviceTypes, sensorTypes, attributes, controlledDevices);
//		List<Action> actions=RuleService.getAllActions(rules, devices);
//		List<Trigger> triggers=RuleService.getAllTriggers(rules, sensorTypes, biddableTypes);
//		////////找到涉及相同causal类型的属性的triggers，分别获得分段点，用于多个场景的初始值分段赋值
//		List<List<Trigger>> attributesSameTriggers=new ArrayList<List<Trigger>>();
//		for(String[] declaration:declarations) {
//			/////////declaration: [0]clock/double [1]temperature
//			if(declaration[0].equals("double")||declaration[0].equals("clock")) {
//				for(SensorType sensor:sensorTypes) {
//					////找到检测该属性的sensor
//					if(sensor.attribute.equals(declaration[1]) &&
//							sensor.style.equals("causal")) {
//						/////causal类型的属性用于区分场景
//						/////找到涉及该属性的所有triggers
//						List<Trigger> triggersWithSameAttribute=getTriggersWithSameAttribute(declaration, triggers);
//						if(triggersWithSameAttribute.size()>0) {
//							/////如果有涉及该属性的triggers
//							attributesSameTriggers.add(triggersWithSameAttribute);
//						}
//						break;
//					}
//				}
//			}
//		}
		/////计算上述分段情况下能生成多少个场景
		////如temperature属性的断点为18、30 && humidity属性的断点为40
		////则能生成3*2个场景
		long generationStartTime=System.currentTimeMillis();
		int scenarioNum=getScenarioNum(attributesSameTriggers);
		/////获得控制器模型
//
//		/////模型声明各个场景相同
//		String modelDeclaration=getModelDeclaration(actions, triggers, devices, biddableTypes, controllers, attributes, simulationTime);
//		/////query仿真公式
//		String queryFormula=getQueryFormula(declarations, simulationTime);
		//////------------放到控制器生成那部分------------
		
		
		////文件名
		String fileNameWithoutSuffix=fileName.substring(0, fileName.lastIndexOf(".xml"));
		/////各场景分别赋予不同的值
		ExecutorService executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
		for(int i=0;i<scenarioNum;i++) {
			/////生成场景树子节点
			final int k=i;
			Runnable runnable=new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					SceneChild sceneChild=new SceneChild();
					sceneChild.setName("scenario-"+k);
					////场景模型文件名
					String newFileName=fileNameWithoutSuffix+"-scenario-"+k+".xml";
					/////给causal的参数赋值并返回
					long startTime=System.currentTimeMillis();
					List<AttributeValue> attributeValues=setCausalInitialValue(declarations, attributesSameTriggers, k);
					System.out.println("setAttributeTime:"+(System.currentTimeMillis()-startTime));
					try {
						generateScenario(declarations,  fileName, newFileName, filePath);
					} catch (DocumentException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("scenarioTime:"+(System.currentTimeMillis()-startTime));
					/////作为展示细节按钮的子节点
					AttributeValue attributeValue=new AttributeValue();
					attributeValue.setName("scenario-"+k +" details");
					sceneChild.addChildrens(attributeValues);
					sceneChild.addChildren(attributeValue);
					scenesTree.addChildren(sceneChild);
				}
			};
			executorService.execute(runnable);

			/////生成模型
			
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("senarioGenerationTime:"+(System.currentTimeMillis()-generationStartTime));
		return scenesTree;
	}
	

	public static void generateScenario(List<String[]> declarations, String modelDeclaration,String query,String fileName,String newFileName,String filePath) throws DocumentException, IOException {
		StringBuilder globalDeclaration=new StringBuilder();
		for(String[] declaration:declarations) {
			if(declaration[1].contains("[0]")) {
				globalDeclaration.append(declaration[0]+" "+declaration[1].replace("[0]", "[1]"));
			}else {
				globalDeclaration.append(declaration[0]+" "+declaration[1]);
			}
			
			if(declaration[2]!=null&&!declaration[2].equals("")) {
				globalDeclaration.append("="+declaration[2]);
			}
			globalDeclaration.append(";\r\n");
		}
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(filePath+fileName));
		Element rootElement=document.getRootElement();
		////全局声明
		Element declarationElement=rootElement.element("declaration");
		declarationElement.setText(globalDeclaration.toString());
		
		///////---------把模型声明和验证公式都放在控制器模型生成上
		////模型声明
		Element modelDeclarationElement=rootElement.element("system");
		modelDeclarationElement.setText(modelDeclaration);
		////仿真验证公式
		Element queriesElement=rootElement.element("queries");
		Element queryElement=queriesElement.element("query");
		if(queryElement!=null) {
			Element formulaElement=queryElement.element("formula");
			if(formulaElement!=null) {
				formulaElement.setText(query);
			}else {
				formulaElement=queryElement.addElement("formula");
				formulaElement.setText(query);
			}
		}else {
			queryElement=queriesElement.addElement("query");
			Element formulaElement=queryElement.addElement("formula");
			formulaElement.setText(query);
		}
		///////---------把模型声明和验证公式都放在控制器模型生成上-----------
		
		OutputStream os=new FileOutputStream(filePath+newFileName);
		OutputFormat format=OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		format.setTrimText(false); //保留换行，但是出现空行
		format.setNewlines(false);
		XMLWriter writer=new XMLWriter(os,format);
		writer.write(document);
		writer.close();
		os.close();
	}


	public static void generateScenario(List<String[]> declarations, String fileName,String newFileName,String filePath) throws DocumentException, IOException {
		StringBuilder globalDeclaration=new StringBuilder();
		long startTime=System.currentTimeMillis();
		for(String[] declaration:declarations) {
			if(declaration[1].contains("[0]")) {
				globalDeclaration.append(declaration[0]+" "+declaration[1].replace("[0]", "[1]"));
			}else {
				globalDeclaration.append(declaration[0]+" "+declaration[1]);
			}
			
			if(declaration[2]!=null&&!declaration[2].equals("")) {
				globalDeclaration.append("="+declaration[2]);
			}
			globalDeclaration.append(";\r\n");
		}
		System.out.println("declarationTime:"+(System.currentTimeMillis()-startTime));
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(filePath+fileName));
		Element rootElement=document.getRootElement();
		////全局声明
		Element declarationElement=rootElement.element("declaration");
		declarationElement.setText(globalDeclaration.toString());
		
		///////---------把模型声明和验证公式都放在控制器模型生成上
//		////模型声明
//		Element modelDeclarationElement=rootElement.element("system");
//		modelDeclarationElement.setText(modelDeclaration);
//		////仿真验证公式
//		Element queriesElement=rootElement.element("queries");
//		Element queryElement=queriesElement.element("query");
//		if(queryElement!=null) {
//			Element formulaElement=queryElement.element("formula");
//			if(formulaElement!=null) {
//				formulaElement.setText(query);
//			}else {
//				formulaElement=queryElement.addElement("formula");
//				formulaElement.setText(query);
//			}
//		}else {
//			queryElement=queriesElement.addElement("query");
//			Element formulaElement=queryElement.addElement("formula");
//			formulaElement.setText(query);
//		}
		///////---------把模型声明和验证公式都放在控制器模型生成上-----------
		
		OutputStream os=new FileOutputStream(filePath+newFileName);
		OutputFormat format=OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		format.setTrimText(false); //保留换行，但是出现空行
		format.setNewlines(false);
		XMLWriter writer=new XMLWriter(os,format);
		writer.write(document);
		writer.close();
		os.close();
	}
	
	public static String getQueryFormula(List<String[]> declarations,String simulationTime) {
		StringBuilder queryFormula=new StringBuilder();
		queryFormula.append("simulate[<="+simulationTime+"] {");
		
		for(int i=0;i<declarations.size();i++) {
			if(!declarations.get(i)[0].contains("chan")) {				
				///只考虑非通道参数
				String dataName=declarations.get(i)[1];
				if(dataName.contains("[")) {
					////设备标识符  如 bulb[2] => bulb[0],bulb[1]
					int num=Integer.parseInt(dataName.substring(dataName.indexOf("["), dataName.indexOf("]")).substring(1));
					///如果设备没有实例化，则不考虑

					dataName="";
					String identifierName=declarations.get(i)[1].substring(0, declarations.get(i)[1].indexOf("["));
					for(int k=0;k<num;k++) {
						if(k<num-1) {
							dataName=dataName+identifierName+"["+k+"],";
						}else {
							dataName=dataName+identifierName+"["+k+"]";
						}
					}
				}
				if(!dataName.equals("")) {
					if(i<declarations.size()-1) {
						queryFormula.append(dataName+",");
					}else {
						queryFormula.append(dataName);
					}
				}
				
			}
			
		}
		queryFormula.append("}");
		
		return queryFormula.toString();
	}
	
	public static List<AttributeValue> setCausalInitialValue(List<String[]> declarations,List<List<Trigger>> attributesSameTriggers,int scenarioIndex) {
		/////返回场景树中各场景下的各属性子节点的取值
		List<AttributeValue> attributeValues=new ArrayList<>();
		for(List<Trigger> triggersWithSameAttribute:attributesSameTriggers) {
			////给每个属性赋值
			int k=scenarioIndex % (triggersWithSameAttribute.size()+1);
			scenarioIndex=scenarioIndex/(triggersWithSameAttribute.size()+1);
			for(String[] declaration:declarations) {
				if(declaration[1].equals(triggersWithSameAttribute.get(0).attrVal[0])) {
					AttributeValue attributeValue=new AttributeValue();
					attributeValue.setName(triggersWithSameAttribute.get(0).attrVal[0]);
					if(k==0) {
						///第一段,value-1
						Double value=Double.parseDouble(triggersWithSameAttribute.get(k).attrVal[2]);
						declaration[2]=String.format("%.1f", value-10);
					}else if(k==triggersWithSameAttribute.size()) {
						///最后一段,value+1
						Double value=Double.parseDouble(triggersWithSameAttribute.get(k-1).attrVal[2]);
						declaration[2]=String.format("%.1f", value+10);
					}else {
						///中间段,中间值
						Double value1=Double.parseDouble(triggersWithSameAttribute.get(k-1).attrVal[2]);
						Double value2=Double.parseDouble(triggersWithSameAttribute.get(k).attrVal[2]);
						declaration[2]=String.format("%.1f", (value1+value2)/2);
					}
					attributeValue.setValue(Double.parseDouble(declaration[2]));
					attributeValues.add(attributeValue);
					break;
				}
			}
		}
		return attributeValues;
	}
	

	//////////计算场景数量
	public static int getScenarioNum(List<List<Trigger>> attributesSameTriggers) {
		////如temperature属性的断点为18、30 && humidity属性的断点为40
		////则能生成3*2个场景
		int scenarioNum=1;
		for(List<Trigger> triggersWithSameAttribute:attributesSameTriggers) {
			scenarioNum=scenarioNum*(triggersWithSameAttribute.size()+1);
		}
		return scenarioNum;
	}
	
	/////////找到涉及相同属性的triggers, 并按照分段点取值大小排序（分段点取值不重复）
	public static List<Trigger> getTriggersWithSameAttribute(String[] declaration,List<Trigger> triggers) {
		///如temperature>30和temperature<18,temperature<=30
		////temperature属性的断点为18、30
		List<Trigger> triggersWithSameAttribute=new ArrayList<>();
		for(Trigger trigger:triggers) {			
			if(trigger.attrVal[0].equals(declaration[1])) {
				////////找到涉及该属性的triggers
				////////保证断点值不重复
				boolean pointValueExist=false;
				for(Trigger sameTrigger:triggersWithSameAttribute) {
					if(trigger.attrVal[2].equals(sameTrigger.attrVal[2])) {
						////////保证断点值不重复
						pointValueExist=true;
					}
				}
				if(!pointValueExist) {
					triggersWithSameAttribute.add(trigger);
				}
			}
		}
		Comparator<Trigger> comparator=new Comparator<Trigger>() {
			/////按分段点取值排序
			/////其中分段点的取值为attrVal[2]
			@Override
			public int compare(Trigger t1, Trigger t2) {
				Double v1=Double.parseDouble(t1.attrVal[2]);
				Double v2=Double.parseDouble(t2.attrVal[2]);
				if(v1<v2) {
					return -1;
				}else {
					return 1;
				}
			}
		};
		//////////对triggers进行排序
		Collections.sort(triggersWithSameAttribute, comparator);		
		return triggersWithSameAttribute;
	}
	
	
	

	public static void generatePersonModel(TemplGraph person,String modelFilePath) throws DocumentException, IOException {
		SAXReader reader= new SAXReader();
		Document document = reader.read(new File(modelFilePath));
		Element rootElement=document.getRootElement();
		List<Element> templateElements=rootElement.elements("template");
		for(Element templateElement:templateElements) {
			Element nameElement=templateElement.element("name");
			if(nameElement.getTextTrim().equals(person.getName())) {
				///是人的模型
				//删除原来的模板
				rootElement.remove(templateElement);				
				break;
			}
		}
		templateElements=rootElement.elements("template");
		///新建新的Person模板
		Element personElement=DocumentHelper.createElement("template");
		templateElements.add(0, personElement);
		Element nameElement=personElement.addElement("name");
		nameElement.setText(person.getName());
		Element parameterElement=personElement.addElement("parameter");
		parameterElement.setText(person.getParameter());
		Element declarationElement=personElement.addElement("declaration");
		declarationElement.setText(person.getDeclaration());
		
		List<Element> transitionElements=new ArrayList<Element>();
		int count=1;
		for(TemplGraphNode node:person.getTemplGraphNodes()) {
			/////location
			Element locationElement=personElement.addElement("location");
			locationElement.addAttribute("id", node.id);
			locationElement.addAttribute("x", 150*count+"");
			locationElement.addAttribute("y", "100");
			if(node.style.equals("committed")) {
				locationElement.addElement("committed");
			}else if(!node.name.equals("")) {
				Element locationNameElement=locationElement.addElement("name");
				locationNameElement.setText(node.name);
				if(!node.getInvariant().equals("")) {
					Element labelElement=locationElement.addElement("label");
					labelElement.addAttribute("kind", "invariant");
					labelElement.setText(node.getInvariant());
				}
			}
			//////// transition
			for(TemplTransition outTransition:node.outTransitions) {
				Element transitionElement=DocumentHelper.createElement("transition");
				Element sourceElement=transitionElement.addElement("source");
				sourceElement.addAttribute("ref", outTransition.source);
				Element targetElement=transitionElement.addElement("target");
				targetElement.addAttribute("ref", outTransition.target);
				if(!outTransition.guard.equals("")) {
					Element guardElement=transitionElement.addElement("label");
					guardElement.addAttribute("kind", "guard");
					guardElement.setText(outTransition.guard);
				}
				if(!outTransition.assignment.equals("")) {
					Element assignmentElement=transitionElement.addElement("label");
					assignmentElement.addAttribute("kind", "assignment");
					assignmentElement.setText(outTransition.assignment);
				}
				transitionElements.add(transitionElement);
			}			
			count++;
		}
		Element initElement=personElement.addElement("init");
		initElement.addAttribute("ref", person.getTemplGraphNodes().get(0).id);
		
		for(Element transitionElement:transitionElements) {
			personElement.add(transitionElement);
		}
		
		OutputStream os=new FileOutputStream(modelFilePath);
		OutputFormat format=OutputFormat.createPrettyPrint();
		format.setEncoding("utf-8");
		format.setTrimText(false);
		format.setNewlines(true);
		XMLWriter writer=new XMLWriter(os,format);
		writer.write(document);
		writer.close();
		os.close();
	}
	
	
	/////////////////////////////////////2020.12.29///////////////////////////////
	//获得属性条件和反转条件
	//////String[0]-----condition String[1]-----reverseCondition
	public static String[] getAttrConAndReverseCon(String trigger){
		String[] conAndReverseCon=new String[2];
		String reverseCondition="";
		String condition=trigger;
		if(trigger.indexOf("FOR")>=0) {
			condition=trigger.substring(0, trigger.indexOf("FOR")).trim();
		}
		if(trigger.indexOf(".")>0) {
			condition=condition.substring(condition.indexOf(".")).substring(".".length());
		}
		if(condition.indexOf(">=")>0) {
			reverseCondition=condition.replace(">=", "<");
		}else if(condition.indexOf(">")>0) {
			reverseCondition=condition.replace(">", "<=");
		}else if(condition.indexOf("<=")>0) {
			reverseCondition=condition.replace("<=", ">");
		}else if(condition.indexOf("<")>0) {
			reverseCondition=condition.replace("<", ">=");
		}else if(condition.indexOf("=")>0) {
			reverseCondition=condition.replace("=", "!=");
			condition=condition.replace("=", "==");
		}
		//距离感应  这块暂时不考虑了
//		if(reverseCondition.indexOf("distanceFrom")>=0) {
//			for(TemplGraph templGraph:templGraphs) {
//				if(templGraph.getDeclaration().indexOf("sensor")>=0 && templGraph.getDeclaration().indexOf("distance")>0) {
//					for(TemplGraphNode templGraphNode:templGraph.getTemplGraphNodes()) {
//						for(TemplTransition inTransition:templGraphNode.inTransitions) {
//							if(inTransition.assignment!=null) {
//								String[] assignments=inTransition.assignment.split(",");
//								for(String assignment:assignments) {
//									if(assignment.indexOf("get()")>0) {
//										assignment=assignment.trim();
//										String attribute=assignment.substring(0, assignment.indexOf("=")).trim();
//										if(reverseCondition.indexOf(">")>0) {
//											String originAttribute=reverseCondition.substring(0, reverseCondition.indexOf(">")).trim();
//											reverseCondition=reverseCondition.replace(originAttribute, attribute);
//											condition=condition.replace(originAttribute, attribute);
//										}
//										if(reverseCondition.indexOf("<")>0) {
//											String originAttribute=reverseCondition.substring(0, reverseCondition.indexOf("<")).trim();
//											reverseCondition=reverseCondition.replace(originAttribute, attribute);
//											condition=condition.replace(originAttribute, attribute);
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//			
//		}

		conAndReverseCon[0]=condition;
		conAndReverseCon[1]=reverseCondition;
		return conAndReverseCon;
	}
	

	
	
	//获得设备状态条件和反转条件
	//////String[0]-----condition String[1]-----reverseCondition
//	public static List<String> getConAndReverseCon(String trigger,List<TemplGraph> templGraphs){
//		List<String> conAndReverseCon=new ArrayList<String>();
//		String reverseCondition=null;
//		String condition=null;
//		String device=null;
//		String state=null;
//		if(trigger.indexOf(".")>0) {
//			state=trigger.substring(trigger.indexOf(".")).substring(".".length());
//			device=trigger.substring(0, trigger.indexOf("."));
//		}
//		//找到该设备状态对应的值/condition
//		for(TemplGraph templGraph:templGraphs) {
//			if(templGraph.getName().equals(device)) {
//				for(TemplGraphNode templGraphNode:templGraph.getTemplGraphNodes()) {
//					if(templGraphNode.name.equals(state)) {
//						if(templGraphNode.inTransitions!=null) {
//							if(templGraphNode.inTransitions.get(0).assignment!=null) {
//								String[] assignments=templGraphNode.inTransitions.get(0).assignment.split(",");
//								//设备参数，首字母小写
//								String deviceParameter=device.substring(0, 1).toLowerCase()+device.substring(1);
//								for(String assignment:assignments) {
//									if(assignment.indexOf(deviceParameter)>=0) {
//										condition=assignment;
//										break;
//									}
//								}
//							}
//						}						
//						break;
//					}
//				}				
//				break;
//			}
//		}
//		if(condition!=null && condition.indexOf("=")>0) {
//			reverseCondition=condition.replace("=", "!=");
//			condition=condition.replace("=", "==");
//		}
//		conAndReverseCon.add(condition);
//		conAndReverseCon.add(reverseCondition);
//		return conAndReverseCon;
//	}
	
	/////////action 的transition生成
//	public static String getControllerAction(String ruleAction,List<DeviceDetail> devices) {
//		String actionStr="";
//		String deviceName="";
//		if(ruleAction.indexOf(".")>0) {
//			deviceName=ruleAction.substring(0, ruleAction.indexOf(".")).trim();
//			actionStr=ruleAction.substring(ruleAction.indexOf(".")).substring(1).trim();
//		}
//		if(!deviceName.equals("")) {
//			for(DeviceDetail device:devices) {
//				if(deviceName.equals(device.getDeviceName())) {
//					actionStr=actionStr+"["+device.getConstructionNum()+"]";
//					break;
//				}
//			}
//		}
//		return actionStr;
//	}
	
	

}
