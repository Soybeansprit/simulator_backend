package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 记录当前规则能触发的其他规则，与前驱规则相反
 * */
public class RuleAndTriggeredRule {
    private String currentRule="";  //当前规则名
    private List<String> triggeredRules=new ArrayList<>();  ///触发的其他规则
    private List<String> canBeTriggeredByRules=new ArrayList<>();  ///能触发该规则的其他规则

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
