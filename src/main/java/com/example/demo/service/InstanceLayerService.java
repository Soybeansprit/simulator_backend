package com.example.demo.service;

import com.example.demo.bean.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

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
     * instance4={instanceName:Bulb_0,entityType:Bulb,location:L1}  设备
     * instance5={instanceName:AirConditioner_0,entityType :AirConditioner,location:L1}
     * instance6={instanceName:Window_0,entityType:Window,location:L1}
     * instance7={instanceName:Window_1,entityType:Window,location:L2}
     * instance8={instanceName:TV_0,entityType:TV,location:L3}
     * */

    ///以modelLayer和实例信息表作为输入，输出instanceLayer
    public static InstanceLayer getInstanceLayer(String filePath, String instanceInformationFileName, ModelLayer modelLayer) throws IOException {
        HashMap<String, EntityType> modelLayerMap=ModelLayerService.getModelMap(modelLayer);


        InstanceLayer instanceLayer=new InstanceLayer();
        ///先解析实例信息表，以.properties格式
        Properties properties=PropertyService.getProperties(filePath, instanceInformationFileName);
        for(String key:properties.stringPropertyNames()){
            if (key.equals("locations")){
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
