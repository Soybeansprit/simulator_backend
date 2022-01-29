package com.example.demo.service;

import com.example.demo.bean.*;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.print.Doc;
import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * /////发生在静态分析之后，如果没有静态分析就算了
 * 添加TAP规则后，基于实例层
 * 可生成交互环境模型
 * 可生成控制器模型
 * 最后系统声明，同时分不同场景
 * 写回模型中，一个场景对应一个模型
 * */
@Service
public class SystemModelGenerationService {
    public static void main(String[] args) throws DocumentException, IOException {

        String ruleText="IF co2ppm>800 THEN purifier_0.turn_ap_on\n" +
                "IF co2ppm<=800 THEN purifier_0.turn_ap_off\n" +
                "IF humidity>=50 THEN humidifier_0.turn_hum_off\n" +
                "IF humidity<50 THEN humidifier_0.turn_hum_on\n" +
                "IF ac_0.cool THEN window_0.close_window\n" +
                "IF ac_0.heat THEN window_0.close_window\n" +
                "IF emma.out THEN vacuum_0.turn_vacuum_off\n" +
                "IF fan_0.fon THEN heater_0.turn_heat_off\n" +
                "IF temperature>28 THEN fan_0.turn_fan_on\n" +
                "IF temperature<24 THEN ac_0.turn_ac_heat\n" +
                "IF temperature<28 THEN fan_0.turn_fan_off\n" +
                "IF temperature>24 THEN ac_0.turn_ac_off\n" +
                "IF alarm_0.alarmon THEN window_0.open_window\n" +
                "IF tv_0.tvon THEN audio_0.turn_audio_on\n" +
                "IF brightness>20000 THEN bulb_0.turn_bulb_off\n";
        List<String> locations=new ArrayList<>();
//        locations.add("living_room");
//        locations.add("kitchen");
//        locations.add("bathroom");
//        locations.add("bedroom");
//        locations.add("out");
        ModelLayer modelLayer=ModelLayerService.getModelLayer("D:\\example\\例子\\","ontology.xml","changed-ontology.xml",locations);
        InstanceLayer instanceLayer=InstanceLayerService.getInstanceLayer("D:\\example\\例子\\实验\\","bianhan-information.properties",modelLayer);
//        List<Rule> rules=RuleService.getRuleList(ruleText);
//        HashMap<String,Trigger> triggerMap=getTriggerMapFromRules(rules,instanceLayer);
//        HashMap<String,Action> actionMap=getActionMapFromRules(rules);
//        InstanceLayer interactiveEnvironment=getInteractiveEnvironment(instanceLayer,modelLayer,triggerMap,actionMap);
//        HashMap<String, Instance> interactiveInstanceMap=InstanceLayerService.getInstanceMap(interactiveEnvironment);
//        ///生成IFD
//        StaticAnalysisService.generateIFD(triggerMap,actionMap,rules,interactiveEnvironment,interactiveInstanceMap,"ifd.dot","D:\\example\\例子\\");
//        String[] intoLocationTime=getIntoLocationTime("300",instanceLayer.getHumanInstance());
//        generateCommonModelFile("300",intoLocationTime,"D:\\example\\例子\\","changed-ontology.xml","D:\\example\\例子\\","changed-ontology.xml",instanceLayer,rules,triggerMap,actionMap,interactiveEnvironment,interactiveInstanceMap);
//        List<String[]> attributeValues=new ArrayList<>();
//        String[] attributeValue1={"temperature","35.0"};
//        String[] attributeValue2={"co2ppm","500.0"};
//        String[] attributeValue3={"humidity","40.0"};
//        String[] attributeValue4={"brightness","500.0"};
//        attributeValues.add(attributeValue1);
//        attributeValues.add(attributeValue2);
//        attributeValues.add(attributeValue3);
//        attributeValues.add(attributeValue4);
//        generateSingleScenario("D:\\example\\例子\\","changed-ontology.xml","D:\\example\\例子\\实验\\","yx-es.xml",modelLayer,rules,attributeValues);
//
////        getInteractiveEnvironment(instanceLayer,modelLayer,rules,"D:\\example\\例子\\","changed-ontology.xml");
//        String simulationResult=SimulationService.getSimulationResult(AddressService.UPPAAL_PATH,"D:\\example\\例子\\实验\\","yx-es.xml","windows");
//        List<DataTimeValue> dataTimeValues=SimulationService.parseSimulationResult(simulationResult,instanceLayer,"D:\\example\\例子\\实验\\","yx-es.txt");
////
//        double temperSat=AnalysisService.getSatisfaction("temperature",24,28,dataTimeValues);
//        double humiSat=AnalysisService.getSatisfaction("humidity",50,Double.MAX_VALUE,dataTimeValues);
//        double co2Sat=AnalysisService.getSatisfaction("co2ppm",-Double.MAX_VALUE,800,dataTimeValues);
//        double brightSat=AnalysisService.getSatisfaction("brightness",300,Double.MAX_VALUE,dataTimeValues);
//        System.out.println(temperSat);
//        System.out.println(humiSat);
//        System.out.println(co2Sat);
//        System.out.println(brightSat);
//        System.out.println();


//        List<List<String[]>> deviceStatesDurationList=AnalysisService.getDeviceStatesDurationList(dataTimeValues,instanceLayer);
//        System.out.println(deviceStatesDurationList);
//        List<DeviceConflict> deviceConflicts=AnalysisService.getDevicesConflict(dataTimeValues);
//        System.out.println(deviceConflicts);
//        List<DeviceJitter> deviceJitters=AnalysisService.getDevicesJitter(dataTimeValues,3);
//        System.out.println(deviceJitters);
//        AnalysisService.getPropertyConformDurations(dataTimeValues,"!temperature<8&AirConditioner_0.heat",instanceLayer);
////        List<List<List<String[]>>> allDeviceConflictDirectRules=AnalysisService.getAllDeviceConflictDirectRules(deviceConflicts,dataTimeValues,instanceLayer,rules);
////        for (List<List<String[]>> allConflictTimeAndRelatedRules:allDeviceConflictDirectRules){
////            List<List<String[]>> synthesizedConflictDirectRules=AnalysisService.getConflictDirectRulesSynthesize(allConflictTimeAndRelatedRules);
////            System.out.println(synthesizedConflictDirectRules);
////        }
//        HashMap<String,Rule> ruleHashMap=new HashMap<>();
//        for (Rule rule:rules){
//            ruleHashMap.put(rule.getRuleName(),rule);
//        }
//        HashMap<String,DataTimeValue> dataTimeValueHashMap=new HashMap<>();
//        for (DataTimeValue dataTimeValue:dataTimeValues){
//            dataTimeValueHashMap.put(dataTimeValue.getDataName(),dataTimeValue);
//        }
//        RuleAndPreRule currentRule=new RuleAndPreRule();
//        currentRule.setCurrentRule(ruleHashMap.get("rule2"));
//
//        List<IFDGraph.GraphNode> graphNodes=StaticAnalysisService.parseIFDAndGetIFDNode("D:\\example\\例子\\","ifd.dot");
//        HashMap<String, IFDGraph.GraphNode> graphNodeHashMap=new HashMap<>();
//        for (IFDGraph.GraphNode graphNode:graphNodes){
//            graphNodeHashMap.put(graphNode.getName(),graphNode);
//        }
//        List<RuleAndPreRule> allRulePreRules=AnalysisService.getAllRulePreRules(graphNodeHashMap,ruleHashMap);
//        HashMap<String,RuleAndPreRule> ruleNameRuleAndPreRuleMap=new HashMap<>();
//        for (RuleAndPreRule ruleAndPreRule:allRulePreRules){
//            ruleNameRuleAndPreRuleMap.put(ruleAndPreRule.getCurrentRule().getRuleName(),ruleAndPreRule);
//        }
//        for (DeviceConflict deviceConflict:deviceConflicts){
//            if (deviceConflict.getConflictTimeValues().size()<=0){
//                continue;
//            }
//            for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
//
//                if (deviceConflict.getInstanceName().equals(deviceInstance.getInstanceName())){
//
//                    List<List<List<String[]>>> deviceAllConflictStatesDirectRules=AnalysisService.getDeviceConflictStatesDirectRulesInAScenario(deviceConflict,dataTimeValues,deviceInstance,ruleHashMap);
//                    List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=AnalysisService.getDeviceAllStatesRuleAndPreRules(deviceAllConflictStatesDirectRules,dataTimeValueHashMap,ruleNameRuleAndPreRuleMap,ruleHashMap);
//                    ///综合原因
//                    List<List<DeviceStateAndCausingRules>> synthesizedDeviceAllStatesRuleAndPreRules=AnalysisService.getDeviceConflictStatesCausingRulesSynthesize(deviceAllStatesRuleAndPreRules);
//                    System.out.println(synthesizedDeviceAllStatesRuleAndPreRules);
//                    break;
//                }
//            }
//
//        }
//        for (DeviceJitter deviceJitter:deviceJitters){
//            if (deviceJitter.getJitterTimeValues().size()<=0){
//                continue;
//            }
//            for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
//                if (deviceJitter.getInstanceName().equals(deviceInstance.getInstanceName())){
//                    List<List<List<String[]>>> deviceAllJitterStatesDirectRules=AnalysisService.getDeviceJitterStatesDirectRulesInAScenario(deviceJitter,dataTimeValues,deviceInstance,ruleHashMap);
//                    ///转成DeviceStateAndCausingRules格式
//                    List<List<DeviceStateAndCausingRules>> deviceAllJitterStatesRuleAndPreRules=AnalysisService.getDeviceAllStatesRuleAndPreRules(deviceAllJitterStatesDirectRules,dataTimeValueHashMap,ruleNameRuleAndPreRuleMap,ruleHashMap);
//                    ///综合原因
//                    List<List<DeviceStateAndCausingRules>> synthesizedAllJitterStatesRuleAndPreRules=AnalysisService.getDeviceJitterStatesCausingRulesSynthesize(deviceAllJitterStatesRuleAndPreRules);
//                    System.out.println(synthesizedAllJitterStatesRuleAndPreRules);
//                }
//            }
//        }
//
//
//
//        IFDGraph.GraphNode ruleNode=graphNodeHashMap.get("rule2");
//        AnalysisService.getRulePreRule(ruleNode,graphNodeHashMap,currentRule,ruleHashMap,2.06,dataTimeValueHashMap);
//        System.out.println(currentRule);


        List<DataTimeValue> dataTimeValues=getDataTimeValuesFromTxt("D:\\example\\例子\\实验\\","zwh-weekday-bp.txt");
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.getDataName().indexOf("deviceName")>=0){
                for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
                    if (deviceInstance.getInstanceName().equals(dataTimeValue.getInstanceName())){
                        List<String[]> deviceStatesDuration=AnalysisService.getDeviceStatesDuration(dataTimeValue,deviceInstance);
                        System.out.println("  "+deviceInstance.getInstanceName());
                        for (String[] deviceStateDuration:deviceStatesDuration){
                            System.out.println("     "+deviceStateDuration[1]+" : " +deviceStateDuration[2] +"s");
                        }
                        break;
                    }
                }
            }
        }
//        double temperSat=AnalysisService.getSatisfaction("temperature",10.0,30,dataTimeValues);
//        double humiSat=AnalysisService.getSatisfaction("humidity",60.0,Double.MAX_VALUE,dataTimeValues);
//        double co2Sat=AnalysisService.getSatisfaction("co2ppm",-Double.MIN_VALUE,800,dataTimeValues);
//        double brightSat=AnalysisService.getSatisfaction("brightness",300,Double.MAX_VALUE,dataTimeValues);
//        System.out.println(temperSat);
//        System.out.println(humiSat);
//        System.out.println(co2Sat);
//        System.out.println(brightSat);
//        System.out.println();
    }

    public static List<DataTimeValue> getDataTimeValuesFromTxt(String filePath,String fileName){
        List<DataTimeValue> dataTimeValues=new ArrayList<>();
        try (FileReader fr=new FileReader(filePath+fileName);
             BufferedReader br=new BufferedReader(fr)){

            String line="";
            while((line=br.readLine())!=null) {
                if (line.trim().equals("")){
                    continue;
                }
                int index=line.indexOf(":");
                String dataName=line.substring(0,index);
                DataTimeValue dataTimeValue=new DataTimeValue();
                dataTimeValue.setDataName(dataName);
                if (dataName.indexOf("deviceName")>=0){
                    String instanceName=dataName.substring(dataName.indexOf("deviceName=")+"deviceName=".length(),dataName.indexOf(","));
                    dataTimeValue.setInstanceName(instanceName);
                }
                SimulationService.getDataTimeValue(dataTimeValue,line);
                dataTimeValues.add(dataTimeValue);
            }

        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return dataTimeValues;
    }

//    public static InstanceLayer getInteractiveEnvironment(InstanceLayer instanceLayer,ModelLayer modelLayer, List<Rule> rules,String filePath,String changedModelFileName) throws DocumentException, IOException {
//        InstanceLayer interactiveEnvironment=new InstanceLayer();
//        HashMap<String, Instance> instanceLayerMap=InstanceLayerService.getInstanceMap(instanceLayer);
//        HashMap<String,Instance> interactiveInstanceMap=new HashMap<>();
//        HashMap<String,Trigger> triggerMap=new HashMap<>();
//        HashMap<String,Action> actionMap=new HashMap<>();
//        for (Rule rule:rules){
//            ///遍历规则抽取实例
//            ///遍历trigger
//            for (String triggerContent:rule.getTrigger()){
//                ///解析trigger
//                Trigger trigger=getTriggerFromTriggerContent(triggerContent,triggerMap,instanceLayerMap,instanceLayer);
//                triggerMap.putIfAbsent(triggerContent,trigger);
//                trigger.getRelatedRules().add(rule);
//                ///trigger涉及到哪些实例
//                ///trigger所属实例
//                String instanceName=trigger.getInstanceName();
//                if (!instanceName.equals("")){
//                    ///像时间这种属性不属于任意一个实体
//                    interactiveInstanceMap.putIfAbsent(instanceName,instanceLayerMap.get(instanceName));
//                }
//
//                ///相关传感器
//                String sensorName=trigger.getSensor();
//                if (!sensorName.equals("")){
//                    ///像可控设备这种不被传感器检测属性
//                    interactiveInstanceMap.putIfAbsent(sensorName,instanceLayerMap.get(sensorName));
//                }
//            }
//
//            ///遍历action
//            for (String actionContent:rule.getAction()){
//                Action action=getActionFromActionContent(actionContent,actionMap);
//                actionMap.putIfAbsent(actionContent,action);
//                action.getRelatedRules().add(rule);
//                ///action涉及到哪个实例  //设备、cyber service
//                String instanceName=action.getInstanceName();
//                interactiveInstanceMap.putIfAbsent(instanceName,instanceLayerMap.get(instanceName));
//            }
//
//        }
//        ///生成IFD
//        ////先生成交互环境模型
//        for (Map.Entry<String, Instance> instanceEntry:interactiveInstanceMap.entrySet()){
//            Instance instance=instanceEntry.getValue();
//            if (instance instanceof HumanInstance){
//                interactiveEnvironment.setHumanInstance((HumanInstance) instance);
//            }else if (instance instanceof AttributeEntityInstance){
//                interactiveEnvironment.setAttributeEntityInstance((AttributeEntityInstance) instance);
//            }else if (instance instanceof UncertainEntityInstance){
//                interactiveEnvironment.getUncertainEntityInstances().add((UncertainEntityInstance) instance);
//            }else if (instance instanceof CyberServiceInstance){
//                interactiveEnvironment.getCyberServiceInstances().add((CyberServiceInstance) instance);
//            }else if (instance instanceof DeviceInstance){
//                interactiveEnvironment.getDeviceInstances().add((DeviceInstance) instance);
//            }else if (instance instanceof SensorInstance){
//                interactiveEnvironment.getSensorInstances().add((SensorInstance) instance);
//            }
//        }
//
//        ///生成IFD
//        StaticAnalysisService.generateIFD(triggerMap,actionMap,rules,interactiveEnvironment,interactiveInstanceMap,"ifd.dot","D:\\example\\例子\\");
//
//        ///生成控制器模型
//        SAXReader reader= new SAXReader();
//        Document document = reader.read(new File(filePath+changedModelFileName));
//        Element rootElement=document.getRootElement();
////		System.out.println(rootElement.getName());
//        List<Element> templateElements=rootElement.elements("template");
//        ///同时把人的模型写进去
//        Element humanElement=DocumentHelper.createElement("template");
//        getHumanElement(instanceLayer.getHumanInstance().getHuman(), humanElement);
//        templateElements.add(0,humanElement);
//
//        ///生成控制器模型,并写入
//        for (Rule rule:rules){
//            Element controllerElement=DocumentHelper.createElement("template");
//            getControllerElement(controllerElement,rule,triggerMap,actionMap,interactiveInstanceMap);
//            templateElements.add(0,controllerElement);
//        }
//
//        ///系统声明
//        String[] intoLocationTime={"60.0,120.0,180.0,240.0"};
//        String modelDeclaration=getModelDeclaration(interactiveEnvironment,rules,intoLocationTime);
//
//        String query=getQuery(instanceLayer,rules,"300");
//        setSystemDeclaration(rootElement,modelDeclaration,"",query);
//
//        ///写入xml文件中
//        writeTheXMLFile(document,filePath,changedModelFileName);
//
//
//        generateMultiScenariosAccordingToTriggers(filePath,changedModelFileName,"D:\\example\\例子\\test\\",modelLayer,rules,triggerMap);
//
//        return interactiveEnvironment;
//    }

//    public static void generateSystemModel(String filePath,String fileName,InstanceLayer interactiveEnvironment,ModelLayer modelLayer,)

    ///写入xml文件中
    public static void writeTheXMLFile(Document document,String filePath,String fileName) throws IOException {
        ///写入xml文件中
        OutputStream os=new FileOutputStream(filePath+fileName);
        OutputFormat format=OutputFormat.createPrettyPrint();
        format.setEncoding("utf-8");
        format.setTrimText(false);
        format.setNewlines(true);
        XMLWriter writer=new XMLWriter(os,format);
        writer.write(document);
        writer.close();
        os.close();
    }

    ///生成控制器模型，需要写回xml文件中，同时人模型可在这个时候写入
    ///要添加涉及时间的控制器，前面规则处理也要重新处理
    ///添加一个time全局变量
    public static void getControllerElement(Element controllerElement,Rule rule,HashMap<String,Trigger> triggerMap,HashMap<String,Action> actionMap,HashMap<String,Instance> interactiveInstanceMap){
        Element nameElement=controllerElement.addElement("name");
        //模型名为：RuleName
        nameElement.setText("Rule"+rule.getRuleName().substring("rule".length()));
        //模型的声明declaration,声明局部变量t
        Element declarationElement=controllerElement.addElement("declaration");
        declarationElement.setText("clock t;");
        //初始节点  //初始节点具有不变式t<=1，周期性执行
        Element startElement=controllerElement.addElement("location");
        ///初始节点id、位置、具有不变式t<=1
        getLocationElement(startElement,"id0","-300","0","","t<=1","");

        //第一个节点为初始节点
        Element initElement=controllerElement.addElement("init");
        initElement.addAttribute("ref", "id0");

        List<Element> locationElements=controllerElement.elements("location");

        List<Element> transitionElements=controllerElement.elements("transition");
        //中间节点
        int count=1; //节点计数
        //判断条件的节点
        for(int i=0;i<rule.getTrigger().size();i++) {
            String triggerContent=rule.getTrigger().get(i);

            ///找到对应的trigger解析结果
            Trigger trigger=triggerMap.get(triggerContent);
            String satGuard=getSatGuardFromTrigger(triggerContent,interactiveInstanceMap,triggerMap);  ///对应trigger满足的guard
            String unsatGuard=getUnsatGuardFromSatGuard(satGuard);  ////对应trigger不满足的guard


            ///创建trigger节点和从该节点出发的两条边，一条满足，指向下一个节点，一条不满足，指向初始节点
            getTriggerLocationTransition(locationElements,transitionElements,"id0",-300,0,count,
                    satGuard,i==rule.getTrigger().size()-1&&trigger.getForTime().equals("")?rule.getRuleName()+"=1,t=0":"t=0","",
                    unsatGuard,rule.getRuleName()+"=0,t=0","");

            if(i==0) {
                //如果为第一个trigger判断节点，
                //则从初始节点到该节点有个transition
                //transition guard为t>=1
                Element firstTransitionElement=DocumentHelper.createElement("transition");
                getTransitionElement(firstTransitionElement,"id0","id"+count,"","t>=1","t=0","");
                transitionElements.add(0,firstTransitionElement);
            }


            count++;
            //////////对于有for存在的trigger，再创建一个节点，在该节点上等待一段时间后，再判断该trigger是否满足
            if (!trigger.getForTime().equals("")){
                ///for节点，等待特定时间
                Element forLocationElement=DocumentHelper.createElement("location");
                getLocationElement(forLocationElement,"id"+count,""+(-300+count*150),"0","","t<="+trigger.getForTime(),"");
                locationElements.add(0,forLocationElement);
                Element forTransitionElement=DocumentHelper.createElement("transition");
                getTransitionElement(forTransitionElement,"id"+count,"id"+(count+1),"","t>="+trigger.getForTime(),"t=0","");
                transitionElements.add(0,forTransitionElement);
                ///for之后再添加相同trigger节点判断是否满足
                count++;
                ///再创建trigger节点和从该节点出发的两条边，一条满足，指向下一个节点，一条不满足，指向初始节点
                getTriggerLocationTransition(locationElements,transitionElements,"id0",-300,0,count,
                        satGuard,i==rule.getTrigger().size()-1?rule.getRuleName()+"=1,t=0":"t=0","",
                        unsatGuard,rule.getRuleName()+"=0,t=0","");
                count++;
            }



        }

        //action的节点
        for(int i=0;i<rule.getAction().size();i++) {
            //action节点
            String actionContent=rule.getAction().get(i);
            Element locationElement=DocumentHelper.createElement("location");
            getLocationElement(locationElement,"id"+count,""+(-300+count*150),"0","","","committed");
            locationElements.add(0,locationElement);

            Element transitionElement=DocumentHelper.createElement("transition");


            //================================不考虑 for=======================
//				String[] actionTime=action.split("for");
//				if(actionTime[0].indexOf(".")>0)
//				actionTime[0]=actionTime[0].substring(actionTime[0].indexOf(".")).substring(1).trim();
//				if(action.indexOf("for")>0) {
//					if(actionTime[0].indexOf(".")>0)
//					actionTime[1]=actionTime[1].substring(actionTime[0].indexOf(".")).substring(1).trim();
//				}
            //================================不考虑 for=======================

            String synchronisation="";   ///信号通道
            Action action=actionMap.get(actionContent);   ///找到对应的action
            Instance instance=interactiveInstanceMap.get(action.getInstanceName());  ///找到所属实例
            if (instance instanceof DeviceInstance){
                DeviceInstance deviceInstance=(DeviceInstance) instance;
                DeviceType deviceType=deviceInstance.getDeviceType();
                for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
                    if (stateSyncValueEffect.getSynchronisation().equals(action.getSync())){
                        ///找到对应状态的状态标识符取值
                        synchronisation=stateSyncValueEffect.getSynchronisation()+"["+deviceInstance.getSequenceNumber()+"]";
                        break;
                    }
                }
            }else if (instance instanceof CyberServiceInstance){
                synchronisation=action.getSync();
            }
            System.out.println("synchronisation:"+synchronisation);


            if(i<rule.getAction().size()-1) {
                //action 可能包含for表示经过多长时间后进行下一个
                //without for
                getTransitionElement(transitionElement,"id"+count,"id"+(1+count),synchronisation+"!","","","");


                //================================不考虑 for=======================
//					if(action.indexOf("for")>0) {
//						//for time 节点
//						Element nextLocationElement=DocumentHelper.createElement("location");
//						nextLocationElement.addAttribute("id", "id"+(2+count));
//						locationElements.add(0,nextLocationElement);
//						//位置
//						nextLocationElement.addAttribute("x", ""+(-300+(count+1)*150));
//						nextLocationElement.addAttribute("y", "0");
//						//该节点到下一节点的transition
//						Element nextTransitionElement=DocumentHelper.createElement("transition");
//						Element nextSourceElement=nextTransitionElement.addElement("source");
//						Element nextTargetElement=nextTransitionElement.addElement("target");
//						nextSourceElement.addAttribute("ref", "id"+(2+count));
//						nextTargetElement.addAttribute("ref", "id"+(3+count));
//						String time=actionTime[1].substring(0, actionTime[1].indexOf("s"));
//						Element invariantElement=nextLocationElement.addElement("label");
//						invariantElement.addAttribute("kind", "invariant");
//						invariantElement.setText("t<="+time);
//						Element assignmentElement =nextTransitionElement.addElement("label");
//						assignmentElement.addAttribute("kind", "assignment");
//						assignmentElement.setText("t=0");
//						Element guardElement=nextTransitionElement.addElement("label");
//						guardElement.addAttribute("kind", "guard");
//						guardElement.setText("t>="+time);
//						transitionElements.add(0,nextTransitionElement);
//						count++;
//					}
                //================================不考虑 for=======================
            }
            if(i==rule.getAction().size()-1) {
                //最后一个action节点与end连接
                getTransitionElement(transitionElement,"id"+count,"id0",synchronisation+"!","","","");

                Element finalNailElement=transitionElement.addElement("nail");
                finalNailElement.addAttribute("x", ""+(-300+count*150));
                finalNailElement.addAttribute("y", "-50");

                Element nailElement0=transitionElement.addElement("nail");
                nailElement0.addAttribute("x", "-300");
                nailElement0.addAttribute("y", "-50");
            }
            transitionElements.add(0,transitionElement);
            count++;
        }
    }

    ////获得满足trigger的guard
    public static String getSatGuardFromTrigger(String triggerContent,HashMap<String,Instance> interactiveInstanceMap,HashMap<String,Trigger> triggerMap){
        ///找到对应的trigger解析结果
        Trigger trigger=triggerMap.get(triggerContent);
        String satGuard="";  ///对应trigger满足的guard
        ///获得满足trigger对应的guard
        if (trigger.getTriggerForm()[1].equals(".")){
            Instance instance=interactiveInstanceMap.get(trigger.getInstanceName());
            if (instance instanceof HumanInstance){
                ///如果是人
                HumanInstance humanInstance=(HumanInstance) instance;
                Human human=humanInstance.getHuman();
                for (String[] stateValue:human.getStateValues()){
                    if (stateValue[0].equals(trigger.getTriggerForm()[2])){
                        ///找到对应状态的状态标识符取值  //////stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值
                        satGuard=human.getIdentifier()+"=="+stateValue[2];
                        break;
                    }
                }
            }else if (instance instanceof DeviceInstance){
                ///如果是设备
                DeviceInstance deviceInstance=(DeviceInstance) instance;
                DeviceType deviceType=deviceInstance.getDeviceType();
                for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
                    if (stateSyncValueEffect.getStateName().equals(trigger.getTriggerForm()[2])){
                        ///找到对应状态的状态标识符取值
                        satGuard=deviceType.getIdentifier()+"["+deviceInstance.getSequenceNumber()+"]=="+stateSyncValueEffect.getValue();
                        break;
                    }
                }
            }else if (instance instanceof UncertainEntityInstance){
                ///如果是不确定实体
                UncertainEntityInstance uncertainEntityInstance=(UncertainEntityInstance) instance;
                UncertainEntityType uncertainEntityType=uncertainEntityInstance.getUncertainEntityType();
                for (String[] stateValue:uncertainEntityType.getStateValues()){
                    if (stateValue[0].equals(trigger.getTriggerForm()[2])){
                        ///找到对应状态的状态标识符取值  //////stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值
                        satGuard=uncertainEntityType.getIdentifier()+"=="+stateValue[2];
                        break;
                    }
                }
            }
        }else{
            ///attribute<(<=,>,>=)value格式
            satGuard=triggerContent;
        }
        return satGuard;
    }

    ///获得不满足trigger的guard
    public static String getUnsatGuardFromSatGuard(String satGuard){
        String unsatGuard="";  ////对应trigger不满足的guard
        ///对unsatGuard的处理
        if (satGuard.contains("==")){
            unsatGuard=satGuard.replace("==","!=");
        }else if (satGuard.contains(">=")){
            unsatGuard=satGuard.replace(">=","<");
        }else if (satGuard.contains(">")){
            unsatGuard=satGuard.replace(">","<=");
        }else if (satGuard.contains("<=")){
            unsatGuard=satGuard.replace("<=",">");
        }else if (satGuard.contains("<")){
            unsatGuard=satGuard.replace("<",">=");
        }

        return unsatGuard;
    }

    ////生成trigger节点即其两条边，triggerLocation出发有两条边，一条指向下一个节点，另一条指向初始节点
    public static void getTriggerLocationTransition(List<Element> locationElements,List<Element> transitionElements,String initId,int initX,int initY,int count,
                                                    String satGuard,String satAssignment,String satSynchronisation,
                                                    String unsatGuard,String unsatAssignment,String unsatSynchronisation){
        //条件判断节点，对用一个trigger节点
        Element locationElement=DocumentHelper.createElement("location");
        //trigger对应的节点，节点id、位置、属于committed节点
        getLocationElement(locationElement,"id"+count,""+(initX+count*150),"0","","","committed");
        ///添加location节点
        locationElements.add(0,locationElement);

        ///从该节点有两条transition，一条表示满足，指向下一个节点，另一条表示不满足，指向初始节点
        //满足条件的transition
        Element satTransitionElement=DocumentHelper.createElement("transition");

        //不满足条件的transition，从该节点指向初始节点
        Element unsatTransitionElement=DocumentHelper.createElement("transition");
        ///条件满足的transition，从当前节点指向下一个几点，guard为满足的guard
        getTransitionElement(satTransitionElement,"id"+count,"id"+(1+count),satSynchronisation,satGuard,satAssignment,"");
        ///条件不满足的transition，从当前接待你指向初始节点，guard为不满足的guard，并将标识符置为0
        getTransitionElement(unsatTransitionElement,"id"+count,initId,unsatSynchronisation,unsatGuard,unsatAssignment,"");


        ///将两个transition元素添加
        transitionElements.add(0,satTransitionElement);
        transitionElements.add(0,unsatTransitionElement);
        //增加两个nail,为了美观
        Element unsatNailElement0=unsatTransitionElement.addElement("nail");
        unsatNailElement0.addAttribute("x", ""+(initX+count*150));
        unsatNailElement0.addAttribute("y", ""+(initY+count*50));
        Element unsatNailElement1=unsatTransitionElement.addElement("nail");
        unsatNailElement1.addAttribute("x", ""+initX);
        unsatNailElement1.addAttribute("y", ""+(initY+count*50));

    }

    ///根据人模型信息，生成xml中的元素
    public static void getHumanElement(Human human, Element humanElement){
        Element nameElement=humanElement.addElement("name");
        nameElement.setText(human.getTypeName());  ///模型名

        ///parameter
        Element parameterElement=humanElement.addElement("parameter");
        StringBuilder parameter=new StringBuilder();  ///传参部分

        ///declaration
        Element declarationElement=humanElement.addElement("declaration");
        StringBuilder declaration=new StringBuilder();
        declaration.append("//human\nclock t;\n" );
        declarationElement.setText(declaration.toString());

        ///先创建一个init location
        Element initLocationElement=humanElement.addElement("location");
        getLocationElement(initLocationElement,"id0","150","0","","","committed");

        ///init
        Element initElement=humanElement.addElement("init");
        initElement.addAttribute("ref", "id0");

        ///locations
        List<Element> locationElements=humanElement.elements("location");
        ///transitions
        List<Element> transitionElements=new ArrayList<Element>();
        //////stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值
        for (int i=0;i<human.getStateValues().size();i++){
            String[] stateValue=human.getStateValues().get(i);
            ///location
            Element locationElement=DocumentHelper.createElement("location");

            if (i<human.getStateValues().size()-1){
                ///不是最后一个location，存在不变式 time<=t1、t2.。。 t1、t2。。。是参数

                getLocationElement(locationElement,stateValue[1],150*(i+2)+"","0",stateValue[0],"time<=t"+(i+1),"");
                ///parameter
                if (i>0){
                    parameter.append(", ");
                }
                parameter.append("double t"+(i+1));
            }else {
                getLocationElement(locationElement,stateValue[1],150*(i+2)+"","0",stateValue[0],"","");
            }
            locationElements.add(0,locationElement);

            ///transition
            Element transitionElement= DocumentHelper.createElement("transition");

            ///guard
            if (i>0){
                ///最后一个transition没有guard
                getTransitionElement(transitionElement,"id"+i,stateValue[1],"","time>=t"+i,human.getIdentifier()+"="+stateValue[2],"");
            }else {
                getTransitionElement(transitionElement,"id"+i,stateValue[1],"","",human.getIdentifier()+"="+stateValue[2],"");
            }
            transitionElements.add(0,transitionElement);
//            ///assignment

        }

        parameterElement.setText(parameter.toString());

        for(Element transitionElement:transitionElements) {
            humanElement.add(transitionElement);
        }
    }
    ////transitionElement的处理，source、target、synchronisation、guard、assignment、select
    public static void getTransitionElement(Element transitionElement,String sourceId,String targetId,String synchronisation,String guard,String assignment,String select){
        if (!sourceId.equals("")){
            Element sourceElement=transitionElement.addElement("source");
            sourceElement.addAttribute("ref", sourceId);   ///源节点
        }
        if (!targetId.equals("")){
            Element targetElement=transitionElement.addElement("target");
            targetElement.addAttribute("ref", targetId);   ///目标节点
        }
        if (!select.equals("")){
            Element selectLabelElement=transitionElement.addElement("select");
            selectLabelElement.setText(select);
        }
        if (!guard.equals("")){
            Element guardLabelElement=transitionElement.addElement("label");
            guardLabelElement.addAttribute("kind", "guard");
            guardLabelElement.setText(guard);
        }
        if (!synchronisation.equals("")){
            Element synchronisationLabelElement=transitionElement.addElement("label");
            synchronisationLabelElement.addAttribute("kind", "synchronisation");
            synchronisationLabelElement.setText(synchronisation);
        }
        if (!assignment.equals("")){
            Element assignmentLabelElement=transitionElement.addElement("label");
            assignmentLabelElement.addAttribute("kind", "assignment");
            assignmentLabelElement.setText(assignment);
        }
    }
    ////locationElement的处理，id、name、invariant、type【committed、initial、urgent】
    public static void getLocationElement(Element locationElement,String locationId,String locationX,String locationY,String locationName,String invariant,String type){
        locationElement.addAttribute("id", locationId);
        //初始节点位置
        if (!locationX.equals("")){
            locationElement.addAttribute("x", locationX);
        }
        if (!locationY.equals("")){
            locationElement.addAttribute("y", locationY);
        }
        ///节点名
        if (!locationName.equals("")){
            Element nameElement=locationElement.addElement("name");
            nameElement.setText(locationName);
        }
        //不变式
        if (!invariant.equals("")){
            Element invariantLabelElement0=locationElement.addElement("label");
            invariantLabelElement0.addAttribute("kind", "invariant");
            invariantLabelElement0.setText(invariant);
        }
        ///committed或urgent节点
        if (!type.equals("")){
            locationElement.addElement(type);
        }

    }



    ///生成系统声明，需写回xml文件中，会有n个系统模型
    // 其中全局变量是不同场景不同的，但是其他所有场景都是一样的，只要交互环境相同。
    ///先将其他部分写入，然后再每个场景重写。

    ////设置模型声明、信道声明、全局变量声明，以及query。
    public static void setSystemDeclaration(Element rootElement,String modelDeclaration,String declaration,String query){
        ///全局声明和信号通道
        if (!declaration.equals("")){
            Element declarationElement=rootElement.element("declaration");
            declarationElement.setText(declaration);
        }

        ///模型声明
        if (!modelDeclaration.equals("")){
            Element systemElement=rootElement.element("system");
            systemElement.setText(modelDeclaration);
        }

        ///设置query
        /**
         * 	<queries>
         * 		<query>
         * 			<formula>simulate[&lt;=300] {temperature,coppm,position,bulb[0],window[0],airPurifier[0],airConditioner[0],rule1,rule2,rule3,rule4,time}</formula>
         * 			<comment></comment>
         * 		</query>
         * 	</queries>
         * */
        if (!query.equals("")){
            Element queriesElement=rootElement.element("queries");
            if (queriesElement==null){
                queriesElement=rootElement.addElement("queries");
            }
            Element queryElement=queriesElement.element("query");
            if (queryElement==null){
                queryElement=queryElement.addElement("query");
            }
            Element formulaElement=queryElement.element("formula");
            if (formulaElement==null){
                formulaElement=queryElement.addElement("formula");
            }
            formulaElement.setText(query);
            if (queryElement.element("comment")==null){
                queryElement.addElement("comment");
            }
        }

    }

    ///模型声明，声明实例，并声明需要执行的模型，需要输入交互环境，执行的规则，人进入每个房间的时间点
    public static String getModelDeclaration(InstanceLayer interactiveEnvironment, List<Rule> rules,String[] intoLocationTime){
        StringBuilder modelDeclarationSb=new StringBuilder();  ///声明实例模型
        StringBuilder systemSb=new StringBuilder();
        systemSb.append("system ");  ///声明执行模型
        ///人的声明，需要确定参数
        HumanInstance humanInstance=interactiveEnvironment.getHumanInstance();
        if (!humanInstance.getInstanceName().equals("")){
            ///交换环境中有human
            String humanInstanceName="";
            if (!humanInstance.getInstanceName().equals(humanInstance.getEntityTypeName())){
                //实例名与类型名不同
                humanInstanceName=humanInstance.getInstanceName();
            }else {
                //实例名与类型名相同
                humanInstanceName=humanInstance.getInstanceName()+"Instance";
            }
            ///声明实例模型
            modelDeclarationSb.append(humanInstanceName);
            modelDeclarationSb.append("=");
            modelDeclarationSb.append(humanInstance.getEntityTypeName());
            modelDeclarationSb.append("(");
            ///参数值传入，也就是各时间点
            for (int i=0;i<intoLocationTime.length;i++){
                if (i>0){
                    modelDeclarationSb.append(",");
                }
                modelDeclarationSb.append(intoLocationTime[i]);
            }
            modelDeclarationSb.append(");\n");

            ///声明执行模型
            systemSb.append(humanInstanceName);
            systemSb.append(",");
        }
        ///attribute实体的声明
        AttributeEntityInstance attributeEntityInstance=interactiveEnvironment.getAttributeEntityInstance();
        if(!attributeEntityInstance.getInstanceName().equals("")){
            ///声明实例模型和执行模型
            getModelDeclarationForAttrUncertainCyberInstance(attributeEntityInstance,modelDeclarationSb,systemSb);
        }
        ///不确定实体实例的声明
        for (UncertainEntityInstance uncertainEntityInstance:interactiveEnvironment.getUncertainEntityInstances()){
            ///声明实例模型和执行模型
            getModelDeclarationForAttrUncertainCyberInstance(uncertainEntityInstance,modelDeclarationSb,systemSb);
        }
        ///设备实例的声明
        for (DeviceInstance deviceInstance:interactiveEnvironment.getDeviceInstances()){
            ///实例名与类型名不同，需要声明
            ///声明实例模型
            modelDeclarationSb.append(deviceInstance.getInstanceName());
            modelDeclarationSb.append("=");
            modelDeclarationSb.append(deviceInstance.getEntityTypeName());
            modelDeclarationSb.append("(");
            modelDeclarationSb.append(deviceInstance.getSequenceNumber());  ///传入参数
            modelDeclarationSb.append(");\n");

            ///声明执行模型
            systemSb.append(deviceInstance.getInstanceName());
            systemSb.append(",");
        }
        ///cyber service实例的声明
        for (CyberServiceInstance cyberServiceInstance:interactiveEnvironment.getCyberServiceInstances()){
            ///声明实例模型和执行模型
            getModelDeclarationForAttrUncertainCyberInstance(cyberServiceInstance,modelDeclarationSb,systemSb);
        }
        ///传感器模型不声明实例也声明执行

        ///声明执行的控制器模型
        for (int i=0;i<rules.size();i++){
            String ruleName=rules.get(i).getRuleName();
            ruleName=ruleName.substring(0,1).toUpperCase()+ruleName.substring(1);
            systemSb.append(ruleName);
            if (i<rules.size()-1){
                systemSb.append(",");
            }
        }
        systemSb.append(";\n");
        ///实例模型声明后再添加执行模型声明
        modelDeclarationSb.append(systemSb);

        return modelDeclarationSb.toString();
    }

    ////该方法声明实例模型和执行模型, 由于attribute实体、不确定实体、cyber service实体是没有参数的，声明实例时不需要传参
    public static void getModelDeclarationForAttrUncertainCyberInstance(Instance instance,StringBuilder modelDeclarationSb,StringBuilder systemSb){
        if (!instance.getInstanceName().equalsIgnoreCase(instance.getEntityTypeName())){
            ///如果实例名与类型名不同，需要声明
            ///声明实例模型
            modelDeclarationSb.append(instance.getInstanceName());
            modelDeclarationSb.append("=");
            modelDeclarationSb.append(instance.getEntityTypeName());
            modelDeclarationSb.append("();\n");
        }
        ///声明执行模型
        systemSb.append(instance.getInstanceName());
        systemSb.append(",");
    }

    ///全局声明，包括信道声明和全局变量声明,不能只声明执行的实例，需要对所有模型的变量进行声明
    ///attribute不在此处赋值，因为attribute在不同的场景中是不同的
    public static String getGeneralDeclaration(ModelLayer modelLayer,List<Rule> rules){
        StringBuilder declarationSb=new StringBuilder();
        ///全局变量声明包括状态标识符、属性、属性总变化率
        ///信道声明包括设备同步信道、cyber service 同步信道
        ///模型层涉及到的模型都需要声明，无论有没有对应实例
        Human human=modelLayer.getHuman();
        if (!human.getTypeName().equals("")){
            //人状态标识符
            declarationSb.append("int ");
            declarationSb.append(human.getIdentifier());
            declarationSb.append(";\n");
        }
        for (UncertainEntityType uncertainEntityType:modelLayer.getUncertainEntityTypes()){
            //不确定实体状态标识符
            declarationSb.append("int ");
            declarationSb.append(uncertainEntityType.getIdentifier());
            declarationSb.append(";\n");
        }
        for (DeviceType deviceType:modelLayer.getDeviceTypes()){
            //设备状态标识符
            declarationSb.append("int ");
            declarationSb.append(deviceType.getIdentifier());
            declarationSb.append("[");
            if (deviceType.getInstanceNumber()>0){
                ///该类设备存在实例
                declarationSb.append(deviceType.getInstanceNumber());
            }else {
                ///就算该设备没有实例也需要声明一个
                declarationSb.append("1");
            }
            declarationSb.append("];\n");
            //设备synchronisation
            for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
                declarationSb.append("urgent broadcast chan ");
                declarationSb.append(stateSyncValueEffect.getSynchronisation());
                declarationSb.append("[");
                if (deviceType.getInstanceNumber()>0){
                    ///该类设备存在实例
                    declarationSb.append(deviceType.getInstanceNumber());
                }else {
                    ///就算该设备没有实例也需要声明一个
                    declarationSb.append("1");
                }

                declarationSb.append("];\n");
            }
        }
        for (CyberServiceType cyberServiceType:modelLayer.getCyberServiceTypes()){
            //cyber service synchronisation
            for(String[] stateSync:cyberServiceType.getStateSyncs()){
                ///stateSync[0]=stateId,stateSync[1]=synchronization,该synchronisation是带符号的，？或者！
                String synchronisation=stateSync[1];
                synchronisation=synchronisation.substring(0,synchronisation.length()-1);
                ///无论有没有该实例都需要声明通道
                declarationSb.append("urgent broadcast chan ");
                declarationSb.append(synchronisation);
                declarationSb.append(";\n");

            }
        }
        for (Rule rule:rules){
            ///控制器的标识符
            declarationSb.append("int ");
            declarationSb.append(rule.getRuleName());
            declarationSb.append("=0;\n");
        }
        ///attribute实体的变量声明，此处只声明总变化率，因为attribute的取值不同系统模型也是不同的
        AttributeEntityType attributeEntityType=modelLayer.getAttributeEntity();
        if (!attributeEntityType.getTypeName().equals("")){
            for (AttributeEntityType.Attribute attribute:attributeEntityType.getAttributes()){
                ///声明总变化率
                declarationSb.append("double ");
                declarationSb.append(attribute.getDelta());
                declarationSb.append("=0.0;\n");
//                ///attribute声明并赋值
//                for (String[] attributeValue:attributeValues){
//                    if (attributeValue[0].equals(attribute.getAttribute())){
//                        declarationSb.append("clock ");
//                        declarationSb.append(attribute.getAttribute());
//                        declarationSb.append("=");
//                        declarationSb.append(attributeValue[1]);
//                        break;
//                    }
//                }
            }
        }
        declarationSb.append("clock time=0.0;\n");
        return declarationSb.toString();
        ///attribute的声明，会产生多个场景，每个场景不同的取值
    }

    ///给attribute赋值   attributeValue[0]=attribute, attributeValues[1]=value
    public static String getAttributeDeclaration(AttributeEntityType attributeEntityType,List<String[]> attributeValues,String sameDeclaration){
        StringBuilder declarationSb=new StringBuilder();
        for (AttributeEntityType.Attribute attribute:attributeEntityType.getAttributes()){
            boolean existValue=false;
            for (String[] attributeValue:attributeValues){
                if (attributeValue[0].equals(attribute.getAttribute())){
                    ///存在该属性的赋值
                    existValue=true;
                    declarationSb.append("clock ");
                    declarationSb.append(attribute.getAttribute());
                    declarationSb.append("=");
                    declarationSb.append(attributeValue[1]);
                    declarationSb.append(";\n");
                    break;
                }
            }
            if (!existValue){
                ///不存在该属性的赋值
                declarationSb.append("clock ");
                declarationSb.append(attribute.getAttribute());
                declarationSb.append("=0.0;\n");
            }
        }
        declarationSb.append(sameDeclaration);
        return declarationSb.toString();
    }

    ///设置仿真query,设置仿真时间，以及要获取的仿真参数,实例层作为输入，获得所有实例的状态标识符的数据
    public static String getQuery(InstanceLayer instanceLayer,List<Rule> rules,String simulationTime){
        StringBuilder querySb=new StringBuilder();
        querySb.append("simulate[<=");
        querySb.append(simulationTime);
        querySb.append("] {");
        if (!instanceLayer.getHumanInstance().getInstanceName().equals("")){
            HumanInstance humanInstance=instanceLayer.getHumanInstance();
            querySb.append(humanInstance.getHuman().getIdentifier());
            querySb.append(",");
        }
        if (!instanceLayer.getAttributeEntityInstance().getInstanceName().equals("")){
            AttributeEntityInstance attributeEntityInstance=instanceLayer.getAttributeEntityInstance();
            for (AttributeEntityType.Attribute attribute:attributeEntityInstance.getAttributeEntityType().getAttributes()){
                querySb.append(attribute.getAttribute());
                querySb.append(",");
            }
        }
        for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
            querySb.append(deviceInstance.getDeviceType().getIdentifier());
            querySb.append("[");
            querySb.append(deviceInstance.getSequenceNumber());
            querySb.append("],");
        }
        for (UncertainEntityInstance uncertainEntityInstance:instanceLayer.getUncertainEntityInstances()){
            querySb.append(uncertainEntityInstance.getUncertainEntityType().getIdentifier());
            querySb.append(",");
        }
        for (int i=0;i<rules.size();i++){
            if (i< rules.size()-1){
                querySb.append(rules.get(i).getRuleName());
                querySb.append(",");
            }else {
                querySb.append(rules.get(i).getRuleName());
            }
        }
        querySb.append("}");
        return querySb.toString();
    }
    
    ///不同attribute取值对应不同场景，该方法设置declaration——全局变量，添加到模型中
    public static void generateScenario(String filePath1,String fileName1,String filePath2,String fileName2,AttributeEntityType attributeEntityType,String sameDeclaration,List<String[]> attributeValues) throws IOException, DocumentException {
        ///写文件
        SAXReader reader= new SAXReader();
        Document document = reader.read(new File(filePath1+fileName1));
        Element rootElement=document.getRootElement();
        ///declaration中相同部分
//        StringBuilder declarationSb=getGeneralDeclaration(modelLayer,rules);

        ///设置declaration中attribute的取值，不同排列组合对应不同场景
        String declaration=getAttributeDeclaration(attributeEntityType, attributeValues,sameDeclaration);
        ///将declaration写入xml文件中
        setSystemDeclaration(rootElement,"",declaration,"");
        ///写文件
        writeTheXMLFile(document,filePath2,fileName2);
    }

    ///生成单一场景
    public static void generateSingleScenario(String filePath1,String fileName1,String filePath2,String fileName2,ModelLayer modelLayer,List<Rule> rules,List<String[]> attributeValues){
        ///declaration中相同部分
        String sameDeclaration=getGeneralDeclaration(modelLayer,rules);
        try {
            generateScenario(filePath1,fileName1,filePath2,fileName2,modelLayer.getAttributeEntity(),sameDeclaration,attributeValues);
        }catch (DocumentException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    ///生成多个场景
    public static ScenarioTree.ScenesTree generateMultiScenarios(String filePath1,String fileName1,String filePath2,String fileNameWithoutSuffix,ModelLayer modelLayer,List<Rule> rules,List<List<String[]>> attributeValuesList){
        ///declaration中相同部分
        String sameDeclaration=getGeneralDeclaration(modelLayer,rules);
        ////可以考虑多线程完成
        ExecutorService executorService=new ThreadPoolExecutor(15, 30, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(1000));
        ///场景数
        int scenarioNumber=attributeValuesList.size();
        ScenarioTree.ScenesTree scenesTree=new ScenarioTree.ScenesTree();   ////生成场景树
        scenesTree.setName("smart home");
        for (int i=0;i< scenarioNumber;i++){
            final int scenarioId=i;
            Runnable runnable= () -> {
                try {
                    ////各个场景对应的子节点
                    ScenarioTree.SceneChild sceneChild=new ScenarioTree.SceneChild();
                    sceneChild.setName("scenario-"+scenarioId);
                    for (String[] attributeValue:attributeValuesList.get(scenarioId)){
                        ////各个属性对应的子节点
                        ScenarioTree.AttributeValue attributeValueSet=new ScenarioTree.AttributeValue();
                        attributeValueSet.setName(attributeValue[0]);
                        attributeValueSet.setValue(Double.parseDouble(attributeValue[1]));
                        sceneChild.addChildren(attributeValueSet);
                    }
                    ScenarioTree.AttributeValue attributeValue=new ScenarioTree.AttributeValue();
                    attributeValue.setName("scenario-"+scenarioId +" details");
                    sceneChild.addChildren(attributeValue);
                    scenesTree.addChildren(sceneChild);
                    generateScenario(filePath1,fileName1,filePath2,fileNameWithoutSuffix+"-scenario-"+scenarioId+".xml",modelLayer.getAttributeEntity(),sameDeclaration,attributeValuesList.get(scenarioId));
                }catch (DocumentException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
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
        ////排序一下场景的顺序啊
        Comparator<ScenarioTree.SceneChild> c= (o1, o2) -> {
            int sceneNum1=Integer.parseInt(o1.getName().substring("scenario-".length()));
            int sceneNum2=Integer.parseInt(o2.getName().substring("scenario-".length()));
            return sceneNum1-sceneNum2;
        };
        scenesTree.getChildren().sort(c);
        return scenesTree;
    }

    ///根据trigger给attribute分段取值，不同排列组合对应不同场景，从而生成不同系统模型
    public static ScenarioTree.ScenesTree generateMultiScenariosAccordingToTriggers(String initModelFileName,String filePath1,String fileName1,String filePath2,ModelLayer modelLayer,List<Rule> rules,HashMap<String,Trigger> triggerMap){
        List<List<String[]>> attributeValuesList=getInitialValuesForAttributes(modelLayer.getAttributeEntity(),triggerMap);
        String fileNameWithoutSuffix=initModelFileName.substring(0,initModelFileName.indexOf(".xml"));
        ScenarioTree.ScenesTree scenesTree=generateMultiScenarios(filePath1,fileName1,filePath2,fileNameWithoutSuffix,modelLayer,rules,attributeValuesList);
        return scenesTree;
    }

    ///全局变量声明中，attribute的取值与TAP规则的trigger有关，找到与该attribute相关的trigger，按不同值分段，给每一段都分别取值。
    /// 最后每个attribute都有多种取值，排列组合后对应不同的场景
    public static List<List<String[]>> getInitialValuesForAttributes(AttributeEntityType attributeEntityType,HashMap<String,Trigger> triggerMap){
        ///先给每个attribute找到对应的trigger并按照trigger中value的大小排序
        List<List<String[]>> triggerValuesWithSameAttributes=new ArrayList<>();  ///获得每个attribute所对应trigger form
        for (AttributeEntityType.Attribute attribute:attributeEntityType.getAttributes()){
            ////获得该属性相关的所有trigger
            List<String[]> triggerValuesWithSameAttribute=getTriggerValuesWithSameAttribute(attribute.getAttribute(),triggerMap);
            if (triggerValuesWithSameAttribute.size()>0){
                ///trigger中有涉及到该属性
                triggerValuesWithSameAttributes.add(triggerValuesWithSameAttribute);
            }

        }
        ///获得总场景数
        int scenarioNumber=1;
        for (List<String[]> triggerValuesWithSameAttribute:triggerValuesWithSameAttributes){
            ///n个取值，有n+1段
            scenarioNumber*=(triggerValuesWithSameAttribute.size()+1);
        }
        ///计算各场景属性取值
        List<List<String[]>> attributeValuesList=new ArrayList<>();
        for (int i=0;i<scenarioNumber;i++){
            ////给每个属性赋值
            int scenarioIndex=i;
            List<String[]> attributeValues=new ArrayList<>();
            for (List<String[]> triggerValuesWithSameAttribute:triggerValuesWithSameAttributes){
                ///获得该场景下每个attribute的取值
                int k=scenarioIndex % (triggerValuesWithSameAttribute.size()+1);
                scenarioIndex=scenarioIndex/(triggerValuesWithSameAttribute.size()+1);
                String[] attributeValue=new String[2];   ///给attribute赋值   attributeValue[0]=attribute, attributeValues[1]=value
                attributeValue[0]=triggerValuesWithSameAttribute.get(0)[0];
                if(k==0) {
                    ///第一段,value-10
                    Double value=Double.parseDouble(triggerValuesWithSameAttribute.get(k)[2]);
                    attributeValue[1]=String.format("%.1f", value-10);
                }else if(k==triggerValuesWithSameAttribute.size()) {
                    ///最后一段,value+1
                    Double value=Double.parseDouble(triggerValuesWithSameAttribute.get(k-1)[2]);
                    attributeValue[1]=String.format("%.1f", value+10);
                }else {
                    ///中间段,中间值
                    Double value1=Double.parseDouble(triggerValuesWithSameAttribute.get(k-1)[2]);
                    Double value2=Double.parseDouble(triggerValuesWithSameAttribute.get(k)[2]);
                    attributeValue[1]=String.format("%.1f", (value1+value2)/2);
                }

                attributeValues.add(attributeValue);

            }
            attributeValuesList.add(attributeValues);
        }
        return attributeValuesList;
    }

    ///先给每个attribute找到所有对应的trigger并按照trigger中value的大小排序
    /////////找到涉及相同属性的triggers的triggerForm, 并按照分段点取值大小排序（分段点取值不重复）
    public static List<String[]> getTriggerValuesWithSameAttribute(String attributeName,HashMap<String,Trigger> triggerMap) {
        ///如temperature>30和temperature<18,temperature<=30
        ////temperature属性的断点为18、30
        List<String[]> triggerFormsWithSameAttribute=new ArrayList<>();  ///new String[3];
        for(Map.Entry<String,Trigger> triggerKeyValue:triggerMap.entrySet()) {
            Trigger trigger=triggerKeyValue.getValue();
            if(trigger.getTriggerForm()[0].equals(attributeName)) {
                ////////找到涉及该属性的triggers
                ////////保证断点值不重复
                boolean pointValueExist=false;
                for(String[] otherTriggerForm:triggerFormsWithSameAttribute) {
                    if(trigger.getTriggerForm()[2].equals(otherTriggerForm[2])) {
                        ////////保证断点值不重复
                        pointValueExist=true;
                    }
                }
                if(!pointValueExist) {
                    triggerFormsWithSameAttribute.add(trigger.getTriggerForm());
                }
            }
        }
        Comparator<String[]> comparator=new Comparator<String[]>() {
            /////按分段点取值排序
            /////其中分段点的取值为triggerForm[2]
            @Override
            public int compare(String[] t1, String[] t2) {
                Double v1=Double.parseDouble(t1[2]);
                Double v2=Double.parseDouble(t2[2]);
                if(v1<v2) {
                    return -1;
                }else {
                    return 1;
                }
            }
        };
        //////////对triggers进行排序
        Collections.sort(triggerFormsWithSameAttribute, comparator);
        return triggerFormsWithSameAttribute;
    }

    ///生成共同的文件
    public static void generateCommonModelFile(String simulationTime,String[] intoLocationTime,String filePath1,String modelFileName1,String filePath2,String modelFileName2,InstanceLayer instanceLayer,List<Rule> rules,HashMap<String,Trigger> triggerMap,HashMap<String,Action> actionMap,InstanceLayer interactiveEnvironment,HashMap<String,Instance> interactiveInstanceMap){
        ///生成控制器模型
        try {
            SAXReader reader= new SAXReader();
            Document document = reader.read(new File(filePath1+modelFileName1));
            Element rootElement=document.getRootElement();
//		System.out.println(rootElement.getName());
            List<Element> templateElements=rootElement.elements("template");
            ///同时把人的模型写进去
            Element humanElement=DocumentHelper.createElement("template");
            getHumanElement(instanceLayer.getHumanInstance().getHuman(), humanElement);
            templateElements.add(0,humanElement);

            ///生成控制器模型,并写入
            for (Rule rule:rules){
                Element controllerElement=DocumentHelper.createElement("template");
                getControllerElement(controllerElement,rule,triggerMap,actionMap,interactiveInstanceMap);
                templateElements.add(0,controllerElement);
            }

            ///系统声明
            String modelDeclaration=getModelDeclaration(interactiveEnvironment,rules,intoLocationTime);

            String query=getQuery(instanceLayer,rules,simulationTime);
            setSystemDeclaration(rootElement,modelDeclaration,"",query);

            ///写入xml文件中
            writeTheXMLFile(document,filePath2,modelFileName2);
        }catch (IOException e){
            e.printStackTrace();
        }catch (DocumentException e){
            e.printStackTrace();
        }

    }
    ////根据仿真时间长度和人的空间位置个数计算人进入每个空间位置的时间
    public static String[] getIntoLocationTime(String simulationTime,HumanInstance humanInstance){
        int locationNums=humanInstance.getHuman().getStateValues().size();
        double durationTime=Double.parseDouble(simulationTime)/locationNums;
        String[] intoLocationTime=new String[locationNums-1];
        for (int i=1;i<locationNums;i++){
            intoLocationTime[i-1]=durationTime*i+"";
        }
        return intoLocationTime;
    }


    ///解析rules，生成交互环境,这过程中可以生成IFD？
    public static InstanceLayer getInteractiveEnvironment(InstanceLayer instanceLayer,ModelLayer modelLayer,HashMap<String,Trigger> triggerMap,HashMap<String,Action> actionMap){
        InstanceLayer interactiveEnvironment=new InstanceLayer();
        HashMap<String,Instance> interactiveInstanceMap=new HashMap<>();
        HashMap<String, Instance> instanceLayerMap=InstanceLayerService.getInstanceMap(instanceLayer);
        for (Map.Entry<String,Trigger> triggerEntry:triggerMap.entrySet()){
            ///trigger涉及到哪些实例
            ///trigger所属实例
            Trigger trigger=triggerEntry.getValue();
            String instanceName=trigger.getInstanceName();
            if (!instanceName.equals("")){
                ///像时间这种属性不属于任意一个实体
                interactiveInstanceMap.putIfAbsent(instanceName,instanceLayerMap.get(instanceName));
            }

            ///相关传感器
            String sensorName=trigger.getSensor();
            if (!sensorName.equals("")){
                ///像可控设备这种不被传感器检测属性
                interactiveInstanceMap.putIfAbsent(sensorName,instanceLayerMap.get(sensorName));
            }
        }
        for (Map.Entry<String,Action> actionEntry:actionMap.entrySet()){
            ///action涉及到哪个实例  //设备、cyber service
            Action action=actionEntry.getValue();
            String instanceName=action.getInstanceName();
            interactiveInstanceMap.putIfAbsent(instanceName,instanceLayerMap.get(instanceName));
        }
        ////先生成交互环境模型
        for (Map.Entry<String, Instance> instanceEntry:interactiveInstanceMap.entrySet()){
            Instance instance=instanceEntry.getValue();
            if (instance instanceof HumanInstance){
                interactiveEnvironment.setHumanInstance((HumanInstance) instance);
            }else if (instance instanceof AttributeEntityInstance){
                interactiveEnvironment.setAttributeEntityInstance((AttributeEntityInstance) instance);
            }else if (instance instanceof UncertainEntityInstance){
                interactiveEnvironment.getUncertainEntityInstances().add((UncertainEntityInstance) instance);
            }else if (instance instanceof CyberServiceInstance){
                interactiveEnvironment.getCyberServiceInstances().add((CyberServiceInstance) instance);
            }else if (instance instanceof DeviceInstance){
                interactiveEnvironment.getDeviceInstances().add((DeviceInstance) instance);
            }else if (instance instanceof SensorInstance){
                interactiveEnvironment.getSensorInstances().add((SensorInstance) instance);
            }
        }
        return interactiveEnvironment;
    }

    ///获得triggerMap
    public static HashMap<String,Trigger> getTriggerMapFromRules(List<Rule> rules,InstanceLayer instanceLayer){
        HashMap<String,Trigger> triggerMap=new HashMap<>();
        HashMap<String, Instance> instanceLayerMap=InstanceLayerService.getInstanceMap(instanceLayer);
        for (Rule rule:rules){
            ///遍历规则抽取实例
            ///遍历trigger
            for (String triggerContent:rule.getTrigger()){
                ///解析trigger
                Trigger trigger=getTriggerFromTriggerContent(triggerContent,triggerMap,instanceLayerMap,instanceLayer);
                triggerMap.putIfAbsent(triggerContent,trigger);
                trigger.getRelatedRules().add(rule);
//                ///trigger涉及到哪些实例
//                ///trigger所属实例
//                String instanceName=trigger.getInstanceName();
//                if (!instanceName.equals("")){
//                    ///像时间这种属性不属于任意一个实体
//                    interactiveInstanceMap.putIfAbsent(instanceName,instanceLayerMap.get(instanceName));
//                }
//
//                ///相关传感器
//                String sensorName=trigger.getSensor();
//                if (!sensorName.equals("")){
//                    ///像可控设备这种不被传感器检测属性
//                    interactiveInstanceMap.putIfAbsent(sensorName,instanceLayerMap.get(sensorName));
//                }
            }
        }
        return triggerMap;
    }

    ///获得actionMap
    public static HashMap<String,Action> getActionMapFromRules(List<Rule> rules){
        HashMap<String,Action> actionMap=new HashMap<>();
        for (Rule rule:rules){
            ///遍历action
            for (String actionContent:rule.getAction()){
                Action action=getActionFromActionContent(actionContent,actionMap);
                actionMap.putIfAbsent(actionContent,action);
                action.getRelatedRules().add(rule);
//                ///action涉及到哪个实例  //设备、cyber service
//                String instanceName=action.getInstanceName();
//                interactiveInstanceMap.putIfAbsent(instanceName,instanceLayerMap.get(instanceName));
            }
        }
        return actionMap;
    }


    ///解析trigger triggerForm  TriggerId  triggerContent  InstanceName  sensor
    public static Trigger getTriggerFromTriggerContent(String triggerContent, HashMap<String,Trigger> triggerMap,HashMap<String,Instance> instanceLayerMap,InstanceLayer instanceLayer){
        Trigger trigger;
        trigger=triggerMap.get(triggerContent);

        if (trigger==null){
            ///还没有这个trigger
            trigger=new Trigger();
            ///如果trigger里含有for,
            String triggerContentWithoutFor="";
            if (triggerContent.indexOf(" for ")>=0){
                int forIndex=triggerContent.indexOf(" for ");
                triggerContentWithoutFor=triggerContent.substring(0,forIndex).trim();
                trigger.setForTime(triggerContent.substring(forIndex+" for ".length()));
            }else {
                triggerContentWithoutFor=triggerContent;
            }
            ///解析trigger
            String[] triggerForm=RuleService.getTriggerForm(triggerContentWithoutFor);

            trigger.setTriggerForm(triggerForm);  ///格式
            trigger.setTriggerId("trigger"+(triggerMap.size()+1));  ///triggerId
            trigger.setTriggerContent(triggerContent);  ///trigger内容
            if (triggerForm[1].equals(".")){
                ////instance.state格式
                ///实例名
                trigger.setInstanceName(triggerForm[0]);
                ///找到实例类型
                Instance instance=instanceLayerMap.get(triggerForm[0]);
                ///找传感器
                String identifier="";   ///找到对应的状态标识符
                if (instance instanceof HumanInstance){
                    ///human状态
                    HumanInstance humanInstance=(HumanInstance) instance;
                    identifier=humanInstance.getHuman().getIdentifier();
                } else if (instance instanceof UncertainEntityInstance){
                    ///不确定实体状态
                    UncertainEntityInstance uncertainEntityInstance=(UncertainEntityInstance) instance;
                    identifier=uncertainEntityInstance.getUncertainEntityType().getIdentifier();
                }
                if (!identifier.equals("")){
                    ///人和不确定实体需要传感器监测
                    for (SensorInstance sensorInstance:instanceLayer.getSensorInstances()){
                        if (sensorInstance.getSensorType().getAttribute().equals(identifier)){
                            ///找到对应传感器
                            trigger.setSensor(sensorInstance.getInstanceName());
                            break;
                        }
                    }
                }

            }else{
                if (trigger.getTriggerForm()[0].equals("time")){
                    // 以及time<(<=,>,>=)value格式
                    ///Timer为传感器，时间不属于哪个实体
                    trigger.setSensor("Timer");
                }else {
                    ////attribute<(<=,>,>=)value格式
                    ///实例名
                    String instanceName=instanceLayer.getAttributeEntityInstance().getInstanceName();
                    trigger.setInstanceName(instanceName);
                    ///传感器
                    for (SensorInstance sensorInstance:instanceLayer.getSensorInstances()){
                        if (sensorInstance.getSensorType().getAttribute().equals(triggerForm[0])){
                            ///找到对应传感器
                            trigger.setSensor(sensorInstance.getInstanceName());
                            break;
                        }
                    }
                }

            }

        }
        return trigger;
    }

    ///解析action  actionContent  ActionId  InstanceName  Sync
    public static Action getActionFromActionContent(String actionContent,HashMap<String,Action> actionMap){
        Action action;
        String[] instanceSync=actionContent.split("\\.");
        action=actionMap.get(actionContent);
        if (action==null){
            ///还没有这个action
            action=new Action();
            action.setActionContent(actionContent);
            action.setActionId("action"+(actionMap.size()+1));
            action.setInstanceName(instanceSync[0].trim());
            action.setSync(instanceSync[1].trim());
        }
        return action;
    }



}
