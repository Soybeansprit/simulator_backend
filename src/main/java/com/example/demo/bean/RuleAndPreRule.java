package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 记录规则的前驱规则
 * */
public class RuleAndPreRule {
    private Rule currentRule=new Rule(); ///当前规则
    private List<RuleAndPreRule> preRules=new ArrayList<>();  ///前驱规则
    private boolean isCurrentTraversed=false;  ///表示当前rule是否遍历过

    public Rule getCurrentRule() {
        return currentRule;
    }

    public void setCurrentRule(Rule currentRule) {
        this.currentRule = currentRule;
    }

    public List<RuleAndPreRule> getPreRules() {
        return preRules;
    }

    public void setPreRules(List<RuleAndPreRule> preRules) {
        this.preRules = preRules;
    }

    public boolean isCurrentTraversed() {
        return isCurrentTraversed;
    }

    public void setCurrentTraversed(boolean currentTraversed) {
        isCurrentTraversed = currentTraversed;
    }
}
