package com.example.demo.bean;

public class UnusedRuleAndReason {
    private Rule unusedRule=new Rule();
    private String reason="";

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
