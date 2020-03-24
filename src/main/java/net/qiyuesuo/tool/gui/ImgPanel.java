package net.qiyuesuo.tool.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ImgPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private int page;
	private BufferedImage bufferedImage;
	private int realWidth;
	private int realHeight;

	public ImgPanel(int page, BufferedImage bufferedImage) {
		super();
		this.page = page;
		this.bufferedImage = bufferedImage;
		this.realWidth = CompSize.IMAGE_WIDTH;
		this.realHeight = bufferedImage.getHeight() * CompSize.IMAGE_WIDTH / bufferedImage.getWidth();
		this.setSize(realWidth, realHeight);
		this.setPreferredSize(new Dimension(realWidth, realHeight));
		this.setBounds(0, 0, realWidth, realHeight);
	}

	@Override
	protected void paintComponent(Graphics g) {
		// 清除控件显示，在下次重新绘制的时候清除当前显示，不然会出现图片重叠现象
		g.clearRect(0, 0, realWidth, realHeight);
		g.drawImage(bufferedImage, 0, 0, realWidth, realHeight, null);
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public void setBufferedImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}

	public int getRealWidth() {
		return realWidth;
	}

	public void setRealWidth(int realWidth) {
		this.realWidth = realWidth;
	}

	public int getRealHeight() {
		return realHeight;
	}

	public void setRealHeight(int realHeight) {
		this.realHeight = realHeight;
	}

}
