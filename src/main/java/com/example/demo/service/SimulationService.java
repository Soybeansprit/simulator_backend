package com.example.demo.service;

import com.example.demo.bean.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 仿真后获得仿真路径，对仿真路径进行解析
 * 1.进行仿真
 * 2.获得仿真数据
 * */
@Service
public class SimulationService {
    public static void main(String[] args) {
        String result=getSimulationResult(AddressService.UPPAAL_PATH,"D:\\example\\例子\\test\\","changed-ontology-0.xml","windows");
        System.out.println(result);

    }

    ////获取仿真结果并解析
    public static List<DataTimeValue> parseSimulationResult(String simulationResult, InstanceLayer instanceLayer,String resultFilePath,String resultFileName){
        /**直接获得的仿真结果数据如下所示，对其进行解析
         * coppm:
         * [0]: (0,70) (0,70) (300.0099999998721,70)
         * bulb[0]:
         * [0]: (0,0) (0,0) (1,0) (1,0) (1.07,1) (1.07,1) (300.0099999998721,1)
         * blind[0]:
         * [0]: (0,0) (0,0) (30,0) (30.00000000000184,0) (30.07000000000185,1) (30.07000000000185,1) (300.0099999998721,1)
         * */
        ////先删除  : [0]
        simulationResult=simulationResult.replace(":\n[0]","");
        ////解析仿真结果
        ////先拆分出每个数据的仿真路径
        String[] traces=simulationResult.split("\n");
        ////针对每个数据的仿真路径进行解析
        List<DataTimeValue> dataTimeValues=new ArrayList<>();
        for (String trace:traces){
            if (trace.equals("")){
                continue;
            }
            DataTimeValue dataTimeValue=parseSimulationTrace(trace,instanceLayer);
            dataTimeValues.add(dataTimeValue);
            System.out.println(dataTimeValue);
        }

        if (!(resultFilePath.equals("")||resultFileName.equals(""))){
            ////把仿真结果写入到文档中
            generateSimulationResultFile(resultFilePath,resultFileName,simulationResult,instanceLayer);
        }
        return dataTimeValues;
    }

    ////解析某一数据的仿真路径
    public static DataTimeValue parseSimulationTrace(String trace,InstanceLayer instanceLayer){
        DataTimeValue dataTimeValue=new DataTimeValue();
        ///trace:  window[0]: (0,0) (0,0) (300.0099999998757,0)
        int index=trace.indexOf(":");
        String dataName=trace.substring(0,index);
        dataTimeValue.setDataName(dataName);   ///数据名：window[0]
        ////寻找对应的实例
        if (instanceLayer.getHumanInstance().getHuman().getIdentifier().equals(dataName)){
            dataTimeValue.setInstanceName(instanceLayer.getHumanInstance().getInstanceName());
        }
        for (AttributeEntityType.Attribute attribute:instanceLayer.getAttributeEntityInstance().getAttributeEntityType().getAttributes()){
            if (attribute.getAttribute().equals(dataName)){
                dataTimeValue.setInstanceName(instanceLayer.getAttributeEntityInstance().getInstanceName());
                break;
            }
        }
        for (UncertainEntityInstance uncertainEntityInstance:instanceLayer.getUncertainEntityInstances()){
            if (uncertainEntityInstance.getUncertainEntityType().getIdentifier().equals(dataName)){
                dataTimeValue.setInstanceName(uncertainEntityInstance.getInstanceName());
                break;
            }
        }
        for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
            String identifier=deviceInstance.getDeviceType().getIdentifier()+"["+deviceInstance.getSequenceNumber()+"]";
            if (identifier.equals(dataName)){
                dataTimeValue.setInstanceName(deviceInstance.getInstanceName());
                dataTimeValue.setDevice(true);   ///设备
                break;
            }
        }

        int rIndex=trace.lastIndexOf(")");
        trace=trace.substring(index+": (".length(),rIndex);    ///trace：0,0) (0,0) (300.0099999998757,0
        String[] timeValueStrs=trace.split("\\) \\(");   ///按 ) ( 切割
        for (int i=0;i<timeValueStrs.length;i++){
            if (i>0){
                if (timeValueStrs[i].equals(timeValueStrs[i-1])){
                    ///和前一个数据相同则跳过，去重
                    continue;
                }
            }
            ////0,0，划分成 0 0
            String[] timeValueSplit=timeValueStrs[i].split(",");
            double[] timeValue=new double[2];
            timeValue[0]=Double.parseDouble(timeValueSplit[0]);
            timeValue[1]=Double.parseDouble(timeValueSplit[1]);
            int size=dataTimeValue.getTimeValues().size();
            if (size>=2){
                String currentValue=timeValue[1]+"";
                String lastValue=dataTimeValue.getTimeValues().get(size-1)[1]+"";
                String lastLastValue=dataTimeValue.getTimeValues().get(size-2)[1]+"";
                if ((currentValue).equals(lastValue)&&(currentValue).equals(lastLastValue)){
                    ///如果当前的取值和上一个取值以及上上个取值相同，就将上一个timeValue删除
                    dataTimeValue.getTimeValues().remove(size-1);
                }
            }
            dataTimeValue.getTimeValues().add(timeValue);
        }
        return dataTimeValue;
    }

    ////把仿真结果写入到文档中
    public static void generateSimulationResultFile(String resultFilePath,String resultFileName,String simulationResult,InstanceLayer instanceLayer){
        for (DeviceInstance deviceInstance:instanceLayer.getDeviceInstances()){
            ////再将设备状态标识符对应为相应的设备实例信息【实例名、所属设备类型、所处空间位置】
            ////设备状态标识符
            String identifier=deviceInstance.getDeviceType().getIdentifier()+"["+deviceInstance.getSequenceNumber()+"]";
            ////设备实例信息【实例名、所属设备类型、所处空间位置】
            StringBuilder deviceInformationSb=new StringBuilder();
            deviceInformationSb.append("deviceName=");
            deviceInformationSb.append(deviceInstance.getInstanceName());
            deviceInformationSb.append(",deviceType=");
            deviceInformationSb.append(deviceInstance.getDeviceType().getTypeName());
            deviceInformationSb.append(",location=");
            deviceInformationSb.append(deviceInstance.getLocation());
            simulationResult=simulationResult.replace(identifier,deviceInformationSb.toString());
        }
		try (FileWriter fr=new FileWriter(resultFilePath+resultFileName);
             PrintWriter pw=new PrintWriter(fr)){
			pw.write(simulationResult);
		}catch(IOException e) {
			e.printStackTrace();
		}
    }

    ////获得仿真结果
    public static String getSimulationResult(String uppaalPath,String filePath,String fileName,String system) {
        InputStream error = null;
        try {
//		  		System.out.println(command.toString());
            Process process = Runtime.getRuntime().exec(system.equalsIgnoreCase("windows")?getCMDCommand(uppaalPath,filePath,fileName):getLinuxCommand(uppaalPath, filePath, fileName));
            error = process.getErrorStream();
//		  		long startTime0=System.currentTimeMillis();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
//		  		long endTime0=System.currentTimeMillis();
//	  			System.out.println("simulationData time: "+(endTime0-startTime0));
            StringBuffer resultBuffer = new StringBuffer();
            String s = "";
            s = bufferedReader.readLine();
            if(s==null) {
                System.out.println("null");
            }
            while (s != null) {
                if(s.indexOf("[error]")>=0){
                    ///如果模型有误直接返回null
                    return null;
                }
//		  			System.out.println(s);
                resultBuffer.append(s+"\n");
//		  			long startTime=System.currentTimeMillis();
                s = bufferedReader.readLine();
//		  			long endTime=System.currentTimeMillis();
//		  			System.out.println("readline time: "+(endTime-startTime));
//		  			if(s!=null) {
//		  				System.out.println(s.toString());
//		  			}

            }
            bufferedReader.close();
            process.waitFor();
            String result=resultBuffer.toString();
            int formulaIsSatisfiedIndex=result.indexOf("Formula is satisfied.");
            if(formulaIsSatisfiedIndex>=0) {
                result=result.substring(formulaIsSatisfiedIndex).substring("Formula is satisfied.".length());
            }
            return result;
        } catch (Exception ex) {
            if (error != null) {
                try {
                    error.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return ex.getMessage();
        }
    }

    ////如果是window系统，用cmd方法
    public static String getCMDCommand(String uppaalPath,String filePath,String fileName) {
        StringBuffer command = new StringBuffer();
        command.append("cmd /c d: ");
        //这里的&&在多条语句的情况下使用，表示等上一条语句执行成功后在执行下一条命令，
        //也可以使用&表示执行上一条后台就立刻执行下一条语句
        command.append(String.format(" && cd %s", uppaalPath));
        command.append(" && verifyta.exe -O std "+filePath+fileName);
        return command.toString();
    }

    ////如果是Linux系统，用该方法
    public static String getLinuxCommand(String uppaalPath,String filePath,String fileName) {
        StringBuffer command = new StringBuffer();
        //这里的&&在多条语句的情况下使用，表示等上一条语句执行成功后在执行下一条命令，
        //也可以使用&表示执行上一条后台就立刻执行下一条语句
        command.append(String.format("%s", uppaalPath));
        command.append("./verifyta -O std "+filePath+fileName);
        return command.toString();
    }

}
