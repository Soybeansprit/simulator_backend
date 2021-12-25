package com.example.demo.service;

import org.springframework.stereotype.Service;
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
     * instance1={instanceName:Emma,entityType:Human}
     * instance2={instanceName:SMS,entityType:SMS}
     * instance3={instanceName:Air,entityType:Air}
     * instance4={instanceName:Bulb_0,entityType:Bulb,location:L1}
     * instance5={instanceName:AirConditioner_0,entityType :AirConditioner,location:L1}
     * instance6={instanceName:Window_0,entityType:Window,location:L1}
     * instance7={instanceName:Window_1,entityType:Window,location:L2}
     * instance8={instanceName:TV_0,entityType:TV,location:L3}
     * */



}
