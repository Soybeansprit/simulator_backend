package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * 自定义性质验证结果，要求性质不可达
 * 自定义性质为子状态的组合，P:= B & B & ...
 * 子状态 B:=[!] attribute<(<=,>,>=)value|instance.state
 * */
public class PropertyAnalysisResult {
    private String property="";   //性质内容
    private boolean reachable=false;   //是否可达，可达则不满足
    private List<Rule> relatedRules=new ArrayList<>();  //可能引发该性质不满足的相关TAP规则
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
