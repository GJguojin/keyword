package com.gj.tool.position;

import java.util.HashMap;
import java.util.Map;

import com.gj.tool.position.KeywordSearchOptions.PositionType;

/**
 * 签名位置
 */
public class KeywordPosition {

	private int page;				// 签名所在页码
	private double x;				// x坐标占比(相对于左下角)
	private double y;				// y坐标占比(相对于左下角)
	private double coordinateX;     // x坐标(相对于左下角)
	private double coordinateY;     // y坐标(相对于左下角)
	private double height = 841.92; //宽
	private double width = 595.32;  //高
	private PositionType positionType;
	private Map<PositionType,KeywordPosition> otherPositions = new HashMap<>();
	
	public KeywordPosition() {
		super();
	}
	
	public KeywordPosition(KeywordPosition position) {
		this.page = position.getPage();
		this.x = position.getX();
		this.y = position.getY();
		this.coordinateX = position.getCoordinateX();
		this.coordinateY = position.getCoordinateY(); 
		this.width = position.getWidth();
		this.height = position.getHeight();
	}

	public KeywordPosition(KeywordPosition position,PositionType positionType) {
		this(position);
		this.positionType = positionType;
	}
	
	public KeywordPosition getOtherPosition(PositionType otherPositionType) {
		if(positionType == null) {
			return null;
		}
		return otherPositions.get(otherPositionType);
	}
	
	public void putOtherPosition(PositionType otherPositionType,KeywordPosition otherPosition) {
		if(otherPositionType == null || otherPosition == null) {
			return;
		}
		otherPositions.put(otherPositionType, otherPosition);
	}
	
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getCoordinateX() {
		return coordinateX;
	}
	public void setCoordinateX(double coordinateX) {
		this.coordinateX = coordinateX;
	}
	public double getCoordinateY() {
		return coordinateY;
	}
	public void setCoordinateY(double coordinateY) {
		this.coordinateY = coordinateY;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public PositionType getPositionType() {
		return positionType;
	}

	public void setPositionType(PositionType positionType) {
		this.positionType = positionType;
	}

	public Map<PositionType, KeywordPosition> getOtherPositions() {
		return otherPositions;
	}

	public void setOtherPositions(Map<PositionType, KeywordPosition> otherPositions) {
		this.otherPositions = otherPositions;
	}
	
}
