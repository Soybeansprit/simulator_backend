package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.bean.Rule;

public class RandomRules {

	public static void main(String[] args) {
		String ruleStrList=" IF temperature<=15 THEN AirConditioner_0.turn_ac_heat\r\n" + 
				" IF temperature>=28 THEN AirConditioner_0.turn_ac_cool,Heater_0.turn_heat_off\r\n" + 
				" IF humidity<20 THEN Humidifier_0.turn_hum_on,Dehumidifier_0.turn_dehum_off\r\n" + 
				" IF Person.Location1 THEN Bulb_0.turn_bulb_on,Bulb_1.turn_bulb_on,Blind_1.open_blind,Blind_2.open_blind\r\n" + 
				" IF Person.Location1 THEN TV_0.turn_tv_on\r\n" + 
				" IF Person.Location2 THEN Bulb_2.turn_bulb_on,Bulb_3.turn_bulb_on,Blind_3.open_blind\r\n" + 
				" IF Person.Location4 THEN Bulb_6.turn_bulb_on,Bulb_7.turn_bulb_on,Blind_0.open_blind\r\n" + 
				" IF Person.Location3 THEN Bulb_4.turn_bulb_on,Bulb_5.turn_bulb_on\r\n" + 
				" IF Person.Location5 THEN Bulb_8.turn_bulb_on,Blind_4.open_blind\r\n" + 
				" IF Person.Out THEN Bulb_0.turn_bulb_off,Bulb_1.turn_bulb_off,Bulb_2.turn_bulb_off,Bulb_3.turn_bulb_off,Bulb_4.turn_bulb_off,Bulb_5.turn_bulb_off\r\n" + 
				" IF Person.Out THEN Blind_0.close_blind,Blind_1.close_blind,Blind_2.close_blind,Blind_3.close_blind\r\n" + 
				" IF Person.Out THEN AirConditioner_0.turn_ac_off,Humidifier_0.turn_hum_off,Fan_0.turn_fan_off,TV_0.turn_tv_off\r\n" + 
				" IF Rain.isRain THEN Blind_0.close_blind,Blind_1.close_blind,Blind_2.close_blind,Blind_3.close_blind\r\n" + 
				" IF AirConditioner_0.cool THEN Blind_0.close_blind,Blind_1.close_blind,Blind_2.close_blind,Blind_3.close_blind,Blind_4.close_blind\r\n" + 
				" IF AirConditioner_0.heat THEN Blind_0.close_blind,Blind_1.close_blind,Blind_2.close_blind,Blind_3.close_blind,Blind_4.close_blind\r\n" + 
				" IF Wind.Gale THEN Blind_3.close_blind\r\n" + 
				" IF Person.Out THEN Robot_0.dock_robot\r\n" + 
				" IF NOT_Person.Out  THEN Robot_0.start_robot\r\n" + 
				" IF humidity>50 THEN Humidifier_0.turn_hum_off\r\n" + 
				" IF Fire.OnFire THEN Alarm_0.turn_alarm_on\r\n" + 
				" IF Fire.NoFire THEN Alarm_0.turn_alarm_off\r\n" + 
				"  IF co2ppm>800 THEN AirPurifier_0.turn_ap_on\r\n" + 
				" IF pm_2_5>75 THEN AirPurifier_0.turn_ap_on\r\n" + 
				" IF aqi>=150 THEN Blind_0.close_blind,Blind_1.close_blind,Blind_2.close_blind,Blind_3.close_blind\r\n" + 
				" IF co2ppm<400 AND pm_2_5<20 THEN AirPurifier_0.turn_ap_off\r\n" + 
				" IF NOT_Person.Location1 THEN Bulb_0.turn_bulb_off,Bulb_1.turn_bulb_off,Blind_1.close_blind,Blind_2.close_blind\r\n" + 
				"  IF NOT_Person.Location1 THEN TV_0.turn_tv_off\r\n" + 
				" IF NOT_Person.Location2 THEN Bulb_2.turn_bulb_off,Bulb_3.turn_bulb_off\r\n" + 
				" IF NOT_Person.Location4 THEN Bulb_6.turn_bulb_off,Bulb_7.turn_bulb_off,Blind_0.close_blind\r\n" + 
				" IF NOT_Person.Location3 THEN Bulb_4.turn_bulb_off,Bulb_5.turn_bulb_off\r\n" + 
				" IF NOT_Person.Location5 THEN Bulb_8.turn_bulb_off\r\n" + 
				" IF NOT_Person.Out THEN Window_0.open_window,Window_1.open_window\r\n" + 
				" IF temperature<10 THEN Heater_0.turn_heat_on\r\n" + 
				" IF humidity>75 THEN Dehumidifier_0.turn_dehum_on,Humidifier_0.turn_hum_off\r\n" + 
				"";
		List<Rule> rules=RuleService.getRuleList(ruleStrList);
		getRandomRules(rules);
	}
	public static void getRandomRules(List<Rule> rules) {
		List<String> triggers=new ArrayList<String>();
		List<String> actions=new ArrayList<>();
		for(Rule rule:rules) {
			triggers.addAll(rule.getTrigger());
			actions.addAll(rule.getAction());
		}
		for(String trigger:triggers) {
			for(String action:actions) {
				System.out.printf("IF %s THEN %s%n",trigger,action);
			}
		}
	}
}
