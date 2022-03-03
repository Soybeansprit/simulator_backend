package com.example.demo.service;

import com.example.demo.bean.*;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * 实例层，设备位置信息表加载模块，格式为.properties
 * 解析设备位置信息表，各实例，设备实例：设备名、设备类型、设备位置
 * 结合模型层
 * 最终获得设备实例的：设备名、设备类型、空间位置、所属设备类型实例序号
 * 实例包括 设备实例、人、带各种环境属性的实体、不确定实体
 * */
@Service
public class InstanceLayerService {
    ///解析设备位置信息表，并实例化
    /**
     * 实例层需要记录的信息：有哪些实例，属于什么类型，如果是设备，需要确定所属设备类型的实例序号
     * instance1={instanceName:Emma,entityType:Human}
     * instance2={instanceName:SMS,entityType:SMS}
     * instance3={instanceName:Air,entityType:Air}
     * instance4={instanceName:Bulb_0,entityType:Bulb,location:L1,visible:true}  设备
     * instance5={instanceName:AirConditioner_0,entityType :AirConditioner,location:L1,visible:false}
     * instance6={instanceName:Window_0,entityType:Window,location:L1,visible:true}
     * instance7={instanceName:Window_1,entityType:Window,location:L2,visible:true}
     * instance8={instanceName:TV_0,entityType:TV,location:L3,visible:false}
     * */

    ///根据instanceLayer生成各实例的状态表示（除了带属性的实体）、指令表示（可控实体）、属性表示（各环境属性），即生成相应的trigger和action
    public static void main(String[] args) throws DocumentException, IOException {
        List<String> locations=new ArrayList<>();
        ModelLayer modelLayer=ModelLayerService.getModelLayer("D:\\example\\例子\\论文实验\\","ontology.xml","ontology.xml",locations);
//        generateInstances("D:\\example\\例子\\论文实验\\自动建模能力\\","instance.properties",modelLayer);

        generateTriggerActions(modelLayer,"D:\\example\\例子\\论文实验\\自动建模能力\\","10.properties");
    }

    //随机生成实例层
    public static void generateInstances(String filePath,String fileName,ModelLayer modelLayer){
        StringBuilder sb=new StringBuilder();
        String[] locations={"living_room","kitchen","bathroom","bedroom","guest_room","out"};
//        sb.append("locations={");
//        for (int i=0;i<locations.length;i++){
//            sb.append(locations[i]);
//            if (i< locations.length-1){
//                sb.append(",");
//            }
//        }
//        sb.append("}");
        int count=10;
        for (DeviceType deviceType:modelLayer.getDeviceTypes()){
            Random r=new Random();

            for (int i=0;i<25;i++){
                int loc=r.nextInt(locations.length);
                boolean visible=loc==0?true:false;
                String ins=String.format("instance%d={instanceName:%s,entityType:%s,location:%s,visible:%b}\n",count,deviceType.getTypeName()+"_"+i,deviceType.getTypeName(),locations[loc],visible);
                sb.append(ins);
                count++;
            }
        }
        try(FileWriter fileWriter=new FileWriter(filePath+fileName);PrintWriter printWriter=new PrintWriter(fileWriter)){
            printWriter.println(sb);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    ///根据instanceLayer生成各实例的状态表示（除了带属性的实体）、指令表示（可控实体）、属性表示（各环境属性），即生成相应的trigger和action
    public static void generateTriggerActions(ModelLayer modelLayer,String instanceInformationFilePath,String instanceInformationFileName) throws DocumentException, IOException {
        InstanceLayer instanceLayer=getInstanceLayer(instanceInformationFilePath,instanceInformationFileName,modelLayer);
        generateTriggerActions(instanceLayer,instanceInformationFilePath,instanceInformationFileName.substring(0,instanceInformationFileName.indexOf(".")));
    }


    ///根据instanceLayer生成各实例的状态表示（除了带属性的实体）、指令表示（可控实体）、属性表示（各环境属性），即生成相应的trigger和action
    public static void generateTriggerActions(InstanceLayer instanceLayer,String filePath,String fileName){
        StringBuilder sb=new StringBuilder();
        StringBuilder triggerSb=new StringBuilder();
        StringBuilder actionSb=new StringBuilder();
        List<String> triggers=new ArrayList<>();
        List<String> actions=new ArrayList<>();
        if (!instanceLayer.getAttributeEntityInstance().getInstanceName().equals("")){
            AttributeEntityType attributeEntityType=instanceLayer.getAttributeEntityInstance().getAttributeEntityType();
            for (AttributeEntityType.Attribute attribute:attributeEntityType.getAttributes()){
                triggerSb.append("IF "+attribute.getAttribute()+" \n");
                triggers.add("IF "+attribute.getAttribute()+" ");
            }
            sb.append("带环境属性的实体："+instanceLayer.getAttributeEntityInstance().getInstanceName()+"\n");
        }
        if (!instanceLayer.getHumanInstance().getInstanceName().equals("")){
            Human human=instanceLayer.getHumanInstance().getHuman();
            for (String[] stateValue: human.getStateValues()){
                triggerSb.append("IF "+instanceLayer.getHumanInstance().getInstanceName()+"."+stateValue[0]+" \n");
                triggers.add("IF "+instanceLayer.getHumanInstance().getInstanceName()+"."+stateValue[0]+" ");
            }
            sb.append("人："+instanceLayer.getHumanInstance().getInstanceName()+"\n");
        }
        sb.append("不确定实体："+instanceLayer.getUncertainEntityInstances().size()+"\n");
        for (UncertainEntityInstance uncertainEntityInstance: instanceLayer.getUncertainEntityInstances()){
            UncertainEntityType uncertainEntityType= uncertainEntityInstance.getUncertainEntityType();
            for (String[] stateValue: uncertainEntityType.getStateValues()){
                triggerSb.append("IF "+uncertainEntityInstance.getInstanceName()+"."+stateValue[0]+" \n");
                triggers.add("IF "+uncertainEntityInstance.getInstanceName()+"."+stateValue[0]+" ");
            }
            sb.append("     "+uncertainEntityInstance.getInstanceName()+"\n");
        }
        sb.append("传感器："+instanceLayer.getSensorInstances().size()+"\n");
        for (SensorInstance sensorInstance:instanceLayer.getSensorInstances()){
            sb.append("     "+sensorInstance.getInstanceName()+"\n");
        }
        sb.append("网络服务："+instanceLayer.getCyberServiceInstances().size()+"\n");
        for (CyberServiceInstance cyberServiceInstance: instanceLayer.getCyberServiceInstances()){
            CyberServiceType cyberServiceType= cyberServiceInstance.getCyberServiceType();
            for (String[] stateSync: cyberServiceType.getStateSyncs()){
                if (stateSync[1].contains("?")){
                    String sync=stateSync[1].substring(0,stateSync.length-1);
                    actionSb.append("THEN "+cyberServiceInstance.getInstanceName()+"."+sync+" \n");
                    actions.add("THEN "+cyberServiceInstance.getInstanceName()+"."+sync);
                }
            }
            sb.append("     "+cyberServiceInstance.getInstanceName()+"\n");
        }
        sb.append("可控设备："+instanceLayer.getDeviceInstances().size()+"\n");
        int visibleNum=0;   ///可观察设备数
        HashMap<String,Integer> typeNumHashMap=new HashMap<>();
        for (DeviceInstance deviceInstance: instanceLayer.getDeviceInstances()){
            DeviceType deviceType= deviceInstance.getDeviceType();
            Integer num=typeNumHashMap.get(deviceType.getTypeName());
            if (num==null||num==0){
                typeNumHashMap.put(deviceType.getTypeName(), 1);
            }else {
                typeNumHashMap.put(deviceType.getTypeName(),num+1);
            }
            for (DeviceType.StateSyncValueEffect stateSyncValueEffect: deviceType.getStateSyncValueEffects()){
                triggerSb.append("IF "+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getStateName()+" \n");
                triggers.add("IF "+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getStateName()+" ");
                actionSb.append("THEN "+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getSynchronisation()+" \n");
                actions.add("THEN "+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getSynchronisation());
            }
            if (deviceInstance.isVisible()){
                visibleNum++;
            }
//            sb.append("     "+deviceInstance.getInstanceName()+"\n");
        }
        sb.append("     可控设备类型数："+typeNumHashMap.size()+"\n");
        sb.append("         可观察设备数："+visibleNum+"\n");
        for (Map.Entry<String,Integer> typeNum:typeNumHashMap.entrySet()){
            sb.append("       "+typeNum.getKey()+"："+typeNum.getValue()+"\n");
        }
        sb.append("\n\n\n");
        sb.append("TAP规则：\n\n");
        for (String trigger:triggers){
            for (String action:actions){
                sb.append(trigger+action+"\n");
            }
        }

        try(FileWriter fileWriter=new FileWriter(filePath+fileName);PrintWriter printWriter=new PrintWriter(fileWriter)){
            printWriter.println(sb);
//            printWriter.println(triggerSb);
//            printWriter.println(actionSb);
        }catch (IOException e){
            e.printStackTrace();
        }


    }

    ///以modelLayer和实例信息表作为输入，输出instanceLayer
    public static InstanceLayer getInstanceLayer(String filePath, String instanceInformationFileName, ModelLayer modelLayer) throws IOException {
        HashMap<String, EntityType> modelLayerMap=ModelLayerService.getModelMap(modelLayer);


        InstanceLayer instanceLayer=new InstanceLayer();
        ///先解析实例信息表，以.properties格式
        Properties properties=PropertyService.getProperties(filePath, instanceInformationFileName);
        for(String key:properties.stringPropertyNames()){
            if (key.equals("locations")){
                if(modelLayer.getHuman().getStateValues().size()>0){
                    ///前面自定义的空间位置信息优先
                    continue;
                }
                ///假如实例信息表中有空间信息
                ///空间信息，然后生成人的模型
                String locationDetail=properties.getProperty(key).trim();
                locationDetail=locationDetail.substring(1,locationDetail.length()-1);
                String[] locations=locationDetail.split(",");
                Human human=ModelLayerService.getHuman(locations);
                modelLayer.setHuman(human);
                continue;
            }
            String instanceDetail=properties.getProperty(key).trim();
            instanceDetail=instanceDetail.substring(1,instanceDetail.length()-1);
            String[] variableValues=instanceDetail.split(",");
            if (instanceDetail.indexOf("location")>=0){
                ///是设备的实例
                DeviceInstance deviceInstance=new DeviceInstance();
                for (String variableValue:variableValues){
                    String[] keyValue=variableValue.split(":");
                    if (keyValue[0].trim().equals("instanceName")){
                        deviceInstance.setInstanceName(keyValue[1].trim());   ///实例名
                    }else if (keyValue[0].trim().equals("entityType")){
                        String entityTypeName=keyValue[1].trim();
                        deviceInstance.setEntityTypeName(entityTypeName);  ///设备类型名
                        EntityType entityType=modelLayerMap.get(entityTypeName);
                        if (entityType instanceof DeviceType){
                            DeviceType deviceType=(DeviceType) entityType;
                            deviceInstance.setDeviceType(deviceType);  ///设备类型
                            int num=deviceType.getInstanceNumber();  ///实例序号
                            deviceInstance.setSequenceNumber(num);
                            num++;
                            deviceType.setInstanceNumber(num);   ///该类设备实例个数
                        }
                    }else if (keyValue[0].trim().equals("location")){
                        deviceInstance.setLocation(keyValue[1].trim());   ///设备实例所处空间位置
                    }else if (keyValue[0].trim().equals("visible")){  ///可控设备是否可被外界观察
                        if (keyValue[1].trim().equalsIgnoreCase("true")){
                            deviceInstance.setVisible(true);  //可被外界观察
                        }else {
                            deviceInstance.setVisible(false);  //不可被观察
                        }
                    }

                }
                instanceLayer.getDeviceInstances().add(deviceInstance);
            }else{
                if (variableValues[1].indexOf("entityType")>=0){
                    String entityTypeName=variableValues[1].substring(variableValues[1].indexOf("entityType")+"entityType:".length()).trim();
                    EntityType entityType=modelLayerMap.get(entityTypeName);
                    if (entityType instanceof Human){   ///人的实例
                        HumanInstance humanInstance=instanceLayer.getHumanInstance();
                        humanInstance.setHuman((Human) entityType);  ///人的类型
                        humanInstance.setEntityTypeName(entityTypeName);  ///人的类型名
                        if (variableValues[0].indexOf("instanceName")>=0){
                            String instanceName=variableValues[0].substring(variableValues[0].indexOf("instanceName")+"instanceName:".length()).trim();
                            humanInstance.setInstanceName(instanceName);
                        }
                    }else if (entityType instanceof AttributeEntityType){  ///attribute的实例
                        AttributeEntityInstance attributeEntityInstance=instanceLayer.getAttributeEntityInstance();
                        attributeEntityInstance.setAttributeEntityType((AttributeEntityType) entityType);  ///attribute类型
                        attributeEntityInstance.setEntityTypeName(entityTypeName);  ///attribute类型名
                        if (variableValues[0].indexOf("instanceName")>=0){
                            String instanceName=variableValues[0].substring(variableValues[0].indexOf("instanceName")+"instanceName:".length()).trim();
                            attributeEntityInstance.setInstanceName(instanceName);  ///attribute实例名
                        }
                    }else if (entityType instanceof CyberServiceType){   ///cyber service的实例
                        CyberServiceInstance cyberServiceInstance=new CyberServiceInstance();
                        cyberServiceInstance.setCyberServiceType((CyberServiceType) entityType);  ///cyber 类型
                        cyberServiceInstance.setEntityTypeName(entityTypeName);  ///cyber 类型名
                        if (variableValues[0].indexOf("instanceName")>=0){
                            String instanceName=variableValues[0].substring(variableValues[0].indexOf("instanceName")+"instanceName:".length()).trim();
                            cyberServiceInstance.setInstanceName(instanceName);  ///cyber实例名
                        }
                        instanceLayer.getCyberServiceInstances().add(cyberServiceInstance);

                    }else if (entityType instanceof UncertainEntityType){  ////不确定实体的实例
                        UncertainEntityInstance uncertainEntityInstance=new UncertainEntityInstance();
                        uncertainEntityInstance.setUncertainEntityType((UncertainEntityType) entityType);  ///不确定实体类型
                        uncertainEntityInstance.setEntityTypeName(entityTypeName);  ///不确定实体类型名
                        if (variableValues[0].indexOf("instanceName")>=0){
                            String instanceName=variableValues[0].substring(variableValues[0].indexOf("instanceName")+"instanceName:".length()).trim();
                            uncertainEntityInstance.setInstanceName(instanceName);  ///不确定实例名
                        }
                        instanceLayer.getUncertainEntityInstances().add(uncertainEntityInstance);
                    }else if (entityType instanceof SensorType){  ///传感器
                        SensorInstance sensorInstance=new SensorInstance();
                        sensorInstance.setSensorType((SensorType) entityType);   ///传感器类型
                        sensorInstance.setEntityTypeName(entityTypeName);  ///传感器类型名
                        if (variableValues[0].indexOf("instanceName")>=0){
                            String instanceName=variableValues[0].substring(variableValues[0].indexOf("instanceName")+"instanceName:".length()).trim();
                            sensorInstance.setInstanceName(instanceName);   ///传感器实例名
                        }
                        instanceLayer.getSensorInstances().add(sensorInstance);
                    }
                }

            }

        }
        instanceLayer.getHumanInstance().setHuman(modelLayer.getHuman());


        ///添加未提到的monitored entity
        ///包括人、attribute相关实体、不确定实体
        if (instanceLayer.getHumanInstance().getInstanceName().equals("")){
            instanceLayer.getHumanInstance().setInstanceName(modelLayer.getHuman().getTypeName());
            instanceLayer.getHumanInstance().setEntityTypeName(modelLayer.getHuman().getTypeName());
            instanceLayer.getHumanInstance().setHuman(modelLayer.getHuman());
        }
        if (instanceLayer.getAttributeEntityInstance().getInstanceName().equals("")){
            instanceLayer.getAttributeEntityInstance().setInstanceName(modelLayer.getAttributeEntity().getTypeName());
            instanceLayer.getAttributeEntityInstance().setEntityTypeName(modelLayer.getAttributeEntity().getTypeName());
            instanceLayer.getAttributeEntityInstance().setAttributeEntityType(modelLayer.getAttributeEntity());
        }
        int uncertainEntityExistNum=instanceLayer.getUncertainEntityInstances().size();
        for (UncertainEntityType uncertainEntityType:modelLayer.getUncertainEntityTypes()){
            ///把没有提到的不确定实体添加上去
            boolean exist=false;
            for (int i=0;i<uncertainEntityExistNum;i++){
                ///遍历前面添加的不确定实体
                UncertainEntityInstance uncertainEntityInstance=instanceLayer.getUncertainEntityInstances().get(i);
                if (uncertainEntityInstance.getEntityTypeName().equals(uncertainEntityType.getTypeName())){
                    exist=true;
                    break;
                }
            }
            if (!exist){
                ///把没有提到的不确定实体添加上去
                UncertainEntityInstance uncertainEntityInstance=new UncertainEntityInstance();
                uncertainEntityInstance.setInstanceName(uncertainEntityType.getTypeName());
                uncertainEntityInstance.setEntityTypeName(uncertainEntityType.getTypeName());
                uncertainEntityInstance.setUncertainEntityType(uncertainEntityType);
                instanceLayer.getUncertainEntityInstances().add(uncertainEntityInstance);
            }

        }

        ////应该来说monitored entity是保证有的，看sensor能检测什么？


        ///添加未提到的sensor
        int sensorExistNum=instanceLayer.getSensorInstances().size();
        for (SensorType sensorType:modelLayer.getSensorTypes()){
            boolean exist=false;
            for (int i=0;i<sensorExistNum;i++){
                SensorInstance sensorInstance=instanceLayer.getSensorInstances().get(i);
                if (sensorInstance.getEntityTypeName().equals(sensorType.getTypeName())){
                    exist=true;
                    break;
                }
            }
            if (!exist){
                SensorInstance sensorInstance=new SensorInstance();
                sensorInstance.setInstanceName(sensorType.getTypeName());
                sensorInstance.setEntityTypeName(sensorType.getTypeName());
                sensorInstance.setSensorType(sensorType);
                instanceLayer.getSensorInstances().add(sensorInstance);
            }
        }

        return instanceLayer;
    }

    ///以key-value的map形式存储实例层的模型
    public static HashMap<String,Instance> getInstanceMap(InstanceLayer instanceLayer){
        HashMap<String, Instance> instanceLayerMap=new HashMap<>();
        for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
            instanceLayerMap.put(deviceInstance.getInstanceName(),deviceInstance);
        }
        for (SensorInstance sensorInstance:instanceLayer.getSensorInstances()){
            instanceLayerMap.put(sensorInstance.getInstanceName(),sensorInstance);
        }
        for (UncertainEntityInstance uncertainEntityInstance:instanceLayer.getUncertainEntityInstances()){
            instanceLayerMap.put(uncertainEntityInstance.getInstanceName(),uncertainEntityInstance);
        }
        for (CyberServiceInstance cyberServiceInstance:instanceLayer.getCyberServiceInstances()){
            instanceLayerMap.put(cyberServiceInstance.getInstanceName(),cyberServiceInstance);
        }
        instanceLayerMap.put(instanceLayer.getAttributeEntityInstance().getInstanceName(),instanceLayer.getAttributeEntityInstance());
        instanceLayerMap.put(instanceLayer.getHumanInstance().getInstanceName(),instanceLayer.getHumanInstance());
        return instanceLayerMap;
    }


}
