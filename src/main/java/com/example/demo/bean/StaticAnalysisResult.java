package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态分析结果
 * 不可触发规则
 * 冗余规则
 * 规则不完整，即哪些设备被开启后，没有相应的关闭的规则
 * 循环规则
 * */
public class StaticAnalysisResult {
    private List<UnusedRuleAndReason> unusedRuleAndReasons=new ArrayList<>();  ///不可触发规则
    private List<List<Rule>> redundantRules=new ArrayList<>();  ///冗余规则
    private List<DeviceInstance> cannotOffDevices=new ArrayList<>();  ///规则不完整，能被开启却不能被关闭的设备
    private List<Rule> usableRules=new ArrayList<>(); ///规则有错误的，写错的
    private List<List<Rule>> loopRules=new ArrayList<>();  ///循环规则

    public List<UnusedRuleAndReason> getUnusedRuleAndReasons() {
        return unusedRuleAndReasons;
    }

    public void setUnusedRuleAndReasons(List<UnusedRuleAndReason> unusedRuleAndReasons) {
        this.unusedRuleAndReasons = unusedRuleAndReasons;
    }

    public List<List<Rule>> getRedundantRules() {
        return redundantRules;
    }

    public void setRedundantRules(List<List<Rule>> redundantRules) {
        this.redundantRules = redundantRules;
    }

    public List<DeviceInstance> getCannotOffDevices() {
        return cannotOffDevices;
    }

    public void setCannotOffDevices(List<DeviceInstance> cannotOffDevices) {
        this.cannotOffDevices = cannotOffDevices;
    }

    public List<Rule> getUsableRules() {
        return usableRules;
    }

    public void setUsableRules(List<Rule> usableRules) {
        this.usableRules = usableRules;
    }

    public List<List<Rule>> getLoopRules() {
        return loopRules;
    }

    public void setLoopRules(List<List<Rule>> loopRules) {
        this.loopRules = loopRules;
    }
}
