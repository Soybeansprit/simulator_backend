package com.example.demo.service;

import com.example.demo.bean.*;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 模型层，模型加载模块，模型为.xml格式
 * 载入模型后解析模型，模型类型、状态、属性or标识符取值
 * 区分monitored和controlled,实际区分了人、不确定实体、具有各种环境属性的实体（air）、cyber service、设备、传感器
 * 人的模型需要输入空间信息后，直接安排顺序，还需要写回模型文件中
 * */
@Service
public class ModelLayerService {


    ///解析xml文件，并记录对应模型
    ///模型层包括各种类型的模型，人、不确定实体、具有各种环境属性的实体（air）、cyber service、设备、传感器
    ///其中人需要给了空间位置后才有吧
    public static ModelLayer getModelLayer(String filePath,String modelFileFileName,String changedModelFileName,List<String> locations) throws DocumentException {
        ModelLayer modelLayer=new ModelLayer();
        GetTemplate getTemplate=new GetTemplate();
        ///解析xml文件，获得模板
        GetTemplate.deleteFileLine(filePath+modelFileFileName, filePath+changedModelFileName, 2);  ///更改为有效的xml文件
        List<GetTemplate.Template> templates=getTemplate.getTemplate(filePath+changedModelFileName);   ///解析
        ///分别获得各种类型的模型
        Human human=getHuman(locations);  //人
        List<UncertainEntityType> uncertainEntityTypes=new ArrayList<>();  //不确定实体
        AttributeEntityType attributeEntityType=new AttributeEntityType();  //具有各种环境属性的实体具有各种环境属性的实体
        List<DeviceType> deviceTypes=new ArrayList<>();  //设备
        List<CyberServiceType> cyberServiceTypes=new ArrayList<>();  //cyber service
        List<SensorType> sensorTypes=new ArrayList<>();  //传感器
        ///确定各类模型
        for (GetTemplate.Template template:templates){
            if(template.getDeclaration().indexOf("uncertain")>=0){
                ///不确定实体具有不确定性，每个状态具有状态名，只考虑每个状态对应的状态标识符取值
                UncertainEntityType uncertainEntityType=new UncertainEntityType();
                getUncertainEntityTypeFromTemplate(template,uncertainEntityType);
                uncertainEntityTypes.add(uncertainEntityType);

            }else if(template.getDeclaration().indexOf("attributes")>=0){
                ///具有各种环境属性的实体只有一个location，location上面是各属性对应的不变式，考虑各属性关系
                getAttributeEntityTypeFromTemplate(template,attributeEntityType);


            }else if (template.getDeclaration().indexOf("device")>=0){
                ///设备模型，n各location对应n个状态，都有状态名，有对应的同步信号，是参数化模型，可被多次实例化，不同状态可能对环境产生影响
                DeviceType deviceType=new DeviceType();
                getDeviceTypeFromTemplate(template,deviceType);
                deviceTypes.add(deviceType);

            }else if(template.getDeclaration().indexOf("cyber")>=0){
                //cyber service 类型，只有状态、同步信号，对应输入输出
                CyberServiceType cyberServiceType=new CyberServiceType();
                getCyberServiceTypeFromTemplate(template,cyberServiceType);
                cyberServiceTypes.add(cyberServiceType);
            }else if(template.getDeclaration().indexOf("sensor")>=0){
                ///传感器，一个状态，一条transition，指向自己本身
                SensorType sensorType=new SensorType();
                getSensorTypeFromTemplate(template,sensorType);
                sensorTypes.add(sensorType);
            }
        }
        ///还需要确定deviceType对应的影响属性的关系
        for (DeviceType deviceType:deviceTypes){
            for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
                for (String[] effect:stateSyncValueEffect.getEffects()){
                    ///如果会对环境产生影响，///effect[0]=attribute, effect[1]=delta（对于会对总变化率产生影响的）, effect[2]=影响值
                    ///暂时存在effect[1]作为delta，后面再查attribute相关实体，看是attribute还是delta
                    for (AttributeEntityType.Attribute attribute:attributeEntityType.getAttributes()){
                        if (attribute.getAttribute().trim().equals(effect[1].trim())){
                            ///其实effect[1]存的是属性名，而不是delta
                            effect[0]=attribute.getAttribute().trim();
                            effect[1]="";
                        }else if(attribute.getDelta().trim().equals(effect[1].trim())){
                            ///effect[1]存的就是delta
                            effect[0]=attribute.getAttribute().trim();
                        }
                    }
                }
            }
        }
        ///还需要确定sensor所检测的模型类型
        for (SensorType sensorType:sensorTypes){
            if (sensorType.getMonitoredEntityType().equals("")){
                for (AttributeEntityType.Attribute attribute:attributeEntityType.getAttributes()){
                    ///是否检测的环境属性
                    if (attribute.getAttribute().trim().equals(sensorType.getAttribute().trim())){
                        sensorType.setMonitoredEntityType(attributeEntityType.getTypeName());
                        break;
                    }
                }
                if (sensorType.getMonitoredEntityType().equals("")){
                    ///是否检测人
                    if (human.getIdentifier().trim().equals(sensorType.getAttribute().trim())){
                        sensorType.setMonitoredEntityType(human.getTypeName());
                    }
                }
                if (sensorType.getMonitoredEntityType().equals("")){
                    ///是否检测不确定实体
                    for (UncertainEntityType uncertainEntityType:uncertainEntityTypes){
                        if (uncertainEntityType.getIdentifier().trim().equals(sensorType.getAttribute().trim())){
                            sensorType.setMonitoredEntityType(uncertainEntityType.getTypeName());
                            break;
                        }
                    }
                }
            }
        }
        ////添加一个Timer用于计时
        SensorType timerType=new SensorType();
        timerType.setTypeName("Timer");
        timerType.setAttribute("time");
        sensorTypes.add(timerType);

        modelLayer.setDeviceTypes(deviceTypes);
        modelLayer.setAttributeEntity(attributeEntityType);
        modelLayer.setSensorTypes(sensorTypes);
        modelLayer.setUncertainEntityTypes(uncertainEntityTypes);
        modelLayer.setHuman(human);
        modelLayer.setCyberServiceTypes(cyberServiceTypes);


        return modelLayer;
    }

    ///传感器
    public static void getSensorTypeFromTemplate(GetTemplate.Template template, SensorType sensorType){
        sensorType.setTypeName(template.getName());
        GetTemplate.Transition transition=template.getTransitions().get(0);
        if (transition.getLabels().size()>0){
            for (GetTemplate.Label label: transition.getLabels()){
                if (label.getKind().equals("assignment")){
                    String[] assignments=label.getContent().split(",");
                    for (String assignment:assignments){
                        String[] variableValue=assignment.split("=");
                        if (variableValue[1].indexOf("get()")>=0){
                            ///检测属性
                            sensorType.setAttribute(variableValue[0].trim());
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    //设备
    public static void getDeviceTypeFromTemplate(GetTemplate.Template template, DeviceType deviceType){
        ///先获得declaration中的内容
        String[] declarations=template.getDeclaration().split(";");
        ///设备模型，n各location对应n个状态，都有状态名，有对应的同步信号，是参数化模型，可被多次实例化，不同状态可能对环境产生影响
        deviceType.setTypeName(template.getName());  ///类型名
        for (GetTemplate.Location location:template.getLocations()){
            if (!location.getName().equals("")){
                ///状态信息，状态名，状态id
                DeviceType.StateSyncValueEffect stateSyncValueEffect=deviceType.new StateSyncValueEffect();
                stateSyncValueEffect.setStateName(location.getName());
                stateSyncValueEffect.setStateId(location.getId());
                deviceType.getStateSyncValueEffects().add(stateSyncValueEffect);
            }
        }
        ///获得状态对应的状态标识符、synchronisation、effect
        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
            if (stateSyncValueEffect.getValue().equals("")){
                ///还没有处理过状态标识符取值
                for (GetTemplate.Transition transition:template.getTransitions()){
                    if (transition.getTargetId().equals(stateSyncValueEffect.getStateId())){
                        ///找到指向该状态的transition
                        if (transition.getLabels().size()>0){
                            for (GetTemplate.Label label:transition.getLabels()){
                                if (label.getKind().equals("assignment")){
                                    String[] assignments=label.getContent().split(",");
                                    for (String assignment:assignments){
                                        String[] variableValue=assignment.trim().split("=");
                                        int index=-1;
                                        if ((index=variableValue[0].indexOf("[i]"))>=0){
                                            ///状态标识符及取值
                                            if (deviceType.getIdentifier().equals("")){
                                                deviceType.setIdentifier(variableValue[0].substring(0,index));
                                            }
                                            stateSyncValueEffect.setValue(variableValue[1]);
                                        }else {
                                            ///对环境属性的影响
                                            ///dpm_2_5=dpm_2_5+(offdpm-ondpm)
                                            ///暂时存在effect[1]作为delta，后面再查attribute相关实体，看是attribute还是delta
                                            String[] effect=new String[3];  ///effect[0]=attribute, effect[1]=delta（对于会对总变化率产生影响的）, effect[2]=影响值
                                            effect[1]=variableValue[0].trim();
                                            int leftIndex=-1,rightIndex=-1,minusIndex=-1;
                                            if ((leftIndex=variableValue[1].indexOf("("))>=0&&
                                                    (rightIndex=variableValue[1].indexOf(")"))>=0&&
                                                    (minusIndex=variableValue[1].indexOf("-"))>=0){
                                                String effectVariable=variableValue[1].substring(leftIndex+1,minusIndex);  ///获得影响的变量
                                                ///接下来获得对应的影响值
                                                for (String declaration:declarations){
                                                    if (declaration.indexOf(effectVariable)>=0){
                                                        String[] varValue=declaration.split("=");  ///double offdpm=0.0;
                                                        effect[2]=varValue[1];
                                                        break;
                                                    }
                                                }
                                            }
                                            stateSyncValueEffect.getEffects().add(effect);
                                        }


                                    }
                                }else if (label.getKind().equals("synchronisation")){
                                    String synchronisation="";
                                    int index=-1;
                                    if ((index=label.getContent().indexOf("[i]"))>=0){
                                        synchronisation=label.getContent().substring(0,index);
                                    }
                                    stateSyncValueEffect.setSynchronisation(synchronisation);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    //cyber service 类型，只有状态、同步信号，对应输入输出
    public static void getCyberServiceTypeFromTemplate(GetTemplate.Template template,CyberServiceType cyberServiceType){
        //cyber service 类型，只有状态、同步信号，对应输入输出
        cyberServiceType.setTypeName(template.getName());
        for (GetTemplate.Location location:template.getLocations()){
            String[] stateSync=new String[2]; ///stateSync[0]=stateId,stateSync[1]=synchronization
            ///先确定状态id
            stateSync[0]=location.getId();
            cyberServiceType.getStateSyncs().add(stateSync);
        }
        for (String[] stateSync:cyberServiceType.getStateSyncs()){
            ///再确定该到状态的信号通道
            if (stateSync[1]==null||stateSync[1].equals("")){
                for (GetTemplate.Transition transition:template.getTransitions()){
                    if (transition.getTargetId().equals(stateSync[0])){
                        if (transition.getLabels().size()>0){
                            for (GetTemplate.Label label: transition.getLabels()){
                                if (label.getKind().equals("synchronisation")){
                                    stateSync[1]=label.getContent();
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    //不确定实体
    public static void getUncertainEntityTypeFromTemplate(GetTemplate.Template template,UncertainEntityType uncertainEntityType){
        //不确定实体
        uncertainEntityType.setTypeName(template.getName());  ///类型名
        List<String[]> stateValues=new ArrayList<>();
        ///获得各个状态
        for(GetTemplate.Location location:template.getLocations()){
            if (!location.getName().trim().equals("")){
                ///不确定实体中有名字的location是一个状态
                String[] stateValue=new String[3];  ///stateValue[0]=状态名,stateValue[1]=状态id,stateValue[1]=状态标识符取值
                stateValue[0]=location.getName().trim();
                stateValue[1]=location.getId().trim();
                stateValues.add(stateValue);
            }
        }
        ///获得各状态的状态标识符取值
        for(String[] stateValue:stateValues){
            if(stateValue[2]==null||stateValue[2].equals("")){
                for(GetTemplate.Transition transition:template.getTransitions()){
                    if (transition.getTargetId().equals(stateValue[1])){
                        ///找到指向该状态的transition
                        if (transition.getLabels().size()>0){
                            for (GetTemplate.Label label:transition.getLabels()){
                                ///找到assignment
                                if(label.getKind().equals("assignment")){
                                    String[] assignments=label.getContent().split(",");   ///rain=0,t=0
                                    ///找到状态标识符
                                    for (String assignment:assignments){
                                        String[] variableValue=assignment.split("=");    ///rain=0 => variableValue[0]="rain",variableValue[1]=0
                                        if (!variableValue[0].trim().equals("t")){
                                            ///状态标识符及取值
                                            if (uncertainEntityType.getIdentifier().equals("")){
                                                uncertainEntityType.setIdentifier(variableValue[0]);
                                                stateValue[2]=variableValue[1];
                                            }else if (uncertainEntityType.getIdentifier().equals(variableValue[0])){
                                                stateValue[2]=variableValue[1];
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        uncertainEntityType.setStateValues(stateValues);
    }

    ///attribute实体
    public static void getAttributeEntityTypeFromTemplate(GetTemplate.Template template,AttributeEntityType attributeEntityType){
        ///attribute实体
        attributeEntityType.setTypeName(template.getName().trim());  ///类型名
        if (template.getLocations().size()>0){
            GetTemplate.Location location=template.getLocations().get(0);
            if (!location.getInvariant().equals("")){
                ///很多环境属性的invariants
                String[] invariants=location.getInvariant().split("&&");  ///temperature'==dtemper&&humidity'==dhumi
                for (String invariant:invariants){
                    String[] attributeDelta=invariant.split("'==");  ///temperature'==dtemper => attributeDelta[0]="temperature", attributeDelta[1]="dtemper"
                    AttributeEntityType.Attribute attribute=attributeEntityType.new Attribute();
                    attribute.setAttribute(attributeDelta[0].trim());
                    attribute.setContent(invariant.trim());
                    attribute.setDelta(attributeDelta[1].trim());
                    attributeEntityType.getAttributes().add(attribute);
                }
            }
        }
    }

    ////获得人的模型，根据空间位置信息获得
    public static Human getHuman(List<String> locations){
        Human human=new Human();
        human.setTypeName("Human");  //模型名
        human.setIdentifier("position");  //人的状态标识符
        List<String[]> stateValues=new ArrayList<>();  ///各状态对应的状态标识符取值
        for(int i=0;i<locations.size();i++){
            String[] stateValue=new String[3];  ///stateValue[0]=状态名,stateValue[1]=状态id,stateValue[1]=状态标识符取值
            stateValue[0]=locations.get(i).trim();  ///状态名
            stateValue[1]="id"+(i+1);    ///状态id
            stateValue[2]=i+"";  //状态标识符取值
            stateValues.add(stateValue);
        }
        human.setStateValues(stateValues);
        return human;
    }
    ////获得人的模型，根据空间位置信息获得
    public static Human getHuman(String[] locations){
        Human human=new Human();
        human.setTypeName("Human");  //模型名
        human.setIdentifier("position");  //人的状态标识符
        List<String[]> stateValues=new ArrayList<>();  ///各状态对应的状态标识符取值
        for(int i=0;i<locations.length;i++){
            String[] stateValue=new String[3];  ///stateValue[0]=状态名,stateValue[1]=状态id,stateValue[1]=状态标识符取值
            stateValue[0]=locations[i].trim();  ///状态名
            stateValue[1]="id"+(i+1);    ///状态id
            stateValue[2]=i+"";  //状态标识符取值
            stateValues.add(stateValue);
        }
        human.setStateValues(stateValues);
        return human;
    }


//    ///根据人模型信息，生成xml中的元素
//    public static void getHumanElement(Human human, Element humanElement){
//        Element nameElement=humanElement.addElement("name");
//        nameElement.setText(human.getTypeName());  ///模型名
//
//        ///parameter
//        Element parameterElement=humanElement.addElement("parameter");
//        StringBuilder parameter=new StringBuilder();  ///传参部分
//
//        ///declaration
//        Element declarationElement=humanElement.addElement("declaration");
//        StringBuilder declaration=new StringBuilder();
//        declaration.append(
//                "//human\n" +
//                "clock t;\n"+
//                "clock time;");
//        declarationElement.setText(declaration.toString());
//
//        ///先创建一个init location
//        Element initLocationElement=humanElement.addElement("location");
//        initLocationElement.addAttribute("id","id0");
//        initLocationElement.addAttribute("x","150");
//        initLocationElement.addAttribute("y","100");
//        initLocationElement.addElement("committed");
//        ///init
//        Element initElement=humanElement.addElement("init");
//        initElement.addAttribute("ref", "id0");
//
//        ///locations
//        List<Element> locationElements=humanElement.elements("location");
//        ///transitions
//        List<Element> transitionElements=new ArrayList<Element>();
//        for (int i=0;i<human.getStateValues().size();i++){
//            String[] stateValue=human.getStateValues().get(i);
//            ///location
//            Element locationElement=DocumentHelper.createElement("location");
//            locationElement.addAttribute("id",stateValue[1]);
//            locationElement.addAttribute("x",150*(i+2)+"");
//            locationElement.addAttribute("y","100");
//            locationElement.addElement("name").setText(stateValue[0]);
//            if (i<human.getStateValues().size()-1){
//                ///不是最后一个location，存在不变式 time<=t1、t2.。。 t1、t2。。。是参数
//                Element labelElement=locationElement.addElement("label");
//                labelElement.addAttribute("kind", "invariant");
//                labelElement.setText("time<=t"+(i+1));
//                ///parameter
//                if (i>0){
//                    parameter.append(", ");
//                }
//                parameter.append("double t"+(i+1));
//            }
//            locationElements.add(0,locationElement);
//
//            ///transition
//            Element transitionElement= DocumentHelper.createElement("transition");
//            Element sourceElement=transitionElement.addElement("source");
//            sourceElement.addAttribute("ref", "id"+i);
//            Element targetElement=transitionElement.addElement("target");
//            targetElement.addAttribute("ref", stateValue[1]);
//                ///guard
//            if (i>0){
//                Element guardElement=transitionElement.addElement("label");
//                guardElement.addAttribute("kind", "guard");
//                guardElement.setText("time>=t"+i);
//            }
//                ///assignment
//            Element assignmentElement=transitionElement.addElement("label");
//            assignmentElement.addAttribute("kind", "assignment");
//            assignmentElement.setText(human.getIdentifier()+"="+stateValue[2]);
//            transitionElements.add(transitionElement);
//        }
//
//        parameterElement.setText(parameter.toString());
//
//        for(Element transitionElement:transitionElements) {
//            humanElement.add(transitionElement);
//        }
//    }

    ///以key-value的map形式存储模型层的模型
    public static HashMap<String, EntityType> getModelMap(ModelLayer modelLayer){
        HashMap<String, EntityType> modelLayerMap=new HashMap<>();
        for (DeviceType deviceType:modelLayer.getDeviceTypes()){
            modelLayerMap.put(deviceType.getTypeName(),deviceType);
        }
        for (SensorType sensorType:modelLayer.getSensorTypes()){
            modelLayerMap.put(sensorType.getTypeName(),sensorType);
        }
        for (UncertainEntityType uncertainEntityType:modelLayer.getUncertainEntityTypes()){
            modelLayerMap.put(uncertainEntityType.getTypeName(),uncertainEntityType);
        }
        for (CyberServiceType cyberServiceType:modelLayer.getCyberServiceTypes()){
            modelLayerMap.put(cyberServiceType.getTypeName(),cyberServiceType);
        }
        modelLayerMap.put(modelLayer.getAttributeEntity().getTypeName(),modelLayer.getAttributeEntity());
        modelLayerMap.put(modelLayer.getHuman().getTypeName(),modelLayer.getHuman());
        return modelLayerMap;
    }
}
