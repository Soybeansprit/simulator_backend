package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 自定义性质验证结果
 * */
public class PropertyAnalysisResult {
    private String property="";   //性质内容
    private boolean reachable=false;   //是否可达
    private List<Rule> relatedRules=new ArrayList<>();  //相关规则
    private List<String> addRuleContents=new ArrayList<>();  //建议添加的规则

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
