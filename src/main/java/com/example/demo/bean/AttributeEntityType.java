package com.example.demo.bean;


import java.util.ArrayList;
import java.util.List;

/**
 * 带各种环境属性的实体（air），只有一个实例
 * 实体类型名,【内容、属性名、属性总变化率变量名】
 * */
public class AttributeEntityType extends EntityType{
    private List<Attribute> attributes=new ArrayList<>();


    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public static class Attribute{
        public Attribute() {
        }
        /////各属性信息
        private String content="";  ///内容 temperature'==dtemper   更改了模型，添加了一个Attribute模型，用来表示各个属性的变化
        private String attribute="";  ////temperature
        private String delta="";  ////dtemper
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public String getAttribute() {
            return attribute;
        }
        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }
        public String getDelta() {
            return delta;
        }
        public void setDelta(String delta) {
            this.delta = delta;
        }
    }
}
