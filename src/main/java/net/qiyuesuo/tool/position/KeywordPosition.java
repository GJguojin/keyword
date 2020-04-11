package net.qiyuesuo.tool.position;

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
	
}
