package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;
/**
 * uppaal上的模型类
 * 包括模型类、模型声明、模型参数、初始节点、节点及边
 * */
public class ModelGraph {
	//////表示uppaal模型的类
	public static class TemplGraph {
		private String name="";   /////模型名
		private String declaration="";   ///模型声明
		private String parameter="";  ///模型参数
		private String init="";   ///初始节点id
		private List<TemplGraphNode> templGraphNodes=new ArrayList<TemplGraphNode>();  ///各节点和边的关系
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDeclaration() {
			return declaration;
		}
		public void setDeclaration(String declaration) {
			this.declaration = declaration;
		}
		public String getParameter() {
			return parameter;
		}
		public void setParameter(String parameter) {
			this.parameter = parameter;
		}
		public String getInit() {
			return init;
		}
		public void setInit(String init) {
			this.init = init;
		}
		public List<TemplGraphNode> getTemplGraphNodes() {
			return templGraphNodes;
		}
		public void setTemplGraphNodes(List<TemplGraphNode> templGraphNodes) {
			this.templGraphNodes = templGraphNodes;
		}
		
		public void addTemplGraphNode(TemplGraphNode node) {
			this.templGraphNodes.add(node);
		}

	}
	
	/////节点类
	public static class TemplGraphNode {
		public String name="";  ///节点名
		public String id="";  ///节点id
		public String invariant="";  ///节点上的不变式
		public String style="";       //节点类型 location or branchpoint or committed  urgent?
		public boolean flag=false;   ///表示节点是否被访问过
		public List<TemplTransition> inTransitions=new ArrayList<TemplTransition>();  ///节点的入边
		public List<TemplTransition> outTransitions=new ArrayList<TemplTransition>();  ///节点的出边
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getInvariant() {
			return invariant;
		}
		public void setInvariant(String invariant) {
			this.invariant = invariant;
		}
		public String getStyle() {
			return style;
		}
		public void setStyle(String style) {
			this.style = style;
		}
		public boolean isFlag() {
			return flag;
		}
		public void setFlag(boolean flag) {
			this.flag = flag;
		}
		public List<TemplTransition> getInTransitions() {
			return inTransitions;
		}
		public void setInTransitions(List<TemplTransition> inTransitions) {
			this.inTransitions = inTransitions;
		}
		public List<TemplTransition> getOutTransitions() {
			return outTransitions;
		}
		public void setOutTransitions(List<TemplTransition> outTransitions) {
			this.outTransitions = outTransitions;
		}
	}
	
	/////边类
	public static class TemplTransition {
		public String source="";  ///源节点id
		public String target="";  ///目标节点id
		public TemplGraphNode node=new TemplGraphNode();  ///另一个节点
		public String assignment="";  ///边上的更新
		public String synchronisation=""; ///边上的同步信号
		public String guard="";  ///边上的卫式
		public String probability="";     //边上的权重，source or target中有branchpoint则有这一项
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getTarget() {
			return target;
		}
		public void setTarget(String target) {
			this.target = target;
		}
		public TemplGraphNode getNode() {
			return node;
		}
		public void setNode(TemplGraphNode node) {
			this.node = node;
		}
		public String getAssignment() {
			return assignment;
		}
		public void setAssignment(String assignment) {
			this.assignment = assignment;
		}
		public String getSynchronisation() {
			return synchronisation;
		}
		public void setSynchronisation(String synchronisation) {
			this.synchronisation = synchronisation;
		}
		public String getGuard() {
			return guard;
		}
		public void setGuard(String guard) {
			this.guard = guard;
		}
		public String getProbability() {
			return probability;
		}
		public void setProbability(String probability) {
			this.probability = probability;
		}
	}



}
