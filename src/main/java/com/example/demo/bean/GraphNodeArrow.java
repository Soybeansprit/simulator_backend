package com.example.demo.bean;

public class GraphNodeArrow {
	private GraphNode graphNode=new GraphNode();
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
