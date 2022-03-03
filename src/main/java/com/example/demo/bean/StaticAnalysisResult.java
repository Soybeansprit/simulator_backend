package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class StaticAnalysisResult {
    private List<UnusedRuleAndReason> unusedRuleAndReasons=new ArrayList<>();
    private List<List<Rule>> redundantRules=new ArrayList<>();
    private List<DeviceInstance> cannotOffDevices=new ArrayList<>();
    private List<Rule> usableRules=new ArrayList<>();
    private List<List<Rule>> loopRules=new ArrayList<>();

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
