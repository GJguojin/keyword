package net.qiyuesuo.tool.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	
	private CenterPanel center;
	private EastPanle east;
	
	
	private ComContext comContext =new ComContext();

	public MainFrame(String title) throws HeadlessException {
		super(title);
		comContext.setMainFrame(this);
	}
	
	public MainFrame init() {
		this.setSize(CompSize.MAIN_FRAME_WIDTH, CompSize.MAIN_FRAME_HEIGHT);
		this.setResizable( false );
		setLocationCenter(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setCenter(new CenterPanel(comContext));
		this.setEast(new EastPanle(comContext));
		this.setLayout(new BorderLayout(1,1));
		
		this.add(east, BorderLayout.EAST);
		this.add(center, BorderLayout.CENTER);
		addCenterScrollPane();

		return this;
	}
	private void addCenterScrollPane() {
		JScrollPane centerScrollPane = new JScrollPane(center);
		this.comContext.setCenterScrollPane(centerScrollPane);
		centerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		centerScrollPane.getVerticalScrollBar().setUnitIncrement(50);//鼠标滚动量
		this.add(centerScrollPane);
	}
	
	
	public static void setLocationCenter(Container container){
		Toolkit kit = Toolkit.getDefaultToolkit(); // 定义工具包
		Dimension screenSize = kit.getScreenSize(); // 获取屏幕的尺寸
		int screenWidth = screenSize.width / 2; // 获取屏幕的宽
		int screenHeight = screenSize.height / 2; // 获取屏幕的高
		int height = container.getHeight();
		int width = container.getWidth();
		container.setLocation( screenWidth - width / 2, screenHeight - height / 2 );
	}

	public CenterPanel getCenter() {
		return center;
	}

	public void setCenter(CenterPanel center) {
		this.center = center;
	}

	public EastPanle getEast() {
		return east;
	}

	public void setEast(EastPanle east) {
		this.east = east;
	}

}
