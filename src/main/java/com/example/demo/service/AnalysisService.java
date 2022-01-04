package com.example.demo.service;

import com.example.demo.bean.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 对仿真数据进行分析，冲突、抖动、自定义性质、以及其他分析【满意度、能耗等】
 * 定位错误原因
 * */
public class AnalysisService {
    ///定位找原因

    ///综合IFD寻找更深层次的原因
    ///综合所有原因
    ///根据IFD，往前回溯找其他引发该规则的规则，当当前规则涉及设备状态时
    public static void getRulePreRule(IFDGraph.GraphNode ruleNode,RuleAndPreRule currentRule,HashMap<String,Rule> ruleHashMap, double triggeredTime, HashMap<String,DataTimeValue> dataTimeValueHashMap){
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
                        if (!actionNode.isTraversed()){
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
                                        getRulePreRule(otherRuleNode,preRule,ruleHashMap,otherTriggeredTime,dataTimeValueHashMap);
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    ///找到所有设备的冲突情况
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
    ///冲突定位找原因，每次冲突都定位找原因,找到在那段时间触发的规则及触发时间，同时该规则要与该设备相关
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
        for (int i=0;i<dataTimeValue.getTimeValues().size();i+=2){
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


    ////获得一条property所有element同时满足的时间段区间
    public static List<double[]> getPropertyConformDurations(List<DataTimeValue> dataTimeValues,String property,InstanceLayer instanceLayer){
        List<PropertyElementCheckResult> propertyElementCheckResults=getPropertyConformTimeValues(dataTimeValues,property,instanceLayer);
        List<double[]> durations=getConformTogetherDurations(propertyElementCheckResults);
        return durations;
    }
    ////分别获得所有element满足的时间段区间
    public static List<PropertyElementCheckResult> getPropertyConformTimeValues(List<DataTimeValue> dataTimeValues,String property,InstanceLayer instanceLayer){
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
                for (DataTimeValue dataTimeValue:dataTimeValues){
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
                for (DataTimeValue dataTimeValue:dataTimeValues){
                    if (dataTimeValue.getDataName().equals(propertyElementForm[0])){
                        relatedDataTimeValue=dataTimeValue;
                        break;
                    }
                }
            }
            if (relatedDataTimeValue!=null){
                PropertyElementCheckResult propertyElementCheckResult=getPropertyElementConformTimeValues(relatedDataTimeValue,propertyElementForm,relatedInstance,neg);
                propertyElementCheckResult.setElementContent(propertyElement);
                propertyElementCheckResult.setNeg(neg);
                System.out.println(propertyElementCheckResult);
                propertyElementCheckResults.add(propertyElementCheckResult);
            }

        }
        return propertyElementCheckResults;
    }
    ////寻找n个property elements同时满足的时间段
    public static List<double[]> getConformTogetherDurations(List<PropertyElementCheckResult> propertyElementCheckResults){
        List<double[]> satDurations=new ArrayList<>();
        for (int i=0;i<propertyElementCheckResults.get(0).getSatTimeValues().size();i++){
            double[] satDuration=new double[2];  ///一个时间段
            satDuration[0]=propertyElementCheckResults.get(0).getSatTimeValues().get(i)[0][0];    ///开始时间
            satDuration[1]=propertyElementCheckResults.get(0).getSatTimeValues().get(i)[1][0];    ///结束时间
            satDurations.add(satDuration);
        }
        for (int i=1;i<propertyElementCheckResults.size();i++){
            satDurations=getIntersection(satDurations,propertyElementCheckResults.get(i).getSatTimeValues());
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
        if (currentTimeValue[0]>=nextTimeValue[0]) return;
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
        int value=0;
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
                    String[] deviceNotOff=new String[2];
                    deviceNotOff[0]=dataTimeValue.getInstanceName();
                    deviceNotOff[1]="notOff";
                    deviceCannotBeTurnedOffOrOnList.add(deviceNotOff);
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
                        ////累加满足的时间
                        satisfyTime+=getSatInterval(lowValue,highValue,dataTimeValue.getTimeValues().get(i),dataTimeValue.getTimeValues().get(i-1));
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
    ///计算耗能。即计算各设备各状态的时间  deviceStateDuration[0]设备名  deviceStateDuration[1]状态名  deviceStateDuration[2]该状态保持时间
    public static List<String[]> getDeviceStatesDuration(DataTimeValue deviceDataTimeValue,DeviceInstance deviceInstance){
        DeviceType deviceType=deviceInstance.getDeviceType();
        List<String[]> deviceStatesDuration=new ArrayList<>();
        for (DeviceType.StateSyncValueEffect stateSyncValueEffect:deviceType.getStateSyncValueEffects()){
            String[] deviceStateDuration=new String[3];
            deviceStateDuration[0]=deviceInstance.getInstanceName();    ///设备名
            deviceStateDuration[1]=stateSyncValueEffect.getStateName();   ///状态名
            double duration=getCertainStateDuration(deviceDataTimeValue,stateSyncValueEffect.getValue());
            deviceStateDuration[2]=duration+"";
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
