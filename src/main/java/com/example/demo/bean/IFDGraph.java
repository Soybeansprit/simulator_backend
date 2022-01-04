package com.example.demo.bean;

import java.util.ArrayList;
import java.util.List;

public class IFDGraph {
	public static class GraphNode {
		////实体节点不用管

		private String name="";  ///////节点名 trigger1
		private String shape=""; ///////节点形状 oval
		private String fillcolor=""; ////节点填充色 
		private String label="";  //////节点内容 
		public boolean flag=false; ///用于表示是否遍历过
		private List<GraphNodeArrow> pNodeList=new ArrayList<GraphNodeArrow>(); ////指向该节点的边的列表
		private List<GraphNodeArrow> cNodeList=new ArrayList<GraphNodeArrow>();	///该节点出发的边的裂变

		private String[] relatedInstanceAndColor=new String[2];   ///如果是trigger或者action，会涉及到相应的实例，添加实例名以及节点的颜色
		private boolean isTraversed=false;  ///表示是否遍历过
		private List<GraphNodeArrow> pNodeArrowList=new ArrayList<>();   ////指向该节点的边的列表
		private List<GraphNodeArrow> cNodeArrowList=new ArrayList<>();   ///该节点出发的边的裂变

		public GraphNode(String name, String shape, String fillcolor, String label) {
			this.name = name;
			this.shape = shape;
			this.fillcolor = fillcolor;
			this.label = label;
		}
		public GraphNode(){

		}

		public void addpNodeList(GraphNodeArrow pNodeList) {
			this.pNodeList.add(pNodeList);
		}
		
		public void addcNodeList(GraphNodeArrow cNodeList) {
			this.cNodeList.add(cNodeList);
		}


		public String getName() {
			return name;
		}


		public void setName(String name) {
			this.name = name;
		}


		public String getShape() {
			return shape;
		}


		public void setShape(String shape) {
			this.shape = shape;
		}


		public String getFillcolor() {
			return fillcolor;
		}


		public void setFillcolor(String fillcolor) {
			this.fillcolor = fillcolor;
		}


		public String getLabel() {
			return label;
		}

		public boolean isFlag() {
			return flag;
		}

		public void setFlag(boolean flag) {
			this.flag = flag;
		}

		public void setLabel(String label) {
			this.label = label;
		}


		public List<GraphNodeArrow> getpNodeList() {
			return pNodeList;
		}


		public void setpNodeList(List<GraphNodeArrow> pNodeList) {
			this.pNodeList = pNodeList;
		}


		public List<GraphNodeArrow> getcNodeList() {
			return cNodeList;
		}

		public String[] getRelatedInstanceAndColor() {
			return relatedInstanceAndColor;
		}

		public void setRelatedInstanceAndColor(String[] relatedInstanceAndColor) {
			this.relatedInstanceAndColor = relatedInstanceAndColor;
		}

		public boolean isTraversed() {
			return isTraversed;
		}

		public void setTraversed(boolean traversed) {
			isTraversed = traversed;
		}

		public void setcNodeList(List<GraphNodeArrow> cNodeList) {
			this.cNodeList = cNodeList;
		}

		public List<GraphNodeArrow> getpNodeArrowList() {
			return pNodeArrowList;
		}

		public void setpNodeArrowList(List<GraphNodeArrow> pNodeArrowList) {
			this.pNodeArrowList = pNodeArrowList;
		}

		public List<GraphNodeArrow> getcNodeArrowList() {
			return cNodeArrowList;
		}

		public void setcNodeArrowList(List<GraphNodeArrow> cNodeArrowList) {
			this.cNodeArrowList = cNodeArrowList;
		}
	}
	
	public static class GraphNodeArrow {
		/////节点的边
		private GraphNode graphNode=new GraphNode();  ///边的另一边的节点
		private String label="";
		private String color="";
		private String style="";
		public GraphNode getGraphNode() {
			return graphNode;
		}
		public void setGraphNode(GraphNode graphNode) {
			this.graphNode = graphNode;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getColor() {
			return color;
		}
		public void setColor(String color) {
			this.color = color;
		}
		public String getStyle() {
			return style;
		}
		public void setStyle(String style) {
			this.style = style;
		}
	}


}
