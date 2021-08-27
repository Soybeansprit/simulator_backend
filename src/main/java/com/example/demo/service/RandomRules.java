package com.example.demo.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.bean.Rule;

public class RandomRules {

	public static void main(String[] args) {
		String ruleStrList=" IF NOT_Person.Out AND temperature>=28 THEN AirConditioner_0.turn_ac_cool,Heater_0.turn_heat_off\r\n" + 
				" IF Person.Location1 AND temperature>25 THEN AirConditioner_0.turn_ac_cool\r\n" + 
				" IF Person.Location1 AND temperature<25 THEN AirConditioner_0.turn_ac_heat\r\n" + 
				" IF humidity<20 THEN Humidifier_0.turn_hum_on,Dehumidifier_0.turn_dehum_off\r\n" + 
				" IF Person.Location1 THEN Bulb_0.turn_bulb_on,Bulb_1.turn_bulb_on\r\n" + 
				" IF Person.Location1 THEN Window_1.open_window,Window_2.open_window\r\n" + 
				" IF Person.Location1 THEN TV_0.turn_tv_on\r\n" + 
				" IF TV_0.tvon THEN Bulb_0.turn_bulb_on,Bulb_1.turn_bulb_on\r\n" + 
				" IF Person.Location2 THEN Bulb_2.turn_bulb_on,Bulb_3.turn_bulb_on,Window_3.open_window\r\n" + 
				" IF Person.Location4 THEN Bulb_6.turn_bulb_on,Bulb_7.turn_bulb_on,Window_0.open_window\r\n" + 
				" IF Person.Location3 THEN Bulb_4.turn_bulb_on,Bulb_5.turn_bulb_on\r\n" + 
				" IF Person.Location5 THEN Bulb_8.turn_bulb_on,Window_4.open_window\r\n" + 
				" IF Person.Out THEN Bulb_0.turn_bulb_off,Bulb_1.turn_bulb_off,Bulb_2.turn_bulb_off,Bulb_3.turn_bulb_off,Bulb_4.turn_bulb_off,Bulb_5.turn_bulb_off\r\n" + 
				" IF Person.Out THEN Window_0.close_window,Window_1.close_window,Window_2.close_window,Window_3.close_window\r\n" + 
				" IF Person.Out THEN AirConditioner_0.turn_ac_off,Humidifier_0.turn_hum_off,Fan_0.turn_fan_off,TV_0.turn_tv_off\r\n" + 
				" IF Rain.isRain THEN Window_0.close_window,Window_1.close_window,Window_2.close_window,Window_3.close_window\r\n" + 
				" IF AirConditioner_0.cool THEN Window_0.close_window,Window_1.close_window,Window_2.close_window,Window_3.close_window,Window_4.close_window\r\n" + 
				" IF AirConditioner_0.heat THEN Window_0.close_window,Window_1.close_window,Window_2.close_window,Window_3.close_window,Window_4.close_window\r\n" + 
				" IF Wind.Gale THEN Window_3.close_window\r\n" + 
				" IF Person.Out THEN Robot_0.dock_robot\r\n" + 
				" IF NOT_Person.Out  THEN Robot_0.start_robot\r\n" + 
				" IF humidity>50 THEN Humidifier_0.turn_hum_off\r\n" + 
				" IF Fire.OnFire THEN Alarm_0.turn_alarm_on\r\n" + 
				" IF Fire.NoFire THEN Alarm_0.turn_alarm_off\r\n" + 
				" IF co2ppm>800 THEN AirPurifier_0.turn_ap_on\r\n" + 
				" IF pm_2_5>75 THEN AirPurifier_0.turn_ap_on\r\n" + 
				" IF aqi>=150 THEN Window_0.close_window,Window_1.close_window,Window_2.close_window,Window_3.close_window\r\n" + 
				" IF co2ppm<400 AND pm_2_5<20 THEN AirPurifier_0.turn_ap_off\r\n" + 
				" IF NOT_Person.Location1 THEN Bulb_0.turn_bulb_off,Bulb_1.turn_bulb_off,Window_1.close_window,Window_2.close_window\r\n" + 
				" IF NOT_Person.Location1 THEN TV_0.turn_tv_off\r\n" + 
				" IF NOT_Person.Location2 THEN Bulb_2.turn_bulb_off,Bulb_3.turn_bulb_off\r\n" + 
				" IF NOT_Person.Location4 THEN Bulb_6.turn_bulb_off,Bulb_7.turn_bulb_off,Window_0.close_window\r\n" + 
				" IF NOT_Person.Location3 THEN Bulb_4.turn_bulb_off,Bulb_5.turn_bulb_off\r\n" + 
				" IF NOT_Person.Location5 THEN Bulb_8.turn_bulb_off\r\n" + 
				" IF NOT_Person.Out THEN Blind_0.open_blind,Blind_1.open_blind\r\n" + 
				" IF temperature<10 THEN Heater_0.turn_heat_on\r\n" + 
				" IF humidity>75 THEN Dehumidifier_0.turn_dehum_on,Humidifier_0.turn_hum_off\r\n" + 
				" IF Door_0.dopen THEN Camera_0.turn_camera_on";
		List<Rule> rules=RuleService.getRuleList(ruleStrList);
		getRandomRules(rules);
	}
	public static void getRandomRules(List<Rule> rules) {
		List<String> triggers=new ArrayList<String>();
		List<String> actions=new ArrayList<>();
		for(Rule rule:rules) {
			for(String t:rule.getTrigger()) {
				if(triggers.contains(t)) {
					continue;
				}else {
					triggers.add(t);
				}
			}
			for(String a:rule.getAction()) {
				if(actions.contains(a)) {
					continue;
				}else {
					actions.add(a);
				}
			}
		}
		StringBuilder sb=new StringBuilder();
		File file=new File("D:\\rules.txt");
		try (FileWriter fw=new FileWriter(file);){
			int count=1;
			for(String action:actions) {
				for(String trigger:triggers) {
					sb.append(String.format("%d: IF %s THEN %s%n",count, trigger,action));
					System.out.printf("IF %s THEN %s%n",trigger,action);
					count++;
				}
			}
			fw.write(sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
