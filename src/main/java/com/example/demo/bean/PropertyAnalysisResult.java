package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class PropertyAnalysisResult {
    private String property="";
    private boolean reachable=false;
    private List<Rule> relatedRules=new ArrayList<>();
    private List<String> addRuleContents=new ArrayList<>();

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public List<Rule> getRelatedRules() {
        return relatedRules;
    }

    public void setRelatedRules(List<Rule> relatedRules) {
        this.relatedRules = relatedRules;
    }

    public List<String> getAddRuleContents() {
        return addRuleContents;
    }

    public void setAddRuleContents(List<String> addRuleContents) {
        this.addRuleContents = addRuleContents;
    }
}
