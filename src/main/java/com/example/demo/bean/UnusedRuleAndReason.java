package com.example.demo.bean;
/**
 * 对于不可触发规则，有不可触发的原因
 * 可能是trigger矛盾
 * 或者是trigger不可达
 * */
public class UnusedRuleAndReason {
    private Rule unusedRule=new Rule();
    private String reason="";   ///不可触发原因

    public Rule getUnusedRule() {
        return unusedRule;
    }

    public void setUnusedRule(Rule unusedRule) {
        this.unusedRule = unusedRule;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
