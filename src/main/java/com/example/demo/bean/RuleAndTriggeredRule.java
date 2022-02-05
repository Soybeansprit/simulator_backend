package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class RuleAndTriggeredRule {
    private String currentRule="";
    private List<String> triggeredRules=new ArrayList<>();
    private List<String> canBeTriggeredByRules=new ArrayList<>();

    public String getCurrentRule() {
        return currentRule;
    }

    public void setCurrentRule(String currentRule) {
        this.currentRule = currentRule;
    }

    public List<String> getTriggeredRules() {
        return triggeredRules;
    }

    public void setTriggeredRules(List<String> triggeredRules) {
        this.triggeredRules = triggeredRules;
    }

    public List<String> getCanBeTriggeredByRules() {
        return canBeTriggeredByRules;
    }

    public void setCanBeTriggeredByRules(List<String> canBeTriggeredByRules) {
        this.canBeTriggeredByRules = canBeTriggeredByRules;
    }
}
