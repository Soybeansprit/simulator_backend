package com.example.demo.bean;
/**
 * 人的实例
 * 人只有一个实例
 * */
public class HumanInstance extends Instance{
    private Human human=new Human();

    public Human getHuman() {
        return human;
    }

    public void setHuman(Human human) {
        this.human = human;
    }
}
