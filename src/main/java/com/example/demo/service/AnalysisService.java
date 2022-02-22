package com.example.demo.service;

import com.example.demo.bean.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对仿真数据进行分析，冲突、抖动、自定义性质、以及其他分析【满意度、能耗等】
 * 定位错误原因
 * 需要综合原因
 * */
public class AnalysisService {
    ///定位找原因

    ///综合IFD寻找更深层次的原因
    ///综合所有原因，对于单个场景下，对于不同冲突，对于找到触发规则如果触发时间都相同，可以综合在一起
    public static List<List<String[]>> getConflictDirectRulesSynthesize(List<List<String[]>> allConflictTimeAndRelatedRules){
        List<List<String[]>> synthesizedConflictDirectRules=new ArrayList<>();
        if (allConflictTimeAndRelatedRules.size()>0){
            synthesizedConflictDirectRules.add(allConflictTimeAndRelatedRules.get(0));
        }
        for (int i=1;i<allConflictTimeAndRelatedRules.size();i++){
            getConflictDirectRulesSynthesize(synthesizedConflictDirectRules,allConflictTimeAndRelatedRules.get(i));
        }
        return synthesizedConflictDirectRules;
    }
    ///对于两次冲突原因的综合
    public static void getConflictDirectRulesSynthesize(List<List<String[]>> synthesizedConflictDirectRules,List<String[]> conflictTimeAndRelatedRules){
        for (List<String[]> conflictDirectRules:synthesizedConflictDirectRules){
            ///判断是否与conflictTimeAndRelatedRules的规则和时间相同
            if (isRuleTimeSame(conflictDirectRules,conflictTimeAndRelatedRules)){
                return;
            }
        }
        synthesizedConflictDirectRules.add(conflictTimeAndRelatedRules);
    }
    public static boolean isRuleTimeSame(List<String[]> conflictTimeAndRelatedRules1,List<String[]> conflictTimeAndRelatedRules2){
        for (String[] conflictRule2:conflictTimeAndRelatedRules2){
            if (!conflictRule2[0].equals("conflictTime")){
                ////要都存在且完全相同
                boolean exist=false;
                for (String[] conflictRule1:conflictTimeAndRelatedRules1){
                    if (conflictRule1[0].equals(conflictRule2[0])){
                        if (conflictRule1[1].equals(conflictRule2[1])){
                            exist=true;
                        }
                        break;
                    }
                }
                if (!exist){
                    return false;
                }
            }
        }
        return true;
    }

    ///找某个时间触发的规则的前驱规则
    public static RuleAndPreRule getRulePreRule(String[] ruleTime,HashMap<String, IFDGraph.GraphNode> graphNodeHashMap,HashMap<String,Rule> ruleHashMap,HashMap<String,DataTimeValue> dataTimeValueHashMap){
        IFDGraph.GraphNode ruleNode=graphNodeHashMap.get(ruleTime[0]); ///找到ifd上对应的规则节点
        RuleAndPreRule currentRule=new RuleAndPreRule();
        currentRule.setCurrentRule(ruleHashMap.get(ruleTime[0]));   ////找到对应的规则
        double triggerTime=Double.parseDouble(ruleTime[1]);
        setTraversedFalse(graphNodeHashMap);
        ///获得当前规则的前驱规则
        getRulePreRule(ruleNode,graphNodeHashMap,currentRule,ruleHashMap,triggerTime,dataTimeValueHashMap);
        return currentRule;
    }
    //根据IFD，往前回溯找其他引发该规则的规则，当当前规则涉及设备状态时
    public static void getRulePreRule(IFDGraph.GraphNode ruleNode,HashMap<String, IFDGraph.GraphNode> graphNodeHashMap,RuleAndPreRule currentRule,HashMap<String,Rule> ruleHashMap, double triggeredTime, HashMap<String,DataTimeValue> dataTimeValueHashMap){
        ruleNode.setTraversed(true);
        for (IFDGraph.GraphNodeArrow triggerArrow:ruleNode.getpNodeArrowList()){
            IFDGraph.GraphNode triggerNode=triggerArrow.getGraphNode();   ///rule节点前面的节点为trigger节点
            if (!triggerNode.isTraversed()&&triggerNode.getRelatedInstanceAndColor()[1].equals("darkseagreen1")){
                triggerNode.setTraversed(true);  ///没被遍历过，并且是设备状态的trigger
                ///且是设备状态的trigger
                ///往前找action
                for (IFDGraph.GraphNodeArrow actionArrow:triggerNode.getpNodeArrowList()){
                    if (actionArrow.getColor().equals("red")&&actionArrow.getStyle().equals("")){
                        ///找到红色的实线的边
                        IFDGraph.GraphNode actionNode=actionArrow.getGraphNode();  ///action节点
                        if (actionNode.getShape().equals("record")&&!actionNode.isTraversed()){
                            actionNode.setTraversed(true);  ///没有遍历过的action
                            ///找到rule
                            for (IFDGraph.GraphNodeArrow ruleArrow:actionNode.getpNodeArrowList()){
                                IFDGraph.GraphNode otherRuleNode=ruleArrow.getGraphNode();
                                if (!otherRuleNode.isTraversed()){ ///找没有遍历过的rule
                                    otherRuleNode.setTraversed(true);
                                    ///看该rule能否在这之前触发
                                    DataTimeValue dataTimeValue=dataTimeValueHashMap.get(otherRuleNode.getName());
                                    double otherTriggeredTime=canRuleBeTriggeredInDuration(dataTimeValue,0.0,triggeredTime);
                                    if (otherTriggeredTime>0.0){
                                        ///表明能被触发
                                        RuleAndPreRule preRule=new RuleAndPreRule();
                                        preRule.setCurrentRule(ruleHashMap.get(dataTimeValue.getDataName()));
                                        currentRule.getPreRules().add(preRule);
                                        ///递归找其他前驱规则
                                        getRulePreRule(otherRuleNode,graphNodeHashMap,preRule,ruleHashMap,otherTriggeredTime,dataTimeValueHashMap);
//                                        ///把graphNodes的traversed重新置为false
//                                        setTraversedFalse(graphNodeHashMap);
//                                        ///把初始的ruleNode置为true
//                                        ruleNode.setTraversed(true);
//                                        otherRuleNode.setTraversed(true);
                                    }
                                    otherRuleNode.setTraversed(false);
                                }
                            }
                            actionNode.setTraversed(false);
                        }
                    }
                }
                triggerNode.setTraversed(false);
            }
            ruleNode.setTraversed(false);
        }
    }
    ////把graphNodes的traversed重新置为false
    public static void setTraversedFalse(HashMap<String, IFDGraph.GraphNode> graphNodeHashMap){
        for (Map.Entry<String, IFDGraph.GraphNode> graphNodeEntry:graphNodeHashMap.entrySet()){
            graphNodeEntry.getValue().setTraversed(false);
        }
    }
    ///找到所有设备的冲突情况,针对单个场景
    public static List<List<List<String[]>>> getAllDeviceConflictDirectRules(List<DeviceConflict> deviceConflicts,List<DataTimeValue> dataTimeValues,InstanceLayer instanceLayer,List<Rule> rules){
        List<List<List<String[]>>> allDeviceConflictDirectRules=new ArrayList<>();
        for (DeviceConflict deviceConflict:deviceConflicts){
            if (deviceConflict.getConflictTimeValues().size()>0){
                for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
                    if (deviceInstance.getInstanceName().equals(deviceConflict.getInstanceName())){
                        List<List<String[]>> conflictDirectRules=getConflictDirectRules(deviceConflict,dataTimeValues,deviceInstance,rules);
                        allDeviceConflictDirectRules.add(conflictDirectRules);
                        System.out.println(conflictDirectRules);
                        break;
                    }
                }
            }
        }
        return allDeviceConflictDirectRules;
    }
    ///冲突定位找原因，每次冲突都定位找原因,找到在那段时间触发的规则及触发时间，同时该规则要与该设备相关,针对单个场景中该设备的所有冲突
    public static List<List<String[]>> getConflictDirectRules(DeviceConflict deviceConflict,List<DataTimeValue> dataTimeValues,DeviceInstance deviceInstance,List<Rule> rules){
        List<List<String[]>> allConflictTimeAndRelatedRules=new ArrayList<>();
        for (List<Double> conflictTimeValue:deviceConflict.getConflictTimeValues()){
            List<String[]> conflictTimeAndTriggeredRules=getSingleConflictDirectRules(conflictTimeValue,dataTimeValues,deviceInstance,rules);
            allConflictTimeAndRelatedRules.add(conflictTimeAndTriggeredRules);
        }
        return allConflictTimeAndRelatedRules;
    }
    ///找某次冲突相关的并且触发了的规则，及其触发时间
    public static List<String[]> getSingleConflictDirectRules(List<Double> conflictTimeValue,List<DataTimeValue> dataTimeValues,DeviceInstance deviceInstance,List<Rule> rules){
        ///每次冲突都定位找原因
        double conflictTime=conflictTimeValue.get(0);
        ///找在该时间段触发的所有规则
        List<String[]> conflictTimeAndTriggeredRules=new ArrayList<>();
        String[] conflictAndTime=new String[2];
        conflictAndTime[0]="conflictTime";
        conflictAndTime[1]=conflictTime+"";
        conflictTimeAndTriggeredRules.add(conflictAndTime);  ///冲突时间
        conflictTimeAndTriggeredRules.addAll(getTriggeredRuleTimes(dataTimeValues,conflictTime)); ///找到该时间触发的规则
        ///删掉无关的rule
        for(int i=conflictTimeAndTriggeredRules.size()-1;i>0;i--){
            for (Rule rule:rules){
                if (rule.getRuleName().equals(conflictTimeAndTriggeredRules.get(i)[0])){
                    ///找到对应的规则信息
                    ///看是否与该设备相关

                    if (!isRuleRelatedToDeviceInstance(rule,deviceInstance)){
                        ///无关则删除
                        conflictTimeAndTriggeredRules.remove(i);
                    }
                    break;
                }
            }
        }
        return conflictTimeAndTriggeredRules;
    }
    ///看是否是cause指定设备状态的rule,如果是，则
    public static boolean isRuleCausingInstanceStateValue(Rule rule,DeviceInstance deviceInstance,int stateValue){
        for (String action:rule.getAction()){
            String[] instanceSync=action.split("\\.");
            if (instanceSync[0].trim().equals(deviceInstance.getInstanceName())){
                ///与该设备有关的规则
                ///再看是否是指定状态
                for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceInstance.getDeviceType().getStateSyncValueEffects()){
                    int value=Integer.parseInt(stateSyncValueEffect.getValue());
                    if (value==stateValue){
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }
    ///看该规则与设备实例是否相关
    public static boolean isRuleRelatedToDeviceInstance(Rule rule,DeviceInstance deviceInstance){
        for (String action:rule.getAction()){
            String[] instanceSync=action.split("\\.");
            if (instanceSync[0].trim().equals(deviceInstance.getInstanceName())){
                ///与该设备有关的规则
                return true;
            }
        }
        return false;
    }
    ///找到在该时间点触发的规则和触发开始时间
    public static List<String[]> getTriggeredRuleTimes(List<DataTimeValue> dataTimeValues,double time){
        List<String[]> triggeredRuleTimes=new ArrayList<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.getDataName().indexOf("rule")>=0){
                ///找到规则相关的仿真路径
                ///看在该时间有没有触发
                double triggeredTime=canRuleBeTriggeredInDuration(dataTimeValue,time,time);
                if (triggeredTime>0.0){
                    ///触发了
                    String[] triggeredRuleTime=new String[2];
                    triggeredRuleTime[0]=dataTimeValue.getDataName();
                    triggeredRuleTime[1]=triggeredTime+"";
                    triggeredRuleTimes.add(triggeredRuleTime);
                }
            }
        }
        return triggeredRuleTimes;
    }
    ///看某条规则能否在特定时间段触发,返回触发的时间点
    public static double canRuleBeTriggeredInDuration(DataTimeValue dataTimeValue,double leftTime,double rightTime){
        for (int i=0;i<dataTimeValue.getTimeValues().size()-1;i+=2){
            double[] currentTimeValue=dataTimeValue.getTimeValues().get(i);
            double[] nextTimeValue=dataTimeValue.getTimeValues().get(i+1);
            if (currentTimeValue[1]<0.5){
                continue;
            }
            if (currentTimeValue[0]>rightTime){
                break;
            }
            if (nextTimeValue[0]>=leftTime&&currentTimeValue[0]<=rightTime){
                ///在该时间段能触发
                return currentTimeValue[0];
            }
        }
        ///不能被触发
        return 0.0;
    }





//    ////综合所有冲突原因
//    public static List<List<List<String[]>>> getAllConflictsStatesDirectRulesSynthesis(List<List<List<String[]>>> allConflictsStatesDirectRules){
//        List<List<List<String[]>>> synthesisOfDeviceConflictsStatesDirectRules=new ArrayList<>();
//
//    }
    ////综合不同conflicts的直接原因，对于两个conflicts状态导致直接原因，如果冲突状态、导致冲突的原因和触发时间都相同，则可以综合为一个
    public static List<List<List<String[]>>> getSynthesisOfDeviceConflictsStatesDirectRules(List<List<List<String[]>>> synthesisOfDeviceConflictsStatesDirectRules, List<List<String[]>> singleConflictStatesDirectRules){
        for (List<List<String[]>> conflictStatesDirectRules:synthesisOfDeviceConflictsStatesDirectRules){
            if (isTwoStatesDirectRulesSame(conflictStatesDirectRules,singleConflictStatesDirectRules)){
                return synthesisOfDeviceConflictsStatesDirectRules;
            }
        }
        synthesisOfDeviceConflictsStatesDirectRules.add(singleConflictStatesDirectRules);
        return synthesisOfDeviceConflictsStatesDirectRules;
    }
    ///判断两个状态原因是否相同，规则和触发时间
    public static boolean isTwoStatesDirectRulesSame(List<List<String[]>> statesDirectRules1,List<List<String[]>> statesDirectRules2){
        for (int i=0;i<statesDirectRules2.size();i++){
            boolean stateExist=false;
            for (int j=0;j<statesDirectRules1.size();j++){
                if (statesDirectRules2.get(i).get(0)[0].equals(statesDirectRules1.get(j).get(0)[0])){
                    ///状态相同
                    stateExist=true;
                    ////接下来判断规则是否相同
                    for (int m=1;m<statesDirectRules2.get(i).size();m++){
                        ///看这条规则和触发时间是否存在
                        boolean ruleExist=false;
                        String[] ruleTriggerTime2=statesDirectRules2.get(i).get(m);
                        for (int n=1;n<statesDirectRules1.get(j).size();n++){
                            String[] ruleTriggerTime1=statesDirectRules1.get(j).get(m);
                            if (ruleTriggerTime1[0].equals(ruleTriggerTime2[0])&&ruleTriggerTime1[1].equals(ruleTriggerTime2[1])){
                                ruleExist=true;
                                break;
                            }
                        }
                        if (!ruleExist){
                            return false;
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }


    ////综合jitter的原因
    public static void getDeviceJitterStatesCausingRulesSynthesized(List<List<DeviceStateAndCausingRules>> deviceAllJitterStatesRuleAndPreRules){
        /////首先对每一段jitter都分别进行综合，最后再对所有jitter一起综合
        for (List<DeviceStateAndCausingRules> singleJitterStatesRuleAndPreRules:deviceAllJitterStatesRuleAndPreRules){
            getDeviceSingleJitterStatesCausingRulesSynthesized(singleJitterStatesRuleAndPreRules);
        }
        ////然后对n段综合后的整体综合
        getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllJitterStatesRuleAndPreRules);
    }
    /////对一段jitter进行综合
    public static void getDeviceSingleJitterStatesCausingRulesSynthesized(List<DeviceStateAndCausingRules> singleJitterStatesRuleAndPreRules){
        /////对一段jitter进行综合
        for (int i=singleJitterStatesRuleAndPreRules.size()-1;i>=0;i--){
            ///判断前面相同状态的节点中的原因是否包含当前节点状态的原因，如果包含，则删除当前节点，如果含有不包含的原因，则添加不存在的规则，并删除当前节点
            DeviceStateAndCausingRules currentStateCausingRules=singleJitterStatesRuleAndPreRules.get(i);
            boolean existState=false;
            for (int j=0;j<i;j++){
                ///先判断是否相同状态，相同状态才判断原因是否相同
                DeviceStateAndCausingRules preStateCausingRules=singleJitterStatesRuleAndPreRules.get(j);
                if (preStateCausingRules.getStateName().equals(currentStateCausingRules.getStateName())){
                    existState=true;
                    ////判断pre中是否包含current的原因
                    for (RuleAndPreRule currentRuleAndCausingRules:currentStateCausingRules.getCausingRulesAndPreRules()){
                        boolean exist=false;
                        for (RuleAndPreRule preRuleAndCausingRules:preStateCausingRules.getCausingRulesAndPreRules()){
                            if (isTwoRuleAndPreRuleSame(preRuleAndCausingRules,currentRuleAndCausingRules)){
                                exist=true;
                                break;
                            }
                        }
                        if (!exist){
                            ////不存在该原因，就添加上去
                            preStateCausingRules.getCausingRulesAndPreRules().add(currentRuleAndCausingRules);
                        }
                    }
                }

            }
            if (existState){
                singleJitterStatesRuleAndPreRules.remove(i);
            }
        }
    }

    ////综合jitter原因
    public static List<List<DeviceStateAndCausingRules>> getDeviceJitterStatesCausingRulesSynthesize(List<List<DeviceStateAndCausingRules>> deviceAllJitterStatesRuleAndPreRules){
        List<List<DeviceStateAndCausingRules>> synthesizedDeviceAllStatesRuleAndPreRules=new ArrayList<>();
        /////首先对每一段jitter都分别进行综合，最后再对所有jitter一起综合
        List<DeviceStateAndCausingRules> firstSynthesizedSingleStatesRuleAndPreRules=getSingleJitterStatesCausingRulesSynthesized(deviceAllJitterStatesRuleAndPreRules.get(0));
        synthesizedDeviceAllStatesRuleAndPreRules.add(firstSynthesizedSingleStatesRuleAndPreRules);
        for (int i=1;i<deviceAllJitterStatesRuleAndPreRules.size();i++){
            List<DeviceStateAndCausingRules> synthesizedSingleJitterCausingRuleAndPreRules=getSingleJitterStatesCausingRulesSynthesized(deviceAllJitterStatesRuleAndPreRules.get(i));
            ///跟前面的进行综合
            getStatesCausingRulesSynthesized(synthesizedDeviceAllStatesRuleAndPreRules,synthesizedSingleJitterCausingRuleAndPreRules);
        }
        return synthesizedDeviceAllStatesRuleAndPreRules;
    }

    ////对一段jitter的原因进行综合
    public static List<DeviceStateAndCausingRules> getSingleJitterStatesCausingRulesSynthesized(List<DeviceStateAndCausingRules> singleJitterStatesRuleAndPreRules){
        List<DeviceStateAndCausingRules> synthesizedSingleJitterCausingRuleAndPreRules=new ArrayList<>();
        synthesizedSingleJitterCausingRuleAndPreRules.add(singleJitterStatesRuleAndPreRules.get(0));  ////先添加第一个
        for (int i=0;i<singleJitterStatesRuleAndPreRules.size();i++){
            ///判断在综合里是否已经存在
            DeviceStateAndCausingRules deviceStateAndCausingRules1=singleJitterStatesRuleAndPreRules.get(i);
            boolean exist=false;
            for (DeviceStateAndCausingRules deviceStateAndCausingRules2:synthesizedSingleJitterCausingRuleAndPreRules){
                if (isTwoStateCausingRulesSame(deviceStateAndCausingRules1,deviceStateAndCausingRules2)){
                    exist=true;
                    break;
                }
            }
            if (!exist){
                ///不存在就添加
                synthesizedSingleJitterCausingRuleAndPreRules.add(deviceStateAndCausingRules1);
            }
        }
        return synthesizedSingleJitterCausingRuleAndPreRules;
    }

    ////综合冲突原因
    public static List<List<DeviceStateAndCausingRules>> getDeviceConflictStatesCausingRulesSynthesize(List<List<DeviceStateAndCausingRules>> deviceAllConflictStatesRuleAndPreRules){
        List<List<DeviceStateAndCausingRules>> synthesizedDeviceAllStatesRuleAndPreRules=new ArrayList<>();
        synthesizedDeviceAllStatesRuleAndPreRules.add(deviceAllConflictStatesRuleAndPreRules.get(0));
        for (int i=1;i<deviceAllConflictStatesRuleAndPreRules.size();i++){
            getStatesCausingRulesSynthesized(synthesizedDeviceAllStatesRuleAndPreRules,deviceAllConflictStatesRuleAndPreRules.get(i));
        }
        return synthesizedDeviceAllStatesRuleAndPreRules;
    }

    ///综合冲突原因
    public static void getDeviceConflictStatesCausingRulesSynthesized(List<List<DeviceStateAndCausingRules>> deviceAllConflictStatesRuleAndPreRules){
        getDeviceConflictOrJitterStatesCausingRulesSynthesized(deviceAllConflictStatesRuleAndPreRules);
//        for (int i=deviceAllConflictStatesRuleAndPreRules.size()-1;i>=0;i--){
//            ///从后往前遍历，看当前的冲突状态及原因在前面是否已经包含了，如果已经包含，就删除这个，如果当前的包含前面的某个，则替换，并删除当前节点，如果不存在，则不处理
//            List<DeviceStateAndCausingRules> currentDeviceConflictStatesCausingRules=deviceAllConflictStatesRuleAndPreRules.get(i);
//            ///遍历该节点前面的节点，看是否存在上述情况
//            for (int j=0;j<i;j++){
//                List<DeviceStateAndCausingRules> toBeCheckDeviceConflictStatesCausingRules=deviceAllConflictStatesRuleAndPreRules.get(j);
//                boolean allExist=true;
//                boolean allViseExist=true;
//                for (DeviceStateAndCausingRules stateCausingRules:currentDeviceConflictStatesCausingRules){
//                    ///看已有的是否包含了这个，或者这个是否包含了已有的
//                    boolean exist=false;
//                    boolean viseExist=false;
//                    for (DeviceStateAndCausingRules otherStateCausingRules:toBeCheckDeviceConflictStatesCausingRules){
//                        if(isTwoStateCausingRulesSame(stateCausingRules,otherStateCausingRules)){
//                            exist=true;
//                        }
//                        if(isTwoStateCausingRulesSame(otherStateCausingRules,stateCausingRules)){
//                            viseExist=true;
//                        }
//                    }
//                    if (!exist){
//                        allExist=false;
//                    }
//                    if (!viseExist){
//                        allViseExist=false;
//                    }
//                    if (!exist&&!viseExist){
//                        break;
//                    }
//                }
//                if (allExist){
//                    ///前面的这个节点包含当前节点，就删掉当前节点
//                    deviceAllConflictStatesRuleAndPreRules.remove(i);
//                    break;
//                }
//                if (allViseExist){
//                    ///当前节点包含前面的这个节点，替换节点内容，并删除当前节点
//                    deviceAllConflictStatesRuleAndPreRules.set(j,currentDeviceConflictStatesCausingRules);
//                    deviceAllConflictStatesRuleAndPreRules.remove(i);
//                    break;
//                }
//            }
//        }
    }

    ////综合原因
    public static void getDeviceConflictOrJitterStatesCausingRulesSynthesized(List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules){
        for (int i=deviceAllStatesRuleAndPreRules.size()-1;i>=0;i--){
            ///从后往前遍历，看当前节点状态及原因在前面是否已经包含了，如果已经包含，就删除这个，如果当前的包含前面的某个，则替换，并删除当前节点，如果不存在，则不处理
            List<DeviceStateAndCausingRules> currentDeviceStatesCausingRules=deviceAllStatesRuleAndPreRules.get(i);
            ///遍历该节点前面的节点，看是否存在上述情况
            for (int j=0;j<i;j++){
                List<DeviceStateAndCausingRules> toBeCheckDeviceStatesCausingRules=deviceAllStatesRuleAndPreRules.get(j);
                boolean allExist=true;
                boolean allViseExist=true;
                for (DeviceStateAndCausingRules stateCausingRules:currentDeviceStatesCausingRules){
                    ///看已有的是否包含了这个，或者这个是否包含了已有的
                    boolean exist=false;
                    boolean viseExist=false;
                    for (DeviceStateAndCausingRules otherStateCausingRules:toBeCheckDeviceStatesCausingRules){
                        if(isTwoStateCausingRulesSame(stateCausingRules,otherStateCausingRules)){
                            exist=true;
                        }
                        if(isTwoStateCausingRulesSame(otherStateCausingRules,stateCausingRules)){
                            viseExist=true;
                        }
                    }
                    if (!exist){
                        allExist=false;
                    }
                    if (!viseExist){
                        allViseExist=false;
                    }
                    if (!exist&&!viseExist){
                        break;
                    }
                }
                if (allExist){
                    ///前面的这个节点包含当前节点，就删掉当前节点
                    deviceAllStatesRuleAndPreRules.remove(i);
                    break;
                }
                if (allViseExist){
                    ///当前节点包含前面的这个节点，替换节点内容，并删除当前节点
                    deviceAllStatesRuleAndPreRules.set(j,currentDeviceStatesCausingRules);
                    deviceAllStatesRuleAndPreRules.remove(i);
                    break;
                }
            }
        }
    }
    ////综合原因
    public static void getStatesCausingRulesSynthesized(List<List<DeviceStateAndCausingRules>> synthesizedDeviceAllStatesRuleAndPreRules,List<DeviceStateAndCausingRules> toBeSynthesizedStatesCausingRules){
        for (int i=0;i<synthesizedDeviceAllStatesRuleAndPreRules.size();i++){
            List<DeviceStateAndCausingRules> deviceStateAndCausingRulesList=synthesizedDeviceAllStatesRuleAndPreRules.get(i);
            ///遍历每次冲突状态发生的原因
            ///看状态和相关规则组成是否与待合并的相同
            boolean allExist=true;
            boolean allViseExist=true;
            for (DeviceStateAndCausingRules stateCausingRules:toBeSynthesizedStatesCausingRules){
                ///看已有的是否包含了这个，或者这个是否包含了已有的
                boolean exist=false;
                boolean viseExist=false;
                for (DeviceStateAndCausingRules otherStateCausingRules:deviceStateAndCausingRulesList){
                    if(isTwoStateCausingRulesSame(stateCausingRules,otherStateCausingRules)){
                        exist=true;
                    }else if(isTwoStateCausingRulesSame(otherStateCausingRules,stateCausingRules)){
                        viseExist=true;
                    }
                }
                if (!exist){
                    allExist=false;
                }
                if (!viseExist){
                    allViseExist=false;
                }
                if (!exist&&!viseExist){
                    break;
                }
            }
            if (allExist){
                return;
            }
            if (allViseExist){
                synthesizedDeviceAllStatesRuleAndPreRules.set(i,toBeSynthesizedStatesCausingRules);
                return;
            }
        }
        synthesizedDeviceAllStatesRuleAndPreRules.add(toBeSynthesizedStatesCausingRules);
    }
    ////判断两个stateCausingRules是否相同
    public static boolean isTwoStateCausingRulesSame(DeviceStateAndCausingRules deviceStateAndCausingRules1,DeviceStateAndCausingRules deviceStateAndCausingRules2){
        if (deviceStateAndCausingRules1.getDeviceName().equals(deviceStateAndCausingRules2.getDeviceName())&&
                deviceStateAndCausingRules1.getStateName().equals(deviceStateAndCausingRules2.getStateName())){
            ////首先设备状态名要相同
            ////判断规则是否相同
            for (RuleAndPreRule ruleAndPreRule1:deviceStateAndCausingRules1.getCausingRulesAndPreRules()){
                boolean exist=false;
                for (RuleAndPreRule ruleAndPreRule2:deviceStateAndCausingRules2.getCausingRulesAndPreRules()){
                    if (ruleAndPreRule1.getCurrentRule().getRuleName().equals(ruleAndPreRule2.getCurrentRule().getRuleName())){
                        if (isTwoRuleAndPreRuleSame(ruleAndPreRule1,ruleAndPreRule2)){
                            exist=true;
                        }
                        break;
                    }
                }
                if (!exist){
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    ////获得一个场景下某设备所有冲突/jitter的直接原因、前驱原因，用于后续的综合
    public static List<List<DeviceStateAndCausingRules>> getDeviceAllStatesRuleAndPreRules(List<List<List<String[]>>> deviceAllStatesDirectRules,
                                                                                          HashMap<String,DataTimeValue> dataTimeValueHashMap,HashMap<String,RuleAndPreRule> ruleNameRuleAndPreRuleHashMap,HashMap<String,Rule> ruleHashMap){
        HashMap<String,RuleAndPreRule> ruleTimeRuleAndPreRuleHashMap=new HashMap<>();
        List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=new ArrayList<>();
        for (List<List<String[]>> conflictStatesDirectRules:deviceAllStatesDirectRules){

            List<DeviceStateAndCausingRules> conflictStatesAndCausingRules=new ArrayList<>();
            for (List<String[]> stateDirectRules:conflictStatesDirectRules){
                ///每个状态
                DeviceStateAndCausingRules deviceStateAndCausingRules=getDeviceStateAndCausingRulesForStateDirectRules(stateDirectRules,ruleTimeRuleAndPreRuleHashMap,dataTimeValueHashMap,ruleNameRuleAndPreRuleHashMap,ruleHashMap);
                conflictStatesAndCausingRules.add(deviceStateAndCausingRules);
            }
            deviceAllStatesRuleAndPreRules.add(conflictStatesAndCausingRules);
        }
        return deviceAllStatesRuleAndPreRules;
    }


    /////将List<String[]> 格式的stateDirectRule转为DeviceStateAndCausingRules类，并获得直接原因的前驱原因
    public static DeviceStateAndCausingRules getDeviceStateAndCausingRulesForStateDirectRules(List<String[]> stateDirectRules,HashMap<String,RuleAndPreRule> ruleTimeRuleAndPreRuleHashMap,
                                                                                              HashMap<String,DataTimeValue> dataTimeValueHashMap,HashMap<String,RuleAndPreRule> ruleNameRuleAndPreRuleHashMap,HashMap<String,Rule> ruleHashMap){
        String[] deviceStateNameValue=stateDirectRules.get(0);   ///0-deviceName, 1-stateName, 2-stateValue
        DeviceStateAndCausingRules deviceStateAndCausingRules=new DeviceStateAndCausingRules();
        deviceStateAndCausingRules.setDeviceName(deviceStateNameValue[0]);   ////设备名
        deviceStateAndCausingRules.setStateName(deviceStateNameValue[1]);   ////状态名
        deviceStateAndCausingRules.setStateValue(Integer.parseInt(deviceStateNameValue[2]));   ////状态值
        for (int i=1;i<stateDirectRules.size();i++){
            ///获得规则和触发时间  0-rule, 1-triggeredTime
            String[] ruleTriggeredTime=stateDirectRules.get(i);
            String ruleName=ruleTriggeredTime[0];    ///规则名
            double triggeredTime=Double.parseDouble(ruleTriggeredTime[1]);   ////触发时间

            RuleAndPreRule ruleAndTriggeredPreRule=ruleTimeRuleAndPreRuleHashMap.get(ruleName+":"+triggeredTime);  ///看是否已经计算过
            if (ruleAndTriggeredPreRule==null) {  ///没有计算过
                ruleAndTriggeredPreRule=new RuleAndPreRule();
                ruleAndTriggeredPreRule.setCurrentRule(ruleHashMap.get(ruleName));
                RuleAndPreRule ruleAndPreRule=ruleNameRuleAndPreRuleHashMap.get(ruleName);
                ////获得触发规则的触发了的前驱规则
                getRuleTriggeredPreRules(triggeredTime,dataTimeValueHashMap,ruleAndTriggeredPreRule,ruleAndPreRule);
                ruleTimeRuleAndPreRuleHashMap.put(ruleName+":"+triggeredTime,ruleAndTriggeredPreRule);
            }

            deviceStateAndCausingRules.getCausingRulesAndPreRules().add(ruleAndTriggeredPreRule);
        }
        return deviceStateAndCausingRules;
    }


    ////获得一个场景下某设备的所有冲突的直接原因
    public static List<List<List<String[]>>> getDeviceConflictStatesDirectRulesInAScenario(DeviceConflict deviceConflict,List<DataTimeValue> dataTimeValues,DeviceInstance deviceInstance,HashMap<String,Rule> ruleHashMap){
        List<List<List<String[]>>> deviceAllConflictStatesDirectRules=new ArrayList<>();   ////一次conflict对应一个List<List<String[]>>
        for (List<Double> conflictTimeValue:deviceConflict.getConflictTimeValues()){
            ////遍历每次冲突,分别找到对应状态的规则  一个stateDirectRules对应一个List<String[]>
            List<List<String[]>> singleConflictStatesDirectRules=getSingleConflictStatesDirectRules(conflictTimeValue,dataTimeValues,deviceInstance,ruleHashMap);
            if (singleConflictStatesDirectRules==null) {
                continue;
            }
            deviceAllConflictStatesDirectRules.add(singleConflictStatesDirectRules);
        }
        return deviceAllConflictStatesDirectRules;
    }
    ////获得某次conflict直接原因,分别获得此冲突所涉及的状态的原因,conflictTimeValue给出冲突时间和冲突的状态取值,一个state对应一个List<String[]>
    public static List<List<String[]>> getSingleConflictStatesDirectRules(List<Double> conflictTimeValue,List<DataTimeValue> dataTimeValues,DeviceInstance deviceInstance,HashMap<String,Rule> ruleHashMap){

        double conflictTime=conflictTimeValue.get(0);   ////冲突时间
        List<List<String[]>> singleConflictStatesDirectRules=new ArrayList<>();
        for (int i=1;i<conflictTimeValue.size();i++){
            double stateValue=conflictTimeValue.get(i);
            ////获得该状态的原因
            List<String[]> stateCausingRules=getStateCausingRules(deviceInstance.getInstanceName(),(int) stateValue,conflictTime,deviceInstance,ruleHashMap,dataTimeValues);
            if (stateCausingRules.size()>1){
                ///表明有相应的触发规则,因为get(0)上存的是设备状态信息
                singleConflictStatesDirectRules.add(stateCausingRules);
            }
        }
        if (singleConflictStatesDirectRules.size()<2){
            ////有些状态找不出相应的规则，conflict失效
            return null;
        }
        return singleConflictStatesDirectRules;
    }



    ////获得一个场景下所有jitter直接原因
    public static List<List<List<String[]>>> getDeviceJitterStatesDirectRulesInAScenario(DeviceJitter deviceJitter,List<DataTimeValue> dataTimeValues,DeviceInstance deviceInstance,HashMap<String,Rule> ruleHashMap){
        List<List<List<String[]>>> deviceAllJitterStatesDirectRules=new ArrayList<>();   ////一段jitter对应一个List<List<String[]>>
        for (List<double[]> jitterTimeValue:deviceJitter.getJitterTimeValues()){
            List<List<String[]>> singleJitterStatesDirectRules=getSingleJitterStatesDirectRules(jitterTimeValue,dataTimeValues,deviceInstance,ruleHashMap);
            if (singleJitterStatesDirectRules==null){
                continue;
            }
            deviceAllJitterStatesDirectRules.add(singleJitterStatesDirectRules);
        }
        return deviceAllJitterStatesDirectRules;
    }
    /////获得某一段jitter的直接原因，分别获得每个状态的直接原因，List<double[]>, 一次抖动jitterTimeValue.get(0)[0]=time, jitterTimeValue.get(0)[1]=value...，一个state对应一个List<String[]>
    public static List<List<String[]>> getSingleJitterStatesDirectRules(List<double[]> jitterTimeValue,List<DataTimeValue> dataTimeValues,DeviceInstance deviceInstance,HashMap<String,Rule> ruleHashMap){
        List<List<String[]>> singleJitterStatesDirectRules=new ArrayList<>();
        for (double[] timeValue:jitterTimeValue){
            double stateTime=timeValue[0];
            int stateValue=(int) timeValue[1];
            ///获得该状态的直接原因
            List<String[]> stateCausingRules=getStateCausingRules(deviceInstance.getInstanceName(),stateValue,stateTime,deviceInstance,ruleHashMap,dataTimeValues);
            if (stateCausingRules.size()>1){
                ///表明有相应的触发规则,因为get(0)上存的是设备状态信息
                singleJitterStatesDirectRules.add(stateCausingRules);
            }
        }
        if (singleJitterStatesDirectRules.size()<2){
            ///jitter失效
        }
        return singleJitterStatesDirectRules;
    }
    ///判断两个rulePreRule是否相同
    public static boolean isTwoRuleAndPreRuleSame(RuleAndPreRule ruleAndPreRule1,RuleAndPreRule ruleAndPreRule2){
        if (ruleAndPreRule1==ruleAndPreRule2){
            return true;
        }
        if (!ruleAndPreRule1.getCurrentRule().getRuleName().equals(ruleAndPreRule2.getCurrentRule().getRuleName())){
            ///如果当前规则都不同，则不同
            return false;
        }
        if (ruleAndPreRule1.getPreRules().size()!=ruleAndPreRule2.getPreRules().size()){
            ///如果前置规则数量不同，则不同
            return false;
        }
        ///判断前置规则是否
        for (int i=0;i<ruleAndPreRule1.getPreRules().size();i++){
            if (!isTwoRuleAndPreRuleSame(ruleAndPreRule1.getPreRules().get(i),ruleAndPreRule2.getPreRules().get(i))){
                return false;
            }
        }
        return true;
    }

    ///找某个时间触发的规则的前驱规则，找到前驱规则中可触发的规则，因此需要输入当前规则及其前驱规则，看前驱规则能否触发
    public static void getRuleTriggeredPreRules(double time,HashMap<String,DataTimeValue> dataTimeValueHashMap,RuleAndPreRule ruleAndTriggeredPreRule,RuleAndPreRule ruleAndPreRule){
        for (RuleAndPreRule preRule:ruleAndPreRule.getPreRules()){
            ////遍历前驱规则，看在time时间之前能否被触发
            DataTimeValue dataTimeValue=dataTimeValueHashMap.get(preRule.getCurrentRule().getRuleName());
            double preTriggeredTime=canRuleBeTriggeredInDuration(dataTimeValue,0.0,time);
            if (preTriggeredTime>0){
                ////能被触发则添加该规则到ruleAndPreRule的preRule中，同时看该preRule的前驱规则能否被触发
                RuleAndPreRule triggeredPreRule=new RuleAndPreRule();
                triggeredPreRule.setCurrentRule(preRule.getCurrentRule());
                ruleAndTriggeredPreRule.getPreRules().add(triggeredPreRule);
                getRuleTriggeredPreRules(preTriggeredTime,dataTimeValueHashMap,triggeredPreRule,preRule);
            }
        }
    }
    ///找所有规则在IFD中的前驱规则，为所谓是否触发
    public static List<RuleAndPreRule> getAllRulePreRules(HashMap<String, IFDGraph.GraphNode> graphNodeHashMap,HashMap<String,Rule> ruleHashMap){
        List<RuleAndPreRule> allRulePreRules=new ArrayList<>();
        for (Map.Entry<String,Rule> ruleEntry:ruleHashMap.entrySet()){
            Rule rule=ruleEntry.getValue();
            RuleAndPreRule currentRule=new RuleAndPreRule();
            currentRule.setCurrentRule(rule);
            getRulePreRules(graphNodeHashMap.get(rule.getRuleName()),currentRule,ruleHashMap);
            allRulePreRules.add(currentRule);
        }
        return allRulePreRules;
    }
    ///找某条规则在IFD中的前驱规则，无所谓是否触发
    public static void getRulePreRules(IFDGraph.GraphNode ruleNode,RuleAndPreRule currentRule,HashMap<String,Rule> ruleHashMap){
        ruleNode.setTraversed(true);
        for (IFDGraph.GraphNodeArrow triggerArrow:ruleNode.getpNodeArrowList()){
            IFDGraph.GraphNode triggerNode=triggerArrow.getGraphNode();   ///rule节点前面的节点为trigger节点
            if (!triggerNode.isTraversed()&&triggerNode.getRelatedInstanceAndColor()[1].equals("darkseagreen1")){
                triggerNode.setTraversed(true);  ///没被遍历过，并且是设备状态的trigger
                ///且是设备状态的trigger
                ///往前找action
                for (IFDGraph.GraphNodeArrow actionArrow:triggerNode.getpNodeArrowList()){
                    if (actionArrow.getColor().equals("red")&&actionArrow.getStyle().equals("")){
                        ///找到红色的实线的边
                        IFDGraph.GraphNode actionNode=actionArrow.getGraphNode();  ///action节点
                        if (actionNode.getShape().equals("record")&&!actionNode.isTraversed()){
                            actionNode.setTraversed(true);  ///没有遍历过的action
                            ///找到rule
                            for (IFDGraph.GraphNodeArrow ruleArrow:actionNode.getpNodeArrowList()){
                                IFDGraph.GraphNode otherRuleNode=ruleArrow.getGraphNode();
                                if (otherRuleNode.getShape().equals("hexagon")&&!otherRuleNode.isTraversed()){ ///找没有遍历过的rule
                                    otherRuleNode.setTraversed(true);
                                    ///是前驱规则
                                    RuleAndPreRule preRule=new RuleAndPreRule();
                                    preRule.setCurrentRule(ruleHashMap.get(otherRuleNode.getName()));
                                    currentRule.getPreRules().add(preRule);
                                    ///递归找其他前驱规则
                                    getRulePreRules(otherRuleNode,preRule,ruleHashMap);
                                    ///把graphNodes的traversed重新置为false
//                                    setTraversedFalse(graphNodeHashMap);
//                                    ///把初始的ruleNode置为true
//                                    ruleNode.setTraversed(true);
//                                    otherRuleNode.setTraversed(true);
                                    otherRuleNode.setTraversed(false);
                                }
                            }
                            actionNode.setTraversed(false);
                        }
                    }
                }

            }
            triggerNode.setTraversed(false);
        }
        ruleNode.setTraversed(false);
    }

    ////找某个时间设备某个状态发生的原因,定位rule。List<String[]> deviceStateCausingRules第一项存设备状态以及状态值信息，后面存规则和触发时间
    public static List<String[]> getStateCausingRules(String deviceInstanceName,int stateValue,double time,DeviceInstance deviceInstance,HashMap<String,Rule> ruleHashMap,List<DataTimeValue> dataTimeValues){
        List<String[]> deviceStateCausingRules=new ArrayList<>();
        String[] deviceStateNameValue=new String[3];   ///记录设备名、状态、状态对应取值
        deviceStateNameValue[0]=deviceInstanceName; ///设备名
        deviceStateNameValue[2]=stateValue+""; ///设备状态取值
        deviceStateCausingRules.add(deviceStateNameValue);  ///第一项都是存设备信息
        ///先找到状态信息，状态名和状态对应的sync
        String sync="";
        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceInstance.getDeviceType().getStateSyncValueEffects()){
            if (Integer.parseInt(stateSyncValueEffect.getValue())==stateValue){
                sync=stateSyncValueEffect.getSynchronisation();
                deviceStateNameValue[1]=stateSyncValueEffect.getStateName();  ///状态名
                break;
            }
        }
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.getDataName().indexOf("rule")>=0){
                ///是规则相关仿真路径
                Rule rule=ruleHashMap.get(dataTimeValue.getDataName());  ///获得相应的规则信息
                ///判断该规则是否能导致相关state
                if (isRuleRelatedToInstanceSynchronisation(rule,deviceInstanceName,sync)){
                    ///看能否被触发
                    double triggeredTime=canRuleBeTriggeredInDuration(dataTimeValue,time,time);
                    if (triggeredTime>0.0){
                        ///可以被触发
                        String[] ruleTriggeredTime=new String[2];  //规则和触发时间
                        ruleTriggeredTime[0]=dataTimeValue.getDataName(); ///规则名
                        ruleTriggeredTime[1]=triggeredTime+"";  ///触发时间
                        deviceStateCausingRules.add(ruleTriggeredTime);
                    }
                }
            }
        }
        return deviceStateCausingRules;
    }
    ///看某个规则是否发送相应的sync，从而表明该规则是否能造成相应的状态
    public static boolean isRuleRelatedToInstanceSynchronisation(Rule rule,String deviceInstanceName,String synchronisation){
        for (String action: rule.getAction()){
            String[] instanceSync=action.split("\\.");
            if (instanceSync[0].trim().equals(deviceInstanceName)&&instanceSync[1].trim().equals(synchronisation)){
                return true;
            }
        }
        return false;
    }


    ////获得所有设备在所有场景下的冲突时状态触发原因
    public static HashMap<String,List<List<DeviceStateAndCausingRules>>> getDeviceConflictAllStatesRuleAndPreRulesHashMap(List<Rule> rules,String ifdFileName,List<Scenario> scenarios,List<DeviceInstance> deviceInstances){
        HashMap<String,Rule> ruleHashMap=getRuleHashMap(rules);
        List<IFDGraph.GraphNode> graphNodes=StaticAnalysisService.parseIFDAndGetIFDNode(AddressService.IFD_FILE_PATH,ifdFileName);
        HashMap<String, IFDGraph.GraphNode> graphNodeHashMap=getGraphNodeHashMap(graphNodes);
        List<RuleAndPreRule> allRulePreRules=AnalysisService.getAllRulePreRules(graphNodeHashMap,ruleHashMap);
        HashMap<String,RuleAndPreRule> ruleNameRuleAndPreRuleMap=getRuleAndPreRuleHashMap(allRulePreRules);
        HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=new HashMap<>();
        for (Scenario scenario:scenarios){
            HashMap<String,DataTimeValue> dataTimeValueHashMap=getDataTimeValueHashMap(scenario.getDataTimeValues());
            for (DeviceConflict deviceConflict:scenario.getDeviceConflicts()){
                if (deviceConflict.getConflictTimeValues().size()<=0){
                    continue;
                }
                for (DeviceInstance deviceInstance:deviceInstances){

                    if (deviceConflict.getInstanceName().equals(deviceInstance.getInstanceName())){

                        List<List<List<String[]>>> deviceAllConflictStatesDirectRules=AnalysisService.getDeviceConflictStatesDirectRulesInAScenario(deviceConflict,scenario.getDataTimeValues(),deviceInstance,ruleHashMap);
                        List<List<DeviceStateAndCausingRules>> deviceAllStatesRuleAndPreRules=AnalysisService.getDeviceAllStatesRuleAndPreRules(deviceAllConflictStatesDirectRules,dataTimeValueHashMap,ruleNameRuleAndPreRuleMap,ruleHashMap);
                        List<List<DeviceStateAndCausingRules>> existDeviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesHashMap.get(deviceInstance.getInstanceName());
                        if (existDeviceAllStatesRuleAndPreRules!=null){
                            existDeviceAllStatesRuleAndPreRules.addAll(deviceAllStatesRuleAndPreRules);
                        }else {
                            deviceAllStatesRuleAndPreRulesHashMap.put(deviceInstance.getInstanceName(), deviceAllStatesRuleAndPreRules);
                        }
                        break;
                    }
                }

            }
        }
        return deviceAllStatesRuleAndPreRulesHashMap;
    }

    ////获得所有设备在所有场景下的抖动时状态触发原因
    public static HashMap<String,List<List<DeviceStateAndCausingRules>>> getDeviceJitterAllStatesRuleAndPreRulesHashMap(List<Rule> rules,String ifdFileName,List<Scenario> scenarios,List<DeviceInstance> deviceInstances){
        HashMap<String,Rule> ruleHashMap=getRuleHashMap(rules);
        List<IFDGraph.GraphNode> graphNodes=StaticAnalysisService.parseIFDAndGetIFDNode(AddressService.IFD_FILE_PATH,ifdFileName);
        HashMap<String, IFDGraph.GraphNode> graphNodeHashMap=getGraphNodeHashMap(graphNodes);
        List<RuleAndPreRule> allRulePreRules=AnalysisService.getAllRulePreRules(graphNodeHashMap,ruleHashMap);
        HashMap<String,RuleAndPreRule> ruleNameRuleAndPreRuleMap=getRuleAndPreRuleHashMap(allRulePreRules);
        HashMap<String,List<List<DeviceStateAndCausingRules>>> deviceAllStatesRuleAndPreRulesHashMap=new HashMap<>();
        for (Scenario scenario:scenarios){
            HashMap<String,DataTimeValue> dataTimeValueHashMap=getDataTimeValueHashMap(scenario.getDataTimeValues());
            for (DeviceJitter deviceJitter: scenario.getDeviceJitters()){
                if (deviceJitter.getJitterTimeValues().size()<=0){
                    continue;
                }
                for (DeviceInstance deviceInstance:deviceInstances){
                    if (deviceJitter.getInstanceName().equals(deviceInstance.getInstanceName())){
                        List<List<List<String[]>>> deviceAllJitterStatesDirectRules=AnalysisService.getDeviceJitterStatesDirectRulesInAScenario(deviceJitter,scenario.getDataTimeValues(),deviceInstance,ruleHashMap);
                        ///转成DeviceStateAndCausingRules格式
                        List<List<DeviceStateAndCausingRules>> deviceAllJitterStatesRuleAndPreRules=AnalysisService.getDeviceAllStatesRuleAndPreRules(deviceAllJitterStatesDirectRules,dataTimeValueHashMap,ruleNameRuleAndPreRuleMap,ruleHashMap);
                        /////首先对每一段jitter都分别进行综合，获得一段抖动每个状态发生的原因
                        for (List<DeviceStateAndCausingRules> singleJitterStatesRuleAndPreRules:deviceAllJitterStatesRuleAndPreRules){
                            getDeviceSingleJitterStatesCausingRulesSynthesized(singleJitterStatesRuleAndPreRules);
                        }
                        List<List<DeviceStateAndCausingRules>> existDeviceAllStatesRuleAndPreRules=deviceAllStatesRuleAndPreRulesHashMap.get(deviceInstance.getInstanceName());
                        if (existDeviceAllStatesRuleAndPreRules!=null){
                            existDeviceAllStatesRuleAndPreRules.addAll(deviceAllJitterStatesRuleAndPreRules);
                        }else {
                            deviceAllStatesRuleAndPreRulesHashMap.put(deviceInstance.getInstanceName(), deviceAllJitterStatesRuleAndPreRules);
                        }

                    }
                }
            }

        }
        return deviceAllStatesRuleAndPreRulesHashMap;
    }

    public static HashMap<String,Rule> getRuleHashMap(List<Rule> rules){
        HashMap<String,Rule> ruleHashMap=new HashMap<>();
        for (Rule rule:rules){
            ruleHashMap.put(rule.getRuleName(),rule);
        }
        return ruleHashMap;
    }

    public static HashMap<String, IFDGraph.GraphNode> getGraphNodeHashMap(List<IFDGraph.GraphNode> graphNodes){
        HashMap<String, IFDGraph.GraphNode> graphNodeHashMap=new HashMap<>();
        for (IFDGraph.GraphNode graphNode:graphNodes){
            graphNodeHashMap.put(graphNode.getName(),graphNode);
        }
        return graphNodeHashMap;
    }

    public static HashMap<String,RuleAndPreRule> getRuleAndPreRuleHashMap(List<RuleAndPreRule> allRulePreRules){
        HashMap<String,RuleAndPreRule> ruleNameRuleAndPreRuleMap=new HashMap<>();
        for (RuleAndPreRule ruleAndPreRule:allRulePreRules){
            ruleNameRuleAndPreRuleMap.put(ruleAndPreRule.getCurrentRule().getRuleName(),ruleAndPreRule);
        }
        return ruleNameRuleAndPreRuleMap;
    }

    public static HashMap<String,DataTimeValue> getDataTimeValueHashMap(List<DataTimeValue> dataTimeValues){
        HashMap<String,DataTimeValue> dataTimeValueHashMap=new HashMap<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            dataTimeValueHashMap.put(dataTimeValue.getDataName(),dataTimeValue);
        }
        return dataTimeValueHashMap;
    }




    ////jitter定位找出一段jitter中，每次状态变化的原因

    ///验证所有设备，看是否存在状态冲突
    public static List<DeviceConflict> getDevicesConflict(List<DataTimeValue> dataTimeValues){
        List<DeviceConflict> deviceConflicts=new ArrayList<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.isDevice()){
                DeviceConflict deviceConflict=getDeviceConflict(dataTimeValue);
                deviceConflicts.add(deviceConflict);
            }
        }
        return deviceConflicts;
    }
    ///验证设备是否冲突
    public static DeviceConflict getDeviceConflict(DataTimeValue dataTimeValue){
        int size=dataTimeValue.getTimeValues().size();
        DeviceConflict deviceConflict=new DeviceConflict();
        deviceConflict.setInstanceName(dataTimeValue.getInstanceName());
        for (int i=0;i<size;i++){
            double currentTimeValue[]=dataTimeValue.getTimeValues().get(i);
            for (int j=i+1;j<size;j++){
                double nextTimeValue[]=dataTimeValue.getTimeValues().get(j);
                if ((currentTimeValue[0]+"").equals(nextTimeValue[0]+"")&&!(currentTimeValue[1]+"").equals(nextTimeValue[1]+"")){
                    ///如果时间相同，而取值不同，则说明冲突了
                    ///
                    if (j==i+1){
                        ///如果是第一个冲突状态，就创建一个新的冲突时间状态值
                        List<Double> conflictTimeValue=new ArrayList<>();
                        conflictTimeValue.add(currentTimeValue[0]);  ///添加时间
                        conflictTimeValue.add(currentTimeValue[1]);  ///添加当前状态取值
                        conflictTimeValue.add(nextTimeValue[1]);   ///添加下一个状态取值
                        deviceConflict.getConflictTimeValues().add(conflictTimeValue);
                    }else {
                        ///否则直接添加到最后一个冲突时间状态值上
                        int conflictSize=deviceConflict.getConflictTimeValues().size();
                        deviceConflict.getConflictTimeValues().get(conflictSize-1).add(nextTimeValue[1]);  ///添加下一个状态取值
                    }
                }else {
                    ///否则，从这个节点开始继续找
                    i=j-1;
                    break;
                }
            }
        }
        return deviceConflict;
    }


    ////获得所有设备的抖动分析结果
    public static List<DeviceJitter> getDevicesJitter(List<DataTimeValue> dataTimeValues,double interval){
        List<DeviceJitter> deviceJitters=new ArrayList<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.isDevice()){
                DeviceJitter deviceJitter=getDeviceJitter(dataTimeValue,interval);
                deviceJitters.add(deviceJitter);
            }
        }
        return deviceJitters;
    }
    ///验证设备是否抖动,interval是在该时间区间内如果设备状态不断切换则说明发生了抖动
    public static DeviceJitter getDeviceJitter(DataTimeValue dataTimeValue, double interval){
        DeviceJitter deviceJitter=new DeviceJitter();
        deviceJitter.setInstanceName(dataTimeValue.getInstanceName());
        List<double[]> timeValues=dataTimeValue.getTimeValues();
        int size=timeValues.size();
        List<List<double[]>> jitterTimeValues=new ArrayList<>();
        for (int i=0;i<size;i++){
            ///一个状态维持时间很短，马上就切换到下一个状态了
            double boundTime=interval+timeValues.get(i)[0];   ///如果状态转变时间在这个界限前，就说明该状态维持时间短
            for (int j=i+1;j<size;j++){
                ///看该状态持续时间多长
                if (!(timeValues.get(j)[1]+"").equals(timeValues.get(i)[1]+"")){
                    ///如果状态不同，则可能是状态发生变化了，如果该状态能维持一段时间，则说明确实是状态发生了变化
                    if (j+1<size){
                        ///存在下一个时间取值
                        if ((timeValues.get(j+1)[1]+"").equals(timeValues.get(j)[1]+"")){
                            ////该状态持续了一段时间
                            if (timeValues.get(j)[0]<boundTime){
                                ///在时间界限内,表明上一个状态维持时间较短

                                int jitterSize=jitterTimeValues.size();
                                if (timeValues.get(i)[0]>0){
                                    if (jitterSize>0){
                                        ///已经存在了jitter了
                                        int jitterTimeValueSize=jitterTimeValues.get(jitterSize-1).size();
                                        ///最后一个jitter时间状态值的长度
                                        if (jitterTimeValues.get(jitterSize-1).get(jitterTimeValueSize-1).equals(timeValues.get(i))){
                                            ///最后一个jitter的最后一个时间值和新jitter的第一个时间值相同，则可将新jitter添加到最后一个jitter上
                                            jitterTimeValues.get(jitterSize-1).add(timeValues.get(j));
                                        }else {
                                            ///否则是新的jitter
                                            List<double[]> newJitterTimeValue=new ArrayList<>();
                                            newJitterTimeValue.add(timeValues.get(i));
                                            newJitterTimeValue.add(timeValues.get(j));
                                            jitterTimeValues.add(newJitterTimeValue);
                                        }
                                    }else {
                                        ///否则是新的jitter
                                        List<double[]> newJitterTimeValue=new ArrayList<>();
                                        newJitterTimeValue.add(timeValues.get(i));
                                        newJitterTimeValue.add(timeValues.get(j));
                                        jitterTimeValues.add(newJitterTimeValue);
                                    }
                                }


                            }
                            i=j-1;

                            break;
                        }
                    }
                }

            }
        }

        deviceJitter.setJitterTimeValues(jitterTimeValues);
        return deviceJitter;
    }

    /**
     * 隐私性验证
     * 先生成每个可观察设备的隐私性性质组合
     * 然后分别进行验证，前提人有out状态
     *
     * */

    public static List<List<String>[]> privacyVerification(List<DeviceInstance> deviceInstances,HumanInstance humanInstance,List<Scenario> scenarios,InstanceLayer instanceLayer){
        ///先看human是否有out状态
//        List<String>[] homeBoundedOutBoundedResults=new List[2];
//        homeBoundedOutBoundedResults[0]=new ArrayList<>();   ///这些状态出现人必定在家
//        homeBoundedOutBoundedResults[1]=new ArrayList<>();   ///这些状态出现人必定不在家
        List<List<String>[]> homeBoundedOutBoundedResults=new ArrayList<>();

        String out="";
        for (String[] stateValue:humanInstance.getHuman().getStateValues()){
            if (stateValue[0].trim().equalsIgnoreCase("out")){
                ///有人out状态
                out=stateValue[0].trim();
                break;
            }
        }
        if (out.equals("")){
            ///没有out状态，则不考虑隐私性验证了
            return homeBoundedOutBoundedResults;
        }
        ///获得所有场景的dataTimeValue的map
        List<HashMap<String,DataTimeValue>> dataTimeValueHashMapList=new ArrayList<>();
        for (Scenario scenario:scenarios){
            //获得该场景的仿真轨迹map
            HashMap<String,DataTimeValue> dataTimeValueHashMap=new HashMap<>();
            for (DataTimeValue dataTimeValue:scenario.getDataTimeValues()){
                dataTimeValueHashMap.put(dataTimeValue.getDataName(), dataTimeValue);
            }
            dataTimeValueHashMapList.add(dataTimeValueHashMap);
        }
        for (DeviceInstance deviceInstance:deviceInstances){
            ///分别验证可观察设备的隐私性
            if (deviceInstance.isVisible()){
                //获得可被观察的设备的待验证隐私性性质
                List<String[]> privacyProperties=getDeviceInstancePrivacyProperties(deviceInstance,humanInstance,out);
                ///验证这组隐私性性质
                List<String>[] homeBoundedOutBoundedProperties=privacyVerificationForSingleDeviceInstance(privacyProperties,dataTimeValueHashMapList,instanceLayer);
                if (homeBoundedOutBoundedProperties[0].size()>0&&homeBoundedOutBoundedProperties[1].size()>0){
                    ///存在隐私性问题，因为能从一些状态推断当前人是否在家
                    homeBoundedOutBoundedResults.add(homeBoundedOutBoundedProperties);
                }
//                homeBoundedOutBoundedResults[0].addAll(homeBoundedOutBoundedProperties[0]);
//                homeBoundedOutBoundedResults[1].addAll(homeBoundedOutBoundedProperties[1]);

            }
        }
        return homeBoundedOutBoundedResults;
    }

    ///生成某个可被观察的设备的待验证隐私性性质
    public static List<String[]> getDeviceInstancePrivacyProperties(DeviceInstance deviceInstance,HumanInstance humanInstance,String out){
        List<String[]> privacyProperties=new ArrayList<>();
//        ///先看human是否有out状态
//        String out="";
//        for (String[] stateValue:humanInstance.getHuman().getStateValues()){
//            if (stateValue[0].trim().equalsIgnoreCase("out")){
//                ///有人out状态
//                out=stateValue[0].trim();
//                break;
//            }
//        }
//        if (out.equals("")){
//            ///没有out状态，则不考虑隐私性验证了
//            return privacyProperties;
//        }
        DeviceType deviceType=deviceInstance.getDeviceType();
        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
            String[] statePrivacyProperties=new String[2];
            //Human.home&device.s_i
            statePrivacyProperties[0]="!"+humanInstance.getInstanceName()+"."+out+"&"+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getStateName();
            //Human.out&device.s_i
            statePrivacyProperties[1]=humanInstance.getInstanceName()+"."+out+"&"+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getStateName();
            privacyProperties.add(statePrivacyProperties);
        }

        return privacyProperties;
    }

    ///然后验证这组隐私性性质，针对一个可观察设备的隐私性验证
    public static List<String>[] privacyVerificationForSingleDeviceInstance(List<String[]> privacyProperties,List<HashMap<String,DataTimeValue>> dataTimeValueHashMapList,InstanceLayer instanceLayer){
        ///看是否有一个设备状态是只跟!out一起出现的，而另一个设备状态只跟out一起出现
        List<String>[] homeBoundedOutBoundedProperties=new List[2];  //[0]存放设备状态是只跟!out一起出现的性质 [1]存放设备状态只跟out一起出现的性质
        homeBoundedOutBoundedProperties[0]=new ArrayList<>();
        homeBoundedOutBoundedProperties[1]=new ArrayList<>();
//        List<HashMap<String,DataTimeValue>> dataTimeValueHashMapList=new ArrayList<>();
//        for (Scenario scenario:scenarios){
//            //获得该场景的仿真轨迹map
//            HashMap<String,DataTimeValue> dataTimeValueHashMap=new HashMap<>();
//            for (DataTimeValue dataTimeValue:scenario.getDataTimeValues()){
//                dataTimeValueHashMap.put(dataTimeValue.getDataName(), dataTimeValue);
//            }
//            dataTimeValueHashMapList.add(dataTimeValueHashMap);
//        }
        for (String[] statePrivacyProperties:privacyProperties){
            ///分别进行验证
            boolean stateHomePropertyIsReachable=false;  ///该状态和!out是否能同时发生
            boolean stateOutPropertyIsReachable=false;   ///该状态和out是否能同时发生
            for (HashMap<String,DataTimeValue> dataTimeValueHashMap:dataTimeValueHashMapList){
                if (!stateHomePropertyIsReachable){ ///获得包括当前场景的对该状态与!out的性质的验证结果
                    ////获得单个场景每个property组成满足的时间段
                    List<PropertyElementCheckResult> propertyElementCheckResults=getPropertyConformTimeValues(dataTimeValueHashMap,statePrivacyProperties[0],instanceLayer);
                    ////获得单个场景下property中同时满足的时间段
                    List<double[]> durations=getConformTogetherDurations(propertyElementCheckResults);
                    if (durations.size()>0){
                        stateHomePropertyIsReachable=true;
                    }
                }
                if (!stateOutPropertyIsReachable){  ///获得包括当前场景的对该状态与out的性质的验证结果
                    ////获得单个场景每个property组成满足的时间段
                    List<PropertyElementCheckResult> propertyElementCheckResults=getPropertyConformTimeValues(dataTimeValueHashMap,statePrivacyProperties[1],instanceLayer);
                    ////获得单个场景下property中同时满足的时间段
                    List<double[]> durations=getConformTogetherDurations(propertyElementCheckResults);
                    if (durations.size()>0){
                        stateOutPropertyIsReachable=true;
                    }
                }
                if (stateOutPropertyIsReachable&&stateHomePropertyIsReachable){
                    break;
                }

            }
            if (stateHomePropertyIsReachable&&!stateOutPropertyIsReachable){
                ///该状态只与!out一起出现
                homeBoundedOutBoundedProperties[0].add(statePrivacyProperties[0]);
            }else if (!stateHomePropertyIsReachable&&stateOutPropertyIsReachable){
                //该状态只与out一起出现
                homeBoundedOutBoundedProperties[1].add(statePrivacyProperties[1]);
            }

//            PropertyAnalysisResult propertyAnalysisResult0=new PropertyAnalysisResult();
//            PropertyAnalysisResult propertyAnalysisResult1=new PropertyAnalysisResult();
//            for (HashMap<String,DataTimeValue> dataTimeValueHashMap:dataTimeValueHashMapList){
//                ///获得包括当前场景的对该状态与!out的性质的验证结果
//                propertyAnalysisResult0=setPropertyAnalysisResult(propertyAnalysisResult0,dataTimeValueHashMap,statePrivacyProperties[0],instanceLayer,rules);
//                ///获得包括当前场景的对该状态与out的性质的验证结果
//                propertyAnalysisResult1=setPropertyAnalysisResult(propertyAnalysisResult1,dataTimeValueHashMap,statePrivacyProperties[0],instanceLayer,rules);
//
//            }
//            ///看该状态与!out和out的性质都是否能满足
//            if (propertyAnalysisResult0.isReachable()&&!propertyAnalysisResult1.isReachable()){
//                ///该状态只与!out一起出现
//                homeBoundedOutBoundedProperties[0].add(propertyAnalysisResult0.getProperty());
//            }else if (!propertyAnalysisResult0.isReachable()&&propertyAnalysisResult1.isReachable()){
//                //该状态只与out一起出现
//                homeBoundedOutBoundedProperties[1].add(propertyAnalysisResult1.getProperty());
//            }

        }
        return homeBoundedOutBoundedProperties;
    }


    /**
     * /////特定于某个场景找原因，给建议,对于涉及设备状态的可以改变
    // attribute<(<=,>,>=)value,看有没有能改变attribute的规则
    ////instance.state，如果是设备的话，看有没有相关规则，
    ////attribute<(<=,>,>=)value & instance.state，
    ////
    */
    public static PropertyAnalysisResult getPropertyReachableReasonAndGiveAdvise(List<double[]> satDurations, HashMap<String,DataTimeValue> dataTimeValueHashMap,List<PropertyElementCheckResult> propertyElementCheckResults,InstanceLayer instanceLayer,List<Rule> rules){
        PropertyAnalysisResult propertyAnalysisResult=new PropertyAnalysisResult();
        if (satDurations.size()>0){
            ///有满足的情况
            boolean sat=false;
            for (double[] duration:satDurations){
                if ((duration[1]-duration[0])>1){
                    sat=true;
                    break;
                }
            }
            if (!sat){
                return propertyAnalysisResult;
            }
            propertyAnalysisResult.setReachable(true);
            List<Rule> relatedRules=new ArrayList<>();
            List<String> addRuleContent=new ArrayList<>();
            if(propertyElementCheckResults.size()==1){
                // attribute<(<=,>,>=)value,看有没有能改变attribute的规则
                ////instance.state，如果是设备的话，看有没有相关规则，
                String[] elementForm=propertyElementCheckResults.get(0).getElementForm();
                ///先找到会影响相应属性的设备
                List<DeviceInstance> relatedDeviceInstances=getRelatedDeviceInstances(propertyElementCheckResults.get(0),instanceLayer.getDeviceInstances());
                if (!elementForm[1].equals(".")){
                    ////然后找到对应状态
                    ////可以添加规则开启设备来改属性
                    // attribute<(<=,>,>=)value
                    for (DeviceInstance deviceInstance:relatedDeviceInstances){
                        DeviceType deviceType= deviceInstance.getDeviceType();
                        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
                            for (String[] effect:stateSyncValueEffect.getEffects()){
                                if (effect[0].equals(elementForm[0])){
                                    if (elementForm[1].contains(">")&&Double.parseDouble(effect[1])<0||elementForm[1].contains("<")&&Double.parseDouble(effect[1])>0){
                                        ////添加规则
                                        String ruleContent="IF "+elementForm[0]+elementForm[1]+elementForm[2]+" THEN "+deviceInstance.getInstanceName()+"."+stateSyncValueEffect.getSynchronisation();
                                        addRuleContent.add(ruleContent);
                                    }
                                }
                            }
                        }
                    }

                }else {
                    ////对于设备
                    if (elementForm[0].equals(relatedDeviceInstances.get(0).getInstanceName())){
                        ////是某个设备状态
                        ////看有什么规则让它转变为这个状态了，如果有指出该规则，顺便告知添加什么规则
                        String stateValue="0";
                        String sync="";
                        List<String> otherSyncs=new ArrayList<>();
                        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:relatedDeviceInstances.get(0).getDeviceType().getStateSyncValueEffects()){
                            if (stateSyncValueEffect.getStateName().equals(elementForm[2])){
                                stateValue=stateSyncValueEffect.getValue();
                                sync=stateSyncValueEffect.getSynchronisation();

                            }else {
                                otherSyncs.add(stateSyncValueEffect.getSynchronisation());
                            }
                        }
//                        for (double[][] timeValue: propertyElementCheckResults.get(0).getSatTimeValues()){
//                            List<String[]> stateDirectRules=getStateCausingRules(relatedDeviceInstances.get(0).getInstanceName(),Integer.parseInt(stateValue),
//                                    timeValue[0][0],relatedDeviceInstances.get(0),ruleHashMap,dataTimeValues);
//                        }
                        ///看该场景中相关设备状态规则是否会发生

                        for (Rule rule:rules){
                            if (isRuleRelatedToInstanceSynchronisation(rule,relatedDeviceInstances.get(0).getInstanceName(),sync)){
                                DataTimeValue dataTimeValue=dataTimeValueHashMap.get(rule.getRuleName());
                                double triggeredTime=canRuleBeTriggeredInDuration(dataTimeValue,0,dataTimeValue.getTimeValues().get(dataTimeValue.getTimeValues().size()-1)[0]);
                                if (triggeredTime>0){
                                    relatedRules.add(rule);
                                }
                            }

                        }
                        ///添加规则
                        for (String otherSync:otherSyncs){
                            ////添加规则
                            String ruleContent="IF "+elementForm[0]+elementForm[1]+elementForm[2]+" THEN "+relatedDeviceInstances.get(0).getInstanceName()+"."+otherSync;
                            addRuleContent.add(ruleContent);
                        }
                    }
                }
            }else if (propertyElementCheckResults.size()>1){
                for (PropertyElementCheckResult propertyElementCheckResult1:propertyElementCheckResults){
                    if (propertyElementCheckResult1.getElementForm()[1].equals(".")){
                        List<DeviceInstance> relatedDeviceInstances=getRelatedDeviceInstances(propertyElementCheckResult1,instanceLayer.getDeviceInstances());
                        if (relatedDeviceInstances.size()>0){
                            ////是设备状态
                            String sync="";
                            List<String> otherSyncs=new ArrayList<>();
                            for (DeviceType.StateSyncValueEffect stateSyncValueEffect:relatedDeviceInstances.get(0).getDeviceType().getStateSyncValueEffects()){
                                if (stateSyncValueEffect.getStateName().equals(propertyElementCheckResult1.getElementForm()[2])){
                                    sync= stateSyncValueEffect.getSynchronisation();
                                }else {
                                    otherSyncs.add(stateSyncValueEffect.getSynchronisation());
                                }
                            }
                            for (Rule rule:rules){
                                if (isRuleRelatedToInstanceSynchronisation(rule,relatedDeviceInstances.get(0).getInstanceName(),sync)){
                                    DataTimeValue dataTimeValue=dataTimeValueHashMap.get(rule.getRuleName());
                                    double triggeredTime=canRuleBeTriggeredInDuration(dataTimeValue,0,dataTimeValue.getTimeValues().get(dataTimeValue.getTimeValues().size()-1)[0]);
                                    if (triggeredTime>0){
                                        relatedRules.add(rule);
                                    }
                                }

                            }
                            for (PropertyElementCheckResult propertyElementCheckResult2:propertyElementCheckResults){
                                if (!propertyElementCheckResult2.equals(propertyElementCheckResult1)){
                                    ///添加规则
                                    String[] elementForm2=propertyElementCheckResult2.getElementForm();
                                    for (String otherSync:otherSyncs){
                                        ////添加规则
                                        String ruleContent="IF "+elementForm2[0]+elementForm2[1]+elementForm2[2]+" THEN "+relatedDeviceInstances.get(0).getInstanceName()+"."+otherSync;
                                        addRuleContent.add(ruleContent);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            propertyAnalysisResult.setRelatedRules(relatedRules);
            propertyAnalysisResult.setAddRuleContents(addRuleContent);
        }
        return propertyAnalysisResult;
    }

    /**
     * 获得相关设备实例
     * */
    public static List<DeviceInstance> getRelatedDeviceInstances(PropertyElementCheckResult propertyElementCheckResult,List<DeviceInstance> deviceInstances){
        List<DeviceInstance> relatedDeviceInstances=new ArrayList<>();
        if (!propertyElementCheckResult.getElementForm()[1].equals(".")){
            // attribute<(<=,>,>=)value,看有没有能改变attribute的规则
            ////instance.state，如果是设备的话，看有没有相关规则，
            ///先找到会影响相应属性的设备
            for (DeviceInstance deviceInstance:deviceInstances){
                DeviceType deviceType=deviceInstance.getDeviceType();
                boolean hasEffect=false;
                first:
                for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
                    for (String[] effect:stateSyncValueEffect.getEffects()){
                        if (effect[0].equals(propertyElementCheckResult.getElementForm()[0])){
                            hasEffect=true;
                            break first;
                        }
                    }
                }
                if (hasEffect){
                    relatedDeviceInstances.add(deviceInstance);
                }
            }
        }else {
            ////设备状态相关
            for (DeviceInstance deviceInstance:deviceInstances){
                if (deviceInstance.getInstanceName().equals(propertyElementCheckResult.getInstanceName())){
                    relatedDeviceInstances.add(deviceInstance);
                    break;
                }
            }
        }
        return relatedDeviceInstances;
    }

    public static PropertyAnalysisResult setPropertyAnalysisResult(PropertyAnalysisResult propertyAnalysisResult,HashMap<String,DataTimeValue> dataTimeValueHashMap,String property,InstanceLayer instanceLayer,List<Rule> rules){
        ////获得单个场景每个property组成满足的时间段
        List<PropertyElementCheckResult> propertyElementCheckResults=getPropertyConformTimeValues(dataTimeValueHashMap,property,instanceLayer);
        ////获得单个场景下property中同时满足的时间段
        List<double[]> durations=getConformTogetherDurations(propertyElementCheckResults);

        ///在当前场景下property是否可达，不可达给出原因和建议
        PropertyAnalysisResult newPropertyAnalysisResult=getPropertyReachableReasonAndGiveAdvise(durations,dataTimeValueHashMap,propertyElementCheckResults,instanceLayer,rules);
        if (propertyAnalysisResult==null||!propertyAnalysisResult.isReachable()){
            //添加验证结果
            newPropertyAnalysisResult.setProperty(property);
            return newPropertyAnalysisResult;
        }else if (newPropertyAnalysisResult.getRelatedRules().size()>0||newPropertyAnalysisResult.getAddRuleContents().size()>0){
            //添加新的相关规则
            for (Rule newRule:newPropertyAnalysisResult.getRelatedRules()){
//                        boolean exist=false;
//                        for (Rule rule:propertyAnalysisResult.getRelatedRules()){
//                            if (rule.getRuleName().equals(newRule.getRuleName())){
//                                exist=true;
//                                break;
//                            }
//                        }
                if (!propertyAnalysisResult.getRelatedRules().contains(newRule)){
                    //添加建议的规则
                    propertyAnalysisResult.getRelatedRules().add(newRule);
                }
            }
            for (String newRuleContent: newPropertyAnalysisResult.getAddRuleContents()){
                //添加新的建议的相关规则
                if (!propertyAnalysisResult.getAddRuleContents().contains(newRuleContent)){
                    propertyAnalysisResult.getAddRuleContents().add(newRuleContent);
                }
            }

        }
        return propertyAnalysisResult;
    }

    ////获得最终结果
    public static List<PropertyAnalysisResult> getPropertiesAnalysisResultAllScenarios(List<Scenario> scenarios,InstanceLayer instanceLayer,List<Rule> rules,List<String> properties){
        List<PropertyAnalysisResult> propertyAnalysisResults=new ArrayList<>();
        HashMap<String,PropertyAnalysisResult> propertyAnalysisResultHashMap=new HashMap<>();
        for (Scenario scenario:scenarios){
            //获得该场景的仿真轨迹map
            HashMap<String,DataTimeValue> dataTimeValueHashMap=new HashMap<>();
            for (DataTimeValue dataTimeValue:scenario.getDataTimeValues()){
                dataTimeValueHashMap.put(dataTimeValue.getDataName(), dataTimeValue);
            }
            for (String property:properties){
                ///获得该property在前面几个场景验证过的结果
                PropertyAnalysisResult propertyAnalysisResult=propertyAnalysisResultHashMap.get(property);
                ///获得包括当前场景的对property的验证结果
                PropertyAnalysisResult newProperAnalysisResult=setPropertyAnalysisResult(propertyAnalysisResult,dataTimeValueHashMap,property,instanceLayer,rules);
                propertyAnalysisResultHashMap.put(property,newProperAnalysisResult);
//                ////获得单个场景每个property组成满足的时间段
//                List<PropertyElementCheckResult> propertyElementCheckResults=getPropertyConformTimeValues(dataTimeValueHashMap,property,instanceLayer);
//                ////获得单个场景下property中同时满足的时间段
//                List<double[]> durations=getConformTogetherDurations(propertyElementCheckResults);
//                ///获得该property在前面几个场景验证过的结果
//                PropertyAnalysisResult propertyAnalysisResult=propertyAnalysisResultHashMap.get(property);
//                ///在当前场景下property是否可达，不可达给出原因和建议
//                PropertyAnalysisResult newPropertyAnalysisResult=getPropertyReachableReasonAndGiveAdvise(durations,dataTimeValueHashMap,propertyElementCheckResults,instanceLayer,rules);
//                if (propertyAnalysisResult==null||!propertyAnalysisResult.isReachable()){
//                    //添加验证结果
//                    newPropertyAnalysisResult.setProperty(property);
//                    propertyAnalysisResultHashMap.put(property,newPropertyAnalysisResult);
//                }else if (newPropertyAnalysisResult.getRelatedRules().size()>0||newPropertyAnalysisResult.getAddRuleContents().size()>0){
//                    //添加新的相关规则
//                    for (Rule newRule:newPropertyAnalysisResult.getRelatedRules()){
////                        boolean exist=false;
////                        for (Rule rule:propertyAnalysisResult.getRelatedRules()){
////                            if (rule.getRuleName().equals(newRule.getRuleName())){
////                                exist=true;
////                                break;
////                            }
////                        }
//                        if (!propertyAnalysisResult.getRelatedRules().contains(newRule)){
//                            //添加建议的规则
//                            propertyAnalysisResult.getRelatedRules().add(newRule);
//                        }
//                    }
//                    for (String newRuleContent: newPropertyAnalysisResult.getAddRuleContents()){
//                        //添加新的建议的相关规则
//                        if (!propertyAnalysisResult.getAddRuleContents().contains(newRuleContent)){
//                            propertyAnalysisResult.getAddRuleContents().add(newRuleContent);
//                        }
//                    }
//                }


            }
        }
        for (Map.Entry<String,PropertyAnalysisResult> propertyAnalysisResultEntry:propertyAnalysisResultHashMap.entrySet()){
            propertyAnalysisResults.add(propertyAnalysisResultEntry.getValue());
        }
        return propertyAnalysisResults;
    }

    ////获得单个场景下一条property所有element同时满足的时间段区间，即单场景下property可达的时间段
    public static void getPropertyConformDurations(String simulationTime,HashMap<String,DataTimeValue> dataTimeValueHashMap,String property,InstanceLayer instanceLayer,List<Rule> rules){
        ////获得单个场景每个property组成满足的时间段
        List<PropertyElementCheckResult> propertyElementCheckResults=getPropertyConformTimeValues(dataTimeValueHashMap,property,instanceLayer);
        ////获得单个场景下property中同时满足的时间段
        List<double[]> durations=getConformTogetherDurations(propertyElementCheckResults);
        PropertyAnalysisResult propertyAnalysisResult=getPropertyReachableReasonAndGiveAdvise(durations,dataTimeValueHashMap,propertyElementCheckResults,instanceLayer,rules);
        return;
    }

    ////单个场景下分别获得所有element满足的时间段区间
    public static List<PropertyElementCheckResult> getPropertyConformTimeValues(HashMap<String,DataTimeValue> dataTimeValueHashMap,String property,InstanceLayer instanceLayer){
        String[] propertyElements=property.split("&");
        List<PropertyElementCheckResult> propertyElementCheckResults=new ArrayList<>();
        for (String propertyElement:propertyElements){
            propertyElement=propertyElement.trim();
            boolean neg=false;
            if (propertyElement.startsWith("!")) {
                neg=true;
                propertyElement=propertyElement.substring(1);
            }
            String[] propertyElementForm=RuleService.getTriggerForm(propertyElement);///property的格式与trigger有异曲同工之处，可以使用该方法解析propertyElement
            DataTimeValue relatedDataTimeValue=null;
            Instance relatedInstance=null;
            if (propertyElementForm[1].equals(".")){
                for (Map.Entry<String,DataTimeValue> dataTimeValueEntry:dataTimeValueHashMap.entrySet()){
                    DataTimeValue dataTimeValue=dataTimeValueEntry.getValue();
                    if (dataTimeValue.getInstanceName().equals(propertyElementForm[0])){
                        relatedDataTimeValue=dataTimeValue;
                        break;
                    }
                }
                if (instanceLayer.getHumanInstance().getInstanceName().equals(propertyElementForm[0])){
                    relatedInstance=instanceLayer.getHumanInstance();
                }else {
                    boolean findInstance=false;
                    for (UncertainEntityInstance uncertainEntityInstance:instanceLayer.getUncertainEntityInstances()){
                        if (uncertainEntityInstance.getInstanceName().equals(propertyElementForm[0])){
                            relatedInstance=uncertainEntityInstance;
                            findInstance=true;
                            break;
                        }
                    }
                    if (!findInstance){
                        for (DeviceInstance deviceInstance: instanceLayer.getDeviceInstances()){
                            if (deviceInstance.getInstanceName().equals(propertyElementForm[0])){
                                relatedInstance=deviceInstance;
                                break;
                            }
                        }
                    }
                }
            }else {
                for (Map.Entry<String,DataTimeValue> dataTimeValueEntry:dataTimeValueHashMap.entrySet()){
                    DataTimeValue dataTimeValue=dataTimeValueEntry.getValue();
                    if (dataTimeValue.getDataName().equals(propertyElementForm[0])){
                        relatedDataTimeValue=dataTimeValue;
                        break;
                    }
                }
            }
            if (relatedDataTimeValue!=null){
                /////对该element的检测结果
                PropertyElementCheckResult propertyElementCheckResult=getPropertyElementConformTimeValues(relatedDataTimeValue,propertyElementForm,relatedInstance,neg);
                propertyElementCheckResult.setElementContent(propertyElement);
                propertyElementCheckResult.setElementForm(propertyElementForm);
                propertyElementCheckResult.setNeg(neg);
                System.out.println(propertyElementCheckResult);
                propertyElementCheckResults.add(propertyElementCheckResult);
            }

        }
        return propertyElementCheckResults;
    }
    ////寻找单场景下n个property elements同时满足的时间段
    public static List<double[]> getConformTogetherDurations(List<PropertyElementCheckResult> propertyElementCheckResults){
        List<double[]> satDurations=new ArrayList<>();
        for (int i=0;i<propertyElementCheckResults.get(0).getSatTimeValues().size();i++){
            double[] satDuration=new double[2];  ///一个时间段
            satDuration[0]=propertyElementCheckResults.get(0).getSatTimeValues().get(i)[0][0];    ///开始时间
            satDuration[1]=propertyElementCheckResults.get(0).getSatTimeValues().get(i)[1][0];    ///结束时间
            satDurations.add(satDuration);
        }
        for (int i=1;i<propertyElementCheckResults.size();i++){
            ////返回满足的区间
            satDurations=getIntersection(satDurations,propertyElementCheckResults.get(i).getSatTimeValues());
        }
        for (int i=satDurations.size()-1;i>=0;i--){
            double[] satDuration=satDurations.get(i);
            if ((satDuration[1]-satDuration[0])<=1){
                satDurations.remove(i);
            }
        }
        return satDurations;
    }
    ////获取时间段交集
    public static List<double[]> getIntersection(List<double[]> durations1,List<double[][]> satTimeValues){
        List<double[]> newDurations=new ArrayList<>();
        for (int i=0,j=0;i<durations1.size()&&j<satTimeValues.size();){
            ///[[s11,f11],[s12,f12],[s13,f13]]  [[s21,f21],[s22,f22],[s23,f23]]
            if (durations1.get(i)[0]>=satTimeValues.get(j)[1][0]){
                ///1时间段的开始比2时间段的结束更晚
                j++;
            }else if (durations1.get(i)[1]<=satTimeValues.get(j)[0][0]){
                ///1时间段的结束比2时间段的开始更早
                i++;
            }else if (durations1.get(i)[1]>=satTimeValues.get(j)[0][0]&&durations1.get(i)[1]<=satTimeValues.get(j)[1][0]){
                ///1时间段的结束在2时间段的开始和结束之间
                double[] newDuration=new double[2];
                newDuration[0]=Math.max(durations1.get(i)[0],satTimeValues.get(j)[0][0]);  ///开始时间
                newDuration[1]=durations1.get(i)[1];   ///结束时间
                newDurations.add(newDuration);
                i++;
            }else if (satTimeValues.get(j)[1][0]>=durations1.get(i)[0]&&satTimeValues.get(j)[1][0]<=durations1.get(i)[1]){
                ///2时间段的结束时间在1时间段的开始和结束之间
                double[] newDuration=new double[2];
                newDuration[0]=Math.max(durations1.get(i)[0],satTimeValues.get(j)[0][0]);  ///开始时间
                newDuration[1]=satTimeValues.get(j)[1][0];   ///结束时间
                newDurations.add(newDuration);
                j++;
            }
        }
        return newDurations;
    }
    ////自定义性质的验证，[!]attribute<(<=,>,>=)value, [!]instance.state
    public static PropertyElementCheckResult getPropertyElementConformTimeValues(DataTimeValue dataTimeValue,String[] propertyElementForm,Instance instance,boolean neg){
        PropertyElementCheckResult propertyElementCheckResult=new PropertyElementCheckResult();
        propertyElementCheckResult.setDataName(dataTimeValue.getDataName());
        propertyElementCheckResult.setInstanceName(dataTimeValue.getInstanceName());
        if (propertyElementForm[1].equals(".")&&instance!=null){
            ///实体状态，找到对应状态的取值, instance.state
            int value=getInstanceStateValue(instance,propertyElementForm[2]);
            List<double[]> timeValues=dataTimeValue.getTimeValues();
            int size=timeValues.size();
            for (int i=0;i<size;i++){
                int currentValue=(int) timeValues.get(i)[1];
                if (neg?currentValue!=value:currentValue==value){
                    ///状态开始时间和结束时间
                    ///timeValues.get(i)[0]开始时间
                    if (i<size-1){
                        int nextValue=(int) timeValues.get(i+1)[1];
                        if (neg?nextValue!=value:nextValue==value){
                            //////timeValues.get(i)[0]结束时间
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),timeValues.get(i),timeValues.get(i+1));
                        }
                    }
                }
            }
        }else{
            List<double[]> timeValues=dataTimeValue.getTimeValues();
            int size=timeValues.size();
            for (int i=0;i<size-1;i++){
                double[] currentTimeValue=timeValues.get(i); ///左边时间取值
                double[] nextTimeValue=timeValues.get(i+1);  ///右边时间取值
                double[] boundTimeValue=new double[2];  ///边界时间取值
                boundTimeValue[1]=Double.parseDouble(propertyElementForm[2]);  ///边界点值
                ///计算斜率等,计算边界值  v=k*t+b
                if (!(currentTimeValue[1]+"").equals(nextTimeValue[1]+"")){
                    double k=(currentTimeValue[1]-nextTimeValue[1])/(currentTimeValue[0]-nextTimeValue[0]);
                    double b=currentTimeValue[1]-k*currentTimeValue[0];
                    boundTimeValue[0]=(boundTimeValue[1]-b)/k;   ///计算边界点时间
                }
                if (propertyElementForm[1].indexOf(">")>=0){
                    ///对于property是 > value
                    if ((currentTimeValue[1]+"").equals(nextTimeValue[1]+"")){
                        ///两个取值相同
                        if (neg?currentTimeValue[1]<boundTimeValue[1]:currentTimeValue[1]>boundTimeValue[1]){
                            ///取反则看当前值是否小于边界值，否则看当前值是否大于边界值，如是，则整个区间满足
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,nextTimeValue);
                        }
                    }else if (currentTimeValue[1]>nextTimeValue[1]){
                        ///左边相对于右边更能满足
                        ///如果左边的值大于右边的值
                        if (boundTimeValue[0]>=currentTimeValue[0]&&boundTimeValue[0]<=nextTimeValue[0]){
                            ///如果边界时间在区间之间
                            if (neg){
                                ///取反，则添加边界右边的区间，即右边取值小于边界值
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),boundTimeValue,nextTimeValue);
                            }else {
                                ///不取反，则添加边界左边的区间，即左边取值大于边界值
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,boundTimeValue);
                            }
                        }else if (neg?boundTimeValue[0]<=currentTimeValue[0]:boundTimeValue[0]>=nextTimeValue[0]){
                            ///如果边界时间不在区间内，
                            ///取反，且边界时间在左边时间的左边，则说明整个区间都满足，即区间内所有取值小于边界值
                            ///不取反，且边界时间在右边时间的右边，则说明整个区间满足，即区间内所有值大于边界值
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,nextTimeValue);
                        }
                    }else if (currentTimeValue[1]<nextTimeValue[1]){
                        ///右边相对于左边更能满足
                        ///如果右边的值大于左边的值
                        if (boundTimeValue[0]>=currentTimeValue[0]&&boundTimeValue[0]<=nextTimeValue[0]){
                            ///如果边界点在区间内
                            if (neg){
                                ///取反，则添加边界左边区间，即右边取值小于边界值
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,boundTimeValue);
                            }else {
                                ///不取反，则添加边界右边区间，即左边取值大于边界值
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),boundTimeValue,nextTimeValue);
                            }

                        }else if (neg?boundTimeValue[0]>=nextTimeValue[0]:boundTimeValue[0]<=currentTimeValue[0]){
                            ///如果边界时间不在区间内，
                            ///取反，且边界时间在右边时间的右边，则说明整个区间满足，即区间内所有取值小于边界值
                            ///不取反，且边界时间在左边时间的左边，则说明整个区间都满足，即区间内所有值大于边界值
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,nextTimeValue);
                        }
                    }
                }else if (propertyElementForm[1].indexOf("<")>=0){
                    ///对于property是 < value
                    if ((currentTimeValue[1]+"").equals(nextTimeValue[1]+"")){
                        ///两个取值相同
                        if (neg?currentTimeValue[1]>boundTimeValue[1]:currentTimeValue[1]<boundTimeValue[1]){
                            ///取反则看当前值是否大于边界值，否则看当前值是否小于边界值，如是，则整个区间满足
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,nextTimeValue);
                        }
                    }else if (currentTimeValue[1]>nextTimeValue[1]){
                        ///右边相对于左边更能满足
                        ///如果左边的值大于右边的值
                        if (boundTimeValue[0]>=currentTimeValue[0]&&boundTimeValue[0]<=nextTimeValue[0]){
                            ///如果边界时间在区间之间
                            if (neg){
                                ///取反，则添加边界左边的区间
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,boundTimeValue);
                            }else {
                                ///不取反，则添加边界右边的区间
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),boundTimeValue,nextTimeValue);
                            }
                        }else if (neg?boundTimeValue[0]>=nextTimeValue[0]:boundTimeValue[0]<=currentTimeValue[0]){
                            ///如果边界时间不在区间内，
                            ///取反，且边界时间在右边时间的右边，则说明整个区间满足，即所有值都大于边界值
                            ///不取反，且边界时间在左边时间的左边，则说明整个区间都满足，即所有值都小于边界值
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,nextTimeValue);
                        }
                    }else if (currentTimeValue[1]<nextTimeValue[1]){
                        ///左边相对于右边更能满足
                        ///如果左边的值小于右边的值
                        if (boundTimeValue[0]>=currentTimeValue[0]&&boundTimeValue[0]<=nextTimeValue[0]){
                            ///如果边界时间在区间之间
                            if (neg){
                                ///取反，则添加边界右边的区间，即右边取值大于边界值
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),boundTimeValue,nextTimeValue);
                            }else {
                                ///不取反，则添加边界左边的区间，即左边取值小于边界值
                                addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,boundTimeValue);
                            }
                        }else if (neg?boundTimeValue[0]<=currentTimeValue[0]:boundTimeValue[0]>=nextTimeValue[0]){
                            ///如果边界时间不在区间内，
                            ///取反，且边界时间在左边时间的左边，则说明整个区间都满足，即区间内所有取值大于边界值
                            ///不取反，且边界时间在右边时间的右边，则说明整个区间满足，即区间内所有值小于边界值
                            addSatTimeValue(propertyElementCheckResult.getSatTimeValues(),currentTimeValue,nextTimeValue);
                        }
                    }
                }

            }

        }
        return propertyElementCheckResult;
    }
    ///添加满足的区间，区间左边和区间右边
    public static void addSatTimeValue(List<double[][]> satTimeValues,double[] currentTimeValue,double[] nextTimeValue){
        if (currentTimeValue[0]>=nextTimeValue[0]) return;  ////currentTime不能大于nextTime
        int satTimeValueSize=satTimeValues.size();
        if (satTimeValueSize>0){
            ///看开始时间能不能衔接上最后一个结束时间，如果能就直接将结束时间改为当前结束时间，否则创建新的时间段
            double[] finalTimeValue=satTimeValues.get(satTimeValueSize-1)[1];
            if ((currentTimeValue[0]+"").equals(finalTimeValue[0]+"")||(currentTimeValue[0]-finalTimeValue[0])<0.1&&(currentTimeValue[0]-finalTimeValue[0])>0){
                ///能衔接上最后一个结束时间
                finalTimeValue[0]=nextTimeValue[0];
                finalTimeValue[1]=nextTimeValue[1];
            }else{
                addNewSatTimeValue(satTimeValues,currentTimeValue,nextTimeValue);
            }
        }else {
            addNewSatTimeValue(satTimeValues,currentTimeValue,nextTimeValue);
        }
    }
    ///添加新的满足的时间段
    public static void addNewSatTimeValue(List<double[][]> satTimeValues,double[] currentTimeValue,double[] nextTimeValue){
        double[][] satTimeValue=new double[2][2];
        satTimeValue[0][0]=currentTimeValue[0];
        satTimeValue[0][1]=currentTimeValue[1];
        satTimeValue[1][0]=nextTimeValue[0];
        satTimeValue[1][1]=nextTimeValue[1];
        satTimeValues.add(satTimeValue);
    }
    ///获得实例某状态取值
    public static int getInstanceStateValue(Instance instance,String state){
        ///实体状态，找到对应状态的取值, instance.state
        int value=-1;
        if (instance instanceof DeviceInstance){
            DeviceInstance deviceInstance=(DeviceInstance) instance;
            for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceInstance.getDeviceType().getStateSyncValueEffects()){
                if (stateSyncValueEffect.getStateName().equals(state)){
                    value=Integer.parseInt(stateSyncValueEffect.getValue());
                    break;
                }
            }
        }else if (instance instanceof HumanInstance){
            HumanInstance humanInstance=(HumanInstance) instance;
            //////stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值
            for (String[] stateValue:humanInstance.getHuman().getStateValues()){
                if (stateValue[0].equals(state)){
                    value=Integer.parseInt(stateValue[2]);
                    break;
                }
            }
        }else if (instance instanceof UncertainEntityInstance){
            UncertainEntityInstance uncertainEntityInstance=(UncertainEntityInstance) instance;
            //////stateValue[0]=状态名,stateValue[1]=状态id,stateValue[2]=状态标识符取值
            for (String[] stateValue:uncertainEntityInstance.getUncertainEntityType().getStateValues()){
                if (stateValue[0].equals(state)){
                    value=Integer.parseInt(stateValue[2]);
                    break;
                }
            }
        }
        return value;
    }


    ///获得在所有场景下都无法触发的规则
    public static List<String> getNotTriggeredRulesInAll(List<List<String>> notTriggeredRulesOfDifferentScenarios){
        List<String> notTriggeredRulesInAll=new ArrayList<>();
        if (notTriggeredRulesOfDifferentScenarios.size()>0){
            notTriggeredRulesInAll=notTriggeredRulesOfDifferentScenarios.get(0);
        }
        for (int i=1;i<notTriggeredRulesOfDifferentScenarios.size();i++){
            if (notTriggeredRulesInAll.size()==0){
                break;
            }
            notTriggeredRulesInAll=getNotTriggeredRulesSynthesize(notTriggeredRulesInAll,notTriggeredRulesOfDifferentScenarios.get(i));
        }
        return notTriggeredRulesInAll;
    }
    ///综合两个场景下不能触发的规则
    public static List<String> getNotTriggeredRulesSynthesize(List<String> notTriggeredRules1,List<String> notTriggeredRules2){
        for (int i=notTriggeredRules1.size()-1;i>=0;i--){
            boolean alsoNotTriggered=false;
            ///在场景2下同样无法触发，则保留，在场景2下能被触发则删除
            for (String notTriggeredRule:notTriggeredRules2){
                if (notTriggeredRule.equals(notTriggeredRules1.get(i))){
                    alsoNotTriggered=true;
                    break;
                }
            }
            if (!alsoNotTriggered){
                notTriggeredRules1.remove(i);
            }
        }
        return notTriggeredRules1;
    }
    ///找到该场景下无法触发的规则
    public static List<String> getNotTriggeredRulesInAScenario(List<DataTimeValue> dataTimeValues){
        List<String> notTriggeredRules=new ArrayList<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.getDataName().indexOf("rule")>=0){
                if (dataTimeValue.getTimeValues().size()<=2){
                    ///size大于2则说明能被触发
                    notTriggeredRules.add(dataTimeValue.getDataName());
                }
            }
        }
        return notTriggeredRules;
    }


    ///找到不完整性，也就是该一个设备被打开后，无法被关闭，不再被关闭
    ///获得所有场景下都打开后无法关闭的设备（可以有场景无法打开）
    public static List<String> getDeviceCannotBeTurnedOffListInAll(List<List<String[]>> deviceCannotBeTurnedOffOrOnListOfDifferentScenarios){
        List<String> deviceCannotBeTurnedOffList=new ArrayList<>();
        List<String[]> deviceCannotBeTurnedOffOrOnList=new ArrayList<>();
        if (deviceCannotBeTurnedOffOrOnListOfDifferentScenarios.size()>0){
            deviceCannotBeTurnedOffOrOnList=deviceCannotBeTurnedOffOrOnListOfDifferentScenarios.get(0);
        }
        for (int i=1;i<deviceCannotBeTurnedOffOrOnListOfDifferentScenarios.size();i++){
            if (deviceCannotBeTurnedOffOrOnList.size()==0){
                break;
            }
            deviceCannotBeTurnedOffOrOnList=getDeviceCannotBeTurnedOffOrOnListSynthesize(deviceCannotBeTurnedOffOrOnList,deviceCannotBeTurnedOffOrOnListOfDifferentScenarios.get(i));
        }
        for (String[] deviceCannotBeTurnedOffOrOn:deviceCannotBeTurnedOffOrOnList){
            ///确定确实是无法被关闭的设备
            if (deviceCannotBeTurnedOffOrOn[1].equals("notOff")){
                deviceCannotBeTurnedOffList.add(deviceCannotBeTurnedOffOrOn[0]);
            }
        }
        return deviceCannotBeTurnedOffList;
    }
    ///综合两个场景下打开后无法关闭的设备
    public static List<String[]> getDeviceCannotBeTurnedOffOrOnListSynthesize(List<String[]> deviceCannotBeTurnedOffOrOnList1,List<String[]> deviceCannotBeTurnedOffOrOnList2){
        for (int i=deviceCannotBeTurnedOffOrOnList1.size()-1;i>=0;i--){
            String[] deviceCannotBeTurnedOffOrOn1=deviceCannotBeTurnedOffOrOnList1.get(i);
            boolean exist=false;
            boolean notOff=false;
            for (String[] deviceCannotBeTurnedOffOrOn2:deviceCannotBeTurnedOffOrOnList2){
                if (deviceCannotBeTurnedOffOrOn2[0].equals(deviceCannotBeTurnedOffOrOn1[0])){
                    exist=true;  ///存在该设备
                    if (deviceCannotBeTurnedOffOrOn2[1].equals("notOff")){
                        notOff=true;   ///该设备无法关
                    }
                    break;
                }
            }
            if (!exist){
                deviceCannotBeTurnedOffOrOnList1.remove(i);
                continue;
            }
            if (notOff){
                deviceCannotBeTurnedOffOrOn1[1]="notOff";
            }
        }
        return deviceCannotBeTurnedOffOrOnList1;
    }
    ///找到某个场景下被打开后就不再被关闭的的设备，如果整个过程没有被打开，也添加进来用String数组存储，[0]=deviceName,[1]="notOff"/"notOn"
    public static List<String[]> getDeviceCannotBeTurnedOffOrOnListInAScenario(List<DataTimeValue> dataTimeValues){
        List<String[]> deviceCannotBeTurnedOffOrOnList=new ArrayList<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.isDevice()){
                ///开始两个timeValue必然是value为0.0的,因此不用看
                if (dataTimeValue.getTimeValues().size()>2){
                    ////打开了，且后面有数据为0则表明是能关闭，而如果没有则表明不能关闭
                    boolean canBeOff=false;
                    for (int i=2;i<dataTimeValue.getTimeValues().size();i++){
                        if ((int) dataTimeValue.getTimeValues().get(i)[1]==0){
                            canBeOff=true;
                            break;
                        }
                    }
                    if(!canBeOff){
                        String[] deviceNotOff=new String[2];
                        deviceNotOff[0]=dataTimeValue.getInstanceName();
                        deviceNotOff[1]="notOff";
                        deviceCannotBeTurnedOffOrOnList.add(deviceNotOff);
                    }

                }else {
                    ///如果设备一直没打开
                    String[] deviceNotOn=new String[2];
                    deviceNotOn[0]=dataTimeValue.getInstanceName();
                    deviceNotOn[1]="notOn";
                    deviceCannotBeTurnedOffOrOnList.add(deviceNotOn);
                }
            }
        }
        return deviceCannotBeTurnedOffOrOnList;
    }



    ///计算满意度
    public static double getSatisfaction(String attribute, double lowValue, double highValue, List<DataTimeValue> dataTimeValues){
        double satisfaction=0.0;
        for (DataTimeValue dataTimeValue:dataTimeValues){
            double satisfyTime=0.0;
            if (dataTimeValue.getDataName().equals(attribute)){
                ////找到attribute对应的仿真路径
                int size=dataTimeValue.getTimeValues().size();
                  for (int i=0;i<size;i++){
                    if (i>0){
                        double[] currentTimeValue=dataTimeValue.getTimeValues().get(i);
                        double[] lastTimeValue=dataTimeValue.getTimeValues().get(i-1);
                        if ((currentTimeValue[0]+"").equals(lastTimeValue[0]+"")) continue;
                        ////累加满足的时间
                        satisfyTime+=getSatInterval(lowValue,highValue,currentTimeValue,lastTimeValue);
                    }
                }
                ///计算满意度，满足的时间/总的仿真时间
                satisfaction=satisfyTime/dataTimeValue.getTimeValues().get(size-1)[0];
                break;
            }
        }
        return satisfaction;
    }
    ////计算当前时间值和上一个时间值之间取值满足特定取值区间的持续时间。作为时间区间，currentTimeValue为区间右边界，lastTimeValue为区间左边界
    public static double getSatInterval(double lowValue,double highValue,double[] currentTimeValue,double[] lastTimeValue){
        double interval=0.0;
        double currentValue=currentTimeValue[1];    ///右边界取值
        double lastValue=lastTimeValue[1];   ///左边界取值
        if ((currentValue+"").equals(lastValue+"")){
            ///两个取值相同
            if (currentValue>=lowValue && currentValue<=highValue){
                ///取值在要求的区间内
                interval+=currentTimeValue[0]-lastTimeValue[0];
            }
//        }else if (currentValue<lastValue){
//            ///右边界取值低于左边界取值
//            if (currentValue>=highValue||lastValue<=lowValue){
//                ///右边界高于最高值或左边界低于最低值，则整个区间都不满足
//            }else if (currentValue>=lowValue&&lastValue<=highValue){
//                ///整个区间都满足
//                interval+=currentTimeValue[0]-lastTimeValue[0];
//            }else {
//                ///计算斜率等,计算两个边界值
//                double k=(currentValue-lastValue)/(currentTimeValue[0]-lastTimeValue[0]);
//                double b=currentValue-k*currentTimeValue[0];
//                double leftTime=(highValue-b)/k;
//                double rightTime=(lowValue-b)/k;
//                interval=Math.min(rightTime,currentTimeValue[0])-Math.max(leftTime,lastTimeValue[0]);
//            }
//        }else if (currentValue>lastValue){
//            ///左边界取值低于右边界取值
//            if (lastValue>=highValue||currentValue<=lowValue){
//                ///右边界高于最高值或左边界低于最低值，则整个区间都不满足
//            }else if (lastValue>=lowValue&&currentValue<=highValue){
//                ///整个区间都满足
//                interval+=currentTimeValue[0]-lastTimeValue[0];
//            }else {
//                ///计算斜率等,计算两个边界值
//                double k=(currentValue-lastValue)/(currentTimeValue[0]-lastTimeValue[0]);
//                double b=currentValue-k*currentTimeValue[0];
//                double rightTime=(highValue-b)/k;
//                double leftTime=(lowValue-b)/k;
//                interval=Math.min(rightTime,currentTimeValue[0])-Math.max(leftTime,lastTimeValue[0]);
//            }
        }else {
            if (currentValue>highValue&&lastValue>highValue||currentValue<lowValue&&lastValue<lowValue){
                ///不满足
            }else if (currentValue<=highValue&&currentValue>=lowValue&&lastValue<=highValue&&lastValue>=lowValue){
                ///整个区间都满足
                interval+=currentTimeValue[0]-lastTimeValue[0];
            }else {
                ///计算斜率等,计算两个边界值  v=k*t+b
                double k=(currentValue-lastValue)/(currentTimeValue[0]-lastTimeValue[0]);
                double b=currentValue-k*currentTimeValue[0];
                double highTime=(highValue-b)/k;
                double lowTime=(lowValue-b)/k;
                double leftTime=Math.min(lowTime,highTime);
                double rightTime=Math.max(lowTime,highTime);
                interval=Math.min(rightTime,currentTimeValue[0])-Math.max(leftTime,lastTimeValue[0]);
            }
        }
        return interval;
    }


    ///计算所有设备各状态的持续时间，可用于计算能耗
    public static List<List<String[]>> getDeviceStatesDurationList(List<DataTimeValue> dataTimeValues, InstanceLayer instanceLayer){
        List<List<String[]>> deviceStatesDurationList=new ArrayList<>();
        for (DataTimeValue dataTimeValue:dataTimeValues){
            if (dataTimeValue.isDevice()){
                for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
                    if (deviceInstance.getInstanceName().equals(dataTimeValue.getInstanceName())){
                        List<String[]> deviceStatesDuration=getDeviceStatesDuration(dataTimeValue,deviceInstance);
                        deviceStatesDurationList.add(deviceStatesDuration);
                        break;
                    }
                }
            }
        }
        return deviceStatesDurationList;
    }
    ///计算耗能。即计算各设备各状态的时间  deviceStateDuration[0]设备名  deviceStateDuration[1]状态名  deviceStateDuration[2]该状态保持时间 [4]用来存功率
    public static List<String[]> getDeviceStatesDuration(DataTimeValue deviceDataTimeValue,DeviceInstance deviceInstance){
        DeviceType deviceType=deviceInstance.getDeviceType();
        List<String[]> deviceStatesDuration=new ArrayList<>();
        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
            String[] deviceStateDuration=new String[4];
            deviceStateDuration[0]=deviceInstance.getInstanceName();    ///设备名
            deviceStateDuration[1]=stateSyncValueEffect.getStateName();   ///状态名
            double duration=getCertainStateDuration(deviceDataTimeValue,stateSyncValueEffect.getValue());
            deviceStateDuration[2]=duration+"";
            deviceStateDuration[3]="0";
            deviceStatesDuration.add(deviceStateDuration);   ////该状态保持时间
        }
        return deviceStatesDuration;
    }
    ///计算某状态在整个仿真过程中的保持时间
    public static double getCertainStateDuration(DataTimeValue deviceDataTimeValue,String stateValue){
        double duration=0.0;
        int value=Integer.parseInt(stateValue);
        int size=deviceDataTimeValue.getTimeValues().size();
        for (int i=0;i<size;i++){
            double timeValue[]=deviceDataTimeValue.getTimeValues().get(i);
            ///如果当前的值和value相等，就看上一个值是否同样相等，如果相等，则添加二者之间的时间间隔。
            if (i>0 && (int) timeValue[1]==value){
                ////看上一个值是否同样相等
                double lastTimeValue[]=deviceDataTimeValue.getTimeValues().get(i-1);
                if ((int) lastTimeValue[1]==value){
                    ///如果相等，则添加二者之间的时间间隔
                    duration+=timeValue[0]-lastTimeValue[0];
                }
            }
        }
        return duration;
    }

}
