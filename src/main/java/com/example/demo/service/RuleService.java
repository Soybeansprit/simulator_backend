package com.example.demo.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.bean.Rule;

@Service
public class RuleService {
	
	public static void main(String[] args) throws IOException {
		RuleService ruleService=new RuleService();
		String ruleStrList="1. IF SmartHomeSecurity.homeMode AND temperature<=15 THEN Heater.turn_heat_on\r\n" + 
				"\r\n" + 
				"2. IF SmartHomeSecurity.homeMode AND temperature>=30 THEN AirConditioner.turn_ac_cool\r\n" + 
				"\r\n" + 
				"3. IF SmartHomeSecurity.homeMode AND humidity<20 THEN Humidifier.turn_hum_on\r\n" + 
				"\r\n" + 
				"4. IF SmartHomeSecurity.homeMode AND humidity>=45 THEN Humidifier.turn_hum_off\r\n" + 
				"\r\n" + 
				"5. IF SmartHomeSecurity.homeMode AND humidity>65 THEN Fan.turn_fan_on\r\n" + 
				"\r\n" + 
				"6. IF SmartHomeSecurity.homeMode AND temperature>28 THEN Fan.turn_fan_on\r\n" + 
				"\r\n" + 
				"7. IF SmartHomeSecurity.homeMode AND temperature<20 THEN Fan.turn_fan_off\r\n" + 
				"\r\n" + 
				"8. IF SmartHomeSecurity.homeMode AND rain=1 THEN PhilipsHueLight.turn_phl_white\r\n" + 
				"\r\n" + 
				"9. IF SmartHomeSecurity.homeMode AND temperature<=10 THEN PhilipsHueLight.turn_phl_blue\r\n" + 
				"\r\n" + 
				"10. IF SmartHomeSecurity.homeMode AND leak=1 THEN PhilipsHueLight.turn_phl_blue\r\n" + 
				"\r\n" + 
				"11. IF SmartHomeSecurity.awayMode THEN PhilipsHueLight.turn_phl_off\r\n" + 
				"\r\n" + 
				"12. IF SmartHomeSecurity.homeMode THEN PhilipsHueLight.turn_phl_white\r\n" + 
				"\r\n" + 
				"13. IF Door.dopen THEN PhilipsHueLight.turn_phl_white\r\n" + 
				"\r\n" + 
				"14. IF SmartHomeSecurity.homeMode AND co2ppm>=1000 THEN PhilipsHueLight.turn_phl_red\r\n" + 
				"\r\n" + 
				"15. IF SmartHomeSecurity.awayMode THEN Fan.turn_fan_on\r\n" + 
				"\r\n" + 
				"16. IF Door.dopen THEN Heater.turn_heat_off\r\n" + 
				"\r\n" + 
				"17. IF Window.wopen THEN Heater.turn_heat_off\r\n" + 
				"\r\n" + 
				"18. IF SmartHomeSecurity.awayMode THEN Heater.turn_heat_off,AirConditioner.turn_ac_off,Fan.turn_fan_off,Blind.close_blind,Bulb.turn_bulb_off\r\n" + 
				"\r\n" + 
				"19. IF SmartHomeSecurity.homeMode AND temperature<18 THEN AirConditioner.turn_ac_heat\r\n" + 
				"\r\n" + 
				"20. IF SmartHomeSecurity.homeMode AND temperature>30 THEN AirConditioner.turn_ac_cool\r\n" + 
				"\r\n" + 
				"21. IF SmartHomeSecurity.homeMode THEN Robot.dock_robot\r\n" + 
				"\r\n" + 
				"22. IF SmartHomeSecurity.awayMode THEN Robot.start_robot\r\n" + 
				"\r\n" + 
				"23. IF SmartHomeSecurity.awayMode THEN Window.close_window,Door.close_door\r\n" + 
				"\r\n" + 
				"24. IF number>0 THEN SmartHomeSecurity.turn_sms_home\r\n" + 
				"\r\n" + 
				"25. IF number=0 THEN SmartHomeSecurity.turn_sms_away\r\n" + 
				"\r\n" + 
				"26. IF SmartHomeSecurity.homeMode AND temperature>28 THEN Blind.open_blind\r\n" + 
				"\r\n" + 
				"27. IF SmartHomeSecurity.homeMode THEN Bulb.turn_on_bulb\r\n" + 
				"\r\n" + 
				"28. IF SmartHomeSecurity.homeMode AND co2ppm>=800 THEN Fan.turn_on_fan,Window.open_window\r\n" + 
				"\r\n" + 
				"29. IF AirConditioner.cool THEN Window.close_window\r\n" + 
				"\r\n" + 
				"30. IF AirConditioner.heat THEN Window.close_window";
		List<Rule> rules=getRuleList(ruleStrList);
		System.out.println(rules);
		
		String rulePath="D:\\rules0105new.txt";
		List<Rule> txtRules=ruleService.getRuleListFromTxt(rulePath);
		System.out.println(txtRules);
	}

	//////////////////////////////解析rule//////////////////////////////
	public static Rule getRule(String ruleStr,int num) {
		Rule rule=null;
		if(ruleStr.endsWith("\r\n")) {
			ruleStr=ruleStr.substring(0, ruleStr.indexOf("\r\n"));
		}
		if(ruleStr.endsWith("\n")) {
			ruleStr=ruleStr.substring(0, ruleStr.indexOf("\n"));
		}
		ruleStr=ruleStr.trim();
		if(ruleStr!=null && ruleStr.toUpperCase().indexOf("IF ")>=0 &&
				ruleStr.toUpperCase().indexOf(" THEN ")>0) {
			rule=new Rule();
			int ifIndex=ruleStr.toUpperCase().indexOf("IF ");    //////IF 的下标
			int thenIndex=ruleStr.toUpperCase().indexOf(" THEN ");     //////THEN 的下标
			
			if(ifIndex>=thenIndex) {
				System.out.println(ruleStr+"\n    not rule!");
				rule=null;
			}else {				
				rule.setRuleName("rule"+num);   ///////规则的序号 rule1
				rule.setRuleContent(ruleStr.substring(ruleStr.toUpperCase().indexOf("IF ")));   /////规则内容  IF...THEN...
				String triggerStr=ruleStr.substring(ifIndex, thenIndex).substring("IF ".length()).trim();  ////trigger字符串串
				String actionStr=ruleStr.substring(thenIndex).substring(" THEN ".length()).trim();    /////action字符串
				List<String> triggers=Arrays.asList(triggerStr.split("AND"));   ////trigger列表
				List<String> actions=Arrays.asList(actionStr.split(","));       /////action列表
				for(int i=0;i<triggers.size();i++) {
					String[] triggerForm=getTriggerForm(triggers.get(i));
					StringBuilder triggerSb=new StringBuilder();
					triggerSb.append(triggerForm[0]);
					triggerSb.append(triggerForm[1]);
					triggerSb.append(triggerForm[2]);
					String trigger=triggerSb.toString();
					triggers.set(i, trigger);					
				}
				rule.setTrigger(triggers);
				for(int i=0;i<actions.size();i++) {
					String[] instanceSync=actions.get(i).split("\\.");
					StringBuilder actionSb=new StringBuilder();
					actionSb.append(instanceSync[0].trim());
					actionSb.append(".");
					actionSb.append(instanceSync[1].trim());
					String action=actionSb.toString();
					actions.set(i, action);
				}
				rule.setAction(actions);				
			}			
		}
		return rule;
	}
	

	/////////////////解析所有规则
	public static List<Rule> getRuleList(List<String> ruleTextLines){
		List<Rule> rules=new ArrayList<Rule>();
		List<String> ruleStrList=new ArrayList<String>();;
		for(String ruleTextLine:ruleTextLines) {
			////////找到含IF...THEN...的行
			if(ruleTextLine!=null && ruleTextLine.toUpperCase().indexOf("IF ")>=0 &&
					ruleTextLine.toUpperCase().indexOf(" THEN ")>0) {
				ruleTextLine=ruleTextLine.substring(ruleTextLine.toUpperCase().indexOf("IF "));
				ruleStrList.add(ruleTextLine);
			}
		}
		for(int i=0;i<ruleStrList.size();i++) {
			String ruleStr=ruleStrList.get(i);
			////////解析IF...THEN... 获得Rule结构的rule
			Rule rule=getRule(ruleStr, i+1);
			if(rule!=null) {
				rules.add(rule);
			}			
		}
		return rules;
	}
	
//	public static List<Action> getAllActions(List<Rule> rules,List<DeviceDetail> devices){
//		List<Action> actions=new ArrayList<Action>();  ///存所有actions
//		for(Rule rule:rules) {
//			for(String action:rule.getAction()) {  //////遍历一条规则的所有actions
//				boolean exist=false;
//				for(Action act:actions) {
//					if(act.action.equals(action)) {  ////看是否已经存在于 actions 中
//						exist=true;
//						break;
//					}
//				}
//				if(!exist) {          ////不存在就添加
//					Action act=new Action();
//					act.action=action;       /////action内容  Bulb_0.turn_bulb_on
//					act.actionNum="action"+actions.size();     ///action序号（用于ifd中）action0
//					if(action.indexOf(".")>0) {
//						act.actionPulse=action.substring(action.indexOf(".")).substring(1).trim(); ///actionPulse: turn_bulb)on
//						act.device=action.substring(0, action.indexOf("."));     /////device: Bulb_0
//					}else {
//						act.actionPulse=action;
//					}
//					for(DeviceDetail device:devices) {
//						if(device.getDeviceName().equals(act.device)) {
//							for(String[] stateActionValue:device.getDeviceType().stateActionValues) {
//								if(stateActionValue[1].equals(act.actionPulse)) {
//									act.toState=stateActionValue[0];    //////找到这个action对应的状态
//									act.value=stateActionValue[2];      //////和标识符的取值
//									break;
//								}
//							}
//							break;
//						}
//					}
//					for(Rule otherRule:rules) {
//						for(String oAction:otherRule.getAction()) {
//							////////获得有这个action的所有规则
//							if(oAction.equals(act.action)) {
//								act.rules.add(otherRule);
//								break;
//							}
//						}
//					}
//					actions.add(act);
//				}
//
//			}
//		}
//		return actions;
//	}
	


	////解析trigger的格式 attribute<(<=,>,>=)value or instance.state
	// / time<30      instance.state for 3  时间值
	public static String[] getTriggerForm(String triggerContent){
		String[] triggerForm=new String[3];
		if(triggerContent.contains(">=")) {
			triggerForm[1]=">=";
			triggerForm[0]=triggerContent.substring(0,triggerContent.indexOf(">=")).trim();
			triggerForm[2]=triggerContent.substring(triggerContent.indexOf(">=")).substring(2).trim();
		}else if(triggerContent.contains(">")) {
			triggerForm[1]=">";
			triggerForm[0]=triggerContent.substring(0, triggerContent.indexOf(">")).trim();
			triggerForm[2]=triggerContent.substring(triggerContent.indexOf(">")).substring(1).trim();
		}else if(triggerContent.contains("<=")) {
			triggerForm[1]="<=";
			triggerForm[0]=triggerContent.substring(0,triggerContent.indexOf("<=")).trim();
			triggerForm[2]=triggerContent.substring(triggerContent.indexOf("<=")).substring(2).trim();
		}else if(triggerContent.contains("<")) {
			triggerForm[1]="<";
			triggerForm[0]=triggerContent.substring(0, triggerContent.indexOf("<")).trim();
			triggerForm[2]=triggerContent.substring(triggerContent.indexOf("<")).substring(1).trim();
		}else if(triggerContent.contains("=")) {
			triggerForm[1]="=";
			triggerForm[0]=triggerContent.substring(0,triggerContent.indexOf("=")).trim();
			triggerForm[2]=triggerContent.substring(triggerContent.indexOf("=")).substring(1).trim();
		}else if(triggerContent.contains(".")) {
			//////表示实体状态  Bulb_0.bon   Person.lobby
			triggerForm[1]=".";
			triggerForm[0]=triggerContent.substring(0, triggerContent.indexOf(".")).trim();
			triggerForm[2]=triggerContent.substring(triggerContent.indexOf(".")).substring(1).trim();
		}
		return triggerForm;
	}
	
	
	
	///////////////////////////从字符串获得所有规则//////////////////////////////////
	public static List<Rule> getRuleList(String ruleTxt){
		List<String> ruleStrList=new ArrayList<String>();
		int changeLine=0;
		if(ruleTxt.indexOf("\r\n")>0) {
			changeLine=1;
		}else if(ruleTxt.indexOf("\n")>0) {
			changeLine=2;
		}
		//////////////////////换行了才能算///////////////////////
		List<String> strList=new ArrayList<String>();
		if(changeLine==1) {
			strList=Arrays.asList(ruleTxt.split("\r\n"));
		}else if(changeLine==2) {
			strList=Arrays.asList(ruleTxt.split("\n"));
		}
		for(String str:strList) {
			if(str!=null && str.toUpperCase().indexOf("IF ")>=0 &&
					str.toUpperCase().indexOf(" THEN ")>0) {
				ruleStrList.add(str);
			}
		}
		List<Rule> rules=new ArrayList<Rule>();
		for(int i=0;i<ruleStrList.size();i++) {
			String ruleStr=ruleStrList.get(i);
			Rule rule=getRule(ruleStr, i+1);
			if(rule!=null) {
				rules.add(rule);
			}			
		}
		return rules;
	}
	
	public List<Rule> getRuleListFromTxt(String path){
		List<String> ruleStrList=new ArrayList<String>();
		List<Rule> rules=new ArrayList<Rule>();
		try {
			FileReader fr=new FileReader(path);
			BufferedReader br=new BufferedReader(fr);
			
			String str="";
			while((str=br.readLine())!=null) {
				if(str!=null && str.toUpperCase().indexOf("IF ")>=0 &&
						str.toUpperCase().indexOf(" THEN ")>0) {
					ruleStrList.add(str);
				}
			}
			
			br.close();
			fr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		for(int i=0;i<ruleStrList.size();i++) {
			String ruleStr=ruleStrList.get(i);
			Rule rule=getRule(ruleStr, i+1);
			if(rule!=null) {
				rules.add(rule);
			}			
		}		
		return rules;
	}
	
	
	

	
	
}
